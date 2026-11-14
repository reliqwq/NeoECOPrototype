package cn.dancingsnow.neoecoprototype.event;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.wireless.WirelessChannelModuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.chat.Component;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.attachment.AttachmentType;
import net.neoforged.neoforge.attachment.IAttachmentHolder;
import net.neoforged.neoforge.attachment.IAttachmentSerializer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import javax.annotation.Nullable;

/**
 * Wireless edges between two network devices that are nowhere near each other.
 *
 * <p>The whole idea rests on one public AE2 call, {@link GridHelper#createConnection}: it puts an edge
 * between two nodes that share no space, which is a cable that costs zero blocks and zero cable length in
 * the pathfinding graph. Everything here is the bookkeeping that call does not come with - who remembers
 * the pair, who is allowed to build the edge, and what happens when one of the two ends goes to sleep.</p>
 *
 * <p>Two measurements taken with the throwaway probe block decided the shape of this class, and both are
 * load-bearing:</p>
 * <ul>
 *   <li>When a chunk unloads, AE2 takes the edge down with the node. The surviving end still held a live
 *       {@code IGridConnection} in its field, so the rebuild path has to <em>forget</em> the reference
 *       rather than call {@code destroy()} on a half-dead edge.</li>
 *   <li>Nothing re-built the edge after a reload while the pairing itself was still on disk. Every AE2
 *       rebuild hook we could have used lives inside the block entity, and these endpoints are other
 *       mods' block entities - so the rebuild has to be driven from outside, by a pass that only asks
 *       "are both ends awake now".</li>
 * </ul>
 *
 * <p>Storage is a level attachment, which is what keeps this off the list of things {@code AGENTS.md}
 * forbids: the records are held by the {@link ServerLevel} instance itself and saved into its
 * {@code neoforge_data_attachments}, not in a static table keyed by dimension.</p>
 *
 * <p><b>There is no channel price, allowance or budget here, and there was one until 2026-10-29.</b> It was
 * charged against a quantity AE2 does not have: with a controller online {@code PathingService} assigns
 * channels per path and keeps no network-wide pool (the only budget arithmetic in that class is the
 * controllerless branch, {@code channelsByBlocks = grid.size() x calculateAdHocChannels()}, which feeds power
 * usage rather than a cap), and neither {@code IGridNode} nor {@code IGridConnection} can be told to limit
 * channels - both {@code getMaxChannels()} answers have no setter. So the numbers drifted the way invented
 * ones do (nine taps on one controller row read {@code channels=0/128}), and the gate blocked setups that
 * work: a player's base is many small controllerless networks, and the advice the refusal gave - "tap an AE2
 * controller" - landed the credit on the network they were standing on rather than the one being refused
 * (three refusals in one client session, each {@code channels=0/0} with {@code controller=NO_CONTROLLER} on
 * both ends). What a link consumes is whatever AE2 decides to give the machines at its two ends, and the card
 * now reports that rather than rationing it: {@link #status} prints AE2's own mode, controller verdict and
 * per-face ledger. What is still ours to set is the shape of the link itself, and it lives in the server
 * config: the {@code wireless_max_links_per_device} edges a device may carry (a wireless edge costs no
 * blocks, so without it a player could click one machine into a hundred-port hub), its distance, whether it
 * crosses dimensions, and the AE/t it draws - {@link #billNow} is that last one.</p>
 */
public final class WirelessLinks {

    /**
     * The timed pass is only a fallback - the moments a link actually has to be re-established are the
     * player's click (handled synchronously) and a chunk waking up (handled by {@code ChunkEvent.Load},
     * see {@link #rebuildAround}). Nothing else needs it, so it is deliberately slow: five seconds between
     * sweeps, and each sweep walks only the links this dimension signed up for.
     */
    private static final int SWEEP_TICKS = 100;

    /**
     * How long to wait before asking AE2 for the same edge a second time. One accepted
     * {@code createConnection} costs two grid propagations plus a repath of the merged network (measured in
     * {@code appeng.me.GridConnection#mergeGrids} and the {@code repath()} that follows it), so a pair that
     * keeps dropping its edge must not be rebuilt on every pass.
     */
    private static final int REBUILD_COOLDOWN_TICKS = 100;

    /** Consecutive refusals before we stop asking and say so once, instead of filling the log forever. */
    private static final int MAX_REBUILD_ATTEMPTS = 5;

    private static final String TAG_LINKS = "links";
    private static final String TAG_A = "a";
    private static final String TAG_B = "b";
    private static final String TAG_DIM = "dim";
    private static final String TAG_POS = "pos";
    private static final String TAG_GROUP = "group";

    private WirelessLinks() {
    }

    /**
     * A pair of devices joined without a cable. The two ends are sorted on the way in, because one edge
     * has to have exactly one identity: {@link GridHelper#createConnection} called twice for the same two
     * nodes puts two edges in the graph, and nothing downstream would ever notice.
     */
    public record Link(GlobalPos a, GlobalPos b) {

        public static Link of(GlobalPos x, GlobalPos y) {
            return compare(x, y) <= 0 ? new Link(x, y) : new Link(y, x);
        }

        public boolean touches(GlobalPos p) {
            return a.equals(p) || b.equals(p);
        }

        public GlobalPos other(GlobalPos p) {
            return a.equals(p) ? b : a;
        }

        /** One copy per record, kept by the dimension the first endpoint lives in. */
        public ResourceKey<Level> ownerDimension() {
            return a.dimension();
        }
    }

    public static String normalizeGroup(@Nullable String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.codePoints().filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append).toString().trim();
        int count = normalized.codePointCount(0, normalized.length());
        if (count > 32) {
            normalized = normalized.substring(0, normalized.offsetByCodePoints(0, 32));
        }
        return normalized;
    }

    public record LinkSummary(String group, GlobalPos a, GlobalPos b, boolean live) {
    }

    private static int compare(GlobalPos x, GlobalPos y) {
        int byDimension = x.dimension().location().compareTo(y.dimension().location());
        return byDimension != 0 ? byDimension : Long.compare(x.pos().asLong(), y.pos().asLong());
    }

    /** What a sweep pass can say about one endpoint without loading anything. */
    private enum Presence {
        /** The device is awake and answers as a node host. */
        AWAKE,
        /** Its chunk, or its whole dimension, is asleep. Nothing can be concluded. */
        ASLEEP,
        /** Its chunk is loaded and there is no network device there: the block was taken away. */
        GONE
    }

    /** The persisted half is the list of pairs; edges are ours only while both ends are up. */
    public static final class Data {
        final List<Link> links = new ArrayList<>();
        final Map<Link, String> groups = new HashMap<>();
        final Map<Link, IGridConnection> edges = new HashMap<>();
        /** Per-pair bookkeeping for the rebuild throttle; none of it is persisted. */
        final Map<Link, Long> lastAttemptTick = new HashMap<>();
        final Map<Link, Integer> rebuildFailures = new HashMap<>();
        final Set<Link> givenUp = new HashSet<>();
        /**
         * Links whose chunk just woke, and the game time their wake expires. Per link on purpose: a
         * burst must not put the whole table back on the clock, and a world generates chunks constantly.
         * Not persisted.
         */
        final Map<Link, Long> urgentUntil = new HashMap<>();
        /**
         * Links whose merge still has to be confirmed against AE2's own verdict, and the game time that
         * confirmation is due. Armed by a successful bind, cleared by {@link #tendVerifies}. Not persisted.
         */
        final Map<Link, Long> verifyUntil = new HashMap<>();
        /**
         * What the last timed pass charged for the links this dimension owns, so the card can print a number
         * that was actually taken rather than a rate. Not persisted: after a reload it is zero until the
         * first sweep, which is what "last bill" means.
         */
        Bill lastBill = Bill.NONE;

        public List<Link> links() {
            return List.copyOf(links);
        }

        public String group(Link link) {
            return groups.getOrDefault(link, "");
        }

        public boolean record(Link link, String groupName) {
            if (!links.add(link)) {
                return false;
            }
            setGroup(link, groupName);
            return true;
        }

        /**
         * Names an existing pair, or takes it out of its group when the name is empty. Separate from
         * {@link #record} because recording happens once per pair while a group can be changed any number of
         * times - the two were one call only while binding was the single writer.
         */
        public void setGroup(Link link, String groupName) {
            String group = normalizeGroup(groupName);
            if (group.isEmpty()) {
                groups.remove(link);
            } else {
                groups.put(link, group);
            }
        }

        public int linksInGroup(String groupName) {
            String group = normalizeGroup(groupName);
            int count = 0;
            for (Link link : links) {
                if (group.equals(group(link))) {
                    count++;
                }
            }
            return count;
        }

        public List<Link> removeGroup(String groupName) {
            String group = normalizeGroup(groupName);
            List<Link> removed = new ArrayList<>();
            for (Iterator<Link> it = links.iterator(); it.hasNext(); ) {
                Link link = it.next();
                if (!group.equals(group(link))) {
                    continue;
                }
                removed.add(link);
                drop(link);
                it.remove();
            }
            return List.copyOf(removed);
        }

        int linksAt(GlobalPos endpoint) {
            int count = 0;
            for (Link link : links) {
                if (link.touches(endpoint)) {
                    count++;
                }
            }
            return count;
        }

        /** Forgets everything we believed about one pair, record included. */
        void drop(Link link) {
            groups.remove(link);
            edges.remove(link);
            lastAttemptTick.remove(link);
            rebuildFailures.remove(link);
            givenUp.remove(link);
            urgentUntil.remove(link);
        }
    }

    /**
     * Hand-written NBT rather than a codec: the payload is a list of two field pairs and the whole point is
     * that reading a bad entry must not take the level down.
     */
    public static final IAttachmentSerializer<CompoundTag, Data> SERIALIZER =
            new IAttachmentSerializer<>() {
                @Override
                public Data read(IAttachmentHolder holder, CompoundTag tag, HolderLookup.Provider registries) {
                    Data data = new Data();
                    for (Tag entry : tag.getList(TAG_LINKS, Tag.TAG_COMPOUND)) {
                        if (entry instanceof CompoundTag pair) {
                            GlobalPos a = readEndpoint(pair.getCompound(TAG_A));
                            GlobalPos b = readEndpoint(pair.getCompound(TAG_B));
                            if (a != null && b != null && !a.equals(b)) {
                                Link link = Link.of(a, b);
                                data.record(link, pair.getString(TAG_GROUP));
                            }
                        }
                    }
                    return data;
                }

                @Override
                public CompoundTag write(Data data, HolderLookup.Provider registries) {
                    ListTag list = new ListTag();
                    for (Link link : data.links) {
                        CompoundTag pair = new CompoundTag();
                        pair.put(TAG_A, writeEndpoint(link.a()));
                        pair.put(TAG_B, writeEndpoint(link.b()));
                        String group = data.group(link);
                        if (!group.isEmpty()) {
                            pair.putString(TAG_GROUP, group);
                        }
                        list.add(pair);
                    }
                    CompoundTag tag = new CompoundTag();
                    tag.put(TAG_LINKS, list);
                    return tag;
                }
            };

    public static AttachmentType<Data> dataType() {
        return AttachmentType.builder(Data::new).serialize(SERIALIZER).build();
    }

    private static CompoundTag writeEndpoint(GlobalPos at) {
        CompoundTag tag = new CompoundTag();
        tag.putString(TAG_DIM, at.dimension().location().toString());
        tag.putLong(TAG_POS, at.pos().asLong());
        return tag;
    }

    private static GlobalPos readEndpoint(CompoundTag tag) {
        String dimension = tag.getString(TAG_DIM);
        if (dimension.isEmpty()) {
            return null;
        }
        // A corrupted save must drop the pair, not throw while the level attachment is being read.
        var location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            return null;
        }
        return GlobalPos.of(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location),
                BlockPos.of(tag.getLong(TAG_POS)));
    }

    // ==================== what the card asks for ====================

    /** True when the block at that position is something AE2 would run a network through. */
    public static boolean isDevice(ServerLevel acting, GlobalPos at) {
        return presence(acting, at) == Presence.AWAKE;
    }

    /**
     * True only when the square is loaded and demonstrably holds no network device. Deliberately narrower
     * than "not a device": an asleep chunk or an unloaded dimension answers false here, because a card may
     * not lose its mark just because nobody has looked at that dimension today (§6's "看不出" is not "没了").
     */
    public static boolean endpointIsGone(ServerLevel acting, GlobalPos at) {
        return presence(acting, at) == Presence.GONE;
    }

    /**
     * A reply the card can hand to a player.
     *
     * <p>Two renderings of one outcome. {@code english} is the fully written sentence and it never loses
     * information: it is what the log gets and what the guards read, so a reading taken from a session log
     * and a reading taken from a test failure are the same string. {@code langKey} + {@code args} is what the
     * player gets, which is the only reason a Chinese client reads Chinese. A reply with no key is a reply
     * that speaks English to a player who does not read it, and on 2026-11-04 that was the whole content of
     * every refusal the two cards made - the guarded test now scans the three card sources and fails on one.
     *
     * <p>Where the args are values a player can read as-is (a coordinate, a count, a mode name, a device
     * label) the {@code en_us} value, rendered through {@link #asComponent()}, rebuilds the {@code english}
     * sentence exactly, and the guard checks that on the replies it can trigger. Where the log sentence embeds
     * the whole {@link #status} instrument the player's arg is something shorter instead - the device's label
     * for {@code marked}, {@link #channelsText} for {@code marked_gone_remarked} - because a player should not
     * read a debug line; those two are deliberately outside the equality check.
     *
     * @param args what fills that key's {@code %s} holes, in order
     */
    public record Reply(String langKey, Object[] args, String english) {

        /**
         * A player-facing outcome. {@code name} is the last part of
         * {@code gui.neoecoprototype.wireless_link.<name>}, which both lang files must carry.
         */
        public static Reply of(String name, String english, Object... args) {
            return new Reply("gui.neoecoprototype.wireless_link." + name, args, english);
        }

        /** The component the card sends: the key unrendered, so the client writes it in its own language. */
        public Component asComponent() {
            return Component.translatable(langKey, args);
        }
    }

    /**
     * Puts a pair into the record and tries the edge right away, so the player gets the reading in the same
     * click that made the link.
     */
    public static String bind(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos from, GlobalPos to) {
        return bindReply(acting, type, from, to).english();
    }

    /** {@link #bind} as the card needs it: the same work, plus the key each outcome speaks in. */
    public static Reply bindReply(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos from,
                                  GlobalPos to) {
        return bindReply(acting, type, from, to, "");
    }

    public static Reply bindReply(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos from,
                                  GlobalPos to, String groupName) {
        String group = normalizeGroup(groupName);
        if (from.equals(to)) {
            return Reply.of("same_block", "both ends are the same block");
        }
        int maxLinks = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .WIRELESS_MAX_LINKS_PER_DEVICE.get();
        boolean sameWorld = from.dimension().equals(to.dimension());
        if (!sameWorld && !cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .WIRELESS_ALLOW_CROSS_DIMENSION.get()) {
            return Reply.of("cross_dim_off", "these two are in different dimensions ("
                    + from.dimension().location() + " and " + to.dimension().location()
                    + "), and wireless_allow_cross_dimension is off in this world's config - no cable can do"
                    + " this either, so the switch keeps it a deliberate choice rather than a default",
                    from.dimension().location().toString(), to.dimension().location().toString());
        }
        int rangeBlocks = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .WIRELESS_MAX_RANGE_BLOCKS.get();
        if (rangeBlocks > 0 && sameWorld) {
            long dx = from.pos().getX() - to.pos().getX();
            long dy = from.pos().getY() - to.pos().getY();
            long dz = from.pos().getZ() - to.pos().getZ();
            long apart = Math.round(Math.sqrt((double) (dx * dx + dy * dy + dz * dz)));
            if (apart > rangeBlocks) {
                return Reply.of("out_of_range", "these two are " + apart + " blocks apart and"
                                + " wireless_max_range_blocks is " + rangeBlocks + " in this world's config -"
                                + " move one closer, or raise the number if you meant a link to reach that far",
                        apart, rangeBlocks);
            }
        }
        Link link = Link.of(from, to);
        ServerLevel owner = levelOf(acting, link.ownerDimension());
        if (owner == null) {
            return Reply.of("owner_dim_unloaded", "the dimension that would own this link is not loaded");
        }
        Data data = owner.getData(type.get());
        if (data.links.contains(link)) {
            return Reply.of("already_linked", "already linked " + describeLabelled(acting, link),
                    endLabel(acting, link, true), endLabel(acting, link, false));
        }
        if (!presenceIsAwake(owner, link.a()) || !presenceIsAwake(owner, link.b())) {
            GlobalPos dead = presence(owner, link.a()) == Presence.AWAKE ? link.b() : link.a();
            return Reply.of("not_a_device",
                    "that is not a network device: " + labelText(acting, dead), label(acting, dead));
        }
        int onA = linksAtEverywhere(owner, type, link.a());
        if (maxLinks > 0 && onA >= maxLinks) {
            return Reply.of("at_limit", labelText(acting, link.a()) + " already carries " + onA
                    + " links and this world's config caps a device at " + maxLinks
                    + " (wireless_max_links_per_device) - that number is a pack author's choice, not AE2's"
                    + " ceiling; set it to 0 and the only ceiling left is the channel width of the square"
                    + " itself",
                    label(acting, link.a()), onA, maxLinks);
        }
        int onB = linksAtEverywhere(owner, type, link.b());
        if (maxLinks > 0 && onB >= maxLinks) {
            return Reply.of("at_limit", labelText(acting, link.b()) + " already carries " + onB
                    + " links and this world's config caps a device at " + maxLinks
                    + " (wireless_max_links_per_device) - that number is a pack author's choice, not AE2's"
                    + " ceiling; set it to 0 and the only ceiling left is the channel width of the square"
                    + " itself",
                    label(acting, link.b()), onB, maxLinks);
        }
        IGridNode a = node(owner, link.a());
        IGridNode b = node(owner, link.b());
        if (a == null || b == null) {
            // Measured in the client: a block answers as a device the moment it is placed (that is what
            // let the card mark it), while AE2 only builds the node when that block first ticks - so this
            // is the one refusal worth saying how to get past.
            return noNodeYet(acting, a == null ? link.a() : link.b());
        }
        // A subsystem channel module is asked for one of its ports rather than for its own node, and this has
        // to happen here rather than in the rebuild pass: an edge created on the module node would not be
        // findable by landingNodes (which deliberately excludes that node), so the next sweep would read the
        // link as having no edge at all and build a second one onto a port.
        var moduleA = moduleOf(a);
        if (moduleA != null) {
            var port = moduleA.portForNewLink(farNodeCount(b));
            if (port == null) {
                // "No room on any port" and "not wired into a grid yet" are different answers, and only one of
                // them tells the player to build a second module.
                return moduleA.gridOnline() ? noFreePort(acting, link.a()) : noNodeYet(acting, link.a());
            }
            a = port;
        }
        var moduleB = moduleOf(b);
        if (moduleB != null) {
            // Counted from {@code a}, which is port 0 of A's grid by now - the same grid either way, so the
            // number is A's node count, which is exactly what B's port has to have room for.
            var port = moduleB.portForNewLink(farNodeCount(a));
            if (port == null) {
                return moduleB.gridOnline() ? noFreePort(acting, link.b()) : noNodeYet(acting, link.b());
            }
            b = port;
        }
        if (physicallyWiredTogether(a, b)) {
            return Reply.of("wired_already", "those two are already wired together with blocks: a wireless"
                    + " edge there would carry nothing, and unbinding it would tear out the connection you"
                    + " built");
        }
        if (a.getGrid() == b.getGrid()) {
            int nodes = gridNodes(a.getGrid());
            return Reply.of("one_network", "those two are already on one network of " + nodes
                            + " nodes, so a wireless edge between them would change nothing - they can already"
                            + " reach each other through the blocks you laid",
                    nodes);
        }
        // This pre-check is intentionally limited to two controllerless networks. AE2's own controller
        // validator follows physical controller-block connectivity; a virtual edge does not turn two separate
        // controller structures into one controller group (guarded by twoOnlineControllerPilesStayOnlineAcrossAWirelessEdge).
        // The remaining all-or-nothing case is controllerless ad-hoc capacity.
        Starvation risk = starvationRisk(a, b);
        if (risk != null) {
            return Reply.of("starvation", "these two networks together need " + risk.demand()
                    + " channel(s) and the smaller of their two ad-hoc ceilings is " + risk.fits()
                    + " (this side " + risk.ceilingA() + ", that side " + risk.ceilingB()
                    + "), and neither has an AE2 controller: the joined network would get zero channels and"
                    + " every device on it would read offline, so this link is refused - put an AE2 controller"
                    + " into one of them, or move some devices off one side first",
                    risk.demand(), risk.fits(), risk.ceilingA(), risk.ceilingB());
        }
        Full fullA = fullLanding(a);
        if (fullA != null) {
            return Reply.of("endpoint_full", labelText(acting, link.a()) + " is already carrying " + fullA.used()
                    + " of the " + fullA.max() + " channels it can carry, so a wireless edge into it would"
                    + " get nothing: that square is where the other network has to enter, and AE2 caps an"
                    + " entry at that number. Move the link onto a subsystem channel module, or onto a dense"
                    + " cable face, instead of onto this machine",
                    label(acting, link.a()), fullA.used(), fullA.max());
        }
        Full fullB = fullLanding(b);
        if (fullB != null) {
            return Reply.of("endpoint_full", labelText(acting, link.b()) + " is already carrying " + fullB.used()
                    + " of the " + fullB.max() + " channels it can carry, so a wireless edge into it would"
                    + " get nothing: that square is where the other network has to enter, and AE2 caps an"
                    + " entry at that number. Move the link onto a subsystem channel module, or onto a dense"
                    + " cable face, instead of onto this machine",
                    label(acting, link.b()), fullB.used(), fullB.max());
        }
        data.record(link, group);
        // No dirty flag to set: LevelAttachmentsSavedData.isDirty() answers true unconditionally, so a
        // level save always re-reads whatever this object currently holds.
        EdgeResult result = ensureEdge(owner, data, link);
        if (result.edge() != null) {
            data.verifyUntil.put(link, owner.getGameTime() + MERGE_VERIFY_TICKS);
            // No promise here about the Network Tool: measured in the client on 2026-10-30, reopening the tool
            // after a join still showed nothing, so the retired-grid object (batch wireless_tool_stalegrid) is
            // a real effect of a merge but not the whole of what he sees. The status line carries AE2's own
            // machine table below, which is what lets a player compare the tool against the source.
            return Reply.of("linked", "linked " + describeLabelled(acting, link) + " | "
                            + readings(result.edge(), result.a(), result.b()),
                    endLabel(acting, link, true), endLabel(acting, link, false),
                    channelsTextOf(tappedNode(acting, link, from, result),
                            from.equals(link.a()) ? link.a() : link.b()));
        }
        if (result.missingNode() != null) {
            GlobalPos missing = result.missingNode();
            return Reply.of("recorded_no_node", "recorded but no network node at " + labelText(acting, missing)
                    + " - a block counts as a device the moment it is placed, while AE2 only builds its"
                    + " node when that block first ticks, so binding it again in a moment should work",
                    label(acting, missing));
        }
        return Reply.of("recorded_no_edge",
                "recorded " + describeLabelled(acting, link) + " but no edge was made (see log)",
                endLabel(acting, link, true), endLabel(acting, link, false));
    }

    /**
     * True when that position holds an AE2 controller - the block whose tap moves the network's channel
     * mode. Deliberately only the block question: whether that controller is part of a group AE2 actually
     * accepts is answered by {@link #controllerState}, and the card needs to route a conflicted pile into
     * the message that says so rather than into the device branch.
     */
    public static boolean isController(ServerLevel acting, GlobalPos at) {
        if (!at.dimension().equals(acting.dimension()) || !chunkIsHere(acting, at)) {
            return false;
        }
        return acting.getBlockState(at.pos()).getBlock()
                == appeng.core.definitions.AEBlocks.CONTROLLER.block();
    }

    /**
     * Tapping a controller pushes that network one AE2 channel mode up - DEFAULT to X2, X2 to X3, X3 to X4 -
     * and stops there. This is the whole content of the controller tap: every tap moves one step, so the
     * card has no purchase, no credit and nothing to re-read about a previous tap.
     *
     * <p>It is the only <em>per-network</em> lever that exists, and it is reachable only where a controller
     * already is. Measured on 19.2.17: {@code GridNode.getMaxChannels()}
     * answers {@code 8 x cableCapacityFactor} for a node without {@code DENSE_CAPACITY} and both our
     * machines and eco's carry no such flag (eco's {@code NEBlockEntity} constructor sets exactly
     * {@code MULTIBLOCK, REQUIRE_CHANNEL}), so a wireless edge crossing one of our blocks tops out at 8 -
     * confirmed live on 2026-10-29: nine interfaces behind one link charged 8 and the ninth got nothing,
     * while the same rig under X2 charged all nine.
     *
     * <p>Do not "fix" that by hanging this call on a device tap. Every card gesture in the field is already
     * a sneak one - the shipped tooltip says sneak-right-click to mark and sneak-right-click to join, because
     * a plain tap on a device is answered by the device's own GUI before the item is ever asked - so a sneak
     * discriminator would take marking away rather than add a knob. {@code raiseMode} itself asks only for a
     * node on a grid (the ad-hoc guard has always driven it through a device), which is why the advice line in
     * {@code adHocWords} names the two levers a controllerless player really has instead of this one.
     *
     * <p>Never down. The card cannot tell a mode we forced from one the player set in
     * {@code ae2-common.toml} - {@code channelModeLocked} is private and the public reader returns the
     * effective value - so lowering would silently overwrite somebody's config. And {@code INFINITE} is
     * deliberately unreachable here: it answers {@code Integer.MAX_VALUE} for every node, which does not
     * raise a channel budget, it deletes the concept.
     */
    public static String raiseMode(ServerLevel acting, GlobalPos at) {
        return raiseModeReply(acting, at).english();
    }

    /** {@link #raiseMode} as the card needs it: the same ladder, plus the key each answer speaks in. */
    public static Reply raiseModeReply(ServerLevel acting, GlobalPos at) {
        ServerLevel owner = levelOf(acting, at.dimension());
        if (owner == null) {
            return Reply.of("dim_unloaded", "that dimension is not loaded");
        }
        var node = node(owner, at);
        if (node == null || node.getGrid() == null) {
            return Reply.of("not_on_network", labelText(acting, at)
                    + " is not on a network yet, so there is no channel mode to raise", label(acting, at));
        }
        var pathing = node.getGrid().getService(appeng.api.networking.pathing.IPathingService.class);
        if (!(pathing instanceof appeng.me.service.PathingService service)) {
            return Reply.of("no_pathing_service", "AE2 no longer hands a pathing service this can set ("
                    + pathing + "), so the card cannot raise a mode", String.valueOf(pathing));
        }
        var current = service.getChannelMode();
        var next = switch (current) {
            case DEFAULT -> appeng.api.networking.pathing.ChannelMode.X2;
            case X2 -> appeng.api.networking.pathing.ChannelMode.X3;
            case X3 -> appeng.api.networking.pathing.ChannelMode.X4;
            default -> null;
        };
        if (next == null) {
            return Reply.of("already_top", "already at " + current.name()
                    + ", the highest mode this card offers", current.name());
        }
        service.setForcedChannelMode(next);
        // Stated as AE2's own arithmetic rather than read off the block that was tapped: a controller's node
        // answers getMaxChannels() == 0 because it carries channels rather than using them, so "this node
        // reads max=0" was a true fact about the wrong object and read like the raise had failed. The ladder
        // guard asserts the same number straight off a device node, so the sentence and the test agree.
        return Reply.of("raised", "raised this network to " + next.name() + ": a device without dense"
                        + " capacity now takes " + 8 * next.getCableCapacityFactor() + " channels per face (8 x "
                        + next.getCableCapacityFactor() + ")",
                next.name(), 8 * next.getCableCapacityFactor(), next.getCableCapacityFactor());
    }

    /** AE2's controller verdict for a position's network, spelled out because the card has to say it back. */
    public static String controllerState(ServerLevel acting, GlobalPos at) {
        var node = node(acting, at);
        return node == null ? "no network node there" : controllerStateOf(node.getGrid());
    }

    private static String controllerStateOf(IGrid grid) {
        var pathing = grid == null ? null : grid.getPathingService();
        return pathing == null ? "unreadable pathing service" : String.valueOf(pathing.getControllerState());
    }

    /**
     * How many nodes AE2 counts as belonging to that network. Said out loud because "they are already on one
     * network" is not actionable by itself: a player who marked two machines that look unrelated needs the
     * size of the thing that already joins them - and the number also separates a real answer from the old
     * refusal, which was written to make a control group readable rather than to tell a player anything
     * (eight identical lines of it in one client session, 2026-10-29).
     *
     * <p>Two callers now, and the second one is load-bearing in a different way: {@code farNodeCount} feeds it
     * to {@code WirelessChannelModuleBlockEntity#portForNewLink}, where this count decides whether a network
     * shares a port's 32 channels with another network or gets a port to itself. It has to stay AE2's own node
     * count - not a count of the player's machines - because that is the number the port's arithmetic is
     * measured against.
     */
    private static int gridNodes(IGrid grid) {
        int nodes = 0;
        for (var unused : grid.getNodes()) {
            nodes++;
        }
        return nodes;
    }

    /**
     * Whether that network can power anything at all, because that is a different question from channels and
     * a player needs to know which of the two he is short of. Measured in {@code appeng.me.GridNode}:
     * {@code isPowered()} is {@code myGrid.getEnergyService().isNetworkPowered()} - an energy fact, not a
     * node fact - while {@code meetsChannelRequirements()} is {@code !hasFlag(REQUIRE_CHANNEL) ||
     * getUsedChannels() > 0}. AE2's own in-game tooltip (the class {@code igtooltip.GridNodeState}, which is
     * what Jade and The One Probe draw) turns exactly those two into "Device Offline" and "Device Missing
     * Channel", so a machine that reads offline is telling us the network has no ME power, and the card has
     * so far been silent about it.
     */
    private static String powerWords(IGrid grid) {
        var energy = grid == null ? null
                : grid.getService(appeng.api.networking.energy.IEnergyService.class);
        return energy == null ? "" : " powered=" + energy.isNetworkPowered();
    }

    /**
     * What AE2's controllerless ("ad-hoc") rules say about a network, in the two numbers a player can act on.
     * Measured on 19.2.17: {@code ChannelMode} carries {@code adHocNetworkChannels} - DEFAULT 8, X2 16,
     * X3 24, X4 32, INFINITE 2^31-1 - and {@code PathingService#calculateAdHocChannels} counts the nodes that
     * need channels against it; past the limit it stores {@code AdHocNetworkError.TOO_MANY_CHANNELS} and
     * returns 0, {@code updateControllerState} then writes {@code channelsInUse = channelsByBlocks = 0}, and
     * every device on the network reads as offline. Nothing about that is ours: the card can lift the limit
     * one step per controller tap, and a controller in the network leaves this branch entirely. The client
     * showed why the two numbers have to appear together (2026-10-29): {@code adHocLimit=8} alone reads as
     * nothing, while {@code adHocLimit=8 (this network holds 92 nodes)} is the whole diagnosis - X4 buys only
     * 32 slots, so a network that size cannot be raised out of this and needs a controller. The count is
     * {@code IGrid.getNodes()}, AE2's own total, not a recount of the nodes that need channels: that set is
     * private to {@code PathingService}, and standing in for it is exactly how the last invented number got
     * here.
     */
    private static String adHocWords(IGrid grid) {
        var pathing = grid == null ? null : grid.getPathingService();
        if (pathing == null || pathing.getControllerState()
                != appeng.api.networking.pathing.ControllerState.NO_CONTROLLER) {
            return "";
        }
        var words = " adHocLimit=" + pathing.getChannelMode().getAdHocNetworkChannels()
                + " (this network holds " + gridNodes(grid) + " nodes)";
        if (pathing instanceof appeng.me.service.PathingService service
                && service.getAdHocNetworkError() != null) {
            words += " adHocError=" + service.getAdHocNetworkError().name()
                    + " - AE2 has given this whole network zero channels, so every device on it reads"
                    + " offline: this network has no controller, so the card's raise gesture has nothing to"
                    + " tap - put an AE2 controller into it and this cap goes away entirely";
        }
        return words;
    }
    /**
     * Drops every link at one endpoint. The record may be filed under the other endpoint's dimension, so
     * every awake dimension is asked - cheap here, because this only ever runs on a click.
     */
    public static Reply unlinkReply(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        int dropped = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (!level.hasData(type.get())) {
                continue;
            }
            Data data = level.getData(type.get());
            for (Iterator<Link> it = data.links.iterator(); it.hasNext(); ) {
                Link link = it.next();
                if (!link.touches(endpoint)) {
                    continue;
                }
                // Only destroy an edge we can still prove is the one between these two nodes: the first
                // thing appeng.me.GridConnection#destroy() does is ask sideA for its grid, and a node that
                // already left the world answers null.
                IGridConnection held = data.edges.get(link);
                boolean provable = isStillBetween(level, held, link);
                NeoECOPrototype.LOGGER.info("wireless link: unlink {} held={} provable={} (a)={} (b)={}",
                        describe(link), held != null, provable, node(level, link.a()) != null,
                        node(level, link.b()) != null);
                if (provable) {
                    held.destroy();
                }
                data.drop(link);
                it.remove();
                dropped++;
            }
        }
        NeoECOPrototype.LOGGER.info("wireless link: unbind {} dropped={} link(s)", describe(endpoint), dropped);
        return dropped == 0
                ? Reply.of("carries_none", labelText(acting, endpoint) + " carries no wireless link",
                        label(acting, endpoint))
                : Reply.of("dropped", "dropped " + dropped + " link(s) at " + labelText(acting, endpoint),
                        dropped, label(acting, endpoint));
    }

    /** Drops every link at one endpoint and answers in English - what the log and the guards read. */
    public static String unlink(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        return unlinkReply(acting, type, endpoint).english();
    }

    /**
     * How many wireless edges one device is in. A record is filed under the dimension its first endpoint
     * lives in, so a device whose neighbours are spread across dimensions is only counted correctly by
     * asking every awake dimension - which is affordable because this runs on a click, not on a tick.
     */
    public static int linksAtEverywhere(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        int count = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (level.hasData(type.get())) {
                count += level.getData(type.get()).linksAt(endpoint);
            }
        }
        return count;
    }

    /**
     * Puts every link that touches {@code endpoint} into one group, and answers how many pairs that changed.
     * A null endpoint means every recorded link, which is what the manager uses when the card holds no mark:
     * the screen lists the links it is about to rename, so "apply" over that list is the same promise as
     * "apply" over one device's links, and a player who never marked anything is not left clicking a button
     * that answers zero.
     *
     * <p>Answering a count rather than nothing is deliberate - zero is a real answer ("this device carries no
     * links"), and the caller says so instead of leaving it ambiguous.
     */
    public static int assignGroup(ServerLevel acting, Supplier<AttachmentType<Data>> type,
                                  @Nullable GlobalPos endpoint, String groupName) {
        String group = normalizeGroup(groupName);
        int moved = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (!level.hasData(type.get())) {
                continue;
            }
            Data data = level.getData(type.get());
            for (Link link : data.links()) {
                if (endpoint != null && !link.touches(endpoint)) {
                    continue;
                }
                if (!group.equals(data.group(link))) {
                    data.setGroup(link, group);
                    moved++;
                }
            }
        }
        return moved;
    }

    public static List<LinkSummary> groupOverview(ServerLevel acting, Supplier<AttachmentType<Data>> type) {
        List<LinkSummary> rows = new ArrayList<>();
        for (ServerLevel owner : acting.getServer().getAllLevels()) {
            if (!owner.hasData(type.get())) {
                continue;
            }
            Data data = owner.getData(type.get());
            for (Link link : data.links) {
                rows.add(new LinkSummary(data.group(link), link.a(), link.b(),
                        isStillBetween(owner, data.edges.get(link), link)));
            }
        }
        rows.sort(java.util.Comparator.comparing((LinkSummary row) -> groupLabel(row.group()), String.CASE_INSENSITIVE_ORDER)
                .thenComparing(row -> describe(row.a()) + describe(row.b()), String.CASE_INSENSITIVE_ORDER));
        return List.copyOf(rows);
    }

    public static Reply unlinkGroupReply(ServerLevel acting, Supplier<AttachmentType<Data>> type,
                                         String groupName) {
        String group = normalizeGroup(groupName);
        if (group.isEmpty()) {
            return Reply.of("group_required", "enter a named group before unlinking it");
        }
        int removed = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (!level.hasData(type.get())) {
                continue;
            }
            Data data = level.getData(type.get());
            List<Link> grouped = data.links().stream().filter(link -> group.equals(data.group(link))).toList();
            for (Link link : grouped) {
                IGridConnection held = data.edges.get(link);
                if (isStillBetween(level, held, link)) {
                    held.destroy();
                }
            }
            removed += data.removeGroup(group).size();
        }
        NeoECOPrototype.LOGGER.info("wireless link: group unlink '{}' removed={} link(s)", groupLabel(group), removed);
        return removed == 0
                ? Reply.of("group_empty", "wireless group '" + groupLabel(group) + "' has no links",
                        groupLabel(group))
                : Reply.of("group_unlinked", "wireless group '" + groupLabel(group) + "' removed " + removed
                        + " link(s)", groupLabel(group), removed);
    }

    public static String groupLabel(String groupName) {
        String group = normalizeGroup(groupName);
        return group.isEmpty() ? "(ungrouped)" : group;
    }

    /**
     * What a player holding a card wants to know about the block in front of them: how many links are
     * recorded there, how many of them are backed by an edge right now, and what AE2 itself says about the
     * network - its channel mode, its verdict on the controller group, and the channels every connected face
     * is worth against what it is using. The two link counts differ whenever an endpoint is asleep, and
     * saying both is the only way the card can admit that.
     * <p>This string is the instrument: it is what the log gets and what the guards read. The player's line is
     * {@link #channelsText}, which prints only the two channel numbers and goes through the lang files so it
     * arrives in the language the game is played in.
     */
    public static String status(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        var node = node(acting, endpoint);
        return describe(endpoint) + " block=" + blockId(acting, endpoint)
                + " wirelessLinks=" + linksAtEverywhere(acting, type, endpoint)
                + " live=" + liveLinksAt(acting, type, endpoint)
                // A module that has handed out every outlet has to read differently from a plain device with
                // no outlets at all: the two link counts are identical, and the difference is the whole answer.
                // Busy, not open: an unbound port stays open on purpose, so the open count would say 6/6 to a
                // player who has just unbound all six (see WirelessChannelModuleBlockEntity#busyPorts).
                + (node != null && node.getOwner() instanceof WirelessChannelModuleBlockEntity module
                        ? " ports=" + module.busyPorts() + "/" + WirelessChannelModuleBlockEntity.MAX_PORTS
                        : "")
                + " " + billWords(acting, type)
                + (node == null ? " (no network node there)" : " " + channelMode(node) + " controller="
                + controllerStateOf(node.getGrid()) + powerWords(node.getGrid())
                + adHocWords(node.getGrid()) + " " + faceLedger(node) + " " + toolTable(node.getGrid()));
    }

    /**
     * The one line a player gets about a device: the channel budget AE2 gives it and what is allocated right
     * now, as {@code 拥有 : 已分配}. Both numbers are AE2's own - {@code IGridNode.getMaxChannels()} and
     * {@code getUsedChannels()} are on the public interface, and the maximum already carries the network's
     * multiplier (measured on 19.2.17: a node without {@code DENSE_CAPACITY} answers {@code 8 x factor}, a
     * dense one {@code 32 x factor}, {@code ChannelMode.INFINITE} short-circuits both to
     * {@code Integer.MAX_VALUE}). That pair is the whole answer to "how many devices will this link actually
     * carry", which is why it replaced the seven-field sentence this card used to print on 2026-11-04: the
     * verdict, the power state and the device count were either not this device's business or already visible
     * in AE2's own panels, and a player asked for the two numbers and nothing else.
     *
     * <p>{@link #status} keeps printing all of it, because that string is the instrument the guards and the
     * session log read; this method is the player's view of the same tick.
     */
    public static Component channelsText(ServerLevel acting, GlobalPos at) {
        var node = node(acting, at);
        if (node == null) {
            return Component.translatable("gui.neoecoprototype.wireless_link.channels_none", describe(at));
        }
        return Component.translatable("gui.neoecoprototype.wireless_link.channels",
                node.getMaxChannels(), node.getUsedChannels());
    }

    /**
     * The pair the {@code linked} line reports: the device the player tapped, not whichever end sorts first.
     *
     * <p>Two readings on 2026-11-14 forced this. {@code Link.of} sorts the pair, so {@code link.a()} is the
     * smaller coordinate - a module at x=7 and a supplier at x=10 always reported the module, and the same two
     * blocks reported the supplier if it happened to sit west. The player tapped one of them and the sentence
     * talked about the other one half the time.
     *
     * <p>And on a module the honest number is the port's, not the main node's: the join is measured off
     * {@code [0=18/32/con=2, 1=18/32/con=2]} in the suite, while the main node keeps answering the ad-hoc-ish
     * count of the nodes behind it. The port is where the edge lands and where the channels are spent, so a
     * player asking "what did this link cost me" is asking about the port. A plain device has no port, and its
     * own node is the right answer there - measured as a stable {@code 8 : 1} on every supplier bind.
     */
    private static IGridNode tappedNode(ServerLevel acting, Link link, GlobalPos from, EdgeResult result) {
        var tapped = from.equals(link.a()) ? link.a() : link.b();
        var tappedNode = node(levelOf(acting, tapped.dimension()), tapped);
        if (moduleOf(tappedNode) != null) {
            // The edge's own ends are the two ports; take the one on the tapped module's grid.
            for (var end : new IGridNode[]{result.a(), result.b()}) {
                if (end != null && tappedNode != null && end.getGrid() == tappedNode.getGrid()) {
                    return end;
                }
            }
        }
        return tappedNode;
    }

    /** The {@code channels} line for a node already in hand; nothing in hand is the none-sentence. */
    private static Component channelsTextOf(@Nullable IGridNode node, GlobalPos fallback) {
        return node == null
                ? Component.translatable("gui.neoecoprototype.wireless_link.channels_none", describe(fallback))
                : Component.translatable("gui.neoecoprototype.wireless_link.channels",
                        node.getMaxChannels(), node.getUsedChannels());
    }

    /** The same two numbers as a plain pair, for a guard or a log line that has no component to send. */
    public static String channelsReading(ServerLevel acting, GlobalPos at) {
        var node = node(acting, at);
        return node == null
                ? describe(at) + " has no network node"
                : describe(at) + " channels " + node.getMaxChannels() + " allocated " + node.getUsedChannels();
    }

    /**
     * The machine list AE2's Network Tool draws, read off the very object the tool builds its packet from:
     * {@code NetworkStatus#fromGrid} is what {@code NetworkStatusMenu#broadcastChanges} serialises every 15
     * ticks, and this prints one entry per group the same way ({@code <item> x<count>}). Added on 2026-10-30
     * because the card claimed a join was visible in the tool and a client reading said it was not: this line
     * separates "the tool has nothing to draw" from "the tool has rows and drew none of them", which no amount
     * of reading AE2's code from here can decide.
     */
    private static String toolTable(appeng.api.networking.IGrid grid) {
        var groups = appeng.menu.me.networktool.NetworkStatus.fromGrid(grid).getGroupedMachines();
        var table = new StringBuilder("toolTable=").append(groups.size()).append(" groups [");
        for (var group : groups) {
            if (table.length() > "toolTable=0 groups [".length()) {
                table.append(", ");
            }
            table.append(group.getDisplay()).append(" x").append(group.getCount());
        }
        return table.append(']').toString();
    }

    /**
     * AE2's own ceiling for every face that is actually connected, read the only way its API allows:
     * {@code IGridConnection} exposes no maximum (only {@code getUsedChannels()} - the
     * {@code getMaxChannels()} that exists is on the impl class {@code appeng.me.GridConnection}), so a face
     * is worth the smaller of the two nodes' {@code getMaxChannels()}. That number already carries the
     * network's multiplier: measured on 19.2.17 a node without {@code DENSE_CAPACITY} answers
     * {@code 8 x cableCapacityFactor}, a dense one {@code 32 x factor}, and {@code ChannelMode.INFINITE}
     * short-circuits both to {@code Integer.MAX_VALUE}. Our machines carry no flags at all, so under
     * {@code channels = "DEFAULT"} every face of one of them reads 8.
     */
    private static String faceLedger(IGridNode node) {
        StringBuilder faces = new StringBuilder();
        for (var face : node.getInWorldConnections().entrySet()) {
            var other = face.getValue().getOtherSide(node);
            if (other == null) {
                continue;
            }
            if (faces.length() > 0) {
                faces.append(", ");
            }
            faces.append(face.getKey().name()).append(' ')
                    .append(Math.min(node.getMaxChannels(), other.getMaxChannels()))
                    .append('/').append(face.getValue().getUsedChannels());
        }
        return "edges=[" + faces + "]";
    }

    /** The mode AE2 is actually using for this grid - config, or whatever a previous raise locked in. */
    private static String channelMode(IGridNode node) {
        var grid = node.getGrid();
        var pathing = grid == null
                ? null : grid.getService(appeng.api.networking.pathing.IPathingService.class);
        var mode = pathing == null ? null : pathing.getChannelMode();
        return "mode=" + (mode == null ? "unknown" : mode.name());
    }


    private static int liveLinksAt(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        int live = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (!level.hasData(type.get())) {
                continue;
            }
            Data data = level.getData(type.get());
            for (Link link : data.links) {
                if (link.touches(endpoint) && isStillBetween(level, data.edges.get(link), link)) {
                    live++;
                }
            }
        }
        return live;
    }

    /**
     * The edge we currently believe we hold for this pair, or null. Public because the question "is the
     * record backed by a real edge right now" is the only way to tell a link that works from a link that
     * was recorded and silently refused.
     */
    @Nullable
    public static IGridConnection edgeOf(ServerLevel acting, Supplier<AttachmentType<Data>> type, Link link) {
        ServerLevel owner = levelOf(acting, link.ownerDimension());
        if (owner == null || !owner.hasData(type.get())) {
            return null;
        }
        return owner.getData(type.get()).edges.get(link);
    }

    /**
     * Drop only our reference to the edge - the edge itself stays in AE2's graph. This is the state a
     * rebuild pass has to survive: the pairing is on record, the cable is already in the graph, and the
     * pass must notice that rather than ask for a second one.
     */
    public static void forgetEdge(ServerLevel acting, Supplier<AttachmentType<Data>> type, Link link) {
        ServerLevel owner = levelOf(acting, link.ownerDimension());
        if (owner != null && owner.hasData(type.get())) {
            owner.getData(type.get()).edges.remove(link);
        }
    }

    /** Whether a chunk wake has handed this pair to the burst - pure in-memory, safe from any thread. */
    public static boolean isUrgent(ServerLevel acting, Supplier<AttachmentType<Data>> type, Link link) {
        return acting.hasData(type.get()) && acting.getData(type.get()).urgentUntil.containsKey(link);
    }

    /** The recorded pair for two endpoints, in the same canonical order the store uses. */
    public static Link linkOf(GlobalPos x, GlobalPos y) {
        return Link.of(x, y);
    }

    // ==================== the rebuild pass ====================

    public static boolean sweepDue(long gameTime) {
        return gameTime % SWEEP_TICKS == 0;
    }

    /** What one timed pass is worth in ticks - public because a bill has to be checkable against it. */
    public static int sweepIntervalTicks() {
        return SWEEP_TICKS;
    }

    /**
     * The only thing that can put a lost edge back: these endpoints belong to other mods' block entities,
     * and we have no hook inside them. Not a scan - it walks the links this dimension already signed up
     * for, and does nothing at all when the dimension has none.
     */
    public static void sweep(Level level, Supplier<AttachmentType<Data>> type) {
        if (!(level instanceof ServerLevel acting) || acting.isClientSide || !acting.hasData(type.get())) {
            return;
        }
        Data data = acting.getData(type.get());
        if (sweepDue(acting.getGameTime())) {
            rebuildNow(acting, type);
            billNow(acting, type);
        } else if (!data.urgentUntil.isEmpty()) {
            tendWakes(acting, data);
        }
        if (!data.verifyUntil.isEmpty()) {
            tendVerifies(acting, data);
        }
    }

    /**
     * What one pass over this dimension's live links took out of the networks that own them.
     *
     * @param charged AE actually extracted
     * @param billed  how many live links were charged for
     * @param unpaid  how many of those could not be paid for in full
     */
    public record Bill(double charged, int billed, int unpaid) {
        static final Bill NONE = new Bill(0.0D, 0, 0);
    }

    /**
     * The energy price of a link, billed once per timed pass. This is the whole reason the feature can be
     * turned on: a wireless edge crosses no cable, so on AE2's own books a hub of them costs nothing, and
     * the config's {@code wireless_ae_per_link_per_tick} is what makes it cost something.
     * <p>The network that owns the link's first end pays - after a merge there is exactly one grid across
     * both ends, so it does not matter which end is picked and there is no second bill to avoid.
     * <p>Charged through {@code IEnergyService.extractAEPower} rather than by registering an idle draw,
     * because there is no public way to set one: {@code IGridNode} exposes only the getter, and the setter
     * lives on the impl class {@code appeng.me.GridNode}, which a foreign mod's node need not be. Public
     * because a test (and the card's own reading) has to be able to ask for one pass right now.
     */
    public static Bill billNow(ServerLevel acting, Supplier<AttachmentType<Data>> type) {
        if (!acting.hasData(type.get())) {
            return Bill.NONE;
        }
        Data data = acting.getData(type.get());
        double rate = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .WIRELESS_AE_PER_LINK_PER_TICK.get();
        Bill previous = data.lastBill;
        data.lastBill = bill(acting, data, rate);
        if (billIsWorthAWord(previous, data.lastBill)) {
            // One line per dimension whenever the number moves, and none while it does not. Without this the
            // price is only visible in the card's status reading, and that runs on one gesture - so a player
            // who never thinks to make it has no way to find out that their links are being billed at all.
            String line = "wireless link: {} link(s) charged in {} at {} AE/t each, {} AE taken over the"
                    + " last {} ticks, {} could not be paid{}";
            Object[] args = {data.lastBill.billed(), acting.dimension().location(), rate,
                    Math.round(data.lastBill.charged() * 1000.0D) / 1000.0D, SWEEP_TICKS,
                    data.lastBill.unpaid(), data.lastBill.unpaid() > 0
                            ? " - AE2 takes a grid offline when its store runs dry, so this is the network"
                              + " losing power rather than a link being torn down" : ""};
            if (data.lastBill.unpaid() > 0) {
                NeoECOPrototype.LOGGER.warn(line, args);
            } else {
                NeoECOPrototype.LOGGER.info(line, args);
            }
        }
        return data.lastBill;
    }

    /**
     * Whether a bill deserves a line in the log: the count of billed links moved, the count that could not
     * pay did, or the amount taken did (a pack moving the rate keeps the counts identical and still has to
     * show). The pass runs every {@link #SWEEP_TICKS} for every dimension that holds a link, so a bill that
     * repeats itself says nothing new and must not repeat itself in the log either.
     */
    public static boolean billIsWorthAWord(Bill previous, Bill now) {
        return now.billed() != previous.billed() || now.unpaid() != previous.unpaid()
                || Double.compare(now.charged(), previous.charged()) != 0;
    }

    private static Bill bill(ServerLevel acting, Data data, double rate) {
        if (rate <= 0.0D || data.links.isEmpty()) {
            return Bill.NONE;
        }
        double asked = rate * SWEEP_TICKS;
        double charged = 0.0D;
        int billed = 0;
        int unpaid = 0;
        for (Link link : data.links) {
            if (!isStillBetween(acting, data.edges.get(link), link)) {
                continue;
            }
            billed++;
            var node = node(acting, link.a());
            var grid = node == null ? null : node.getGrid();
            var energy = grid == null
                    ? null : grid.getService(appeng.api.networking.energy.IEnergyService.class);
            if (energy == null) {
                unpaid++;
                continue;
            }
            // PowerMultiplier.CONFIG is not a lever: both of its constants are constructed with multiplier
            // 1.0 and nothing in AE2 19.2.17 assigns the field, so asking for ONE says the same thing.
            double got = energy.extractAEPower(asked, appeng.api.config.Actionable.MODULATE,
                    appeng.api.config.PowerMultiplier.ONE);
            charged += got;
            if (got + 1.0E-9 < asked) {
                unpaid++;
            }
        }
        return new Bill(charged, billed, unpaid);
    }

    /** The price in force and what the last pass actually collected, summed over every dimension. */
    private static String billWords(ServerLevel acting, Supplier<AttachmentType<Data>> type) {
        double rate = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .WIRELESS_AE_PER_LINK_PER_TICK.get();
        double charged = 0.0D;
        int billed = 0;
        int unpaid = 0;
        for (ServerLevel level : acting.getServer().getAllLevels()) {
            if (!level.hasData(type.get())) {
                continue;
            }
            Bill bill = level.getData(type.get()).lastBill;
            charged += bill.charged();
            billed += bill.billed();
            unpaid += bill.unpaid();
        }
        return "aePerLink=" + rate + " AE/t lastBill=" + Math.round(charged * 1000.0D) / 1000.0D
                + "AE billed=" + billed + " unpaid=" + unpaid;
    }

    /**
     * How long after a merge to wait before trusting AE2's verdict. The recompute happens in
     * {@code PathingService#onServerEndTick} and only once {@code repath()} has set the reboot flag, so
     * reading the state in the tick the edge was made shows the pre-merge answer and would approve anything.
     */
    private static final int MERGE_VERIFY_TICKS = 4;

    /**
     * The second half of the merge guard. {@link #starvationRisk} refuses the merges that must fail; this
     * confirms the ones it allowed against AE2's own numbers, and undoes any that end up starved - destroy
     * while the edge can still be proved ours, drop the record, name both numbers in the log. The branch is
     * meant to be unreachable, because the pre-check compares demand against the <em>smaller</em> ceiling and
     * so holds whichever grid survives. Reaching it means AE2 counts differently than we do, which is worth a
     * warning and an undo rather than a network the player finds dead ten minutes later.
     */
    private static void tendVerifies(ServerLevel acting, Data data) {
        long now = acting.getGameTime();
        for (Iterator<Map.Entry<Link, Long>> it = data.verifyUntil.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Link, Long> due = it.next();
            if (!data.links.contains(due.getKey())) {
                it.remove();
                continue;
            }
            if (now < due.getValue()) {
                continue;
            }
            it.remove();
            var node = node(acting, due.getKey().a());
            String verdict = starvationVerdict(node == null ? null : node.getGrid());
            if (verdict == null) {
                continue;
            }
            NeoECOPrototype.LOGGER.warn("wireless link: undoing {} because the joined network cannot carry"
                    + " itself: {} - the pre-check said it would fit, so AE2 counts differently than we do",
                    describe(due.getKey()), verdict);
            undoLink(acting, data, due.getKey());
        }
    }

    /** Destroys one edge we can still prove is ours and forgets its record; never destroys an unprovable one. */
    private static void undoLink(ServerLevel acting, Data data, Link link) {
        var held = data.edges.get(link);
        if (held != null && isStillBetween(acting, held, link)) {
            held.destroy();
        }
        data.drop(link);
    }

    /**
     * AE2's own verdict on a grid, or null while it carries itself. An ad-hoc error means the whole network
     * got zero channels; a conflicted controller group means the same thing for a different reason.
     */
    @Nullable
    private static String starvationVerdict(@Nullable appeng.api.networking.IGrid grid) {
        if (grid == null) {
            return null;
        }
        if ("CONTROLLER_CONFLICT".equals(controllerStateOf(grid))) {
            return "AE2 reads the controllers on the joined network as CONTROLLER_CONFLICT, which leaves it"
                    + " with no channels at all";
        }
        if (grid.getPathingService() instanceof appeng.me.service.PathingService service
                && service.getAdHocNetworkError() != null
                // The ad-hoc field is only authoritative on a grid AE2 is treating as ad-hoc. A wireless merge
                // keeps one of the two grid objects and its PathingService with it, so a network that was over
                // the ad-hoc ceiling a moment ago still reports the error until the next pathing pass - even
                // though it now has a controller and is not subject to that budget at all. Measured 2026-11-14:
                // two port links bound cleanly, then this verdict read the stale error four ticks later and
                // dropped both links, splitting the grids back apart (ports left with one connection each).
                && controllerNodes(grid) == 0) {
            return "AE2 set adHocError=" + service.getAdHocNetworkError().name() + " on the joined network";
        }
        return null;
    }

    /**
     * The pre-check. Only two controllerless networks can starve each other - once a controller is in play the
     * ad-hoc budget does not apply - so anything with a controller on either side passes here and is left to
     * {@link #starvationVerdict}. The ceiling used is the <em>smaller</em> of the two: which of the two grid
     * objects survives the merge is AE2's choice, and we do not bet somebody's whole network on it.
     */
    @Nullable
    private static Starvation starvationRisk(IGridNode a, IGridNode b) {
        var gridA = a.getGrid();
        var gridB = b.getGrid();
        // The premise is read off the node list, not off getControllerState(): that answer is a cached field
        // AE2 recomputes on its own schedule, and a wireless merge does not refresh it in the tick it happens.
        // Measured 2026-11-14 binding a second far network right after a first - the joined grid reported
        // state=NO_CONTROLLER with boot=false while holding one ControllerBlockEntity node, and the guard
        // refused a merge AE2 itself was already serving. The node list has no such lag.
        if (gridA == null || gridB == null
                || controllerNodes(gridA) > 0 || controllerNodes(gridB) > 0) {
            return null;
        }
        int demand = adHocDemand(gridA) + adHocDemand(gridB);
        int ceilingA = adHocCeiling(gridA);
        int ceilingB = adHocCeiling(gridB);
        int fits = Math.min(ceilingA, ceilingB);
        if (demand <= fits) {
            return null;
        }
        return new Starvation(demand, fits, ceilingA, ceilingB);
    }

    /**
     * How many AE2 controller nodes a grid holds. This is the un-lagged version of "does this network have a
     * controller": {@code getControllerState()} is cached and only refreshed on AE2's own schedule, so right
     * after a wireless merge it can still answer NO_CONTROLLER for a grid that already contains one.
     */
    private static int controllerNodes(IGrid grid) {
        int controllers = 0;
        for (var node : grid.getNodes()) {
            if (node.getOwner() instanceof appeng.blockentity.networking.ControllerBlockEntity) {
                controllers++;
            }
        }
        return controllers;
    }

    /** The four numbers the merge guard names when it refuses: demand, the binding ceiling, and the two sides. */
    private record Starvation(int demand, int fits, int ceilingA, int ceilingB) {
    }

    /**
     * The gate that replaced the link count: AE2's own headroom on the square the edge would land on.
     *
     * <p>Refusing here is sound rather than merely cautious. A channel that the joined network wants has to
     * pass through this node, so if AE2 has already handed that node everything it can carry, the far side
     * cannot be served either - and the link would only start drawing
     * {@code wireless_ae_per_link_per_tick} for a connection that carries nothing. Measured in the client on
     * 2026-11-13 in one world with the same module at both ends: an ordinary machine read
     * {@code a(max=8,used=8)} while the dense module end read {@code b(max=32,used=11)} - the machine was
     * already full, and a ninth spoke into it would have been billed for zero channels.
     *
     * <p>The reading is only a headroom measurement on a controlled network. On a controllerless one
     * {@code PathingService} runs {@code AdHocChannelUpdater}, which writes the SAME network-wide count into
     * every node it visits, so {@code getUsedChannels()} there is not this node's usage at all and every
     * ordinary node would look full at exactly 8. The controllerless case is
     * {@link #starvationRisk}'s job: it compares AE2's own ad-hoc demand against
     * {@code ChannelMode.getAdHocNetworkChannels()}, which is the number that actually governs it.
     *
     * @return the two numbers when that node can carry nothing more, null when it still has room
     */
    private static Full fullLanding(IGridNode node) {
        var grid = node == null ? null : node.getGrid();
        int max = node == null ? 0 : node.getMaxChannels();
        // A CANNOT_CARRY node answers 0, and no width is not the same thing as a full one: refusing on
        // 0 >= 0 would reject every link that touches a controller block.
        // The controlled-network test is the node count rather than getControllerState() for the same reason
        // starvationRisk uses it: after a wireless merge the cached state lags a tick, and reading it there
        // would let a genuinely full square through - failing open on the one gate whose job is to refuse.
        if (grid == null || max <= 0 || controllerNodes(grid) == 0) {
            return null;
        }
        int used = node.getUsedChannels();
        return used >= max ? new Full(used, max) : null;
    }

    /** The two numbers the landing gate names when it refuses: what the node carries, and what it can carry. */
    private record Full(int used, int max) {
    }

    /**
     * What AE2's ad-hoc branch would charge for this grid, counted the way it counts: one per node that
     * requires a channel, with a multiblock's members folded into the single slot the multiblock itself takes
     * (measured in {@code PathingService#calculateAdHocChannels}). That set is private, so this walks
     * {@code IGrid#getNodes()} through public API rather than pretending to know it.
     */
    private static int adHocDemand(appeng.api.networking.IGrid grid) {
        var counted = new java.util.HashSet<IGridNode>();
        int need = 0;
        for (IGridNode node : grid.getNodes()) {
            if (!node.hasFlag(appeng.api.networking.GridFlags.REQUIRE_CHANNEL) || !counted.add(node)) {
                continue;
            }
            if (node.hasFlag(appeng.api.networking.GridFlags.MULTIBLOCK)
                    && node.getService(appeng.api.networking.IGridMultiblock.class)
                    instanceof appeng.api.networking.IGridMultiblock multiblock) {
                for (var members = multiblock.getMultiblockNodes(); members.hasNext(); ) {
                    counted.add(members.next());
                }
            }
            need++;
        }
        return need;
    }

    /** The ad-hoc ceiling of that grid right now - the same number {@code adHocWords} prints. */
    private static int adHocCeiling(appeng.api.networking.IGrid grid) {
        var pathing = grid.getPathingService();
        return pathing == null ? 0 : pathing.getChannelMode().getAdHocNetworkChannels();
    }

    /**
     * The follow-up to a chunk wake: retry only the links that woke, and say so out loud when a wake did not
     * come good. The retry window exists because AE2 only builds a node on the block entity's first tick -
     * after {@code ChunkEvent.Load} - so the first attempt usually finds nothing. If the window runs out and
     * the edge is still missing, that is either a slow load or AE2 having moved when nodes appear, and the
     * slow sweep takes over either way: the honest remaining thing is to name it in the log.
     */
    private static void tendWakes(ServerLevel acting, Data data) {
        long now = acting.getGameTime();
        for (Iterator<Map.Entry<Link, Long>> it = data.urgentUntil.entrySet().iterator(); it.hasNext(); ) {
            Map.Entry<Link, Long> wake = it.next();
            Link link = wake.getKey();
            if (!data.links.contains(link)) {
                it.remove();
                continue;
            }
            if (now > wake.getValue()) {
                it.remove();
                if (!isStillBetween(acting, data.edges.get(link), link)) {
                    NeoECOPrototype.LOGGER.warn("wireless link: {} was not re-established within {} ticks of"
                            + " its chunk waking (self has a node={}, far has a node={}); the {}-tick sweep"
                            + " takes over", describe(link), WAKE_BURST_TICKS, node(acting, link.a()) != null,
                            node(acting, link.b()) != null, SWEEP_TICKS);
                }
                continue;
            }
            if (isStillBetween(acting, data.edges.get(link), link)) {
                it.remove();
                continue;
            }
            if (!rebuildLink(acting, data, link, "wake burst")) {
                data.drop(link);
                it.remove();
            }
        }
    }

    /**
     * How long a chunk wake keeps the pass running every tick. AE2 only builds a block entity's node on
     * that entity's first tick, which is *after* {@code ChunkEvent.Load} - so waking up has to be followed
     * by a short burst of passes, not a single attempt that would find nothing and then wait out the slow
     * sweep.
     */
    private static final int WAKE_BURST_TICKS = 20;

    /** The pass itself, unthrottled: a click, or a test, has to be able to ask for it right now. */
    public static void rebuildNow(ServerLevel acting, Supplier<AttachmentType<Data>> type) {
        if (!acting.hasData(type.get())) {
            return;
        }
        Data data = acting.getData(type.get());
        for (Iterator<Link> it = data.links.iterator(); it.hasNext(); ) {
            Link link = it.next();
            if (!rebuildLink(acting, data, link, "sweep")) {
                data.drop(link);
                it.remove();
            }
        }
    }

    /**
     * A player is taking this block away, so every pairing pointing at this square dies right now, while the
     * machine is still in the world to prove which edge belonged to it.
     *
     * <p>Nothing later can do this job. The only question a pass can ask is "is there a device in that
     * square", and a player who replaces the machine in the same square answers yes - so the record
     * survives and whatever moved in inherits a link belonging to the device that left. That is not a race
     * to shrink with urgency: an experiment on 2026-10-29 armed the burst and still read
     * {@code wirelessLinks=1 live=0} six ticks later. It is what a position-keyed pairing costs, and the
     * break event is the one moment the bill can be paid.
     *
     * @return what was dropped; empty when this square claimed nothing
     */
    public static String noteBlockBroken(net.minecraft.world.level.LevelAccessor level,
                                         Supplier<AttachmentType<Data>> type,
                                         net.minecraft.core.BlockPos broken) {
        if (!(level instanceof ServerLevel acting) || acting.isClientSide) {
            return "";
        }
        var where = GlobalPos.of(acting.dimension(), broken);
        // Asked before acting, because this runs on every block a player breaks: unlink would walk every
        // level and log its own verdict even when the square claimed nothing, which would bury the world in
        // "carries no wireless link" lines.
        boolean claims = false;
        for (ServerLevel each : acting.getServer().getAllLevels()) {
            if (each.hasData(type.get())
                    && each.getData(type.get()).links.stream().anyMatch(link -> link.touches(where))) {
                claims = true;
                break;
            }
        }
        if (!claims) {
            return "";
        }
        String report = unlink(acting, type, where);
        NeoECOPrototype.LOGGER.info("wireless link: broken at {} cleared {}", broken.toShortString(), report);
        return report;
    }

    /**
     * A chunk just woke up, which is the one moment a link is genuinely expected to need rebuilding - so the
     * timed pass can afford to be slow. Only links with an endpoint inside that chunk are touched, and the
     * test is a chunk-coordinate long, not a world query. Nothing is read from the level on this path either:
     * the event runs inside the chunk pipeline, so this marks the pair urgent and reads nothing at all. What the
     * hook buys is that urgency - the burst of main-thread passes that follows
     * catches the edge within a second instead of at the next slow sweep, and AE2 would not have built the
     * node yet anyway (it comes on the block entity's first tick, after this event).
     */
    public static void rebuildAround(net.minecraft.world.level.LevelAccessor level,
                                     Supplier<AttachmentType<Data>> type, int chunkX, int chunkZ) {
        if (!(level instanceof ServerLevel acting) || acting.isClientSide || !acting.hasData(type.get())) {
            return;
        }
        Data data = acting.getData(type.get());
        if (data.links.isEmpty()) {
            return;
        }
        long woke = net.minecraft.world.level.ChunkPos.asLong(chunkX, chunkZ);
        for (Link link : data.links) {
            if (!inChunk(acting, link.a(), chunkX, chunkZ, woke) && !inChunk(acting, link.b(), chunkX, chunkZ, woke)) {
                continue;
            }
            data.urgentUntil.put(link, acting.getGameTime() + WAKE_BURST_TICKS);
        }
    }

    private static boolean inChunk(ServerLevel acting, GlobalPos at, int chunkX, int chunkZ, long woke) {
        return at.dimension().equals(acting.dimension())
                && net.minecraft.world.level.ChunkPos.asLong(at.pos().getX() >> 4, at.pos().getZ() >> 4) == woke;
    }

    /**
     * One recorded pair, one decision.
     *
     * @param trigger which pass is asking, so the line that reports a dropped record names who killed it
     * @return false when the record is dead and the caller should drop it (an endpoint is no longer a
     *         network device)
     */
    private static boolean rebuildLink(ServerLevel acting, Data data, Link link, String trigger) {
        Presence pa = presence(acting, link.a());
        Presence pb = presence(acting, link.b());
        if (pa == Presence.GONE || pb == Presence.GONE) {
            GlobalPos gone = pa == Presence.GONE ? link.a() : link.b();
            GlobalPos other = pa == Presence.GONE ? link.b() : link.a();
            Presence otherPresence = pa == Presence.GONE ? pb : pa;
            // This is the one decision that deletes a player's pairing, so it names what it saw. The
            // boundary is narrower than "the machine is not ready": presence() asks for a block entity of
            // AE2's hierarchy, not for a node, so a device that was placed one tick ago and has not built
            // its node yet is AWAKE here and survives. What can still be killed is a square whose block was
            // swapped for something outside that hierarchy - which for a player usually means they broke it
            // and the sweep landed in the gap before they put it back.
            IGridConnection held = data.edges.get(link);
            data.drop(link);
            NeoECOPrototype.LOGGER.warn("wireless link: dropped {} in {} because {} is no longer a device;"
                            + " there is {} (held edge={}, other end {} is {}, node={})",
                    describe(link), trigger, describe(gone), whatStandsThere(acting, gone),
                    held == null ? "none" : "virtual edge", describe(other), name(otherPresence),
                    node(acting, other) != null);
            return false;
        }
        // Keep what we can still prove. Rebuilding an edge that is already in the graph buys a full
        // network recalculation and changes nothing, and an endpoint whose chunk is asleep answers
        // null here - which is also how we avoid asking for an edge while one end is missing.
        if (isStillBetween(acting, data.edges.get(link), link)) {
            return true;
        }
        data.edges.remove(link);
        EdgeResult result = ensureEdge(acting, data, link);
        if (result.edge() != null) {
            NeoECOPrototype.LOGGER.info("wireless link: rebuilt {} in {} | {}", describe(link), trigger,
                    readings(result.edge(), result.a(), result.b()));
        }
        return true;
    }

    /**
     * What stands on a recorded position, for the drop line. Only legal because {@link Presence#GONE} is
     * concluded from a chunk that {@link #chunkIsHere} already answered non-null for, and every caller of
     * this runs from the tick pass on the main thread - so this is a cache hit, not a load request.
     *
     * <p>Two names, because they separate the two ways a record can die: {@code air} means the player took
     * it away and the drop is right; a block with a class name means something is standing there that we do
     * not recognise as a device, and {@link #presence} already excluded both {@code IGridConnectedBlockEntity}
     * and a node-host capability, so this is the only line that can name what it excluded.</p>
     */
    private static String whatStandsThere(ServerLevel acting, GlobalPos at) {
        if (!at.dimension().equals(acting.dimension()) || !chunkIsHere(acting, at)) {
            return "unreadable (that dimension is not the one this pass is running in)";
        }
        var block = acting.getBlockState(at.pos()).getBlock();
        var entity = acting.getBlockEntity(at.pos());
        return (block == net.minecraft.world.level.block.Blocks.AIR ? "air" : block.getDescriptionId())
                + ", blockEntity=" + (entity == null ? "none" : entity.getClass().getName());
    }

    private static String name(Presence presence) {
        return switch (presence) {
            case AWAKE -> "awake";
            case ASLEEP -> "asleep";
            case GONE -> "gone";
        };
    }

    /**
     * The module behind an endpoint node, or null for any other device. Read off
     * {@code IGridNode.getOwner()} - the node carries its block entity - so no caller has to guess whether
     * the chunk is loaded enough to ask the world.
     */
    @Nullable
    private static WirelessChannelModuleBlockEntity moduleOf(@Nullable IGridNode node) {
        return node != null && node.getOwner() instanceof WirelessChannelModuleBlockEntity module ? module : null;
    }

    /** The endpoint has a block and no node yet: AE2 builds nodes on the block's first tick. */
    private static Reply noNodeYet(ServerLevel acting, GlobalPos at) {
        return Reply.of("no_node_yet", "no network node at " + labelText(acting, at)
                        + " yet - the block is there but AE2 has not given it a node, so bind it again in a moment",
                label(acting, at));
    }

    /**
     * Every port this module has is carrying all of its channels, so the next network through it would land on
     * a square with nothing left to give. This is the module's own ceiling and it is deliberately not the same
     * sentence as {@code endpoint_full}, which is about the square on the other end of the edge: a player told
     * the far side is full when it is the near module that ran out of ports goes looking in the wrong network.
     */
    private static Reply noFreePort(ServerLevel acting, GlobalPos at) {
        return Reply.of("no_free_port", "no channel port on " + labelText(acting, at) + " is free: all "
                        + WirelessChannelModuleBlockEntity.MAX_PORTS + " of them already carry another network,"
                        + " so one more through this module would have no outlet of its own - bind that network"
                        + " through a second subsystem channel module, or unbind one of these first",
                label(acting, at), WirelessChannelModuleBlockEntity.MAX_PORTS);
    }

    /**
     * Every node a recorded endpoint could legitimately be reached through.
     *
     * <p>For almost every device that is exactly one node, and this is {@link #node} wrapped in a list. A
     * subsystem channel module is the exception: it owns several virtual dense ports, each worth its own 32
     * channels, and a link is on the module as soon as it is on any one of them. Answering with the whole set
     * is what stops the rebuild pass from tearing an edge down every pass - the pass has no record of
     * <em>which</em> port a link landed on, and it does not need one, because ports are interchangeable
     * buckets of the same width. What it must not do is also add the module's own node: that node is the one
     * shared 32 the ports exist to get away from, and accepting it would let a link slide onto it and read as
     * healthy while a whole port's worth of channels goes unused.
     *
     * <p>The module is recognised through {@code IGridNode.getOwner()} rather than by reading the block entity
     * out of the world, so this keeps {@link #node}'s chunk and thread gating instead of inventing a second one.
     */
    private static List<IGridNode> landingNodes(ServerLevel acting, GlobalPos at) {
        var main = node(acting, at);
        var module = moduleOf(main);
        if (module != null) {
            // Deliberately does not open a port: this answers "where could an edge of ours already be", and a
            // module with none standing simply has an empty answer until something asks for one.
            return module.livePortNodes();
        }
        return main == null ? List.of() : List.of(main);
    }

    /** The virtual edge between any legal pair of these two endpoint sets, or null. */
    @Nullable
    private static IGridConnection firstConnectionBetween(List<IGridNode> onA, List<IGridNode> onB) {
        for (var a : onA) {
            for (var b : onB) {
                var existing = connectionBetween(a, b);
                if (existing != null) {
                    return existing;
                }
            }
        }
        return null;
    }

    /**
     * Brings one recorded pair to "there is an edge between these two nodes", by the cheapest route that
     * gets there: adopt the edge AE2 already has, otherwise ask for one - through the throttle, and never
     * more often than once per {@link #REBUILD_COOLDOWN_TICKS} per pair.
     */
    private static EdgeResult ensureEdge(ServerLevel acting, Data data, Link link) {
        IGridNode mainA = node(acting, link.a());
        IGridNode mainB = node(acting, link.b());
        if (mainA == null || mainB == null) {
            return new EdgeResult(null, null, null, mainA == null ? link.a() : link.b());
        }
        var onA = landingNodes(acting, link.a());
        var onB = landingNodes(acting, link.b());
        IGridConnection existing = firstConnectionBetween(onA, onB);
        if (existing != null) {
            // appeng.me.GridConnection#create throws IllegalStateException for a pair that is already
            // connected, so adopting is the only way this pass can be called twice on the same link.
            data.edges.put(link, existing);
            data.rebuildFailures.remove(link);
            data.givenUp.remove(link);
            return new EdgeResult(existing, existing.a(), existing.b(), null);
        }
        if (data.givenUp.contains(link)) {
            return new EdgeResult(null, null, null, null);
        }
        Long last = data.lastAttemptTick.get(link);
        if (last != null && acting.getGameTime() - last < REBUILD_COOLDOWN_TICKS) {
            return new EdgeResult(null, null, null, null);
        }
        // Nothing to adopt, so a new edge is asked for. On a module end that means a port with channels to
        // spare; running out of those is a capacity answer, not a failure, so it waits rather than counting
        // attempts - the loud version belongs to the bind that created the record.
        IGridNode a = creationNode(acting, link.a(), onA, node(acting, link.b()));
        IGridNode b = creationNode(acting, link.b(), onB, node(acting, link.a()));
        if (a == null || b == null) {
            return new EdgeResult(null, null, null, null);
        }
        data.lastAttemptTick.put(link, acting.getGameTime());
        IGridConnection created;
        try {
            created = GridHelper.createConnection(a, b);
        } catch (RuntimeException e) {
            // Reached when another pass (or another endpoint's sweep) connected this pair between the scan
            // above and this call. Ask once more before blaming the link: an IllegalStateException here means
            // the edge exists, not that AE2 refused us.
            IGridConnection adopted = firstConnectionBetween(landingNodes(acting, link.a()),
                    landingNodes(acting, link.b()));
            if (adopted != null) {
                data.edges.put(link, adopted);
                data.rebuildFailures.remove(link);
                data.givenUp.remove(link);
                return new EdgeResult(adopted, adopted.a(), adopted.b(), null);
            }
            int failures = data.rebuildFailures.merge(link, 1, Integer::sum);
            if (failures >= MAX_REBUILD_ATTEMPTS) {
                data.givenUp.add(link);
                NeoECOPrototype.LOGGER.warn("wireless link: {} was refused {} times, leaving it alone until "
                        + "it is unbound and bound again", describe(link), failures);
            } else {
                NeoECOPrototype.LOGGER.warn("wireless link: createConnection refused {} -> {} (attempt {}): {}",
                        describe(link.a()), describe(link.b()), failures, e.toString());
            }
            return new EdgeResult(null, null, null, null);
        }
        data.edges.put(link, created);
        data.rebuildFailures.remove(link);
        return new EdgeResult(created, a, b, null);
    }

    /**
     * The node to ask for a new edge on: the module's free port where there is a module, otherwise the one
     * endpoint node. {@code candidates} is already the legal set, so a plain device never re-reads anything.
     */
    @Nullable
    private static IGridNode creationNode(ServerLevel acting, GlobalPos at, List<IGridNode> candidates,
                                          @Nullable IGridNode far) {
        var module = moduleOf(node(acting, at));
        if (module != null) {
            return module.portForNewLink(farNodeCount(far));
        }
        return candidates.isEmpty() ? null : candidates.get(0);
    }

    /**
     * How many nodes the network on the far side of a prospective link has, as AE2 counts them - the number a
     * port compares against its own remaining channels before it agrees to share.
     *
     * <p>Unreadable answers come back larger than {@code WirelessChannelModuleBlockEntity#PACKABLE_NETWORK_NODES}
     * on purpose: a network whose size cannot be measured is never packed onto a port that already carries one,
     * because the conservative mistake (open another outlet) is visible to the player as a number, while the
     * optimistic one shows up as devices that stopped working.
     */
    private static int farNodeCount(@Nullable IGridNode far) {
        if (far == null) {
            return Integer.MAX_VALUE;
        }
        var grid = far.getGrid();
        return grid == null ? Integer.MAX_VALUE : gridNodes(grid);
    }

    /**
     * The wireless edge between these two nodes, or null. Deliberately blind to in-world connections:
     * a cable the player built between the same two blocks is not ours to adopt, and taking it would let an
     * unlink destroy a physical connection. AE2's own addons make the same split - ExtendedAE and
     * ae2lt's {@code WirelessLinkOps#createVirtualConnection} both refuse a pair that is already wired, and
     * ae2lt answers it with the words "A physical connection already exists between the target and
     * transmitter."
     */
    @Nullable
    private static IGridConnection connectionBetween(IGridNode a, IGridNode b) {
        for (IGridConnection connection : a.getConnections()) {
            if (spans(connection, a, b) && !connection.isInWorld()) {
                return connection;
            }
        }
        return null;
    }

    /**
     * Whether this connection joins exactly these two nodes, read off {@code IGridConnection.a()/b()} -
     * both are public API, and unlike {@code getOtherSide(node)} they do not depend on which end we happen
     * to be asking from. Either order counts: the pair has no direction.
     */
    private static boolean spans(IGridConnection connection, IGridNode a, IGridNode b) {
        return (connection.a() == a && connection.b() == b) || (connection.a() == b && connection.b() == a);
    }

    /**
     * True only when the reference really is the edge this pair asked for. A node that left the world
     * answers null here, which is what turns "the far chunk went to sleep" and "AE2 dropped our edge" into
     * the same safe answer: stop believing the reference, and let {@link #ensureEdge} sort it out.
     *
     * <p>Each side is a set rather than a node (see {@link #landingNodes}), so a link sitting on any one of a
     * module's ports counts as still where it belongs.
     */
    private static boolean isStillBetween(ServerLevel acting, @Nullable IGridConnection held, Link link) {
        if (held == null || held.isInWorld()) {
            return false;
        }
        for (var a : landingNodes(acting, link.a())) {
            for (var b : landingNodes(acting, link.b())) {
                if (spans(held, a, b)) {
                    return true;
                }
            }
        }
        return false;
    }

    /** A connection the player built out of blocks between exactly these two devices. */
    private static boolean physicallyWiredTogether(IGridNode a, IGridNode b) {
        for (IGridConnection connection : a.getConnections()) {
            if (spans(connection, a, b) && connection.isInWorld()) {
                return true;
            }
        }
        return false;
    }

    /** What {@link #ensureEdge} reached: an edge (with its two nodes for the report), or why there is none. */
    private record EdgeResult(@Nullable IGridConnection edge, @Nullable IGridNode a, @Nullable IGridNode b,
                              @Nullable GlobalPos missingNode) {
    }

    // ==================== reading the other mods' blocks without touching their code ====================

    private static boolean presenceIsAwake(ServerLevel acting, GlobalPos at) {
        return presence(acting, at) == Presence.AWAKE;
    }

    private static Presence presence(ServerLevel acting, GlobalPos at) {
        ServerLevel level = levelOf(acting, at.dimension());
        if (level == null) {
            return Presence.ASLEEP;
        }
        if (!chunkIsHere(level, at)) {
            return Presence.ASLEEP;
        }
        // Either answer means "AE2 would run a network through this position", which is the only question
        // a record cares about: the node itself may briefly be absent while a machine is unformed.
        boolean device = level.getBlockEntity(at.pos()) instanceof appeng.me.helpers.IGridConnectedBlockEntity
                || GridHelper.getNodeHost(level, at.pos()) != null;
        return device ? Presence.AWAKE : Presence.GONE;
    }

    /**
     * Null on an asleep endpoint. The node comes from {@code IGridConnectedBlockEntity#getGridNode()}, not
     * from {@code IInWorldGridNodeHost#getGridNode(side)}, and that is not a style choice: the side-based
     * answer only returns a node when {@code appeng.me.InWorldGridNode#isExposedOnSide} says that side is
     * exposed, which a machine standing there with no cable on it is not - so asking by side answers null
     * for exactly the devices this feature exists to bind. The side lookup is kept only for hosts that are
     * not block entities of AE2's own hierarchy, i.e. cable-mounted parts.
     */
    /**
     * Which block actually stands on that square, spelled as its registry id - printed because on 2026-10-30 a
     * client reading and our own fixture disagreed about which machine a joined network contained, and nothing
     * on either side could say which block had been clicked. Gated exactly like {@link #node}: the dimension is
     * resolved first and the chunk has to already be here, so this never loads anything (§6).
     */
    private static String blockId(ServerLevel acting, GlobalPos at) {
        ServerLevel level = levelOf(acting, at.dimension());
        if (level == null || !chunkIsHere(level, at)) {
            return "unknown";
        }
        return String.valueOf(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(level.getBlockState(at.pos()).getBlock()));
    }

    private static IGridNode node(ServerLevel acting, GlobalPos at) {
        ServerLevel level = levelOf(acting, at.dimension());
        if (level == null || !chunkIsHere(level, at)) {
            return null;
        }
        if (level.getBlockEntity(at.pos()) instanceof appeng.me.helpers.IGridConnectedBlockEntity connected) {
            return connected.getGridNode();
        }
        var host = GridHelper.getNodeHost(level, at.pos());
        if (host == null) {
            return null;
        }
        for (net.minecraft.core.Direction side : net.minecraft.core.Direction.values()) {
            var node = host.getGridNode(side);
            if (node != null) {
                return node;
            }
        }
        return null;
    }

    /**
     * The only safe way to ask what is standing at a recorded position: {@code getChunkNow} answers null when
     * this is not the main thread, and on the main thread it answers null unless that chunk is already FULL in
     * memory. Either way a non-null answer means the block-entity read that follows is a cache hit and cannot
     * queue work for anybody. Both halves matter, and the second one is what a live client taught us the hard
     * way twice: {@code ChunkEvent.Load} is posted from inside {@code ChunkStatusTasks.full} - on a chunk worker
     * the first time, and on the {@code Server thread} while {@code MinecraftServer.prepareLevels} polls its own
     * queue the second time - and a {@code hasChunk} that answers true at exactly that moment only says the
     * holder is {@code visible}, not that it is FULL. Asking for contents then parks the caller in
     * {@code ServerChunkCache.getChunk -> managedBlock}, and during world load the work it is waiting for is
     * the work the parked thread was supposed to run: start-region generation never leaves 0%.
     */
    private static boolean chunkIsHere(ServerLevel acting, GlobalPos at) {
        return acting.getChunkSource().getChunkNow(at.pos().getX() >> 4, at.pos().getZ() >> 4) != null;
    }

    /** Whether this thread may read the level at all; {@link #chunkIsHere} already carries this inside it. */
    public static boolean mayReadBlocks(ServerLevel acting) {
        MinecraftServer server = acting.getServer();
        return server != null && server.isSameThread();
    }

    /** Asking for a dimension never loads it: an unloaded one is simply not in the map. */
    private static ServerLevel levelOf(ServerLevel acting, ResourceKey<Level> dimension) {
        if (acting.dimension().equals(dimension)) {
            return acting;
        }
        MinecraftServer server = acting.getServer();
        return server == null ? null : server.getLevel(dimension);
    }

    // ==================== the numbers ====================

    public static String describe(GlobalPos at) {
        return at.dimension().location() + "@" + at.pos().toShortString();
    }

    public static String describe(Link link) {
        return describe(link.a()) + " <-> " + describe(link.b());
    }

    /**
     * The label a player reads for one square: that block's own name, then the coordinate under it. A
     * Component on purpose - {@code Block#getName()} is translatable, and a name resolved on the server is
     * English whoever is reading it, so the resolution has to be left to the client.
     *
     * <p>Two cases fall back to the bare {@link #describe} rather than naming anything: a square whose chunk
     * the given level cannot see, and a square holding air. The first is honesty about not having looked; the
     * second matters because {@link #noteBlockBroken} reports what it cleared <em>after</em> the player took
     * the machine away, and "Air@-32, -60, 10" would be a true statement with nothing useful in it - the
     * coordinate is the shorter way of saying the same thing.
     */
    public static Component label(Level looking, GlobalPos at) {
        if (!looking.dimension().equals(at.dimension()) || !looking.isLoaded(at.pos())) {
            return Component.literal(describe(at));
        }
        var state = looking.getBlockState(at.pos());
        if (state.isAir()) {
            return Component.literal(describe(at));
        }
        return state.getBlock().getName().copy()
                .append(Component.literal(coordinateSuffix(looking, at)));
    }

    /**
     * {@link #label} as the string the log and the guards read. The level asked is the one the square is in,
     * so a cross-dimension pair still gets named here even though a client could not have read it; on a
     * dedicated server the resolved name is English, which is what makes the log sentence and the {@code en_us}
     * value one story rather than two.
     */
    public static String labelText(ServerLevel acting, GlobalPos at) {
        var owner = levelOf(acting, at.dimension());
        return label(owner == null ? acting : owner, at).getString();
    }

    /** One end of a link, named - the pair form the card sends as two separate arguments. */
    public static Component endLabel(ServerLevel acting, Link link, boolean first) {
        return labelAnywhere(acting, first ? link.a() : link.b());
    }

    /**
     * {@link #label} for a square the caller may not be standing in: the level asked is the one that square
     * belongs to, so a link whose far end is in another dimension still gets its block named rather than its
     * raw coordinates. Falls back to the acting level when that dimension is not loaded, which {@link #label}
     * then answers as coordinates - the same answer it gives for any square it cannot see.
     */
    public static Component labelAnywhere(ServerLevel acting, GlobalPos at) {
        ServerLevel owner = levelOf(acting, at.dimension());
        return label(owner == null ? acting : owner, at);
    }

    /** The pair as the log reads it: both ends named with {@link #labelText}, joined by the same arrow. */
    public static String describeLabelled(ServerLevel acting, Link link) {
        return labelText(acting, link.a()) + " <-> " + labelText(acting, link.b());
    }

    private static String coordinateSuffix(Level looking, GlobalPos at) {
        return looking.dimension().equals(at.dimension())
                ? "@" + at.pos().toShortString()
                : "@" + at.dimension().location() + "@" + at.pos().toShortString();
    }

    /**
     * One line per edge. {@code getMaxChannels()} is not on AE2's public {@code IGridConnection}, so the
     * capacity that decides whether a wireless link can starve a network is only readable off the
     * implementation class - which is the entire reason this feature needs a report at all.
     */
    private static String readings(IGridConnection edge, IGridNode a, IGridNode b) {
        StringBuilder line = new StringBuilder();
        line.append("edge=").append(edge.isInWorld() ? "in-world" : "virtual");
        if (edge instanceof appeng.me.GridConnection internal) {
            line.append(" max=").append(internal.getMaxChannels())
                    .append(" used=").append(internal.getUsedChannels());
        }
        line.append(" a(max=").append(a == null ? "?" : a.getMaxChannels())
                .append(",used=").append(a == null ? "?" : a.getUsedChannels()).append(')')
                .append(" b(max=").append(b == null ? "?" : b.getMaxChannels())
                .append(",used=").append(b == null ? "?" : b.getUsedChannels()).append(')');
        if (a != null && b != null) {
            line.append(" sameGrid=").append(a.getGrid() == b.getGrid());
            var grid = a.getGrid();
            var pathing = grid.getPathingService();
            line.append(" gridNodes=").append(gridNodes(grid))
                    .append(" mode=").append(pathing.getChannelMode())
                    .append(" controller=").append(pathing.getControllerState())
                    .append(" gridUsed=").append(pathing.getUsedChannels())
                    .append(powerWords(grid))
                    .append(adHocWords(grid));
        }
        return line.toString();
    }
}

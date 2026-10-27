package cn.dancingsnow.neoecoprototype.event;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.HolderLookup;
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
 */
public final class WirelessLinks {

    /** The product rule from the sketch: a device answers for at most this many wireless edges. */
    public static final int MAX_LINKS_PER_ENDPOINT = 4;

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

    /** The persisted half is the list of pairs; the edges are ours only for as long as both ends are up. */
    public static final class Data {
        final List<Link> links = new ArrayList<>();
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

        public List<Link> links() {
            return List.copyOf(links);
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
                                data.links.add(Link.of(a, b));
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
        var location = ResourceLocation.parse(dimension);
        return GlobalPos.of(ResourceKey.create(net.minecraft.core.registries.Registries.DIMENSION, location),
                BlockPos.of(tag.getLong(TAG_POS)));
    }

    // ==================== what the card asks for ====================

    /** True when the block at that position is something AE2 would run a network through. */
    public static boolean isDevice(ServerLevel acting, GlobalPos at) {
        return presence(acting, at) == Presence.AWAKE;
    }

    /**
     * Puts a pair into the record and tries the edge right away, so the player gets the reading in the same
     * click that made the link.
     */
    public static String bind(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos from, GlobalPos to) {
        if (from.equals(to)) {
            return "both ends are the same block";
        }
        Link link = Link.of(from, to);
        ServerLevel owner = levelOf(acting, link.ownerDimension());
        if (owner == null) {
            return "the dimension that would own this link is not loaded";
        }
        Data data = owner.getData(type.get());
        if (data.links.contains(link)) {
            return "already linked " + describe(link);
        }
        if (!presenceIsAwake(owner, link.a()) || !presenceIsAwake(owner, link.b())) {
            return "that is not a network device: " + describe(presence(owner, link.a()) == Presence.AWAKE
                    ? link.b() : link.a());
        }
        if (linksAtEverywhere(owner, type, link.a()) >= MAX_LINKS_PER_ENDPOINT) {
            return describe(link.a()) + " already carries " + linksAtEverywhere(owner, type, link.a())
                    + " links (max " + MAX_LINKS_PER_ENDPOINT + ")";
        }
        if (linksAtEverywhere(owner, type, link.b()) >= MAX_LINKS_PER_ENDPOINT) {
            return describe(link.b()) + " already carries " + linksAtEverywhere(owner, type, link.b())
                    + " links (max " + MAX_LINKS_PER_ENDPOINT + ")";
        }
        IGridNode a = node(owner, link.a());
        IGridNode b = node(owner, link.b());
        if (a == null || b == null) {
            // Measured in the client: a block answers as a device the moment it is placed (that is what
            // let the card mark it), while AE2 only builds the node when that block first ticks - so this
            // is the one refusal worth saying how to get past.
            return "no network node at " + describe(a == null ? link.a() : link.b())
                    + " yet - the block is there but AE2 has not given it a node, so bind it again in a moment";
        }
        if (physicallyWiredTogether(a, b)) {
            return "those two are already wired together with blocks: a wireless edge there would carry "
                    + "nothing, and unbinding it would tear out the connection you built";
        }
        if (a.getGrid() == b.getGrid()) {
            return "the two are already on one network, so an edge between them proves nothing";
        }
        data.links.add(link);
        // No dirty flag to set: LevelAttachmentsSavedData.isDirty() answers true unconditionally, so a
        // level save always re-reads whatever this object currently holds.
        EdgeResult result = ensureEdge(owner, data, link);
        if (result.edge() != null) {
            return "linked " + describe(link) + " | " + readings(result.edge(), result.a(), result.b());
        }
        if (result.missingNode() != null) {
            return "recorded but no network node at " + describe(result.missingNode())
                    + " - a block counts as a device the moment it is placed, while AE2 only builds its"
                    + " node when that block first ticks, so binding it again in a moment should work";
        }
        return "recorded " + describe(link) + " but no edge was made (see log)";
    }

    /**
     * Drops every link at one endpoint. The record may be filed under the other endpoint's dimension, so
     * every awake dimension is asked - cheap here, because this only ever runs on a click.
     */
    public static String unlink(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
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
                if (held != null && isStillBetween(level, held, link)) {
                    held.destroy();
                }
                data.drop(link);
                it.remove();
                dropped++;
            }
        }
        NeoECOPrototype.LOGGER.info("wireless link: unbind {} dropped={} link(s)", describe(endpoint), dropped);
        return dropped == 0
                ? describe(endpoint) + " carries no wireless link"
                : "dropped " + dropped + " link(s) at " + describe(endpoint);
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
     * What a player holding a card wants to know about the block in front of them: how many links are
     * recorded there, and how many of them are backed by an edge right now. The two differ whenever an
     * endpoint is asleep, and saying both is the only way the card can admit that.
     */
    public static String status(ServerLevel acting, Supplier<AttachmentType<Data>> type, GlobalPos endpoint) {
        return describe(endpoint) + " wirelessLinks=" + linksAtEverywhere(acting, type, endpoint)
                + " live=" + liveLinksAt(acting, type, endpoint);
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
        } else if (!data.urgentUntil.isEmpty()) {
            tendWakes(acting, data);
        }
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
            if (!rebuildLink(acting, data, link)) {
                data.links.remove(link);
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
            if (!rebuildLink(acting, data, it.next())) {
                it.remove();
            }
        }
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
     * @return false when the record is dead and the caller should drop it (an endpoint is no longer a
     *         network device)
     */
    private static boolean rebuildLink(ServerLevel acting, Data data, Link link) {
        Presence pa = presence(acting, link.a());
        Presence pb = presence(acting, link.b());
        if (pa == Presence.GONE || pb == Presence.GONE) {
            GlobalPos gone = pa == Presence.GONE ? link.a() : link.b();
            data.drop(link);
            NeoECOPrototype.LOGGER.info("wireless link: dropped {} because {} is no longer a device",
                    describe(link), describe(gone));
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
            NeoECOPrototype.LOGGER.info("wireless link: rebuilt {} | {}", describe(link),
                    readings(result.edge(), result.a(), result.b()));
        }
        return true;
    }

    /**
     * Brings one recorded pair to "there is an edge between these two nodes", by the cheapest route that
     * gets there: adopt the edge AE2 already has, otherwise ask for one - through the throttle, and never
     * more often than once per {@link #REBUILD_COOLDOWN_TICKS} per pair.
     */
    private static EdgeResult ensureEdge(ServerLevel acting, Data data, Link link) {
        IGridNode a = node(acting, link.a());
        IGridNode b = node(acting, link.b());
        if (a == null || b == null) {
            return new EdgeResult(null, null, null, a == null ? link.a() : link.b());
        }
        IGridConnection existing = connectionBetween(a, b);
        if (existing != null) {
            // appeng.me.GridConnection#create throws IllegalStateException for a pair that is already
            // connected, so adopting is the only way this pass can be called twice on the same link.
            data.edges.put(link, existing);
            data.rebuildFailures.remove(link);
            data.givenUp.remove(link);
            return new EdgeResult(existing, a, b, null);
        }
        if (data.givenUp.contains(link)) {
            return new EdgeResult(null, null, null, null);
        }
        Long last = data.lastAttemptTick.get(link);
        if (last != null && acting.getGameTime() - last < REBUILD_COOLDOWN_TICKS) {
            return new EdgeResult(null, null, null, null);
        }
        data.lastAttemptTick.put(link, acting.getGameTime());
        IGridConnection created;
        try {
            created = GridHelper.createConnection(a, b);
        } catch (RuntimeException e) {
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
     */
    private static boolean isStillBetween(ServerLevel acting, @Nullable IGridConnection held, Link link) {
        if (held == null || held.isInWorld()) {
            return false;
        }
        IGridNode a = node(acting, link.a());
        IGridNode b = node(acting, link.b());
        return a != null && b != null && spans(held, a, b);
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
            int nodes = 0;
            for (var unused : grid.getNodes()) {
                nodes++;
            }
            var pathing = grid.getPathingService();
            line.append(" gridNodes=").append(nodes)
                    .append(" mode=").append(pathing.getChannelMode())
                    .append(" controller=").append(pathing.getControllerState())
                    .append(" gridUsed=").append(pathing.getUsedChannels());
        }
        return line.toString();
    }
}

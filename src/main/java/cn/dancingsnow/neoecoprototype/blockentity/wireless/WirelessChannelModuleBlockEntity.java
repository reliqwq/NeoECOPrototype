package cn.dancingsnow.neoecoprototype.blockentity.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import java.util.ArrayList;
import java.util.List;

/**
 * A fixed in-world AE2 node that can serve as the local endpoint of a wireless link.
 *
 * <p>The block itself is one dense face, so on its own it is worth 32 channels no matter how many links land
 * on it - AE2 puts the ceiling on the node ({@code GridNode.getMaxChannels}), not on the edge, and every edge
 * that lands here walks through that one number. The ports below exist to break exactly that: each one is a
 * virtual dense node hanging off the <em>controller</em> node rather than off this block's own node, so it
 * tops its own {@code channelBottlenecks} bucket and is worth another 32 to whatever it feeds. That is also
 * why a port attaches to the controller and not to us: routed through this block's node, all six would share
 * its single 32 and the whole exercise would change nothing.
 *
 * <p>A port carries no flag asking for a channel of its own - only {@link GridFlags#DENSE_CAPACITY} - because
 * one that did would spend a channel out of the bucket it exists to widen.
 *
 * <p>Lives on this instance and nowhere else: never a static table. A port left behind by an unloaded chunk
 * keeps occupying a controller bucket, which reads to the player as channels that went somewhere they cannot
 * see, so both unload paths destroy every port and the next bind rebuilds them from nothing.
 */
public final class WirelessChannelModuleBlockEntity extends AENetworkedBlockEntity
        implements IInWorldGridNodeHost {

    /** AE2 prices a dense face at 32 channels and a cube has six faces: that is where the arithmetic stops. */
    public static final int MAX_PORTS = 6;

    /** AE2's own per-node ceiling at {@code ChannelMode.X4}, quoted so the javadoc above stays checkable. */
    public static final int CHANNELS_PER_PORT = 32;

    private static final String PORT_TAG = "neoecoprototype-channel-port-";
    private static final String LINK_GROUP_TAG = "linkGroup";

    private String linkGroup = "";
    private final List<IManagedGridNode> ports = new ArrayList<>();
    private final List<IGridConnection> trunks = new ArrayList<>();

    /**
     * Channels we handed out against a port that AE2 has not billed yet, and the number of links that promise
     * was priced at.
     *
     * <p>{@code IGridNode.getUsedChannels()} is the right number and it is a tick late: the pathing service
     * recomputes it on its own schedule, so two binds made before the next pathing pass both read the same
     * pre-merge figure and both think the port is nearly empty. Measured 2026-11-14 - binding two networks of
     * seventeen machines back to back left the port at {@code 0/32} while it read {@code con=3}, and the second
     * network packed on top of the first. AE2 then had 32 channels for 34 billed nodes and quietly left
     * thirteen machines offline, which is the optimistic mistake this map exists to prevent.
     *
     * <p>An entry is dropped as soon as AE2's number catches up with it, and as soon as the port has fewer
     * links than the promise was struck at - a link left by the unlink card is a link AE2 has to recount, and
     * trusting the stale promise then would keep a freed port looking full. The mistake that is left is the
     * visible one: a port that reads fuller than it is for a tick, which costs an outlet, never a channel.
     */
    private final java.util.Map<IGridNode, Promise> promised = new java.util.HashMap<>();

    /** What a port was priced at, and how many links hung off it when the price was struck. */
    private record Promise(int channels, int links) {
    }

    public WirelessChannelModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    protected IManagedGridNode createMainNode() {
        return super.createMainNode().setFlags(GridFlags.REQUIRE_CHANNEL, GridFlags.DENSE_CAPACITY);
    }

    @Override
    public IGridNode getGridNode(Direction side) {
        return getMainNode().getNode();
    }

    @Override
    public AECableType getCableConnectionType(Direction side) {
        return AECableType.DENSE_SMART;
    }

    /**
     * Whether AE2 has wired this block into a grid yet. It is the difference between two answers a player
     * hears differently: "no port has room" and "that module is not on a network at all".
     */
    public boolean gridOnline() {
        return getMainNode().isReady() && trunkNode() != null;
    }

    /**
     * The port a new link should land on.
     *
     * <p>Two rules, tried in creation order, and the first port that satisfies either one wins:
     * <ol>
     * <li>a port with nothing but its trunk on it - the original rule, one network per outlet;</li>
     * <li>a port that already carries a network, if the channels the newcomer will draw still fit in what the
     * port has left.</li>
     * </ol>
     *
     * <p>Rule 2 is priced in channels, and the first two versions of it were priced in node count instead -
     * first a satellite was "eight or fewer nodes", then the price became {@code farNodes + 1}. Both were
     * wrong in the same direction. The client on 2026-11-14 killed the first: a player linking a wall of
     * machines through their cable buses had every bind open its own outlet, six of them, and the card refused
     * the seventh. The same client killed the second a few hours later, from the other side: every outlet read
     * {@code 24/32} with eight channels still free and an eight-channel network still refused, because the
     * count charged each machine's cable as well as the machine.
     *
     * <p>What the port is actually charged is one channel per far node that carries
     * {@link appeng.api.networking.GridFlags#REQUIRE_CHANNEL}, measured rather than assumed: a group of
     * seventeen machines spends eighteen of a port's thirty-two in the suite
     * ({@code [0=18/32/con=2, 1=18/32/con=2]}) - the seventeen interfaces plus the module they hang off, and
     * none of the seventeen dense cables between them. The caller passes that count, so the fit test below is
     * AE2's own arithmetic and nothing else. A network too big for any outlet still gets one to itself through
     * rule 1 - the permission to share is what is priced, never the permission to connect.
     *
     * <p>Ports are opened on demand rather than all six up front. A port nobody uses is not free: it is a node
     * in the grid's node list, so it shows up in the Network Tool's node count, it gets walked by every path
     * calculation over that grid, and the block reads as a bigger machine than it is. Six eager ports made
     * every module cost six nodes from the moment it was placed.
     *
     * @param farChannels how many channels the network on the other side of the link will pull through this
     *                    port, as AE2 bills them; pass {@link Integer#MAX_VALUE} when it cannot be read, and the
     *                    port is treated as unable to afford it, so an unknown network only ever lands on an
     *                    unused one
     */
    public IGridNode portForNewLink(int farChannels) {
        for (var port : ports) {
            if (!port.isReady()) {
                continue;
            }
            var node = port.getNode();
            if (node == null) {
                continue;
            }
            if (node.getConnections().size() <= 1) {
                promised.remove(node);
                return node;
            }
            if (portLoad(node) + (long) farChannels <= node.getMaxChannels()) {
                return node;
            }
        }
        if (ports.size() >= MAX_PORTS) {
            return null;
        }
        var upstream = trunkNode();
        if (upstream == null) {
            return null;
        }
        var index = ports.size();
        var port = GridHelper.createManagedNode(this, (target, node) -> {
                }).setInWorldNode(false)
                .setIdlePowerUsage(0.0)
                .setTagName(PORT_TAG + index)
                .setFlags(GridFlags.DENSE_CAPACITY);
        port.create(level, worldPosition);
        trunks.add(GridHelper.createConnection(upstream, port.getNode()));
        ports.add(port);
        return port.getNode();
    }

    /**
     * What a port is carrying right now: AE2's own bill, or the channels we handed out against it if AE2 has
     * not noticed them yet and the port still has the links they were priced at. See {@link #promised}.
     */
    private int portLoad(IGridNode node) {
        int billed = node.getUsedChannels();
        int links = node.getConnections().size() - 1;
        Promise owed = promised.get(node);
        if (owed == null || owed.links() > links || owed.channels() <= billed) {
            promised.remove(node);
            return billed;
        }
        return owed.channels();
    }

    /**
     * That an edge was just built on {@code port} carrying {@code channels} of its 32. Called by the pass that
     * creates the edge and by nothing else - {@link #portForNewLink} is a question, so the bind that only
     * decides whether to refuse does not spend anything.
     */
    public void noteChannelsPromised(IGridNode port, int channels) {
        promised.put(port, new Promise(portLoad(port) + channels,
                port.getConnections().size() - 1));
    }

    public String linkGroup() {
        return linkGroup;
    }

    public void setLinkGroup(String value) {
        String normalized = normalizeLinkGroup(value);
        if (normalized.equals(linkGroup)) {
            return;
        }
        linkGroup = normalized;
        setChanged();
        markForUpdate();
    }

    private static String normalizeLinkGroup(String value) {
        String normalized = value.codePoints()
                .filter(codePoint -> !Character.isISOControl(codePoint))
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString().trim();
        int length = normalized.codePointCount(0, normalized.length());
        return length <= 32 ? normalized : normalized.substring(0, normalized.offsetByCodePoints(0, 32));
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        if (!linkGroup.isEmpty()) {
            data.putString(LINK_GROUP_TAG, linkGroup);
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        linkGroup = normalizeLinkGroup(data.getString(LINK_GROUP_TAG));
    }

    /** How many ports this block is holding right now. */
    public int livePorts() {
        return ports.size();
    }

    /**
     * How many of the standing ports are carrying something - a port whose only connection is the trunk
     * feeding it has lost its link and is waiting to be handed out again.
     *
     * <p>{@code WirelessLinks#status} prints this rather than {@link #livePorts()}, because the two part
     * company the moment a player unlinks: an unbound port stays open on purpose (destroying it and rebuilding
     * it on the next bind would churn the grid's node list every time), so the open count only ever goes up.
     * Measured in the client on 2026-11-14: after six links were unbound one at a time the card still answered
     * {@code ports=6/6}, and the report was that the module reads as still occupied. The links were gone - the
     * very next bind onto the freed square succeeded - so the number was the bug, not the ports.
     */
    public int busyPorts() {
        int busy = 0;
        for (var port : ports) {
            if (!port.isReady()) {
                continue;
            }
            var node = port.getNode();
            if (node != null && node.getConnections().size() > 1) {
                busy++;
            }
        }
        return busy;
    }

    /** The current AE2 per-port limit, including the active channel-mode multiplier. */
    public int portChannelCapacity() {
        var node = getMainNode().getNode();
        return node == null || node.getGrid() == null ? CHANNELS_PER_PORT : node.getMaxChannels();
    }

    /** The total channel budget represented by all six module outlets. */
    public long totalPortCapacity() {
        return (long) MAX_PORTS * portChannelCapacity();
    }

    /** Channels currently charged to the module's live ports, as AE2 reports them. */
    public long totalPortUsed() {
        long used = 0;
        for (var node : livePortNodes()) {
            used += node.getUsedChannels();
        }
        return used;
    }

    /**
     * The port handles themselves, in creation order, for a caller that has to ask them a question
     * <em>after</em> the block is gone.
     *
     * <p>Needed because AE2 refuses {@code IGridNode.getGrid()} on a destroyed node - it answers
     * "A node is being used after it has been destroyed" and throws, so a teardown test cannot prove a port
     * died by interrogating the node it used to own. The handle is the object that carries the destruction:
     * {@code ManagedGridNode.destroy()} nulls its node, and nothing else in this class does.
     */
    public List<IManagedGridNode> portHandles() {
        return List.copyOf(ports);
    }

    /**
     * A snapshot of the ports that are still standing, in creation order.
     *
     * <p>AE2 has no "is this node destroyed" query ({@code IGridNode} answers none, and
     * {@code appeng.me.GridNode} only has {@code destroy()}), so liveness is read off the wrapper:
     * {@code ManagedGridNode.destroy()} nulls its node field, and {@code isReady()} is
     * {@code initData == null && node != null} - created and not destroyed, which is the same thing asked for.
     *
     * <p>The caller gets a copy rather than the live list because the sweep walks this while deciding whether
     * to build an edge, and building one can reach back into this block.
     */
    public List<IGridNode> livePortNodes() {
        var alive = new ArrayList<IGridNode>(ports.size());
        for (var port : ports) {
            if (!port.isReady()) {
                continue;
            }
            var node = port.getNode();
            if (node != null) {
                alive.add(node);
            }
        }
        return alive;
    }

    /**
     * The node every port hangs off: this grid's controller node when there is one, because that node is
     * {@code CANNOT_CARRY} and is the root of the pathing search, so a port under it becomes the bottleneck
     * of its own subtree instead of a second caller on this block's 32. With no controller in the grid there
     * is nothing to widen - an ad-hoc network gets its channel count from a single network-wide branch - so
     * the ports simply ride on our own node and cost nothing but a few extra entries in the node list.
     */
    private IGridNode trunkNode() {
        var mainNode = getMainNode().getNode();
        if (mainNode == null) {
            return null;
        }
        var grid = getMainNode().getGrid();
        if (grid == null) {
            return mainNode;
        }
        for (var node : grid.getNodes()) {
            if (node.getOwner() instanceof ControllerBlockEntity) {
                return node;
            }
        }
        return mainNode;
    }

    private void destroyPorts() {
        promised.clear();
        // Connections first: AE2 frees the channels an edge was carrying when the edge goes, and a destroyed
        // node with a live edge pointing at it is exactly the ghost this class exists to avoid.
        for (var trunk : trunks) {
            trunk.destroy();
        }
        trunks.clear();
        for (var port : ports) {
            port.destroy();
        }
        ports.clear();
    }

    @Override
    public void setRemoved() {
        destroyPorts();
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        destroyPorts();
        super.onChunkUnloaded();
    }
}

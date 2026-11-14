package cn.dancingsnow.neoecoprototype.blockentity.wireless;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.blockentity.networking.ControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
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

    private final List<IManagedGridNode> ports = new ArrayList<>();
    private final List<IGridConnection> trunks = new ArrayList<>();

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
     * The size of far network that is allowed to share a port with a network already on it.
     *
     * <p>Eight, and the number is a bet about growth rather than an AE2 constant: a network of eight or fewer
     * nodes is a satellite (a few machines behind one interface), and a player who adds machines to it is
     * adding them to a grid that already has a controller and a power store - it will outgrow the shared 32 and
     * AE2 will starve some of its devices, which is the failure this rule accepts in exchange for capacity.
     * Above eight the port stays private, because a network that big is the kind a player keeps building on.
     *
     * <p>Measured on 2026-11-14, in the suite's own log line: a group of 17 machines spends 18 of its port's 32
     * channels ({@code [0=18/32/con=2, 1=18/32/con=2]}), so the arithmetic is AE2's, not an estimate - the one
     * extra channel is the far network's own end of the edge.
     */
    public static final int PACKABLE_NETWORK_NODES = 8;

    /**
     * The port a new link should land on.
     *
     * <p>Two rules, tried in creation order, and the first port that satisfies either one wins:
     * <ol>
     * <li>a port with nothing but its trunk on it - the original rule, one network per outlet;</li>
     * <li>a port that already carries a network, if the newcomer is at most
     * {@link #PACKABLE_NETWORK_NODES} nodes and its channels fit in what the port has left.</li>
     * </ol>
     *
     * <p>Rule 2 is what makes one module worth more than six networks: four 3-node satellites spend 3 channels
     * each and all land on port 0, so the block absorbs them without opening a second outlet. Rule 1 is what
     * keeps the promise route D was built for: a 17-machine network fails rule 2 on size alone, so it never
     * shares a bucket, and the fixture that measures 34 of 34 machines coming up through two ports stays true
     * whether or not packing is in use.
     *
     * <p>Ports are opened on demand rather than all six up front. A port nobody uses is not free: it is a node
     * in the grid's node list, so it shows up in the Network Tool's node count, it gets walked by every path
     * calculation over that grid, and the block reads as a bigger machine than it is. Six eager ports made
     * every module cost six nodes from the moment it was placed.
     *
     * @param farNodes how many nodes the network on the other side of the link has, as AE2 counts them; pass a
     *                 value larger than {@link #PACKABLE_NETWORK_NODES} when it cannot be read, so an unknown
     *                 network is never packed
     */
    public IGridNode portForNewLink(int farNodes) {
        for (var port : ports) {
            if (!port.isReady()) {
                continue;
            }
            var node = port.getNode();
            if (node == null) {
                continue;
            }
            boolean unused = node.getConnections().size() <= 1;
            boolean hasRoom = farNodes <= PACKABLE_NETWORK_NODES
                    && node.getUsedChannels() + farNodes <= node.getMaxChannels();
            if (unused || hasRoom) {
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

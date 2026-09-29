package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.util.RandomSource;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * C1 computation host. {@code communication_interface} carries whether the interface cell holds the
 * communication interface, so the blockstate file can hand that case its own formed model instead of
 * making a resource pack read the world. The calculator measures it and hands it here; placement never
 * sets it.
 *
 * <p>The host's block entity is eco's own {@code ECOComputationSystemBlockEntity} on purpose. AE2 files
 * every grid node under {@code owner.getClass()} and looks machines up by that exact key
 * ({@code Grid.add} has a single {@code put}, no superclass walk), so a subclass here is invisible to
 * eco's {@code getMachines(ECOComputationSystemBlockEntity.class)} -- which is how the subsystem gets
 * into AE2's CPU list and, through the same collection, into crafting execution.
 *
 * <p>Two siblings used to live here as block states - {@code energized_threading_core} and
 * {@code energized_parallel_core} - and both were removed in favour of the members drawing themselves.
 * They never selected different artwork (all eighteen combination models were copies of the plain formed
 * face), so the only thing they bought was a blockstate file of 512 entries where 20 say the same thing,
 * and two more property names for a resource pack to collide with.
 */
public class SimplifyComputationSystemBlock extends ECOComputationSystem {
    /** True when the interface cell holds {@code simplify_computation_network_interface}, the GUI one. */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    /** What the calculator measured, parked per dimension and position until a tick may write it. */
    private static final Map<ResourceKey<Level>, Map<Long, Boolean>> PENDING_SHAPE =
            new ConcurrentHashMap<>();

    private static Map<Long, Boolean> pendingFor(Level level) {
        return PENDING_SHAPE.computeIfAbsent(level.dimension(), d -> new ConcurrentHashMap<>());
    }

    public SimplifyComputationSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState()
                .setValue(COMMUNICATION_INTERFACE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE);
    }

    /**
     * Records what the machine turned out to hold and asks for a tick that can write it. The calculator
     * cannot write on its own stack: it runs inside AE2's cluster recalculation, which holds a global
     * "modification in progress" latch, and a block change made anywhere on that stack wedges every
     * multiblock for the rest of the session -- measured, not theorised. Repeated calls collapse into the
     * one scheduled tick, and a tick that finds nothing to change writes nothing.
     */
    public void publishShape(ServerLevel level, BlockPos pos, boolean communicationInterface) {
        pendingFor(level).put(pos.asLong(), communicationInterface);
        level.scheduleTick(pos, this, 1);
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        Boolean shape = pendingFor(level).remove(pos.asLong());
        BlockState next = state;
        if (shape != null && state.hasProperty(COMMUNICATION_INTERFACE)) {
            next = next.setValue(COMMUNICATION_INTERFACE,
                    state.getValue(NEBlock.FORMED) && shape);
        }
        // eco lights these two from a shell cell beside the host and reads them back to decide whether the
        // cluster registers its CPUs through a switch frequency. An L1 machine has no switch block and so
        // no frequency, and leaving them set sends our CPUs into a logical network that does not exist -
        // the machine looks connected but never appears in the network's CPU list. Nothing clears them any
        // more either: our calculator replaces the eco method that would, so a host an older world saved
        // with them set would carry them forever.
        next = pinned(next, ECOComputationSystem.NETWORK_SWITCH);
        next = pinned(next, ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
        if (next != state) {
            level.setBlock(pos, next, Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState pinned(BlockState state, Property<Boolean> bit) {
        return state.hasProperty(bit) && state.getValue(bit) ? state.setValue(bit, false) : state;
    }
}

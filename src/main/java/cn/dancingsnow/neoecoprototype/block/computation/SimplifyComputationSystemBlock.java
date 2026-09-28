package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * C1 computation host. The three properties below carry what the finished machine turned out to
 * contain, so the blockstate file can hand each combination its own formed model instead of making a
 * resource pack read the world. The calculator writes them; they are never set by placement.
 */
public class SimplifyComputationSystemBlock extends ECOComputationSystem {
    /** True when the interface cell holds {@code simplify_computation_network_interface}, the GUI one. */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");
    public static final BooleanProperty ENERGIZED_THREADING_CORE =
            BooleanProperty.create("energized_threading_core");
    public static final BooleanProperty ENERGIZED_PARALLEL_CORE =
            BooleanProperty.create("energized_parallel_core");

    public SimplifyComputationSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(COMMUNICATION_INTERFACE, false)
                .setValue(ENERGIZED_THREADING_CORE, false)
                .setValue(ENERGIZED_PARALLEL_CORE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE, ENERGIZED_THREADING_CORE, ENERGIZED_PARALLEL_CORE);
    }
}

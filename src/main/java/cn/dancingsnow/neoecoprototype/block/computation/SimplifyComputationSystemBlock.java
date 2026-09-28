package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * C1 computation host. {@code communication_interface} carries whether the interface cell holds the
 * communication interface, so the blockstate file can hand that case its own formed model instead of
 * making a resource pack read the world. The calculator writes it; placement never does.
 *
 * <p>Two siblings used to live here - {@code energized_threading_core} and {@code energized_parallel_core}
 * - and both were removed in favour of the members drawing themselves. They never selected different
 * artwork (all eighteen combination models were copies of the plain formed face), so the only thing they
 * bought was a blockstate file of 512 entries where 20 say the same thing, and two more property names
 * for a resource pack to collide with.
 */
public class SimplifyComputationSystemBlock extends ECOComputationSystem {
    /** True when the interface cell holds {@code simplify_computation_network_interface}, the GUI one. */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    public SimplifyComputationSystemBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(COMMUNICATION_INTERFACE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(COMMUNICATION_INTERFACE);
    }
}

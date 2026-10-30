package cn.dancingsnow.neoecoprototype.block.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Window pane of the reactor shell.
 *
 * <p>Same-block faces are skipped: a 5x5 face of panes then draws its border once, around the whole
 * window, instead of once per block in a grid.
 */
public class SimplifyReactorGlassBlock extends Block {

    public SimplifyReactorGlassBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean skipRendering(BlockState state, BlockState adjacentState, Direction side) {
        return adjacentState.is(this) || super.skipRendering(state, adjacentState, side);
    }

    @Override
    protected boolean propagatesSkylightDown(BlockState state, BlockGetter level, BlockPos pos) {
        return true;
    }
}

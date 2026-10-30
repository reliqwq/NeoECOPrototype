package cn.dancingsnow.neoecoprototype.block.reactor;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.reactor.SimplifyReactorControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Front-face controller of the superconductive reactor.
 *
 * <p>Right-click builds the cube (or reports why it cannot), shift-right-click reports the current
 * state. Its front faces the player who places it, which is also the direction the validator treats
 * as outward.
 */
public class SimplifyReactorControllerBlock
        extends AEBaseEntityBlock<SimplifyReactorControllerBlockEntity> {

    public SimplifyReactorControllerBlock(Properties properties) {
        super(properties);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof SimplifyReactorControllerBlockEntity controller)) {
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        if (!level.isClientSide && player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                controller.reportStatus(serverPlayer);
            } else {
                controller.requestBuild(serverPlayer);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighbourBlock,
                                   BlockPos neighbourPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighbourBlock, neighbourPos, movedByPiston);
        if (!level.isClientSide
                && level.getBlockEntity(pos) instanceof SimplifyReactorControllerBlockEntity controller) {
            controller.requestScan();
        }
    }
}

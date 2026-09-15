package cn.dancingsnow.neoecoprototype.block.trinity;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Central controller for the first-generation Trinity multiblock shell. */
public class SimplifyTrinityControllerBlock extends NEBlock<SimplifyTrinityControllerBlockEntity>
        implements BlockUIMenuType.BlockUI {
    public SimplifyTrinityControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any().setValue(FORMED, false));
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               net.minecraft.world.phys.BlockHitResult hit) {
        if (!level.isClientSide && player instanceof ServerPlayer serverPlayer
                && level.getBlockEntity(pos) instanceof SimplifyTrinityControllerBlockEntity controller) {
            if (serverPlayer.isShiftKeyDown()) {
                controller.validateNow();
                serverPlayer.sendSystemMessage(controller.getStatusMessage());
            } else {
                controller.validateNow();
                serverPlayer.sendSystemMessage(net.minecraft.network.chat.Component.literal(
                        "Trinity: opening overview at " + pos));
                BlockUIMenuType.openUI(serverPlayer, pos);
            }
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (isPlayerCloseEnough(holder.player.level(), holder.pos, holder.player)
                && holder.player.level().getBlockEntity(holder.pos) instanceof SimplifyTrinityControllerBlockEntity controller) {
            return controller.createUI(holder);
        }
        return null;
    }

    @Override
    public boolean stillValid(BlockUIMenuType.BlockUIHolder holder) {
        return BlockUIMenuType.BlockUI.super.stillValid(holder)
                && isPlayerCloseEnough(holder.player.level(), holder.pos, holder.player);
    }

    private static boolean isPlayerCloseEnough(Level level, BlockPos pos, Player player) {
        return player.level() == level
                && level.getBlockState(pos).getBlock() instanceof SimplifyTrinityControllerBlock
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}

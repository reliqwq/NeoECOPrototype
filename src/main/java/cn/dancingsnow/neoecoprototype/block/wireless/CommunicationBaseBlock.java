package cn.dancingsnow.neoecoprototype.block.wireless;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.wireless.CommunicationCoreBlockEntity;
import cn.dancingsnow.neoecoprototype.items.WirelessChannelModuleUI;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The communication core's base block, and the only block in the column that carries the relay.
 *
 * <p>A plain right-click opens the same panel the subsystem channel module has - the tower
 * {@code instanceof}s straight into {@code WirelessChannelModuleUI}, so its ports and booster slots
 * read exactly where a player already learned to look. A sneak right-click with an empty hand asks
 * the tower to build itself: the column is small enough that a dedicated builder screen would cost
 * more than the one gesture it replaces, and the feedback comes back through the same multiblock
 * message keys the L1 rooms and Trinity already speak.
 *
 * <p>The card's gestures are all sneak-right-clicks too, but a card is an item - its useOn runs
 * first and answers PASS on the base, so the two never compete for the same click.
 */
public class CommunicationBaseBlock extends AEBaseEntityBlock<CommunicationCoreBlockEntity>
        implements BlockUIMenuType.BlockUI {

    public CommunicationBaseBlock(Properties properties) {
        super(properties);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(BlockStateProperties.HORIZONTAL_FACING,
                rotation.rotate(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(BlockStateProperties.HORIZONTAL_FACING)));
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return holder.player.level().getBlockEntity(holder.pos)
                instanceof CommunicationCoreBlockEntity tower
                ? WirelessChannelModuleUI.create(tower, holder.player)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (!(level.getBlockEntity(pos) instanceof CommunicationCoreBlockEntity tower)) {
            return InteractionResult.FAIL;
        }
        if (player.isShiftKeyDown()) {
            if (player instanceof ServerPlayer serverPlayer) {
                tower.requestBuild(serverPlayer);
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}

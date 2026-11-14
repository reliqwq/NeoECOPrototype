package cn.dancingsnow.neoecoprototype.block.wireless;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.wireless.WirelessChannelModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.items.WirelessChannelModuleUI;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The module's block. A plain right-click opens its panel; the card's gestures are all sneak-right-clicks, so
 * the two never compete for the same click.
 */
public final class WirelessChannelModuleBlock extends AEBaseEntityBlock<WirelessChannelModuleBlockEntity>
        implements BlockUIMenuType.BlockUI {

    public WirelessChannelModuleBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        return holder.player.level().getBlockEntity(holder.pos)
                instanceof WirelessChannelModuleBlockEntity module
                ? WirelessChannelModuleUI.create(module, holder.player)
                : null;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (getBlockEntity(level, pos) == null) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            BlockUIMenuType.openUI(serverPlayer, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }
}

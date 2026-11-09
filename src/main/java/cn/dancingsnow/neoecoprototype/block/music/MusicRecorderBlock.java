package cn.dancingsnow.neoecoprototype.block.music;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.music.MusicRecorderBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.music.MusicRecorderUI;
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
 * The recorder's block: right-click opens the machine. It is a plain block entity rather than a networked
 * one - recording is local playback, and the only thing that talks to a grid is the jukebox the finished
 * disc is played in.
 */
public final class MusicRecorderBlock extends AEBaseEntityBlock<MusicRecorderBlockEntity>
        implements BlockUIMenuType.BlockUI {

    public MusicRecorderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof MusicRecorderBlockEntity host) {
            return MusicRecorderUI.create(holder, host);
        }
        return null;
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

package cn.dancingsnow.neoecoprototype.block.music;

import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.music.MusicJukeboxBlockEntity;
import cn.dancingsnow.neoecoprototype.items.MusicDataDiscItem;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The player's door into the music key: right-click with a data disc to put it on, right-click empty-handed
 * to take it back off. There is no screen - the machine has one slot and no settings.
 *
 * <p>It is a block rather than a drive cell on purpose. A disc mounted in a drive would have to be read by
 * the drive's ticker, and a player who wanted to listen would need the drive to be online; here the music
 * plays where the block stands, and the ticks it pays go into whatever network the block is cabled to.
 *
 * <p>{@code HAS_RECORD} is the only state, and it exists for the model (a disc sits on the spindle); the
 * block entity owns the disc itself.
 */
public final class MusicJukeboxBlock extends AEBaseEntityBlock<MusicJukeboxBlockEntity> {

    public static final BooleanProperty HAS_RECORD = BlockStateProperties.HAS_RECORD;

    public MusicJukeboxBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(HAS_RECORD, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HAS_RECORD);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack held, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (!(held.getItem() instanceof MusicDataDiscItem)) {
            return super.useItemOn(held, state, level, pos, player, hand, hit);
        }
        if (level.getBlockEntity(pos) instanceof MusicJukeboxBlockEntity jukebox) {
            if (!level.isClientSide()) {
                jukebox.insertDisc(held, player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        return ItemInteractionResult.FAIL;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos,
                                               Player player, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof MusicJukeboxBlockEntity jukebox) {
            if (!level.isClientSide()) {
                jukebox.ejectDisc();
            }
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }
}

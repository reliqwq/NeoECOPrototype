package cn.dancingsnow.neoecoprototype.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class EnergizedMEChestBlock extends AEBaseEntityBlock<EnergizedMEChestBlockEntity> {
    public static final EnumProperty<EnergizedMEChestBlockEntity.DisplayState> CELL_STATE =
            EnumProperty.create("cell_state", EnergizedMEChestBlockEntity.DisplayState.class);

    public EnergizedMEChestBlock() {
        super(Properties.of()
                .mapColor(MapColor.METAL)
                .strength(3.0F, 12.0F)
                .sound(SoundType.METAL)
                .noOcclusion()
                .requiresCorrectToolForDrops());
        registerDefaultState(defaultBlockState().setValue(CELL_STATE, EnergizedMEChestBlockEntity.DisplayState.OFF));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(CELL_STATE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState state, EnergizedMEChestBlockEntity blockEntity) {
        return state.setValue(CELL_STATE, blockEntity.getDisplayState());
    }

    @Override
    public BlockState playerWillDestroy(Level level, BlockPos pos, BlockState state, Player player) {
        if (level.getBlockEntity(pos) instanceof EnergizedMEChestBlockEntity chest) {
            chest.preparePlayerBreak(player.isCreative());
        }
        return super.playerWillDestroy(level, pos, state, player);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof EnergizedMEChestBlockEntity chest)) {
            return super.useWithoutItem(state, level, pos, player, hitResult);
        }
        if (!level.isClientSide) {
            if (hitResult.getDirection() == chest.getTop()) {
                chest.openTerminal(player);
            } else {
                chest.openCellMenu(player);
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

}

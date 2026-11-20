package cn.dancingsnow.neoecoprototype.block.crafting;

import appeng.block.AEBaseEntityBlock;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;

/**
 * The L1 assembler as its own block, in the shape ExtendedAE proved: an {@code AEBaseEntityBlock} with a
 * {@code powered} flag the block entity drives, no inheritance from AE2's molecular assembler anywhere.
 * The blockstate JSON maps every variant to one model, so the property is state for Jade and the TESR
 * rather than a visual switch.
 */
public class SimplifyStonecuttingAssemblerBlock extends AEBaseEntityBlock<SimplifyStonecuttingAssemblerBlockEntity> {
    public static final BooleanProperty POWERED = BooleanProperty.create("powered");

    public SimplifyStonecuttingAssemblerBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(POWERED, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<
            net.minecraft.world.level.block.Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(POWERED);
    }

    @Override
    protected BlockState updateBlockStateFromBlockEntity(BlockState state,
            SimplifyStonecuttingAssemblerBlockEntity blockEntity) {
        return state.setValue(POWERED, blockEntity.isPowered());
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!level.isClientSide) {
            var blockEntity = getBlockEntity(level, pos);
            if (blockEntity != null) {
                MenuOpener.open(ProcessorAssemblerMenu.type(), player, MenuLocators.forBlockEntity(blockEntity));
            }
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }
}

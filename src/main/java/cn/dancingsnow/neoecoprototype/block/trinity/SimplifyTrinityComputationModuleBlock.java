package cn.dancingsnow.neoecoprototype.block.trinity;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.BlockHitResult;

/**
 * Computation wing of the Trinity machine. Hosts one computation cell:
 * right click with a cell inserts it, sneak + right click removes it.
 */
public class SimplifyTrinityComputationModuleBlock extends NEBlock<SimplifyTrinityComputationModuleBlockEntity> {
    public SimplifyTrinityComputationModuleBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH));
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack heldItem, BlockState state, Level level, BlockPos pos,
                                              Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof SimplifyTrinityComputationModuleBlockEntity be
                && be.isItemValid(heldItem) && !be.hasCell()) {
            if (level.isClientSide) {
                return ItemInteractionResult.SUCCESS;
            }
            if (!be.insertCell(heldItem)) {
                return ItemInteractionResult.FAIL;
            }
            if (!player.isCreative()) {
                heldItem.shrink(1);
                if (heldItem.isEmpty()) {
                    player.setItemInHand(hand, ItemStack.EMPTY);
                }
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide());
        }
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof SimplifyTrinityComputationModuleBlockEntity be
                && !be.hasCell() && !heldItem.isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.trinity_computation_module.invalid_cell"), true);
            return ItemInteractionResult.FAIL;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof SimplifyTrinityComputationModuleBlockEntity be) {
            if (be.hasCell() && player.isShiftKeyDown()) {
                if (level.isClientSide) {
                    return InteractionResult.SUCCESS;
                }
                ItemStack removed = be.getCellStack();
                be.setCellStack(ItemStack.EMPTY);
                if (!removed.isEmpty()) {
                    player.setItemInHand(InteractionHand.MAIN_HAND, removed);
                }
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
            if (!player.isShiftKeyDown() && be.openControllerUi(player)) {
                return InteractionResult.sidedSuccess(level.isClientSide());
            }
        }
        return InteractionResult.PASS;
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }
}

package cn.dancingsnow.neoecoprototype.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
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
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/** L1 storage subsystem host (controller) block. */
public class SimplifyStorageControllerBlock extends NEBlock<SimplifyStorageHostBlockEntity>
        implements BlockUIMenuType.BlockUI {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final BooleanProperty MIRRORED = BooleanProperty.create("mirrored");
    /**
     * The interface cell holds the block named "...Communication Interface" rather than the plain
     * one. Set by the calculator when the machine forms; a resource pack can pick a formed model per
     * value instead of reading the world.
     */
    public static final BooleanProperty COMMUNICATION_INTERFACE =
            BooleanProperty.create("communication_interface");

    public SimplifyStorageControllerBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(FACING, Direction.NORTH)
                .setValue(MIRRORED, false)
                .setValue(COMMUNICATION_INTERFACE, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(MIRRORED, COMMUNICATION_INTERFACE);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hitResult) {
        if (!isPlayerCloseEnough(level, pos, player)) {
            return InteractionResult.FAIL;
        }
        if (player instanceof ServerPlayer serverPlayer) {
            if (serverPlayer.isShiftKeyDown()) {
                if (level.getBlockEntity(pos) instanceof SimplifyStorageHostBlockEntity host) {
                    if (host.getCluster() != null) {
                        host.breakCluster();
                    }
                    host.rebuildMultiblock();
                }
                return InteractionResult.CONSUME;
            }
            BlockUIMenuType.openUI(serverPlayer, pos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (isPlayerCloseEnough(holder.player.level(), holder.pos, holder.player)
                && holder.player.level().getBlockEntity(holder.pos) instanceof SimplifyStorageHostBlockEntity host) {
            return host.createUI(holder);
        }
        return null;
    }

    @Override
    public boolean stillValid(BlockUIMenuType.BlockUIHolder holder) {
        return BlockUIMenuType.BlockUI.super.stillValid(holder)
                && isPlayerCloseEnough(holder.player.level(), holder.pos, holder.player);
    }

    public static boolean isPlayerCloseEnough(Level level, BlockPos pos, Player player) {
        return player.level() == level
                && level.getBlockState(pos).getBlock() instanceof SimplifyStorageControllerBlock
                && player.distanceToSqr(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D) <= 64.0D;
    }
}

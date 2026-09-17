package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import appeng.api.orientation.BlockOrientation;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Base for Trinity's dedicated module blocks.
 *
 * <p>Every Trinity member -- controller and modules alike -- uses
 * {@link SimplifyTrinityClusterCalculator}, which is what makes them belong to one single
 * {@link SimplifyTrinityCluster}. This mirrors the upstream
 * {@code NEIntegratedWorkingStation*} pattern: a combined machine needs its own block set that
 * shares one calculator, because eco's stock parts hard-bind to their own subsystem cluster
 * (storage / computation / crafting) in their constructors and can never be adopted by Trinity.
 */
public abstract class SimplifyTrinityModuleBlockEntity<E extends SimplifyTrinityModuleBlockEntity<E>>
        extends NEBlockEntity<SimplifyTrinityCluster, E> {
    protected SimplifyTrinityModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state, SimplifyTrinityClusterCalculator::new);
    }

    @Override
    public java.util.Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        if (!isFormed()) {
            return java.util.Set.of();
        }
        return java.util.EnumSet.allOf(Direction.class);
    }

    /** Opens the controller UI from a reachable outer module. */
    public final boolean openControllerUi(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer) || !(level instanceof net.minecraft.server.level.ServerLevel serverLevel)) {
            return false;
        }
        SimplifyTrinityControllerBlockEntity controller = null;
        SimplifyTrinityCluster cluster = getCluster();
        if (cluster != null) {
            controller = cluster.getController();
        }
        if (controller == null) {
            BlockPos center = getBlockPos();
            for (int dx = -1; dx <= 1 && controller == null; dx++) {
                for (int dy = -1; dy <= 1 && controller == null; dy++) {
                    for (int dz = -1; dz <= 1; dz++) {
                        BlockEntity candidate = serverLevel.getBlockEntity(center.offset(dx, dy, dz));
                        if (candidate instanceof SimplifyTrinityControllerBlockEntity found) {
                            controller = found;
                            break;
                        }
                    }
                }
            }
        }
        if (controller == null) {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.trinity_module.controller_not_found"), true);
            return false;
        }
        BlockUIMenuType.openUI(serverPlayer, controller.getBlockPos());
        return true;
    }
}

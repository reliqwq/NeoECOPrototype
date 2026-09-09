package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.233, Eco 21.2.0-preview11.
 * Last verified: 2026-09-08. Upgrade check: verifyInternalStructure and target accessor.
 */

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEComputationClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyComputationClusterCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes L1 structure checks through the addon geometry while leaving Eco tiers unchanged. */
@Mixin(NEComputationClusterCalculator.class)
public abstract class NEComputationClusterCalculatorMixin {
    @Inject(method = "verifyInternalStructure", at = @At("HEAD"), cancellable = true)
    private void neoecoprototype$l1Structure(
            ServerLevel level,
            BlockPos min,
            BlockPos max,
            CallbackInfoReturnable<Boolean> cir) {
        if (containsSimplifyController(level, min, max)) {
            NEBlockEntity<NEComputationCluster, ?> target =
                    (NEBlockEntity<NEComputationCluster, ?>) ((MBCalculatorTargetAccessor) (Object) this)
                            .neoecoprototype$getTarget();
            cir.setReturnValue(new SimplifyComputationClusterCalculator(target)
                    .verifyInternalStructure(level, min, max));
        }
    }

    private static boolean containsSimplifyController(ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ECOComputationSystemBlockEntity controller
                    && controller.getTier() instanceof SimplifyTier) {
                return true;
            }
        }
        return false;
    }
}

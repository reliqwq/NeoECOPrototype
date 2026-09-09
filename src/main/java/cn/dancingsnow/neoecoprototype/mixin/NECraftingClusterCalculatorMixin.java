package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.233, Eco 21.2.0-preview12.
 * Last verified: 2026-09-08. Upgrade check: verifyInternalStructure and target accessor.
 */

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyCraftingClusterCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Routes L1 crafting structure checks through the addon block set. */
@Mixin(NECraftingClusterCalculator.class)
public abstract class NECraftingClusterCalculatorMixin {
    @Inject(method = "verifyInternalStructure", at = @At("HEAD"), cancellable = true)
    private void neoecoprototype$l1Structure(
            ServerLevel level,
            BlockPos min,
            BlockPos max,
            CallbackInfoReturnable<Boolean> cir) {
        if (!containsSimplifyController(level, min, max)) {
            return;
        }
        @SuppressWarnings("unchecked")
        NEBlockEntity<NECraftingCluster, ?> target =
                (NEBlockEntity<NECraftingCluster, ?>) ((MBCalculatorTargetAccessor) (Object) this)
                        .neoecoprototype$getTarget();
        cir.setReturnValue(new SimplifyCraftingClusterCalculator(target)
                .verifyInternalStructure(level, min, max));
    }

    private static boolean containsSimplifyController(ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos pos : BlockPos.betweenClosed(min, max)) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof ECOCraftingSystemBlockEntity controller
                    && controller.getTier() instanceof SimplifyCraftingTier) {
                return true;
            }
        }
        return false;
    }
}

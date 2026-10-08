package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.251, AE2 19.2.17, Eco 21.2.1. That host class still declares
 * public MultiBlockDefinition getBuildDefinition() and no longer overrides onReady(), so the definition
 * is the only thing taken from it.
 */

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/** Supplies the L1 placement definition to Eco's exact computation host class. */
@Mixin(ECOComputationSystemBlockEntity.class)
public abstract class ECOComputationSystemBlockEntityMixin {
    @Shadow
    @Final
    private IECOTier tier;

    @Inject(method = "getBuildDefinition", at = @At("HEAD"), cancellable = true)
    private void neoecoprototype$l1Definition(CallbackInfoReturnable<MultiBlockDefinition> cir) {
        // Compared against the one constant rather than the enum type: SimplifyTier also carries
        // L1_REINFORCED, L1_ENERGIZED_THREADING and L1_PARALLEL_SWITCH, which belong to members and cells.
        if (tier == SimplifyTier.L1) {
            cir.setReturnValue(SimplifyComputationDefinition.L1);
        }
    }
}

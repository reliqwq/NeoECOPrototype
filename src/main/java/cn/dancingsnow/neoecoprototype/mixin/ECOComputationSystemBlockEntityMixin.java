package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.251, AE2 19.2.17, Eco 21.2.0-beta5.
 * beta5 keeps getBuildDefinition but no longer exposes the old onReady hook.
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
        if (tier instanceof SimplifyTier) {
            cir.setReturnValue(SimplifyComputationDefinition.L1);
        }
    }
}

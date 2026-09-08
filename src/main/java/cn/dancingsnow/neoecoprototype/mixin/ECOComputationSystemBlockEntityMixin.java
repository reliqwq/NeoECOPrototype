package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.233, AE2 19.2.17, Eco 21.2.0-preview10.
 * Last verified: 2026-09-08. Upgrade check: getBuildDefinition/onReady signatures.
 */

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
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

    @Inject(method = "onReady", at = @At("RETURN"))
    private void neoecoprototype$l1IdlePower(CallbackInfo ci) {
        if (tier instanceof SimplifyTier) {
            ((ECOComputationSystemBlockEntity) (Object) this).getMainNode()
                    .setIdlePowerUsage(SimplifyPowerProfile.L1.computationControllerIdlePower());
        }
    }
}

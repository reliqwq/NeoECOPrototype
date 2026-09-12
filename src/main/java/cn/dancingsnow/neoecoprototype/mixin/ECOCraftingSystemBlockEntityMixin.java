package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.233, AE2 19.2.17, Eco 21.2.0-preview12.
 * Last verified: 2026-09-08. Upgrade check: getBuildDefinition/getCoolingRecipe/onReady.
 */

import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ECOCraftingSystemBlockEntity.class)
public abstract class ECOCraftingSystemBlockEntityMixin {
    @Inject(method = "getBuildDefinition", at = @At("HEAD"), cancellable = true)
    private void neoecoprototype$l1Definition(CallbackInfoReturnable<MultiBlockDefinition> cir) {
        ECOCraftingSystemBlockEntity self = (ECOCraftingSystemBlockEntity) (Object) this;
        if (self.getTier() instanceof SimplifyCraftingTier) {
            cir.setReturnValue(SimplifyCraftingDefinition.L1);
        }
    }

    @Inject(method = "onReady", at = @At("RETURN"))
    private void neoecoprototype$l1IdlePower(CallbackInfo ci) {
        ECOCraftingSystemBlockEntity self = (ECOCraftingSystemBlockEntity) (Object) this;
        if (self.getTier() instanceof SimplifyCraftingTier) {
            self.getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.craftingControllerIdlePower());
        }
    }
}

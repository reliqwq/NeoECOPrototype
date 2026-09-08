package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview10. Check crafting component #onReady methods. */

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingVentBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingWorkerBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scales Eco's 64 AE/t crafting component baseline for addon-owned L1 parts. */
@Mixin({
        ECOCraftingParallelCoreBlockEntity.class,
        ECOCraftingVentBlockEntity.class,
        ECOCraftingWorkerBlockEntity.class
})
public abstract class ECOCraftingHighPowerMixin {
    @Inject(method = "onReady", at = @At("RETURN"))
    private void neoecoprototype$l1HighComponentPower(CallbackInfo ci) {
        NEBlockEntity<?, ?> self = (NEBlockEntity<?, ?>) (Object) this;
        if (NeoECOPrototype.MOD_ID.equals(
                BuiltInRegistries.BLOCK.getKey(self.getBlockState().getBlock()).getNamespace())) {
            self.getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.highIdleComponentPower());
        }
    }
}

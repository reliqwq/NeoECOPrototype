package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview10. Check NEBlockEntity#onReady return injection. */

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Scales Eco's shared 16 AE/t component baseline for addon-owned L1 parts. */
@Mixin(NEBlockEntity.class)
public abstract class NEBlockEntityPowerMixin {
    @Inject(method = "onReady", at = @At("RETURN"))
    private void neoecoprototype$l1BaseComponentPower(CallbackInfo ci) {
        NEBlockEntity<?, ?> self = (NEBlockEntity<?, ?>) (Object) this;
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(self.getBlockState().getBlock());
        if (NeoECOPrototype.MOD_ID.equals(blockId.getNamespace())) {
            self.getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.baseComponentIdlePower());
        }
    }
}

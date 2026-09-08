package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview10. */

import cn.dancingsnow.neoecoae.gui.crafting.ClientUIBridge;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.loading.FMLEnvironment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Supplier;

/** Uses eco's common UI fallback before its reflective client bridge runs on a dedicated server. */
@Mixin(ClientUIBridge.class)
public abstract class ECOClientUIBridgeMixin {
    @Inject(method = "call", at = @At("HEAD"), cancellable = true)
    private static void neoecoprototype$dedicatedServerFallback(
            String methodName,
            Class<?> parameterType,
            Object argument,
            Class<?> resultType,
            Supplier<?> fallback,
            CallbackInfoReturnable<Object> cir) {
        if (FMLEnvironment.dist == Dist.DEDICATED_SERVER) {
            cir.setReturnValue(fallback.get());
        }
    }
}

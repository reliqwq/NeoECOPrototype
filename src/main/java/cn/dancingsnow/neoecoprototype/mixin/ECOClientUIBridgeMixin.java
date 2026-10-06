package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251, eco 21.2.1-beta2. Re-checked against those jars on 2026-10-06:
 * ClientUIBridge.call is still the five-argument static (String, Class<?>, Object, Class<T>,
 * Supplier<T>), which is what the handler's parameter list below has to match. */

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

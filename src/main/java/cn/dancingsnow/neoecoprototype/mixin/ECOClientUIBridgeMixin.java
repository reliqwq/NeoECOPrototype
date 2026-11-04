package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251, eco 21.2.15-beta. Re-checked against that jar on 2026-11-04:
 * ClientUIBridge.call is still the five-argument static (String, Class<?>, Object, Class<T>,
 * Supplier<T>), which is what the handler's parameter list below has to match; it still opens with
 * Class.forName (offset 2, target CraftingInterfaceClientUI) with no side check; and its exception table
 * covers offsets 0-46 with ReflectiveOperationException and LinkageError only. NeoForge's
 * "invalid dist DEDICATED_SERVER" is a plain RuntimeException, so it escapes that table and eco's own
 * fallback (offset 52) is unreachable on a server - which is exactly what this mixin reaches for it.
 * Retire this when either reading changes: the table gains RuntimeException, or the method starts with a
 * dist check. Both are readable straight off javap -c, so this is a condition, not a discussion.
 * Earlier checks: 21.2.11 on 2026-10-27, 21.2.10 on 2026-10-25, 21.2.9 on 2026-10-22, 21.2.1-beta2 on
 * 2026-10-06, the 21.2.1 release on 2026-10-08. */

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

package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.233, Eco 21.2.0-preview12. Check ComputationInterfaceUI#title.
 *
 * Optional like the other two title redirects: a renamed or removed title() must cost us the
 * localised title, not crash eco's GUI for players on another eco build. LocalGuiTitleContext logs
 * when none of them fire.
 */

import cn.dancingsnow.neoecoprototype.gui.LocalGuiTitleContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "cn.dancingsnow.neoecoae.gui.computation.ComputationInterfaceUI")
public abstract class ECOComputationInterfaceUIMixin {
    @Redirect(
            method = "title",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            require = 0, expect = 0)
    private static MutableComponent neoecoprototype$localTitle(String key) {
        return Component.translatable(LocalGuiTitleContext.replace(key));
    }
}

package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.251, Eco 21.2.0-beta4.
 *
 * The title is built inside a compiler generated lambda whose name carries an index, so this pin
 * breaks whenever eco adds a lambda to the enclosing method: beta5 renumbered
 * StorageInterfaceUI#create and the redirect silently stopped matching. require = 0 keeps that miss
 * from crashing eco's GUI on another build - it costs the localised title only, and
 * LocalGuiTitleContext logs it. Matching any index by regex was tried against beta6 and crashed the
 * client instead: the selector also picks up lambdas whose translatable call has a different
 * signature, and @Redirect rejects those as an invalid target descriptor no matter what
 * require is set to.
 */

import cn.dancingsnow.neoecoprototype.gui.LocalGuiTitleContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "cn.dancingsnow.neoecoae.gui.storage.StorageInterfaceUI")
public abstract class ECOStorageInterfaceUIMixin {
    @Redirect(
            method = "lambda$create$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            require = 0, expect = 0)
    private static MutableComponent neoecoprototype$localTitle(String key) {
        return Component.translatable(LocalGuiTitleContext.replace(key));
    }
}

package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview11. Check CraftingInterfaceUI#create lambda. */

import cn.dancingsnow.neoecoprototype.gui.LocalGuiTitleContext;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(targets = "cn.dancingsnow.neoecoae.gui.crafting.CraftingInterfaceUI")
public abstract class ECOCraftingInterfaceUIMixin {
    @Redirect(
            method = "lambda$create$1",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            require = 1)
    private static MutableComponent neoecoprototype$localTitle(String key) {
        return Component.translatable(LocalGuiTitleContext.replace(key));
    }
}

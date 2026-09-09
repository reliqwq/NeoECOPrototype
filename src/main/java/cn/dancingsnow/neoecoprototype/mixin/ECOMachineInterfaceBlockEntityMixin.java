package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview11. Check ECOMachineInterfaceBlockEntity#createUI. */

import cn.dancingsnow.neoecoprototype.gui.LocalGuiTitleContext;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ECOMachineInterfaceBlockEntity.class)
public abstract class ECOMachineInterfaceBlockEntityMixin {
    @Inject(method = "createUI", at = @At("HEAD"), require = 1)
    private void neoecoprototype$begin(CallbackInfoReturnable<ModularUI> callback) {
        LocalGuiTitleContext.begin((ECOMachineInterfaceBlockEntity<?>) (Object) this);
    }

    @Inject(method = "createUI", at = @At("RETURN"), require = 1)
    private void neoecoprototype$end(CallbackInfoReturnable<ModularUI> callback) {
        LocalGuiTitleContext.end();
    }
}

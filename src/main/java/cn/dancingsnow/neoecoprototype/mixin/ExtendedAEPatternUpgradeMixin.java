package cn.dancingsnow.neoecoprototype.mixin;

import cn.dancingsnow.neoecoprototype.integration.ae2.PatternProviderUpgradeTarget;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Pseudo
@Mixin(targets = "com.glodblock.github.extendedae.common.items.ItemUpgrade", remap = false)
public abstract class ExtendedAEPatternUpgradeMixin {
    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Ljava/lang/Object;getClass()Ljava/lang/Class;"))
    private Class<?> neoecoprototype$matchAddonUpgradeTargets(Object target) {
        return PatternProviderUpgradeTarget.normalize(target.getClass());
    }

    @Redirect(method = "useOn", at = @At(value = "INVOKE", target = "Lappeng/api/parts/IPart;getClass()Ljava/lang/Class;"))
    private Class<?> neoecoprototype$matchAddonPartUpgradeTargets(appeng.api.parts.IPart target) {
        return PatternProviderUpgradeTarget.normalize(target.getClass());
    }
}

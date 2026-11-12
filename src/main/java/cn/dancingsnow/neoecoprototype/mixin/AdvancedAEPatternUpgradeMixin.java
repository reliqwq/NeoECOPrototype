package cn.dancingsnow.neoecoprototype.mixin;

import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;
import com.llamalad7.mixinextras.sugar.Local;
import net.neoforged.fml.ModList;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Coerce;

@Pseudo
@Mixin(targets = "net.pedroksl.advanced_ae.common.items.AdvPatternProviderUpgradeItem", remap = false)
public abstract class AdvancedAEPatternUpgradeMixin {
    @Redirect(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/pedroksl/advanced_ae/xmod/Addons;isLoaded()Z"))
    private boolean neoecoprototype$keepExtendedAeGateForL1(@Coerce Object addon,
                                                              @Local(ordinal = 0) Class<?> targetType) {
        return ModList.get().isLoaded("extendedae")
                || targetType == SimplifyPatternProviderBlockEntity.class
                || targetType == SimplifyPatternProviderPart.class;
    }

    @Redirect(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/pedroksl/advanced_ae/xmod/eae/ExtendedAEPlugin;isEntityProvider(Ljava/lang/Class;)Z"))
    private boolean neoecoprototype$includeL1BlockProvider(Class<?> type) {
        return type == SimplifyPatternProviderBlockEntity.class
                || type.getName().equals("com.glodblock.github.extendedae.common.tileentities.TileExPatternProvider");
    }

    @Redirect(method = "useOn", at = @At(value = "INVOKE",
            target = "Lnet/pedroksl/advanced_ae/xmod/eae/ExtendedAEPlugin;isPartProvider(Ljava/lang/Class;)Z"))
    private boolean neoecoprototype$includeL1PartProvider(Class<?> type) {
        return type == SimplifyPatternProviderPart.class
                || type.getName().equals("com.glodblock.github.extendedae.common.parts.PartExPatternProvider");
    }
}

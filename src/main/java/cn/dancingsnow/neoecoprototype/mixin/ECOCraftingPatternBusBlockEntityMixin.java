package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview11. Check headerRow argument layout. */

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(ECOCraftingPatternBusBlockEntity.class)
public abstract class ECOCraftingPatternBusBlockEntityMixin {
    @ModifyArg(
            method = "headerRow",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/network/chat/Component;translatable(Ljava/lang/String;)Lnet/minecraft/network/chat/MutableComponent;"),
            index = 0)
    private String neoecoprototype$localPatternBusTitle(String key) {
        ECOCraftingPatternBusBlockEntity blockEntity = (ECOCraftingPatternBusBlockEntity) (Object) this;
        if (blockEntity.getBlockState().is(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get())) {
            return "block.neoecoprototype.simplify_crafting_pattern_bus";
        }
        return key;
    }
}

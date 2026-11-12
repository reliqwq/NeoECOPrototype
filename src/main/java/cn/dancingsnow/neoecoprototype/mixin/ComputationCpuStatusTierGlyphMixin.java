package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251, eco 21.2.16. */

import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Pseudo
@Mixin(targets = "cn.dancingsnow.neoecoae.gui.computation.ComputationCpuStatusPanel", remap = false)
public abstract class ComputationCpuStatusTierGlyphMixin {
    private static final ResourceLocation L1_GLYPH = ResourceLocation.fromNamespaceAndPath(
            "neoecoprototype", "textures/gui/panel/crafting_tier.png");

    @Shadow
    private int tier;

    @Shadow
    private boolean tierBuiltin;

    @Shadow
    private Component tierName;

    @Inject(method = "drawTier", at = @At("HEAD"), cancellable = true, require = 1)
    private void neoecoprototype$drawL1TierGlyph(GUIContext context, Font font, float x, float y,
                                                  CallbackInfo ci) {
        if (!this.tierBuiltin && this.tier == 0 && "L1".equals(this.tierName.getString())) {
            context.drawTexture(SpriteTexture.of(L1_GLYPH).setSprite(0, 0, 13, 18),
                    x + 134, y + 5, 13, 18);
            ci.cancel();
        }
    }
}

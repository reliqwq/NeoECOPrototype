package cn.dancingsnow.neoecoprototype.client.key;

import appeng.api.client.AEKeyRenderHandler;
import appeng.api.client.AEKeyRendering;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicKey;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicKeyType;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Draws music data with one 16x16 icon in GUIs and on block faces, so the key reads the same in
 * terminals, cells and the Data Energistics interfaces.
 */
public final class MusicKeyRenderer implements AEKeyRenderHandler<MusicKey> {
    private static final ResourceLocation ICON =
            NeoECOPrototype.id("textures/item/music_data.png");

    private MusicKeyRenderer() {
    }

    /** Called once from client setup, and only while Data Energistics is present. */
    public static void register() {
        if (AEKeyRendering.get(MusicKeyType.TYPE) == null) {
            AEKeyRendering.register(MusicKeyType.TYPE, MusicKey.class, new MusicKeyRenderer());
        }
    }

    /** The one icon this key is drawn with, shared with JEI so the two views cannot drift apart. */
    public static ResourceLocation icon() {
        return ICON;
    }

    @Override
    public void drawInGui(Minecraft minecraft, GuiGraphics guiGraphics, int x, int y, MusicKey key) {
        guiGraphics.blit(ICON, x, y, 0, 0, 16, 16);
    }

    @Override
    public void drawOnBlockFace(PoseStack poseStack, MultiBufferSource buffers, MusicKey key, float scale,
                                int light, Level level) {
        float half = (scale - 0.05F) / 2.0F;
        PoseStack.Pose pose = poseStack.last();
        VertexConsumer buffer = buffers.getBuffer(RenderType.entityCutout(ICON));
        quad(buffer, pose, -half, -half, half, half, 0.01F, light);
    }

    private static void quad(VertexConsumer buffer, PoseStack.Pose pose, float left, float top,
                             float right, float bottom, float depth, int light) {
        buffer.addVertex(pose, left, bottom, depth).setColor(0xFFFFFFFF).setUv(0.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(pose, right, bottom, depth).setColor(0xFFFFFFFF).setUv(1.0F, 1.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(pose, right, top, depth).setColor(0xFFFFFFFF).setUv(1.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
        buffer.addVertex(pose, left, top, depth).setColor(0xFFFFFFFF).setUv(0.0F, 0.0F)
                .setOverlay(OverlayTexture.NO_OVERLAY).setLight(light).setNormal(pose, 0.0F, 0.0F, 1.0F);
    }

    @Override
    public Component getDisplayName(MusicKey key) {
        return key.getDisplayName();
    }
}

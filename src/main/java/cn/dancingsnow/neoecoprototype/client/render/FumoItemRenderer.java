package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.component.ResolvableProfile;

import java.util.EnumSet;
import java.util.Set;

/** Renders the doll in item form: inventory, hand, ground drop and item frame. */
public class FumoItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static final Set<ItemDisplayContext> HAND_CONTEXTS = EnumSet.of(
            ItemDisplayContext.FIRST_PERSON_LEFT_HAND, ItemDisplayContext.FIRST_PERSON_RIGHT_HAND,
            ItemDisplayContext.THIRD_PERSON_LEFT_HAND, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND);
    private static FumoItemRenderer instance;

    public static FumoItemRenderer get() {
        if (instance == null) {
            instance = new FumoItemRenderer(
                    Minecraft.getInstance().getBlockEntityRenderDispatcher(),
                    Minecraft.getInstance().getEntityModels());
        }
        return instance;
    }

    private final FumoModel classic;
    private final FumoModel slim;

    private FumoItemRenderer(BlockEntityRenderDispatcher dispatcher, EntityModelSet models) {
        super(dispatcher, models);
        classic = new FumoModel(models.bakeLayer(FumoModel.LAYER_LOCATION));
        slim = new FumoModel(models.bakeLayer(FumoModel.SLIM_LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        ResolvableProfile owner = stack.get(ModRegistration.FUMO_OWNER.get());
        FumoRenderer.Skin skin = FumoRenderer.skinOf(owner == null ? null : owner.gameProfile());
        // ItemRenderer pre-shifts by -0.5 because custom renderers are expected to draw in block
        // space; this model stands on its own origin, so undo that before placing it anywhere.
        if (displayContext == ItemDisplayContext.HEAD) {
            // Worn oversized (1.25x) so it reads as a plushie on the head; the anchor puts its seat
            // on the crown (0.95 floats, 0.70 buries the legs), and it stays unturned so it looks
            // where the wearer looks.
            poseStack.translate(0.5F, 0.85F, 0.5F);
            poseStack.scale(1.25F, 1.25F, 1.25F);
        } else {
            // Held, the doll should read as a small plushie rather than filling the screen.
            boolean held = HAND_CONTEXTS.contains(displayContext);
            float scale = held ? 0.55F : 0.9F;
            poseStack.translate(0.5F, 0.5F, 0.5F);
            poseStack.scale(scale, scale, scale);
            // Held by the feet: keep the soles on the grip point instead of centring the whole body
            // there, so it reads as carried by the legs rather than hugged at the waist.
            if (!held) poseStack.translate(0.0F, -FumoModel.HEIGHT / 2.0F, 0.0F);
            // The doll should look at whoever holds it, so every context gets the half turn; the GUI
            // adds a slight tilt and three-quarter turn so the icon does not read as a flat decal.
            boolean gui = displayContext == ItemDisplayContext.GUI;
            // Held, the doll also turns a little toward the viewer's right instead of staring dead on.
            float yaw = gui ? 205.0F : (held ? 130.0F : 180.0F);
            poseStack.mulPose(Axis.XP.rotationDegrees(gui ? 15.0F : 0.0F));
            poseStack.mulPose(Axis.YP.rotationDegrees(yaw));
        }
        (skin.slim() ? slim : classic).renderToBuffer(poseStack,
                buffers.getBuffer(RenderType.entityCutoutNoCull(skin.texture())),
                light, overlay, -1);
    }
}

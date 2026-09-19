package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.block.decoration.FumoBlock;
import cn.dancingsnow.neoecoprototype.blockentity.decoration.FumoBlockEntity;
import com.mojang.authlib.GameProfile;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

/** Renders the sitting fumo doll from the skin of whoever it belongs to. */
public class FumoRenderer implements BlockEntityRenderer<FumoBlockEntity> {
    /** Bundled skin used when a doll carries no owner profile; original art, ships in the jar. */
    public static final ResourceLocation DEFAULT_TEXTURE =
            NeoECOPrototype.id("textures/block/fumo/placeholder_skin.png");
    /** Pack `<name>.png` (lowercase) here to override a downloaded skin after editing it. */
    private static final String LOCAL_SKIN_PREFIX = "textures/block/fumo/skins/";

    /** Which skin to draw with, and whether its arm columns are the slim 3/4/3/4 widths. */
    public record Skin(ResourceLocation texture, boolean slim) {
    }

    public static Skin skinOf(@Nullable GameProfile profile) {
        if (profile == null) return new Skin(DEFAULT_TEXTURE, false);
        PlayerSkin resolved = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
        ResourceLocation local = localSkin(profile.getName());
        return new Skin(local != null ? local : resolved.texture(),
                resolved.model() == PlayerSkin.Model.SLIM);
    }

    @Nullable
    private static ResourceLocation localSkin(String name) {
        if (name == null || name.isEmpty()) return null;
        ResourceLocation location = NeoECOPrototype.id(LOCAL_SKIN_PREFIX + name.toLowerCase(Locale.ROOT) + ".png");
        return Minecraft.getInstance().getResourceManager().getResource(location).isPresent() ? location : null;
    }

    private final FumoModel classic;
    private final FumoModel slim;

    public FumoRenderer(BlockEntityRendererProvider.Context context) {
        classic = new FumoModel(context.bakeLayer(FumoModel.LAYER_LOCATION));
        slim = new FumoModel(context.bakeLayer(FumoModel.SLIM_LAYER_LOCATION));
    }

    public FumoModel modelFor(Skin skin) {
        return skin.slim() ? slim : classic;
    }

    @Override
    public void render(FumoBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        Skin skin = skinOf(blockEntity.ownerProfile());
        poseStack.pushPose();
        poseStack.translate(0.5, 0.0, 0.5);
        // The model is authored facing north, and Direction.toYRot() is the entity yaw of that
        // direction, so this is the same rotation LivingEntityRenderer applies for a facing entity.
        poseStack.mulPose(Axis.YP.rotationDegrees(state.getValue(FumoBlock.FACING).toYRot() - 180.0F));
        modelFor(skin).renderToBuffer(poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(skin.texture())),
                packedLight, packedOverlay, -1);
        poseStack.popPose();
    }
}

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
    /**
     * Pack `<name>.png` (lowercase) here to ship a doll skin in the jar instead of resolving one, e.g.
     * `textures/block/fumo/skins/reliqwq.png`. A skin whose arms are 3 pixels wide takes the file name
     * `<name>_slim.png`; see {@link #skinOf} for why the file decides that and the profile cannot.
     */
    private static final String LOCAL_SKIN_PREFIX = "textures/block/fumo/skins/";
    private static final String LOCAL_SKIN_SLIM_SUFFIX = "_slim";

    /** Which skin to draw with, and whether its arm columns are the slim 3/4/3/4 widths. */
    public record Skin(ResourceLocation texture, boolean slim) {
    }

    public static Skin skinOf(@Nullable GameProfile profile) {
        if (profile == null) return new Skin(DEFAULT_TEXTURE, false);
        ResourceLocation local = localSkin(profile.getName());
        if (local != null) {
            // A bundled skin is authored by us, so it carries its own wrist width. Taking the model from
            // the vanilla lookup instead would pair a 4-pixel arm texture with the 3-pixel model, or the
            // other way around, because that lookup keys off the profile's uuid and not off this file.
            return new Skin(local, local.getPath().endsWith(LOCAL_SKIN_SLIM_SUFFIX));
        }
        PlayerSkin resolved = Minecraft.getInstance().getSkinManager().getInsecureSkin(profile);
        return new Skin(resolved.texture(), resolved.model() == PlayerSkin.Model.SLIM);
    }

    @Nullable
    private static ResourceLocation localSkin(String name) {
        if (name == null || name.isEmpty()) return null;
        var resources = Minecraft.getInstance().getResourceManager();
        String base = name.toLowerCase(Locale.ROOT);
        for (String suffix : new String[]{"", LOCAL_SKIN_SLIM_SUFFIX}) {
            ResourceLocation location = NeoECOPrototype.id(LOCAL_SKIN_PREFIX + base + suffix + ".png");
            if (resources.getResource(location).isPresent()) {
                return location;
            }
        }
        return null;
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

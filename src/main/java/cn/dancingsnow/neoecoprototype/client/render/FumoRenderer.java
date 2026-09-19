package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.block.decoration.FumoBlock;
import cn.dancingsnow.neoecoprototype.blockentity.decoration.FumoBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

/** Renders the sitting fumo doll from the bound player-skin texture. */
public class FumoRenderer implements BlockEntityRenderer<FumoBlockEntity> {
    public static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/block/fumo/mita_skin.png");
    private final FumoModel model;

    public FumoRenderer(BlockEntityRendererProvider.Context context) {
        model = new FumoModel(context.bakeLayer(FumoModel.LAYER_LOCATION));
        FumoItemRenderer.init(context.getBlockEntityRenderDispatcher(), context.getModelSet());
    }

    @Override
    public void render(FumoBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        BlockState state = blockEntity.getBlockState();
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F - state.getValue(FumoBlock.FACING).toYRot()));
        model.renderToBuffer(poseStack,
                bufferSource.getBuffer(RenderType.entityCutoutNoCull(TEXTURE)),
                packedLight, packedOverlay, -1);
        poseStack.popPose();
    }
}

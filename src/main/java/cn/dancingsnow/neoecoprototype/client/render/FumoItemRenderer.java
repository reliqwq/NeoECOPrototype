package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.model.geom.EntityModelSet;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderDispatcher;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Renders the fumo doll for the item form (inventory, hand, item frames). */
public class FumoItemRenderer extends BlockEntityWithoutLevelRenderer {
    private static BlockEntityRenderDispatcher dispatcher;
    private static EntityModelSet modelSet;
    private static FumoItemRenderer instance;

    public static void init(BlockEntityRenderDispatcher renderDispatcher, EntityModelSet models) {
        dispatcher = renderDispatcher;
        modelSet = models;
    }

    public static FumoItemRenderer getInstance() {
        if (instance == null) {
            instance = new FumoItemRenderer(dispatcher, modelSet);
        }
        return instance;
    }

    private final FumoModel model;

    private FumoItemRenderer(BlockEntityRenderDispatcher renderDispatcher, EntityModelSet models) {
        super(renderDispatcher, models);
        model = new FumoModel(models.bakeLayer(FumoModel.LAYER_LOCATION));
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext displayContext, PoseStack poseStack,
                             MultiBufferSource buffers, int light, int overlay) {
        model.renderToBuffer(poseStack,
                buffers.getBuffer(RenderType.entityCutoutNoCull(FumoRenderer.TEXTURE)),
                light, overlay, -1);
    }
}

package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;

/**
 * Sitting chibi fumo built on the standard player-skin UV layout
 * (head 0,0 / body 16,16 / right arm 40,16 / left arm 32,48 / legs 0,16 and 16,48),
 * so any player skin textures it correctly without a remap table.
 * Authored facing north like vanilla humanoid models.
 */
public class FumoModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(NeoECOPrototype.id("fumo"), "main");
    private static final float ARM_SPLAY = 0.18f;
    private final ModelPart root;

    public FumoModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("root");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition root = mesh.getRoot();
        PartDefinition body = root.addOrReplaceChild("root",
                CubeListBuilder.create().texOffs(16, 16).addBox(-4f, 0f, -2f, 8f, 6f, 4f),
                PartPose.offset(0f, 0f, 0f));
        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, 0f, -4f, 8f, 8f, 8f),
                PartPose.offset(0f, -6f, 0f));
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16).addBox(-1.5f, -2f, -2f, 3f, 5f, 4f),
                PartPose.offsetAndRotation(-5.5f, 4f, 0f, 0f, 0f, ARM_SPLAY));
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).addBox(-1.5f, -2f, -2f, 3f, 5f, 4f),
                PartPose.offsetAndRotation(5.5f, 4f, 0f, 0f, 0f, -ARM_SPLAY));
        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16).addBox(-2f, -1f, -6f, 4f, 2f, 4f),
                PartPose.offsetAndRotation(-2f, 1f, 0f, 0f, 0f, 0f));
        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).addBox(-2f, -1f, -6f, 4f, 2f, 4f),
                PartPose.offsetAndRotation(2f, 1f, 0f, 0f, 0f, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               int color) {
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
    }
}

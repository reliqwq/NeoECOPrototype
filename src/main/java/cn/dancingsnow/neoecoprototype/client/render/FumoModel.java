package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

/**
 * Sitting chibi doll built on the standard player-skin UV layout, so any skin textures it correctly
 * without a remap table. Authored facing north with y growing downward, like vanilla humanoid models;
 * {@link #renderToBuffer} repeats the flip+lift LivingEntityRenderer applies for entities.
 */
public class FumoModel extends Model {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(NeoECOPrototype.id("fumo"), "main");
    /** Slim (Alex) skins pack the arm columns 3/4/3/4 wide, so the arm cube needs a 3-wide nominal box. */
    public static final ModelLayerLocation SLIM_LAYER_LOCATION =
            new ModelLayerLocation(NeoECOPrototype.id("fumo_slim"), "main");
    /**
     * Baked mesh-space extremes, in 1/16 model units with y growing downward: the hat layer's top
     * and the sole of the foot. Everything else is derived, so renderToBuffer leaves the doll
     * standing on the pose-stack origin and exactly HEIGHT tall.
     */
    private static final float MESH_TOP = -0.25F;
    private static final float MESH_BOTTOM = 15.125F;
    /** Lift that puts the sole on the origin, in blocks. */
    private static final float BOTTOM = MESH_BOTTOM / 16.0F;
    /** Overall height in blocks, so callers can centre the doll in an icon box. */
    public static final float HEIGHT = (MESH_BOTTOM - MESH_TOP) / 16.0F;
    private static final float ARM_TILT = -0.3927F;
    private static final float LEG_SPLAY = 0.18F;
    private static final float ARM_SPLAY = 0.18F;
    /** The torso leans about the seat, so the head has to be pushed forward to stay over it. */
    private static final float HEAD_SHIFT_Z = -1.5F;
    /** Negative leans the torso backward about the seat; the head and legs counter it. */
    private static final float BACK_LEAN = -0.15F;

    private final ModelPart root;

    public FumoModel(ModelPart root) {
        super(RenderType::entityCutoutNoCull);
        this.root = root.getChild("body");
    }

    /**
     * Cube sizes below must match the skin slot's natural size, because the UV is derived from the
     * cube itself; slimming or lengthening a limb is done with a (possibly negative) deformation,
     * which only moves vertices and leaves the UV anchored in its own column.
     */
    private static final CubeDeformation SLIM_TORSO = new CubeDeformation(-1.5F, 0.0F, 0.0F);
    private static final CubeDeformation LONG_LEG = new CubeDeformation(-0.75F, 0.0F, 0.5F);
    private static final CubeDeformation FOOT = new CubeDeformation(-0.875F, 0.0F, -1.0F);

    public static LayerDefinition createBodyLayer() {
        return createBodyLayer(false);
    }

    public static LayerDefinition createSlimBodyLayer() {
        return createBodyLayer(true);
    }

    private static LayerDefinition createBodyLayer(boolean slim) {
        // The body pivot sits on the seat, so leaning rotates the plushie like a real sit instead of
        // hinging it at the neck; every other pivot below is measured from it.
        // Arms only: a slim skin's four arm columns are 3/4/3/4 wide, so a 4-wide nominal box makes
        // the palm cap read two empty columns. Both variants end up 2.5 wide after deformation.
        float armWidth = slim ? 3.0F : 4.0F;
        CubeDeformation armShape = new CubeDeformation(slim ? -0.25F : -0.75F, 0.0F, -0.75F);
        MeshDefinition mesh = new MeshDefinition();
        PartDefinition body = mesh.getRoot().addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(16, 16)
                        .addBox(-4f, -8f, -2f, 8f, 8f, 4f, SLIM_TORSO),
                PartPose.offsetAndRotation(0f, 15f, 0f, BACK_LEAN, 0f, 0f));
        body.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4f, 0f, -4f, 8f, 8f, 8f),
                PartPose.offsetAndRotation(0f, -15f, HEAD_SHIFT_Z, -BACK_LEAN, 0f, 0f));
        // Grow the geometry instead of enlarging the cube: the UV is derived from the cube size, so a
        // 8.5 cube would shift the overlay half a pixel and spill into the next skin row.
        body.addOrReplaceChild("hat",
                CubeListBuilder.create().texOffs(32, 0)
                        .addBox(-4f, 0f, -4f, 8f, 8f, 8f, new CubeDeformation(0.25F)),
                PartPose.offsetAndRotation(0f, -15f, HEAD_SHIFT_Z, -BACK_LEAN, 0f, 0f));
        // The character's right side is -X in this space. A positive yaw pushes the -Z foot toward
        // -X, and a positive roll pushes the +Y hand toward -X, hence the mirrored signs per side.
        body.addOrReplaceChild("right_arm",
                CubeListBuilder.create().texOffs(40, 16)
                        .addBox(-armWidth / 2.0F, 0f, -2f, armWidth, 5f, 4f, armShape),
                PartPose.offsetAndRotation(-3.5f, -8f, 0f, ARM_TILT, 0f, ARM_SPLAY));
        body.addOrReplaceChild("left_arm",
                CubeListBuilder.create().texOffs(32, 48).mirror()
                        .addBox(-armWidth / 2.0F, 0f, -2f, armWidth, 5f, 4f, armShape),
                PartPose.offsetAndRotation(3.5f, -8f, 0f, ARM_TILT, 0f, -ARM_SPLAY));
        // Seated thighs: 2.5 wide with a 1-unit gap, stretched forward to 5 long and splayed ~10°
        // outward. They counter the lean so the feet stay flat on the floor. The foot is a second
        // cube in the same part, so it follows the leg; it is deliberately 0.25 narrower and sits
        // 0.125 lower than the thigh, because any face the two cubes share a plane with z-fights.
        body.addOrReplaceChild("right_leg",
                CubeListBuilder.create().texOffs(0, 16)
                        .addBox(-2f, 0f, -2f, 4f, 2.5f, 4f, LONG_LEG)
                        .texOffs(0, 22)
                        .addBox(-2f, 0.125f, -3.25f, 4f, 2.5f, 4f, FOOT),
                PartPose.offsetAndRotation(-1.75f, -2.5f, -3f, -BACK_LEAN, LEG_SPLAY, 0f));
        body.addOrReplaceChild("left_leg",
                CubeListBuilder.create().texOffs(16, 48).mirror()
                        .addBox(-2f, 0f, -2f, 4f, 2.5f, 4f, LONG_LEG)
                        .texOffs(16, 54).mirror()
                        .addBox(-2f, 0.125f, -3.25f, 4f, 2.5f, 4f, FOOT),
                PartPose.offsetAndRotation(1.75f, -2.5f, -3f, -BACK_LEAN, -LEG_SPLAY, 0f));
        return LayerDefinition.create(mesh, 64, 64);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay,
                               int color) {
        poseStack.pushPose();
        // Model and EntityModel do not apply this; LivingEntityRenderer does, and every caller of a
        // y-down humanoid model has to repeat it or the doll renders head-first into the floor.
        poseStack.scale(-1.0F, -1.0F, 1.0F);
        poseStack.translate(0.0F, -(BOTTOM + 0.001F), 0.0F);
        root.render(poseStack, buffer, packedLight, packedOverlay, color);
        poseStack.popPose();
    }
}

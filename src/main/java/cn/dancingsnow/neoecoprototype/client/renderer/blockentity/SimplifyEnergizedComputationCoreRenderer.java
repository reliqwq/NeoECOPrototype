package cn.dancingsnow.neoecoprototype.client.renderer.blockentity;

import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.client.rendering.IFixedBlockEntityRenderer;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.resources.ResourceLocation;

/**
 * Draws the energized core's face once the structure forms. The block hides its own model when
 * formed, the way every eco shell member does, because a member that keeps drawing in the shell is
 * seen from both sides at once and flickers at any viewing angle. eco leaves that cell empty; we
 * paint the face back through eco's own section-geometry hook, on six plates pushed 1/16 out of the
 * block so they cannot tie with the member faces eco draws on the same shell plane.
 */
public final class SimplifyEnergizedComputationCoreRenderer implements
        IFixedBlockEntityRenderer<ECOComputationParallelCoreBlockEntity> {

    public static final ResourceLocation FORMED_FACE_MODEL = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "block/energized_computation_core_face");

    @Override
    public void renderFixed(ECOComputationParallelCoreBlockEntity blockEntity, float partialTick,
                            PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        // The same FORMED the block's own getRenderShape reads. NEBlockEntity.isFormed() is a field
        // eco writes when the cluster adopts a member, and a member standing in the shell never gets
        // it written: gate on that and the block hides itself while we still think it is unformed.
        if (!blockEntity.getBlockState().getValue(NEBlock.FORMED)) {
            return;
        }
        tessellateModel(poseStack, bufferSource, FORMED_FACE_MODEL, packedLight, packedOverlay);
    }
}

package cn.dancingsnow.neoecoprototype.client.renderer.blockentity;

import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.client.rendering.IFixedBlockEntityRenderer;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationDrive;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import org.joml.Quaternionf;

public final class SimplifyComputationDriveRenderer implements
        IFixedBlockEntityRenderer<SimplifyComputationDriveBlockEntity>,
        BlockEntityRenderer<SimplifyComputationDriveBlockEntity> {

    public SimplifyComputationDriveRenderer() {
    }

    public SimplifyComputationDriveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void renderFixed(SimplifyComputationDriveBlockEntity blockEntity, float partialTick,
                            PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        ItemStack itemStack = blockEntity.getCellStack();
        Direction facing = blockEntity.getBlockState().getValue(ECOComputationDrive.FACING);
        int rotation = switch (facing) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        Quaternionf facingRot = Axis.YN.rotationDegrees(rotation);
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.translate(0.25 * facing.getStepX(), 0, 0.25 * facing.getStepZ());
        poseStack.mulPose(facingRot);

        boolean formed = blockEntity.isFormed();
        boolean shouldCellWork = false;
        IECOTier cableTier = blockEntity.getTier();
        if (itemStack != null && !itemStack.isEmpty()
                && itemStack.getItem() instanceof cn.dancingsnow.neoecoae.items.ECOComputationCellItem item) {
            IECOTier itemTier = item.getTier();
            shouldCellWork = formed && cableTier != null && itemTier.compareTo(cableTier) <= 0;
            ResourceLocation model = shouldCellWork
                    ? ECOComputationModels.getFormedModel(itemStack.getItem())
                    : ECOComputationModels.getNormalModel(itemStack.getItem());
            if (shouldCellWork) {
                cableTier = itemTier;
            }
            // Cells supplied by another addon may be valid ECO cells without a registered model.
            if (model != null) {
                tessellateModel(poseStack, bufferSource, model, packedLight, packedOverlay);
            }
        }

        ResourceLocation cableModel = null;
        boolean connected = false;
        if (formed) {
            if (itemStack != null && shouldCellWork) {
                poseStack.translate(0, 0, -0.35);
                cableModel = ECOComputationModels.getCableConnectedModel(cableTier);
                connected = true;
            } else {
                if (blockEntity.isLowerDrive()) {
                    poseStack.translate(0, 0.688, -0.3);
                } else {
                    poseStack.translate(0, -0.688, -0.3);
                }
                cableModel = ECOComputationModels.getCableDisconnectedModel(cableTier);
            }
        }
        if (blockEntity.isLowerDrive()) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.scale(-1, -1, 1);
            if (connected) {
                poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            }
        }
        if (cableModel != null) {
            tessellateModel(poseStack, bufferSource, cableModel, packedLight, packedOverlay);
        }
        poseStack.popPose();
    }

    @Override
    public void render(SimplifyComputationDriveBlockEntity blockEntity, float partialTick,
                       PoseStack poseStack, MultiBufferSource bufferSource,
                       int packedLight, int packedOverlay) {
    }
}

package cn.dancingsnow.neoecoprototype.client.renderer.blockentity;

import appeng.api.storage.cells.CellState;
import appeng.client.render.tesr.CellLedRenderer;
import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.client.rendering.IFixedBlockEntityRenderer;
import cn.dancingsnow.neoecoae.impl.storage.infinite.ECOInfiniteStorageMember;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.Direction;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.phys.Vec2;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

/** Renders the mounted cell into the drive section mesh and its status LED. */
public class SimplifyDriveRenderer implements BlockEntityRenderer<SimplifyDriveBlockEntity>,
        IFixedBlockEntityRenderer<SimplifyDriveBlockEntity> {

    private static final int INFINITE_MEMBER_LED_COLOR = 0xCA6CFF;

    public SimplifyDriveRenderer() {
    }

    public SimplifyDriveRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SimplifyDriveBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack cellStack = blockEntity.getCellStack();
        if (ECOInfiniteStorageMember.isMember(cellStack)) {
            renderLed(blockEntity, poseStack, bufferSource,
                    FastColor.ARGB32.color(255, INFINITE_MEMBER_LED_COLOR));
            return;
        }
        if (!blockEntity.isMounted() || !blockEntity.isOnline()) {
            return;
        }
        CellState cellState = blockEntity.getCellState();
        if (cellState != CellState.ABSENT) {
            renderLed(blockEntity, poseStack, bufferSource,
                    FastColor.ARGB32.color(255, cellState.getStateColor()));
        }
    }

    private static void renderLed(SimplifyDriveBlockEntity blockEntity, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int stateColor) {
        BlockState blockState = blockEntity.getBlockState();
        Direction face = blockState.getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();

        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(face.getRotation());
        poseStack.translate(0, -0.501, 0);

        float pixel = 1f / 16;
        Vec2 offset = new Vec2(-5 * pixel, -5 * pixel);
        float xStart = offset.x;
        float zStart = offset.y;
        float xEnd = offset.x + pixel;
        float zEnd = offset.y + pixel * 2;

        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer consumer = bufferSource.getBuffer(CellLedRenderer.RENDER_LAYER);
        consumer.addVertex(matrix, xStart, 0, zStart).setColor(stateColor);
        consumer.addVertex(matrix, xEnd, 0, zStart).setColor(stateColor);
        consumer.addVertex(matrix, xEnd, 0, zEnd).setColor(stateColor);
        consumer.addVertex(matrix, xStart, 0, zEnd).setColor(stateColor);
        poseStack.popPose();
    }

    @Override
    public void renderFixed(SimplifyDriveBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack cellStack = blockEntity.getCellStack();
        if (cellStack == null || cellStack.isEmpty()) {
            return;
        }

        Direction blockFacing = blockEntity.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING);
        int rotationDegrees = switch (blockFacing) {
            case NORTH -> 0;
            case EAST -> 90;
            case SOUTH -> 180;
            case WEST -> 270;
            default -> 0;
        };
        Quaternionf rotation = Axis.YN.rotationDegrees(rotationDegrees);

        poseStack.pushPose();
        switch (blockFacing) {
            case NORTH -> poseStack.translate(2 / 16f, 2 / 16f, 0 / 16f);
            case SOUTH -> poseStack.translate(14 / 16f, 2 / 16f, 16 / 16f);
            case WEST -> poseStack.translate(0 / 16f, 2 / 16f, 14 / 16f);
            case EAST -> poseStack.translate(16 / 16f, 2 / 16f, 2 / 16f);
        }
        poseStack.mulPose(rotation);
        tessellateModel(poseStack, bufferSource,
                ECOCellModels.getModelLocation(cellStack.getItem()), packedLight, packedOverlay);
        poseStack.popPose();
    }
}

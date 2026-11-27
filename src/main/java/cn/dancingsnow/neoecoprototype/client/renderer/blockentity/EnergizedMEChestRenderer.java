package cn.dancingsnow.neoecoprototype.client.renderer.blockentity;

import appeng.api.storage.cells.CellState;
import appeng.client.render.tesr.CellLedRenderer;
import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity.DisplayState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.joml.Matrix4f;
import org.joml.Quaternionf;

import java.util.List;

public class EnergizedMEChestRenderer implements BlockEntityRenderer<EnergizedMEChestBlockEntity> {

    private static final ResourceLocation DEFAULT_ITEM_CELL_MODEL = ResourceLocation.fromNamespaceAndPath(
            NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_item");

    public EnergizedMEChestRenderer() {
    }

    public EnergizedMEChestRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(EnergizedMEChestBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        renderCell(blockEntity, poseStack, bufferSource, packedLight, packedOverlay);
        DisplayState displayState = blockEntity.getDisplayState();
        CellState cellState = null;
        if (displayState == DisplayState.EMPTY) {
            cellState = CellState.EMPTY;
        } else if (displayState == DisplayState.PARTIAL) {
            cellState = CellState.NOT_EMPTY;
        } else if (displayState == DisplayState.TYPES_FULL) {
            cellState = CellState.TYPES_FULL;
        } else if (displayState == DisplayState.FULL) {
            cellState = CellState.FULL;
        }
        if (cellState != null) {
            renderLed(blockEntity, poseStack, bufferSource,
                    FastColor.ARGB32.color(255, cellState.getStateColor()));
        }
    }

    private static void renderLed(EnergizedMEChestBlockEntity blockEntity, PoseStack poseStack,
                                  MultiBufferSource bufferSource, int color) {
        Direction opposite = blockEntity.getBlockState()
                .getValue(BlockStateProperties.HORIZONTAL_FACING).getOpposite();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.mulPose(opposite.getRotation());
        poseStack.translate(0, -0.501, 0);
        float unit = 0.0625f;
        float x = -5.0f * unit;
        float y = -5.0f * unit;
        Matrix4f matrix = poseStack.last().pose();
        VertexConsumer buffer = bufferSource.getBuffer(CellLedRenderer.RENDER_LAYER);
        buffer.addVertex(matrix, x, 0, y).setColor(color);
        buffer.addVertex(matrix, x + unit, 0, y).setColor(color);
        buffer.addVertex(matrix, x + unit, 0, y + 2.0f * unit).setColor(color);
        buffer.addVertex(matrix, x, 0, y + 2.0f * unit).setColor(color);
        poseStack.popPose();
    }

    private void renderCell(EnergizedMEChestBlockEntity blockEntity, PoseStack poseStack,
                            MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        ItemStack cellStack = blockEntity.getCellStack();
        if (cellStack == null || cellStack.isEmpty()) {
            return;
        }

        Direction facing = blockEntity.getBlockState().getValue(BlockStateProperties.HORIZONTAL_FACING);
        Quaternionf rotation = Axis.YP.rotationDegrees(-facing.toYRot() + 180.0F);
        poseStack.pushPose();
        switch (facing) {
            case NORTH -> poseStack.translate(2 / 16f, 2 / 16f, 0);
            case SOUTH -> poseStack.translate(14 / 16f, 2 / 16f, 1);
            case WEST -> poseStack.translate(0, 2 / 16f, 14 / 16f);
            case EAST -> poseStack.translate(1, 2 / 16f, 2 / 16f);
        }
        poseStack.mulPose(rotation);
        tessellateModel(poseStack, bufferSource, modelFor(cellStack), packedLight, packedOverlay);
        poseStack.popPose();
    }

    private void tessellateModel(PoseStack poseStack, MultiBufferSource bufferSource,
                                 ResourceLocation modelLocation, int packedLight, int packedOverlay) {
        BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(ModelResourceLocation.standalone(modelLocation));
        VertexConsumer buffer = bufferSource.getBuffer(RenderType.cutout());
        RandomSource random = RandomSource.create(42L);
        for (Direction direction : Direction.values()) {
            renderQuads(poseStack, buffer, model.getQuads(null, direction, random), packedLight, packedOverlay);
        }
        renderQuads(poseStack, buffer, model.getQuads(null, null, random), packedLight, packedOverlay);
    }

    private void renderQuads(PoseStack poseStack, VertexConsumer buffer, List<BakedQuad> quads,
                             int packedLight, int packedOverlay) {
        for (BakedQuad quad : quads) {
            buffer.putBulkData(poseStack.last(), quad, 1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
        }
    }

    private static ResourceLocation modelFor(ItemStack cellStack) {
        if (ECOStorageCells.isCellHandled(cellStack)) {
            return ECOCellModels.getModelLocation(cellStack.getItem());
        }
        return DEFAULT_ITEM_CELL_MODEL;
    }
}

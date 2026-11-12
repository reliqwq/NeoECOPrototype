package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoae.blocks.ECOIntegratedWorkingStation;
import cn.dancingsnow.neoecoae.blocks.entity.ECOLargeIntegratedWorkingStationBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ProcessorAssemblyWorkstationSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.client.model.data.ModelProperty;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

final class WorkstationSmartCoolingBakedModel extends BakedModelWrapper<BakedModel> {
    private static final ModelProperty<Boolean> OUR_MODULE = new ModelProperty<>();

    WorkstationSmartCoolingBakedModel(BakedModel original) {
        super(original);
    }

    @Override
    public ModelData getModelData(BlockAndTintGetter level, net.minecraft.core.BlockPos pos, BlockState state,
                                  ModelData data) {
        ModelData originalData = originalModel.getModelData(level, pos, state, data);
        if (level.getBlockEntity(pos) instanceof ECOLargeIntegratedWorkingStationBlockEntity host
                && ProcessorAssemblyWorkstationSupport.MODULE_ID.equals(host.getModuleAppearanceId())) {
            return originalData.derive().with(OUR_MODULE, true).build();
        }
        return originalData;
    }

    private BakedModel selected(@Nullable BlockState state, ModelData data) {
        if (state == null
                || !Boolean.TRUE.equals(data.get(OUR_MODULE))
                || !state.getOptionalValue(ECOIntegratedWorkingStation.SMART_COOLING).orElse(false)) {
            return originalModel;
        }
        boolean working = state.getOptionalValue(ECOIntegratedWorkingStation.WORKING).orElse(false);
        ResourceLocation modelId = working
                ? ProcessorAssemblyWorkstationSupport.SMART_WORKING_MODEL
                : ProcessorAssemblyWorkstationSupport.SMART_IDLE_MODEL;
        BakedModel model = Minecraft.getInstance().getModelManager()
                .getModel(ModelResourceLocation.standalone(modelId));
        return model == Minecraft.getInstance().getModelManager().getMissingModel() ? originalModel : model;
    }

    @Override
    public ChunkRenderTypeSet getRenderTypes(BlockState state, RandomSource random, ModelData data) {
        return selected(state, data).getRenderTypes(state, random, data);
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource random,
                                    ModelData data, @Nullable RenderType renderType) {
        BakedModel model = selected(state, data);
        if (model == originalModel || state == null) {
            return model.getQuads(state, side, random, data, renderType);
        }
        int turns = switch (state.getOptionalValue(BlockStateProperties.HORIZONTAL_FACING).orElse(Direction.NORTH)) {
            case EAST -> 1;
            case SOUTH -> 2;
            case WEST -> 3;
            default -> 0;
        };
        Direction localSide = side;
        if (localSide != null && localSide.getAxis().isHorizontal()) {
            for (int i = 0; i < turns; i++) {
                localSide = localSide.getCounterClockWise();
            }
        }
        List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, localSide, random, data, renderType));
        BakedModel coolingScreen = Minecraft.getInstance().getModelManager().getModel(
                ModelResourceLocation.standalone(ProcessorAssemblyWorkstationSupport.SMART_COOLING_SCREEN_MODEL));
        if (coolingScreen != Minecraft.getInstance().getModelManager().getMissingModel()) {
            quads.addAll(coolingScreen.getQuads(state, localSide, random, data, renderType));
        }
        if (turns == 0) {
            return quads;
        }
        List<BakedQuad> rotated = new ArrayList<>(quads.size());
        for (BakedQuad quad : quads) {
            rotated.add(rotate(quad, turns));
        }
        return rotated;
    }

    private static BakedQuad rotate(BakedQuad quad, int turns) {
        int[] vertices = quad.getVertices().clone();
        int stride = vertices.length / 4;
        for (int vertex = 0; vertex < 4; vertex++) {
            int offset = vertex * stride;
            float x = Float.intBitsToFloat(vertices[offset]);
            float z = Float.intBitsToFloat(vertices[offset + 2]);
            for (int i = 0; i < turns; i++) {
                float nextX = 1 - z;
                z = x;
                x = nextX;
            }
            vertices[offset] = Float.floatToRawIntBits(x);
            vertices[offset + 2] = Float.floatToRawIntBits(z);
            if (stride >= 8) {
                int normal = vertices[offset + 7];
                int nx = (byte) normal;
                int nz = (byte) (normal >>> 16);
                for (int i = 0; i < turns; i++) {
                    int nextX = -nz;
                    nz = nx;
                    nx = nextX;
                }
                vertices[offset + 7] = (normal & 0xFF00FF00) | (nx & 0xFF) | ((nz & 0xFF) << 16);
            }
        }
        Direction direction = quad.getDirection();
        if (direction.getAxis().isHorizontal()) {
            for (int i = 0; i < turns; i++) {
                direction = direction.getClockWise();
            }
        }
        return new BakedQuad(vertices, quad.getTintIndex(), direction, quad.getSprite(), quad.isShade(),
                quad.hasAmbientOcclusion());
    }
}

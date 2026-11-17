package cn.dancingsnow.neoecoprototype.client.model;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.client.model.BakedModelWrapper;
import net.neoforged.neoforge.client.model.data.ModelData;
import net.neoforged.neoforge.common.util.TriState;

import java.util.List;

/**
 * Draws the formed cooling controller through the level's own lighting instead of through the light baked
 * into its quads - the same trick eco plays for its three tiers, rebuilt here because their selector only
 * ever matches {@code neoecoae} and three literal paths, so an addon's controller cannot join it.
 *
 * <p>The two halves have to agree or the block doubles up: {@link #getQuads} hands the terrain layer nothing
 * at all, and the geometry then comes only from {@link #renderFixed}, called by our fixed block entity
 * renderer with a buffer source that recomputes each vertex's light from the world. The unformed variant is
 * left alone, so it keeps drawing the way any ordinary block does.
 *
 * <p>Splitting the two halves on {@code isShade} - 60 body faces to the terrain layer, 4 emissive faces to the
 * lit pass - was tried on 2026-11-17 to make the block appear in a GuideME scene at all, and the player's
 * read of the result was that the faces came out painted the wrong way round. It went back the same hour, so
 * the terrain layer is not a substitute for this model: only {@link #renderFixed} draws it correctly.
 *
 * <p>That is also why the member is an empty cell in a GuideME scene, and no scene data can fix it:
 * {@code AddSectionGeometryEvent}, the only door into {@code FixedBlockEntityRenderers}, is fired from
 * {@code SectionRenderDispatcher}, while GuideME's scene renderer calls
 * {@code BlockRenderDispatcher.renderBatched} directly. Both computation scenes already carry the controller's
 * block entity (measured 2026-11-17: 30 blocks with block-entity data in each, the controller among them), so
 * re-saving the structure changes nothing - the pass simply is not asked.
 */
public final class SimplifyCoolingControllerBakedModel extends BakedModelWrapper<BakedModel> {
    private static final Direction[] DIRECTIONS = Direction.values();
    /** The quad order must not drift between the six sides, and a fixed seed is what keeps it still. */
    private static final long SEED = 42L;

    public SimplifyCoolingControllerBakedModel(BakedModel originalModel) {
        super(originalModel);
    }

    @Override
    public List<BakedQuad> getQuads(BlockState state, Direction side, RandomSource random, ModelData data,
                                    RenderType renderType) {
        return renderType == null
                ? this.originalModel.getQuads(state, side, random, data, null)
                : List.of();
    }

    /**
     * A quad that says it does not want ambient occlusion is rebuilt with that flag off, because the fixed
     * buffer source shades every vertex it is given - without the rebuild, a flat face would be shaded twice.
     */
    public void renderFixed(BlockState state, PoseStack poseStack, MultiBufferSource bufferSource,
                            int packedLight, int packedOverlay) {
        var random = RandomSource.create(SEED);
        for (RenderType renderType : this.originalModel.getRenderTypes(state, random, ModelData.EMPTY)) {
            var consumer = bufferSource.getBuffer(renderType);
            TriState ao = this.originalModel.useAmbientOcclusion(state, ModelData.EMPTY, renderType);
            boolean hasAmbientOcclusion = ao.isTrue()
                    || ao.isDefault() && this.originalModel.useAmbientOcclusion();
            for (int index = 0; index <= DIRECTIONS.length; index++) {
                random.setSeed(SEED);
                Direction side = index == DIRECTIONS.length ? null : DIRECTIONS[index];
                for (BakedQuad quad : this.originalModel.getQuads(state, side, random, ModelData.EMPTY,
                        renderType)) {
                    consumer.putBulkData(poseStack.last(),
                            hasAmbientOcclusion ? quad
                                    : new BakedQuad(quad.getVertices(), quad.getTintIndex(), quad.getDirection(),
                                    quad.getSprite(), quad.isShade(), false),
                            1.0F, 1.0F, 1.0F, 1.0F, packedLight, packedOverlay);
                }
            }
        }
    }
}

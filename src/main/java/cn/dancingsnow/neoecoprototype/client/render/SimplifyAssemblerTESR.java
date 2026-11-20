package cn.dancingsnow.neoecoprototype.client.render;

import appeng.client.render.crafting.AssemblerAnimationStatus;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import appeng.client.render.effects.ParticleTypes;
import appeng.core.AppEngClient;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.WeakHashMap;

/**
 * The assembler's crafting animation, cloned from AE2's molecular assembler renderer (via ExtendedAE's
 * identical clone): the standalone block entity cannot reuse AE2's, whose generic is pinned to AE2's own
 * block entity class. Same pieces - the shared lights model, crafting particles, the crafted item hovering
 * in the middle - fed from the block entity's stream-synced fields instead of AE2's animation packet.
 * The weak, renderer-instance cache is keyed by client block entity objects (not world/position statics), so
 * status accumulates between render frames and is released when the client unloads the entity.
 */
public class SimplifyAssemblerTESR implements BlockEntityRenderer<SimplifyStonecuttingAssemblerBlockEntity> {
    public static final net.minecraft.client.resources.model.ModelResourceLocation LIGHTS_MODEL =
            MolecularAssemblerRenderer.LIGHTS_MODEL;

    private final RandomSource particleRandom = RandomSource.create();
    private final Map<SimplifyStonecuttingAssemblerBlockEntity, Status> statuses = new WeakHashMap<>();

    public SimplifyAssemblerTESR(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(SimplifyStonecuttingAssemblerBlockEntity assembler, float partialTicks,
                       @NotNull PoseStack poseStack, @NotNull MultiBufferSource buffer,
                       int combinedLight, int combinedOverlay) {
        if (assembler.getAnimationSpeed() > 0 && !assembler.getAnimationDisplay().isEmpty()) {
            Status status = statuses.compute(assembler, (be, old) -> old == null
                    || old.speed() != be.getAnimationSpeed()
                    || !ItemStack.matches(old.display(), be.getAnimationDisplay())
                    ? new Status(be.getAnimationSpeed(), be.getAnimationDisplay().copy(),
                            new AssemblerAnimationStatus(be.getAnimationSpeed(), be.getAnimationDisplay()))
                    : old);
            advanceAndRender(assembler, partialTicks, poseStack, buffer, combinedLight, status.animation());
        } else {
            statuses.remove(assembler);
        }
        if (assembler.isPowered()) {
            renderPowerLight(poseStack, buffer, combinedLight, combinedOverlay);
        }
    }

    private void renderPowerLight(PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                  int combinedOverlay) {
        Minecraft minecraft = Minecraft.getInstance();
        BakedModel lightsModel = minecraft.getModelManager().getModel(LIGHTS_MODEL);
        VertexConsumer bufferSource = buffer.getBuffer(RenderType.tripwire());
        minecraft.getBlockRenderer().getModelRenderer().renderModel(poseStack.last(), bufferSource, null,
                lightsModel, 1, 1, 1, combinedLight, combinedOverlay, ModelData.EMPTY, null);
    }

    private void advanceAndRender(SimplifyStonecuttingAssemblerBlockEntity assembler, float partialTicks,
                                  PoseStack poseStack, MultiBufferSource buffer, int combinedLight,
                                  AssemblerAnimationStatus status) {
        double centerX = assembler.getBlockPos().getX() + 0.5F;
        double centerY = assembler.getBlockPos().getY() + 0.5F;
        double centerZ = assembler.getBlockPos().getZ() + 0.5F;
        Minecraft minecraft = Minecraft.getInstance();
        if (!minecraft.isPaused()) {
            if (status.isExpired()) {
                statuses.remove(assembler);
                return;
            }
            status.setAccumulatedTicks(status.getAccumulatedTicks() + partialTicks);
            status.setTicksUntilParticles(status.getTicksUntilParticles() - partialTicks);
        }
        if (status.getTicksUntilParticles() <= 0) {
            status.setTicksUntilParticles(4);
            if (AppEngClient.instance().shouldAddParticles(particleRandom)) {
                for (int i = 0; i < (int) Math.ceil(status.getSpeed() / 5.0); i++) {
                    minecraft.particleEngine.createParticle(ParticleTypes.CRAFTING, centerX, centerY, centerZ,
                            0, 0, 0);
                }
            }
        }
        ItemStack stack = status.getIs();
        var itemRenderer = minecraft.getItemRenderer();
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        poseStack.translate(0, stack.getItem() instanceof BlockItem ? -0.2F : -0.3F, 0);
        itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, combinedLight,
                net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY, poseStack, buffer,
                assembler.getLevel(), 0);
        poseStack.popPose();
    }

    private record Status(byte speed, ItemStack display, AssemblerAnimationStatus animation) {
    }
}

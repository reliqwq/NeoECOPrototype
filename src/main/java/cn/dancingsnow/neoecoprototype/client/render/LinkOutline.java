package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import java.util.ArrayList;
import java.util.List;
import java.util.OptionalDouble;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * A box drawn around one square in the world for a few seconds - the panel's "highlight" action.
 *
 * <p>The mod this was modelled on (ExtendedAE) delegates to Glodium's outline renderer: a 3px line box with
 * depth testing disabled, drawn at {@link RenderLevelStageEvent.Stage#AFTER_PARTICLES}. Matching that render
 * state matters - an ordinary depth-tested line on the block's own boundary is hidden by the block itself.
 * The mechanism stays local here, so this optional integration does not become a hard dependency.
 *
 * <p>Client-side only and never synced as a packet: the panel already ships integers to the client for its
 * selection wash, so the highlight rides the same binding as a counter. Each client starts its own deadline
 * when the counter moves, which is why two players looking at the same module can see the same box without
 * the server tracking either of them.
 *
 * <p>The marks are keyed by dimension and position and carry their own expiry, so nothing can outlive the
 * moment it was asked for - the same shape the repo allows for per-square client state.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, value = Dist.CLIENT)
public final class LinkOutline {

    /** Six seconds at the normal 20 ticks per second. */
    public static final int DEFAULT_TICKS = 120;

    private static final List<Mark> MARKS = new ArrayList<>();
    private static final RenderType OUTLINE = createOutlineType();

    private LinkOutline() {
    }

    private static RenderType createOutlineType() {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setLineState(new RenderStateShard.LineStateShard(OptionalDouble.of(3.0D)))
                .setTransparencyState(RenderStateShard.GLINT_TRANSPARENCY)
                .setTextureState(RenderStateShard.NO_TEXTURE)
                .setDepthTestState(RenderStateShard.NO_DEPTH_TEST)
                .setCullState(RenderStateShard.NO_CULL)
                .setLightmapState(RenderStateShard.NO_LIGHTMAP)
                .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                .setShaderState(RenderStateShard.RENDERTYPE_LINES_SHADER)
                .createCompositeState(false);
        return RenderType.create("neoecoprototype_link_outline", DefaultVertexFormat.POSITION_COLOR_NORMAL,
                VertexFormat.Mode.LINES, 65536, false, false, state);
    }

    private record Mark(ResourceKey<Level> dimension, BlockPos pos, long until) {
    }

    /**
     * Start a box around each end named in {@code pulse} - {@code dimension:x,y,z|dimension:x,y,z}, the form
     * the module panel ships. Ends in another dimension are skipped: there is nothing to point at from here.
     */
    public static void highlight(String pulse) {
        for (String end : pulse.split("\\|")) {
            int colon = end.lastIndexOf(':');
            if (colon <= 0) {
                continue;
            }
            var dimension = net.minecraft.resources.ResourceKey.create(
                    net.minecraft.core.registries.Registries.DIMENSION,
                    ResourceLocation.parse(end.substring(0, colon)));
            String[] parts = end.substring(colon + 1).split(",");
            if (parts.length != 3) {
                continue;
            }
            try {
                var pos = new BlockPos(Integer.parseInt(parts[0].trim()),
                        Integer.parseInt(parts[1].trim()), Integer.parseInt(parts[2].trim()));
                highlight(dimension, pos, DEFAULT_TICKS);
            } catch (NumberFormatException ignored) {
                // A malformed pulse is a programming error, not a player's: draw the ends that parsed.
            }
        }
    }

    /** Draw a box around one square of the level the caller is looking at, for a while. */
    public static void highlight(ResourceKey<Level> dimension, BlockPos pos, int ticks) {
        var level = Minecraft.getInstance().level;
        if (level == null || !level.dimension().equals(dimension)) {
            // The player is not standing in that dimension, so there is nothing to point at from here.
            return;
        }
        long until = level.getGameTime() + ticks;
        MARKS.removeIf(mark -> mark.dimension().equals(dimension) && mark.pos().equals(pos));
        MARKS.add(new Mark(dimension, pos.immutable(), until));
    }

    @SubscribeEvent
    public static void onStage(RenderLevelStageEvent event) {
        if (MARKS.isEmpty()
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_PARTICLES) {
            return;
        }
        var level = Minecraft.getInstance().level;
        if (level == null) {
            MARKS.clear();
            return;
        }
        long now = level.getGameTime();
        MARKS.removeIf(mark -> mark.until() < now);
        if (MARKS.isEmpty() || (System.currentTimeMillis() / 500L & 1L) == 0L) {
            return;
        }

        Camera camera = event.getCamera();
        Vec3 eye = camera.getPosition();
        PoseStack pose = event.getPoseStack();
        var buffers = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.disableDepthTest();
        RenderSystem.enableBlend();
        pose.pushPose();
        pose.translate(-eye.x, -eye.y, -eye.z);
        VertexConsumer lines = buffers.getBuffer(OUTLINE);
        for (Mark mark : MARKS) {
            if (!mark.dimension().equals(level.dimension())) {
                continue;
            }
            // Match Glodium/EAE: red, width 3, depth-independent and a hair outside the square.
            AABB box = new AABB(mark.pos()).inflate(0.002);
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, lines, box,
                    1.0F, 0.0F, 0.0F, 1.0F);
        }
        pose.popPose();
        buffers.endBatch(OUTLINE);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}

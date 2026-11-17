package cn.dancingsnow.neoecoprototype.client.render;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.items.SimplifyMigrationDriveItem;
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
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/**
 * Depth-independent world outlines for selected wireless endpoints and migration-drive target structures.
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
public final class LinkOutline {

    /** Six seconds at the normal 20 ticks per second. */
    public static final int DEFAULT_TICKS = 120;

    private static final int DRIVE_SCAN_RADIUS = 16;
    private static final long DRIVE_SCAN_INTERVAL = 5;
    private static final long DRIVE_MARK_TICKS = DRIVE_SCAN_INTERVAL + 2;
    private static final int LINK_COLOR = 0xFFFF0000;

    private static final List<Mark> MARKS = new ArrayList<>();
    private static final RenderType OUTLINE = createOutlineType();
    private static long lastDriveScan = -DRIVE_SCAN_INTERVAL;
    private static boolean driveScanReported;

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

    private record Mark(ResourceKey<Level> dimension, AABB bounds, long until, int color, boolean drive) {
    }

    private record DriveTarget(AABB bounds, int color) {
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
        var bounds = new AABB(pos).inflate(0.002D);
        MARKS.removeIf(mark -> !mark.drive() && mark.dimension().equals(dimension)
                && mark.bounds().equals(bounds));
        MARKS.add(new Mark(dimension, bounds, until, LINK_COLOR, false));
    }

    public static void onClientTick(ClientTickEvent.Post event) {
        var minecraft = Minecraft.getInstance();
        var level = minecraft.level;
        var player = minecraft.player;
        if (level == null || player == null
                || !(player.getMainHandItem().getItem() instanceof SimplifyMigrationDriveItem drive)) {
            clearDriveHighlights();
            lastDriveScan = -DRIVE_SCAN_INTERVAL;
            driveScanReported = false;
            return;
        }
        if (level.getGameTime() - lastDriveScan < DRIVE_SCAN_INTERVAL) {
            return;
        }
        lastDriveScan = level.getGameTime();

        var origin = player.blockPosition();
        var targets = new ArrayList<DriveTarget>();
        int loadedChunks = 0;
        int candidateHosts = 0;
        int minChunkX = (origin.getX() - DRIVE_SCAN_RADIUS) >> 4;
        int maxChunkX = (origin.getX() + DRIVE_SCAN_RADIUS) >> 4;
        int minChunkZ = (origin.getZ() - DRIVE_SCAN_RADIUS) >> 4;
        int maxChunkZ = (origin.getZ() + DRIVE_SCAN_RADIUS) >> 4;
        for (int chunkX = minChunkX; chunkX <= maxChunkX; chunkX++) {
            for (int chunkZ = minChunkZ; chunkZ <= maxChunkZ; chunkZ++) {
                LevelChunk chunk = level.getChunkSource().getChunk(chunkX, chunkZ, false);
                if (chunk == null) {
                    continue;
                }
                loadedChunks++;
                for (var blockEntity : chunk.getBlockEntities().values()) {
                    if (!(blockEntity instanceof cn.dancingsnow.neoecoae.multiblock.placement
                            .MultiBlockBuildController.Host host)) {
                        continue;
                    }
                    candidateHosts++;
                    BlockPos pos = blockEntity.getBlockPos();
                    if (pos.distToCenterSqr(origin.getX() + 0.5D, origin.getY() + 0.5D,
                            origin.getZ() + 0.5D) > (double) DRIVE_SCAN_RADIUS * DRIVE_SCAN_RADIUS) {
                        continue;
                    }
                    var target = drive.eligibleTierTarget(host, player);
                    if (target != null) {
                        int color = SimplifyMigrationDriveItem.targetTierColor(target.target());
                        if (color != 0) {
                            targets.add(new DriveTarget(target.bounds(), color));
                        }
                    }
                }
            }
        }
        replaceDriveHighlights(level.dimension(), targets, level.getGameTime() + DRIVE_MARK_TICKS);
        if (!driveScanReported) {
            NeoECOPrototype.LOGGER.info("migration drive highlight scan: drive={} position={} loadedChunks={}"
                            + " candidateHosts={} targets={}", drive.rung(), origin, loadedChunks, candidateHosts,
                    targets.size());
            driveScanReported = true;
        }
    }

    private static void clearDriveHighlights() {
        MARKS.removeIf(Mark::drive);
    }

    private static void replaceDriveHighlights(ResourceKey<Level> dimension, List<DriveTarget> targets, long until) {
        clearDriveHighlights();
        for (DriveTarget target : targets) {
            MARKS.add(new Mark(dimension, target.bounds(), until, target.color(), true));
        }
    }

    public static void onStage(RenderLevelStageEvent event) {
        if (MARKS.isEmpty()
                || event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
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
            int color = mark.color();
            net.minecraft.client.renderer.LevelRenderer.renderLineBox(pose, lines, mark.bounds(),
                    ((color >> 16) & 0xFF) / 255.0F, ((color >> 8) & 0xFF) / 255.0F,
                    (color & 0xFF) / 255.0F, 1.0F);
        }
        pose.popPose();
        buffers.endBatch(OUTLINE);
        RenderSystem.enableDepthTest();
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
    }
}

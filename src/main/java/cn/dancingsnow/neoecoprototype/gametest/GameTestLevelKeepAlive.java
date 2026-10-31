package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.gametest.framework.GameTestInfo;
import net.minecraft.gametest.framework.GameTestServer;
import net.minecraft.gametest.framework.GameTestTicker;
import net.minecraft.gametest.framework.StructureUtils;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.lang.reflect.Field;
import java.util.Collection;

/**
 * Keeps the game test overworld ticking and its chunk gate open. Version lock: NeoForge 21.1.251.
 *
 * <p>{@code ServerLevel#tick} runs its own body only while the level has a player or a forced chunk; with
 * neither it gives up after 300 empty ticks, and that freezes gameTime, the chunk system and every
 * {@code GameTestInstance} timer at once. A suite that needs more than those 300 ticks - a fresh world,
 * where all those far-flung test plots still have to be generated - then stops at a handful of tests,
 * records no failure and no timeout, and never ends. One forced chunk is the documented way to tell the
 * level it is attended. This fires only inside a {@link GameTestServer}.</p>
 *
 * <p>The gate: {@code GameTestInfo#tick} holds a test until every chunk of
 * {@code StructureUtils#getStructureBoundingBox(structureBlockEntity)} is entity-ticking, but the harness
 * only forces the chunks of the box {@code prepareTestStructure} computed itself. For a zero-size template
 * the two boxes differ by one block (prepare moves its box onto its own minimum corner, the gate does
 * not), so a gate box that crosses a chunk border is one chunk outside the forced set: the batch then sits
 * at {@code ticksToWaitForChunkLoading == 20} forever with no failure and no timeout. All three templates
 * in this mod store {@code size} as an int array, which {@code StructureTemplate#load} reads back as a
 * list - an empty one - so they all load as 0x0x0; that is measured, not hypothetical. We therefore force
 * exactly the chunks the gate asks about.</p>
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class GameTestLevelKeepAlive {

    private static boolean forced = false;
    private static Field testInfosField;

    @SubscribeEvent
    public static void onLevelTick(LevelTickEvent.Post event) {
        if (!(event.getLevel() instanceof ServerLevel level)
                || !(level.getServer() instanceof GameTestServer)
                || level.dimension() != Level.OVERWORLD) {
            return;
        }
        if (!forced) {
            forced = level.setChunkForced(0, 0, true);
        }
        forceChunksTheGateWaitsFor(level);
    }

    private static void forceChunksTheGateWaitsFor(ServerLevel level) {
        Collection<GameTestInfo> testInfos = tickerTestInfos();
        if (testInfos == null) {
            return;
        }
        for (GameTestInfo info : testInfos) {
            if (info.isDone() || info.getStructureBlockPos() == null) {
                continue;
            }
            BoundingBox box;
            try {
                box = StructureUtils.getStructureBoundingBox(info.getStructureBlockEntity());
            } catch (RuntimeException e) {
                continue;
            }
            box.intersectingChunks().forEach(chunk -> {
                if (!level.isPositionEntityTicking(chunk.getWorldPosition())) {
                    level.setChunkForced(chunk.x, chunk.z, true);
                }
            });
        }
    }

    @SuppressWarnings("unchecked")
    private static Collection<GameTestInfo> tickerTestInfos() {
        try {
            Field field = testInfosField;
            if (field == null) {
                field = GameTestTicker.class.getDeclaredField("testInfos");
                field.setAccessible(true);
                testInfosField = field;
            }
            return (Collection<GameTestInfo>) field.get(GameTestTicker.SINGLETON);
        } catch (ReflectiveOperationException | RuntimeException ignored) {
            return null;
        }
    }
}

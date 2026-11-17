package cn.dancingsnow.neoecoprototype.gui;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtAccounter;
import net.minecraft.nbt.NbtIo;
import net.minecraft.nbt.Tag;
import org.junit.jupiter.api.Test;

/**
 * Every machine scene in the guide cuts its shell the same way: only the casing layer at the end of the
 * long axis farthest from the interface is rendered, so the reader sees the members sitting inside an open
 * frame with one closed end behind them. The crafting and storage scenes were authored that way first, so
 * they are the reference - the computation scenes are checked against the rule they satisfy, not against a
 * remembered list of positions.
 */
class ComputationGuideSceneCasingTest {
    private static final Path SCENES = Path.of("src/main/resources/assets/neoecoprototype/ae2guide/scenes");

    @Test
    void everyMachineSceneCutsItsShellByTheSameRule() throws Exception {
        for (String scene : java.util.List.of("l1_storage_min.nbt", "l1_crafting_min.nbt",
                "l1_compute_min.nbt", "l1_compute_energized.nbt")) {
            assertOneCutawayRule(scene);
        }
    }

    private static void assertOneCutawayRule(String sceneName) throws Exception {
        Path scene = SCENES.resolve(sceneName);
        assertTrue(Files.isRegularFile(scene), scene + " is not a file");
        CompoundTag data = NbtIo.readCompressed(scene, NbtAccounter.unlimitedHeap());
        var palette = data.getList("palette", Tag.TAG_COMPOUND);
        var blocks = data.getList("blocks", Tag.TAG_COMPOUND);

        var interfaceAt = new java.util.ArrayList<int[]>();
        var casings = new java.util.ArrayList<int[][]>();
        int[] min = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};
        int[] max = {Integer.MIN_VALUE, Integer.MIN_VALUE, Integer.MIN_VALUE};
        for (int i = 0; i < blocks.size(); i++) {
            var block = blocks.getCompound(i);
            var pos = block.getList("pos", Tag.TAG_INT);
            int[] p = {pos.getInt(0), pos.getInt(1), pos.getInt(2)};
            for (int axis = 0; axis < 3; axis++) {
                min[axis] = Math.min(min[axis], p[axis]);
                max[axis] = Math.max(max[axis], p[axis]);
            }
            var state = palette.getCompound(block.getInt("state"));
            String name = state.getString("Name");
            if (name.endsWith("interface")) {
                interfaceAt.add(p);
            }
            if (name.endsWith("_casing")) {
                casings.add(new int[][]{p, {block.getInt("state")}});
            }
        }
        assertEquals(1, interfaceAt.size(), sceneName + " has " + interfaceAt.size()
                + " interface blocks, so the far end has nothing to be measured from");
        assertTrue(!casings.isEmpty(), sceneName + " has no casing blocks at all");

        int lengthAxis = max[0] - min[0] >= max[2] - min[2] ? 0 : 2;
        int interfaceCoordinate = interfaceAt.get(0)[lengthAxis];
        int far = casings.stream().mapToInt(c -> c[0][lengthAxis])
                .reduce(interfaceCoordinate, (a, b) -> Math.abs(a - interfaceCoordinate)
                        >= Math.abs(b - interfaceCoordinate) ? a : b);

        int visible = 0;
        for (int[][] casing : casings) {
            int[] pos = casing[0];
            var properties = palette.getCompound(casing[1][0]).getCompound("Properties");
            assertTrue(properties.contains("invisible"), sceneName + " casing at "
                    + java.util.Arrays.toString(pos) + " has no invisible property to cut it away with");
            String invisible = properties.getString("invisible");
            boolean shouldRender = pos[lengthAxis] == far;
            assertEquals(shouldRender ? "false" : "true", invisible, sceneName + " casing at "
                    + java.util.Arrays.toString(pos) + " disagrees with the crafting/storage rule (long axis "
                    + (lengthAxis == 0 ? "x" : "z") + ", interface at " + interfaceCoordinate
                    + ", closed end at " + far + ")");
            if (shouldRender) {
                visible++;
            }
        }
        assertTrue(visible > 0, sceneName + " closes no end at all on its "
                + (lengthAxis == 0 ? "x" : "z") + " axis");
    }
}

package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * The data disc ships both of the drawn faces as one animated item texture: frame 0 is the teal ring,
 * frame 1 the flat one. Three things have to hold for that to look like a disc rather than a glitch.
 *
 * <p>It has to be a two-frame sheet with metadata beside it - a strip without an {@code .mcmeta} is
 * stretched whole into the item slot, which is the failure the pack-wide animation guard exists for.
 * The two frames have to share one alpha mask, because the vanilla disc silhouette is what keeps the
 * item sitting on the same 15x10 footprint while the skin changes; a frame that drifts one pixel makes
 * the disc jitter every fifteen ticks. And the frames have to differ, or the metadata animates a still.
 */
final class MusicDataDiscAnimationTest {
    private static final Path TEXTURE =
            Path.of("src/main/resources/assets/neoecoprototype/textures/item/music_data_disc.png");
    private static final Path METADATA = Path.of(TEXTURE + ".mcmeta");
    private static final Path MODEL =
            Path.of("src/main/resources/assets/neoecoprototype/models/item/music_data_disc.json");

    /** Vanilla's disc footprint, measured off the 1.21.1 client jar: 15x10 at (0,3), 118 opaque pixels. */
    private static final int[] FOOTPRINT = {0, 3, 15, 13};
    private static final int OPAQUE = 118;

    @Test
    void bothDrawnFacesAnimateOverTheSameSilhouette() throws Exception {
        assertTrue(Files.isRegularFile(TEXTURE), "the data disc texture is missing at " + TEXTURE);
        BufferedImage sheet = ImageIO.read(TEXTURE.toFile());
        assertNotNull(sheet, "could not decode " + TEXTURE + " as a png");
        assertEquals(16, sheet.getWidth(), "the sheet is " + sheet.getWidth() + " wide; item textures are 16");
        assertEquals(32, sheet.getHeight(), "the sheet is 16x" + sheet.getHeight() + ", which is not two 16x16"
                + " frames stacked. Two faces means two frames, one per drawing");

        boolean[][] first = mask(sheet, 0);
        boolean[][] second = mask(sheet, 16);
        assertSilhouette("frame 0", first);
        assertSilhouette("frame 1", second);
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (first[y][x] != second[y][x]) {
                    throw new AssertionError("the two frames disagree about (" + x + "," + y + "): frame 0 is "
                            + (first[y][x] ? "opaque" : "empty") + " there and frame 1 is the opposite. The disc"
                            + " would change shape, not just skin, every fifteen ticks");
                }
            }
        }

        boolean differ = false;
        for (int y = 0; y < 16 && !differ; y++) {
            for (int x = 0; x < 16; x++) {
                if (sheet.getRGB(x, y) != sheet.getRGB(x, y + 16)) {
                    differ = true;
                    break;
                }
            }
        }
        assertTrue(differ, "both frames are pixel-identical, so the .mcmeta animates a still picture and the"
                + " second drawing never shows");
    }

    @Test
    void theSheetIsAnimatedAtFifteenTicksWithoutBlending() throws Exception {
        assertTrue(Files.isRegularFile(METADATA), "there is no " + METADATA.getFileName() + " beside the"
                + " 16x32 sheet, so the game stretches both frames into the item slot at once instead of"
                + " alternating them");
        JsonObject root = JsonParser.parseString(Files.readString(METADATA)).getAsJsonObject();
        assertTrue(root.has("animation"), METADATA.getFileName() + " carries no \"animation\" block: " + root);
        JsonObject animation = root.getAsJsonObject("animation");
        assertEquals(15, animation.get("frametime").getAsInt(), "each drawing now holds for "
                + animation.get("frametime").getAsInt() + " ticks; the two faces are meant to trade places"
                + " every 15 (0.75s)");
        assertTrue(animation.has("interpolate"), METADATA.getFileName() + " should spell out"
                + " \"interpolate\": false - the default is the same value but the choice is deliberate here,"
                + " because blending two unrelated drawings ghosts both of them");
        assertFalse(animation.get("interpolate").getAsBoolean(), "interpolation is on, which cross-fades the"
                + " teal ring into the flat face and paints colours that are in neither drawing");
    }

    @Test
    void theItemModelDrawsThatOneSheet() throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(MODEL)).getAsJsonObject();
        String layer = model.getAsJsonObject("textures").get("layer0").getAsString();
        assertEquals("neoecoprototype:item/music_data_disc", layer, "the item model points layer0 at " + layer
                + "; the animation only plays for the sheet it names");
    }

    private static void assertSilhouette(String frame, boolean[][] opaque) {
        int minX = 16, minY = 16, maxX = -1, maxY = -1, count = 0;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if (!opaque[y][x]) continue;
                count++;
                minX = Math.min(minX, x);
                minY = Math.min(minY, y);
                maxX = Math.max(maxX, x);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals(FOOTPRINT[2], maxX + 1, frame + " ends at column " + (maxX + 1)
                + " but the vanilla disc ends at 15");
        assertEquals(FOOTPRINT[3], maxY + 1, frame + " ends at row " + (maxY + 1)
                + " but the vanilla disc ends at 13");
        assertEquals(FOOTPRINT[0], minX, frame + " starts at column " + minX + " but the vanilla disc starts at 0");
        assertEquals(FOOTPRINT[1], minY, frame + " starts at row " + minY + " but the vanilla disc starts at 3");
        assertEquals(OPAQUE, count, frame + " covers " + count + " pixels but the vanilla disc covers " + OPAQUE
                + "; the item would read as a different size than every other disc in the tab");
    }

    private static boolean[][] mask(BufferedImage sheet, int top) {
        boolean[][] opaque = new boolean[16][16];
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                opaque[y][x] = (sheet.getRGB(x, top + y) >>> 24) != 0;
            }
        }
        return opaque;
    }
}

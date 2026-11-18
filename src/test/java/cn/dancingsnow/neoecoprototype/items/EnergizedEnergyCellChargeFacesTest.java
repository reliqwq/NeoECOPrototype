package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * TedXenon's five charge faces per energized battery tier are wired through blockstate properties, so the
 * chain has to be checked end to end: variant -&gt; model -&gt; texture -&gt; mcmeta. A missing rung is not
 * visible in the item frame or the creative tab (both show the empty face), only on a battery that is being
 * filled, so nothing but this guard sees it.
 */
class EnergizedEnergyCellChargeFacesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");
    private static final int LEVELS = 5;

    @Test
    void everyChargeRungHasAModelATextureAndTheRightAnimationShape() throws Exception {
        for (String tier : new String[]{"16g", "512g"}) {
            var variants = readJson(ASSETS.resolve("blockstates/energized_energy_cell_" + tier + ".json"))
                    .getAsJsonObject("variants");
            assertTrue(variants.size() == LEVELS, tier + ": expected " + LEVELS
                    + " level variants, found " + variants.size());

            for (int level = 0; level < LEVELS; level++) {
                String key = "level=" + level;
                assertTrue(variants.has(key), tier + ": blockstate has no " + key + " variant");
                String modelId = variants.getAsJsonObject(key).get("model").getAsString();
                assertEquals("neoecoprototype:block/energized_energy_cell/cell_" + tier + "_" + level, modelId,
                        tier + " " + key + " points at an unexpected model");

                Path model = ASSETS.resolve("models/block/energized_energy_cell/cell_" + tier + "_"
                        + level + ".json");
                assertTrue(Files.isRegularFile(model), model + " is missing");
                var modelJson = readJson(model).getAsJsonObject("textures");
                String textureId = modelJson.get("all").getAsString();
                assertEquals("neoecoprototype:block/energized_energy_cell/" + tier + "_face_" + level, textureId,
                        "cell_" + tier + "_" + level + " does not show rung " + level);
                assertTrue(modelJson.get("particle").getAsString().equals(textureId),
                        "cell_" + tier + "_" + level + " breaks its own face on the particle");

                Path texture = ASSETS.resolve("textures/block/energized_energy_cell/" + tier + "_face_"
                        + level + ".png");
                assertTrue(Files.isRegularFile(texture), texture + " is missing");
                BufferedImage image = ImageIO.read(texture.toFile());
                assertNotNull(image, texture + " is not a readable image");
                int expectedHeight = "16g".equals(tier) ? 16 : 256;
                assertEquals(16, image.getWidth(), texture + " width");
                assertEquals(expectedHeight, image.getHeight(), texture + " height");

                Path mcmeta = texture.resolveSibling(texture.getFileName() + ".mcmeta");
                if ("16g".equals(tier)) {
                    assertTrue(!Files.exists(mcmeta), mcmeta + " exists but the 16g rung is a still face");
                } else {
                    assertTrue(Files.exists(mcmeta), mcmeta + " is missing: the 512g rung must animate");
                    var animation = readJson(mcmeta).getAsJsonObject("animation");
                    assertTrue(animation.get("interpolate").getAsBoolean(),
                            mcmeta + " must interpolate - the bar fills smoothly, it does not flicker");
                    var frames = animation.getAsJsonArray("frames");
                    assertEquals(16, frames.size(), mcmeta + " must name all 16 frames");
                    var last = frames.get(15).getAsJsonObject();
                    assertEquals(15, last.get("index").getAsInt(), mcmeta + " last frame index");
                    assertEquals(25, last.get("time").getAsInt(), mcmeta + " last frame must hold 25 ticks");
                }
            }

            Path itemModel = ASSETS.resolve("models/item/energized_energy_cell_" + tier + ".json");
            String parent = readJson(itemModel).getAsJsonObject().get("parent").getAsString();
            assertEquals("neoecoprototype:block/energized_energy_cell/cell_" + tier + "_0", parent,
                    itemModel + " should show the empty rung in inventory");
        }
    }

    /** The other half of the chain: nothing may still point at the faces this delivery replaced. */
    @Test
    void theSupersededSingleFaceIsGoneFromEveryReferenceRoot() throws IOException {
        try (var paths = Files.walk(ASSETS)) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString();
                if (!(name.endsWith(".json") || name.endsWith(".mcmeta") || name.endsWith(".md"))) {
                    continue;
                }
                if (name.equals("face_16g.png.mcmeta") || name.equals("face_512g.png.mcmeta")) {
                    continue;
                }
                String text = Files.readString(path, StandardCharsets.UTF_8);
                assertTrue(!text.contains("energized_energy_cell/face_16g")
                                && !text.contains("energized_energy_cell/face_512g"),
                        path + " still references the single face the five-rung set replaced");
            }
        }
        for (String tier : new String[]{"16g", "512g"}) {
            assertTrue(!Files.exists(ASSETS.resolve("models/block/energized_energy_cell/cell_" + tier + ".json")),
                    "cell_" + tier + ".json is unreferenced now and must not ship");
            assertTrue(!Files.exists(ASSETS.resolve("textures/block/energized_energy_cell/face_" + tier + ".png")),
                    "face_" + tier + ".png is unreferenced now and must not ship");
        }
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

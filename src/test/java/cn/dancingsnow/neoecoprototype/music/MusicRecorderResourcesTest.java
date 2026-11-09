package cn.dancingsnow.neoecoprototype.music;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * The recorder has no server-side face for its art and no suite face for its recipes: GameTest never
 * loads models and never resolves JEI ingredients, so a wrong texture path, a missing loot table or a
 * recipe that names an item nobody can craft would ship silently - and the music data type would stay
 * as unobtainable as it was before this machine existed.
 */
final class MusicRecorderResourcesTest {

    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");
    private static final Path DATA = Path.of("src/main/resources/data/neoecoprototype");
    private static final Path SOURCES = Path.of("src/main/java/cn/dancingsnow/neoecoprototype");

    @Test
    void thePanelAndTheBlockFaceAreDrawn() throws Exception {
        BufferedImage panel = image(ASSETS.resolve("textures/gui/music_recorder.png"));
        assertEquals(168, panel.getWidth(), "the recorder panel is " + panel.getWidth() + " px wide; the"
                + " layout and the twelve input cells were placed against a 168-wide texture");
        assertEquals(170, panel.getHeight(), "the recorder panel is 170 px tall; the player inventory half"
                + " is copied from the dissociator panel at that height");
        BufferedImage face = image(ASSETS.resolve("textures/block/music_recorder.png"));
        assertEquals(16, face.getWidth(), "the block face is not 16 px wide");
        assertEquals(16, face.getHeight(), "the block face is not 16 px tall");
    }

    @Test
    void theBlockstateModelAndLootTableNameTheSameBlock() throws Exception {
        JsonObject blockstate = json(ASSETS.resolve("blockstates/music_recorder.json"));
        JsonObject variants = blockstate.getAsJsonObject("variants");
        assertTrue(variants.has(""), "the recorder block has properties but no empty variant; it has no"
                + " blockstate properties at all, so the only legal key is \"\"");
        assertEquals("neoecoprototype:block/music_recorder",
                variants.getAsJsonObject("").get("model").getAsString(),
                "the recorder's variant points somewhere other than its own block model");

        JsonObject model = json(ASSETS.resolve("models/block/music_recorder.json"));
        assertEquals("minecraft:block/cube_all", model.get("parent").getAsString(),
                "the recorder is a plain cube; a different parent needs a matching model file");
        assertEquals("neoecoprototype:block/music_recorder",
                model.getAsJsonObject("textures").get("all").getAsString(),
                "the block model does not name the block face texture that was just checked");

        JsonObject item = json(ASSETS.resolve("models/item/music_recorder.json"));
        assertEquals("neoecoprototype:block/music_recorder", item.get("parent").getAsString(),
                "the item model no longer reuses the block model, so the inventory icon would go missing");

        JsonObject loot = json(DATA.resolve("loot_table/blocks/music_recorder.json"));
        String dropped = loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString();
        assertEquals("neoecoprototype:music_recorder", dropped,
                "breaking the recorder drops " + dropped + " instead of the machine");
    }

    @Test
    void theWholeMusicChainIsCraftable() throws Exception {
        for (String[] recipe : List.of(
                new String[]{"music_recorder", "neoecoprototype:music_recorder"},
                new String[]{"music_jukebox", "neoecoprototype:music_jukebox"},
                new String[]{"music_data_disc", "neoecoprototype:music_data_disc"})) {
            Path file = DATA.resolve("recipe/" + recipe[0] + ".json");
            assertTrue(Files.isRegularFile(file), "no recipe named " + recipe[0] + "; the chain that makes"
                    + " music data visible is not obtainable in survival");
            JsonObject json = json(file);
            assertEquals("minecraft:crafting_shaped", json.get("type").getAsString(),
                    recipe[0] + " is not a shaped crafting recipe");
            assertEquals(recipe[1], json.getAsJsonObject("result").get("id").getAsString(),
                    recipe[0] + " crafts " + json.getAsJsonObject("result").get("id")
                            + " instead of " + recipe[1]);
            JsonObject key = json.getAsJsonObject("key");
            assertTrue(key.size() >= 3, recipe[0] + " uses only " + key.size() + " ingredients; a machine"
                    + " recipe that cheap is usually a placeholder");
            for (var entry : key.entrySet()) {
                assertTrue(entry.getValue().getAsJsonObject().has("item"),
                        recipe[0] + " ingredient " + entry.getKey() + " is not an item entry");
            }
        }
    }

    @Test
    void musicDataIsRegisteredAsAJeiIngredientBehindItsGuards() throws Exception {
        String ingredient = Files.readString(SOURCES.resolve("integration/jei/MusicDataIngredient.java"));
        assertTrue(ingredient.contains("IIngredientType<MusicKey>"),
                "MusicDataIngredient no longer declares an ingredient type, so JEI has nothing to draw");
        assertTrue(ingredient.contains("MusicKeyRenderer.icon()"),
                "the JEI renderer stopped sharing the terminal icon; the key would wear two different faces");
        String converter = Files.readString(
                SOURCES.resolve("integration/jei/MusicDataIngredientConverter.java"));
        assertTrue(converter.contains("implements IngredientConverter<MusicKey>"),
                "the converter no longer plugs into ae2jeiintegration, so Data Energistics' reassembler"
                        + " page falls back to an iconless wrapped stack");
        String plugin = Files.readString(SOURCES.resolve("integration/jei/NeoECOPrototypeJeiPlugin.java"));
        assertTrue(plugin.contains("registerIngredients"), "the JEI plugin has no registerIngredients");
        assertTrue(plugin.contains("isLoaded(\"data_energistics\")"),
                "the music ingredient is not gated on Data Energistics; without it the key cannot exist");
        assertTrue(plugin.contains("isLoaded(\"ae2jeiintegration\")"),
                "the converter is not gated on ae2jeiintegration");
        assertTrue(plugin.contains("addIngredientInfo(MusicKey.INSTANCE"),
                "music data has no JEI info page, so the player still cannot read where it comes from");
    }

    @Test
    void theKeySentencesExistInBothLanguages() throws Exception {
        for (String lang : List.of("en_us", "zh_cn")) {
            JsonObject json = json(ASSETS.resolve("lang/" + lang + ".json"));
            for (String key : List.of("block.neoecoprototype.music_recorder",
                    "gui.neoecoprototype.music_recorder_data_slot",
                    "jei.neoecoprototype.music_data.info",
                    "key.neoecoprototype.music_data")) {
                JsonElement value = json.get(key);
                assertNotNull(value, key + " is missing from " + lang);
                assertTrue(!value.getAsString().isBlank(), key + " is blank in " + lang);
            }
        }
    }

    private static BufferedImage image(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "missing art at " + path);
        BufferedImage image = ImageIO.read(path.toFile());
        assertNotNull(image, "could not decode " + path + " as a png");
        return image;
    }

    private static JsonObject json(Path path) throws Exception {
        assertTrue(Files.isRegularFile(path), "missing " + path);
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}

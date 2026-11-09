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
        assertEquals(176, panel.getWidth(), "the recorder panel is " + panel.getWidth() + " px wide;"
                + " MusicRecorderUI draws the title, the four-column slot block, the data cell and the two"
                + " buttons against a 176-wide texture");
        assertEquals(204, panel.getHeight(), "the recorder panel is " + panel.getHeight() + " px tall;"
                + " MusicRecorderUI places the player inventory half, copied from the dissociator panel,"
                + " at that height");
        BufferedImage buttons = image(ASSETS.resolve("textures/gui/music_recorder_buttons.png"));
        assertEquals(48, buttons.getWidth(), "the button atlas is " + buttons.getWidth()
                + " px wide; the code samples three 16 px plates and three 16 px glyph cells from it");
        assertEquals(32, buttons.getHeight(), "the button atlas is " + buttons.getHeight()
                + " px tall; row 0 is the plate states and row 1 the glyphs");
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
                    "gui.neoecoprototype.music_recorder_idle",
                    "gui.neoecoprototype.music_recorder_now_playing",
                    "gui.neoecoprototype.music_recorder_paused",
                    "gui.neoecoprototype.music_recorder_pause",
                    "gui.neoecoprototype.music_recorder_skip",
                    "jei.neoecoprototype.music_data.info",
                    "key.neoecoprototype.music_data")) {
                JsonElement value = json.get(key);
                assertNotNull(value, key + " is missing from " + lang);
                assertTrue(!value.getAsString().isBlank(), key + " is blank in " + lang);
            }
        }
    }

    /**
     * The two buttons are wired to the block entity and the pause icon follows the server state. There is
     * no suite face for a click and no suite face for a texture swap, so both channels are pinned here: a
     * button without its handler, or a pause state that is read from the client's own block entity, would
     * look right and do nothing.
     */
    @Test
    void thePauseAndSkipButtonsReachTheServer() throws Exception {
        String ui = Files.readString(SOURCES.resolve("blockentity/music/MusicRecorderUI.java"));
        assertTrue(ui.contains("setOnServerClick"),
                "the buttons no longer use the server-click channel; a local click would never reach the"
                        + " block entity that owns the run");
        assertTrue(ui.contains("host::togglePaused"), "the pause button does not call togglePaused");
        assertTrue(ui.contains("host::skipCurrentSong"),
                "the skip button does not call skipCurrentSong");
        assertTrue(ui.contains("boolS2C(host::isPaused)"),
                "the pause icon is not bound server to client; it would keep the face it opened with");
        assertTrue(ui.contains("play.setDisplay(paused)") && ui.contains("pause.setDisplay(!paused)"),
                "the pause icon no longer swaps between the pause and play faces");
        assertTrue(ui.contains("left(0).top(0).width(BUTTON_SIZE).height(BUTTON_SIZE)"),
                "the pause glyph wrapper has no box of its own; Taffy would size it to zero and draw the"
                        + " pause/play face offset from the plate");
        String entities = Files.readString(SOURCES.resolve("blockentity/music/MusicRecorderBlockEntity.java"));
        assertTrue(entities.contains("tickServer(Level level) {\n        if (paused) {"),
                "the recorder ticks while paused; pausing has to stop the run");
        assertTrue(entities.contains("skipped.contains(id)"),
                "the recorder does not filter skipped songs, so a skip would only postpone the song");
        assertTrue(entities.contains("tag.putBoolean(PAUSED_TAG, paused)")
                        && entities.contains("paused = tag.getBoolean(PAUSED_TAG)"),
                "the pause flag is not persisted; a reload would silently start recording again");
    }

    /**
     * The panel names the song it is playing. GameTest never opens the panel, so the binding itself is
     * pinned here: drop it and the room hears a song the panel never names, with nothing going red.
     */
    @Test
    void thePanelNamesTheSongItIsPlaying() throws Exception {
        String ui = Files.readString(SOURCES.resolve("blockentity/music/MusicRecorderUI.java"));
        assertTrue(ui.contains("componentS2C(host::currentSongLine)"),
                "the panel no longer binds the song line server to client; the top strip would stay on"
                        + " whatever text it opened with");
        String be = Files.readString(SOURCES.resolve("blockentity/music/MusicRecorderBlockEntity.java"));
        assertTrue(be.contains("playingName = song.value().description()"),
                "the recorder stopped taking the song's own display name; the line would have to show a"
                        + " raw song id");
        for (String lang : List.of("en_us", "zh_cn")) {
            JsonObject json = json(ASSETS.resolve("lang/" + lang + ".json"));
            assertTrue(json.get("gui.neoecoprototype.music_recorder_now_playing").getAsString()
                            .contains("%s"),
                    "the now-playing line in " + lang + " has no %s slot, so the song name would be"
                            + " dropped from the sentence");
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

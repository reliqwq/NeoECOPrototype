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

    private static final List<String> OUR_FACES = List.of("music_recorder_front", "music_jukebox_front",
            "music_jukebox_top", "music_jukebox_top_with_record");

    /** AE2's own casing ramp: the only greys the machines may be built from. */
    private static final List<Integer> AE_CASING = List.of(
            0xF2F2F2, 0xE8E8EA, 0xDEDFE3, 0xD5D6DC, 0xCBCCD4, 0xBFC1CD,
            0xB3B6C6, 0x9A9FB4, 0x878FA5, 0x787E97, 0x4D4D67, 0x413F54);

    /** The cyan the music family is keyed on, shared with the music key icon and the disc item. */
    private static final List<Integer> MUSIC_ACCENT = List.of(0x009295, 0x79F2D3, 0x05625D, 0x052A32);

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
        for (String face : OUR_FACES) {
            BufferedImage art = image(ASSETS.resolve("textures/block/" + face + ".png"));
            assertEquals(16, art.getWidth(), face + " is " + art.getWidth() + " px wide");
            assertEquals(16, art.getHeight(), face + " is " + art.getHeight() + " px tall");
        }
    }

    @Test
    void theBlockstateModelAndLootTableNameTheSameBlock() throws Exception {
        assertFacings("music_recorder", 4);
        assertFacings("music_jukebox", 8);

        JsonObject model = json(ASSETS.resolve("models/block/music_recorder.json"));
        assertEquals("minecraft:block/cube", model.get("parent").getAsString(),
                "the recorder is no longer a six-faced cube; the casing on its sides and back needs a"
                        + " per-face model");
        assertEquals("neoecoprototype:block/music_recorder_front",
                model.getAsJsonObject("textures").get("north").getAsString(),
                "the recorder's front is not the front texture that was just checked");

        JsonObject item = json(ASSETS.resolve("models/item/music_recorder.json"));
        assertEquals("neoecoprototype:block/music_recorder", item.get("parent").getAsString(),
                "the item model no longer reuses the block model, so the inventory icon would go missing");

        JsonObject loot = json(DATA.resolve("loot_table/blocks/music_recorder.json"));
        String dropped = loot.getAsJsonArray("pools").get(0).getAsJsonObject()
                .getAsJsonArray("entries").get(0).getAsJsonObject().get("name").getAsString();
        assertEquals("neoecoprototype:music_recorder", dropped,
                "breaking the recorder drops " + dropped + " instead of the machine");
    }

    /**
     * The machines have to read as AE devices, not as dark boxes: a light casing on both sides, the back
     * and the bottom, borrowed straight from AE2's own generic textures instead of redrawn, with only the
     * front (and the jukebox's platter) drawn by us. The art those two replaced was one dark six-colour
     * ramp repeated on every face - that is what made them look foreign next to eco's machines, and
     * nothing in the suite can see a texture, so the rule is pinned here.
     */
    @Test
    void theMachinesAreAECasingWithAFaceOfTheirOwn() throws Exception {
        record Machine(String model, String front, String top) {
        }
        for (Machine machine : List.of(
                new Machine("music_recorder", "music_recorder_front", "ae2:block/generics/top"),
                new Machine("music_jukebox", "music_jukebox_front", "music_jukebox_top"),
                new Machine("music_jukebox_with_record", "music_jukebox_front",
                        "music_jukebox_top_with_record"))) {
            JsonObject textures = json(ASSETS.resolve("models/block/" + machine.model() + ".json"))
                    .getAsJsonObject("textures");
            String top = machine.top().startsWith("ae2:")
                    ? machine.top() : "neoecoprototype:block/" + machine.top();
            assertEquals("neoecoprototype:block/" + machine.front(),
                    textures.get("north").getAsString(),
                    machine.model() + " no longer puts its own face on the front");
            assertEquals("ae2:block/generics/side", textures.get("east").getAsString(),
                    machine.model() + " draws its own side; the casing has to come from AE2, a hand-drawn"
                            + " side is how the family drifts back out of the AE look");
            assertEquals(textures.get("east").getAsString(), textures.get("west").getAsString(),
                    machine.model() + " has two different sides; the casing is symmetric");
            assertEquals("ae2:block/generics/back", textures.get("south").getAsString(),
                    machine.model() + " no longer uses AE2's back casing");
            assertEquals("ae2:block/generics/bottom", textures.get("down").getAsString(),
                    machine.model() + " no longer uses AE2's bottom casing");
            assertEquals(top, textures.get("up").getAsString(),
                    machine.model() + " lost its top face");
            assertTrue(!textures.get("north").getAsString().equals(textures.get("up").getAsString()),
                    machine.model() + " repeats one texture on the front and the top; the faces are"
                            + " supposed to tell you which side you are looking at");
        }
        for (String face : OUR_FACES) {
            BufferedImage art = image(ASSETS.resolve("textures/block/" + face + ".png"));
            for (int y = 0; y < 16; y++) {
                for (int x = 0; x < 16; x++) {
                    int rgb = art.getRGB(x, y) & 0xFFFFFF;
                    if (!AE_CASING.contains(rgb) && !MUSIC_ACCENT.contains(rgb)) {
                        throw new AssertionError(face + " has " + String.format("#%06X", rgb) + " at "
                                + x + "," + y + "; every colour on our own faces has to come from the AE"
                                + " casing ramp or the cyan music accent, otherwise the dark ramp that made"
                                + " these machines look non-AE is back");
                    }
                }
            }
            for (int x = 0; x < 16; x++) {
                assertTrue(luminance(art.getRGB(x, 0)) >= 0.85F,
                        face + " has a dark pixel on its top edge at x=" + x + "; the bezel has to be the"
                                + " light AE casing, that is the visual signature of an AE device");
            }
            for (int y = 0; y < 15; y++) {
                assertTrue(luminance(art.getRGB(0, y)) >= 0.85F,
                        face + " has a dark pixel on its left edge at y=" + y);
                assertTrue(luminance(art.getRGB(15, y)) >= 0.85F,
                        face + " has a dark pixel on its right edge at y=" + y);
            }
        }
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
        assertTrue(ingredient.contains("MusicKeyRenderer.blit("),
                "the JEI renderer stopped sharing the terminal's icon draw; the key would wear two"
                        + " different faces");
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

    /**
     * Both GUI paths draw the key with the long blit overload. The icon is a standalone 16x16 png and the
     * short overload normalises against a 256px atlas, so it samples one transparent texel - the key then
     * shows up as an empty slot in the terminal and in JEI with nothing going red.
     */
    @Test
    void theKeyIconIsSampledAtItsOwnSize() throws Exception {
        String renderer = Files.readString(SOURCES.resolve("client/key/MusicKeyRenderer.java"));
        assertTrue(renderer.contains("0.0F, 0.0F, 16, 16, 16, 16"),
                "the key icon is blitted without naming its 16x16 texture size; the short overload"
                        + " assumes a 256px atlas and samples only the top-left texel, so the key draws blank");
        String jei = Files.readString(SOURCES.resolve("integration/jei/MusicDataIngredient.java"));
        assertTrue(jei.contains("MusicKeyRenderer.blit(graphics, 0, 0)"),
                "the JEI renderer stopped sharing the terminal's blit, so the two views can drift apart");
        BufferedImage icon = image(ASSETS.resolve("textures/item/music_data.png"));
        assertEquals(16, icon.getWidth(), "the key icon is " + icon.getWidth() + " px wide");
        assertEquals(16, icon.getHeight(), "the key icon is " + icon.getHeight() + " px tall");
    }

    /**
     * The arrow between the cells and the data disc is the recording clock, and it has no suite face:
     * GameTest never opens a panel and never renders. Its fill is bound to the song's own progress, so one
     * full sweep lands on the tick one song is written onto the data disc; a missing element, a box that
     * drifted off the painted arrow, or a fill on some other clock would look plausible and record wrong.
     */
    @Test
    void theArrowFillsWithTheSongItIsRecording() throws Exception {
        BufferedImage panel = image(ASSETS.resolve("textures/gui/music_recorder.png"));
        int minX = Integer.MAX_VALUE;
        int maxX = -1;
        int minY = Integer.MAX_VALUE;
        int maxY = -1;
        for (int y = 40; y < 90; y++) {
            for (int x = 78; x < 120; x++) {
                if (luminance(panel.getRGB(x, y)) >= 0.4F) {
                    continue;
                }
                minX = Math.min(minX, x);
                maxX = Math.max(maxX, x);
                minY = Math.min(minY, y);
                maxY = Math.max(maxY, y);
            }
        }
        assertEquals("82,54,112,63", minX + "," + minY + "," + maxX + "," + maxY,
                "the arrow the panel paints moved, so the animation box in MusicRecorderUI no longer sits"
                        + " on it");

        String ui = Files.readString(SOURCES.resolve("blockentity/music/MusicRecorderUI.java"));
        for (String box : List.of("ARROW_X = 82", "ARROW_Y = 54", "ARROW_WIDTH = 31",
                "ARROW_HEIGHT = 10", "ARROW_SHAFT_TOP = 4", "ARROW_SHAFT_HEIGHT = 2")) {
            assertTrue(ui.contains(box), "the arrow animation no longer declares " + box
                    + ", so its fill would paint somewhere other than the arrow");
        }
        assertTrue(ui.contains("new ArrowFlow()"), "the panel builds no animated arrow at all");
        assertEquals(2, occurrences(ui, "floatValS2C(() -> host.progressFraction())"),
                "the arrow and the groove no longer both read the song progress; the arrow would run on"
                        + " a clock of its own and finish without a song being written");
        assertTrue(ui.contains("ARROW_SHAFT_LENGTH * value"),
                "the arrow's fill stopped following the song progress, so a sweep would end without a"
                        + " song being written");
        assertTrue(ui.contains("if (value <= 0.0F)"),
                "the arrow paints while no song runs; the face would claim a recording that is not there");
    }

    /**
     * Every horizontal facing needs its own variant, turned 90 degrees per quarter: the model's front is
     * its north face, so a missing variant would leave the machine's face on the wrong side - and a
     * variant that does not rotate would put the same face on every side of a facing block.
     */
    private static void assertFacings(String block, int expectedVariants) throws Exception {
        JsonObject variants = json(ASSETS.resolve("blockstates/" + block + ".json"))
                .getAsJsonObject("variants");
        assertEquals(expectedVariants, variants.size(), block + " has " + variants.size()
                + " variants; four horizontal facings (times has_record for the jukebox) each need one");
        String[] directions = {"north", "east", "south", "west"};
        for (int index = 0; index < directions.length; index++) {
            for (String record : expectedVariants == 8 ? List.of("false", "true") : List.of("")) {
                String key = record.isEmpty() ? "facing=" + directions[index]
                        : "facing=" + directions[index] + ",has_record=" + record;
                assertTrue(variants.has(key), block + " has no variant " + key + ", so the machine"
                        + " would fall back to a model that does not know which way it faces");
                JsonElement turn = variants.getAsJsonObject(key).get("y");
                assertEquals(90 * index, turn == null ? 0 : turn.getAsInt(),
                        block + " at " + key + " does not turn the model a quarter per facing");
            }
        }
    }

    private static int occurrences(String text, String needle) {
        int count = 0;
        for (int at = text.indexOf(needle); at >= 0; at = text.indexOf(needle, at + needle.length())) {
            count++;
        }
        return count;
    }

    private static float luminance(int argb) {
        int red = (argb >> 16) & 0xFF;
        int green = (argb >> 8) & 0xFF;
        int blue = argb & 0xFF;
        return (0.299F * red + 0.587F * green + 0.114F * blue) / 255.0F;
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

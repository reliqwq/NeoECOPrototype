package cn.dancingsnow.neoecoprototype.recipe;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.zip.ZipFile;
import org.junit.jupiter.api.Test;

/**
 * Powder snow's coolant rung, read off the data files rather than off a remembered number.
 *
 * <p>The rung is a pair of jsons - our recipe and the {@code c:} fluid tag it points at - and neither one is
 * checked by the game: eco loads them and the bath matches through {@code SizedFluidIngredient}. So this
 * reads both, and reads the rungs upstream ships straight out of the eco jar to keep the ladder claim honest
 * instead of quoting the numbers I typed when I wrote the file.
 *
 * <p>Only the powder snow rung is tag-open. The ice cream rung stays a hard fluid id by reliqwq's ruling of
 * 2026-11-18 - "我们这个是特殊的冰淇淋" - so do not "unify" the two files: the dessert is one of ours and no
 * other pack's equivalent is meant to be able to drive a bath at rung 8.</p>
 */
class PowderSnowCoolantLineTest {
    private static final Path OUR_COOLING = Path.of("src/main/resources/data/neoecoprototype/recipe/cooling");
    private static final Path COMMON_TAG = Path.of("src/main/resources/data/c/tags/fluid/powder_snow.json");
    private static final String RUNG = "powder_snow.json";

    /**
     * The rung has to be written as a tag, or another pack's powder snow fluid cannot use it - and it has to
     * sit between the rungs upstream already owns, or a player walks past a rung the game never offered.
     */
    @Test
    void theCoolingRungIsWrittenAsACommonTag() throws IOException {
        var rung = read(OUR_COOLING.resolve(RUNG));
        assertEquals("neoecoae:cooling", rung.get("type").getAsString(), RUNG + " is not an eco cooling recipe");
        var input = rung.getAsJsonObject("input");
        assertTrue(input.has("tag") && !input.has("fluid"),
                RUNG + " must name a fluid tag, not a fluid id - a hard id locks the rung to our own fluid and"
                        + " the whole point of the tag is that any pack's powder snow counts");
        assertEquals("c:powder_snow", input.get("tag").getAsString(),
                RUNG + " points at the wrong common tag");
        assertEquals(100, input.get("amount").getAsInt(), RUNG + " draws a different amount than the other rungs");

        List<String> table = new ArrayList<>();
        int ourCoolant = rung.get("coolant").getAsInt();
        int ourOverclock = rung.get("max_overclock").getAsInt();
        JsonObject above = null;
        for (var foreign : listForeignCoolingRungs()) {
            table.add("upstream: coolant " + foreign.get("coolant").getAsInt() + ", overclock "
                    + foreign.get("max_overclock").getAsInt());
            if (foreign.get("coolant").getAsInt() <= ourCoolant) {
                continue;
            }
            if (above == null || foreign.get("coolant").getAsInt() < above.get("coolant").getAsInt()) {
                above = foreign;
            }
        }
        table.add(0, RUNG + " (ours): coolant " + ourCoolant + ", overclock " + ourOverclock);
        for (Path path : listOurs()) {
            if (path.getFileName().toString().equals(RUNG)) {
                continue;
            }
            var other = read(path);
            table.add(path.getFileName() + " (ours): coolant " + other.get("coolant").getAsInt()
                    + ", overclock " + other.get("max_overclock").getAsInt());
            assertTrue(other.get("coolant").getAsInt() != ourCoolant,
                    "two of our cooling rungs share coolant " + ourCoolant + " - one of them is not a rung");
        }

        assertNotNull(above,
                "our rung pays " + ourCoolant + " but no upstream rung pays more, so powder snow is the top"
                        + " coolant in the game and was not meant as a third rung. Ladder: "
                        + String.join(" | ", table));
        assertTrue(ourOverclock <= above.get("max_overclock").getAsInt(),
                "our rung overclocks " + ourOverclock + " while the rung above it only reaches "
                        + above.get("max_overclock").getAsInt() + ". Ladder: " + String.join(" | ", table));
        assertEquals(11000, ourCoolant, RUNG + " moved off the number reliqwq asked for (the third rung,"
                + " between our own 10000 and eco's 12000) - if the ladder was retuned, fix this line"
                + " and the release notes");
    }

    /** The other half of the same claim: the tag exists, and it holds exactly our two fluid forms. */
    @Test
    void theCommonTagHoldsOurFluidAndNothingElse() throws IOException {
        assertTrue(Files.isRegularFile(COMMON_TAG), COMMON_TAG + " is missing, so the rung matches nothing");
        var values = read(COMMON_TAG).getAsJsonArray("values");
        var seen = new TreeSet<String>();
        for (var value : values) {
            seen.add(value.getAsString());
        }
        assertTrue(!seen.isEmpty(), COMMON_TAG + " lists no fluid at all, so the rung can never be used");
        assertTrue(seen.contains("neoecoprototype:powder_snow"),
                COMMON_TAG + " does not list our fluid: " + seen);
        assertEquals(2, seen.size(), "the common tag for powder snow is ours to fill; it already holds " + seen
                + " - if a second fluid was added on purpose, say why here and in the notes");
        for (String id : seen) {
            assertTrue(id.startsWith("neoecoprototype:"), COMMON_TAG + " ships a foreign member: " + id
                    + " - a pack should add its own through its own datapack, not through our jar");
        }
    }

    private static List<Path> listOurs() throws IOException {
        try (var paths = Files.list(OUR_COOLING)) {
            var found = paths.filter(path -> path.getFileName().toString().endsWith(".json")).sorted().toList();
            assertTrue(!found.isEmpty(), "no cooling recipes under " + OUR_COOLING
                    + ", so this rule read nothing");
            return found;
        }
    }

    /** eco's rungs, out of the jar we build against - the same two the ladder claim compares itself with. */
    private static List<JsonObject> listForeignCoolingRungs() throws IOException {
        var rungs = new ArrayList<JsonObject>();
        Path jar = null;
        for (String entry : System.getProperty("java.class.path").split(java.io.File.pathSeparator)) {
            if (entry.replace('\\', '/').contains("/neoecoae-21.2.")) {
                jar = Path.of(entry);
            }
        }
        assertNotNull(jar, "no eco jar on the test classpath, so the ladder was compared against nothing");
        try (var zip = new ZipFile(jar.toFile())) {
            var names = zip.stream().filter(entry -> !entry.isDirectory()
                    && entry.getName().startsWith("data/neoecoae/recipe/cooling/")).toList();
            assertTrue(!names.isEmpty(), jar + " carries no cooling recipes - re-read where upstream puts them");
            for (var entry : names) {
                try (Reader reader = new InputStreamReader(zip.getInputStream(entry), StandardCharsets.UTF_8)) {
                    var parsed = JsonParser.parseReader(reader);
                    if (!parsed.isJsonObject()) {
                        continue;
                    }
                    var json = parsed.getAsJsonObject();
                    if (json.has("coolant") && json.has("max_overclock")) {
                        rungs.add(json);
                    }
                }
            }
        }
        return rungs;
    }

    private static JsonObject read(Path path) throws IOException {
        try (var reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

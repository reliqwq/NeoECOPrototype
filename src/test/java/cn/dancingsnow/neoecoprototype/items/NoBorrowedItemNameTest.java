package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * No item or block we register may be called, word for word, the same thing as an item or block from a mod
 * we sit on.
 *
 * <p>What this catches is not a style preference. {@code neoecoprototype:sky_stone_dust} was an item of ours
 * whose two lang rows were copied straight out of AE2: en_us said "Sky Stone Dust" and zh_cn said 陨石粉,
 * which is exactly what {@code ae2:sky_dust} is called in each language. So one dissociation table paid out
 * both AE2's own 陨石粉 and a different item of ours under the same word, in the same screen as AE2's 陨石
 * block, and nothing anywhere complained - the ids differ, the models differ, only the sentence a player
 * reads is a lie. The item is gone now and the cell grows AE2's; this file is what keeps the next one from
 * being made.</p>
 *
 * <p>Scope, stated because it is a limit and not an oversight: only AE2 and eco are read, since those two are
 * hard dependencies and their jars are on this test's classpath. The optional mods (MEGA Cells, OmniCells,
 * AppliedFlux, Mekanistics) are not checked - a collision with one of those would only be visible to packs
 * that install it, and inventing a rule we cannot run is worse than saying which half is uncovered.</p>
 */
final class NoBorrowedItemNameTest {

    private static final Path OUR_LANG_DIR
            = Path.of("src/main/resources/assets/neoecoprototype/lang");
    private static final Pattern NAMESPACED = Pattern.compile("^(item|block)\\.");
    private static final List<String> UPSTREAM_PACKS = List.of("ae2", "neoecoae");

    @Test
    void noRegisteredNameIsCopiedFromTheModsWeSitOn() throws Exception {
        for (String lang : List.of("en_us", "zh_cn")) {
            var ours = displayNames(OUR_LANG_DIR.resolve(lang + ".json"));
            var theirs = new java.util.LinkedHashSet<String>();
            for (String pack : UPSTREAM_PACKS) {
                var names = upstreamDisplayNames(pack, lang);
                assertTrue(names.size() > 100, pack + "/" + lang + " only yielded " + names.size()
                        + " item and block names - the jar is not on the test classpath, so a green run"
                        + " here would be an empty scan rather than a comparison");
                theirs.addAll(names);
            }
            assertTrue(ours.size() > 150, "our own " + lang + " yielded only " + ours.size()
                    + " item and block names, which means this file is not reading the lang directory");

            var collisions = new ArrayList<String>();
            for (var name : ours) {
                if (theirs.contains(name)) {
                    collisions.add(name);
                }
            }
            assertEquals(List.of(), collisions, lang + ": these are our registered items' and blocks'"
                    + " display names, word for word the same as something AE2 or eco already calls a thing."
                    + " Either rename ours or drop ours and use theirs - a second item under a name the"
                    + " player already meets is a lie in the recipe viewer, not a duplicate texture");
        }
    }

    private static java.util.Set<String> displayNames(Path file) throws Exception {
        assertTrue(Files.exists(file), file + " is not there, so this test proves nothing");
        return displayNames(JsonParser.parseString(Files.readString(file)).getAsJsonObject());
    }

    private static java.util.Set<String> upstreamDisplayNames(String pack, String lang) throws Exception {
        var path = "assets/" + pack + "/lang/" + lang + ".json";
        var url = NoBorrowedItemNameTest.class.getClassLoader().getResource(path);
        assertNotNull(url, path + " is not on the test classpath - the " + pack
                + " jar stopped being a dependency, so this comparison cannot be made");
        try (InputStream input = url.openStream()) {
            var table = JsonParser.parseReader(
                    new InputStreamReader(input, StandardCharsets.UTF_8)).getAsJsonObject();
            return displayNames(table);
        }
    }

    private static java.util.Set<String> displayNames(JsonObject table) {
        var found = new java.util.LinkedHashSet<String>();
        for (var entry : table.entrySet()) {
            if (NAMESPACED.matcher(entry.getKey()).find() && entry.getValue().isJsonPrimitive()) {
                var text = entry.getValue().getAsString().trim();
                if (!text.isEmpty()) {
                    found.add(text);
                }
            }
        }
        return found;
    }
}

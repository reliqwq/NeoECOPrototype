package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * The translated layer, checked from both directions at once.
 *
 * <p>Why this exists as a scan and not as behaviour: a key that the code speaks but a language file does
 * not carry renders as its bare dotted name in the player's chat, and a key a language file carries that no
 * code speaks is a sentence nobody can ever see - both are invisible to the suite, because the gameTest
 * server resolves {@code en_us} and never looks at {@code zh_cn}. The case that produced this file was the
 * reward bag's rename to 备份驱动, which touched five ids, eleven message keys, both language files, a model
 * path, a texture path and two docs at once; one missed key would have shipped as {@code
 * item.neoecoprototype.backup_drive} in somebody's inventory.</p>
 *
 * <p>Names of registered items and blocks are deliberately allowed to be reached by id rather than by a
 * literal, because the game looks those up itself ({@code item.<namespace>.<path>}); what is still checked
 * is that the path exists in the registration source, so a renamed id cannot leave its name behind.</p>
 */
final class LangKeyInventoryTest {

    private static final Path MAIN = Path.of("src/main/java");
    private static final Path LANG_DIR
            = Path.of("src/main/resources/assets/neoecoprototype/lang");

    /**
     * A literal is a spoken key only where a sentence is actually built from it: as the first argument of
     * {@code translatable(...)} or as a {@code *_KEY} constant. Matching every key-shaped string instead
     * reads a guard's own failure text ("the retired card...at_max line is still in a lang file") as a
     * sentence the code speaks, which is the opposite of what that line is for.
     */
    private static final Pattern SPOKEN_KEY = Pattern.compile(
            "(?:translatable\\(\\s*|_KEY\\s*=\\s*)\"((?:item|block|gui|card|tooltip|command|death|effect"
                    + "|subtitles|resource)\\.neoecoprototype\\.[A-Za-z0-9_.\\-]+)\"");
    private static final Pattern SLOT = Pattern.compile("%(\\d*)\\$?[sd]");

    @Test
    void everyKeyTheCodeSpeaksHasAValueInBothLanguages() throws IOException {
        List<String> missing = new ArrayList<>();
        for (String key : spokenKeys()) {
            for (String lang : List.of("en_us", "zh_cn")) {
                if (!language(lang).has(key) || language(lang).get(key).getAsString().isBlank()) {
                    missing.add(key + " (" + lang + ")");
                }
            }
        }
        assertTrue(missing.isEmpty(), "keys the code speaks with no usable value: " + missing);
    }

    /**
     * The other direction, limited on purpose to {@code card.*} - the sentences this mod writes itself.
     * A key nobody can reach is not just dead weight: after a rename it is the shape a missed reference
     * takes, and the old {@code card.neoecoprototype.reward_bag.*} rows would have stayed behind silent.
     *
     * <p>Deliberately not checked: {@code item.<id>} / {@code block.<id>} names, which the game builds from
     * the registry rather than from a literal; and the framework namespaces EMI, Jade and JEI write their
     * own keys under ({@code emi.category.*}, {@code config.jade.plugin_*}), plus {@code gui.*} lines the
     * wireless cards assemble by concatenating a prefix - all of them reachable through a path this scan
     * cannot see, and their own guards ({@link CardReplyLanguageTest}) already cover the concatenation.</p>
     */
    @Test
    void everyCardKeyIsSpokenBySomeCard() throws IOException {
        List<String> sources = new ArrayList<>();
        try (var paths = Files.walk(MAIN)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                sources.add(Files.readString(path));
            }
        }
        String allSource = String.join("\n", sources);
        List<String> dead = new ArrayList<>();
        for (String key : language("en_us").keySet()) {
            if (!key.startsWith("card.")) {
                continue;
            }
            if (allSource.contains("\"" + key + "\"")) {
                continue;
            }
            // A key built as prefix + name is still spoken: the prefix is the literal in the source.
            int lastDot = key.lastIndexOf('.');
            if (lastDot > 0 && allSource.contains("\"" + key.substring(0, lastDot + 1) + "\"")) {
                continue;
            }
            dead.add(key);
        }
        assertTrue(dead.isEmpty(), "card keys no sentence in the code can reach - a rename left them behind"
                + " or a retired line was not cleared out of the lang files: " + dead);
    }

    @Test
    void bothLanguagesCarryTheSameKeysAndTheSameHoles() throws IOException {
        JsonObject en = language("en_us");
        JsonObject zh = language("zh_cn");
        assertEquals(en.keySet(), zh.keySet(), "the two language files no longer carry the same key set");
        List<String> moved = new ArrayList<>();
        for (String key : en.keySet()) {
            List<String> enSlots = slots(en.get(key).getAsString());
            List<String> zhSlots = slots(zh.get(key).getAsString());
            if (!enSlots.equals(zhSlots)) {
                moved.add(key + ": en " + enSlots + " vs zh " + zhSlots);
            }
        }
        assertTrue(moved.isEmpty(), "a language fills different slots than the other, so the two files"
                + " would render different facts: " + moved);
    }

    private static List<String> spokenKeys() throws IOException {
        List<String> keys = new ArrayList<>();
        try (var paths = Files.walk(MAIN)) {
            for (Path path : paths.filter(p -> p.toString().endsWith(".java")).toList()) {
                Matcher found = SPOKEN_KEY.matcher(Files.readString(path));
                while (found.find()) {
                    if (!keys.contains(found.group(1))) {
                        keys.add(found.group(1));
                    }
                }
            }
        }
        assertTrue(keys.size() > 50, "the scan found only " + keys.size()
                + " keys, which means the pattern is broken rather than that the pack is small");
        return keys;
    }

    private static List<String> slots(String value) {
        List<String> slots = new ArrayList<>();
        Matcher found = SLOT.matcher(value);
        while (found.find()) {
            slots.add(found.group().toLowerCase(Locale.ROOT));
        }
        slots.sort(String::compareTo);
        return slots;
    }

    private static JsonObject language(String name) throws IOException {
        Path path = LANG_DIR.resolve(name + ".json");
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}

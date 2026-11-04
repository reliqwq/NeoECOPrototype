package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

/**
 * Every sentence the two wireless cards hand a player must go out as a translation key.
 *
 * <p>Why this is a rule and not a preference: until 2026-11-04 the cards built their refusals as plain English
 * strings and only their successes had keys, so a Chinese player who did something wrong - tapped a controller
 * with the unlink card, tapped a wall, tapped a device that carries nothing - read an English sentence, while
 * the one line they wanted less (the device's whole status instrument) was in Chinese. He saw it himself and
 * named it: "有中文有英文". The mixed case is exactly the failure this scan forbids, and it is a scan rather
 * than a behaviour test because a refusal branch needs a world, a config value and a second grid to reach,
 * whereas the shape "this reply has a key" is visible in the source.
 *
 * <p>{@link #eachReplyUsesTheSameSlotsInBothLanguages} also checks the holes: a key shipped in one language
 * with {@code %s} and in the other with {@code %1$s} throws at the player's chat instead of failing here, and
 * two languages that fill different slots still render - with the wrong fact in the wrong place.
 *
 * <p>Deliberately not covered here: whether the {@code en_us} value still renders byte-for-byte the sentence
 * the code builds for the log. That comparison needs the real arguments, so it lives in the gameTest
 * {@code wireless_reply_keys} batch.
 */
final class CardReplyLanguageTest {

    private static final Path MAIN = Path.of("src/main/java/cn/dancingsnow/neoecoprototype");
    private static final List<Path> CARD_SOURCES = List.of(
            MAIN.resolve("event/WirelessLinks.java"),
            MAIN.resolve("items/WirelessLinkCardItem.java"),
            MAIN.resolve("items/WirelessUnlinkCardItem.java"));

    private static final Path LANG_DIR
            = Path.of("src/main/resources/assets/neoecoprototype/lang");
    private static final String KEY_PREFIX = "gui.neoecoprototype.wireless_link.";
    private static final Pattern REPLY_NAME = Pattern.compile("Reply\\.of\\(\\s*\"([a-z0-9_]+)\"");
    private static final Pattern SLOT = Pattern.compile("%(\\d*)\\$?[sd]");

    @Test
    void noCardReplyCanSpeakWithoutALangKey() throws Exception {
        for (Path source : CARD_SOURCES) {
            String text = Files.readString(source);
            assertFalse(text.contains("untranslated"),
                    source + " builds a reply with no lang key again, so its refusals stop being translated");
        }
    }

    @Test
    void everyReplyNameHasItsKeyInBothLanguages() throws Exception {
        var names = replyNames();
        // Seven replies were keyed before this round (linked / marked / dropped / already_linked / raised /
        // already_top / teardown_moved) and 21 more on 2026-11-04, when every refusal got a key. The count is
        // spelled out so folding one reply into another, or keying a new outcome, has to be said on purpose.
        assertEquals(28, names.size(), "keyed card replies found: " + names);
        var zh = language("zh_cn");
        var en = language("en_us");
        for (String name : names) {
            String key = KEY_PREFIX + name;
            assertTrue(en.has(key), "en_us.json has no " + key
                    + ", so this reply renders as a bare key for English players");
            assertTrue(zh.has(key), "zh_cn.json has no " + key
                    + ", so this reply renders as a bare key for Chinese players");
        }
    }

    @Test
    void eachReplyUsesTheSameSlotsInBothLanguages() throws Exception {
        var zh = language("zh_cn");
        var en = language("en_us");
        for (String name : replyNames()) {
            String key = KEY_PREFIX + name;
            if (!en.has(key) || !zh.has(key)) {
                continue; // the other test reports a missing key; comparing holes here would just double it
            }
            var enSlots = slots(en.get(key).getAsString());
            var zhSlots = slots(zh.get(key).getAsString());
            assertEquals(enSlots.toString(), zhSlots.toString(),
                    key + " fills different argument slots per language, so one of them shows the wrong fact");
            // String.format refuses a format string that uses %s and %1$s at once, so a value that mixes the
            // two throws at the player's chat instead of here. Several bare %s in a row is fine and is what
            // linked / dropped / raised use.
            assertFalse(mixesSlots(enSlots), key + "'s en_us mixes bare %s with indexed slots, which"
                    + " String.format rejects: " + enSlots);
            assertFalse(mixesSlots(zhSlots), key + "'s zh_cn mixes bare %s with indexed slots, which"
                    + " String.format rejects: " + zhSlots);
        }
    }

    private static boolean mixesSlots(List<String> found) {
        boolean bare = found.stream().anyMatch(slot -> slot.equals("%s"));
        boolean indexed = found.stream().anyMatch(slot -> !slot.equals("%s"));
        return bare && indexed;
    }

    /**
     * The reply names the cards actually build. Read from source rather than from a table on
     * {@code WirelessLinks}, because a reply is a call site, not a field.
     */
    private static Set<String> replyNames() throws Exception {
        var names = new LinkedHashSet<String>();
        for (Path source : CARD_SOURCES) {
            Matcher matcher = REPLY_NAME.matcher(Files.readString(source));
            while (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        return names;
    }

    private static List<String> slots(String value) {
        var found = new ArrayList<String>();
        Matcher matcher = SLOT.matcher(value);
        while (matcher.find()) {
            found.add(matcher.group());
        }
        return found;
    }

    private static JsonObject language(String name) throws Exception {
        return JsonParser.parseString(Files.readString(LANG_DIR.resolve(name + ".json"))).getAsJsonObject();
    }
}

package cn.dancingsnow.neoecoprototype.client.render;

import org.junit.jupiter.api.Test;

import java.util.function.Predicate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

/**
 * Guards the bundled doll-skin naming rule. It is pure string logic on purpose: which file wins decides
 * whether a doll is drawn with the 3-pixel or the 4-pixel arm model, and getting it backwards looks like
 * an art bug rather than a code bug.
 */
class FumoSkinNamingTest {

    @Test
    void slimFileWinsWhenBothAreBundled() {
        Predicate<String> everything = name -> true;
        assertEquals(FumoSkinNaming.SLIM_SUFFIX, FumoSkinNaming.pickSuffix("reliqwq", everything),
                "a dual-authored skin must take the slim model, not fall back to classic");
    }

    @Test
    void classicIsTheFallbackAndSlimStillResolvesAlone() {
        assertEquals("", FumoSkinNaming.pickSuffix("reliqwq", name -> name.equals("reliqwq")),
                "classic-only bundles must keep resolving");
        assertEquals(FumoSkinNaming.SLIM_SUFFIX,
                FumoSkinNaming.pickSuffix("yang120", name -> name.equals("yang120_slim")),
                "slim-only bundles must resolve too");
    }

    @Test
    void unknownNameHasNoBundle() {
        assertNull(FumoSkinNaming.pickSuffix("nobody", name -> false),
                "an unbundled owner must fall through to profile resolution, not to a missing file");
    }
}

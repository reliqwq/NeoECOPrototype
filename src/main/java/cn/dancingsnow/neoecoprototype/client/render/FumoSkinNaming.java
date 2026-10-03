package cn.dancingsnow.neoecoprototype.client.render;

import java.util.function.Predicate;

/**
 * The naming rule for doll skins bundled in the jar, kept free of Minecraft types so it can be asserted
 * by a plain unit test - {@code FumoRenderer} itself is unreachable there because a client interface does
 * not resolve on the test classpath.
 *
 * <p>Which file wins decides whether a doll is drawn with the 3-pixel or the 4-pixel arm model, so getting
 * it backwards reads in game as "the arms are textured wrong" and would be blamed on the art.
 */
public final class FumoSkinNaming {
    /** A bundled skin whose arms are 3 pixels wide carries this suffix; see {@link #pickSuffix}. */
    public static final String SLIM_SUFFIX = "_slim";

    private static final String[] PROBE_ORDER = {SLIM_SUFFIX, ""};

    /**
     * Returns the suffix of the bundled skin a name resolves to, or null when it has none. The slim file
     * wins when both are present, so a classic-only bundle stays the fallback rather than the match.
     *
     * @param fileWithoutExtension asked with the bare name plus each candidate suffix
     */
    public static String pickSuffix(String base, Predicate<String> fileWithoutExtension) {
        for (String suffix : PROBE_ORDER) {
            if (fileWithoutExtension.test(base + suffix)) {
                return suffix;
            }
        }
        return null;
    }

    private FumoSkinNaming() {
    }
}

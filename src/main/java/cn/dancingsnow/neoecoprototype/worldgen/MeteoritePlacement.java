package cn.dancingsnow.neoecoprototype.worldgen;

/**
 * Where a floating comet may be placed, written without touching a Minecraft type.
 *
 * <p>The test source set has no Minecraft on its classpath (every JUnit here is a plain-logic test), and
 * {@link CryotheumMeteoriteStructure} extends {@code Structure}, so a gate that lives inside that class
 * cannot be exercised at all. Keeping the decision here means the branches are covered while the structure
 * itself stays a thin caller.
 */
public final class MeteoritePlacement {
    private MeteoritePlacement() {
    }

    /**
     * @param enabled        the server config switch for the whole comet
     * @param radius         the head radius from the structure JSON
     * @param centerY        the height the head is centred on
     * @param minBuildHeight the dimension's lowest buildable block
     * @param maxBuildHeight the dimension's highest buildable block
     */
    public static boolean wantsComet(boolean enabled, int radius, int centerY,
                                     int minBuildHeight, int maxBuildHeight) {
        return enabled
                && centerY + radius < maxBuildHeight
                && centerY - radius > minBuildHeight;
    }
}

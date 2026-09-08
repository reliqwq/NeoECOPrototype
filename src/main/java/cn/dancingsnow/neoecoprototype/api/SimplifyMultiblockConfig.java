package cn.dancingsnow.neoecoprototype.api;

/** Shared geometry constants for the addon-owned L1 multiblocks. */
public final class SimplifyMultiblockConfig {
    /** Maximum repeatable length accepted by the fixed L1 validators. */
    public static final int L1_MAX_LENGTH = 15;

    /** Fixed controller-to-tail offset used by placement definitions. */
    public static final int PLACEMENT_BASE_LENGTH_OFFSET = 4;

    /** Placement repeat upper bound corresponding to the fixed L1 geometry. */
    public static final int L1_PLACEMENT_EXPAND_MAX = L1_MAX_LENGTH - PLACEMENT_BASE_LENGTH_OFFSET;

    private SimplifyMultiblockConfig() {
    }
}

package cn.dancingsnow.neoecoprototype.gametest;

import net.minecraft.world.item.Item;

/**
 * The only place in the game test source that names an Omni Cells class.
 *
 * <p>{@code ECOUniversalStorageCellItem}'s own superclass comes from AE2 Omni Cells, so a method that
 * resolves it on a pack without that mod fails at the check, and the game test bootstrap resolves every
 * {@code @GameTest} body of its class before any test runs. Kept apart, it is linked only when a caller has
 * already asked {@code ModList} whether Omni Cells is there.</p>
 */
final class CellCardPolicyProbe {

    private CellCardPolicyProbe() {
    }

    /**
     * True for every matrix whose storage backend is Omni Cells: eco's own universal cell class, which the
     * KubeJS builder instantiates directly, and the four L1 items that subclass it.
     */
    static boolean isOmniBacked(Item item) {
        return item instanceof cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
    }
}

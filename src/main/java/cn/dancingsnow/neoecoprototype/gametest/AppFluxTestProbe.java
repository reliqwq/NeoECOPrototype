package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.config.Actionable;
import net.minecraft.world.item.ItemStack;

/**
 * The only place in the game test source that touches AppliedFlux classes.
 *
 * <p>It has to live apart from {@code NeoECOPrototypeGameTests}: the game test bootstrap resolves every
 * {@code @GameTest} method on its own class, and a reference to a class from a mod that is not installed
 * stops the server from starting at all - which is far worse than one skipped test. A separate class is
 * only linked when a method here actually runs, and every caller first checks that the flux cells exist.
 */
final class AppFluxTestProbe {

    /** Pushes everything it can into an FE cell and reports how many units the cell took. */
    static long insertEverything(ItemStack stack, Object cellInventory) {
        var key = com.glodblock.github.appflux.common.me.key.FluxKey
                .of(com.glodblock.github.appflux.common.me.key.type.EnergyType.FE);
        return ((cn.dancingsnow.neoecoae.api.storage.IECOStorageCell) cellInventory)
                .insert(key, Long.MAX_VALUE, Actionable.MODULATE, null);
    }

    /** AppliedFlux's induction card. */
    static net.minecraft.world.level.ItemLike inductionCard() {
        return com.glodblock.github.appflux.common.AFSingletons.INDUCTION_CARD;
    }

    private AppFluxTestProbe() {
    }
}

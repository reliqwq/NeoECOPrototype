package cn.dancingsnow.neoecoprototype.integration.dataenergistics;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import net.neoforged.fml.ModList;

/**
 * The one place the music key is pushed into a grid, so the jukebox never spells out a key it may not be
 * allowed to name.
 *
 * <p>The key type is registered by {@link MusicDataEnergisticsPlugin}, which Data Energistics only resolves
 * while it is installed. {@link MusicKey} itself has no Data Energistics imports, so merely loading this
 * class is safe; asking AE2 to store a key type nobody registered is not. Every caller must therefore pass
 * {@link #loaded()} first - which is also the switch that turns the jukebox into a plain player.</p>
 */
public final class MusicNetworkFeed {

    public static boolean loaded() {
        return ModList.get().isLoaded("data_energistics");
    }

    /** How many of {@code ticks} the grid took. Never more than asked for, never negative. */
    public static long insert(IGrid grid, long ticks, IActionSource source) {
        if (grid == null || ticks <= 0L) {
            return 0L;
        }
        return grid.getStorageService().getInventory()
                .insert(MusicKey.INSTANCE, ticks, Actionable.MODULATE, source);
    }

    /** Whether one more tick would be taken, asked without changing anything. */
    public static boolean accepts(IGrid grid, IActionSource source) {
        return grid != null && grid.getStorageService().getInventory()
                .insert(MusicKey.INSTANCE, 1L, Actionable.SIMULATE, source) >= 1L;
    }

    private MusicNetworkFeed() {
    }
}

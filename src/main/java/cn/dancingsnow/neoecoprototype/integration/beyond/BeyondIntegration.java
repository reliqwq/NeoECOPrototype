package cn.dancingsnow.neoecoprototype.integration.beyond;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Optional Beyond Dimensions integration. Loaded only when the dependency is present. */
public final class BeyondIntegration {
    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_BEYOND_STORAGE_CELL = items.register(
                "beyond_storage_cell",
                () -> new BeyondStorageCellItem(new Item.Properties().stacksTo(1)));
    }

    public static void registerCellHandler() {
        BeyondStorageCellHandler.register();
    }

    private BeyondIntegration() {
    }
}

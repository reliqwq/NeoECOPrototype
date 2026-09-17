package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Optional OmniCells integration using Neo ECO's native storage implementations. */
public final class UniversalIntegration {
    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K = items.register("simplify_universal_storage_cell_1k",
                () -> new SimplifyUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1K));
        ModRegistration.OPTIONAL_UNIVERSAL_CELL_1M = items.register("simplify_universal_storage_cell_1m",
                () -> new SimplifyUniversalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1M));
        ModRegistration.OPTIONAL_QUANTUM_CELL_1K = items.register("simplify_quantum_storage_cell_1k",
                () -> new SimplifyQuantumStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1K));
        ModRegistration.OPTIONAL_QUANTUM_CELL_1M = items.register("simplify_quantum_storage_cell_1m",
                () -> new SimplifyQuantumStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1M));
    }

    private UniversalIntegration() {
    }
}

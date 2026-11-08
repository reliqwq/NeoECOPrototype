package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Optional OmniCells integration using Neo ECO's native storage implementations. */
public final class UniversalIntegration {
    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K = items.register("simplify_universal_storage_cell_1k",
                () -> new SimplifyUniversalStorageCellItem(
                        ModRegistration.matrixName("simplify_universal_storage_cell_1k", SimplifyUniversalStorageCellItem.BYTES_1K).stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1K));
        ModRegistration.OPTIONAL_UNIVERSAL_CELL_1M = items.register("simplify_universal_storage_cell_1m",
                () -> new SimplifyUniversalStorageCellItem(
                        ModRegistration.matrixName("simplify_universal_storage_cell_1m", SimplifyUniversalStorageCellItem.BYTES_1M).stacksTo(1),
                        SimplifyUniversalStorageCellItem.BYTES_1M));
        ModRegistration.OPTIONAL_QUANTUM_CELL_1K = items.register("simplify_quantum_storage_cell_1k",
                () -> new SimplifyQuantumStorageCellItem(
                        ModRegistration.matrixName("simplify_quantum_storage_cell_1k", SimplifyQuantumStorageCellItem.BYTES_1K).stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1K));
        ModRegistration.OPTIONAL_QUANTUM_CELL_1M = items.register("simplify_quantum_storage_cell_1m",
                () -> new SimplifyQuantumStorageCellItem(
                        ModRegistration.matrixName("simplify_quantum_storage_cell_1m", SimplifyQuantumStorageCellItem.BYTES_1M).stacksTo(1),
                        SimplifyQuantumStorageCellItem.BYTES_1M));
        ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1K = items.register(
                "simplify_complex_universal_storage_cell_1k",
                () -> new SimplifyComplexUniversalStorageCellItem(
                        ModRegistration.matrixName("simplify_complex_universal_storage_cell_1k", SimplifyComplexUniversalStorageCellItem.BYTES_1K).stacksTo(1),
                        SimplifyComplexUniversalStorageCellItem.BYTES_1K));
        // The waste family is an omni matrix whose single substance comes out of Mekanism, so all three
        // have to be present before this line can even build the item.
        if (net.neoforged.fml.ModList.get().isLoaded("mekanism")
                && net.neoforged.fml.ModList.get().isLoaded("appmek")) {
            ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1K = items.register(
                    "simplify_spent_nuclear_waste_storage_cell_1k",
                    () -> new SimplifySpentNuclearWasteCellItem(
                            ModRegistration.matrixName("simplify_spent_nuclear_waste_storage_cell_1k", SimplifySpentNuclearWasteCellItem.BYTES_1K).stacksTo(1),
                            SimplifySpentNuclearWasteCellItem.BYTES_1K));
            ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1M = items.register(
                    "simplify_spent_nuclear_waste_storage_cell_1m",
                    () -> new SimplifySpentNuclearWasteCellItem(
                            ModRegistration.matrixName("simplify_spent_nuclear_waste_storage_cell_1m", SimplifySpentNuclearWasteCellItem.BYTES_1M).stacksTo(1),
                            SimplifySpentNuclearWasteCellItem.BYTES_1M));
        }
        ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M = items.register(
                "simplify_complex_universal_storage_cell_1m",
                () -> new SimplifyComplexUniversalStorageCellItem(
                        ModRegistration.matrixName("simplify_complex_universal_storage_cell_1m", SimplifyComplexUniversalStorageCellItem.BYTES_1M).stacksTo(1),
                        SimplifyComplexUniversalStorageCellItem.BYTES_1M));
    }

    private UniversalIntegration() {
    }
}

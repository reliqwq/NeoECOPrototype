package cn.dancingsnow.neoecoprototype.integration.mekanism;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.item.Item;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

/** Optional Mekanism integration. This class is loaded only when Mekanism is present. */
public final class MekanismIntegration {
    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_CHEMICAL_CELL_1K = items.register("simplify_chemical_storage_cell_1k",
                () -> new SimplifyChemicalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyChemicalStorageCellItem.BYTES_1K, 1 << 2));
        ModRegistration.OPTIONAL_CHEMICAL_CELL_4K = items.register("simplify_chemical_storage_cell_4k",
                () -> new SimplifyChemicalStorageCellItem(new Item.Properties().stacksTo(1),
                        1L << 12, 1 << 4));
        ModRegistration.OPTIONAL_CHEMICAL_CELL_16K = items.register("simplify_chemical_storage_cell_16k",
                () -> new SimplifyChemicalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyChemicalStorageCellItem.BYTES_16K, 1 << 6));
        ModRegistration.OPTIONAL_CHEMICAL_CELL_1M = items.register("simplify_chemical_storage_cell_1m",
                () -> new SimplifyChemicalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyChemicalStorageCellItem.BYTES_1M, 1 << 12));
        ModRegistration.OPTIONAL_CHEMICAL_CELL_4M = items.register("simplify_chemical_storage_cell_4m",
                () -> new SimplifyChemicalStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyChemicalStorageCellItem.BYTES_4M, 1 << 14));
        // The chemical small-bulk discs also need MegaCells for the mega_chemical cell type.
        if (ModList.get().isLoaded("megacells")) {
            ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL = items.register("simplify_small_bulk_chemical_storage_cell",
                    () -> new SimplifySmallBulkChemicalStorageCellItem(new Item.Properties().stacksTo(1), 3));
            ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED =
                    items.register("simplify_small_bulk_chemical_storage_cell_expanded",
                            () -> new SimplifySmallBulkChemicalStorageCellItem(new Item.Properties().stacksTo(1), 10));
        }
    }

    private MekanismIntegration() {
    }
}

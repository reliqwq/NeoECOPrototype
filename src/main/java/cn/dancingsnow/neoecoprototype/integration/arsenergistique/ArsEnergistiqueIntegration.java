package cn.dancingsnow.neoecoprototype.integration.arsenergistique;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype.api.EcoCellTypes;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import gripe._90.arseng.me.key.SourceKey;
import gripe._90.arseng.me.key.SourceKeyType;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/**
 * Optional Ars Nouveau integration: the source medium at L1.
 *
 * <p>The mod that actually carries the medium is Ars Energistique ({@code arseng}), not Ars Nouveau: it
 * registers the {@code arseng:source} AE2 key type and eco hangs its {@code neoecoae:source} cell type off
 * that same key. Ars Nouveau is arseng's own hard dependency, so it is present whenever this runs.
 *
 * <p>eco ships source cells at L4/L6/L9 only, and our L1 storage host refuses a cell above its own tier -
 * so without this family an Ars player has no way to put source into our storage at all. The five rungs
 * mirror the item and fluid ladders byte for byte (256 types, 1/256 of the capacity each) and carry our
 * L1 tier, which is what lets them mount in our drives and host.
 *
 * <p>The key type is handed to eco as a supplier: eco only asks it when a stack reports what it holds, so
 * naming arseng's class inside that supplier does not load it during registration. This
 * class itself is only touched inside {@code ModList.isLoaded("arseng")}.
 */
public final class ArsEnergistiqueIntegration {
    /** The same type budget every L1 medium carries. */
    private static final int TOTAL_TYPES = 256;

    /**
     * The self-growing member: it grows source and takes nothing in, like the singularity and meteorite-dust
     * cells. Its content supplier reads {@link SourceKey#KEY} on first use - building this profile during the
     * item registration event must not have to touch arseng's static initialiser, and nothing asks a cell
     * what it grows before the registries freeze.
     */
    public static final SimplifySingularityCellItem.SelfGrow SOURCE =
            new SimplifySingularityCellItem.SelfGrow(
                    ArsEnergistiqueIntegration::sourceKey,
                    NeoECOPrototypeServerConfig::sourceCellTicksPerBatch,
                    NeoECOPrototypeServerConfig::sourceCellAmountPerBatch,
                    SimplifySingularityCellItem.SOURCE_CELL_TYPE);

    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_SOURCE_GROWING_CELL = items.register(
                "simplify_source_growing_cell",
                () -> new SimplifySingularityCellItem(
                        new Item.Properties().stacksTo(1), SOURCE));
        ModRegistration.OPTIONAL_SOURCE_CELL_HOUSING = items.register(
                "simplify_source_storage_matrix_housing", () -> new Item(new Item.Properties()));
        ModRegistration.OPTIONAL_SOURCE_CELL_1K = sourceCell(
                items, "simplify_source_storage_cell_1k", SimplifyStorageCellItem.BYTES_1K);
        ModRegistration.OPTIONAL_SOURCE_CELL_16K = sourceCell(
                items, "simplify_source_storage_cell_16k", SimplifyStorageCellItem.BYTES_16K);
        ModRegistration.OPTIONAL_SOURCE_CELL_64K = sourceCell(
                items, "simplify_source_storage_cell_64k", SimplifyStorageCellItem.BYTES_64K);
        ModRegistration.OPTIONAL_SOURCE_CELL_1M = sourceCell(
                items, "simplify_source_storage_cell_1m", SimplifyStorageCellItem.BYTES_1M);
        ModRegistration.OPTIONAL_SOURCE_CELL_4M = sourceCell(
                items, "simplify_source_storage_cell_4m", SimplifyStorageCellItem.BYTES_4M);
    }

    private static Supplier<? extends Item> sourceCell(DeferredRegister<Item> items, String id, long bytes) {
        return items.register(id, () -> new SimplifyStorageCellItem(ModRegistration.matrixName(id, bytes).stacksTo(1),
                ArsEnergistiqueIntegration::sourceKeyType,
                ArsEnergistiqueIntegration::sourceCellType,
                bytes, (int) (bytes >> 8), TOTAL_TYPES));
    }

    /** arseng's own key type: 1000 source per AE byte, the ratio its own source cells are built on. */
    private static AEKeyType sourceKeyType() {
        return SourceKeyType.TYPE;
    }

    /** eco registers this cell type only while arseng is present, which is this family's gate too. */
    private static ECOCellType sourceCellType() {
        return EcoCellTypes.require("source");
    }

    /** The one thing the growing cell holds: arseng's singleton source key. */
    private static AEKey sourceKey() {
        return SourceKey.KEY;
    }

    private ArsEnergistiqueIntegration() {
    }
}

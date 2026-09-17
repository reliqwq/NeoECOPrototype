package cn.dancingsnow.neoecoprototype.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/**
 * Single source of truth for storage-matrix materials.
 *
 * <p>Every matrix family owns one housing (item) and one in-drive model. Nothing else may pick a
 * texture ad hoc, otherwise the same kind of matrix ends up rendered with another kind's palette.
 *
 * <p>Fixed families registered as real items expose their material here too, so a script can render
 * its own matrix in that family's style through {@code .material(...)}:
 * <ul>
 *   <li>小宗 small bulk - {@code item/storage_recolor/small_bulk_cell_housing} (dark grey)</li>
 *   <li>猪咪 pigcat - {@code item/pigcat_storage_cell}</li>
 *   <li>全能 universal - eco omni housing, copied locally</li>
 *   <li>量子 quantum - eco quantum omni housing, copied locally</li>
 * </ul>
 */
public final class MatrixMaterials {
    /** Appearance kinds; a script may also override the material of any matrix. */
    public enum Kind {
        /** 物品 */
        ITEM("item/simplify_item_storage_cell_1k", "block/cell/storage_cell_l1_item"),
        /** 流体 */
        FLUID("item/simplify_fluid_storage_cell_1k", "block/cell/storage_cell_l1_fluid"),
        /** 化学品 */
        CHEMICAL("item/simplify_chemical_storage_cell_1k", "block/cell/storage_cell_l1_chemical"),
        /** 无限 */
        INFINITE("item/simplify_concrete_storage_cell", "block/cell/storage_cell_l1_concrete"),
        /** 其他：物品栏沿用原量子外壳，驱动器为棕色 */
        OTHER("item/simplify_default_storage_cell", "block/cell/storage_cell_l1_custom"),
        /** 小宗（MEGA 家族，深灰） */
        SMALL_BULK("item/simplify_small_bulk_storage_cell", "block/cell/storage_cell_l1_small_bulk"),
        /** 猪咪 */
        PIGCAT("item/pigcat_storage_cell", "block/cell/storage_cell_l1_pigcat"),
        /** 全能 */
        UNIVERSAL("item/simplify_universal_storage_cell_1k", "block/cell/storage_cell_l1_universal"),
        /** 量子 */
        QUANTUM("item/simplify_quantum_storage_cell_1k", "block/cell/storage_cell_l1_quantum");

        private final String inventoryPath;
        private final String drivePath;

        Kind(String inventoryPath, String drivePath) {
            this.inventoryPath = inventoryPath;
            this.drivePath = drivePath;
        }

        public ResourceLocation inventoryModel() {
            return ResourceLocation.fromNamespaceAndPath("neoecoprototype", inventoryPath);
        }

        public ResourceLocation driveModel() {
            return ResourceLocation.fromNamespaceAndPath("neoecoprototype", drivePath);
        }
    }

    /** Material of a realistic matrix: a fluid/chemical keeps its own, an unbounded item matrix is infinite. */
    public static Kind of(StorageMatrixDefinition.MatrixType type, StorageMatrixDefinition.MatrixSize size) {
        return switch (type) {
            case FLUID -> Kind.FLUID;
            case CHEMICAL -> Kind.CHEMICAL;
            case ITEM -> size == StorageMatrixDefinition.MatrixSize.INFINITE ? Kind.INFINITE : Kind.ITEM;
        };
    }

    /** Family names that select a material without changing how the matrix stores things. */
    public static final java.util.List<String> FAMILY_NAMES = java.util.List.of(
            "other", "small_bulk", "pigcat", "universal", "quantum");

    @Nullable
    public static Kind parse(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        try {
            return Kind.valueOf(value.toUpperCase(java.util.Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unknown matrix material: " + value
                    + " (expected item/fluid/chemical/infinite/other/small_bulk/pigcat/universal/quantum)");
        }
    }

    private MatrixMaterials() {
    }
}

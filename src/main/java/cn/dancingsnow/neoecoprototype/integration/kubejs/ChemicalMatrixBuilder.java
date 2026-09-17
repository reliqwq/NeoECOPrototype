package cn.dancingsnow.neoecoprototype.integration.kubejs;

import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry;
import cn.dancingsnow.neoecoprototype.integration.mekanism.SimplifyChemicalStorageCellItem;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

/** KubeJS builder for finite chemical storage matrices. Requires Mekanism and AppMek. */
public final class ChemicalMatrixBuilder extends ItemBuilder {
    private StorageMatrixDefinition.MatrixSize size = StorageMatrixDefinition.MatrixSize.K1;
    private ResourceLocation chemicalType;
    private ResourceLocation inventoryModel;
    private ResourceLocation driveModel;

    public ChemicalMatrixBuilder(ResourceLocation id) {
        super(id);
        this.inventoryModel = ResourceLocation.fromNamespaceAndPath(
                "neoecoprototype", "item/simplify_chemical_storage_cell_1k");
        this.driveModel = ResourceLocation.fromNamespaceAndPath(
                "neoecoprototype", "block/cell/storage_cell_l1_chemical");
    }

    @Info("绑定化学品类型（用于定义和脚本校验；矩阵仍由 AppMek 提供完整化学品存储能力）")
    public ChemicalMatrixBuilder chemicalType(ResourceLocation id) {
        this.chemicalType = id;
        return this;
    }

    @Info("设置容量：1k、4k、16k、1m 或 4m")
    public ChemicalMatrixBuilder size(String value) {
        this.size = switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "1k" -> StorageMatrixDefinition.MatrixSize.K1;
            case "4k" -> StorageMatrixDefinition.MatrixSize.K4;
            case "16k" -> StorageMatrixDefinition.MatrixSize.K16;
            case "1m" -> StorageMatrixDefinition.MatrixSize.M1;
            case "4m" -> StorageMatrixDefinition.MatrixSize.M4;
            default -> throw new IllegalArgumentException("Unknown chemical matrix size: " + value);
        };
        this.inventoryModel = ResourceLocation.fromNamespaceAndPath(
                "neoecoprototype", "item/simplify_chemical_storage_cell_" + value.toLowerCase(java.util.Locale.ROOT));
        return this;
    }

    @Info("覆盖物品栏模型")
    public ChemicalMatrixBuilder inventoryModel(ResourceLocation model) {
        this.inventoryModel = model;
        return this;
    }

    @Info("覆盖驱动器内格子模型")
    public ChemicalMatrixBuilder cellModel(ResourceLocation model) {
        this.driveModel = model;
        return this;
    }

    @Override
    public Item createObject() {
        if (chemicalType == null) {
            throw new IllegalStateException("Chemical matrix '%s' requires chemicalType(...).".formatted(this.id));
        }
        long bytes = this.size.bytes();
        int bytesPerType = (int) (bytes / SimplifyChemicalStorageCellItem.TOTAL_TYPES);
        SimplifyChemicalStorageCellItem item = new SimplifyChemicalStorageCellItem(
                this.createItemProperties().stacksTo(1), bytes, bytesPerType);
        StorageMatrixRegistry.register(StorageMatrixDefinition.finite(
                this.id, StorageMatrixDefinition.MatrixType.CHEMICAL, this.size,
                bytes, bytesPerType, SimplifyChemicalStorageCellItem.TOTAL_TYPES,
                SimplifyTier.L1, this.inventoryModel, this.driveModel), item);
        return item;
    }
}

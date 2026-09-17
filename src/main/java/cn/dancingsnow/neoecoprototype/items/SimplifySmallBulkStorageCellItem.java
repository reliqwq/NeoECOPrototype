package cn.dancingsnow.neoecoprototype.items;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * L1 item-only bulk cell backed by eco's standard storage-cell engine.
 *
 * <p>It intentionally reuses {@link ECOStorageCellItem}: eco continues to own cell persistence,
 * partitioning, migration, upgrades, and AE2 mount behavior. The only L1-specific tuning is the
 * long-capacity byte budget and a deliberately low, fixed type count. Compression remains owned
 * by the MEGA compression upgrade/backend; this item only stores marker configuration.
 */
public final class SimplifySmallBulkStorageCellItem extends ECOStorageCellItem {
    public static final long BULK_BYTES = Long.MAX_VALUE;
    private final int totalTypes;

    public SimplifySmallBulkStorageCellItem(Item.Properties properties,
                                            Supplier<ECOCellType> cellType,
                                            int totalTypes) {
        super(properties, SimplifyTier.L1, AEKeyType.items(), cellType,
                BULK_BYTES, 1, 0.0D);
        if (totalTypes <= 0) {
            throw new IllegalArgumentException("Small bulk cell type count must be positive");
        }
        this.totalTypes = totalTypes;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        ConfigInventory[] holder = new ConfigInventory[1];
        holder[0] = ConfigInventory.configTypes(totalTypes)
                .supportedTypes(Set.of(AEKeyType.items()))
                .changeListener(() -> saveConfigInventory(stack, holder[0]))
                .build();
        holder[0].beginBatch();
        try {
            holder[0].readFromList(stack.getOrDefault(AEComponents.STORAGE_CELL_CONFIG_INV, List.of()));
        } finally {
            holder[0].endBatchSuppressed();
        }
        return holder[0];
    }

    private static void saveConfigInventory(ItemStack stack, ConfigInventory inventory) {
        List<GenericStack> values = new java.util.ArrayList<>(
                stack.getOrDefault(AEComponents.STORAGE_CELL_CONFIG_INV, List.of()));
        while (values.size() < 10) {
            values.add(null);
        }
        List<GenericStack> active = inventory.toList();
        for (int i = 0; i < active.size(); i++) {
            values.set(i, active.get(i));
        }
        stack.set(AEComponents.STORAGE_CELL_CONFIG_INV, values);
    }

    @Override
    public long getBytes() {
        return BULK_BYTES;
    }

    @Override
    public int getBytesPerType() {
        return 1;
    }

    @Override
    public int getTotalTypes() {
        return totalTypes;
    }
}

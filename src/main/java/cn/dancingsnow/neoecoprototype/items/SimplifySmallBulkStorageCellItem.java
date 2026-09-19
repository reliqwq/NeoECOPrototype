package cn.dancingsnow.neoecoprototype.items;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ISaveProvider;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IECOBulkMarkableCellItem;
import cn.dancingsnow.neoecoae.impl.storage.ECOStorageCell;
import cn.dancingsnow.neoecoae.integration.megacells.backend.ECOMegaLongBulkStorageCell;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * L1 item-only bulk cell reusing eco's MEGA long-bulk storage backend, so marked
 * items behave as compression chains (any chain variant folds into the marked
 * form; requires the MEGA Cells compression card).
 *
 * <p>It intentionally reuses {@link ECOStorageCellItem}: eco continues to own cell persistence,
 * partitioning, migration, upgrades, and AE2 mount behavior. The only L1-specific tuning is the
 * long-capacity byte budget and a deliberately low, fixed type count. Compression remains owned
 * by the MEGA compression upgrade/backend; this item only stores marker configuration.
 *
 * <p>Implementing {@code IECOBulkMarkableCellItem} opts this cell into the storage host's manual
 * marking page (neoecoae beta4+; the class fails to load on beta3 and older). The page edits the
 * same {@link #getConfigInventory} data, so marks are shared with the host-side small bulk panel.
 */
public final class SimplifySmallBulkStorageCellItem extends ECOStorageCellItem
        implements IECOBulkMarkableCellItem {
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

    // 标准引擎没有压缩链行为（标记=精确白名单，标铁块塞铁锭进不去）。大盘后端
    // ECOMegaLongBulkStorageCell 实现存储链，并按 beta4 约定从同一份 getConfigInventory
    // 读取标记、容量口径取本物品的 getTotalTypes（3/10）。切换后标准引擎写入的存量
    // 组件不再被读取——更新前先取回盘内内容。
    @Override
    protected ECOStorageCell createCellInventory(ItemStack stack, ISaveProvider saveProvider) {
        if (ModList.get().isLoaded("megacells")) {
            return new ECOMegaLongBulkStorageCell(stack, saveProvider);
        }
        return super.createCellInventory(stack, saveProvider);
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

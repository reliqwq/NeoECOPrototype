package cn.dancingsnow.neoecoprototype.items;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * L1 fluid small-bulk cell: eco's standard fluid storage engine with a tiny
 * per-stack marked partition (3/10 types) and long-capacity bytes. Marks are
 * edited in the AE2 cell workbench like every other partitionable cell;
 * fluids have no compression chains, so the marks are a plain whitelist.
 */
public final class SimplifySmallBulkFluidStorageCellItem extends ECOStorageCellItem {
    public static final long BULK_BYTES = Long.MAX_VALUE;

    private final int totalTypes;

    public SimplifySmallBulkFluidStorageCellItem(Item.Properties properties,
                                                 Supplier<ECOCellType> cellType,
                                                 int totalTypes) {
        super(properties, SimplifyTier.L1, AEKeyType.fluids(), cellType, BULK_BYTES, 1, 0.0D);
        if (totalTypes <= 0) {
            throw new IllegalArgumentException("Small bulk fluid cell type count must be positive");
        }
        this.totalTypes = totalTypes;
    }

    /** ECO MEGA fluid type shared with upstream's mega fluid cells. */
    public static ECOCellType getMegaFluidCellType() {
        ECOCellType type = NERegistries.CELL_TYPE.get(
                ResourceLocation.fromNamespaceAndPath("neoecoae", "mega_fluid"));
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: mega_fluid");
        }
        return type;
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        ConfigInventory[] holder = new ConfigInventory[1];
        holder[0] = ConfigInventory.configTypes(totalTypes)
                .supportedTypes(Set.of(AEKeyType.fluids()))
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

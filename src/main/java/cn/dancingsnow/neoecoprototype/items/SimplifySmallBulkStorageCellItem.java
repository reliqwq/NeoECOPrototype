package cn.dancingsnow.neoecoprototype.items;

import appeng.api.ids.AEComponents;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.cells.ISaveProvider;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoae.storage.ECOStorageCell;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * L1 item-only bulk cell: an unbounded byte budget with very few filter slots.
 *
 * <p>A mark is a whitelist entry, nothing more. This cell used to borrow eco's MEGA long-bulk backend so
 * that marking one form of an item folded every form of its compression chain into that single slot, and
 * that folding asked for the MEGA Cells compression card. The backend is gone by decision: it made this
 * class fail to load if eco ever removed the bulk API it sat on, which is a worse trade than losing the
 * folding - and the folding was already not working, since the fixture asserting it stayed red. The
 * compression card is no longer offered to this cell either.</p>
 *
 * <p>It still reuses {@link ECOStorageCellItem} on purpose. eco owns cell persistence, partitioning,
 * migration, upgrades and AE2 mount behaviour for every cell in the game, and that is a hard dependency
 * this addon accepts; only the bulk-specific surface was cut. The one thing that used to come from the
 * MEGA backend and is now ours to keep is the byte total - {@link #BULK_BYTES} - which is what the family
 * is actually for.</p>
 */
public final class SimplifySmallBulkStorageCellItem extends ECOStorageCellItem {
    public static final long BULK_BYTES = Long.MAX_VALUE;

    /** Where eco's MEGA long-bulk backend kept its contents: {@code CUSTOM_DATA}, not {@code STORAGE_CELL_INV}. */
    private static final String LONG_BULK_TAG = "neoecoae_mega_long_bulk";
    private static final String ENTRIES_TAG = "entries";
    private static final String ITEM_TAG = "item";
    private static final String COMPONENTS_TAG = "components";
    private static final String UNITS_TAG = "units";

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
        List<GenericStack> values = new ArrayList<>(
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

    /**
     * Gives the cell to eco's standard engine, after moving anything an older version stored in eco's
     * MEGA long-bulk payload.
     *
     * <p>The two backends do not share a component: the standard one reads {@code STORAGE_CELL_INV},
     * while the long-bulk one kept its own unit map under {@code CUSTOM_DATA -> neoecoae_mega_long_bulk}.
     * Without this step every small bulk cell in an upgraded world would read as empty - the contents are
     * still on the stack, but nothing looks there any more, which is worse than losing them loudly.</p>
     *
     * <p>Each old entry is already denominated in its own item form (the backend multiplied by the chain
     * factor on the way in), so this is a copy rather than a conversion: no compression-chain lookup, and
     * no MegaCells or eco class touched. Entries whose item is no longer registered cannot be recovered
     * by anyone, so they are counted and reported, and the payload is dropped either way - leaving it
     * would only make every later load retry a parse that can never succeed.</p>
     */
    @Override
    protected ECOStorageCell createCellInventory(ItemStack stack, ISaveProvider saveProvider) {
        migrateLongBulkContents(stack);
        return super.createCellInventory(stack, saveProvider);
    }

    private static void migrateLongBulkContents(ItemStack stack) {
        if (!(stack.get(DataComponents.CUSTOM_DATA) instanceof CustomData held)) {
            return;
        }
        CompoundTag root = held.copyTag();
        CompoundTag payload = root.getCompound(LONG_BULK_TAG);
        if (payload.isEmpty()) {
            return;
        }
        ListTag entries = payload.getList(ENTRIES_TAG, Tag.TAG_COMPOUND);
        List<GenericStack> moved = new ArrayList<>(entries.size());
        int lost = 0;
        for (int i = 0; i < entries.size(); i++) {
            CompoundTag entry = entries.getCompound(i);
            AEItemKey key = readEntryKey(entry);
            long units = entry.getLong(UNITS_TAG);
            if (key == null || units <= 0L) {
                lost++;
                continue;
            }
            moved.add(new GenericStack(key, units));
        }
        if (!moved.isEmpty()) {
            mergeInto(stack, moved);
        }
        if (lost > 0) {
            NeoECOPrototype.LOGGER.warn("small bulk cell: {} long-bulk entries could not be moved because"
                    + " their item is no longer registered", lost);
        }
        root.remove(LONG_BULK_TAG);
        if (root.isEmpty()) {
            stack.remove(DataComponents.CUSTOM_DATA);
        } else {
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(root));
        }
    }

    /** Adds {@code moved} to whatever the standard component already holds, one entry per item. */
    private static void mergeInto(ItemStack stack, List<GenericStack> moved) {
        List<GenericStack> stored = new ArrayList<>(
                stack.getOrDefault(AEComponents.STORAGE_CELL_INV, List.of()));
        for (GenericStack incoming : moved) {
            boolean merged = false;
            for (int i = 0; i < stored.size(); i++) {
                GenericStack here = stored.get(i);
                if (here != null && here.what().equals(incoming.what())) {
                    stored.set(i, new GenericStack(here.what(), here.amount() + incoming.amount()));
                    merged = true;
                    break;
                }
            }
            if (!merged) {
                stored.add(incoming);
            }
        }
        stack.set(AEComponents.STORAGE_CELL_INV, stored);
    }

    private static AEItemKey readEntryKey(CompoundTag entry) {
        ResourceLocation id = ResourceLocation.tryParse(entry.getString(ITEM_TAG));
        if (id == null) {
            return null;
        }
        var item = BuiltInRegistries.ITEM.getHolder(id).orElse(null);
        if (item == null) {
            return null;
        }
        var patch = DataComponentPatch.CODEC
                .parse(NbtOps.INSTANCE, entry.getCompound(COMPONENTS_TAG))
                .result().orElse(DataComponentPatch.EMPTY);
        return AEItemKey.of(new ItemStack(item, 1, patch));
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

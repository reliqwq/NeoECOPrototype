package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import net.minecraft.resources.ResourceLocation;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 * L1 storage matrix cells (1 KiB / 16 KiB / 64 KiB / 1 MiB / 4 MiB).
 *
 * <p>Extends eco's {@link ECOStorageCellItem} so that eco's registered
 * {@code IECOCellHandler} already recognises it ({@code instanceof} check) and
 * routes all cell-engine work (persistence, partition list, upgrades, tooltips)
 * to eco's {@code ECOStorageCell} implementation - we never re-implement the
 * cell itself. The byte / per-type / type-count values are overridden so the L1
 * cells have their own lower capacity regardless of how the base class derives
 * them from the tier.
 *
 * <p>Capacity ladder uses 256 storage types with a bytes-per-type ratio of 1/256:
 * 1 KiB = 1 &lt;&lt; 10, 16 KiB = 1 &lt;&lt; 14, 64 KiB = 1 &lt;&lt; 16,
 * 1 MiB = 1 &lt;&lt; 20, 4 MiB = 1 &lt;&lt; 22.
 */
public class SimplifyStorageCellItem extends ECOStorageCellItem {

    /** 1 KiB */
    public static final long BYTES_1K = 1L << 10;
    /** 16 KiB */
    public static final long BYTES_16K = 1L << 14;
    /** 64 KiB */
    public static final long BYTES_64K = 1L << 16;
    /** 1 MiB */
    public static final long BYTES_1M = 1L << 20;
    /** 4 MiB */
    public static final long BYTES_4M = 1L << 22;

    /**
     * 2.5 KiB - the deliberately small capacity of the pigcat matrix.
     * 2560 = 2.5 x 1024, so the cell reads as "2.5k" next to eco's k/M/MiB ladder.
     * The per-type cost keeps the same bytes/256 ratio as every other L1 cell.
     */
    public static final long BYTES_2K5 = 2560L;
    /** 2560 / 256 types, matching the {@code bytes >> 8} ratio of the other L1 cells. */
    public static final int BYTES_2K5_PER_TYPE = (int) (BYTES_2K5 >> 8);

    private final long bytes;
    private final int bytesPerType;
    private final int totalTypes;

    /** Resolve after eco's custom registry has finished loading. */
    public static ECOCellType getItemCellType() {
        return ecoCellType("items");
    }

    /** Resolve after eco's custom registry has finished loading. */
    public static ECOCellType getFluidCellType() {
        return ecoCellType("fluids");
    }

    private static ECOCellType ecoCellType(String path) {
        ECOCellType type = NERegistries.CELL_TYPE.get(
                ResourceLocation.fromNamespaceAndPath("neoecoae", path));
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: " + path);
        }
        return type;
    }

    /** 1 MiB cell with the original L1 tuning. */
    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType) {
        this(properties, keyType, cellType, BYTES_1M, 1 << 12, 256);
    }

    /** Cell with the L1 type layout: 256 types, each costing bytes / 256. */
    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType,
                                   long bytes) {
        this(properties, keyType, cellType, bytes, (int) (bytes >> 8), 256);
    }

    public SimplifyStorageCellItem(Item.Properties properties, AEKeyType keyType, Supplier<ECOCellType> cellType,
                                   long bytes, int bytesPerType, int totalTypes) {
        // Idle drain scales with capacity exactly like eco's tier-derived cells
        // (totalBytes / 1 MiB AE per tick) instead of staying at the tier's 1.0.
        super(properties, SimplifyTier.L1, keyType, cellType, bytes, bytesPerType, (double) bytes / (1L << 20));
        this.bytes = bytes;
        this.bytesPerType = bytesPerType;
        this.totalTypes = totalTypes;
    }

    @Override
    public long getBytes() {
        return bytes;
    }

    @Override
    public int getBytesPerType() {
        return bytesPerType;
    }

    @Override
    public int getTotalTypes() {
        return totalTypes;
    }
}

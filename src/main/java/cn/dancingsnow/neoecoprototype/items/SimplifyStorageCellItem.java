package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.EcoCellTypes;
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

    /**
     * The three small rungs carry a 25% surcharge over the power-of-two name: AE2's own 1K cell holds 1024
     * bytes and ours holds 1280, so a pair side by side in one drive stores different amounts under the same
     * "1K" label. The ids, models, lang and recipes still say 1k / 16k / 64k on purpose - those strings are
     * the save keys, and renaming one would strand every cell already placed in a world for a label nobody
     * reads off the ground.
     */
    /** 1 KiB + 25% = 1280 bytes. */
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    /** 16 KiB + 25% = 20480 bytes. */
    public static final long BYTES_16K = (1L << 14) * 5 / 4;
    /** 64 KiB + 25% = 81920 bytes. */
    public static final long BYTES_64K = (1L << 16) * 5 / 4;
    /** 1 MiB */
    public static final long BYTES_1M = 1L << 20;
    /** 4 MiB */
    public static final long BYTES_4M = 1L << 22;

    /**
     * 2.5 KiB - the deliberately small capacity of the pigmee matrix.
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
        return EcoCellTypes.require("items");
    }

    /** Resolve after eco's custom registry has finished loading. */
    public static ECOCellType getFluidCellType() {
        return EcoCellTypes.require("fluids");
    }

    /**
     * ECO MEGA item type used by the small-bulk matrix family, or the plain item type if eco stops
     * registering it - see {@code EcoCellTypes.bulkOrPlain} for why the throw was the wrong answer here.
     */
    public static ECOCellType getMegaItemCellType() {
        return EcoCellTypes.bulkOrPlain("mega_item", "items");
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

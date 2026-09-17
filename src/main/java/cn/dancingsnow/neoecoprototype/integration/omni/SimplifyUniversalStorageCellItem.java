package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/**
 * L1 Omni cell using eco's native OmniCells storage backend.
 *
 * <p>The cell type is the registry entry from {@link UniversalCellTypes} rather than a private
 * instance, so eco's storage hosts recognise it and give it its own labelled row.
 */
public final class SimplifyUniversalStorageCellItem extends ECOUniversalStorageCellItem {
    public static final long BYTES_1K = 1L << 10;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 256;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getUniversalCellType() {
        return UniversalCellTypes.UNIVERSAL.get();
    }

    public SimplifyUniversalStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, UniversalCellTypes.UNIVERSAL,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}

package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/**
 * L1 Omni cell using eco's native OmniCells storage backend.
 *
 * <p>It rides eco's registered {@code neoecoae:omni} cell type, so the host panel labels this matrix's row
 * exactly as it labels eco's own omni matrices - the same way the quantum matrix already rides
 * {@code quantum_omni}.
 */
public final class SimplifyUniversalStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6; 1M stays level.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 512;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getUniversalCellType() {
        return NEOmniCellTypes.OMNI.get();
    }

    public SimplifyUniversalStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, NEOmniCellTypes.OMNI::get,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}

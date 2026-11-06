package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/**
 * L1 complex omni matrix, on eco's own 复合全能 row.
 *
 * <p>eco separates 全能 (63 types) from 复合全能 (6400 types) by cell type, not by bytes, so this family
 * borrows eco's registered type instead of minting a label of its own - the same way the quantum matrix
 * already borrows {@code quantum_omni}.
 */
public final class SimplifyComplexUniversalStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6; 1M stays level.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final int TOTAL_TYPES = 1024;
    public static final double IDLE_DRAIN = 8.0D;

    public static ECOCellType getComplexUniversalCellType() {
        return NEOmniCellTypes.COMPLEX_OMNI.get();
    }

    public SimplifyComplexUniversalStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, NEOmniCellTypes.COMPLEX_OMNI::get,
                IDLE_DRAIN, TOTAL_TYPES, bytes);
    }
}

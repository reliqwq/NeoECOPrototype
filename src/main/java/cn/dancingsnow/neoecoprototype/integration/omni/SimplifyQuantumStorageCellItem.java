package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/** L1 quantum matrix using eco's unlimited-type OmniCells backend. */
public final class SimplifyQuantumStorageCellItem extends ECOUniversalStorageCellItem {
    public static final long BYTES_1K = 1L << 10;
    public static final long BYTES_1M = 1L << 20;
    public static final int UNLIMITED_TYPES = -1;
    public static final double IDLE_DRAIN = 8.0D;

    public static cn.dancingsnow.neoecoae.api.storage.ECOCellType getQuantumCellType() {
        return NEOmniCellTypes.QUANTUM_OMNI.get();
    }

    public SimplifyQuantumStorageCellItem(Item.Properties properties, long bytes) {
        super(properties, SimplifyTier.L1, NEOmniCellTypes.QUANTUM_OMNI::get,
                IDLE_DRAIN, UNLIMITED_TYPES, bytes);
    }
}

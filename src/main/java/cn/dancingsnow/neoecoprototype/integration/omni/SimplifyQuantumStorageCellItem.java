package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes;
import cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/** L1 quantum matrix using eco's unlimited-type OmniCells backend. */
public final class SimplifyQuantumStorageCellItem extends ECOUniversalStorageCellItem {
    // The surcharged 1K, same as the item / fluid / chemical / FE families as of 1.4.6. The 1M is where this
    // family is on its own: reliqwq asked for the quantum matrix's top rung to be worth more, so it carries
    // +50% (1572864 bytes) while the other three omni families - and appflux's and our own rungs - stay at
    // the raw 1048576. That makes a quantum 1M half again a universal 1M, which is the point of the rung.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_1M = (1L << 20) * 3 / 2;
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

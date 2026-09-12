package cn.dancingsnow.neoecoprototype.integration.mekanism;

import cn.dancingsnow.neoecoae.integration.appmek.item.ECOChemicalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.world.item.Item;

/** A small L1 Mekanism chemical cell backed by Neo ECO's chemical cell handler. */
public final class SimplifyChemicalStorageCellItem extends ECOChemicalStorageCellItem {
    public static final long BYTES_1K = 1L << 10;
    public static final long BYTES_16K = 1L << 14;
    public static final long BYTES_1M = 1L << 20;
    public static final long BYTES_4M = 1L << 22;
    public static final int TOTAL_TYPES = 25;

    private final long bytes;
    private final int bytesPerType;

    public SimplifyChemicalStorageCellItem(Item.Properties properties, long bytes, int bytesPerType) {
        super(properties, SimplifyTier.L1);
        this.bytes = bytes;
        this.bytesPerType = bytesPerType;
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
        return TOTAL_TYPES;
    }

    @Override
    public double getIdleDrain() {
        return (double) bytes / (1L << 20);
    }
}

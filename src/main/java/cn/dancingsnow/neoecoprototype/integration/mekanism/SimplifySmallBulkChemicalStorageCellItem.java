package cn.dancingsnow.neoecoprototype.integration.mekanism;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.integration.appmek.item.ECOChemicalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/**
 * L1 chemical small-bulk cell: eco's standard chemical storage engine with a
 * tiny per-stack marked partition (3/10 types) and long-capacity bytes. Marks
 * are edited in the AE2 cell workbench; chemicals have no compression chains,
 * so the marks are a plain whitelist.
 */
public final class SimplifySmallBulkChemicalStorageCellItem extends ECOChemicalStorageCellItem {
    public static final long BULK_BYTES = Long.MAX_VALUE;

    private final int totalTypes;

    public SimplifySmallBulkChemicalStorageCellItem(Item.Properties properties, int totalTypes) {
        super(properties, SimplifyTier.L1);
        if (totalTypes <= 0) {
            throw new IllegalArgumentException("Small bulk chemical cell type count must be positive");
        }
        this.totalTypes = totalTypes;
    }

    /** ECO MEGA chemical type shared with upstream's mega chemical cells. */
    @Override
    public ECOCellType getCellType() {
        ECOCellType type = NERegistries.CELL_TYPE.get(
                ResourceLocation.fromNamespaceAndPath("neoecoae", "mega_chemical"));
        if (type == null) {
            throw new IllegalStateException("Missing eco cell type: mega_chemical");
        }
        return type;
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

package cn.dancingsnow.neoecoprototype.integration.mekanism;

import cn.dancingsnow.neoecoae.integration.appmek.item.ECOChemicalStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import appeng.api.stacks.AEKey;

/** A small L1 Mekanism chemical cell backed by Neo ECO's chemical cell handler. */
public final class SimplifyChemicalStorageCellItem extends ECOChemicalStorageCellItem {
    // The three small rungs carry the same 25% surcharge the item and fluid cells do
    // (SimplifyStorageCellItem.BYTES_1K): AE2's 1K is 1024 bytes, ours is 1280, and the rung names stay
    // 1k/16k/64k because those strings are the save keys. TOTAL_TYPES below is untouched, so a chemical
    // cell still holds at most 25 substances - what grew is how much of each fits.
    public static final long BYTES_1K = (1L << 10) * 5 / 4;
    public static final long BYTES_16K = (1L << 14) * 5 / 4;
    public static final long BYTES_64K = (1L << 16) * 5 / 4;
    public static final long BYTES_1M = 1L << 20;
    public static final long BYTES_4M = 1L << 22;
    public static final int TOTAL_TYPES = 25;

    private final long bytes;
    private final int bytesPerType;
    private final ResourceLocation boundChemical;

    public SimplifyChemicalStorageCellItem(Item.Properties properties, long bytes, int bytesPerType) {
        this(properties, bytes, bytesPerType, null);
    }

    public SimplifyChemicalStorageCellItem(Item.Properties properties, long bytes, int bytesPerType,
                                           ResourceLocation boundChemical) {
        super(properties, SimplifyTier.L1);
        this.bytes = bytes;
        this.bytesPerType = bytesPerType;
        this.boundChemical = boundChemical;
    }

    @Override
    public boolean isBlackListed(ItemStack stack, AEKey key) {
        if (boundChemical != null && key instanceof MekanismKey chemicalKey
                && !boundChemical.equals(chemicalKey.getId())) {
            return true;
        }
        return super.isBlackListed(stack, key);
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

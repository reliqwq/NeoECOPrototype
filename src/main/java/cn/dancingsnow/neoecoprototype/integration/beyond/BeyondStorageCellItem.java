package cn.dancingsnow.neoecoprototype.integration.beyond;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import com.wintercogs.beyonddimensions.common.item.NetedItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An ECO item cell whose contents live in a bound Beyond Dimensions network. */
public final class BeyondStorageCellItem extends NetedItem implements IBasicECOCellItem {
    public static final long EXTERNAL_BYTES = Long.MAX_VALUE;
    public static final int EXTERNAL_TYPES = Integer.MAX_VALUE;
    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("item.neoecoprototype.beyond_storage_cell"), EXTERNAL_TYPES);

    public BeyondStorageCellItem(Item.Properties properties) {
        super(properties);
    }

    @Override
    public IECOTier getTier() {
        return SimplifyTier.L1;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
    }

    @Override
    public long getBytes() {
        return EXTERNAL_BYTES;
    }

    @Override
    public int getBytesPerType() {
        return 1;
    }

    @Override
    public int getTotalTypes() {
        return EXTERNAL_TYPES;
    }

    @Override
    public double getIdleDrain() {
        return 1.0D;
    }

    @Override
    public ECOCellType getCellType() {
        return CELL_TYPE;
    }

    // The contents live in the bound network, so there is nothing local to partition
    // or to match fuzzily. Both hooks stay inert instead of faking a cell partition.

    @Override
    public appeng.util.ConfigInventory getConfigInventory(ItemStack stack) {
        return null;
    }

    @Override
    public appeng.api.config.FuzzyMode getFuzzyMode(ItemStack stack) {
        return appeng.api.config.FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack stack, appeng.api.config.FuzzyMode mode) {
        // unsupported: external network storage
    }
}

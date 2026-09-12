package cn.dancingsnow.neoecoprototype.integration.beyond;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOCellHandler;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.IECOTier;
import com.wintercogs.beyonddimensions.api.dimensionnet.DimensionsNet;
import com.wintercogs.beyonddimensions.api.dimensionnet.UnifiedStorage;
import com.wintercogs.beyonddimensions.common.item.NetedItem;
import com.wintercogs.beyonddimensions.integration.module.ae2.me.NetStorageCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/** Bridges a bound Beyond Dimensions UnifiedStorage into Neo ECO's cell API. */
public final class BeyondStorageCellHandler implements IECOCellHandler {
    private static final BeyondStorageCellHandler INSTANCE = new BeyondStorageCellHandler();

    public static void register() {
        ECOStorageCells.register(INSTANCE);
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.getItem() instanceof BeyondStorageCellItem;
    }

    @Override
    @Nullable
    public IECOStorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        if (!isCell(stack)) {
            return null;
        }
        DimensionsNet net = NetedItem.getNet(stack);
        if (net == null || net.getUnifiedStorage() == null) {
            return null;
        }
        return new Adapter(net.getUnifiedStorage());
    }

    private static final class Adapter implements IECOStorageCell {
        private final NetStorageCell delegate;
        private final UnifiedStorage storage;

        private Adapter(UnifiedStorage storage) {
            this.storage = storage;
            this.delegate = new NetStorageCell(storage);
        }

        @Override
        public CellState getStatus() {
            return delegate.getStatus();
        }

        @Override
        public double getIdleDrain() {
            return delegate.getIdleDrain();
        }

        @Override
        public boolean canFitInsideCell() {
            return false;
        }

        @Override
        public void persist() {
            delegate.persist();
        }

        @Override
        public boolean isPreferredStorageFor(AEKey key, IActionSource source) {
            return delegate.isPreferredStorageFor(key, source);
        }

        @Override
        public long insert(AEKey key, long amount, Actionable mode, IActionSource source) {
            return delegate.insert(key, amount, mode, source);
        }

        @Override
        public long extract(AEKey key, long amount, Actionable mode, IActionSource source) {
            return delegate.extract(key, amount, mode, source);
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            delegate.getAvailableStacks(out);
        }

        @Override
        public Component getDescription() {
            return Component.translatable("item.neoecoprototype.beyond_storage_cell");
        }

        @Override
        public IECOTier getTier() {
            return cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1;
        }

        @Override
        public ECOCellType getCellType() {
            return BeyondStorageCellItem.CELL_TYPE;
        }

        @Override
        public long getStoredItemTypes() {
            KeyCounter snapshot = new KeyCounter();
            delegate.getAvailableStacks(snapshot);
            return snapshot.size();
        }

        @Override
        public long getTotalItemTypes() {
            return BeyondStorageCellItem.EXTERNAL_TYPES;
        }

        @Override
        public long getUsedBytes() {
            KeyCounter snapshot = new KeyCounter();
            delegate.getAvailableStacks(snapshot);
            return Math.min(Long.MAX_VALUE, snapshot.size());
        }

        @Override
        public long getTotalBytes() {
            return BeyondStorageCellItem.EXTERNAL_BYTES;
        }
    }

    private BeyondStorageCellHandler() {
    }
}

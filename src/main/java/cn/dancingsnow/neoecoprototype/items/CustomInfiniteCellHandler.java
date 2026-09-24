package cn.dancingsnow.neoecoprototype.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOCellHandler;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

/**
 * KubeJS 自定义无限存储矩阵的接入：与 {@link InfiniteConcreteCellHandler} 同一套
 * "无限仓库"行为，但绑定键由脚本决定。格子无状态、无需持久化。
 */
public final class CustomInfiniteCellHandler implements IECOCellHandler {

    private static final CustomInfiniteCellHandler INSTANCE = new CustomInfiniteCellHandler();

    public static void register() {
        ECOStorageCells.register(INSTANCE);
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.getItem() instanceof CustomInfiniteCellItem;
    }

    @Override
    @Nullable
    public IECOStorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        return isCell(stack) ? new Cell((CustomInfiniteCellItem) stack.getItem()) : null;
    }

    /** A stateless endless source bound to one script-chosen key. */
    private static final class Cell implements IECOStorageCell {
        private final CustomInfiniteCellItem item;

        private Cell(CustomInfiniteCellItem item) {
            this.item = item;
        }

        private boolean matches(AEKey what) {
            return item.getRecord().equals(what);
        }

        @Override
        public CellState getStatus() {
            return CellState.NOT_EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return CustomInfiniteCellItem.IDLE_DRAIN;
        }

        @Override
        public boolean canFitInsideCell() {
            // An endless source must not disappear into another storage cell.
            return false;
        }

        @Override
        public void persist() {
            // stateless
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            return matches(what);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            // Deposits are subsumed into the endless supply and not tracked.
            return matches(what) ? amount : 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return matches(what) ? amount : 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            out.add(item.getRecord(),
                    CustomInfiniteCellItem.REPORTED_AMOUNT * item.getKeyType().getAmountPerUnit());
        }

        @Override
        public Component getDescription() {
            return item.getDescription();
        }

        @Override
        public cn.dancingsnow.neoecoae.api.IECOTier getTier() {
            return SimplifyTier.L1;
        }

        @Override
        public ECOCellType getCellType() {
            return item.getCellType();
        }

        @Override
        public long getStoredItemTypes() {
            return 1;
        }

        @Override
        public long getTotalItemTypes() {
            return 1;
        }

        @Override
        public long getUsedBytes() {
            return 0;
        }

        @Override
        public long getTotalBytes() {
            return CustomInfiniteCellItem.REPORTED_BYTES;
        }

        // 让存储主机按无限盘展示（"无限"），而不是无意义的"类型: 1 / 字节: 0/1"。
        @Override
        public boolean hasInfiniteTypeCapacity() {
            return true;
        }

        @Override
        public boolean isInfiniteStorageEligible() {
            return false;
        }
    }
}

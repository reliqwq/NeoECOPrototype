package cn.dancingsnow.neoecoprototype.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOCellHandler;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Bridges the infinite concrete storage matrix into Neo ECO's cell API, in the
 * style of ExtendedAE's ME infinity cells: while mounted, concrete deposits are
 * subsumed into an endless virtual supply (routed here first, so the regular
 * matrices never hold concrete), and any of the 16 vanilla concrete colors can
 * be extracted endlessly. The cell is stateless - there is nothing to persist.
 */
public final class InfiniteConcreteCellHandler implements IECOCellHandler {
    static final List<AEItemKey> CONCRETE_KEYS = List.of(
            AEItemKey.of(Blocks.WHITE_CONCRETE),
            AEItemKey.of(Blocks.ORANGE_CONCRETE),
            AEItemKey.of(Blocks.MAGENTA_CONCRETE),
            AEItemKey.of(Blocks.LIGHT_BLUE_CONCRETE),
            AEItemKey.of(Blocks.YELLOW_CONCRETE),
            AEItemKey.of(Blocks.LIME_CONCRETE),
            AEItemKey.of(Blocks.PINK_CONCRETE),
            AEItemKey.of(Blocks.GRAY_CONCRETE),
            AEItemKey.of(Blocks.LIGHT_GRAY_CONCRETE),
            AEItemKey.of(Blocks.CYAN_CONCRETE),
            AEItemKey.of(Blocks.PURPLE_CONCRETE),
            AEItemKey.of(Blocks.BLUE_CONCRETE),
            AEItemKey.of(Blocks.BROWN_CONCRETE),
            AEItemKey.of(Blocks.GREEN_CONCRETE),
            AEItemKey.of(Blocks.RED_CONCRETE),
            AEItemKey.of(Blocks.BLACK_CONCRETE));

    private static final InfiniteConcreteCellHandler INSTANCE = new InfiniteConcreteCellHandler();

    public static void register() {
        ECOStorageCells.register(INSTANCE);
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.getItem() instanceof SimplifyConcreteStorageCellItem;
    }

    @Override
    @Nullable
    public IECOStorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        return isCell(stack) ? new Cell() : null;
    }

    private static boolean isConcrete(AEKey what) {
        return what instanceof AEItemKey itemKey && CONCRETE_KEYS.contains(itemKey);
    }

    /** A stateless endless source: every concrete color, always, forever. */
    private static final class Cell implements IECOStorageCell {
        private static final Component DESCRIPTION = Component.translatable(
                "item.neoecoprototype.simplify_concrete_storage_cell");

        @Override
        public CellState getStatus() {
            return CellState.NOT_EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return SimplifyConcreteStorageCellItem.IDLE_DRAIN;
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
            // Keep concrete out of the regular matrices: the network routes
            // concrete here first.
            return isConcrete(what);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            // Deposits are subsumed into the endless supply and not tracked -
            // concrete never accumulates in the regular matrices.
            return isConcrete(what) ? amount : 0;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            return isConcrete(what) ? amount : 0;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (AEItemKey concrete : CONCRETE_KEYS) {
                out.add(concrete, SimplifyConcreteStorageCellItem.REPORTED_AMOUNT_PER_TYPE);
            }
        }

        @Override
        public Component getDescription() {
            return DESCRIPTION;
        }

        @Override
        public cn.dancingsnow.neoecoae.api.IECOTier getTier() {
            return SimplifyTier.L1;
        }

        @Override
        public ECOCellType getCellType() {
            return SimplifyConcreteStorageCellItem.CELL_TYPE;
        }

        @Override
        public long getStoredItemTypes() {
            return CONCRETE_KEYS.size();
        }

        @Override
        public long getTotalItemTypes() {
            return CONCRETE_KEYS.size();
        }

        @Override
        public long getUsedBytes() {
            return 0;
        }

        @Override
        public long getTotalBytes() {
            return SimplifyConcreteStorageCellItem.REPORTED_BYTES;
        }
    }
}

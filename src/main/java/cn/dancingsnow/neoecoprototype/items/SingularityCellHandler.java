package cn.dancingsnow.neoecoprototype.items;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IECOCellHandler;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.jetbrains.annotations.Nullable;

/**
 * The cell engine behind {@link SimplifySingularityCellItem}: it grows singularities on the world clock,
 * hands them out on request, and refuses everything pushed into it.
 *
 * <p>No accumulator thread and no ticker - the bank is computed from the two numbers on the stack each time
 * it is asked, so nothing has to run while the cell sits idle.
 */
public final class SingularityCellHandler implements IECOCellHandler {

    private static final SingularityCellHandler INSTANCE = new SingularityCellHandler();

    public static void register() {
        cn.dancingsnow.neoecoae.api.storage.ECOStorageCells.register(INSTANCE);
    }

    /**
     * The world tick the network is running on, or -1 where there is no server to ask: a multiplayer client
     * has no access to the server's clock, and then the cell simply reports nothing rather than guessing.
     */
    public static long serverGameTime() {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        if (server == null) {
            return -1L;
        }
        Level level = server.getLevel(Level.OVERWORLD);
        return level == null ? -1L : level.getGameTime();
    }

    @Override
    public boolean isCell(ItemStack stack) {
        return stack.getItem() instanceof SimplifySingularityCellItem;
    }

    @Override
    @Nullable
    public IECOStorageCell getCellInventory(ItemStack stack, @Nullable ISaveProvider saveProvider) {
        return isCell(stack) ? new Cell(stack) : null;
    }

    /** Bound to the stack it was handed, because the only state worth keeping lives on the stack. */
    private static final class Cell implements IECOStorageCell {
        private final ItemStack stack;

        private Cell(ItemStack stack) {
            this.stack = stack;
        }

        private long ready() {
            long now = serverGameTime();
            if (now < 0L) {
                return 0L;
            }
            var bank = SimplifySingularityCellItem.stampIfNeeded(stack, now);
            return bank == null ? 0L : SimplifySingularityCellItem.bankOf(bank, now);
        }

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            // It grows its own stock; anything pushed at it is refused outright.
            return 0L;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            if (!SimplifySingularityCellItem.SINGULARITY.equals(what)) {
                return 0L;
            }
            long ready = ready();
            long taken = Math.min(amount, ready);
            if (taken <= 0L) {
                return 0L;
            }
            if (mode == Actionable.MODULATE) {
                var bank = stack.get(SimplifySingularityCellItem.bankComponent());
                if (bank != null) {
                    stack.set(SimplifySingularityCellItem.bankComponent(),
                            new SimplifySingularityCellItem.Bank(bank.startGameTime(), bank.drawn() + taken));
                }
            }
            return taken;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            long ready = ready();
            if (ready > 0L) {
                out.add(SimplifySingularityCellItem.SINGULARITY, ready);
            }
        }

        @Override
        public CellState getStatus() {
            return ready() > 0L ? CellState.NOT_EMPTY : CellState.EMPTY;
        }

        @Override
        public double getIdleDrain() {
            return SimplifySingularityCellItem.IDLE_DRAIN;
        }

        @Override
        public boolean canFitInsideCell() {
            // Its own stock must not be swallowed by another storage cell.
            return false;
        }

        @Override
        public void persist() {
            // The bank lives on the stack itself, so there is nothing separate to write.
        }

        @Override
        public boolean isPreferredStorageFor(AEKey what, IActionSource source) {
            // It takes nothing in, so routing anything here would only lose it.
            return false;
        }

        @Override
        public Component getDescription() {
            return stack.getHoverName();
        }

        @Override
        public IECOTier getTier() {
            return SimplifyTier.L1;
        }

        @Override
        public ECOCellType getCellType() {
            return SimplifySingularityCellItem.CELL_TYPE;
        }

        @Override
        public long getStoredItemTypes() {
            return ready() > 0L ? 1L : 0L;
        }

        @Override
        public long getTotalItemTypes() {
            return SimplifySingularityCellItem.TYPES;
        }

        @Override
        public long getUsedBytes() {
            return 0L;
        }

        @Override
        public long getTotalBytes() {
            return SimplifySingularityCellItem.REPORTED_BYTES;
        }

        @Override
        public boolean hasInfiniteTypeCapacity() {
            return false;
        }

        @Override
        public boolean isInfiniteStorageEligible() {
            // Not a candidate for eco's infinite-storage migration: its stock is generated, not migrated.
            return false;
        }
    }
}

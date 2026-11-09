package cn.dancingsnow.neoecoprototype.blockentity.storage;

import appeng.api.config.Actionable;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.MEStorage;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.storage.SaturatingStackAccumulator;
import cn.dancingsnow.neoecoae.storage.transfer.ECOIOPortTransfer;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Moves contents between the L1 cluster's cells and the grid the 通讯接口 is attached to, in INPUT / OUTPUT mode.
 *
 * <p>eco drives this from its own storage controller's ticker ({@code ECOStorageInterfaceTransfer} called out of
 * {@code ECOStorageSystemBlockEntity#tick}), and our host is not that class, so nothing on the write side ever ran:
 * the mode button persisted a field that had no reader. This is the same chain, driven from
 * {@link SimplifyStorageHostBlockEntity#tick} instead, over the same engine ({@link ECOIOPortTransfer}) so the
 * per-tick budget, the storage-bus exclusion and the creative-input option all behave as they do upstream.</p>
 */
final class SimplifyStorageInterfaceTransfer {

    private final SimplifyStorageHostBlockEntity host;
    private final ECOIOPortTransfer ioPortTransfer = new ECOIOPortTransfer();
    @Nullable
    private CombinedCells cachedCombined;

    SimplifyStorageInterfaceTransfer(SimplifyStorageHostBlockEntity host) {
        this.host = host;
    }

    /** @return how many units moved this tick. */
    long transferContents(SimplifyStorageInterfaceBlockEntity iface) {
        if (!host.isFormed() || !iface.isStorageTransferMode() || !iface.isTargetOnline()) {
            return 0L;
        }
        IGrid grid = iface.getMainNode().getGrid();
        if (grid == null) {
            return 0L;
        }
        MEStorage source = hostStorage();
        if (source == null) {
            return 0L;
        }
        return ioPortTransfer.transfer(grid, source, iface.isStorageInputMode(),
                iface.isIgnoringCreativeStorageInput(), iface.isExcludingStorageBusContainers(),
                IActionSource.ofMachine(iface), iface::acceptsStorageResource);
    }

    /**
     * The cluster's accepted cells as one inventory, cached until the set of cells changes. Acceptance is the
     * drive's own rule, so a cell the host refuses to mount is refused here too - the transfer must not become a
     * back door around the tier check.
     */
    @Nullable
    private MEStorage hostStorage() {
        SimplifyStorageCluster cluster = host.getCluster();
        if (cluster == null) {
            return null;
        }
        List<MEStorage> cells = new ArrayList<>();
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            IECOStorageCell cell = drive.getAcceptedCellInventory();
            if (cell != null) {
                cells.add(cell);
            }
        }
        if (cells.isEmpty()) {
            cachedCombined = null;
            return null;
        }
        if (cachedCombined == null || !cachedCombined.inventories().equals(cells)) {
            cachedCombined = new CombinedCells(List.copyOf(cells), host.getBlockState().getBlock().getName());
        }
        return cachedCombined;
    }

    /**
     * Copied from eco's {@code ECOStorageInterfaceTransfer$CombinedStorage}, minus its infinite-domain view:
     * inserts go to the highest-priority cell first (so marked cells keep winning), extracts walk the list in
     * order, and the listing saturates instead of overflowing when several cells hold the same key.
     */
    private record CombinedCells(List<MEStorage> inventories, Component description) implements MEStorage {

        @Override
        public long insert(AEKey what, long amount, Actionable mode, IActionSource source) {
            long inserted = 0L;
            List<MEStorage> sorted = inventories.stream()
                    .sorted(Comparator.comparingInt(CombinedCells::storagePriority).reversed())
                    .toList();
            for (MEStorage storage : sorted) {
                if (inserted >= amount) {
                    break;
                }
                inserted += storage.insert(what, amount - inserted, mode, source);
            }
            return inserted;
        }

        @Override
        public long extract(AEKey what, long amount, Actionable mode, IActionSource source) {
            long extracted = 0L;
            for (MEStorage storage : inventories) {
                if (extracted >= amount) {
                    break;
                }
                extracted += storage.extract(what, amount - extracted, mode, source);
            }
            return extracted;
        }

        @Override
        public void getAvailableStacks(KeyCounter out) {
            for (MEStorage storage : inventories) {
                KeyCounter local = new KeyCounter();
                storage.getAvailableStacks(local);
                SaturatingStackAccumulator.addAll(out, local);
            }
        }

        @Override
        public Component getDescription() {
            return description;
        }

        private static int storagePriority(MEStorage storage) {
            if (storage instanceof IECOStorageCell cell) {
                int priority = cell.getStoragePriority();
                if (priority == 0 && cell.prioritizesMarkedInserts()) {
                    return 1;
                }
                return priority;
            }
            return 0;
        }
    }
}

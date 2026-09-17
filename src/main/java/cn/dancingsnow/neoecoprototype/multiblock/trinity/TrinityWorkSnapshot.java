package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;

/** Immutable read-only view of Trinity's data, energy, and work readiness. */
public record TrinityWorkSnapshot(
        TrinityEnergySnapshot energy,
        boolean storageCellPresent,
        boolean storageMounted,
        long storageUsedBytes,
        long storageTotalBytes,
        long storageItemTypes,
        boolean computationCellPresent,
        /** Trinity module configuration capacity; never an AE2/eco CPU capacity. */
        long computationConfigurationCapacity,
        int patternCount,
        boolean inputValid,
        String state) {

    public static TrinityWorkSnapshot capture(SimplifyTrinityCluster cluster, boolean inputValid) {
        SimplifyTrinityStorageModuleBlockEntity storage = cluster.getStorageModule();
        SimplifyTrinityComputationModuleBlockEntity computation = cluster.getComputationModule();
        SimplifyTrinityCraftingModuleBlockEntity crafting = cluster.getCraftingModule();

        long used = 0;
        long total = 0;
        long itemTypes = 0;
        if (storage != null) {
            IECOStorageCell cell = storage.getCellInventory();
            if (cell != null) {
                used = cell.getUsedBytes();
                total = cell.getTotalBytes();
                itemTypes = cell.getStoredItemTypes();
            }
        }
        boolean energyReady = cluster.getEnergySnapshot().available();
        boolean cellsReady = storage != null && storage.hasCell()
                && computation != null && computation.hasCell();
        int patterns = crafting == null ? 0 : crafting.getAvailablePatterns().size();
        String state = !inputValid ? "BLOCKED_INPUT"
                : !energyReady ? "BLOCKED_ENERGY"
                : !cellsReady ? "BLOCKED_CELL"
                : patterns == 0 ? "READY_NO_PATTERN" : "READY";
        return new TrinityWorkSnapshot(cluster.getEnergySnapshot(), storage != null && storage.hasCell(),
                storage != null && storage.isMounted(), used, total, itemTypes,
                computation != null && computation.hasCell(),
                computation == null ? 0L : computation.getComputationConfigurationCapacity(), patterns,
                inputValid, state);
    }
}

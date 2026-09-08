package cn.dancingsnow.neoecoprototype.integration.ae2;

import appeng.api.networking.IGrid;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.storage.IStorageProvider;

/** Narrow AE2 boundary used by storage business logic. */
public final class SimplifyGridFacade {
    private SimplifyGridFacade() {
    }

    public static boolean isConnected(IManagedGridNode node) {
        return node != null && node.getNode() != null && node.getGrid() != null;
    }

    public static boolean isOnSameGrid(IManagedGridNode first, IManagedGridNode second) {
        IGrid firstGrid = first == null ? null : first.getGrid();
        IGrid secondGrid = second == null ? null : second.getGrid();
        return firstGrid != null && firstGrid == secondGrid;
    }

    public static void requestStorageUpdate(IManagedGridNode node) {
        if (isConnected(node)) {
            IStorageProvider.requestUpdate(node);
        }
    }

    public static void postPowerStorageEvent(IManagedGridNode node, IAEPowerStorage storage,
                                             GridPowerStorageStateChanged.PowerEventType type) {
        if (node != null) {
            node.ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(storage, type)));
        }
    }

    public static void postProvidePowerEvent(IManagedGridNode node, IAEPowerStorage storage) {
        postPowerStorageEvent(node, storage, GridPowerStorageStateChanged.PowerEventType.PROVIDE_POWER);
    }

    public static void postReceivePowerEvent(IManagedGridNode node, IAEPowerStorage storage) {
        postPowerStorageEvent(node, storage, GridPowerStorageStateChanged.PowerEventType.RECEIVE_POWER);
    }

    public static void alertDevice(IManagedGridNode node) {
        if (node != null) {
            node.ifPresent((grid, gridNode) -> grid.getTickManager().alertDevice(gridNode));
        }
    }
}

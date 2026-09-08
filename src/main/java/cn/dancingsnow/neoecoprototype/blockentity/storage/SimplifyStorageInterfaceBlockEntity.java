package cn.dancingsnow.neoecoprototype.blockentity.storage;

import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.orientation.BlockOrientation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * Interface between the L1 storage subsystem and the outside ME network.
 *
 * <p>The latest eco storage contract puts finite cell mounting on each drive's
 * {@code IStorageProvider} node. This interface therefore only bridges the L1
 * energy cells for grids where member power nodes are not yet merged, preserving
 * compatibility with worlds created by the earlier addon implementation.
 */
public class SimplifyStorageInterfaceBlockEntity extends ECOMachineInterfaceBlockEntity<SimplifyStorageCluster>
        implements IStorageProvider, IAEPowerStorage {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageInterfaceBlockEntity.class);

    /** Last bridge state emitted to the server log; avoids repeating identical tick/event messages. */
    private String lastLoggedBridgeState;

    /** Only the communication interface exposes Eco's storage GUI. */
    @Override
    public boolean supportsStorageInterfaceUi() {
        return getBlockState().is(ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get());
    }

    public SimplifyStorageInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    /**
     * Compatibility path for the preview6 runtime: when no drive node has
     * joined an AE2 grid, mount the cells through the already-connected
     * interface. Once any drive is online, the neweco8 drive-owned providers
     * take over and this provider mounts nothing.
     */
    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        SimplifyStorageCluster cluster = getCluster();
        SimplifyStorageHostBlockEntity controller = cluster == null ? null : cluster.getController();
        if (controller == null || cluster == null || isDriveProviderOnThisGrid(cluster)) {
            return;
        }
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            IECOStorageCell cell = drive.getCellInventory();
            ItemStack cellStack = drive.getCellStack();
            if (cell == null || cellStack == null || cellStack.isEmpty()
                    || controller.getTier().compareTo(cell.getTier()) < 0) {
                drive.setMounted(false);
                continue;
            }
            storageMounts.mount(cell, controller.getStoragePriority());
            drive.setMounted(true);
        }
    }

    /**
     * The preview6 node graph can leave drive providers on a different grid
     * from the external interface. Only skip the compatibility path when a
     * drive provider is actually mounted by this interface's grid.
     */
    private boolean isDriveProviderOnThisGrid(SimplifyStorageCluster cluster) {
        return SimplifyGridFacade.isConnected(getMainNode()) && cluster.getDrives().stream()
                .anyMatch(drive -> drive.getMainNode().isOnline()
                        && SimplifyGridFacade.isOnSameGrid(getMainNode(), drive.getMainNode()));
    }

    /**
     * The formed interface is the external AE2 attachment point. Keep this
     * explicit so older eco runtime jars do not restrict it to adjacent
     * {@code NEBlockEntity} members and reject a normal AE2 cable.
     */
    @Override
    public Set<Direction> getGridConnectableSides(BlockOrientation orientation) {
        return isFormed() && !isServerStopping()
                ? EnumSet.allOf(Direction.class)
                : EnumSet.noneOf(Direction.class);
    }

    private void refreshExternalGridSides() {
        if (isServerStopping()) {
            return;
        }
        getMainNode().setExposedOnSides(getGridConnectableSides(getOrientation()));
    }

    @Override
    public void onReady() {
        getMainNode()
                .addService(IStorageProvider.class, this)
                .addService(IAEPowerStorage.class, this);
        super.onReady();
        refreshExternalGridSides();
        requestStorageProviderUpdate();
    }

    @Override
    public void updateCluster(SimplifyStorageCluster nextCluster) {
        boolean wasPowerBridgeAvailable = isPowerBridgeAvailable();
        super.updateCluster(nextCluster);
        refreshExternalGridSides();
        requestStorageProviderUpdate();
        if (!wasPowerBridgeAvailable && isPowerBridgeAvailable()) {
            publishPowerBridgeState();
        }
    }

    private void requestStorageProviderUpdate() {
        if (level != null && !level.isClientSide && !isServerStopping()) {
            SimplifyGridFacade.requestStorageUpdate(getMainNode());
        }
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (isServerStopping()) {
            return;
        }
        super.onMainNodeStateChanged(reason);
        // The drive providers are refreshed by their own nodes; this interface only
        // announces the compatibility power bridge after joining an ME grid.
        if (level != null && !level.isClientSide
                && isFormed() && getCluster() != null
                && getMainNode().getNode() != null && getMainNode().isOnline()
                && SimplifyGridFacade.isConnected(getMainNode())) {
            requestStorageProviderUpdate();
            publishPowerBridgeState();
            logBridgeStateIfChanged();
        }
    }

    private boolean isPowerBridgeAvailable() {
        return isFormed() && !bridgedCells().isEmpty();
    }

    /**
     * EnergyService registers IAEPowerStorage services from state-change events.
     * The bridge's access restriction changes when the multiblock forms, so it
     * must announce both directions after joining an already-running grid.
     */
    private void publishPowerBridgeState() {
        if (level == null || level.isClientSide || isServerStopping()
                || !getMainNode().isOnline()) {
            return;
        }
        if (!SimplifyGridFacade.isConnected(getMainNode()) || !isPowerBridgeAvailable()) {
            return;
        }
        SimplifyGridFacade.postProvidePowerEvent(getMainNode(), this);
        SimplifyGridFacade.postReceivePowerEvent(getMainNode(), this);
        logBridgeStateIfChanged();
    }

    private void logBridgeStateIfChanged() {
        if (level == null || level.isClientSide || getMainNode().getNode() == null) {
            return;
        }
        String state = isPowerBridgeAvailable()
                + "|" + bridgedCells().size()
                + "|" + getAEMaxPower()
                + "|" + getGridConnectableSides(getOrientation())
                + "|" + getMainNode().getNode().getConnectedSides();
        if (state.equals(lastLoggedBridgeState)) {
            return;
        }
        lastLoggedBridgeState = state;
        if (isPowerBridgeAvailable()) {
            LOGGER.info("L1 power bridge online @{}: cells={}, capacity={}, connected={}",
                    worldPosition, bridgedCells().size(), getAEMaxPower(),
                    getMainNode().getNode().getConnectedSides());
        } else {
            LOGGER.info("L1 power bridge offline @{}", worldPosition);
        }
    }

    // ------------------------------------------------------------------
    // Power bridge: expose the cluster's energy cells to the outer ME grid.
    // ------------------------------------------------------------------

    public List<SimplifyEnergyCellBlockEntity> bridgedCells() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null || cluster.getController() == null) {
            return List.of();
        }
        return cluster.getEnergyCells();
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return isFormed() && !bridgedCells().isEmpty()
                ? AccessRestriction.READ_WRITE
                : AccessRestriction.NO_ACCESS;
    }

    @Override
    public double getAEMaxPower() {
        double max = 0;
        for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
            max += cell.getAEMaxPower();
        }
        return max;
    }

    @Override
    public double getAECurrentPower() {
        double current = 0;
        for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
            current += cell.getAECurrentPower();
        }
        return current;
    }

    @Override
    public double injectAEPower(double amt, Actionable mode) {
        double remaining = amt;
        for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
            if (remaining <= 0) {
                break;
            }
            remaining = cell.injectAEPower(remaining, mode);
        }
        double inserted = amt - remaining;
        if (mode == Actionable.MODULATE && inserted > 0) {
            for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
                cell.syncDisplayLevelNow();
            }
        }
        return remaining;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        double toExtract = pm.multiply(amt);
        double extracted = 0;
        for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
            if (toExtract <= 0) {
                break;
            }
            double fromCell = cell.extractAEPower(toExtract, mode, PowerMultiplier.ONE);
            toExtract -= fromCell;
            extracted += fromCell;
        }
        double real = pm.divide(extracted);
        if (mode == Actionable.MODULATE && real > 0) {
            for (SimplifyEnergyCellBlockEntity cell : bridgedCells()) {
                cell.syncDisplayLevelNow();
            }
        }
        return real;
    }
}

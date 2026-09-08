package cn.dancingsnow.neoecoprototype.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyEnergyCellBlock;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.blockentity.powersink.IExternalPowerSink;
import appeng.me.energy.StoredEnergyAmount;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * L1 high-density energy cell: 100 000 AE buffer, participates in the cluster's
 * ME grid as {@link IAEPowerStorage} + {@link IGridTickable}. Behaviour mirrors
 * eco's {@code ECOEnergyCellBlockEntity} with the L1 tier numbers.
 */
public class SimplifyEnergyCellBlockEntity extends NEBlockEntity<SimplifyStorageCluster, SimplifyEnergyCellBlockEntity>
        implements IExternalPowerSink, IGridTickable {

    @Persisted
    private final StoredEnergyAmount energyStored;
    private boolean neighborChangePending;
    /** Cached displayed block-state level; only diffs trigger a {@code setBlockAndUpdate}. */
    private byte currentDisplayLevel;

    public SimplifyEnergyCellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
        this.energyStored = new StoredEnergyAmount(0, SimplifyTier.L1.getPowerStorageSize(), this::emitPowerEvent);
    }

    private void emitPowerEvent(GridPowerStorageStateChanged.PowerEventType type) {
        if (isServerStopping()) {
            return;
        }
        SimplifyGridFacade.postPowerStorageEvent(getMainNode(), this, type);
    }

    @Override
    public double injectAEPower(double amt, Actionable mode) {
        double inserted = this.energyStored.insert(amt, mode == Actionable.MODULATE);
        if (mode == Actionable.MODULATE && inserted > 0) {
            this.onEnergyChanged();
        }
        return amt - inserted;
    }

    @Override
    public double extractAEPower(double amt, Actionable mode, PowerMultiplier pm) {
        double extracted = pm.divide(this.extractAEPower(pm.multiply(amt), mode));
        if (mode == Actionable.MODULATE && extracted > 0) {
            this.onEnergyChanged();
        }
        return extracted;
    }

    private double extractAEPower(double amt, Actionable mode) {
        return this.energyStored.extract(amt, mode == Actionable.MODULATE);
    }

    @Override
    public double getAEMaxPower() {
        return this.energyStored.getMaximum();
    }

    @Override
    public double getAECurrentPower() {
        return this.energyStored.getAmount();
    }

    @Override
    public boolean isAEPublicPowerStorage() {
        return true;
    }

    @Override
    public AccessRestriction getPowerFlow() {
        return isFormed() ? AccessRestriction.READ_WRITE : AccessRestriction.NO_ACCESS;
    }

    @Override
    public double injectExternalPower(PowerUnit externalUnit, double amount, Actionable mode) {
        return PowerUnit.AE.convertTo(externalUnit, injectAEPower(PowerUnit.AE.convertTo(externalUnit, amount), mode));
    }

    @Override
    public double getExternalPowerDemand(PowerUnit externalUnit, double maxPowerRequired) {
        return PowerUnit.AE.convertTo(externalUnit, Math.max(0.0, getAEMaxPower() - getAECurrentPower()));
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 20, !neighborChangePending);
    }

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        if (isServerStopping()) {
            return TickRateModulation.SLEEP;
        }
        if (neighborChangePending) {
            neighborChangePending = false;
            setChanged();
            updateStateForPowerLevel();
        }
        return TickRateModulation.SLEEP;
    }

    /** 0..4 display bucket, mirroring eco's energy cell block. */
    public static int getStorageLevelFromFillFactor(double fillFactor) {
        return (int) Math.floor(4 * Math.clamp(fillFactor + 0.01, 0, 1));
    }

    /** Push the current charge level into the {@code level} block-state property. */
    private void updateStateForPowerLevel() {
        if (isRemoved() || isServerStopping() || level == null) {
            return;
        }
        int storageLevel = getStorageLevelFromFillFactor(
                energyStored.getAmount() / energyStored.getMaximum());
        if (currentDisplayLevel != storageLevel) {
            currentDisplayLevel = (byte) storageLevel;
            level.setBlockAndUpdate(
                    worldPosition,
                    level.getBlockState(worldPosition).setValue(SimplifyEnergyCellBlock.LEVEL, storageLevel));
        }
    }

    /**
     * Server-side: refresh the displayed {@code level} right now.
     * <p>Normally eco's energy cell waits for its own grid tick
     * ({@link #tickingRequest}); under "plan 1" this cell's node is NOT part of
     * a ticking ME grid, so the storage interface (the only grid member) calls
     * this directly whenever it pushed/pulled power through the power bridge.
     */
    public void syncDisplayLevelNow() {
        updateStateForPowerLevel();
    }

    private void onEnergyChanged() {
        if (isServerStopping()) {
            return;
        }
        setChanged();
        if (!neighborChangePending) {
            neighborChangePending = true;
            SimplifyGridFacade.alertDevice(getMainNode());
        }
    }

    @Override
    public void onReady() {
        getMainNode()
                .addService(IAEPowerStorage.class, this)
                .addService(IGridTickable.class, this);
        super.onReady();
        // Sync the display level with the stored energy once, mirroring eco's
        // ECOEnergyCellBlockEntity#onReady.
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(SimplifyEnergyCellBlock.LEVEL)) {
                currentDisplayLevel = (byte) (int) state.getValue(SimplifyEnergyCellBlock.LEVEL);
            }
        }
        updateStateForPowerLevel();
        getMainNode().setIdlePowerUsage(0);
    }
}

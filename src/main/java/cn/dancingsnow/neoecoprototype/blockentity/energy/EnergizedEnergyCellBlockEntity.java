package cn.dancingsnow.neoecoprototype.blockentity.energy;

import appeng.api.config.Actionable;
import appeng.api.ids.AEComponents;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.util.SettingsFrom;
import cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The battery behind {@link EnergizedEnergyCellBlock}. The parent already publishes this as an
 * {@code IAEPowerStorage} grid node and persists the stored amount, so the only thing left is the size of
 * the buffer - which comes off the block, because the two tiers share one block entity type.
 */
public class EnergizedEnergyCellBlockEntity extends AENetworkedPoweredBlockEntity {

    /** The bucket last written into the block state; -1 forces the first tick to publish one. */
    private byte currentDisplayLevel = -1;

    public EnergizedEnergyCellBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        // A battery idles; it is not a machine. Same call AE2's own energy acceptor makes.
        getMainNode().setIdlePowerUsage(0.0);
        if (state.getBlock() instanceof EnergizedEnergyCellBlock cell) {
            setInternalMaxPower(cell.getCapacity());
        }
        // Default is false in AEBasePoweredBlockEntity, and a storage nobody may read is not a battery.
        setInternalPublicPowerStorage(true);
    }

    /**
     * The charge bar TedXenon drew as five faces per tier. Ten ticks is the cheapest cadence that still
     * reads as live while a battery fills, and the write only happens when the bucket actually moves.
     */
    public static void tick(Level level, BlockPos pos, BlockState state, EnergizedEnergyCellBlockEntity battery) {
        if (level.getGameTime() % 10 == 0L) {
            battery.updateStateForPowerLevel();
        }
    }

    /** 0..4 display bucket, the same arithmetic {@code SimplifyEnergyCellBlockEntity} uses for eco's cell. */
    public static int getStorageLevelFromFillFactor(double fillFactor) {
        return (int) Math.floor(4 * Math.clamp(fillFactor + 0.01, 0, 1));
    }

    /** Push the current charge bucket into the {@code level} property, which is what picks the face. */
    private void updateStateForPowerLevel() {
        if (isRemoved() || level == null || level.isClientSide()) {
            return;
        }
        double maximum = getAEMaxPower();
        if (maximum <= 0.0) {
            return;
        }
        int storageLevel = getStorageLevelFromFillFactor(getAECurrentPower() / maximum);
        if (currentDisplayLevel != storageLevel) {
            currentDisplayLevel = (byte) storageLevel;
            level.setBlockAndUpdate(worldPosition, level.getBlockState(worldPosition)
                    .setValue(cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock.LEVEL,
                            storageLevel));
        }
    }

    @Override
    public void onReady() {
        super.onReady();
        if (level != null) {
            BlockState state = level.getBlockState(worldPosition);
            if (state.hasProperty(cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock.LEVEL)) {
                currentDisplayLevel = (byte) (int) state.getValue(
                        cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock.LEVEL);
            }
        }
        updateStateForPowerLevel();
    }

    /** A battery holds charge, not items - the same answer AE2's own energy acceptor gives. */
    @Override
    public appeng.api.inventories.InternalInventory getInternalInventory() {
        return appeng.api.inventories.InternalInventory.empty();
    }

    /**
     * AE2's energy service drops a storage out of its provider set when a draw leaves it dry, and out of its
     * requester set when an injection fills it; a storage only returns through this event. The parent's hook
     * is empty because its machine subclasses are not public storage, so without this override a battery
     * recharges exactly until its first full drain and then sits dead in the grid forever.
     */
    @Override
    protected void emitPowerStateEvent(GridPowerStorageStateChanged.PowerEventType type) {
        getMainNode().ifPresent((grid, node) -> grid.postEvent(new GridPowerStorageStateChanged(this, type)));
    }

    /**
     * Carry the charge into the dropped item, the way AE2's own cell does. The parent class does not do
     * this: only {@code EnergyCellBlockEntity} writes {@code STORED_ENERGY}, and that component is both
     * what the item tooltip reads and what a re-placed battery starts from.
     */
    @Override
    public void exportSettings(SettingsFrom mode, DataComponentMap.Builder builder, Player player) {
        super.exportSettings(mode, builder, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            double stored = getAECurrentPower();
            if (stored > 0.0) {
                builder.set(AEComponents.STORED_ENERGY, stored);
            }
        }
    }

    @Override
    public void importSettings(SettingsFrom mode, DataComponentMap map, Player player) {
        super.importSettings(mode, map, player);
        if (mode == SettingsFrom.DISMANTLE_ITEM) {
            Double stored = map.get(AEComponents.STORED_ENERGY);
            if (stored != null && stored > 0.0) {
                injectAEPower(stored, Actionable.MODULATE);
            }
        }
    }
}
package cn.dancingsnow.neoecoprototype.blockentity.storage;

import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoae.util.ServerTaskUtil;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyDriveBlock;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import appeng.api.networking.IGridNodeListener;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * L1 drive: hosts exactly one ECO-compatible storage cell (any cell accepted by
 * eco's {@link ECOStorageCells}, which includes this addon's L1 cells).
 * <p>
 * The drive follows eco's current storage contract: it is an
 * {@link IStorageProvider} and mounts its own cell through its multiblock grid
 * node once the cluster is formed.
 */
public class SimplifyDriveBlockEntity extends NEBlockEntity<SimplifyStorageCluster, SimplifyDriveBlockEntity>
        implements ISyncPersistRPCBlockEntity, ISaveProvider, IStorageProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyDriveBlockEntity.class);
    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @DescSynced
    @Persisted
    @RequireRerender
    private ItemStack cellStack = ItemStack.EMPTY;
    @Nullable
    private ItemStack cachedCellStack;
    @Nullable
    private IECOStorageCell cachedCellInventory;

    @DescSynced
    private boolean online;
    @DescSynced
    private boolean mounted;
    @DescSynced
    private CellState cellState = CellState.ABSENT;

    public SimplifyDriveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    /** Mark whether the cluster's storage interface currently mounts our cell. */
    public void setMounted(boolean mounted) {
        if (this.mounted != mounted) {
            this.mounted = mounted;
            setChanged();
            markForUpdate();
        }
    }

    public boolean isMounted() {
        return mounted;
    }

    public boolean isOnline() {
        return online;
    }

    public CellState getCellState() {
        return cellState;
    }

    public boolean isItemValid(ItemStack stack) {
        return ECOStorageCells.isCellHandled(stack);
    }

    @Nullable
    public ItemStack getCellStack() {
        return cellStack.isEmpty() ? null : cellStack;
    }

    public boolean hasCell() {
        return !cellStack.isEmpty();
    }

    /** @return true when the cell was inserted. */
    public boolean insertCell(ItemStack stack) {
        if (!cellStack.isEmpty() || !isItemValid(stack)) {
            return false;
        }
        invalidateCellInventoryCache();
        this.cellStack = stack.copyWithCount(1);
        updateCellState();
        syncCellBlockState();
        updateIdlePower();
        notifyClusterStorageChanged();
        setChanged();
        markForUpdate();
        return true;
    }

    /** @return the removed cell, or {@link ItemStack#EMPTY} when empty / locked. */
    public ItemStack removeCell() {
        if (cellStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ECOStorageCells.releaseCellInventory(cellStack, this);
        ItemStack removed = cellStack;
        this.cellStack = ItemStack.EMPTY;
        invalidateCellInventoryCache();
        updateCellState();
        syncCellBlockState();
        updateIdlePower();
        notifyClusterStorageChanged();
        setChanged();
        markForUpdate();
        return removed;
    }

    /** Ask this drive's provider node to remount its cell. */
    private void notifyClusterStorageChanged() {
        if (level != null && !level.isClientSide && !isServerStopping()) {
            requestProviderUpdate();
            SimplifyStorageCluster cluster = getCluster();
            if (cluster != null && cluster.getTheInterface() != null) {
                SimplifyGridFacade.requestStorageUpdate(cluster.getTheInterface().getMainNode());
            }
        }
    }

    private void requestProviderUpdate() {
        SimplifyGridFacade.requestStorageUpdate(getMainNode());
    }

    @Nullable
    public IECOStorageCell getCellInventory() {
        if (cellStack.isEmpty()) {
            invalidateCellInventoryCache();
            return null;
        }
        if (cachedCellStack != cellStack) {
            cachedCellStack = cellStack;
            cachedCellInventory = ECOStorageCells.getCellInventory(cellStack, this);
        }
        return cachedCellInventory;
    }

    private void invalidateCellInventoryCache() {
        cachedCellStack = null;
        cachedCellInventory = null;
    }

    @Override
    public void onChunkUnloaded() {
        ECOStorageCells.releaseCellInventory(cellStack, this);
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        ECOStorageCells.releaseCellInventory(cellStack, this);
        super.setRemoved();
    }

    private void syncCellBlockState() {
        if (level == null || level.isClientSide || isServerStopping()) {
            return;
        }
        BlockState state = getBlockState();
        BlockState newState = state.setValue(SimplifyDriveBlock.HAS_CELL, hasCell());
        if (newState != state) {
            level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
        }
    }

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        SimplifyStorageCluster cluster = getCluster();
        SimplifyStorageHostBlockEntity controller = cluster == null ? null : cluster.getController();
        IECOStorageCell cell = getCellInventory();
        if (controller != null && cell != null
                && controller.getTier().compareTo(cell.getTier()) >= 0) {
            storageMounts.mount(cell, controller.getStoragePriority());
            setMounted(true);
            updateCellState();
            return;
        }
        setMounted(false);
        updateCellState();
    }

    @Override
    public void onReady() {
        getMainNode().addService(IStorageProvider.class, this);
        super.onReady();
        online = getMainNode().isOnline();
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (isServerStopping()) {
            return;
        }
        super.onMainNodeStateChanged(reason);
        boolean nextOnline = getMainNode().isOnline();
        if (online != nextOnline) {
            online = nextOnline;
            setChanged();
            markForUpdate();
        }
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null && cluster.getTheInterface() != null) {
            SimplifyGridFacade.requestStorageUpdate(cluster.getTheInterface().getMainNode());
        }
    }

    private void updateIdlePower() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;
        double power = profile.storageDriveIdlePower();
        if (isFormed() && getCluster() != null && getCluster().getController() != null) {
            IECOStorageCell cell = getCellInventory();
            if (cell != null
                    && getCluster().getController().getTier().compareTo(cell.getTier()) >= 0) {
                power += profile.scale(cell.getIdleDrain());
            }
        }
        getMainNode().setIdlePowerUsage(power);
    }

    /**
     * Spill the inserted cell when the drive is broken / removed, exactly like
     * eco's own drive ({@code ECODriveBlockEntity#addAdditionalDrops}). AE2's
     * base block invokes this hook on removal and pops the returned stacks, so
     * the cell (whose data lives on the ItemStack itself) survives intact.
     * <p>NOTE: do NOT touch the level / block state in here - the block is
     * mid-removal; placing a block state (e.g. clearing HAS_CELL) would
     * resurrect the drive at this position and break its own loot drop.
     */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (!cellStack.isEmpty()) {
            ECOStorageCells.releaseCellInventory(cellStack, this);
            drops.add(cellStack);
            // Clear so a second invocation of this hook cannot drop it twice.
            cellStack = ItemStack.EMPTY;
            invalidateCellInventoryCache();
        }
    }

    private void updateCellState() {
        if (level == null || level.isClientSide) {
            return;
        }
        IECOStorageCell cellInventory = getCellInventory();
        CellState nextState = cellInventory == null ? CellState.ABSENT : cellInventory.getStatus();
        if (cellState != nextState) {
            cellState = nextState;
            markForUpdate();
        }
    }

    @Override
    public void saveChanges() {
        if (cachedCellInventory != null) {
            cachedCellInventory.persist();
        }
        notifyPersistence();
    }

    public void notifyPersistence() {
        if (level instanceof ServerLevel serverLevel) {
            ServerTaskUtil.executeIfServerRunning(serverLevel, () -> {
                updateCellState();
                setChanged();
                markForUpdate();
            });
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        data.put("cell", cellStack.saveOptional(registries));
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        invalidateCellInventoryCache();
        if (data.contains("cell")) {
            CompoundTag cellTag = data.getCompound("cell");
            cellStack = cellTag.isEmpty()
                    ? ItemStack.EMPTY
                    : ItemStack.parseOptional(registries, cellTag);
        }
        updateCellState();
    }
}

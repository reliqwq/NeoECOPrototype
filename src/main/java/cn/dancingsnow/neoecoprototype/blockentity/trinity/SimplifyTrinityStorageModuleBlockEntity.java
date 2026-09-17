package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import appeng.api.networking.IGridNodeListener;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.annotation.RequireRerender;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * Trinity storage module: holds one ECO-compatible cell and mounts it into the ME network.
 *
 * <p>Unlike the L1 drive this module is a member of the Trinity cluster, so it mounts its cell
 * through its own grid node as soon as the Trinity structure is formed -- no separate storage
 * host or interface is involved.
 */
public class SimplifyTrinityStorageModuleBlockEntity
        extends SimplifyTrinityModuleBlockEntity<SimplifyTrinityStorageModuleBlockEntity>
        implements ISyncPersistRPCBlockEntity, ISaveProvider, IStorageProvider {

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
    private boolean mounted;
    @DescSynced
    private CellState cellState = CellState.ABSENT;

    public SimplifyTrinityStorageModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    public boolean isMounted() {
        return mounted;
    }

    public CellState getCellState() {
        return cellState;
    }

    public boolean isItemValid(ItemStack stack) {
        return ECOStorageCells.isCellHandled(stack);
    }

    public boolean hasCell() {
        return !cellStack.isEmpty();
    }

    @Nullable
    public ItemStack getCellStack() {
        return cellStack.isEmpty() ? null : cellStack;
    }

    /** @return true when the cell was inserted. */
    public boolean insertCell(ItemStack stack) {
        if (!cellStack.isEmpty() || !isItemValid(stack)) {
            return false;
        }
        invalidateCellInventoryCache();
        this.cellStack = stack.copyWithCount(1);
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
        setChanged();
        markForUpdate();
        return true;
    }

    /** @return the removed cell, or {@link ItemStack#EMPTY} when empty. */
    public ItemStack removeCell() {
        if (cellStack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        ECOStorageCells.releaseCellInventory(cellStack, this);
        ItemStack removed = cellStack;
        this.cellStack = ItemStack.EMPTY;
        invalidateCellInventoryCache();
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
        setChanged();
        markForUpdate();
        return removed;
    }

    private void requestProviderUpdate() {
        if (level != null && !level.isClientSide && !isServerStopping()) {
            SimplifyGridFacade.requestStorageUpdate(getMainNode());
        }
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

    @Override
    public void mountInventories(IStorageMounts storageMounts) {
        IECOStorageCell cell = getCellInventory();
        if (getCluster() != null && cell != null) {
            storageMounts.mount(cell, 0);
            setMounted(true);
            updateCellState();
            return;
        }
        setMounted(false);
        updateCellState();
    }

    private void setMounted(boolean next) {
        if (mounted != next) {
            mounted = next;
            setChanged();
            markForUpdate();
        }
    }

    @Override
    public void onReady() {
        getMainNode().addService(IStorageProvider.class, this);
        super.onReady();
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
        updateCellState();
        updateIdlePower();
        requestProviderUpdate();
    }

    private void updateIdlePower() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;
        double power = profile.storageDriveIdlePower();
        if (getCluster() != null) {
            IECOStorageCell cell = getCellInventory();
            if (cell != null) {
                power += profile.scale(cell.getIdleDrain());
            }
        }
        getMainNode().setIdlePowerUsage(power);
    }

    /** Spill the inserted cell when the module is broken, matching the L1 drive. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (!cellStack.isEmpty()) {
            ECOStorageCells.releaseCellInventory(cellStack, this);
            drops.add(cellStack);
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
            cellStack = cellTag.isEmpty() ? ItemStack.EMPTY : ItemStack.parseOptional(registries, cellTag);
        }
        updateCellState();
    }
}

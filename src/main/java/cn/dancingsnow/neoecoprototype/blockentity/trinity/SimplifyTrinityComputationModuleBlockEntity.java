package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import cn.dancingsnow.neoecoae.items.ECOComputationCellItem;
import cn.dancingsnow.neoecoae.util.ICellHost;
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
 * Trinity configuration module: hosts one eco computation cell for display/configuration.
 *
 * <p>The cell tier is shown as Trinity module capacity only. This module is not an eco
 * computation cluster, does not register an {@code ICraftingCPU}, and does not contribute CPU
 * capacity to the network.
 */
public class SimplifyTrinityComputationModuleBlockEntity
        extends SimplifyTrinityModuleBlockEntity<SimplifyTrinityComputationModuleBlockEntity>
        implements ISyncPersistRPCBlockEntity, ICellHost {

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @DescSynced
    @Persisted
    @RequireRerender
    @Nullable
    private ItemStack cellStack = null;

    public SimplifyTrinityComputationModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    /** @return true when the computation cell was accepted. */
    public boolean insertCell(ItemStack itemStack) {
        if (hasCell() || !isItemValid(itemStack)) {
            return false;
        }
        setCellStack(itemStack);
        return true;
    }

    @Override
    public void setCellStack(ItemStack itemStack) {
        this.cellStack = itemStack.isEmpty() ? null : itemStack.copyWithCount(1);
        setChanged();
        markForUpdate();
        if (getCluster() != null) {
            getCluster().refreshServices();
        }
    }

    @Override
    public ItemStack getCellStack() {
        return cellStack == null ? ItemStack.EMPTY : cellStack;
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return !stack.isEmpty() && stack.getItem() instanceof ECOComputationCellItem;
    }

    public boolean hasCell() {
        return cellStack != null && !cellStack.isEmpty();
    }

    /** Trinity configuration capacity shown by the hosted cell, or 0 when the slot is empty. */
    public long getComputationConfigurationCapacity() {
        ItemStack stack = getCellStack();
        if (stack.isEmpty() || !(stack.getItem() instanceof ECOComputationCellItem cellItem)) {
            return 0L;
        }
        return cellItem.getTier().getCPUTotalBytes();
    }

    /** @deprecated Use {@link #getComputationConfigurationCapacity()}; this is not network CPU capacity. */
    @Deprecated
    public long getComputationBytes() {
        return getComputationConfigurationCapacity();
    }

    @Override
    public void notifyPersistence() {
        setChanged();
        markForUpdate();
    }

    /** Spill the hosted cell when the module is broken. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (hasCell()) {
            drops.add(getCellStack());
            cellStack = null;
        }
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        if (hasCell()) {
            data.put("cell", getCellStack().saveOptional(registries));
        }
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        if (data.contains("cell")) {
            CompoundTag cellTag = data.getCompound("cell");
            cellStack = cellTag.isEmpty() ? null : ItemStack.parseOptional(registries, cellTag);
        }
    }
}

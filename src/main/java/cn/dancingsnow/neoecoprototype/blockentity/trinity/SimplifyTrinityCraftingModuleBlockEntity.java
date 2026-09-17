package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.inventories.InternalInventory;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.stacks.AEItemKey;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;

/**
 * Trinity crafting module: a real AE2 pattern provider.
 *
 * <p>eco's own crafting pipeline cannot be reused here -- {@code ECOCraftingCPU} and
 * {@code ECOCraftingCPULogic} are bound to {@code NEComputationCluster} and
 * {@code ECOComputationThreadingCoreBlockEntity} by their constructors, and re-implementing the
 * CPU lifecycle plus dispatcher would mean rewriting several thousand lines. Instead this module
 * delegates to AE2's own {@link PatternProviderLogic}, so it stores encoded patterns, exposes them
 * to whatever crafting CPU the ME network provides (AE2's own or eco's computation machine), and
 * pushes ingredients to its neighbours exactly like a stock pattern provider.
 */
public class SimplifyTrinityCraftingModuleBlockEntity
        extends SimplifyTrinityModuleBlockEntity<SimplifyTrinityCraftingModuleBlockEntity>
        implements ISyncPersistRPCBlockEntity, PatternProviderLogicHost {

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);
    @Nullable
    private PatternProviderLogic logic;
    /**
     * Machines fed by this module return their product here; see
     * {@link TrinityReturnItemHandler}. Exposed as a block capability in
     * {@code NeoECOPrototype#registerCapabilities}.
     */
    private final TrinityReturnItemHandler returnHandler = new TrinityReturnItemHandler(this);

    public SimplifyTrinityCraftingModuleBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public TrinityReturnItemHandler getReturnHandler() {
        return returnHandler;
    }

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    private PatternProviderLogic logic() {
        if (logic == null) {
            logic = new PatternProviderLogic(getMainNode(), this);
        }
        return logic;
    }

    @Override
    public PatternProviderLogic getLogic() {
        return logic();
    }

    @Override
    public BlockEntity getBlockEntity() {
        return this;
    }

    /** Ingredient push targets: every face, matching a stock pattern provider's default. */
    @Override
    public EnumSet<Direction> getTargets() {
        return EnumSet.allOf(Direction.class);
    }

    @Override
    public AEItemKey getTerminalIcon() {
        return AEItemKey.of(ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get());
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return new ItemStack(ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get());
    }

    @Override
    public InternalInventory getTerminalPatternInventory() {
        return logic().getPatternInv();
    }

    @Override
    public PatternContainerGroup getTerminalGroup() {
        return logic().getTerminalGroup();
    }

    /** Returns whether an adjacent AE2 crafting machine can accept this provider's plans. */
    public boolean hasAdjacentCraftingMachine() {
        Level currentLevel = getLevel();
        if (currentLevel == null) {
            return false;
        }
        for (Direction direction : getTargets()) {
            if (ICraftingMachine.of(currentLevel, getBlockPos().relative(direction), direction.getOpposite()) != null) {
                return true;
            }
        }
        return false;
    }

    /** Exposed for tests and diagnostics; rebuilds AE2's decoded-pattern cache when it is stale. */
    public List<IPatternDetails> getAvailablePatterns() {
        PatternProviderLogic providerLogic = logic();
        if (providerLogic.getAvailablePatterns().isEmpty() && !providerLogic.getPatternInv().isEmpty()) {
            providerLogic.updatePatterns();
        }
        return providerLogic.getAvailablePatterns();
    }

    @Override
    public void onReady() {
        getMainNode().addService(ICraftingProvider.class, logic());
        super.onReady();
        // AE2's own pattern provider rebuilds its decoded-pattern cache in onReady(). Without this
        // the cache stays empty after a world reload even though the pattern inventory was restored.
        logic().updatePatterns();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (isServerStopping()) {
            return;
        }
        super.onMainNodeStateChanged(reason);
        logic().onMainNodeStateChanged();
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        logic().writeToNBT(data, registries);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        logic().readFromNBT(data, registries);
        // readFromNBT only restores the inventory; the decoded cache must be rebuilt explicitly.
        logic().updatePatterns();
    }

    /** Spill the stored patterns when the module is broken, like a stock pattern provider. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        logic().addDrops(drops);
    }
}

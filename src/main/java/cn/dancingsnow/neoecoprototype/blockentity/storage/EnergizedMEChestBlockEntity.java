package cn.dancingsnow.neoecoprototype.blockentity.storage;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.Settings;
import appeng.api.config.SortDir;
import appeng.api.config.SortOrder;
import appeng.api.config.ViewItems;
import appeng.api.networking.GridFlags;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNodeListener;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.events.GridPowerStorageStateChanged;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import appeng.api.storage.ILinkStatus;
import appeng.api.storage.ITerminalHost;
import appeng.api.storage.MEStorage;
import appeng.api.storage.StorageCells;
import appeng.api.storage.SupplierStorage;
import appeng.api.storage.cells.CellState;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.storage.cells.StorageCell;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.util.IConfigManager;
import appeng.blockentity.ServerTickingBlockEntity;
import appeng.blockentity.grid.AENetworkedPoweredBlockEntity;
import appeng.helpers.IPriorityHost;
import appeng.menu.ISubMenu;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuLocators;
import appeng.menu.me.items.BasicCellChestMenu;
import appeng.util.inv.AppEngInternalInventory;
import cn.dancingsnow.neoecoae.all.NEItems;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.api.storage.IECOConfigurableCellHost;
import cn.dancingsnow.neoecoae.api.storage.IECOCellWorkbenchDrive;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCellItem;
import cn.dancingsnow.neoecoprototype.block.storage.EnergizedMEChestBlock;
import cn.dancingsnow.neoecoprototype.menu.EnergizedMEChestMenu;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.util.StringRepresentable;

import java.util.EnumSet;
import java.util.List;
import java.util.UUID;
import javax.annotation.Nullable;

public class EnergizedMEChestBlockEntity extends AENetworkedPoweredBlockEntity
        implements IStorageProvider, ITerminalHost, IPriorityHost, ISaveProvider, ServerTickingBlockEntity,
        IECOConfigurableCellHost, IECOCellWorkbenchDrive {
    public enum DisplayState implements StringRepresentable {
        OFF,
        ABSENT,
        EMPTY,
        PARTIAL,
        TYPES_FULL,
        FULL;

        @Override
        public String getSerializedName() {
            return name().toLowerCase();
        }
    }

    private static final double BASE_IDLE_POWER = 1.0;
    private static final double MAX_POWER = 5000.0;

    private final AppEngInternalInventory cellInventory = new AppEngInternalInventory(this, 1);
    private final IConfigManager config = IConfigManager.builder(this::saveChanges)
            .registerSetting(Settings.SORT_BY, SortOrder.NAME)
            .registerSetting(Settings.VIEW_MODE, ViewItems.ALL)
            .registerSetting(Settings.SORT_DIRECTION, SortDir.ASCENDING)
            .build();

    private ItemStack cachedCellStack = ItemStack.EMPTY;
    private StorageCell currentCell;
    private boolean currentCellFromEco;
    private UUID cellInteractionId = UUID.randomUUID();
    private double idlePowerUsage = BASE_IDLE_POWER;
    private boolean gridNodeInitialized;
    private int priority;
    private DisplayState lastDisplayState = DisplayState.OFF;
    private boolean dropCellOnBreak = true;

    public EnergizedMEChestBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        setInternalMaxPower(MAX_POWER * PowerMultiplier.CONFIG.multiply(1.0));
        setInternalPublicPowerStorage(true);
        setInternalPowerFlow(AccessRestriction.WRITE);
        setPowerSides(EnumSet.allOf(Direction.class));
        cellInventory.setMaxStackSize(0, 1);
        cellInventory.setFilter(new appeng.util.inv.filter.IAEItemFilter() {
            @Override
            public boolean allowInsert(appeng.api.inventories.InternalInventory inventory, int slot, ItemStack stack) {
                return acceptsCell(stack);
            }
        });
        getMainNode().addService(IStorageProvider.class, this);
        getMainNode().setFlags(GridFlags.REQUIRE_CHANNEL);
        updateIdlePowerUsage(BASE_IDLE_POWER);
    }

    public static boolean isCellValid(ItemStack stack) {
        return isAe2Cell(stack) || isEcoCellAtOrBelow(stack, 0);
    }

    public boolean acceptsCell(ItemStack stack) {
        if (getBlockState().is(ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get())) {
            return isAe2Cell(stack) || isEcoCellAtOrBelow(stack, 1)
                    || stack.is(NEItems.ECO_INFINITE_ITEM_CELL.get());
        }
        return isCellValid(stack);
    }

    private static boolean isAe2Cell(ItemStack stack) {
        return !stack.isEmpty() && StorageCells.isCellHandled(stack);
    }

    private static boolean isEcoCellAtOrBelow(ItemStack stack, int tier) {
        return !stack.isEmpty() && stack.getItem() instanceof IECOStorageCellItem cell
                && cell.getTier().getTier() <= tier;
    }

    @Override
    public appeng.api.inventories.InternalInventory getInternalInventory() {
        return cellInventory;
    }

    public AppEngInternalInventory getCellSlotInventory() {
        return cellInventory;
    }

    public ItemStack getCell() {
        return cellInventory.getStackInSlot(0);
    }

    public StorageCell getCurrentCell() {
        updateHandler();
        return currentCell;
    }

    @Override
    public IECOStorageCell getCellInventory() {
        updateHandler();
        return currentCellFromEco && currentCell instanceof IECOStorageCell ecoCell ? ecoCell : null;
    }

    @Override
    public BlockEntity getCellWorkbenchBlockEntity() {
        return this;
    }

    @Override
    public boolean isFormed() {
        return !isRemoved();
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return !isRemoved() && player.level() == level;
    }

    @Override
    public List<? extends IECOCellWorkbenchDrive> getStorageDrivesForIntegration() {
        return List.of(this);
    }

    @Override
    public BlockPos getBlockPos() {
        return worldPosition.immutable();
    }

    @Override
    public UUID getCellInteractionId() {
        return cellInteractionId;
    }

    @Override
    public boolean canExtractCell() {
        return !isRemoved();
    }

    @Override
    public int getConfiguredCellPriority() {
        return priority;
    }

    @Override
    public void setConfiguredCellPriority(int priority) {
        setPriority(priority);
    }

    @Nullable
    @Override
    public ItemStack getCellStack() {
        ItemStack stack = getCell();
        return stack.isEmpty() ? null : stack;
    }

    @Override
    public void setCellStack(ItemStack stack) {
        if (!canExtractCell()) {
            return;
        }
        if (stack == null) {
            if (getCell().isEmpty()) {
                return;
            }
        } else if (!isCellValid(stack) || stack.getCount() != 1) {
            return;
        }

        releaseCurrentCell();
        cachedCellStack = ItemStack.EMPTY;
        currentCell = null;
        currentCellFromEco = false;
        cellInteractionId = UUID.randomUUID();
        cellInventory.setItemDirect(0, stack == null ? ItemStack.EMPTY : stack);
        idlePowerUsage = BASE_IDLE_POWER;
        updateIdlePowerUsage(BASE_IDLE_POWER);
        updateHandler();
        IStorageProvider.requestUpdate(getMainNode());
        invalidateCapabilities();
        markForUpdate();
    }

    @Override
    public boolean isItemValid(ItemStack stack) {
        return isCellValid(stack);
    }

    @Override
    public void reloadCellConfiguration() {
        if (level == null || level.isClientSide || getCell().isEmpty()) {
            return;
        }
        releaseCurrentCell();
        cachedCellStack = ItemStack.EMPTY;
        currentCell = null;
        currentCellFromEco = false;
        updateHandler();
        IStorageProvider.requestUpdate(getMainNode());
        invalidateCapabilities();
        markForUpdate();
    }

    @Override
    public void returnToCellWorkbenchHost(net.minecraft.server.level.ServerPlayer player) {
        MenuOpener.open(EnergizedMEChestMenu.type(), player, MenuLocators.forBlockEntity(this));
    }

    public DisplayState getDisplayState() {
        if (isClientSide()) {
            return getBlockState().getValue(EnergizedMEChestBlock.CELL_STATE);
        }
        if (!isPowered()) {
            return DisplayState.OFF;
        }
        return displayStateOf(getCellStatus());
    }

    public CellState getCellStatus() {
        StorageCell cell = getCurrentCell();
        return cell == null ? CellState.ABSENT : cell.getStatus();
    }

    private static DisplayState displayStateOf(CellState status) {
        return switch (status) {
            case ABSENT -> DisplayState.ABSENT;
            case EMPTY -> DisplayState.EMPTY;
            case NOT_EMPTY -> DisplayState.PARTIAL;
            case TYPES_FULL -> DisplayState.TYPES_FULL;
            case FULL -> DisplayState.FULL;
        };
    }

    public boolean isPowered() {
        if (isClientSide()) {
            return getBlockState().getValue(EnergizedMEChestBlock.CELL_STATE) != DisplayState.OFF;
        }
        return getMainNode().isPowered() || getAECurrentPower() > 0.0;
    }

    public boolean openTerminal(Player player) {
        if (getCurrentCell() == null) {
            player.displayClientMessage(Component.translatable("message.neoecoprototype.energized_me_chest.no_cell"), true);
            return false;
        }
        return MenuOpener.open(BasicCellChestMenu.TYPE, player, MenuLocators.forBlockEntity(this));
    }

    public void openCellMenu(Player player) {
        MenuOpener.open(EnergizedMEChestMenu.type(), player, MenuLocators.forBlockEntity(this));
    }

    @Override
    public MEStorage getInventory() {
        return new SupplierStorage(() -> {
            IGrid grid = getMainNode().getGrid();
            if (grid != null && getMainNode().isOnline() && isPowered()) {
                return grid.getStorageService().getInventory();
            }
            return getCurrentCell();
        });
    }

    @Override
    public ILinkStatus getLinkStatus() {
        if (getCurrentCell() == null) {
            return ILinkStatus.ofDisconnected(Component.translatable("message.neoecoprototype.energized_me_chest.no_cell"));
        }
        if (!isPowered()) {
            return ILinkStatus.ofDisconnected(Component.translatable("gui.neoecoprototype.out_of_power"));
        }
        return ILinkStatus.ofConnected();
    }

    @Override
    public void mountInventories(IStorageMounts mounts) {
        if (!getMainNode().isOnline()) {
            return;
        }
        StorageCell cell = getCurrentCell();
        if (cell != null) {
            mounts.mount(cell, priority);
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public void setPriority(int priority) {
        if (this.priority != priority) {
            this.priority = priority;
            IStorageProvider.requestUpdate(getMainNode());
            saveChanges();
        }
    }

    @Override
    public IConfigManager getConfigManager() {
        return config;
    }

    @Override
    public IUpgradeInventory getUpgrades() {
        return appeng.api.upgrades.UpgradeInventories.empty();
    }

    @Override
    public ItemStack getMainMenuIcon() {
        return ModRegistration.ENERGIZED_ME_CHEST_ITEM.get().getDefaultInstance();
    }

    @Override
    public void returnToMainMenu(Player player, ISubMenu subMenu) {
        MenuOpener.returnTo(EnergizedMEChestMenu.type(), player, MenuLocators.forBlockEntity(this));
    }

    private void updateIdlePowerUsage(double usage) {
        idlePowerUsage = usage;
        if (!gridNodeInitialized || getMainNode().isReady()) {
            getMainNode().setIdlePowerUsage(usage);
        }
    }

    @Override
    public void serverTick() {
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !grid.getEnergyService().isNetworkPowered()) {
            super.extractAEPower(idlePowerUsage * PowerMultiplier.CONFIG.multiply(1.0), Actionable.MODULATE);
        }
        updateDisplay();
    }

    @Override
    protected void emitPowerStateEvent(GridPowerStorageStateChanged.PowerEventType eventType) {
        getMainNode().ifPresent(grid -> grid.postEvent(new GridPowerStorageStateChanged(this, eventType)));
        updateDisplay();
        markForUpdate();
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inventory, int slot) {
        if (inventory != cellInventory) {
            return;
        }
        releaseCurrentCell();
        cachedCellStack = ItemStack.EMPTY;
        currentCell = null;
        currentCellFromEco = false;
        cellInteractionId = UUID.randomUUID();
        idlePowerUsage = BASE_IDLE_POWER;
        updateIdlePowerUsage(BASE_IDLE_POWER);
        updateHandler();
        IStorageProvider.requestUpdate(getMainNode());
        invalidateCapabilities();
        markForUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State state) {
        super.onMainNodeStateChanged(state);
        IStorageProvider.requestUpdate(getMainNode());
        updateDisplay();
    }

    @Override
    public void saveChanges() {
        if (currentCell != null) {
            currentCell.persist();
        }
        super.saveChanges();
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        data.writeVarInt(BuiltInRegistries.ITEM.getId(getCell().getItem()));
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        Item item = BuiltInRegistries.ITEM.byId(data.readVarInt());
        ItemStack stack = item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
        if (getCell().getItem() != item) {
            cellInventory.setItemDirect(0, stack);
            changed = true;
        }
        return changed;
    }

    @Override
    public void saveAdditional(CompoundTag data, HolderLookup.Provider registries) {
        super.saveAdditional(data, registries);
        cellInventory.writeToNBT(data, "cell", registries);
        config.writeToNBT(data, registries);
        data.putInt("priority", priority);
    }

    @Override
    public void loadTag(CompoundTag data, HolderLookup.Provider registries) {
        super.loadTag(data, registries);
        releaseCurrentCell();
        cachedCellStack = ItemStack.EMPTY;
        currentCell = null;
        currentCellFromEco = false;
        cellInteractionId = UUID.randomUUID();
        cellInventory.readFromNBT(data, "cell", registries);
        config.readFromNBT(data, registries);
        priority = data.getInt("priority");
        updateHandler();
    }

    @Override
    public void onReady() {
        super.onReady();
        gridNodeInitialized = true;
        updateHandler();
        IStorageProvider.requestUpdate(getMainNode());
        updateDisplay();
    }

    @Override
    public void onChunkUnloaded() {
        releaseCurrentCell();
        super.onChunkUnloaded();
    }

    @Override
    public void setRemoved() {
        releaseCurrentCell();
        super.setRemoved();
    }

    public void preparePlayerBreak(boolean creative) {
        dropCellOnBreak = !creative;
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        ItemStack cell = getCell();
        if (!cell.isEmpty()) {
            releaseCurrentCell();
            cellInventory.setItemDirect(0, ItemStack.EMPTY);
            cachedCellStack = ItemStack.EMPTY;
            currentCell = null;
            currentCellFromEco = false;
            cellInteractionId = UUID.randomUUID();
            idlePowerUsage = BASE_IDLE_POWER;
            updateIdlePowerUsage(BASE_IDLE_POWER);
            IStorageProvider.requestUpdate(getMainNode());
            invalidateCapabilities();
        }
        super.addAdditionalDrops(level, pos, drops);
        if (!cell.isEmpty() && dropCellOnBreak) {
            drops.add(cell);
        }
    }

    private void updateHandler() {
        if (isClientSide()) {
            return;
        }
        ItemStack stack = getCell();
        if (stack.isEmpty()) {
            releaseCurrentCell();
            cachedCellStack = stack;
            idlePowerUsage = BASE_IDLE_POWER;
            updateIdlePowerUsage(BASE_IDLE_POWER);
            return;
        }
        if (cachedCellStack == stack && currentCell != null) {
            return;
        }

        releaseCurrentCell();
        cachedCellStack = stack;
        StorageCell cell = null;
        boolean fromEco = false;
        if (StorageCells.isCellHandled(stack)) {
            cell = StorageCells.getCellInventory(stack, this);
        }
        if (cell == null && ECOStorageCells.isCellHandled(stack)) {
            cell = ECOStorageCells.getCellInventory(stack, this);
            fromEco = cell != null;
        }
        currentCell = cell;
        currentCellFromEco = fromEco;
        idlePowerUsage = BASE_IDLE_POWER + (cell == null ? 0.0 : cell.getIdleDrain());
        updateIdlePowerUsage(idlePowerUsage);
        updateDisplay();
    }

    private void releaseCurrentCell() {
        if (currentCell != null) {
            currentCell.persist();
            if (currentCellFromEco && !cachedCellStack.isEmpty()) {
                ECOStorageCells.releaseCellInventory(cachedCellStack, this);
            }
        }
        currentCell = null;
        currentCellFromEco = false;
    }

    private void updateDisplay() {
        if (isClientSide()) {
            return;
        }
        CellState status = currentCell == null ? CellState.ABSENT : currentCell.getStatus();
        DisplayState state = isPowered() ? displayStateOf(status) : DisplayState.OFF;
        if (state != lastDisplayState) {
            lastDisplayState = state;
            markForUpdate();
        }
    }
}

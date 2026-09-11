package cn.dancingsnow.neoecoprototype.blockentity.storage;

import appeng.api.networking.IGridNodeListener;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostActionUI;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostUI;
import cn.dancingsnow.neoecoae.gui.theme.NEStyleSheets;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.style.StylesheetManager;
import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import com.lowdragmc.lowdraglib2.syncdata.annotation.Persisted;
import com.lowdragmc.lowdraglib2.syncdata.holder.blockentity.ISyncPersistRPCBlockEntity;
import com.lowdragmc.lowdraglib2.syncdata.storage.FieldManagedStorage;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandlerModifiable;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** L1 storage controller state and host for the reusable eco builder UI. */
public class SimplifyStorageHostBlockEntity
        extends NEBlockEntity<SimplifyStorageCluster, SimplifyStorageHostBlockEntity>
        implements ISyncPersistRPCBlockEntity, IStorageProvider, MultiBlockBuildController.Host {

    private static final Logger LOGGER = LoggerFactory.getLogger(SimplifyStorageHostBlockEntity.class);
    private static final int DEFAULT_BUILD_LENGTH = 1;

    private final FieldManagedStorage syncStorage = new FieldManagedStorage(this);

    @Override
    public FieldManagedStorage getSyncStorage() {
        return syncStorage;
    }

    @Persisted
    @DescSynced
    private int selectedBuildLength = DEFAULT_BUILD_LENGTH;

    @Persisted
    @DescSynced
    private boolean mirrorBuild;

    @Persisted
    @DescSynced
    private int storagePriority;

    @DescSynced
    private boolean buildInProgress;

    private final MultiBlockBuildController buildController = new MultiBlockBuildController(this);
    private boolean mirrored;

    public SimplifyStorageHostBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    /** Drives own the finite cells; the host still advertises the storage service like eco's controller. */
    @Override
    public void mountInventories(IStorageMounts storageMounts) {
    }

    @Override
    public void updateCluster(SimplifyStorageCluster next) {
        boolean wasAttached = getCluster() != null;
        super.updateCluster(next);
        boolean isAttached = getCluster() != null;
        if (level != null && !level.isClientSide && wasAttached != isAttached) {
            LOGGER.info("host @{} cluster attach={} (tick {})", worldPosition, isAttached,
                    level.getServer() != null ? level.getServer().getTickCount() : -1);
            if (getCluster() != null) {
                SimplifyGridFacade.requestStorageUpdate(getMainNode());
                int onlineDrives = 0;
                int poweredDrives = 0;
                int connectedDrives = 0;
                for (SimplifyDriveBlockEntity drive : getCluster().getDrives()) {
                    var managedNode = drive.getMainNode();
                    SimplifyGridFacade.requestStorageUpdate(managedNode);
                    if (drive.isOnline()) {
                        onlineDrives++;
                    }
                    if (managedNode.isPowered()) {
                        poweredDrives++;
                    }
                    if (SimplifyGridFacade.isConnected(managedNode)) {
                        connectedDrives++;
                    }
                }
                LOGGER.info("L1 drives status @{}: total={}, online={}, powered={}, connected={}",
                        worldPosition, getCluster().getDrives().size(), onlineDrives, poweredDrives, connectedDrives);
                if (getCluster().getTheInterface() != null) {
                    SimplifyGridFacade.requestStorageUpdate(getCluster().getTheInterface().getMainNode());
                }
            }
        }
    }

    /** Server ticker used by the incremental non-creative builder. */
    public static void tick(Level level, BlockPos pos, BlockState state, SimplifyStorageHostBlockEntity host) {
        boolean wasBuilding = host.isBuildInProgress();
        host.buildController.tick(level);
        if (wasBuilding && !host.isBuildInProgress() && level instanceof ServerLevel) {
            LOGGER.info("L1 auto-build finished @{} formed={} length={} mirrored={}",
                    pos, host.isFormed(), host.getSelectedBuildLength(), host.isMirrorBuild());
        }
    }

    public SimplifyTier getTier() {
        return SimplifyTier.L1;
    }

    public boolean isMirrored() {
        return mirrored;
    }

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    public int getStoragePriority() {
        return storagePriority;
    }

    public void setStoragePriority(int storagePriority) {
        if (this.storagePriority == storagePriority) {
            return;
        }
        this.storagePriority = storagePriority;
        setChanged();
        refreshDriveStorageProviders();
    }

    private void refreshDriveStorageProviders() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return;
        }
        SimplifyGridFacade.requestStorageUpdate(getMainNode());
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            SimplifyGridFacade.requestStorageUpdate(drive.getMainNode());
        }
        if (cluster.getTheInterface() != null) {
            SimplifyGridFacade.requestStorageUpdate(cluster.getTheInterface().getMainNode());
        }
    }

    @Override
    public void onReady() {
        getMainNode().addService(IStorageProvider.class, this);
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.storageControllerIdlePower());
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        if (level == null || level.isClientSide || isRemoved()) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (state.hasProperty(SimplifyStorageControllerBlock.MIRRORED)) {
            BlockState newState = state.setValue(
                    SimplifyStorageControllerBlock.MIRRORED,
                    isFormed() && mirrored);
            if (newState != state) {
                level.setBlock(worldPosition, newState, Block.UPDATE_CLIENTS);
            }
        }
    }

    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        UIElement root = StorageHostUI.create(createStoragePanelConfig())
                .addClass("panel_bg");

        StorageHostActionUI.Elements actionUI = StorageHostActionUI.create(new StorageHostActionUI.Config(
                holder.player,
                () -> selectedBuildLength,
                () -> mirrorBuild,
                mirror -> buildController.setMirrorBuild(holder.player, mirror),
                () -> buildController.decreaseBuildLength(holder.player),
                () -> buildController.increaseBuildLength(holder.player),
                () -> requestBuild(holder.player),
                this::isFormed,
                () -> buildInProgress,
                buildController::createLocalPreviewPlan,
                () -> storagePriority,
                priority -> {
                    if (canPlayerInteract(holder.player)) {
                        setStoragePriority(priority);
                    }
                },
                delta -> changeStoragePriority(holder.player, delta),
                 () -> false,
                 () -> 0L,
                 () -> { }));
        actionUI.addTo(root);
        return new ModularUI(UI.of(root,
                java.util.List.of(StylesheetManager.INSTANCE.getStylesheetSafe(NEStyleSheets.ECO))), holder.player);
    }

    private StorageHostUI.Config createStoragePanelConfig() {
        return new StorageHostUI.Config(
                () -> getBlockState().getBlock().getName(),
                this::getStoredEnergy,
                this::getMaxEnergy,
                () -> 0L,
                () -> Long.toString(getMaxLoadUsedBytes()),
                this::createCellEntries,
                java.util.List.of(
                        createStorageTypeLine(SimplifyStorageCellItem.getItemCellType(), 0),
                        createStorageTypeLine(SimplifyStorageCellItem.getFluidCellType(), 1)),
                () -> false,
                () -> false,
                () -> false,
                createReadOnlyEmptyComponentInventory());
    }

    private StorageHostUI.StorageTypeLine createStorageTypeLine(ECOCellType cellType, int registryIndex) {
        return new StorageHostUI.StorageTypeLine(
                cellType,
                registryIndex,
                () -> storageTotals(cellType).usedTypes(),
                () -> storageTotals(cellType).totalTypes(),
                () -> storageTotals(cellType).usedBytes(),
                () -> storageTotals(cellType).totalBytes(),
                () -> "0");
    }

    private java.util.List<StorageHostUI.CellEntry> createCellEntries() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return java.util.List.of();
        }
        java.util.List<StorageHostUI.CellEntry> entries = new java.util.ArrayList<>();
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            IECOStorageCell cell = drive.getCellInventory();
            if (cell == null) {
                continue;
            }
            ECOCellType cellType = cell.getCellType();
            int typeId = cellType.equals(SimplifyStorageCellItem.getFluidCellType())
                    ? 1 : 0;
            int kind = typeId == 1 ? StorageHostUI.CellEntry.KIND_FLUID : StorageHostUI.CellEntry.KIND_ITEM;
            entries.add(new StorageHostUI.CellEntry(
                    typeId,
                    cell.getTier().getTier(),
                    kind,
                    Math.max(0L, cell.getStoredItemTypes()),
                    Math.max(0L, cell.getTotalItemTypes()),
                    Math.max(0L, cell.getUsedBytes()),
                    Math.max(0L, cell.getTotalBytes()),
                    cell.hasInfiniteTypeCapacity()));
        }
        return entries;
    }

    private L1StorageTotals storageTotals(ECOCellType cellType) {
        long usedTypes = 0L;
        long totalTypes = 0L;
        long usedBytes = 0L;
        long totalBytes = 0L;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return new L1StorageTotals(0L, 0L, 0L, 0L);
        }
        for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
            IECOStorageCell cell = drive.getCellInventory();
            if (cell == null || !cellType.equals(cell.getCellType())) {
                continue;
            }
            usedTypes = saturatingAdd(usedTypes, Math.max(0L, cell.getStoredItemTypes()));
            totalTypes = saturatingAdd(totalTypes, Math.max(0L, cell.getTotalItemTypes()));
            usedBytes = saturatingAdd(usedBytes, Math.max(0L, cell.getUsedBytes()));
            totalBytes = saturatingAdd(totalBytes, Math.max(0L, cell.getTotalBytes()));
        }
        return new L1StorageTotals(usedTypes, totalTypes, usedBytes, totalBytes);
    }

    private long getStoredEnergy() {
        double energy = 0.0D;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null) {
            for (SimplifyEnergyCellBlockEntity cell : cluster.getEnergyCells()) {
                energy += cell.getAECurrentPower();
            }
        }
        return saturatingDoubleToLong(energy);
    }

    private long getMaxEnergy() {
        double energy = 0.0D;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null) {
            for (SimplifyEnergyCellBlockEntity cell : cluster.getEnergyCells()) {
                energy += cell.getAEMaxPower();
            }
        }
        return saturatingDoubleToLong(energy);
    }

    private long getMaxLoadUsedBytes() {
        return saturatingAdd(
                storageTotals(SimplifyStorageCellItem.getItemCellType()).usedBytes(),
                storageTotals(SimplifyStorageCellItem.getFluidCellType()).usedBytes());
    }

    private long getMaxLoadTotalBytes() {
        return saturatingAdd(
                storageTotals(SimplifyStorageCellItem.getItemCellType()).totalBytes(),
                storageTotals(SimplifyStorageCellItem.getFluidCellType()).totalBytes());
    }

    private int getIdleMatrixCount() {
        int idle = 0;
        SimplifyStorageCluster cluster = getCluster();
        if (cluster != null) {
            for (SimplifyDriveBlockEntity drive : cluster.getDrives()) {
                IECOStorageCell cell = drive.getCellInventory();
                if (cell != null && cell.getStoredItemTypes() <= 0L) {
                    idle++;
                }
            }
        }
        return idle;
    }

    private static long saturatingAdd(long left, long right) {
        if (right <= 0L) {
            return left;
        }
        return left > Long.MAX_VALUE - right ? Long.MAX_VALUE : left + right;
    }

    private static long saturatingDoubleToLong(double value) {
        if (!(value > 0.0D)) {
            return 0L;
        }
        return value >= Long.MAX_VALUE ? Long.MAX_VALUE : Math.round(value);
    }

    private static IItemHandlerModifiable createReadOnlyEmptyComponentInventory() {
        return new ItemStackHandler(1) {
            @Override
            public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
                return stack;
            }

            @Override
            public ItemStack extractItem(int slot, int amount, boolean simulate) {
                return ItemStack.EMPTY;
            }

            @Override
            public boolean isItemValid(int slot, ItemStack stack) {
                return false;
            }

            @Override
            public void setStackInSlot(int slot, ItemStack stack) {
            }
        };
    }

    private record L1StorageTotals(long usedTypes, long totalTypes, long usedBytes, long totalBytes) {
    }


    private void requestBuild(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!canPlayerInteract(player)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.too_far");
            return;
        }
        if (!(level instanceof ServerLevel)) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.server_only");
            return;
        }
        if (isFormed()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.already_formed");
            return;
        }
        if (isBuildInProgress()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.build_in_progress");
            return;
        }
        MultiBlockPlacementPlan plan = buildController.createLocalPreviewPlan();
        if (plan == null) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.no_plan");
            return;
        }
        if (!plan.getConflictPositions().isEmpty()) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.conflicts", plan.getConflictPositions().size());
            return;
        }
        if (!serverPlayer.isCreative()
                && !MultiBlockPlacementService.hasRequiredItems(serverPlayer, plan.getRequiredItems())) {
            sendBuildMessage(serverPlayer, "gui.neoecoprototype.multiblock.missing_items");
            return;
        }
        LOGGER.info("L1 auto-build accepted @{} length={} mirrored={} missing={} requiredItems={} creative={}",
                worldPosition, getSelectedBuildLength(), isMirrorBuild(), plan.getMissingBlocks().size(),
                plan.getRequiredItemCount(), serverPlayer.isCreative());
        buildController.autoBuild(serverPlayer);
    }

    private static void sendBuildMessage(ServerPlayer player, String key, Object... args) {
        player.displayClientMessage(Component.translatable(key, args), true);
    }

    private void changeStoragePriority(Player player, int delta) {
        if (canPlayerInteract(player)) {
            setStoragePriority(storagePriority + delta);
        }
    }

    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyStorageDefinition.L1;
    }

    @Override
    public int getMinBuildLength() {
        return getBuildDefinition().getExpandMin();
    }

    @Override
    public int getMaxBuildLength() {
        return getBuildDefinition().getExpandMax();
    }

    @Override
    public int getSelectedBuildLength() {
        return selectedBuildLength;
    }

    @Override
    public void setSelectedBuildLength(int length) {
        selectedBuildLength = Math.clamp(length, getMinBuildLength(), getMaxBuildLength());
    }

    @Override
    public boolean isMirrorBuild() {
        return mirrorBuild;
    }

    @Override
    public void setMirrorBuild(boolean mirrorBuild) {
        this.mirrorBuild = mirrorBuild;
    }

    @Override
    public boolean isBuildInProgress() {
        return buildInProgress;
    }

    @Override
    public void setBuildInProgress(boolean buildInProgress) {
        this.buildInProgress = buildInProgress;
    }

    @Override
    public boolean isFormed() {
        return formed;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return level != null && SimplifyStorageControllerBlock.isPlayerCloseEnough(level, worldPosition, player);
    }

    @Override
    public Level getBuildLevel() {
        return level;
    }

    @Override
    public BlockPos getBuildPosition() {
        return worldPosition;
    }

    @Override
    public BlockState getBuildState() {
        return getBlockState();
    }

    @Override
    public void rebuildAfterBuild() {
        rebuildMultiblock();
    }

    @Override
    public void buildStateChanged() {
        setChanged();
        markForUpdate();
    }

    @Override
    public void onMainNodeStateChanged(IGridNodeListener.State reason) {
        if (isServerStopping()) {
            return;
        }
        super.onMainNodeStateChanged(reason);
    }
}

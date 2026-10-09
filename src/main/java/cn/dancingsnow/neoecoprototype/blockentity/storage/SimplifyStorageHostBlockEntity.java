package cn.dancingsnow.neoecoprototype.blockentity.storage;

import appeng.api.networking.IGridNodeListener;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEItemKey;
import gripe._90.megacells.misc.CompressionService;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.IStorageProvider;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCellItem;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostActionUI;
import cn.dancingsnow.neoecoae.gui.storage.StorageHostUI;
import cn.dancingsnow.neoecoae.gui.theme.NEStyleSheets;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifyUniversalStorageCellItem;
import cn.dancingsnow.neoecoprototype.integration.omni.SimplifyQuantumStorageCellItem;
import net.neoforged.fml.ModList;
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
import net.minecraft.resources.ResourceLocation;
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

import java.util.ArrayList;
import java.util.List;

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
    private boolean communicationInterface;

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
            LOGGER.debug("host @{} cluster attach={} (tick {})", worldPosition, isAttached,
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
                LOGGER.debug("L1 drives status @{}: total={}, online={}, powered={}, connected={}",
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

    public void setMirrored(boolean mirrored) {
        this.mirrored = mirrored;
    }

    /** Recorded by the calculator; published onto the block state by {@link #updateState(boolean)}. */
    public void setCommunicationInterface(boolean communicationInterface) {
        this.communicationInterface = communicationInterface;
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
            boolean formed = isFormed();
            BlockState newState = state
                    .setValue(SimplifyStorageControllerBlock.MIRRORED, formed && mirrored)
                    .setValue(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE,
                            formed && communicationInterface);
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
                 this::bulkMarkingAvailable,
                 this::bulkMarkingThreshold,
                 // eco 21.2.0 turned this from a Runnable into a Consumer<Boolean> carrying the shift
                 // state of the right-click that triggers it. We mark the same way either way for now,
                 // but the flag is there if the button ever wants "shift = ignore the threshold".
                 shift -> autoMarkBulkCells(holder.player)));
        actionUI.addTo(root);
        return new ModularUI(UI.of(root,
                java.util.List.of(StylesheetManager.INSTANCE.getStylesheetSafe(NEStyleSheets.ECO))), holder.player);
    }

    private boolean bulkMarkingAvailable() {
        return !smallBulkDrives().isEmpty();
    }

    private long bulkMarkingThreshold() {
        return NeoECOPrototypeServerConfig.MEGA_BULK_AUTO_MARK_THRESHOLD.get();
    }

    private void autoMarkBulkCells(Player player) {
        if (!canPlayerInteract(player)) {
            return;
        }
        var counts = runAutoMark();
        player.displayClientMessage(Component.translatable(
                "gui.neoecoprototype.storage.bulk_mark.result", counts.added(), counts.alreadyMarked()), true);
    }

    /** What one pass of the marking button did. */
    public record BulkMarkCounts(int added, int alreadyMarked) {
    }

    /**
     * One pass of the marking, without a player: the button only adds the interaction check and the chat line,
     * so this is the half a test can reach.
     */
    public BulkMarkCounts runAutoMark() {
        if (level == null || level.isClientSide) {
            return new BulkMarkCounts(0, 0);
        }
        if (!ModList.get().isLoaded("megacells")) {
            // The chain lookup further down is MegaCells' own code. Small-bulk cells cannot be
            // registered without the mod, but a world made while it was installed still carries the
            // drives, so the button can still be pressed with the mod gone.
            return new BulkMarkCounts(0, 0);
        }
        long threshold = bulkMarkingThreshold();
        // One inventory read per drive, not one per (target, source) pair: every small-bulk drive in the
        // cluster is offered the same sources, and getAvailableStacks walks the cell each time it is called.
        var availableByDrive = new java.util.IdentityHashMap<SimplifyDriveBlockEntity, appeng.api.stacks.KeyCounter>();
        for (SimplifyDriveBlockEntity source : getCluster().getDrives()) {
            IECOStorageCell inventory = source.getCellInventory();
            if (inventory == null) {
                continue;
            }
            appeng.api.stacks.KeyCounter available = new appeng.api.stacks.KeyCounter();
            inventory.getAvailableStacks(available);
            availableByDrive.put(source, available);
        }
        int added = 0;
        int alreadyMarked = 0;
        List<AEItemKey> markedKeys = new ArrayList<>();
        for (SimplifyDriveBlockEntity target : smallBulkDrives()) {
            ItemStack targetStack = target.getCellStack();
            if (!(targetStack.getItem() instanceof SimplifySmallBulkStorageCellItem item)) {
                continue;
            }
            var config = item.getConfigInventory(targetStack);
            for (var bySource : availableByDrive.entrySet()) {
                if (bySource.getKey() == target) {
                    continue;
                }
                for (var entry : bySource.getValue()) {
                    if (entry.getLongValue() <= threshold || !(entry.getKey() instanceof AEItemKey key)) {
                        continue;
                    }
                    if (CompressionService.getChain(key).isEmpty()) {
                        continue;
                    }
                    boolean exists = markedKeys.stream().anyMatch(existing -> sameMarkerChain(existing, key));
                    for (int slot = 0; slot < config.size() && !exists; slot++) {
                        if (config.getKey(slot) instanceof AEItemKey existing && sameMarkerChain(existing, key)) {
                            exists = true;
                        }
                    }
                    if (exists) {
                        alreadyMarked++;
                        continue;
                    }
                    for (int slot = 0; slot < config.size(); slot++) {
                        if (config.getKey(slot) == null) {
                            config.setStack(slot, new appeng.api.stacks.GenericStack(key, 0L));
                            markedKeys.add(key);
                            added++;
                            break;
                        }
                    }
                }
            }
        }
        refreshDriveStorageProviders();
        return new BulkMarkCounts(added, alreadyMarked);
    }

    /**
     * eco's own rule for "these two markers stand for the same compression chain", so the two hosts cannot
     * drift apart on what counts as already marked. It treats an empty chain as never matching, which is what
     * the gate above relies on.
     */
    public static boolean sameMarkerChain(AEItemKey left, AEItemKey right) {
        return cn.dancingsnow.neoecoae.integration.StorageBulkMarkingIntegration
                .isSameMarkerChain(left.toStack(), right.toStack());
    }

    public List<SimplifyDriveBlockEntity> smallBulkDrives() {
        SimplifyStorageCluster cluster = getCluster();
        if (cluster == null) {
            return List.of();
        }
        return cluster.getDrives().stream()
                .filter(drive -> drive.getCellStack() != null
                        && drive.getCellStack().getItem() instanceof SimplifySmallBulkStorageCellItem)
                .toList();
    }

    private StorageHostUI.Config createStoragePanelConfig() {
        return new StorageHostUI.Config(
                () -> getBlockState().getBlock().getName(),
                this::getStoredEnergy,
                this::getMaxEnergy,
                () -> 0L,
                () -> Long.toString(getMaxLoadUsedBytes()),
                this::createCellEntries,
                createStorageTypeLines(),
                () -> false,
                () -> false,
                () -> false,
                () -> "",
                () -> 0,
                createReadOnlyEmptyComponentInventory());
    }

    /**
     * The panel's rows. eco joins a cell entry to a row by number ({@code entry.typeId() ==
     * line.registryIndex()}), so the list below and {@link #panelRowOf} have to speak the same numbers -
     * which is why neither of them writes a bare digit.
     */
    public static final int ROW_ITEM = 0;
    public static final int ROW_MEGA_ITEM = 1;
    public static final int ROW_FLUID = 2;
    public static final int ROW_CHEMICAL = 3;
    public static final int ROW_UNIVERSAL = 4;
    public static final int ROW_QUANTUM = 5;
    public static final int ROW_CONCRETE = 6;
    public static final int ROW_FLUX = 7;
    /** Not a row: a cell the panel must not count anywhere. */
    public static final int ROW_NONE = -1;

    private java.util.List<StorageHostUI.StorageTypeLine> createStorageTypeLines() {
        java.util.List<StorageHostUI.StorageTypeLine> lines = new java.util.ArrayList<>();
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getItemCellType(), ROW_ITEM));
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getMegaItemCellType(), ROW_MEGA_ITEM));
        lines.add(createStorageTypeLine(SimplifyStorageCellItem.getFluidCellType(), ROW_FLUID));
        ECOCellType chemical = getChemicalCellType();
        if (chemical != null) {
            lines.add(createStorageTypeLine(chemical, ROW_CHEMICAL));
        }
        if (ModList.get().isLoaded("ae2omnicells")) {
            lines.add(createStorageTypeLine(SimplifyUniversalStorageCellItem.getUniversalCellType(), ROW_UNIVERSAL));
            lines.add(createStorageTypeLine(SimplifyQuantumStorageCellItem.getQuantumCellType(), ROW_QUANTUM));
        }
        // Dedicated row for the infinite concrete matrix.
        lines.add(createStorageTypeLine(SimplifyConcreteStorageCellItem.CELL_TYPE, ROW_CONCRETE));
        // The flux matrices get their own row. Its name is not ours to write: eco builds the flux cell type's
        // description out of appflux's own medium word (appflux.key.flux), so this row reads exactly as it
        // does in eco's host - the same way the item row reads "item" rather than "matrix".
        ECOCellType flux = getFluxCellType();
        if (flux != null) {
            lines.add(createStorageTypeLine(flux, ROW_FLUX));
        }
        return lines;
    }

    /**
     * Which row of the panel a mounted cell belongs to, or {@link #ROW_NONE} when no row claims it.
     *
     * @param keyTypes the mounted item's own key types, or null when the stack is not an eco cell item at
     *                 all - a foreign cell then has nothing to read but its cell type
     */
    public static int panelRowOf(ECOCellType cellType, java.util.Set<AEKeyType> keyTypes) {
        if (SimplifySingularityCellItem.CELL_TYPE.equals(cellType)) {
            // No row for it, and not because nobody thought of one: it reports an unbounded byte total and
            // its stock is a world-clock figure, so whichever row it landed in would read as that medium's
            // own number. The mounted cell is named on the drive and in Jade instead.
            return ROW_NONE;
        }
        int row = SimplifyStorageCellItem.getMegaItemCellType().equals(cellType) ? ROW_MEGA_ITEM : ROW_ITEM;
        // The same gate as the two rows above, and for the same reason: our omni item classes extend
        // eco's, which extend OmniCells', so reaching for them at all without that mod loaded is a
        // NoClassDefFoundError - not a null.
        if (ModList.get().isLoaded("ae2omnicells")) {
            if (SimplifyUniversalStorageCellItem.getUniversalCellType().equals(cellType)) {
                return ROW_UNIVERSAL;
            }
            if (SimplifyQuantumStorageCellItem.getQuantumCellType().equals(cellType)) {
                return ROW_QUANTUM;
            }
        }
        if (SimplifyConcreteStorageCellItem.CELL_TYPE.equals(cellType)) {
            return ROW_CONCRETE;
        }
        ECOCellType flux = registeredFluxType();
        if (flux != null && flux.equals(cellType)) {
            return ROW_FLUX;
        }
        if (keyTypes != null) {
            if (keyTypes.contains(AEKeyType.fluids())) {
                return ROW_FLUID;
            }
            if (keyTypes.stream().anyMatch(type -> "chemical".equals(type.getId().getPath()))) {
                return ROW_CHEMICAL;
            }
        } else if (SimplifyStorageCellItem.getFluidCellType().equals(cellType)) {
            // A fluid matrix whose item does not implement eco's cell interface: still the fluid medium.
            return ROW_FLUID;
        }
        return row;
    }

    /** The icon eco draws for a row. Flux is neither items nor fluids to appflux, so it takes "other". */
    public static int panelKindOf(int row) {
        return switch (row) {
            case ROW_FLUID -> StorageHostUI.CellEntry.KIND_FLUID;
            case ROW_CHEMICAL -> StorageHostUI.CellEntry.KIND_GAS;
            case ROW_FLUX -> StorageHostUI.CellEntry.KIND_OTHER;
            default -> StorageHostUI.CellEntry.KIND_ITEM;
        };
    }

    private static java.util.Set<AEKeyType> keyTypesOf(SimplifyDriveBlockEntity drive) {
        ItemStack cellStack = drive.getCellStack();
        return cellStack != null && cellStack.getItem() instanceof IECOStorageCellItem cellItem
                ? cellItem.getKeyTypes() : null;
    }

    private ECOCellType getChemicalCellType() {
        return NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", "mekanism"));
    }

    /** eco registers the flux type only while appflux is present; without it this row is simply not drawn. */
    private ECOCellType getFluxCellType() {
        return registeredFluxType();
    }

    private static ECOCellType registeredFluxType() {
        return NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", "flux"));
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
            int typeId = panelRowOf(cellType, keyTypesOf(drive));
            if (typeId == ROW_NONE) {
                continue;
            }
            entries.add(new StorageHostUI.CellEntry(
                    typeId,
                    cell.getTier().getTier(),
                    panelKindOf(typeId),
                    Math.max(0L, cell.getStoredItemTypes()),
                    Math.max(0L, cn.dancingsnow.neoecoprototype.items.SmallBulkTypeCap
                            .of(drive.getCellStack(), cell)),
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
            totalTypes = saturatingAdd(totalTypes, Math.max(0L,
                    cn.dancingsnow.neoecoprototype.items.SmallBulkTypeCap.of(drive.getCellStack(), cell)));
            usedBytes = saturatingAdd(usedBytes, Math.max(0L, cell.getUsedBytes()));
            totalBytes = saturatingAdd(totalBytes, Math.max(0L, cell.getTotalBytes()));
        }
        return new L1StorageTotals(usedTypes, totalTypes, usedBytes, totalBytes);
    }

    private L1StorageTotals chemicalStorageTotals() {
        ECOCellType chemical = getChemicalCellType();
        return chemical == null ? new L1StorageTotals(0L, 0L, 0L, 0L) : storageTotals(chemical);
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
                saturatingAdd(
                        storageTotals(SimplifyStorageCellItem.getItemCellType()).usedBytes(),
                        storageTotals(SimplifyStorageCellItem.getFluidCellType()).usedBytes()),
                chemicalStorageTotals().usedBytes());
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

package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridConnection;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECluster;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import appeng.api.networking.IGridNode;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * Runtime membership for the Trinity machine.
 *
 * <p>The controller and the three dedicated modules are all real members of this one cluster;
 * that is what makes Trinity a single machine instead of three borrowed subsystems.
 */
public class SimplifyTrinityCluster extends NECluster<SimplifyTrinityCluster> {
    private SimplifyTrinityControllerBlockEntity controller;
    @Nullable
    private SimplifyTrinityStorageModuleBlockEntity storageModule;
    @Nullable
    private SimplifyTrinityComputationModuleBlockEntity computationModule;
    @Nullable
    private SimplifyTrinityCraftingModuleBlockEntity craftingModule;

    /**
     * Read-only scan observations used for diagnostics. These lists never own or register external
     * storage, computation, pattern, or CPU subsystems; the Trinity modules and network services
     * remain the only execution path.
     */
    private final List<BlockEntity> storageParts = new ArrayList<>();
    private final List<BlockEntity> computationParts = new ArrayList<>();
    private final List<BlockEntity> craftingParts = new ArrayList<>();

    private TrinityService storageService = TrinityServiceAdapters.module(TrinityService.Module.STORAGE, null);
    private TrinityService computationService = TrinityServiceAdapters.module(TrinityService.Module.COMPUTATION, null);
    private TrinityService craftingService = TrinityServiceAdapters.module(TrinityService.Module.CRAFTING, null);
    private TrinityEnergySnapshot energySnapshot = new TrinityEnergySnapshot(0, 0, 0, false, false);
    private long computationConfigurationCapacity;
    private final List<IGridConnection> internalGridConnections = new ArrayList<>();

    public SimplifyTrinityCluster(BlockPos min, BlockPos max) {
        super(min, max);
    }

    public SimplifyTrinityControllerBlockEntity getController() {
        return controller;
    }

    public void setController(SimplifyTrinityControllerBlockEntity controller) {
        this.controller = controller;
    }

    @Nullable
    public SimplifyTrinityStorageModuleBlockEntity getStorageModule() {
        return storageModule;
    }

    @Nullable
    public SimplifyTrinityComputationModuleBlockEntity getComputationModule() {
        return computationModule;
    }

    @Nullable
    public SimplifyTrinityCraftingModuleBlockEntity getCraftingModule() {
        return craftingModule;
    }

    public void setStorageModule(@Nullable SimplifyTrinityStorageModuleBlockEntity module) {
        this.storageModule = module;
    }

    public void setComputationModule(@Nullable SimplifyTrinityComputationModuleBlockEntity module) {
        this.computationModule = module;
    }

    public void setCraftingModule(@Nullable SimplifyTrinityCraftingModuleBlockEntity module) {
        this.craftingModule = module;
    }

    /** Drops the module references before the wing snapshot is rebuilt. */
    public void clearModules() {
        storageModule = null;
        computationModule = null;
        craftingModule = null;
    }

    public List<BlockEntity> getStorageParts() {
        return storageParts;
    }

    public List<BlockEntity> getComputationParts() {
        return computationParts;
    }

    public List<BlockEntity> getCraftingParts() {
        return craftingParts;
    }

    public TrinityService getStorageService() {
        return storageService;
    }

    public TrinityService getComputationService() {
        return computationService;
    }

    public TrinityService getCraftingService() {
        return craftingService;
    }

    public TrinityEnergySnapshot getEnergySnapshot() {
        return energySnapshot;
    }

    /** Performs a read-only preflight; no task or inventory mutation occurs. */
    public boolean isStorageResourceConnected() {
        return TrinityResourceCheck.isStorageConnected(storageParts);
    }

    public TrinityPatternCheck getPatternCheck(TrinityTaskRequest request) {
        return TrinityPatternCheck.simulate(craftingParts, request.targetItem(), request.quantity());
    }

    public TrinityPlanCheck getPlanCheck(TrinityTaskRequest request) {
        refreshServices();
        TrinityPatternCheck pattern = getPatternCheck(request);
        TrinityResourceCheck resource = getResourceCheck(request, pattern);
        return TrinityPlanCheck.evaluate(pattern, resource, craftingService);
    }

    /** Read-only preflight for the requested task, including pattern-input requirements. */
    public TrinityResourceCheck getResourceCheck(TrinityTaskRequest request) {
        return getResourceCheck(request, getPatternCheck(request));
    }

    private TrinityResourceCheck getResourceCheck(TrinityTaskRequest request,
                                                  TrinityPatternCheck pattern) {
        if (request.targetItem().equals(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("air"))) {
            return TrinityResourceCheck.noTarget();
        }
        return TrinityResourceCheck.simulateForCraft(storageParts, request.targetItem(),
                request.quantity(), pattern);
    }

    public TrinityTaskReadiness checkTaskReadiness() {
        return checkTaskReadiness(TrinityTaskRequest.fullTask(1));
    }

    public TrinityTaskReadiness checkTaskReadiness(TrinityTaskRequest request) {
        refreshServices();
        TrinityPatternCheck pattern = getPatternCheck(request);
        TrinityResourceCheck resource = getResourceCheck(request, pattern);
        return TrinityTaskReadiness.check(request, resource, pattern,
                storageService, computationService, craftingService, energySnapshot);
    }

    /** Refreshes read-only adapters without changing ownership of source clusters. */
    public void refreshServices() {
        connectInternalGrid();
        storageService = TrinityServiceAdapters.module(TrinityService.Module.STORAGE, storageModule);
        computationService = TrinityServiceAdapters.module(TrinityService.Module.COMPUTATION, computationModule);
        craftingService = TrinityServiceAdapters.module(TrinityService.Module.CRAFTING, craftingModule);
        energySnapshot = TrinityEnergySnapshot.capture(controller,
                membersOf(storageModule), membersOf(computationModule), membersOf(craftingModule));
        computationConfigurationCapacity = computationModule == null ? 0L : computationModule.getComputationConfigurationCapacity();
    }

    private void connectInternalGrid() {
        if (controller == null || controller.getMainNode().getNode() == null) {
            return;
        }
        IGridNode controllerNode = controller.getMainNode().getNode();
        connect(controllerNode, storageModule);
        connect(controllerNode, computationModule);
        connect(controllerNode, craftingModule);
    }

    private void connect(IGridNode controllerNode, @Nullable BlockEntity module) {
        if (!(module instanceof cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity<?, ?> member)) {
            return;
        }
        IGridNode moduleNode = member.getMainNode().getNode();
        if (moduleNode == null || controllerNode == moduleNode
                || controllerNode.getConnections().stream().anyMatch(connection ->
                connection.getOtherSide(controllerNode) == moduleNode)) {
            return;
        }
        internalGridConnections.add(GridHelper.createConnection(controllerNode, moduleNode));
    }

    /** Configuration capacity shown by Trinity; it is not an AE2 or eco crafting CPU. */
    public long getComputationConfigurationCapacity() {
        return computationConfigurationCapacity;
    }

    private static List<BlockEntity> membersOf(@Nullable BlockEntity module) {
        return module == null ? List.of() : List.of(module);
    }

    /** Records a module marker without making it a Trinity network member. */
    public void recordPart(BlockEntity part, Module module) {
        if (part == null) {
            return;
        }
        switch (module) {
            case STORAGE -> storageParts.add(part);
            case COMPUTATION -> computationParts.add(part);
            case CRAFTING -> craftingParts.add(part);
        }
    }

    /** Clears the read-only scan before rebuilding the wing snapshot. */
    public void clearRecordedParts() {
        storageParts.clear();
        computationParts.clear();
        craftingParts.clear();
    }

    @Override
    public void destroy() {
        internalGridConnections.forEach(IGridConnection::destroy);
        internalGridConnections.clear();
        super.destroy();
        controller = null;
        storageModule = null;
        computationModule = null;
        craftingModule = null;
        storageParts.clear();
        computationParts.clear();
        craftingParts.clear();
        storageService = TrinityServiceAdapters.module(TrinityService.Module.STORAGE, null);
        computationService = TrinityServiceAdapters.module(TrinityService.Module.COMPUTATION, null);
        craftingService = TrinityServiceAdapters.module(TrinityService.Module.CRAFTING, null);
        energySnapshot = new TrinityEnergySnapshot(0, 0, 0, false, false);
        computationConfigurationCapacity = 0L;
    }

    public enum Module {
        STORAGE, COMPUTATION, CRAFTING
    }
}

package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import cn.dancingsnow.neoecoae.multiblock.cluster.NECluster;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Runtime membership for the Trinity shell; service integration is added later. */
public class SimplifyTrinityCluster extends NECluster<SimplifyTrinityCluster> {
    private SimplifyTrinityControllerBlockEntity controller;
    private final List<BlockEntity> storageParts = new ArrayList<>();
    private final List<BlockEntity> computationParts = new ArrayList<>();
    private final List<BlockEntity> craftingParts = new ArrayList<>();
    private TrinityService storageService = TrinityServiceAdapters.storage(List.of());
    private TrinityService computationService = TrinityServiceAdapters.computation(List.of());
    private TrinityService craftingService = TrinityServiceAdapters.crafting(List.of());
    private TrinityEnergySnapshot energySnapshot = new TrinityEnergySnapshot(0, 0, 0, false, false);

    public SimplifyTrinityCluster(BlockPos min, BlockPos max) {
        super(min, max);
    }

    public SimplifyTrinityControllerBlockEntity getController() {
        return controller;
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

    public void setController(SimplifyTrinityControllerBlockEntity controller) {
        this.controller = controller;
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
        TrinityResourceCheck resource = request.targetItem().equals(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("air"))
                ? TrinityResourceCheck.noTarget()
                : TrinityResourceCheck.simulateStorage(storageParts, request.targetItem(), request.quantity());
        return TrinityPlanCheck.evaluate(pattern, resource, craftingService);
    }

    public TrinityTaskReadiness checkTaskReadiness() {
        return checkTaskReadiness(TrinityTaskRequest.fullTask(1));
    }

    public TrinityTaskReadiness checkTaskReadiness(TrinityTaskRequest request) {
        refreshServices();
        TrinityPatternCheck pattern = getPatternCheck(request);
        TrinityResourceCheck resource = request.targetItem().equals(
                net.minecraft.resources.ResourceLocation.withDefaultNamespace("air"))
                ? TrinityResourceCheck.noTarget()
                : TrinityResourceCheck.simulateStorage(storageParts, request.targetItem(), request.quantity());
        return TrinityTaskReadiness.check(request, resource, pattern,
                storageService, computationService, craftingService, energySnapshot);
    }

    /** Refreshes read-only adapters without changing ownership of source clusters. */
    public void refreshServices() {
        storageService = TrinityServiceAdapters.storage(storageParts);
        computationService = TrinityServiceAdapters.computation(computationParts);
        craftingService = TrinityServiceAdapters.crafting(craftingParts);
        energySnapshot = TrinityEnergySnapshot.capture(controller, storageParts, computationParts, craftingParts);
    }

    /** Clears the read-only scan before rebuilding the wing snapshot. */
    public void clearRecordedParts() {
        storageParts.clear();
        computationParts.clear();
        craftingParts.clear();
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

    @Override
    public void destroy() {
        super.destroy();
        controller = null;
        storageParts.clear();
        computationParts.clear();
        craftingParts.clear();
        storageService = TrinityServiceAdapters.storage(List.of());
        computationService = TrinityServiceAdapters.computation(List.of());
        craftingService = TrinityServiceAdapters.crafting(List.of());
        energySnapshot = new TrinityEnergySnapshot(0, 0, 0, false, false);
    }

    public enum Module {
        STORAGE, COMPUTATION, CRAFTING
    }
}

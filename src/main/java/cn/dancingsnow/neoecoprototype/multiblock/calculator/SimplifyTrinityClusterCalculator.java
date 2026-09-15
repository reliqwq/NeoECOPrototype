package cn.dancingsnow.neoecoprototype.multiblock.calculator;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEClusterCalculator;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster.Module;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import appeng.me.cluster.MBCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/** Fixed cross-shaped Trinity validator. */
public class SimplifyTrinityClusterCalculator extends NEClusterCalculator<SimplifyTrinityCluster> {
    public SimplifyTrinityClusterCalculator(NEBlockEntity<SimplifyTrinityCluster, ?> target) {
        super(target);
    }

    @Override
    protected int maxLength() {
        return 7;
    }

    /**
     * The cross silhouette has casing gaps between the controller and its wings, so the base
     * class's greedy per-axis expansion can never discover the 7x3x7 bounds. Derive them from
     * the fixed controller anchor instead, then reuse the standard assembly steps.
     */
    @Override
    public void calculateMultiblock(ServerLevel level, BlockPos pos) {
        if (MBCalculator.isModificationInProgress()) {
            return;
        }
        SimplifyTrinityCluster currentCluster = target.getCluster();
        if (currentCluster != null && currentCluster.isDestroyed()) {
            return;
        }
        BlockPos min = pos.offset(-3, -1, -3);
        BlockPos max = pos.offset(3, 1, 3);
        try {
            if (!checkMultiblockScale(min, max) || !verifyInternalStructure(level, min, max)) {
                disconnect();
                return;
            }
            boolean updateGrid = false;
            SimplifyTrinityCluster cluster = target.getCluster();
            if (cluster == null || !cluster.getBoundsMin().equals(min) || !cluster.getBoundsMax().equals(max)) {
                cluster = createCluster(level, min, max);
                MBCalculator.setModificationInProgress(cluster);
                updateBlockEntities(cluster, level, min, max);
                updateGrid = true;
            } else {
                MBCalculator.setModificationInProgress(cluster);
                cluster.clearRecordedParts();
                recordWingParts(cluster, level, min);
                cluster.refreshServices();
            }
            cluster.updateStatus(updateGrid);
        } catch (Exception err) {
            disconnect();
        } finally {
            MBCalculator.setModificationInProgress(null);
        }
    }

    @Override
    public void updateMultiblockAfterNeighborUpdate(ServerLevel level, BlockPos pos, BlockPos changedPos) {
        calculateMultiblock(level, pos);
    }

    @Override
    protected Holder<Block> casing() {
        return BuiltInRegistries.BLOCK.wrapAsHolder(ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get());
    }

    @Override
    public SimplifyTrinityCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new SimplifyTrinityCluster(min, max);
    }

    @Override
    public boolean checkMultiblockScale(BlockPos min, BlockPos max) {
        return max.getX() - min.getX() == 6 && max.getY() - min.getY() == 2 && max.getZ() - min.getZ() == 6;
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof SimplifyTrinityControllerBlockEntity;
    }

    /** Only the Trinity controller belongs to this cluster; wings remain independently owned. */
    @Override
    public void updateBlockEntities(SimplifyTrinityCluster cluster, ServerLevel level, BlockPos min, BlockPos max) {
        if (target instanceof SimplifyTrinityControllerBlockEntity controller) {
            cluster.addBlockEntity(controller);
            cluster.setController(controller);
            cluster.clearRecordedParts();
            recordWingParts(cluster, level, min);
            // The controller is the only Trinity-owned member; the base class normally performs
            // this write-back, but this override must repeat it or the cluster stays unattached.
            controller.updateCluster(cluster);
            cluster.updateFormed(true);
            cluster.refreshServices();
        }
    }

    private void recordWingParts(SimplifyTrinityCluster cluster, ServerLevel level, BlockPos min) {
        for (int y = 0; y < 3; y++) {
            cluster.recordPart(level.getBlockEntity(min.offset(0, y, 3)), Module.STORAGE);
        }
        cluster.recordPart(level.getBlockEntity(min.offset(1, 0, 3)), Module.STORAGE);
        cluster.recordPart(level.getBlockEntity(min.offset(1, 1, 3)), Module.STORAGE);
        cluster.recordPart(level.getBlockEntity(min.offset(1, 2, 3)), Module.STORAGE);
        cluster.recordPart(level.getBlockEntity(min.offset(3, 0, 1)), Module.COMPUTATION);
        cluster.recordPart(level.getBlockEntity(min.offset(3, 1, 1)), Module.COMPUTATION);
        cluster.recordPart(level.getBlockEntity(min.offset(3, 2, 1)), Module.COMPUTATION);
        cluster.recordPart(level.getBlockEntity(min.offset(3, 1, 0)), Module.COMPUTATION);
        cluster.recordPart(level.getBlockEntity(min.offset(4, 0, 2)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(4, 1, 2)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(4, 2, 2)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(5, 0, 3)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(5, 1, 3)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(5, 2, 3)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(5, 1, 2)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(6, 0, 3)), Module.CRAFTING);
        cluster.recordPart(level.getBlockEntity(min.offset(6, 2, 3)), Module.CRAFTING);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        if (target instanceof SimplifyTrinityControllerBlockEntity controller) {
            controller.setStructureValidation(result);
        }
        return result.valid();
    }

    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        BlockPos controllerPos = min.offset(3, 1, 3);
        if (!checkMultiblockScale(min, max)) {
            return StructureValidation.invalid(controllerPos, "Trinity structure must occupy exactly 7x3x7.");
        }
        if (!(level.getBlockEntity(controllerPos) instanceof SimplifyTrinityControllerBlockEntity)) {
            return StructureValidation.invalid(controllerPos, "Missing Trinity controller at the center.");
        }
        List<String> issues = new ArrayList<>();
        require(level, controllerPos, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(), issues);
        validateCoreCasing(level, min, issues);
        validateStorageWing(level, min, issues);
        validateComputationWing(level, min, issues);
        validateCraftingWing(level, min, issues);
        return issues.isEmpty()
                // Functional block entities only; the casing blocks carry no block entity and are
                // therefore never recorded as wing components.
                ? StructureValidation.valid(controllerPos, 6, 4, 9)
                : StructureValidation.invalid(controllerPos, issues);
    }

    private void validateCoreCasing(ServerLevel level, BlockPos min, List<String> issues) {
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                for (int y : new int[] {0, 2}) {
                    // The east edge is occupied by the crafting fluid hatches.
                    if (x == 4 && z == 2 && (y == 0 || y == 2)) {
                        continue;
                    }
                    require(level, min.offset(x, y, z), ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get(), issues);
                }
            }
        }
    }

    private void validateStorageWing(ServerLevel level, BlockPos min, List<String> issues) {
        require(level, min.offset(2, 1, 3), ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get(), issues);
        require(level, min.offset(1, 0, 3), ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get(), issues);
        require(level, min.offset(1, 1, 3), ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get(), issues);
        require(level, min.offset(1, 2, 3), ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get(), issues);
        for (int y = 0; y < 3; y++) {
            require(level, min.offset(0, y, 3), ModRegistration.SIMPLIFY_DRIVE_BLOCK.get(), issues);
        }
    }

    private void validateComputationWing(ServerLevel level, BlockPos min, List<String> issues) {
        require(level, min.offset(3, 0, 1), ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get(), issues);
        require(level, min.offset(3, 1, 1), ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), issues);
        require(level, min.offset(3, 2, 1), ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get(), issues);
        require(level, min.offset(3, 1, 0), ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get(), issues);
    }

    private void validateCraftingWing(ServerLevel level, BlockPos min, List<String> issues) {
        require(level, min.offset(4, 1, 3), ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get(), issues);
        require(level, min.offset(4, 0, 2), ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get(), issues);
        require(level, min.offset(4, 1, 2), ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), issues);
        require(level, min.offset(4, 2, 2), ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get(), issues);
        require(level, min.offset(5, 0, 3), ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get(), issues);
        require(level, min.offset(5, 1, 3), ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get(), issues);
        require(level, min.offset(5, 2, 3), ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get(), issues);
        require(level, min.offset(5, 1, 2), ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get(), issues);
        require(level, min.offset(6, 0, 3), ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get(), issues);
        require(level, min.offset(6, 2, 3), ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get(), issues);
    }

    private static void require(ServerLevel level, BlockPos pos, Block expected, List<String> issues) {
        if (!level.getBlockState(pos).is(expected)) {
            issues.add(pos + " expected " + expected.getDescriptionId());
        }
    }

    public record StructureValidation(boolean valid, BlockPos controllerPos, List<String> issues,
                                      int storageParts, int computationParts, int craftingParts) {
        static StructureValidation valid(BlockPos pos, int storageParts, int computationParts, int craftingParts) {
            return new StructureValidation(true, pos, List.of(), storageParts, computationParts, craftingParts);
        }

        public static StructureValidation invalid(String issue) {
            return new StructureValidation(false, null, List.of(issue), 0, 0, 0);
        }

        static StructureValidation invalid(BlockPos pos, String issue) {
            return new StructureValidation(false, pos, List.of(issue), 0, 0, 0);
        }

        static StructureValidation invalid(BlockPos pos, List<String> issues) {
            return new StructureValidation(false, pos, List.copyOf(issues), 0, 0, 0);
        }
    }
}

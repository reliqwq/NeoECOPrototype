package cn.dancingsnow.neoecoprototype.multiblock.calculator;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockRotation;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.SimplifyTrinityCluster;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import appeng.me.cluster.MBCalculator;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Trinity validator: a 3x3x3 shell holding the controller plus one dedicated module per wing.
 *
 * <p>The bounds are derived from the controller anchor instead of greedy per-axis expansion,
 * because every Trinity member (controller and modules) runs this calculator from its own
 * position. Members locate the controller first and then derive the shared box from it.
 */
public class SimplifyTrinityClusterCalculator extends NEClusterCalculator<SimplifyTrinityCluster> {
    public SimplifyTrinityClusterCalculator(NEBlockEntity<SimplifyTrinityCluster, ?> target) {
        super(target);
    }

    /**
     * Edge length of the Trinity shell. The placement preview
     * ({@code SimplifyTrinityDefinition}) reuses the offsets below so the two can never drift.
     */
    public static final int SHELL_SIZE = 3;

    /** Functional slot offsets inside the 3x3x3 box, relative to {@code min}. */
    public static final BlockPos CONTROLLER_OFFSET = new BlockPos(1, 1, 1);
    public static final BlockPos STORAGE_MODULE_OFFSET = new BlockPos(0, 1, 1);
    public static final BlockPos COMPUTATION_MODULE_OFFSET = new BlockPos(2, 1, 1);
    public static final BlockPos CRAFTING_MODULE_OFFSET = new BlockPos(1, 1, 0);

    /** True when the given in-shell offset is one of the four functional slots. */
    public static boolean isFunctionalSlot(BlockPos offset) {
        return offset.equals(CONTROLLER_OFFSET)
                || offset.equals(STORAGE_MODULE_OFFSET)
                || offset.equals(COMPUTATION_MODULE_OFFSET)
                || offset.equals(CRAFTING_MODULE_OFFSET);
    }

    @Override
    protected int maxLength() {
        return 3;
    }

    @Override
    public void calculateMultiblock(ServerLevel level, BlockPos pos) {
        if (MBCalculator.isModificationInProgress()) {
            return;
        }
        SimplifyTrinityCluster currentCluster = target.getCluster();
        if (currentCluster != null && currentCluster.isDestroyed()) {
            return;
        }
        BlockPos controllerPos = resolveControllerPos(level, pos);
        if (controllerPos == null) {
            // The controller's chunk may simply not be loaded yet while this member is restoring.
            // Only tear down when we actually hold a cluster; otherwise a member that loads before
            // the controller would destroy a structure that was never broken.
            if (target.getCluster() != null) {
                disconnect();
            }
            return;
        }
        BlockPos min = controllerPos.offset(-1, -1, -1);
        BlockPos max = controllerPos.offset(1, 1, 1);
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
                cluster.clearModules();
                collectModules(cluster, level, min);
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
        int span = SHELL_SIZE - 1;
        return max.getX() - min.getX() == span
                && max.getY() - min.getY() == span
                && max.getZ() - min.getZ() == span;
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof SimplifyTrinityControllerBlockEntity
                || blockEntity instanceof SimplifyTrinityStorageModuleBlockEntity
                || blockEntity instanceof SimplifyTrinityComputationModuleBlockEntity
                || blockEntity instanceof SimplifyTrinityCraftingModuleBlockEntity;
    }

    /**
     * Adopts every Trinity member in the box. Unlike the base class this tolerates positions that
     * carry a casing block without a block entity: only the four functional slots are members.
     */
    @Override
    public void updateBlockEntities(SimplifyTrinityCluster cluster, ServerLevel level, BlockPos min, BlockPos max) {
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            BlockEntity blockEntity = level.getBlockEntity(candidate);
            if (blockEntity == null) {
                continue;
            }
            if (!isValidBlockEntity(blockEntity)) {
                disconnect();
                return;
            }
            cluster.addBlockEntity(blockEntity);
        }
        cluster.getBlockEntities().forEachRemaining(member -> {
            if (member instanceof NEBlockEntity<?, ?> blockEntity) {
                updateClusterUnchecked(blockEntity, cluster);
            }
        });
        if (level.getBlockEntity(min.offset(CONTROLLER_OFFSET))
                instanceof SimplifyTrinityControllerBlockEntity controller) {
            cluster.setController(controller);
        }
        cluster.clearModules();
        collectModules(cluster, level, min);
        cluster.updateFormed(true);
        cluster.refreshServices();
    }

    private void collectModules(SimplifyTrinityCluster cluster, ServerLevel level, BlockPos min) {
        cluster.clearRecordedParts();
        Direction facing = controllerFacing(level, min);
        if (level.getBlockEntity(worldPos(min, STORAGE_MODULE_OFFSET, facing))
                instanceof SimplifyTrinityStorageModuleBlockEntity module) {
            cluster.setStorageModule(module);
            cluster.recordPart(module, SimplifyTrinityCluster.Module.STORAGE);
        }
        if (level.getBlockEntity(worldPos(min, COMPUTATION_MODULE_OFFSET, facing))
                instanceof SimplifyTrinityComputationModuleBlockEntity module) {
            cluster.setComputationModule(module);
            cluster.recordPart(module, SimplifyTrinityCluster.Module.COMPUTATION);
        }
        if (level.getBlockEntity(worldPos(min, CRAFTING_MODULE_OFFSET, facing))
                instanceof SimplifyTrinityCraftingModuleBlockEntity module) {
            cluster.setCraftingModule(module);
            cluster.recordPart(module, SimplifyTrinityCluster.Module.CRAFTING);
        }
    }

    private static Direction controllerFacing(ServerLevel level, BlockPos min) {
        BlockEntity entity = level.getBlockEntity(min.offset(CONTROLLER_OFFSET));
        if (entity instanceof SimplifyTrinityControllerBlockEntity controller
                && controller.getBlockState().hasProperty(net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING)) {
            return controller.getBlockState().getValue(
                    net.minecraft.world.level.block.state.properties.BlockStateProperties.HORIZONTAL_FACING);
        }
        return Direction.NORTH;
    }

    private static BlockPos worldPos(BlockPos min, BlockPos validatorOffset, Direction facing) {
        BlockPos definitionOffset = validatorOffset.subtract(CONTROLLER_OFFSET)
                .offset(MultiBlockRotation.CONTROLLER_ANCHOR);
        return MultiBlockRotation.localToWorld(definitionOffset, min.offset(CONTROLLER_OFFSET), facing, false);
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void updateClusterUnchecked(NEBlockEntity<?, ?> member, SimplifyTrinityCluster cluster) {
        ((NEBlockEntity) member).updateCluster(cluster);
    }

    /** The controller anchor for this calculation, or null when no controller is loaded nearby. */
    private BlockPos resolveControllerPos(ServerLevel level, BlockPos pos) {
        for (int y = -1; y <= 1; y++) {
            for (int z = -1; z <= 1; z++) {
                for (int x = -1; x <= 1; x++) {
                    BlockPos candidate = pos.offset(x, y, z);
                    if (level.getBlockEntity(candidate) instanceof SimplifyTrinityControllerBlockEntity) {
                        return candidate;
                    }
                }
            }
        }
        return null;
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
        BlockPos controllerPos = min.offset(CONTROLLER_OFFSET);
        if (!checkMultiblockScale(min, max)) {
            return StructureValidation.invalid(controllerPos, "Trinity structure must occupy exactly 3x3x3.");
        }
        if (!(level.getBlockEntity(controllerPos) instanceof SimplifyTrinityControllerBlockEntity)) {
            return StructureValidation.invalid(controllerPos, "Missing Trinity controller at the center.");
        }
        List<String> issues = new ArrayList<>();
        requireBlock(level, controllerPos, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(), issues);
        Direction facing = controllerFacing(level, min);
        requireBlockEntity(level, worldPos(min, STORAGE_MODULE_OFFSET, facing),
                SimplifyTrinityStorageModuleBlockEntity.class, "storage module", min, issues);
        requireBlockEntity(level, worldPos(min, COMPUTATION_MODULE_OFFSET, facing),
                SimplifyTrinityComputationModuleBlockEntity.class, "computation module", min, issues);
        requireBlockEntity(level, worldPos(min, CRAFTING_MODULE_OFFSET, facing),
                SimplifyTrinityCraftingModuleBlockEntity.class, "crafting module", min, issues);
        Block casingBlock = ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get();
        for (BlockPos candidate : BlockPos.betweenClosed(min, max)) {
            if (candidate.equals(controllerPos)
                    || candidate.equals(worldPos(min, STORAGE_MODULE_OFFSET, facing))
                    || candidate.equals(worldPos(min, COMPUTATION_MODULE_OFFSET, facing))
                    || candidate.equals(worldPos(min, CRAFTING_MODULE_OFFSET, facing))) {
                continue;
            }
            requireBlock(level, candidate, casingBlock, issues);
        }
        return issues.isEmpty()
                ? StructureValidation.valid(controllerPos, 1, 1, 1)
                : StructureValidation.invalid(controllerPos, issues);
    }

    private static void requireBlock(ServerLevel level, BlockPos pos, Block expected, List<String> issues) {
        if (!level.getBlockState(pos).is(expected)) {
            issues.add(pos + " expected " + expected.getDescriptionId());
        }
    }

    private static void requireBlockEntity(ServerLevel level, BlockPos pos, Class<?> expected,
                                           String label, BlockPos min, List<String> issues) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        String relative = "@(" + (pos.getX() - min.getX()) + ","
                + (pos.getY() - min.getY()) + "," + (pos.getZ() - min.getZ()) + ")";
        if (blockEntity == null) {
            issues.add(label + " " + relative);
            return;
        }
        if (!expected.isInstance(blockEntity)) {
            issues.add(label + " " + relative + " found " + blockEntity.getClass().getSimpleName());
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

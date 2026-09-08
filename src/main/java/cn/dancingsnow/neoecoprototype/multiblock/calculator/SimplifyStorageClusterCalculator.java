package cn.dancingsnow.neoecoprototype.multiblock.calculator;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import appeng.api.orientation.RelativeSide;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEClusterCalculator;
import cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyEnergyCellBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Structure validator of the L1 storage subsystem. Same geometry rules as eco's
 * {@code NEStorageClusterCalculator}, but validated against this addon's own
 * block set. Only the L1 controller tier (index 0) energy cells are accepted,
 * matching the {@code tier.supportsComponentTier(...)} semantics eco uses.
 */
public class SimplifyStorageClusterCalculator extends NEClusterCalculator<SimplifyStorageCluster> {

    /** Side-effect-free result of the storage geometry check. */
    public record StructureValidation(boolean valid, boolean mirrored, BlockPos controllerPos) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos);
        }
    }

    /** Max expandable length along the drive axis (measured in blocks). */
    private static final int MAX_LENGTH = SimplifyMultiblockConfig.L1_MAX_LENGTH;

    public SimplifyStorageClusterCalculator(NEBlockEntity<SimplifyStorageCluster, ?> t) {
        super(t);
    }

    @Override
    public SimplifyStorageCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new SimplifyStorageCluster(min, max);
    }

    @Override
    protected int maxLength() {
        return MAX_LENGTH;
    }

    @Override
    protected Holder<Block> casing() {
        return BuiltInRegistries.BLOCK.wrapAsHolder(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity te) {
        return te instanceof NEBlockEntity<?, ?> neBlockEntity
                && neBlockEntity.getCalculator() instanceof SimplifyStorageClusterCalculator;
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        applyMirroredState(level, result);
        return result.valid();
    }

    /** Reads the world and returns the geometry result without mutating the controller. */
    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        BlockPos controllerPos = null;
        for (BlockPos pos : allPossibleController(min, max)) {
            if (level.getBlockEntity(pos) instanceof SimplifyStorageHostBlockEntity) {
                controllerPos = pos;
                break;
            }
        }
        if (controllerPos == null) {
            return StructureValidation.invalid(null);
        }

        BlockState controllerState = level.getBlockState(controllerPos);
        IOrientationStrategy strategy = OrientationStrategies.horizontalFacing();
        Direction back = strategy.getSide(controllerState, RelativeSide.BACK);
        Direction front = back.getOpposite();
        Direction top = strategy.getSide(controllerState, RelativeSide.TOP);
        Direction down = top.getOpposite();
        Direction left = strategy.getSide(controllerState, RelativeSide.RIGHT);
        Direction right = left.getOpposite();

        if (verifyStructure(level, controllerPos, front, back, top, down, left, right)) {
            return new StructureValidation(true, false, controllerPos);
        }
        if (verifyStructure(level, controllerPos, front, back, top, down, right, left)) {
            return new StructureValidation(true, true, controllerPos);
        }
        return StructureValidation.invalid(controllerPos);
    }

    private void applyMirroredState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() != null
                && level.getBlockEntity(result.controllerPos()) instanceof SimplifyStorageHostBlockEntity controller) {
            controller.setMirrored(result.mirrored());
        }
    }

    private boolean verifyStructure(
            ServerLevel level,
            BlockPos controllerPos,
            Direction front,
            Direction back,
            Direction top,
            Direction down,
            Direction staticSide,
            Direction expandSide
    ) {
        if (!validateCasingColumn(level, controllerPos.relative(staticSide), top, down)) {
            return false;
        }
        if (!validateCasingColumn(level, controllerPos.relative(back), top, down)) {
            return false;
        }
        // interface at the back of the static side
        BlockPos interfacePos = controllerPos.relative(staticSide).relative(back);
        if (!validateBlock(level, interfacePos, state -> state.is(ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get())
                        || state.is(ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get()))) {
            return false;
        }
        if (!validateBlock(level, interfacePos.relative(top), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))) {
            return false;
        }
        if (!validateBlock(level, interfacePos.relative(down), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))) {
            return false;
        }
        if (!validateBlock(level, controllerPos.relative(top), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))) {
            return false;
        }
        if (!validateBlock(level, controllerPos.relative(down), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))) {
            return false;
        }

        BlockPos transitionCenter = controllerPos.relative(expandSide);
        if (!validateCasingColumn(level, transitionCenter, top, down)) {
            return false;
        }
        if (!validateCasingColumn(level, transitionCenter.relative(back), top, down)) {
            return false;
        }

        // drive row extending along expandSide (one drive per column position)
        BlockPos firstStorageColumn = transitionCenter.relative(expandSide);
        BlockPos storageBlocksStart = firstStorageColumn.relative(top);
        BlockPos storageBlocksEnd = expandTowards(
                level,
                expandSide,
                firstStorageColumn.relative(down),
                ((state, pos) -> isDriveFacingFront(state, front))
        );
        if (!validateBlocks(
                level,
                storageBlocksStart,
                storageBlocksEnd,
                state -> isDriveFacingFront(state, front)
        )) {
            return false;
        }

        // vent row behind the drives, facing back
        BlockPos ventStart = firstStorageColumn.relative(back);
        Optional<BlockPos> ventEndResult = validateBlockLine(
                level,
                expandSide,
                ventStart,
                (state, pos) -> state.is(ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get())
                        && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == back
        );
        if (ventEndResult.isEmpty()) {
            return false;
        }
        BlockPos ventEnd = ventEndResult.get();

        // energy cell rows above and below the vent, facing back
        BlockPos upperEnergyCellStart = firstStorageColumn.relative(back).relative(top);
        BlockPos upperEnergyCellEnd = energyCellLineEnd(level, expandSide, upperEnergyCellStart, back);
        if (upperEnergyCellEnd == null) {
            return false;
        }
        BlockPos lowerEnergyCellStart = firstStorageColumn.relative(back).relative(down);
        BlockPos lowerEnergyCellEnd = energyCellLineEnd(level, expandSide, lowerEnergyCellStart, back);
        if (lowerEnergyCellEnd == null) {
            return false;
        }

        // tail casings
        BlockPos.MutableBlockPos tailCasing = storageBlocksEnd.mutable().move(expandSide).move(top);
        List<BlockPos> tailCasingPoses = List.of(
                upperEnergyCellEnd.relative(expandSide),
                lowerEnergyCellEnd.relative(expandSide),
                ventEnd.relative(expandSide),
                tailCasing.immutable(),
                tailCasing.relative(top),
                tailCasing.relative(down)
        );
        if (!ensureSameSurface(tailCasingPoses)) {
            return false;
        }
        for (BlockPos pos : tailCasingPoses) {
            if (!level.getBlockState(pos).is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get())) {
                return false;
            }
        }
        return true;
    }

    private boolean isDriveFacingFront(BlockState state, Direction front) {
        return state.is(ModRegistration.SIMPLIFY_DRIVE_BLOCK.get())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == front;
    }

    /**
     * Validates a horizontal line of energy cells; returns the end of the line,
     * or {@code null} when the start position is invalid.
     */
    private BlockPos energyCellLineEnd(ServerLevel level, Direction expandSide, BlockPos start, Direction back) {
        if (!validateBlock(level, start, state -> isSupportedEnergyCell(state, back))) {
            return null;
        }
        BlockPos end = expandTowards(level, expandSide, start, state -> isSupportedEnergyCell(state, back));
        return end;
    }

    private boolean isSupportedEnergyCell(BlockState state, Direction back) {
        if (!(state.getBlock() instanceof SimplifyEnergyCellBlock cell)) {
            return false;
        }
        if (state.getValue(SimplifyEnergyCellBlock.FACING) != back) {
            return false;
        }
        // structure is only validated for the L1 host, whose tier is index 0;
        // only L1 energy cells (also index 0) are supported.
        return true;
    }

    private boolean validateCasingColumn(ServerLevel level, BlockPos centerPos, Direction top, Direction down) {
        return validateBlock(level, centerPos, state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))
                && validateBlock(level, centerPos.relative(top), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()))
                && validateBlock(level, centerPos.relative(down), state -> state.is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get()));
    }

    // ------------------------------------------------------------------
    //  Diagnostics: on failure the controller prints which cells are wrong.
    // ------------------------------------------------------------------

    /** Empty when the structure is valid; otherwise a list of concrete issues. */
    public static List<String> diagnose(ServerLevel level, SimplifyStorageHostBlockEntity controller) {
        BlockPos controllerPos = controller.getBlockPos();
        BlockState cs = controller.getBlockState();
        IOrientationStrategy strategy = OrientationStrategies.horizontalFacing();
        Direction back = strategy.getSide(cs, RelativeSide.BACK);
        Direction front = back.getOpposite();
        Direction top = strategy.getSide(cs, RelativeSide.TOP);
        Direction down = top.getOpposite();
        Direction left = strategy.getSide(cs, RelativeSide.RIGHT);
        Direction right = left.getOpposite();

        List<String> issues = new ArrayList<>();
        if (checkFull(level, controllerPos, front, back, top, down, left, right, issues)) {
            return List.of();
        }
        List<String> mirrored = new ArrayList<>();
        if (checkFull(level, controllerPos, front, back, top, down, right, left, mirrored)) {
            return List.of();
        }
        return mirrored.size() < issues.size() ? mirrored : issues;
    }

    private static boolean checkFull(
            ServerLevel level,
            BlockPos controllerPos,
            Direction front,
            Direction back,
            Direction top,
            Direction down,
            Direction staticSide,
            Direction expandSide,
            List<String> issues
    ) {
        boolean[] ok = {true};

        // helper that verifies a single expected block
        java.util.function.BiFunction<BlockPos, Block, Boolean> expect = (pos, block) -> {
            if (level.getBlockState(pos).is(block)) {
                return true;
            }
            ok[0] = false;
            addIssue(issues, controllerPos, pos, block, level);
            return false;
        };
        // casing columns: [center, top, down] around an anchor
        java.util.function.Function<BlockPos, Boolean> expectCasingColumn = anchor -> {
            Block block = ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get();
            boolean good = true;
            good &= expect.apply(anchor, block);
            good &= expect.apply(anchor.relative(top), block);
            good &= expect.apply(anchor.relative(down), block);
            return good;
        };

        expectCasingColumn.apply(controllerPos.relative(staticSide));
        expectCasingColumn.apply(controllerPos.relative(back));
        // interface at back of static side (+ casing above/below)
        BlockPos interfacePos = controllerPos.relative(staticSide).relative(back);
        expect.apply(interfacePos, ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get());
        expect.apply(interfacePos.relative(top), ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        expect.apply(interfacePos.relative(down), ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        expect.apply(controllerPos.relative(top), ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        expect.apply(controllerPos.relative(down), ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());

        BlockPos transitionCenter = controllerPos.relative(expandSide);
        expectCasingColumn.apply(transitionCenter);
        expectCasingColumn.apply(transitionCenter.relative(back));

        // drive columns (3-high stacks) extending along expandSide
        BlockPos firstStorageColumn = transitionCenter.relative(expandSide);
        int columnIndex = 1;
        while (isDriveColumn(level, columnAt(controllerPos, firstStorageColumn, expandSide, columnIndex, top, down), front)) {
            columnIndex++;
        }
        int driveColumns = columnIndex - 1;
        if (driveColumns < 1) {
            ok[0] = false;
            BlockPos mid = firstStorageColumn;
            addIssue(issues, controllerPos, mid, ModRegistration.SIMPLIFY_DRIVE_BLOCK.get(), level);
            if (level.getBlockState(mid.relative(top)).is(ModRegistration.SIMPLIFY_DRIVE_BLOCK.get())
                    || level.getBlockState(mid).is(ModRegistration.SIMPLIFY_DRIVE_BLOCK.get())
                    || level.getBlockState(mid.relative(down)).is(ModRegistration.SIMPLIFY_DRIVE_BLOCK.get())) {
                issues.add("驱动器堆叠必须在同一 x 位置从上到下 3 台，且 facing=" + front.getName()
                        + "（你似乎把驱动器摆成单台/不同朝向）");
            }
        } else {
            for (int k = 1; k <= driveColumns; k++) {
                BlockPos colMid = columnAt(controllerPos, firstStorageColumn, expandSide, k, top, down);
                Block block = ModRegistration.SIMPLIFY_DRIVE_BLOCK.get();
                if (!level.getBlockState(colMid.relative(top)).is(block)
                        || !level.getBlockState(colMid).is(block)
                        || !level.getBlockState(colMid.relative(down)).is(block)) {
                    ok[0] = false;
                    addIssue(issues, controllerPos, colMid, block, level);
                } else {
                    // verify facing
                    if (level.getBlockState(colMid).getValue(BlockStateProperties.HORIZONTAL_FACING) != front
                            || level.getBlockState(colMid.relative(top)).getValue(BlockStateProperties.HORIZONTAL_FACING) != front
                            || level.getBlockState(colMid.relative(down)).getValue(BlockStateProperties.HORIZONTAL_FACING) != front) {
                        ok[0] = false;
                        issues.add(rel(controllerPos, colMid)
                                + " 驱动器 facing 应为 " + front.getName()
                                + "，请用扳手/潜行调整朝向（或朝反方向摆）");
                    }
                }
                // vent behind + energy cells above/below per drive column
                BlockPos ventPos = colMid.relative(back);
                if (level.getBlockState(ventPos).is(ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get())
                        && level.getBlockState(ventPos).getValue(BlockStateProperties.HORIZONTAL_FACING) == back) {
                    // ok
                } else {
                    ok[0] = false;
                    addIssue(issues, controllerPos, ventPos, ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get(), level);
                }
                BlockPos energyTop = ventPos.relative(top);
                BlockPos energyDown = ventPos.relative(down);
                if (!(level.getBlockState(energyTop).getBlock() instanceof SimplifyEnergyCellBlock)) {
                    ok[0] = false;
                    addIssue(issues, controllerPos, energyTop, ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get(), level);
                } else if (level.getBlockState(energyTop).getValue(SimplifyEnergyCellBlock.FACING) != back) {
                    ok[0] = false;
                    issues.add(rel(controllerPos, energyTop) + " 储能元件 facing 应为 " + back.getName());
                }
                if (!(level.getBlockState(energyDown).getBlock() instanceof SimplifyEnergyCellBlock)) {
                    ok[0] = false;
                    addIssue(issues, controllerPos, energyDown, ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get(), level);
                } else if (level.getBlockState(energyDown).getValue(SimplifyEnergyCellBlock.FACING) != back) {
                    ok[0] = false;
                    issues.add(rel(controllerPos, energyDown) + " 储能元件 facing 应为 " + back.getName());
                }
            }
        }

        // tail casing (one step past the last drive column, all 6 cells)
        if (driveColumns >= 1) {
            BlockPos lastMid = columnAt(controllerPos, firstStorageColumn, expandSide, driveColumns, top, down);
            // plus the z=-1 side cells of the tail face
            BlockPos tailAnchor = lastMid.relative(expandSide);
            List<BlockPos> tailAll = List.of(
                    tailAnchor.relative(back),
                    tailAnchor.relative(back).relative(top),
                    tailAnchor.relative(back).relative(down),
                    tailAnchor,
                    tailAnchor.relative(top),
                    tailAnchor.relative(down));
            for (BlockPos pos : tailAll) {
                if (!level.getBlockState(pos).is(ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get())) {
                    ok[0] = false;
                    addIssue(issues, controllerPos, pos, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get(), level);
                }
            }
        }
        return ok[0];
    }

    private static boolean isDriveColumn(ServerLevel level, BlockPos mid, Direction front) {
        Block drive = ModRegistration.SIMPLIFY_DRIVE_BLOCK.get();
        BlockState topState = level.getBlockState(mid.relative(net.minecraft.core.Direction.UP));
        BlockState midState = level.getBlockState(mid);
        BlockState downState = level.getBlockState(mid.relative(net.minecraft.core.Direction.DOWN));
        return topState.is(drive) && midState.is(drive) && downState.is(drive)
                && topState.getValue(BlockStateProperties.HORIZONTAL_FACING) == front
                && midState.getValue(BlockStateProperties.HORIZONTAL_FACING) == front
                && downState.getValue(BlockStateProperties.HORIZONTAL_FACING) == front;
    }

    /** column mid position for index k (1-based) along expandSide from firstStorageColumn. */
    private static BlockPos columnAt(BlockPos controllerPos, BlockPos firstColumn, Direction expandSide, int k,
                                     Direction top, Direction down) {
        return firstColumn.relative(expandSide, k - 1);
    }

    private static void addIssue(List<String> issues, BlockPos origin, BlockPos pos, Block expected,
                                 ServerLevel level) {
        issues.add(rel(origin, pos) + " 应为 " + expected.getDescriptionId() + "，实际 "
                + level.getBlockState(pos).getBlock().getDescriptionId()
                + (level.getBlockState(pos).isAir() ? " (空气)" : ""));
    }

    private static String rel(BlockPos origin, BlockPos pos) {
        return "(" + (pos.getX() - origin.getX()) + "," + (pos.getY() - origin.getY()) + ","
                + (pos.getZ() - origin.getZ()) + ")";
    }

    /**
     * Positions where the host controller can sit inside the 3x(2..N) bounds.
     * Mirrors eco's {@code MultiBlockUtil.allPossibleController}.
     */
    private static Iterable<BlockPos> allPossibleController(BlockPos min, BlockPos max) {
        int xSize = max.getX() - min.getX() + 1;
        int zSize = max.getZ() - min.getZ() + 1;
        int yCenter = min.getY() + 1;

        if (xSize > zSize && zSize == 2) {
            int xLow = min.getX() + 1;
            int xHigh = max.getX() - 1;
            return List.of(
                    new BlockPos(xLow, yCenter, min.getZ()),
                    new BlockPos(xLow, yCenter, max.getZ()),
                    new BlockPos(xHigh, yCenter, min.getZ()),
                    new BlockPos(xHigh, yCenter, max.getZ())
            );
        }
        if (zSize > xSize && xSize == 2) {
            int zLow = min.getZ() + 1;
            int zHigh = max.getZ() - 1;
            return List.of(
                    new BlockPos(min.getX(), yCenter, zLow),
                    new BlockPos(max.getX(), yCenter, zLow),
                    new BlockPos(min.getX(), yCenter, zHigh),
                    new BlockPos(max.getX(), yCenter, zHigh)
            );
        }
        return List.of();
    }
}

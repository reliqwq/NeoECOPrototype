package cn.dancingsnow.neoecoprototype.multiblock.calculator;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationThreadingCoreBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEComputationClusterCalculator;
import cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Optional;

/** L1 computation geometry using the addon block set and eco's CPU cluster. */
public class SimplifyComputationClusterCalculator extends NEComputationClusterCalculator {
    private static final int MAX_LENGTH = SimplifyMultiblockConfig.L1_MAX_LENGTH;

    /** Side-effect-free result of the computation geometry check. */
    public record StructureValidation(boolean valid, boolean mirrored,
                                      BlockPos controllerPos, BlockPos coolerPos) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos, null);
        }
    }

    public SimplifyComputationClusterCalculator(NEBlockEntity<NEComputationCluster, ?> blockEntity) {
        super(blockEntity);
    }

    @Override
    protected int maxLength() {
        return MAX_LENGTH;
    }

    @Override
    protected Holder<Block> casing() {
        return BuiltInRegistries.BLOCK.wrapAsHolder(ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get());
    }

    @Override
    public NEComputationCluster createCluster(ServerLevel level, BlockPos min, BlockPos max) {
        return new NEComputationCluster(min, max);
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        applyValidationState(level, result);
        return result.valid();
    }

    /** Reads the world and returns the geometry result without mutating block entities. */
    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        Optional<ControllerContext<ECOComputationSystemBlockEntity>> contextResult = findUniqueController(
                level, min, max, ECOComputationSystemBlockEntity.class);
        if (contextResult.isEmpty()) {
            return StructureValidation.invalid(null);
        }

        ControllerContext<ECOComputationSystemBlockEntity> context = contextResult.orElseThrow();
        ECOComputationSystemBlockEntity controller = context.controller();
        BlockPos controllerPos = context.position();
        IECOTier tier = controller.getTier();
        Direction front = context.front();
        Direction back = context.back();
        Direction top = context.top();
        Direction down = context.down();
        Direction left = context.left();
        Direction right = context.right();

        Optional<BlockPos> coolerPos = verifyStructure(
                level, controllerPos, tier, front, back, top, down, right, left);
        if (coolerPos.isPresent()) {
            return new StructureValidation(true, false, controllerPos, coolerPos.get());
        }
        coolerPos = verifyStructure(level, controllerPos, tier, front, back, top, down, left, right);
        if (coolerPos.isPresent()) {
            return new StructureValidation(true, true, controllerPos, coolerPos.get());
        }
        return StructureValidation.invalid(controllerPos);
    }

    private void applyValidationState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() != null
                && level.getBlockEntity(result.controllerPos()) instanceof ECOComputationSystemBlockEntity controller) {
            controller.setMirrored(result.mirrored());
        }
        if (result.coolerPos() != null
                && level.getBlockEntity(result.coolerPos()) instanceof ECOComputationCoolingControllerBlockEntity cooler) {
            cooler.setMirrored(result.mirrored());
        }
    }

    private Optional<BlockPos> verifyStructure(ServerLevel level, BlockPos controllerPos, IECOTier tier,
                                                Direction front, Direction back, Direction top, Direction down,
                                                Direction interfaceSide, Direction expandSide) {
        if (!validateShell(level, controllerPos, top, down, interfaceSide)
                || !validateShell(level, controllerPos, top, down, expandSide)
                || !validateShell(level, controllerPos, top, down, back)
                || !validateShell(level, controllerPos.relative(back).relative(expandSide), top, down)) {
            return Optional.empty();
        }

        BlockPos interfacePos = controllerPos.relative(back).relative(interfaceSide);
        if (!validateComputationInterface(level, interfacePos, top, down)) {
            return Optional.empty();
        }

        BlockPos connectorStart = controllerPos.relative(expandSide).relative(expandSide);
        Optional<BlockPos> connectorEndResult = validateBlockLine(level, expandSide, connectorStart,
                matchingStateFacing(holder(ModRegistration.SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get()), front));
        if (connectorEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos connectorEnd = connectorEndResult.orElseThrow();

        BlockPos threadingCoreStart = connectorStart.relative(back);
        Optional<BlockPos> threadingCoreEndResult = validateBlockLine(level, expandSide, threadingCoreStart,
                matchingThreadingCore(level, tier, back, threadingCoreStart));
        if (threadingCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos threadingCoreEnd = threadingCoreEndResult.orElseThrow();

        BlockPos upperParallelCoreStart = threadingCoreStart.relative(top);
        Optional<BlockPos> upperParallelCoreEndResult = validateBlockLine(level, expandSide, upperParallelCoreStart,
                matchingParallelCore(level, tier, back));
        if (upperParallelCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos upperParallelCoreEnd = upperParallelCoreEndResult.orElseThrow();

        BlockPos lowerParallelCoreStart = threadingCoreStart.relative(down);
        Optional<BlockPos> lowerParallelCoreEndResult = validateBlockLine(level, expandSide, lowerParallelCoreStart,
                matchingParallelCore(level, tier, back));
        if (lowerParallelCoreEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos lowerParallelCoreEnd = lowerParallelCoreEndResult.orElseThrow();

        BlockPos upperDriveStart = connectorStart.relative(top);
        Optional<BlockPos> upperDriveEndResult = validateBlockLine(level, expandSide, upperDriveStart,
                matchingStateFacing(holder(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get()), front));
        if (upperDriveEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos upperDriveEnd = upperDriveEndResult.orElseThrow();

        BlockPos lowerDriveStart = connectorStart.relative(down);
        Optional<BlockPos> lowerDriveEndResult = validateBlockLine(level, expandSide, lowerDriveStart,
                matchingStateFacing(holder(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get()), front));
        if (lowerDriveEndResult.isEmpty()) {
            return Optional.empty();
        }
        BlockPos lowerDriveEnd = lowerDriveEndResult.orElseThrow();

        List<BlockPos> tails = List.of(connectorEnd, threadingCoreEnd, upperDriveEnd,
                lowerDriveEnd, upperParallelCoreEnd, lowerParallelCoreEnd);
        if (!ensureSameSurface(tails)) {
            return Optional.empty();
        }

        List<BlockPos> tailCasings = List.of(
                threadingCoreEnd.relative(expandSide),
                upperDriveEnd.relative(expandSide),
                lowerDriveEnd.relative(expandSide),
                upperParallelCoreEnd.relative(expandSide),
                lowerParallelCoreEnd.relative(expandSide));

        BlockPos coolerPos = connectorEnd.relative(expandSide);
        if (!validateBlock(level, coolerPos, matchingCoolingController(level, tier, expandSide), coolerPos)
                || !tailCasings.stream().allMatch(tail -> isShell(level.getBlockState(tail)))) {
            return Optional.empty();
        }
        return Optional.of(coolerPos);
    }

    /**
     * One shell column: the cell at {@code pos} plus the cells above and below it. Mirrors eco's
     * {@code validateCasing}, which is final and pinned to {@link #casing()}, so that the energized core
     * can stand in for a casing block the way eco lets its network switch do.
     */
    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down) {
        return isShell(level.getBlockState(pos))
                && isShell(level.getBlockState(pos.relative(top)))
                && isShell(level.getBlockState(pos.relative(down)));
    }

    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down,
                                  Direction side) {
        return validateShell(level, pos.relative(side), top, down);
    }

    /**
     * A shell cell is the casing or an energized core. The core may only do this because it hides
     * itself once the structure forms ({@code SimplifyEnergizedComputationCoreBlock.hideWhenFormed}),
     * exactly like eco's {@code NENetworkSwitchBlock}. Draw anything here and it flickers: every
     * formed casing is {@code RenderShape.INVISIBLE} and stops culling neighbours, so the shell is
     * transparent and a member in it is seen from both sides at once.
     */
    private boolean isShell(BlockState state) {
        return state.is(casing())
                || state.is(holder(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get()));
    }

    private boolean validateComputationInterface(ServerLevel level, BlockPos interfacePos,
                                                  Direction top, Direction down) {
        return validateInterface(level, interfacePos, top, down,
                holder(ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get()), casing())
                || validateInterface(level, interfacePos, top, down,
                holder(ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get()), casing());
    }

    private static Holder<Block> holder(Block block) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(block);
    }

    /**
     * The threading core line. The energized core is only accepted at {@code firstPos} -- the cell
     * nearest the controller -- which is what caps a structure at exactly one of them without any
     * counting state: every other cell of the line rejects it and the line then fails to reach the
     * tail casing.
     */
    private static java.util.function.BiPredicate<BlockState, BlockPos> matchingThreadingCore(
            ServerLevel level, IECOTier tier, Direction facing, BlockPos firstPos) {
        return (state, pos) -> (
                        state.is(holder(ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get()))
                        || (pos.equals(firstPos)
                                && state.is(holder(ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get()))))
                && level.getBlockEntity(pos) instanceof ECOComputationThreadingCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    /**
     * The parallel core column takes the plain L1 core or the energized core. They are separate Block
     * instances, so both have to be named here; whether the host accepts what the one that is standing
     * there actually reports is still {@code supportsComponentTier}'s call.
     *
     * <p>Only the plain core has to face {@code back} -- that is eco's own rule. The energized core is
     * one block a player drops in by hand and its facing changes nothing about what it contributes
     * (the cluster just sums {@code getCPUAccelerators()}), so demanding a facing there only made
     * otherwise-correct structures fail to form.
     *
     * <p>The energized core may not stand in the shell instead: eco renders every formed casing with
     * {@code RenderShape.INVISIBLE} and stops it culling its neighbours, so a member in a shell cell
     * can be seen through from inside and flickers. eco's own answer there is to draw nothing.
     */
    private static java.util.function.BiPredicate<BlockState, BlockPos> matchingParallelCore(
            ServerLevel level, IECOTier tier, Direction facing) {
        return (state, pos) -> (
                        (state.is(holder(ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get()))
                                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing)
                        || state.is(holder(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get())))
                && level.getBlockEntity(pos) instanceof ECOComputationParallelCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier());
    }

    private static java.util.function.BiPredicate<BlockState, BlockPos> matchingCoolingController(
            ServerLevel level, IECOTier tier, Direction facing) {
        return (state, pos) -> state.is(holder(ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get()))
                && level.getBlockEntity(pos) instanceof ECOComputationCoolingControllerBlockEntity cooler
                && tier.supportsComponentTier(cooler.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }
}

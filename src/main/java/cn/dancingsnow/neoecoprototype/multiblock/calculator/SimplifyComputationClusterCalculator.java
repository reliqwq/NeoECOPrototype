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
                                      BlockPos controllerPos, BlockPos coolerPos,
                                      boolean communicationInterface) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos, null, false);
        }
    }

    /** What {@link #verifyStructure} reports when one hand of the machine checks out. */
    private record Formed(BlockPos coolerPos, boolean communicationInterface) { }

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
        // The one cell an energized core may stand in for a casing. It sits directly behind the host, so
        // it is the same cell for both hands and for a mirrored build - which is also why it no longer
        // needs a per-hand argument.
        net.minecraft.world.level.block.state.BlockState controllerState = level.getBlockState(controllerPos);
        BlockPos energizedCoreCell = energizedCoreCell(controllerPos, controllerState);

        Optional<Formed> formed = verifyStructure(
                level, controllerPos, tier, front, back, top, down, right, left, energizedCoreCell, min, max);
        if (formed.isPresent()) {
            var shape = formed.get();
            return new StructureValidation(true, false, controllerPos, shape.coolerPos(),
                    shape.communicationInterface());
        }
        formed = verifyStructure(level, controllerPos, tier, front, back, top, down, left, right,
                energizedCoreCell, min, max);
        if (formed.isPresent()) {
            var shape = formed.get();
            return new StructureValidation(true, true, controllerPos, shape.coolerPos(),
                    shape.communicationInterface());
        }
        return StructureValidation.invalid(controllerPos);
    }

    private void applyValidationState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() != null
                && level.getBlockEntity(result.controllerPos()) instanceof ECOComputationSystemBlockEntity controller) {
            controller.setMirrored(result.mirrored());
            // The shape goes to the block rather than being written here: this stack is inside AE2's
            // cluster recalculation, where a block change would wedge every multiblock.
            if (level.getBlockState(result.controllerPos()).getBlock()
                    instanceof cn.dancingsnow.neoecoprototype.block.computation
                            .SimplifyComputationSystemBlock host) {
                host.publishShape(level, result.controllerPos(), result.communicationInterface());
            }
        }
        if (result.coolerPos() != null
                && level.getBlockEntity(result.coolerPos()) instanceof ECOComputationCoolingControllerBlockEntity cooler) {
            cooler.setMirrored(result.mirrored());
        }
    }

    private Optional<Formed> verifyStructure(ServerLevel level, BlockPos controllerPos, IECOTier tier,
                                                Direction front, Direction back, Direction top, Direction down,
                                                Direction interfaceSide, Direction expandSide,
                                                BlockPos energizedCorePos, BlockPos min, BlockPos max) {
        if (!validateShell(level, controllerPos, top, down, interfaceSide, energizedCorePos)
                || !validateShell(level, controllerPos, top, down, expandSide, energizedCorePos)
                || !validateShell(level, controllerPos, top, down, back, energizedCorePos)
                || !validateShell(level, controllerPos.relative(back).relative(expandSide), top, down,
                        energizedCorePos)) {
            return Optional.empty();
        }

        BlockPos interfacePos = controllerPos.relative(back).relative(interfaceSide);
        Optional<Block> matchedInterface = matchedComputationInterface(level, interfacePos, top, down);
        if (matchedInterface.isEmpty()) {
            return Optional.empty();
        }
        boolean communicationInterface = matchedInterface.get()
                == ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get();

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
                || !tailCasings.stream().allMatch(tail -> level.getBlockState(tail).is(casing()))) {
            return Optional.empty();
        }
        // The host stands in a column no shell walk visits, so the two cells above and below it are the
        // only cells of the structure nothing above looks at. They sit inside the bounds the cluster is
        // built from, so whatever stands there gets adopted without ever passing a geometry test - which
        // is how a threading core, or a second energized core, used to join the machine from a position
        // no build plan can produce. The column is shell like every other column, so it is casing.
        for (Direction lift : new Direction[]{top, down}) {
            if (!level.getBlockState(controllerPos.relative(lift)).is(casing())) {
                return Optional.empty();
            }
        }
        // Whether the machine carries an energized member is no longer reported anywhere: both of them
        // draw themselves once formed, so the only consumer would have been the host's block state, and
        // the host's formed face looks the same either way. The allotted cell above still has to be
        // checked, because that is what holds the structure to at most one energized core.
        return Optional.of(new Formed(coolerPos, communicationInterface));
    }

    /**
     * One shell column: the cell at {@code pos} plus the cells above and below it. Mirrors eco's
     * {@code validateCasing}, which is final and pinned to {@link #casing()}.
     *
     * @param energizedCorePos the one cell where an energized core may stand in for a casing, or
     *                         {@code null} where the column has to be all casing
     */
    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down,
                                  BlockPos energizedCorePos) {
        return isShell(level.getBlockState(pos), pos, energizedCorePos)
                && isShell(level.getBlockState(pos.relative(top)), pos.relative(top), energizedCorePos)
                && isShell(level.getBlockState(pos.relative(down)), pos.relative(down), energizedCorePos);
    }

    private boolean validateShell(ServerLevel level, BlockPos pos, Direction top, Direction down,
                                  Direction side, BlockPos energizedCorePos) {
        return validateShell(level, pos.relative(side), top, down, energizedCorePos);
    }

    /**
     * The one cell an energized core may occupy, for a host at {@code controllerPos}: directly behind
     * it. Read through AE2's orientation API so it means "behind the host" from whichever way the
     * machine faces, and identical for a mirrored build.
     *
     * <p>Two reasons this cell rather than the shell column beside the host, which is where it used to
     * go: the host's formed model covers that whole 3x3 face, so a member cube beside the host is
     * strictly coplanar with it and the two quads fight over depth - the core had to stop drawing
     * itself to stop flickering. Behind the host is a different plane, so it can draw its own (and
     * animated) look. And the side column is exactly where eco puts its own network switch
     * ({@code NENetworkSwitchUtil.switchPosition}), so a core standing there made eco report
     * {@code high_energy_network_switch=true} on a machine that has no switch.
     *
     * <p>Public and shared with the game tests so a test cannot pass by agreeing with a wrong reading
     * of the rule.
     */
    public static BlockPos energizedCoreCell(BlockPos controllerPos,
                                             net.minecraft.world.level.block.state.BlockState controllerState) {
        return controllerPos.relative(appeng.api.orientation.IOrientationStrategy
                .get(controllerState).getSide(controllerState, appeng.api.orientation.RelativeSide.BACK));
    }

    /**
     * A shell cell is the casing, or the energized core in the single cell allotted to it. The core
     * belongs in the shell at all: once the structure forms it swaps to a model that carries only the
     * lit face, like every other member of the shell.
     *
     * <p>Allotting one cell is also how "exactly one" gets enforced: the geometry refuses the block
     * everywhere else, so there is nothing to count afterwards.
     */
    private boolean isShell(BlockState state, BlockPos pos, BlockPos energizedCorePos) {
        return state.is(casing())
                || (pos.equals(energizedCorePos)
                        && state.is(holder(ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get())));
    }

    /** Which of the two interface kinds occupies the interface cell, if either does. */
    private Optional<Block> matchedComputationInterface(ServerLevel level, BlockPos interfacePos,
                                                        Direction top, Direction down) {
        Block communication = ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get();
        Block network = ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get();
        if (validateInterface(level, interfacePos, top, down, holder(communication), casing())) {
            return Optional.of(communication);
        }
        if (validateInterface(level, interfacePos, top, down, holder(network), casing())) {
            return Optional.of(network);
        }
        return Optional.empty();
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
     * The parallel core column takes the plain L1 core, facing {@code back} -- that is eco's own rule.
     *
     * <p>The energized core is deliberately not accepted here even though it is a parallel core as far
     * as the cluster is concerned. It has exactly one allotted cell, in the shell (see
     * {@link #isShell}); a second place that takes it would mean counting afterwards to hold the
     * "only one" promise, and geometry that refuses the block everywhere else needs no counting.
     */
    private static java.util.function.BiPredicate<BlockState, BlockPos> matchingParallelCore(
            ServerLevel level, IECOTier tier, Direction facing) {
        return (state, pos) -> state.is(holder(ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get()))
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing
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

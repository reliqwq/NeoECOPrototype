package cn.dancingsnow.neoecoprototype.multiblock.calculator;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import cn.dancingsnow.neoecoae.config.NEConfig;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.stream.Stream;

/** L1 crafting geometry using the addon block set and Eco's crafting cluster. */
public class SimplifyCraftingClusterCalculator extends NECraftingClusterCalculator {
    private static final Logger LOGGER = LoggerFactory.getLogger("neoecoprototype-crafting");

    /** Side-effect-free result of the crafting geometry check. */
    public record StructureValidation(boolean valid, boolean mirrored, BlockPos controllerPos) {
        public static StructureValidation invalid(BlockPos controllerPos) {
            return new StructureValidation(false, false, controllerPos);
        }
    }

    public SimplifyCraftingClusterCalculator(NEBlockEntity<NECraftingCluster, ?> target) {
        super(target);
    }

    @Override
    protected int maxLength() {
        return NEConfig.craftingSystemMaxLength;
    }

    @Override
    protected Holder<Block> casing() {
        return BuiltInRegistries.BLOCK.wrapAsHolder(ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get());
    }

    @Override
    public boolean verifyInternalStructure(ServerLevel level, BlockPos min, BlockPos max) {
        StructureValidation result = validateStructure(level, min, max);
        applyMirroredState(level, result);
        // Successful checks run frequently while the eco cluster is alive. Formation and
        // destruction are already logged by the cluster/host, so do not emit one INFO line
        // per validation tick here. Keep failures available only to debug logging while
        // diagnosing a malformed structure.
        if (!result.valid()) {
            LOGGER.debug("L1 crafting check failed: controller={} bounds={}..{}",
                    result.controllerPos(), min, max);
        }
        return result.valid();
    }

    /** Reads the world and returns the geometry result without mutating the controller. */
    public StructureValidation validateStructure(ServerLevel level, BlockPos min, BlockPos max) {
        Optional<ControllerContext<ECOCraftingSystemBlockEntity>> result = findUniqueController(
                level, min, max, ECOCraftingSystemBlockEntity.class);
        if (result.isEmpty()) {
            return StructureValidation.invalid(null);
        }

        ControllerContext<ECOCraftingSystemBlockEntity> context = result.orElseThrow();
        BlockPos controllerPos = context.position();
        ECOCraftingSystemBlockEntity controller = context.controller();
        Direction interfaceSide = context.right();
        Direction expandSide = context.left();
        if (verifyStructure(level, controllerPos, controller.getTier(), context.front(), context.back(),
                context.top(), context.down(), interfaceSide, expandSide)) {
            return new StructureValidation(true, false, controllerPos);
        }
        if (verifyStructure(level, controllerPos, controller.getTier(), context.front(), context.back(),
                context.top(), context.down(), expandSide, interfaceSide)) {
            return new StructureValidation(true, true, controllerPos);
        }
        return StructureValidation.invalid(controllerPos);
    }

    private void applyMirroredState(ServerLevel level, StructureValidation result) {
        if (result.controllerPos() != null
                && level.getBlockEntity(result.controllerPos()) instanceof ECOCraftingSystemBlockEntity controller) {
            controller.setMirrored(result.mirrored());
        }
    }

    private boolean verifyStructure(ServerLevel level, BlockPos controllerPos, IECOTier tier,
                                    Direction front, Direction back, Direction top, Direction down,
                                    Direction interfaceSide, Direction expandSide) {
        if (!validateCasing(level, controllerPos, top, down, interfaceSide)
                || !validateCasing(level, controllerPos, top, down, expandSide)
                || !validateCasing(level, controllerPos, top, down, back)
                || !validateCasing(level, controllerPos.relative(back).relative(expandSide), top, down)) {
            return false;
        }

        BlockPos interfacePos = controllerPos.relative(back).relative(interfaceSide);
        if (!validateBlock(level, interfacePos, this::isCraftingInterface)
                || !validateBlock(level, interfacePos.relative(top), state ->
                state.is(holder(ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get())))
                || !validateBlock(level, interfacePos.relative(down), state ->
                state.is(holder(ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get())))) {
            return false;
        }

        BlockPos workerStart = controllerPos.relative(expandSide).relative(expandSide);
        Optional<BlockPos> workerEnd = validateBlockLine(level, expandSide, workerStart,
                matchingFacing(holder(ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get()), front));
        Optional<BlockPos> upperCoreEnd = validateBlockLine(level, expandSide, workerStart.relative(top),
                matchingParallelCore(level, tier, front));
        Optional<BlockPos> lowerCoreEnd = validateBlockLine(level, expandSide, workerStart.relative(down),
                matchingParallelCore(level, tier, front));
        BlockPos ventStart = workerStart.relative(back);
        Optional<BlockPos> ventEnd = validateBlockLine(level, expandSide, ventStart,
                matchingFacing(holder(ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get()), back));
        Optional<BlockPos> upperBusEnd = validateBlockLine(level, expandSide, ventStart.relative(top),
                matchingFacing(holder(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()), back));
        Optional<BlockPos> lowerBusEnd = validateBlockLine(level, expandSide, ventStart.relative(down),
                matchingFacing(holder(ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()), back));

        if (workerEnd.isEmpty() || upperCoreEnd.isEmpty() || lowerCoreEnd.isEmpty()
                || ventEnd.isEmpty() || upperBusEnd.isEmpty() || lowerBusEnd.isEmpty()) {
            return false;
        }

        var endCasing = Stream.of(workerEnd.orElseThrow(), upperCoreEnd.orElseThrow(), lowerCoreEnd.orElseThrow(),
                ventEnd.orElseThrow(), upperBusEnd.orElseThrow(), lowerBusEnd.orElseThrow())
                .map(pos -> pos.relative(expandSide)).toList();
        return ensureSameSurface(endCasing)
                && endCasing.stream().allMatch(pos -> level.getBlockState(pos)
                .is(holder(ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get())));
    }

    private boolean isCraftingInterface(BlockState state) {
        return state.is(holder(ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get()))
                || state.is(holder(ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get()));
    }

    private static BiPredicate<BlockState, BlockPos> matchingFacing(Holder<Block> block, Direction facing) {
        return (state, pos) -> state.is(block)
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    private static BiPredicate<BlockState, BlockPos> matchingParallelCore(
            Level level, IECOTier tier, Direction facing) {
        return (state, pos) -> state.is(holder(ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get()))
                && level.getBlockEntity(pos) instanceof ECOCraftingParallelCoreBlockEntity core
                && tier.supportsComponentTier(core.getTier())
                && state.getValue(BlockStateProperties.HORIZONTAL_FACING) == facing;
    }

    private static Holder<Block> holder(Block block) {
        return BuiltInRegistries.BLOCK.wrapAsHolder(block);
    }

    @Override
    public boolean isValidBlockEntity(BlockEntity blockEntity) {
        return blockEntity instanceof NEBlockEntity<?, ?> ne
                && ne.getCalculator() instanceof NECraftingClusterCalculator;
    }
}

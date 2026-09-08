package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingParallelCore;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingPatternBus;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingVent;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingWorker;
import cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.config.NEConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/** Placement definition for the L1 crafting subsystem. */
public final class SimplifyCraftingDefinition {
    public static final MultiBlockDefinition L1 = create();

    private SimplifyCraftingDefinition() {
    }

    private static MultiBlockDefinition create() {
        Holder<Block> owner = BuiltInRegistries.BLOCK.wrapAsHolder(ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get());
        BlockState casing = ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get().defaultBlockState();
        // Parallel cores face the controller front, like Eco's crafting definition.
        BlockState parallel = ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get().defaultBlockState()
                .setValue(ECOCraftingParallelCore.FACING, Direction.NORTH);
        BlockState patternBus = ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get().defaultBlockState()
                .setValue(ECOCraftingPatternBus.FACING, Direction.SOUTH);
        BlockState vent = ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get().defaultBlockState()
                .setValue(ECOCraftingVent.FACING, Direction.SOUTH);

        return MultiBlockDefinition.builder(owner)
                .setBlock(pos(1, 1, 0), ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 0, 0), casing)
                .setBlock(pos(2, 0, 0), casing)
                .setBlock(pos(2, 1, 0), casing)
                .setBlock(pos(1, 2, 0), casing)
                .setBlock(pos(2, 2, 0), casing)
                .setBlock(pos(1, 0, 1), casing)
                .setBlock(pos(2, 0, 1), ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get().defaultBlockState())
                .setBlock(pos(2, 1, 1), ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 1, 1), casing)
                .setBlock(pos(1, 2, 1), casing)
                .setBlock(pos(2, 2, 1), ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get().defaultBlockState())
                .setBlock(pos(0, 0, 0), casing)
                .setBlock(pos(0, 1, 0), casing)
                .setBlock(pos(0, 2, 0), casing)
                .setBlock(pos(0, 0, 1), casing)
                .setBlock(pos(0, 1, 1), casing)
                .setBlock(pos(0, 2, 1), casing)
                .setBlockRepeatable(pos(-1, 1, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 2, 0), Direction.WEST, parallel)
                .setBlockRepeatable(pos(-1, 0, 0), Direction.WEST, parallel)
                .setBlockRepeatable(pos(-1, 0, 1), Direction.WEST, patternBus)
                .setBlockRepeatable(pos(-1, 1, 1), Direction.WEST, vent)
                .setBlockRepeatable(pos(-1, 2, 1), Direction.WEST, patternBus)
                .setBlockWithRepeatShifted(pos(-1, 1, 0), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 2, 0), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 0, 0), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 0, 1), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 1, 1), Direction.WEST, 0, casing)
                .setBlockWithRepeatShifted(pos(-1, 2, 1), Direction.WEST, 0, casing)
                .expandMin(1)
                .expandMax(NEConfig.craftingSystemMaxLength
                        - SimplifyMultiblockConfig.PLACEMENT_BASE_LENGTH_OFFSET)
                .onFormed((blockPos, level) -> {
                    BlockState state = level.getBlockState(blockPos);
                    BlockState formed = state;
                    if (state.hasProperty(NEBlock.FORMED)) {
                        formed = formed.setValue(NEBlock.FORMED, true);
                    }
                    if (formed.hasProperty(ECOMachineCasing.INVISIBLE)) {
                        Vec3 local = blockPos.getCenter();
                        Vec3 controller = new Vec3(1.5, 1.5, 0.5);
                        formed = formed.setValue(ECOMachineCasing.INVISIBLE,
                                local.distanceToSqr(controller) <= 3.0D);
                    }
                    if (formed != state) {
                        level.setBlockAndUpdate(blockPos, formed);
                    }
                })
                // Keep the addon definition out of eco's global list; JEI registers it explicitly.
                .create(definition -> {
                });
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}

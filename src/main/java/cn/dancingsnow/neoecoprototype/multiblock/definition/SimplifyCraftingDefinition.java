package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingParallelCore;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingPatternBus;
import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingVent;
import cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
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
                // Our own constant, not eco's config field: NEConfig.craftingSystemMaxLength is a plain
                // static int that eco only fills when its config loads, so reading it here baked in 0
                // whenever this class initialised first - which is what putting the definition touch in
                // commonSetup did, and F1's build plan came out as expandMin 1 / expandMax -4. The other
                // two L1 definitions already use this constant; eco's default (15) minus the same offset
                // gives the identical 11, so nothing moves at default config.
                .expandMax(SimplifyMultiblockConfig.L1_PLACEMENT_EXPAND_MAX)
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
                // Joins eco's definition list on purpose; see SimplifyStorageDefinition. The old comment
                // here said "JEI registers it explicitly", and that registration was removed - asking for
                // eco's category by name made the whole JEI plugin depend on plugin order.
                // create(Consumer) deliberately does NOT append to NEMultiBlocks.DEFINITIONS, and
                // that matters: touching that field runs eco's own <clinit>, which reads
                // NEConfig.*SystemMaxLength before its server config has loaded, so eco's own L4-L9
                // definitions come out with a build range of 1 .. -4 and their host UI then throws
                // IllegalArgumentException: 1 > -4 on open. Guarded by
                // NeoECOPrototypeGameTests#ecoDefinitionsKeepAUsableBuildRange.
                .create(definition -> {
                });
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}

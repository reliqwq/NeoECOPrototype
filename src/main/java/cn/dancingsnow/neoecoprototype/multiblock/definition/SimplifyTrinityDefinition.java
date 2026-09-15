package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityControllerBlock;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/** Fixed 7x3x7 cross-shaped Trinity placement preview. */
public final class SimplifyTrinityDefinition {
    public static final MultiBlockDefinition L1 = create();

    private SimplifyTrinityDefinition() {
    }

    private static MultiBlockDefinition create() {
        Holder<Block> owner = BuiltInRegistries.BLOCK.wrapAsHolder(
                ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get());
        BlockState casing = ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get().defaultBlockState();
        BlockState controller = ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);
        MultiBlockDefinition.Builder builder = MultiBlockDefinition.builder(owner)
                .setBlock(pos(3, 1, 3), controller);

        // Three-wide central core, with empty corners to make the cross silhouette explicit.
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                for (int y : new int[] {0, 2}) {
                    builder.setBlock(pos(x, y, z), casing);
                }
            }
        }
        // West: storage wing.
        builder.setBlock(pos(2, 1, 3), casing);
        builder.setBlock(pos(1, 0, 3), ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(1, 1, 3), ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(1, 2, 3), ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get().defaultBlockState());
        for (int y = 0; y < 3; y++) {
            builder.setBlock(pos(0, y, 3), ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState());
        }
        // North: computation wing.
        builder.setBlock(pos(3, 0, 1), ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(3, 1, 1), ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(3, 2, 1), ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(3, 1, 0), ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get().defaultBlockState());
        // East: complete crafting wing, including worker, cores, bus, interface and hatches.
        builder.setBlock(pos(4, 1, 3), casing);
        builder.setBlock(pos(4, 0, 2), ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(4, 1, 2), ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(4, 2, 2), ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(5, 0, 3), ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(5, 1, 3), ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(5, 2, 3), ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(5, 1, 2), ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(6, 0, 3), ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get().defaultBlockState());
        builder.setBlock(pos(6, 2, 3), ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get().defaultBlockState());
        return builder.create(definition -> {
        });
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}

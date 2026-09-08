package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyEnergyCellBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageVentBlock;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/** Placement definition for the L1 storage subsystem builder UI. */
public final class SimplifyStorageDefinition {
    public static final MultiBlockDefinition L1 = create();

    private SimplifyStorageDefinition() {
    }

    private static MultiBlockDefinition create() {
        Holder<Block> owner = BuiltInRegistries.BLOCK.wrapAsHolder(
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get());
        BlockState casing = ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get().defaultBlockState();
        BlockState energyCell = ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get().defaultBlockState()
                .setValue(SimplifyEnergyCellBlock.FACING, Direction.SOUTH);
        BlockState vent = ModRegistration.SIMPLIFY_STORAGE_VENT_BLOCK.get().defaultBlockState()
                .setValue(SimplifyStorageVentBlock.FACING, Direction.SOUTH);

        MultiBlockDefinition.Builder builder = MultiBlockDefinition.builder(owner)
                .setBlock(pos(1, 1, 0), ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 0, 0), casing)
                .setBlock(pos(2, 0, 0), casing)
                .setBlock(pos(2, 1, 0), casing)
                .setBlock(pos(1, 2, 0), casing)
                .setBlock(pos(2, 2, 0), casing)
                .setBlock(pos(1, 0, 1), casing)
                .setBlock(pos(2, 0, 1), casing)
                .setBlock(pos(2, 1, 1), ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get().defaultBlockState())
                .setBlock(pos(1, 1, 1), casing)
                .setBlock(pos(1, 2, 1), casing)
                .setBlock(pos(2, 2, 1), casing);

        for (int y = 0; y < 3; y++) {
            builder.setBlock(pos(0, y, 0), casing);
            builder.setBlock(pos(0, y, 1), casing);
        }

        return builder
                .setBlockRepeatable(pos(-1, 0, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 1, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 2, 0), Direction.WEST,
                        ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState())
                .setBlockRepeatable(pos(-1, 0, 1), Direction.WEST, energyCell)
                .setBlockRepeatable(pos(-1, 1, 1), Direction.WEST, vent)
                .setBlockRepeatable(pos(-1, 2, 1), Direction.WEST, energyCell)
                .setBlockWithRepeatShifted(pos(0, 0, 0), Direction.WEST, 1, casing)
                .setBlockWithRepeatShifted(pos(0, 0, 1), Direction.WEST, 1, casing)
                .setBlockWithRepeatShifted(pos(0, 1, 0), Direction.WEST, 1, casing)
                .setBlockWithRepeatShifted(pos(0, 1, 1), Direction.WEST, 1, casing)
                .setBlockWithRepeatShifted(pos(0, 2, 0), Direction.WEST, 1, casing)
                .setBlockWithRepeatShifted(pos(0, 2, 1), Direction.WEST, 1, casing)
                .expandMin(1)
                .expandMax(SimplifyMultiblockConfig.L1_PLACEMENT_EXPAND_MAX)
                .onFormed((blockPos, level) -> {
                    BlockState state = level.getBlockState(blockPos);
                    BlockState formedState = state;
                    if (state.hasProperty(NEBlock.FORMED)) {
                        formedState = formedState.setValue(NEBlock.FORMED, true);
                    }
                    if (formedState.hasProperty(ECOMachineCasing.INVISIBLE)) {
                        Vec3 myPos = blockPos.getCenter();
                        Vec3 controllerPos = new Vec3(1.5, 1.5, 0.5);
                        formedState = formedState.setValue(
                                ECOMachineCasing.INVISIBLE,
                                myPos.distanceToSqr(controllerPos) <= 3);
                    }
                    if (formedState != state) {
                        level.setBlockAndUpdate(blockPos, formedState);
                    }
                })
                // Do not add the addon definition to eco's global definition list.
                .create(definition -> {
                });
    }

    private static BlockPos pos(int x, int y, int z) {
        return new BlockPos(x, y, z);
    }
}

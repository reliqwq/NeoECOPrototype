package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.reactor.SimplifyReactorStructure;
import cn.dancingsnow.neoecoprototype.multiblock.reactor.SimplifyReactorStructure.Part;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Placement preview for the superconductive reactor: a fixed 7x7x7 cube, no repeat axis.
 *
 * <p>Every offset comes from {@link SimplifyReactorStructure}, the same class the controller's
 * validator walks, so the preview and a formed machine can never disagree.
 */
public final class SimplifyReactorDefinition {
    public static final MultiBlockDefinition CUBE = create();

    private SimplifyReactorDefinition() {
    }

    private static MultiBlockDefinition create() {
        Holder<Block> owner = BuiltInRegistries.BLOCK.wrapAsHolder(
                ModRegistration.REACTOR_CONTROLLER_BLOCK.get());
        BlockState frame = ModRegistration.REACTOR_FRAME_BLOCK.get().defaultBlockState();
        BlockState glass = ModRegistration.REACTOR_GLASS_BLOCK.get().defaultBlockState();
        BlockState coil = ModRegistration.REACTOR_COIL_BLOCK.get().defaultBlockState();
        BlockState core = ModRegistration.REACTOR_CORE_BLOCK.get().defaultBlockState();
        BlockState controller = ModRegistration.REACTOR_CONTROLLER_BLOCK.get().defaultBlockState()
                .setValue(BlockStateProperties.HORIZONTAL_FACING, Direction.NORTH);

        MultiBlockDefinition.Builder builder = MultiBlockDefinition.builder(owner);
        for (int depth = 0; depth < SimplifyReactorStructure.SIZE; depth++) {
            for (int up = -SimplifyReactorStructure.HALF; up <= SimplifyReactorStructure.HALF; up++) {
                for (int across = -SimplifyReactorStructure.HALF;
                        across <= SimplifyReactorStructure.HALF; across++) {
                    BlockState state = switch (SimplifyReactorStructure.partAt(depth, across, up)) {
                        case CONTROLLER -> controller;
                        case FRAME -> frame;
                        case GLASS -> glass;
                        case COIL -> coil;
                        case CORE -> core;
                        case AIR -> null;
                    };
                    if (state == null) {
                        continue;
                    }
                    BlockPos offset = SimplifyReactorStructure.localOffset(depth, across, up);
                    builder.setBlock(SimplifyReactorStructure.definitionPos(offset), state);
                }
            }
        }
        // Fixed size: pin the build length to one so the builder UI's length controls go inert
        // instead of offering a length the validator would reject anyway.
        return builder
                .expandMin(1)
                .expandMax(1)
                // The non-registering overload on purpose - see SimplifyTrinityDefinition: touching
                // NEMultiBlocks.DEFINITIONS runs eco's <clinit>, which reads its server config before
                // that config has loaded.
                .create(definition -> {
                });
    }
}

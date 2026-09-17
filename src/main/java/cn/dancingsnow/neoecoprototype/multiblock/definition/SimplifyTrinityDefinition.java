package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockRotation;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Placement preview for the Trinity shell.
 *
 * <p>All slot offsets come from {@link SimplifyTrinityClusterCalculator}, so this preview and the
 * runtime validator can never disagree about the layout.
 */
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
                .setBlock(toDefinitionPos(SimplifyTrinityClusterCalculator.CONTROLLER_OFFSET), controller)
                .setBlock(toDefinitionPos(SimplifyTrinityClusterCalculator.STORAGE_MODULE_OFFSET),
                        ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get().defaultBlockState())
                .setBlock(toDefinitionPos(SimplifyTrinityClusterCalculator.COMPUTATION_MODULE_OFFSET),
                        ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get().defaultBlockState())
                .setBlock(toDefinitionPos(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET),
                        ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get().defaultBlockState());

        int last = SimplifyTrinityClusterCalculator.SHELL_SIZE - 1;
        for (int x = 0; x <= last; x++) {
            for (int y = 0; y <= last; y++) {
                for (int z = 0; z <= last; z++) {
                    BlockPos offset = new BlockPos(x, y, z);
                    if (SimplifyTrinityClusterCalculator.isFunctionalSlot(offset)) {
                        continue;
                    }
                    builder.setBlock(toDefinitionPos(offset), casing);
                }
            }
        }
        // Trinity is a fixed-size machine: no repeatable blocks, so pin the build length to 1 and
        // the builder UI's length controls become inert instead of offering meaningless values.
        return builder
                .expandMin(1)
                .expandMax(1)
                .create(definition -> {
                });
    }

    /**
     * Converts a validator offset (relative to the shell's low corner, as used by
     * {@link SimplifyTrinityClusterCalculator}) into this definition's coordinate space.
     *
     * <p>eco's placement service anchors every definition at
     * {@code MultiBlockRotation.CONTROLLER_ANCHOR = (1, 1, 0)} and <b>skips that exact position</b>
     * when building a plan. The controller must therefore sit at that anchor; putting it anywhere
     * else makes the service drop whatever block really occupies the anchor -- which silently
     * produced a plan that never placed the crafting module.
     */
    private static BlockPos toDefinitionPos(BlockPos validatorOffset) {
        return validatorOffset.subtract(SimplifyTrinityClusterCalculator.CONTROLLER_OFFSET)
                .offset(MultiBlockRotation.CONTROLLER_ANCHOR);
    }
}

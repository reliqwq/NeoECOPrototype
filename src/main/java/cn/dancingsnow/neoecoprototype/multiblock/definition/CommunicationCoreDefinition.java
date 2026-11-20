package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Placement preview for the communication core: the base is the controller the service anchors on,
 * and the build length buys tower sections above it. The preview shows plain core sections only;
 * range and efficiency modules are swapped in by hand afterwards, and the runtime validator accepts
 * them at the same positions (see the block entity's scan for the per-type caps).
 */
public final class CommunicationCoreDefinition {

    /** Ceiling of the repeat axis: eight sections above the base, matching the shipped height cap. */
    public static final int MAX_SECTIONS = 8;

    public static final MultiBlockDefinition TOWER = create();

    private CommunicationCoreDefinition() {
    }

    private static MultiBlockDefinition create() {
        Holder<Block> owner = BuiltInRegistries.BLOCK.wrapAsHolder(
                ModRegistration.COMMUNICATION_BASE_BLOCK.get());
        BlockState section = ModRegistration.COMMUNICATION_CORE_BLOCK.get().defaultBlockState();

        return MultiBlockDefinition.builder(owner)
                .setBlockRepeatable(CommunicationCoreStructure.definitionPos(1), Direction.UP, section)
                .expandMin(1)
                .expandMax(MAX_SECTIONS)
                // The non-registering overload on purpose - see SimplifyStorageDefinition: touching
                // NEMultiBlocks.DEFINITIONS runs eco's <clinit>, which reads its server config before
                // that config has loaded.
                .create(definition -> {
                });
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** Keeps the addon bus out of migration sources until source/target overlap is verified. */
public final class SimplifyCraftingPatternBusBlockEntity extends ECOCraftingPatternBusBlockEntity {
    public SimplifyCraftingPatternBusBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean allowsPatternMigrationSource() {
        return false;
    }
}

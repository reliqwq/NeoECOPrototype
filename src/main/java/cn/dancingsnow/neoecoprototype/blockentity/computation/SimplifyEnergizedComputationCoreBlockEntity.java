package cn.dancingsnow.neoecoprototype.blockentity.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Exists only so the energized core has a block entity type of its own. eco collects members by
 * {@code instanceof}, so this still lands in the cluster's parallel-core list, but the client can
 * hang a renderer on this type without drawing lamps on every plain parallel core as well.
 */
public class SimplifyEnergizedComputationCoreBlockEntity extends ECOComputationParallelCoreBlockEntity {
    public SimplifyEnergizedComputationCoreBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                       BlockState blockState, IECOTier tier) {
        super(type, pos, blockState, tier);
    }
}

package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationParallelCore;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;

public class SimplifyComputationParallelCoreBlock extends ECOComputationParallelCore {
    public SimplifyComputationParallelCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationParallelCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}

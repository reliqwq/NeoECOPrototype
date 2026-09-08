package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationParallelCore;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;

public class SimplifyComputationParallelCoreBlock extends ECOComputationParallelCore {
    public SimplifyComputationParallelCoreBlock(Properties properties) {
        super(properties, SimplifyTier.L1);
    }
}

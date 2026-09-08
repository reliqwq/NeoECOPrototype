package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationThreadingCore;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;

public class SimplifyComputationThreadingCoreBlock extends ECOComputationThreadingCore {
    public SimplifyComputationThreadingCoreBlock(Properties properties) {
        super(properties, SimplifyTier.L1);
    }
}

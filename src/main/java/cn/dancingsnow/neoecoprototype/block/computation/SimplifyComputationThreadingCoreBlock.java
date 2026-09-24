package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.computation.ECOComputationThreadingCore;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;

public class SimplifyComputationThreadingCoreBlock extends ECOComputationThreadingCore {
    public SimplifyComputationThreadingCoreBlock(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationThreadingCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}

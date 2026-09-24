package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.items.ECOComputationCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;

public class SimplifyComputationCellItem extends ECOComputationCellItem {
    public SimplifyComputationCellItem(Properties properties) {
        this(properties, SimplifyTier.L1);
    }

    public SimplifyComputationCellItem(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}

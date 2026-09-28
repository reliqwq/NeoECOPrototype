package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/**
 * The energized core takes the one cell directly behind the computation host. It still hides itself once
 * the machine forms, like every eco shell member: the host's formed model carries a glass quad at the
 * plane between the two cells, so a member face drawn there is strictly coplanar with it and the two
 * fight over depth. Measured - the formed model's own elements sit at z=1, exactly the boundary.
 *
 * <p>What the move bought is that the cell is no longer eco's network-switch position, so a core standing
 * there can no longer make eco report a switch the machine does not have.
 */
public class SimplifyEnergizedComputationCoreBlock extends SimplifyComputationParallelCoreBlock {
    public SimplifyEnergizedComputationCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }

    @Override
    protected boolean hideWhenFormed() {
        return true;
    }
}

package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/**
 * The energized core takes a slot in the machine's outer shell. It keeps drawing its own block model
 * when the structure forms; the blockstate swaps it for a formed model that carries only the lit face,
 * so nothing has to be rendered from outside.
 */
public class SimplifyEnergizedComputationCoreBlock extends SimplifyComputationParallelCoreBlock {
    public SimplifyEnergizedComputationCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}

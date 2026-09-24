package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/**
 * The energized core takes a slot in the machine's outer shell, where every neighbour is a formed
 * casing. Formed casings are {@code RenderShape.INVISIBLE} and stop culling their neighbours, so a
 * member that kept drawing there would be seen from both sides at once and would flicker at any
 * viewing angle. eco solves this for its network switch by drawing nothing; {@code NEBlock} exposes
 * that as {@link #hideWhenFormed()}, so we only have to opt in.
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

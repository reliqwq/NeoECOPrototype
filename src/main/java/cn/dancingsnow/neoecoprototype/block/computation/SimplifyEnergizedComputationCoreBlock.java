package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/**
 * The energized core takes a slot in the machine's outer shell, and a shell cell must not draw
 * itself: the controller's own formed model already covers the whole 3x3 face (measured:
 * {@code controller_formed_base} spans x/y -16..32), so a member cube at 0..16 sits coplanar with
 * it and the two quads fight over depth. eco uses exactly this lever for every shell member it ships
 * (interface, fluid hatches, network switch), while column members such as the plain parallel core
 * keep drawing because their neighbours are solid and cull normally.
 *
 * <p>Consequence worth knowing before changing it: this block is accepted both in the shell and in
 * the parallel column, and one block has one render shape, so it disappears in both once formed.
 * The artwork for "an energized core is here" therefore belongs on the host's
 * {@code energized_parallel_core} blockstate variant, not on this block.
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

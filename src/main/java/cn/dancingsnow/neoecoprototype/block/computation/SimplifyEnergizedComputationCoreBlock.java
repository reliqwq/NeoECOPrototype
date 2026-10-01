package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;

/**
 * The energized core takes the one cell directly behind the computation host. That position is the whole
 * point: the cell is no longer eco's network-switch position, so a core standing there can no longer make
 * eco report a switch the machine does not have.
 *
 * <p>It draws itself once the machine forms. It used to hide, on the theory that the host's formed model
 * paints a quad on the plane the two share, but measured against that model the quads land at z=1 and
 * z=32 while the core's front face is at z=16 -- nothing is coplanar -- and eco ships this same cube shape
 * (0..16, all six faces plus a light overlay) as its own formed core without hiding it.
 */
public class SimplifyEnergizedComputationCoreBlock extends SimplifyComputationParallelCoreBlock {
    public SimplifyEnergizedComputationCoreBlock(Properties properties, IECOTier tier) {
        super(properties, tier);
    }
}

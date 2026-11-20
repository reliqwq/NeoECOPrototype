package cn.dancingsnow.neoecoprototype.multiblock.definition;

import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockRotation;
import net.minecraft.core.BlockPos;

/**
 * Geometry of the communication core tower: a 1xN column authored with the controller (the base)
 * at the origin and the sections rising above it, section 1 sitting directly on the base.
 *
 * <p>There is no facing to rotate - a vertical column is rotation-invariant - but the placement
 * definition still works in the controller-anchor coordinate space, so the definition and the
 * runtime validator read this one class and can never disagree about which block belongs where.
 * Definition Y stays non-negative for every section: the anchor is one block above the definition's
 * floor, and {@code MultiBlockContext} silently drops anything placed below it.
 */
public final class CommunicationCoreStructure {

    /** Local offset of section {@code index} above the base: section 1 is the first block on top. */
    public static BlockPos sectionLocal(int index) {
        return new BlockPos(0, index, 0);
    }

    /** The same position in the placement definition's coordinate space, where the controller is the anchor. */
    public static BlockPos definitionPos(int index) {
        return sectionLocal(index).offset(MultiBlockRotation.CONTROLLER_ANCHOR);
    }

    /** World position of section {@code index} above the base block. Rotation would not move it. */
    public static BlockPos worldPos(BlockPos basePos, int index) {
        return basePos.above(index);
    }

    private CommunicationCoreStructure() {
    }
}

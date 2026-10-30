package cn.dancingsnow.neoecoprototype.multiblock.reactor;

import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockRotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

/**
 * Geometry of the 7x7x7 superconductive reactor, authored facing north with the controller at the
 * origin: {@code depth} runs away from the controller into the cube, {@code across} is the
 * controller's right, {@code up} is +Y.
 *
 * <p>Both the placement definition and the runtime validator read this class, so a preview and a
 * formed machine can never disagree about which block belongs where.
 */
public final class SimplifyReactorStructure {
    /** Edge length of the cube. */
    public static final int SIZE = 7;
    /** Blocks either side of the controller's axes, i.e. {@code (SIZE - 1) / 2}. */
    public static final int HALF = 3;

    public enum Part {
        CONTROLLER, FRAME, GLASS, COIL, CORE, AIR
    }

    /** A position in the authored (north-facing, controller-at-origin) layout. */
    public static BlockPos localOffset(int depth, int across, int up) {
        return new BlockPos(across, up, depth);
    }

    /** The same position in the placement definition's coordinate space, where the controller is the anchor. */
    public static BlockPos definitionPos(BlockPos localOffset) {
        return localOffset.offset(MultiBlockRotation.CONTROLLER_ANCHOR);
    }

    /** The world position of a local offset, rotated by the controller's facing. */
    public static BlockPos worldPos(BlockPos controllerPos, Direction facing, BlockPos localOffset) {
        return MultiBlockRotation.localToWorld(definitionPos(localOffset), controllerPos, facing, false);
    }

    /**
     * What belongs at a local position. Edges are frame, faces are glass, and the chamber holds a
     * 3x3x3 coil shell wrapped around one core; everything else inside is air.
     */
    public static Part partAt(int depth, int across, int up) {
        if (depth == 0 && across == 0 && up == 0) {
            return Part.CONTROLLER;
        }
        int extremes = (atBound(depth, 0, SIZE - 1) ? 1 : 0)
                + (atBound(across, -HALF, HALF) ? 1 : 0)
                + (atBound(up, -HALF, HALF) ? 1 : 0);
        if (extremes >= 2) {
            return Part.FRAME;
        }
        if (extremes == 1) {
            return Part.GLASS;
        }
        int d = Math.abs(depth - HALF);
        int a = Math.abs(across);
        int u = Math.abs(up);
        if (d == 0 && a == 0 && u == 0) {
            return Part.CORE;
        }
        return Math.max(d, Math.max(a, u)) == 1 ? Part.COIL : Part.AIR;
    }

    /** The block above the cube's top face centre, where the storm bonus is measured from. */
    public static BlockPos topProbe(BlockPos controllerPos, Direction facing) {
        return worldPos(controllerPos, facing, localOffset(HALF, 0, HALF + 1));
    }

    private static boolean atBound(int value, int low, int high) {
        return value == low || value == high;
    }

    private SimplifyReactorStructure() {
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.decoration;

import cn.dancingsnow.neoecoprototype.event.PlushieScare;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

/**
 * The named dolls' presence on a shelf: nothing to save and nothing to draw, only the sweep that makes
 * creepers back away from it - the rule itself lives in {@link PlushieScare}, because a doll worn on a
 * head repels them the same way.
 */
public class NamedDollBlockEntity extends BlockEntity {
    public NamedDollBlockEntity(BlockPos pos, BlockState state) {
        super(ModRegistration.FUMO_DOLL_BE.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NamedDollBlockEntity self) {
        if (PlushieScare.sweepDue(level.getGameTime())) {
            scareAround(level, pos);
        }
    }

    /** The block's own contribution to the shared rule: the centre of this block is the plushie. */
    public static int scareAround(Level level, BlockPos pos) {
        return PlushieScare.scareAround(level, new Vec3(pos.getX() + 0.5D, pos.getY() + 0.5D, pos.getZ() + 0.5D));
    }
}

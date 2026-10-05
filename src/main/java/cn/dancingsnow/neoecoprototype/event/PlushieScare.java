package cn.dancingsnow.neoecoprototype.event;

import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/**
 * Creepers keep their distance from a plushie, the way vanilla keeps them away from cats: a plushie is a
 * stuffed cat, and the cat answer is the one the creeper already knows how to execute.
 *
 * <p>Two things can be that plushie - a named doll placed on a shelf, and a named doll worn on someone's
 * head - and both pay for it the same way: whoever carries the doll looks for creepers around itself, on
 * its own schedule. The other shape, a goal on every creeper asking whether a plushie is near, would cost
 * the same as the two vanilla goals a creeper is already running against cats and ocelots, but it would
 * scale with how many creepers the loader happens to have awake. This scales with dolls, which is the
 * number the player can see.
 */
public final class PlushieScare {
    /** The distance vanilla's own cat answer keeps. */
    private static final double SCARE_DISTANCE = 6.0D;
    /** How past the wearer a creeper is told to walk, so it does not stall on the rim of the radius. */
    private static final double FLEE_DISTANCE = 4.0D;
    /** One sweep per this many ticks; a goal would have run at roughly twice this rate. */
    private static final int SWEEP_TICKS = 5;
    /** Walking and sprinting alike, which is what vanilla hands the creeper's cat goal. */
    private static final double FLEE_SPEED = 1.2D;

    private PlushieScare() {
    }

    /** Whether this tick is the one where a plushie looks for creepers. */
    public static boolean sweepDue(long gameTime) {
        return gameTime % SWEEP_TICKS == 0;
    }

    /** True while a named doll occupies this entity's head. */
    public static boolean wearsNamedDoll(LivingEntity entity) {
        return FumoItem.isNamedDoll(entity.getItemBySlot(EquipmentSlot.HEAD));
    }

    /**
     * Every creeper within the scare radius of this point is sent away from it.
     *
     * @return how many creepers were given somewhere else to be, which is what the suite reads to tell a
     *         plushie that sweeps from one that matches nobody.
     */
    public static int scareAround(Level level, Vec3 centre) {
        if (level.isClientSide()) {
            return 0;
        }
        AABB area = new AABB(centre.x, centre.y, centre.z, centre.x, centre.y, centre.z)
                .inflate(SCARE_DISTANCE, 3.0D, SCARE_DISTANCE);
        double radiusSqr = SCARE_DISTANCE * SCARE_DISTANCE;
        int scared = 0;
        for (Creeper creeper : level.getEntitiesOfClass(Creeper.class, area,
                found -> found.isAlive() && found.distanceToSqr(centre) <= radiusSqr)) {
            Vec3 away = creeper.position().subtract(centre);
            Vec3 direction = new Vec3(away.x, 0.0D, away.z);
            // Standing on the plushie leaves no direction to run in, so pick one rather than freeze.
            if (direction.lengthSqr() < 1.0E-4D) {
                direction = new Vec3(0.0D, 0.0D, -1.0D);
            }
            Vec3 destination = creeper.position().add(direction.normalize().scale(FLEE_DISTANCE));
            creeper.getNavigation().setSpeedModifier(FLEE_SPEED);
            creeper.getNavigation().moveTo(destination.x, destination.y, destination.z, FLEE_SPEED);
            scared++;
        }
        return scared;
    }
}

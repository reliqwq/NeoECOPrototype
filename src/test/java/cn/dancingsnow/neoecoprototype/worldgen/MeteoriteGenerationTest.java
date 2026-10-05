package cn.dancingsnow.neoecoprototype.worldgen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The two ways a comet decides not to appear, and the branch that still lets it through. The real entry
 * point needs a {@code GenerationContext}, which cannot be built outside a running world, so the decision
 * itself lives in {@link MeteoritePlacement} where it can be reached.
 */
class MeteoriteGenerationTest {
    private static final int RADIUS = 9;
    private static final int CENTER_Y = 140;
    private static final int MIN_BUILD_HEIGHT = -64;
    private static final int MAX_BUILD_HEIGHT = 320;

    @Test
    void aDisabledCometIsNeverPlaced() {
        assertFalse(MeteoritePlacement.wantsComet(false, RADIUS, CENTER_Y,
                MIN_BUILD_HEIGHT, MAX_BUILD_HEIGHT),
                "with the switch off the End must stay empty of comets, whatever the height says");
    }

    @Test
    void anEnabledCometIsStillPlaced() {
        assertTrue(MeteoritePlacement.wantsComet(true, RADIUS, CENTER_Y,
                MIN_BUILD_HEIGHT, MAX_BUILD_HEIGHT),
                "the same call with the switch on has to produce a comet, or the gate is a plain refusal "
                        + "and turning it on would do nothing");
    }

    @Test
    void aCometThatWouldPokePastTheBuildLimitIsRefusedEvenWhenEnabled() {
        assertFalse(MeteoritePlacement.wantsComet(true, RADIUS, CENTER_Y,
                MIN_BUILD_HEIGHT, CENTER_Y + RADIUS),
                "the top of the head has to stay under the build limit even for an enabled comet");
        assertFalse(MeteoritePlacement.wantsComet(true, RADIUS, CENTER_Y,
                CENTER_Y - RADIUS, MAX_BUILD_HEIGHT),
                "and so has the bottom, which is the side that would sit in the void");
    }
}

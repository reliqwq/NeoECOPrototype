package cn.dancingsnow.neoecoprototype.api;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SimplifyPowerProfileTest {
    private static final double EPSILON = 1.0E-9D;

    @Test
    void l1UsesOneEighthOfEcoIdlePower() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;

        assertEquals(1.0D / 8.0D, profile.idlePowerMultiplier(), EPSILON);
    }

    @Test
    void componentPowerBaselinesUseReducedCraftingPower() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;

        assertEquals(16.0D / 8.0D, profile.baseComponentIdlePower(), EPSILON);
        assertEquals(32.0D / 8.0D, profile.highIdleComponentPower(), EPSILON);
        assertEquals(256.0D / 8.0D, profile.storageDriveIdlePower(), EPSILON);
    }

    @Test
    void storageControllerUsesOneEighthOfEcoBaseline() {
        assertEquals(258.0D / 8.0D,
                SimplifyPowerProfile.L1.storageControllerIdlePower(), EPSILON);
    }

    @Test
    void computationAndCraftingControllersUseConfiguredBaselines() {
        SimplifyPowerProfile profile = SimplifyPowerProfile.L1;

        assertEquals(16.0D / 8.0D, profile.computationControllerIdlePower(), EPSILON);
        assertEquals(32.0D / 8.0D, profile.craftingControllerIdlePower(), EPSILON);
    }
}

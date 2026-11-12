package cn.dancingsnow.neoecoprototype.integration.ae2;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.parts.crafting.PatternProviderPart;
import appeng.parts.misc.InterfacePart;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPoweredMEInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.part.PoweredInterfacePart;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;
import cn.dancingsnow.neoecoprototype.part.SuperconductiveInterfacePart;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PatternProviderUpgradeTargetTest {
    @Test
    void normalizesAddonMachinesToTheClassesRecognizedByUpgradeCards() {
        assertEquals(PatternProviderBlockEntity.class,
                PatternProviderUpgradeTarget.normalize(SimplifyPatternProviderBlockEntity.class));
        assertEquals(PatternProviderPart.class,
                PatternProviderUpgradeTarget.normalize(SimplifyPatternProviderPart.class));
    }

    @Test
    void leavesInterfacesOutsideTheUpgradeTargetSet() {
        assertEquals(SimplifyPoweredMEInterfaceBlockEntity.class,
                PatternProviderUpgradeTarget.normalize(SimplifyPoweredMEInterfaceBlockEntity.class));
        assertEquals(SimplifySuperconductiveInterfaceBlockEntity.class,
                PatternProviderUpgradeTarget.normalize(SimplifySuperconductiveInterfaceBlockEntity.class));
        assertEquals(PoweredInterfacePart.class,
                PatternProviderUpgradeTarget.normalize(PoweredInterfacePart.class));
        assertEquals(SuperconductiveInterfacePart.class,
                PatternProviderUpgradeTarget.normalize(SuperconductiveInterfacePart.class));
    }

    @Test
    void leavesUnrelatedAndVanillaTargetsUnchanged() {
        assertEquals(PatternProviderBlockEntity.class,
                PatternProviderUpgradeTarget.normalize(PatternProviderBlockEntity.class));
        assertEquals(InterfaceBlockEntity.class,
                PatternProviderUpgradeTarget.normalize(InterfaceBlockEntity.class));
        assertEquals(InterfacePart.class,
                PatternProviderUpgradeTarget.normalize(InterfacePart.class));
        assertEquals(Object.class, PatternProviderUpgradeTarget.normalize(Object.class));
    }
}

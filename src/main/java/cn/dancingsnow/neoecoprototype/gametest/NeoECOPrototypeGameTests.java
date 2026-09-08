package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Minimal runtime smoke test proving the addon registry is available to GameTest. */
@GameTestHolder(NeoECOPrototype.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeoECOPrototypeGameTests {
    private NeoECOPrototypeGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void registrationsAreAvailable(GameTestHelper helper) {
        helper.assertTrue(ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get() != null,
                "storage communication interface is not registered");
        helper.assertTrue(ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get() != null,
                "storage network interface is not registered");
        helper.succeed();
    }
}

package cn.dancingsnow.neoecoprototype.integration.emi;

import cn.dancingsnow.neoecoae.integration.emi.NeoECOAEEmiPlugin;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.stack.EmiStack;

/**
 * Names our three L1 hosts as workstations of eco's multiblock page. eco's own plugin builds that
 * page by walking {@code NEMultiBlocks.DEFINITIONS}, which our definitions already joined, so the
 * recipes are there but nothing points at the blocks that perform them: the list of hosts it
 * registers is hardcoded to its nine controllers. Trinity stays out, same as in
 * {@link cn.dancingsnow.neoecoprototype.integration.jei.NeoECOPrototypeJeiPlugin}.
 */
@EmiEntrypoint
public final class NeoECOPrototypeEmiPlugin implements EmiPlugin {

    @Override
    public void register(EmiRegistry registry) {
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get()));
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get()));
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()));
    }
}

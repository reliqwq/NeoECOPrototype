package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoae.integration.jei.NeoECOAEJeiPlugin;
import cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Adds the L1 definitions to eco's existing multiblock preview category. */
@JeiPlugin
public final class NeoECOPrototypeJeiPlugin implements IModPlugin {
    /** Resolve definitions only after registries have been bound by NeoForge. */
    private static List<cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition> l1Definitions() {
        return List.of(
                SimplifyStorageDefinition.L1,
                SimplifyComputationDefinition.L1,
                SimplifyCraftingDefinition.L1);
    }

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        registration.addRecipes(NeoECOAEJeiPlugin.MULTIBLOCK_TYPE,
                l1Definitions().stream().map(MultiBlockInfoWrapper::new).toList());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                NeoECOAEJeiPlugin.MULTIBLOCK_TYPE);
        registration.addRecipeCatalyst(
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                NeoECOAEJeiPlugin.MULTIBLOCK_TYPE);
        registration.addRecipeCatalyst(
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                NeoECOAEJeiPlugin.MULTIBLOCK_TYPE);
    }
}

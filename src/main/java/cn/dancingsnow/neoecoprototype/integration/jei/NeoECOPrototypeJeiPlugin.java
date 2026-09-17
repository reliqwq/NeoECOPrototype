package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoae.integration.jei.NeoECOAEJeiPlugin;
import cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyTrinityDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import java.util.List;

/** Adds the L1 definitions to eco's existing multiblock preview category. */
@JeiPlugin
public final class NeoECOPrototypeJeiPlugin implements IModPlugin {
    /**
     * Trinity remains experimental and is intentionally absent from player-facing JEI until its
     * complete release design is ready. Keep registrations intact for worlds and GameTests.
     */
    private static final boolean TRINITY_VISIBLE_IN_JEI = false;

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
        List<cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition> definitions =
                l1Definitions();
        if (TRINITY_VISIBLE_IN_JEI) {
            definitions = new java.util.ArrayList<>(definitions);
            definitions.add(SimplifyTrinityDefinition.L1);
        }
        registration.addRecipes(NeoECOAEJeiPlugin.MULTIBLOCK_TYPE,
                definitions.stream().map(MultiBlockInfoWrapper::new).toList());
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
        if (TRINITY_VISIBLE_IN_JEI) {
            registration.addRecipeCatalyst(
                    ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(),
                    NeoECOAEJeiPlugin.MULTIBLOCK_TYPE);
        }
    }

    @Override
    public void onRuntimeAvailable(IJeiRuntime runtime) {
        if (TRINITY_VISIBLE_IN_JEI) {
            return;
        }
        runtime.getIngredientManager().removeIngredientsAtRuntime(
                mezz.jei.api.constants.VanillaTypes.ITEM_STACK,
                List.of(
                        new ItemStack(ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_ITEM.get()),
                        new ItemStack(ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_ITEM.get()),
                        new ItemStack(ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_ITEM.get()),
                        new ItemStack(ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get())));
    }
}

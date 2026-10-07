package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoae.integration.jei.NeoECOAEJeiPlugin;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.runtime.IJeiRuntime;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** Adds the processor assembler page; the L1 structures ride on eco's multiblock category. */
@JeiPlugin
public final class NeoECOPrototypeJeiPlugin implements IModPlugin {
    /**
     * Trinity is deliberately not offered to players yet: it stays out of JEI until its release
     * design is finished, and its items carry the red "not implemented" line from
     * {@link cn.dancingsnow.neoecoprototype.tooltip.SimplifyTooltipHandler}. Both halves are
     * intentional markers, not leftovers — flip this to true when Trinity ships, and keep the
     * registrations alive for worlds and GameTests meanwhile.
     */
    private static final boolean TRINITY_VISIBLE_IN_JEI = false;

    @Override
    public ResourceLocation getPluginUid() {
        return ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "jei_plugin");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        registration.addRecipeCategories(new ProcessorAssemblerCategory(registration.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        // Our three L1 structures, into eco's own category, by category object rather than by name:
        // asking JEI for the category by uid made this method depend on plugin order and it threw with
        // EMI installed. eco fills the same page from NEMultiBlocks.DEFINITIONS, but joining that list
        // runs eco's class initialiser before its server config loads and leaves its own L4-L9
        // definitions with a build range of 1 .. -4, so we hand the definitions over directly instead.
        registration.addRecipes(NeoECOAEJeiPlugin.MULTIBLOCK_TYPE, List.of(
                new cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper(
                        cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition.L1),
                new cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper(
                        cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition.L1),
                new cn.dancingsnow.neoecoae.integration.xei.multiblock.MultiBlockInfoWrapper(
                        cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition.L1)));
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        List<ProcessorAssemblerRecipe> recipes = new java.util.ArrayList<>(level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList());
        if (NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER.get()) {
            recipes.addAll(ProcessorAssemblerRecipes.derived(level, recipes));
        }
        registration.addRecipes(ProcessorAssemblerCategory.TYPE,
                recipes.stream().filter(recipe ->
                        !NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(recipe.result().getItem()))
                        .toList());
        registration.addIngredientInfo(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get(),
                Component.translatable("jei.neoecoprototype.processor_assembler.encode_hint"));
        // The L1 structures are not added here on purpose. eco's own plugin fills its multiblock page by
        // walking NEMultiBlocks.DEFINITIONS, and a definition joins that list inside Builder.create(), so
        // adding them again would duplicate them - and asking for eco's category by name made this whole
        // method depend on plugin order (with EMI installed it threw here and took the assembler page down
        // with it, measured 2026-10-06). NeoECOPrototype#commonSetup touches the definitions instead.
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
        registration.addRecipeCatalyst(
                ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get(),
                ProcessorAssemblerCategory.TYPE);
        if (TRINITY_VISIBLE_IN_JEI) {
            registration.addRecipeCatalyst(
                    ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(),
                    NeoECOAEJeiPlugin.MULTIBLOCK_TYPE);
        }
    }

    /**
     * Our own encode handler, registered per category so it wins over ae2jeiintegration's universal one --
     * that handler collects inputs into a set keyed by item, which collapses two identical ingredients into
     * one slot and produces a pattern the assembler rightly refuses.
     */
    @Override
    public void registerRecipeTransferHandlers(mezz.jei.api.registration.IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(
                new ProcessorAssemblerTransferHandler(registration.getTransferHelper()),
                ProcessorAssemblerCategory.TYPE);
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

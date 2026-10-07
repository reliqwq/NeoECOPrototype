package cn.dancingsnow.neoecoprototype.integration.emi;

import cn.dancingsnow.neoecoae.integration.emi.NeoECOAEEmiPlugin;
import cn.dancingsnow.neoecoae.integration.emi.recipe.MultiblockEmiRecipe;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.ArrayList;
import java.util.List;

/**
 * Puts our three L1 structures into eco's multiblock page in EMI, and names the blocks that perform
 * them. eco's own plugin fills that page by walking {@code NEMultiBlocks.DEFINITIONS} and registers its
 * nine controllers as workstations by name; we hand over our definitions and our three hosts instead of
 * joining that list, because reading it runs eco's class initialiser before its server config has
 * loaded. Trinity stays out, same as in {@link
 * cn.dancingsnow.neoecoprototype.integration.jei.NeoECOPrototypeJeiPlugin}.
 *
 * <p>The L1 assembler gets its own page here as well, which is what makes the two viewers agree: that
 * page only existed in JEI until this file registered it.
 */
@EmiEntrypoint
public final class NeoECOPrototypeEmiPlugin implements EmiPlugin {
    /** Our own page: the L1 assembler's shapeless processor recipes. */
    public static final EmiRecipeCategory PROCESSOR_ASSEMBLER = new EmiRecipeCategory(
            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "processor_assembler"),
            EmiStack.of(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get()));

    @Override
    public void register(EmiRegistry registry) {
        // eco's own page loop walks NEMultiBlocks.DEFINITIONS, which we must not join this early - see
        // NeoECOPrototype#commonSetup. So we build its recipe wrapper for our three structures here.
        registry.addRecipe(new MultiblockEmiRecipe(
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition.L1));
        registry.addRecipe(new MultiblockEmiRecipe(
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition.L1));
        registry.addRecipe(new MultiblockEmiRecipe(
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition.L1));
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get()));
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get()));
        registry.addWorkstation(NeoECOAEEmiPlugin.MULTIBLOCK,
                EmiStack.of(ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()));
        registerAssembler(registry);
    }

    /**
     * The same list JEI shows, and it has to be built the same way: half of it may not exist in the
     * recipe manager at all, because the config can copy the processor recipes off AE2's inscriber
     * instead, and a pack can switch individual outputs off. Anything the two viewers disagree about
     * here reads as a missing recipe in whichever one the player happens to have open.
     */
    private static void registerAssembler(EmiRegistry registry) {
        var level = net.minecraft.client.Minecraft.getInstance().level;
        if (level == null) {
            return;
        }
        registry.addCategory(PROCESSOR_ASSEMBLER);
        registry.addWorkstation(PROCESSOR_ASSEMBLER,
                EmiStack.of(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get()));
        List<RecipeHolder<ProcessorAssemblerRecipe>> fromJson = level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get());
        List<ProcessorAssemblerRecipe> jsonRecipes = new ArrayList<>();
        for (var holder : fromJson) {
            if (!isShown(holder.value())) {
                continue;
            }
            jsonRecipes.add(holder.value());
            registry.addRecipe(new ProcessorAssemblerEmiRecipe(PROCESSOR_ASSEMBLER, holder.id(),
                    holder.value(), holder));
        }
        if (!NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER.get()) {
            return;
        }
        for (var derived : ProcessorAssemblerRecipes.derived(level, jsonRecipes)) {
            if (!isShown(derived)) {
                continue;
            }
            var output = BuiltInRegistries.ITEM.getKey(derived.result().getItem());
            // The leading slash is EMI asking for it: a recipe id that has no RecipeHolder behind
            // it is synthetic to EMI, and without the prefix it logs "not present in recipe manager"
            // once per derived recipe (measured 16 lines in a client log, 2026-10-07).
            registry.addRecipe(new ProcessorAssemblerEmiRecipe(PROCESSOR_ASSEMBLER,
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                            "/assembler_derived/" + output.getNamespace() + "/" + output.getPath()),
                    derived, null));
        }
    }

    private static boolean isShown(ProcessorAssemblerRecipe recipe) {
        return !NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(recipe.result().getItem());
    }
}


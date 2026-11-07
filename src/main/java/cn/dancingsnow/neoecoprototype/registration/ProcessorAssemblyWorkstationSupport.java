package cn.dancingsnow.neoecoprototype.registration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoae.api.workstation.ECOWorkstationModules;
import cn.dancingsnow.neoecoae.api.workstation.ECOWorkstationRecipeAdapters;
import cn.dancingsnow.neoecoae.recipe.IntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.crafting.FluidIngredient;
import net.neoforged.neoforge.fluids.crafting.SizedFluidIngredient;

import java.util.ArrayList;
import java.util.List;

/**
 * The L1 processor assembler as an eco workstation module: one card of ours, inserted into eco's large
 * integrated working station, makes this addon's processor-assembly recipes craftable there.
 *
 * <p>Both halves are eco's own public addon doors, and they are what the machine actually reads. The card
 * side is {@code ECOWorkstationModules.register}: the workstation's pattern provider calls
 * {@code LargeWorkstationRecipes.getAvailable(level, controller.getInstalledModuleId())}, and that overload
 * filters by module id whenever the installed card resolves to a registered (custom) module - so our own id
 * is a real answer, not a tooltip. The recipe side is {@code ECOWorkstationRecipeAdapters.register}, which
 * {@code LargeWorkstationRecipes.getAll} asks for every recipe holder whose own type is not one of eco's
 * compat types.</p>
 *
 * <p>Registering a module also takes our recipe type out of eco's catch-all. {@code sourceTypes} is consulted
 * before {@code LargeWorkstationModule.fromSourceType}, whose namespace switch does not know us, so until
 * this class runs our type classified as {@code OTHER} - which every card grants, and which therefore granted
 * nothing either, because no adapter existed. That is why both registrations are one change: a family with no
 * recipes behind it would only remove access.</p>
 *
 * <p>The numbers are eco's own cheapest rung, not invented here: 1000 AE comes from the lowest
 * {@code energy} in eco's shipped {@code integrated_working_station} data (and matches what our own recipes in
 * that folder already pay), and 100 ticks is {@code IntegratedWorkingStationRecipe}'s own default. eco's
 * reference value for a source type it does not know would be 200, the cap, so this is the lower cooling bar
 * reliqwq asked for rather than a number of our own devising.</p>
 */
public final class ProcessorAssemblyWorkstationSupport {
    /** eco refuses {@code neoecoae} here on purpose: modules are named by the addon that owns them. */
    public static final ResourceLocation MODULE_ID = NeoECOPrototype.id("processor_assembly");
    /** Must equal the recipe type our holders report, or eco rejects the adapted recipe by identity. */
    private static final ResourceLocation SOURCE_TYPE = sourceType();
    /** eco ORs 0xFF000000 onto this number in its panel, so the alpha byte must not be carried. */
    static final int MODULE_COLOR = ModRegistration.NAME_THEME_TIER_L1 & 0xFFFFFF;
    private static final int ENERGY_AE = 1000;
    private static final int PROCESSING_TICKS = 100;

    private ProcessorAssemblyWorkstationSupport() {
    }

    private static ResourceLocation sourceType() {
        ResourceLocation type = BuiltInRegistries.RECIPE_TYPE.getKey(
                ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get());
        if (type == null) {
            throw new IllegalStateException("Missing eco-side recipe type for the L1 processor assembler");
        }
        return type;
    }

    /** eco's own panel shows these two beside its family names, so they name the family, not the item. */
    static final String TITLE_KEY = "gui.neoecoprototype.workstation.module.processor_assembly";
    static final String SHORT_TITLE_KEY = "gui.neoecoprototype.workstation.module_short.processor_assembly";

    /** Called from commonSetup alongside the other eco extension registrations. */
    public static void register() {
        ResourceLocation card = BuiltInRegistries.ITEM.getKey(
                ModRegistration.PROCESSOR_ASSEMBLY_WORKSTATION_CARD.get());
        ECOWorkstationModules.register(100, new ECOWorkstationModules.Module(MODULE_ID, card,
                java.util.Set.of(SOURCE_TYPE), Component.translatable(TITLE_KEY),
                Component.translatable(SHORT_TITLE_KEY), MODULE_COLOR, null, null, null, null));
        ECOWorkstationRecipeAdapters.register(MODULE_ID, 100, SOURCE_TYPE,
                (recipe, registries) -> adapt(recipe));
    }

    /**
     * One processor-assembly recipe, stated as a workstation contract: every ingredient is one unit (that is
     * what our machine consumes per craft, and a repeated ingredient is already a second list entry), no
     * fluid in either direction.
     *
     * <p>The card itself is deliberately not part of the family it unlocks: it is the key, not the content,
     * and a workstation that could print it would hand out its own unlocks without ever going near our
     * assembler. It stays a processor-assembler recipe, which is where every one of our cards is made.
     *
     * @return null for anything that is not our recipe, which leaves it to eco's own compat table
     */
    static LargeWorkstationRecipe adapt(RecipeHolder<?> recipe) {
        if (!(recipe.value() instanceof ProcessorAssemblerRecipe processor)) return null;
        if (processor.result().is(ModRegistration.PROCESSOR_ASSEMBLY_WORKSTATION_CARD.get())) return null;
        List<SizedIngredient> inputs = new ArrayList<>(processor.ingredients().size());
        for (Ingredient ingredient : processor.ingredients()) {
            if (ingredient.isEmpty()) {
                // A hole in the middle of a processor recipe would be a recipe we shipped wrong; refusing it
                // here is cheaper than letting the workstation show a craft the assembler cannot run.
                return null;
            }
            inputs.add(new SizedIngredient(ingredient, 1));
        }
        var display = new IntegratedWorkingStationRecipe(inputs,
                new SizedFluidIngredient(FluidIngredient.empty(), 1), processor.result(), FluidStack.EMPTY,
                ENERGY_AE, PROCESSING_TICKS);
        return new LargeWorkstationRecipe(recipe.id(), display, ENERGY_AE, List.of(), SOURCE_TYPE);
    }
}

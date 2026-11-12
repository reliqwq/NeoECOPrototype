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
import net.minecraft.world.item.ItemStack;
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
 * <p>The numbers are ours: one AE per run, so the cost of running this family is the ingredients and the
 * time, not a second energy gate on top of them. The adapted recipe states {@link #RECIPE_SCALE} runs at
 * once, and its energy is scaled with it, because that is the shape our patterns are encoded at on this
 * machine: eco multiplies the recipe's inputs and energy by the pattern's own scale. A one-run declaration
 * would still answer a 128x pattern, so the per-item price would be the same - but a pattern smaller than
 * the stated scale would also be accepted, and this family is meant to be pulled in batches. Stating 64
 * runs makes that batch the smallest answerable unit.</p>
 *
 * <p>{@link #PROCESSING_TICKS} is what eco's panel shows, not how long the station takes:
 * {@code createPendingBatch} writes the batch's own {@code processingTicks = 1} and never reads the
 * recipe's, so the station's overclock decides the pace. The number still has to be set through
 * {@link LargeWorkstationRecipe#processing} because the convenience constructor derives it from
 * {@code referenceTicks}, whose fallback for a source type eco does not know is 200 - the cap - and that
 * would be the wrong number on the panel and in eco's own tie-break between equally costly recipes.</p>
 */
public final class ProcessorAssemblyWorkstationSupport {
    /** eco refuses {@code neoecoae} here on purpose: modules are named by the addon that owns them. */
    public static final ResourceLocation MODULE_ID = NeoECOPrototype.id("processor_assembly");
    /** Must equal the recipe type our holders report, or eco rejects the adapted recipe by identity. */
    private static final ResourceLocation SOURCE_TYPE = sourceType();
    /** eco ORs 0xFF000000 onto this number in its panel, so the alpha byte must not be carried. */
    static final int MODULE_COLOR = ModRegistration.NAME_THEME_TIER_L1 & 0xFFFFFF;
    /**
     * How many runs of one of our recipes a single workstation pattern execution carries. The L1 assembler
     * is unaffected: it matches the pattern it is handed against its own recipe type, so 1x patterns still
     * run there.
     */
    public static final int RECIPE_SCALE = 64;
    /** What one run costs; the adapted recipe states {@link #RECIPE_SCALE} runs, so the per-item price holds. */
    private static final int ENERGY_PER_RUN = 1;
    private static final int ENERGY_AE = ENERGY_PER_RUN * RECIPE_SCALE;
    /**
     * The interval the panel shows for one adaptation. Display and ordering only: the large station writes
     * its batch's own {@code processingTicks = 1}, so this number does not slow the machine down.
     */
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

    /**
     * The panel draws these with {@code SpriteTexture.of(location)}, whose default location is
     * {@code textures/gui/icon.png} - so the whole texture path has to be in the id, not just a name. Sizes
     * are read by the panel as (0, 0, 15, 15) for the icon and (0, 0, 28, 28) for the circuit, which is why
     * the two files are exactly that.
     */
    static final ResourceLocation ICON_TEXTURE =
            NeoECOPrototype.id("textures/gui/workstation/processor_assembly_icon.png");
    static final ResourceLocation CIRCUIT_TEXTURE =
            NeoECOPrototype.id("textures/gui/workstation/processor_assembly_circuit.png");

    /**
     * The formed controller's appearance while this card is installed. eco bakes these standalone
     * ({@code ModelEvent.RegisterAdditional}) and draws them instead of the blockstate model, which is the
     * only way an addon can own the look: writing into {@code assets/neoecoae/} is rejected by CI. Both are
     * eco's geometry re-pointed at our textures - see {@code tools/make_workstation_controller_textures.py}.
     * Both light strips animate at eco's own rhythm (two frames, 15 ticks, interpolated): the working
     * strip is eco's pair retinted, the idle strip pairs the retinted idle sheet with a frame that flips
     * both greens across every lit pixel of the front and top faces - a smaller change reads as a still
     * image at block scale. The working screen keeps eco's 22-frame scan at 5 ticks; the idle screen is
     * still, as eco's is.
     */
    static final ResourceLocation IDLE_MODEL = NeoECOPrototype.id("block/workstation/controller_off");
    static final ResourceLocation WORKING_MODEL = NeoECOPrototype.id("block/workstation/controller_on");
    public static final ResourceLocation SMART_IDLE_MODEL = NeoECOPrototype.id("block/workstation/controller_off_smart");
    public static final ResourceLocation SMART_WORKING_MODEL = NeoECOPrototype.id("block/workstation/controller_on_smart");
    public static final ResourceLocation SMART_COOLING_SCREEN_MODEL =
            NeoECOPrototype.id("block/workstation/controller_cooling_screen");

    /** Called from commonSetup alongside the other eco extension registrations. */
    public static void register() {
        ResourceLocation card = BuiltInRegistries.ITEM.getKey(
                ModRegistration.PROCESSOR_ASSEMBLY_WORKSTATION_CARD.get());
        ECOWorkstationModules.register(100, new ECOWorkstationModules.Module(MODULE_ID, card,
                java.util.Set.of(SOURCE_TYPE), Component.translatable(TITLE_KEY),
                Component.translatable(SHORT_TITLE_KEY), MODULE_COLOR,
                ICON_TEXTURE, CIRCUIT_TEXTURE, IDLE_MODEL, WORKING_MODEL));
        ECOWorkstationRecipeAdapters.register(MODULE_ID, 100, SOURCE_TYPE,
                (recipe, registries) -> adapt(recipe));
    }

    /**
     * One processor-assembly recipe, stated as a workstation contract: every ingredient is
     * {@link #RECIPE_SCALE} units (a repeated ingredient is already a second list entry), the output count
     * is scaled the same way, and there is no fluid in either direction.
     *
     * <p>The card is deliberately not one of these. It is crafted by eco's large integrated working station
     * from a {@code neoecoae:large_integrated_working_station} recipe, because that is the one shape that can
     * state per-ingredient counts and a per-craft energy cost; our own recipe type carries neither, so putting
     * the card here would have made eight assemblers and 32 crystal matrices collapse to one unit each.</p>
     *
     * @return null for anything that is not our recipe, which never happens through our own registration
     */
    static LargeWorkstationRecipe adapt(RecipeHolder<?> recipe) {
        if (!(recipe.value() instanceof ProcessorAssemblerRecipe processor)) return null;
        List<SizedIngredient> inputs = new ArrayList<>(processor.ingredients().size());
        for (Ingredient ingredient : processor.ingredients()) {
            if (ingredient.isEmpty()) {
                // A hole in the middle of a processor recipe would be a recipe we shipped wrong; refusing it
                // here is cheaper than letting the workstation show a craft the assembler cannot run.
                return null;
            }
            inputs.add(new SizedIngredient(ingredient, RECIPE_SCALE));
        }
        ItemStack result = processor.result().copy();
        result.setCount(Math.multiplyExact(result.getCount(), RECIPE_SCALE));
        var display = new IntegratedWorkingStationRecipe(inputs,
                new SizedFluidIngredient(FluidIngredient.empty(), 1), result, FluidStack.EMPTY,
                ENERGY_AE, PROCESSING_TICKS);
        return new LargeWorkstationRecipe(recipe.id(), display, ENERGY_AE, List.of(), SOURCE_TYPE)
                .processing(PROCESSING_TICKS, 0, false);
    }
}

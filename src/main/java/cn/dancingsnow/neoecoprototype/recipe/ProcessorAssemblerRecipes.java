package cn.dancingsnow.neoecoprototype.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Derives processor recipes from AE2's inscriber so pack content that adds inscriber recipes needs no
 * extra data files. A press-mode recipe consumes printed parts, and each printed part stands for the
 * material that was inscribed into it, so the printed part is replaced by its own input.
 */
public final class ProcessorAssemblerRecipes {
    private ProcessorAssemblerRecipes() {
    }

    /**
     * Derived additions that are not already covered by {@code existing}. The bundled JSON recipes were
     * authored from this same rule, so without this they would each be listed twice.
     */
    public static List<ProcessorAssemblerRecipe> derived(Level level, List<ProcessorAssemblerRecipe> existing) {
        return derive(level.getRecipeManager(), existing).stream().map(DerivedRecipe::recipe).toList();
    }

    public static List<RecipeHolder<?>> derivedHolders(RecipeManager manager) {
        List<ProcessorAssemblerRecipe> existing = manager
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        List<RecipeHolder<?>> holders = new ArrayList<>();
        for (DerivedRecipe derived : derive(manager, existing)) {
            if (derived.recipe().hasItemOutput()
                    && NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(derived.recipe().result().getItem())) {
                continue;
            }
            ResourceLocation sourceId = derived.sourceId();
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath("neoecoprototype",
                    "derived/inscriber/" + sourceId.getNamespace() + "/" + sourceId.getPath());
            if (manager.byKey(id).isEmpty()) {
                holders.add(new RecipeHolder<>(id, derived.recipe()));
            }
        }
        return holders;
    }

    private static List<DerivedRecipe> derive(RecipeManager manager, List<ProcessorAssemblerRecipe> existing) {
        var inscriber = manager.getAllRecipesFor(AERecipeTypes.INSCRIBER);

        // printed part -> the material inscribed into it. Only a one-for-one inscribe recipe can be
        // unfolded into a material slot; a bulk recipe (several parts from one input) has no whole-number
        // cost, and an output with two such sources has no single material to pick.
        Map<Item, Ingredient> inscribedFrom = new HashMap<>();
        Set<Item> ambiguous = new HashSet<>();
        for (var holder : inscriber) {
            InscriberRecipe recipe = holder.value();
            if (recipe.getProcessType() != InscriberProcessType.INSCRIBE) continue;
            ItemStack result = recipe.getResultItem();
            if (result.isEmpty() || result.getCount() != 1) continue;
            if (inscribedFrom.putIfAbsent(result.getItem(), recipe.getMiddleInput()) != null) {
                ambiguous.add(result.getItem());
            }
        }
        ambiguous.forEach(inscribedFrom::remove);

        Set<String> covered = existing.stream().map(ProcessorAssemblerRecipes::signature)
                .collect(java.util.stream.Collectors.toSet());
        List<DerivedRecipe> derived = new ArrayList<>();
        for (var holder : inscriber) {
            InscriberRecipe press = holder.value();
            if (press.getProcessType() != InscriberProcessType.PRESS) continue;
            List<Ingredient> ingredients = List.of(
                    press.getMiddleInput(), press.getTopOptional(), press.getBottomOptional());
            // Press-making and dust recipes leave a slot empty; the assembler needs exactly three.
            if (ingredients.stream().anyMatch(Ingredient::isEmpty)) continue;
            List<Ingredient> simplified = ingredients.stream()
                    .map(ingredient -> substitute(ingredient, inscribedFrom))
                    .toList();
            ProcessorAssemblerRecipe recipe = new ProcessorAssemblerRecipe(simplified, press.getResultItem());
            if (!covered.contains(signature(recipe))) derived.add(new DerivedRecipe(holder.id(), recipe));
        }
        return derived;
    }

    private record DerivedRecipe(ResourceLocation sourceId, ProcessorAssemblerRecipe recipe) {
    }


    /** Recipe id folders select the machine family; the EC9 accepts every declared recipe. */
    public static boolean isL1Recipe(ResourceLocation id) {
        String path = id.getPath();
        return !path.startsWith("derived/inscriber/") && !path.startsWith("ec9_processor_assembler/");
    }

    /**
     * The addon recipe an AE2 processing pattern asks for, or null when no registered output set and item
     * inputs cover it. All outputs must describe the same positive integral run count. {@code ec9} includes
     * both the inscriber-derived family and recipes filed under {@code ec9_processor_assembler/}.
     */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, boolean ec9) {
        var outputs = pattern.getOutputs();
        if (outputs.isEmpty() || outputs.size() > 2) return null;
        if (outputs.stream().anyMatch(output -> output.amount() <= 0)) return null;
        List<SlotDemand> demands = slotDemands(pattern);
        if (demands == null) return null;
        List<ProcessorAssemblerRecipe> declared = level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream()
                .filter(holder -> ec9 || isL1Recipe(holder.id()))
                .map(RecipeHolder::value).toList();
        for (ProcessorAssemblerRecipe recipe : declared) {
            if (recipe.hasItemOutput()
                    && NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(recipe.result().getItem())) continue;
            if (matchesPattern(recipe, demands, outputs)) return recipe;
        }
        return null;
    }

    /** Existing item-only callers remain source-compatible; both machine families answer them. */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, Item output) {
        ProcessorAssemblerRecipe recipe = resolve(level, pattern, true);
        return recipe != null && recipe.hasItemOutput() && recipe.result().is(output) ? recipe : null;
    }

    private static boolean matchesPattern(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
            List<GenericStack> outputs) {
        int expectedOutputs = (recipe.hasItemOutput() ? 1 : 0) + (recipe.hasFluidOutput() ? 1 : 0);
        if (outputs.size() != expectedOutputs) return false;
        long runs = -1;
        boolean sawItem = false;
        boolean sawFluid = false;
        for (GenericStack output : outputs) {
            long perRun;
            boolean matches;
            if (output.what() instanceof AEItemKey item && recipe.hasItemOutput() && !sawItem) {
                sawItem = true;
                matches = item.matches(recipe.result());
                perRun = recipe.result().getCount();
            } else if (output.what() instanceof AEFluidKey fluid && recipe.hasFluidOutput() && !sawFluid) {
                sawFluid = true;
                matches = fluid.matches(recipe.fluidResult());
                perRun = recipe.fluidResult().getAmount();
            } else {
                return false;
            }
            if (!matches || perRun <= 0 || output.amount() <= 0 || output.amount() % perRun != 0) return false;
            long outputRuns = output.amount() / perRun;
            if (runs >= 0 && runs != outputRuns) return false;
            runs = outputRuns;
        }
        if (runs <= 0) return false;
        List<ItemStack[]> units = unitsFor(recipe, demands, runs);
        return units != null && recipe.matchesUnits(units);
    }

    /**
     * The recipe's own units: each slot's demand divided by the integral run count shared by all outputs.
     * AE2 stores repeated ingredients and batch count in the same per-slot multiplier; the output side
     * disambiguates them.
     */
    @Nullable
    private static List<ItemStack[]> unitsFor(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
            long runs) {
        if (runs <= 0) return null;
        List<ItemStack[]> units = new ArrayList<>();
        for (SlotDemand demand : demands) {
            if (demand.demand() % runs != 0) return null;
            long perRun = demand.demand() / runs;
            if (perRun <= 0 || perRun > ProcessorAssemblerRecipe.MAX_INPUTS) return null;
            for (long unit = 0; unit < perRun; unit++) units.add(demand.alternatives());
        }
        return units.size() > ProcessorAssemblerRecipe.MAX_INPUTS ? null : units;
    }

    /** What one pattern slot accepts, and how many it asks for before the batch factor is divided out. */
    private record SlotDemand(ItemStack[] alternatives, long demand) {
    }

    /**
     * One entry per pattern slot: its candidate items and its raw demand, which is the candidate amount
     * times the slot multiplier. All candidates of a slot must ask for the same number, or the slot has no
     * single demand to compare against the recipe.
     */
    @Nullable
    private static List<SlotDemand> slotDemands(IPatternDetails pattern) {
        List<SlotDemand> demands = new ArrayList<>();
        for (IPatternDetails.IInput slot : pattern.getInputs()) {
            GenericStack[] possible = slot.getPossibleInputs();
            if (possible.length == 0) return null;
            long amount = possible[0].amount();
            if (amount <= 0) return null;
            ItemStack[] alternatives = new ItemStack[possible.length];
            for (int candidate = 0; candidate < possible.length; candidate++) {
                if (!(possible[candidate].what() instanceof AEItemKey key)
                        || possible[candidate].amount() != amount) return null;
                alternatives[candidate] = key.toStack();
            }
            demands.add(new SlotDemand(alternatives, amount * slot.getMultiplier()));
        }
        return demands;
    }

    /** Output and the sorted registry names of every ingredient choice. */
    private static String signature(ProcessorAssemblerRecipe recipe) {
        String item = recipe.hasItemOutput()
                ? BuiltInRegistries.ITEM.getKey(recipe.result().getItem()).toString() : "<no-item>";
        String fluid = recipe.hasFluidOutput()
                ? recipe.fluidResult().getFluid().toString() + "@" + recipe.fluidResult().getComponentsPatch()
                        + "x" + recipe.fluidResult().getAmount()
                : "<no-fluid>";
        return item + "/" + fluid + Arrays.toString(recipe.ingredients().stream()
                .flatMap(ingredient -> Arrays.stream(ingredient.getItems()))
                .map(stack -> BuiltInRegistries.ITEM.getKey(stack.getItem()).toString())
                .sorted()
                .toArray());
    }

    /** Only a single-item ingredient can be traced back; tags and multi-item choices are kept as-is. */
    private static Ingredient substitute(Ingredient ingredient, Map<Item, Ingredient> inscribedFrom) {
        ItemStack[] choices = ingredient.getItems();
        if (choices.length != 1 || choices[0].isEmpty()) return ingredient;
        return inscribedFrom.getOrDefault(choices[0].getItem(), ingredient);
    }
}

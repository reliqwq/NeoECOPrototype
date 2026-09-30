package cn.dancingsnow.neoecoprototype.recipe;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
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
        var inscriber = level.getRecipeManager().getAllRecipesFor(AERecipeTypes.INSCRIBER);

        // printed part -> the material inscribed into it
        Map<Item, Ingredient> inscribedFrom = new HashMap<>();
        for (var holder : inscriber) {
            InscriberRecipe recipe = holder.value();
            if (recipe.getProcessType() != InscriberProcessType.INSCRIBE) continue;
            ItemStack result = recipe.getResultItem();
            if (!result.isEmpty()) inscribedFrom.put(result.getItem(), recipe.getMiddleInput());
        }

        Set<String> covered = existing.stream().map(ProcessorAssemblerRecipes::signature)
                .collect(java.util.stream.Collectors.toSet());
        List<ProcessorAssemblerRecipe> derived = new ArrayList<>();
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
            if (!covered.contains(signature(recipe))) derived.add(recipe);
        }
        return derived;
    }

    /**
     * The addon recipe an AE2 processing pattern asks for, or null when nothing covers it.
     *
     * <p>Declared recipes are tried first because they are curated; the inscriber derivation is the
     * fallback that lets other packs' press recipes work without a data file.
     */
    @Nullable
    public static ProcessorAssemblerRecipe resolve(Level level, IPatternDetails pattern, Item output) {
        var outputs = pattern.getOutputs();
        if (outputs.size() != 1 || !(outputs.get(0).what() instanceof AEItemKey)) return null;
        List<SlotDemand> demands = slotDemands(pattern);
        if (demands == null) return null;
        long outputAmount = outputs.get(0).amount();
        List<ProcessorAssemblerRecipe> declared = level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (ProcessorAssemblerRecipe recipe : declared) {
            if (matchesPattern(recipe, demands, outputAmount, output)) return recipe;
        }
        if (NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER.get()) {
            for (ProcessorAssemblerRecipe recipe : derived(level, declared)) {
                if (matchesPattern(recipe, demands, outputAmount, output)) return recipe;
            }
        }
        return null;
    }

    private static boolean matchesPattern(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
            long outputAmount, Item output) {
        if (recipe.result().getItem() != output) return false;
        List<ItemStack[]> units = unitsFor(recipe, demands, outputAmount);
        return units != null && recipe.matchesUnits(units);
    }

    /**
     * The recipe's own units: each slot's demand divided by how many times the pattern runs that recipe.
     *
     * <p>AE2 stores two different things in the same per-slot multiplier -- "this slot needs two of the
     * item" (a pattern that merged the repeated ingredient) and "craft 64 at once" (a batched pattern).
     * What tells them apart is the pattern's output amount: a 2+1 pattern of a one-output recipe asks for
     * two and one, while a pattern crafting 64 of that same recipe asks for one of each. Dividing the
     * output side out is what lets both work; reading the multiplier as either meaning on its own broke
     * one of the two.
     */
    @Nullable
    private static List<ItemStack[]> unitsFor(ProcessorAssemblerRecipe recipe, List<SlotDemand> demands,
            long outputAmount) {
        int resultCount = recipe.result().getCount();
        if (resultCount <= 0 || outputAmount % resultCount != 0) return null;
        long runs = outputAmount / resultCount;
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

    /** Output item plus the sorted registry names of every ingredient choice. */
    private static String signature(ProcessorAssemblerRecipe recipe) {
        return recipe.result().getItem() + Arrays.toString(recipe.ingredients().stream()
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

package cn.dancingsnow.neoecoprototype.recipe;

import appeng.recipes.AERecipeTypes;
import appeng.recipes.handlers.InscriberProcessType;
import appeng.recipes.handlers.InscriberRecipe;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;

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

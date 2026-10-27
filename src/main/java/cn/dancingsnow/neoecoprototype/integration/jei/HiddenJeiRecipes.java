package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoprototype.integration.HiddenEasterEggRecipes;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.world.item.crafting.RecipeHolder;

final class HiddenJeiRecipes {
    private HiddenJeiRecipes() {}

    static <T> void hide(IRecipeManager manager, RecipeType<T> type) {
        if (!RecipeHolder.class.isAssignableFrom(type.getRecipeClass())) return;
        var hidden = manager.createRecipeLookup(type).includeHidden().get()
            .filter(HiddenEasterEggRecipes::isHiddenRecipe).toList();
        if (!hidden.isEmpty()) manager.hideRecipes(type, hidden);
    }
}

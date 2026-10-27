package cn.dancingsnow.neoecoprototype.integration;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;

/** Filters the viewer only; the server keeps the real machine recipe. */
public final class HiddenEasterEggRecipes {
    public static final ResourceLocation INFINITE_SAND = ResourceLocation.fromNamespaceAndPath(
        "neoecoprototype", "compat/useless_stretcher/infinite_sand_cell");

    private HiddenEasterEggRecipes() {}

    public static boolean isHidden(ResourceLocation id) {
        return INFINITE_SAND.equals(id) || id != null && id.getNamespace().equals("jei")
            && id.getPath().equals("/" + INFINITE_SAND.getNamespace() + "/" + INFINITE_SAND.getPath());
    }

    public static boolean isHiddenRecipe(Object recipe) {
        return recipe instanceof RecipeHolder<?> holder && isHidden(holder.id());
    }
}

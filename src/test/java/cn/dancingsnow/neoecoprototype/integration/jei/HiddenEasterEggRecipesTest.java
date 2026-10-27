package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoprototype.integration.HiddenEasterEggRecipes;
import cn.dancingsnow.neoecoprototype.integration.mekanism.MilkInfusionTestBootstrap;
import java.util.List;
import java.util.stream.Stream;
import mezz.jei.api.recipe.IRecipeLookup;
import mezz.jei.api.recipe.IRecipeManager;
import mezz.jei.api.recipe.RecipeType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class HiddenEasterEggRecipesTest {
    @BeforeAll static void bootstrap() { MilkInfusionTestBootstrap.initialize(); }

    @Test void hidesTheRecipeHolderAndItsEmiBridgeIdWhileKeepingNormalRecipes() {
        IRecipeManager manager = mock(IRecipeManager.class);
        RecipeType<RecipeHolder> type = new RecipeType<>(ResourceLocation.parse("mekanism:metallurgic_infusing"), RecipeHolder.class);
        IRecipeLookup<RecipeHolder> lookup = mock(IRecipeLookup.class);
        var hidden = new RecipeHolder<>(HiddenEasterEggRecipes.INFINITE_SAND, mock(Recipe.class));
        var normal = new RecipeHolder<>(ResourceLocation.parse("mekanism:alloy/infused"), mock(Recipe.class));
        when(manager.createRecipeLookup(type)).thenReturn(lookup);
        when(lookup.includeHidden()).thenReturn(lookup);
        when(lookup.get()).thenReturn(Stream.of(hidden, normal));
        HiddenJeiRecipes.hide(manager, type);
        verify(manager).hideRecipes(type, List.of(hidden));
        assertTrue(HiddenEasterEggRecipes.isHidden(ResourceLocation.parse(
            "jei:/neoecoprototype/compat/useless_stretcher/infinite_sand_cell")));
        assertFalse(HiddenEasterEggRecipes.isHidden(null));
        assertFalse(HiddenEasterEggRecipes.isHiddenRecipe(normal));
    }
}

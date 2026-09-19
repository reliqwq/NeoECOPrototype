package cn.dancingsnow.neoecoprototype.integration.jei;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/** Shows the assembler's accepted combinations, including the ones derived from AE2's inscriber. */
public class ProcessorAssemblerCategory implements IRecipeCategory<ProcessorAssemblerRecipe> {
    public static final RecipeType<ProcessorAssemblerRecipe> TYPE =
            RecipeType.create(NeoECOPrototype.MOD_ID, "processor_assembler", ProcessorAssemblerRecipe.class);

    private final IDrawable icon;

    public ProcessorAssemblerCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK,
                new ItemStack(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get()));
    }

    @Override
    public RecipeType<ProcessorAssemblerRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("block.neoecoprototype.simplify_stonecutting_assembler");
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    // JEI requires an explicit size whenever the category supplies no background.
    @Override
    public int getWidth() {
        return 110;
    }

    @Override
    public int getHeight() {
        return 20;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, ProcessorAssemblerRecipe recipe, IFocusGroup focuses) {
        List<Ingredient> ingredients = recipe.ingredients();
        for (int slot = 0; slot < ingredients.size(); slot++) {
            builder.addSlot(RecipeIngredientRole.INPUT, slot * 20, 1).addIngredients(ingredients.get(slot));
        }
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 1).addItemStack(recipe.result());
    }
}

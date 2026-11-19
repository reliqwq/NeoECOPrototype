package cn.dancingsnow.neoecoprototype.integration.emi;

import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;

/**
 * One L1 assembler recipe in EMI.
 *
 * <p>The ingredient list is shown as it actually matches: three or four alternatives in any order, so the
 * page draws them as plain input slots and no arrangement is implied. Derived recipes - the ones the config
 * can copy off AE2's inscriber instead of a JSON - have no {@code RecipeHolder} behind them, and EMI is
 * told that rather than given a fake one.
 */
public final class ProcessorAssemblerEmiRecipe extends BasicEmiRecipe {
    /** One slot per ingredient, then the arrow and up to two outputs. */
    private static final int SLOT = 18;
    private static final int ARROW_X = 4 * SLOT + 4;
    private static final int OUTPUT_X = ARROW_X + 24;

    private final RecipeHolder<ProcessorAssemblerRecipe> holder;

    public ProcessorAssemblerEmiRecipe(EmiRecipeCategory category, ResourceLocation id,
                                      ProcessorAssemblerRecipe recipe,
                                      RecipeHolder<ProcessorAssemblerRecipe> holder) {
        super(category, id, OUTPUT_X + SLOT * Math.max(1,
                (recipe.hasItemOutput() ? 1 : 0) + (recipe.hasFluidOutput() ? 1 : 0)), SLOT);
        this.holder = holder;
        for (Ingredient ingredient : recipe.ingredients()) inputs.add(EmiIngredient.of(ingredient));
        if (recipe.hasItemOutput()) outputs.add(EmiStack.of(recipe.result()));
        if (recipe.hasFluidOutput()) {
            var fluid = recipe.fluidResult();
            outputs.add(EmiStack.of(fluid.getFluid(), fluid.getComponentsPatch(), fluid.getAmount()));
        }
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int index = 0; index < inputs.size(); index++) {
            widgets.addSlot(inputs.get(index), index * SLOT, 0);
        }
        widgets.addTexture(EmiTexture.EMPTY_ARROW, ARROW_X, 1);
        for (int index = 0; index < outputs.size(); index++) {
            widgets.addSlot(outputs.get(index), OUTPUT_X + index * SLOT, 0).recipeContext(this);
        }
    }

    @Override
    public RecipeHolder<?> getBackingRecipe() {
        return holder;
    }
}

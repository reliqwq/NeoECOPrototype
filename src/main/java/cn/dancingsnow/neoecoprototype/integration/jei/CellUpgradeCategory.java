package cn.dancingsnow.neoecoprototype.integration.jei;

import appeng.recipes.game.StorageCellUpgradeRecipe;
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
import net.minecraft.world.item.Items;

/**
 * AE2's storage-cell upgrades happen in a crafting grid, and the recipe answers {@code isSpecial()} - the one
 * shape JEI's own Crafting page will not draw. Nothing else draws it either: AE2 19.2.17 has no JEI plugin at
 * all (its integration modules are curios, emi, igtooltip, itemlists, jade, rei, theoneprobe, wthit, and the
 * only "jei" path in the jar is a leftover texture), the two classes that name this recipe type are AE2's own
 * EMI and REI plugins, and ae2jeiintegration 1.2.1 registers eight categories, none of them this one. So the
 * recipes load and no viewer in JEI can show them - measured 2026-11-16, when the expanded small-bulk cells
 * had no recipe row while their JSON was in the jar and their conditions passed.
 *
 * <p>Two in, two out. The upgrade spends the old component and hands a different one back, and a page with a
 * single output would read as if the component were consumed. The four answers come from AE2's own public
 * getters, which is what its EMI and REI pages use too.
 */
public class CellUpgradeCategory implements IRecipeCategory<StorageCellUpgradeRecipe> {
    /** The uid is AE2's serializer id, so a page from elsewhere for the same class merges into this one. */
    public static final RecipeType<StorageCellUpgradeRecipe> TYPE =
            RecipeType.create("ae2", "storage_cell_upgrade", StorageCellUpgradeRecipe.class);

    private final IDrawable icon;

    public CellUpgradeCategory(IGuiHelper guiHelper) {
        icon = guiHelper.createDrawableIngredient(VanillaTypes.ITEM_STACK, new ItemStack(Items.CRAFTING_TABLE));
    }

    @Override
    public RecipeType<StorageCellUpgradeRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.neoecoprototype.cell_upgrade");
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
    public void setRecipe(IRecipeLayoutBuilder builder, StorageCellUpgradeRecipe recipe, IFocusGroup focuses) {
        builder.addSlot(RecipeIngredientRole.INPUT, 1, 1)
                .addItemStack(new ItemStack(recipe.getInputCell()));
        builder.addSlot(RecipeIngredientRole.INPUT, 21, 1)
                .addItemStack(new ItemStack(recipe.getInputComponent()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 68, 1)
                .addItemStack(new ItemStack(recipe.getResultComponent()));
        builder.addSlot(RecipeIngredientRole.OUTPUT, 90, 1)
                .addItemStack(new ItemStack(recipe.getResultCell()));
    }
}

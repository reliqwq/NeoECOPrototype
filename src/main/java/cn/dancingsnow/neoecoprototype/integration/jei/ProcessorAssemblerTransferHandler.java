package cn.dancingsnow.neoecoprototype.integration.jei;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.integration.modules.itemlists.EncodingHelper;
import appeng.menu.me.items.PatternEncodingTermMenu;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.transfer.IRecipeTransferError;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandler;
import mezz.jei.api.recipe.transfer.IRecipeTransferHandlerHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Lets JEI's transfer button encode one of our assembler recipes into a processing pattern.
 *
 * <p>Why it has to be ours: ae2jeiintegration registers a <em>universal</em> handler for the encoding
 * terminal, and it collects the recipe's inputs through a set keyed by item, so two identical ingredients
 * collapse into one slot -- our 3-unit recipes arrive at the machine as a 2-unit pattern, which the
 * assembler then rightly refuses. JEI prefers a category-specific handler over a universal one, so
 * registering this against {@link ProcessorAssemblerCategory#TYPE} wins, and every ingredient entry becomes
 * its own slot, which is exactly the shape {@code ProcessorAssemblerRecipes.resolve} expects.
 *
 * <p>{@code EncodingHelper} is AE2's internal encoding entry point; it is the only way to fill the terminal
 * the way AE2 itself does, and ae2jeiintegration already calls it too.
 */
public final class ProcessorAssemblerTransferHandler
        implements IRecipeTransferHandler<PatternEncodingTermMenu, ProcessorAssemblerRecipe> {
    private final IRecipeTransferHandlerHelper transferHelper;

    public ProcessorAssemblerTransferHandler(IRecipeTransferHandlerHelper transferHelper) {
        this.transferHelper = transferHelper;
    }

    @Override
    public Class<? extends PatternEncodingTermMenu> getContainerClass() {
        return PatternEncodingTermMenu.class;
    }

    @Override
    public Optional<MenuType<PatternEncodingTermMenu>> getMenuType() {
        return Optional.of(PatternEncodingTermMenu.TYPE);
    }

    @Override
    public RecipeType<ProcessorAssemblerRecipe> getRecipeType() {
        return ProcessorAssemblerCategory.TYPE;
    }

    /**
     * The units handed to AE2: one entry per ingredient, each offering that ingredient's own items. The
     * count is always one because a vanilla {@link Ingredient} carries no amount -- repeating an ingredient
     * in the list is what asks for two of it.
     */
    @Override
    @Nullable
    public IRecipeTransferError transferRecipe(PatternEncodingTermMenu menu, ProcessorAssemblerRecipe recipe,
            IRecipeSlotsView recipeSlotsView, Player player, boolean simulate, boolean handleErrors) {
        List<List<GenericStack>> inputs = new ArrayList<>(recipe.ingredients().size());
        for (Ingredient ingredient : recipe.ingredients()) {
            ItemStack[] choices = ingredient.getItems();
            if (choices.length == 0) {
                // An empty tag: nothing could fill this slot, so say so instead of encoding a pattern the
                // assembler would only refuse later.
                return transferHelper.createUserErrorWithTooltip(
                        Component.translatable("jei." + NeoECOPrototype.MOD_ID + ".processor_assembler.no_match"));
            }
            List<GenericStack> slot = new ArrayList<>(choices.length);
            for (ItemStack choice : choices) slot.add(new GenericStack(AEItemKey.of(choice), 1));
            inputs.add(slot);
        }
        if (!simulate) {
            EncodingHelper.encodeProcessingRecipe(menu, inputs,
                    List.of(new GenericStack(AEItemKey.of(recipe.result()), recipe.result().getCount())));
        }
        return null;
    }
}

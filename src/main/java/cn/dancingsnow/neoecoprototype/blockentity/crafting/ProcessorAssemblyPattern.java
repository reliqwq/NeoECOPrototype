package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * Presents an addon processor recipe, carried by an otherwise unsupported pattern (a processing
 * pattern), as the pattern kind AE2's molecular assembler can actually run. The recipe itself is
 * shapeless, so the ingredients are laid into the grid in the pattern's own order.
 */
record ProcessorAssemblyPattern(IPatternDetails delegate, ProcessorAssemblerRecipe recipe)
        implements IMolecularAssemblerSupportedPattern {

    @Override
    public AEItemKey getDefinition() {
        return delegate.getDefinition();
    }

    @Override
    public IInput[] getInputs() {
        return delegate.getInputs();
    }

    @Override
    public java.util.List<GenericStack> getOutputs() {
        return delegate.getOutputs();
    }

    @Override
    public boolean isSlotEnabled(int slot) {
        return slot < recipe.ingredients().size();
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        ItemStack stack = key.toStack();
        return recipe.ingredients().stream().anyMatch(ingredient -> ingredient.test(stack));
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] inputs, CraftingGridAccessor accessor) {
        for (int slot = 0; slot < getInputs().length; slot++) {
            GenericStack[] possible = getInputs()[slot].getPossibleInputs();
            if (possible.length == 0 || !(possible[0].what() instanceof AEItemKey key)) continue;
            accessor.set(slot, key.toStack());
            take(inputs, key);
        }
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        return recipe.result().copy();
    }

    private static void take(KeyCounter[] inputs, AEItemKey key) {
        for (KeyCounter counter : inputs) {
            if (counter.get(key) > 0) {
                counter.remove(key, 1);
                return;
            }
        }
    }
}

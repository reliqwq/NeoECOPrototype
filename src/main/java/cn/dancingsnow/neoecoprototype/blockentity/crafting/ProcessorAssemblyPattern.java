package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.HashMap;
import java.util.Map;

/**
 * Presents an addon processor recipe, carried by an otherwise unsupported pattern (a processing
 * pattern), as the pattern kind AE2's molecular assembler can actually run.
 */
public record ProcessorAssemblyPattern(IPatternDetails delegate, ProcessorAssemblerRecipe recipe)
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
        return slot < 9;
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        return recipe.ingredients().stream().anyMatch(ingredient -> ingredient.test(key.toStack()));
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] inputs, CraftingGridAccessor accessor) {
        Map<AEItemKey, Long> required = new HashMap<>();
        for (IInput input : getInputs()) {
            long perCraft = perCraft(input);
            GenericStack selected = selectAvailableInput(inputs, input.getPossibleInputs(), perCraft);
            if (selected == null || !(selected.what() instanceof AEItemKey key)) {
                throw new IllegalStateException("No available item input for processor assembler");
            }
            required.merge(key, perCraft, Long::sum);
        }

        long batchCount = Long.MAX_VALUE;
        for (var entry : required.entrySet()) {
            batchCount = Math.min(batchCount, available(inputs, entry.getKey()) / entry.getValue());
        }
        if (batchCount <= 0 || batchCount == Long.MAX_VALUE) {
            throw new IllegalStateException("No complete processor assembler batch is available");
        }

        int gridSlot = 0;
        for (var entry : required.entrySet()) {
            long total = entry.getValue() * batchCount;
            if (!take(inputs, entry.getKey(), total)) {
                throw new IllegalStateException("Could not consume processor assembler input " + entry.getKey());
            }
            while (total > 0) {
                if (gridSlot >= 9) {
                    throw new IllegalStateException("Processor assembler needs more than 9 grid slots");
                }
                int stackSize = (int) Math.min(64, total);
                accessor.set(gridSlot++, entry.getKey().toStack(stackSize));
                total -= stackSize;
            }
        }
    }

    /**
     * How many of one input slot a single craft consumes. AE2 keeps the repeat count in the slot's
     * multiplier rather than in the candidate amount, so a slot that needs two of the item reads as
     * amount 1 with multiplier 2 -- reading only the amount is what made the assembler insert one.
     */
    private static long perCraft(IInput input) {
        GenericStack[] possible = input.getPossibleInputs();
        return possible.length == 0 ? 0 : possible[0].amount() * input.getMultiplier();
    }

    private static GenericStack selectAvailableInput(KeyCounter[] inputs, GenericStack[] possible, long perCraft) {
        for (GenericStack candidate : possible) {
            if (candidate.what() instanceof AEItemKey key && available(inputs, key) >= perCraft) {
                return candidate;
            }
        }
        return null;
    }

    private static long available(KeyCounter[] inputs, AEItemKey key) {
        long total = 0;
        for (KeyCounter counter : inputs) total += counter.get(key);
        return total;
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        long supplied = 0;
        for (int slot = 0; slot < input.size(); slot++) supplied += input.getItem(slot).getCount();
        long perBatch = 0;
        for (IInput inputSlot : getInputs()) perBatch += perCraft(inputSlot);
        long batchCount = perBatch == 0 ? 0 : supplied / perBatch;
        if (batchCount <= 0) return ItemStack.EMPTY;
        // The pattern says what one of its runs yields, which is the recipe's output scaled by its batch
        // factor: a 64x processor pattern asks for 64 per execution, and answering it with the recipe's
        // single processor would silently drop 63 of them.
        long perRun = getOutputs().isEmpty() ? recipe.result().getCount() : getOutputs().get(0).amount();
        ItemStack result = recipe.result().copy();
        result.setCount((int) Math.min(result.getMaxStackSize(), batchCount * perRun));
        return result;
    }

    private static boolean take(KeyCounter[] inputs, AEItemKey key, long amount) {
        long remaining = amount;
        for (KeyCounter counter : inputs) {
            long taken = Math.min(counter.get(key), remaining);
            if (taken <= 0) continue;
            counter.remove(key, taken);
            remaining -= taken;
            if (remaining == 0) return true;
        }
        return false;
    }
}

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
public record ProcessorAssemblyPattern(IPatternDetails delegate, ProcessorAssemblerRecipe recipe,
        OutputSink surplusSink) implements IMolecularAssemblerSupportedPattern {
    /** The assembler's crafting grid, which is also the most material one cycle may hold. */
    private static final int GRID_SLOTS = 9;

    /** Where the over-amount of a finished cycle goes: the machine hands it to its own network. */
    @FunctionalInterface
    public interface OutputSink {
        void deliver(AEItemKey what, long amount);
    }

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
        return slot < GRID_SLOTS;
    }

    @Override
    public boolean isItemValid(int slot, AEItemKey key, Level level) {
        return recipe.ingredients().stream().anyMatch(ingredient -> ingredient.test(key.toStack()));
    }

    @Override
    public void fillCraftingGrid(KeyCounter[] inputs, CraftingGridAccessor accessor) {
        Map<AEItemKey, Long> required = new HashMap<>();
        if (!requiredFor(inputs, required)) {
            throw new IllegalStateException("No available item input for processor assembler");
        }
        long batchCount = fittingRuns(required, inputs);
        if (batchCount <= 0) {
            throw new IllegalStateException("No processor assembler run fits the grid right now");
        }

        int gridSlot = 0;
        for (var entry : required.entrySet()) {
            long total = entry.getValue() * batchCount;
            if (!take(inputs, entry.getKey(), total)) {
                throw new IllegalStateException("Could not consume processor assembler input " + entry.getKey());
            }
            int limit = stackLimit(entry.getKey());
            while (total > 0) {
                int stackSize = (int) Math.min(limit, total);
                accessor.set(gridSlot++, entry.getKey().toStack(stackSize));
                total -= stackSize;
            }
        }
    }

    /** What one run consumes, merged per item the way AE2 merges a repeated ingredient. */
    private boolean requiredFor(KeyCounter[] inputs, Map<AEItemKey, Long> required) {
        for (IInput input : getInputs()) {
            long perCraft = perCraft(input);
            GenericStack selected = selectAvailableInput(inputs, input.getPossibleInputs(), perCraft);
            if (selected == null || !(selected.what() instanceof AEItemKey key)) return false;
            required.merge(key, perCraft, Long::sum);
        }
        return true;
    }

    /**
     * How many runs the machine could take from these contents right now, or zero when it could take none.
     *
     * <p>Ask before filling: anything {@link #fillCraftingGrid} throws escapes through AE2's grid tick and
     * takes the server down with it, so a batch that does not fit has to be refused by the caller instead.
     */
    public long fittingRuns(KeyCounter[] inputs) {
        Map<AEItemKey, Long> required = new HashMap<>();
        return requiredFor(inputs, required) ? fittingRuns(required, inputs) : 0;
    }

    private long fittingRuns(Map<AEItemKey, Long> required, KeyCounter[] inputs) {
        long batchCount = runsPerCycle();
        for (var entry : required.entrySet()) {
            batchCount = Math.min(batchCount, available(inputs, entry.getKey()) / entry.getValue());
        }
        long low = 0;
        long high = batchCount;
        while (low < high) {
            long middle = low + (high - low + 1) / 2;
            if (gridSlots(required, middle) <= GRID_SLOTS) low = middle;
            else high = middle - 1;
        }
        return low;
    }

    /** Grid slots a batch needs: every item is carried in stacks of its own size, so partial stacks cost. */
    private static long gridSlots(Map<AEItemKey, Long> required, long runs) {
        long slots = 0;
        for (var entry : required.entrySet()) {
            int limit = stackLimit(entry.getKey());
            slots += (entry.getValue() * runs + limit - 1) / limit;
        }
        return slots;
    }

    /** How many of one item may share a slot: one stack of that item, never more. */
    private static int stackLimit(AEItemKey key) {
        return key.toStack(1).getMaxStackSize();
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

    /** What one run of the pattern yields, which is the amount the pattern was encoded with. */
    private long outputPerRun() {
        return getOutputs().isEmpty() ? recipe.result().getCount() : getOutputs().get(0).amount();
    }

    /** How many runs one cycle can hand over: as many as fit into a single stack of the output. */
    private long runsPerCycle() {
        long perRun = outputPerRun();
        return perRun <= 0 ? 1 : Math.max(1, recipe.result().getMaxStackSize() / perRun);
    }

    /**
     * What the grid content pays for, including the pattern's own batch factor: a 64x processor pattern
     * asks for 64 per execution, and answering it with the recipe's single processor would silently drop
     * 63 of them.
     */
    private long cycleOutput(net.minecraft.world.item.crafting.CraftingInput input) {
        long supplied = 0;
        for (int slot = 0; slot < input.size(); slot++) supplied += input.getItem(slot).getCount();
        long perBatch = 0;
        for (IInput inputSlot : getInputs()) perBatch += perCraft(inputSlot);
        long batchCount = perBatch == 0 ? 0 : supplied / perBatch;
        return batchCount <= 0 ? 0 : batchCount * outputPerRun();
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        long total = cycleOutput(input);
        if (total <= 0) return ItemStack.EMPTY;
        ItemStack result = recipe.result().copy();
        result.setCount((int) Math.min(result.getMaxStackSize(), total));
        return result;
    }

    /**
     * Hands over what one ejection cannot: the cycle ends with a single ItemStack, and the game will not
     * store more of an item than one stack holds (its codec errors on load), so the remainder goes to the
     * sink instead of being quietly thrown away with the grid.
     *
     * <p>This is the only place a finished cycle can act, because AE2's hasMats() probe calls
     * {@link #assemble} to ask whether a run is possible at all -- handing items out there would duplicate
     * them on a question. AE2 calls this method exactly once per completed cycle.
     */
    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(
            net.minecraft.world.item.crafting.CraftingInput input) {
        long total = cycleOutput(input);
        int ejected = (int) Math.min(recipe.result().getMaxStackSize(), total);
        if (total > ejected) {
            surplusSink.deliver(AEItemKey.of(recipe.result().getItem()), total - ejected);
        }
        return net.minecraft.core.NonNullList.withSize(input.size(), ItemStack.EMPTY);
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

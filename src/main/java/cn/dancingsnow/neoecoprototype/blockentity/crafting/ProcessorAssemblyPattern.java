package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

/** Presents an addon processor recipe as the pattern kind AE2's molecular assembler can run. */
public record ProcessorAssemblyPattern(IPatternDetails delegate, ProcessorAssemblerRecipe recipe,
        OutputSink surplusSink, FluidOutputSink fluidSink) implements IMolecularAssemblerSupportedPattern {
    private static final int GRID_SLOTS = 9;

    public ProcessorAssemblyPattern(IPatternDetails delegate, ProcessorAssemblerRecipe recipe, OutputSink surplusSink) {
        this(delegate, recipe, surplusSink, (fluid, amount) -> {});
    }

    @FunctionalInterface
    public interface OutputSink {
        void deliver(AEItemKey what, long amount);
    }

    @FunctionalInterface
    public interface FluidOutputSink {
        void deliver(AEFluidKey fluid, long amount);
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

    public boolean hasFluidOutput() {
        return recipe.hasFluidOutput();
    }

    public boolean isFluidOnly() {
        return recipe.hasFluidOutput() && !recipe.hasItemOutput();
    }

    public boolean hasItemOutput() {
        return recipe.hasItemOutput();
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
            long total = Math.multiplyExact(entry.getValue(), batchCount);
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

    private boolean requiredFor(KeyCounter[] inputs, Map<AEItemKey, Long> required) {
        for (IInput input : getInputs()) {
            long perCraft = perCraft(input);
            GenericStack selected = selectAvailableInput(inputs, input.getPossibleInputs(), perCraft);
            if (selected == null || !(selected.what() instanceof AEItemKey key)) return false;
            required.merge(key, perCraft, Long::sum);
        }
        return true;
    }

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

    private static long gridSlots(Map<AEItemKey, Long> required, long runs) {
        long slots = 0;
        for (var entry : required.entrySet()) {
            int limit = stackLimit(entry.getKey());
            slots += (entry.getValue() * runs + limit - 1) / limit;
        }
        return slots;
    }

    private static int stackLimit(AEItemKey key) {
        return key.toStack(1).getMaxStackSize();
    }

    private static long perCraft(IInput input) {
        GenericStack[] possible = input.getPossibleInputs();
        return possible.length == 0 ? 0 : possible[0].amount() * input.getMultiplier();
    }

    private static GenericStack selectAvailableInput(KeyCounter[] inputs, GenericStack[] possible, long perCraft) {
        for (GenericStack candidate : possible) {
            if (candidate.what() instanceof AEItemKey key && available(inputs, key) >= perCraft) return candidate;
        }
        return null;
    }

    private static long available(KeyCounter[] inputs, AEItemKey key) {
        long total = 0;
        for (KeyCounter counter : inputs) total += counter.get(key);
        return total;
    }

    private long runsPerCycle() {
        long runs = Long.MAX_VALUE;
        if (recipe.hasItemOutput()) runs = Math.min(runs,
                recipe.result().getMaxStackSize() / outputItemPerPattern());
        if (recipe.hasFluidOutput()) runs = Math.min(runs, Integer.MAX_VALUE / fluidOutputPerPattern());
        return Math.max(1, runs);
    }

    private long outputItemPerPattern() {
        for (GenericStack output : getOutputs()) {
            if (output.what() instanceof AEItemKey item && item.matches(recipe.result())) {
                return Math.toIntExact(output.amount());
            }
        }
        return recipe.result().getCount();
    }

    private long fluidOutputPerPattern() {
        for (GenericStack output : getOutputs()) {
            if (output.what() instanceof AEFluidKey fluid && fluid.matches(recipe.fluidResult())) {
                return output.amount();
            }
        }
        return recipe.fluidResult().getAmount();
    }

    public long requestedRuns() {
        long runs = -1;
        for (GenericStack output : getOutputs()) {
            long perRun = output.what() instanceof AEItemKey && recipe.hasItemOutput()
                    ? recipe.result().getCount()
                    : output.what() instanceof AEFluidKey && recipe.hasFluidOutput()
                            ? recipe.fluidResult().getAmount() : 0;
            if (perRun <= 0 || output.amount() <= 0 || output.amount() % perRun != 0) return 0;
            long current = output.amount() / perRun;
            if (runs >= 0 && runs != current) return 0;
            runs = current;
        }
        return Math.max(0, runs);
    }

    private long cycleRuns(net.minecraft.world.item.crafting.CraftingInput input) {
        long supplied = 0;
        for (int slot = 0; slot < input.size(); slot++) supplied += input.getItem(slot).getCount();
        long perBatch = 0;
        for (IInput inputSlot : getInputs()) perBatch += perCraft(inputSlot);
        return perBatch == 0 ? 0 : supplied / perBatch;
    }

    @Override
    public ItemStack assemble(net.minecraft.world.item.crafting.CraftingInput input, Level level) {
        if (!recipe.hasItemOutput()) return ItemStack.EMPTY;
        long total = Math.multiplyExact(cycleRuns(input), outputItemPerPattern());
        if (total <= 0) return ItemStack.EMPTY;
        ItemStack result = recipe.result().copy();
        result.setCount((int) Math.min(result.getMaxStackSize(), total));
        return result;
    }

    /** AE2 calls this once per completed cycle; unlike assemble(), this is not a hasMats probe. */
    @Override
    public net.minecraft.core.NonNullList<ItemStack> getRemainingItems(
            net.minecraft.world.item.crafting.CraftingInput input) {
        long runs = cycleRuns(input);
        if (runs > 0 && recipe.hasItemOutput()) {
            long total = Math.multiplyExact(runs, outputItemPerPattern());
            int ejected = (int) Math.min(recipe.result().getMaxStackSize(), total);
            if (total > ejected) surplusSink.deliver(AEItemKey.of(recipe.result().getItem()), total - ejected);
        }
        if (runs > 0 && recipe.hasFluidOutput()) {
            long total = Math.multiplyExact(runs, fluidOutputPerPattern());
            fluidSink.deliver(AEFluidKey.of(recipe.fluidResult()), total);
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

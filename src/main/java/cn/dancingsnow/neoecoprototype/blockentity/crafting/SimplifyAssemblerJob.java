package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import net.minecraft.core.NonNullList;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.HashMap;
import java.util.Map;

/**
 * One accepted assembler job: the matched recipe, the run count the pushed pattern asked for, and the
 * pattern itself (kept only so the completion event can name it). Unlike the old wrapper this is plain
 * data - the machine owns the whole cycle now, so there is nothing for AE2's crafting grid to run, and
 * nothing here survives a save: an in-flight job is lost on load and providers re-push, exactly the
 * semantics the previous implementation had.
 */
public record SimplifyAssemblerJob(IPatternDetails pattern, ProcessorAssemblerRecipe recipe, long runs) {
    /** AE2 counts one slot's real demand in amount x multiplier. */
    public static long perCraft(IPatternDetails.IInput input) {
        GenericStack[] possible = input.getPossibleInputs();
        return possible.length == 0 ? 0 : possible[0].amount() * input.getMultiplier();
    }

    /** What one run of this recipe produces as an item, or zero when the recipe is fluid-only. */
    public long itemOutputPerRun() {
        return recipe.hasItemOutput() ? recipe.result().getCount() : 0;
    }

    /** The run count the pattern encodes, derived from whichever output half it states. */
    public static long requestedRuns(IPatternDetails pattern, ProcessorAssemblerRecipe recipe) {
        long runs = -1;
        for (GenericStack output : pattern.getOutputs()) {
            long perRun = output.what() instanceof AEItemKey && recipe.hasItemOutput()
                    ? recipe.result().getCount()
                    : output.what() instanceof appeng.api.stacks.AEFluidKey && recipe.hasFluidOutput()
                            ? recipe.fluidResult().getAmount() : 0;
            if (perRun <= 0 || output.amount() <= 0 || output.amount() % perRun != 0) return 0;
            long current = output.amount() / perRun;
            if (runs >= 0 && runs != current) return 0;
            runs = current;
        }
        return Math.max(0, runs);
    }

    /**
     * The job's whole material demand, merged per item key: every pattern slot's amount x multiplier is
     * what one accepted push owns, because a provider pushes exactly the pattern's encoded demand. A
     * 64x pattern of a three-ingredient recipe therefore names 64 of each, not 64 x 64.
     */
    public static Map<AEItemKey, Long> encodedDemand(IPatternDetails pattern) {
        var demand = new HashMap<AEItemKey, Long>();
        for (IPatternDetails.IInput input : pattern.getInputs()) {
            GenericStack[] possible = input.getPossibleInputs();
            if (possible.length == 0) continue;
            long perCraft = possible[0].amount() * input.getMultiplier();
            if (perCraft <= 0 || !(possible[0].what() instanceof AEItemKey key)) continue;
            demand.merge(key, perCraft, Long::sum);
        }
        return demand;
    }

    /**
     * Consumes the job's materials from the pushed counters. The push contract is that a returned true
     * owns everything handed over, and a provider pushes exactly the pattern's demand - so anything left
     * over or missing means a non-standard push, which the caller refuses before calling this.
     *
     * @return false when the counters do not hold exactly one job's demand
     */
    public static boolean consumeJobInputs(IPatternDetails pattern, KeyCounter[] inputs) {
        Map<AEItemKey, Long> demand = encodedDemand(pattern);
        long totalDemand = 0;
        long totalAvailable = 0;
        for (var entry : demand.entrySet()) {
            long available = available(inputs, entry.getKey());
            if (available != entry.getValue()) return false;
            totalDemand += entry.getValue();
            totalAvailable += available;
        }
        for (KeyCounter counter : inputs) {
            for (var entry : counter) {
                if (!demand.containsKey(entry.getKey())) totalAvailable += entry.getLongValue();
            }
        }
        if (totalDemand != totalAvailable) return false;
        for (var entry : demand.entrySet()) {
            if (!take(inputs, entry.getKey(), entry.getValue())) return false;
        }
        return true;
    }

    private static long available(KeyCounter[] inputs, AEItemKey key) {
        long total = 0;
        for (KeyCounter counter : inputs) total += counter.get(key);
        return total;
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

    /**
     * The stacks one run of the recipe's ingredients unfolds into - the display grid's readout of what the
     * machine is working on. Purely cosmetic: the machine's accounting never looks at these.
     */
    public NonNullList<ItemStack> previewStacks() {
        var stacks = NonNullList.withSize(9, ItemStack.EMPTY);
        int slot = 0;
        for (Ingredient ingredient : recipe.ingredients()) {
            ItemStack[] choices = ingredient.getItems();
            if (slot < 9 && choices.length > 0) stacks.set(slot++, choices[0]);
        }
        return stacks;
    }

    /** Total fluid one completion delivers, or an empty stack for item-only recipes. */
    public FluidStack fluidOutput() {
        if (!recipe.hasFluidOutput()) return FluidStack.EMPTY;
        return recipe.fluidResult().copyWithAmount(
                (int) Math.min(Integer.MAX_VALUE, Math.multiplyExact(runs, recipe.fluidResult().getAmount())));
    }

    /** Total item output across all runs, capped to one stack as the ejection path demands. */
    public ItemStack itemOutput() {
        if (!recipe.hasItemOutput()) return ItemStack.EMPTY;
        ItemStack result = recipe.result().copy();
        result.setCount((int) Math.min(result.getMaxStackSize(),
                Math.multiplyExact(runs, result.getCount())));
        return result;
    }

    /** Vanilla container shape for the crafting event, which wants a Container beside the result. */
    public static net.minecraft.world.Container singleSlotContainer(ItemStack stack) {
        return new net.minecraft.world.Container() {
            @Override public int getContainerSize() { return 1; }
            @Override public boolean isEmpty() { return stack.isEmpty(); }
            @Override public ItemStack getItem(int slot) { return slot == 0 ? stack : ItemStack.EMPTY; }
            @Override public ItemStack removeItem(int slot, int amount) { return ItemStack.EMPTY; }
            @Override public ItemStack removeItemNoUpdate(int slot) { return ItemStack.EMPTY; }
            @Override public void setItem(int slot, ItemStack value) { }
            @Override public void setChanged() { }
            @Override public boolean stillValid(net.minecraft.world.entity.player.Player player) { return false; }
            @Override public void clearContent() { }
        };
    }
}

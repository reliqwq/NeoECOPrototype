package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

/** Molecular assembler that also accepts the addon's processor patterns. */
public class SimplifyStonecuttingAssemblerBlockEntity extends MolecularAssemblerBlockEntity {
    /** The last pattern output we logged a refusal for, so a provider retrying every tick logs once. */
    @Nullable
    private AEItemKey refusedOutput;

    public SimplifyStonecuttingAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction direction) {
        if (level == null || pattern instanceof IMolecularAssemblerSupportedPattern) {
            return super.pushPattern(pattern, inputs, direction);
        }
        ProcessorAssemblerRecipe recipe = matchRecipe(pattern);
        if (recipe == null) {
            logRefusal(pattern, "没有对得上的配方");
            return false;
        }
        var plan = new ProcessorAssemblyPattern(pattern, recipe, this::deliverSurplus);
        // Filling the grid throws when the batch cannot be held, and a throw here rides straight out of
        // AE2's grid tick and crashes the world -- so the fit is asked about first and refused quietly.
        if (plan.fittingRuns(inputs) <= 0) {
            logRefusal(pattern, "一批原料摆不进 9 格工作台，或者一次产出装不下一组");
            return false;
        }
        refusedOutput = null;
        return super.pushPattern(plan, inputs, direction);
    }

    /**
     * Where the part of a finished cycle that did not fit into the one ejected stack goes: this grid's
     * storage, since a cycle can only hand over a single stack of an item. Whatever the network will not
     * take falls next to the machine -- the alternative is that the materials simply stop existing.
     */
    private void deliverSurplus(AEItemKey what, long amount) {
        long left = amount;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            left -= grid.getStorageService().getInventory().insert(what, left,
                    Actionable.MODULATE, IActionSource.ofMachine(this));
        }
        if (left <= 0) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            NeoECOPrototype.LOGGER.warn("[stonecutting assembler] had no output for {} x {} at {}",
                    what, left, worldPosition);
            return;
        }
        var center = worldPosition.getCenter();
        int stackSize = what.toStack(1).getMaxStackSize();
        while (left > 0) {
            int count = (int) Math.min(stackSize, left);
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, center.x, center.y, center.z,
                    new ItemStack(what.getItem(), count)));
            left -= count;
        }
    }

    /**
     * Refusing silently is what made this impossible to diagnose from a report: the crafting CPU had
     * already accepted the job, so all the player sees is materials that never move.
     *
     * <p>Logged once per machine per output, because a provider retries a refused pattern every tick.
     * Each slot prints candidates x amount x multiplier -- the multiplier is where AE2 keeps "this slot
     * needs two of the item", and reading past it is exactly how the repeated-ingredient recipes were
     * miscounted.
     */
    private void logRefusal(IPatternDetails pattern, String reason) {
        var outputs = pattern.getOutputs();
        AEItemKey output = outputs.size() == 1 && outputs.get(0).what() instanceof AEItemKey key ? key : null;
        if (output == null || output.equals(refusedOutput)) return;
        refusedOutput = output;
        var slots = new StringBuilder();
        for (IPatternDetails.IInput slot : pattern.getInputs()) {
            GenericStack[] possible = slot.getPossibleInputs();
            slots.append('[').append(possible.length).append('x')
                    .append(possible.length > 0 ? possible[0].amount() : 0).append('x')
                    .append(slot.getMultiplier()).append(']');
        }
        NeoECOPrototype.LOGGER.warn("[stonecutting assembler] refused a pattern at {}: {}；output={} "
                        + "patternClass={} slots={} (每槽 = 候选数 x 每候选数量 x 批量倍数)",
                worldPosition, reason, outputs, pattern.getClass().getSimpleName(), slots);
    }

    /**
     * What the pattern asks for, unless its output is one of the processors the config refuses. The
     * refusal is checked here rather than inside the recipe lookup so the JEI page and the machine agree
     * on one rule.
     */
    @Nullable
    private ProcessorAssemblerRecipe matchRecipe(IPatternDetails pattern) {
        var outputs = pattern.getOutputs();
        if (outputs.size() != 1 || !(outputs.get(0).what() instanceof AEItemKey outputKey)) return null;
        if (NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(outputKey.getItem())) return null;
        return ProcessorAssemblerRecipes.resolve(level, pattern, outputKey.getItem());
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
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

/** Assembler for the addon's processor recipes; it refuses ordinary molecular-assembly patterns. */
public class SimplifyStonecuttingAssemblerBlockEntity extends MolecularAssemblerBlockEntity {
    /** The last pattern output we logged a refusal for, so a provider retrying every tick logs once. */
    @Nullable
    private AEItemKey refusedOutput;

    public SimplifyStonecuttingAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /**
     * AE2's implementation names and icons this machine as {@code AEBlocks.MOLECULAR_ASSEMBLER} - both halves
     * are hard-coded constants that never look at the block state - so an inheriting addon machine handed its
     * own identity to every asker: terminals listed our assembler as "分子装配室" with AE2's item icon.
     */
    @Override
    public appeng.api.implementations.blockentities.PatternContainerGroup getCraftingMachineInfo() {
        var inherited = super.getCraftingMachineInfo();
        return new appeng.api.implementations.blockentities.PatternContainerGroup(
                AEItemKey.of(getBlockState().getBlock()),
                hasCustomName() ? getCustomName() : getBlockState().getBlock().getName(),
                inherited.tooltip());
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction direction) {
        // Inheriting AE2's assembler made this machine a plain molecular assembler to anything that asks
        // by behaviour - a pattern provider included - so an ordinary pattern could be pushed in from the
        // network and run here. It is refused on purpose; see the guard test.
        if (level == null || pattern instanceof ProcessorAssemblyPattern) {
            return super.pushPattern(pattern, inputs, direction);
        }
        ProcessorAssemblerRecipe recipe = matchRecipe(pattern);
        if (recipe == null) {
            logRefusal(pattern, "这台机器只跑处理器装配配方，普通分子装配样板请交给分子装配室");
            return false;
        }
        var plan = new ProcessorAssemblyPattern(pattern, recipe, this::deliverSurplus);
        String refusal = refusalReason(plan, inputs);
        if (refusal != null) {
            logRefusal(pattern, refusal);
            return false;
        }
        refusedOutput = null;
        return super.pushPattern(plan, inputs, direction);
    }

    /**
     * Why this plan cannot run here right now, or null when it can. Asked before AE2 sees the plan:
     * filling the grid throws when the push cannot be consumed whole, and a throw here rides straight
     * out of AE2's grid tick and crashes the world, so both limits are checked and refused quietly.
     *
     * <p>The batch ceiling comes first because it is the one a player can raise: a pattern that is a
     * real recipe and still above {@code processor_assembler_max_runs_per_cycle} has to say so, or the
     * refusal reads as "this recipe does not exist".
     */
    @Nullable
    public String refusalReason(ProcessorAssemblyPattern plan, KeyCounter[] inputs) {
        long runs = plan.requestedRuns();
        int cap = NeoECOPrototypeServerConfig.processorAssemblerMaxRunsPerCycle();
        if (runs > cap) {
            return "样板一次要 " + runs + " 份，超过本机每周期上限 " + cap
                    + " 份（服务端配置 processor_assembler_max_runs_per_cycle）";
        }
        if (plan.fittingRuns(inputs) <= 0) {
            return "一批原料摆不进 9 格工作台，或者一次产出装不下一组";
        }
        return null;
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
                        + "patternClass={} slots={} askedBy={} (每槽 = 候选数 x 每候选数量 x 批量倍数)",
                worldPosition, reason, outputs, pattern.getClass().getSimpleName(), slots, describeCallers());
    }

    /**
     * The first few callers outside our own code, so a refusal says who pushed. Inheriting AE2's assembler
     * makes this machine a target for anything that walks {@code ICraftingMachine}, and which thing walked
     * in is not something the refusal should leave to inference.
     */
    private String describeCallers() {
        var picked = new java.util.ArrayList<String>();
        for (var frame : Thread.currentThread().getStackTrace()) {
            String cls = frame.getClassName();
            if (cls.startsWith("java.") || cls.startsWith("jdk.")
                    || cls.startsWith("cn.dancingsnow.neoecoprototype.")) {
                continue;
            }
            picked.add(cls.substring(cls.lastIndexOf('.') + 1) + "#" + frame.getMethodName());
            if (picked.size() == 4) {
                break;
            }
        }
        return picked.isEmpty() ? "unknown" : String.join(" < ", picked);
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

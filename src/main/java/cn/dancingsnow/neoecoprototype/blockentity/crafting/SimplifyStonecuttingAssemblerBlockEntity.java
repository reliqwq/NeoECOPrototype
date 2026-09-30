package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
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
            logRefusal(pattern);
            return false;
        }
        refusedOutput = null;
        return super.pushPattern(new ProcessorAssemblyPattern(pattern, recipe), inputs, direction);
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
    private void logRefusal(IPatternDetails pattern) {
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
        NeoECOPrototype.LOGGER.warn("[stonecutting assembler] refused a pattern at {}: output={} patternClass={} "
                        + "slots={} (每槽 = 候选数 x 每候选数量 x 批量倍数)",
                worldPosition, outputs, pattern.getClass().getSimpleName(), slots);
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

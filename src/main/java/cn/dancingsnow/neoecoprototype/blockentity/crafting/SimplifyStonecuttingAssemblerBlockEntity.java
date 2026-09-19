package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * Green molecular assembler that additionally runs the addon's shapeless processor recipes. AE2 only
 * lets crafting patterns into the assembler, so a matching processing pattern is wrapped into one it
 * can run; everything else (grid, upgrades, timing, animation, GUI) stays AE2's.
 */
public class SimplifyStonecuttingAssemblerBlockEntity extends MolecularAssemblerBlockEntity {
    public SimplifyStonecuttingAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction direction) {
        if (level == null || pattern instanceof IMolecularAssemblerSupportedPattern) {
            return super.pushPattern(pattern, inputs, direction);
        }
        ProcessorAssemblerRecipe recipe = matchRecipe(pattern);
        return recipe != null
                && super.pushPattern(new ProcessorAssemblyPattern(pattern, recipe), inputs, direction);
    }

    /** The pattern must ask for exactly one recipe's three ingredients and that recipe's output. */
    private ProcessorAssemblerRecipe matchRecipe(IPatternDetails pattern) {
        var outputs = pattern.getOutputs();
        if (pattern.getInputs().length != ProcessorAssemblerRecipe.REQUIRED_INPUTS || outputs.size() != 1
                || !(outputs.get(0).what() instanceof AEItemKey outputKey)) {
            return null;
        }
        if (NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(outputKey.getItem())) {
            return null;
        }
        int size = ProcessorAssemblerRecipe.REQUIRED_INPUTS;
        ItemStack[] stacks = new ItemStack[size];
        for (int slot = 0; slot < size; slot++) {
            GenericStack[] possible = pattern.getInputs()[slot].getPossibleInputs();
            if (possible.length != 1 || possible[0].amount() != 1
                    || !(possible[0].what() instanceof AEItemKey key)) {
                return null;
            }
            stacks[slot] = key.toStack();
        }
        List<ProcessorAssemblerRecipe> declared = level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream().map(RecipeHolder::value).toList();
        for (ProcessorAssemblerRecipe recipe : declared) {
            if (recipe.result().getItem() == outputKey.getItem() && recipe.matches(stacks)) return recipe;
        }
        if (NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER.get()) {
            for (ProcessorAssemblerRecipe recipe : ProcessorAssemblerRecipes.derived(level, declared)) {
                if (recipe.result().getItem() == outputKey.getItem() && recipe.matches(stacks)) return recipe;
            }
        }
        return null;
    }
}

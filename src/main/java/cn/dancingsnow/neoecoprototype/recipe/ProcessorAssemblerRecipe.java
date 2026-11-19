package cn.dancingsnow.neoecoprototype.recipe;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.fluids.FluidStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Shapeless processor recipe: three or four inputs may be inserted in any order. */
public record ProcessorAssemblerRecipe(List<Ingredient> ingredients, ItemStack result, FluidStack fluidResult)
        implements Recipe<ProcessorAssemblerRecipe.Input> {
    public static final int MIN_INPUTS = 3;
    public static final int MAX_INPUTS = 4;
    public static final Codec<List<Ingredient>> INGREDIENTS_CODEC =
            Codec.list(Ingredient.CODEC, MIN_INPUTS, MAX_INPUTS);

    public static final RecipeType<ProcessorAssemblerRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "neoecoprototype:processor_assembler";
        }
    };

    public ProcessorAssemblerRecipe {
        ingredients = List.copyOf(ingredients);
        result = result == null ? ItemStack.EMPTY : result.copy();
        fluidResult = fluidResult == null ? FluidStack.EMPTY : fluidResult.copy();
        if (result.isEmpty() && fluidResult.isEmpty()) {
            throw new IllegalArgumentException("Processor assembler recipe must produce an item or fluid");
        }
    }

    public ProcessorAssemblerRecipe(List<Ingredient> ingredients, ItemStack result) {
        this(ingredients, result, FluidStack.EMPTY);
    }

    public long outputAmountPerRun() {
        return result.isEmpty() ? fluidResult.getAmount() : result.getCount();
    }

    public boolean hasItemOutput() {
        return !result.isEmpty();
    }

    public boolean hasFluidOutput() {
        return !fluidResult.isEmpty();
    }

    /** True when the stacks can be assigned to the ingredients one-to-one, in any order. */
    public boolean matches(ItemStack[] stacks) {
        List<ItemStack[]> units = new ArrayList<>(stacks.length);
        for (ItemStack stack : stacks) units.add(new ItemStack[] { stack });
        return matchesUnits(units);
    }

    /**
     * True when the ingredients can take the pattern's units one-to-one, in any order.
     *
     * <p>A unit is one item the pattern demands, not one pattern slot: AE2 merges repeated ingredients
     * into a single slot carrying an amount, and one slot may offer alternative items. Matching on slot
     * count therefore rejected perfectly good patterns, which the assembler then silently refused.
     */
    public boolean matchesUnits(List<ItemStack[]> units) {
        return units.size() == ingredients.size() && assign(0, units, new boolean[units.size()]);
    }

    /** A unit satisfies an ingredient when any of its alternative items does; which one is chosen later. */
    private boolean assign(int index, List<ItemStack[]> units, boolean[] taken) {
        if (index == ingredients.size()) return true;
        Ingredient ingredient = ingredients.get(index);
        for (int unit = 0; unit < units.size(); unit++) {
            if (taken[unit] || Arrays.stream(units.get(unit)).noneMatch(ingredient::test)) continue;
            taken[unit] = true;
            if (assign(index + 1, units, taken)) return true;
            taken[unit] = false;
        }
        return false;
    }

    @Override
    public boolean matches(Input input, Level level) {
        return matches(input.items().toArray(new ItemStack[0]));
    }

    @Override
    public ItemStack assemble(Input input, HolderLookup.Provider registries) {
        return result.copy();
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return width * height >= MIN_INPUTS;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider registries) {
        return result;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<ProcessorAssemblerRecipe> {
        private static final MapCodec<ProcessorAssemblerRecipe> CODEC = RecordCodecBuilder.mapCodec(instance -> instance.group(
                INGREDIENTS_CODEC.fieldOf("ingredients").forGetter(ProcessorAssemblerRecipe::ingredients),
                ItemStack.OPTIONAL_CODEC.optionalFieldOf("result", ItemStack.EMPTY).forGetter(ProcessorAssemblerRecipe::result),
                FluidStack.OPTIONAL_CODEC.optionalFieldOf("fluid_result", FluidStack.EMPTY)
                        .forGetter(ProcessorAssemblerRecipe::fluidResult)
        ).apply(instance, ProcessorAssemblerRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INGREDIENTS_STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(INGREDIENTS_CODEC);

        private static final StreamCodec<RegistryFriendlyByteBuf, ProcessorAssemblerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        INGREDIENTS_STREAM_CODEC, ProcessorAssemblerRecipe::ingredients,
                        ItemStack.OPTIONAL_STREAM_CODEC, ProcessorAssemblerRecipe::result,
                        FluidStack.OPTIONAL_STREAM_CODEC, ProcessorAssemblerRecipe::fluidResult,
                        ProcessorAssemblerRecipe::new);

        @Override
        public MapCodec<ProcessorAssemblerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, ProcessorAssemblerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }

    public record Input(List<ItemStack> items) implements RecipeInput {
        public Input(ItemStack... items) {
            this(List.of(items));
        }

        @Override
        public ItemStack getItem(int index) {
            return index < items.size() ? items.get(index) : ItemStack.EMPTY;
        }

        @Override
        public int size() {
            return items.size();
        }

        @Override
        public boolean isEmpty() {
            return items.stream().allMatch(ItemStack::isEmpty);
        }
    }
}

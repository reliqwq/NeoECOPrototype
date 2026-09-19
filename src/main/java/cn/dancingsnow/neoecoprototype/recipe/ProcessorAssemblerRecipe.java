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
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeInput;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.List;

/** Shapeless processor recipe: the three inputs may be inserted into the assembler in any order. */
public record ProcessorAssemblerRecipe(List<Ingredient> ingredients, ItemStack result)
        implements Recipe<ProcessorAssemblerRecipe.Input> {
    public static final int REQUIRED_INPUTS = 3;
    public static final Codec<List<Ingredient>> INGREDIENTS_CODEC =
            Codec.list(Ingredient.CODEC, REQUIRED_INPUTS, REQUIRED_INPUTS);

    public static final RecipeType<ProcessorAssemblerRecipe> TYPE = new RecipeType<>() {
        @Override
        public String toString() {
            return "neoecoprototype:processor_assembler";
        }
    };

    public ProcessorAssemblerRecipe {
        ingredients = List.copyOf(ingredients);
    }

    /** True when the stacks can be assigned to the ingredients one-to-one, in any order. */
    public boolean matches(ItemStack[] stacks) {
        return stacks.length == REQUIRED_INPUTS && assign(0, stacks, new boolean[stacks.length]);
    }

    private boolean assign(int index, ItemStack[] stacks, boolean[] taken) {
        if (index == ingredients.size()) return true;
        Ingredient ingredient = ingredients.get(index);
        for (int slot = 0; slot < stacks.length; slot++) {
            if (taken[slot] || !ingredient.test(stacks[slot])) continue;
            taken[slot] = true;
            if (assign(index + 1, stacks, taken)) return true;
            taken[slot] = false;
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
        return width * height >= REQUIRED_INPUTS;
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
                ItemStack.CODEC.fieldOf("result").forGetter(ProcessorAssemblerRecipe::result)
        ).apply(instance, ProcessorAssemblerRecipe::new));

        private static final StreamCodec<RegistryFriendlyByteBuf, List<Ingredient>> INGREDIENTS_STREAM_CODEC =
                ByteBufCodecs.fromCodecWithRegistries(INGREDIENTS_CODEC);

        private static final StreamCodec<RegistryFriendlyByteBuf, ProcessorAssemblerRecipe> STREAM_CODEC =
                StreamCodec.composite(
                        INGREDIENTS_STREAM_CODEC, ProcessorAssemblerRecipe::ingredients,
                        ItemStack.STREAM_CODEC, ProcessorAssemblerRecipe::result,
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

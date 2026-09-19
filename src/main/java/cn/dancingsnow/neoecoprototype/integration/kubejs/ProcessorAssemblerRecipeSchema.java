package cn.dancingsnow.neoecoprototype.integration.kubejs;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import dev.latvian.mods.kubejs.recipe.RecipeKey;
import dev.latvian.mods.kubejs.recipe.component.IngredientComponent;
import dev.latvian.mods.kubejs.recipe.component.ItemStackComponent;
import dev.latvian.mods.kubejs.recipe.component.ListRecipeComponent;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchema;
import dev.latvian.mods.kubejs.recipe.schema.RecipeSchemaRegistry;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;

import java.util.List;

/**
 * Teaches KubeJS which JSON fields of {@code neoecoprototype:processor_assembler} are inputs and which
 * is the output, so scripts can match and remove recipes by field. Deliberately declares no
 * constructor: KubeJS cannot coerce a plain JS array into the recipe's {@code List<Ingredient>}, so
 * scripts add recipes with {@code event.custom({ ... })} instead of a builder call.
 */
public final class ProcessorAssemblerRecipeSchema {
    private static final RecipeKey<List<Ingredient>> INGREDIENTS =
            ListRecipeComponent.create(IngredientComponent.INGREDIENT.instance(), false, false)
                    .inputKey("ingredients");
    private static final RecipeKey<ItemStack> RESULT =
            ItemStackComponent.ITEM_STACK.instance().outputKey("result");

    public static final RecipeSchema SCHEMA = new RecipeSchema(INGREDIENTS, RESULT)
            .uniqueId(RESULT);

    private ProcessorAssemblerRecipeSchema() {
    }

    public static void register(RecipeSchemaRegistry registry) {
        registry.register(NeoECOPrototype.id("processor_assembler"), SCHEMA);
    }
}

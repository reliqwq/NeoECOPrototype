package cn.dancingsnow.neoecoprototype.items;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.GenericStack;
import appeng.util.CraftingRecipeUtil;
import cn.dancingsnow.neoecoae.all.NERecipeTypes;
import cn.dancingsnow.neoecoae.recipe.IntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoae.recipe.LargeIntegratedWorkingStationRecipe;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoprototype.registration.ProcessorAssemblyWorkstationSupport;
import net.minecraft.ChatFormatting;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.fluids.FluidStack;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

/**
 * 创造模式专用：右键随机发一小批编码样板，覆盖本模组与 eco 的全部合成配方、本模组的装配室配方，
 * 以及 eco 的集成工作站 / 大型集成工作站配方。每次只发 {@link #BATCH_SIZE} 张，且跳过背包里已有的
 * （样板不可堆叠，所以"已有"按物品与组件逐张比），重复点几次才会把整套收齐。
 *
 * <p>随机而不是按序号依次发，是因为这套池子有三百多张、背包放不下；按序号发会让玩家必须先清空背包
 * 才能拿到后面的，而随机 + 跳过已有保证每次点都有新东西，且最终一定收敛到全集。</p>
 */
public final class PatternGeneratorItem extends Item {

    /** One inventory row per click: the whole pool is ~4 rows, and a click stays visibly "a handful". */
    public static final int BATCH_SIZE = 9;

    /**
     * Both namespaces are ours to document: we ship the addon, eco is the mod this addon is built on.
     * Recipes added by packs under other namespaces are not ours to hand out.
     */
    private static final String ECO_NAMESPACE = "neoecoae";

    public PatternGeneratorItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            generate(serverLevel, player);
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide());
    }

    /**
     * Right-clicking a block must not also open that block: {@code onItemUseFirst} runs before the block's
     * own use, so the same click works on air and on a machine.
     */
    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            generate(serverLevel, player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide());
    }

    private static void generate(ServerLevel level, Player player) {
        List<ItemStack> pool;
        try {
            pool = buildPool(level);
        } catch (RuntimeException e) {
            NeoECOPrototype.LOGGER.warn("Could not build the pattern pool", e);
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.pattern_generator.failed"), false);
            return;
        }
        if (pool.isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.pattern_generator.empty"), false);
            return;
        }

        // Random per click, seeded from the world so repeated clicks in one session keep moving.
        java.util.Collections.shuffle(pool, new Random(level.getRandom().nextLong()));

        Inventory inventory = player.getInventory();
        List<ItemStack> batch = selectBatch(pool, BATCH_SIZE, inventory::contains);
        if (batch.isEmpty()) {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.pattern_generator.complete", pool.size()), false);
            return;
        }

        int delivered = 0;
        for (ItemStack pattern : batch) {
            if (!inventory.add(pattern)) {
                break;
            }
            delivered++;
        }
        inventory.setChanged();
        if (delivered < batch.size()) {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.pattern_generator.full", delivered), false);
            return;
        }
        player.displayClientMessage(Component.translatable(
                        "message.neoecoprototype.pattern_generator.given", delivered, pool.size())
                .withStyle(ChatFormatting.GRAY), false);
    }

    /**
     * The first {@code size} entries the player does not already hold. Split out because "never hand the
     * same encoded pattern out twice" is the one rule a guard can pin exactly, without relying on the odds
     * of a random draw.
     */
    public static List<ItemStack> selectBatch(List<ItemStack> pool, int size,
                                              java.util.function.Predicate<ItemStack> alreadyHeld) {
        List<ItemStack> batch = new ArrayList<>(size);
        for (ItemStack pattern : pool) {
            if (alreadyHeld.test(pattern)) {
                continue;
            }
            batch.add(pattern);
            if (batch.size() >= size) {
                break;
            }
        }
        return batch;
    }

    /** Every pattern one click can hand out, rebuilt per call: recipes change with a data pack reload. */
    public static List<ItemStack> buildPool(ServerLevel level) {
        List<ItemStack> pool = new ArrayList<>();
        addCraftingPatterns(level, pool);
        addProcessorPatterns(level, pool);
        addEcoStationPatterns(level, pool);
        return pool;
    }

    private static boolean isOurFamily(ResourceLocation id) {
        String namespace = id.getNamespace();
        return namespace.equals(NeoECOPrototype.MOD_ID) || namespace.equals(ECO_NAMESPACE);
    }

    /**
     * One crafting pattern per distinct output. A recipe cannot carry two encodings of the same output
     * usefully, and the pool has a few replicated entries (same result, different id); the lexicographically
     * smallest id wins so the choice is stable across reloads.
     */
    private static void addCraftingPatterns(ServerLevel level, List<ItemStack> pool) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<CraftingRecipe>> holders = new ArrayList<>(
                manager.getAllRecipesFor(RecipeType.CRAFTING));
        holders.sort(Comparator.comparing(holder -> holder.id().toString()));
        Map<AEItemKey, ResourceLocation> seen = new HashMap<>();
        for (RecipeHolder<CraftingRecipe> holder : holders) {
            if (!isOurFamily(holder.id())) {
                continue;
            }
            try {
                CraftingRecipe recipe = holder.value();
                if (recipe.isSpecial()) {
                    continue;
                }
                ItemStack result = recipe.getResultItem(level.registryAccess());
                if (result.isEmpty()) {
                    continue;
                }
                AEItemKey output = AEItemKey.of(result);
                if (output == null || seen.containsKey(output)) {
                    continue;
                }
                pool.add(encodeCrafting(level, holder));
                seen.put(output, holder.id());
            } catch (RuntimeException e) {
                // Recipes that cannot be stated as a 3x3 grid (shaped wider than three, more than nine
                // ingredients) are skipped one by one rather than failing the whole click.
                NeoECOPrototype.LOGGER.warn("Skipping pattern for crafting recipe {}", holder.id(), e);
            }
        }
    }

    private static ItemStack encodeCrafting(ServerLevel level, RecipeHolder<CraftingRecipe> holder) {
        NonNullList<Ingredient> grid = CraftingRecipeUtil.ensure3by3CraftingMatrix(holder.value());
        ItemStack[] inputs = new ItemStack[9];
        boolean substitute = false;
        for (int slot = 0; slot < 9; slot++) {
            Ingredient ingredient = grid.get(slot);
            if (ingredient.isEmpty()) {
                inputs[slot] = ItemStack.EMPTY;
                continue;
            }
            ItemStack[] choices = ingredient.getItems();
            if (choices.length == 0) {
                throw new IllegalArgumentException("ingredient has no items");
            }
            inputs[slot] = choices[0].copyWithCount(1);
            if (choices.length > 1) {
                substitute = true;
            }
        }
        ItemStack result = holder.value().getResultItem(level.registryAccess());
        return PatternDetailsHelper.encodeCraftingPattern(holder, inputs, result, substitute, false);
    }

    /**
     * Our processor-assembly recipes, encoded at the batch the eco workstation adapter declares
     * ({@link ProcessorAssemblyWorkstationSupport#RECIPE_SCALE}): the station divides the pattern's demand
     * by that declared count, so a 1x pattern would only ever state one run, and this family is meant to be
     * pulled in batches. The same encoded shape still runs on our own assembler - it divides the output
     * count out before matching.
     */
    private static void addProcessorPatterns(ServerLevel level, List<ItemStack> pool) {
        RecipeManager manager = level.getRecipeManager();
        List<RecipeHolder<ProcessorAssemblerRecipe>> holders = new ArrayList<>(
                manager.getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get()));
        holders.sort(Comparator.comparing(holder -> holder.id().toString()));
        Map<AEItemKey, ResourceLocation> seen = new HashMap<>();
        for (RecipeHolder<ProcessorAssemblerRecipe> holder : holders) {
            try {
                ProcessorAssemblerRecipe recipe = holder.value();
                if (recipe.result().isEmpty()) {
                    continue;
                }
                AEItemKey output = AEItemKey.of(recipe.result());
                if (output == null || seen.containsKey(output)) {
                    continue;
                }
                List<GenericStack> inputs = new ArrayList<>();
                for (Ingredient ingredient : recipe.ingredients()) {
                    ItemStack[] choices = ingredient.getItems();
                    if (choices.length == 0) {
                        throw new IllegalArgumentException("ingredient has no items");
                    }
                    AEItemKey key = AEItemKey.of(choices[0]);
                    if (key == null) {
                        throw new IllegalArgumentException("ingredient has no item key");
                    }
                    mergeInput(inputs, key, ProcessorAssemblyWorkstationSupport.RECIPE_SCALE);
                }
                long amount = Math.multiplyExact((long) recipe.result().getCount(),
                        ProcessorAssemblyWorkstationSupport.RECIPE_SCALE);
                pool.add(PatternDetailsHelper.encodeProcessingPattern(
                        inputs, List.of(new GenericStack(output, amount))));
                seen.put(output, holder.id());
            } catch (RuntimeException e) {
                NeoECOPrototype.LOGGER.warn("Skipping pattern for processor recipe {}", holder.id(), e);
            }
        }
    }

    private static void mergeInput(List<GenericStack> inputs, AEKey what, long amount) {
        for (int i = 0; i < inputs.size(); i++) {
            GenericStack existing = inputs.get(i);
            if (existing.what().equals(what)) {
                inputs.set(i, new GenericStack(what, existing.amount() + amount));
                return;
            }
        }
        inputs.add(new GenericStack(what, amount));
    }

    /**
     * eco's two working-station families. The large family wraps the same display recipe with an extra
     * matching multiplier, so both go through the same encoder; a pattern that states exactly the recipe's
     * declared amounts runs it once.
     */
    private static void addEcoStationPatterns(ServerLevel level, List<ItemStack> pool) {
        RecipeManager manager = level.getRecipeManager();
        Set<String> seen = new HashSet<>();
        List<RecipeHolder<IntegratedWorkingStationRecipe>> integrated = new ArrayList<>(
                manager.getAllRecipesFor(NERecipeTypes.INTEGRATED_WORKING_STATION.get()));
        integrated.sort(Comparator.comparing(holder -> holder.id().toString()));
        for (RecipeHolder<IntegratedWorkingStationRecipe> holder : integrated) {
            addStationPattern(holder.id(), holder.value(), pool, seen);
        }
        List<RecipeHolder<LargeIntegratedWorkingStationRecipe>> large = new ArrayList<>(
                manager.getAllRecipesFor(NERecipeTypes.LARGE_INTEGRATED_WORKING_STATION.get()));
        large.sort(Comparator.comparing(holder -> holder.id().toString()));
        for (RecipeHolder<LargeIntegratedWorkingStationRecipe> holder : large) {
            addStationPattern(holder.id(), holder.value().display(), pool, seen);
        }
    }

    private static void addStationPattern(ResourceLocation id, IntegratedWorkingStationRecipe recipe,
            List<ItemStack> pool, Set<String> seen) {
        if (!isOurFamily(id)) {
            return;
        }
        try {
            // Not isSpecial(): eco's recipe answers that with a constant true (it means "not offered by
            // vanilla's recipe book"), so filtering on it dropped the whole station family.
            if (recipe.itemOutput().isEmpty() && recipe.fluidOutput().isEmpty()) {
                return;
            }
            List<GenericStack> inputs = new ArrayList<>();
            for (SizedIngredient sized : recipe.inputItems()) {
                ItemStack[] choices = sized.getItems();
                if (choices.length == 0) {
                    throw new IllegalArgumentException("ingredient has no items");
                }
                AEItemKey key = AEItemKey.of(choices[0].copyWithCount(1));
                if (key == null) {
                    throw new IllegalArgumentException("ingredient has no item key");
                }
                mergeInput(inputs, key, sized.count());
            }
            if (!recipe.inputFluid().ingredient().isEmpty()) {
                FluidStack[] fluids = recipe.inputFluid().getFluids();
                if (fluids.length == 0) {
                    throw new IllegalArgumentException("fluid ingredient has no fluids");
                }
                GenericStack fluid = GenericStack.fromFluidStack(fluids[0]);
                if (fluid == null) {
                    throw new IllegalArgumentException("fluid ingredient has no key");
                }
                mergeInput(inputs, fluid.what(), fluid.amount());
            }
            List<GenericStack> outputs = new ArrayList<>();
            if (!recipe.itemOutput().isEmpty()) {
                AEItemKey output = AEItemKey.of(recipe.itemOutput());
                if (output == null) {
                    throw new IllegalArgumentException("output has no item key");
                }
                outputs.add(new GenericStack(output, recipe.itemOutput().getCount()));
            }
            if (!recipe.fluidOutput().isEmpty()) {
                GenericStack fluid = GenericStack.fromFluidStack(recipe.fluidOutput().copy());
                if (fluid == null) {
                    throw new IllegalArgumentException("output has no fluid key");
                }
                outputs.add(fluid);
            }
            if (inputs.isEmpty() || outputs.isEmpty()) {
                return;
            }
            if (!seen.add(signature(inputs, outputs))) {
                return;
            }
            pool.add(PatternDetailsHelper.encodeProcessingPattern(inputs, outputs));
        } catch (RuntimeException e) {
            NeoECOPrototype.LOGGER.warn("Skipping pattern for working station recipe {}", id, e);
        }
    }

    /** The two station families share a display recipe shape, so equality has to be over the real demand. */
    private static String signature(List<GenericStack> inputs, List<GenericStack> outputs) {
        StringBuilder builder = new StringBuilder();
        for (GenericStack stack : inputs) {
            builder.append('i').append(stack.what().getId()).append('x').append(stack.amount()).append(';');
        }
        for (GenericStack stack : outputs) {
            builder.append('o').append(stack.what().getId()).append('x').append(stack.amount()).append(';');
        }
        return builder.toString();
    }
}

package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.IGrid;
import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.IPartHost;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.upgrades.Upgrades;
import appeng.api.util.AEColor;
import appeng.blockentity.crafting.CraftingBlockEntity;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.helpers.InterfaceLogicHost;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.IGridConnectedBlockEntity;
import cn.dancingsnow.neoecoae.all.NEMultiBlocks;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoae.multiblock.placement.WorldPlannedBlock;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.integration.ae2.OversizeInterfaceLogic;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifyMigrationDriveItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyComputationClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityCraftingExecutor;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
import javax.annotation.Nullable;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

/** Minimal runtime smoke test proving the addon registry is available to GameTest. */
@GameTestHolder(NeoECOPrototype.MOD_ID)
@PrefixGameTestTemplate(false)
public final class NeoECOPrototypeGameTests {
    private NeoECOPrototypeGameTests() {
    }

    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void registrationsAreAvailable(GameTestHelper helper) {
        helper.assertTrue(ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get() != null,
                "storage communication interface is not registered");
        helper.assertTrue(ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get() != null,
                "storage network interface is not registered");
        if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")) {
            helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                                    "simplify_chemical_storage_cell_1m")),
                    "optional Mekanism chemical storage cell is not registered");
        }
        helper.assertTrue(BuiltInRegistries.ITEM.containsKey(
                        ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                                "simplify_concrete_storage_cell")),
                "infinite concrete storage cell is not registered");
        helper.succeed();
    }

    /** Verifies the fixed 3/10-type design and all data assets without duplicating eco's backend. */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void smallBulkCellsAndUpgradeRecipeAreRegistered(GameTestHelper helper) {
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL == null
                || ModRegistration.SIMPLIFY_SMALL_BULK_CELL_EXPANDED == null) {
            helper.succeed(); // MegaCells absent: the small bulk family is not registered.
            return;
        }
        var base = (cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem)
                ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get();
        var expanded = (cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem)
                ModRegistration.SIMPLIFY_SMALL_BULK_CELL_EXPANDED.get();
        helper.assertTrue(base.getBytes() == Long.MAX_VALUE && base.getTotalTypes() == 3,
                "base small bulk cell must have Long.MAX_VALUE capacity and three types");
        helper.assertTrue(expanded.getBytes() == Long.MAX_VALUE && expanded.getTotalTypes() == 10,
                "expanded small bulk cell must have Long.MAX_VALUE capacity and ten types");
        helper.assertTrue(base.getKeyType() == appeng.api.stacks.AEKeyType.items()
                        && expanded.getKeyType() == appeng.api.stacks.AEKeyType.items(),
                "small bulk cells must be item-only cells");

        var recipeId = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                "cell_upgrade/simplify_small_bulk_storage_cell_expanded");
        var recipe = helper.getLevel().getRecipeManager().byKey(recipeId);
        helper.assertTrue(recipe.isPresent(), "missing NBT-preserving small bulk upgrade recipe: " + recipeId);
        helper.assertTrue(recipe.orElseThrow().value()
                        instanceof appeng.recipes.game.StorageCellUpgradeRecipe,
                "small bulk upgrade must use AE2 storage_cell_upgrade, not a copy recipe");

        ItemStack populatedBase = new ItemStack(base);
        populatedBase.set(net.minecraft.core.component.DataComponents.CUSTOM_NAME,
                net.minecraft.network.chat.Component.literal("keep-me"));
        var input = CraftingInput.of(2, 1, List.of(populatedBase,
                new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_EXPANSION_CARD.get())));
        var upgrade = (appeng.recipes.game.StorageCellUpgradeRecipe) recipe.orElseThrow().value();
        helper.assertTrue(upgrade.matches(input, helper.getLevel()),
                "small bulk upgrade recipe does not match its base cell and expansion card");
        ItemStack upgraded = upgrade.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(upgraded.is(expanded), "small bulk upgrade did not produce the 10-type cell");
        helper.assertTrue(net.minecraft.network.chat.Component.literal("keep-me").equals(
                        upgraded.get(net.minecraft.core.component.DataComponents.CUSTOM_NAME)),
                "AE2 cell upgrade did not preserve source-cell components");
        helper.succeed();
    }

    /** The small-bulk family opts into AE2 upgrade cards (fuzzy/inverter, one of each). */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void smallBulkUpgradeCardsAreAssociated(GameTestHelper helper) {
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL == null) {
            helper.succeed(); // MegaCells absent: the small bulk family is not registered.
            return;
        }
        var fuzzy = AEItems.FUZZY_CARD.asItem();
        var inverter = AEItems.INVERTER_CARD.asItem();
        helper.assertTrue(Upgrades.getMaxInstallable(fuzzy,
                        ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get()) == 1,
                "fuzzy card must be associated with the small bulk cell");
        helper.assertTrue(Upgrades.getMaxInstallable(inverter,
                        ModRegistration.SIMPLIFY_SMALL_BULK_CELL_EXPANDED.get()) == 1,
                "inverter card must be associated with the expanded small bulk cell");
        if (ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL != null) {
            helper.assertTrue(Upgrades.getMaxInstallable(fuzzy,
                    ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL.get()) == 1,
                    "fuzzy card must be associated with the small bulk fluid cell");
        }
        helper.succeed();
    }

    /**
     * The small-bulk item cell mounts through eco's standard engine, and its type ceiling is the item's
     * own slot count.
     *
     * <p>Both halves are the cut being guarded. eco's MEGA long-bulk backend is what used to answer here,
     * and it hard-coded 25 reachable types (50 with its upgrade card) without ever reading the item - so
     * a cell with 3 or 10 filter slots reported a ceiling it could not reach, and the panel, the tooltip
     * and Jade each carried a workaround to undo that. Re-adding the backend, or letting the ceiling come
     * from anywhere but the item, turns this red.</p>
     */
    @GameTest(template = "empty", batch = "small_bulk_engine", timeoutTicks = 100, required = false,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void smallBulkCellMountsThroughTheStandardEngine(GameTestHelper helper) {
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL == null) {
            helper.succeed(); // MegaCells absent: the small bulk family is not registered.
            return;
        }
        var stack = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
        var cell = ECOStorageCells.getCellInventory(stack, (ISaveProvider) null);
        if (cell == null) {
            helper.fail("the small bulk cell got no storage inventory, so it cannot mount in a drive");
            return;
        }
        if (cell.getClass() != cn.dancingsnow.neoecoae.impl.storage.ECOStorageCell.class) {
            helper.fail("the small bulk cell mounted through " + cell.getClass().getSimpleName()
                    + " instead of eco's standard storage engine");
            return;
        }
        int slots = ((cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem)
                ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get()).getTotalTypes();
        if (cell.getTotalItemTypes() != slots) {
            helper.fail("the mounted cell reports " + cell.getTotalItemTypes() + " reachable types while"
                    + " the item says " + slots + ", so a backend ceiling is back");
            return;
        }
        helper.succeed();
    }

    /**
     * Contents written by the old MEGA long-bulk backend move into the component the standard engine reads.
     *
     * <p>The two backends never shared a place to keep items: the old one wrote a unit map under
     * {@code CUSTOM_DATA -> neoecoae_mega_long_bulk}, the standard one reads
     * {@code AEComponents.STORAGE_CELL_INV}. Without the move, every small bulk cell in an upgraded world
     * reads as empty - the items are still on the stack, but nothing looks there - which is the one
     * failure this family is not allowed to have.</p>
     */
    @GameTest(template = "empty", batch = "small_bulk_migration", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void longBulkContentsMoveIntoTheStandardComponent(GameTestHelper helper) {
        var stack = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
        var entry = new net.minecraft.nbt.CompoundTag();
        entry.putString("item", "minecraft:stone");
        entry.put("components", new net.minecraft.nbt.CompoundTag());
        entry.putLong("units", 1234L);
        var entries = new net.minecraft.nbt.ListTag();
        entries.add(entry);
        var payload = new net.minecraft.nbt.CompoundTag();
        payload.put("entries", entries);
        var root = new net.minecraft.nbt.CompoundTag();
        root.put("neoecoae_mega_long_bulk", payload);
        stack.set(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.of(root));

        var cell = ECOStorageCells.getCellInventory(stack, (ISaveProvider) null);
        if (cell == null) {
            helper.fail("the migrated cell produced no inventory, so nothing read it back");
            return;
        }
        java.util.List<appeng.api.stacks.GenericStack> stored =
                stack.getOrDefault(appeng.api.ids.AEComponents.STORAGE_CELL_INV, java.util.List.of());
        long stone = 0L;
        for (var held : stored) {
            if (held != null && held.what() instanceof appeng.api.stacks.AEItemKey key
                    && key.getId().getPath().equals("stone")) {
                stone = held.amount();
            }
        }
        if (stone != 1234L) {
            helper.fail("the old long-bulk payload left " + stored.size() + " entries and the stone count"
                    + " reads " + stone + ", expected 1234");
            return;
        }
        if (cell.getStoredItemTypes() != 1L) {
            helper.fail("the mounted engine sees " + cell.getStoredItemTypes() + " stored types, expected 1");
            return;
        }
        var leftovers = stack.getOrDefault(net.minecraft.core.component.DataComponents.CUSTOM_DATA,
                net.minecraft.world.item.component.CustomData.EMPTY).copyTag();
        if (leftovers.contains("neoecoae_mega_long_bulk")) {
            helper.fail("the old payload survived the move, so the next load would add these items again");
            return;
        }
        helper.succeed();
    }


    /**
     * Every item our own crafting recipes ask from another mod is still obtainable in this pack.
     *
     * <p>Why this exists: eco's author says he retires content by deleting recipes rather than code. A
     * deleted recipe is silent for an addon - the item class stays, our recipe still loads, JEI still
     * shows our page, and the only symptom is a player who can never gather the ingredient. A class
     * removal at least crashes. This is the check that turns the silent case into a red test.</p>
     *
     * <p>Scope, stated so nobody reads more out of it than it does: vanilla crafting recipes only, and
     * only each recipe's <em>primary</em> output - {@code getAllResults()} is not on the compile-time
     * {@code Recipe} here, so an item that only ever comes out as a secondary result would have to be
     * allow-listed with that reason written down. "No recipe yields it" is not yet "unobtainable" either:
     * a mined block or a smelted output needs no crafting recipe - which is why every entry in
     * {@link #OBTAINABLE_WITHOUT_CRAFTING} carries the reason that was checked for it rather than just
     * an id.</p>
     */
    @GameTest(template = "empty", batch = "recipe_inputs_obtainable", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void everyForeignRecipeInputStillHasAWayToObtainIt(GameTestHelper helper) {
        var level = helper.getLevel();
        var manager = level.getRecipeManager();

        java.util.Set<Item> craftedBySomething = new java.util.HashSet<>();
        for (var holder : manager.getRecipes()) {
            craftedBySomething.add(holder.value().getResultItem(level.registryAccess()).getItem());
        }
        // The ruler has to be able to say no: if this set were accidentally universal, every assertion
        // below would pass forever. An unobtainable-by-anything block is the control sample.
        if (craftedBySomething.contains(Items.BEDROCK)) {
            helper.fail("the produced-item set contains bedrock, so it is not a real set of recipe"
                    + " outputs and every check below would pass vacuously");
            return;
        }

        var missing = new java.util.LinkedHashSet<String>();
        var checked = new java.util.LinkedHashSet<Item>();
        for (var holder : manager.getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            var key = holder.id();
            if (!NeoECOPrototype.MOD_ID.equals(key.getNamespace())) {
                continue;
            }
            for (var ingredient : holder.value().getIngredients()) {
                for (var stack : ingredient.getItems()) {
                    var item = stack.getItem();
                    var namespace = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)
                            .getNamespace();
                    if (namespace.equals(NeoECOPrototype.MOD_ID) || namespace.equals("minecraft")
                            || OBTAINABLE_WITHOUT_CRAFTING.containsKey(item)) {
                        continue;
                    }
                    // Distinct items, not ingredient slots: one shaped recipe can ask for the same
                    // housing three times, and reporting that as "three problems" is how a count
                    // starts lying about the thing it is counting.
                    checked.add(item);
                    if (!craftedBySomething.contains(item)) {
                        missing.add(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(item)
                                + " (asked for by " + key.getPath() + ")");
                    }
                }
            }
        }
        if (checked.isEmpty()) {
            helper.fail("not one foreign item is consumed by our crafting recipes, so this scan"
                    + " measured nothing - the recipes moved, the namespace filter broke, or the"
                    + " ingredient walk stopped reaching nested values");
            return;
        }
        if (!missing.isEmpty()) {
            helper.fail(missing.size() + " distinct foreign ingredient(s) our recipes consume have no"
                    + " recipe behind them in this pack: " + String.join(", ", missing));
            return;
        }
        helper.succeed();
    }

    /**
     * Every tag folder we ship has a name in both languages.
     *
     * <p>Why this exists: an unnamed tag is not silent the way a missing item name is - the readers that
     * show a tag's display name fall back to the raw key, and EMI says so out loud ({@code Untranslated tag
     * #neoecoprototype:storage_matrices}, read off the dev client on 2026-10-24). Today the tag is only
     * named by an advancement condition, so nothing a player sees is wrong yet; the day one is used as a
     * recipe ingredient this guard is what keeps the raw key off the page.</p>
     */
    @GameTest(template = "empty", batch = "tag_names", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void everyShippedTagHasANameInBothLanguages(GameTestHelper helper) {
        var folder = "data/" + NeoECOPrototype.MOD_ID + "/tags";
        var tagPaths = new java.util.TreeSet<String>();
        try {
            for (var entry : System.getProperty("java.class.path")
                    .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
                var start = java.nio.file.Path.of(entry).toAbsolutePath().resolve(folder);
                if (!java.nio.file.Files.isDirectory(start)) {
                    continue;
                }
                try (var walk = java.nio.file.Files.walk(start)) {
                    for (var path : (Iterable<java.nio.file.Path>) walk
                            .filter(java.nio.file.Files::isRegularFile)::iterator) {
                        var relative = start.relativize(path).toString()
                                .replace(java.io.File.separatorChar, '/');
                        if (!relative.endsWith(".json")) {
                            continue;
                        }
                        // tags/item/foo.json names tag.neoecoprototype:foo - the registry folder is not part
                        // of the key, so dropping that first segment is the whole mapping rule here.
                        var cut = relative.indexOf('/');
                        if (cut < 0) {
                            helper.fail(relative + " sits directly in " + folder
                                    + " with no registry folder, so its tag key cannot be derived");
                            return;
                        }
                        tagPaths.add(relative.substring(cut + 1, relative.length() - 5));
                    }
                }
            }
        } catch (java.io.IOException failure) {
            helper.fail("the tag folders could not be read: " + failure);
            return;
        }
        if (tagPaths.isEmpty()) {
            helper.fail("not one tag file was found under " + folder
                    + " on this classpath, so the scan measured nothing");
            return;
        }
        for (var lang : List.of("zh_cn", "en_us")) {
            var table = com.google.gson.JsonParser.parseString(
                    readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper)).getAsJsonObject();
            for (var tagPath : tagPaths) {
                var key = "tag." + NeoECOPrototype.MOD_ID + "." + tagPath;
                if (!table.has(key) || table.get(key).getAsString().isBlank()) {
                    helper.fail("lang/" + lang + ".json has no " + key + " (tag " + tagPath
                            + "), so a reader that shows the tag's name falls back to the raw key");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * Items that legitimately have no crafting recipe, each with the way it is actually obtained.
     * Anything added here needs that reason checked, not guessed - an entry with a wrong reason is how
     * this guard stops meaning anything.
     */
    private static final java.util.Map<Item, String> OBTAINABLE_WITHOUT_CRAFTING = java.util.Map.of();

    /**
     * Every recipe file we ship really reached the recipe manager.
     *
     * <p>Why this exists: a json the recipe codec refuses is dropped in silence. The load writes one ERROR
     * line and carries on, so the jar builds, the game starts, the viewer simply has no page, and the only
     * thing a player can notice is an item nobody can make. Measured here on 2026-10-24, from an unpacking
     * recipe that asked for nine plushies where one stacks to a single slot:
     * {@code Item stack with stack size of 9 was larger than maximum: 1}, and the recipe was gone.</p>
     *
     * <p>Scope, stated so nobody reads more out of it than it does: our own namespace, and only files
     * carrying no {@code neoforge:conditions} - a gated recipe is supposed to vanish when its mod is absent,
     * and deciding that is not this server's job.</p>
     */
    @GameTest(template = "empty", batch = "recipe_files_all_load", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void everyShippedRecipeFileReachesTheRecipeManager(GameTestHelper helper) {
        var folder = "data/" + NeoECOPrototype.MOD_ID + "/recipe";
        var shipped = new java.util.TreeMap<ResourceLocation, String>();
        var skipped = new java.util.ArrayList<String>();
        try {
            for (var entry : System.getProperty("java.class.path")
                    .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
                var start = java.nio.file.Path.of(entry).toAbsolutePath().resolve(folder);
                if (!java.nio.file.Files.isDirectory(start)) {
                    continue;
                }
                try (var walk = java.nio.file.Files.walk(start)) {
                    for (var path : (Iterable<java.nio.file.Path>) walk
                            .filter(java.nio.file.Files::isRegularFile)::iterator) {
                        var relative = start.relativize(path).toString()
                                .replace(java.io.File.separatorChar, '/');
                        if (!relative.endsWith(".json")) {
                            continue;
                        }
                        var text = java.nio.file.Files.readString(path);
                        if (text.contains("neoforge:conditions")) {
                            skipped.add(relative + " (gated)");
                            continue;
                        }
                        shipped.put(NeoECOPrototype.id(relative.substring(0, relative.length() - 5)), relative);
                    }
                }
            }
        } catch (java.io.IOException failure) {
            helper.fail("the recipe folder could not be read: " + failure);
            return;
        }
        if (shipped.isEmpty()) {
            helper.fail("not one ungated recipe file was found under " + folder
                    + " on this classpath, so the scan measured nothing - the folder moved, or only jar"
                    + " entries carry it now: " + skipped);
            return;
        }
        var manager = helper.getLevel().getRecipeManager();
        var missing = new java.util.ArrayList<String>();
        for (var file : shipped.entrySet()) {
            if (manager.byKey(file.getKey()).isEmpty()) {
                missing.add(file.getKey() + " (from " + file.getValue() + ")");
            }
        }
        if (!missing.isEmpty()) {
            helper.fail(missing.size() + " of the " + shipped.size()
                    + " ungated recipe files we ship were dropped at load, so their output cannot be made"
                    + " by anyone: " + String.join(", ", missing));
            return;
        }
        helper.succeed();
    }

    /** Fixed infinite sources keep a null config so the cell workbench refuses them (AE2 NPE guard). */
    /**
     * The matrix registration factory is advertised to script authors in docs/kubejs.md, and until now it
     * had zero callers and zero runs. What is reachable from a gameTest is the pair of guards in front of
     * the registration step, so that is what this pins - including the guard that used to compare the
     * matrix id against the registry's own namespace (minecraft), which rejected every legal input.
     */
    @GameTest(template = "empty", batch = "matrix_registration_api", timeoutTicks = 100)
    public static void matrixRegistrationFactoryGuardsRejectAndAccept(GameTestHelper helper) {
        var items = net.neoforged.neoforge.registries.DeferredRegister.create(
                net.minecraft.core.registries.Registries.ITEM, NeoECOPrototype.MOD_ID);
        var id = NeoECOPrototype.id("gametest_matrix_registration_guard");
        var definition = cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.finite(
                id, cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.MatrixType.ITEM,
                cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.MatrixSize.K4,
                4096L, 1024, 4, cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1, null, null);
        java.util.function.Supplier<cn.dancingsnow.neoecoae.api.storage.ECOCellType> cellType =
                cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem::getItemCellType;

        var infinite = cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.infinite(
                NeoECOPrototype.id("gametest_matrix_registration_guard_infinite"),
                cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition.MatrixType.ITEM,
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1, 1.0D, null, null);
        var refusedInfinite = failureOf(() -> cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistration
                .registerFinite(items, "gametest_matrix_registration_guard_infinite", infinite,
                        appeng.api.stacks.AEKeyType.items(), cellType));
        if (!"IllegalArgumentException".equals(refusedInfinite)) {
            helper.fail("an infinite matrix must be refused with IllegalArgumentException, got "
                    + refusedInfinite);
            return;
        }

        var refusedName = failureOf(() -> cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistration
                .registerFinite(items, "a_different_name", definition,
                        appeng.api.stacks.AEKeyType.items(), cellType));
        if (!"IllegalArgumentException".equals(refusedName)) {
            helper.fail("a registration name that does not match the definition id (" + id
                    + ") must be refused with IllegalArgumentException, got " + refusedName);
            return;
        }

        // The matching pair has to get past both guards and hand back a holder. items.register() only
        // queues, so this works at any time; resolving it does not - new Item(...) reaches
        // BuiltInRegistries.ITEM.createIntrusiveHolder, and that registry is frozen after registration,
        // so nothing here calls holder.get(). Before the namespace fix this call threw
        // IllegalArgumentException, which is exactly the regression this assertion catches.
        var accepted = failureOf(() -> cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistration
                .registerFinite(items, id.getPath(), definition,
                        appeng.api.stacks.AEKeyType.items(), cellType));
        if (!"nothing".equals(accepted)) {
            helper.fail("a matching name and definition id must pass both guards and queue, got "
                    + accepted + " for " + id + " / " + id.getPath());
            return;
        }

        helper.succeed();
    }

    /** The simple name of what a call throws, or "nothing" when it returns normally. */
    private static String failureOf(Runnable call) {
        try {
            call.run();
            return "nothing";
        } catch (RuntimeException thrown) {
            return thrown.getClass().getSimpleName();
        }
    }


    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void infiniteConcreteCellIsNotWorkbenchEditable(GameTestHelper helper) {
        var item = ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get();
        var stack = new ItemStack(item);
        helper.assertTrue(item.getConfigInventory(stack) == null,
                "infinite concrete cell must keep a null config inventory");
        helper.assertTrue(!item.isEditable(stack),
                "infinite concrete cell must not be workbench-editable");
        helper.succeed();
    }

    /**
     * The auto-build preview must describe a buildable shell on empty ground. This is the only
     * thing that exercises {@code SimplifyTrinityDefinition}, which otherwise sits unused.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityBuildPlanMatchesDefinition(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = min.offset(SimplifyTrinityClusterCalculator.CONTROLLER_OFFSET);
        helper.setBlock(controllerPos, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get());

        helper.runAfterDelay(5, () -> {
            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            helper.assertTrue(blockEntity instanceof SimplifyTrinityControllerBlockEntity,
                    "Trinity controller block entity is missing");
            if (blockEntity instanceof SimplifyTrinityControllerBlockEntity controller) {
                var plan = new cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController(controller)
                        .createLocalPreviewPlan();
                helper.assertTrue(plan != null, "Trinity build plan is null");
                helper.assertTrue(plan.getConflictPositions().isEmpty(),
                        "an empty test plot should have no build conflicts, found "
                                + plan.getConflictPositions().size() + ": "
                                + plan.getConflictPositions().stream()
                                .map(conflict -> conflict + " has "
                                        + helper.getLevel().getBlockState(conflict) + " but wants "
                                        + plan.getAllBlocks().stream()
                                        .filter(block -> block.worldPos().equals(conflict))
                                        .map(block -> block.targetState().toString())
                                        .findFirst().orElse("<nothing>"))
                                .toList());

                // The controller is already placed, so the plan covers the remaining shell cells.
                int shellCells = SimplifyTrinityClusterCalculator.SHELL_SIZE
                        * SimplifyTrinityClusterCalculator.SHELL_SIZE
                        * SimplifyTrinityClusterCalculator.SHELL_SIZE;
                helper.assertTrue(plan.getAllBlocks().size() == shellCells - 1,
                        "build plan should cover " + (shellCells - 1) + " cells, got "
                                + plan.getAllBlocks().size());
                helper.assertTrue(plan.getRequiredItemCount() == shellCells - 1,
                        "build plan should require " + (shellCells - 1) + " blocks, got "
                                + plan.getRequiredItemCount());
                // Exactly four materials: the casing plus the three Trinity modules.
                helper.assertTrue(plan.getRequiredItems().size() == 4,
                        "build plan should need 4 distinct items, got " + plan.getRequiredItems().size());
                helper.assertTrue(
                        plan.getRequiredItems().stream().anyMatch(item -> item.stack().getItem()
                                == ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_ITEM.get()),
                        "build plan does not require the green aluminium casing");
            }
            helper.succeed();
        });
    }

    /**
     * Trinity's layout is NOT mirror-symmetric (storage west, computation east, crafting north),
     * so a mirrored build would place the modules where the validator rejects them and the player
     * would burn 23 casings for nothing. The mirror toggle must therefore be inert.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityBuildIgnoresMirror(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = min.offset(SimplifyTrinityClusterCalculator.CONTROLLER_OFFSET);
        helper.setBlock(controllerPos, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get());

        helper.runAfterDelay(5, () -> {
            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            helper.assertTrue(blockEntity instanceof SimplifyTrinityControllerBlockEntity,
                    "Trinity controller block entity is missing");
            if (blockEntity instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.setMirrorBuild(true);
                helper.assertTrue(!controller.isMirrorBuild(),
                        "Trinity must ignore the mirror toggle: its layout is not mirror-symmetric");

                var plan = new cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController(controller)
                        .createLocalPreviewPlan();
                helper.assertTrue(plan != null, "Trinity build plan is null");
                // Assert the relative arrangement, which is what the validator actually requires and
                // is independent of how eco anchors a definition in world space. The controller is
                // already placed and is deliberately skipped by the plan, so it is the origin.
                BlockPos origin = helper.absolutePos(controllerPos);
                helper.assertTrue(plan.getAllBlocks().stream().noneMatch(block ->
                                block.targetState().getBlock()
                                        == ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get()),
                        "the plan must not try to place a second controller");
                helper.assertTrue(hasPlanned(plan, origin.offset(-1, 0, 0),
                                ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get()),
                        "storage module is not planned west of the controller: " + describe(plan, origin));
                helper.assertTrue(hasPlanned(plan, origin.offset(1, 0, 0),
                                ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get()),
                        "computation module is not planned east of the controller: " + describe(plan, origin));
                helper.assertTrue(hasPlanned(plan, origin.offset(0, 0, -1),
                                ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get()),
                        "crafting module is not planned north of the controller: " + describe(plan, origin));
            }
            helper.succeed();
        });
    }

    /**
     * The controller panel must actually build. It embeds eco's build action bar, and nothing else
     * in the test suite executes that code path -- an exception here would crash the player on
     * right-click instead of failing a build.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityControllerPanelBuilds(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);

        helper.runAfterDelay(20, () -> {
            BlockPos absolute = helper.absolutePos(controllerPos);
            var blockEntity = helper.getLevel().getBlockEntity(absolute);
            helper.assertTrue(blockEntity instanceof SimplifyTrinityControllerBlockEntity,
                    "Trinity controller block entity is missing");
            if (blockEntity instanceof SimplifyTrinityControllerBlockEntity controller) {
                var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                var blockState = helper.getLevel().getBlockState(absolute);
                var holder = new com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder(
                        (com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUI) blockState.getBlock(),
                        player, absolute, blockState);
                helper.assertTrue(controller.createUI(holder) != null,
                        "Trinity controller returned a null UI");
            }
            helper.succeed();
        });
    }

    /**
     * The two fluid hatches are the only pair of blocks whose UI we build ourselves - we hand eco's
     * {@code FluidHatchUI} the tank field instead of letting the block entity do it - and until now
     * nothing in the suite ever made that call. A player right-clicks these, so a throw here is a
     * crash report rather than a failed build.
     */
    @GameTest(template = "empty", batch = "fluid_hatch_ui", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void fluidHatchesBuildTheirUI(GameTestHelper helper) {
        var blocks = List.of(ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get(),
                ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get());
        for (int i = 0; i < blocks.size(); i++) {
            helper.setBlock(new BlockPos(i, 1, 0), blocks.get(i));
        }
        helper.runAfterDelay(5, () -> {
            var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            for (int i = 0; i < blocks.size(); i++) {
                var absolute = helper.absolutePos(new BlockPos(i, 1, 0));
                var state = helper.getLevel().getBlockState(absolute);
                if (!(state.getBlock()
                        instanceof com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUI blockUI)) {
                    helper.fail("the placed hatch is no longer a BlockUI, so right-click has nothing to open: "
                            + state.getBlock());
                    return;
                }
                var blockEntity = helper.getLevel().getBlockEntity(absolute);
                if (blockEntity == null) {
                    helper.fail(state.getBlock() + " was placed without a block entity, so createUI's "
                            + "instanceof falls through to the null return");
                    return;
                }
                var holder = new com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder(
                        blockUI, player, absolute, state);
                try {
                    if (blockUI.createUI(holder) == null) {
                        helper.fail("right-clicking " + state.getBlock() + " would hand LDLib a null UI; "
                                + "the block entity there is " + blockEntity.getClass().getName());
                        return;
                    }
                } catch (RuntimeException failure) {
                    helper.fail("right-clicking " + state.getBlock() + " throws " + failure + ", first frames: "
                            + java.util.Arrays.toString(java.util.Arrays.copyOf(
                                    failure.getStackTrace(), Math.min(5, failure.getStackTrace().length))));
                    return;
                }
            }
            helper.succeed();
        });
    }

    /**
     * Breaking a Trinity block must drop the block itself. Without a loot table a broken block
     * drops nothing, which is especially painful for the controller: its recipe consumes three
     * subsystem hosts. This breaks the real blocks instead of just checking that files exist.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityBlocksDropThemselves(GameTestHelper helper) {
        List<net.minecraft.world.level.block.Block> blocks = List.of(
                ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get(),
                ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get(),
                ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get());
        List<net.minecraft.world.item.Item> items = List.of(
                ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_ITEM.get(),
                ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_ITEM.get(),
                ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_ITEM.get(),
                ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get());

        for (int i = 0; i < blocks.size(); i++) {
            helper.setBlock(new BlockPos(i, 0, 0), blocks.get(i));
        }
        helper.runAfterDelay(5, () -> {
            // NOTE: GameTestHelper#destroyBlock calls destroyBlock(pos, false, null), i.e. with
            // drops disabled, so it can never be used to test loot tables. Break the blocks with
            // drops enabled instead.
            var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            for (int i = 0; i < blocks.size(); i++) {
                helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(i, 0, 0)), true, player);
            }
            helper.runAfterDelay(5, () -> {
                for (int i = 0; i < items.size(); i++) {
                    helper.assertItemEntityPresent(items.get(i), new BlockPos(i, 0, 0), 2.0);
                }
                helper.succeed();
            });
        });
    }

    /**
     * The Trinity blocks must be obtainable in survival. A malformed recipe JSON simply fails to
     * load, so asserting the recipes exist is a real gate on the data files.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityBlocksAreCraftable(GameTestHelper helper) {
        var recipeManager = helper.getLevel().getRecipeManager();
        for (String path : List.of(
                "simplify_trinity_controller",
                "simplify_trinity_storage_module",
                "simplify_trinity_computation_module",
                "simplify_trinity_crafting_module")) {
            ResourceLocation id = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path);
            var holder = recipeManager.byKey(id);
            helper.assertTrue(holder.isPresent(), "missing crafting recipe: " + id);
            var result = holder.orElseThrow().value()
                    .getResultItem(helper.getLevel().registryAccess());
            helper.assertTrue(result.getItem() == BuiltInRegistries.ITEM.get(id),
                    "recipe " + id + " produces " + result + " instead of the Trinity block");
        }
        helper.succeed();
    }

    private static boolean hasPlanned(
            cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan plan,
            BlockPos pos, net.minecraft.world.level.block.Block block) {
        return plan.getAllBlocks().stream().anyMatch(planned ->
                planned.worldPos().equals(pos) && planned.targetState().getBlock() == block);
    }

    private static String describe(
            cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan plan, BlockPos origin) {
        return plan.getAllBlocks().stream()
                .map(planned -> {
                    BlockPos d = planned.worldPos().subtract(origin);
                    return "(" + d.getX() + "," + d.getY() + "," + d.getZ() + ")"
                            + BuiltInRegistries.BLOCK.getKey(planned.targetState().getBlock()).getPath()
                            .replace("simplify_trinity_", "T:")
                            .replace("simplify_green_aluminum_casing", "casing");
                })
                .toList().toString();
    }

    /**
     * Places the complete Trinity shell; {@code min} is the low corner of the shell.
     *
     * <p>Every slot comes from {@link SimplifyTrinityClusterCalculator}'s public offsets -- the same
     * constants the placement preview uses -- so a wrong offset breaks these tests instead of
     * silently producing an unformable structure.
     */
    private static BlockPos buildTrinityShell(GameTestHelper helper, BlockPos min) {
        var casing = ModRegistration.SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get();
        int last = SimplifyTrinityClusterCalculator.SHELL_SIZE - 1;
        for (int x = 0; x <= last; x++) {
            for (int y = 0; y <= last; y++) {
                for (int z = 0; z <= last; z++) {
                    BlockPos offset = new BlockPos(x, y, z);
                    if (SimplifyTrinityClusterCalculator.isFunctionalSlot(offset)) {
                        continue;
                    }
                    helper.setBlock(min.offset(offset), casing);
                }
            }
        }
        BlockPos controllerPos = min.offset(SimplifyTrinityClusterCalculator.CONTROLLER_OFFSET);
        helper.setBlock(controllerPos, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get());
        helper.setBlock(min.offset(SimplifyTrinityClusterCalculator.STORAGE_MODULE_OFFSET),
                ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get());
        helper.setBlock(min.offset(SimplifyTrinityClusterCalculator.COMPUTATION_MODULE_OFFSET),
                ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get());
        helper.setBlock(min.offset(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET),
                ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get());
        return controllerPos;
    }

    /**
     * Builds the 3x3x3 Trinity shell and asserts a single cluster really forms.
     *
     * <p>This is the regression guard for the architectural failure this machine used to have:
     * the old 7x3x7 layout borrowed eco/L1 parts that bind to their own subsystem cluster, so
     * the controller could never adopt its wings.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityStructureForms(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);

        helper.runAfterDelay(20, () -> {
            var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            helper.assertTrue(blockEntity instanceof SimplifyTrinityControllerBlockEntity,
                    "Trinity controller block entity is missing");

            if (blockEntity instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(10, () -> {
                if (blockEntity instanceof SimplifyTrinityControllerBlockEntity controller) {
                    helper.assertTrue(controller.isFormed(),
                            "Trinity cluster did not form for a complete 3x3x3 shell");
                    helper.assertTrue(controller.getCluster() != null,
                            "Trinity cluster is null after forming");
                    helper.assertTrue(controller.getCluster().getStorageModule() != null,
                            "Trinity cluster did not adopt its storage module");
                    helper.assertTrue(controller.getCluster().getComputationModule() != null,
                            "Trinity cluster did not adopt its computation module");
                    helper.assertTrue(controller.getCluster().getCraftingModule() != null,
                            "Trinity cluster did not adopt its crafting module");
                }
                helper.succeed();
            });
        });
    }

    /**
     * Proves the Trinity computation module hosts a computation cell and that the Trinity cluster
     * aggregates its CPU bytes -- the same job eco's computation drive does for a computation
     * cluster, except here the module is a Trinity member rather than a foreign part.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityComputationModuleHostsCell(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos computationPos = min.offset(SimplifyTrinityClusterCalculator.COMPUTATION_MODULE_OFFSET);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(10, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(computationPos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityComputationModuleBlockEntity,
                        "Trinity computation module block entity is missing");
                helper.assertTrue(controllerBe instanceof SimplifyTrinityControllerBlockEntity
                                && ((SimplifyTrinityControllerBlockEntity) controllerBe).getCluster() != null,
                        "Trinity cluster is missing, cannot aggregate computation bytes");
                if (moduleBe instanceof SimplifyTrinityComputationModuleBlockEntity module) {
                    helper.assertTrue(!module.hasCell(),
                            "a freshly built Trinity computation module should start with an empty cell slot");
                    helper.assertTrue(
                            ((SimplifyTrinityControllerBlockEntity) controllerBe).getCluster()
                                    .getComputationConfigurationCapacity() == 0L,
                            "empty computation module should contribute zero CPU bytes");
                    module.setCellStack(
                            new ItemStack(ModRegistration.SIMPLIFY_COMPUTATION_CELL_1M.get()));
                    helper.assertTrue(module.hasCell(),
                            "Trinity computation module rejected a valid computation cell");
                    helper.assertTrue(
                            ((SimplifyTrinityControllerBlockEntity) controllerBe).getCluster()
                                    .getComputationConfigurationCapacity() > 0L,
                            "Trinity cluster did not aggregate the computation cell's CPU bytes");
                }
                helper.succeed();
            });
        });
    }

    /**
     * Storage round-trip through the ME network: a cell inserted into the Trinity storage module
     * must actually hold items written into the grid, and give them back.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityStorageModuleStoresItems(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos storagePos = min.offset(SimplifyTrinityClusterCalculator.STORAGE_MODULE_OFFSET);

        Block energyCell = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "creative_energy_cell"));
        helper.setBlock(min.offset(-1, 1, 1), energyCell);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(20, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(storagePos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity,
                        "Trinity storage module block entity is missing");
                if (moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity module) {
                    helper.assertTrue(
                            module.insertCell(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get())),
                            "Trinity storage module rejected a valid L1 cell");
                    SimplifyGridFacade.requestStorageUpdate(module.getMainNode());
                }
                helper.runAfterDelay(20, () -> {
                    if (moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity module) {
                        helper.assertTrue(module.isMounted(),
                                "Trinity storage module did not mount its cell into the ME network");
                        var grid = module.getMainNode().getGrid();
                        helper.assertTrue(grid != null, "Trinity storage module is not on a grid");

                        var storage = grid.getStorageService().getInventory();
                        var source = IActionSource.ofMachine(module);
                        AEItemKey diamond = AEItemKey.of(Items.DIAMOND);

                        var cell = module.getCellInventory();
                        helper.assertTrue(cell != null, "the inserted cell has no inventory");
                        helper.assertTrue(cell.getUsedBytes() == 0L,
                                "a freshly inserted cell should report zero used bytes");

                        long inserted = storage.insert(diamond, 64, Actionable.MODULATE, source);
                        helper.assertTrue(inserted == 64,
                                "ME storage only accepted " + inserted + " of 64 diamonds");
                        // Proves the items landed in THIS module's cell rather than being absorbed
                        // somewhere else on the grid: the cell's own byte counter must move.
                        helper.assertTrue(cell.getUsedBytes() > 0L,
                                "the Trinity module's cell reports no used bytes after an insert");
                        helper.assertTrue(
                                storage.extract(diamond, 64, Actionable.SIMULATE, source) == 64,
                                "ME storage does not report the stored diamonds back");
                        helper.assertTrue(
                                storage.extract(diamond, 64, Actionable.MODULATE, source) == 64,
                                "ME storage could not give the diamonds back");
                        helper.assertTrue(
                                storage.extract(diamond, 1, Actionable.SIMULATE, source) == 0,
                                "ME storage still reports diamonds after they were taken out");
                        helper.assertTrue(cell.getUsedBytes() == 0L,
                                "the Trinity module's cell still reports used bytes after extraction");
                    }
                    helper.succeed();
                });
            });
        });
    }

    /**
     * Proves the Trinity crafting module really is an AE2 crafting provider: it registers the
     * service on its grid node and surfaces the patterns stored in its pattern inventory.
     *
     * <p>eco's own CPU pipeline cannot be reused (its constructors are bound to
     * {@code NEComputationCluster}), so the module delegates to AE2's {@code PatternProviderLogic}.
     * This test pins that contract down.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityCraftingModuleProvidesPatterns(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos craftingPos = min.offset(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(10, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(craftingPos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityCraftingModuleBlockEntity,
                        "Trinity crafting module block entity is missing");
                if (moduleBe instanceof SimplifyTrinityCraftingModuleBlockEntity module) {
                    helper.assertTrue(module.getMainNode().getNode()
                                    .getService(ICraftingProvider.class) != null,
                            "Trinity crafting module did not register itself as a crafting provider");
                    helper.assertTrue(module.getAvailablePatterns().isEmpty(),
                            "a freshly built Trinity crafting module should hold no patterns");

                    ItemStack encoded = PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)),
                            List.of(new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 1)));
                    helper.assertTrue(!encoded.isEmpty(), "failed to encode the test pattern");
                    module.getLogic().getPatternInv().setItemDirect(0, encoded);
                    module.getLogic().updatePatterns();

                    helper.assertTrue(module.getAvailablePatterns().size() == 1,
                            "Trinity crafting module did not surface its stored pattern");
                }
                helper.succeed();
            });
        });
    }

    /**
     * End-to-end crafting wiring: a real AE2 crafting CPU plus the Trinity pattern provider on one
     * network must make the pattern craftable, and the provider must actually move the ingredients
     * into the adjacent inventory when a CPU dispatches to it.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityPatternIsUsableByCraftingService(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos craftingPos = min.offset(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET);
        // The CPU only needs to be on the same grid, so it sits beside the computation module;
        // the chest must be a face-neighbour of the crafting module to receive pushed ingredients.
        BlockPos cpuPos = min.offset(3, 1, 1);
        BlockPos chestPos = min.offset(1, 1, -1);

        Block energyCell = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "creative_energy_cell"));
        Block cpuBlock = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "1k_crafting_storage"));
        helper.assertTrue(cpuBlock != null && cpuBlock != net.minecraft.world.level.block.Blocks.AIR,
                "AE2 crafting storage is not available for the test");
        helper.setBlock(min.offset(-1, 1, 1), energyCell);
        helper.setBlock(cpuPos, cpuBlock);
        helper.setBlock(chestPos, net.minecraft.world.level.block.Blocks.CHEST);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(20, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(craftingPos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityCraftingModuleBlockEntity,
                        "Trinity crafting module block entity is missing");
                if (moduleBe instanceof SimplifyTrinityCraftingModuleBlockEntity module) {
                    var grid = module.getMainNode().getGrid();
                    helper.assertTrue(grid != null, "Trinity crafting module is not on a grid");

                    ItemStack encoded = PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)),
                            List.of(new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 1)));
                    module.getLogic().getPatternInv().setItemDirect(0, encoded);
                    module.getLogic().updatePatterns();

                    var craftingService = grid.getCraftingService();
                    helper.assertTrue(craftingService.getCpus().size() == 1,
                            "the AE2 crafting CPU next to Trinity did not form");
                    helper.assertTrue(craftingService.isCraftable(AEItemKey.of(Items.GOLD_INGOT)),
                            "the network does not consider the Trinity pattern craftable");
                    helper.assertTrue(
                            !craftingService.getCraftingFor(AEItemKey.of(Items.GOLD_INGOT)).isEmpty(),
                            "the crafting service cannot see the Trinity pattern provider's pattern");

                    // Dispatch exactly as a CPU would, then check the ingredients physically moved.
                    IPatternDetails details = PatternDetailsHelper.decodePattern(encoded, helper.getLevel());
                    helper.assertTrue(details != null, "failed to decode the test pattern");
                    var patternInputs = details.getInputs();
                    KeyCounter[] inputs = new KeyCounter[patternInputs.length];
                    for (int i = 0; i < patternInputs.length; i++) {
                        KeyCounter counter = new KeyCounter();
                        for (GenericStack stack : patternInputs[i].getPossibleInputs()) {
                            // Stock what the slot actually demands: AE2 keeps a repeated ingredient as one
                            // slot with a multiplier, so seeding only the candidate amount would starve
                            // this dispatch the moment a pattern carries two of the same item.
                            counter.add(stack.what(), stack.amount() * patternInputs[i].getMultiplier());
                        }
                        inputs[i] = counter;
                    }
                    helper.assertTrue(module.getLogic().pushPattern(details, inputs),
                            "Trinity pattern provider refused a dispatch from the crafting CPU");

                    var chestHandler = helper.getLevel().getCapability(
                            net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK,
                            helper.absolutePos(chestPos), null);
                    helper.assertTrue(chestHandler != null, "chest has no item handler");
                    long received = 0;
                    for (int slot = 0; slot < chestHandler.getSlots(); slot++) {
                        received += chestHandler.getStackInSlot(slot).getCount();
                    }
                    helper.assertTrue(received >= 1,
                            "Trinity pattern provider did not push the ingredient into the chest");
                }
                helper.succeed();
            });
        });
    }

    /**
     * The local resource observation must not pretend to resolve a multi-input AE2 plan.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityPreflightReportsEveryInput(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3), module -> {
            module.getLogic().getPatternInv().setItemDirect(0,
                    PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1),
                                    new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 2)),
                            List.of(new GenericStack(AEItemKey.of(Items.DIAMOND), 1))));
        }, (controller, network) -> {
            var inventory = network.getStorageService().getInventory();
            long iron = inventory.insert(AEItemKey.of(Items.IRON_INGOT), 1,
                    Actionable.MODULATE, IActionSource.ofMachine(controller));
            helper.assertTrue(iron == 1, "could not stock the first multi-input ingredient");
            var request = cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRequest
                    .fullTask(ResourceLocation.withDefaultNamespace("diamond"), 1);
            var check = controller.getCluster().getResourceCheck(request);
            helper.assertTrue(check.requirements().size() == 1,
                    "resource observation should report only the requested output: " + check.requirements());
            helper.assertTrue(check.requirements().get(0).what().equals(AEItemKey.of(Items.DIAMOND)),
                    "resource observation must not invent pattern inputs: " + check.requirements());
            helper.assertTrue(!check.available(), "direct output inventory should not contain diamond yet");

            long gold = inventory.insert(AEItemKey.of(Items.GOLD_INGOT), 2,
                    Actionable.MODULATE, IActionSource.ofMachine(controller));
            helper.assertTrue(gold == 2, "could not stock the second multi-input ingredient");
            var stillAdvisory = controller.getCluster().getResourceCheck(request);
            helper.assertTrue(!stillAdvisory.available(),
                    "local observation must not become an execution verdict after pattern inputs change");
            helper.succeed();
        });
    }

    /**
     * The local pattern observation reports candidates but never selects a batch or input plan.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityPreflightUsesSelectedPatternBatch(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3), module -> {
            module.getLogic().getPatternInv().setItemDirect(0,
                    PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)),
                            List.of(new GenericStack(AEItemKey.of(Items.DIAMOND), 4))));
            module.getLogic().getPatternInv().setItemDirect(1,
                    PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 3)),
                            List.of(new GenericStack(AEItemKey.of(Items.DIAMOND), 1))));
        }, (controller, network) -> {
            var inventory = network.getStorageService().getInventory();
            long inserted = inventory.insert(AEItemKey.of(Items.GOLD_INGOT), 15,
                    Actionable.MODULATE, IActionSource.ofMachine(controller));
            helper.assertTrue(inserted == 15, "could not stock the gold for the batch-selection test");
            var request = cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityTaskRequest
                    .fullTask(ResourceLocation.withDefaultNamespace("diamond"), 5);
            var check = controller.getCluster().getResourceCheck(request);
            helper.assertTrue(!check.available(),
                    "direct storage observation must not treat stocked gold as a completed diamond task");
            helper.assertTrue(check.requirements().size() == 1
                            && check.requirements().get(0).what().equals(AEItemKey.of(Items.DIAMOND)),
                    "preflight must not select a gold batch or invent input requirements: " + check.requirements());
            helper.succeed();
        });
    }

    /**
     * Proves the Trinity storage module actually joins an ME grid and mounts its cell --
     * the part that used to be impossible while the wings belonged to foreign clusters.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void trinityStorageModuleMountsCell(GameTestHelper helper) {
        BlockPos min = new BlockPos(3, 3, 3);
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos storagePos = min.offset(SimplifyTrinityClusterCalculator.STORAGE_MODULE_OFFSET);

        // Ad-hoc ME network: a creative energy cell just outside the shell's west face.
        Block energyCell = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "creative_energy_cell"));
        helper.assertTrue(energyCell != null && energyCell != net.minecraft.world.level.block.Blocks.AIR,
                "AE2 creative energy cell is not available for the test");
        helper.setBlock(min.offset(-1, 1, 1), energyCell);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(20, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(storagePos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity,
                        "Trinity storage module block entity is missing");
                if (moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity module) {
                    helper.assertTrue(module.isFormed(), "Trinity storage module is not formed");
                    // getGrid() != null proves nothing here: the four Trinity members form an
                    // ad-hoc grid among themselves as soon as they are formed. Drawing power from
                    // the external creative energy cell is the only real proof that the machine
                    // is reachable by an outside ME network.
                    helper.assertTrue(module.getMainNode().isPowered(),
                            "Trinity storage module is not powered by the adjacent ME network");
                    helper.assertTrue(
                            module.insertCell(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get())),
                            "Trinity storage module rejected a valid L1 cell");
                    SimplifyGridFacade.requestStorageUpdate(module.getMainNode());
                }
                helper.runAfterDelay(20, () -> {
                    if (moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity module) {
                        helper.assertTrue(module.isMounted(),
                                "Trinity storage module did not mount its cell into the ME network");
                    }
                    helper.succeed();
                });
            });
        });
    }

    /**
     * Places a Trinity machine plus everything an AE2 crafting job needs, forms it, mounts a
     * storage cell, and hands the finished rig to {@code runJob}.
     *
     * <p>The molecular assembler must sit on the crafting module's -Z face: that is the only face
     * of the module not covered by Trinity's own shell. The crafting CPU only has to share the
     * grid, so it sits beside the computation module.</p>
     */
    private static void withTrinityCraftRig(GameTestHelper helper, BlockPos min,
            java.util.function.Consumer<SimplifyTrinityCraftingModuleBlockEntity> encodePatterns,
            java.util.function.BiConsumer<SimplifyTrinityControllerBlockEntity, IGrid> runJob) {
        BlockPos controllerPos = buildTrinityShell(helper, min);
        BlockPos storagePos = min.offset(SimplifyTrinityClusterCalculator.STORAGE_MODULE_OFFSET);
        BlockPos craftingPos = min.offset(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET);
        BlockPos assemblerPos = min.offset(1, 1, -1);

        Block energyCell = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "creative_energy_cell"));
        Block cpuBlock = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "1k_crafting_storage"));
        Block assemblerBlock = BuiltInRegistries.BLOCK.get(
                ResourceLocation.fromNamespaceAndPath("ae2", "molecular_assembler"));
        helper.assertTrue(assemblerBlock != null
                        && assemblerBlock != net.minecraft.world.level.block.Blocks.AIR,
                "AE2 molecular assembler is not available for the test");
        helper.setBlock(min.offset(-1, 1, 1), energyCell);
        helper.setBlock(min.offset(3, 1, 1), cpuBlock);
        helper.setBlock(assemblerPos, assemblerBlock);

        helper.runAfterDelay(20, () -> {
            var controllerBe = helper.getLevel().getBlockEntity(helper.absolutePos(controllerPos));
            if (controllerBe instanceof SimplifyTrinityControllerBlockEntity controller) {
                controller.rebuildMultiblock();
            }
            helper.runAfterDelay(20, () -> {
                var moduleBe = helper.getLevel().getBlockEntity(helper.absolutePos(storagePos));
                var craftingBe = helper.getLevel().getBlockEntity(helper.absolutePos(craftingPos));
                helper.assertTrue(moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity,
                        "Trinity storage module block entity is missing");
                helper.assertTrue(craftingBe instanceof SimplifyTrinityCraftingModuleBlockEntity,
                        "Trinity crafting module block entity is missing");
                if (craftingBe instanceof SimplifyTrinityCraftingModuleBlockEntity module) {
                    encodePatterns.accept(module);
                    module.getLogic().updatePatterns();
                }
                if (moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity module) {
                    helper.assertTrue(
                            module.insertCell(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get())),
                            "Trinity storage module rejected a valid L1 cell");
                    SimplifyGridFacade.requestStorageUpdate(module.getMainNode());
                }

                helper.runAfterDelay(20, () -> {
                    if (!(moduleBe instanceof SimplifyTrinityStorageModuleBlockEntity storage)) {
                        helper.fail("Trinity storage module disappeared");
                        return;
                    }
                    helper.assertTrue(storage.isMounted(),
                            "Trinity storage module did not mount its cell");
                    IGrid network = storage.getMainNode().getGrid();
                    helper.assertTrue(network != null, "Trinity storage module is not on a grid");
                    if (network == null) {
                        return;
                    }
                    helper.assertTrue(network.getCraftingService().getCpus().size() == 1,
                            "the AE2 crafting CPU did not form next to Trinity");
                    var assemblerBe = helper.getLevel()
                            .getBlockEntity(helper.absolutePos(assemblerPos));
                    helper.assertTrue(assemblerBe
                                    instanceof appeng.blockentity.crafting.MolecularAssemblerBlockEntity,
                            "molecular assembler block entity is missing at " + assemblerPos
                                    + ", found " + assemblerBe);
                    if (assemblerBe instanceof appeng.blockentity.crafting.MolecularAssemblerBlockEntity
                            assembler) {
                        helper.assertTrue(assembler.getMainNode().isPowered(),
                                "the molecular assembler is not powered by an ME network");
                        helper.assertTrue(assembler.getMainNode().getGrid() == network,
                                "the molecular assembler joined a different ME grid than Trinity");
                    }
                    var controllerBe2 = helper.getLevel()
                            .getBlockEntity(helper.absolutePos(controllerPos));
                    if (controllerBe2 instanceof SimplifyTrinityControllerBlockEntity controller) {
                        runJob.accept(controller, network);
                    } else {
                        helper.fail("Trinity controller block entity is missing");
                    }
                });
            });
        });
    }

    private static ItemStack[] emptyPatternGrid() {
        ItemStack[] slots = new ItemStack[9];
        java.util.Arrays.fill(slots, ItemStack.EMPTY);
        return slots;
    }

    private static RecipeHolder<CraftingRecipe> craftingRecipe(GameTestHelper helper,
            int width, int height, ItemStack... ingredients) {
        RecipeHolder<CraftingRecipe> recipe = helper.getLevel().getRecipeManager().getRecipeFor(
                        RecipeType.CRAFTING,
                        CraftingInput.of(width, height, List.of(ingredients)),
                        helper.getLevel())
                .orElse(null);
        helper.assertTrue(recipe != null,
                "crafting recipe for " + ingredients[0].getItem() + " is missing");
        return recipe;
    }

    /**
     * Stocks the network, submits a Trinity task, and waits for it to reach a terminal state.
     * Asserts the job completed and that the requested output really landed back in the network.
     */
    private static void runCraftingJob(GameTestHelper helper,
            SimplifyTrinityControllerBlockEntity controller, IGrid network,
            String targetText, int quantity, AEItemKey stock, int stockAmount, AEItemKey output) {
        var inventory = network.getStorageService().getInventory();
        long inserted = inventory.insert(stock, stockAmount, Actionable.MODULATE,
                IActionSource.ofMachine(controller));
        helper.assertTrue(inserted == stockAmount,
                "could only stock " + inserted + " of " + stockAmount + " " + stock.getId());
        helper.assertTrue(network.getCraftingService().isCraftable(output),
                "the network does not consider " + output.getId() + " craftable");
        helper.assertTrue(controller.getServerTickCount() > 0,
                "the Trinity controller block entity ticker never ran");
        startAndAwaitTerminal(helper, controller, targetText, quantity, executor -> {
            helper.assertTrue(executor.phase() == TrinityCraftingExecutor.Phase.COMPLETED,
                    "the job did not complete; state=" + executor.phase()
                            + " detail=" + executor.detail());
            helper.assertTrue(executor.produced() >= quantity,
                    "only " + executor.produced() + " " + output.getId()
                            + " reached the network");
            long stored = inventory.extract(output, quantity, Actionable.SIMULATE,
                    IActionSource.ofMachine(controller));
            helper.assertTrue(stored >= quantity,
                    "the network only holds " + stored + " " + output.getId());
            // The output alone does not prove the chain ran: it would also be there if
            // Trinity had simply ignored the plan. Insisting that the raw material was
            // actually consumed is what proves the ingredients really flowed through.
            long leftover = inventory.extract(stock, stockAmount, Actionable.SIMULATE,
                    IActionSource.ofMachine(controller));
            helper.assertTrue(leftover < stockAmount,
                    "the job reported success but no " + stock.getId()
                            + " was consumed (" + leftover + "/" + stockAmount
                            + " remain), so the recipe chain never actually ran");
            helper.succeed();
        });
    }

    /**
     * Submits the configured task and polls until the executor reaches a terminal phase, then hands
     * the finished executor to {@code onTerminal}. Success and failure jobs share this so that
     * "how long do we wait" is decided in exactly one place.
     */
    private static void startAndAwaitTerminal(GameTestHelper helper,
            SimplifyTrinityControllerBlockEntity controller, String targetText, int quantity,
            java.util.function.Consumer<TrinityCraftingExecutor> onTerminal) {
        helper.runAfterDelay(10, () -> {
            controller.setTaskTargetText(targetText);
            controller.setTaskQuantityText(String.valueOf(quantity));
            helper.assertTrue(controller.startCraftingTask(),
                    "Trinity refused to submit the crafting job for " + targetText);
            helper.assertTrue(controller.getCraftingExecutor().isBusy(),
                    "Trinity started a job but the executor is not busy");

            waitUntil(helper, 40,
                    () -> controller.getCraftingExecutor().isTerminal(),
                    () -> "the Trinity crafting job for " + targetText
                            + " never finished; state=" + controller.getCraftingExecutor().phase()
                            + " - " + controller.getCraftingExecutor().detail(),
                    () -> onTerminal.accept(controller.getCraftingExecutor()));
        });
    }

    /**
     * The real closed loop: Trinity submits an AE2 crafting job, the network's CPU runs it, and the
     * output comes back into Trinity's own storage.
     *
     * <p>This is what moves Trinity from "preflight system" to "working system". Trinity does not
     * run the craft itself -- it owns the request, the link, and the output check, while AE2's
     * crafting CPU and the molecular assembler do the physical work.</p>
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 800)
    public static void trinityCraftingJobCompletesEndToEnd(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3),
                module -> {
                    ItemStack[] slots = emptyPatternGrid();
                    slots[0] = new ItemStack(Items.OAK_LOG);
                    module.getLogic().getPatternInv().setItemDirect(0,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                    slots, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                },
                (controller, network) -> runCraftingJob(helper, controller, network,
                        "minecraft:oak_planks", 4,
                        AEItemKey.of(Items.OAK_LOG), 4, AEItemKey.of(Items.OAK_PLANKS)));
    }

    /**
     * A two-stage chain: the network holds only oak logs, but the task asks for sticks, so the
     * plan has to run log -> planks -> sticks. Trinity must track the *final* output, not the
     * intermediate one.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 800)
    public static void trinityCraftingJobHandlesRecursiveChain(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3),
                module -> {
                    // REVERSE CONTROL: the intermediate log -> plank step is deliberately missing.
                    ItemStack[] logs = emptyPatternGrid();
                    logs[0] = new ItemStack(Items.OAK_LOG);
                    if (Boolean.parseBoolean(System.getProperty("trinity.reverseControl", "false"))) {
                        module.getLogic().getPatternInv().setItemDirect(0, ItemStack.EMPTY);
                    } else {
                        module.getLogic().getPatternInv().setItemDirect(0,
                                PatternDetailsHelper.encodeCraftingPattern(
                                        craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                        logs, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                    }

                    // Sticks are two planks stacked vertically, so slots 0 and 3 of the 3x3 grid.
                    ItemStack[] planks = emptyPatternGrid();
                    planks[0] = new ItemStack(Items.OAK_PLANKS);
                    planks[3] = new ItemStack(Items.OAK_PLANKS);
                    module.getLogic().getPatternInv().setItemDirect(1,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 2, new ItemStack(Items.OAK_PLANKS),
                                            new ItemStack(Items.OAK_PLANKS)),
                                    planks, new ItemStack(Items.STICK, 4), false, false));
                },
                (controller, network) -> runCraftingJob(helper, controller, network,
                        "minecraft:stick", 4,
                        AEItemKey.of(Items.OAK_LOG), 4, AEItemKey.of(Items.STICK)));
    }

    /**
     * The last step of the execution plan: when a job cannot run, the stored raw material must
     * still be there. Sticks are deliberately not craftable in this rig (only the log -> plank
     * pattern is encoded), so the plan must fail -- and a failed plan must leave the logs alone.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 800)
    public static void trinityCraftingJobFailsWithoutTouchingStock(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3),
                module -> {
                    ItemStack[] logs = emptyPatternGrid();
                    logs[0] = new ItemStack(Items.OAK_LOG);
                    module.getLogic().getPatternInv().setItemDirect(0,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                    logs, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                },
                (controller, network) -> {
                    AEItemKey logKey = AEItemKey.of(Items.OAK_LOG);
                    var inventory = network.getStorageService().getInventory();
                    long inserted = inventory.insert(logKey, 4, Actionable.MODULATE,
                            IActionSource.ofMachine(controller));
                    helper.assertTrue(inserted == 4,
                            "could only stock " + inserted + " of 4 " + logKey.getId());
                    helper.assertTrue(
                            !network.getCraftingService().isCraftable(AEItemKey.of(Items.STICK)),
                            "sticks must not be craftable in this rig, otherwise the test proves nothing");
                    startAndAwaitTerminal(helper, controller, "minecraft:stick", 4, executor -> {
                        helper.assertTrue(
                                executor.phase() == TrinityCraftingExecutor.Phase.FAILED,
                                "the job should have failed; state=" + executor.phase()
                                        + " detail=" + executor.detail());
                        long logs = inventory.extract(logKey, 4, Actionable.SIMULATE,
                                IActionSource.ofMachine(controller));
                        helper.assertTrue(logs == 4,
                                "a failed job consumed stock: only " + logs + "/4 oak logs remain");
                        long planks = inventory.extract(AEItemKey.of(Items.OAK_PLANKS), 1,
                                Actionable.SIMULATE, IActionSource.ofMachine(controller));
                        helper.assertTrue(planks == 0,
                                "a failed job still produced " + planks + " oak planks");
                        helper.succeed();
                    });
                });
    }

    /**
     * Cancelling must actually stop the job: nothing may be crafted afterwards, and the stock the
     * job would have used must still be there. The cancel lands in the same tick as the submit,
     * while the plan is still being calculated, so the outcome is deterministic.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 800)
    public static void trinityCraftingJobCanBeCancelled(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3),
                module -> {
                    ItemStack[] logs = emptyPatternGrid();
                    logs[0] = new ItemStack(Items.OAK_LOG);
                    module.getLogic().getPatternInv().setItemDirect(0,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                    logs, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                },
                (controller, network) -> {
                    AEItemKey logKey = AEItemKey.of(Items.OAK_LOG);
                    var inventory = network.getStorageService().getInventory();
                    long inserted = inventory.insert(logKey, 4, Actionable.MODULATE,
                            IActionSource.ofMachine(controller));
                    helper.assertTrue(inserted == 4,
                            "could only stock " + inserted + " of 4 " + logKey.getId());
                    helper.runAfterDelay(10, () -> {
                        controller.setTaskTargetText("minecraft:oak_planks");
                        controller.setTaskQuantityText("4");
                        helper.assertTrue(controller.startCraftingTask(),
                                "Trinity refused to submit the crafting job");
                        controller.cancelCraftingTask();

                        var executor = controller.getCraftingExecutor();
                        helper.assertTrue(
                                executor.phase() == TrinityCraftingExecutor.Phase.FAILED,
                                "cancelling should fail the job at once; state=" + executor.phase()
                                        + " detail=" + executor.detail());
                        helper.assertTrue(executor.detail().contains("cancelled"),
                                "the cancellation was not reported: " + executor.detail());

                        // Give a wrongly resurrected plan plenty of time to submit itself.
                        helper.runAfterDelay(60, () -> {
                            var later = controller.getCraftingExecutor();
                            helper.assertTrue(
                                    later.phase() == TrinityCraftingExecutor.Phase.FAILED,
                                    "a cancelled job came back to life; state=" + later.phase()
                                            + " detail=" + later.detail());
                            long logs = inventory.extract(logKey, 4, Actionable.SIMULATE,
                                    IActionSource.ofMachine(controller));
                            helper.assertTrue(logs == 4,
                                    "the cancelled job consumed stock: only " + logs
                                            + "/4 oak logs remain");
                            long planks = inventory.extract(AEItemKey.of(Items.OAK_PLANKS), 1,
                                    Actionable.SIMULATE, IActionSource.ofMachine(controller));
                            helper.assertTrue(planks == 0,
                                    "the cancelled job still produced " + planks + " oak planks");
                            helper.succeed();
                        });
                    });
                });
    }

    /**
     * Once a real AE2 link exists, cancellation must reject any late output from that old link.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 800)
    public static void trinityCraftingJobRejectsLateOutputFromCancelledLink(GameTestHelper helper) {
        withTrinityCraftRig(helper, new BlockPos(3, 3, 3),
                module -> {
                    ItemStack[] logs = emptyPatternGrid();
                    logs[0] = new ItemStack(Items.OAK_LOG);
                    module.getLogic().getPatternInv().setItemDirect(0,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                    logs, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                },
                (controller, network) -> {
                    var inventory = network.getStorageService().getInventory();
                    AEItemKey logKey = AEItemKey.of(Items.OAK_LOG);
                    long inserted = inventory.insert(logKey, 4, Actionable.MODULATE,
                            IActionSource.ofMachine(controller));
                    helper.assertTrue(inserted == 4, "could not stock oak logs for cancellation test");
                    helper.runAfterDelay(10, () -> {
                        controller.setTaskTargetText("minecraft:oak_planks");
                        controller.setTaskQuantityText("4");
                        helper.assertTrue(controller.startCraftingTask(),
                                "Trinity refused to submit the crafting job");
                        waitUntil(helper, 40,
                                () -> !controller.getRequestedJobs().isEmpty(),
                                () -> "the job never exposed a live AE2 link; state="
                                        + controller.getCraftingExecutor().phase(),
                                () -> {
                                    ICraftingLink oldLink = controller.getRequestedJobs().stream()
                                            .findFirst().orElseThrow();
                                    controller.cancelCraftingTask();
                                    helper.assertTrue(controller.getCraftingExecutor().phase()
                                                    == TrinityCraftingExecutor.Phase.FAILED,
                                            "cancelled link task did not enter FAILED");
                                    long accepted = controller.insertCraftedItems(oldLink,
                                            AEItemKey.of(Items.OAK_PLANKS), 4, Actionable.SIMULATE);
                                    helper.assertTrue(accepted == 0,
                                            "cancelled link was accepted as late output");
                                    helper.runAfterDelay(60, () -> {
                                        helper.assertTrue(controller.getCraftingExecutor().phase()
                                                        == TrinityCraftingExecutor.Phase.FAILED,
                                                "cancelled link resurrected the task");
                                        helper.succeed();
                                    });
                                });
                    });
                });
    }

    /**
     * The processor assembler recipes are machine-only and shapeless: reachable through the custom
     * recipe type, and matched regardless of which input slot holds which ingredient.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void processorAssemblerRecipesMatchInAnyOrder(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager().getAllRecipesFor(
                ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get());
        helper.assertTrue(recipes.size() >= 3,
                "expected at least three processor assembler recipes, found " + recipes.size());

        record Expectation(List<String> ingredients, String result) {}
        List<Expectation> expectations = List.of(
                new Expectation(List.of("ae2:silicon", "minecraft:redstone", "minecraft:gold_ingot"),
                        "ae2:logic_processor"),
                new Expectation(List.of("ae2:silicon", "minecraft:redstone", "ae2:certus_quartz_crystal"),
                        "ae2:calculation_processor"),
                new Expectation(List.of("ae2:silicon", "minecraft:redstone", "minecraft:diamond"),
                        "ae2:engineering_processor"));

        for (Expectation expectation : expectations) {
            List<ItemStack> stacks = expectation.ingredients().stream().map(NeoECOPrototypeGameTests::stackOf)
                    .toList();
            ItemStack result = stackOf(expectation.result());

            var match = recipes.stream()
                    .filter(holder -> holder.value().result().getItem() == result.getItem())
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(match != null,
                    "no processor assembler recipe produces " + expectation.result());
            if (match == null) {
                continue;
            }
            for (ItemStack[] permutation : permutations(stacks)) {
                helper.assertTrue(match.value().matches(permutation), "recipe for " + expectation.result()
                        + " rejected the order " + describe(permutation));
            }

            ItemStack[] wrong = stacks.toArray(new ItemStack[0]);
            wrong[wrong.length - 1] = stackOf("minecraft:cobblestone");
            helper.assertTrue(!match.value().matches(wrong),
                    "recipe for " + expectation.result() + " accepted " + describe(wrong));
        }

        // A pattern for a recipe that repeats one ingredient: two slots carrying the same item plus the
        // third ingredient. This is the case the assembler used to refuse outright, because the old guard
        // demanded one candidate per slot AND a slot count equal to the ingredient count, so any pattern
        // that merged or reordered the repeats was dropped before the recipes were even looked up.
        var cell = new GenericStack(AEItemKey.of(stackOf("neoecoprototype:simplify_computation_cell_1m")), 1);
        var component = new GenericStack(
                AEItemKey.of(stackOf("neoecoprototype:simplify_storage_component_4m")), 1);
        var output = stackOf("neoecoprototype:energized_computation_cell_4m");
        var repeated = new HandmadePattern(AEItemKey.of(output),
                List.of(new HandmadeInput(1, cell), new HandmadeInput(1, cell), new HandmadeInput(1, component)),
                List.of(new GenericStack(AEItemKey.of(output), 1)));
        if (cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes.resolve(helper.getLevel(), repeated,
                output.getItem()) == null) {
            helper.fail("a pattern with two slots carrying the same item was refused for a recipe that needs"
                    + " two of it");
            return;
        }

        // AE2's own shape for the same recipe when the player fills the encoding grid with two of one item:
        // it merges them into a single slot whose multiplier is 2, leaving the other slot at 1, while the
        // output stays 1. This is the pattern a player actually reports, and it is told apart from a batched
        // pattern by that output amount -- the two share the multiplier field.
        var merged = new HandmadePattern(AEItemKey.of(output),
                List.of(new HandmadeInput(2, cell), new HandmadeInput(1, component)),
                List.of(new GenericStack(AEItemKey.of(output), 1)));
        if (cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes.resolve(helper.getLevel(), merged,
                output.getItem()) == null) {
            helper.fail("a pattern carrying the repeated ingredient as one slot with multiplier 2 was refused"
                    + " -- this is the shape AE2 stores when a recipe needs two of the same item");
            return;
        }

        // AE2's multiplier is a batch factor for the whole pattern (a 64x pattern crafts 64 at once), not
        // a per-slot repeat count. Matching must ignore it -- folding it in rejects every batched pattern,
        // which is exactly the regression a doubled processor pattern hit.
        var batched = new HandmadePattern(AEItemKey.of(output),
                List.of(new HandmadeInput(64, cell), new HandmadeInput(64, cell), new HandmadeInput(64, component)),
                List.of(new GenericStack(AEItemKey.of(output), 64)));
        var batchedMatch = cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes.resolve(
                helper.getLevel(), batched, output.getItem());
        if (batchedMatch == null) {
            helper.fail("a 64x batched processing pattern was refused: the multiplier belongs to the"
                    + " consuming side, not to the recipe comparison");
            return;
        }

        // Short by one ingredient: still has to be refused, or the machine would craft from less than the
        // recipe asks for.
        var shortOfOne = new HandmadePattern(AEItemKey.of(output),
                List.of(new HandmadeInput(1, cell), new HandmadeInput(1, component)),
                List.of(new GenericStack(AEItemKey.of(output), 1)));
        if (cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes.resolve(helper.getLevel(), shortOfOne,
                output.getItem()) != null) {
            helper.fail("a pattern asking for a single computation cell was accepted by a recipe that needs two");
            return;
        }

        // The multiplier is applied where items are actually taken: this 64x pattern asks for 128 cells and
        // 64 components. Cells stack to eight, so holding 128 of them legally takes sixteen slots and the
        // assembler only has nine. It used to be done by stuffing sixty-four into one slot -- an item stack
        // bigger than the item allows, which the game's own codec rejects when the machine is loaded again.
        // The honest answer for this shape is a refusal that consumes nothing.
        var patternInputs = batched.getInputs();
        KeyCounter[] supplied = new KeyCounter[patternInputs.length];
        for (int slot = 0; slot < patternInputs.length; slot++) {
            KeyCounter counter = new KeyCounter();
            for (GenericStack candidate : patternInputs[slot].getPossibleInputs()) {
                counter.add(candidate.what(), candidate.amount() * patternInputs[slot].getMultiplier());
            }
            supplied[slot] = counter;
        }
        var plan = new cn.dancingsnow.neoecoprototype.blockentity.crafting.ProcessorAssemblyPattern(
                batched, batchedMatch, (what, amount) -> helper.fail(
                        "a refused pattern must not reach the output sink at all"));
        long runs = plan.fittingRuns(supplied);
        if (runs != 0) {
            helper.fail("a 64x pattern needing sixteen slots of cells was offered " + runs
                    + " runs; nine slots cannot hold it");
            return;
        }
        if (supplied[0].get(cell.what()) != 64L) {
            helper.fail("asking whether a pattern fits consumed material: " + supplied[0].get(cell.what())
                    + " of the cells are gone");
            return;
        }
        helper.succeed();
    }

    /**
     * The L1 assembler inherits AE2's molecular assembler, so a pattern provider on the same grid can push
     * an ordinary crafting pattern straight into it. It is refused on purpose - the machine's job is the
     * processor recipes - and this is the guard that the refusal is real: the pattern used here is exactly
     * the kind AE2's own assembler would accept, and the refusal must not consume or store anything.
     */
    @GameTest(template = "empty", batch = "processor_assembler", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void assemblerRefusesOrdinaryCraftingPatterns(GameTestHelper helper) {
        var pos = new BlockPos(0, 1, 0);
        helper.setBlock(pos, ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get());
        if (!(helper.getBlockEntity(pos) instanceof cn.dancingsnow.neoecoprototype
                .blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity assembler)) {
            helper.fail("the assembler did not come into existence");
            return;
        }
        var chest = new ItemStack(Items.CHEST);
        net.minecraft.world.item.crafting.RecipeHolder<net.minecraft.world.item.crafting.CraftingRecipe> holder = null;
        for (var candidate : helper.getLevel().getRecipeManager()
                .getAllRecipesFor(net.minecraft.world.item.crafting.RecipeType.CRAFTING)) {
            if (ItemStack.isSameItemSameComponents(candidate.value().getResultItem(
                    helper.getLevel().registryAccess()), chest)) {
                holder = candidate;
                break;
            }
        }
        if (holder == null) {
            helper.fail("no vanilla crafting recipe yields a chest, so this cannot build a real pattern");
            return;
        }
        var used = new ItemStack[holder.value().getIngredients().size()];
        for (int i = 0; i < used.length; i++) {
            var ingredient = holder.value().getIngredients().get(i);
            used[i] = ingredient.isEmpty() ? ItemStack.EMPTY : ingredient.getItems()[0];
        }
        var encoded = PatternDetailsHelper.encodeCraftingPattern(holder, used, chest, false, false);
        IPatternDetails pattern = PatternDetailsHelper.decodePattern(encoded, helper.getLevel());
        if (!(pattern instanceof appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern)) {
            helper.fail("the test pattern is not one AE2's assembler would accept, so this proves nothing");
            return;
        }
        var slots = pattern.getInputs();
        KeyCounter[] supplied = new KeyCounter[slots.length];
        for (int i = 0; i < slots.length; i++) {
            KeyCounter counter = new KeyCounter();
            for (GenericStack stack : slots[i].getPossibleInputs()) {
                counter.add(stack.what(), stack.amount() * slots[i].getMultiplier());
            }
            supplied[i] = counter;
        }
        if (assembler.pushPattern(pattern, supplied, net.minecraft.core.Direction.UP)) {
            helper.fail("the assembler accepted an ordinary molecular-assembly pattern");
            return;
        }
        for (int i = 0; i < supplied.length; i++) {
            long left = 0L;
            for (GenericStack stack : slots[i].getPossibleInputs()) {
                left += supplied[i].get(stack.what());
            }
            long asked = 0L;
            for (GenericStack stack : slots[i].getPossibleInputs()) {
                asked += stack.amount() * slots[i].getMultiplier();
            }
            if (left != asked) {
                helper.fail("refusing the pattern consumed material from slot " + i + ": " + left
                        + " left of " + asked);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * A provider's terminal group is the machine it pushes to, and AE2 names that machine from two inputs
     * only: {@code Nameable.getName()} when the block entity has one, otherwise the display name of the
     * block's own item. This guard reads exactly those two inputs on both machines, so the load-bearing half
     * is that OUR assembler names itself while a real AE2 assembler names itself - not that the reader
     * returns nothing at all, which is what an empty pass would look like.
     */
    @GameTest(template = "empty", batch = "pattern_provider_group", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void providerGroupNamesTheFacedMachine(GameTestHelper helper) {
        var oursPos = new BlockPos(1, 1, 1);
        helper.setBlock(oursPos, ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get());
        var ours = helper.getBlockEntity(oursPos);
        if (ours == null) {
            helper.fail("our assembler did not come into existence, so nothing below can be read");
            return;
        }
        if (!(ours instanceof appeng.api.implementations.blockentities.ICraftingMachine)) {
            helper.fail("our assembler is not an ICraftingMachine, so AE2's fromMachine can never name it");
            return;
        }
        var oursExpected = ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get().getName().getString();
        var oursName = ours instanceof net.minecraft.world.Nameable nameable
                ? nameable.getName().getString()
                : AEItemKey.of(helper.getLevel().getBlockState(oursPos).getBlock())
                        .getDisplayName().getString();
        if (!oursExpected.equals(oursName)) {
            helper.fail("the machine a provider faces reads as \"" + oursName + "\" but its block name is \""
                    + oursExpected + "\"");
            return;
        }
        if (oursName.contains("分子装配室") || oursName.contains("Molecular")) {
            helper.fail("a provider facing our assembler would be listed as \"" + oursName + "\"");
            return;
        }
        // The control: the same two inputs on AE2's own assembler must produce AE2's own name, so the
        // green above cannot come from a reader that is blind.
        var aPos = new BlockPos(5, 5, 5);
        helper.setBlock(aPos, appeng.core.definitions.AEBlocks.MOLECULAR_ASSEMBLER.block());
        var aBe = helper.getBlockEntity(aPos);
        if (aBe == null) {
            helper.fail("AE2's molecular assembler did not come into existence, so the control read nothing");
            return;
        }
        var aExpected = appeng.core.definitions.AEBlocks.MOLECULAR_ASSEMBLER.block().getName().getString();
        var aName = aBe instanceof net.minecraft.world.Nameable nameable
                ? nameable.getName().getString()
                : AEItemKey.of(helper.getLevel().getBlockState(aPos).getBlock())
                        .getDisplayName().getString();
        if (!aExpected.equals(aName) || aName.equals(oursName)) {
            helper.fail("the control assembler reads as \"" + aName + "\", expected \"" + aExpected
                    + "\" and distinct from ours");
            return;
        }
        // The half that actually broke in game: AE2's getCraftingMachineInfo() hard-codes
        // AEBlocks.MOLECULAR_ASSEMBLER for both the name and the icon, so a subclass that does not override
        // it hands every asker AE2's identity. Read it on our machine and require our own.
        if (!(ours instanceof appeng.blockentity.crafting.MolecularAssemblerBlockEntity oursMachine)) {
            helper.fail("our assembler is not a MolecularAssemblerBlockEntity, so the info call cannot be read");
            return;
        }
        var oursInfo = oursMachine.getCraftingMachineInfo();
        var oursIcon = AEItemKey.of(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get());
        if (!oursIcon.equals(oursInfo.icon())) {
            helper.fail("our assembler advertises icon " + oursInfo.icon() + " instead of " + oursIcon);
            return;
        }
        if (!oursExpected.equals(oursInfo.name().getString())) {
            helper.fail("our assembler advertises the name \"" + oursInfo.name().getString()
                    + "\" instead of \"" + oursExpected + "\"");
            return;
        }
        // The control keeps those two from passing on a reader that is simply blind: AE2's own assembler
        // must still report AE2's icon and AE2's name through the same method.
        if (!(aBe instanceof appeng.blockentity.crafting.MolecularAssemblerBlockEntity aMachine)) {
            helper.fail("AE2's assembler BE is not a MolecularAssemblerBlockEntity, so the control read nothing");
            return;
        }
        var aInfo = aMachine.getCraftingMachineInfo();
        if (!AEItemKey.of(appeng.core.definitions.AEBlocks.MOLECULAR_ASSEMBLER.block()).equals(aInfo.icon())
                || !aExpected.equals(aInfo.name().getString())) {
            helper.fail("AE2's own assembler reports " + aInfo.icon() + " / \"" + aInfo.name().getString()
                    + "\", so this guard is not reading what AE2 hands out");
            return;
        }
        helper.succeed();
    }

    /**
     * A batched push may not consume more than the cycle can hand over. Snowballs are the shape that
     * shows it: the recipe yields four per run and a stack holds sixteen, so four runs fill one cycle --
     * and a fifth used to be paid for out of the network and then dropped, because AE2 rebuilds the grid
     * from {@code getRemainingItems()} and this interface's default is all-empty.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void processorAssemblerBatchCannotOverspend(GameTestHelper helper) {
        var block = new GenericStack(AEItemKey.of(Items.SNOW_BLOCK), 1);
        var recipe = new cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe(
                List.of(Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.SNOW_BLOCK),
                        Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.SNOW_BLOCK)),
                new ItemStack(Items.SNOWBALL, 4));
        var pattern = new HandmadePattern(AEItemKey.of(Items.SNOWBALL),
                List.of(new HandmadeInput(1, block), new HandmadeInput(1, block),
                        new HandmadeInput(1, block), new HandmadeInput(1, block)),
                List.of(new GenericStack(AEItemKey.of(Items.SNOWBALL), 4)));

        var key = AEItemKey.of(Items.SNOW_BLOCK);
        KeyCounter[] supplies = new KeyCounter[4];
        for (int slot = 0; slot < supplies.length; slot++) {
            supplies[slot] = new KeyCounter();
            supplies[slot].add(key, 20L);
        }

        var grid = new java.util.HashMap<Integer, ItemStack>();
        var handedOver = new long[1];
        var plan = new cn.dancingsnow.neoecoprototype.blockentity.crafting.ProcessorAssemblyPattern(
                pattern, recipe, (what, amount) -> handedOver[0] += amount);
        plan.fillCraftingGrid(supplies, grid::put);

        long left = 0;
        for (KeyCounter counter : supplies) left += counter.get(key);
        if (left != 64L) {
            helper.fail("a cycle that hands over one stack of 16 snowballs took " + (80L - left)
                    + " snow blocks out of the network; " + left + " should have been left where they are");
            return;
        }

        List<ItemStack> stacks = new ArrayList<>();
        for (int slot = 0; slot < 9; slot++) stacks.add(grid.getOrDefault(slot, ItemStack.EMPTY));
        var input = CraftingInput.of(3, 3, stacks);
        var output = plan.assemble(input, helper.getLevel());
        if (output.getCount() != 16) {
            helper.fail("16 consumed blocks should come back as 16 snowballs, got " + output.getCount());
            return;
        }
        plan.getRemainingItems(input);
        if (handedOver[0] != 0L) {
            helper.fail("a cycle that stayed inside one stack still handed " + handedOver[0]
                    + " snowballs to the output sink");
            return;
        }

        // The other shape: one run of the pattern asks for more than a stack can hold. A finished cycle
        // can still only eject one stack, so the rest has to leave through the output sink -- otherwise the
        // materials the machine already took are paid for and delivered as a fraction.
        var cell = stackOf("neoecoprototype:simplify_computation_cell_1m");
        if (cell.getMaxStackSize() != 8) {
            helper.fail("expected a result that stacks to eight, got " + cell.getMaxStackSize()
                    + " -- the shape below needs one that cannot fit one run");
            return;
        }
        var cellRecipe = new cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe(
                List.of(Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.REDSTONE),
                        Ingredient.of(Items.GOLD_INGOT)),
                new ItemStack(cell.getItem(), 1));
        var one = new GenericStack(AEItemKey.of(Items.SNOW_BLOCK), 1);
        var cellPattern = new HandmadePattern(AEItemKey.of(cell.getItem()),
                List.of(new HandmadeInput(64, one),
                        new HandmadeInput(64, new GenericStack(AEItemKey.of(Items.REDSTONE), 1)),
                        new HandmadeInput(64, new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 1))),
                List.of(new GenericStack(AEItemKey.of(cell.getItem()), 64)));
        var cellGrid = new java.util.HashMap<Integer, ItemStack>();
        var cellHandedOver = new long[1];
        var cellPlan = new cn.dancingsnow.neoecoprototype.blockentity.crafting.ProcessorAssemblyPattern(
                cellPattern, cellRecipe, (what, amount) -> cellHandedOver[0] += amount);
        var cellSupplies = new KeyCounter[3];
        for (int slot = 0; slot < cellSupplies.length; slot++) {
            cellSupplies[slot] = new KeyCounter();
            cellSupplies[slot].add(cellPattern.getInputs()[slot].getPossibleInputs()[0].what(), 64L * 40L);
        }
        cellPlan.fillCraftingGrid(cellSupplies, cellGrid::put);
        var cellStacks = new ArrayList<ItemStack>();
        for (int slot = 0; slot < 9; slot++) cellStacks.add(cellGrid.getOrDefault(slot, ItemStack.EMPTY));
        var cellInput = CraftingInput.of(3, 3, cellStacks);
        var ejected = cellPlan.assemble(cellInput, helper.getLevel());
        cellPlan.getRemainingItems(cellInput);
        if (ejected.getCount() != 8 || cellHandedOver[0] != 56L) {
            helper.fail("a pattern asking for 64 of an eight-stack item ejected " + ejected.getCount()
                    + " and handed the sink " + cellHandedOver[0] + "; together they have to make 64");
            return;
        }

        // The shape that used to take the world down: enough material pushed in that the batch needs more
        // than nine slots. The fill used to throw, and the throw rode out through AE2's grid tick into the
        // server tick loop. It has to shrink the batch instead.
        var heavyRecipe = new cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe(
                List.of(Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.SNOW_BLOCK),
                        Ingredient.of(Items.SNOW_BLOCK), Ingredient.of(Items.SNOW_BLOCK)),
                new ItemStack(Items.SNOWBALL, 1));
        var heavyUnit = new GenericStack(AEItemKey.of(Items.SNOW_BLOCK), 16);
        var heavyPattern = new HandmadePattern(AEItemKey.of(Items.SNOWBALL),
                List.of(new HandmadeInput(1, heavyUnit), new HandmadeInput(1, heavyUnit),
                        new HandmadeInput(1, heavyUnit), new HandmadeInput(1, heavyUnit)),
                List.of(new GenericStack(AEItemKey.of(Items.SNOWBALL), 1)));
        var heavySupplies = new KeyCounter[4];
        for (int slot = 0; slot < heavySupplies.length; slot++) {
            heavySupplies[slot] = new KeyCounter();
            heavySupplies[slot].add(AEItemKey.of(Items.SNOW_BLOCK), 2_500L);
        }
        var heavyGrid = new java.util.HashMap<Integer, ItemStack>();
        var heavyPlan = new cn.dancingsnow.neoecoprototype.blockentity.crafting.ProcessorAssemblyPattern(
                heavyPattern, heavyRecipe, (what, amount) -> helper.fail(
                        "a batch that fits the grid has no surplus to hand over: " + what + " x" + amount));
        if (heavyPlan.fittingRuns(heavySupplies) <= 0) {
            helper.fail("ten thousand snow blocks were on hand and the assembler still refused the pattern");
            return;
        }
        heavyPlan.fillCraftingGrid(heavySupplies, heavyGrid::put);
        if (heavyGrid.size() > 9) {
            helper.fail("the batch filled " + heavyGrid.size() + " grid slots");
            return;
        }
        var heavyStacks = new ArrayList<ItemStack>();
        for (int slot = 0; slot < 9; slot++) heavyStacks.add(heavyGrid.getOrDefault(slot, ItemStack.EMPTY));
        var heavyInput = CraftingInput.of(3, 3, heavyStacks);
        var heavyOut = heavyPlan.assemble(heavyInput, helper.getLevel());
        long heavyPlaced = 0;
        for (ItemStack stack : heavyGrid.values()) heavyPlaced += stack.getCount();
        if (heavyOut.getCount() * 64L != heavyPlaced) {
            helper.fail("the grid holds " + heavyPlaced + " snow blocks, which is not a whole number of the "
                    + heavyOut.getCount() + " snowballs this cycle pays for (64 blocks each)");
            return;
        }
        helper.succeed();
    }

    /** A pattern built by hand, laid out the way AE2's own processing patterns store their slots. */
    private record HandmadePattern(AEItemKey definition, List<HandmadeInput> slots,
            List<GenericStack> outputs) implements IPatternDetails {
        @Override
        public AEItemKey getDefinition() {
            return definition;
        }

        @Override
        public IPatternDetails.IInput[] getInputs() {
            return slots.toArray(new HandmadeInput[0]);
        }

        @Override
        public List<GenericStack> getOutputs() {
            return outputs;
        }
    }

    private record HandmadeInput(long multiplier, GenericStack... possibleInputs)
            implements IPatternDetails.IInput {
        @Override
        public GenericStack[] getPossibleInputs() {
            return possibleInputs;
        }

        @Override
        public long getMultiplier() {
            return multiplier;
        }

        @Override
        public boolean isValid(appeng.api.stacks.AEKey what, net.minecraft.world.level.Level level) {
            for (GenericStack candidate : possibleInputs) {
                if (candidate.what().equals(what)) return true;
            }
            return false;
        }

        @Override
        public appeng.api.stacks.AEKey getRemainingKey(appeng.api.stacks.AEKey template) {
            return possibleInputs[0].what();
        }
    }

    /**
     * The inscriber-derived whitelist must reproduce the curated simplification: a press recipe eats
     * printed parts, and each printed part has to unfold back into the material inscribed into it.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void processorRecipesDeriveFromInscriberMaterials(GameTestHelper helper) {
        var derived = cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes
                .derived(helper.getLevel(), List.of());
        helper.assertTrue(derived.size() >= 3, "derived only " + derived.size() + " recipes from the inscriber");

        // The bundled JSON recipes are exactly AE2's own press recipes, so dedupe must drop those three
        // while still letting other mods' inscriber recipes through.
        var declared = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get())
                .stream().map(net.minecraft.world.item.crafting.RecipeHolder::value).toList();
        var extra = cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes
                .derived(helper.getLevel(), declared);
        for (var expectation : List.of("ae2:logic_processor", "ae2:calculation_processor",
                "ae2:engineering_processor")) {
            Item output = stackOf(expectation).getItem();
            helper.assertTrue(extra.stream().noneMatch(recipe -> recipe.result().getItem() == output),
                    "derivation re-added " + expectation + ", which the bundled JSON already provides");
        }

        record Expectation(String material, String result) {}
        for (var expectation : List.of(
                new Expectation("minecraft:gold_ingot", "ae2:logic_processor"),
                new Expectation("ae2:certus_quartz_crystal", "ae2:calculation_processor"),
                new Expectation("minecraft:diamond", "ae2:engineering_processor"))) {
            List<ItemStack> stacks = List.of(stackOf("ae2:silicon"), stackOf("minecraft:redstone"),
                    stackOf(expectation.material()));
            var match = derived.stream()
                    .filter(recipe -> !recipe.result().isEmpty()
                            && recipe.result().getItem() == stackOf(expectation.result()).getItem())
                    .findFirst()
                    .orElse(null);
            helper.assertTrue(match != null,
                    "inscriber derivation produced no recipe for " + expectation.result());
            if (match == null) continue;
            for (ItemStack[] permutation : permutations(stacks)) {
                helper.assertTrue(match.matches(permutation), "derived recipe for " + expectation.result()
                        + " rejected the order " + describe(permutation));
            }
        }
        helper.succeed();
    }

    private static List<ItemStack[]> permutations(List<ItemStack> stacks) {
        List<ItemStack[]> out = new java.util.ArrayList<>();
        if (stacks.size() <= 1) {
            out.add(stacks.toArray(new ItemStack[0]));
            return out;
        }
        for (int first = 0; first < stacks.size(); first++) {
            List<ItemStack> rest = new java.util.ArrayList<>(stacks);
            rest.remove(first);
            for (ItemStack[] tail : permutations(rest)) {
                ItemStack[] whole = new ItemStack[stacks.size()];
                whole[0] = stacks.get(first);
                System.arraycopy(tail, 0, whole, 1, tail.length);
                out.add(whole);
            }
        }
        return out;
    }

    private static String describe(ItemStack[] stacks) {
        return java.util.Arrays.stream(stacks).map(stack -> stack.getItem().toString()).toList().toString();
    }

    private static ItemStack stackOf(String itemId) {
        var id = ResourceLocation.parse(itemId);
        var item = BuiltInRegistries.ITEM.get(id);
        if (item == null || item == net.minecraft.world.item.Items.AIR) {
            throw new IllegalStateException("missing item for processor test: " + itemId);
        }
        return new ItemStack(item);
    }

    /**
     * Polls every 10 ticks until {@code condition} holds, then runs {@code onSuccess}.
     * The failure message is a supplier so it reports the state at the moment of failure.
     */
    private static void waitUntil(GameTestHelper helper, int triesLeft, BooleanSupplier condition,
                                  java.util.function.Supplier<String> failureMessage,
                                  Runnable onSuccess) {
        if (condition.getAsBoolean()) {
            onSuccess.run();
            return;
        }
        if (triesLeft <= 0) {
            helper.fail(failureMessage.get());
            return;
        }
        helper.runAfterDelay(10,
                () -> waitUntil(helper, triesLeft - 1, condition, failureMessage, onSuccess));
    }
    /**
     * Guards the 1.2.5 regression: loading eco's interface UI classes must never abort with a fatal
     * mixin injection error. Loading with initialize = false still runs the transformer, which is the
     * part we care about, without touching client-only static state. The five title redirects that
     * used to be the fragile part are gone - eco 21.2.1 asks the block itself - so what is left here
     * is the guard for whichever mixins we still apply to those classes.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ecoInterfaceUiClassesLoadWithoutFatalMixinError(GameTestHelper helper) {
        for (var name : new String[]{
                "cn.dancingsnow.neoecoae.gui.storage.StorageInterfaceUI",
                "cn.dancingsnow.neoecoae.gui.crafting.CraftingInterfaceUI",
                "cn.dancingsnow.neoecoae.gui.computation.ComputationInterfaceUI"}) {
            try {
                Class.forName(name, false, NeoECOPrototypeGameTests.class.getClassLoader());
            } catch (Throwable failure) {
                helper.fail("loading " + name + " threw " + failure);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * eco 21.2.1 hands the GUI header to {@link GuiTitleProvider} on the block, so our four blocks must
     * answer with their own name and eco's fallback must survive everywhere else. This is the
     * assertion that goes red if a block stops implementing the interface - the old mixins were
     * require = 0, so losing a title there was silent for four releases.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ourBlocksOwnTheirGuiTitle(GameTestHelper helper) {
        var titled = new net.minecraft.world.level.block.Block[]{
                ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()};
        for (var block : titled) {
            Component resolved = GuiTitleProvider.title(block.defaultBlockState(),
                    Component.translatable("gui.neoecoae.interface.title"));
            if (!block.getDescriptionId().equals(titleKey(resolved))) {
                helper.fail(block.getDescriptionId() + " GUI title resolved to " + titleKey(resolved)
                        + ", expected its own key - the eco 21.2.1 GuiTitleProvider hook is not answering");
                return;
            }
        }
        // The plain interfaces keep eco's own header, so the hook cannot be blanket applied.
        Component kept = GuiTitleProvider.title(
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get().defaultBlockState(),
                Component.translatable("gui.neoecoae.storage_interface.title"));
        if (!"gui.neoecoae.storage_interface.title".equals(titleKey(kept))) {
            helper.fail("the non-network storage interface claimed a GUI title: " + titleKey(kept));
            return;
        }
        helper.succeed();
    }

    private static String titleKey(Component component) {
        return component.getContents() instanceof TranslatableContents contents
                ? contents.getKey() : "<" + component.getString() + ">";
    }

    /**
     * The things about a doll that are invisible to the compiler: the extra effect it has to hand out, the
     * tooltip line that describes it (it was silently missing once, and "没回血" was really "没说"), that
     * all four carry stats at all, that each blockstate turns with the facing it was set down in, and that
     * a placed or worn doll actually moves a creeper. Reads everything through the mod classloader, so it
     * costs no plot and touches no world.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void namedDollsGrantWhatTheyAdvertiseAndScareCreepers(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get());
        var extras = cn.dancingsnow.neoecoprototype.item.decoration.FumoItem.extraEffects(stack);
        // The tooltip lines are built from this same list, so what is asserted here is what a player is
        // promised - and the levels matter twice over: FumoEquipmentEffects hands this amplifier straight
        // to the MobEffectInstance, and describe() writes it into the name.
        if (extras.size() != 2) {
            helper.fail("TedXenon's doll hands out " + extras.size() + " effects, expected 2: " + extras);
            return;
        }
        if (extras.get(0).effect() != net.minecraft.world.effect.MobEffects.REGENERATION
                || extras.get(0).amplifier() != 1) {
            helper.fail("TedXenon's first effect is " + extras.get(0) + ", expected Regeneration at 1");
            return;
        }
        if (extras.get(1).effect() != net.minecraft.world.effect.MobEffects.LUCK
                || extras.get(1).amplifier() != 0) {
            helper.fail("TedXenon's second effect is " + extras.get(1) + ", expected Luck at 0");
            return;
        }
        // Vanilla writes the first level with no numeral and the second with one; the tooltip follows it,
        // so a doll promising "幸运 I" would be the thing that reads wrong.
        if (!extras.get(1).describe().getSiblings().isEmpty()) {
            helper.fail("the first-level effect's name carries " + extras.get(1).describe().getSiblings()
                    + ", expected no numeral after it");
            return;
        }
        var numerals = extras.get(0).describe().getSiblings();
        if (numerals.size() != 2 || !(numerals.get(1).getContents()
                instanceof net.minecraft.network.chat.contents.PlainTextContents literal)
                || !"II".equals(literal.text())) {
            helper.fail("the second-level effect's name carries " + numerals + ", expected \" II\"");
            return;
        }
        for (java.util.function.Supplier<? extends net.minecraft.world.item.Item> doll : List.of(
                ModRegistration.FUMO_DOLL_RELIQWQ_ITEM, ModRegistration.FUMO_DOLL_YANG120_ITEM,
                ModRegistration.FUMO_DOLL_KOOOKI_ITEM)) {
            if (!cn.dancingsnow.neoecoprototype.item.decoration.FumoItem
                    .extraEffects(new ItemStack(doll.get())).isEmpty()) {
                helper.fail(doll.get() + " hands out an effect it does not advertise in the guide");
                return;
            }
        }
        for (java.util.function.Supplier<? extends net.minecraft.world.item.Item> doll : List.of(
                ModRegistration.FUMO_DOLL_RELIQWQ_ITEM, ModRegistration.FUMO_DOLL_YANG120_ITEM,
                ModRegistration.FUMO_DOLL_KOOOKI_ITEM, ModRegistration.FUMO_DOLL_TEDXENON_ITEM)) {
            if (cn.dancingsnow.neoecoprototype.item.decoration.FumoItem
                    .statsOf(new ItemStack(doll.get())) == null) {
                helper.fail(doll.get() + " is in the creative tab as a doll but carries no stats");
                return;
            }
        }
        // The tooltip line is built from this key with the effect's own display name; if the key is
        // gone the doll stops advertising what it does and there is no error anywhere to notice.
        for (String lang : new String[]{"en_us", "zh_cn"}) {
            String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
            if (json == null) {
                return;
            }
            if (!json.contains("fumo_reliqwq.worn_extra")) {
                helper.fail("lang/" + lang + ".json has no fumo_reliqwq.worn_extra key");
                return;
            }
        }
        // Creepers back away from a plushie, worn or placed, and three things can undo that without a
        // word: the ticker hook never reaching the block (getTicker is an override, so a mistyped
        // signature would leave vanilla's null in place), a sweep whose radius or query matches nobody,
        // and a wearer check that reads the wrong slot. All three are read here against one creeper stood
        // two blocks from the spot a doll would sit in and then moved well away from it, and dressed and
        // undressed afterwards - the creeper is discarded on the way out, because one left wandering the
        // shared test room could detonate while unrelated tests read the same chunks.
        var dollState = ModRegistration.FUMO_DOLL_RELIQWQ_BLOCK.get().defaultBlockState();
        if (dollState.getTicker(helper.getLevel(), ModRegistration.FUMO_DOLL_BE.get()) == null) {
            helper.fail("a placed named doll ships no server ticker, so nothing ever sweeps for creepers");
            return;
        }
        // The cadence has to fire - once, and only once, in any five ticks. Always false would be a doll
        // that never looks for creepers at all, and always true would be one query per doll per tick,
        // which is the cost the interval exists to keep down.
        long now = helper.getLevel().getGameTime();
        int dueInFive = 0;
        for (long tick = now; tick < now + 5; tick++) {
            if (cn.dancingsnow.neoecoprototype.event.PlushieScare.sweepDue(tick)) {
                dueInFive++;
            }
        }
        if (dueInFive != 1) {
            helper.fail("the plushie sweep fell " + dueInFive + " times in five ticks, so its cadence is"
                    + " either never reached or running every tick");
            return;
        }
        // Deliberately without a setBlock: this test otherwise touches no world, and placing a block in
        // the shared test chunks adds save-and-unload work to a suite that is already hauling chunks at
        // coordinates ten million away. The block-to-block-entity pairing is therefore not asserted
        // here - the non-null ticker above says the doll's state offers one for that block entity type,
        // and the registration that lists the four blocks under it is the other half, which only the eye
        // confirms.
        BlockPos dollPos = helper.absolutePos(BlockPos.ZERO);
        // Built and added by hand rather than through the helper: GameTestHelper has no createEntity in
        // 1.21.1. Its AI is off so it cannot pick a target, swell and detonate in the shared test room
        // while unrelated tests read these chunks - the sweep only ever needs its position.
        var creeper = new net.minecraft.world.entity.monster.Creeper(
                net.minecraft.world.entity.EntityType.CREEPER, helper.getLevel());
        creeper.setNoAi(true);
        creeper.setPos(dollPos.getX() + 2.5D, dollPos.getY(), dollPos.getZ() + 0.5D);
        helper.getLevel().addFreshEntity(creeper);
        int nearDoll;
        int farFromDoll;
        boolean wearingDoll;
        boolean wearingNothing;
        try {
            nearDoll = cn.dancingsnow.neoecoprototype.blockentity.decoration.NamedDollBlockEntity
                    .scareAround(helper.getLevel(), dollPos);
            creeper.setPos(dollPos.getX() + 30.5D, dollPos.getY(), dollPos.getZ() + 0.5D);
            farFromDoll = cn.dancingsnow.neoecoprototype.blockentity.decoration.NamedDollBlockEntity
                    .scareAround(helper.getLevel(), dollPos);
            // The worn half of the same rule reads the head slot, so that is the slot it has to find.
            creeper.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                    new ItemStack(ModRegistration.FUMO_DOLL_RELIQWQ_ITEM.get()));
            wearingDoll = cn.dancingsnow.neoecoprototype.event.PlushieScare.wearsPlushie(creeper);
            creeper.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
            wearingNothing = cn.dancingsnow.neoecoprototype.event.PlushieScare.wearsPlushie(creeper);
        } finally {
            creeper.discard();
        }
        if (nearDoll < 1) {
            helper.fail("a creeper two blocks from a doll's position was not sent away by its sweep, so"
                    + " creepers ignore the dolls");
            return;
        }
        if (farFromDoll != 0) {
            helper.fail("the doll's sweep still reached a creeper 30 blocks away, so the scare radius"
                    + " does not bind and every loaded creeper is being pathed at");
            return;
        }
        // The worn half asks one question before it sweeps at all - is there a named doll on the head -
        // and a wrong slot here would leave the player tick sweeping for a doll nobody is wearing, or
        // never sweeping for one that is.
        if (!wearingDoll) {
            helper.fail("an entity with a named doll in its head slot is not read as wearing one, so a"
                    + " worn plushie repels nobody");
            return;
        }
        if (wearingNothing) {
            helper.fail("an empty head slot still reads as wearing a plushie, so every creeper near every"
                    + " player is being pathed at");
            return;
        }
        // And a doll set down has to look at whoever set it down. A blockstate key naming a property the
        // block lacks is dropped with one warning line and the state quietly keeps the other drawing, so
        // the two halves - the property on the block and the rotation in the file - are checked together.
        for (var doll : new Object[][]{
                {ModRegistration.FUMO_DOLL_RELIQWQ_BLOCK.get(), "fumo_doll_reliqwq"},
                {ModRegistration.FUMO_DOLL_YANG120_BLOCK.get(), "fumo_doll_yang120"},
                {ModRegistration.FUMO_DOLL_KOOOKI_BLOCK.get(), "fumo_doll_kouooki"},
                {ModRegistration.FUMO_DOLL_TEDXENON_BLOCK.get(), "fumo_doll_tedxenon"}}) {
            var block = (Block) doll[0];
            var path = "blockstates/" + doll[1] + ".json";
            var file = readShippedResource(
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path));
            if (file == null) {
                return;
            }
            var variants = com.google.gson.JsonParser.parseString(file).getAsJsonObject()
                    .getAsJsonObject("variants");
            if (variants == null) {
                helper.fail(path + " has no variants object");
                return;
            }
            for (var state : block.getStateDefinition().getPossibleStates()) {
                var facing = state.getValue(
                        cn.dancingsnow.neoecoprototype.block.decoration.NamedDollBlock.FACING);
                var key = "facing=" + valueName(state,
                        cn.dancingsnow.neoecoprototype.block.decoration.NamedDollBlock.FACING);
                if (!variants.has(key)) {
                    helper.fail(path + " has no \"" + key + "\" entry, so a doll placed facing " + facing
                            + " keeps the north drawing");
                    return;
                }
                int wanted = switch (facing) {
                    case NORTH -> 0;
                    case EAST -> 90;
                    case SOUTH -> 180;
                    case WEST -> 270;
                    default -> -1;
                };
                var variant = variants.getAsJsonObject(key);
                int drawn = variant.has("y") ? variant.get("y").getAsInt() : 0;
                if (wanted >= 0 && drawn != wanted) {
                    helper.fail(path + " gives y=" + drawn + " for " + key + ", which should be " + wanted
                            + ", so that facing renders turned the wrong way");
                    return;
                }
            }
        }
        helper.succeed();
    }


    /**
     * The plushie is on eco's fast path for foreign CPUs, and this is the only face that claim has.
     *
     * <p>{@code mixins/ae2/crafting/Ae2CraftingCpuFastPathMixin} injects a {@code neoecoae$fastPath} field
     * (type {@code ECOExternalCpuFastPath}, which also writes itself into the cluster's NBT) into AE2's
     * {@code CraftingCpuLogic} - the object whose {@code inventory}/{@code cluster}/{@code job} the mixin
     * shadows - reachable from the cluster through its public final {@code craftingLogic} field. Our CPU's
     * block entity *is* AE2's {@code CraftingBlockEntity} - deliberately, from the V1 decision - so the
     * cluster it hands out should carry that field with nothing for us to implement. If eco ever retargets
     * the mixin or renames the field, this goes red and the plushie silently drops back to the plain path.
     *
     * <p>The last check is the control: the same walk looking for a name that cannot exist must come up
     * empty, so the first check cannot be green because the walker always says yes.
     */
    @GameTest(template = "empty", batch = "doll_cpu", timeoutTicks = 240,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void plushieCpuCarriesEcosExternalCpuFastPath(GameTestHelper helper) {
        var dollPos = new BlockPos(2, 3, 2);
        var powerPos = dollPos.below();
        helper.setBlock(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(dollPos, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        Runnable leave = () -> {
            helper.setBlock(dollPos, Blocks.AIR);
            helper.setBlock(powerPos, Blocks.AIR);
        };
        waitUntil(helper, 8, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            return cpu != null && cpu.isFormed() && cpu.getCluster() != null;
        }, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            var why = "no formed plushie CPU with a cluster at " + at(dollPos) + ": blockEntity="
                    + (cpu == null ? "not AE2's crafting block entity" : "present")
                    + " formed=" + (cpu != null && cpu.isFormed());
            leave.run();
            return why;
        }, () -> {
            var cluster = plushieCpuAt(helper, dollPos).getCluster();
            var logic = cluster.craftingLogic;
            var field = ecoFastPathField(logic.getClass(), "neoecoae$fastPath");
            if (field == null) {
                leave.run();
                helper.fail("the plushie's " + logic.getClass().getName() + " carries no neoecoae$fastPath "
                        + "field up its class chain, so eco's external-CPU fast path is not attached to it");
                return;
            }
            if (!"cn.dancingsnow.neoecoae.crafting.execution.ECOExternalCpuFastPath"
                    .equals(field.getType().getName())) {
                leave.run();
                helper.fail("neoecoae$fastPath is typed " + field.getType().getName()
                        + ", expected eco's ECOExternalCpuFastPath");
                return;
            }
            if (ecoFastPathField(logic.getClass(), "neoecoae$noSuchField") != null) {
                leave.run();
                helper.fail("the field walk found a name that cannot exist, so the check above proves nothing");
                return;
            }
            leave.run();
            helper.succeed();
        });
    }

    /** Walks a class and its superclasses for a declared field name, the way the mixin attaches its state. */
    private static java.lang.reflect.Field ecoFastPathField(Class<?> type, String name) {
        for (Class<?> c = type; c != null && c != Object.class; c = c.getSuperclass()) {
            try {
                return c.getDeclaredField(name);
            } catch (NoSuchFieldException ignored) {
                // keep walking up
            }
        }
        return null;
    }

    /**
     * The plushie CPU is a whole machine in one block: it forms on its own, sits on the network, carries
     * the bytes and the threads it advertises, and is the one entry the crafting service lists. Twelve and
     * twelve are the design and are written here rather than read back from the registration, so a change
     * to either number has to be decided twice.
     */
    @GameTest(template = "empty", batch = "doll_cpu", timeoutTicks = 240,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void plushieCpuCarriesItsOwnBytesAndThreads(GameTestHelper helper) {
        var dollPos = new BlockPos(2, 3, 2);
        var powerPos = dollPos.below();
        helper.setBlock(powerPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(dollPos, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        Runnable leave = () -> {
            helper.setBlock(dollPos, Blocks.AIR);
            helper.setBlock(powerPos, Blocks.AIR);
        };
        waitUntil(helper, 8, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            var node = nodeAt(helper, dollPos);
            return cpu != null && cpu.isFormed() && node != null && node.getGrid() != null;
        }, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            var why = "the plushie never became a listed CPU at " + at(dollPos) + ": blockEntity="
                    + (cpu == null ? "not AE2's crafting block entity" : "present")
                    + " formed=" + (cpu != null && cpu.isFormed())
                    + " node=" + nodeGrid(helper, dollPos) + " power=" + nodeGrid(helper, powerPos);
            leave.run();
            return why;
        }, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            var cluster = cpu.getCluster();
            var cpus = nodeAt(helper, dollPos).getGrid().getCraftingService().getCpus();
            var name = cluster.getName();
            if (cpus.size() != 1) {
                leave.run();
                helper.fail("the plushie's grid lists " + cpus.size() + " CPUs, expected the plushie alone");
                return;
            }
            // The name is the part AE2 would otherwise invent: a cluster without a custom-named member is
            // named after the pile of units in it, and the plushie is one block that is not AE2's unit.
            if (name == null || !"yang咪口算单元".equals(name.getString())) {
                leave.run();
                helper.fail("the CPU is named " + (name == null ? "null" : "\"" + name.getString() + "\"")
                        + ", expected \"yang咪口算单元\"");
                return;
            }
            if (cpu.getStorageBytes() != 12L << 20 || cpu.getAcceleratorThreads() != 12) {
                leave.run();
                helper.fail("the plushie block itself reports bytes=" + cpu.getStorageBytes()
                        + " threads=" + cpu.getAcceleratorThreads() + ", expected " + (12L << 20) + " and 12");
                return;
            }
            if (cluster.getAvailableStorage() != 12L << 20 || cluster.getCoProcessors() != 12) {
                leave.run();
                helper.fail("the CPU the service hands out reports storage="
                        + cluster.getAvailableStorage() + " coProcessors=" + cluster.getCoProcessors()
                        + ", expected " + (12L << 20) + " and 12");
                return;
            }
            leave.run();
            helper.succeed();
        });
    }

    /**
     * A power cell beside the plushie reaches nobody, and the same cell under it does. The first half is the
     * control for the second: without it, "not connected" would be indistinguishable from a block entity
     * that never came online, so formation is read during the refused half rather than assumed.
     */
    @GameTest(template = "empty", batch = "doll_cpu_side", timeoutTicks = 300,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void plushieCpuReachesTheNetworkOnlyFromBelow(GameTestHelper helper) {
        var dollPos = new BlockPos(2, 3, 2);
        var sidePos = dollPos.east();
        var belowPos = dollPos.below();
        helper.setBlock(sidePos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(dollPos, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        Runnable leave = () -> {
            helper.setBlock(dollPos, Blocks.AIR);
            helper.setBlock(sidePos, Blocks.AIR);
            helper.setBlock(belowPos, Blocks.AIR);
        };
        waitUntil(helper, 8, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            return cpu != null && cpu.isFormed() && nodeAt(helper, sidePos) != null;
        }, () -> {
            var cpu = plushieCpuAt(helper, dollPos);
            var why = "the plushie did not form beside a power cell at " + at(dollPos) + ": formed="
                    + (cpu != null && cpu.isFormed()) + " node=" + nodeGrid(helper, dollPos)
                    + " power=" + nodeGrid(helper, sidePos);
            leave.run();
            return why;
        }, () -> {
            var dollGrid = nodeAt(helper, dollPos).getGrid();
            var sideGrid = nodeAt(helper, sidePos).getGrid();
            if (dollGrid != null && dollGrid == sideGrid) {
                leave.run();
                helper.fail("the cell east of the formed plushie joined its grid, so its channels do not"
                        + " leave through the bottom face only: doll " + exposureReport(helper, dollPos));
                return;
            }
            helper.setBlock(sidePos, Blocks.AIR);
            helper.setBlock(belowPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
            waitUntil(helper, 8, () -> {
                var node = nodeAt(helper, dollPos);
                var under = nodeAt(helper, belowPos);
                return node != null && under != null && under.getGrid() != null
                        && node.getGrid() == under.getGrid();
            }, () -> {
                var why = "with the power cell under it the plushie is still on its own grid: doll="
                        + nodeGrid(helper, dollPos) + " " + exposureReport(helper, dollPos)
                        + " under=" + nodeGrid(helper, belowPos) + " " + exposureReport(helper, belowPos);
                leave.run();
                return why;
            }, () -> {
                leave.run();
                helper.succeed();
            });
        });
    }

    /**
     * Which faces AE2 lets this node be reached through, and which ones it actually reached - the two
     * readings that tell a refused side apart from a node that never came up.
     */
    private static String exposureReport(GameTestHelper helper, BlockPos pos) {
        var node = nodeAt(helper, pos);
        if (node == null) {
            return "no node";
        }
        var exposure = node instanceof appeng.me.InWorldGridNode inWorld
                ? "exposed[down=" + inWorld.isExposedOnSide(Direction.DOWN)
                        + ",up=" + inWorld.isExposedOnSide(Direction.UP)
                        + ",east=" + inWorld.isExposedOnSide(Direction.EAST) + "]"
                : "not an in-world node";
        return exposure + " connected=" + node.getConnectedSides();
    }

    /**
     * An AE2 crafting storage stood against the plushie stays its own machine. AE2 finds the cells of a CPU
     * by geometry, not by network, so this is the half that a bottom face alone cannot refuse: if the two
     * ever shared a cluster, the plushie's bytes would be the wall's bytes plus twelve megabytes and the
     * threads would be shared too.
     */
    @GameTest(template = "empty", batch = "doll_cpu_wall", timeoutTicks = 240,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void plushieCpuStaysOutOfTheAe2CraftingWall(GameTestHelper helper) {
        var dollPos = new BlockPos(2, 3, 2);
        var belowPos = dollPos.below();
        var wallPos = dollPos.above();
        helper.setBlock(belowPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(wallPos, AEBlocks.CRAFTING_STORAGE_1K.block());
        helper.setBlock(dollPos, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        Runnable leave = () -> {
            helper.setBlock(dollPos, Blocks.AIR);
            helper.setBlock(wallPos, Blocks.AIR);
            helper.setBlock(belowPos, Blocks.AIR);
        };
        waitUntil(helper, 8, () -> {
            var doll = plushieCpuAt(helper, dollPos);
            var wall = plushieCpuAt(helper, wallPos);
            return doll != null && wall != null && doll.isFormed() && wall.isFormed();
        }, () -> {
            var doll = plushieCpuAt(helper, dollPos);
            var wall = plushieCpuAt(helper, wallPos);
            var why = "one of the two blocks did not form at " + at(dollPos) + "/" + at(wallPos)
                    + ": plushie=" + (doll == null ? "wrong block entity" : doll.isFormed())
                    + " ae2Storage=" + (wall == null ? "wrong block entity" : wall.isFormed());
            leave.run();
            return why;
        }, () -> {
            var doll = plushieCpuAt(helper, dollPos);
            var wall = plushieCpuAt(helper, wallPos);
            var dollCluster = doll.getCluster();
            var wallCluster = wall.getCluster();
            if (dollCluster == null || wallCluster == null) {
                leave.run();
                helper.fail("both blocks reported themselves formed, yet the clusters read as plushie="
                        + dollCluster + " ae2Storage=" + wallCluster + " at " + at(dollPos) + "/"
                        + at(wallPos));
                return;
            }
            if (dollCluster == wallCluster) {
                leave.run();
                helper.fail("the AE2 crafting storage above the plushie shares its cluster, whose storage is"
                        + " " + dollCluster.getAvailableStorage() + " bytes");
                return;
            }
            if (dollCluster.getAvailableStorage() != 12L << 20
                    || wallCluster.getAvailableStorage() != 1024L) {
                leave.run();
                helper.fail("the two machines report plushie=" + dollCluster.getAvailableStorage()
                        + " bytes, ae2Storage=" + wallCluster.getAvailableStorage()
                        + " bytes, expected " + (12L << 20) + " and 1024");
                return;
            }
            leave.run();
            helper.succeed();
        });
    }

    /**
     * Two plushies side by side are no CPU at all - the gate accepts a single plushie and nothing else.
     * The half that makes this a reading rather than a guess is the second one: take the neighbour back out
     * and the remaining plushie has to come back as a solo CPU, which rules out "this pile never worked".
     */
    @GameTest(template = "empty", batch = "doll_cpu_partial", timeoutTicks = 300,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void partialPlushiePileFormsNoCpu(GameTestHelper helper) {
        var first = new BlockPos(2, 2, 2);
        var second = first.east();
        var power = first.below();
        Runnable leave = () -> {
            helper.setBlock(first, Blocks.AIR);
            helper.setBlock(second, Blocks.AIR);
            helper.setBlock(power, Blocks.AIR);
        };
        helper.setBlock(power, AEBlocks.CREATIVE_ENERGY_CELL.block());
        helper.setBlock(first, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        helper.setBlock(second, ModRegistration.FUMO_DOLL_YANG120_CPU_BLOCK.get());
        // Refusal is the expected outcome, so this waits a fixed stretch rather than watching for a
        // decision: a test that waits for the pile to form would time out on the very behaviour it means
        // to pin. Forty ticks is twice what the solo plushie and the three-cubed pile took to form here.
        helper.runAfterDelay(40, () -> {
            var a = plushieCpuAt(helper, first);
            var b = plushieCpuAt(helper, second);
            if (a == null || b == null) {
                leave.run();
                helper.fail("one of the two plushies has no crafting block entity at all: "
                        + plushieReport(helper, first) + " / " + plushieReport(helper, second));
                return;
            }
            var aFormed = a.isFormed();
            var bFormed = b.isFormed();
            if (aFormed || bFormed) {
                leave.run();
                helper.fail("a 2-long pile formed: " + at(first) + "=" + aFormed + " " + at(second) + "="
                        + bFormed + " shared cluster=" + (a.getCluster() == b.getCluster())
                        + ", expected the size gate to refuse a pile of two plushies");
                return;
            }
            // Now the same two cells with the neighbour taken away have to form - otherwise the refusal
            // above would only prove that this spot never works.
            helper.setBlock(second, Blocks.AIR);
            waitUntil(helper, 8, () -> {
                var solo = plushieCpuAt(helper, first);
                return solo != null && solo.isFormed();
            }, () -> {
                var why = "after the neighbour was taken back the lone plushie still did not form: "
                        + plushieReport(helper, first) + " power=" + nodeGrid(helper, power);
                leave.run();
                return why;
            }, () -> {
                leave.run();
                helper.succeed();
            });
        });
    }

    /**
     * The giant plushie draws nothing of her own - her size comes from the client renderer - so the four
     * things that make that work are all readable without a frame: the block declares no model of its own,
     * the shared doll block entity type accepts her state (which is what lets one renderer cover her and
     * what keeps the creeper sweep running), that pairing offers a server ticker, and her blockstate names
     * the plain doll's model instead of carrying a second copy of the artwork.
     */
    @GameTest(template = "empty", batch = "doll_giant", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void giantPlushieDrawsThroughTheDollSheIsScaledFrom(GameTestHelper helper) {
        var giant = ModRegistration.FUMO_DOLL_YANG120_GIANT_BLOCK.get();
        var state = giant.defaultBlockState();
        if (state.getRenderShape() != net.minecraft.world.level.block.RenderShape.INVISIBLE) {
            helper.fail("the giant plushie renders as " + state.getRenderShape()
                    + ", expected INVISIBLE, so her own cube and the scaled drawing would both show up");
            return;
        }
        var dollType = ModRegistration.FUMO_DOLL_BE.get();
        if (!dollType.isValid(state)) {
            helper.fail("the shared doll block entity type does not accept the giant's state, so neither the"
                    + " one renderer nor the creeper sweep can ever run for her");
            return;
        }
        if (state.getTicker(helper.getLevel(), dollType) == null) {
            helper.fail("the giant plushie offers no server ticker, so she stopped repelling creepers the"
                    + " moment she grew");
            return;
        }
        var file = readShippedResource(ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                "blockstates/fumo_doll_yang120_giant.json"));
        if (file == null) {
            helper.fail("the giant plushie ships no blockstate, so the renderer has no model to scale");
            return;
        }
        var variants = com.google.gson.JsonParser.parseString(file).getAsJsonObject()
                .getAsJsonObject("variants");
        for (var facing : new String[]{"north", "east", "south", "west"}) {
            var variant = variants.getAsJsonObject("facing=" + facing);
            if (variant == null
                    || !"neoecoprototype:block/dolls/yang120".equals(variant.get("model").getAsString())) {
                helper.fail("facing=" + facing + " names "
                        + (variant == null ? "no entry" : variant.get("model"))
                        + ", expected the plain doll's model, so the giant is scaled off a second copy of her");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Nine plain plushies press into one giant at a crafting table. The recipe is the only survival path
     * to her, so this pins the four ways it could be wrong without crashing: no recipe at all, a second
     * recipe competing for the same output, an input widened to a tag that also takes the CPU plushie, and
     * a shape loose enough to accept an incomplete grid.
     */
    @GameTest(template = "empty", batch = "doll_giant_recipe", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ninePlainPlushiesPressIntoTheGiant(GameTestHelper helper) {
        var level = helper.getLevel();
        var plain = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_ITEM.get());
        var cpu = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_CPU_ITEM.get());
        var giant = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM.get());

        var makers = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(holder -> ItemStack.isSameItem(holder.value().getResultItem(level.registryAccess()),
                        giant))
                .toList();
        if (makers.size() != 1) {
            helper.fail(makers.size() + " crafting recipes yield the giant plushie, expected exactly one: "
                    + makers.stream().map(holder -> holder.id().toString()).collect(java.util.stream.Collectors.joining(", ")));
            return;
        }
        var recipe = makers.get(0).value();
        var result = recipe.getResultItem(level.registryAccess());
        if (result.getCount() != 1) {
            helper.fail("one pressing of nine plushies makes " + result.getCount()
                    + " giants, expected 1, so the easter egg counts as a compression in only one direction");
            return;
        }
        for (var ingredient : recipe.getIngredients()) {
            for (var asked : ingredient.getItems()) {
                if (!asked.is(plain.getItem())) {
                    helper.fail("the giant asks for " + asked + ", expected only the plain yang120 plushie,"
                            + " so a grid that should not press together might do so anyway");
                    return;
                }
            }
        }

        var pressed = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, java.util.Collections.nCopies(9, plain)), level).orElse(null);
        if (pressed == null || pressed.value() != recipe) {
            helper.fail("a full 3x3 of plain plushies resolves to "
                    + (pressed == null ? "no recipe" : pressed.id().toString())
                    + ", expected " + makers.get(0).id() + ", so the recipe never matches a real table");
            return;
        }

        // Control one: the same shape with a hole in it. A pattern that accepts this is not 3x3.
        var withAHole = new java.util.ArrayList<ItemStack>(java.util.Collections.nCopies(8, plain));
        withAHole.add(ItemStack.EMPTY);
        var holed = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, withAHole), level);
        if (holed.isPresent()) {
            helper.fail("eight plushies and an empty corner pressed into a giant via " + holed.get().id()
                    + ", so the pattern is looser than 3x3");
            return;
        }

        // Control two: the CPU plushie is a different item and must not count as a plain one.
        var cpuGrid = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(3, 3, java.util.Collections.nCopies(9, cpu)), level);
        if (cpuGrid.isPresent()) {
            helper.fail("nine CPU plushies matched " + cpuGrid.get().id()
                    + ", expected no match, so the giant recipe takes the working doll too");
            return;
        }
        helper.succeed();
    }

    /**
     * The giant presses back into the nine plushies she came from, in one slot of a crafting table.
     *
     * <p>The middle assertion is the one that keeps this file honest about last night's failure: a recipe
     * whose result counts more than the item stacks to is dropped at load in silence (measured here as
     * {@code Item stack with stack size of 9 was larger than maximum: 1}), so the plushies have to stay
     * stackable to nine or more for the unpacking to exist at all. {@code everyShippedRecipeFileReachesTheRecipeManager}
     * is the net under that; this is the version that says which number is load-bearing.</p>
     */
    @GameTest(template = "empty", batch = "doll_giant_unrecipe", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void theGiantPressesBackIntoNinePlushies(GameTestHelper helper) {
        var level = helper.getLevel();
        var plain = ModRegistration.FUMO_DOLL_YANG120_ITEM.get();
        var plainStack = new ItemStack(plain);
        var giant = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM.get());

        if (plainStack.getMaxStackSize() < 9) {
            helper.fail("the plain plushie stacks to " + plainStack.getMaxStackSize()
                    + ", below the 9 this unpacking hands out, so the recipe is dropped at load and nobody"
                    + " can unpack her");
            return;
        }

        var makers = level.getRecipeManager().getAllRecipesFor(RecipeType.CRAFTING).stream()
                .filter(holder -> ItemStack.isSameItem(holder.value().getResultItem(level.registryAccess()),
                        plainStack))
                .toList();
        if (makers.size() != 1) {
            helper.fail(makers.size() + " vanilla crafting recipes yield the plain plushie, expected exactly"
                    + " one (her assembly-room recipe is a different RecipeType and is not counted here): "
                    + makers.stream().map(holder -> holder.id().toString())
                            .collect(java.util.stream.Collectors.joining(", ")));
            return;
        }
        var recipe = makers.get(0).value();
        var result = recipe.getResultItem(level.registryAccess());
        if (result.getCount() != 9) {
            helper.fail("one giant unpacks into " + result.getCount()
                    + " plushies, expected 9, so the compression loses or invents plushies");
            return;
        }
        if (recipe.getIngredients().size() != 1) {
            helper.fail("the unpacking asks for " + recipe.getIngredients().size()
                    + " ingredient slots, expected 1, so it is no longer a one-into-nine unpack");
            return;
        }
        for (var asked : recipe.getIngredients().get(0).getItems()) {
            if (!asked.is(giant.getItem())) {
                helper.fail("the unpacking accepts " + asked + ", expected only the giant plushie, so some"
                        + " other item might unpack into nine plushies");
                return;
            }
        }

        var unpacked = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(1, 1, List.of(giant)), level).orElse(null);
        if (unpacked == null || unpacked.value() != recipe) {
            helper.fail("a table holding just the giant resolves to "
                    + (unpacked == null ? "no recipe" : unpacked.id().toString()) + ", expected "
                    + makers.get(0).id() + ", so the unpacking never matches a real table");
            return;
        }

        // Control: the plushie she unpacks into must not unpack again, or nine become eighty-one.
        var dollAlone = level.getRecipeManager().getRecipeFor(RecipeType.CRAFTING,
                CraftingInput.of(1, 1, List.of(plainStack)), level);
        if (dollAlone.isPresent()) {
            helper.fail("a single plain plushie matched " + dollAlone.get().id()
                    + ", expected no match, so plushies multiply on their own");
            return;
        }

        // And the nine have to arrive, not just be named: an empty survival pocket, then count what is in it.
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        player.getInventory().clearContent();
        if (!player.getInventory().add(new ItemStack(plain, 9))) {
            helper.fail("the inventory refused nine plushies outright, so the unpacking delivers nothing");
            return;
        }
        int total = 0;
        for (var held : player.getInventory().items) {
            if (held.is(plain)) {
                if (held.getCount() > held.getMaxStackSize()) {
                    helper.fail("a plushie stack of " + held.getCount() + " sits in a slot that takes "
                            + held.getMaxStackSize());
                    return;
                }
                total += held.getCount();
            }
        }
        if (total != 9) {
            helper.fail("nine plushies arrived as " + total
                    + ", expected 9 - what Inventory#add does not take never comes back");
            return;
        }
        helper.succeed();
    }

    /**
     * The plushies read apart in an inventory without needing a file to say so: the four named dolls share
     * one green, the working doll wears a different colour from all of them, and the giant is the only one
     * of the six with a glint. Every coloured name here is checked in three parts - it sits on ITEM_NAME
     * (on CUSTOM_NAME every stack would render as anvil-renamed and italic), it translates the block's own
     * key (invent a key nobody ships and the slot shows the raw letters), and that key is in both languages.
     */
    @GameTest(template = "empty", batch = "doll_names", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void plushiesReadApartInAnInventory(GameTestHelper helper) {
        var dolls = List.of(ModRegistration.FUMO_DOLL_RELIQWQ_ITEM, ModRegistration.FUMO_DOLL_YANG120_ITEM,
                ModRegistration.FUMO_DOLL_KOOOKI_ITEM, ModRegistration.FUMO_DOLL_TEDXENON_ITEM);

        Integer sharedGreen = null;
        for (var doll : dolls) {
            var stack = new ItemStack(doll.get());
            var colour = nameColour(helper, stack);
            if (colour == null) {
                return;
            }
            if (sharedGreen == null) {
                sharedGreen = colour;
            } else if (!sharedGreen.equals(colour)) {
                helper.fail(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                        + " wears a different green from the other named dolls: "
                        + Integer.toHexString(colour) + " vs " + Integer.toHexString(sharedGreen));
                return;
            }
            if (stack.has(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) {
                helper.fail("a plain named doll glinted, so the giant's glint stops being the thing that"
                        + " tells them apart");
                return;
            }
        }
        if (sharedGreen == null) {
            helper.fail("none of the named dolls was reached, so this measured no colour at all");
            return;
        }

        var cpu = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_CPU_ITEM.get());
        var cpuColour = nameColour(helper, cpu);
        if (cpuColour == null) {
            return;
        }
        if (cpuColour.equals(sharedGreen)) {
            helper.fail("the CPU plushie's name is the same colour as the plain dolls ("
                    + Integer.toHexString(cpuColour) + "), so the working one does not read apart on a shelf");
            return;
        }

        var giant = new ItemStack(ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM.get());
        if (giant.has(net.minecraft.core.component.DataComponents.ITEM_NAME)) {
            helper.fail("the giant plushie was given a coloured name, so it is now told apart twice over and"
                    + " one of those can be missed");
            return;
        }
        if (!giant.has(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE)) {
            helper.fail("the giant plushie has no glint, so in an inventory of stacked plushies she is"
                    + " indistinguishable from the doll she is drawn from");
            return;
        }
        if (!Boolean.TRUE.equals(
                giant.get(net.minecraft.core.component.DataComponents.ENCHANTMENT_GLINT_OVERRIDE))) {
            helper.fail("the giant's glint override is not TRUE, which the renderer reads as no glint");
            return;
        }

        // The shelf numbers the same change moved: every plushie stacks to sixteen.
        var onShelf = new ArrayList<ItemStack>();
        for (var doll : dolls) {
            onShelf.add(new ItemStack(doll.get()));
        }
        onShelf.add(cpu);
        onShelf.add(giant);
        for (var stack : onShelf) {
            if (stack.getMaxStackSize() != 16) {
                helper.fail(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                        + " stacks to " + stack.getMaxStackSize() + ", expected 16");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * The charging doll, read on the three layers a player can tell apart: the stat the tooltip quotes, the
     * tick that pays it out, and the number that actually lands in a carried item. The item is AE2's own
     * charged staff because it answers the NeoForge item energy capability through
     * {@code PoweredItemCapabilities} - the same surface any FE tool in the pack answers - and because it
     * holds far more than the two sweeps below, so its own capacity is not what bounds the reading.
     */
    @GameTest(template = "empty", batch = "doll_charge", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void tedxenonsDollChargesWhatTheWearerCarries(GameTestHelper helper) {
        record DollRate(java.util.function.Supplier<? extends net.minecraft.world.item.Item> doll,
                        double aePerTick) {
        }

        // Exactly one doll carries a rate, and the other five carry zero - a second charging doll would be
        // a content change nobody announced, and a zero here means the sweep never asks any stack at all.
        // The one non-zero value is the shipped default, the same constant the config key defaults to, so
        // the doll's stat and the key cannot drift apart.
        var rates = List.of(
                new DollRate(ModRegistration.FUMO_DOLL_RELIQWQ_ITEM, 0.0D),
                new DollRate(ModRegistration.FUMO_DOLL_YANG120_ITEM, 0.0D),
                new DollRate(ModRegistration.FUMO_DOLL_KOOOKI_ITEM, 0.0D),
                new DollRate(ModRegistration.FUMO_DOLL_YANG120_CPU_ITEM, 0.0D),
                new DollRate(ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM, 0.0D),
                new DollRate(ModRegistration.FUMO_DOLL_TEDXENON_ITEM,
                        NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT));
        for (var rate : rates) {
            var stats = cn.dancingsnow.neoecoprototype.item.decoration.FumoItem
                    .statsOf(new ItemStack(rate.doll().get()));
            if (stats == null) {
                helper.fail(rate.doll().get() + " carries no stats at all, so nothing about it can be read");
                return;
            }
            if (stats.chargeAEPerTick() != rate.aePerTick()) {
                helper.fail(rate.doll().get() + " pays out " + stats.chargeAEPerTick()
                        + " AE/t, expected " + rate.aePerTick());
                return;
            }
        }

        // The shipped default, pinned without reading a config file: the key's default and the constant
        // the doll is registered with are the same number, and that number is 100 AE/t.
        if (NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK.getDefault()
                != NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT) {
            helper.fail("the charge key defaults to "
                    + NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK.getDefault() + " AE/t, but the doll"
                    + " is registered with " + NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT
                    + " - the tooltip and the payout would be authored from two different numbers");
            return;
        }
        if (NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT != 100.0D) {
            helper.fail("the shipped charge rate is "
                    + NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT + " AE/t, expected the"
                    + " announced 100 - if this was meant, the changelog has to say so too");
            return;
        }
        // The live rate is the server's, and every payout below is measured against it rather than against
        // a literal, so a run with the key set away from its default pins the wiring: a sweep that kept a
        // hard-coded 100 AE/t would fail that run and pass this one.
        double configuredRate = NeoECOPrototypeServerConfig.fumoChargeAePerTick();
        if (configuredRate <= 0.0D) {
            helper.fail("this run's server config has charging at " + configuredRate + " AE/t, so every"
                    + " reading below would be zero by construction; the sweep needs a non-zero rate");
            return;
        }
        int sweepBudget = (int) Math.round(configuredRate * 5.0D);

        // The line the tooltip shows carries no number at all: the rate is a server key, and a dedicated
        // client never loads one, so a number here would have to be hard-coded and could drift.
        String chargeKey = "block.neoecoprototype.fumo_reliqwq.worn_charge";
        String chargeLine = Component.translatable(chargeKey).getString();
        var tedTooltip = new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get())
                .getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                        net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        if (tedTooltip.stream().noneMatch(line -> line.getString().equals(chargeLine))) {
            helper.fail("TedXenon's tooltip has no line equal to '" + chargeLine + "', it has "
                    + tedTooltip.stream().map(Component::getString).toList());
            return;
        }
        for (var rate : rates) {
            if (rate.aePerTick() > 0.0D) {
                continue;
            }
            var lines = new ItemStack(rate.doll().get())
                    .getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                            net.minecraft.world.item.TooltipFlag.Default.NORMAL);
            if (lines.stream().anyMatch(line -> line.getString().equals(chargeLine))) {
                helper.fail(rate.doll().get() + " advertises charging, but its rate is zero, so the line"
                        + " and the sweep disagree");
                return;
            }
        }
        for (String lang : new String[]{"en_us", "zh_cn"}) {
            String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
            if (json == null) {
                return;
            }
            var table = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (!table.has(chargeKey)) {
                helper.fail("lang/" + lang + ".json has no " + chargeKey + " key");
                return;
            }
            String text = table.get(chargeKey).getAsString();
            // The tooltip passes no argument now, so a placeholder left in either language would render
            // wrong or throw; the key has to be a plain sentence.
            if (text.contains("%")) {
                helper.fail("lang/" + lang + ".json still writes a placeholder into " + chargeKey + " ('"
                        + text + "'), but the tooltip passes no argument any more");
                return;
            }
        }

        // The cadence itself has to fire once in five wearer ticks: always false is a doll that never pays
        // out, always true is forty-one capability lookups every tick.
        int dueInFive = 0;
        for (int tick = 0; tick < 5; tick++) {
            if (cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects.chargeDue(tick)) {
                dueInFive++;
            }
        }
        if (dueInFive != 1) {
            helper.fail("the charging cadence fell " + dueInFive + " times in five ticks, so it is either"
                    + " never reached or running every tick");
            return;
        }

        // Then the tick itself. FakePlayer is the only stand-in that is a ServerPlayer and it has no
        // connection to be surprised by the effect packets this handler sends.
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "doll-charge"));
        player.getInventory().clearContent();
        player.getInventory().selected = 0;
        player.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD,
                new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get()));
        var staff = AEItems.CHARGED_STAFF.stack();
        player.getInventory().setItem(10, staff);
        var staffEnergy = net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM
                .getCapability(staff, null);
        if (staffEnergy == null) {
            helper.fail("a fresh charged staff answers no item energy capability, so this test would"
                    + " measure an empty loop and pass");
            return;
        }
        int start = staffEnergy.getEnergyStored();
        if (start != 0) {
            helper.fail("a fresh charged staff starts with " + start + " AE, expected 0 - the reading"
                    + " below would be off by that much");
            return;
        }
        // One paying tick is five ticks' worth at the configured rate.
        player.tickCount = 0;
        cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        if (staffEnergy.getEnergyStored() != sweepBudget) {
            helper.fail("one paying tick left the staff at " + staffEnergy.getEnergyStored()
                    + " AE, expected " + sweepBudget + " (" + configuredRate
                    + " AE/t from the server config times the five-tick sweep)");
            return;
        }
        // And a tick off the cadence pays nothing, which is what keeps the cost bounded.
        player.tickCount = 1;
        cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        if (staffEnergy.getEnergyStored() != sweepBudget) {
            helper.fail("a tick off the cadence still paid out: the staff went from " + sweepBudget + " to "
                    + staffEnergy.getEnergyStored() + " AE");
            return;
        }
        // The held slot is part of the sweep: the stack in the wearer's hand is slot 0, so it takes the
        // whole budget first and the backpack staff behind it gets nothing this tick.
        var held = AEItems.CHARGED_STAFF.stack();
        player.getInventory().setItem(0, held);
        player.getInventory().selected = 0;
        var heldEnergy = net.neoforged.neoforge.capabilities.Capabilities.EnergyStorage.ITEM
                .getCapability(held, null);
        int backpackBefore = staffEnergy.getEnergyStored();
        player.tickCount = 5;
        cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        if (heldEnergy == null || heldEnergy.getEnergyStored() != sweepBudget) {
            helper.fail("the staff in the wearer's hand ended at "
                    + (heldEnergy == null ? "no capability" : heldEnergy.getEnergyStored() + " AE")
                    + ", expected " + sweepBudget + " - the held slot is not being charged");
            return;
        }
        if (staffEnergy.getEnergyStored() != backpackBefore) {
            helper.fail("the sweep paid out more than one budget: the backpack staff went from "
                    + backpackBefore + " to " + staffEnergy.getEnergyStored() + " AE in the same tick");
            return;
        }
        // A full inventory accepts nothing; the sweep has to report zero rather than count the request.
        staffEnergy.receiveEnergy(Integer.MAX_VALUE, false);
        heldEnergy.receiveEnergy(Integer.MAX_VALUE, false);
        if (staffEnergy.getEnergyStored() != staffEnergy.getMaxEnergyStored()) {
            helper.fail("the staff would not fill: " + staffEnergy.getEnergyStored() + " of "
                    + staffEnergy.getMaxEnergyStored() + ", so the full-stack control below is unarmed");
            return;
        }
        int paidIntoFull = cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects
                .chargeInventory(player, sweepBudget);
        if (paidIntoFull != 0) {
            helper.fail("a full inventory still reported " + paidIntoFull + " AE paid out");
            return;
        }
        helper.succeed();
    }

    /**
     * One plushie's name, checked where it has to sit: ITEM_NAME, translating the block's own key, present
     * in both languages, with a colour. Null once a {@code helper.fail} has been written - the caller must
     * return on null.
     */
    private static Integer nameColour(GameTestHelper helper, ItemStack stack) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
            helper.fail(id + "'s colour sits on CUSTOM_NAME, so every stack reads as renamed and renders"
                    + " italic");
            return null;
        }
        var name = stack.get(net.minecraft.core.component.DataComponents.ITEM_NAME);
        if (name == null) {
            helper.fail(id + " has no ITEM_NAME, so it shows plain white in an inventory");
            return null;
        }
        if (!(name.getContents() instanceof net.minecraft.network.chat.contents.TranslatableContents contents)) {
            helper.fail(id + "'s name is not a translation key, so it cannot follow the language");
            return null;
        }
        var key = contents.getKey();
        var blockKey = ((net.minecraft.world.item.BlockItem) stack.getItem()).getBlock().getDescriptionId();
        if (!key.equals(blockKey)) {
            helper.fail(id + " names " + key + ", expected the block's own " + blockKey
                    + ", so the item and the placed block would drift apart");
            return null;
        }
        var colour = name.getStyle().getColor();
        if (colour == null) {
            helper.fail(id + "'s name has no colour, so the shelf cannot read which plushie it is");
            return null;
        }
        for (var lang : List.of("zh_cn", "en_us")) {
            var table = com.google.gson.JsonParser.parseString(
                    readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper)).getAsJsonObject();
            if (!table.has(key) || table.get(key).getAsString().isBlank()) {
                helper.fail("lang/" + lang + ".json has no " + key
                        + ", so the coloured name shows the raw key instead of a word");
                return null;
            }
        }
        return colour.getValue();
    }

    /** What an ItemStack is, in the short form a failure line needs. */
    private static String itemName(ItemStack stack) {
        return net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(stack.getItem())
                + " x" + stack.getCount();
    }

    /** What one plushie cell currently reads, for the failure lines that have to say which half failed. */
    private static String plushieReport(GameTestHelper helper, BlockPos pos) {
        var cpu = plushieCpuAt(helper, pos);
        if (cpu == null) {
            return "no crafting block entity";
        }
        return "formed=" + cpu.isFormed() + " cluster=" + cpu.getCluster()
                + " node=" + nodeGrid(helper, pos);
    }

    /** AE2's own crafting-unit block entity standing at a template-relative cell, or null. */
    @javax.annotation.Nullable
    private static CraftingBlockEntity plushieCpuAt(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof CraftingBlockEntity cpu
                ? cpu : null;
    }


    /** Fails naming the path that lost the owner, so a break reports where rather than just going red. */
    private static boolean sameOwner(net.minecraft.world.item.component.ResolvableProfile expected,
                                     @org.jetbrains.annotations.Nullable net.minecraft.world.item.component.ResolvableProfile actual,
                                     String path, GameTestHelper helper) {
        if (actual != null && expected.name().equals(actual.name()) && expected.id().equals(actual.id())
                && propertiesOf(expected).equals(propertiesOf(actual))) {
            return true;
        }
        helper.fail("the doll loses its owner through the " + path + " path: expected " + expected
                + ", got " + (actual == null ? "no owner at all" : actual));
        return false;
    }

    private static List<com.mojang.authlib.properties.Property> propertiesOf(
            net.minecraft.world.item.component.ResolvableProfile profile) {
        return List.copyOf(profile.properties().get("textures"));
    }

    private static String readResource(String path, GameTestHelper helper) {
        try (var stream = NeoECOPrototype.class.getResourceAsStream(path)) {
            if (stream == null) {
                helper.fail("missing " + path + " on the classpath");
                return null;
            }
            return new String(stream.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            helper.fail("reading " + path + " threw " + failure);
            return null;
        }
    }

    /**
     * Guards the resources that code only names inside a string, which is the one set an
     * "unreferenced file" cleanup cannot see: deleting the assembler style JSON made every player
     * leave the world when the processor assembly GUI opened. Part models are whitelisted by AE2 at
     * startup and their cable item models are derived from the naming convention, so a missing file
     * surfaces as a crash while tesselating chunks, not as a compile error. Reads through the mod
     * classloader because a dedicated server does not have to index {@code assets/}.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void stringReferencedAssetsAreShipped(GameTestHelper helper) {
        for (ResourceLocation location : List.of(
                // ProcessorAssemblerScreen.STYLE, read when the GUI opens
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "screens/processor_assembler.json"),
                // The three interfaces go through AE2's StyleManager, which resolves inside its own
                // namespace, so NeoECOPrototypeClient's "/screens/neoecoprototype/*.json" paths land here
                ResourceLocation.fromNamespaceAndPath("ae2",
                        "screens/neoecoprototype/l1_powered_me_interface.json"),
                ResourceLocation.fromNamespaceAndPath("ae2",
                        "screens/neoecoprototype/superconductive_interface.json"),
                ResourceLocation.fromNamespaceAndPath("ae2",
                        "screens/neoecoprototype/l1_pattern_provider.json"),
                // SimplifyTier / SimplifyCraftingTier badge. eco reads it in two places only
                // (ComputationCpuEntry and its CraftingStatus menu mixin), both of them its CPU list -
                // nothing in our own panel draws it, which is why a wrong-size file is invisible in-game
                // until someone opens a CPU picker.
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/tier/l1.png"),
                // The five panel sprites eco cuts at their top-left corner once our blocks answer
                // IHostPanelTextureProvider: 10x5 / 10x6 / 10x6 for the computation rows and 13x18 plus a
                // 13x16 two-state strip for the storage panel. Nothing in Java loads them by name, so a
                // deleted file would only show as a blank cell in someone's panel.
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/computation_upper.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/computation_core.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/computation_lower.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/storage_tier.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/storage_marker.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/panel/host_marker.png"),
                // PartModel base models, frozen by PartModels.registerModels
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/part/powered_me_interface.json"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/part/superconductive_interface.json"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/part/pattern_provider.json"),
                // AE2 derives item models as models/item/cable_<part>.json from the part location
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/item/cable_powered_me_interface.json"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/item/cable_superconductive_interface.json"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "models/item/cable_pattern_provider.json"),
                // The two names NeoECOPrototypeClient hands to the fluid extension. The bucket model bakes
                // one of them at load time, so a missing file is a pink square, not a startup error.
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/dissolution_solution_still.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/dissolution_solution_flow.png"))) {
            String archivePath = "/assets/" + location.getNamespace() + "/" + location.getPath();
            try (var resource = NeoECOPrototype.class.getResourceAsStream(archivePath)) {
                if (resource == null) {
                    helper.fail("missing " + location + " (" + archivePath + ")");
                    return;
                }
                if (resource.readAllBytes().length == 0) {
                    helper.fail("empty " + location);
                    return;
                }
            } catch (java.io.IOException failure) {
                helper.fail("could not read " + location + ": " + failure);
                return;
            }
        }
        // Having the png is only half of it: the block atlas stitches what somebody lists, and the fluid
        // renderer looks its sprites up by name in that atlas. A texture that exists but is never listed
        // renders as the missing sprite while everything starts fine.
        String stitched;
        try {
            stitched = blockAtlasMergeText();
        } catch (java.io.IOException failure) {
            helper.fail("could not enumerate the block atlas definitions: " + failure);
            return;
        }
        if (stitched.isEmpty()) {
            helper.fail("no assets/minecraft/atlases/blocks.json on the classpath, so the atlas guard"
                    + " checked nothing");
            return;
        }
        for (String sprite : List.of("dissolution_solution_still", "dissolution_solution_flow")) {
            var texture = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/" + sprite);
            if (!stitched.contains(texture.toString())) {
                helper.fail(texture + " is not listed by any blocks.json, so the fluid and its bucket get"
                        + " no sprite from the block atlas");
                return;
            }
        }
        // Every block we register has to ship a block loot table, or breaking it drops nothing. AE2's own
        // family does this through the table (100 tables, exactly one of them using a loot function), so
        // this is not a path a subclass inherits from MolecularAssemblerBlockEntity. Two incidents came
        // from ignoring it: the doll's table was unparsable because it keyed the function "type", and ten
        // blocks - three network interfaces, the powered ME and superconductive interfaces, the pattern
        // provider, the assembler, two energized cores and the green aluminium casing - had no file at all,
        // which is what makes a wrench right-click delete a machine.
        var resourceManager = helper.getLevel().getServer().getResourceManager();
        for (var entry : BuiltInRegistries.BLOCK.entrySet()) {
            var id = entry.getKey().location();
            if (!id.getNamespace().equals(NeoECOPrototype.MOD_ID)) continue;
            // A liquid block drops nothing on purpose - water and lava declare it the same way - and the
            // bucket, not the block, is the thing a player can pick up.
            if (entry.getValue() instanceof net.minecraft.world.level.block.LiquidBlock) continue;
            var table = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    "loot_table/blocks/" + id.getPath() + ".json");
            if (resourceManager.getResource(table).isEmpty()) {
                helper.fail("block " + id + " has no " + table + ", so breaking or wrenching it drops "
                        + "nothing and the machine is destroyed silently");
                return;
            }
        }
        // An animation strip is only a strip if its .mcmeta came along. Blockbench exports the frames
        // stacked inside the png, and the metadata file beside it is what tells the game to walk them; drop
        // that file and every frame gets stretched into the face at once, so a light that animates in the
        // editor ships as one still picture. Two of TedXenon's 10-01 sheets came in that state.
        java.util.SortedSet<String> unanimated;
        int strips;
        try {
            var scan = animationStripsWithoutMetadata();
            if (scan == null) {
                var classpath = System.getProperty("java.class.path");
                helper.fail("found no textures under " + "assets/" + NeoECOPrototype.MOD_ID + "/textures or its"
                        + " fallback-pack twin, so the animation guard checked nothing; classpath="
                        + classpath.substring(0, Math.min(200, classpath.length())));
                return;
            }
            unanimated = scan.missing();
            strips = scan.strips();
        } catch (java.io.IOException failure) {
            helper.fail("could not enumerate our textures: " + failure);
            return;
        }
        if (!unanimated.isEmpty()) {
            helper.fail("animation strip(s) with no .mcmeta beside them: " + unanimated);
            return;
        }
        NeoECOPrototype.LOGGER.info("animation strips, both packs: {} of them, every one with its .mcmeta",
                strips);
        helper.succeed();
    }

    /**
     * Our textures that are a whole number of frames tall and have no {@code .mcmeta} next to them. Read
     * straight off the classpath because the resource manager will happily hand back a png that is missing
     * nothing it claims to need -- the missing file is the one nobody asks for. Null when the scan found no
     * texture at all, which means the apparatus is blind rather than the assets clean.
     */
    /** Strips found, and the ones missing their metadata. Null when the scan saw no texture at all. */
    private record AnimationScan(java.util.SortedSet<String> missing, int strips) { }

    private static AnimationScan animationStripsWithoutMetadata() throws java.io.IOException {
        // The built-in fallback pack ships its own copies of the animated sheets, and it lost one of the
        // two .mcmeta files when it was copied over -- a strip without metadata is a still picture.
        var folders = List.of("assets/" + NeoECOPrototype.MOD_ID + "/textures",
                "legacy_art/assets/" + NeoECOPrototype.MOD_ID + "/textures");
        var present = new java.util.TreeSet<String>();
        var frames = new java.util.HashMap<String, int[]>();
        for (var entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            java.nio.file.Path root = java.nio.file.Path.of(entry).toAbsolutePath();
            for (var folder : folders) {
                java.nio.file.Path start = root.resolve(folder);
                if (java.nio.file.Files.isDirectory(start)) {
                    try (var walk = java.nio.file.Files.walk(start)) {
                        for (var path : (Iterable<java.nio.file.Path>) walk
                                .filter(java.nio.file.Files::isRegularFile)::iterator) {
                            var name = folder + "/" + start.relativize(path).toString()
                                    .replace(java.io.File.separator, "/");
                            present.add(name);
                            if (name.endsWith(".png")) {
                                try (var in = java.nio.file.Files.newInputStream(path)) {
                                    rememberSize(name, in.readNBytes(24), frames);
                                }
                            }
                        }
                    }
                } else if (entry.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")
                        && java.nio.file.Files.isRegularFile(root)) {
                    try (var zip = new java.util.zip.ZipFile(root.toFile())) {
                        for (var file : java.util.Collections.list(zip.entries())) {
                            if (!file.getName().startsWith(folder + "/") || file.isDirectory()) continue;
                            present.add(file.getName());
                            if (file.getName().endsWith(".png")) {
                                try (var in = zip.getInputStream(file)) {
                                    rememberSize(file.getName(), in.readNBytes(24), frames);
                                }
                            }
                        }
                    }
                }
            }
        }
        if (frames.isEmpty()) return null;
        var missing = new java.util.TreeSet<String>();
        var strips = 0;
        for (var size : frames.entrySet()) {
            int width = size.getValue()[0], height = size.getValue()[1];
            if (height <= width || height % width != 0) {
                continue;
            }
            strips++;
            if (!present.contains(size.getKey() + ".mcmeta")) {
                missing.add(size.getKey() + " is " + width + "x" + height + ", " + height / width
                        + " frames tall, with nothing to animate it");
            }
        }
        return new AnimationScan(missing, strips);
    }

    /**
     * The two keys that decide whether a machine's lamps glow, read off NeoForge 21.1.251's constant pools:
     * {@code BlockElement$Deserializer} takes {@code shade} <i>and</i> {@code neoforge_data}, while
     * {@code BlockElementFace$Deserializer} takes {@code uv, texture, cullface, tintindex, neoforge_data}
     * and never {@code shade}. A face-level {@code shade} is therefore decoration in the file only, and
     * 1.3.5 shipped 279 of them where the per-face light data used to be, which is how every multiblock
     * lamp in the pack went dark without one error line. Both halves of that mistake are the tripwire.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, batch = "emissive_face_keys")
    public static void emissiveFacesKeepTheirLightKeys(GameTestHelper helper) {
        java.util.Map<String, String> models;
        try {
            models = shippedModelJsons();
        } catch (java.io.IOException failure) {
            helper.fail("could not enumerate our model jsons: " + failure);
            return;
        }
        // A scan that quietly sees nothing reads exactly like a clean pack, and that is the failure mode
        // this guards, so the checker is pointed at planted copies first: one carrying the dead key, and
        // one carrying a live light override in the shape that first fooled this scan -- a composite
        // model whose "children" is a map of names rather than an array.
        var plantedDead = countEmissiveKeys(java.util.Map.of("control.json",
                "{\"elements\":[{\"name\":\"light\",\"faces\":{\"north\":"
                        + "{\"texture\":\"#light\",\"shade\":false}}}]}"));
        if (plantedDead.faceShade() != 1 || plantedDead.faceLightData() != 0) {
            helper.fail("the planted dead-key model read back as faceShade=" + plantedDead.faceShade()
                    + " and faceLightData=" + plantedDead.faceLightData() + ", expected 1 and 0, so this"
                    + " scan cannot see the thing it is supposed to catch");
            return;
        }
        var plantedLit = countEmissiveKeys(java.util.Map.of("composite.json",
                "{\"children\":{\"coolant\":{\"elements\":[{\"name\":\"light\",\"faces\":{\"north\":"
                        + "{\"texture\":\"#light\",\"neoforge_data\":{\"block_light\":15,\"sky_light\":15}}}}]}}}"));
        if (plantedLit.faceLightData() != 1 || plantedLit.faceShade() != 0) {
            helper.fail("a lit face nested under a map-shaped children read as faceLightData="
                    + plantedLit.faceLightData() + ", expected 1, so the floor below is measured blind");
            return;
        }
        // The fallback pack is scored on its own line, because what it carries is a decision we make: when
        // the drive-mounted cell family came out of the pack on 2026-10-23, a combined floor went red by 65
        // while no live face had lost anything.
        var live = new java.util.LinkedHashMap<String, String>();
        var fallback = new java.util.LinkedHashMap<String, String>();
        for (var modelEntry : models.entrySet()) {
            (modelEntry.getKey().startsWith("legacy_art/") ? fallback : live)
                    .put(modelEntry.getKey(), modelEntry.getValue());
        }
        var liveScan = countEmissiveKeys(live);
        var packScan = countEmissiveKeys(fallback);
        if (liveScan.models() < 300 || packScan.models() < 150) {
            helper.fail("the scan found " + liveScan.models() + " live and " + packScan.models()
                    + " fallback-pack model jsons, expected at least 300 and 150, so one half of this"
                    + " guard checked nothing");
            return;
        }
        var dead = new java.util.TreeSet<String>(liveScan.deadShadeFiles());
        dead.addAll(packScan.deadShadeFiles());
        if (!dead.isEmpty()) {
            helper.fail((liveScan.faceShade() + packScan.faceShade()) + " face-level \"shade\" key(s) in "
                    + dead + " -- NeoForge's face deserializer has no such key, so those quads carry no light"
                    + " at all and the lamp is dark");
            return;
        }
        if (liveScan.faceLightData() < FACE_LIGHT_DATA_FLOOR) {
            helper.fail("only " + liveScan.faceLightData() + " face-level neoforge_data keys in live assets,"
                    + " floor is " + FACE_LIGHT_DATA_FLOOR + " (the fallback pack adds "
                    + packScan.faceLightData() + ") -- an emissive face lost its light data again");
            return;
        }
        // Not asserted: a light-named face with no override is a look decision, not a dead key. 1.3.5's
        // own base and off models have ten of them, and the fallback pack has four.
        NeoECOPrototype.LOGGER.info("emissive keys: {} live models / {} live face-level neoforge_data"
                        + " (floor {}), {} fallback models / {} keys; {} light-named live faces carry no"
                        + " override",
                liveScan.models(), liveScan.faceLightData(), FACE_LIGHT_DATA_FLOOR,
                packScan.models(), packScan.faceLightData(), liveScan.lightFacesWithoutData());
        helper.succeed();
    }

    /** Live assets alone, measured 2026-10-23: 325 lit faces across 337 models. The floor deliberately
     *  ignores the fallback pack - that pack's contents change by decision, and its drive-cell family
     *  leaving it took 65 keys out of a combined count without touching live art. A floor only blocks a
     *  decrease, so adding lit faces stays free. */
    private static final int FACE_LIGHT_DATA_FLOOR = 325;

    private record EmissiveScan(int models, int faceShade, int faceLightData, int lightFacesWithoutData,
                                java.util.SortedSet<String> deadShadeFiles) { }

    private static EmissiveScan countEmissiveKeys(java.util.Map<String, String> models) {
        // [0] face-level shade, [1] face-level neoforge_data, [2] light-named faces with neither.
        var counts = new int[3];
        var dead = new java.util.TreeSet<String>();
        for (var entry : models.entrySet()) {
            com.google.gson.JsonElement root;
            try {
                root = com.google.gson.JsonParser.parseString(entry.getValue());
            } catch (com.google.gson.JsonParseException failure) {
                continue;   // an unparsable model is somebody else's guard's problem
            }
            // A composite model nests whole child models under "children", which NeoForge accepts as
            // either an array or a map of names -- the fallback art pack uses the map form, and reading
            // only the array form is how this scan first went red on a pack that was actually clean.
            countEmissiveKeys(root, entry.getKey(), "", counts, dead);
        }
        return new EmissiveScan(models.size(), counts[0], counts[1], counts[2], dead);
    }

    private static void countEmissiveKeys(com.google.gson.JsonElement node, String file, String elementName,
                                          int[] counts, java.util.SortedSet<String> dead) {
        if (node == null || node.isJsonNull()) return;
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(child -> countEmissiveKeys(child, file, elementName, counts, dead));
            return;
        }
        if (!node.isJsonObject()) return;
        var object = node.getAsJsonObject();
        var name = object.has("name") && object.get("name").isJsonPrimitive()
                ? object.get("name").getAsString() : elementName;
        if (object.has("faces") && object.get("faces").isJsonObject()) {
            for (var faceEntry : object.getAsJsonObject("faces").entrySet()) {
                var value = faceEntry.getValue();
                if (!value.isJsonObject()) continue;
                var face = value.getAsJsonObject();
                boolean lit = face.has("neoforge_data");
                if (face.has("shade")) {
                    counts[0]++;
                    dead.add(file + " (" + name + "." + faceEntry.getKey() + ")");
                }
                if (lit) counts[1]++;
                var texture = face.has("texture") ? face.get("texture").getAsString() : "";
                if ((name + " " + texture).toLowerCase(java.util.Locale.ROOT).contains("light") && !lit) {
                    counts[2]++;
                }
            }
        }
        for (var member : object.entrySet()) {
            if (member.getKey().equals("faces")) continue;
            if (member.getKey().equals("elements")) {
                // An element's own "shade" is the one NeoForge reads, so it is not a violation and must
                // not be counted; only its faces are.
                if (member.getValue().isJsonArray()) {
                    member.getValue().getAsJsonArray().forEach(
                            element -> countEmissiveKeys(element, file, name, counts, dead));
                }
                continue;
            }
            countEmissiveKeys(member.getValue(), file, name, counts, dead);
        }
    }

    /**
     * The stripe each of our in-drive cell models is meant to read off its type texture, as the north face's
     * uv. Two textures carry stripes. {@code drive/cell_type} is eight media colours two pixels wide, laid
     * out in columns (chemical 0, fluid 2, item 4, air 6, energy and fe sharing 8, lightning 10, bulk 12,
     * omni 14), and one model reads it now: the plain small bulk one, which the scripted matrices borrow.
     * {@code drive/cell_type_lower} is the same media as bands six pixels wide stacked by row (chemical y0,
     * fluid y3, item y6, and a red band at y9 drawn for the FE family, which is the only thing that reads it);
     * everything else reads that one.
     * The art batch of 2026-10-19 moved our own chemical / fluid / item / default / custom models onto the
     * row sheet and turned their lamp from a vertical 2x6 strip into a horizontal 6x3 bar, so a stripe in
     * this table is only meaningful together with the geometry shipped beside it.
     *
     * <p>The one that was wrong: both FE matrices were registered on the item cells' models and so read the
     * item colour, on the recorded belief that our copy of {@code cell_type} carried no energy stripe. It
     * does -- the file is eco's byte for byte, and eco's own fe models differ from its item models by
     * nothing but the four numbers in this stripe's offset.
     *
     * <p>This guards the model files. The {@code ECOCellModels.register} lines that point a cell item at a
     * model are client setup, and a game test server is a dedicated server that never runs it, so a wrong
     * registration stays an eye check.
     */
    private static final java.util.Map<String, String> CELL_TYPE_STRIPE = java.util.Map.ofEntries(
            java.util.Map.entry("storage_cell_l1_chemical", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l1r_chemical", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l1_fluid", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l1r_fluid", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l1_item", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1r_item", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1_default", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1_custom", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1_small_bulk", "4,0,6,6"),
            java.util.Map.entry("storage_cell_l1_fe", "0,9,6,12"),
            java.util.Map.entry("storage_cell_l1r_fe", "0,9,6,12"),
            // All three FE matrices read the red band she added to cell_type_lower the day this landed - the
            // row was drawn for them. Their rungs are told apart by the level band alone, like every other
            // family on that sheet.
            java.util.Map.entry("storage_cell_l0_fe_1k", "0,9,6,12"),
            java.util.Map.entry("storage_cell_l0_chemical_1k", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l0_chemical_4k", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l0_chemical_16k", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l0_chemical_64k", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l0_fluid_1k", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l0_fluid_4k", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l0_fluid_16k", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l0_fluid_64k", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l0_item_1k", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l0_item_4k", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l0_item_16k", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l0_item_64k", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1_small_bulk_chemical", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l1r_small_bulk_chemical", "0,0,6,3"),
            java.util.Map.entry("storage_cell_l1_small_bulk_fluid", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l1r_small_bulk_fluid", "0,3,6,6"),
            java.util.Map.entry("storage_cell_l1_small_bulk_item", "0,6,6,9"),
            java.util.Map.entry("storage_cell_l1r_small_bulk_item", "0,6,6,9"));

    /** The cell models that deliberately draw no type lamp at all, only the level bands. */
    private static final List<String> CELL_MODELS_WITHOUT_TYPE =
            List.of("storage_cell_l1_concrete", "storage_cell_l1_pigmee", "storage_cell_l1_singularity",
                    // The omni families, taken by the artist batch of 2026-10-19: the type band came out and
                    // the housing plate carries the family colour instead. Asked upstream the same day, the
                    // answer was 靠外壳辨认 - read it off the shell.
                    "storage_cell_l1_universal", "storage_cell_l1r_universal",
                    "storage_cell_l1_quantum", "storage_cell_l1r_quantum",
                    "storage_cell_l1_complex_universal", "storage_cell_l1_spent_waste",
                    // Their 1K rungs: same family plate and the same missing lamp, only the level band
                    // moves down to the blue pair every other family's 1K reads.
                    "storage_cell_l0_universal", "storage_cell_l0_quantum",
                    "storage_cell_l0_complex_universal", "storage_cell_l0_spent_waste");

    /**
     * What one cell model's type band actually says: its north uv, the texture it binds, how many faces the
     * band has and how many of those carry per-face light.
     */
    private record CellTypeBand(String northUv, String texture, int faces, int litFaces) { }

    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, batch = "cell_type_stripe")
    public static void driveCellModelsReadTheirOwnTypeStripe(GameTestHelper helper) {
        // The reader is pointed at planted models first, both ways round, because a scan that finds no type
        // band reads exactly like a model that was built without one -- and thirteen of ours really were. The
        // planted copy also carries the textures map, because a face binds a key ("#type") and the sheet
        // lives in that map: reading the key instead of the resolved path is how the light check below
        // matched no model at all and still reported green.
        var planted = readCellTypeBand("{\"textures\":{\"type\":\"neoecoprototype:block/x/drive/cell_type\"},"
                + "\"elements\":[{\"name\":\"type\",\"faces\":{\"north\":{\"uv\":[8,0,10,6],"
                + "\"texture\":\"#type\",\"neoforge_data\":{\"block_light\":15}}}}]}");
        if (planted == null || !planted.northUv().equals("8,0,10,6")
                || !planted.texture().equals("neoecoprototype:block/x/drive/cell_type")
                || planted.faces() != 1 || planted.litFaces() != 1) {
            helper.fail("the planted type band read back as " + planted + ", expected northUv=8,0,10,6 on"
                    + " the resolved cell_type path with one lit face, so the census below is measured blind");
            return;
        }
        if (readCellTypeBand("{\"elements\":[{\"name\":\"level\",\"faces\":{\"north\":{\"uv\":[0,0,6,2],"
                + "\"texture\":\"#level\"}}}]}") != null) {
            helper.fail("a model with no type band read as if it had one, so \"no lamp\" and \"scan went"
                    + " blind\" cannot be told apart");
            return;
        }
        java.util.Map<String, String> models;
        try {
            models = shippedModelJsons();
        } catch (java.io.IOException failure) {
            helper.fail("could not enumerate our model jsons: " + failure);
            return;
        }
        var cellModels = new java.util.TreeMap<String, String>();
        for (var entry : models.entrySet()) {
            var path = entry.getKey();
            if (path.startsWith("assets/" + NeoECOPrototype.MOD_ID + "/models/block/cell/")) {
                cellModels.put(path.substring(path.lastIndexOf('/') + 1, path.length() - ".json".length()),
                        entry.getValue());
            }
        }
        if (cellModels.size() < CELL_TYPE_STRIPE.size()) {
            helper.fail("the scan found only " + cellModels.size() + " cell models, the census expects at"
                    + " least " + CELL_TYPE_STRIPE.size() + ", so it checked nothing");
            return;
        }
        // Both ways round: the loop below only reads the table for models it found, so a model file that
        // goes missing would take its expectation with it and leave the census green.
        var missingModels = new java.util.ArrayList<String>();
        for (var named : CELL_TYPE_STRIPE.keySet()) {
            if (!cellModels.containsKey(named)) {
                missingModels.add(named);
            }
        }
        for (var named : CELL_MODELS_WITHOUT_TYPE) {
            if (!cellModels.containsKey(named)) {
                missingModels.add(named);
            }
        }
        if (!missingModels.isEmpty()) {
            helper.fail("the census names models the scan never found, so their stripe is asserted"
                    + " against nothing: " + missingModels);
            return;
        }
        var strays = new java.util.ArrayList<String>();
        var unlit = new java.util.ArrayList<String>();
        int lowerUnlitFaces = 0;
        int sharedSheetFaces = 0;
        for (var entry : cellModels.entrySet()) {
            var name = entry.getKey();
            var band = readCellTypeBand(entry.getValue());
            var expected = CELL_TYPE_STRIPE.get(name);
            if (expected == null) {
                if (!CELL_MODELS_WITHOUT_TYPE.contains(name)) {
                    strays.add(name + " -> " + (band == null ? "no type band" : band.northUv()));
                } else if (band != null) {
                    helper.fail(name + " is one of the models that should carry no type lamp, but it"
                            + " draws a band at uv " + band.northUv());
                    return;
                }
                continue;
            }
            if (band == null) {
                helper.fail(name + " lost its type band entirely");
                return;
            }
            if (!band.northUv().equals(expected)) {
                helper.fail(name + " reads type uv " + band.northUv() + ", expected " + expected
                        + " -- one media's colour is standing in another family's lamp");
                return;
            }
            // Every model that still reads the shared cell_type sheet has to light its own lamp: the plain
            // small bulk one does, and eco's cell models leave just two of their type faces dark, so a dark
            // one here reads as a lost key rather than a look. The row-sheet group is left out -- none of its
            // type faces carry light at all. That is her look for the whole family, and the FE matrices moved
            // onto that sheet with it, so it is a thing to raise rather than this guard's to decide.
            if (band.texture().endsWith("cell_type")) {
                sharedSheetFaces += band.faces();
                if (band.litFaces() != band.faces()) {
                    unlit.add(name + " (" + band.litFaces() + "/" + band.faces() + ")");
                }
            } else if (band.texture().endsWith("cell_type_lower")) {
                lowerUnlitFaces += band.faces() - band.litFaces();
            }
        }
        if (!strays.isEmpty()) {
            helper.fail("cell models outside the census, add each one's stripe or say it draws no lamp: "
                    + strays);
            return;
        }
        // The 4M rung is carried by a sheet of its own, not by a colour: cell_level_4m and her
        // cell_level_lower_4m are both 16x32 with two frames whose only content is a green-to-blue slide, so a
        // 4M model with no band on one of them shows a still lamp and stops saying 4M. The band that pulses is
        // not always the wide one - the expanded small bulk models keep their static purple family band and
        // pulse only the short one, through a second "level_4m" key - so this reads what each face binds and
        // resolves it through the model's own textures map.
        var noPulse = new java.util.ArrayList<String>();
        for (var entry : cellModels.entrySet()) {
            if (!entry.getKey().contains("_l1r_")) {
                continue;
            }
            var root = com.google.gson.JsonParser.parseString(entry.getValue()).getAsJsonObject();
            var sheets = new java.util.HashMap<String, String>();
            root.getAsJsonObject("textures").entrySet().forEach(binding -> {
                if (binding.getValue().isJsonPrimitive()) {
                    sheets.put(binding.getKey(), binding.getValue().getAsString());
                }
            });
            var pulses = new java.util.ArrayList<String>();
            root.getAsJsonArray("elements").forEach(element -> {
                var faces = element.getAsJsonObject().getAsJsonObject("faces");
                if (faces == null) {
                    return;
                }
                faces.entrySet().forEach(side -> {
                    var bound = side.getValue().getAsJsonObject().get("texture");
                    var sheet = bound == null ? ""
                            : sheets.getOrDefault(bound.getAsString().substring(1), "");
                    if (sheet.endsWith("_4m")) {
                        pulses.add(bound.getAsString());
                    }
                });
            });
            if (pulses.isEmpty()) {
                noPulse.add(entry.getKey());
            }
        }
        if (!noPulse.isEmpty()) {
            helper.fail("4M cell models with no band on an animated level sheet, so they never pulse:"
                    + noPulse);
            return;
        }
        if (sharedSheetFaces == 0) {
            helper.fail("no cell model resolved onto the shared cell_type sheet, so the lit-face check"
                    + " below it had nothing to look at");
            return;
        }
        if (!unlit.isEmpty()) {
            helper.fail("type faces on cell_type without per-face light: " + unlit);
            return;
        }
        NeoECOPrototype.LOGGER.info("cell type stripes: {} models censused, {} of them draw no type lamp,"
                        + " {} type faces on cell_type all carry light, {} unlit type faces on"
                        + " cell_type_lower are logged but not asserted",
                cellModels.size(), CELL_MODELS_WITHOUT_TYPE.size(), sharedSheetFaces, lowerUnlitFaces);
        helper.succeed();
    }

    /** The first type-named element's faces, or null when the model draws no type band. */
    private static CellTypeBand readCellTypeBand(String modelJson) {
        com.google.gson.JsonElement root;
        try {
            root = com.google.gson.JsonParser.parseString(modelJson);
        } catch (com.google.gson.JsonParseException failure) {
            return null;
        }
        var found = new java.util.ArrayList<com.google.gson.JsonObject>();
        collectTypeFaces(root, found);
        if (found.isEmpty()) {
            return null;
        }
        var faces = found.get(0).getAsJsonObject("faces");
        var sheets = new java.util.HashMap<String, String>();
        if (root.isJsonObject() && root.getAsJsonObject().has("textures")
                && root.getAsJsonObject().get("textures").isJsonObject()) {
            root.getAsJsonObject().getAsJsonObject("textures").entrySet().forEach(binding -> {
                if (binding.getValue().isJsonPrimitive()) {
                    sheets.put(binding.getKey(), binding.getValue().getAsString());
                }
            });
        }
        String northUv = null;
        String texture = "";
        int count = 0;
        int lit = 0;
        for (var entry : faces.entrySet()) {
            if (!entry.getValue().isJsonObject()) {
                continue;
            }
            var face = entry.getValue().getAsJsonObject();
            count++;
            if (face.has("neoforge_data")) {
                lit++;
            }
            if (entry.getKey().equals("north")) {
                var uv = face.has("uv") ? face.getAsJsonArray("uv") : new com.google.gson.JsonArray();
                var parts = new java.util.ArrayList<String>();
                uv.forEach(value -> parts.add(String.valueOf(value.getAsInt())));
                northUv = String.join(",", parts);
                var binding = face.has("texture")
                        ? face.get("texture").getAsString().replaceFirst("^#", "") : "";
                texture = sheets.getOrDefault(binding, binding);
            }
        }
        return new CellTypeBand(northUv == null ? "" : northUv, texture, count, lit);
    }

    private static void collectTypeFaces(com.google.gson.JsonElement node,
                                         java.util.List<com.google.gson.JsonObject> found) {
        if (node == null || node.isJsonNull()) {
            return;
        }
        if (node.isJsonArray()) {
            node.getAsJsonArray().forEach(child -> collectTypeFaces(child, found));
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        var object = node.getAsJsonObject();
        if (object.has("name") && object.get("name").isJsonPrimitive()
                && object.get("name").getAsString().equals("type")
                && object.has("faces") && object.get("faces").isJsonObject()
                && !object.getAsJsonObject("faces").isEmpty()) {
            found.add(object);
            return;
        }
        object.entrySet().forEach(member -> collectTypeFaces(member.getValue(), found));
    }

    /** The five migration drives: the three paid rungs, the drive that comes back, and the creative one. */
    private static final List<String> MIGRATION_DRIVES = List.of(
            "migration_drive_1_4", "migration_drive_4_6", "migration_drive_6_9",
            "migration_drive_empty", "migration_drive_creative");

    /**
     * Every drive that is registered has to have its own model, its own texture behind that model's layer0,
     * and a name in both lang files. All five are in the creative tab and none has a recipe yet, which is the
     * point of parking them - but a parked item with no artwork is worse than an absent one, because the
     * player who gets one by command holds a purple-black cube with the raw key over it.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, batch = "migration_drive")
    public static void migrationDrivesShipArtAndNames(GameTestHelper helper) {
        // The reader is planted first: a model naming a texture we do not ship must come back as that
        // texture and then as missing, or the loop below is green only because it cannot see anything.
        var planted = firstLayerTexture("{\"textures\":{\"layer0\":\"neoecoprototype:item/not_shipped\"}}");
        if (!"neoecoprototype:item/not_shipped".equals(planted)) {
            helper.fail("the planted model read its layer0 as " + planted
                    + ", so this guard cannot see a texture at all");
            return;
        }
        if (readShippedResource(ResourceLocation.fromNamespaceAndPath("neoecoprototype",
                "textures/item/not_shipped.png")) != null) {
            helper.fail("the planted texture path resolved to a shipped file, so the lookup is not the one"
                    + " the loop uses");
            return;
        }
        for (var path : MIGRATION_DRIVES) {
            var id = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path);
            if (!BuiltInRegistries.ITEM.containsKey(id)) {
                helper.fail(id + " is listed in the guard but never registered");
                return;
            }
            var model = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                    "models/item/" + path + ".json");
            String modelJson = readShippedResource(model);
            if (modelJson == null) {
                helper.fail(path + " has no item model at " + model
                        + ", so it renders as a missing texture");
                return;
            }
            var texture = firstLayerTexture(modelJson);
            if (texture == null) {
                helper.fail(model + " declares no layer0, so there is nothing for the model to draw");
                return;
            }
            var parsed = ResourceLocation.tryParse(texture);
            if (parsed == null || readShippedResource(ResourceLocation.fromNamespaceAndPath(
                    parsed.getNamespace(), "textures/" + parsed.getPath() + ".png")) == null) {
                helper.fail(path + " binds layer0 " + texture + ", which is not shipped");
                return;
            }
            for (String lang : new String[]{"en_us", "zh_cn"}) {
                String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
                if (json == null) {
                    return;
                }
                com.google.gson.JsonObject keys;
                try {
                    keys = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
                } catch (com.google.gson.JsonParseException | IllegalStateException failure) {
                    helper.fail("lang/" + lang + ".json did not parse: " + failure);
                    return;
                }
                var key = "item.neoecoprototype." + path;
                if (!keys.has(key) || keys.get(key).getAsString().isBlank()) {
                    helper.fail("lang/" + lang + ".json has no " + key
                            + ", so the item shows the raw key in the hand");
                    return;
                }
            }
        }
        // The name colours are a fact this guard can actually read: four rungs, four distinct colours, and
        // the spent shell stays plain so it cannot be mistaken for a usable disk.
        var rungPaths = List.of("migration_drive_1_4", "migration_drive_4_6", "migration_drive_6_9",
                "migration_drive_creative");
        var seenColors = new java.util.LinkedHashMap<String, Integer>();
        for (var path : rungPaths) {
            var stack = new ItemStack(BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path)));
            if (stack.has(net.minecraft.core.component.DataComponents.CUSTOM_NAME)) {
                helper.fail(path + "'s colour sits on CUSTOM_NAME, so every stack reads as renamed and"
                        + " renders italic");
                return;
            }
            var color = stack.getHoverName().getStyle().getColor();
            if (color == null) {
                helper.fail(path + " has no name colour, so the ladder cannot be read in an inventory");
                return;
            }
            int argb = color.getValue();
            if (seenColors.containsValue(argb)) {
                helper.fail(path + " shares name colour " + Integer.toHexString(argb)
                        + " with another rung");
                return;
            }
            seenColors.put(path, argb);
        }
        var spentShell = new ItemStack(ModRegistration.MIGRATION_DRIVE_EMPTY.get());
        if (spentShell.has(net.minecraft.core.component.DataComponents.ITEM_NAME)) {
            helper.fail("the spent shell carries a coloured name, so it reads as another usable disk");
            return;
        }
        NeoECOPrototype.LOGGER.info("migration drives: {} registered, each with its own model, layer0"
                + " texture and name in both languages; rung name colours {}", MIGRATION_DRIVES.size(),
                seenColors.values().stream().map(c -> Integer.toHexString(c)).toList());
        helper.succeed();
    }

    /**
     * All five migration disks are reachable from the creative tab. The tab is read through the generator the
     * client itself uses, with a known member and a known outsider checked first, so a blind or empty read
     * cannot come back green.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, batch = "migration_drive")
    public static void migrationDrivesAreInCreativeTab(GameTestHelper helper) {
        var tab = ModRegistration.SIMPLIFY_TAB.get();
        tab.buildContents(new net.minecraft.world.item.CreativeModeTab.ItemDisplayParameters(
                net.minecraft.world.flag.FeatureFlags.DEFAULT_FLAGS, true, helper.getLevel().registryAccess()));
        var contents = tab.getDisplayItems();
        if (contents.isEmpty()) {
            helper.fail("the tab generator returned no stacks at all, so membership proves nothing");
            return;
        }
        java.util.function.Predicate<Item> inTab =
                item -> contents.stream().anyMatch(stack -> stack.getItem() == item);
        if (!inTab.test(ModRegistration.SIMPLIFY_DRIVE_ITEM.get())) {
            helper.fail("the drive is missing from the tab, so this reader cannot see membership at all");
            return;
        }
        if (inTab.test(ModRegistration.FLAWLESS_BUDDING_GRETTE_ITEM.get())) {
            helper.fail("the outsider control is inside the tab, so the membership test is not the tab");
            return;
        }
        for (String path : MIGRATION_DRIVES) {
            var item = BuiltInRegistries.ITEM.get(
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path));
            if (!inTab.test(item)) {
                helper.fail(path + " is not in the creative tab (" + contents.size() + " stacks scanned)");
                return;
            }
        }
        helper.succeed();
    }

    /** The layer0 binding of an item model, or null when it declares none. */
    private static String firstLayerTexture(String modelJson) {
        try {
            var root = com.google.gson.JsonParser.parseString(modelJson);
            if (!root.isJsonObject()) {
                return null;
            }
            var textures = root.getAsJsonObject().get("textures");
            if (textures == null || !textures.isJsonObject()) {
                return null;
            }
            var layer0 = textures.getAsJsonObject().get("layer0");
            return layer0 == null || !layer0.isJsonPrimitive() ? null : layer0.getAsString();
        } catch (com.google.gson.JsonParseException | IllegalStateException failure) {
            return null;
        }
    }

    /**
     * Every model json of both packs that ship inside our jar. Read off the classpath because a dedicated
     * server does not have to index {@code assets/}, and the fallback art pack is not under the namespace
     * the resource manager would look at anyway.
     */
    private static java.util.Map<String, String> shippedModelJsons() throws java.io.IOException {
        var found = new java.util.LinkedHashMap<String, String>();
        var prefixes = List.of("assets/" + NeoECOPrototype.MOD_ID + "/models/",
                "legacy_art/assets/" + NeoECOPrototype.MOD_ID + "/models/");
        for (var entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            java.nio.file.Path root = java.nio.file.Path.of(entry).toAbsolutePath();
            for (var prefix : prefixes) {
                java.nio.file.Path dir = root.resolve(prefix);
                if (java.nio.file.Files.isDirectory(dir)) {
                    try (var walk = java.nio.file.Files.walk(dir)) {
                        for (var path : (Iterable<java.nio.file.Path>) walk
                                .filter(p -> p.toString().endsWith(".json"))::iterator) {
                            found.put(prefix + dir.relativize(path).toString()
                                    .replace(java.io.File.separator, "/"),
                                    java.nio.file.Files.readString(path));
                        }
                    }
                } else if (entry.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")
                        && java.nio.file.Files.isRegularFile(root)) {
                    try (var zip = new java.util.zip.ZipFile(root.toFile())) {
                        for (var file : java.util.Collections.list(zip.entries())) {
                            if (!file.getName().startsWith(prefix) || file.isDirectory()
                                    || !file.getName().endsWith(".json") || found.containsKey(file.getName())) {
                                continue;
                            }
                            try (var in = zip.getInputStream(file)) {
                                found.put(file.getName(), new String(in.readAllBytes(),
                                        java.nio.charset.StandardCharsets.UTF_8));
                            }
                        }
                    }
                }
            }
        }
        return found;
    }

    /**
     * Every {@code assets/minecraft/atlases/blocks.json} on the classpath, concatenated. The game reads all
     * of them and merges their sources, and vanilla's own copy sits at that same path, so a single
     * classloader stream would return whichever one it happened to see first.
     */
    private static String blockAtlasMergeText() throws java.io.IOException {
        var text = new StringBuilder();
        String archivePath = "assets/minecraft/atlases/blocks.json";
        for (var entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            java.nio.file.Path root = java.nio.file.Path.of(entry).toAbsolutePath();
            java.nio.file.Path plain = root.resolve(archivePath);
            if (java.nio.file.Files.isRegularFile(plain)) {
                text.append(java.nio.file.Files.readString(plain));
            } else if (entry.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")
                    && java.nio.file.Files.isRegularFile(root)) {
                try (var zip = new java.util.zip.ZipFile(root.toFile())) {
                    var found = zip.getEntry(archivePath);
                    if (found != null) {
                        try (var in = zip.getInputStream(found)) {
                            text.append(new String(in.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                        }
                    }
                }
            }
        }
        return text.toString();
    }

    /** PNG width and height live in the IHDR chunk, at fixed offsets, in big-endian. */
    private static void rememberSize(String name, byte[] header, java.util.Map<String, int[]> sizes) {
        if (header.length < 24 || header[1] != 'P' || header[2] != 'N' || header[3] != 'G') return;
        sizes.put(name, new int[]{
                (header[16] & 0xFF) << 24 | (header[17] & 0xFF) << 16
                        | (header[18] & 0xFF) << 8 | header[19] & 0xFF,
                (header[20] & 0xFF) << 24 | (header[21] & 0xFF) << 16
                        | (header[22] & 0xFF) << 8 | header[23] & 0xFF});
    }

    /**
     * The mixin list is the one registry a compile error cannot police. Delete a mixin class and leave
     * its name in {@code neoecoprototype.mixins.json} and Sponge only logs a load-time failure that is
     * easy to scroll past; delete the JSON entry and leave the class, and the behaviour that mixin
     * pinned quietly comes back. Both directions are checked against the class files actually present,
     * so tightening the list from 16 entries to 7 cannot half-land. Enumerating class files never loads
     * them, which keeps the two client mixins out of this server JVM.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void mixinListMatchesShippedMixinClasses(GameTestHelper helper) {
        String json = readResource("/neoecoprototype.mixins.json", helper);
        if (json == null) return;
        var root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
        String packageName = root.get("package").getAsString();
        var registered = new java.util.TreeSet<String>();
        int declaredEntries = 0;
        for (String field : List.of("mixins", "client")) {
            var array = root.getAsJsonArray(field);
            if (array == null) continue;
            for (var entry : array) {
                registered.add(entry.getAsString());
                declaredEntries++;
            }
        }
        java.util.SortedSet<String> present;
        try {
            present = classNamesInPackage(packageName);
        } catch (Exception failure) {
            helper.fail("could not enumerate " + packageName + " on the classpath: " + failure);
            return;
        }
        if (present.isEmpty()) {
            helper.fail("found no class files for " + packageName + ", so this guard checked nothing; classpath="
                    + System.getProperty("java.class.path").substring(0, Math.min(200,
                            System.getProperty("java.class.path").length())));
            return;
        }
        var classesNotRegistered = new java.util.TreeSet<String>(present);
        classesNotRegistered.removeAll(registered);
        var registeredWithoutClass = new java.util.TreeSet<String>(registered);
        registeredWithoutClass.removeAll(present);
        if (!classesNotRegistered.isEmpty() || !registeredWithoutClass.isEmpty()
                || declaredEntries != registered.size()) {
            helper.fail("mixin list out of sync: json declares " + declaredEntries + " entries ("
                    + registered.size() + " distinct) but " + packageName + " holds " + present.size()
                    + " classes; not registered=" + classesNotRegistered
                    + ", no such class=" + registeredWithoutClass
                    + ", duplicated=" + (declaredEntries - registered.size()));
            return;
        }
        helper.succeed();
    }

    /**
     * Top-level class names present under {@code packageName}, named the way {@code mixins.json} names
     * them: relative to {@code package}, dots for separators. Nested and synthetic classes are skipped,
     * since Sponge is only ever given top-level ones. The JVM's own classpath is walked rather than
     * {@code getClassLoader().getResources()}, because the dev launch hands our classes to NeoForge's
     * mod folder loader and that loader returns nothing for the directory even though
     * {@code build/classes/java/main} sits on {@code -cp}; a guard that silently sees zero classes is
     * worse than no guard at all.
     */
    private static java.util.SortedSet<String> classNamesInPackage(String packageName)
            throws java.io.IOException {
        String packagePath = packageName.replace('.', '/') + "/";
        var names = new java.util.TreeSet<String>();
        for (String entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            java.nio.file.Path path = java.nio.file.Path.of(entry).toAbsolutePath();
            if (java.nio.file.Files.isDirectory(path)) {
                collectFromDirectory(names, packagePath, path);
            } else if (entry.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")) {
                collectFromJar(names, packagePath, path);
            }
        }
        return names;
    }

    private static void collectFromDirectory(java.util.SortedSet<String> names, String packagePath,
            java.nio.file.Path classRoot) throws java.io.IOException {
        var packageRoot = classRoot.resolve(packagePath);
        if (!java.nio.file.Files.isDirectory(packageRoot)) return;
        try (var paths = java.nio.file.Files.walk(packageRoot)) {
            paths.filter(java.nio.file.Files::isRegularFile).forEach(path -> addClassName(names,
                    packageRoot.relativize(path).toString().replace(java.io.File.separator, "/")));
        }
    }

    private static void collectFromJar(java.util.SortedSet<String> names, String packagePath,
            java.nio.file.Path jar) throws java.io.IOException {
        if (!java.nio.file.Files.isRegularFile(jar)) return;
        try (var zip = new java.util.zip.ZipFile(jar.toFile())) {
            for (var entry : java.util.Collections.list(zip.entries())) {
                if (entry.getName().startsWith(packagePath)) {
                    addClassName(names, entry.getName().substring(packagePath.length()));
                }
            }
        }
    }

    private static void addClassName(java.util.SortedSet<String> names, String relativeName) {
        if (!relativeName.endsWith(".class")) return;
        String className = relativeName.substring(0, relativeName.length() - ".class".length());
        if (className.indexOf('$') >= 0) return;
        names.add(className.replace('/', '.'));
    }

    /**
     * Reproduces the placement-order report: put one of our machines down first, then a glass cable
     * against it, and the cable never joins that machine's grid, while the reverse order connects. AE2
     * registers the node-host block capability for its own block entity types only, so an addon machine
     * that is missing from that list cannot be found by anything looking in from outside. AE2's own
     * interface is the control row: if it fails too, the probe is wrong rather than the registration.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 300)
    public static void cablePlacedAgainstAnExistingMachineConnects(GameTestHelper helper) {
        var probes = List.of(
                new CableProbe("ae2 interface (control)", AEBlocks.INTERFACE.block(), new BlockPos(1, 5, 4)),
                new CableProbe("powered ME interface",
                        ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get(), new BlockPos(3, 5, 4)),
                new CableProbe("superconductive interface",
                        ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get(), new BlockPos(5, 5, 4)),
                new CableProbe("pattern provider",
                        ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BLOCK.get(), new BlockPos(7, 5, 4)),
                new CableProbe("stonecutting assembler",
                        ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get(), new BlockPos(1, 5, 6)));

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        for (var probe : probes) {
            helper.setBlock(probe.machinePos, Blocks.AIR);
            helper.setBlock(probe.cablePos(), Blocks.AIR);
            helper.setBlock(probe.machinePos, probe.block);
            helper.assertTrue(placeCableAgainst(helper, player, probe.machinePos, Direction.EAST),
                    "could not place a glass cable against " + probe.label + " at "
                            + at(probe.machinePos));
        }

        waitUntil(helper, 25,
                () -> machineProbesWithoutConnection(helper, probes).isEmpty(),
                () -> "cable placed after the machine stayed disconnected for "
                        + machineProbesWithoutConnection(helper, probes),
                helper::succeed);
    }

    /**
     * The other order, and the control that keeps {@link
     * #cablePlacedAgainstAnExistingMachineConnects} honest: a machine that appears next to an existing
     * cable scans out for itself, so this direction worked even while the capability was missing. If
     * this one fails, nothing here can observe a connection and the other result means nothing.
     */
    @GameTest(template = "trinity_room", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 300)
    public static void machinePlacedAgainstAnExistingCableConnects(GameTestHelper helper) {
        var machinePos = new BlockPos(2, 5, 4);
        var cablePos = machinePos.east();
        helper.setBlock(machinePos, Blocks.AIR);
        helper.setBlock(cablePos, Blocks.AIR);
        // Clicking the north face of the block behind the cable slot puts the cable into that slot.
        helper.setBlock(cablePos.south(), Blocks.STONE);

        var player = helper.makeMockPlayer(GameType.CREATIVE);
        helper.assertTrue(placeCableAgainst(helper, player, cablePos.south(), Direction.NORTH),
                "could not place a glass cable at " + at(cablePos) + ", so this probe measures nothing");

        helper.setBlock(machinePos, ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get());
        waitUntil(helper, 25,
                () -> connectedOnSide(helper, machinePos, Direction.EAST)
                        && connectedOnSide(helper, cablePos, Direction.WEST),
                () -> "even the machine placed after the cable did not connect, so the probe cannot "
                        + "observe grid connections: cable placed first is in place but the powered "
                        + "ME interface at " + at(machinePos) + " has no connection on its east side",
                helper::succeed);
    }

    /**
     * How much one interface marker may stock is widened for every key type by the same factor: AE2
     * caps an item marker at one stack (64) and a fluid marker at 4000 mB, and chemicals copy the fluid
     * value. An earlier revision answered 8192 for everything, which quietly shrank a fluid marker from
     * 4 buckets worth of headroom down to 8 buckets total - so the factor, not the number, is the
     * behaviour worth pinning.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, timeoutTicks = 300)
    public static void superconductiveInterfaceWidensEveryMarkerAmount(GameTestHelper helper) {
        var pos = new BlockPos(0, 0, 0);
        helper.setBlock(pos, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof InterfaceLogicHost host)) {
            helper.fail("the superconductive interface block entity is not an interface logic host");
            return;
        }

        var config = host.getInterfaceLogic().getConfig();
        long iron = config.getMaxAmount(AEItemKey.of(Items.IRON_INGOT));
        long snowball = config.getMaxAmount(AEItemKey.of(Items.SNOWBALL));
        long water = config.getMaxAmount(AEFluidKey.of(Fluids.WATER));
        long raised = OversizeInterfaceLogic.MAX_AMOUNT;

        helper.assertTrue(iron == raised,
                "item markers should cap at " + raised + ", got " + iron);
        helper.assertTrue(snowball >= raised,
                "a 16-stackable item must not end up below the raised cap, got " + snowball);
        helper.assertTrue(water == 4000L * OversizeInterfaceLogic.WIDENING,
                "fluid markers should be AE2's 4000 mB times " + OversizeInterfaceLogic.WIDENING
                        + ", got " + water);

        // The power has to reach the grid through a service on the node, not just a method on the class.
        // AE2 only creates that node in onReady, so this has to be polled rather than read immediately.
        var interfaceHost = (IGridConnectedBlockEntity) helper.getBlockEntity(pos);
        waitUntil(helper, 10,
                () -> providesPassiveGenerator(interfaceHost),
                () -> "the superconductive interface node must offer its "
                        + SimplifySuperconductiveInterfaceBlockEntity.GENERATION_RATE
                        + " AE/t passive generator service",
                helper::succeed);
    }

    private static boolean providesPassiveGenerator(IGridConnectedBlockEntity host) {
        var node = host.getGridNode();
        var generator = node == null ? null : node.getService(IPassiveEnergyGenerator.class);
        return generator != null
                && generator.getRate() == SimplifySuperconductiveInterfaceBlockEntity.GENERATION_RATE;
    }

    /** Side length of the {@code l1_room} template: the smallest cube that holds a minimum L1 build. */
    private static final int L1_ROOM_SIZE = 17;

    /**
     * Builds a complete L1 computation subsystem from eco's own build plan, replacing the
     * {@code replaceNth}-th planned slot of {@code replaceThis} with {@code replaceWith}, then handing
     * the controller to {@code onBuilt} after it has had twenty ticks to rebuild.
     *
     * <p>{@code rotateFacing} turns the replacement a quarter turn -- what a player dropping a single
     * block in by hand does -- while {@code false} keeps the facing the column asks for.
     *
     * <p>Every caller needs its own {@code batch}: tests in one batch run concurrently, and the
     * framework can place two of these 14-block-wide structures seven blocks apart, which makes each
     * controller find the other one and both fail as "not unique".
     */
    private static void buildComputationStructure(GameTestHelper helper, BlockPos controllerPos,
            Block replaceThis, Block replaceWith, int replaceNth, boolean rotateFacing, int buildLength,
            java.util.function.Consumer<ECOComputationSystemBlockEntity> onBuilt) {
        buildComputationStructureKept(helper, controllerPos, replaceThis, replaceWith, replaceNth,
                rotateFacing, buildLength, (controller, finish) -> {
                    try {
                        onBuilt.accept(controller);
                    } finally {
                        finish.run();
                    }
                    helper.succeed();
                });
    }

    /**
     * As {@link #buildComputationStructure}, but hands the caller the teardown: {@code finish} clears the
     * machine and leaves the test running, so the caller decides when to end it. A caller that has to
     * watch more than one tick -- waiting for an ME network to take a node, for instance -- cannot do
     * that through the plain {@code onBuilt} shape, because that one clears the structure the moment the
     * callback returns. {@code helper.fail} throws, so every exit path has to call {@code finish} itself.
     *
     * <p>{@code replaceThis} has to name a block whose planned state carries {@code HORIZONTAL_FACING}:
     * the replacement copies that facing, so passing e.g. the computation casing throws
     * "Cannot get property facing ... in ECOMachineCasing" before the callback ever runs.
     */
    private static void buildComputationStructureKept(GameTestHelper helper, BlockPos controllerPos,
            Block replaceThis, Block replaceWith, int replaceNth, boolean rotateFacing, int buildLength,
            java.util.function.BiConsumer<ECOComputationSystemBlockEntity, Runnable> onBuilt) {
        helper.setBlock(controllerPos, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get());
        BlockPos absolute = helper.absolutePos(controllerPos);
        helper.runAfterDelay(5, () -> {
            if (!(helper.getLevel().getBlockEntity(absolute)
                    instanceof ECOComputationSystemBlockEntity controller)) {
                helper.fail("the L1 controller has no computation block entity at " + absolute);
                return;
            }
            var buildController = new MultiBlockBuildController(controller);
            // The minimum structure has a single threading core, so testing "not the first slot" needs
            // a line that is at least two long.
            var builder = helper.makeMockPlayer(GameType.CREATIVE);
            for (int i = 1; i < buildLength; i++) {
                buildController.increaseBuildLength(builder);
            }
            var plan = buildController.createLocalPreviewPlan();
            if (plan == null || plan.getAllBlocks().isEmpty()) {
                helper.fail("the L1 controller produced no build plan");
                return;
            }
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            List<BlockPos> built = new ArrayList<>();
            int seen = 0;
            for (var planned : plan.getAllBlocks()) {
                // Only the template volume is restored after a test, so anything written outside it
                // leaks into the neighbouring test room and takes its block entities with it.
                BlockPos rel = planned.worldPos().subtract(origin);
                if (rel.getX() < 0 || rel.getY() < 0 || rel.getZ() < 0 || rel.getX() >= L1_ROOM_SIZE
                        || rel.getY() >= L1_ROOM_SIZE || rel.getZ() >= L1_ROOM_SIZE) {
                    helper.fail("the L1 build plan leaves the " + L1_ROOM_SIZE + "^3 template at " + rel);
                    return;
                }
                var plannedState = planned.targetState();
                if (plannedState.getBlock() == replaceThis && seen++ == replaceNth) {
                    var required = plannedState.getValue(BlockStateProperties.HORIZONTAL_FACING);
                    var facing = rotateFacing
                            ? Direction.from2DDataValue((required.get2DDataValue() + 1) % 4) : required;
                    helper.getLevel().setBlockAndUpdate(planned.worldPos(),
                            replaceWith.defaultBlockState()
                                    .setValue(BlockStateProperties.HORIZONTAL_FACING, facing));
                } else {
                    helper.getLevel().setBlockAndUpdate(planned.worldPos(), plannedState);
                }
                built.add(planned.worldPos());
            }
            helper.assertTrue(seen > replaceNth,
                    "the build plan placed only " + seen + " of "
                            + BuiltInRegistries.BLOCK.getKey(replaceThis).getPath()
                            + ", so this test would prove nothing");
            controller.rebuildMultiblock();
            helper.runAfterDelay(20, () -> onBuilt.accept(controller, () -> {
                // Batches run one after another but the framework does not reliably restore one
                // batch before starting the next, and two of these structures seven blocks apart
                // make every controller "not unique". Clear our own blocks so the next test is
                // measuring its own build.
                for (BlockPos pos : built) {
                    helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
                helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
            }));
        });
    }

    /**
     * An assembler finishes by handing the product to whatever item handler faces it, and AE2 never
     * registers that handler for its own provider: it exposes the return inventory as
     * {@code AECapabilities.GENERIC_INTERNAL_INV} and a lowest-priority AE2 hook then wraps every
     * block answering that capability. The wrap is keyed on the block, so our provider -- which has
     * its own block entity type -- answers nothing and the product stays in the assembler. AE2's
     * provider is the control: if it exposes nothing either, the probe is wrong rather than ours.
     */
    @GameTest(template = "trinity_room", batch = "provider_eject",
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void assemblerCanPushItsProductIntoOurProvider(GameTestHelper helper) {
        var oursPos = new BlockPos(1, 5, 4);
        var controlPos = new BlockPos(3, 5, 4);
        helper.setBlock(oursPos, ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BLOCK.get());
        helper.setBlock(controlPos, AEBlocks.PATTERN_PROVIDER.block());

        var control = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities
                        .Capabilities.ItemHandler.BLOCK, helper.absolutePos(controlPos), Direction.UP);
        if (control == null) {
            helper.fail("AE2's own provider exposes no item handler either, so this test cannot speak "
                    + "about ours");
            return;
        }
        var ours = helper.getLevel().getCapability(net.neoforged.neoforge.capabilities
                        .Capabilities.ItemHandler.BLOCK, helper.absolutePos(oursPos), Direction.UP);
        if (ours == null) {
            helper.fail("our provider exposes no item handler, so an assembler standing next to it has "
                    + "nowhere to push its product and the craft stalls with the output inside it");
            return;
        }
        if (!ours.insertItem(0, new ItemStack(Items.IRON_INGOT), false).isEmpty()) {
            helper.fail("our provider refused the pushed product");
            return;
        }
        var logic = ((PatternProviderLogicHost) helper.getBlockEntity(oursPos)).getLogic();
        if (logic.getReturnInv().isEmpty()) {
            helper.fail("the pushed product never reached the provider's return inventory, so nothing "
                    + "will carry it back into the network");
            return;
        }
        helper.succeed();
    }

    /**
     * The energized core is sold as "exactly one, directly behind the host". The geometry enforces it by
     * allotting a single cell, so nothing has to be counted afterwards. These tests each build the
     * structure once: swapping members in and out of one standing structure walks into AE2 refusing to
     * re-initialise a grid node.
     */
    @GameTest(template = "l1_room", batch = "l1_core_behind", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreBehindTheHostAddsItsParallelism(GameTestHelper helper) {
        // The cell comes from the production rule, so the test cannot pass by agreeing with a wrong
        // reading of where the core is supposed to go.
        buildWithEnergizedCore(helper, absolute -> SimplifyComputationClusterCalculator.energizedCoreCell(
                absolute, helper.getLevel().getBlockState(absolute)), true, "the cell behind the host");
    }

    /**
     * The shell cell beside the host used to be the allotted one; it has to refuse the same block now,
     * which is what keeps "exactly one" honest and keeps the core out of eco's network-switch cell.
     */
    @GameTest(template = "l1_room", batch = "l1_core_beside", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreBesideTheHostIsRejected(GameTestHelper helper) {
        buildWithEnergizedCore(helper, absolute -> absolute.relative(
                        appeng.api.orientation.IOrientationStrategy
                                .get(helper.getLevel().getBlockState(absolute))
                                .getSide(helper.getLevel().getBlockState(absolute),
                                        appeng.api.orientation.RelativeSide.RIGHT)),
                false, "the shell cell beside the host");
    }

    /**
     * The column the host itself stands in is not one the shell walk visits, and AE2 grows the
     * cluster bounds over any member block entity, so a core above or below the host used to pass
     * every geometry check and still get adopted.
     */
    @GameTest(template = "l1_room", batch = "l1_core_above", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreAboveTheHostIsRejected(GameTestHelper helper) {
        buildWithEnergizedCoreInHostColumn(helper, Direction.UP);
    }

    /** The same column seen from underneath. */
    @GameTest(template = "l1_room", batch = "l1_core_below", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreBelowTheHostIsRejected(GameTestHelper helper) {
        buildWithEnergizedCoreInHostColumn(helper, Direction.DOWN);
    }

    /**
     * The host stands in a column the shell walk never visits, so its above and below cells are the
     * two places the geometry used to accept a second core by simply not looking.
     */
    private static void buildWithEnergizedCoreInHostColumn(GameTestHelper helper, Direction lift) {
        buildWithEnergizedCore(helper, absolute -> absolute.relative(lift), false,
                "the cell " + (lift == Direction.UP ? "above" : "below") + " the host");
    }

    /**
     * Builds the minimum L1 structure, then puts an energized core into the cell {@code cellOf} picks
     * out and reports whether the machine formed.
     */
    private static void buildWithEnergizedCore(GameTestHelper helper,
                                               java.util.function.Function<BlockPos, BlockPos> cellOf,
                                               boolean expectFormed, String where) {
        BlockPos controllerPos = new BlockPos(13, 3, 4);
        helper.setBlock(controllerPos, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get());
        BlockPos absolute = helper.absolutePos(controllerPos);
        helper.runAfterDelay(5, () -> {
            if (!(helper.getLevel().getBlockEntity(absolute)
                    instanceof ECOComputationSystemBlockEntity controller)) {
                helper.fail("the L1 controller has no computation block entity at " + absolute);
                return;
            }
            var plan = new MultiBlockBuildController(controller).createLocalPreviewPlan();
            if (plan == null || plan.getAllBlocks().isEmpty()) {
                helper.fail("the L1 controller produced no build plan");
                return;
            }
            BlockPos origin = helper.absolutePos(BlockPos.ZERO);
            List<BlockPos> built = new ArrayList<>();
            for (var planned : plan.getAllBlocks()) {
                // Only the template volume is restored after a test, so anything written outside it
                // leaks into the neighbouring test room and takes its block entities with it.
                BlockPos rel = planned.worldPos().subtract(origin);
                if (rel.getX() < 0 || rel.getY() < 0 || rel.getZ() < 0 || rel.getX() >= L1_ROOM_SIZE
                        || rel.getY() >= L1_ROOM_SIZE || rel.getZ() >= L1_ROOM_SIZE) {
                    helper.fail("the L1 build plan leaves the " + L1_ROOM_SIZE + "^3 template at " + rel);
                    return;
                }
                helper.getLevel().setBlockAndUpdate(planned.worldPos(), planned.targetState());
                built.add(planned.worldPos());
            }

            BlockPos cell = cellOf.apply(absolute);
            var stateHere = helper.getLevel().getBlockState(cell);
            // assertTrue does not stop execution: falling through after a recorded failure and then
            // calling succeed() leaves the batch waiting on a test that can no longer finish.
            if (!stateHere.is(ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get())) {
                helper.fail(where + " is " + stateHere.getBlock() + " at " + cell
                        + ", not a casing, so this test cannot speak about it");
                return;
            }

            helper.getLevel().setBlockAndUpdate(cell,
                    ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get().defaultBlockState());
            controller.rebuildMultiblock();
            helper.runAfterDelay(20, () -> {
                try {
                    if (expectFormed) {
                        var cluster = controller.getCluster();
                        if (!controller.isFormed() || cluster == null) {
                            helper.fail(where + " did not accept the energized core");
                            return;
                        }
                        int ours = (int) cluster.getParallelCores().stream()
                                .filter(core -> core.getTier() == SimplifyTier.L1_PARALLEL_SWITCH).count();
                        if (ours != 1) {
                            helper.fail(where + " rejected the energized core");
                            return;
                        }
                        // A formed addon component is the only moment its idle power is observable, so the
                        // power mixin is checked here rather than in a fixture of its own.
                        var poweredCore = cluster.getParallelCores().stream()
                                .filter(core -> core.getTier() == SimplifyTier.L1_PARALLEL_SWITCH)
                                .findFirst().orElseThrow();
                        var coreNode = poweredCore.getMainNode().getNode();
                        if (coreNode == null) {
                            helper.fail(where + "'s energized core has no grid node yet, so its idle"
                                    + " power cannot be read");
                            return;
                        }
                        double expectedIdle = cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile.L1
                                .baseComponentIdlePower();
                        if (Math.abs(coreNode.getIdlePowerUsage() - expectedIdle) > 1e-9) {
                            helper.fail(where + "'s energized core idles at " + coreNode.getIdlePowerUsage()
                                    + " AE/t, but the addon power mixin should have set " + expectedIdle);
                            return;
                        }
                        int plain = cluster.getParallelCores().size() - ours;
                        long expected = (long) plain * SimplifyTier.L1.getCPUAccelerators()
                                + SimplifyTier.L1_PARALLEL_SWITCH.getCPUAccelerators();
                        if (cluster.getCPUAccelerators() != expected) {
                            helper.fail(where + " should add "
                                    + SimplifyTier.L1_PARALLEL_SWITCH.getCPUAccelerators()
                                    + " co-processors on top of " + plain + " plain cores, but the "
                                    + "cluster reports " + cluster.getCPUAccelerators());
                            return;
                        }
                        // The core's formed artwork only earns its place if the block still draws itself.
                        // This assertion used to demand the opposite, on the theory that the host's formed
                        // model paints a quad on the shared plane -- but that model's quads land at z=1 and
                        // z=32 while the core's front face is at z=16, and eco ships this same cube shape
                        // as a formed core without hiding it.
                        var coreState = helper.getLevel().getBlockState(cell);
                        if (coreState.getRenderShape()
                                == net.minecraft.world.level.block.RenderShape.INVISIBLE) {
                            helper.fail(where + " hides the energized core once formed, so the formed model "
                                    + "its blockstate offers can never be drawn: " + coreState);
                            return;
                        }
                        if (coreState.hasProperty(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED)
                                && !coreState.getValue(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED)) {
                            helper.fail(where + " holds the core but the core itself never took formed=true,"
                                    + " so it is showing its unformed grey model: " + coreState);
                            return;
                        }
                        // The host has to publish the same fact in its own block state: that is the only
                        // handle the formed face has for choosing the energized sheets.
                        var hostState = helper.getLevel().getBlockState(absolute);
                        if (!hostState.hasProperty(
                                SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE)
                                || !hostState.getValue(SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE)) {
                            helper.fail(where + " holds the energized core, but the host published "
                                    + "energized_parallel_core=false, so the formed face still draws the "
                                    + "plain sheets: " + hostState);
                            return;
                        }
                    } else if (controller.isFormed()) {
                        helper.fail(where + " took the energized core too, so nothing limits the "
                                + "machine to one");
                        return;
                    } else if (helper.getLevel().getBlockState(absolute)
                            .getValue(SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE)) {
                        helper.fail(where + " was refused, yet the host still claims an energized core: "
                                + helper.getLevel().getBlockState(absolute));
                        return;
                    }
                } finally {
                    for (BlockPos pos : built) {
                        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
                }
                helper.succeed();
            });
        });
    }

    /**
     * The column the controller itself stands in is the only one the geometry walks never look at, and
     * the cluster adopts anything inside its bounds, so a member parked above or below the host joined
     * the machine from a cell no build plan can produce. This builds the minimum machine, swaps exactly
     * one casing out of that column, and requires the machine to refuse - the rest of the structure is
     * the same plan the forming tests already pass.
     */
    private static void memberInHostColumnIsRejected(GameTestHelper helper, Block hostBlock,
                                                     BlockPos controllerPos, Direction lift,
                                                     Block casingBlock, Block member) {
        helper.setBlock(controllerPos, hostBlock);
        BlockPos absolute = helper.absolutePos(controllerPos);
        helper.runAfterDelay(5, () -> {
            if (!(helper.getLevel().getBlockEntity(absolute) instanceof MultiBlockBuildController.Host host)) {
                helper.fail(name(hostBlock) + " has no one-click builder at " + absolute);
                return;
            }
            host.setSelectedBuildLength(host.getMinBuildLength());
            var plan = new MultiBlockBuildController(host).createLocalPreviewPlan();
            if (plan == null || plan.getAllBlocks().isEmpty()) {
                helper.fail("the builder produced no placement plan for " + name(hostBlock));
                return;
            }
            List<BlockPos> built = new ArrayList<>();
            for (var planned : plan.getAllBlocks()) {
                BlockPos rel = planned.worldPos().subtract(helper.absolutePos(BlockPos.ZERO));
                if (rel.getX() < 0 || rel.getY() < 0 || rel.getZ() < 0 || rel.getX() >= L1_ROOM_SIZE
                        || rel.getY() >= L1_ROOM_SIZE || rel.getZ() >= L1_ROOM_SIZE) {
                    helper.fail("the " + name(hostBlock) + " build plan leaves the "
                            + L1_ROOM_SIZE + "^3 template at " + rel);
                    return;
                }
                helper.getLevel().setBlockAndUpdate(planned.worldPos(), planned.targetState());
                built.add(planned.worldPos());
            }

            BlockPos cell = absolute.relative(lift);
            var here = helper.getLevel().getBlockState(cell);
            String where = "the cell " + (lift == Direction.UP ? "above" : "below") + " the host";
            if (!here.is(casingBlock)) {
                helper.fail(where + " is " + here.getBlock() + " at " + cell + ", not a "
                        + name(casingBlock) + ", so this test cannot speak about it");
                return;
            }
            helper.getLevel().setBlockAndUpdate(cell, member.defaultBlockState());
            host.rebuildAfterBuild();
            helper.runAfterDelay(20, () -> {
                try {
                    if (helper.getLevel().getBlockEntity(absolute)
                            instanceof cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity<?, ?> ne
                            && ne.isFormed()) {
                        helper.fail(name(hostBlock) + " formed while holding " + name(member) + " in "
                                + where + ", which is a position no build plan places anything");
                        return;
                    }
                } finally {
                    for (BlockPos pos : built) {
                        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
                }
                helper.succeed();
            });
        });
    }

    /** A threading core above the C1 host is extra threads nobody paid the geometry for. */
    @GameTest(template = "l1_room", batch = "l1_threading_above", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void threadingCoreAboveTheComputationHostIsRejected(GameTestHelper helper) {
        memberInHostColumnIsRejected(helper, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                new BlockPos(13, 3, 4), Direction.UP,
                ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get());
    }

    /** The same column seen from underneath. */
    @GameTest(template = "l1_room", batch = "l1_threading_below", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void threadingCoreBelowTheComputationHostIsRejected(GameTestHelper helper) {
        memberInHostColumnIsRejected(helper, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                new BlockPos(13, 3, 4), Direction.DOWN,
                ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get());
    }

    /**
     * F1's geometry walks the same four columns and left the host's own column unlooked at, so it gets
     * the same guard and the same test - here with its own parallel core, which is the free-throughput
     * version of the same hole.
     */
    @GameTest(template = "l1_room", batch = "f1_core_above", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void parallelCoreAboveTheCraftingHostIsRejected(GameTestHelper helper) {
        memberInHostColumnIsRejected(helper, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                new BlockPos(6, 3, 6), Direction.UP,
                ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get());
    }

    /** The same column seen from underneath. */
    @GameTest(template = "l1_room", batch = "f1_core_below", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void parallelCoreBelowTheCraftingHostIsRejected(GameTestHelper helper) {
        memberInHostColumnIsRejected(helper, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                new BlockPos(6, 3, 6), Direction.DOWN,
                ModRegistration.SIMPLIFY_CRAFTING_CASING_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get());
    }

    /**
     * The energized threading core gives sixteen real threads -- sixteen {@code ECOCraftingCPU} objects,
     * because eco sizes that array from the tier in the constructor.
     */
    @GameTest(template = "l1_room", batch = "l1_threading", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedThreadingCoreInFirstSlotAddsThreads(GameTestHelper helper) {
        var ourTier = SimplifyTier.L1_ENERGIZED_THREADING;
        buildComputationStructure(helper, new BlockPos(13, 3, 4),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                controller -> {
                    NEComputationCluster cluster = controller.getCluster();
                    helper.assertTrue(controller.isFormed() && cluster != null,
                            "the L1 subsystem did not form with the energized core in the first threading"
                                    + " slot");
                    int ours = (int) cluster.getThreadingCores().stream()
                            .filter(core -> core.getTier() == ourTier).count();
                    int plain = cluster.getThreadingCores().size() - ours;
                    helper.assertTrue(ours == 1,
                            "the threading line rejected the energized core next to the controller");
                    helper.assertTrue(ourTier.getCPUThreads() == 16,
                            "this member is sold as 16 threads, but its tier says "
                                    + ourTier.getCPUThreads());
                    helper.assertTrue(cluster.getMaxThreads()
                                    == plain * SimplifyTier.L1.getCPUThreads() + ours * 16,
                            "the threading line should total " + (plain * SimplifyTier.L1.getCPUThreads()
                                    + ours * 16) + " threads, got " + cluster.getMaxThreads());
                    // The threading core draws itself, so the artwork hook for "this line runs on the
                    // energized one" is on the core's own block state, not on the host's.
                    var energized = cluster.getThreadingCores().stream()
                            .filter(core -> core.getTier() == ourTier).findFirst().orElseThrow();
                    var coreState = helper.getLevel().getBlockState(energized.getBlockPos());
                    if (coreState.hasProperty(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED)
                            && !coreState.getValue(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED)) {
                        helper.fail("the threading line runs on the energized core, but that core never "
                                + "took formed=true, so it is still showing its unformed model: " + coreState);
                        return;
                    }
                    if (coreState.getRenderShape()
                            == net.minecraft.world.level.block.RenderShape.INVISIBLE) {
                        helper.fail("the energized threading core hides itself once formed, so nothing "
                                + "draws it and its artwork has nowhere to live: " + coreState);
                    }
                });
    }

    /**
     * eco writes {@code network_switch} / {@code high_energy_network_switch} onto every computation host
     * by inspecting one shell cell beside the controller, and its tooltip then reads "高能网络交换 x8"
     * off those bits. Our energized core used to stand in exactly that cell, so a plain L1 machine ended
     * up claiming a switch mode it does not have. The core now goes behind the host; this guard fails if
     * either bit ever comes up true, and names whatever block is sitting in eco's switch cell.
     */
    @GameTest(template = "l1_room", batch = "l1_switch_claim", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationHostNeverClaimsEcosNetworkSwitch(GameTestHelper helper) {
        buildComputationStructure(helper, new BlockPos(13, 3, 4),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                controller -> {
                    var hostPos = controller.getBlockPos();
                    var state = helper.getLevel().getBlockState(hostPos);
                    // The same host reads its own cell: a plain build keeps the energized face unreachable.
                    if (controller.isFormed() && state.getValue(
                            SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE)) {
                        helper.fail("a plain L1 build, casing in the allotted cell, still published "
                                + "energized_parallel_core=true, so the formed face would draw artwork for "
                                + "a member the machine does not have: " + state);
                        return;
                    }
                    if (!state.hasProperty(cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.NETWORK_SWITCH)) {
                        helper.fail("the host has no network_switch property, so this test cannot read it: "
                                + state);
                        return;
                    }
                    var normal = state.getValue(
                            cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.NETWORK_SWITCH);
                    var high = state.getValue(
                            cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
                    if (!normal && !high) {
                        return;
                    }
                    var where = new StringBuilder();
                    for (boolean mirrored : new boolean[]{false, true}) {
                        var cell = cn.dancingsnow.neoecoae.multiblock.network.NENetworkSwitchUtil
                                .switchPosition(hostPos, state, mirrored);
                        where.append(" mirrored=").append(mirrored).append(" -> ")
                                .append(name(helper.getLevel().getBlockState(cell).getBlock()));
                    }
                    helper.fail("an L1 machine with no eco network switch reported network_switch=" + normal
                            + " high_energy_network_switch=" + high + "; eco reads the cell beside the host"
                            + where + ", and our energized core must not stand there");
                });
    }

    /**
     * eco only ever clears those two bits inside its own {@code verifyInternalStructure}, and our
     * calculator replaces that method, so a host saved with them set has nothing left to write false
     * over them -- its CPUs register on a network that does not exist and it never shows up in the
     * network's CPU list. The only thing that can heal an existing world is our host's own tick, and
     * the gate that used to hold it back was an appearance change, which is why this writes the stale
     * bits onto a lone, unformed host and requires the tick to take them back off.
     */
    @GameTest(template = "l1_room", batch = "l1_stale_bits", timeoutTicks = 300,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationHostHealsStaleSwitchBits(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> {
                    var absolute = helper.absolutePos(hostPos);
                    var stale = helper.getLevel().getBlockState(absolute)
                            .setValue(cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem
                                    .NETWORK_SWITCH, true)
                            .setValue(cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem
                                    .HIGH_ENERGY_NETWORK_SWITCH, true);
                    helper.getLevel().setBlockAndUpdate(absolute, stale);
                    if (!helper.getLevel().getBlockState(absolute).getValue(
                            cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.NETWORK_SWITCH)) {
                        var why = "the probe could not leave network_switch set, so this test measures"
                                + " nothing";
                        finish.run();
                        helper.fail(why);
                        return;
                    }
                    // A stale host only gets a fresh look when something around it moves, which is exactly
                    // what an existing world does on load; ask for that neighbour pass rather than waiting
                    // for a tick that nothing schedules.
                    helper.getLevel().updateNeighborsAt(absolute, helper.getLevel()
                            .getBlockState(absolute).getBlock());
                    waitUntil(helper, 12,
                            () -> {
                                var state = helper.getLevel().getBlockState(absolute);
                                return !state.getValue(cn.dancingsnow.neoecoae.blocks.computation
                                                .ECOComputationSystem.NETWORK_SWITCH)
                                        && !state.getValue(cn.dancingsnow.neoecoae.blocks.computation
                                                .ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
                            },
                            () -> {
                                var why = "a formed host kept eco's switch bits set after 120 ticks, so a"
                                        + " machine saved that way stays out of the network's CPU list: "
                                        + helper.getLevel().getBlockState(absolute);
                                finish.run();
                                return why;
                            },
                            () -> {
                                finish.run();
                                helper.succeed();
                            });
                });
    }

    /**
     * The casing side of formation. Every member of an eco multiblock owns an {@code MBCalculator}, and
     * our computation family reuses eco's block entity classes, so which geometry a *casing*-triggered
     * check uses is decided today by {@code NEComputationClusterCalculatorMixin} re-dispatching on the
     * range it is handed; eco 21.2.1's {@code registerCalculatorFactory} would decide it per block entity
     * type instead, and forget the casing type there and this goes red.
     *
     * <p>Measured before settling on this form: pulling a casing out of a formed L1 machine does not
     * un-form the host within 20 ticks, so "break it and see it re-form" cannot state its own baseline.
     * Asking the casing's calculator about the range the build plan produced is deterministic and is the
     * same entry point AE2 walks.
     */
    @GameTest(template = "l1_room", batch = "l1_casing_geometry", timeoutTicks = 220,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationCasingCalculatorKnowsL1Geometry(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> {
                    BlockPos casing = firstComputationCasing(helper);
                    if (casing == null) {
                        finish.run();
                        helper.fail("the built machine contains no computation casing to ask");
                        return;
                    }
                    // The bounds come off the live cluster: a build plan for an already formed machine is
                    // empty, because there is nothing left to plan.
                    var cluster = controller.getCluster();
                    if (cluster == null || !controller.isFormed()) {
                        finish.run();
                        helper.fail("the L1 machine did not form, so it has no cluster bounds");
                        return;
                    }
                    BlockPos min = cluster.getBoundsMin();
                    BlockPos max = cluster.getBoundsMax();
                    if (!(helper.getLevel().getBlockEntity(casing) instanceof NEBlockEntity<?, ?> be)) {
                        finish.run();
                        helper.fail(at(casing) + " holds no NEBlockEntity, so it has no calculator");
                        return;
                    }
                    var calculator = be.getCalculator();
                    boolean verdict = calculator.verifyInternalStructure(helper.getLevel(), min, max);
                    var which = calculator.getClass().getName();
                    finish.run();
                    if (!verdict) {
                        helper.fail("the casing at " + at(casing) + " rejected the L1 layout it stands in,"
                                + " through " + which + " over " + min + "..." + max);
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * {@code validateInterfaceFace} is the one part of the L1 computation geometry a subclass may answer
     * for itself. Over one standing machine: the plain calculator accepts it; a subclass refusing the
     * face is asked once per hand and the machine fails; a subclass answering with a block of its own
     * passes, without the network interface's face. {@code validateStructure} reads the world only, so
     * all three ask the same machine.
     */
    @GameTest(template = "l1_room", batch = "l1_interface_face_hook", timeoutTicks = 220,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationInterfaceFaceIsOverridable(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> {
                    var cluster = controller.getCluster();
                    if (cluster == null || !controller.isFormed()) {
                        finish.run();
                        helper.fail("the L1 machine did not form, so it has no cluster bounds");
                        return;
                    }
                    BlockPos min = cluster.getBoundsMin();
                    BlockPos max = cluster.getBoundsMax();
                    var level = helper.getLevel();
                    var plain = new SimplifyComputationClusterCalculator(controller).validateStructure(level, min, max);
                    int[] asked = {0};
                    var refused = new SimplifyComputationClusterCalculator(controller) {
                        @Override
                        protected java.util.Optional<Block> validateInterfaceFace(
                                net.minecraft.server.level.ServerLevel level, BlockPos controllerPos,
                                Direction back, Direction top, Direction down, Direction interfaceSide,
                                BlockPos energizedCorePos) {
                            asked[0]++;
                            return java.util.Optional.empty();
                        }
                    }.validateStructure(level, min, max);
                    var own = new SimplifyComputationClusterCalculator(controller) {
                        @Override
                        protected java.util.Optional<Block> validateInterfaceFace(
                                net.minecraft.server.level.ServerLevel level, BlockPos controllerPos,
                                Direction back, Direction top, Direction down, Direction interfaceSide,
                                BlockPos energizedCorePos) {
                            return java.util.Optional.of(Blocks.COBBLESTONE);
                        }
                    }.validateStructure(level, min, max);
                    finish.run();
                    if (!plain.valid()) {
                        helper.fail("the plain calculator rejected the L1 layout over " + min + "..." + max);
                        return;
                    }
                    if (refused.valid() || asked[0] != 2) {
                        helper.fail("a refused interface face should fail both hands, but the machine was "
                                + (refused.valid() ? "accepted" : "rejected") + " after " + asked[0] + " asks");
                        return;
                    }
                    if (!own.valid() || own.communicationInterface()) {
                        helper.fail("an interface face of the subclass's own should form without the network"
                                + " interface's face, but valid=" + own.valid()
                                + " communicationInterface=" + own.communicationInterface());
                        return;
                    }
                    helper.succeed();
                });
    }

    /** The first computation casing standing anywhere inside this test room. */
    private static BlockPos firstComputationCasing(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, L1_ROOM_SIZE - 1, L1_ROOM_SIZE - 1))) {
            if (helper.getLevel().getBlockState(pos)
                    .is(ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get())) {
                return pos.immutable();
            }
        }
        return null;
    }

    /**
     * A cable on the machine's interface pulls the whole computation subsystem -- controller included --
     * onto the player's grid, which is the half that a field test cannot see: eco exposes a host's node
     * only toward other eco blocks ({@code NEBlockEntity.getGridConnectableSides} answers nothing toward
     * air), so wiring the host directly is impossible by design, while the interface block opens all six
     * faces once formed ({@code ECOMachineInterfaceBlockEntity}) and every member node carries
     * {@code IGridMultiblock}. Measured here: host, cable and power on one 92-node grid.
     *
     * <p>The CPU list is asserted as well, and it is the reason the host's block entity is eco's own class
     * rather than a subclass: eco's {@code CraftingServiceMixin.onGetCpus} appends
     * {@code cluster.getActiveCPUs()} plus, when {@code isNetworkRepresentative()} holds and
     * {@code getActiveCPUCount() < getMaxThreads()}, one {@code getFakeCPU()} -- the placeholder an idle
     * subsystem shows as in the crafting status panel. It collects clusters through
     * {@code getMachines(ECOComputationSystemBlockEntity.class)}, and AE2 files nodes under
     * {@code owner.getClass()} only ({@code Grid.add} has a single {@code put} and no superclass walk), so
     * a subclass host is invisible to both the panel and the execution path behind it. This test is the
     * guard against ever needing that subclass again.
     */
    @GameTest(template = "l1_room", batch = "l1_cpu_grid", timeoutTicks = 300,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void formedComputationHostAppearsInTheCpuList(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> {
                    Wiring[] wired = new Wiring[1];
                    // helper.fail throws, so teardown has to happen on the way out rather than after it:
                    // a live host left standing in this room wedges the tests that run after it.
                    Runnable leave = () -> {
                        if (wired[0] != null) {
                            helper.setBlock(wired[0].cable(), Blocks.AIR);
                            helper.setBlock(wired[0].power(), Blocks.AIR);
                        }
                        finish.run();
                    };
                    if (!controller.isFormed()) {
                        leave.run();
                        helper.fail("the L1 machine did not form, so there is nothing to wire: "
                                + helper.getLevel().getBlockState(helper.absolutePos(hostPos)));
                        return;
                    }
                    wired[0] = wireTheInterfaceCell(helper, hostPos);
                    if (wired[0] == null) {
                        leave.run();
                        helper.fail("no interface in the built machine had two free cells on a side, so"
                                + " this test has no way to reach the host's grid");
                        return;
                    }
                    waitUntil(helper, 12,
                            () -> sharesGridWithPower(helper, controller, wired[0].power()),
                            () -> {
                                var why = "the network never reached the host: host=" + gridOf(controller)
                                        + " interface@" + at(wired[0].machine()) + "="
                                        + nodeGrid(helper, wired[0].machine()) + " cable@"
                                        + at(wired[0].cable()) + "=" + nodeGrid(helper, wired[0].cable())
                                        + " power@" + at(wired[0].power()) + "="
                                        + nodeGrid(helper, wired[0].power());
                                leave.run();
                                return why;
                            },
                            () -> {
                                // What is asserted here stops at "the host is on the player's grid".
                                // Whether the CPU panel lists the subsystem is NOT readable from
                                // ICraftingService.getCpus(): eco ships its own snapshot to the client
                                // (MenuDataTransport Channel.CPU + ExactCpuSnapshot) and mixes into
                                // AE2's selection list, so the panel has a different source than the
                                // service -- measured idle here: 0 entries from getCpus().
                                var grid = gridOf(controller);
                                var cpus = grid.getCraftingService().getCpus();
                                long ours = cpus.stream().filter(cpu -> cpu instanceof cn.dancingsnow
                                        .neoecoae.api.me.ECOCraftingCPU).count();
                                if (ours == 0) {
                                    var cluster = controller.getCluster();
                                    var why = "a formed, networked L1 host is missing from getCpus(): "
                                            + "grid=" + grid.size() + " nodes, listed cpus=" + cpus.size()
                                            + ", cluster=" + (cluster == null ? "null"
                                            : ("active=" + cluster.getActiveCPUCount()
                                                    + " maxThreads=" + cluster.getMaxThreads()
                                                    + " representative=" + cluster.isNetworkRepresentative()
                                                    + " fakeCpu=" + (cluster.getFakeCPU() == null
                                                            ? "null" : "present")))
                                            + " machineKeys=" + machineKeyReport(grid);
                                    leave.run();
                                    helper.fail(why);
                                    return;
                                }
                                leave.run();
                                helper.succeed();
                            });
                });
    }

    /**
     * eco's fast planner is not a property of a machine, it is two network-level switches:
     * {@code ECOCraftingNetworkSettings#shouldUseFastPlanner()} reads {@code isFastPlannerEnabled() &&
     * hasComputationHost()} (measured in eco's own default method, offsets 0-23), and the host half - eco's
     * {@code CraftingServiceMixin}, offsets 0-60 - walks
     * {@code getMachines(grid, ECOComputationSystemBlockEntity.class)} for any entry that answers
     * {@code isFormed()} with an online main node. Our C1 deliberately <em>is</em> that block entity class,
     * so the question worth pinning for the whole L1 line is exactly this one: does a formed C1 flip that
     * switch, or does eco only count its own hosts.
     *
     * <p>The second half is the control and it is the reason this is more than a reading: the same call,
     * taken after the host's block is gone, has to answer false. Without that, "true" here would not tell
     * our lookup apart from a switch that is simply always on.</p>
     */
    @GameTest(template = "l1_room", batch = "doll_cpu", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void formedComputationHostIsTheGateForEcosFastPlanner(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> {
                    Wiring[] wired = new Wiring[1];
                    Runnable leave = () -> {
                        if (wired[0] != null) {
                            helper.setBlock(wired[0].cable(), Blocks.AIR);
                            helper.setBlock(wired[0].power(), Blocks.AIR);
                        }
                        finish.run();
                    };
                    if (!controller.isFormed()) {
                        leave.run();
                        helper.fail("the L1 host never formed, so the gate has nothing to count: "
                                + helper.getLevel().getBlockState(helper.absolutePos(hostPos)));
                        return;
                    }
                    wired[0] = wireTheInterfaceCell(helper, hostPos);
                    if (wired[0] == null) {
                        leave.run();
                        helper.fail("no interface in the built machine had two free cells on a side, so the"
                                + " host is never reachable as a grid and the switch cannot be asked");
                        return;
                    }
                    var power = wired[0].power();
                    // Two separate waits, because "on the same grid" and "AE2 calls that node online" are
                    // different moments: eco's gate reads isOnline() on the host's own managed node, and a
                    // node can sit on a live grid while still booting. Polling the cheaper one and reading
                    // the other is how a fixture goes red on a premise instead of on a bug.
                    waitUntil(helper, 12,
                            () -> sharesGridWithPower(helper, controller, power) && hostIsOnline(controller),
                            () -> {
                                var why = "the network never reached the host, or the host never came"
                                        + " online: host=" + gridOf(controller) + " power@" + at(power)
                                        + "=" + nodeGrid(helper, power) + " " + gridStateReport(controller);
                                leave.run();
                                return why;
                            },
                            () -> {
                                var grid = gridOf(controller);
                                var settings = cn.dancingsnow.neoecoae.api.me.network
                                        .ECOCraftingNetworkSettings.of(grid);
                                if (settings == null) {
                                    var why = "the crafting service carries no eco network settings, so"
                                            + " shouldUseFastPlanner() has no subject here: grid=" + grid
                                            + " service=" + grid.getCraftingService().getClass().getName();
                                    leave.run();
                                    helper.fail(why);
                                    return;
                                }
                                if (!settings.neoecoae$hasComputationHost()) {
                                    var why = "a formed, online L1 host did not count as eco's computation"
                                            + " host, so the fast planner stays off for our whole tier:"
                                            + " isFastPlannerEnabled=" + settings.neoecoae$isFastPlannerEnabled()
                                            + " " + gridStateReport(controller);
                                    leave.run();
                                    helper.fail(why);
                                    return;
                                }
                                if (settings.neoecoae$shouldUseFastPlanner()
                                        != settings.neoecoae$isFastPlannerEnabled()) {
                                    var why = "with the host counted, shouldUseFastPlanner() must be exactly"
                                            + " the config switch: planner="
                                            + settings.neoecoae$isFastPlannerEnabled() + " should="
                                            + settings.neoecoae$shouldUseFastPlanner();
                                    leave.run();
                                    helper.fail(why);
                                    return;
                                }
                                NeoECOPrototype.LOGGER.info(
                                        "fast planner gate: formed L1 host counted,"
                                                + " hasComputationHost=true isFastPlannerEnabled={}"
                                                + " shouldUseFastPlanner={} {}",
                                        settings.neoecoae$isFastPlannerEnabled(),
                                        settings.neoecoae$shouldUseFastPlanner(), computationHostReport(grid));
                                // The same reading with the host gone. The power block keeps its own node,
                                // so the grid survives and the switch is still askable.
                                helper.setBlock(hostPos, Blocks.AIR);
                                waitUntil(helper, 8, () -> !hostCounts(helper, power),
                                        () -> {
                                            var node = nodeAt(helper, power);
                                            var why = "hasComputationHost() still answers true after the host"
                                                    + " block was removed, so eco's machine table is holding a"
                                                    + " machine that no longer exists: "
                                                    + (node == null ? "no grid left to ask"
                                                            : computationHostReport(node.getGrid()));
                                            leave.run();
                                            return why;
                                        },
                                        () -> {
                                            leave.run();
                                            helper.succeed();
                                        });
                            });
                });
    }

    /** Whether eco's settings still count a computation host on the grid the block at {@code at} sits on. */
    private static boolean hostCounts(GameTestHelper helper, BlockPos at) {
        var node = nodeAt(helper, at);
        if (node == null) {
            return true;
        }
        var settings = cn.dancingsnow.neoecoae.api.me.network.ECOCraftingNetworkSettings.of(node.getGrid());
        return settings == null || settings.neoecoae$hasComputationHost();
    }

    /** Whether AE2 calls the host's own managed node online - the flag eco's second gate reads. */
    private static boolean hostIsOnline(ECOComputationSystemBlockEntity controller) {
        var mainNode = controller.getMainNode();
        return mainNode != null && mainNode.isOnline();
    }

    /** The three grid facts that decide whether a node is allowed to be online, for the failure line. */
    private static String gridStateReport(ECOComputationSystemBlockEntity controller) {
        var grid = gridOf(controller);
        if (grid == null) {
            return "grid=none (the host has no grid at all)";
        }
        int nodes = 0;
        for (var unused : grid.getNodes()) {
            nodes++;
        }
        var pathing = grid.getPathingService();
        return "nodes=" + nodes + " booting=" + pathing.isNetworkBooting()
                + " controller=" + pathing.getControllerState()
                + " powered=" + grid.getEnergyService().isNetworkPowered()
                + " " + computationHostReport(grid);
    }

    /**
     * What eco's own query sees, machine by machine - both flags its loop reads are in here, because "the
     * table is empty" and "the table has one that answers not-formed" are different bugs and the failure
     * line has to say which one it found.
     */
    private static String computationHostReport(IGrid grid) {
        var machines = grid.getMachines(ECOComputationSystemBlockEntity.class);
        StringBuilder report = new StringBuilder("machines=").append(machines.size()).append(" [");
        for (var machine : machines) {
            var mainNode = machine.getMainNode();
            report.append(at(machine.getBlockPos())).append(" formed=").append(machine.isFormed())
                    .append(" online=").append(mainNode != null && mainNode.isOnline()).append(' ');
        }
        return report.append(']').toString();
    }

    /**
     * eco's fast planner has a second gate that is not a switch: {@code ECOExternalCpuSupport#supportsPlan}
     * decides whether a given {@code ICraftingPlan} is eligible at all, and eco's own planner is asked
     * through {@code ECOPlannerRequester}, which wraps AE2's simulation requester with
     * {@code ECOPlannerOptions}. Our {@code TrinityCraftingExecutor} asks AE2 directly (line 163), so the
     * question worth pinning is whether a plan produced that way is eligible, or whether eligibility only
     * appears when the request carries eco's options.
     *
     * <p>Measured with the two assertions below and one reading that is deliberately not asserted: the
     * third plan asked here is one the network cannot build, and eco's gate answers {@code true} for it too
     * - so this gate reports a capability, not a quality, and "eligible" must not be read as "the fast
     * planner will take this job". What is pinned is that both routes hand back the same
     * {@code appeng.crafting.CraftingPlan} at the same cost, and that eco's gate accepts it: if eco ever
     * starts wrapping plans or tightening acceptance, this goes red instead of us guessing.</p>
     */
    @GameTest(template = "trinity_room", batch = "fast_planner_contract", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ecoPlanEligibilityReadsTheSameJobPlannedBothWays(GameTestHelper helper) {
        BlockPos rigMin = new BlockPos(3, 3, 3);
        withTrinityCraftRig(helper, rigMin,
                module -> {
                    ItemStack[] slots = emptyPatternGrid();
                    slots[0] = new ItemStack(Items.OAK_LOG);
                    module.getLogic().getPatternInv().setItemDirect(0,
                            PatternDetailsHelper.encodeCraftingPattern(
                                    craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)),
                                    slots, new ItemStack(Items.OAK_PLANKS, 4), false, false));
                },
                (controller, network) -> {
                    var service = network.getCraftingService();
                    var target = AEItemKey.of(Items.OAK_PLANKS);
                    var source = IActionSource.ofMachine(controller);
                    long stocked = network.getStorageService().getInventory().insert(
                            AEItemKey.of(Items.OAK_LOG), 4, Actionable.MODULATE, source);
                    if (stocked < 4) {
                        helper.fail("only " + stocked + "/4 oak logs reached the network, so no plan can be"
                                + " built and the gate has nothing to judge");
                        return;
                    }
                    if (!service.isCraftable(target)) {
                        NeoECOPrototype.LOGGER.info(
                                "eco plan eligibility: planks not craftable yet (patterns indexed for"
                                        + " planks={}), waiting for AE2's refresh",
                                service.getCraftingFor(target).size());
                    }
                    waitUntil(helper, 12, () -> service.isCraftable(target),
                            () -> "the rig never considered oak planks craftable, so both plans below would be"
                                    + " empty and the reading would measure nothing: patterns for planks="
                                    + service.getCraftingFor(target).size() + " cpus="
                                    + service.getCpus().size() + " "
                                    + trinityCraftModuleReport(helper, rigMin, network),
                            () -> {
                                var settings = cn.dancingsnow.neoecoae.api.me.network
                                        .ECOCraftingNetworkSettings.of(network);
                                if (settings == null) {
                                    helper.fail("the crafting service carries no eco network settings, so the"
                                            + " wrapped route cannot be built: service="
                                            + service.getClass().getName());
                                    return;
                                }
                                planBothWays(helper, service, network, target, source, settings,
                                        controller.getActionableNode(), cpusOf(service));
                            });
                });
    }

    /** The CPUs on this grid, captured for eco's {@code accepts} ask. */
    private static java.util.List<appeng.api.networking.crafting.ICraftingCPU> cpusOf(
            appeng.api.networking.crafting.ICraftingService service) {
        return java.util.List.copyOf(service.getCpus());
    }

    /** Plans the same job twice - once the way our executor asks, once through eco's planner request - and
     *  reports what eco's eligibility gate says about each. */
    private static void planBothWays(GameTestHelper helper,
            appeng.api.networking.crafting.ICraftingService service, IGrid network,
            appeng.api.stacks.AEKey target,
            IActionSource source,
            cn.dancingsnow.neoecoae.api.me.network.ECOCraftingNetworkSettings settings,
            @Nullable appeng.api.networking.IGridNode node,
            java.util.List<appeng.api.networking.crafting.ICraftingCPU> cpus) {
        var requester = new appeng.api.networking.crafting.ICraftingSimulationRequester() {
            @Override
            public IActionSource getActionSource() {
                return source;
            }

            @Override
            @Nullable
            public appeng.api.networking.IGridNode getGridNode() {
                return node;
            }
        };
        var strategy = appeng.api.networking.crafting.CalculationStrategy.REPORT_MISSING_ITEMS;
        java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan> direct;
        java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan> wrapped;
        java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan> impossible;
        try {
            direct = service.beginCraftingCalculation(helper.getLevel(), requester, target, 4, strategy);
            wrapped = service.beginCraftingCalculation(helper.getLevel(),
                    new cn.dancingsnow.neoecoae.api.me.planning.ECOPlannerRequester(requester,
                            cn.dancingsnow.neoecoae.api.me.planning.ECOPlannerOptions.from(settings)),
                    target, 4, strategy);
            // Recorded, not asserted. Measured here: this gate answers true even for a plan the network
            // cannot build, so "true" for our plans is a capability answer, not evidence about quality -
            // which is why the two assertions below are about eligibility and route-equivalence instead.
            impossible = service.beginCraftingCalculation(helper.getLevel(), requester,
                    appeng.api.stacks.AEItemKey.of(net.minecraft.world.item.Items.DIAMOND_BLOCK), 1, strategy);
        } catch (RuntimeException exception) {
            helper.fail("the crafting calculation could not even be started: " + exception);
            return;
        }
        waitUntil(helper, 20, () -> direct.isDone() && wrapped.isDone() && impossible.isDone(),
                () -> "the crafting calculation never finished: direct=" + direct.isDone()
                        + " wrapped=" + wrapped.isDone() + " impossible=" + impossible.isDone(),
                () -> {
                    var directPlan = finishedPlan(helper, direct, "direct");
                    var wrappedPlan = finishedPlan(helper, wrapped, "wrapped");
                    var impossiblePlan = finishedPlan(helper, impossible, "impossible");
                    if (directPlan == null || wrappedPlan == null || impossiblePlan == null) {
                        return;
                    }
                    var cpu = cpus.isEmpty() ? null : cpus.get(0);
                    NeoECOPrototype.LOGGER.info(
                            "eco plan eligibility: {} | {} | {} | cpus={} hasComputationHost={}"
                                    + " shouldUseFastPlanner={}",
                            planReport("direct", directPlan, cpu),
                            planReport("wrapped", wrappedPlan, cpu),
                            planReport("impossible", impossiblePlan, cpu), cpus.size(),
                            settings.neoecoae$hasComputationHost(), settings.neoecoae$shouldUseFastPlanner());
                    if (directPlan.missingItems().iterator().hasNext()
                            || wrappedPlan.missingItems().iterator().hasNext()) {
                        helper.fail("a plan came back with missing items ("
                                + missingReport(directPlan) + " / " + missingReport(wrappedPlan)
                                + "), so neither route proved anything about eligibility");
                        return;
                    }
                    if (cpu == null) {
                        helper.fail("the rig built no crafting CPU, so eco's accepts(cpu, plan) gate was"
                                + " never asked - the reading above would be half empty");
                        return;
                    }
                    if (!ecoSupports(directPlan, cpu) || !ecoSupports(wrappedPlan, cpu)) {
                        helper.fail("our own plan stopped being eligible for eco's fast planner: "
                                + "direct supportsPlan=" + ecoSupportsPlan(directPlan)
                                + " accepts=" + ecoAccepts(directPlan, cpu)
                                + ", wrapped supportsPlan=" + ecoSupportsPlan(wrappedPlan)
                                + " accepts=" + ecoAccepts(wrappedPlan, cpu));
                        return;
                    }
                    if (!directPlan.getClass().getName().equals(wrappedPlan.getClass().getName())
                            || directPlan.bytes() != wrappedPlan.bytes()) {
                        helper.fail("asking through eco's planner options changed what the calculation"
                                + " returns: direct=" + directPlan.getClass().getName() + " bytes="
                                + directPlan.bytes() + " vs wrapped=" + wrappedPlan.getClass().getName()
                                + " bytes=" + wrappedPlan.bytes() + " - the two routes are no longer the"
                                + " same plan and the eligibility reading above has to be re-taken");
                        return;
                    }
                    helper.succeed();
                });
    }

    private static boolean ecoSupportsPlan(appeng.api.networking.crafting.ICraftingPlan plan) {
        return cn.dancingsnow.neoecoae.crafting.execution.ECOExternalCpuSupport.supportsPlan(plan);
    }

    private static boolean ecoAccepts(appeng.api.networking.crafting.ICraftingPlan plan,
                                      appeng.api.networking.crafting.ICraftingCPU cpu) {
        return cn.dancingsnow.neoecoae.crafting.execution.ECOExternalCpuSupport.accepts(cpu, plan);
    }

    private static boolean ecoSupports(appeng.api.networking.crafting.ICraftingPlan plan,
                                       appeng.api.networking.crafting.ICraftingCPU cpu) {
        return ecoSupportsPlan(plan) && ecoAccepts(plan, cpu);
    }

    /** Which half of the rig is missing when the grid refuses to consider the output craftable. */
    private static String trinityCraftModuleReport(GameTestHelper helper, BlockPos min, IGrid network) {
        var pos = min.offset(SimplifyTrinityClusterCalculator.CRAFTING_MODULE_OFFSET);
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(pos))
                instanceof SimplifyTrinityCraftingModuleBlockEntity module)) {
            return "craftModule@" + at(pos) + "=absent";
        }
        var node = module.getMainNode().getNode();
        return "craftModule@" + at(pos) + " patterns=" + module.getAvailablePatterns().size()
                + " provider=" + (node != null && node.getService(
                        appeng.api.networking.crafting.ICraftingProvider.class) != null)
                + " sameGrid=" + (node != null && node.getGrid() == network);
    }

    /** Unwraps a finished calculation. A plan that could not be built has to end the test, not the branch. */
    @Nullable
    private static appeng.api.networking.crafting.ICraftingPlan finishedPlan(
            GameTestHelper helper,
            java.util.concurrent.Future<appeng.api.networking.crafting.ICraftingPlan> future, String route) {
        try {
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            helper.fail("the " + route + " crafting calculation was interrupted: " + exception);
            return null;
        } catch (java.util.concurrent.ExecutionException exception) {
            helper.fail("the " + route + " crafting calculation threw, so there is no plan to judge: "
                    + exception.getCause());
            return null;
        }
    }

    /** One line per plan: what it is, what it costs, and what eco's gate says about it. */
    private static String planReport(String route, appeng.api.networking.crafting.ICraftingPlan plan,
                                     @Nullable appeng.api.networking.crafting.ICraftingCPU cpu) {
        return route + " class=" + plan.getClass().getName()
                + " bytes=" + plan.bytes()
                + " simulation=" + plan.simulation()
                + " patterns=" + plan.patternTimes().size()
                + " missing=" + missingReport(plan)
                + " supportsPlan=" + ecoSupportsPlan(plan)
                + (cpu == null ? " accepts=no cpu to ask" : " accepts=" + ecoAccepts(plan, cpu));
    }

    private static String missingReport(appeng.api.networking.crafting.ICraftingPlan plan) {
        StringBuilder line = new StringBuilder();
        for (var entry : plan.missingItems()) {
            line.append(entry.getKey().getId()).append('x').append(entry.getLongValue()).append(' ');
        }
        return line.isEmpty() ? "none" : line.toString().trim();
    }

    /** True once the host and the power block at {@code power} sit on the same ME grid. */
    private static boolean sharesGridWithPower(GameTestHelper helper,
                                               ECOComputationSystemBlockEntity controller, BlockPos power) {
        var hostGrid = gridOf(controller);
        var node = nodeAt(helper, power);
        return hostGrid != null && node != null && node.getGrid() == hostGrid;
    }

    /**
     * The node a block entity reports. This has to go through AE2's {@code IGridConnectedBlockEntity},
     * not {@code IInWorldGridNodeHost#getGridNode(side)}: that one is addressed by face and answers null
     * for the whole block, which reads as "no node" on a block that is perfectly well connected.
     */
    private static IGridNode nodeAt(GameTestHelper helper, BlockPos pos) {
        if (helper.getLevel().getBlockEntity(helper.absolutePos(pos))
                instanceof IGridConnectedBlockEntity connected) {
            return connected.getGridNode();
        }
        return null;
    }

    private static IGrid gridOf(ECOComputationSystemBlockEntity controller) {
        var node = controller.getMainNode();
        return node == null ? null : node.getGrid();
    }

    /**
     * Which class keys AE2 filed this grid's machines under. If the exact owner class is the only key that
     * answers, our subclass is invisible to eco's query for {@code ECOComputationSystemBlockEntity} and the
     * panel gap is ours to bridge; if any parent class answers, that theory is dead and the cause is
     * somewhere else.
     */
    private static String machineKeyReport(IGrid grid) {
        return "asEcoHost=" + grid.getMachines(cn.dancingsnow.neoecoae.blocks.entity.computation
                .ECOComputationSystemBlockEntity.class).size()

                + " asNEBlockEntity=" + grid.getMachines(cn.dancingsnow.neoecoae.blocks.entity
                .NEBlockEntity.class).size()
                + " asAENetworked=" + grid.getMachines(appeng.blockentity.grid.AENetworkedBlockEntity.class)
                .size()
                + " asCraftingCPUCtrl=" + grid.getMachines(appeng.api.networking.crafting
                .ICraftingCPU.class).size();
    }

    /** The grid the node at a template-relative position reports, for naming which link is broken. */
    private static String nodeGrid(GameTestHelper helper, BlockPos pos) {
        var node = nodeAt(helper, pos);
        return node == null ? "no node" : String.valueOf(node.getGrid());
    }

    /** The machine cell the cable hangs on, plus the two cells the wiring writes. */
    private record Wiring(BlockPos machine, BlockPos cable, BlockPos power) { }

    /**
     * Stands a glass cable and then our superconductive interface in the first two free cells outside an
     * interface block of the machine, or returns null when the geometry leaves no room to wire it. Only
     * these two cells are written, and the caller's teardown clears them along with the machine.
     */
    private static Wiring wireTheInterfaceCell(GameTestHelper helper, BlockPos hostPos) {
        var player = helper.makeMockPlayer(GameType.CREATIVE);
        for (int x = -8; x <= 8; x++) {
            for (int y = -3; y <= 3; y++) {
                for (int z = -8; z <= 8; z++) {
                    var cell = hostPos.offset(x, y, z);
                    var absolute = helper.absolutePos(cell);
                    var block = helper.getLevel().getBlockState(absolute).getBlock();
                    if (block != ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get()
                            && block != ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get()) {
                        continue;
                    }
                    for (var side : Direction.values()) {
                        var cable = cell.relative(side);
                        var power = cable.relative(side);
                        if (!freeTemplateCell(helper, cable) || !freeTemplateCell(helper, power)) {
                            continue;
                        }
                        if (!placeCableAgainst(helper, player, cell, side)) {
                            continue;
                        }
                        helper.setBlock(power, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
                        return new Wiring(cell, cable, power);
                    }
                }
            }
        }
        return null;
    }

    /**
     * The whole mood is a pure function of the machine's own present, so it can be pinned without a world:
     * it may only ever save power, a host without a face gets exactly the baseline, and out-of-range inputs
     * clamp instead of multiplying the reward.
     */
    @GameTest(template = "empty", batch = "machine_mood", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void machineMoodOnlyEverSavesPowerAndNeverCosts(GameTestHelper helper) {
        // The gate: no face, no difference, however comfortable the machine is.
        if (cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                false, 1.0D, true, true, 0.10D) != 1.0D) {
            helper.fail("a host without a mind got an idle-power discount");
            return;
        }
        if (cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 1.0D, true, false, 0.10D) != 1.0D) {
            helper.fail("an unformed structure got an idle-power discount, so the bonus is not tied"
                    + " to a finished machine");
            return;
        }
        // The ceiling: a perfect situation costs exactly (1 - saving), never less.
        double best = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 1.0D, true, true, 0.10D);
        if (Math.abs(best - 0.90D) > 1.0E-9D) {
            helper.fail("a contented host scaled idle power to " + best + ", expected 0.90 for a 10% cap");
            return;
        }
        // Garbage in must not become a bigger reward: coolant above full is still full.
        double overfull = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 9.0D, true, true, 0.10D);
        if (Math.abs(overfull - best) > 1.0E-9D) {
            helper.fail("coolant fraction 9.0 paid out " + overfull + " instead of clamping to " + best);
            return;
        }
        // Direction: fuller coolant is never worse, and having work is never worse.
        double dry = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 0.0D, false, true, 0.10D);
        double halfIdle = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 0.5D, false, true, 0.10D);
        double halfBusy = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 0.5D, true, true, 0.10D);
        if (!(dry >= halfIdle && halfIdle >= halfBusy && halfBusy >= best)) {
            helper.fail("mood is not monotonic: dry+idle " + dry + ", half+idle " + halfIdle
                    + ", half+busy " + halfBusy + ", full+busy " + best);
            return;
        }
        // A machine with nothing going on pays exactly what a mindless one pays.
        if (Math.abs(dry - 1.0D) > 1.0E-9D) {
            helper.fail("a minded host with no coolant and no work drew " + dry
                    + "x idle power; it should cost exactly the same as a host without a mind");
            return;
        }
        if (cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                true, 1.0D, true, true, 0.0D) != 1.0D) {
            helper.fail("a saving of 0 still changed the draw, so the config cannot turn this off");
            return;
        }
        helper.succeed();
    }

    /**
     * The three rungs, and the corner that used to be wrong. Work decides first: a dry machine with a job
     * running is happy, and coolant only lifts a machine that has nothing to do. The weighted sum this
     * replaced paid a dry-but-busy host four tenths of the saving and put a full bath with no work on the
     * middle face, which is how one tooltip line came to carry a word and a figure telling different
     * stories. This is also the first guard to read {@code mood()} at all - the ladder used to be pinned
     * only at its two ends, the continuous factor here and the pure threshold below it.
     *
     * <p>The word is only ever a lang lookup, so its three keys are read out of both files here: a renamed
     * key shows itself to the player as the raw key, and no other guard in the suite looks at lang.
     */
    @GameTest(template = "empty", batch = "machine_mood", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void machineMoodRungsFollowWorkThenCoolant(GameTestHelper helper) {
        var ladder = java.util.List.of(
                new Object[]{"dry and busy", false, true,
                        cn.dancingsnow.neoecoprototype.api.MachineMood.HAPPY, 2, "happy"},
                new Object[]{"coolant and busy", true, true,
                        cn.dancingsnow.neoecoprototype.api.MachineMood.HAPPY, 2, "happy"},
                new Object[]{"coolant and idle", true, false,
                        cn.dancingsnow.neoecoprototype.api.MachineMood.SLACKING, 1, "slacking"},
                new Object[]{"dry and idle", false, false,
                        cn.dancingsnow.neoecoprototype.api.MachineMood.CALM, 0, "calm"});
        for (var rung : ladder) {
            double coolant = Boolean.TRUE.equals(rung[1]) ? 1.0D : 0.0D;
            boolean busy = (Boolean) rung[2];
            double mood = cn.dancingsnow.neoecoprototype.api.MachineMood.mood(coolant, busy);
            if (mood != (Double) rung[3]) {
                helper.fail(rung[0] + " scored mood " + mood + ", expected " + rung[3]);
                return;
            }
            int tier = cn.dancingsnow.neoecoprototype.api.MachineMood.tierOf(mood, -1);
            if (tier != (Integer) rung[4]) {
                helper.fail(rung[0] + " wore face band " + tier + ", expected " + rung[4]);
                return;
            }
            String key = cn.dancingsnow.neoecoprototype.api.MachineMood.wordKey(tier);
            if (!key.endsWith((String) rung[5])) {
                helper.fail(rung[0] + " reads its word from " + key + ", expected a " + rung[5] + " key");
                return;
            }
            // The figure printed on the same line has to come off the same ladder, or the line lies.
            double factor = cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                    true, coolant, busy, true, 0.10D);
            if (Math.abs(factor - (1.0D - 0.10D * (Double) rung[3])) > 1.0E-9D) {
                helper.fail(rung[0] + " scales idle power to " + factor + ", which is not mood "
                        + rung[3] + " at a 10% cap");
                return;
            }
        }
        for (String lang : new String[]{"en_us", "zh_cn"}) {
            String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
            if (json == null) {
                return;
            }
            com.google.gson.JsonObject keys;
            try {
                keys = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            } catch (com.google.gson.JsonParseException | IllegalStateException failure) {
                helper.fail("lang/" + lang + ".json did not parse: " + failure);
                return;
            }
            for (var rung : ladder) {
                String key = "machine_mood.neoecoprototype." + rung[5];
                if (!keys.has(key) || keys.get(key).getAsString().isBlank()) {
                    helper.fail("lang/" + lang + ".json has no " + key + ", so that rung shows the raw key"
                            + " in the tooltip instead of a word");
                    return;
                }
            }
            if (!keys.has("jade.neoecoprototype.machine_mind")) {
                helper.fail("lang/" + lang + ".json has no jade.neoecoprototype.machine_mind line");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * The face band is the one part of the mood that gets written into the block state, so it must not
     * chatter: coolant drains steadily during a job and comes back when a pipe delivers, which walks the
     * mood straight across whatever threshold is set. Rising and falling therefore use different numbers,
     * and this pins the gap.
     */
    @GameTest(template = "empty", batch = "machine_mood", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void machineMoodBandsDoNotChatter(GameTestHelper helper) {
        // Moods here are percent, divided by 100 at the call, so the numbers in this test read like the
        // bands they are talking about.
        java.util.function.IntBinaryOperator tier = (percent, previous) ->
                cn.dancingsnow.neoecoprototype.api.MachineMood.tierOf(percent / 100.0D, previous);
        // Crossing up needs 0.34; falling back below 0.30 is what gives the band up again.
        if (tier.applyAsInt(50, 0) != 1) {
            helper.fail("mood 0.50 from tier 0 gave " + tier.applyAsInt(50, 0) + ", expected 1");
            return;
        }
        if (tier.applyAsInt(32, 1) != 1) {
            helper.fail("mood 0.32 gave up tier 1, so there is no hysteresis band at all");
            return;
        }
        if (tier.applyAsInt(29, 1) != 0) {
            helper.fail("mood 0.29 kept tier 1, so the band never closes");
            return;
        }
        if (tier.applyAsInt(60, 2) != 1 || tier.applyAsInt(65, 2) != 2) {
            helper.fail("the top band is wrong: 0.60 -> " + tier.applyAsInt(60, 2)
                    + ", 0.65 -> " + tier.applyAsInt(65, 2) + " (expected 1 and 2)");
            return;
        }
        // The whole point: a mood that oscillates inside the gap rewrites nothing.
        int current = 0;
        int writes = 0;
        for (int step = 0; step < 100; step++) {
            int next = tier.applyAsInt(step % 2 == 0 ? 36 : 32, current);
            if (next != current) {
                writes++;
                current = next;
            }
        }
        if (writes > 1) {
            helper.fail("a mood wobbling between 0.32 and 0.36 changed band " + writes
                    + " times; it may settle once and then stay put");
            return;
        }
        // A never-decreasing readout, and out-of-range history cannot escape 0..2.
        int last = 0;
        for (int m = 0; m <= 100; m++) {
            last = tier.applyAsInt(m, last);
        }
        if (last != 2 || tier.applyAsInt(90, 7) != 2 || tier.applyAsInt(90, -3) != 2) {
            helper.fail("bands do not clamp: rising all the way reached " + last + ", from 7 -> "
                    + tier.applyAsInt(90, 7) + ", from -3 -> " + tier.applyAsInt(90, -3));
            return;
        }
        helper.succeed();
    }

    /**
     * The wiring: whatever the host's own situation turns out to be, the number that reaches the grid node
     * must be the baseline times what {@code MachineMood} says for those same inputs. This passes for a
     * host with a face and for one without, and fails if the two ever disagree.
     */
    @GameTest(template = "l1_room", batch = "f1_mood_wiring", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftedHostIdlePowerMatchesItsOwnMood(GameTestHelper helper) {
        var controllerPos = new BlockPos(6, 3, 6);
        buildL1Room(helper, controllerPos, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), host -> {
                    if (!(host instanceof cn.dancingsnow.neoecoprototype.blockentity.crafting
                            .SimplifyCraftingSystemBlockEntity f1)) {
                        helper.fail("the F1 host is not our block entity: " + host.getClass());
                        return;
                    }
                    // One mood sample, then read what actually reached the node.
                    helper.runAfterDelay(25, () -> {
                        int capacity = f1.getDisplayedCoolantCapacity();
                        double coolant = capacity > 0
                                ? (double) f1.getDisplayedCoolantAmount() / capacity : 0.0D;
                        double expected = cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile.L1
                                .craftingControllerIdlePower()
                                * cn.dancingsnow.neoecoprototype.api.MachineMood.idlePowerFactor(
                                f1.hasMind(), coolant, f1.getRunningThreadCount() > 0, f1.isFormed(),
                                cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                                        .MIND_IDLE_POWER_SAVING.get());
                        double actual = f1.getMainNode().getNode().getIdlePowerUsage();
                        if (Math.abs(actual - expected) > 1.0E-6D) {
                            helper.fail("the grid node carries an idle draw of " + actual
                                    + " AE but the host's own situation says " + expected + " (mind="
                                    + f1.hasMind() + " coolant=" + coolant + " busy="
                                    + (f1.getRunningThreadCount() > 0) + ")");
                            return;
                        }
                        if (actual <= 0.0D) {
                            helper.fail("the host published an idle draw of " + actual
                                    + " AE, so it is free to run and the mood is not the interesting"
                                    + " part here");
                            return;
                        }
                        // The gate on the visible half: no face, no band, whatever the coolant says.
                        int tier = helper.getLevel().getBlockState(f1.getBlockPos())
                                .getValue(cn.dancingsnow.neoecoprototype.block.crafting
                                        .SimplifyCraftingSystemBlock.MIND_TIER);
                        if (!f1.hasMind() && tier != 0) {
                            helper.fail("a host without a mind is showing face band " + tier
                                    + ", so the discount and the face are not gated together");
                            return;
                        }
                        helper.succeed();
                    });
                });
    }

    /**
     * The dissociator's panel is an LDLib2 block UI, and LDLib2 builds that UI on the server too - so this
     * runs the same construction a right-click does, in a JVM with no client at all. It is the only cover
     * the panel has: a widget that touches client state while being built, or a slot whose inventory view
     * is the wrong shape, fails here rather than only on somebody's desktop.
     */
    @GameTest(template = "empty", batch = "dissociator_ui", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociatorPanelBuildsWithoutAClient(GameTestHelper helper) {
        var plotPos = new BlockPos(0, 1, 0);
        helper.setBlock(plotPos, ModRegistration.METEORITE_DISSOCIATOR_BLOCK.get());
        helper.runAfterDelay(5, () -> {
            var absolute = helper.absolutePos(plotPos);
            var state = helper.getLevel().getBlockState(absolute);
            if (!(state.getBlock()
                    instanceof com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUI blockUI)) {
                helper.fail("the dissociator block does not implement BlockUI, so right-click has"
                        + " nothing to open: " + state.getBlock());
                return;
            }
            if (!(helper.getLevel().getBlockEntity(absolute)
                    instanceof cn.dancingsnow.neoecoprototype.blockentity.dissociation
                    .MeteoriteDissociatorBlockEntity host)) {
                helper.fail("the dissociator was placed without its own block entity");
                return;
            }
            var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
            var holder = new com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType.BlockUIHolder(
                    blockUI, player, absolute, state);
            try {
                if (blockUI.createUI(holder) == null) {
                    helper.fail("right-clicking the dissociator would hand LDLib a null UI");
                    return;
                }
                // The panel reads three numbers off the machine; if the run state stopped answering, the
                // groove and the baths would freeze at zero with no error anywhere.
                if (host.progressFraction() != 0.0F || host.getCoolantTank().getFluidAmount() != 0) {
                    helper.fail("a freshly placed dissociator reports progress " + host.progressFraction()
                            + " and " + host.getCoolantTank().getFluidAmount() + " mB of coolant");
                    return;
                }
            } catch (RuntimeException failure) {
                helper.fail("building the dissociator panel threw " + failure + ", first frames: "
                        + java.util.Arrays.toString(java.util.Arrays.copyOf(
                                failure.getStackTrace(), Math.min(6, failure.getStackTrace().length))));
                return;
            }
            helper.succeed();
        });
    }

    /** True when {@code pos} is inside the template and holds nothing. */
    /**
     * Stands an AE2 controller on the first free face-adjacent cell of {@code at} and taps it with the card's
     * own grant call. Idempotent so it can sit inside a poll: the second and later visits find the controller
     * already there and only ask whether the network is up yet. Returning false while the controller has no
     * node is the point - the budget is a property of a live network, not of a coordinate.
     */
    /** Clears whichever face cell {@link #grantBudgetBeside} ended up choosing, and says which one that was. */
    private static BlockPos takeControllerBeside(GameTestHelper helper, BlockPos at) {
        for (var side : CONTROLLER_SIDES) {
            var cell = at.relative(side);
            if (helper.getLevel().getBlockState(helper.absolutePos(cell))
                    .getBlock() == appeng.core.definitions.AEBlocks.CONTROLLER.block()) {
                helper.setBlock(cell, Blocks.AIR);
                return cell;
            }
        }
        return null;
    }

    /**
     * One tap on the hub, then one tap per device: the mark survives a join, so wiring a hub to several
     * spokes costs one re-mark instead of one per spoke. The brake is not the gesture count but the channel
     * allowance each join spends, which is why this asserts both halves - the surviving mark, and the escape
     * hatch that only exists because of it (clicking the held device drops every link on it).
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkCardKeepsItsMarkUntilTheHubIsClickedAgain(GameTestHelper helper) {
        var hub = new BlockPos(0, 1, 0);
        var spokeOne = new BlockPos(0, 1, 8);
        var spokeTwo = new BlockPos(8, 1, 0);
        helper.setBlock(hub, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(spokeOne, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        helper.setBlock(spokeTwo, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var hubPos = GlobalPos.of(level.dimension(), helper.absolutePos(hub));
        var onePos = GlobalPos.of(level.dimension(), helper.absolutePos(spokeOne));
        var twoPos = GlobalPos.of(level.dimension(), helper.absolutePos(spokeTwo));
        waitUntil(helper, 25,
                () -> separateNetworks(level, hubPos, onePos) && separateNetworks(level, hubPos, twoPos)
                        && grantBudgetBeside(helper, level, hub),
                () -> "the three devices never became three separate nodes, or the hub's network would not buy"
                        + " an allowance: " + nodeSummary(level, hubPos) + " / "
                        + nodeSummary(level, onePos) + " / " + nodeSummary(level, twoPos),
                () -> {
                    // Half one is the shipped state: off means the card must not even touch the ledger, and
                    // it answers PASS so the device under the cursor behaves like any other item in hand.
                    var flag = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS;
                    if (cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS_DEFAULT) {
                        helper.fail("the wireless feature ships enabled, but this release promises it is off");
                        return;
                    }
                    if (flag.get()) {
                        helper.fail("the server config reads on at test start, so the off-half below is not the"
                                + " default it claims to be");
                        return;
                    }
                    int start = linksOf(WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos));
                    var player = helper.makeMockPlayer(GameType.CREATIVE);
                    player.setShiftKeyDown(true);
                    var card = new ItemStack(ModRegistration.WIRELESS_LINK_CARD.get());
                    tapWithCard(helper, player, card, hub);
                    if (linksOf(WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos)) != start) {
                        helper.fail("an off-by-default card still recorded something on the hub: " + start
                                + " -> " + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos));
                        return;
                    }
                    flag.set(true);
                    try {
                        tapWithCard(helper, player, card, hub);
                        tapWithCard(helper, player, card, spokeOne);
                        String afterOne = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos);
                        if (linksOf(afterOne) != start + 1) {
                            helper.fail("the item's own two taps did not make exactly one link: " + afterOne);
                            return;
                        }
                        tapWithCard(helper, player, card, spokeTwo);
                        String afterTwo = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos);
                        int spokeAfterBind = linksOf(WirelessLinks.status(level,
                                ModRegistration.WIRELESS_LINKS, onePos));
                        if (linksOf(afterTwo) != start + 2) {
                            helper.fail("the mark did not survive the first join, so tapping the second device"
                                    + " marked it instead of joining it: " + afterOne + " -> " + afterTwo);
                            return;
                        }
                        tapWithCard(helper, player, card, hub);
                        String cleared = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, hubPos);
                        if (linksOf(cleared) > start || linksOf(WirelessLinks.status(level,
                                ModRegistration.WIRELESS_LINKS, onePos)) >= spokeAfterBind) {
                            helper.fail("clicking the device the card still holds was expected to drop every"
                                    + " link on it: hub " + afterTwo + " -> " + cleared + ", spoke was "
                                    + spokeAfterBind + " and is now "
                                    + linksOf(WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, onePos)));
                            return;
                        }
                        helper.succeed();
                    } finally {
                        flag.set(false);
                    }
                });
    }

    /** Sneak-right-clicks {@code cell} with the card so the item's own anchor logic runs, not our calls. */
    private static void tapWithCard(GameTestHelper helper, net.minecraft.world.entity.player.Player player,
                                    ItemStack card, BlockPos cell) {
        var absolute = helper.absolutePos(cell);
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(absolute),
                Direction.UP, absolute, false);
        card.getItem().useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND, card, hit));
    }

    /** The record count out of a status line; -1 when the line does not carry one. */
    private static int linksOf(String status) {
        var matched = java.util.regex.Pattern.compile("wirelessLinks=(\\d+)").matcher(status);
        return matched.find() ? Integer.parseInt(matched.group(1)) : -1;
    }

    /** Reads the {@code channels=spent/granted} pair out of a status line; null when the line says unknown. */
    private static int[] ledgerOf(String status) {
        var matched = java.util.regex.Pattern.compile("channels=(\\d+)/(\\d+)").matcher(status);
        return matched.find()
                ? new int[]{Integer.parseInt(matched.group(1)), Integer.parseInt(matched.group(2))}
                : null;
    }

    private static boolean grantBudgetBeside(GameTestHelper helper, ServerLevel level, BlockPos at) {
        for (var side : CONTROLLER_SIDES) {
            var cell = at.relative(side);
            if (helper.getLevel().getBlockState(helper.absolutePos(cell))
                    .getBlock() == appeng.core.definitions.AEBlocks.CONTROLLER.block()) {
                var answer = WirelessLinks.grant(level, ModRegistration.WIRELESS_LINKS,
                        GlobalPos.of(level.dimension(), helper.absolutePos(cell)));
                if (answer.startsWith("granted") || answer.startsWith("already granted")) {
                    return true;
                }
                NeoECOPrototype.LOGGER.info("fixture grant not yet: {}", answer);
                return false;
            }
        }
        for (var side : CONTROLLER_SIDES) {
            var cell = at.relative(side);
            if (!helper.getLevel().getBlockState(helper.absolutePos(cell)).isAir()) {
                continue;
            }
            helper.setBlock(cell, appeng.core.definitions.AEBlocks.CONTROLLER.block());
            return false;
        }
        helper.fail("no air cell beside " + at + " to stand a controller in,"
                + " so this fixture cannot buy the budget a bind now needs");
        return false;
    }

    /** Horizontal faces first: a controller under a machine would sit in the room's floor. */
    private static final net.minecraft.core.Direction[] CONTROLLER_SIDES = {
            net.minecraft.core.Direction.SOUTH, net.minecraft.core.Direction.NORTH,
            net.minecraft.core.Direction.EAST, net.minecraft.core.Direction.WEST,
            net.minecraft.core.Direction.UP};

    private static boolean freeTemplateCell(GameTestHelper helper, BlockPos pos) {
        if (pos.getX() < 0 || pos.getY() < 0 || pos.getZ() < 0 || pos.getX() >= L1_ROOM_SIZE
                || pos.getY() >= L1_ROOM_SIZE || pos.getZ() >= L1_ROOM_SIZE) {
            return false;
        }
        return helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir();
    }

    /**
     * The face is a once-per-host decision, not a once-per-check one: geometry validation re-runs on
     * neighbour changes, chunk loads and rebuilds, so a per-check roll would flip the model back and
     * forth instead of staying rare. This asserts the roll happened and that further checks leave it
     * alone - the property the 1/16 rate is defined against.
     */
    @GameTest(template = "l1_room", batch = "f1_mind_once", timeoutTicks = 200, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingHostRollsItsFaceOnceAndNeverAgain(GameTestHelper helper) {
        var controllerPos = new BlockPos(6, 3, 6);
        buildL1Room(helper, controllerPos, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), host -> {
                    if (!(host instanceof cn.dancingsnow.neoecoprototype.blockentity.crafting
                            .SimplifyCraftingSystemBlockEntity f1)) {
                        helper.fail("the F1 host is not our block entity: " + host.getClass());
                        return;
                    }
                    if (!f1.mindRolled()) {
                        helper.fail("the F1 host formed without rolling for its face, so the blockstate "
                                + "can never show one");
                        return;
                    }
                    var rolled = f1.hasMind();
                    for (int i = 0; i < 8; i++) {
                        f1.updateState(false);
                    }
                    if (f1.hasMind() != rolled) {
                        helper.fail("repeated checks re-rolled the face (" + rolled + " -> "
                                + f1.hasMind() + "), so the chance is per-check, not per host");
                        return;
                    }
                    var state = helper.getLevel().getBlockState(f1.getBlockPos());
                    if (state.getValue(SimplifyCraftingSystemBlock.HAS_MIND) != (rolled && f1.isFormed())) {
                        helper.fail("the host block state says has_mind=" + state.getValue(
                                SimplifyCraftingSystemBlock.HAS_MIND) + " but the machine reports hasMind="
                                + rolled + " formed=" + f1.isFormed());
                    }
                });
    }

    /** The name a blockstate key spells for this property's value; toString() is not it. */
    @SuppressWarnings({"unchecked", "rawtypes"})
    private static String valueName(net.minecraft.world.level.block.state.BlockState state,
                                    net.minecraft.world.level.block.state.properties.Property property) {
        return property.getName((Comparable) state.getValue(property));
    }

    /**
     * Every state the host can be in must resolve to a model, or that state renders as nothing at all
     * -- and the states that go missing are the rare ones nobody clicks through by hand. The hosts now
     * carry up to eight properties, so this checks the shipped blockstate against the whole product
     * rather than against the handful a person would think to look at.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationHostBlockstateCoversEveryState(GameTestHelper helper) {
        assertVariantsCoverEveryState(helper, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                "blockstates/simplify_computation_system.json");
    }

    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void storageHostBlockstateCoversEveryState(GameTestHelper helper) {
        assertVariantsCoverEveryState(helper, ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                "blockstates/simplify_storage_controller.json");
    }

    /**
     * The drive is the one block whose shipped blockstate came from outside this repository, and its
     * keys arrived with a space after every comma -- which the game does not trim, so all twelve keys
     * were dropped and the drive quietly kept the previous file. Coverage alone would not have caught
     * that, which is why the helper also rejects any key naming a property the block lacks.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void driveBlockstateCoversEveryState(GameTestHelper helper) {
        assertVariantsCoverEveryState(helper, ModRegistration.SIMPLIFY_DRIVE_BLOCK.get(),
                "blockstates/simplify_drive.json");
    }

    /**
     * Six blocks ship a single catch-all key rather than one entry per state: they enumerated all four
     * facings although one drawing covers every side -- a full cube with six cullfaces, {@code cube_all},
     * or a doll whose block entity renderer reads the facing off the state itself and needs no per-facing
     * model at all. One test walks all six rather than six tests each walking one: every extra plot
     * shifts where the framework parks
     * the other concurrent tests, and the L1 builders collide when two controllers end up seven blocks
     * apart -- which showed up here as the whole suite wedging, not as a failure.
     */
    @GameTest(template = "empty", batch = "blockstate_catchall", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void collapsedBlockstatesStillCoverEveryState(GameTestHelper helper) {
        var files = new Object[][]{
                {ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get(), "energized_computation_core"},
                {ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get(),
                        "energized_computation_threading_core"},
                {ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get(), "simplify_trinity_storage_module"},
                {ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get(),
                        "simplify_trinity_computation_module"},
                {ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get(),
                        "simplify_trinity_crafting_module"},
        };
        var problems = new ArrayList<String>();
        for (var entry : files) {
            var block = (Block) entry[0];
            var path = "blockstates/" + entry[1] + ".json";
            var text = readShippedResource(
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path));
            if (text == null) {
                problems.add(path + ": unreadable");
                continue;
            }
            var variants = com.google.gson.JsonParser.parseString(text).getAsJsonObject()
                    .getAsJsonObject("variants");
            if (variants == null) {
                problems.add(path + ": no variants object");
                continue;
            }
            var keys = new ArrayList<java.util.Map<String, String>>();
            var badKey = new ArrayList<String>();
            for (var variant : variants.entrySet()) {
                var pairs = new java.util.HashMap<String, String>();
                for (var pair : variant.getKey().split(",")) {
                    if (pair.isEmpty()) {
                        continue;
                    }
                    var kv = pair.split("=", 2);
                    var property = kv.length == 2 ? block.getStateDefinition().getProperty(kv[0]) : null;
                    if (property == null || property.getValue(kv[1]).isEmpty()) {
                        badKey.add(path + ": key \"" + variant.getKey() + "\" is not usable by "
                                + name(block));
                    }
                    pairs.put(kv[0], kv[1]);
                }
                keys.add(pairs);
            }
            if (!badKey.isEmpty()) {
                problems.add(badKey.get(0));
                continue;
            }
            for (var state : block.getStateDefinition().getPossibleStates()) {
                var described = new java.util.HashMap<String, String>();
                for (var property : state.getProperties()) {
                    described.put(property.getName(), valueName(state, property));
                }
                if (keys.stream().noneMatch(key -> described.entrySet().containsAll(key.entrySet()))) {
                    problems.add(path + ": " + described + " has no model");
                    break;
                }
            }
        }
        // No block may default one of its own booleans to true. A block that rebuilds its default state
        // from getStateDefinition().any() silently takes the first value of every property, and for a
        // BooleanProperty that is true -- which is how the C1 host was placed already carrying
        // formed=true, mirrored=true and eco's two switch bits, only for the cluster to correct it a
        // frame later. What the player saw was a flash of the formed face on a block that had never
        // formed. F1 was written against defaultBlockState() and never flickered.
        var badDefaults = new ArrayList<String>();
        for (var registered : BuiltInRegistries.BLOCK) {
            if (!NeoECOPrototype.MOD_ID.equals(BuiltInRegistries.BLOCK.getKey(registered).getNamespace())) {
                continue;
            }
            var owned = registered.defaultBlockState();
            for (var property : owned.getProperties()) {
                if (property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty flag
                        && owned.getValue(flag)) {
                    badDefaults.add(name(registered) + " defaults " + flag.getName() + "=true");
                }
            }
        }
        if (!badDefaults.isEmpty()) {
            helper.fail(badDefaults.size() + " of our blocks default a boolean to true, first: "
                    + badDefaults.get(0));
            return;
        }
        if (!problems.isEmpty()) {
            helper.fail(problems.size() + " collapsed blockstate(s) broken, first: " + problems.get(0));
            return;
        }
        helper.succeed();
    }

    /**
     * A blockstate row chooses a model by name, so a row can pin {@code mirrored=true} and still be
     * handed the un-mirrored artwork without anything complaining -- both files exist, both load, and the
     * machine simply renders the other hand. The C1 host did exactly that on the sixteen rows where the
     * mirrored flag and the communication interface disagreed: a mirrored machine drew the plain face, so
     * its 2x3 end cap landed on the interface side and the end the module line runs out of stayed open.
     * Every row that pins {@code formed=true} has to name the flags it pins.
     *
     * <p>Each convention is only enforced on a file that uses it, so this walks the shipped files rather
     * than a hand-written expectation -- which is also why it counts what it checked and refuses to pass
     * having checked nothing.
     */
    @GameTest(template = "empty", batch = "host_row_tokens", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void formedHostRowsNameTheirOwnFlags(GameTestHelper helper) {
        var files = new String[]{
                "blockstates/simplify_computation_system.json",
                "blockstates/simplify_computation_cooling_controller.json",
                "blockstates/simplify_storage_controller.json",
                "blockstates/simplify_crafting_system.json",
        };
        var conventions = new String[][]{
                {"mirrored", "mirrored"},
                {"communication_interface", "network"},
                {"energized_parallel_core", "energized"},
        };
        var problems = new ArrayList<String>();
        var checked = new int[]{0};
        for (var path : files) {
            var text = readShippedResource(
                    ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path));
            if (text == null) {
                problems.add(path + ": unreadable");
                continue;
            }
            var document = com.google.gson.JsonParser.parseString(text).getAsJsonObject();
            var flags = new ArrayList<java.util.Map<String, String>>();
            var models = new ArrayList<String>();
            if (document.has("variants")) {
                for (var entry : document.getAsJsonObject("variants").entrySet()) {
                    var pinned = new java.util.HashMap<String, String>();
                    for (var pair : entry.getKey().split(",")) {
                        if (pair.isEmpty()) {
                            continue;
                        }
                        var kv = pair.split("=", 2);
                        pinned.put(kv[0], kv.length == 2 ? kv[1] : "");
                    }
                    collectModels(entry.getValue(), pinned, flags, models);
                }
            } else if (document.has("multipart")) {
                for (var element : document.getAsJsonArray("multipart")) {
                    var part = element.getAsJsonObject();
                    var pinned = new java.util.HashMap<String, String>();
                    var when = part.get("when");
                    if (when != null && when.isJsonObject()) {
                        for (var clause : when.getAsJsonObject().entrySet()) {
                            if (!clause.getValue().isJsonArray()) {
                                pinned.put(clause.getKey(), clause.getValue().getAsString());
                            }
                        }
                    }
                    collectModels(part.get("apply"), pinned, flags, models);
                }
            } else {
                problems.add(path + ": neither variants nor multipart");
                continue;
            }
            for (var convention : conventions) {
                var property = convention[0];
                var token = convention[1];
                if (models.stream().noneMatch(model -> namesSegment(model, token))) {
                    continue;
                }
                for (var row = 0; row < flags.size(); row++) {
                    var pinned = flags.get(row);
                    if (!"true".equals(pinned.get("formed")) || !pinned.containsKey(property)) {
                        continue;
                    }
                    var wanted = "true".equals(pinned.get(property));
                    var model = models.get(row);
                    var published = namesSegment(model, token);
                    checked[0]++;
                    if (wanted != published) {
                        problems.add(path + " pins " + property + "=" + pinned.get(property)
                                + " for " + pinned + " but hands the row to " + model);
                    }
                }
            }
        }
        if (!problems.isEmpty()) {
            helper.fail(problems.size() + " formed row(s) render the wrong hand, first: " + problems.get(0));
            return;
        }
        if (checked[0] == 0) {
            helper.fail("no formed row pins any of the flags this guard knows about, so it checked "
                    + "nothing: " + java.util.Arrays.deepToString(conventions));
            return;
        }
        NeoECOPrototype.LOGGER.info("formed row / model-name agreement: {} rows checked across {} files",
                checked[0], files.length);
        helper.succeed();
    }

    /**
     * Whether a model's file name carries {@code segment} as one of its underscore-separated words. Name
     * matching rather than substring matching, because F1's mirrored artwork is
     * {@code controller_l1_formed_mirrored_face} -- the hand is in the middle of the name, and a plain
     * contains() would also let an unrelated word like {@code energized_network} borrow a match.
     */
    private static boolean namesSegment(String model, String segment) {
        for (var word : model.substring(model.lastIndexOf('/') + 1).split("_")) {
            if (word.equals(segment)) {
                return true;
            }
        }
        return false;
    }

    private static void collectModels(com.google.gson.JsonElement apply,
                                      java.util.Map<String, String> pinned,
                                      ArrayList<java.util.Map<String, String>> flags,
                                      ArrayList<String> models) {
        if (apply == null) {
            return;
        }
        if (apply.isJsonArray()) {
            for (var element : apply.getAsJsonArray()) {
                collectModels(element, pinned, flags, models);
            }
            return;
        }
        var model = apply.getAsJsonObject().get("model");
        if (model != null) {
            flags.add(pinned);
            models.add(model.getAsString());
        }
    }

    private static void assertVariantsCoverEveryState(GameTestHelper helper, Block block, String path) {
        var text = readShippedResource(ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, path));
        if (text == null) {
            helper.fail("the shipped blockstate at " + path + " could not be read");
            return;
        }
        var variants = com.google.gson.JsonParser.parseString(text).getAsJsonObject()
                .getAsJsonObject("variants");
        if (variants == null) {
            helper.fail(path + " has no variants object");
            return;
        }
        var entries = new ArrayList<java.util.Map<String, String>>();
        for (var entry : variants.entrySet()) {
            var pairs = new java.util.HashMap<String, String>();
            for (var pair : entry.getKey().split(",")) {
                if (pair.isEmpty()) {
                    continue;
                }
                var kv = pair.split("=", 2);
                if (kv.length != 2) {
                    helper.fail("unparsable blockstate key \"" + entry.getKey() + "\"");
                    return;
                }
                // A key that names a property the block does not have is dropped with a single warning
                // line, and the state quietly keeps whatever a lower-priority pack or an older file
                // provided -- so coverage alone can pass while the intended split never happens. A
                // space after the comma is the usual cause, since the game does not trim the name.
                var property = block.getStateDefinition().getProperty(kv[0]);
                if (property == null) {
                    helper.fail("key \"" + entry.getKey() + "\" in " + path + " names property \""
                            + kv[0] + "\", which " + name(block) + " does not have; the game drops the "
                            + "whole key and the state keeps some other model");
                    return;
                }
                if (property.getValue(kv[1]).isEmpty()) {
                    helper.fail("key \"" + entry.getKey() + "\" in " + path + " gives \"" + kv[1]
                            + "\" for " + property.getName() + ", which is not one of its values");
                    return;
                }
                pairs.put(kv[0], kv[1]);
            }
            entries.add(pairs);
        }

        var uncovered = new ArrayList<String>();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            var wanted = new java.util.HashMap<String, String>();
            for (var property : state.getProperties()) {
                wanted.put(property.getName(), valueName(state, property));
            }
            final var described = wanted;
            if (entries.stream().noneMatch(pairs -> described.entrySet().containsAll(pairs.entrySet()))) {
                if (uncovered.isEmpty()) {
                    NeoECOPrototype.LOGGER.info("blockstate coverage probe: state={} firstFileEntry={}",
                            described, entries.get(0));
                }
                uncovered.add(described.toString());
            }
        }
        helper.assertTrue(uncovered.isEmpty(),
                uncovered.size() + " of " + block.getStateDefinition().getPossibleStates().size()
                        + " states of " + name(block) + " have no model and would render invisible; "
                        + "first few: " + uncovered.subList(0, Math.min(3, uncovered.size())));
        helper.succeed();
    }

    /**
     * A multipart file selects by matching clauses, so a state that matches two of them draws two
     * models in one cell -- which reads as z-fighting, not as a configuration error. Every state of the
     * F1 host has to match exactly one clause, and the shape of the clauses is checked here rather
     * than in game because nothing else walks the product of six properties.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingHostBlockstateMatchesExactlyOneTermPerState(GameTestHelper helper) {
        var text = readShippedResource(ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                "blockstates/simplify_crafting_system.json"));
        if (text == null) {
            helper.fail("the shipped blockstate for the F1 host could not be read");
            return;
        }
        var terms = com.google.gson.JsonParser.parseString(text).getAsJsonObject()
                .getAsJsonArray("multipart");
        if (terms == null) {
            helper.fail("the F1 host blockstate has no multipart array");
            return;
        }
        var clauses = new ArrayList<java.util.Map<String, String>>();
        for (var element : terms) {
            var when = element.getAsJsonObject().getAsJsonObject("when");
            if (when == null) {
                helper.fail("a multipart term of the F1 host blockstate has no when clause");
                return;
            }
            var clause = new java.util.HashMap<String, String>();
            for (var member : when.entrySet()) {
                if ("AND".equals(member.getKey()) || "OR".equals(member.getKey())
                        || "NOT".equals(member.getKey())) {
                    helper.fail("this test only reads flat when clauses; " + member.getKey()
                            + " would need matching semantics");
                    return;
                }
                clause.put(member.getKey(), member.getValue().getAsString());
            }
            clauses.add(clause);
        }

        var block = ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get();
        var wrong = new ArrayList<String>();
        for (var state : block.getStateDefinition().getPossibleStates()) {
            var described = new java.util.HashMap<String, String>();
            for (var property : state.getProperties()) {
                described.put(property.getName(), valueName(state, property));
            }
            long matched = clauses.stream()
                    .filter(clause -> described.entrySet().containsAll(clause.entrySet()))
                    .count();
            if (matched != 1) {
                if (wrong.isEmpty()) {
                    NeoECOPrototype.LOGGER.info("multipart probe: state={} matched={} firstClause={}",
                            described, matched, clauses.get(0));
                }
                wrong.add(described + " x" + matched);
            }
        }
        helper.assertTrue(wrong.isEmpty(),
                wrong.size() + " of " + block.getStateDefinition().getPossibleStates().size()
                        + " states of the F1 host do not match exactly one multipart term; first few: "
                        + wrong.subList(0, Math.min(3, wrong.size())));
        helper.succeed();
    }

    /**
     * Only the cell nearest the controller takes the energized threading core, which is what caps a
     * structure at one of them: anywhere else the line stops matching and the subsystem will not form.
     */
    @GameTest(template = "l1_room", batch = "l1_threading_second", timeoutTicks = 140, templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedThreadingCoreOutsideTheFirstSlotIsRejected(GameTestHelper helper) {
        buildComputationStructure(helper, new BlockPos(13, 3, 4),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get(), 1, false, 2,
                controller -> {
                    helper.assertTrue(!controller.isFormed(),
                            "the threading line accepted the energized core in its second slot; the "
                                    + "\"only one, nearest the controller\" rule is not being enforced");
                });
    }

    /**
     * L1's computation numbers are read from the server config so a pack can buff the tier without
     * adding a member. This checks the tier really forwards to the config: a getter that kept returning
     * the enum constant would pass every other test and still ignore the file. It also pins the
     * energized cell to its own config entry and to L1's tier index, which is what lets an L1 frame
     * mount it at all.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void l1ComputationNumbersComeFromConfig(GameTestHelper helper) {
        var plain = SimplifyTier.L1;
        var reinforced = SimplifyTier.L1_REINFORCED;

        helper.assertTrue(plain.getCPUThreads() == NeoECOPrototypeServerConfig.L1_CPU_THREADS.get(),
                "the L1 threading core should report the configured "
                        + NeoECOPrototypeServerConfig.L1_CPU_THREADS.get() + " threads, got "
                        + plain.getCPUThreads());
        helper.assertTrue(plain.getCPUAccelerators()
                        == NeoECOPrototypeServerConfig.L1_CPU_ACCELERATORS.get(),
                "the L1 parallel core should report the configured "
                        + NeoECOPrototypeServerConfig.L1_CPU_ACCELERATORS.get() + " co-processors, got "
                        + plain.getCPUAccelerators());
        helper.assertTrue(plain.getCPUTotalBytes() == NeoECOPrototypeServerConfig.L1_CPU_TOTAL_BYTES.get(),
                "an L1 cell should report the configured "
                        + NeoECOPrototypeServerConfig.L1_CPU_TOTAL_BYTES.get() + " bytes, got "
                        + plain.getCPUTotalBytes());
        helper.assertTrue(reinforced.getCPUTotalBytes()
                        == NeoECOPrototypeServerConfig.ENERGIZED_CELL_TOTAL_BYTES.get(),
                "the energized cell should report the configured "
                        + NeoECOPrototypeServerConfig.ENERGIZED_CELL_TOTAL_BYTES.get() + " bytes, got "
                        + reinforced.getCPUTotalBytes());
        helper.assertTrue(reinforced.getTier() == plain.getTier() && plain.supportsComponentTier(reinforced),
                "an L1 frame must still mount the bigger cell");
        helper.assertTrue(ModRegistration.ENERGIZED_COMPUTATION_CELL_4M.get().getTier() == reinforced,
                "the 4M cell item reports " + ModRegistration.ENERGIZED_COMPUTATION_CELL_4M.get().getTier()
                        + " instead of the reinforced tier");
        helper.succeed();
    }

    /**
     * Blocks find their models from the registry name, so no code points at those files: a typo or a
     * deleted model shows up in game as the missing-texture cube and in a compile as nothing at all.
     * Walk every block we register and check the blockstate, each model it names, and the item model of
     * its BlockItem.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void everyBlockShipsItsModels(GameTestHelper helper) {
        var missing = new ArrayList<String>();
        for (var block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!NeoECOPrototype.MOD_ID.equals(id.getNamespace())) {
                continue;
            }
            var blockstate = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                    "blockstates/" + id.getPath() + ".json");
            var document = readShippedResource(blockstate);
            if (document == null) {
                missing.add("blockstate " + blockstate);
                continue;
            }
            // A formed model that ships in a blockstate still has to be reachable: a block that reports
            // INVISIBLE once formed never draws it, so the artwork is dead and nobody notices in a compile.
            if (document.contains("formed=true")
                    && block.defaultBlockState()
                    .hasProperty(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED)
                    && block.defaultBlockState()
                    .setValue(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED, true)
                    .getRenderShape() == net.minecraft.world.level.block.RenderShape.INVISIBLE) {
                missing.add(id + " ships a formed=true model in its blockstate but renders INVISIBLE once"
                        + " formed, so that model can never be drawn");
            }
            for (var matcher = MODEL_REFERENCE.matcher(document); matcher.find();) {
                var model = ResourceLocation.tryParse(matcher.group(1));
                if (model == null || !NeoECOPrototype.MOD_ID.equals(model.getNamespace())) {
                    continue; // vanilla and AE2 models come from their own packs
                }
                var modelFile = ResourceLocation.fromNamespaceAndPath(model.getNamespace(),
                        "models/" + model.getPath() + ".json");
                if (readShippedResource(modelFile) == null) {
                    missing.add(modelFile + " referenced by " + id.getPath());
                }
            }
            if (BuiltInRegistries.ITEM.get(id) instanceof BlockItem) {
                var itemModel = ResourceLocation.fromNamespaceAndPath(id.getNamespace(),
                        "models/item/" + id.getPath() + ".json");
                if (readShippedResource(itemModel) == null) {
                    missing.add("item model " + itemModel);
                }
            }
        }
        helper.assertTrue(missing.isEmpty(), "unreferenced-or-missing block models: " + missing);
        helper.succeed();
    }

    private static final Pattern MODEL_REFERENCE = Pattern.compile("\"model\"\\s*:\\s*\"([^\"]+)\"");

    /** The contents of a resource we ship, or null when it is not there. */
    private static String readShippedResource(ResourceLocation location) {
        var path = "/assets/" + location.getNamespace() + "/" + location.getPath();
        try (var resource = NeoECOPrototype.class.getResourceAsStream(path)) {
            return resource == null ? null : new String(resource.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            return null;
        }
    }

    /** A guide page's own links are relative to its folder; its {@code parent} is relative to the pack's guide root. */
    private static final Pattern GUIDE_LINK = Pattern.compile("\\]\\(([^)\\s]+\\.md)");
    private static final Pattern GUIDE_PARENT = Pattern.compile("^[ \\t]*parent:[ \\t]*(\\S+\\.md)",
            Pattern.MULTILINE);

    /**
     * Both packs we ship have to be self-contained. A model that names a texture nobody carries shows up
     * in game as the missing-texture cube and in a compile as nothing at all, and the artwork arrives as
     * Blockbench exports, where renaming one file silently breaks every model that names it -- which is the
     * exact risk the {@code l4} to {@code l1} rename ran through, and the reason it could be done at all.
     *
     * <p>Guide book pages ride the same rule and are pure text, so the compiler cannot see a broken link
     * either: dropping a page while its entry stays in the index leaves a page in the book that opens to
     * nothing.
     *
     * <p>Each reference is resolved inside the pack that carries the file making it. Resolving everything
     * against the live tree instead calls 21 of the fallback pack's models missing, because it ships its own
     * copies of the artwork it names on purpose.
     */
    @GameTest(template = "empty", batch = "asset_references", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void shippedAssetReferencesResolve(GameTestHelper helper) {
        var packs = List.of("assets/" + NeoECOPrototype.MOD_ID,
                "legacy_art/assets/" + NeoECOPrototype.MOD_ID);
        var problems = new ArrayList<String>();
        var jsonFiles = new int[]{0};
        var mdFiles = new int[]{0};
        var checked = new int[]{0};
        for (var pack : packs) {
            java.util.SortedSet<String> present;
            try {
                present = shippedFilesUnder(pack);
            } catch (java.io.IOException failure) {
                helper.fail("could not enumerate " + pack + " on the classpath: " + failure);
                return;
            }
            if (present.isEmpty()) {
                var classpath = System.getProperty("java.class.path");
                helper.fail("found nothing under " + pack + ", so this guard checked nothing; classpath="
                        + classpath.substring(0, Math.min(200, classpath.length())));
                return;
            }
            for (var path : present) {
                if (path.endsWith(".md") && path.contains("/ae2guide/")) {
                    mdFiles[0]++;
                    var page = readClasspathResource("/" + path);
                    if (page == null) {
                        problems.add(path + ": enumerated but not readable");
                        continue;
                    }
                    var ownFolder = path.substring(0, path.lastIndexOf('/') + 1);
                    var link = GUIDE_LINK.matcher(page);
                    while (link.find()) {
                        var target = link.group(1).split("#")[0];
                        if (target.isEmpty()) {
                            continue;
                        }
                        checked[0]++;
                        if (!present.contains(ownFolder + target)) {
                            problems.add(path + " links to " + target + ", no " + ownFolder + target);
                        }
                    }
                    var parent = GUIDE_PARENT.matcher(page);
                    if (parent.find()) {
                        checked[0]++;
                        var wanted = guideRoot(path) + parent.group(1);
                        if (!present.contains(wanted)) {
                            problems.add(path + " names parent " + parent.group(1) + ", no " + wanted);
                        }
                    }
                    continue;
                }
                if (!path.endsWith(".json")) {
                    continue;
                }
                jsonFiles[0]++;
                var text = readClasspathResource("/" + path);
                if (text == null) {
                    problems.add(path + ": enumerated but not readable");
                    continue;
                }
                com.google.gson.JsonElement document;
                try {
                    document = com.google.gson.JsonParser.parseString(text);
                } catch (com.google.gson.JsonSyntaxException failure) {
                    problems.add(path + ": does not parse, " + failure.getMessage());
                    continue;
                }
                var references = new ArrayList<String[]>();
                collectAssetReferences(document, references);
                for (var reference : references) {
                    var wanted = pack + "/" + ("texture".equals(reference[0]) ? "textures/" : "models/")
                            + reference[1] + ("texture".equals(reference[0]) ? ".png" : ".json");
                    checked[0]++;
                    if (!present.contains(wanted)) {
                        problems.add(path + " names " + reference[0] + " " + NeoECOPrototype.MOD_ID + ":"
                                + reference[1] + ", no " + wanted);
                    }
                }
            }
        }
        if (jsonFiles[0] == 0 || mdFiles[0] == 0 || checked[0] == 0) {
            helper.fail("walked " + jsonFiles[0] + " json and " + mdFiles[0] + " guide file(s) and resolved "
                    + checked[0] + " reference(s), so this guard saw nothing it could check");
            return;
        }
        if (!problems.isEmpty()) {
            helper.fail(problems.size() + " asset reference(s) name a file that is not shipped, first: "
                    + problems.get(0));
            return;
        }
        guiSlotRowsMatchTheirTextures(helper);
        NeoECOPrototype.LOGGER.info("asset references resolved inside their own pack: {} of {} json file(s),"
                + " {} guide page(s)", checked[0], jsonFiles[0], mdFiles[0]);
        helper.succeed();
    }

    /**
     * The three GUIs whose slot rows we own: each style JSON has to declare its rows exactly where its
     * texture draws them. AE2 and ExtendedAE both ship zero-error pairs, so the tolerance is none - a row
     * that moved 2px is the defect, not noise to allow for.
     */
    private static void guiSlotRowsMatchTheirTextures(GameTestHelper helper) {
        var patternSlots = new java.util.LinkedHashMap<String, Integer>();
        patternSlots.put("ENCODED_PATTERN",
                cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity.PATTERN_SLOTS);
        patternSlots.put("STORAGE", appeng.helpers.patternprovider.PatternProviderReturnInventory.NUMBER_OF_SLOTS);
        patternSlots.put("PLAYER_INVENTORY", net.minecraft.world.entity.player.Inventory.INVENTORY_SIZE
                - net.minecraft.world.entity.player.Inventory.getSelectionSize());
        patternSlots.put("PLAYER_HOTBAR", net.minecraft.world.entity.player.Inventory.getSelectionSize());
        var interfaceSlots = new java.util.LinkedHashMap<String, Integer>();
        interfaceSlots.put("CONFIG", cn.dancingsnow.neoecoprototype.blockentity.crafting
                .SimplifyPoweredMEInterfaceBlockEntity.MARKER_SLOTS);
        interfaceSlots.put("STORAGE", cn.dancingsnow.neoecoprototype.blockentity.crafting
                .SimplifyPoweredMEInterfaceBlockEntity.MARKER_SLOTS);
        interfaceSlots.put("PLAYER_INVENTORY", patternSlots.get("PLAYER_INVENTORY"));
        interfaceSlots.put("PLAYER_HOTBAR", patternSlots.get("PLAYER_HOTBAR"));
        var oversized = new java.util.LinkedHashMap<String, Integer>(interfaceSlots);
        oversized.put("CONFIG", cn.dancingsnow.neoecoprototype.blockentity.crafting
                .SimplifySuperconductiveInterfaceBlockEntity.MARKER_SLOTS);
        oversized.put("STORAGE", cn.dancingsnow.neoecoprototype.blockentity.crafting
                .SimplifySuperconductiveInterfaceBlockEntity.MARKER_SLOTS);
        var shifted = java.util.Set.of("CONFIG", "STORAGE");
        for (var contract : List.of(
                new GuiContract("l1_pattern_provider", patternSlots, java.util.Set.of(), 8, 9),
                new GuiContract("l1_powered_me_interface", interfaceSlots, shifted, 8, 9),
                new GuiContract("superconductive_interface", oversized, shifted, 8, 9))) {
            var problem = guiContractProblem(contract);
            if (problem != null) {
                helper.fail(contract.name() + ": " + problem);
                return;
            }
        }
    }

    /** A GUI style JSON and texture pair, with the slot counts the menu really hands each section. */
    private record GuiContract(String name, java.util.Map<String, Integer> slots,
                               java.util.Set<String> secondRowShift, int left, int columns) {
    }

    /** What is wrong with one GUI pair, or null when its declared rows sit on its drawn grooves. */
    private static String guiContractProblem(GuiContract contract) {
        var style = readClasspathResource("/assets/ae2/screens/neoecoprototype/" + contract.name() + ".json");
        if (style == null) {
            return "its GUI style JSON is not on the classpath";
        }
        var named = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.backgroundTexture(style);
        if (named == null) {
            return "its style declares no background texture";
        }
        // AE2 resolves an unqualified style texture inside its own namespace and under textures/, while a
        // qualified one is a full path - which is why the same key means two things across our four files.
        var colon = named.indexOf(':');
        var location = colon < 0 ? "ae2/textures/" + named
                : named.substring(0, colon) + "/" + named.substring(colon + 1);
        var texture = readClasspathBytes("/assets/" + (location.endsWith(".png") ? location : location + ".png"));
        if (texture == null) {
            return "names texture " + named + ", which is not on the classpath";
        }
        try {
            int pitch = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.ROW_PITCH;
            int bandLeft = contract.left();
            int bandRight = bandLeft + contract.columns() * pitch;
            var sections = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.sectionsFrom(style,
                    contract.slots(), contract.secondRowShift(),
                    cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.textureHeight(texture));
            var drawn = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.drawnGrooveTops(texture,
                    bandLeft, bandRight);
            if (drawn.isEmpty()) {
                return "the texture yielded 0 groove rows, so this contract checked nothing";
            }
            var declared = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.declaredTops(sections);
            var rows = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment.firstMismatch("row", declared, drawn);
            if (rows != null) {
                return rows;
            }
            var declaredColumns = new ArrayList<Integer>();
            for (int column = 0; column < contract.columns(); column++) {
                declaredColumns.add(bandLeft + column * pitch);
            }
            var drawnColumns = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment
                    .drawnGrooveLefts(texture, drawn.get(0), bandLeft, bandRight);
            if (drawnColumns.isEmpty()) {
                return "the texture yielded 0 groove columns in row " + drawn.get(0) + ", so this checked nothing";
            }
            var columns = cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment
                    .firstMismatch("column", declaredColumns, drawnColumns);
            if (columns != null) {
                return columns;
            }
            NeoECOPrototype.LOGGER.info("{}: {} declared slot row(s) and {} column(s) sit on the grooves"
                            + " the texture draws", contract.name(), declared.size(), drawnColumns.size());
            return null;
        } catch (java.io.IOException | IllegalArgumentException failure) {
            return "reading the pair threw " + failure;
        }
    }

    /** Raw bytes of a classpath resource, or null when it is not shipped. */
    private static byte[] readClasspathBytes(String path) {
        try (var stream = NeoECOPrototype.class.getResourceAsStream(path)) {
            return stream == null ? null : stream.readAllBytes();
        } catch (java.io.IOException failure) {
            return null;
        }
    }

    /**
     * The folder a page's front-matter paths are measured from: the pack's {@code ae2guide/} root, plus the
     * language override folder when the page sits in one.
     */
    private static String guideRoot(String pagePath) {
        var start = pagePath.indexOf("ae2guide/") + "ae2guide/".length();
        var root = pagePath.substring(0, start);
        return pagePath.startsWith("_zh_cn/", start) ? root + "_zh_cn/" : root;
    }

    /**
     * Every file under {@code folder} on the JVM classpath, keyed as {@code folder/<relative path>}. The
     * classpath is walked rather than the resource manager because a dedicated server never indexes
     * {@code assets/}, so the files a broken reference names are invisible to it.
     */
    private static java.util.SortedSet<String> shippedFilesUnder(String folder) throws java.io.IOException {
        var present = new java.util.TreeSet<String>();
        for (var entry : System.getProperty("java.class.path")
                .split(java.util.regex.Pattern.quote(java.io.File.pathSeparator))) {
            java.nio.file.Path root = java.nio.file.Path.of(entry).toAbsolutePath();
            java.nio.file.Path start = root.resolve(folder);
            if (java.nio.file.Files.isDirectory(start)) {
                try (var walk = java.nio.file.Files.walk(start)) {
                    for (var path : (Iterable<java.nio.file.Path>) walk
                            .filter(java.nio.file.Files::isRegularFile)::iterator) {
                        present.add(folder + "/" + start.relativize(path).toString()
                                .replace(java.io.File.separator, "/"));
                    }
                }
            } else if (entry.toLowerCase(java.util.Locale.ROOT).endsWith(".jar")
                    && java.nio.file.Files.isRegularFile(root)) {
                try (var zip = new java.util.zip.ZipFile(root.toFile())) {
                    for (var file : java.util.Collections.list(zip.entries())) {
                        if (file.getName().startsWith(folder + "/") && !file.isDirectory()) {
                            present.add(file.getName());
                        }
                    }
                }
            }
        }
        return present;
    }

    /** Our-namespace texture and model references anywhere in a blockstate, model or item JSON. */
    private static void collectAssetReferences(com.google.gson.JsonElement node, List<String[]> sink) {
        if (node == null) {
            return;
        }
        if (node.isJsonArray()) {
            for (var child : node.getAsJsonArray()) {
                collectAssetReferences(child, sink);
            }
            return;
        }
        if (!node.isJsonObject()) {
            return;
        }
        for (var member : node.getAsJsonObject().entrySet()) {
            var value = member.getValue();
            if ("textures".equals(member.getKey()) && value.isJsonObject()) {
                // The map binds keys to texture paths; "#key" values point back into the same map.
                for (var binding : value.getAsJsonObject().entrySet()) {
                    rememberReference(sink, "texture", binding.getValue());
                }
            } else if (("parent".equals(member.getKey()) || "model".equals(member.getKey())
                    || "apply".equals(member.getKey())) && value.isJsonObject()) {
                collectAssetReferences(value, sink);
            } else if ("parent".equals(member.getKey()) || "model".equals(member.getKey())
                    || "apply".equals(member.getKey())) {
                rememberReference(sink, "model", value);
            } else {
                collectAssetReferences(value, sink);
            }
        }
    }

    private static void rememberReference(List<String[]> sink, String kind, com.google.gson.JsonElement value) {
        if (value == null || !value.isJsonPrimitive() || !value.getAsJsonPrimitive().isString()) {
            return;
        }
        var location = ResourceLocation.tryParse(value.getAsString());
        if (location != null && NeoECOPrototype.MOD_ID.equals(location.getNamespace())) {
            sink.add(new String[]{kind, location.getPath()});
        }
    }

    private static String readClasspathResource(String path) {
        try (var stream = NeoECOPrototype.class.getResourceAsStream(path)) {
            return stream == null ? null : new String(stream.readAllBytes(),
                    java.nio.charset.StandardCharsets.UTF_8);
        } catch (java.io.IOException failure) {
            return null;
        }
    }

    /**
     * The ore is the world-side entry point for eco's 天外寒冰, which eco itself only ever crafts. Its
     * point is that it drops someone else's item, so an eco rename has to fail loudly here instead of
     * silently dropping air in a cave.
     */
    @GameTest(template = "empty", batch = "cryotheum_ore_drop", timeoutTicks = 160,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void cryotheumOreDropsTheEcoCrystal(GameTestHelper helper) {
        var crystal = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath("neoecoae",
                "cryotheum_crystal"));
        if (crystal == null || crystal == net.minecraft.world.item.Items.AIR) {
            helper.fail("neoecoae:cryotheum_crystal is not registered, so the ores drop nothing");
            return;
        }
        var ores = List.of(ModRegistration.NETHER_CRYOTHEUM_ORE_BLOCK.get(),
                ModRegistration.END_CRYOTHEUM_ORE_BLOCK.get(),
                ModRegistration.CRYOTHEUM_ORE_BLOCK.get());
        var breaker = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        for (var i = 0; i < ores.size(); i++) {
            helper.setBlock(new BlockPos(i, 1, 0), ores.get(i));
        }
        helper.runAfterDelay(2, () -> {
            // helper.destroyBlock drops nothing, so drive the drop path by hand.
            for (var i = 0; i < ores.size(); i++) {
                helper.getLevel().destroyBlock(helper.absolutePos(new BlockPos(i, 1, 0)), true, breaker);
            }
            helper.runAfterDelay(6, () -> {
                for (var i = 0; i < ores.size(); i++) {
                    helper.assertItemEntityPresent(crystal, new BlockPos(i, 1, 0), 2.0);
                }
                helper.succeed();
            });
        });
    }

    /**
     * Breaking the ore fills the counter powder snow uses; it does not damage anybody. The second ore in
     * the same tick must not stack on top of the first, because that is what a chain-mining mod turns
     * into a burst of freeze damage.
     *
     * <p>This drives {@link net.minecraft.world.level.block.Block#playerWillDestroy} directly: a game test
     * has no {@code ServerPlayer} to route a break through {@code ServerPlayerGameMode} with, and that
     * method is the one the real player-break path calls (verified in the 1.21.1 sources,
     * {@code ServerPlayerGameMode#destroyBlock}). What is pinned here is our behaviour, not vanilla's
     * dispatch.
     */
    @GameTest(template = "empty", batch = "cryotheum_ore_freeze", timeoutTicks = 140,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void cryotheumOreChillsTheBreakerOncePerTick(GameTestHelper helper) {
        var ore = ModRegistration.CRYOTHEUM_ORE_BLOCK.get();
        helper.setBlock(new BlockPos(0, 1, 0), ore);
        helper.setBlock(new BlockPos(1, 1, 0), ore);
        var breaker = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
        var level = helper.getLevel();
        ore.playerWillDestroy(level, helper.absolutePos(new BlockPos(0, 1, 0)),
                level.getBlockState(helper.absolutePos(new BlockPos(0, 1, 0))), breaker);
        var first = breaker.getTicksFrozen();
        if (first <= 0) {
            helper.fail("breaking the ore left the player at " + first + " freezing ticks");
            return;
        }
        ore.playerWillDestroy(level, helper.absolutePos(new BlockPos(1, 1, 0)),
                level.getBlockState(helper.absolutePos(new BlockPos(1, 1, 0))), breaker);
        helper.assertTrue(breaker.getTicksFrozen() == first,
                "a second ore in the same tick stacked the chill: " + first + " -> "
                        + breaker.getTicksFrozen() + ", which is what chain mining would multiply");
        helper.succeed();
    }

    /**
     * Generation is a config choice, not a datapack, so the predicate that makes it one is worth pinning:
     * an empty list means nowhere rather than everywhere, and a typo has to read as off.
     *
     * <p>The value a config file was loaded with is deliberately not asserted here - a file written by an
     * earlier run wins over the built-in default, so reading it would fail for reasons nobody can see. The
     * built-in default constant is asserted instead, which no local file can move.
     */
    @GameTest(template = "empty", batch = "cryotheum_ore_gate", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void cryotheumOreHonoursTheDimensionConfig(GameTestHelper helper) {
        var nether = net.minecraft.world.level.Level.NETHER;
        var end = net.minecraft.world.level.Level.END;
        var overworld = net.minecraft.world.level.Level.OVERWORLD;
        java.util.function.BiPredicate<net.minecraft.resources.ResourceKey<net.minecraft.world.level.Level>,
                List<? extends String>> gate =
                cn.dancingsnow.neoecoprototype.worldgen.ConfigGatedOreFeature::dimensionAllowed;
        helper.assertTrue(gate.test(nether, List.of("minecraft:the_nether")),
                "a dimension named by the config must be allowed");
        helper.assertTrue(!gate.test(overworld, List.of("minecraft:the_nether")),
                "a dimension the config does not name must place nothing");
        helper.assertTrue(gate.test(end, List.of("minecraft:the_end", "minecraft:overworld")),
                "a list with several entries must allow each of them");
        helper.assertTrue(!gate.test(nether, List.of("the_nether")),
                "a bare path without a namespace must not match, or a typo reads as enabled");
        helper.assertTrue(!gate.test(nether, List.<String>of()),
                "an empty list must switch generation off, not open it up");
        helper.assertTrue(cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                        .CRYOTHEUM_ORE_DIMENSIONS_DEFAULT.isEmpty(),
                "the built-in default now names a dimension, so a fresh install would generate the ores "
                        + "before the crystal family they lead to has any use");
        // The comet is the family's other way into a world, so "ships off" only holds if both are off.
        helper.assertTrue(!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                        .CRYOTHEUM_METEORITE_ENABLED_DEFAULT,
                "the comet generates by default, which would drop mother rock, buds and clusters into a "
                        + "fresh End for a family that still has no recipe");
        helper.succeed();
    }

    /**
     * Worldgen data that fails to parse is a log line nobody reads, and a biome that never asks for the
     * feature means the ore simply does not exist in the world. Check the files loaded, that every biome
     * entry names something real, and - the part that actually matters - that each dimension's biome got
     * its own ore on the ore generation step.
     */
    @GameTest(template = "empty", batch = "cryotheum_ore_data", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void cryotheumOreWorldgenDataIsLoaded(GameTestHelper helper) {
        var resourceManager = helper.getLevel().getServer().getResourceManager();
        var modifiers = List.of("cryotheum_ore_nether", "cryotheum_ore_end");
        var missing = new ArrayList<String>();
        var paths = new ArrayList<String>();
        for (var ore : List.of("nether_cryotheum_ore", "end_cryotheum_ore")) {
            paths.add("worldgen/configured_feature/" + ore + ".json");
            paths.add("worldgen/placed_feature/" + ore + ".json");
        }
        for (var modifier : modifiers) {
            paths.add("neoforge/biome_modifier/" + modifier + ".json");
        }
        for (var path : paths) {
            if (resourceManager.getResource(ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                    path)).isEmpty()) {
                missing.add(path);
            }
        }
        if (!missing.isEmpty()) {
            helper.fail("cryotheum worldgen data not found on the pack: " + missing);
            return;
        }
        if (!BuiltInRegistries.FEATURE.containsKey(
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "configurable_ore"))) {
            helper.fail("the configured features name a feature type that is not registered, so the files "
                    + "would be dropped at load");
            return;
        }
        var registries = helper.getLevel().registryAccess();
        var biomes = registries.registryOrThrow(net.minecraft.core.registries.Registries.BIOME);
        var unresolved = new ArrayList<String>();
        for (var modifier : modifiers) {
            var text = readClasspathResource("/data/neoecoprototype/neoforge/biome_modifier/"
                    + modifier + ".json");
            if (text == null) {
                helper.fail(modifier + " is on the pack but not on the classpath");
                return;
            }
            var entry = com.google.gson.JsonParser.parseString(text).getAsJsonObject()
                    .get("biomes").getAsString();
            if (entry.startsWith("#")) {
                var key = ResourceLocation.tryParse(entry.substring(1));
                if (key == null || biomes.getTag(net.minecraft.tags.TagKey.create(
                        net.minecraft.core.registries.Registries.BIOME, key)).isEmpty()) {
                    unresolved.add(modifier + " -> " + entry);
                }
            } else if (biomes.get(ResourceLocation.tryParse(entry)) == null) {
                unresolved.add(modifier + " -> " + entry);
            }
        }
        helper.assertTrue(unresolved.isEmpty(),
                "a cryotheum biome modifier names biomes or tags that do not exist: " + unresolved);
        // The files parsing is not the same statement as the world asking for them.
        var asked = new ArrayList<String>();
        expectFeature(asked, biomes, registries, "minecraft:nether_wastes", "nether_cryotheum_ore");
        expectFeature(asked, biomes, registries, "minecraft:end_highlands", "end_cryotheum_ore");
        helper.assertTrue(asked.isEmpty(),
                "the ore is authored but no biome asks for it at underground_ores: " + asked);
        // The plain stone variant is a block with no worldgen on purpose: the overworld meteorite idea
        // was dropped, so it is waiting for a home. Pin the absence so it cannot come back as a silent
        // everywhere-ore.
        var placedRegistry = registries.registryOrThrow(
                net.minecraft.core.registries.Registries.PLACED_FEATURE);
        var stillGenerated = new ArrayList<String>();
        for (var idle : List.of("cryotheum_ore")) {
            if (placedRegistry.get(ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, idle))
                    != null) {
                stillGenerated.add(idle);
            }
        }
        helper.assertTrue(stillGenerated.isEmpty(),
                "the overworld cryotheum variants have placed features again - they are meant to stay "
                        + "ungenerated until the End meteorite exists: " + stillGenerated);
        helper.succeed();
    }

    /** True when {@code biome} asks for {@code placed} on the ore step; otherwise a complaint is added. */
    private static void expectFeature(List<String> complaints,
                                      net.minecraft.core.Registry<net.minecraft.world.level.biome.Biome> biomes,
                                      net.minecraft.core.RegistryAccess registries, String biome,
                                      String placed) {
        var holder = biomes.get(ResourceLocation.tryParse(biome));
        if (holder == null) {
            complaints.add(biome + " does not exist");
            return;
        }
        var wanted = registries.registryOrThrow(net.minecraft.core.registries.Registries.PLACED_FEATURE)
                .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.PLACED_FEATURE,
                        ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, placed)));
        var steps = holder.getGenerationSettings().features();
        var step = net.minecraft.world.level.levelgen.GenerationStep.Decoration.UNDERGROUND_ORES.ordinal();
        for (var i = 0; i < steps.size(); i++) {
            if (i == step && steps.get(i).contains(wanted)) {
                return;
            }
        }
        complaints.add(biome + " has no " + placed + " on underground_ores");
    }

    /**
     * The floating rock is our own structure rather than a hook into AE2's meteorite, so everything about
     * it has to be checked on our side: the type and piece are registered, the two data files decoded into
     * the dynamic registries (a field name that disagrees with the codec drops the element and the game
     * only logs it), and the biome set it names actually contains the void biome it is meant to float in.
     */
    @GameTest(template = "empty", batch = "cryotheum_meteorite", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void cryotheumMeteoriteIsRegisteredAndLoaded(GameTestHelper helper) {
        var id = ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "cryotheum_meteorite");
        if (!BuiltInRegistries.STRUCTURE_TYPE.containsKey(id)
                || !BuiltInRegistries.STRUCTURE_PIECE.containsKey(id)) {
            helper.fail("the structure type or its piece is not registered, so the data file cannot decode");
            return;
        }
        var registries = helper.getLevel().registryAccess();
        var structures = registries.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE);
        var structure = structures.get(id);
        if (structure == null) {
            helper.fail("worldgen/structure/cryotheum_meteorite.json did not load - a field name that "
                    + "disagrees with the codec shows up exactly like this");
            return;
        }
        var sets = registries.registryOrThrow(net.minecraft.core.registries.Registries.STRUCTURE_SET);
        var set = sets.get(id);
        helper.assertTrue(set != null && set.structures().stream()
                        .anyMatch(entry -> entry.structure().value() == structure),
                "the structure exists but no structure_set places it, so it can never generate and "
                        + "/locate cannot find it");
        var voidBiome = registries.registryOrThrow(net.minecraft.core.registries.Registries.BIOME)
                .getHolderOrThrow(net.minecraft.resources.ResourceKey.create(
                        net.minecraft.core.registries.Registries.BIOME,
                        ResourceLocation.withDefaultNamespace("small_end_islands")));
        helper.assertTrue(structure.biomes().contains(voidBiome),
                "the meteorite is not allowed in small_end_islands: " + structure.biomes());
        helper.assertTrue(structure.step()
                        == net.minecraft.world.level.levelgen.GenerationStep.Decoration.TOP_LAYER_MODIFICATION,
                "the meteorite is on step " + structure.step() + "; below that the island noise "
                        + "has already run, above it the rock would be overwritten");
        helper.succeed();
    }

    /**
     * The four conditions of grette mother rock have to move in the right direction: a rock grows crystals
     * into the space in front of it, and every condition wears down into the next one. Getting that
     * backwards is invisible in game and silent in a compile - and the first cut of this chain did have it
     * inverted, so both halves are asserted here.
     *
     * <p>Driven tick by tick with a seeded random because waiting for the world to hand out that many
     * random ticks would take longer than the test is worth.
     */
    @GameTest(template = "empty", batch = "grette_chain", timeoutTicks = 120,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void gretteBuddingGrowsAndWearsDown(GameTestHelper helper) {
        var level = helper.getLevel();
        var stages = List.of(ModRegistration.SMALL_GRETTE_BUD.get(),
                ModRegistration.MEDIUM_GRETTE_BUD.get(), ModRegistration.LARGE_GRETTE_BUD.get(),
                ModRegistration.GRETTE_CLUSTER.get());
        var pos = new BlockPos(0, 2, 0);
        helper.setBlock(pos, ModRegistration.FLAWLESS_BUDDING_GRETTE.get());
        // The structure is addressed relatively, but randomTick talks to the real level in world coordinates.
        var anchor = helper.absolutePos(pos);
        var flawless = ModRegistration.FLAWLESS_BUDDING_GRETTE.get();
        var random = net.minecraft.util.RandomSource.create(20261003L);
        for (var i = 0; i < 400; i++) {
            flawless.randomTick(level.getBlockState(anchor), level, anchor, random);
        }
        var grew = false;
        for (var direction : net.minecraft.core.Direction.values()) {
            var state = level.getBlockState(anchor.relative(direction));
            if (stages.stream().anyMatch(state::is)) {
                grew = true;
            }
        }
        helper.assertTrue(grew, "flawless mother rock ran 400 random ticks and grew nothing on any face");
        helper.assertTrue(level.getBlockState(anchor).is(flawless),
                "flawless mother rock wore down to " + level.getBlockState(anchor).getBlock()
                        + " - the top condition has to stay permanent, or an untouched meteorite erodes itself");

        var wornPos = new BlockPos(6, 2, 6);
        helper.setBlock(wornPos, ModRegistration.FLAWED_BUDDING_GRETTE.get());
        var worn = helper.absolutePos(wornPos);
        var flawed = ModRegistration.FLAWED_BUDDING_GRETTE.get();
        var decay = net.minecraft.util.RandomSource.create(7L);
        for (var i = 0; i < 400 && level.getBlockState(worn).is(flawed); i++) {
            flawed.randomTick(level.getBlockState(worn), level, worn, decay);
        }
        helper.assertTrue(level.getBlockState(worn).is(ModRegistration.CHIPPED_BUDDING_GRETTE.get()),
                "flawed mother rock wore down to " + level.getBlockState(worn).getBlock()
                        + ", expected chipped");
        helper.succeed();
    }

    /**
     * The crystal family shipped in v1.3.9 under {@code frigit_*}, and 1.4.0 renames it to
     * {@code grette_*}. Every old id therefore has to keep pointing at the renamed entry, or the blocks
     * already placed in a world and the stacks already sitting in inventories turn into air - vanilla
     * drops a registry id it cannot resolve, silently and with no log line.
     *
     * <p>This asserts the alias on the registries the game itself reads (blocks and items), and a
     * misspelled id as the control, so a constructor that resolves everything to anything cannot pass.
     */
    @GameTest(template = "empty", batch = "grette_alias", timeoutTicks = 40,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void oldFrigitIdsStillResolve(GameTestHelper helper) {
        record Renamed(String oldPath, String newPath, Block block, Item item) {}
        var family = List.of(
                new Renamed("frigit_crystal", "grette_crystal", null,
                        ModRegistration.GRETTE_CRYSTAL.get()),
                new Renamed("frigit_crystal_block", "grette_crystal_block",
                        ModRegistration.GRETTE_CRYSTAL_BLOCK.get(),
                        ModRegistration.GRETTE_CRYSTAL_BLOCK_ITEM.get()),
                new Renamed("frigit_cluster", "grette_cluster",
                        ModRegistration.GRETTE_CLUSTER.get(), ModRegistration.GRETTE_CLUSTER_ITEM.get()),
                new Renamed("small_frigit_bud", "small_grette_bud",
                        ModRegistration.SMALL_GRETTE_BUD.get(), ModRegistration.SMALL_GRETTE_BUD_ITEM.get()),
                new Renamed("medium_frigit_bud", "medium_grette_bud",
                        ModRegistration.MEDIUM_GRETTE_BUD.get(), ModRegistration.MEDIUM_GRETTE_BUD_ITEM.get()),
                new Renamed("large_frigit_bud", "large_grette_bud",
                        ModRegistration.LARGE_GRETTE_BUD.get(), ModRegistration.LARGE_GRETTE_BUD_ITEM.get()),
                new Renamed("damaged_budding_frigit", "damaged_budding_grette",
                        ModRegistration.DAMAGED_BUDDING_GRETTE.get(),
                        ModRegistration.DAMAGED_BUDDING_GRETTE_ITEM.get()),
                new Renamed("chipped_budding_frigit", "chipped_budding_grette",
                        ModRegistration.CHIPPED_BUDDING_GRETTE.get(),
                        ModRegistration.CHIPPED_BUDDING_GRETTE_ITEM.get()),
                new Renamed("flawed_budding_frigit", "flawed_budding_grette",
                        ModRegistration.FLAWED_BUDDING_GRETTE.get(),
                        ModRegistration.FLAWED_BUDDING_GRETTE_ITEM.get()),
                new Renamed("flawless_budding_frigit", "flawless_budding_grette",
                        ModRegistration.FLAWLESS_BUDDING_GRETTE.get(),
                        ModRegistration.FLAWLESS_BUDDING_GRETTE_ITEM.get()));
        for (var entry : family) {
            var oldId = NeoECOPrototype.id(entry.oldPath());
            var newId = NeoECOPrototype.id(entry.newPath());
            if (entry.block() != null) {
                if (!newId.equals(BuiltInRegistries.BLOCK.getKey(entry.block()))) {
                    helper.fail("the renamed block registers as "
                            + BuiltInRegistries.BLOCK.getKey(entry.block()) + ", expected " + newId);
                    return;
                }
                if (BuiltInRegistries.BLOCK.getOptional(oldId).orElse(null) != entry.block()) {
                    helper.fail("block " + oldId + " resolves to "
                            + BuiltInRegistries.BLOCK.getOptional(oldId).orElse(null) + " instead of "
                            + newId + " - a world that already has this block would lose it");
                    return;
                }
            }
            if (!newId.equals(BuiltInRegistries.ITEM.getKey(entry.item()))) {
                helper.fail("the renamed item registers as "
                        + BuiltInRegistries.ITEM.getKey(entry.item()) + ", expected " + newId);
                return;
            }
            if (BuiltInRegistries.ITEM.getOptional(oldId).orElse(null) != entry.item()) {
                helper.fail("item " + oldId + " resolves to "
                        + BuiltInRegistries.ITEM.getOptional(oldId).orElse(null) + " instead of "
                        + newId + " - a stack already in an inventory would vanish");
                return;
            }
        }
        // Control: if the registries answered every id, the loop above would prove nothing.
        var nowhere = NeoECOPrototype.id("frigit_that_never_existed");
        if (BuiltInRegistries.BLOCK.getOptional(nowhere).isPresent()
                || BuiltInRegistries.ITEM.getOptional(nowhere).isPresent()) {
            helper.fail("an id that was never registered resolved to "
                    + BuiltInRegistries.ITEM.getOptional(nowhere) + ", so this guard cannot tell an "
                    + "alias from a registry that answers everything");
            return;
        }
        helper.succeed();
    }

    /** Places a glass cable into the empty cell next to {@code clickedPos} on {@code face}. */
    private static boolean placeCableAgainst(GameTestHelper helper, Player player, BlockPos clickedPos,
                                             Direction face) {
        // The structure is addressed relatively, but a use context and a part host are world objects.
        var clicked = helper.absolutePos(clickedPos);
        var cableStack = AEParts.GLASS_CABLE.stack(AEColor.TRANSPARENT);
        player.setItemInHand(InteractionHand.MAIN_HAND, cableStack);
        var hit = new BlockHitResult(Vec3.atCenterOf(clicked)
                .add(face.getStepX() * 0.5, face.getStepY() * 0.5, face.getStepZ() * 0.5),
                face, clicked, false);
        cableStack.getItem().useOn(new UseOnContext(helper.getLevel(), player,
                InteractionHand.MAIN_HAND, cableStack, hit));
        return helper.getLevel().getBlockEntity(clicked.relative(face)) instanceof IPartHost host
                && host.getPart(null) != null;
    }

    private static String at(BlockPos pos) {
        return pos.getX() + "," + pos.getY() + "," + pos.getZ();
    }

    private static List<String> machineProbesWithoutConnection(GameTestHelper helper,
                                                                List<CableProbe> probes) {
        var stuck = new ArrayList<String>();
        for (var probe : probes) {
            if (!connectedOnSide(helper, probe.machinePos, Direction.EAST)
                    || !connectedOnSide(helper, probe.cablePos(), Direction.WEST)) {
                stuck.add(probe.label);
            }
        }
        return stuck;
    }

    /** True once the node at {@code pos} reports a connection on {@code side}. */
    private static boolean connectedOnSide(GameTestHelper helper, BlockPos pos, Direction side) {
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(pos)) instanceof IInWorldGridNodeHost host)) {
            return false;
        }
        IGridNode node = host.getGridNode(side);
        return node != null && node.getConnectedSides().contains(side);
    }

    private record CableProbe(String label, Block block, BlockPos machinePos) {
        BlockPos cablePos() {
            return machinePos.east();
        }
    }

    /**
     * Builds a whole L1 room from eco's own placement plan, standing {@code wantedInterface} in the
     * cell the plan reserves for {@code planInterface}, then hands the standing machine to
     * {@code onFormed}. Going through the plan instead of hand-placing blocks keeps the test honest
     * when the geometry moves: a room that no longer matches the builder fails here rather than
     * quietly measuring a shape nobody can build.
     */
    private static void buildL1Room(GameTestHelper helper, BlockPos controllerPos, Block hostBlock,
                                    Block planInterface, Block wantedInterface,
                                    java.util.function.Consumer<MultiBlockBuildController.Host> onFormed) {
        buildRoom(helper, controllerPos, hostBlock, planInterface, wantedInterface, false, onFormed);
    }

    /** The same builder, run at the machine's own maximum length instead of its minimum. */
    private static void buildRoomToMaxLength(GameTestHelper helper, BlockPos controllerPos, Block hostBlock,
                                             Block planInterface,
                                             java.util.function.Consumer<MultiBlockBuildController.Host> onFormed) {
        buildRoom(helper, controllerPos, hostBlock, planInterface, planInterface, true, onFormed);
    }

    private static void buildRoom(GameTestHelper helper, BlockPos controllerPos, Block hostBlock,
                                  Block planInterface, Block wantedInterface, boolean atMax,
                                  java.util.function.Consumer<MultiBlockBuildController.Host> onFormed) {
        buildRoomKept(helper, controllerPos, hostBlock, planInterface, wantedInterface, atMax,
                (host, finish) -> {
                    try {
                        onFormed.accept(host);
                    } finally {
                        finish.run();
                        helper.succeed();
                    }
                });
    }

    /**
     * The builder with the teardown handed back to the caller. Everything else about the room is the same,
     * but the blocks come down when the callback says so - which a test needs the moment the thing under
     * test spans ticks rather than one method body, because this file's synchronous shape would clear the
     * buses before the machine has moved anything.
     */
    private static void buildRoomKept(GameTestHelper helper, BlockPos controllerPos, Block hostBlock,
                                      Block planInterface, Block wantedInterface, boolean atMax,
                                      java.util.function.BiConsumer<MultiBlockBuildController.Host, Runnable> onFormed) {
        helper.setBlock(controllerPos, hostBlock);
        BlockPos absolute = helper.absolutePos(controllerPos);
        helper.runAfterDelay(5, () -> {
            if (!(helper.getLevel().getBlockEntity(absolute) instanceof MultiBlockBuildController.Host host)) {
                helper.fail(name(hostBlock) + " has no one-click builder at " + absolute);
                return;
            }
            host.setSelectedBuildLength(atMax ? host.getMaxBuildLength() : host.getMinBuildLength());
            var plan = new MultiBlockBuildController(host).createLocalPreviewPlan();
            if (plan == null || plan.getAllBlocks().isEmpty()) {
                helper.fail("the builder produced no placement plan for " + name(hostBlock));
                return;
            }
            List<BlockPos> built = new ArrayList<>();
            int seen = 0;
            for (var planned : plan.getAllBlocks()) {
                // Only the template volume is restored after a test, so anything written outside it
                // leaks into the neighbouring test room and takes its block entities with it.
                BlockPos rel = planned.worldPos().subtract(helper.absolutePos(BlockPos.ZERO));
                if (rel.getX() < 0 || rel.getY() < 0 || rel.getZ() < 0 || rel.getX() >= L1_ROOM_SIZE
                        || rel.getY() >= L1_ROOM_SIZE || rel.getZ() >= L1_ROOM_SIZE) {
                    helper.fail("the " + name(hostBlock) + " build plan leaves the "
                            + L1_ROOM_SIZE + "^3 template at " + rel);
                    return;
                }
                var target = planned.targetState();
                if (target.getBlock() == planInterface) {
                    seen++;
                    helper.getLevel().setBlockAndUpdate(planned.worldPos(),
                            wantedInterface.defaultBlockState());
                } else {
                    helper.getLevel().setBlockAndUpdate(planned.worldPos(), target);
                }
                built.add(planned.worldPos());
            }
            if (seen == 0) {
                helper.fail("the plan for " + name(hostBlock) + " placed no " + name(planInterface)
                        + ", so this test would prove nothing");
                return;
            }
            host.rebuildAfterBuild();
            helper.runAfterDelay(20, () -> {
                // Two standing machines seven blocks apart make every controller "not unique", so whoever
                // takes the handle has to clear our own blocks before the next batch starts.
                boolean[] down = {false};
                Runnable finish = () -> {
                    if (down[0]) {
                        return;
                    }
                    down[0] = true;
                    for (BlockPos pos : built) {
                        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
                };
                if (!host.isFormed()) {
                    finish.run();
                    helper.fail("the room built from the plan never formed, so the published shape "
                            + "says nothing about the machine");
                    return;
                }
                onFormed.accept(host, finish);
            });
        });
    }

    private static String name(Block block) {
        return BuiltInRegistries.BLOCK.getKey(block).getPath();
    }

    /**
     * A formed host has to say which of the two interface blocks it ended up with, because that is the
     * only handle a resource pack has for choosing the formed look. Both directions are measured per
     * machine: a flag stuck at its default and a flag hard-wired to true look identical right up to
     * the point where an artist has drawn sheets nobody can ever reach. True means the cell holds the
     * 通讯接口, the block with the GUI; the plain endpoint publishes false.
     */
    @GameTest(template = "l1_room", batch = "storage_comm_plain", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void storageHostPublishesTrueForTheNetworkInterfaceBlock(GameTestHelper helper) {
        assertStorageInterfacePublished(helper,
                ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get(), true);
    }

    @GameTest(template = "l1_room", batch = "storage_comm_on", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void storageHostPublishesFalseForThePlainInterfaceBlock(GameTestHelper helper) {
        assertStorageInterfacePublished(helper,
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(), false);
    }

    private static void assertStorageInterfacePublished(GameTestHelper helper, Block interfaceBlock,
                                                        boolean expected) {
        var controllerPos = new BlockPos(6, 3, 6);
        buildL1Room(helper, controllerPos, ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(), interfaceBlock, host -> {
                    var state = helper.getLevel().getBlockState(helper.absolutePos(controllerPos));
                    if (!state.hasProperty(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE)) {
                        helper.fail("the L1 storage host has no communication_interface property at all: "
                                + state);
                        return;
                    }
                    if (state.getValue(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE) != expected) {
                        helper.fail("the machine holds " + name(interfaceBlock) + ", but the host published "
                                + "communication_interface="
                                + state.getValue(SimplifyStorageControllerBlock.COMMUNICATION_INTERFACE)
                                + "; only simplify_storage_network_interface should set it");
                    }
                });
    }

    @GameTest(template = "l1_room", batch = "crafting_comm_plain", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingHostPublishesTrueForTheNetworkInterfaceBlock(GameTestHelper helper) {
        assertCraftingInterfacePublished(helper,
                ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get(), true);
    }

    @GameTest(template = "l1_room", batch = "crafting_comm_on", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingHostPublishesFalseForThePlainInterfaceBlock(GameTestHelper helper) {
        assertCraftingInterfacePublished(helper,
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), false);
    }

    private static void assertCraftingInterfacePublished(GameTestHelper helper, Block interfaceBlock,
                                                         boolean expected) {
        var controllerPos = new BlockPos(6, 3, 6);
        buildL1Room(helper, controllerPos, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), interfaceBlock, host -> {
                    var state = helper.getLevel().getBlockState(helper.absolutePos(controllerPos));
                    if (!state.hasProperty(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)) {
                        helper.fail("the F1 crafting host has no communication_interface property at all: "
                                + state);
                        return;
                    }
                    if (state.getValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE) != expected) {
                        helper.fail("the machine holds " + name(interfaceBlock) + ", but the host published "
                                + "communication_interface="
                                + state.getValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)
                                + "; only simplify_crafting_network_interface should set it");
                    }
                });
    }

    /**
     * The three crafting members are eco's own block entity classes, so the only thing that gives them the
     * addon's idle rate is ECOCraftingHighPowerMixin - and a formed machine is the only moment a grid node
     * exists to read. This is that read: without it the mixin could stop firing and nothing would notice.
     */
    @GameTest(template = "l1_room", batch = "f1_member_power", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingMembersIdleAtTheAddonRate(GameTestHelper helper) {
        double expected = cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile.L1
                .highIdleComponentPower();
        buildL1Room(helper, new BlockPos(6, 3, 6),
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                host -> {
                    int checked = 0;
                    for (int x = 0; x < L1_ROOM_SIZE; x++) {
                        for (int y = 0; y < L1_ROOM_SIZE; y++) {
                            for (int z = 0; z < L1_ROOM_SIZE; z++) {
                                var rel = new BlockPos(x, y, z);
                                var block = helper.getLevel().getBlockState(helper.absolutePos(rel)).getBlock();
                                if (block != ModRegistration.SIMPLIFY_CRAFTING_WORKER_BLOCK.get()
                                        && block != ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get()
                                        && block != ModRegistration.SIMPLIFY_CRAFTING_VENT_BLOCK.get()) {
                                    continue;
                                }
                                var absolute = helper.absolutePos(rel);
                                if (!(helper.getLevel().getBlockEntity(absolute)
                                        instanceof NEBlockEntity<?, ?> member)) {
                                    helper.fail(name(block) + " at " + absolute
                                            + " is not an NEBlockEntity, so its idle power cannot be read");
                                    return;
                                }
                                var node = member.getMainNode() == null
                                        ? null : member.getMainNode().getNode();
                                if (node == null) {
                                    helper.fail(name(block) + " at " + absolute
                                            + " has no grid node in a formed machine, so its idle power"
                                            + " cannot be read");
                                    return;
                                }
                                if (Math.abs(node.getIdlePowerUsage() - expected) > 1e-9) {
                                    helper.fail(name(block) + " idles at " + node.getIdlePowerUsage()
                                            + " AE/t in a formed F1, but the addon power mixin should have"
                                            + " set " + expected);
                                    return;
                                }
                                checked++;
                            }
                        }
                    }
                    if (checked == 0) {
                        helper.fail("the formed F1 room holds none of the three crafting members, so this"
                                + " test would prove nothing");
                    }
                });
    }

    /**
     * One row per addon machine: which class holds its block entity, what tier it carries, and what its
     * grid node actually idles at. Two of those three routes answer for us - the {@code ECOPowerProfiles}
     * provider for the machines whose block entity reports no tier, and {@code IECOTier#getComponentIdlePower}
     * for the ones that do - and a row left at eco's unscaled 16.0 means neither of them fired. The scan
     * fails on an empty read, because a scan that read nothing would prove nothing.
     */
    @GameTest(template = "l1_room", batch = "idle_power_inventory", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void addonMachineIdlePowerInventory(GameTestHelper helper) {
        var inventoryLog = org.slf4j.LoggerFactory.getLogger("neoecoprototype-idle-inventory");
        var placed = new java.util.ArrayList<BlockPos>();
        int slotX = 0;
        int slotZ = 12;
        for (Block block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (id == null || !NeoECOPrototype.MOD_ID.equals(id.getNamespace())
                    || !block.defaultBlockState().hasBlockEntity()) {
                continue;
            }
            if (slotX >= L1_ROOM_SIZE) {
                slotX = 0;
                slotZ++;
                if (slotZ >= L1_ROOM_SIZE) {
                    helper.fail("the room is too small to hold every addon block entity");
                    return;
                }
            }
            var rel = new BlockPos(slotX++, 14, slotZ);
            helper.setBlock(rel, block.defaultBlockState());
            placed.add(rel);
        }
        helper.runAfterDelay(30, () -> {
            int rows = 0;
            for (var rel : placed) {
                var absolute = helper.absolutePos(rel);
                var state = helper.getLevel().getBlockState(absolute);
                if (!(helper.getLevel().getBlockEntity(absolute) instanceof NEBlockEntity<?, ?> machine)) {
                    continue;
                }
                var node = machine.getMainNode() == null ? null : machine.getMainNode().getNode();
                inventoryLog.info("IDLE-INVENTORY {} | {} | tier={} | idle={}",
                        BuiltInRegistries.BLOCK.getKey(state.getBlock()),
                        machine.getClass().getSimpleName(),
                        machine.getTier(),
                        node == null ? "no node" : node.getIdlePowerUsage());
                if (node != null && node.getIdlePowerUsage() == 16.0D) {
                    helper.fail(name(state.getBlock()) + " idles at 16.0 AE/t, which is eco's own "
                            + "un-scaled component baseline - nothing scaled it for the addon's L1 frame");
                    return;
                }
                rows++;
            }
            if (rows == 0) {
                helper.fail("none of the addon blocks placed an NEBlockEntity, so the inventory read nothing");
                return;
            }
            inventoryLog.info("IDLE-INVENTORY done: {} rows, mixin baseline is {} AE/t",
                    rows, cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile.L1.baseComponentIdlePower());
            helper.succeed();
        });
    }

    /**
     * eco's pattern-migration source pool is built from the grid's {@code PatternContainer} machines, and
     * every machine that is a {@code PatternStorageHost} is dropped unless
     * {@code allowsPatternMigrationSource()} says otherwise ({@code PatternCatalog.beginExternalPatternIndexBuild},
     * offsets 125-151). The interface default is false and eco ships no override anywhere in its jar, so both
     * eco's own bus and ours were excluded -- issue #128. Ours answers true now; eco's own bus is placed next
     * to it as the control, because "the default still applies to the class we did not touch" is the only way
     * this test cannot pass by having quietly widened the interface.
     */
    @GameTest(template = "l1_room", batch = "pattern_bus_migration_source_closed", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void addonPatternBusStaysOutOfTheMigrationSourcePool(GameTestHelper helper) {
        var oursPos = new BlockPos(3, 2, 3);
        var ecoPos = new BlockPos(5, 2, 3);
        helper.setBlock(oursPos, ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get());
        helper.setBlock(ecoPos, cn.dancingsnow.neoecoae.all.NEBlocks.CRAFTING_PATTERN_BUS.get());
        helper.runAfterDelay(20, () -> {
            var ours = helper.getLevel().getBlockEntity(helper.absolutePos(oursPos));
            var eco = helper.getLevel().getBlockEntity(helper.absolutePos(ecoPos));
            if (!(ours instanceof cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity oursBus)) {
                helper.fail("our bus placed a " + (ours == null ? "nothing" : ours.getClass().getName())
                        + " instead of an eco pattern bus, so the machine table no longer sees it");
                return;
            }
            if (ours.getClass() != cn.dancingsnow.neoecoae.blocks.entity.crafting
                    .ECOCraftingPatternBusBlockEntity.class) {
                helper.fail("our bus runs an addon subclass again (" + ours.getClass().getName() + "),"
                        + " and 1.4.1 retired it: the only thing that subclass did was answer the hook"
                        + " true");
                return;
            }
            // 1.4.1 closes this door again. A player reported on 2026-10-25 that patterns were lost after
            // a subsystem migration, and our bus had become the first container in this ecosystem sitting
            // in the source pool and the writable target pool at once - so ours now answers what eco's own
            // bus answers, until that loss is understood.
            if (oursBus.allowsPatternMigrationSource()) {
                helper.fail("the addon bus is back in the migration source pool - 1.4.1 took it out over"
                        + " the reported pattern loss, so this is the change that must not come back"
                        + " unnoticed");
                return;
            }
            if (!(eco instanceof cn.dancingsnow.neoecoae.api.PatternStorageHost ecoBus)) {
                helper.fail("eco's own bus is not a PatternStorageHost, so this control row reads nothing");
                return;
            }
            if (ecoBus.allowsPatternMigrationSource()) {
                helper.fail("eco's own bus answers the hook true, so upstream changed its default and"
                        + " this test is comparing against the wrong thing");
                return;
            }
            helper.succeed();
        });
    }


    /**
     * eco's large-workstation recipe scan used to reach for lightning unconditionally: in the 21.2.10 build
     * that first shipped, {@code LargeWorkstationRecipeSources} carried the {@code LightningKey} reference
     * in its own body, so the class could not even be verified without ae2lt - an integrated server died at
     * world load with {@code NoClassDefFoundError} on any base holding one of eco's L4 workstation
     * interfaces, while ae2lt is only declared optional. The re-uploaded 21.2.10 moved that reference into
     * {@code $LightningBorrowing} behind {@code loaded("ae2lt")}, which is the same shape this repo's §4.2
     * requires. Nothing here builds an L4 machine: the scan is the unit, and it runs on every server that
     * has an AE2 grid, so calling it with lightning absent is the whole test.
     */
    @GameTest(template = "empty", batch = "large_station_scan", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void largeWorkstationScanSurvivesWithoutLightning(GameTestHelper helper) {
        if (ModList.get().isLoaded("ae2lt")) {
            helper.fail("ae2lt is loaded in this run, so this scan would not be reading the no-lightning case");
            return;
        }
        java.util.List<cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipe> recipes;
        try {
            recipes = cn.dancingsnow.neoecoae.recipe.LargeWorkstationRecipes.getAll(helper.getLevel());
        } catch (Throwable thrown) {
            helper.fail("the scan reached for lightning although ae2lt is not installed: " + thrown);
            return;
        }
        if (recipes == null) {
            helper.fail("eco's large-workstation scan returned null with no lightning mod, where an empty"
                    + " list is the answer");
            return;
        }
        if (recipes.isEmpty()) {
            helper.fail("the scan answered with no recipe at all, so this run is not reading eco's real"
                    + " table and the check above would pass on an empty list anyway");
            return;
        }
        helper.succeed();
    }


    /**
     * eco builds three of its own placement definitions from {@code NEConfig} values that are only
     * assigned when its config loads, so whatever initialises {@code NEMultiBlocks} earlier leaves those
     * definitions with a build range of 1 .. -4 - and eco's host UI then dies with
     * {@code IllegalArgumentException: 1 > -4} the moment a player opens it. Reading eco's own numbers is
     * the only way this regression cannot pass unnoticed.
     */
    @GameTest(template = "empty", batch = "eco_definition_range", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ecoDefinitionsKeepAUsableBuildRange(GameTestHelper helper) {
        for (var tier : List.of(cn.dancingsnow.neoecoae.api.ECOTier.L4,
                cn.dancingsnow.neoecoae.api.ECOTier.L6,
                cn.dancingsnow.neoecoae.api.ECOTier.L9)) {
            var storage = cn.dancingsnow.neoecoae.all.NEMultiBlocks.getStorageSystemDefinition(tier);
            var computation = cn.dancingsnow.neoecoae.all.NEMultiBlocks.getComputationSystemDefinition(tier);
            var crafting = cn.dancingsnow.neoecoae.all.NEMultiBlocks.getCraftingSystemDefinition(tier);
            for (var pair : List.of(new Object[][]{{"storage", storage}, {"computation", computation},
                    {"crafting", crafting}})) {
                var definition = (cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition) pair[1];
                if (definition == null) {
                    helper.fail(pair[0] + " " + tier + " has no definition at all");
                    return;
                }
                if (definition.getExpandMax() < definition.getExpandMin()) {
                    helper.fail(pair[0] + " " + tier + " reports build range "
                            + definition.getExpandMin() + " .. " + definition.getExpandMax()
                            + ", which makes eco's host UI throw 1 > -4 on open");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * What our three L1 machines cost at their ceiling, read off eco's own planner instead of off our
     * definition source - the numbers then move with the definitions rather than with whoever last
     * transcribed them, which is how a hand-written parts list rots.
     *
     * <p>No block is placed: the bill comes from the plan's position list, which is pure definition. And
     * it is only the structure. Whatever goes in afterwards - storage cells, patterns, coolant, computation
     * cells - never appears in a placement plan, so this is not "everything a working machine needs".
     *
     * <p>The bounding box rides along because the upgrade drives need to know how much clear air a swap
     * consumes: the shape here is the shape a player has to keep free.
     */
    @GameTest(template = "empty", batch = "eco_definition_range", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void fullLengthL1MachinesBillWhatThePlannerSays(GameTestHelper helper) {
        var report = new StringBuilder();
        var empties = new java.util.ArrayList<String>();
        report.append(billLine(helper, "ours storage L1", empties,
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition.L1, true));
        report.append(billLine(helper, "ours computation L1", empties,
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition.L1, true));
        report.append(billLine(helper, "ours crafting L1", empties,
                cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition.L1, true));
        for (var tier : List.of(cn.dancingsnow.neoecoae.api.ECOTier.L4,
                cn.dancingsnow.neoecoae.api.ECOTier.L6,
                cn.dancingsnow.neoecoae.api.ECOTier.L9)) {
            report.append(billLine(helper, "eco storage " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getStorageSystemDefinition(tier), false));
            report.append(billLine(helper, "eco storage " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getStorageSystemDefinition(tier), true));
            report.append(billLine(helper, "eco computation " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getComputationSystemDefinition(tier), false));
            report.append(billLine(helper, "eco computation " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getComputationSystemDefinition(tier), true));
            report.append(billLine(helper, "eco crafting " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getCraftingSystemDefinition(tier), false));
            report.append(billLine(helper, "eco crafting " + tier, empties,
                    cn.dancingsnow.neoecoae.all.NEMultiBlocks.getCraftingSystemDefinition(tier), true));
        }
        if (!empties.isEmpty()) {
            helper.fail("these planned no positions, so their bill is not a reading: " + empties);
            return;
        }
        if (report.toString().lines().count() < 12) {
            helper.fail("only " + report.toString().lines().count()
                    + " bill lines, expected ours at the ceiling plus eco's L4/L6/L9 at both ends");
            return;
        }
        NeoECOPrototype.LOGGER.info("placement bills{}", report);
        helper.succeed();
    }

    /**
     * One line of the bill table: what the planner asks for when the given definition is planned at its
     * shortest or longest build. Positions and pieces are counted apart because a position can bill more
     * than one item, and the bounding box is the air the player has to keep clear of it.
     */
    private static String billLine(GameTestHelper helper, String label, List<String> empties,
                                   MultiBlockDefinition definition, boolean atMax) {
        if (definition == null) {
            empties.add(label + " (no definition)");
            return "";
        }
        int length = atMax ? definition.getExpandMax() : definition.getExpandMin();
        var plan = MultiBlockPlacementService.preview(helper.getLevel(),
                helper.absolutePos(new BlockPos(0, 1, 0)),
                definition.getOwner().value().defaultBlockState(), definition, length, false);
        if (plan.getAllBlocks().isEmpty()) {
            empties.add(label + " at expand " + length);
            return "";
        }
        var totals = new java.util.TreeMap<String, Integer>();
        int pieces = 0;
        int minX = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE;
        int minY = Integer.MAX_VALUE;
        int maxY = Integer.MIN_VALUE;
        int minZ = Integer.MAX_VALUE;
        int maxZ = Integer.MIN_VALUE;
        for (var planned : plan.getAllBlocks()) {
            var stack = planned.requiredItem();
            if (stack.isEmpty()) {
                empties.add(label + " position " + planned.worldPos() + " ("
                        + planned.targetState().getBlock() + ") has no item behind it");
                continue;
            }
            pieces += stack.getCount();
            totals.merge(BuiltInRegistries.ITEM.getKey(stack.getItem()).getPath(),
                    stack.getCount(), Integer::sum);
            var pos = planned.worldPos();
            minX = Math.min(minX, pos.getX());
            maxX = Math.max(maxX, pos.getX());
            minY = Math.min(minY, pos.getY());
            maxY = Math.max(maxY, pos.getY());
            minZ = Math.min(minZ, pos.getZ());
            maxZ = Math.max(maxZ, pos.getZ());
        }
        return "\n  " + label + " expand " + length + " (range " + definition.getExpandMin() + ".."
                + definition.getExpandMax() + "): " + plan.getAllBlocks().size() + " positions, "
                + pieces + " pieces, box " + (maxX - minX + 1) + 'x' + (maxY - minY + 1) + 'x'
                + (maxZ - minZ + 1) + " -> " + totals;
    }

    /**
     * The flux cell family is registered only when appflux is installed, so this skips without it - and
     * it reads the cell through eco's own interface rather than ours, because our compile-time-only
     * dependency is not on this classpath and loading our item class would resolve appflux's key type.
     *
     * <p>The insert at the end is the point of the whole guard. eco's cell engine charges a new type its
     * per-type byte cost before it will take a single unit of it, and
     * {@code ECOStorageCell.canHoldNewItem} asks for free bytes strictly greater than that cost, so a
     * rung sized to one byte with one byte per type refuses its first key forever and stores nothing at
     * all. Only an actual insert says that out loud.
     */
    @GameTest(template = "empty", batch = "fe_cell_l1", timeoutTicks = 100, required = false,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void feCellsCarryTheL1FluxLayout(GameTestHelper helper) {
        if (ModRegistration.OPTIONAL_FE_CELL_1M == null || ModRegistration.OPTIONAL_FE_CELL_4M == null) {
            helper.succeed();
            return;
        }
        if (ModRegistration.OPTIONAL_FE_CELL_1K == null) {
            helper.fail("appflux is installed and the 1M/4M flux cells registered, but the 1K one did not,"
                    + " so the ladder lost a rung rather than the whole family");
            return;
        }
        // The row the L1 storage host draws for a flux matrix is found by comparing the mounted cell's own
        // cell type against eco's registered flux type, so that equality is what the panel depends on.
        var fluxType = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "flux"));
        if (fluxType == null) {
            helper.fail("appflux is loaded yet eco registered no flux cell type,"
                    + " so the host panel has no energy row to put a flux matrix in");
            return;
        }
        var ladder = java.util.List.of(
                new Object[][]{{"1k", ModRegistration.OPTIONAL_FE_CELL_1K.get(),
                        cn.dancingsnow.neoecoprototype.integration.appflux.SimplifyFeStorageCellItem.BYTES_1K},
                        {"1m", ModRegistration.OPTIONAL_FE_CELL_1M.get(),
                        cn.dancingsnow.neoecoprototype.integration.appflux.SimplifyFeStorageCellItem.BYTES_1M},
                        {"4m", ModRegistration.OPTIONAL_FE_CELL_4M.get(),
                        cn.dancingsnow.neoecoprototype.integration.appflux.SimplifyFeStorageCellItem.BYTES_4M}});
        for (var rung : ladder) {
            if (!(rung[1] instanceof cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem cell)) {
                helper.fail("the " + rung[0] + " flux cell is not an IBasicECOCellItem,"
                        + " so no cell handler will claim it");
                return;
            }
            long bytes = (Long) rung[2];
            if (cell.getBytes() != bytes || cell.getBytesPerType() != (int) (bytes >> 8)
                    || cell.getTotalTypes() != 1) {
                helper.fail(rung[0] + " flux cell should be " + bytes + " bytes at one byte per 256"
                        + " against one type, but it reports bytes=" + cell.getBytes()
                        + " perType=" + cell.getBytesPerType() + " types=" + cell.getTotalTypes());
                return;
            }
            if (cell.getKeyTypes().size() != 1
                    || !"flux".equals(cell.getKeyTypes().iterator().next().getId().getPath())) {
                helper.fail(rung[0] + " flux cell should carry exactly appflux's own key type, but it"
                        + " reports " + cell.getKeyTypes());
                return;
            }
            if (cell.getTier() != cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1) {
                helper.fail(rung[0] + " flux cell reports " + cell.getTier() + ", so an L1 drive would"
                        + " refuse to mount it or a higher host would take it for its own tier");
                return;
            }
            var inventory = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells.getCellInventory(
                    new ItemStack((Item) rung[1]), (appeng.api.storage.cells.ISaveProvider) null);
            if (inventory == null) {
                helper.fail("the " + rung[0] + " flux cell has no cell inventory,"
                        + " so eco's handler never claimed it");
                return;
            }
            long accepted = AppFluxTestProbe.insertEverything(new ItemStack((Item) rung[1]), inventory);
            if (accepted <= 0) {
                helper.fail("the " + rung[0] + " flux cell takes no FE at all: " + bytes
                        + " bytes with " + cell.getBytesPerType()
                        + " of them reserved for the type leaves nothing for the first key");
                return;
            }
            if (!fluxType.equals(inventory.getCellType())) {
                helper.fail("the mounted " + rung[0] + " flux cell reports the cell type "
                        + inventory.getCellType() + " instead of eco's flux type, so the host would"
                        + " count it among the items rather than draw it under the energy row");
                return;
            }
        }
        // The singularity cell has no row on purpose - it reports an unbounded byte total - which only holds
        // while its cell type is not any of the media that do have one. Collide them and the cell is quietly
        // counted as that medium instead of being left out.
        var unrowed = cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.CELL_TYPE;
        for (var row : java.util.List.of(
                cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getItemCellType(),
                cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getMegaItemCellType(),
                cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getFluidCellType(),
                cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem.CELL_TYPE,
                fluxType)) {
            if (row.equals(unrowed)) {
                helper.fail("the singularity cell type is the same object as a row the panel draws,"
                        + " so it is being counted in a medium it is not part of");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * eco asks the <em>block</em> - not the block entity - which texture to cut a panel cell from, and it
     * always reads a custom texture at its own top-left corner. Two things are worth pinning there: our
     * blocks answer exactly the parts eco requests and stay silent on the rest (answering everything would
     * hand the storage panel a computation glyph), and the files they name are the size eco cuts - a 10x6
     * sprite behind a 10x5 request loses the bottom row of the drawing and nothing complains about it.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelSkinsAnswerThePartsEcoAsks(GameTestHelper helper) {
        var parts = cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.values();
        var parallel = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get();
        var threading = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get();
        var host = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get();
        for (var part : parts) {
            boolean ours = part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_UPPER
                    || part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_LOWER;
            if (parallel.getPanelTexture(
                    ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get().defaultBlockState(), part)
                    != null != ours) {
                helper.fail("the parallel core should answer the two rows it sits in and nothing else, but"
                        + " its answer for " + part + " was " + ours);
                return;
            }
            ours = part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_CORE;
            if (threading.getPanelTexture(
                    ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get().defaultBlockState(), part)
                    != null != ours) {
                helper.fail("the threading core sits in the middle row only, and its answer for " + part
                        + " was " + ours);
                return;
            }
            ours = part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.STORAGE_TIER
                    || part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.STORAGE_MARKER;
            if (host.getPanelTexture(
                    ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get().defaultBlockState(), part)
                    != null != ours) {
                helper.fail("the storage host owns the tier badge and the marker, and its answer for "
                        + part + " was " + ours);
                return;
            }
        }
        // eco 21.2.9 let the computation and crafting hosts into the same protocol one cell deeper: they own
        // their badge and their bracket, and the switch and mode tiles are now asked per (index, on, hover)
        // rather than picked from eco's own sheet. So the answers are checked across all the combinations,
        // not once - the twelve switch asks are 0..2 x on/off x hover, and eco derives those indices from the
        // widget's own name (substitutions / cycle planning / fast planner, and active cooling = 0 with
        // overclock = 1), which is the half a pack cannot see.
        var providerParts = cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.values();
        var computationHost = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get();
        var craftingHost = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get();
        var computationHostState = ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get().defaultBlockState();
        var craftingHostState = ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get().defaultBlockState();
        for (var part : providerParts) {
            boolean ours = part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_TIER
                    || part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_MARKER;
            if ((computationHost.getPanelTexture(computationHostState, part) != null) != ours) {
                helper.fail("the computation host owns its badge and bracket only, and its answer for "
                        + part + " was " + ours);
                return;
            }
            ours = part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.CRAFTING_TIER
                    || part == cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.CRAFTING_MARKER;
            if ((craftingHost.getPanelTexture(craftingHostState, part) != null) != ours) {
                helper.fail("the crafting host owns its badge and bracket only, and its answer for "
                        + part + " was " + ours);
                return;
            }
        }
        // Off stays eco's: its off tile and the artwork sheet's are the same pixels, so answering it would
        // only move the maintenance of a picture nobody can tell apart onto us.
        for (int index = 0; index < 3; index++) {
            for (boolean on : new boolean[]{false, true}) {
                for (boolean hover : new boolean[]{false, true}) {
                    var named = computationHost.getPanelTexture(computationHostState,
                            cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_SWITCH,
                            index, on, hover);
                    if ((named != null) != on) {
                        helper.fail("switch " + index + " with on=" + on + " hover=" + hover + " answered "
                                + named + " - the engaged state is ours and the idle one is eco's");
                        return;
                    }
                    if (on && named.getPath().endsWith("_hover.png") != hover) {
                        helper.fail("switch " + index + " hover=" + hover + " named " + named
                                + ", so hover and rest would draw the same tile");
                        return;
                    }
                }
            }
        }
        for (int index = 0; index < 2; index++) {
            for (boolean on : new boolean[]{false, true}) {
                for (boolean hover : new boolean[]{false, true}) {
                    var named = craftingHost.getPanelTexture(craftingHostState,
                            cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.CRAFTING_MODE,
                            index, on, hover);
                    if ((named != null) != on) {
                        helper.fail("mode button " + index + " with on=" + on + " hover=" + hover + " answered "
                                + named);
                        return;
                    }
                }
            }
        }
        // Lamp 0 is the one status cell we answer, and it answers from the placed state rather than from the
        // lit eco hands over: that predicate is eco's endgame figure and an L1 host can never satisfy it, so
        // the cell would sit on its default glyph forever. The other five lamps stay eco's, and a change that
        // starts answering them has to go red here first.
        var statusPart = cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_STATUS;
        var computationBlock = ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get();
        var plainHost = computationBlock.defaultBlockState();
        var energizedHost = plainHost.setValue(
                cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationSystemBlock
                        .ENERGIZED_PARALLEL_CORE, true);
        if (computationHost.getPanelTexture(plainHost, statusPart, 0, true, false) != null) {
            helper.fail("lamp 0 was answered on a host with no energized parallel core behind it, so the"
                    + " panel would claim a core that is not there");
            return;
        }
        var energizedLamp = computationHost.getPanelTexture(energizedHost, statusPart, 0, false, false);
        if (energizedLamp == null || !"neoecoprototype".equals(energizedLamp.getNamespace())
                || !energizedLamp.getPath().endsWith("computation_status_0_energized.png")) {
            helper.fail("a host whose placed state carries the energized parallel core should light lamp 0"
                    + " with our own glyph - eco's lit is its endgame predicate and never fires on L1 - but"
                    + " it answered " + energizedLamp);
            return;
        }
        for (int index = 1; index < 6; index++) {
            if (computationHost.getPanelTexture(energizedHost, statusPart, index, true, false) != null) {
                helper.fail("lamp " + index + " is eco's predicate to light, and we answered it anyway");
                return;
            }
        }
        var corePart = cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider.Part.COMPUTATION_CORE;        var plainNamed = threading.getPanelTexture(
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get().defaultBlockState(), corePart);
        var energized = (cn.dancingsnow.neoecoae.api.IHostPanelTextureProvider)
                ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get();
        var energizedNamed = energized.getPanelTexture(
                ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get().defaultBlockState(), corePart);
        if (plainNamed == null || energizedNamed == null || plainNamed.equals(energizedNamed)) {
            helper.fail("an energized threading column names " + energizedNamed + " where a plain one names "
                    + plainNamed + ", so the panel cannot show which core is installed");
            return;
        }
        final int[][] wanted = {{10, 5}, {10, 6}, {10, 6}, {10, 6}, {13, 18}, {13, 16}, {13, 18}, {13, 18},
                {13, 16}, {13, 16}, {15, 26}, {15, 26}, {15, 26}, {15, 26}, {12, 12}};
        String[] files = {"computation_upper", "computation_core", "computation_lower",
                "computation_core_energized", "storage_tier", "storage_marker",
                "computation_tier", "crafting_tier", "computation_marker", "crafting_marker",
                "computation_switch_on", "computation_switch_on_hover",
                "crafting_mode_button_on", "crafting_mode_button_on_hover",
                "computation_status_0_energized"};
        for (int i = 0; i < files.length; i++) {
            var size = pngDimensions("assets/" + NeoECOPrototype.MOD_ID + "/textures/gui/panel/"
                    + files[i] + ".png");
            if (size == null) {
                helper.fail(files[i] + ".png is not on the classpath, so the panel would draw nothing");
                return;
            }
            if (size[0] != wanted[i][0] || size[1] != wanted[i][1]) {
                helper.fail(files[i] + ".png is " + size[0] + "x" + size[1] + ", eco cuts it at "
                        + wanted[i][0] + "x" + wanted[i][1]);
                return;
            }
        }
        helper.succeed();
    }

    /**
     * What a pack's own member of the family is made of: a {@code Growth} naming its content and its pace.
     * Both halves are worth a test because neither is visible on the item - the medium a drive routes a cell
     * through comes from the key it grows, and the figures come from the cell rather than the server config,
     * so a regression here would silently hand every scripted cell the built-in's lava-free, config-paced
     * behaviour.
     *
     * <p>This asserts on {@code Growth}, not on an item, and that is a hard limit worth stating: an
     * {@code Item} cannot be constructed once the registry has frozen, which is exactly when a gameTest runs.
     * So the scripted path itself - {@code grows()} reaching a growth object through a real item, in a real
     * drive - has no suite face at all and is verified in a client with a script.
     */
    @GameTest(template = "empty", batch = "singularity_cell_yield", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void scriptedGrowingCellPaysItsOwnContentAtItsOwnSpeed(GameTestHelper helper) {
        var lava = appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.LAVA);
        var water = appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.WATER);
        var growth = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Growth(
                () -> lava, 7L, 600L);
        // The content is a supplier because a chemical id is not readable while items are still being
        // registered, and the first read is kept: a drive asks this on every check, and resolving through the
        // registry each time would make the mount path depend on a lookup that can only fail at runtime.
        var reads = new java.util.concurrent.atomic.AtomicInteger();
        var lazy = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Growth(
                () -> {
                    reads.incrementAndGet();
                    return water;
                }, 1L, 60L);
        if (!lazy.pays(water) || !lazy.content().equals(water) || reads.get() != 1) {
            helper.fail("a growth resolved its key " + reads.get()
                    + " times across two reads - it should ask its supplier once and keep the answer");
            return;
        }
        // The drive picks a cell's medium off the item, which forwards the key's own type. Lose this and a
        // fluid cell would be mounted in the item row and pay nothing forever.
        helper.assertTrue(growth.content().getType() == appeng.api.stacks.AEKeyType.fluids(),
                "a lava growth reports its medium as " + growth.content().getType());
        if (!growth.pays(lava)) {
            helper.fail("a cell built to grow lava refuses lava, so it would never pay its own stock");
            return;
        }
        if (growth.pays(water)) {
            helper.fail("a cell built to grow lava pays water - the content is a filter, not a label");
            return;
        }
        if (growth.readyAt(0L, 0L, 1800L) != 21L || growth.readyAt(0L, 0L, 599L) != 0L
                || growth.readyAt(0L, 0L, 600L) != 7L) {
            helper.fail("7 per 600 ticks over 1800 should jump 0, 7, then 21, the growth reports "
                    + growth.readyAt(0L, 0L, 599L) + ", " + growth.readyAt(0L, 0L, 600L) + ", "
                    + growth.readyAt(0L, 0L, 1800L));
            return;
        }
        if (growth.readyAt(0L, 14L, 1800L) != 7L) {
            helper.fail("drawing two batches of a seven-item bank should leave 7, the growth reports "
                    + growth.readyAt(0L, 14L, 1800L));
            return;
        }
        // The two paces have to stay two paces: rewrite the server config and the built-in follows it while
        // the scripted growth keeps the numbers its item was built with. If these ever agree, a pack's cell
        // is reading the config and nobody would notice from the item.
        var amount = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .SINGULARITY_CELL_AMOUNT_PER_BATCH;
        var ticks = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .SINGULARITY_CELL_TICKS_PER_BATCH;
        long restoredAmount = amount.get();
        long restoredTicks = ticks.get();
        try {
            amount.set(1000L);
            ticks.set(60L);
            var cell = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                    .Bank(0L, 0L);
            if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.bankOf(cell, 1800L) != 30_000L) {
                helper.fail("the registered cell ignores the config it just changed: 1000 per 60 over 1800"
                        + " should be 30000, it reports "
                        + cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.bankOf(cell, 1800L));
                return;
            }
            if (growth.readyAt(0L, 0L, 1800L) != 21L) {
                helper.fail("a scripted growth followed the server config instead of its own 7 per 600 -"
                        + " it reports " + growth.readyAt(0L, 0L, 1800L));
                return;
            }
        } finally {
            amount.set(restoredAmount);
            ticks.set(restoredTicks);
        }
        // And the record refuses the two nonsense paces rather than growing a cell that pays nothing.
        if (!refusesGrowth(0L, 600L) || !refusesGrowth(7L, 0L) || !refusesGrowth(-1L, 600L)) {
            helper.fail("a growth accepts a batch or interval of zero or less, so a typo in a script makes"
                    + " a dead cell instead of a rejected one");
            return;
        }
        helper.succeed();
    }

    /** Whether constructing a {@code Growth} with these figures throws, as its own guard says it must. */
    private static boolean refusesGrowth(long perBatch, long ticksPerBatch) {
        try {
            new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Growth(
                    () -> appeng.api.stacks.AEFluidKey.of(net.minecraft.world.level.material.Fluids.LAVA),
                    perBatch, ticksPerBatch);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    /**
     * The brackets around the host icon on the computation and crafting panels are the one panel cell eco
     * asks the <em>tier</em> about rather than the block - {@code HostPanelTier.drawMarker} reads
     * {@code IECOTier.getHostMarkerTexture()}. Both facts matter: eco's own tiers leave that method alone, so
     * answering it changes nothing on their panels, and because it is the tier answering, one file serves
     * both machines. The file has to be 13 wide and 16 tall - eco cuts the idle bracket from its lower half
     * and the lit one from its upper half.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void hostMarkerComesFromTheTierAndCarriesBothHalves(GameTestHelper helper) {
        var fromTier = SimplifyTier.L1.getHostMarkerTexture();
        var fromCraftingTier = SimplifyCraftingTier.L1.getHostMarkerTexture();
        if (fromTier == null || fromCraftingTier == null) {
            helper.fail("a tier left getHostMarkerTexture() at eco's null default, so both host panels keep"
                    + " eco's painted brackets");
            return;
        }
        if (!"neoecoprototype".equals(fromTier.getNamespace()) || !"neoecoprototype".equals(
                fromCraftingTier.getNamespace())) {
            helper.fail("a host marker points outside our namespace: " + fromTier + " / " + fromCraftingTier);
            return;
        }
        var size = pngDimensions("assets/" + fromTier.getNamespace() + "/" + fromTier.getPath());
        if (size == null) {
            helper.fail(fromTier + " is not on the classpath, so the marker cell draws nothing");
            return;
        }
        if (size[0] != 13 || size[1] != 16) {
            helper.fail(fromTier + " is " + size[0] + "x" + size[1]
                    + ", eco cuts 13x8 twice from it (y=0 idle, y=8 lit)");
            return;
        }
        helper.succeed();
    }

    /**
     * The one channel that reaches a cell cropped by tier number: eco asks the tier for the sheet itself, by
     * name, and every sprite on it is cut from the answer. Four things are worth pinning. Each name we answer
     * has to name a file that is on the classpath <em>and</em> the same pixel size as the sheet it replaces -
     * a wrong-size sheet stretches across the whole panel and nothing complains. A name we do not have has to
     * answer null, which is eco's own sheet. Both of our tiers answer all six, because the storage panel asks
     * {@code SimplifyTier} and the crafting one asks {@code SimplifyCraftingTier}. And eco's own tiers have to
     * keep answering null - that is what proves shipping six sheets changes nothing on an L4/L6/L9 panel.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelAtlasesMatchTheSheetsTheyReplace(GameTestHelper helper) {
        var ecoSheets = java.util.Map.<String, String[]>of(
                "Computation-BACKGROUND_ATLAS", new String[]{"assets", "neoecoae", "textures", "guis",
                        "eco_cpu_controller.png"},
                "Computation-WIDGETS_ATLAS", new String[]{"assets", "neoecoae", "textures", "guis",
                        "cpu_widget.png"},
                "Crafting-CONTROLLER_ATLAS", new String[]{"assets", "neoecoae", "textures", "guis",
                        "crafting_controller.png"},
                "Crafting-WIDGETS_ATLAS", new String[]{"assets", "neoecoae", "textures", "guis",
                        "crafting_widgets.png"},
                "Storage-CONTROLLER_ATLAS", new String[]{"assets", "neoecoae", "textures", "guis", "storage",
                        "storage_controller.png"},
                "Storage-ATLAS", new String[]{"assets", "neoecoae", "textures", "guis", "storage",
                        "storage_widget.png"});
        for (var atlas : cn.dancingsnow.neoecoprototype.api.PanelAtlas.values()) {
            var texture = atlas.texture();
            var ours = pngDimensions("assets/" + texture.getNamespace() + "/" + texture.getPath());
            if (ours == null) {
                helper.fail(atlas.atlas() + " points at " + texture + ", which is not on the classpath,"
                        + " so the whole panel would draw missing textures");
                return;
            }
            var theirs = pngDimensionsFromMod("neoecoae", ecoSheets.get(atlas.atlas()));
            if (theirs == null) {
                helper.fail("eco's own sheet for " + atlas.atlas() + " is not readable, so this test cannot"
                        + " say our copy matches it - the name or the path moved upstream");
                return;
            }
            if (ours[0] != theirs[0] || ours[1] != theirs[1]) {
                helper.fail(atlas.atlas() + ": our sheet is " + ours[0] + "x" + ours[1]
                        + ", eco's is " + theirs[0] + "x" + theirs[1] + " - every rect on it is cut by"
                        + " eco's own coordinates, so a different size slides them all");
                return;
            }
            if (!texture.equals(SimplifyTier.L1.getPanelAtlas(atlas.atlas()))
                    || !texture.equals(SimplifyCraftingTier.L1.getPanelAtlas(atlas.atlas()))) {
                helper.fail("a tier did not answer " + atlas.atlas() + " with " + texture);
                return;
            }
            if (cn.dancingsnow.neoecoae.api.ECOTier.L4.getPanelAtlas(atlas.atlas()) != null
                    || cn.dancingsnow.neoecoae.api.ECOTier.L9.getPanelAtlas(atlas.atlas()) != null) {
                helper.fail("eco's own tier answered " + atlas.atlas() + " with a sheet of its own, so the"
                        + " null-means-eco's-copy contract this test leans on is not what ships");
                return;
            }
        }
        if (SimplifyTier.L1.getPanelAtlas("Storage-NO_SUCH_ATLAS") != null
                || SimplifyCraftingTier.L1.getPanelAtlas("") != null) {
            helper.fail("a name we do not have was answered with something, and eco would draw that instead"
                    + " of its own sheet");
            return;
        }
        helper.succeed();
    }

    /** PNG header width and height, big-endian at byte 16 - no image library needed for a size check. */
    @javax.annotation.Nullable
    private static int[] pngDimensions(String path) {
        try (var in = NeoECOPrototype.class.getResourceAsStream("/" + path)) {
            return readPngSize(in);
        } catch (java.io.IOException failure) {
            return null;
        }
    }

    /**
     * The same header read out of another mod's jar. {@code getResourceAsStream} cannot see it - NeoForge
     * mounts mod files through its own filesystem, not the classloader - which is what a first draft of
     * {@link #panelAtlasesMatchTheSheetsTheyReplace} measured the hard way.
     */
    @javax.annotation.Nullable
    private static int[] pngDimensionsFromMod(String modId, String... segments) {
        var owner = net.neoforged.fml.ModList.get().getModFileById(modId);
        if (owner == null) {
            return null;
        }
        try (var in = java.nio.file.Files.newInputStream(owner.getFile().findResource(segments))) {
            return readPngSize(in);
        } catch (Exception failure) {
            return null;
        }
    }

    @javax.annotation.Nullable
    private static int[] readPngSize(java.io.InputStream in) throws java.io.IOException {
        if (in == null) {
            return null;
        }
        var head = in.readNBytes(24);
        if (head.length < 24) {
            return null;
        }
        return new int[]{(head[16] & 0xFF) << 24 | (head[17] & 0xFF) << 16 | (head[18] & 0xFF) << 8
                        | (head[19] & 0xFF),
                (head[20] & 0xFF) << 24 | (head[21] & 0xFF) << 16 | (head[22] & 0xFF) << 8
                        | (head[23] & 0xFF)};
    }

    /**
     * eco spells a tier with its own number, and our tier index is 0 - so if the override in
     * {@link SimplifyTier} goes away, the host panel starts calling every L1 cell "L0", a tier that exists
     * nowhere in the ecosystem. The panel now does read the hook - but only through the tier the host hands
     * with each row, which is the half {@code panelRowsFollowTheMachineShape} checks against a formed host;
     * this one checks the enum itself, including the number. That number is the load-bearing part: tier
     * index 0 is what makes an L1 host refuse a higher-tier cell, so if it ever moves, the label is the
     * least of what changed.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelTierNameReadsL1NotTheTierIndex(GameTestHelper helper) {
        for (cn.dancingsnow.neoecoae.api.IECOTier tier : List.of(SimplifyTier.L1, SimplifyTier.L1_REINFORCED,
                SimplifyTier.L1_ENERGIZED_THREADING, SimplifyTier.L1_PARALLEL_SWITCH,
                cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier.L1)) {
            String shown = tier.getPanelTierName().getString();
            if (!"L1".equals(shown)) {
                helper.fail(((Enum<?>) tier).name() + " labels itself \"" + shown + "\", expected L1 - eco's default"
                        + " spells the tier index, which would read L0");
                return;
            }
            if (tier.getTier() != 0) {
                helper.fail(((Enum<?>) tier).name() + " no longer reports tier index 0, so the mount gate moved; it"
                        + " now reports " + tier.getTier());
                return;
            }
        }
        helper.succeed();
    }

    /**
     * The host panel joins a cell to a row by number, so this table is the one place where a wrong digit
     * turns a fluid matrix into a mega item matrix - and the join is invisible until somebody opens the
     * panel. Asserted off the mapping itself rather than through a formed host, because the mapping is pure.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelRowsPutEachMediumWhereItReads(GameTestHelper helper) {
        var item = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getItemCellType();
        var mega = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getMegaItemCellType();
        var fluid = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getFluidCellType();
        var items = java.util.Set.of(appeng.api.stacks.AEKeyType.items());
        var fluids = java.util.Set.of(appeng.api.stacks.AEKeyType.fluids());
        if (SimplifyStorageHostBlockEntity.panelRowOf(item, items) != SimplifyStorageHostBlockEntity.ROW_ITEM) {
            helper.fail("an item matrix must be counted in the item row");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowOf(mega, items)
                != SimplifyStorageHostBlockEntity.ROW_ITEM) {
            helper.fail("a mega item matrix must read as the item medium now that it has no row of its own");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowOf(fluid, fluids)
                != SimplifyStorageHostBlockEntity.ROW_FLUID) {
            helper.fail("a fluid matrix read through eco's cell interface must land in the fluid row");
            return;
        }
        // The same cell seen through the fallback: a matrix whose item does not implement eco's cell
        // interface has only its own cell type to read, and that still says fluid.
        if (SimplifyStorageHostBlockEntity.panelRowOf(fluid, null)
                != SimplifyStorageHostBlockEntity.ROW_FLUID) {
            helper.fail("a fluid matrix without eco's cell interface landed in row "
                    + SimplifyStorageHostBlockEntity.panelRowOf(fluid, null) + " instead of the fluid row");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowOf(
                cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem.CELL_TYPE, null)
                != SimplifyStorageHostBlockEntity.ROW_CONCRETE) {
            helper.fail("the infinite concrete matrix must keep its own row");
            return;
        }
        // Read off eco's own registry rather than off our item classes: our omni families ride eco's
        // registered cell types now, so the row they land in is eco's row, and a private type that merely
        // looks like one would still pass a check written against our own classes.
        var omniType = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "omni"));
        if (omniType != null && SimplifyStorageHostBlockEntity.panelRowOf(omniType, java.util.Set.of())
                != SimplifyStorageHostBlockEntity.ROW_UNIVERSAL) {
            helper.fail("eco's omni cell type must be the row our universal matrix reads as, got row "
                    + SimplifyStorageHostBlockEntity.panelRowOf(omniType, java.util.Set.of()));
            return;
        }
        var complexOmniType = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "complex_omni"));
        if (complexOmniType != null && SimplifyStorageHostBlockEntity.panelRowOf(complexOmniType, java.util.Set.of())
                != SimplifyStorageHostBlockEntity.ROW_COMPLEX_UNIVERSAL) {
            helper.fail("eco's complex omni cell type must have its own row, got row "
                    + SimplifyStorageHostBlockEntity.panelRowOf(complexOmniType, java.util.Set.of()));
            return;
        }
        var wasteType = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                        "neoecoae", "spent_nuclear_waste"));
        if (wasteType != null && SimplifyStorageHostBlockEntity.panelRowOf(wasteType, java.util.Set.of())
                != SimplifyStorageHostBlockEntity.ROW_SPENT_WASTE) {
            helper.fail("eco's spent nuclear waste cell type must have its own row, got row "
                    + SimplifyStorageHostBlockEntity.panelRowOf(wasteType, java.util.Set.of()));
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowOf(
                cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.CELL_TYPE, items)
                != SimplifyStorageHostBlockEntity.ROW_NONE) {
            helper.fail("the singularity cell must not be counted in any row at all");
            return;
        }
        var fluxType = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "flux"));
        if (fluxType != null && SimplifyStorageHostBlockEntity.panelRowOf(fluxType, java.util.Set.of())
                != SimplifyStorageHostBlockEntity.ROW_FLUX) {
            helper.fail("a flux matrix must get the flux row, not the item row it fell into before");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelKindOf(SimplifyStorageHostBlockEntity.ROW_FLUX)
                != cn.dancingsnow.neoecoae.gui.storage.StorageHostUI.CellEntry.KIND_OTHER) {
            helper.fail("the flux row should wear eco's \"other\" icon, not borrow the item one");
            return;
        }
        helper.succeed();
    }

    /**
     * A row's glyph is chosen from {@code StorageTypeLine.resourceId()}, and a null one is not an error -
     * it lands the row on swatch 18, which is one grey box multiplied by the row's accent colour. That is
     * how every row of ours came to wear the same dull shape while eco's host paints eighteen different
     * ones, and nothing about it reaches a log. So the property to pin is "each medium row reads a cell
     * type that is registered under a key the panel can hand back", with the unregistered case asserted
     * alongside it: if {@code getKey} answered something for every type, the first loop would be vacuous.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelRowGlyphIdsComeFromRegisteredTypes(GameTestHelper helper) {
        var registry = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE;
        var media = new java.util.ArrayList<cn.dancingsnow.neoecoae.api.storage.ECOCellType>();
        media.add(cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getItemCellType());
        media.add(cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getMegaItemCellType());
        media.add(cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getFluidCellType());
        for (var path : java.util.List.of("mekanism", "flux", "omni", "complex_omni", "quantum_omni",
                "spent_nuclear_waste")) {
            var type = registry.get(
                    net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", path));
            if (type != null) {
                media.add(type);
            }
        }
        if (media.size() < 3) {
            helper.fail("expected at least the item, mega item and fluid rows to be resolvable, got "
                    + media.size());
            return;
        }
        for (var type : media) {
            var id = registry.getKey(type);
            if (id == null || registry.get(id) != type) {
                helper.fail("a panel row reads a cell type with no registry key (" + type.desc().getString()
                        + "), so its glyph falls back to the grey box");
                return;
            }
        }
        // The contrast that makes the loop above a reading rather than a tautology.
        var unregistered = new cn.dancingsnow.neoecoae.api.storage.ECOCellType(
                net.minecraft.network.chat.Component.literal("control"), 1, false);
        if (registry.getKey(unregistered) != null) {
            helper.fail("an unregistered cell type answered a registry key, so the check above proves nothing");
            return;
        }
        for (var inHouse : java.util.List.of(
                cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem.CELL_TYPE,
                cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.CELL_TYPE,
                cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellItem.INFINITE_CELL_TYPE)) {
            if (registry.getKey(inHouse) != null) {
                helper.fail("this in-house cell type is registered now (" + registry.getKey(inHouse)
                        + "), so the fallback-box note in createStorageTypeLine is stale");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * Which row a mounted cell is drawn in and which types a row sums are two different lookups, and they
     * used to disagree: eco's bulk fluid matrix carries the {@code mega_fluid} cell type, which
     * {@code panelRowOf} routes into the plain fluid row by key type, while the row's own figures matched
     * only {@code fluids} - so the row stood next to a visibly full drive reading "0 types, 0 bytes".
     * Both halves are pure, so this asserts the pair agreeing rather than a fixture. The chemical half can
     * only be checked as far as the suite's mod list reaches: routing needs the cell's own key types, and
     * appmek is not installed here, so it asserts the counted set when eco has registered the type and
     * says nothing otherwise.
     */
    @GameTest(template = "empty", batch = "storage_panel_rows", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void bulkCellsCountInTheRowTheyAreDrawnIn(GameTestHelper helper) {
        var registry = cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE;
        var fluid = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getFluidCellType();
        var megaFluid = registry.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "mega_fluid"));
        if (megaFluid == null) {
            helper.fail("eco's mega_fluid cell type is not registered, so this guard is reading nothing");
            return;
        }
        var megaItem = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getMegaItemCellType();
        if (SimplifyStorageHostBlockEntity.panelRowOf(megaFluid,
                java.util.Set.of(appeng.api.stacks.AEKeyType.fluids()))
                != SimplifyStorageHostBlockEntity.ROW_FLUID) {
            helper.fail("a bulk fluid matrix is no longer routed into the fluid row, so the widened "
                    + "counted set is counting the wrong row");
            return;
        }
        if (!SimplifyStorageHostBlockEntity.panelRowCountedTypes(fluid, SimplifyStorageHostBlockEntity.ROW_FLUID)
                .contains(megaFluid)) {
            helper.fail("the fluid row is drawn under a bulk fluid matrix but does not count its types "
                    + "or bytes - that is the 0/0 the panel showed");
            return;
        }
        var chemical = registry.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "mekanism"));
        var megaChemical = registry.get(
                net.minecraft.resources.ResourceLocation.fromNamespaceAndPath("neoecoae", "mega_chemical"));
        if (chemical != null && megaChemical != null && !SimplifyStorageHostBlockEntity
                .panelRowCountedTypes(chemical, SimplifyStorageHostBlockEntity.ROW_CHEMICAL)
                .contains(megaChemical)) {
            helper.fail("the chemical row does not count bulk chemical matrices either");
            return;
        }
        // Control: each widening stays inside its own medium.
        var item = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getItemCellType();
        var fluidRow = cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem.getFluidCellType();
        if (!SimplifyStorageHostBlockEntity.panelRowCountedTypes(item, SimplifyStorageHostBlockEntity.ROW_ITEM)
                .contains(megaItem)) {
            helper.fail("bulk item matrices are routed into the item row, but the item row does not count"
                    + " them - they would be invisible in the numbers");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowCountedTypes(item, SimplifyStorageHostBlockEntity.ROW_ITEM)
                .contains(megaFluid)) {
            helper.fail("the item row picked up the bulk fluid type; the widening leaked past its media");
            return;
        }
        if (SimplifyStorageHostBlockEntity.panelRowCountedTypes(fluidRow, SimplifyStorageHostBlockEntity.ROW_FLUID)
                .contains(megaItem)) {
            helper.fail("the fluid row picked up the bulk item type");
            return;
        }
        helper.succeed();
    }

    /**
     * A row that draws correctly is not necessarily clickable: eco's grid reads two handles back out of
     * every entry, and {@code StorageCellGrid$CellSlot.onMouseDown} returns without a word when the
     * interaction id is null - which is exactly how "our storage matrices cannot be interacted with"
     * reached us. The position is the other one; without it the row is not even drawn as occupied.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelRowsCarryTheHandlesEcoClicksThrough(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    var drives = storageHost.getCluster() == null
                            ? java.util.List.<cn.dancingsnow.neoecoprototype.blockentity.storage
                                    .SimplifyDriveBlockEntity>of()
                            : storageHost.getCluster().getDrives();
                    if (drives.isEmpty()) {
                        helper.fail("the built room has no drives, so this scan proves nothing");
                        return;
                    }
                    var filled = false;
                    for (var drive : drives) {
                        if (drive.insertCell(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get()))) {
                            filled = true;
                            break;
                        }
                    }
                    if (!filled) {
                        helper.fail("none of the " + drives.size() + " drives would take an L1 item matrix,"
                                + " so there is no row to check");
                        return;
                    }
                    var entries = storageHost.createCellEntries();
                    if (entries.isEmpty()) {
                        helper.fail("a drive holding an L1 item matrix produced no panel row at all");
                        return;
                    }
                    // The rectangle itself. eco's StoragePanelState.cellPage works out the face width as
                    // ceil(size / 3) and then reads row r column c at r * width + c, so one missing row does
                    // not just drop a slot - it narrows the width and skews every row below the gap, and the
                    // last column falls off the page into the panel background's own "no bay" cross.
                    if (entries.size() != drives.size()) {
                        helper.fail("the panel was handed " + entries.size() + " rows for " + drives.size()
                                + " drives, so eco skews the face: "
                                + describeDroppedDrives(drives, entries));
                        return;
                    }
                    for (var entry : entries) {
                        if (entry.position() == null || entry.interactionId() == null) {
                            helper.fail("a panel row reached eco with position=" + entry.position()
                                    + " and interactionId=" + entry.interactionId()
                                    + ", so its grid refuses every click on that row");
                            return;
                        }
                    }
                    // The label eco actually has to hand out: a row that does not name its tier is spelled
                    // from the entry's tier *number*, and ours is 0 - so the panel would print "L0", a tier
                    // that exists nowhere. This is the half the enum-side test cannot see.
                    for (var entry : entries) {
                        if (entry.stack().isEmpty()) {
                            continue;
                        }
                        var label = entry.panelTierName();
                        if (label == null) {
                            helper.fail("a mounted L1 matrix reached eco with no tier name, so the panel"
                                    + " spells it from the tier number and reads L" + entry.tier());
                            return;
                        }
                        if (!"L1".equals(label.getString())) {
                            helper.fail("a mounted L1 matrix reached eco labelled \"" + label.getString()
                                    + "\", expected L1");
                            return;
                        }
                    }
                    // The other half of "cannot interact": the drives that hold nothing have to be listed
                    // too, or there is no slot to drop a matrix onto and eco's insert RPC is never sent.
                    var showsFilled = entries.stream().anyMatch(e -> !e.stack().isEmpty());
                    var showsEmpty = entries.stream().anyMatch(e -> e.stack().isEmpty());
                    if (!showsFilled || !showsEmpty) {
                        helper.fail("the panel rows are filled=" + showsFilled + " empty=" + showsEmpty
                                + " out of " + entries.size() + " rows for " + drives.size()
                                + " drives - both kinds have to be listed or one direction of the drag dies");
                        return;
                    }
                    helper.succeed();
                });
    }

    /** Names the drives that vanished from the panel rows, so the failure above says which cell dropped out. */
    private static String describeDroppedDrives(
            java.util.List<cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity> drives,
            java.util.List<cn.dancingsnow.neoecoae.gui.storage.StorageHostUI.CellEntry> entries) {
        var listed = entries.stream()
                .map(cn.dancingsnow.neoecoae.gui.storage.StorageHostUI.CellEntry::position)
                .filter(java.util.Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
        var missing = drives.stream()
                .map(net.minecraft.world.level.block.entity.BlockEntity::getBlockPos)
                .filter(pos -> !listed.contains(pos))
                .limit(3)
                .toList();
        return "drives with no row: " + missing;
    }

    /**
     * The singularity matrix belongs to no type row - it reports an unbounded byte total and a world-clock
     * stock, so whichever row it landed in would read as that medium's own number. "No row" is a counting
     * decision, and it used to be implemented by leaving the drive out of the list entirely, which broke
     * eco's rectangle and made the panel skew one row per singularity installed. The row has to exist, be
     * clickable, and carry none of the numbers.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void singularityCellGetsARowButNoNumbers(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    var drives = storageHost.getCluster() == null
                            ? java.util.List.<cn.dancingsnow.neoecoprototype.blockentity.storage
                                    .SimplifyDriveBlockEntity>of()
                            : storageHost.getCluster().getDrives();
                    var target = drives.stream()
                            .filter(drive -> drive.insertCell(new ItemStack(
                                    ModRegistration.SIMPLIFY_SINGULARITY_CELL.get())))
                            .findFirst()
                            .orElse(null);
                    if (target == null) {
                        helper.fail("none of the " + drives.size() + " drives took a singularity matrix,"
                                + " so this scan has nothing to read");
                        return;
                    }
                    var entries = storageHost.createCellEntries();
                    if (entries.size() != drives.size()) {
                        helper.fail("a singularity matrix is still a cell standing on a drive, but the panel"
                                + " got " + entries.size() + " rows for " + drives.size() + " drives: "
                                + describeDroppedDrives(drives, entries));
                        return;
                    }
                    var row = entries.stream()
                            .filter(entry -> target.getBlockPos().equals(entry.position()))
                            .findFirst()
                            .orElse(null);
                    if (row == null) {
                        helper.fail("the drive at " + target.getBlockPos() + " holds a singularity matrix"
                                + " but no row points at it");
                        return;
                    }
                    if (row.stack().isEmpty() || row.interactionId() == null) {
                        helper.fail("the singularity row carries no stack or no interaction id, so the panel"
                                + " cannot draw it or take it back out");
                        return;
                    }
                    if (row.typeId() != SimplifyStorageHostBlockEntity.ROW_NONE
                            || row.usedBytes() != 0L || row.totalBytes() != 0L) {
                        helper.fail("the singularity row claims typeId=" + row.typeId() + ", bytes="
                                + row.usedBytes() + "/" + row.totalBytes()
                                + " - it must hold no numbers or the byte bar inherits an unbounded total");
                    }
                    helper.succeed();
                });
    }

    /**
     * The byte bar pins itself to 100% on a host that reports "0 used": eco's
     * {@code StoragePanelState.usage} returns 1.0 for any row whose per-row {@code infinite} flag is set, and
     * it only swaps the percentage for its "unbounded" glyph when the panel-level {@code infiniteStorage}
     * supplier is set too. We never set that one, so a row that claims infinity leaves the panel saying
     * 100.0% next to "0 / 27M". The three facts below are what have to hold together; the unbounded cell is
     * meant to drop out of both sums through its {@code Long.MAX_VALUE} byte total, not through this flag.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void unboundedCellDoesNotPinTheByteBar(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    var drives = storageHost.getCluster() == null
                            ? java.util.List.<cn.dancingsnow.neoecoprototype.blockentity.storage
                                    .SimplifyDriveBlockEntity>of()
                            : storageHost.getCluster().getDrives();
                    var target = drives.stream()
                            .filter(drive -> drive.insertCell(new ItemStack(
                                    ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get())))
                            .findFirst()
                            .orElse(null);
                    if (target == null) {
                        helper.fail("none of the " + drives.size() + " drives took an infinite matrix,"
                                + " so this scan has nothing to read");
                        return;
                    }
                    var row = storageHost.createCellEntries().stream()
                            .filter(entry -> target.getBlockPos().equals(entry.position()))
                            .findFirst()
                            .orElse(null);
                    if (row == null) {
                        helper.fail("the drive at " + target.getBlockPos() + " holds an infinite matrix but"
                                + " no row points at it");
                        return;
                    }
                    if (row.infinite()) {
                        helper.fail("the row sets eco's per-row infinite flag, which makes"
                                + " StoragePanelState.usage return 1.0 for the whole host");
                        return;
                    }
                    if (row.totalBytes() != Long.MAX_VALUE) {
                        helper.fail("the unbounded row reports totalBytes=" + row.totalBytes()
                                + ", expected Long.MAX_VALUE - that value is what drops it out of both sums");
                        return;
                    }
                    var config = storageHost.createStoragePanelConfig(
                            helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL));
                    if (config.infiniteStorage().getAsBoolean() || config.migratingToInfinite().getAsBoolean()) {
                        helper.fail("the panel-level infinite suppliers are on while no row claims infinity;"
                                + " eco would hide the byte figure entirely");
                    }
                    helper.succeed();
                });
    }

    /**
     * Which hand the cell comes out into. eco splits this on the shift key; we used to drop the flag on the
     * floor, so a plain click - which for every upstream player means "put it on my cursor" - sent the cell
     * to the backpack instead. Both branches are asserted, and so are the two refusals that keep the cell
     * inside the machine rather than on the ground: a loaded cursor without shift, no free slot with it.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelTakeFollowsTheShiftSplit(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)
                            || storageHost.getCluster() == null) {
                        helper.fail("no formed L1 storage host to take a cell out of");
                        return;
                    }
                    var cellItem = ModRegistration.SIMPLIFY_ITEM_CELL_1K.get();
                    var drive = storageHost.getCluster().getDrives().stream()
                            .filter(d -> !d.hasCell())
                            .findFirst().orElse(null);
                    if (drive == null || !drive.insertCell(new ItemStack(cellItem))) {
                        helper.fail("no empty drive would take the fixture cell, so the take path cannot be"
                                + " measured");
                        return;
                    }
                    var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                    var menu = player.containerMenu;
                    var inventory = player.getInventory();
                    var at = drive.getBlockPos();
                    // Driven through the config eco's own client receives, not through the method behind
                    // it: the bug this guards was in the lambda that forwards the shift flag, and a test
                    // that calls the method directly passes no matter what that lambda does.
                    var extractor = storageHost.createStoragePanelConfig(player).cellExtractor();
                    var row = storageHost.createCellEntries().stream()
                            .filter(entry -> at.equals(entry.position()) && !entry.stack().isEmpty())
                            .findFirst().orElse(null);
                    if (row == null) {
                        helper.fail("no panel row names the drive holding the fixture cell, so eco's grid has"
                                + " nothing to click on");
                        return;
                    }

                    menu.setCarried(new ItemStack(net.minecraft.world.item.Items.STONE));
                    if (extractor.extract(row.position(), row.interactionId(), false) || !drive.hasCell()
                            || !menu.getCarried().is(net.minecraft.world.item.Items.STONE)) {
                        helper.fail("a plain click with a loaded cursor moved something: took="
                                + (drive.hasCell() ? "no" : "yes") + " cursor=" + menu.getCarried()
                                + " - eco gives up at StorageCellUiExtraction 224..236");
                        return;
                    }

                    menu.setCarried(ItemStack.EMPTY);
                    if (!extractor.extract(row.position(), row.interactionId(), false)) {
                        helper.fail("the panel refused a plain click on a drive that holds a cell");
                        return;
                    }
                    if (drive.hasCell() || !menu.getCarried().is(cellItem)) {
                        helper.fail("a plain click left the cell with the drive holding=" + drive.hasCell()
                                + " and the cursor holding " + menu.getCarried().getCount()
                                + ", expected it on the cursor");
                        return;
                    }
                    menu.setCarried(ItemStack.EMPTY);
                    if (!drive.insertCell(new ItemStack(cellItem))) {
                        helper.fail("could not put the fixture cell back for the shift case");
                        return;
                    }

                    emptyBackpack(inventory);
                    if (!extractor.extract(row.position(), row.interactionId(), true)) {
                        helper.fail("shift-click refused although the backpack had room");
                        return;
                    }
                    if (drive.hasCell() || !menu.getCarried().isEmpty() || !holds(inventory, cellItem)) {
                        helper.fail("a shift click left the drive holding=" + drive.hasCell()
                                + ", cursor=" + menu.getCarried().getCount()
                                + ", backpack holds it=" + holds(inventory, cellItem));
                        return;
                    }

                    if (!drive.insertCell(new ItemStack(cellItem))) {
                        helper.fail("could not put the fixture cell back for the full-backpack case");
                        return;
                    }
                    emptyBackpack(inventory);
                    for (int slot = 0; slot < inventory.items.size(); slot++) {
                        inventory.setItem(slot, new ItemStack(net.minecraft.world.item.Items.STONE));
                    }
                    if (extractor.extract(row.position(), row.interactionId(), true) || !drive.hasCell()
                            || holds(inventory, cellItem)) {
                        helper.fail("shift took the cell out with every slot full: drive still holds it="
                                + drive.hasCell() + " - eco gives up at 217..223 rather than dropping it");
                        return;
                    }
                    helper.succeed();
                });
    }

    /** Empties the player's main row list; {@code Inventory} has no clear() of its own. */
    private static void emptyBackpack(net.minecraft.world.entity.player.Inventory inventory) {
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            inventory.setItem(slot, ItemStack.EMPTY);
        }
    }

    /** @return true when the player's main row list carries one of that item. */
    private static boolean holds(net.minecraft.world.entity.player.Inventory inventory,
            net.minecraft.world.item.Item item) {
        for (int slot = 0; slot < inventory.items.size(); slot++) {
            if (inventory.getItem(slot).is(item)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Which panel row a C1 crystal-array drive sits in, and whether the cell inside it reaches the row.
     *
     * <p>eco picks the row in {@code NEComputationCluster.addBlockEntity} by asking whether the block under
     * the drive {@code is(NEBlocks.COMPUTATION_TRANSMITTER)} - its own transmitter. Ours is a different
     * block and that test compares identity, so every L1 drive went to the lower list, the upper list stayed
     * empty, and the top row of the panel became eleven slots that swallow their own click because
     * {@code ComputationDriveEntry.EMPTY} carries no position. The assertion that matters is the split: this
     * shape has one drive column standing on the transmitter row and one on the casing row, so a correct
     * machine has to show drives in both rows.</p>
     */
    @GameTest(template = "l1_room", batch = "computation_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void computationDrivesSplitIntoBothPanelRows(GameTestHelper helper) {
        var hostPos = new BlockPos(13, 3, 4);
        buildComputationStructureKept(helper, hostPos,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false, 1,
                (controller, finish) -> helper.runAfterDelay(3, () -> {
                    var drives = new java.util.ArrayList<
                            cn.dancingsnow.neoecoprototype.blockentity.computation
                                    .SimplifyComputationDriveBlockEntity>();
                    if (controller.getCluster() != null) {
                        controller.getCluster().getBlockEntities().forEachRemaining(member -> {
                            if (member instanceof cn.dancingsnow.neoecoprototype.blockentity.computation
                                    .SimplifyComputationDriveBlockEntity drive) {
                                drives.add(drive);
                            }
                        });
                    }
                    if (drives.size() < 2) {
                        helper.fail("the built C1 machine has " + drives.size()
                                + " crystal-array drives, and this needs the pair the shape defines");
                        finish.run();
                        return;
                    }
                    var top = controller.getComputationUiDrives(true);
                    var bottom = controller.getComputationUiDrives(false);
                    var problems = new java.util.ArrayList<String>();
                    var inTop = 0;
                    var inBottom = 0;
                    for (var drive : drives) {
                        var belowTransmitter = helper.getLevel()
                                .getBlockState(drive.getBlockPos().below())
                                .getBlock() instanceof cn.dancingsnow.neoecoprototype
                                        .block.computation.SimplifyComputationTransmitterBlock;
                        var listedTop = listed(top, drive.getBlockPos());
                        var listedBottom = listed(bottom, drive.getBlockPos());
                        if (belowTransmitter) {
                            inTop++;
                            if (!listedTop || listedBottom) {
                                problems.add(drive.getBlockPos() + " stands on a transmitter but is listed"
                                        + " top=" + listedTop + " bottom=" + listedBottom);
                            }
                        } else {
                            inBottom++;
                            if (listedTop || !listedBottom) {
                                problems.add(drive.getBlockPos() + " does not stand on a transmitter but is"
                                        + " listed top=" + listedTop + " bottom=" + listedBottom);
                            }
                        }
                    }
                    if (inTop == 0 || inBottom == 0) {
                        problems.add("the shape put all " + drives.size() + " drives in one row (top="
                                + inTop + " bottom=" + inBottom + "), so the other row is dead slots");
                    }
                    // The other half of the report: a cell that is really in the drive has to come through
                    // to the row, or the panel shows a machine that lies about what it holds.
                    var withCell = drives.get(0);
                    var cellItem = ModRegistration.SIMPLIFY_COMPUTATION_CELL_1M.get();
                    if (!withCell.isItemValid(new ItemStack(cellItem))) {
                        problems.add("the drive at " + withCell.getBlockPos()
                                + " rejects our own CE1 cell, so the display path cannot be measured");
                    } else {
                        withCell.setCellStack(new ItemStack(cellItem));
                        var rows = listed(controller.getComputationUiDrives(true), withCell.getBlockPos())
                                ? controller.getComputationUiDrives(true)
                                : controller.getComputationUiDrives(false);
                        var shown = rows.stream()
                                .filter(entry -> withCell.getBlockPos().equals(entry.position()))
                                .findFirst().orElse(null);
                        if (shown == null || shown.stack().isEmpty()) {
                            problems.add("the drive at " + withCell.getBlockPos() + " holds a CE1 cell but"
                                    + " its panel row shows stack="
                                    + (shown == null ? "no row at all" : shown.stack()));
                        }
                        withCell.setCellStack(ItemStack.EMPTY);
                    }
                    if (!problems.isEmpty()) {
                        helper.fail(String.join(" | ", problems));
                        return;
                    }
                    finish.run();
                    helper.succeed();
                }));
    }

    /** @return true when one of the panel's rows names that position. */
    private static boolean listed(
            java.util.List<cn.dancingsnow.neoecoae.gui.computation.ComputationDriveEntry> rows,
            net.minecraft.core.BlockPos pos) {
        return rows.stream().anyMatch(entry -> pos.equals(entry.position()));
    }


    /**
     * A shape parked for one host must not be readable by whatever stands there later. The handoff table
     * is keyed by dimension rather than by world, so a host pulled out before its one-tick handoff ran
     * would otherwise leave a face for the next machine to wear - and in one client process that next
     * machine can be in a different save entirely.
     */
    @GameTest(template = "empty", batch = "computation_shape_stale", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void pendingShapeDiesWithTheHost(GameTestHelper helper) {
        var host = ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get();
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(0, 1, 0));
        var formed = host.defaultBlockState()
                .setValue(cn.dancingsnow.neoecoae.blocks.NEBlock.FORMED, true);

        level.setBlock(pos, formed, Block.UPDATE_ALL);
        host.publishShape(level, pos, new SimplifyComputationSystemBlock.Shape(true, true));
        // Take the host out before the scheduled tick can run, then put a fresh one in its place.
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), Block.UPDATE_ALL);
        level.setBlock(pos, formed, Block.UPDATE_ALL);
        host.tick(formed, level, pos, level.random);

        var after = level.getBlockState(pos);
        if (after.getValue(SimplifyComputationSystemBlock.COMMUNICATION_INTERFACE)
                || after.getValue(SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE)) {
            helper.fail("a shape parked for a host that was removed was applied to the block that replaced it: "
                    + after);
            return;
        }
        helper.succeed();
    }


    /**
     * Where the one block a player hunts comets for actually lands. The piece only ever runs in the End, so
     * the suite drives the placement through the same sink the piece hands its writer: the assertion is about
     * which cell gets it, and about the table that has to give the block back when it is mined.
     */
    @GameTest(template = "empty", batch = "meteorite_core_at_center", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void meteoriteCoreLandsAtTheCometCentre(GameTestHelper helper) {
        var level = helper.getLevel();
        var centre = helper.absolutePos(new BlockPos(2, 3, 2));
        cn.dancingsnow.neoecoprototype.worldgen.CryotheumMeteoriteStructure.MeteoritePiece
                .placeCore(centre, (pos, state) -> level.setBlock(pos, state, Block.UPDATE_ALL));
        if (!level.getBlockState(centre).is(ModRegistration.METEORITE_CORE.get())) {
            helper.fail("the centre cell holds " + level.getBlockState(centre).getBlock()
                    + " instead of the meteorite core");
            return;
        }
        var table = readResource("/data/neoecoprototype/loot_table/blocks/meteorite_core.json", helper);
        if (!table.contains("\"neoecoprototype:meteorite_core\"")) {
            helper.fail("the core's loot table does not give the core back, so mining it destroys it: "
                    + table);
            return;
        }
        helper.succeed();
    }

    /**
     * The dissociation machine's materials, registered before any of the machine exists. The wiring between
     * a fluid, its flowing half, its type and its bucket is the part worth asserting: get it crossed and
     * nothing complains at runtime, the bucket just comes out empty in the hand.
     */
    @GameTest(template = "empty", batch = "dissociation_materials", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociationMaterialsAreWiredToEachOther(GameTestHelper helper) {
        var coreItem = ModRegistration.METEORITE_CORE_ITEM.get();
        if (!(coreItem instanceof net.minecraft.world.item.BlockItem coreBlockItem)
                || coreBlockItem.getBlock() != ModRegistration.METEORITE_CORE.get()) {
            helper.fail("the meteorite core block has no item pointing back at it, the item is " + coreItem);
            return;
        }
        var dust = new ItemStack(ModRegistration.SKY_STONE_DUST.get());
        if (dust.getMaxStackSize() != 64) {
            helper.fail("sky stone dust should stack like any other dust, it stacks to "
                    + dust.getMaxStackSize());
            return;
        }
        var still = ModRegistration.DISSOLUTION_SOLUTION.get();
        var flowing = ModRegistration.FLOWING_DISSOLUTION_SOLUTION.get();
        if (!(still instanceof net.minecraft.world.level.material.FlowingFluid stillFlowing)
                || !(flowing instanceof net.minecraft.world.level.material.FlowingFluid)) {
            helper.fail("both halves of a fluid have to be FlowingFluid or the world can place neither");
            return;
        }
        if (stillFlowing.getFlowing() != flowing) {
            helper.fail("the source half does not name our own flowing half as its pair, it answers "
                    + stillFlowing.getFlowing());
            return;
        }
        if (still.getFluidType() != ModRegistration.DISSOLUTION_SOLUTION_TYPE.get()) {
            helper.fail("the fluid is not carrying our own fluid type, so the client would ask the wrong "
                    + "extension for its artwork");
            return;
        }
        var bucketItem = ModRegistration.DISSOLUTION_BUCKET.get();
        var bucket = new ItemStack(bucketItem);
        if (bucket.getMaxStackSize() != 1) {
            helper.fail("a filled bucket must not stack, it stacks to " + bucket.getMaxStackSize());
            return;
        }
        if (bucketItem.content != still) {
            helper.fail("the bucket carries " + bucketItem.content + " instead of the dissolution solution -"
                    + " crossed wiring shows up as an empty bucket in the hand, not as an error");
            return;
        }
        // A player reported that no block could be placed into the spilled bath. Placement asks the
        // state whether it is replaceable, and that flag is copied straight out of the block's Properties,
        // so a fluid built without .replaceable() simply refuses every block - silently, with no error.
        var placed = ModRegistration.DISSOLUTION_SOLUTION_BLOCK.get().defaultBlockState();
        if (!placed.canBeReplaced()) {
            helper.fail("the bath's block state is not replaceable, so holding a block against it does"
                    + " nothing; vanilla water is replaceable");
            return;
        }
        helper.succeed();
    }

    /**
     * The dissociation table: the shipped one has to load, and the arithmetic that turns weights into shares
     * has to hold without a level, a grid or a GUI. The refusals are asserted too - a table that cannot be
     * turned into a distribution must fail at load, where the recipe manager names the file, rather than
     * silently rolling nothing at the player.
     */
    @GameTest(template = "empty", batch = "dissociation_table", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociationTableLoadsAndNormalises(GameTestHelper helper) {
        var loaded = helper.getLevel().getRecipeManager()
                .getAllRecipesFor(cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe.TYPE);
        cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe table = null;
        for (var holder : loaded) {
            if (holder.value().input().getItem() == ModRegistration.METEORITE_CORE.get().asItem()) {
                table = holder.value();
                break;
            }
        }
        if (table == null) {
            helper.fail("the shipped dissociation table for the meteorite core did not load - "
                    + loaded.size() + " dissociation recipe(s) are registered");
            return;
        }
        double sum = 0.0D;
        for (double share : table.weights().values()) {
            sum += share;
        }
        var core = net.minecraft.world.item.Items.COPPER_INGOT;
        var dust = net.minecraft.world.item.Items.GLOWSTONE_DUST;
        if (Math.abs(sum - 1.0D) > 1.0E-9D) {
            helper.fail("normalised shares must add up to exactly one, they add to " + sum);
            return;
        }
        var skyStone = net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                net.minecraft.resources.ResourceLocation.parse("ae2:sky_stone_block"));
        var skyStoneDust = ModRegistration.SKY_STONE_DUST.get();
        if (skyStone == null || skyStone == net.minecraft.world.item.Items.AIR) {
            helper.fail("the shipped table names ae2:sky_stone_block, which is not registered here");
            return;
        }
        if (Math.abs(table.weights().getOrDefault(skyStone, 0.0D) - 0.4D) > 1.0E-9D
                || Math.abs(table.weights().getOrDefault(skyStoneDust, 0.0D) - 0.4D) > 1.0E-9D) {
            helper.fail("the meteorite and its dust should each hold 0.4 of the shares, the table reads "
                    + table.weights());
            return;
        }
        if (table.maxPerProduct() != 120 || table.draws() != 12 || table.perHit() != 10) {
            helper.fail("one run should be able to put " + table.maxPerProduct()
                    + " of a single product into one slot (12 draws x 10), the table says draws="
                    + table.draws() + " perHit=" + table.perHit());
            return;
        }
        if (table.ticksPerRun() != 200 || Math.abs(table.powerPerRun() - 100000.0D) > 1.0E-9D) {
            helper.fail("the shipped table should run 200 ticks on 100000 AE, it says "
                    + table.ticksPerRun() + " ticks / " + table.powerPerRun() + " AE");
            return;
        }
        int counted = 0;
        for (int n : table.roll(net.minecraft.util.RandomSource.create(20261008L)).values()) {
            counted += n;
        }
        if (counted != 120) {
            helper.fail("a roll must hand out exactly draws x perHit = 120 items, it handed out " + counted);
            return;
        }
        var again = table.roll(net.minecraft.util.RandomSource.create(20261008L));
        if (!again.equals(table.roll(net.minecraft.util.RandomSource.create(20261008L)))) {
            helper.fail("the same seed must give the same roll, or the result cannot be tested at all");
            return;
        }
        var water = new net.neoforged.neoforge.fluids.FluidStack(
                net.minecraft.world.level.material.Fluids.WATER, 1000);
        var thirds = new cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe(
                new ItemStack(ModRegistration.METEORITE_CORE.get()),
                java.util.Map.of(core.asItem(), 1.0D, dust.asItem(), 1.0D,
                        net.minecraft.world.item.Items.IRON_INGOT, 1.0D),
                12, 10, water, water, 100000.0D, 200);
        if (Math.abs(thirds.weights().get(core.asItem()) - 1.0D / 3.0D) > 1.0E-9D) {
            helper.fail("1/1/1 should normalise to a third each, it reads " + thirds.weights());
            return;
        }
        double thirdsSum = 0.0D;
        for (double share : thirds.weights().values()) {
            thirdsSum += share;
        }
        if (thirdsSum != 1.0D) {
            helper.fail("the remainder must be handed to the last product so the shares close exactly, sum is "
                    + thirdsSum);
            return;
        }
        if (!refusesZeroWeights(water)) {
            helper.fail("a table whose weights all add to zero should be refused at load, it was accepted");
            return;
        }
        if (!refusesTooManyProducts(water)) {
            helper.fail("a table with more products than the machine has slots should be refused, it was"
                    + " accepted - the extra products would be rolled into slots that do not exist");
            return;
        }
        helper.succeed();
    }

    private static boolean refusesZeroWeights(net.neoforged.neoforge.fluids.FluidStack water) {
        try {
            new cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe(
                    ItemStack.EMPTY,
                    java.util.Map.of(net.minecraft.world.item.Items.COAL, 0.0D),
                    12, 10, water, water, 100000.0D, 200);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    private static boolean refusesTooManyProducts(net.neoforged.neoforge.fluids.FluidStack water) {
        java.util.Map<net.minecraft.world.item.Item, Double> tooMany = new java.util.LinkedHashMap<>();
        var items = java.util.List.of(net.minecraft.world.item.Items.COAL, net.minecraft.world.item.Items.IRON_INGOT,
                net.minecraft.world.item.Items.GOLD_INGOT, net.minecraft.world.item.Items.DIAMOND,
                net.minecraft.world.item.Items.EMERALD, net.minecraft.world.item.Items.LAPIS_LAZULI,
                net.minecraft.world.item.Items.REDSTONE, net.minecraft.world.item.Items.QUARTZ,
                net.minecraft.world.item.Items.AMETHYST_SHARD, net.minecraft.world.item.Items.COPPER_INGOT,
                net.minecraft.world.item.Items.GLOWSTONE_DUST, net.minecraft.world.item.Items.GUNPOWDER,
                net.minecraft.world.item.Items.SLIME_BALL);
        for (var item : items) {
            tooMany.put(item, 1.0D / items.size());
        }
        try {
            new cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe(
                    ItemStack.EMPTY, tooMany, 12, 10, water, water, 100000.0D, 200);
            return false;
        } catch (IllegalArgumentException expected) {
            return true;
        }
    }

    /**
     * The charge has to be on disk. eco writes no NBT hook at all for its cell and leaves the
     * {@code @Persisted} field to LDLib's block entity mixin, which only serialises anything for a
     * block entity that is itself a persist-managed holder - so this reads back through the same call
     * a chunk load makes, and the fresh cell is what proves the number came off the tag rather than
     * out of a field default.
     */
    @GameTest(template = "empty", batch = "energy_cell_charge_reload", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energyCellChargeSurvivesAChunkSave(GameTestHelper helper) {
        var plotPos = new BlockPos(0, 1, 0);
        helper.setBlock(plotPos, ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get());
        var level = helper.getLevel();
        var pos = helper.absolutePos(plotPos);
        if (!(level.getBlockEntity(pos) instanceof SimplifyEnergyCellBlockEntity cell)) {
            helper.fail("the energy cell block placed without a block entity of its own type");
            return;
        }
        cell.injectAEPower(42_000D, Actionable.MODULATE);
        if (cell.getAECurrentPower() <= 0D) {
            helper.fail("charging the cell by hand left it at " + cell.getAECurrentPower()
                    + " AE, so there is nothing here for a save to carry");
            return;
        }
        var reloaded = net.minecraft.world.level.block.entity.BlockEntity.loadStatic(
                pos, level.getBlockState(pos), cell.saveWithFullMetadata(level.registryAccess()),
                level.registryAccess());
        if (!(reloaded instanceof SimplifyEnergyCellBlockEntity read)) {
            helper.fail("the chunk tag of a charged cell no longer reads back as its own block entity,"
                    + " it came up as " + (reloaded == null ? "nothing" : reloaded.getClass().getName()));
            return;
        }
        if (Math.abs(read.getAECurrentPower() - 42_000D) > 0.001D) {
            helper.fail("a cell charged to 42000 AE reloaded holding " + read.getAECurrentPower()
                    + " AE: the charge is not being written to disk");
            return;
        }
        helper.succeed();
    }

    /**
     * The induction card must install into our interfaces and pattern providers, and must not start
     * naming them: AE2's card tooltip folds every machine that shares a group key into that group's
     * one line, while an ungrouped machine prints its own name instead. Our names differ from AE2's,
     * so a missing group key shows up here as a new line on somebody else's item.
     */
    @GameTest(template = "empty", batch = "fe_induction_card", timeoutTicks = 100, required = false,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void inductionCardAllowsOurMachinesWithoutNamingThem(GameTestHelper helper) {
        if (ModRegistration.OPTIONAL_FE_CELL_1M == null) {
            helper.succeed();
            return;
        }
        var card = AppFluxTestProbe.inductionCard();
        var machines = java.util.List.<net.minecraft.world.level.ItemLike>of(
                ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get(),
                ModRegistration.POWERED_INTERFACE_PART.get(),
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_ITEM.get(),
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_PART.get(),
                ModRegistration.SIMPLIFY_PATTERN_PROVIDER_ITEM.get(),
                ModRegistration.CABLE_PATTERN_PROVIDER_PART.get());
        var lines = appeng.api.upgrades.Upgrades.getTooltipLinesForCard(card);
        for (var machine : machines) {
            if (appeng.api.upgrades.Upgrades.getMaxInstallable(card, machine) != 1) {
                helper.fail(machine.asItem().getDescription().getString()
                        + " does not take an induction card, so it cannot receive power");
                return;
            }
            var name = machine.asItem().getDescription().getString();
            for (var line : lines) {
                if (line.getString().equals(name)) {
                    helper.fail("the induction card tooltip lists " + name
                            + " on its own line instead of folding it into the family it belongs to");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * eco's two recipe viewer pages read one thing only: {@code NEMultiBlocks.DEFINITIONS}, and joining it
     * is the no-arg {@code Builder.create()} - {@code create(Consumer)} deliberately does not join. We stay
     * on the non-joining overload and hand our definitions to both viewer plugins directly, because joining
     * it this early runs eco's own class initialiser before its server config has loaded. The companion
     * {@link #ecoDefinitionsKeepAUsableBuildRange} is what catches it if anyone "fixes" this by flipping
     * the overload without moving the timing.
     */
    @GameTest(template = "empty", batch = "viewer_definitions", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ourDefinitionsStayOutOfEcoListAndEcoStaysUsable(GameTestHelper helper) {
        var owners = new java.util.HashSet<net.minecraft.resources.ResourceLocation>();
        for (var definition : cn.dancingsnow.neoecoae.all.NEMultiBlocks.DEFINITIONS) {
            owners.add(BuiltInRegistries.BLOCK.getKey(definition.getOwner().value()));
        }
        // Named as List<Block> on purpose: letting javac infer the element type from four different host
        // classes makes it compute an intersection over eco's self-referential NEBlock<C, E> generics, and
        // that fails to reconcile NEBlockEntity#getCluster with AE2's IAEMultiBlock.
        List<Block> hosts = List.of(
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get());
        for (var host : hosts) {
            var id = BuiltInRegistries.BLOCK.getKey(host);
            if (owners.contains(id)) {
                helper.fail(id + " is in NEMultiBlocks.DEFINITIONS: joining that list runs eco's class"
                        + " initialiser before its server config loads, which leaves eco's own L4-L9"
                        + " definitions with a build range of 1 .. -4 and crashes its host UI on open");
                return;
            }
        }
        helper.succeed();
    }

    /**
     * The singularity cell is one formula and no accumulator, so the formula is asserted at the boundaries a
     * player or a pack can reach: before the first batch, exactly on it, the refill after a draw, that a
     * huge bank is not truncated by any ceiling, and that the arithmetic survives the world clock running
     * out. The contract numbers ride along, because a silent edit to either of them is a balance change and
     * not a refactor.
     */
    @GameTest(template = "empty", batch = "singularity_cell_math", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void singularityCellBankFollowsItsClock(GameTestHelper helper) {
        long interval = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .singularityCellTicksPerBatch();
        long batch = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .singularityCellAmountPerBatch();
        var fresh = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank(1000L, 0L);
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.bankOf(fresh, 1000L + interval - 1)
                != 0L) {
            helper.fail("the cell handed out a singularity before its first batch finished");
            return;
        }
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.bankOf(fresh, 1000L + interval)
                != batch) {
            helper.fail("one batch should be " + batch + " singularities, the cell reports "
                    + cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                    .bankOf(fresh, 1000L + interval));
            return;
        }
        var drained = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank(1000L, batch);
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.bankOf(drained, 1000L + 2 * interval)
                != batch) {
            helper.fail("drawing one batch should leave the next one to grow into, not empty the clock");
            return;
        }
        long hugeBatches = 100_000_000_000L;
        long hugeBank = cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                .bankOf(fresh, 1000L + interval * hugeBatches);
        if (hugeBank != hugeBatches * batch) {
            helper.fail("nothing may truncate the bank any more - " + hugeBatches + " batches at " + batch
                    + " should be " + (hugeBatches * batch) + ", the cell reports " + hugeBank);
            return;
        }
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                .bankOf(fresh, 1000L + interval * hugeBatches * 2L) <= hugeBank) {
            helper.fail("doubling the elapsed ticks must double the bank, not stop it");
            return;
        }
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                .bankOf(fresh, Long.MAX_VALUE) <= 0L) {
            helper.fail("the world clock running out should saturate the bank, not wrap it negative");
            return;
        }
        helper.assertTrue(cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                        .SINGULARITY_CELL_TICKS_PER_BATCH_MIN == 60L,
                "the cell's shortest interval is part of its contract");
        helper.assertTrue(cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                        .SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX == Long.MAX_VALUE,
                "the batch size is freely configurable up to the top of a long, which is also where the"
                        + " bank saturates - pinning it here is what makes a silent cap change go red");
        // The freed ceiling, tested where it can still go red: a batch well above the old 262,144 has to be
        // honoured by the config read and paid by the bank, and the saturation still has to hold on top of it.
        var amount = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .SINGULARITY_CELL_AMOUNT_PER_BATCH;
        long restored = amount.get();
        try {
            amount.set(10_000_000L);
            if (cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                    .singularityCellAmountPerBatch() != 10_000_000L) {
                helper.fail("amount_per_batch is still clamped somewhere: writing 10000000 reads back "
                        + cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                        .singularityCellAmountPerBatch());
                return;
            }
            if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                    .bankOf(fresh, 1000L + interval) != 10_000_000L) {
                helper.fail("one batch at the new ceiling should pay 10000000, the cell reports "
                        + cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                        .bankOf(fresh, 1000L + interval));
                return;
            }
            if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                    .bankOf(fresh, Long.MAX_VALUE) != Long.MAX_VALUE) {
                helper.fail("a huge batch should saturate the bank at the top of a long, not wrap it past it");
                return;
            }
        } finally {
            amount.set(restored);
        }
        helper.succeed();
    }

    /**
     * The same cell read through eco's own cell API, which is the only way a network sees it: nothing goes
     * in, only AE2's singularity comes out, and the bank it reports is the bank it will pay.
     */
    @GameTest(template = "empty", batch = "singularity_cell_yield", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void singularityCellTakesNothingAndPaysWhatItShows(GameTestHelper helper) {
        var singularity = cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.singularity();
        var stack = new ItemStack(ModRegistration.SIMPLIFY_SINGULARITY_CELL.get());
        var cell = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells
                .getCellInventory(stack, (appeng.api.storage.cells.ISaveProvider) null);
        if (cell == null) {
            helper.fail("no eco cell handler claims the singularity cell, so a drive cannot see it");
            return;
        }
        if (cell.insert(singularity, 64L, Actionable.SIMULATE, null) != 0L
                || cell.insert(appeng.api.stacks.AEItemKey.of(net.minecraft.world.item.Items.COBBLESTONE),
                        64L, Actionable.MODULATE, null) != 0L) {
            helper.fail("the singularity cell accepted something - it is meant to grow its own stock only");
            return;
        }
        // Being carried must not start the cell: a drive asking it what it holds is the one stamp there is.
        // This used to be an Item#inventoryTick hook over all forty-one carried slots, and it is what made a
        // cell sitting in an inventory produce. Put that hook back and the second block below goes red.
        var carried = new ItemStack(ModRegistration.SIMPLIFY_SINGULARITY_CELL.get());
        if (carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()) != null) {
            helper.fail("a fresh cell should arrive with no stamp at all - that is what this checks against");
            return;
        }
        // -1 is the one rule both display surfaces share: no stamp means no number, not a number of zero.
        if (cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                .stockOf(carried, helper.getLevel().getGameTime()) != -1L) {
            helper.fail("an unstamped cell should report no figure at all, not a number to be believed");
            return;
        }
        ModRegistration.SIMPLIFY_SINGULARITY_CELL.get()
                .inventoryTick(carried, helper.getLevel(), null, 0, false);
        if (carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()) != null) {
            helper.fail("being carried must not start the cell - only a reader asking it for stock may, "
                    + "and the stack now holds " + carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()));
            return;
        }
        // A display read has no save provider, and must not start the cell: that write lands on the reader's
        // own copy of the stack, which is how a fresh item in a creative tab ended up counting from tick 0.
        var display = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells
                .getCellInventory(carried, (appeng.api.storage.cells.ISaveProvider) null);
        if (display == null) {
            helper.fail("no eco cell handler claims the singularity cell, so a drive has nothing to start it");
            return;
        }
        display.getAvailableStacks(new appeng.api.stacks.KeyCounter());
        if (carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()) != null) {
            helper.fail("a read with no save provider started the cell - display paths must never write a"
                    + " start tick, because that copy is not the server's");
            return;
        }
        // A drive does start it: it hands itself in as the save provider, and its level is server-side.
        helper.setBlock(new BlockPos(1, 1, 1),
                ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState());
        if (!(helper.getBlockEntity(new BlockPos(1, 1, 1)) instanceof appeng.api.storage.cells.ISaveProvider drive)) {
            helper.fail("our drive is not a save provider, so it cannot start a mounted cell");
            return;
        }
        var mounted = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells.getCellInventory(carried, drive);
        if (mounted == null) {
            helper.fail("no eco cell handler claims the singularity cell");
            return;
        }
        mounted.getAvailableStacks(new appeng.api.stacks.KeyCounter());
        var stamped = carried.get(ModRegistration.SINGULARITY_CELL_BANK.get());
        // The stamp comes from the server's overworld clock rather than this test level's, so the pair worth
        // asserting is that it exists and starts owing nothing - not which tick number it landed on.
        if (stamped == null || stamped.drawn() != 0L
                || cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem
                        .stockOf(carried, stamped.startGameTime()) != 0L) {
            helper.fail("a drive asking the cell what it holds should stamp it with nothing drawn, which"
                    + " reads as a stock of zero - the stack holds " + stamped);
            return;
        }
        mounted.getAvailableStacks(new appeng.api.stacks.KeyCounter());
        if (!stamped.equals(carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()))) {
            helper.fail("asking a second time must not move its start tick, or the bank would reset every"
                    + " time the network polls it - it now holds "
                    + carried.get(ModRegistration.SINGULARITY_CELL_BANK.get()));
            return;
        }
        long interval = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .singularityCellTicksPerBatch();
        long batch = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                .singularityCellAmountPerBatch();
        long now = cn.dancingsnow.neoecoprototype.items.SingularityCellHandler.serverGameTime();
        if (now < 0L) {
            helper.fail("a game test runs on a real server, so the cell should have a clock to read");
            return;
        }
        stack.set(ModRegistration.SINGULARITY_CELL_BANK.get(),
                new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank(
                        now - 3L * interval, 0L));
        var out = new appeng.api.stacks.KeyCounter();
        cell.getAvailableStacks(out);
        if (out.size() != 1 || out.get(singularity) != 3L * batch) {
            helper.fail("three batches should show as " + (3L * batch) + " singularities in one row,"
                    + " but the cell reports " + out);
            return;
        }
        if (cell.getStatus() != appeng.api.storage.cells.CellState.NOT_EMPTY) {
            helper.fail("a cell with three batches in it reports " + cell.getStatus());
            return;
        }
        // The premise the hand-held line rests on, pinned where it can go red: the level a display is handed
        // carries the same tick the bank is derived from, which is why a client can show the number at all.
        if (helper.getLevel().getGameTime() != now) {
            helper.fail("the level clock a tooltip would read (" + helper.getLevel().getGameTime()
                    + ") is not the clock the bank is derived from (" + now + ")");
            return;
        }
        // The cell carries its own stock line, because eco's byte line never runs for an item that is not
        // an ECOStorageCellItem. Hover text is the only place a player can read what this cell holds, and on
        // a multiplayer client the level in the tooltip context is the only clock it can borrow - so the
        // shape the game itself calls with (TooltipContext.of(level)) is the shape asserted here, not the
        // null some outside caller passes.
        var hover = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        var hoverWithoutLevel = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        var cellItem = ModRegistration.SIMPLIFY_SINGULARITY_CELL.get();
        cellItem.appendHoverText(stack,
                net.minecraft.world.item.Item.TooltipContext.of(helper.getLevel()), hover, null);
        cellItem.appendHoverText(stack, null, hoverWithoutLevel, null);
        long stock = 3L * batch;
        if (hover.stream().noneMatch(line -> line.getString().contains(Long.toString(stock)))
                || hoverWithoutLevel.stream()
                        .noneMatch(line -> line.getString().contains(Long.toString(stock)))) {
            helper.fail("hovering a cell holding " + stock + " singularities should name that number both"
                    + " from the level clock " + hover + " and without one " + hoverWithoutLevel);
            return;
        }
        long paid = cell.extract(singularity, Long.MAX_VALUE, Actionable.MODULATE, null);
        if (paid != 3L * batch) {
            helper.fail("the cell promised " + (3L * batch) + " and paid " + paid);
            return;
        }
        var afterPay = stack.get(ModRegistration.SINGULARITY_CELL_BANK.get());
        if (afterPay == null || afterPay.drawn() != 3L * batch) {
            helper.fail("paying out should have written drawn=" + (3L * batch) + " onto the stack,"
                    + " but the stack holds " + afterPay);
            return;
        }
        var drained = new appeng.api.stacks.KeyCounter();
        cell.getAvailableStacks(drained);
        if (drained.get(singularity) != 0L) {
            helper.fail("the cell still advertises " + drained.get(singularity)
                    + " singularities after paying out its whole bank");
            return;
        }
        long afterDry = cell.extract(singularity, 1L, Actionable.MODULATE, null);
        if (afterDry != 0L) {
            helper.fail("the cell paid " + afterDry + " more after the bank ran dry");
            return;
        }
        if (cell.getStatus() != appeng.api.storage.cells.CellState.EMPTY) {
            helper.fail("an empty bank should report EMPTY, the cell reports " + cell.getStatus()
                    + " (start " + stack.get(ModRegistration.SINGULARITY_CELL_BANK.get()).startGameTime()
                    + ", drawn " + stack.get(ModRegistration.SINGULARITY_CELL_BANK.get()).drawn() + ")");
            return;
        }
        if (cell.canFitInsideCell()) {
            helper.fail("an endless generator must not fit inside another storage cell");
            return;
        }
        helper.succeed();
    }

    /**
     * The drive has to answer eco's {@code ICellHost} contract, because that is the name a third-party
     * terminal reaches for. Checked at runtime rather than by assigning to the interface type, so a
     * dropped {@code implements} turns this red instead of failing the build.
     *
     * <p>The three refusals are the part worth pinning: {@code null} clears while an empty stack does
     * not, and an invalid replacement must not cost the player the cell already mounted. Both read
     * like detail until a terminal clears a slot by passing {@code EMPTY} and wipes a drive.</p>
     */
    @GameTest(template = "l1_room", batch = "drive_cell_host_api", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void driveSpeaksEcosCellHostContract(GameTestHelper helper) {
        buildL1Room(helper, new BlockPos(6, 3, 6),
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    var cluster = storageHost.getCluster();
                    var drives = cluster == null
                            ? java.util.List.<cn.dancingsnow.neoecoprototype.blockentity.storage
                                    .SimplifyDriveBlockEntity>of()
                            : cluster.getDrives();
                    if (drives.isEmpty()) {
                        helper.fail("the built room gave us no drive to talk to");
                        return;
                    }
                    var drive = drives.get(0);
                    if (!(drive instanceof cn.dancingsnow.neoecoae.util.ICellHost cellHost)) {
                        helper.fail("the drive no longer implements eco's ICellHost, so a third-party"
                                + " terminal has to keep probing it by method name");
                        return;
                    }
                    var first = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_4M.get());
                    cellHost.setCellStack(first);
                    if (cellHost.getCellStack() == null
                            || cellHost.getCellStack().getItem() != first.getItem()) {
                        helper.fail("setCellStack did not install the cell: the slot reads "
                                + cellHost.getCellStack());
                        return;
                    }
                    // eco documents clearing as null-only and rejects an empty stack; matching it keeps
                    // one contract for both hosts, so this must leave the mounted cell alone.
                    cellHost.setCellStack(ItemStack.EMPTY);
                    if (cellHost.getCellStack() == null
                            || cellHost.getCellStack().getItem() != first.getItem()) {
                        helper.fail("an empty stack cleared the slot, which eco's own drive does not do:"
                                + " the slot reads " + cellHost.getCellStack());
                        return;
                    }
                    var dirt = new ItemStack(net.minecraft.world.item.Items.DIRT);
                    cellHost.setCellStack(dirt);
                    if (cellHost.getCellStack() == null
                            || cellHost.getCellStack().getItem() != first.getItem()) {
                        helper.fail("an invalid replacement destroyed the mounted cell instead of being"
                                + " refused; the slot reads " + cellHost.getCellStack());
                        return;
                    }
                    var second = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
                    cellHost.setCellStack(second);
                    if (cellHost.getCellStack() == null
                            || cellHost.getCellStack().getItem() != second.getItem()) {
                        helper.fail("setCellStack refused to replace a mounted cell, which eco's drive"
                                + " does: the slot reads " + cellHost.getCellStack());
                        return;
                    }
                    cellHost.setCellStack(null);
                    if (cellHost.getCellStack() != null) {
                        helper.fail("setCellStack(null) left a cell in the slot: " + cellHost.getCellStack());
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The expansion card swaps a machine's tier by walking the two tiers' plans cell against cell,
     * which holds only while both tiers of one family fill the same cells and stop at the same
     * length. Both halves are eco's to change - each definition reads its own ceiling off the config
     * - and the failure mode is the card quietly refusing every machine, so the premise is pinned
     * here rather than the behaviour.
     */
    @GameTest(template = "empty", batch = "tier_swap_plans_line_up", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void bothTiersOfEachFamilyPlanTheSameCells(GameTestHelper helper) {
        MultiBlockDefinition[][] ladder = {
                {NEMultiBlocks.STORAGE_SYSTEM_L4, NEMultiBlocks.STORAGE_SYSTEM_L9},
                {NEMultiBlocks.COMPUTATION_SYSTEM_L4, NEMultiBlocks.COMPUTATION_SYSTEM_L9},
                {NEMultiBlocks.CRAFTING_SYSTEM_L4, NEMultiBlocks.CRAFTING_SYSTEM_L9},
        };
        BlockPos anchor = new BlockPos(8, 8, 8);
        for (MultiBlockDefinition[] pair : ladder) {
            String family = pair[0].getName().getString();
            if (pair[0].getExpandMax() != pair[1].getExpandMax()) {
                helper.fail("the two tiers of " + family + " stop at different lengths ("
                        + pair[0].getExpandMax() + " and " + pair[1].getExpandMax()
                        + "), so a machine filled out for one is not filled out for the other");
                return;
            }
            Map<BlockPos, Block> lower = plannedCells(helper.getLevel(), anchor, pair[0]);
            Map<BlockPos, Block> higher = plannedCells(helper.getLevel(), anchor, pair[1]);
            if (!lower.keySet().equals(higher.keySet())) {
                helper.fail("the two tiers of " + family + " fill different cells, so there is no"
                        + " shared shape to trade block for block");
                return;
            }
            int traded = 0;
            for (Map.Entry<BlockPos, Block> cell : lower.entrySet()) {
                Block next = higher.get(cell.getKey());
                if (next == cell.getValue()) {
                    continue;
                }
                traded++;
                if (cell.getValue().asItem() == Items.AIR || next.asItem() == Items.AIR) {
                    helper.fail("a cell " + family + " trades between tiers has no item to hand back"
                            + " or to pay for at " + cell.getKey());
                    return;
                }
            }
            if (traded == 0) {
                helper.fail("the two tiers of " + family + " plan the very same blocks, so an upgrade"
                        + " would have nothing to swap");
                return;
            }
        }
        helper.succeed();
    }

    /** Which block each cell of one shape wants at length 2, mirroring off. */
    private static Map<BlockPos, Block> plannedCells(ServerLevel level, BlockPos anchor,
                                                     MultiBlockDefinition definition) {
        MultiBlockPlacementPlan plan = MultiBlockPlacementService.preview(level, anchor,
                definition.getOwner().value().defaultBlockState(), definition, 2, false);
        Map<BlockPos, Block> cells = new HashMap<>();
        for (WorldPlannedBlock block : plan.getAllBlocks()) {
            cells.put(block.worldPos(), block.targetState().getBlock());
        }
        return cells;
    }

    /** Where the card tests anchor their room: an L1 machine grown to its ceiling still fits the box. */
    private static final BlockPos CARD_HOST = new BlockPos(13, 3, 8);

    /**
     * The card prices a bigger shape before it breaks anything, because the other order takes a
     * working machine apart and hands the player a hole. This is that promise: with nothing to pay
     * from, the room must still stand exactly as it did.
     */
    @GameTest(template = "l1_room", batch = "structure_card_unpaid", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardLeavesTheMachineAloneWhenItCannotBePaidFor(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var before = machineSnapshot(helper);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    giveExpansionCard(player);
                    // Two sheets go in so the arithmetic means something: the card eats one and hands
                    // back one note, so exactly one sheet may be left, and it has to be the renamed one.
                    // Hand it one sheet and an eating card would look identical to a printing one.
                    // Two sheets and one blank pattern go in: the card owes one sheet eaten, one pattern
                    // eaten, and exactly one pattern printed. Hand it no blank pattern and the card falls
                    // back to the renamed paper, which this count would also catch.
                    player.getInventory().add(new ItemStack(Items.PAPER, 2));
                    player.getInventory().add(new ItemStack(AEItems.BLANK_PATTERN));
                    useExpansionCard(helper, player);
                    var after = machineSnapshot(helper);
                    if (before.isEmpty()) {
                        helper.fail("the snapshot box held no machine, so this test proved nothing");
                        return;
                    }
                    if (!before.equals(after)) {
                        helper.fail("the card broke blocks before it checked whether the player could"
                                + " pay for the bigger shape: " + changed(before, after));
                        return;
                    }
                    // The other half of the same promise: a player who cannot pay gets a demolition
                    // notice instead. Stop the card eating the sheet, or print the wrong work order, and
                    // this goes red.
                    int paperItems = 0;
                    int printed = 0;
                    int orders = 0;
                    ItemStack printedStack = ItemStack.EMPTY;
                    for (var held : player.getInventory().items) {
                        if (held.is(Items.PAPER)) {
                            // Count items, not stacks: two sheets share a slot, so a stack count cannot
                            // tell "ate one and handed one back" from "handed one out of nothing".
                            paperItems += held.getCount();
                        }
                        if (held.is(ModRegistration.SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE.get())) {
                            orders += held.getCount();
                        }
                        if (held.is(AEItems.PROCESSING_PATTERN.asItem())) {
                            printed++;
                            printedStack = held;
                        }
                    }
                    if (paperItems != 1 || printed != 1 || orders != 1) {
                        helper.fail("the refused grow left " + paperItems + " paper, " + orders
                                + " work order(s) and " + printed + " printed pattern(s): two sheets and"
                                + " one blank pattern went in, so the card owes one sheet eaten, one blank"
                                + " eaten, and one ticket plus one pattern handed over");
                        return;
                    }
                    var details = PatternDetailsHelper.decodePattern(printedStack, helper.getLevel());
                    var want = AEItemKey.of(ModRegistration.SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE.get());
                    // The parts go in and the ticket comes out. Reverse that and one cheap sheet buys 48
                    // machine parts, which is the reading he caught in game. Only the key is compared,
                    // never the amount:
                    // an input's count is multiplier-dependent, which is the exact trap the assembler
                    // fell into once already.
                    var outputs = details == null ? java.util.List.<appeng.api.stacks.GenericStack>of()
                            : details.getOutputs();
                    if (outputs.size() != 1 || !want.equals(outputs.get(0).what())
                            || details.getInputs().length == 0) {
                        helper.fail("the pattern it printed does not deliver exactly the L1 work order as"
                                + " its output (decoded=" + (details == null ? "null"
                                : details.getInputs().length + " in / " + outputs.size() + " out")
                                + ") - a ticket must never be the ingredient, or it is a part printer");
                        return;
                    }
                    // The card hands back every block it moves before it builds, so a work order that
                    // still asks for them is overcharging: on this room that is the outer casing column,
                    // reclaimed whole and therefore absent from the input list.
                    var casing = AEItemKey.of(ModRegistration.SIMPLIFY_STORAGE_CASING_ITEM.get());
                    var asked = new StringBuilder();
                    for (var input : details.getInputs()) {
                        for (var option : input.getPossibleInputs()) {
                            if (casing.equals(option.what())) {
                                asked.append('c');
                            }
                        }
                    }
                    if (asked.length() > 0) {
                        helper.fail("the work order still asks for the casings the card gives back - the"
                                + " bill was not netted against the reclaim (matched " + asked + ")");
                        return;
                    }
                    // buildL1Room ends the test once this callback returns without failing.
                });
    }

    /**
     * Every block the card moves has to come back as exactly one item - it reclaims the outer column
     * by hand rather than by breaking it with a tool, because the card itself is what is in hand and a
     * drop table that asks for a pickaxe would quietly return nothing. The ledger is the honest
     * reading: for each block, what stands in the world plus what the player carries plus what lies on
     * the floor must add up to the same number before and after.
     */
    // Why there is no mirrored-host fixture: eco writes Host.isMirrorBuild() from the panel toggle only
    // (no save, no load, no formation), so the card has to read the built hand off the host block's own
    // "mirrored" state instead - see SimplifyStructureExpansionCardItem.builtMirrored. A fixture cannot
    // stand in for that check: the one-click builder hands out a plan in raw local coordinates and never
    // calls MultiBlockRotation.localToWorld, so a room assembled from it is not a mirrored room in the
    // sense a rotated preview means it. Measured 2026-10-21: a mirrored L1 fixture disagreed with the
    // card at the cooling-controller cell for every candidate length, which was the fixture lying rather
    // than the machine. This half is verified on a real mirrored host.

    /**
     * Pays for the bigger shape, right-clicks the host with the card, and requires the room to have
     * grown - formed still, and no block invented or lost on the way.
     *
     * <p>The growth is the reading, not the action bar: a fixture cannot see which sentence the card
     * picked, and "it did not grow" and "it grew but lied about why" are different bugs worth telling
     * apart. {@code payable} and {@code conflicts} go into the failure line so a red here says which
     * gate it hit rather than only that something stopped.</p>
     */
    private static void mustGrowWhenClickedWithCard(GameTestHelper helper,
                                                    MultiBlockBuildController.Host host, String what) {
        var player = cardPlayer(helper);
        player.setShiftKeyDown(true);
        giveExpansionCard(player);
        // Two full stacks of each distinct kind, the way the growing fixtures pay: the bill is per
        // kind once the shape repeats, and a 36-slot inventory fills up if it is handed out per cell.
        var kinds = new java.util.LinkedHashSet<Item>();
        for (var state : machineSnapshot(helper).values()) {
            kinds.add(state.getBlock().asItem());
        }
        for (Item kind : kinds) {
            for (int stack = 0; stack < 2; stack++) {
                var given = new ItemStack(kind);
                given.setCount(given.getMaxStackSize());
                if (!player.getInventory().add(given)) {
                    helper.fail("the fixture inventory had no room for a stack of " + kind);
                    return;
                }
            }
        }
        int standingBefore = machineSnapshot(helper).size();
        var before = ledger(helper, player);
        useExpansionCard(helper, player);
        // Read everything while the machine still stands: the sweep below takes the block entity with
        // it, and every one of these answers would become a lie.
        int standingAfter = machineSnapshot(helper).size();
        boolean formed = host.isFormed();
        var after = ledger(helper, player);
        var def = host.getBuildDefinition();
        BlockPos absoluteHost = helper.absolutePos(CARD_HOST);
        var big = cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService
                .preview(helper.getLevel(), absoluteHost,
                        helper.getLevel().getBlockState(absoluteHost), def,
                        def.getExpandMax(), host.isMirrorBuild());
        clearMachineBox(helper);
        if (standingAfter <= standingBefore) {
            helper.fail(what + " did not grow (" + standingBefore + " -> " + standingAfter
                    + " blocks): formed=" + formed + " expandMin=" + def.getExpandMin()
                    + " expandMax=" + def.getExpandMax()
                    + " | at max length: missing=" + big.getMissingBlocks().size()
                    + " conflicts=" + big.getConflictPositions().size()
                    + " bill=" + big.getRequiredItems().size()
                    + " payable=" + cn.dancingsnow.neoecoae.multiblock.placement
                            .MultiBlockPlacementService.hasRequiredItems(player, big.getRequiredItems())
                    + " [" + describeBill(player, big) + "] host@" + CARD_HOST);
            return;
        }
        if (!formed) {
            helper.fail("the card grew " + what + " into a machine that is no longer formed ("
                    + standingBefore + " -> " + standingAfter + " blocks)");
            return;
        }
        if (!before.equals(after)) {
            helper.fail("the card invented or lost blocks while growing " + what + "; before " + before
                    + " after " + after + " | " + describeMissingBill(helper, host));
            return;
        }
        helper.succeed();
    }

    /**
     * Shared body of the off-table fixtures: stand {@code wantedInterface} in the cell whose plan entry
     * is {@code planInterface}, form the machine, then require the card to grow it.
     */
    private static void offTableInterfaceMustNotStopTheCard(GameTestHelper helper, Block hostBlock,
                                                            Block planInterface, Block wantedInterface) {
        buildL1Room(helper, CARD_HOST, hostBlock, planInterface, wantedInterface,
                host -> mustGrowWhenClickedWithCard(helper, host,
                        "a formed " + name(hostBlock) + " wearing " + name(wantedInterface)));
    }

    /**
     * Grows a formed L1 computation host whose interface cell holds the block the plan cannot name.
     *
     * <p>The published shape puts the communication interface at its one interface cell, while
     * {@code SimplifyComputationClusterCalculator.matchedComputationInterface} accepts either interface
     * kind there. So this room stands formed and running with a block in it that no plan is able to
     * ask for - and the card measured its length against that plan, which is why it called a working
     * machine "not formed". Standing the network interface in the plan's communication-interface cell is
     * exactly what {@code buildL1Room} is for, so nothing here is hand-placed.</p>
     *
     * <p>Red until formation stops being read off the definition table: at every candidate length that
     * one cell is a different block from what the plan wants, and with no ownership map to fall back on
     * the whole machine scores below {@code expandMin}.</p>
     */
    @GameTest(template = "l1_room", batch = "structure_card_offtable", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardGrowsAFormedHostWearingAnOffTableInterface(GameTestHelper helper) {
        offTableInterfaceMustNotStopTheCard(helper,
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get());
    }

    /**
     * A block that belongs to nobody still stops the card, and stays standing when it does.
     *
     * <p>The control for the off-table widening: a cell only the *bigger* shape wants is parked full of
     * cobblestone, so the machine is formed (the stray is not inside its own shape) and the plan cannot
     * name the intruder as either its wanted block or an alternate this family accepts. If the widening
     * ever grows past "a member of this formed machine" into "whatever stands there", this is the fixture
     * that goes red - and the second half of the failure, the block still being there, is the part a
     * player would lose cells over.</p>
     */
    @GameTest(template = "l1_room", batch = "structure_card_foreign_cell", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardStillStopsForABlockThatIsNotAMember(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var def = host.getBuildDefinition();
                    BlockPos absoluteHost = helper.absolutePos(CARD_HOST);
                    var standing = new java.util.HashSet<BlockPos>(machineSnapshot(helper).keySet());
                    var big = cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService
                            .preview(helper.getLevel(), absoluteHost,
                                    helper.getLevel().getBlockState(absoluteHost), def,
                                    def.getExpandMax(), host.isMirrorBuild());
                    BlockPos stray = null;
                    for (var planned : big.getAllBlocks()) {
                        if (!standing.contains(planned.worldPos())
                                && helper.getLevel().getBlockState(planned.worldPos()).isAir()) {
                            stray = planned.worldPos();
                            break;
                        }
                    }
                    if (stray == null) {
                        helper.fail("no cell of the bigger shape stands outside the machine that is"
                                + " already built, so this fixture cannot park an intruder anywhere");
                        return;
                    }
                    helper.getLevel().setBlockAndUpdate(stray, Blocks.COBBLESTONE.defaultBlockState());
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    giveExpansionCard(player);
                    // Paid in full on purpose: without this the click stops on "you cannot afford it"
                    // and the assertions below pass while proving nothing about the intruder.
                    var kinds = new java.util.LinkedHashSet<Item>();
                    for (var state : machineSnapshot(helper).values()) {
                        kinds.add(state.getBlock().asItem());
                    }
                    for (Item kind : kinds) {
                        for (int stack = 0; stack < 2; stack++) {
                            var given = new ItemStack(kind);
                            given.setCount(given.getMaxStackSize());
                            if (!player.getInventory().add(given)) {
                                helper.fail("the fixture inventory had no room for a stack of " + kind);
                                return;
                            }
                        }
                    }
                    int before = machineSnapshot(helper).size();
                    var carriedBefore = carried(player);
                    useExpansionCard(helper, player);
                    int after = machineSnapshot(helper).size();
                    boolean stillStands = helper.getLevel().getBlockState(stray)
                            .is(Blocks.COBBLESTONE);
                    boolean formed = host.isFormed();
                    var carriedAfter = carried(player);
                    clearMachineBox(helper);
                    if (after > before) {
                        helper.fail("the card grew past a cobblestone it does not own (" + before + " -> "
                                + after + " blocks) at " + stray + ": the off-table widening reaches"
                                + " beyond this family's members");
                        return;
                    }
                    if (!stillStands) {
                        helper.fail("the card broke the intruder at " + stray
                                + " instead of stopping on it: formed=" + formed);
                        return;
                    }
                    if (!carriedBefore.equals(carriedAfter)) {
                        helper.fail("a refused click still moved blocks into the backpack: was "
                                + carriedBefore + ", now " + carriedAfter);
                        return;
                    }
                    helper.succeed();
                });
    }

    /** The same refusal on the storage side, where the network interface is also accepted off the table. */
    @GameTest(template = "l1_room", batch = "structure_card_offtable_storage", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardGrowsAStorageHostWearingAnOffTableInterface(GameTestHelper helper) {
        offTableInterfaceMustNotStopTheCard(helper,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get());
    }

    /** ... and on the crafting side, the third family whose calculator takes the other interface. */
    @GameTest(template = "l1_room", batch = "structure_card_offtable_crafting", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardGrowsACraftingHostWearingAnOffTableInterface(GameTestHelper helper) {
        offTableInterfaceMustNotStopTheCard(helper,
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get());
    }

    @GameTest(template = "l1_room", batch = "structure_card_grow", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardTradesEveryBlockItMovesOneForOne(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    giveExpansionCard(player);
                    // Two full stacks of each distinct block, not of each position: eleven columns need
                    // 33 drives and 22 energy cells, and a 36-slot inventory fills up long before that if
                    // the hand-out is per position. A refused add is a fixture bug, so it says so.
                    var kinds = new java.util.LinkedHashSet<Item>();
                    for (var state : machineSnapshot(helper).values()) {
                        kinds.add(state.getBlock().asItem());
                    }
                    for (Item kind : kinds) {
                        for (int stack = 0; stack < 2; stack++) {
                            var given = new ItemStack(kind);
                            given.setCount(given.getMaxStackSize());
                            if (!player.getInventory().add(given)) {
                                helper.fail("the fixture inventory had no room for a stack of " + kind);
                                return;
                            }
                        }
                    }
                    var before = ledger(helper, player);
                    var worldBefore = blockCounts(helper);
                    var carriedBefore = carried(player);
                    String missingBill = describeMissingBill(helper, host);
                    int standingBefore = machineSnapshot(helper).size();
                    useExpansionCard(helper, player);
                    var after = ledger(helper, player);
                    var worldAfter = blockCounts(helper);
                    var carriedAfter = carried(player);
                    int standingAfter = machineSnapshot(helper).size();
                    boolean formed = host.isFormed();
                    // Read before the sweep: once the cells are air the host block entity is gone and
                    // every one of these answers would be a lie.
                    boolean couldInteract = host.canPlayerInteract(player);
                    boolean building = host.isBuildInProgress();
                    int chosenLength = host.getSelectedBuildLength();

                    clearMachineBox(helper);
                    if (standingAfter <= standingBefore) {
                        var def = host.getBuildDefinition();
                        BlockPos absoluteHost = helper.absolutePos(CARD_HOST);
                        var big = cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService
                                .preview(helper.getLevel(), absoluteHost,
                                        helper.getLevel().getBlockState(absoluteHost), def,
                                        def.getExpandMax(), host.isMirrorBuild());
                        helper.fail("the room did not grow (" + standingBefore + " -> " + standingAfter
                                + " blocks): interact=" + couldInteract
                                + " building=" + building + " formed=" + formed
                                + " length=" + chosenLength + " of "
                                + host.getMinBuildLength() + "-" + host.getMaxBuildLength()
                                + " | at max length: missing=" + big.getMissingBlocks().size()
                                + " conflicts=" + big.getConflictPositions().size()
                                + " bill=" + big.getRequiredItems().size()
                                + " payable=" + cn.dancingsnow.neoecoae.multiblock.placement
                                .MultiBlockPlacementService.hasRequiredItems(player,
                                        big.getRequiredItems())
                                + " [" + describeBill(player, big) + "]"
                                + " host@" + CARD_HOST + " (a balanced ledger below proves nothing)");
                        return;
                    }
                    if (!before.equals(after)) {
                        helper.fail("the card lost or invented blocks while growing the room; before "
                                + before + " after " + after
                                + " | world " + worldAfter + " was " + worldBefore
                                + " | carried " + carriedAfter + " was " + carriedBefore
                                + " | " + missingBill);
                        return;
                    }
                    if (!formed) {
                        helper.fail("the grown room never re-formed, so the card left a broken machine");
                        return;
                    }
                    // buildL1Room ends the test once this callback returns without failing.
                });
    }

    /**
     * What a player has to carry to grow a room is the difference between the bigger shape and the blocks
     * the card hands back out of its own wall - not the whole bigger shape. The card prints that
     * difference itself when it refuses, so this test reads the number off its own print, hands over
     * exactly that and demands the room build. Charge the gross bill up front and this goes red, which is
     * the reading he caught in game ("现在好像没办法搭建了").
     */
    @GameTest(template = "l1_room", batch = "structure_card_grow", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void expansionCardBuildsForAPlayerWhoCarriesOnlyTheDifference(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    giveExpansionCard(player);
                    player.getInventory().add(new ItemStack(Items.PAPER));
                    player.getInventory().add(new ItemStack(AEItems.BLANK_PATTERN));
                    // Carrying nothing the machine wants: the card must refuse and print the difference.
                    useExpansionCard(helper, player);
                    ItemStack printed = ItemStack.EMPTY;
                    for (var held : player.getInventory().items) {
                        if (held.is(AEItems.PROCESSING_PATTERN.asItem())) {
                            printed = held;
                        }
                    }
                    var details = PatternDetailsHelper.decodePattern(printed, helper.getLevel());
                    if (printed.isEmpty() || details == null) {
                        helper.fail("the refused grow printed no pattern, so the difference this test is"
                                + " supposed to carry could not be read off it");
                        return;
                    }
                    var difference = new KeyCounter();
                    for (var input : details.getInputs()) {
                        for (var option : input.getPossibleInputs()) {
                            // The same idiom the assembler tests use: AE2 keeps a repeated ingredient as
                            // one slot with a multiplier, so amount alone is not what has to be carried.
                            difference.add(option.what(), option.amount() * input.getMultiplier());
                        }
                    }
                    long carriedParts = 0;
                    for (var entry : difference) {
                        carriedParts += entry.getLongValue();
                    }
                    if (carriedParts <= 0) {
                        helper.fail("the printed difference came to " + carriedParts + " parts, so there"
                                + " was nothing to carry and this test would prove nothing");
                        return;
                    }
                    int standingUntouched = machineSnapshot(helper).size();
                    // Half one: one part short of what the card printed must still be a refusal. Without
                    // it the half below would also pass for a card that charges nothing at all.
                    if (!handOutPrintedDifference(player, difference, 1)) {
                        clearMachineBox(helper);
                        helper.fail("the fixture inventory had no room for " + carriedParts
                                + " parts minus one");
                        return;
                    }
                    useExpansionCard(helper, player);
                    if (machineSnapshot(helper).size() != standingUntouched) {
                        clearMachineBox(helper);
                        helper.fail("the room grew one part short of the " + carriedParts
                                + " parts the card printed, so this card does not charge that bill");
                        return;
                    }
                    // Half two: exactly the printed difference, and it has to build. Carrying the whole
                    // shape used to be the price, which is the reading he caught in game.
                    if (!handOutPrintedDifference(player, difference, 0)) {
                        clearMachineBox(helper);
                        helper.fail("the fixture inventory had no room for the " + carriedParts
                                + " parts the card printed as the difference");
                        return;
                    }
                    useExpansionCard(helper, player);
                    int standingAfter = machineSnapshot(helper).size();
                    var formed = host.isFormed();

                    clearMachineBox(helper);
                    if (standingAfter <= standingUntouched) {
                        helper.fail("carrying exactly the " + carriedParts + " parts the card itself printed"
                                + " did not build the bigger room - the gate is still charging the whole"
                                + " shape before it hands the moved blocks back");
                        return;
                    }
                    if (!formed) {
                        helper.fail("the room built from the difference but never re-formed");
                        return;
                    }
                });
    }

    /**
     * The same promise on the machine that carries work rather than storage: a maxed L1 crafting room with
     * one encoded pattern sitting in a bus, then one click with the disk. The bus is eco's own class on both
     * sides of the ladder and its handler is public, so the pattern has to be standing in the same cell
     * afterwards - or in the player's hands, which is the fallback that costs a minute of reinserting rather
     * than the whole plan the pattern encodes.
     *
     * <p>It is put in through {@code setStackInSlot} on purpose: the fixture must not depend on the same
     * filtered insert the carry uses, or a rejection would look like a survival.</p>
     */
    @GameTest(template = "l1_room", batch = "migration_drive_bus", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void migrationDriveCarriesThePatternsInABus(GameTestHelper helper) {
        buildRoomToMaxLength(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                host -> {
                    var level = helper.getLevel();
                    var anchor = helper.absolutePos(CARD_HOST);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();

                    var busPositions = new java.util.ArrayList<BlockPos>();
                    for (var entry : machineSnapshot(helper).entrySet()) {
                        if (entry.getKey().compareTo(anchor) != 0
                                && level.getBlockEntity(entry.getKey())
                                instanceof cn.dancingsnow.neoecoae.blocks.entity.crafting
                                        .ECOCraftingPatternBusBlockEntity) {
                            busPositions.add(entry.getKey());
                        }
                    }
                    if (busPositions.size() < 2) {
                        helper.fail("this crafting room has " + busPositions.size() + " pattern buses; the"
                                + " carry needs at least two so a bus that is not the first one is covered");
                        return;
                    }
                    var pattern = PatternDetailsHelper.encodeProcessingPattern(
                            List.of(new GenericStack(AEItemKey.of(Items.IRON_INGOT), 1)),
                            List.of(new GenericStack(AEItemKey.of(Items.GOLD_INGOT), 1)));
                    if (pattern.isEmpty()) {
                        helper.fail("the fixture could not encode a pattern, so there is nothing to carry");
                        return;
                    }
                    // Through the backing inventory, and deliberately not slot 0 of the first bus: the bus
                    // opens a paged view on its slots, and reading that view is exactly what let a real
                    // machine lose the patterns that were not on the page the carry was looking at.
                    var firstBus = (cn.dancingsnow.neoecoae.blocks.entity.crafting
                            .ECOCraftingPatternBusBlockEntity) level.getBlockEntity(busPositions.get(0));
                    var lastBus = (cn.dancingsnow.neoecoae.blocks.entity.crafting
                            .ECOCraftingPatternBusBlockEntity) level.getBlockEntity(
                            busPositions.get(busPositions.size() - 1));
                    firstBus.getPatternSlotInventory().setItemDirect(5, pattern.copy());
                    var lastSlots = lastBus.getPatternSlotInventory();
                    lastSlots.setItemDirect(lastSlots.size() - 1, pattern.copy());
                    int patternsBefore = everywhere(helper, player, pattern.getItem());
                    if (patternsBefore != 2) {
                        helper.fail("the fixture put in " + patternsBefore + " patterns, expected 2, so the"
                                + " counting place is not the place the swap looks at");
                        return;
                    }

                    var disk = ModRegistration.MIGRATION_DRIVE_1_4.get();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 1));
                    useCard(helper, player, disk);

                    var afterHost = level.getBlockState(anchor).getBlock();
                    if (!name(afterHost).equals("crafting_system_l4")) {
                        helper.fail("the disk left the crafting host on " + name(afterHost)
                                + " instead of eco's crafting_system_l4");
                        return;
                    }
                    int patternsAfter = everywhere(helper, player, pattern.getItem());
                    if (patternsAfter != patternsBefore) {
                        helper.fail("the swap left " + patternsAfter + " patterns in and around this room,"
                                + " there were " + patternsBefore + " before the click, so a work order was"
                                + " duplicated or destroyed");
                        return;
                    }
                    int inBuses = 0;
                    for (var where : busPositions) {
                        if (level.getBlockEntity(where) instanceof cn.dancingsnow.neoecoae.blocks.entity.crafting
                                .ECOCraftingPatternBusBlockEntity afterBus) {
                            for (var stack : afterBus.getPatternSlotInventory()) {
                                if (ItemStack.isSameItemSameComponents(stack, pattern)) {
                                    inBuses++;
                                }
                            }
                        }
                    }
                    NeoECOPrototype.LOGGER.info("migration carry: {}/{} patterns are back in the buses they"
                            + " were taken out of", inBuses, patternsBefore);
                    if (inBuses != patternsBefore) {
                        helper.fail("the swap left " + inBuses + " of " + patternsBefore
                                + " patterns standing in a bus; the rest only made it to the player's"
                                + " hands, which is the degraded path this carry exists to avoid");
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The migration disk on the machine it exists for: our own L1 storage room grown to its ceiling, in
     * survival, with an empty inventory. One click has to land the room on eco's L4 definition, leave it
     * formed, hand every L1 part it replaced back to the player, eat the disk and put the empty one in
     * their hand instead. Nothing is billed - the disk is the price - so an eco item appearing in the
     * inventory would mean the swap took parts from somewhere other than the machine standing there.
     *
     * <p>The doll is asserted on the branch this fixture can reach: only the crafting host carries a mind,
     * so a storage swap must never print one. The other branch - an F1 room that rolled its one-in-sixteen
     * mind - has no suite coverage and needs a client or a seam to force the roll; it is written here so
     * the next reader does not take a green run as proof the doll works.</p>
     */
    @GameTest(template = "l1_room", batch = "migration_drive_swap", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void migrationDriveMovesAnL1RoomOntoEcoL4(GameTestHelper helper) {
        buildRoomToMaxLength(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var level = helper.getLevel();
                    var anchor = helper.absolutePos(CARD_HOST);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();

                    int drivesBefore = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (state.getBlock() == ModRegistration.SIMPLIFY_DRIVE_BLOCK.get()) {
                            drivesBefore++;
                        }
                    }
                    if (drivesBefore == 0) {
                        helper.fail("not one simplify_drive stands in this room before the click, so the"
                                + " refund count below is measuring nothing");
                        return;
                    }

                    var disk = ModRegistration.MIGRATION_DRIVE_1_4.get();
                    // Mount a filled cell into one drive before the click: this whole feature exists so
                    // that a player's mounted contents survive it, and giveBack destroys the block without
                    // drops, so a swap that forgets to carry them simply deletes them.
                    if (!(host instanceof cn.dancingsnow.neoecoprototype.blockentity.storage
                            .SimplifyStorageHostBlockEntity storageHost)
                            || storageHost.getCluster() == null
                            || storageHost.getCluster().getDrives().isEmpty()) {
                        helper.fail("the fixture room has no drive to mount a cell in, so the carry check"
                                + " below measures nothing");
                        return;
                    }
                    var drives = storageHost.getCluster().getDrives();
                    var mountedPos = drives.get(0).getBlockPos();
                    var lastMountedPos = drives.get(drives.size() - 1).getBlockPos();
                    // Two drives, and not neighbours: a carry that only works on the first position it
                    // looks at has passed a fixture like the previous one and still eats a real machine.
                    drives.get(0).setCellStack(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1M.get()));
                    drives.get(drives.size() - 1).setCellStack(
                            new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1M.get()));
                    int cellBefore = everywhere(helper, player, ModRegistration.SIMPLIFY_ITEM_CELL_1M.get());
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 1));
                    useCard(helper, player, disk);

                    var afterHost = level.getBlockState(anchor).getBlock();
                    if (afterHost != cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L4.get()) {
                        helper.fail("the disk left the host on " + name(afterHost)
                                + " instead of storage_system_l4");
                        return;
                    }
                    if (!(level.getBlockEntity(anchor) instanceof MultiBlockBuildController.Host fresh)
                            || !fresh.isFormed()) {
                        helper.fail("the room is on eco's L4 host but not formed, so the swap put blocks"
                                + " in without telling the new host to look at its own shape");
                        return;
                    }

                    var back = carried(player);
                    if (back.getOrDefault(disk, 0) != 0) {
                        helper.fail("the swap landed but the disk was not eaten: still "
                                + back.get(disk) + " in the inventory");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0) != 1) {
                        helper.fail("the player holds "
                                + back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0)
                                + " empty disks, expected exactly the one this click should have left");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get(), 0) != 0) {
                        helper.fail("a storage swap gave out a doll, but only the crafting host can carry"
                                + " a mind to trade for one");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.SIMPLIFY_DRIVE_ITEM.get(), 0) != drivesBefore) {
                        helper.fail("the refund returned "
                                + back.getOrDefault(ModRegistration.SIMPLIFY_DRIVE_ITEM.get(), 0)
                                + " drives but " + drivesBefore + " were standing before the click");
                        return;
                    }
                    // Conservation first: whatever the swap does with the cell, the total across the
                    // machine, the player and the floor has to stay where it was. The placement check below
                    // only says where it ended up; this is the one that catches both directions of wrong.
                    var cell = ModRegistration.SIMPLIFY_ITEM_CELL_1M.get();
                    int cellAfter = everywhere(helper, player, cell);
                    if (cellAfter != cellBefore) {
                        helper.fail("the swap left " + cellAfter + " mounted cells in and around this room,"
                                + " it held " + cellBefore + " before the click, so the cell was either"
                                + " duplicated or destroyed");
                        return;
                    }
                    var after = level.getBlockEntity(mountedPos)
                            instanceof cn.dancingsnow.neoecoae.util.ICellHost mountedDrive
                            ? mountedDrive.getCellStack() : ItemStack.EMPTY;
                    var afterLast = level.getBlockEntity(lastMountedPos)
                            instanceof cn.dancingsnow.neoecoae.util.ICellHost lastDrive
                            ? lastDrive.getCellStack() : ItemStack.EMPTY;
                    boolean inPlace = after != null && after.is(cell)
                            && afterLast != null && afterLast.is(cell);
                    NeoECOPrototype.LOGGER.info("migration carry: the two mounted cells survived{}",
                            inPlace ? " in their own drives" : " outside at least one drive");
                    if (!inPlace && back.getOrDefault(cell, 0) < cellBefore) {
                        helper.fail("the cells mounted at " + mountedPos + " and " + lastMountedPos
                                + " are standing in neither those drives nor the player's inventory,"
                                + " so the swap destroyed them");
                        return;
                    }
                    var strays = new java.util.ArrayList<String>();
                    back.forEach((item, count) -> {
                        var id = BuiltInRegistries.ITEM.getKey(item);
                        if (!NeoECOPrototype.MOD_ID.equals(id.getNamespace())) {
                            strays.add(id + "=" + count);
                        }
                    });
                    if (!strays.isEmpty()) {
                        helper.fail("the swap took or invented parts from outside our own pack: " + strays);
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The computation family is the one machine whose L1 placement shape comes from something we register
     * (the {@code ECOMultiblockDefinitions} provider in {@code NeoECOPrototype#commonSetup}, which answers
     * only for {@code SimplifyTier.L1} + {@code COMPUTATION}), and until now the only thing saying it hands
     * over to eco's L4 was the table check. Here the machine really stands, the 1-4 disk really clicks, and
     * the host on eco's block has to answer {@code isFormed()} - which is the moment our provider must stop
     * answering and eco's own definition must start.
     */
    @GameTest(template = "l1_room", batch = "migration_drive_comp_l4", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void migrationDriveHandsAFormedComputationHostToEcoL4(GameTestHelper helper) {
        buildComputationStructureKept(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(),
                ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), 0, false,
                cn.dancingsnow.neoecoprototype.api.SimplifyMultiblockConfig.L1_PLACEMENT_EXPAND_MAX,
                (controller, finish) -> {
                    var anchor = helper.absolutePos(CARD_HOST);
                    if (!controller.isFormed()) {
                        helper.fail("the L1 computation machine did not form before the click, so this swap"
                                + " would prove nothing");
                        finish.run();
                        return;
                    }
                    var tierBefore = controller.getTier();
                    if (tierBefore != cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1) {
                        helper.fail("the L1 host reports tier " + tierBefore
                                + ", so the provider row this test is about is not the one firing");
                        finish.run();
                        return;
                    }

                    var disk = ModRegistration.MIGRATION_DRIVE_1_4.get();
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 1));
                    useCard(helper, player, disk);

                    var afterHost = helper.getLevel().getBlockState(anchor).getBlock();
                    if (afterHost != cn.dancingsnow.neoecoae.all.NEBlocks.COMPUTATION_SYSTEM_L4.get()) {
                        helper.fail("the disk left the computation host on " + name(afterHost)
                                + " instead of computation_system_l4");
                        finish.run();
                        return;
                    }
                    if (!(helper.getLevel().getBlockEntity(anchor) instanceof NEBlockEntity<?, ?> tieredHost)
                            || tieredHost.getTier() == tierBefore) {
                        helper.fail("the block changed but the host behind it still reports the old tier,"
                                + " so the swap only repainted the shell");
                        finish.run();
                        return;
                    }
                    if (!(helper.getLevel().getBlockEntity(anchor)
                            instanceof MultiBlockBuildController.Host fresh) || !fresh.isFormed()) {
                        helper.fail("the room is on eco's L4 computation host but not formed, so the two"
                                + " definition sources did not hand over cleanly");
                        finish.run();
                        return;
                    }
                    if (carried(player).getOrDefault(disk, 0) != 0) {
                        helper.fail("the swap landed but the disk was not eaten");
                        finish.run();
                        return;
                    }
                    finish.run();
                    helper.succeed();
                });
    }

    /**
     * The creative disk, on the one jump no lower disk can make: our own L1 storage room grown to its
     * ceiling, one click, and the host standing on eco's L9. It keeps every other promise the ladder makes
     * - formed, disk eaten, empty disk back in hand, the parts it replaced refunded - and it bills nothing,
     * which is what the foreign-item check below pins: an eco part in this inventory would mean the swap
     * took material from somewhere other than the machine standing there. "Creative" here means the rung
     * has no recipe yet, not that the disk only works in creative mode, so the player in this fixture is a
     * survival one.
     */
    @GameTest(template = "l1_room", batch = "migration_drive_creative", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void creativeDriveTakesAnL1RoomStraightToL9(GameTestHelper helper) {
        buildRoomToMaxLength(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var level = helper.getLevel();
                    var anchor = helper.absolutePos(CARD_HOST);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();

                    int drivesBefore = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (state.getBlock() == ModRegistration.SIMPLIFY_DRIVE_BLOCK.get()) {
                            drivesBefore++;
                        }
                    }
                    if (drivesBefore == 0) {
                        helper.fail("not one simplify_drive stands in this room before the click, so the"
                                + " refund count below is measuring nothing");
                        return;
                    }

                    var disk = ModRegistration.MIGRATION_DRIVE_CREATIVE.get();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 1));
                    useCard(helper, player, disk);

                    var afterHost = level.getBlockState(anchor).getBlock();
                    if (afterHost != cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L9.get()) {
                        helper.fail("the creative disk left the host on " + name(afterHost)
                                + " instead of storage_system_l9 - this disk is the only one that jumps"
                                + " two rungs");
                        return;
                    }
                    if (!(level.getBlockEntity(anchor) instanceof MultiBlockBuildController.Host fresh)
                            || !fresh.isFormed()) {
                        helper.fail("the room is on eco's L9 host but not formed, so the swap put blocks"
                                + " in without telling the new host to look at its own shape");
                        return;
                    }
                    var back = carried(player);
                    if (back.getOrDefault(disk, 0) != 0) {
                        helper.fail("the swap landed but the creative disk was not eaten: still "
                                + back.get(disk) + " in the inventory");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0) != 1) {
                        helper.fail("the player holds "
                                + back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0)
                                + " empty disks, expected exactly the one this click should have left");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.SIMPLIFY_DRIVE_ITEM.get(), 0) != drivesBefore) {
                        helper.fail("the refund returned "
                                + back.getOrDefault(ModRegistration.SIMPLIFY_DRIVE_ITEM.get(), 0)
                                + " drives but " + drivesBefore + " were standing before the click");
                        return;
                    }
                    var strays = new java.util.ArrayList<String>();
                    back.forEach((item, count) -> {
                        var id = BuiltInRegistries.ITEM.getKey(item);
                        if (!NeoECOPrototype.MOD_ID.equals(id.getNamespace())) {
                            strays.add(id + "=" + count);
                        }
                    });
                    if (!strays.isEmpty()) {
                        helper.fail("the L1→L9 jump billed or invented parts outside our own pack: " + strays);
                        return;
                    }
                    NeoECOPrototype.LOGGER.info("creative jump verified: host on L9, formed, "
                            + drivesBefore + " drives refunded, nothing billed");
                    helper.succeed();
                });
    }

    /**
     * The top rung on a machine that is actually standing on L6: same promise as the L4→L6 case above,
     * asserted on its own because the L6→L9 disk otherwise only ever appears in this suite as the wrong
     * disk in someone else's hand, which proves refusal and never proves the swap.
     */
    @GameTest(template = "l1_room", batch = "migration_drive_rung9", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void rungNineDriveSwapsAnL6RoomWithAnEmptyHand(GameTestHelper helper) {
        buildRoomToMaxLength(helper, CARD_HOST,
                cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L6.get(),
                cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_INTERFACE.get(),
                host -> {
                    var level = helper.getLevel();
                    var anchor = helper.absolutePos(CARD_HOST);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();

                    int l6CellsBefore = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (name(state.getBlock()).equals("energy_cell_l6")) {
                            l6CellsBefore++;
                        }
                    }
                    if (l6CellsBefore == 0) {
                        helper.fail("no energy_cell_l6 stands in this room before the click, so the"
                                + " refund count below is measuring nothing");
                        return;
                    }

                    var disk = ModRegistration.MIGRATION_DRIVE_6_9.get();
                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 1));
                    useCard(helper, player, disk);

                    var afterHost = level.getBlockState(anchor).getBlock();
                    if (afterHost != cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L9.get()) {
                        helper.fail("the L6 disk left the host on " + name(afterHost)
                                + " instead of storage_system_l9");
                        return;
                    }
                    if (!(level.getBlockEntity(anchor) instanceof MultiBlockBuildController.Host fresh)
                            || !fresh.isFormed()) {
                        helper.fail("the room is on L9 but no longer formed");
                        return;
                    }
                    var back = carried(player);
                    if (back.getOrDefault(disk, 0) != 0) {
                        helper.fail("the swap landed but the disk was not consumed: still "
                                + back.get(disk) + " in hand");
                        return;
                    }
                    if (back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0) != 1) {
                        helper.fail("the player holds "
                                + back.getOrDefault(ModRegistration.MIGRATION_DRIVE_EMPTY.get(), 0)
                                + " empty disks, expected exactly the one this click should have left");
                        return;
                    }
                    var oldCells = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                            "neoecoae", "energy_cell_l6"));
                    if (back.getOrDefault(oldCells, 0) != l6CellsBefore) {
                        helper.fail("the swap returned " + back.getOrDefault(oldCells, 0)
                                + " energy_cell_l6 but " + l6CellsBefore + " were standing before it");
                        return;
                    }
                    int l9Cells = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (name(state.getBlock()).equals("energy_cell_l9")) {
                            l9Cells++;
                        }
                    }
                    if (l9Cells == 0) {
                        helper.fail("the host says L9 but not one energy_cell_l9 is standing");
                        return;
                    }
                    NeoECOPrototype.LOGGER.info("L6->L9 swap verified: host on L9, formed,"
                            + " " + l6CellsBefore + " l6 cells refunded, " + l9Cells + " l9 cells standing");
                    helper.succeed();
                });
    }

    /**
     * The whole promise of a rung drive, on the machine it exists for: an eco L4 storage room built out to
     * its maximum length, in survival, with an <b>empty inventory</b>. The wrong-rung drive has to refuse
     * and stay in hand; the right one has to land on L6, be consumed, and take nothing from the player -
     * the disk is the price, so nothing is billed. What it does owe them is every block the swap replaces:
     * the voucher carries the cost and the parts come back, which is what the two counts below pin. That
     * last half is the design: if the disk ever goes back through eco's builder it cannot build for an
     * unbilled survival player at all, so this goes red rather than quietly starting to demand parts.
     */
    @GameTest(template = "l1_room", batch = "migration_drive_rung", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void rungDriveSwapsTierWithAnEmptyHand(GameTestHelper helper) {
        buildRoomToMaxLength(helper, CARD_HOST,
                cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L4.get(),
                cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_INTERFACE.get(),
                host -> {
                    var level = helper.getLevel();
                    var anchor = helper.absolutePos(CARD_HOST);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    player.getInventory().clearContent();
                    var wrong = ModRegistration.MIGRATION_DRIVE_6_9.get();
                    var right = ModRegistration.MIGRATION_DRIVE_4_6.get();

                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(wrong, 1));
                    useCard(helper, player, wrong);
                    if (player.getInventory().countItem(wrong) != 1) {
                        helper.fail("the L6 disk was eaten on an L4 machine");
                        return;
                    }
                    if (level.getBlockState(anchor).getBlock()
                            != cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L4.get()) {
                        helper.fail("a refused click still changed the host block");
                        return;
                    }

                    int l4CellsBefore = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (name(state.getBlock()).equals("energy_cell_l4")) {
                            l4CellsBefore++;
                        }
                    }
                    if (l4CellsBefore == 0) {
                        helper.fail("no energy_cell_l4 stands in this room before the click, so the"
                                + " refund count below is measuring nothing");
                        return;
                    }

                    player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(right, 1));
                    useCard(helper, player, right);
                    var afterHost = level.getBlockState(anchor).getBlock();
                    if (afterHost != cn.dancingsnow.neoecoae.all.NEBlocks.STORAGE_SYSTEM_L6.get()) {
                        helper.fail("the L4 disk left the host on " + name(afterHost)
                                + " instead of storage_system_l6");
                        return;
                    }
                    if (player.getInventory().countItem(right) != 0) {
                        helper.fail("the swap landed but the disk was not consumed: still "
                                + player.getInventory().countItem(right) + " in hand");
                        return;
                    }
                    // The refund, counted against what actually stood here rather than a number copied off
                    // a bill: the voucher carries the price, so the parts a swap replaces go back to the
                    // player instead of vanishing into the machine's new walls.
                    var back = carried(player);
                    var oldCells = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                            "neoecoae", "energy_cell_l4"));
                    var oldHost = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                            "neoecoae", "storage_system_l4"));
                    var newCells = BuiltInRegistries.ITEM.get(ResourceLocation.fromNamespaceAndPath(
                            "neoecoae", "energy_cell_l6"));
                    if (oldCells == net.minecraft.world.item.Items.AIR
                            || oldHost == net.minecraft.world.item.Items.AIR
                            || newCells == net.minecraft.world.item.Items.AIR) {
                        helper.fail("one of the ids this refund check looks up is not a registered item,"
                                + " so a zero in the counts below would be my lookup and not the disk");
                        return;
                    }
                    if (back.getOrDefault(newCells, 0) != 0) {
                        helper.fail("the refund handed out " + back.get(newCells) + " energy_cell_l6,"
                                + " which never stood in this machine");
                        return;
                    }
                    if (back.getOrDefault(oldCells, 0) != l4CellsBefore) {
                        helper.fail("the swap returned " + back.getOrDefault(oldCells, 0)
                                + " energy_cell_l4 but " + l4CellsBefore
                                + " were standing before it, so a cell was eaten or invented");
                        return;
                    }
                    if (back.getOrDefault(oldHost, 0) != 1) {
                        helper.fail("the host block came back as " + back.getOrDefault(oldHost, 0)
                                + ", expected exactly the one that stood in the anchor cell");
                        return;
                    }
                    if (!(level.getBlockEntity(anchor) instanceof MultiBlockBuildController.Host fresh)
                            || !fresh.isFormed()) {
                        helper.fail("the room is on L6 but no longer formed, so the new parts were put"
                                + " in without telling the host to look at its own shape again");
                        return;
                    }
                    int l6Cells = 0;
                    for (var state : machineSnapshot(helper).values()) {
                        if (name(state.getBlock()).equals("energy_cell_l6")) {
                            l6Cells++;
                        }
                    }
                    if (l6Cells == 0) {
                        helper.fail("the host says L6 but not one energy_cell_l6 is standing, so the"
                                + " tier-bearing blocks never changed");
                        return;
                    }
                    NeoECOPrototype.LOGGER.info("free swap verified: host on L6, formed, inventory empty"
                            + ", energy_cell_l6 standing=" + l6Cells);
                });
    }

    /**
     * Both rung disks clicked on a machine that is not on their rung: the disk has to come back out of
     * the click whole and the machine has to be exactly as it was stood. This is the promise that makes
     * the disk a consumable rather than a toll - refuse and eat it, and the player has paid for nothing.
     */
    @GameTest(template = "l1_room", batch = "migration_drive_rung_refuse", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void rungDrivesRefuseTheWrongMachineAndAreNotConsumed(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var before = machineSnapshot(helper);
                    var player = cardPlayer(helper);
                    player.setShiftKeyDown(true);
                    // Two of each: one would also survive being eaten, because shrink(1) would leave an
                    // empty hand and the count check would read 0 either way.
                    for (var disk : new net.minecraft.world.item.Item[]{
                            ModRegistration.MIGRATION_DRIVE_4_6.get(),
                            ModRegistration.MIGRATION_DRIVE_6_9.get()}) {
                        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(disk, 2));
                        useCard(helper, player, disk);
                        int held = player.getItemInHand(InteractionHand.MAIN_HAND).getCount();
                        if (held != 2) {
                            helper.fail(disk.getDescriptionId() + " was consumed on a machine that is"
                                    + " not on its rung - it came back as " + held);
                            return;
                        }
                        if (player.getInventory().countItem(disk) != 2) {
                            helper.fail(disk.getDescriptionId() + " left the inventory at "
                                    + player.getInventory().countItem(disk) + " after a refused click");
                            return;
                        }
                    }
                    var after = machineSnapshot(helper);
                    if (before.isEmpty()) {
                        helper.fail("the snapshot box held no machine, so this test proved nothing");
                        return;
                    }
                    if (!before.equals(after)) {
                        helper.fail("a refused rung disk still moved blocks: " + changed(before, after));
                        return;
                    }
                });
    }

    /**
     * The four disks between them cover the ladder and nothing outside it. The rows are read off eco's own
     * definition list by host-block id ({@code storage_system_l4} and friends) instead of being typed out
     * here, because a hand-written table once paired the crafting rung with the computation definition and
     * the test then failed on the test. Nine tiered hosts cover the three upper disks; our own three L1
     * definitions cover the 1→4 one and the creative one, because that bottom rung exists only on this side.
     */
    @GameTest(template = "empty", batch = "migration_drive_table", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void rungDrivesCoverOneRungPerFamilyAndNothingElse(GameTestHelper helper) {
        var oneToFour = SimplifyMigrationDriveItem.Rung.ONE_TO_FOUR;
        var fourToSix = SimplifyMigrationDriveItem.Rung.FOUR_TO_SIX;
        var sixToNine = SimplifyMigrationDriveItem.Rung.SIX_TO_NINE;
        var straightToNine = SimplifyMigrationDriveItem.Rung.STRAIGHT_TO_NINE;
        var byId = new java.util.HashMap<String, MultiBlockDefinition>();
        var tierOf = new java.util.HashMap<String, String>();
        var familyOf = new java.util.HashMap<String, String>();
        var pattern = java.util.regex.Pattern.compile("^(.*)_l([469])$");
        for (var def : NEMultiBlocks.DEFINITIONS) {
            String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                    .getKey(def.getOwner().value()).getPath();
            var found = pattern.matcher(path);
            if (!found.matches()) {
                continue;
            }
            byId.put(path, def);
            familyOf.put(path, found.group(1));
            tierOf.put(path, found.group(2));
        }
        int rows = 0;
        for (var entry : byId.entrySet()) {
            String path = entry.getKey();
            var def = entry.getValue();
            String family = familyOf.get(path);
            String tier = tierOf.get(path);
            var l6 = byId.get(family + "_l6");
            var l9 = byId.get(family + "_l9");
            // Only the rung each disk owns may answer; everything else has to be refused, because a disk
            // that accepts the wrong machine eats itself on a shape it cannot build.
            if (SimplifyMigrationDriveItem.rungTarget(oneToFour, def) != null) {
                helper.fail("the L1 disk answered an eco host (" + path + ")");
                return;
            }
            var expected46 = "4".equals(tier) ? l6 : null;
            var next46 = SimplifyMigrationDriveItem.rungTarget(fourToSix, def);
            if (next46 != expected46) {
                helper.fail("the L4 disk on " + path + " answered " + defName(next46)
                        + ", expected " + defName(expected46));
                return;
            }
            var expected69 = "6".equals(tier) ? l9 : null;
            var next69 = SimplifyMigrationDriveItem.rungTarget(sixToNine, def);
            if (next69 != expected69) {
                helper.fail("the L6 disk on " + path + " answered " + defName(next69)
                        + ", expected " + defName(expected69));
                return;
            }
            var expectedCreative = "9".equals(tier) ? null : l9;
            var nextCreative = SimplifyMigrationDriveItem.rungTarget(straightToNine, def);
            if (nextCreative != expectedCreative) {
                helper.fail("the creative disk on " + path + " answered " + defName(nextCreative)
                        + ", expected " + defName(expectedCreative));
                return;
            }
            // "There is nothing above it" may only be said where nothing is above it, and that sentence is
            // what a player acts on, so which rung each refusal key lands on is pinned row by row.
            var expectKey = "9".equals(tier) ? "card.neoecoprototype.migration.at_top"
                    : "card.neoecoprototype.migration.wrong_machine";
            if (!SimplifyMigrationDriveItem.refusalKey(def).equals(expectKey)) {
                helper.fail("the refusal on " + path + " would say "
                        + SimplifyMigrationDriveItem.refusalKey(def) + ", expected " + expectKey);
                return;
            }
            rows++;
        }
        // Control: nine hosts is what eco ships. An empty loop would pass every check above.
        if (rows < 9) {
            helper.fail("only " + rows + " tiered hosts were found in eco's definition list, so this"
                    + " table check proves nothing about the three families");
            return;
        }
        for (var entry : java.util.Map.of(
                SimplifyStorageDefinition.L1, "storage_system_l4",
                SimplifyComputationDefinition.L1, "computation_system_l4",
                SimplifyCraftingDefinition.L1, "crafting_system_l4").entrySet()) {
            var l4 = byId.get(entry.getValue());
            var l9 = byId.get(entry.getValue().replace("_l4", "_l9"));
            if (l4 == null || l9 == null) {
                helper.fail("eco ships no " + entry.getValue() + " or its L9, so this row is my lookup");
                return;
            }
            if (SimplifyMigrationDriveItem.rungTarget(oneToFour, entry.getKey()) != l4) {
                helper.fail("the L1 disk on our " + entry.getValue() + " host answered "
                        + defName(SimplifyMigrationDriveItem.rungTarget(oneToFour, entry.getKey()))
                        + ", expected " + defName(l4));
                return;
            }
            if (SimplifyMigrationDriveItem.rungTarget(straightToNine, entry.getKey()) != l9) {
                helper.fail("the creative disk on our " + entry.getValue() + " host answered "
                        + defName(SimplifyMigrationDriveItem.rungTarget(straightToNine, entry.getKey()))
                        + ", expected " + defName(l9));
                return;
            }
            if (!SimplifyMigrationDriveItem.refusalKey(entry.getKey())
                    .equals("card.neoecoprototype.migration.wrong_machine")) {
                helper.fail("our " + entry.getValue() + " host can still climb, so a refusal there must"
                        + " not claim the tier is full");
                return;
            }
        }
        NeoECOPrototype.LOGGER.info("rung-table check covered " + rows + " hosts and 3 L1 rows");
        helper.succeed();
    }

    /**
     * The three modifiers the pigmee housing claims, read off the stack itself, plus the two attribute
     * defaults the tooltip total is built from. Exists because the javadoc that used to describe them was
     * destroyed by an encoding accident and contained numbers nobody could re-derive from the code alone.
     */
    @GameTest(template = "empty", batch = "pigmee_housing_attributes", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void pigmeeHousingReportsItsModifiersInHand(GameTestHelper helper) {
        var stack = new ItemStack(ModRegistration.PIGMEE_STORAGE_MATRIX_HOUSING.get());
        var report = new java.util.ArrayList<String>();
        stack.forEachModifier(net.minecraft.world.entity.EquipmentSlot.MAINHAND,
                (attribute, modifier) -> report.add(
                        net.minecraft.core.registries.BuiltInRegistries.ATTRIBUTE.getKey(attribute.value())
                                + " <- " + modifier.id() + " amount=" + modifier.amount()
                                + " op=" + modifier.operation()));
        if (report.isEmpty()) {
            helper.fail("the housing item declares no main-hand modifiers at all");
            return;
        }
        double damageBase = net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_DAMAGE.value()
                .getDefaultValue();
        double speedBase = net.minecraft.world.entity.ai.attributes.Attributes.ATTACK_SPEED.value()
                .getDefaultValue();
        NeoECOPrototype.LOGGER.info("pigmee housing main-hand modifiers: {} | attribute defaults: damage={}"
                + " speed={} so the held total is {}+ and speed {}+", report, damageBase, speedBase,
                damageBase, speedBase);
        if (report.size() != 3) {
            helper.fail("expected three modifiers (damage, speed, interaction range), got " + report.size()
                    + ": " + report);
            return;
        }
        helper.succeed();
    }

    /**
     * What a battery reports about itself, asserted synchronously: {@code buildRoom} clears the machine and
     * calls succeed() the moment this consumer returns, so anything scheduled from here never runs - an
     * earlier version of this test "passed" that way and asserted nothing. Whether the grid actually folds
     * the buffer into its ceiling needs a node tick, so it is asserted by the sibling fixture
     * {@code energized_cell_ceiling} instead of here.
     */
    @GameTest(template = "l1_room", batch = "energized_cell_grid", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCellReportsItsOwnBuffer(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var level = helper.getLevel();
                    // Two above the host. Measured, this cell is free air that touches no machine node at
                    // all (the two batteries placed here join each other into a grid of two nodes), so
                    // nothing here claims the machine sees them - the ceiling half is asserted in the
                    // sibling fixture {@code energized_cell_ceiling}. Overwriting a member instead (the
                    // first version used east(), which lands inside the 15-wide footprint) does not fail
                    // the test - it wedges the game test server, which is why this line is here.
                    var cellPos = helper.absolutePos(CARD_HOST).above(2);
                    level.setBlockAndUpdate(cellPos,
                            ModRegistration.ENERGIZED_ENERGY_CELL_16G_BLOCK.get().defaultBlockState());
                    if (!(level.getBlockEntity(cellPos) instanceof cn.dancingsnow.neoecoprototype
                            .blockentity.energy.EnergizedEnergyCellBlockEntity cell)) {
                        helper.fail("the placed block runs " + level.getBlockEntity(cellPos)
                                + ", not the battery");
                        return;
                    }
                    if (cell.getAEMaxPower() != 16_000_000_000.0) {
                        helper.fail("the 16G battery reports a buffer of " + cell.getAEMaxPower());
                        return;
                    }
                    if (!cell.isAEPublicPowerStorage()) {
                        helper.fail("the battery is not public storage, so the grid may never read it");
                        return;
                    }
                    // The hollow tier must not silently inherit the other block's number: both tiers share
                    // one block entity type, so the capacity really has to come off the block.
                    var bigPos = cellPos.east();
                    level.setBlockAndUpdate(bigPos,
                            ModRegistration.ENERGIZED_ENERGY_CELL_512G_BLOCK.get().defaultBlockState());
                    if (level.getBlockEntity(bigPos) instanceof cn.dancingsnow.neoecoprototype
                            .blockentity.energy.EnergizedEnergyCellBlockEntity big
                            && big.getAEMaxPower() != 256_000_000_000.0) {
                        helper.fail("the hollow battery reports " + big.getAEMaxPower()
                                + ", so the capacity is not coming off the block");
                        return;
                    }
                });
    }

    /**
     * The line AE2's own energy cell shows and ours did not: "Stored Energy: 1M/16G AE (0.01%)". It is
     * compared against AE2's own formatter, so a change upstream moves the expectation with it instead of
     * leaving a hand-typed string nobody can re-derive. The second half is the reason the line is not just
     * decoration: the number it shows has to come out of the item, and the only thing that puts it there is
     * the block entity exporting it when the battery is dismantled.
     */
    @GameTest(template = "empty", batch = "energized_cell_tooltip", timeoutTicks = 200)
    public static void energizedCellItemsShowTheStoredEnergyLine(GameTestHelper helper) {
        var level = helper.getLevel();
        var pos = helper.absolutePos(new BlockPos(0, 1, 0));
        level.setBlockAndUpdate(pos,
                ModRegistration.ENERGIZED_ENERGY_CELL_16G_BLOCK.get().defaultBlockState());
        if (!(level.getBlockEntity(pos) instanceof cn.dancingsnow.neoecoprototype
                .blockentity.energy.EnergizedEnergyCellBlockEntity cell)) {
            helper.fail("the placed block is not the battery, so the export below proves nothing");
            return;
        }
        cell.injectAEPower(1_000_000.0, Actionable.MODULATE);
        var builder = net.minecraft.core.component.DataComponentMap.builder();
        cell.exportSettings(appeng.util.SettingsFrom.DISMANTLE_ITEM, builder, null);
        var carried = builder.build().get(appeng.api.ids.AEComponents.STORED_ENERGY);
        if (carried == null || carried != 1_000_000.0) {
            helper.fail("a dismantled battery carrying 1M AE exports " + carried
                    + " in STORED_ENERGY, so the item has no number to show");
            return;
        }

        var stack = new ItemStack(ModRegistration.ENERGIZED_ENERGY_CELL_16G_ITEM.get());
        stack.set(appeng.api.ids.AEComponents.STORED_ENERGY, 1_000_000.0);
        // The game's own entry point: ItemStack#getTooltipLines is what the tooltip screen calls, so this
        // asserts the line a player sees rather than the one a hand-picked method would produce.
        var lines = stack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        var expected = appeng.core.localization.Tooltips
                .energyStorageComponent(1_000_000.0, 16_000_000_000.0).getString();
        if (lines.stream().noneMatch(line -> line.getString().equals(expected))) {
            helper.fail("the 16G item's tooltip has no line equal to '" + expected + "', it has "
                    + lines.stream().map(Component::getString).toList()
                    + " from item " + stack.getItem().getClass().getName());
            return;
        }

        // The tiers share one block entity type and one item class, so the number in the tooltip is the
        // check that the capacity is still coming off the block and not off the other tier.
        var bigStack = new ItemStack(ModRegistration.ENERGIZED_ENERGY_CELL_512G_ITEM.get());
        var bigLines = bigStack.getTooltipLines(net.minecraft.world.item.Item.TooltipContext.EMPTY, null,
                net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        var bigExpected = appeng.core.localization.Tooltips
                .energyStorageComponent(0.0, 256_000_000_000.0).getString();
        if (bigLines.stream().noneMatch(line -> line.getString().equals(bigExpected))) {
            helper.fail("the hollow item's tooltip has no line equal to '" + bigExpected + "', it has "
                    + bigLines.stream().map(Component::getString).toList());
            return;
        }
        helper.succeed();
    }

    /**
     * What the grid ceiling is actually made of: AE2 sums power capacity from the providers in the grid the
     * node joined, so two batteries standing next to each other must land in <i>one</i> ceiling.
     *
     * <p>This is the assertion that catches the registration the block itself cannot make: AE2 discovers an
     * in-world node by asking the neighbour's block entity for the {@code IN_WORLD_GRID_NODE_HOST}
     * capability, and it only registers that capability for its own block entity types. An addon battery
     * that is not added to that list by us sits alone in a grid of one node - each half of the sum is then
     * its own buffer, which the earlier version of this test accepted as passing.</p>
     */
    @GameTest(template = "empty", batch = "energized_cell_ceiling", timeoutTicks = 200,
            setupTicks = 10L)
    public static void energizedCellsAddUpInTheGridCeiling(GameTestHelper helper) {
        var level = helper.getLevel();
        var small = helper.absolutePos(new BlockPos(0, 1, 0));
        var big = small.east();
        level.setBlockAndUpdate(small,
                ModRegistration.ENERGIZED_ENERGY_CELL_16G_BLOCK.get().defaultBlockState());
        level.setBlockAndUpdate(big,
                ModRegistration.ENERGIZED_ENERGY_CELL_512G_BLOCK.get().defaultBlockState());
        helper.runAfterDelay(10, () -> {
            var cells = new cn.dancingsnow.neoecoprototype.blockentity.energy
                    .EnergizedEnergyCellBlockEntity[2];
            for (var pair : java.util.List.of(
                    java.util.Map.entry(small, 16_000_000_000.0),
                    java.util.Map.entry(big, 256_000_000_000.0))) {
                if (!(level.getBlockEntity(pair.getKey()) instanceof cn.dancingsnow.neoecoprototype
                        .blockentity.energy.EnergizedEnergyCellBlockEntity cell)) {
                    helper.fail("the block at " + pair.getKey() + " is not the battery any more");
                    return;
                }
                var grid = cell.getMainNode().getGrid();
                if (grid == null) {
                    helper.fail("the battery at " + pair.getKey()
                            + " has no grid 10 ticks after placement, so its node never joined");
                    return;
                }
                if (cells[0] == null) {
                    cells[0] = cell;
                } else {
                    cells[1] = cell;
                }
            }
            var first = cells[0].getMainNode().getGrid();
            var second = cells[1].getMainNode().getGrid();
            if (first != second) {
                helper.fail("the two adjacent batteries sit in two grids of " + first.size() + " and "
                        + second.size() + " nodes, so the neighbour never found our node host - "
                        + "is the battery block entity registered for IN_WORLD_GRID_NODE_HOST?");
                return;
            }
            double expected = 16_000_000_000.0 + 256_000_000_000.0;
            double ceiling = first.getEnergyService().getMaxStoredPower();
            if (ceiling < expected) {
                helper.fail("grid ceiling is " + ceiling + ", expected at least " + expected
                        + " (16G + 256G) - the batteries were never announced to the energy service");
                return;
            }
            helper.succeed();
        });
    }

    /**
     * The panel's drop path has to spend the held cell. It used to copy the stack the client claimed into
     * the drive and leave the hand alone, which made every drop a duplication - and it trusted that claim,
     * so a client could hand over a stack it was not actually holding. eco reads the server-side carried
     * stack, shrinks it, and clears the cursor when it runs out; this asserts the same arithmetic: two in
     * hand, one in hand plus one in the drive afterwards, never three.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelDropSpendsTheHeldCell(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    if (storageHost.getCluster() == null) {
                        helper.fail("the room never formed a cluster, so there is no drive to drop into");
                        return;
                    }
                    var target = storageHost.getCluster().getDrives().stream()
                            .filter(d -> !d.hasCell())
                            .findFirst().orElse(null);
                    if (target == null) {
                        helper.fail("every drive already holds a cell, so the drop path cannot be measured");
                        return;
                    }
                    var player = helper.makeMockPlayer(net.minecraft.world.level.GameType.SURVIVAL);
                    player.containerMenu.setCarried(
                            new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get(), 2));
                    var claimed = player.containerMenu.getCarried();
                    if (!storageHost.insertCellForPanel(target.getBlockPos(), claimed, player)) {
                        helper.fail("the panel refused a drop of a cell the player really was holding");
                        return;
                    }
                    if (!target.hasCell()) {
                        helper.fail("the insert reported success but the drive is still empty");
                        return;
                    }
                    var left = player.containerMenu.getCarried().getCount();
                    if (left != 1) {
                        helper.fail("holding two cells and dropping one left " + left
                                + " in hand - the drive took a copy nobody paid for (total "
                                + (left + 1) + " of 2)");
                        return;
                    }
                    // And the claim must not be the authority: a stack that is not what the hand holds
                    // cannot be inserted, no matter what the client says it is carrying.
                    var other = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_16K.get());
                    var second = storageHost.getCluster().getDrives().stream()
                            .filter(d -> !d.hasCell())
                            .findFirst().orElse(null);
                    if (second != null && storageHost.insertCellForPanel(second.getBlockPos(), other, player)) {
                        helper.fail("the panel accepted a claimed stack the player was not holding");
                    }
                    helper.succeed();
                });
    }

    /**
     * What the panel's consumption line counts: the storage components, and nothing else. eco's own formula
     * is {@code Math.max(0, Math.round(cell.getIdleDrain()))} per drive holding a cell, added up
     * ({@code ECOStorageHostStatistics.collectStorageUiSnapshot}, offsets 344..355), and our cells report
     * {@code bytes / 1 MiB} AE per tick - so a 16K matrix rounds to zero and a host full of them reads 0 AE/t
     * while the network still takes its ~32 AE/t per drive. That is the trade this asserts we are making:
     * the first case is the one that catches a regression back to summing grid nodes, because the machine's
     * own nodes are non-zero here and must NOT appear in the number.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelCountsTheCellsAndNothingElse(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)) {
                        helper.fail("the L1 storage controller is not our host block entity: " + host);
                        return;
                    }
                    var ownNode = storageHost.getMainNode().getNode();
                    if (ownNode == null || ownNode.getIdlePowerUsage() <= 0.0D) {
                        helper.fail("the host node itself draws "
                                + (ownNode == null ? "nothing (no node)" : ownNode.getIdlePowerUsage())
                                + " AE/t, so the exclusion below proves nothing");
                        return;
                    }
                    var drives = storageHost.getCluster() == null
                            ? java.util.List.<cn.dancingsnow.neoecoprototype.blockentity.storage
                                    .SimplifyDriveBlockEntity>of()
                            : storageHost.getCluster().getDrives();
                    var free = new java.util.ArrayList<>(drives.stream()
                            .filter(d -> !d.hasCell()).toList());
                    if (free.size() < 3) {
                        helper.fail("the room has " + free.size() + " empty drives and this needs three");
                        return;
                    }
                    if (storageHost.panelIdlePowerPerTick() != 0L) {
                        helper.fail("a host holding no matrix reports "
                                + storageHost.panelIdlePowerPerTick() + " AE/t - that is the machine's own"
                                + " nodes leaking into a line that is only supposed to count components");
                        return;
                    }
                    if (!free.get(0).insertCell(
                            new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_16K.get()))
                            || storageHost.panelIdlePowerPerTick() != 0L) {
                        helper.fail("a 16K matrix (0.015625 AE/t) did not round away to 0, the line read "
                                + storageHost.panelIdlePowerPerTick());
                        return;
                    }
                    if (!free.get(1).insertCell(
                            new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1M.get()))
                            || storageHost.panelIdlePowerPerTick() != 1L) {
                        helper.fail("a 1M matrix should add exactly 1 AE/t, the line read "
                                + storageHost.panelIdlePowerPerTick());
                        return;
                    }
                    if (!free.get(2).insertCell(
                            new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_4M.get()))
                            || storageHost.panelIdlePowerPerTick() != 5L) {
                        helper.fail("1M plus 4M should add 5 AE/t, the line read "
                                + storageHost.panelIdlePowerPerTick());
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * A slot on screen and the drive it writes into have to be the same place. The cluster hands its drives
     * over in scanner order, and the panel used to list them in exactly that order, so a dropped matrix
     * landed in some other drive. The assertion is deliberately written against world height instead of
     * reusing our own axis maths: the machine's rows are horizontal planes, so a correct listing can never
     * put a lower row before a higher one - and a number that only re-derives the sort would pass no matter
     * how the axes were chosen.
     */
    @GameTest(template = "l1_room", batch = "storage_panel_rows", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void panelRowsFollowTheMachineShape(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    if (!(host instanceof SimplifyStorageHostBlockEntity storageHost)
                            || storageHost.getCluster() == null) {
                        helper.fail("no formed L1 storage host to read the panel rows from");
                        return;
                    }
                    var drives = storageHost.getCluster().getDrives();
                    var filled = 0;
                    for (var drive : drives) {
                        if (drive.insertCell(
                                new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get()))) {
                            filled++;
                        }
                    }
                    if (filled < 2) {
                        helper.fail("only " + filled + " of " + drives.size()
                                + " drives took a cell, so row order cannot be measured");
                        return;
                    }
                    var entries = storageHost.createCellEntries();
                    var previousY = new int[]{Integer.MAX_VALUE};
                    var rows = new int[1];
                    for (var entry : entries) {
                        var pos = entry.position();
                        if (pos == null) {
                            continue;
                        }
                        rows[0]++;
                        if (pos.getY() > previousY[0]) {
                            helper.fail("the panel lists a drive at y=" + pos.getY() + " after one at y="
                                    + previousY[0] + " - a slot on screen and the drive it writes into are"
                                    + " different places");
                            return;
                        }
                        previousY[0] = pos.getY();
                    }
                    if (rows[0] < 2) {
                        helper.fail("the panel produced " + rows[0] + " positioned rows for " + filled
                                + " filled drives, so this scan proves nothing");
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * Every block of ours that carries a grid node must also answer AE2's block-capability query, or nothing
     * can find it from outside: AE2 discovers in-world nodes through {@code GridHelper.getNodeHost} ->
     * {@code level.getCapability(IN_WORLD_GRID_NODE_HOST)}, and it registers that capability for the block
     * entity types in <i>its own</i> registry only. This is the guard for the bug class the 16G/512G
     * batteries shipped with - the block entity implemented {@code IInWorldGridNodeHost}, every same-block
     * assertion stayed green, and a controller standing next to it still never added the buffer.
     */
    @GameTest(template = "l1_room", batch = "node_host_caps", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void everyGridBlockAnswersTheNodeHostCapability(GameTestHelper helper) {
        var level = helper.getLevel();
        var missing = new ArrayList<String>();
        var placed = new int[1];
        var exposed = new int[1];
        var slot = new int[]{0, 1, 0};
        for (var block : BuiltInRegistries.BLOCK) {
            var id = BuiltInRegistries.BLOCK.getKey(block);
            if (!NeoECOPrototype.MOD_ID.equals(id.getNamespace())) continue;
            // Only free air: overwriting a member of the standing machine would measure a broken room
            // instead of a broken registration, and has wedged this server before.
            while (!level.getBlockState(helper.absolutePos(
                    new BlockPos(slot[0], slot[1], slot[2]))).isAir()) {
                if (++slot[0] >= L1_ROOM_SIZE) {
                    slot[0] = 0;
                    if (++slot[1] >= L1_ROOM_SIZE) {
                        slot[1] = 1;
                        if (++slot[2] >= L1_ROOM_SIZE) {
                            helper.fail("the " + L1_ROOM_SIZE + "^3 template ran out of air after "
                                    + placed[0] + " of the " + NeoECOPrototype.MOD_ID + " blocks");
                            return;
                        }
                    }
                }
            }
            var rel = new BlockPos(slot[0], slot[1], slot[2]);
            slot[0]++;
            var abs = helper.absolutePos(rel);
            helper.setBlock(rel, block.defaultBlockState());
            placed[0]++;
            var be = level.getBlockEntity(abs);
            if (be instanceof IInWorldGridNodeHost) {
                exposed[0]++;
                if (appeng.api.networking.GridHelper.getNodeHost(level, abs) == null) {
                    missing.add(id.getPath() + " -> " + be.getClass().getSimpleName());
                }
            }
            // Take it back out again, here, in this cell. Only the template's own volume is restored after
            // a test and this scan walks the whole cube, so a cell left standing leaks into the next test's
            // room - which is how the first round of this guard failed an unrelated fixture one batch later.
            helper.setBlock(rel, Blocks.AIR);
        }
        if (exposed[0] == 0) {
            helper.fail("none of the " + placed[0] + " " + NeoECOPrototype.MOD_ID
                    + " blocks placed here carries a grid node, so this scan proves nothing");
            return;
        }
        if (!missing.isEmpty()) {
            helper.fail(missing.size() + " of " + exposed[0] + " node hosts cannot be found by a neighbour,"
                    + " so a cable or machine standing against them joins nothing: " + missing);
            return;
        }
        NeoECOPrototype.LOGGER.info("node hosts: {} of {} placed blocks carry a grid node and all answer"
                + " IN_WORLD_GRID_NODE_HOST", exposed[0], placed[0]);
        helper.succeed();
    }

    private static String defName(MultiBlockDefinition def) {
        return def == null ? "nothing (refused)"
                : net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getKey(def.getOwner().value()).getPath();
    }

    /**
     * Empty the fake player's inventory, put the card back, then hand over the parts the card printed as
     * its bill - {@code shortBy} of them withheld, so the caller can stand exactly at the price and one
     * part below it. Stacks are split to what the item allows; a 36-slot inventory cannot hold a shape
     * whose parts stack to one otherwise.
     */
    private static boolean handOutPrintedDifference(net.minecraft.server.level.ServerPlayer player,
                                                    KeyCounter parts, long shortBy) {
        player.getInventory().clearContent();
        giveExpansionCard(player);
        long withheld = shortBy;
        for (var entry : parts) {
            if (!(entry.getKey() instanceof appeng.api.stacks.AEItemKey key)) {
                return false;
            }
            long left = entry.getLongValue() - Math.min(withheld, entry.getLongValue());
            withheld -= Math.min(withheld, entry.getLongValue());
            while (left > 0) {
                var stack = new ItemStack(key.getItem());
                int handed = (int) Math.min(left, stack.getMaxStackSize());
                stack.setCount(handed);
                if (!player.getInventory().add(stack)) {
                    return false;
                }
                left -= handed;
            }
        }
        return withheld == 0;
    }

    /**
     * What a formed machine actually culls, asked with the renderer's own question:
     * {@code Block.shouldRenderFace} is the call a face that declares {@code cullface} goes through, and
     * it needs the blocks that really stand next to each other - so the room is built for real instead
     * of being reasoned about from a model file. Each printed line is one (our block, direction,
     * neighbour) triple with the two properties that decide holes and black faces side by side:
     * {@code draws} (would this face be rendered at all) and {@code invisible} (does the neighbour skip
     * its own drawing, which is what eco's shell does once the machine forms).
     */
    @GameTest(template = "l1_room", batch = "face_exposure_probe", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void formedMachineFaceCullingReadings(GameTestHelper helper) {
        buildL1Room(helper, CARD_HOST,
                ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                host -> {
                    var level = helper.getLevel();
                    var tally = new java.util.TreeMap<String, Integer>();
                    var invisible = new int[1];
                    measureFaces(level, machineSnapshot(helper), "l1room", tally, invisible);
                    // eco's three families at their ceiling. The plan's own target states are laid down
                    // without forming, which is enough: occlusion, light blocking and the shell's
                    // invisible flag all live in the block state, not in the block entity. Each machine
                    // gets its own patch of world so they cannot be each other's neighbours.
                    var eco = java.util.List.of(
                            NEMultiBlocks.STORAGE_SYSTEM_L4, NEMultiBlocks.STORAGE_SYSTEM_L6,
                            NEMultiBlocks.STORAGE_SYSTEM_L9,
                            NEMultiBlocks.COMPUTATION_SYSTEM_L4, NEMultiBlocks.COMPUTATION_SYSTEM_L6,
                            NEMultiBlocks.COMPUTATION_SYSTEM_L9,
                            NEMultiBlocks.CRAFTING_SYSTEM_L4, NEMultiBlocks.CRAFTING_SYSTEM_L6,
                            NEMultiBlocks.CRAFTING_SYSTEM_L9);
                    var shape = new java.util.HashMap<BlockPos, BlockState>();
                    for (var definition : eco) {
                        var anchor = helper.absolutePos(CARD_HOST)
                                .offset(48 * (eco.indexOf(definition) + 1), 0, 0);
                        level.setBlock(anchor, definition.getOwner().value().defaultBlockState(),
                                Block.UPDATE_ALL);
                        var plan = MultiBlockPlacementService.preview(level, anchor,
                                level.getBlockState(anchor), definition, definition.getExpandMax(), false);
                        if (plan.getAllBlocks().isEmpty()) {
                            clearMachineBox(helper);
                            helper.fail(name(definition.getOwner().value()) + " at length "
                                    + definition.getExpandMax() + " laid down nothing, so its faces"
                                    + " could not be measured");
                            return;
                        }
                        shape.clear();
                        shape.put(anchor.immutable(), level.getBlockState(anchor));
                        for (var block : plan.getAllBlocks()) {
                            level.setBlock(block.worldPos(), block.targetState(), 0);
                            shape.put(block.worldPos().immutable(), block.targetState());
                        }
                        measureFaces(level, shape, "eco:" + name(definition.getOwner().value()),
                                tally, invisible);
                        for (var pos : shape.keySet()) {
                            level.setBlock(pos,
                                    net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                                    Block.UPDATE_ALL);
                        }
                    }
                    clearMachineBox(helper);
                    // Without one of these the table below says nothing about a formed shell, so the
                    // probe has to be able to fail rather than print an empty report and look clean.
                    if (invisible[0] == 0) {
                        helper.fail("no neighbour in any of these machines reports itself invisible, so"
                                + " the probe measured nothing about a formed shell");
                        return;
                    }
                    tally.forEach((line, count) -> NeoECOPrototype.LOGGER.info(
                            "face-probe x" + count + " " + line));
                    NeoECOPrototype.LOGGER.info("face-probe triples whose neighbour draws nothing: "
                            + invisible[0]);
                    helper.succeed();
                });
    }

    /**
     * Ask the renderer's own question for every face of every block in {@code shape}: would this face
     * still be drawn where it stands, and what does the neighbour do about light and occlusion. One
     * merged line per (tag, block, direction, neighbour) so a whole machine fits in a screenful.
     */
    private static void measureFaces(net.minecraft.server.level.ServerLevel level,
                                     Map<BlockPos, BlockState> shape, String tag,
                                     java.util.TreeMap<String, Integer> tally, int[] invisible) {
        for (var entry : shape.entrySet()) {
            var pos = entry.getKey();
            var state = entry.getValue();
            for (var dir : Direction.values()) {
                var next = pos.relative(dir);
                var neighbour = level.getBlockState(next);
                if (neighbour.isAir()) {
                    continue;
                }
                boolean draws = Block.shouldRenderFace(state, level, pos, dir, next);
                boolean hidden = invisibleShell(neighbour);
                tally.merge("%s %-22s %-5s <- %-24s draws=%-5s invisible=%-5s occludes=%-5s lightBlock=%s"
                                .formatted(tag, name(state.getBlock()), dir.getName(),
                                        name(neighbour.getBlock()), draws, hidden,
                                        neighbour.canOcclude(), neighbour.getLightBlock(level, next)),
                        1, Integer::sum);
                if (hidden) {
                    invisible[0]++;
                }
            }
        }
    }

    /**
     * Whether a block skips drawing itself. eco writes an {@code invisible} property into its shell
     * once the machine forms; read off the state rather than by class, because the property is what the
     * renderer's own {@code skipRendering} looks at.
     */
    private static boolean invisibleShell(BlockState state) {
        var values = state.getValues();
        for (var property : state.getProperties()) {
            if (property.getName().equals("invisible")) {
                return String.valueOf(values.get(property)).equals("true");
            }
        }
        return false;
    }

    /**
     * A player the card's server half will actually accept. {@code GameTestHelper.makeMockPlayer} hands
     * back a bare {@code Player} (an anonymous subclass), while eco's builder needs a {@code ServerPlayer}
     * to place blocks and to charge for them - so the only stand-in that reaches that code is NeoForge's
     * fake player, which is a {@code ServerPlayer} and starts in survival mode.
     *
     * <p>It has to be stood next to the host as well: eco's {@code canPlayerInteract} is
     * {@code ECOStorageSystemBlock.isPlayerCloseEnough}, a distance check against the block position, and
     * a fake player spawns at world origin where every machine looks out of reach.
     */
    private static net.minecraft.server.level.ServerPlayer cardPlayer(GameTestHelper helper) {
        var player = net.neoforged.neoforge.common.util.FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(java.util.UUID.randomUUID(), "card-test"));
        BlockPos absolute = helper.absolutePos(CARD_HOST);
        player.setPos(absolute.getX() + 0.5D, absolute.getY() + 1.0D, absolute.getZ() + 0.5D);
        return player;
    }

    /** What the planner charges for the bigger shape, next to what the player actually holds. */
    private static String describeBill(net.minecraft.server.level.ServerPlayer player,
                                       cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan plan) {
        StringBuilder text = new StringBuilder();
        for (var required : plan.getRequiredItems()) {
            if (text.length() > 0) {
                text.append(", ");
            }
            text.append(net.minecraft.core.registries.BuiltInRegistries.ITEM
                            .getKey(required.stack().getItem()).getPath())
                    .append(" need ").append(required.count())
                    .append(" have ").append(cn.dancingsnow.neoecoae.multiblock.placement
                            .MultiBlockPlacementService.countMatchingItems(player, required.stack()));
        }
        return text.toString();
    }

    /**
     * The bill behind a ledger complaint: what the planner asks for at the bigger length, grouped by how
     * many items a single position costs, so a stack-sized loss can be told apart from a one-for-one bill.
     */
    private static String describeMissingBill(GameTestHelper helper, MultiBlockBuildController.Host host) {
        var def = host.getBuildDefinition();
        if (def == null) {
            return "no definition";
        }
        BlockPos absoluteHost = helper.absolutePos(CARD_HOST);
        var plan = cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService.preview(
                helper.getLevel(), absoluteHost, helper.getLevel().getBlockState(absoluteHost), def,
                def.getExpandMax(), host.isMirrorBuild());
        java.util.Map<Item, java.util.Map<Integer, Integer>> perKind = new java.util.LinkedHashMap<>();
        for (var planned : plan.getMissingBlocks()) {
            var stack = planned.requiredItem();
            perKind.computeIfAbsent(stack.getItem(), kind -> new java.util.TreeMap<>())
                    .merge(stack.getCount(), 1, Integer::sum);
        }
        StringBuilder text = new StringBuilder();
        perKind.forEach((kind, counts) -> text
                .append(net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(kind).getPath())
                .append("->").append(counts).append(' '));
        return "missing positions by requiredItem count: " + text;
    }

    /**
     * Puts the card in the player's hand. Hand it out before any materials: the test player starts with
     * hotbar slot 0 selected and materials land in the first free slots, so a card given later replaces
     * whatever stack was sitting there - one round lost 64 structure blocks that then stood in none of
     * the three places the ledger reads.
     */
    private static void giveExpansionCard(net.minecraft.server.level.ServerPlayer player) {
        player.setItemInHand(InteractionHand.MAIN_HAND,
                new ItemStack(ModRegistration.SIMPLIFY_STRUCTURE_EXPANSION_CARD.get()));
    }

    /** Right-click the host with the card in hand, sneaking, the way a player does it. */
    private static void useExpansionCard(GameTestHelper helper,
                                         net.minecraft.server.level.ServerPlayer player) {
        useCard(helper, player, ModRegistration.SIMPLIFY_STRUCTURE_EXPANSION_CARD.get());
    }

    /** Right-click the host with {@code expected} in hand, the way a player does it. */
    private static void useCard(GameTestHelper helper, net.minecraft.server.level.ServerPlayer player,
                                net.minecraft.world.item.Item expected) {
        var stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        if (!stack.is(expected)) {
            helper.fail("the fixture player holds " + stack.getHoverName().getString()
                    + " rather than the card, so this clicked with nothing in hand");
            return;
        }
        BlockPos absolute = helper.absolutePos(CARD_HOST);
        var hit = new net.minecraft.world.phys.BlockHitResult(Vec3.atCenterOf(absolute), Direction.EAST,
                absolute, false);
        stack.getItem().useOn(new UseOnContext(helper.getLevel(), player, InteractionHand.MAIN_HAND,
                stack, hit));
    }

    /**
     * A drive's collision shape is a full cube, so every up-facing quad of its model - the cavity floor
     * included - is drawn with the light of the cell standing on top of it. Two columns built the same
     * instant, differing only in that one cell, say whether that cell can hold light at all: the control
     * cell above both columns has to read open sky, or the comparison means nothing.
     */
    @GameTest(template = "l1_room", batch = "interior_light_path", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void driveAboveCellLightDemo(GameTestHelper helper) {
        var level = helper.getLevel();
        var base = helper.absolutePos(new BlockPos(1, 1, 1));
        var blocked = base;
        var through = base.offset(6, 0, 0);
        var written = new ArrayList<BlockPos>();
        java.util.function.BiConsumer<BlockPos, Block> buildColumn = (at, above) -> {
            for (BlockPos pos : List.of(at, at.above(), at.above(2))) {
                written.add(pos.immutable());
            }
            level.setBlockAndUpdate(at,
                    ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get().defaultBlockState());
            level.setBlockAndUpdate(at.above(),
                    ModRegistration.SIMPLIFY_DRIVE_BLOCK.get().defaultBlockState());
            level.setBlockAndUpdate(at.above(2), above.defaultBlockState());
        };
        buildColumn.accept(blocked, ModRegistration.SIMPLIFY_ENERGY_CELL_BLOCK.get());
        buildColumn.accept(through, ModRegistration.SIMPLIFY_DRIVE_BLOCK.get());
        helper.runAfterDelay(20, () -> {
            try {
                var engine = level.getLightEngine();
                int open = engine.getLayerListener(net.minecraft.world.level.LightLayer.SKY)
                        .getLightValue(blocked.above(4));
                NeoECOPrototype.LOGGER.info("light-probe control open-air cell two rows up: sky=" + open);
                if (open == 0) {
                    helper.fail("the test world gives this spot no skylight at all, so the two columns"
                            + " cannot be compared here");
                    return;
                }
                for (var pair : List.of(
                        Map.entry("blocker above", blocked.above(2)),
                        Map.entry("see-through above", through.above(2)))) {
                    var cell = pair.getValue();
                    var state = level.getBlockState(cell);
                    NeoECOPrototype.LOGGER.info("light-probe " + pair.getKey() + " -> the cell the drive's"
                            + " cavity floor samples: " + name(state.getBlock())
                            + " lightBlock=" + state.getLightBlock(level, cell)
                            + " occludes=" + state.canOcclude()
                            + " sky=" + engine.getLayerListener(net.minecraft.world.level.LightLayer.SKY)
                                    .getLightValue(cell)
                            + " block=" + engine.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK)
                                    .getLightValue(cell));
                }
                int behindBlocker = engine.getLayerListener(net.minecraft.world.level.LightLayer.SKY)
                        .getLightValue(blocked.above(2));
                int behindThrough = engine.getLayerListener(net.minecraft.world.level.LightLayer.SKY)
                        .getLightValue(through.above(2));
                if (behindThrough <= behindBlocker) {
                    helper.fail("the see-through column did not hold more skylight than the blocking one ("
                            + behindThrough + " vs " + behindBlocker + "), so what the model does with"
                            + " those cells is not what this test claims");
                    return;
                }
                NeoECOPrototype.LOGGER.info("light-probe same drive, same tick: skylight at the sampled"
                        + " cell " + behindBlocker + " under a blocker vs " + behindThrough
                        + " under a see-through block");
                helper.succeed();
            } finally {
                for (var pos : written) {
                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                }
            }
        });
    }

    private static void printLight(net.minecraft.server.level.ServerLevel level, String tag,
                                   BlockPos pos) {
        var state = level.getBlockState(pos);
        var engine = level.getLightEngine();
        NeoECOPrototype.LOGGER.info("light-probe " + tag + " " + pos.toShortString() + " "
                + name(state.getBlock())
                + " sky=" + engine.getLayerListener(net.minecraft.world.level.LightLayer.SKY)
                        .getLightValue(pos)
                + " block=" + engine.getLayerListener(net.minecraft.world.level.LightLayer.BLOCK)
                        .getLightValue(pos)
                + " lightBlock=" + state.getLightBlock(level, pos)
                + " occludes=" + state.canOcclude()
                + " emission=" + state.getLightEmission());
    }

    /** Every non-air block in the box the card can reach, keyed by world position. */
    private static Map<BlockPos, BlockState> machineSnapshot(GameTestHelper helper) {
        Map<BlockPos, BlockState> blocks = new HashMap<>();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, 8, L1_ROOM_SIZE - 1))) {
            BlockState state = helper.getLevel().getBlockState(pos);
            if (!state.isAir()) {
                blocks.put(pos.immutable(), state);
            }
        }
        return blocks;
    }

    /** The two halves of the ledger apart, so a loss says whether it fell out of the world or the bag. */
    private static Map<Item, Integer> blockCounts(GameTestHelper helper) {
        Map<Item, Integer> totals = new HashMap<>();
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, 8, L1_ROOM_SIZE - 1))) {
            Item asItem = helper.getLevel().getBlockState(pos).getBlock().asItem();
            if (asItem != Items.AIR) {
                totals.merge(asItem, 1, Integer::sum);
            }
        }
        return totals;
    }

    /**
     * How many of one item exist in the three places a tier swap can move it to: mounted inside a member of
     * the machine, in the player's inventory, and lying on the floor within the room. A swap that "keeps"
     * the contents has to leave this number alone in both directions - one copy too many is the duplicate a
     * player can sell, one too few is the cell they never get back.
     */
    private static int everywhere(GameTestHelper helper, net.minecraft.server.level.ServerPlayer player,
                                 Item item) {
        int total = 0;
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, 8, L1_ROOM_SIZE - 1))) {
            var blockEntity = helper.getLevel().getBlockEntity(pos);
            if (blockEntity instanceof cn.dancingsnow.neoecoae.util.ICellHost host) {
                var cell = host.getCellStack();
                if (cell != null && cell.is(item)) {
                    total += cell.getCount();
                }
            }
            if (blockEntity instanceof cn.dancingsnow.neoecoae.blocks.entity.crafting
                    .ECOCraftingPatternBusBlockEntity bus) {
                for (var stack : bus.getPatternSlotInventory()) {
                    if (stack.is(item)) {
                        total += stack.getCount();
                    }
                }
            }
        }
        for (var stack : player.getInventory().items) {
            if (stack.is(item)) {
                total += stack.getCount();
            }
        }
        for (var entity : helper.getLevel().getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class,
                new net.minecraft.world.phys.AABB(origin.getX(), origin.getY(), origin.getZ(),
                        origin.getX() + L1_ROOM_SIZE, origin.getY() + 9, origin.getZ() + L1_ROOM_SIZE))) {
            if (entity.getItem().is(item)) {
                total += entity.getItem().getCount();
            }
        }
        return total;
    }

    private static Map<Item, Integer> carried(net.minecraft.server.level.ServerPlayer player) {
        Map<Item, Integer> totals = new HashMap<>();
        for (var stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return totals;
    }

    /** What one machine block is worth, counted in all three places it can be. */
    private static Map<Item, Integer> ledger(GameTestHelper helper,
                                             net.minecraft.server.level.ServerPlayer player) {
        Map<Item, Integer> totals = new HashMap<>();
        for (var stack : player.getInventory().items) {
            if (!stack.isEmpty()) {
                totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        for (var stack : player.getInventory().offhand) {
            if (!stack.isEmpty()) {
                totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, 8, L1_ROOM_SIZE - 1))) {
            Item asItem = helper.getLevel().getBlockState(pos).getBlock().asItem();
            if (asItem != Items.AIR) {
                totals.merge(asItem, 1, Integer::sum);
            }
        }
        var box = new net.minecraft.world.phys.AABB(origin.getX(), origin.getY(), origin.getZ(),
                origin.getX() + L1_ROOM_SIZE, origin.getY() + 9, origin.getZ() + L1_ROOM_SIZE);
        for (var dropped : helper.getLevel().getEntitiesOfClass(
                net.minecraft.world.entity.item.ItemEntity.class, box)) {
            var stack = dropped.getItem();
            if (!stack.isEmpty()) {
                totals.merge(stack.getItem(), stack.getCount(), Integer::sum);
            }
        }
        return totals;
    }

    /** The grown columns fall outside what the template restores, so only a manual sweep puts them back. */
    private static void clearMachineBox(GameTestHelper helper) {
        BlockPos origin = helper.absolutePos(BlockPos.ZERO);
        for (BlockPos pos : BlockPos.betweenClosed(origin,
                origin.offset(L1_ROOM_SIZE - 1, 8, L1_ROOM_SIZE - 1))) {
            if (!helper.getLevel().getBlockState(pos).isAir()) {
                helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            }
        }
    }

    private static String changed(Map<BlockPos, BlockState> before, Map<BlockPos, BlockState> after) {
        StringBuilder text = new StringBuilder();
        for (var entry : before.entrySet()) {
            BlockState now = after.get(entry.getKey());
            if (now != entry.getValue()) {
                if (text.length() > 0) {
                    text.append("; ");
                }
                text.append(entry.getKey()).append(" went from ").append(entry.getValue().getBlock())
                        .append(" to ").append(now == null ? "air" : now.getBlock());
            }
        }
        return text.length() == 0 ? "nothing" : text.toString();
    }

    /**
     * A dissociator fed by FE alone, with no ME network anywhere near it: one core, one run's worth of
     * both baths and enough charge in the buffer must come out as exactly draws x perHit items, all of
     * them kinds the shipped table names, and the core and the fluids must be gone.
     */
    @GameTest(template = "empty", batch = "dissociator_run", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociatorRunsOnItsOwnBuffer(GameTestHelper helper) {
        var plotPos = new BlockPos(0, 1, 0);
        helper.setBlock(plotPos, ModRegistration.METEORITE_DISSOCIATOR_BLOCK.get());
        var level = helper.getLevel();
        var pos = helper.absolutePos(plotPos);
        if (!(level.getBlockEntity(pos)
                instanceof cn.dancingsnow.neoecoprototype.blockentity.dissociation
                .MeteoriteDissociatorBlockEntity machine)) {
            helper.fail("the dissociator block placed without a block entity of its own type");
            return;
        }
        var recipe = level.getRecipeManager()
                .getRecipeFor(ModRegistration.DISSOCIATION_TYPE.get(),
                        new cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe.Input(
                                new ItemStack(ModRegistration.METEORITE_CORE_ITEM.get())), level)
                .map(holder -> holder.value()).orElse(null);
        if (recipe == null) {
            helper.fail("the dissociation table that ships in the jar was not found at run time,"
                    + " so this test would prove nothing about the machine");
            return;
        }
        machine.getInputInventory().setItemDirect(0,
                new ItemStack(ModRegistration.METEORITE_CORE_ITEM.get()));
        // Two runs' worth of each bath, so the test can read off exactly one run's worth being spent
        // rather than merely noticing that an empty tank stayed empty.
        machine.getCoolantTank().setFluid(recipe.coolant().copyWithAmount(recipe.coolant().getAmount() * 2));
        machine.getSolutionTank().setFluid(recipe.solution().copyWithAmount(recipe.solution().getAmount() * 2));
        int neededFe = (int) Math.ceil(appeng.api.config.PowerUnit.AE
                .convertTo(appeng.api.config.PowerUnit.FE, recipe.powerPerRun()));
        for (int fed = 0; fed < neededFe; fed += cn.dancingsnow.neoecoprototype.blockentity
                .dissociation.MeteoriteDissociatorBlockEntity.FE_MAX_RECEIVE) {
            machine.getEnergyStorage().receiveEnergy(
                    cn.dancingsnow.neoecoprototype.blockentity.dissociation
                            .MeteoriteDissociatorBlockEntity.FE_MAX_RECEIVE, false);
        }
        var state = level.getBlockState(pos);
        for (int tick = 0; tick < recipe.ticksPerRun() + 2; tick++) {
            cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity
                    .serverTick(level, pos, state, machine);
        }
        if (!machine.getInputInventory().getStackInSlot(0).isEmpty()) {
            helper.fail("the run finished but the core is still in the input slot");
            return;
        }
        if (machine.getCoolantTank().getFluidAmount() != recipe.coolant().getAmount()) {
            helper.fail("a run left the coolant bath at " + machine.getCoolantTank().getFluidAmount()
                    + " mB of the " + recipe.coolant().getAmount() * 2 + " it was given, so it did not"
                    + " spend the " + recipe.coolant().getAmount() + " mB the table asks for");
            return;
        }
        if (machine.getSolutionTank().getFluidAmount() != recipe.solution().getAmount()) {
            helper.fail("a run left the solution bath at " + machine.getSolutionTank().getFluidAmount()
                    + " mB of the " + recipe.solution().getAmount() * 2 + " it was given");
            return;
        }
        int produced = 0;
        var products = recipe.weights().keySet();
        for (int slot = 0; slot < machine.getOutputInventory().size(); slot++) {
            var stack = machine.getOutputInventory().getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            if (!products.contains(stack.getItem())) {
                helper.fail("the machine produced "
                        + BuiltInRegistries.ITEM.getKey(stack.getItem())
                        + ", which the table does not list as a product");
                return;
            }
            produced += stack.getCount();
        }
        if (produced != recipe.maxPerProduct()) {
            helper.fail("a run yielded " + produced + " items but the table says draws x per_hit = "
                    + recipe.maxPerProduct() + "; something was lost between the roll and the slots");
            return;
        }
        helper.succeed();
    }

    /**
     * A machine that has never seen a bucket must still be able to sync itself to a client. This is the
     * path that took the integrated server down once: the visual-state codec is reached on every chunk
     * update, and a codec that refuses an empty stack turns the machine's default state into a crash.
     */
    @GameTest(template = "empty", batch = "dissociator_sync_packet", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociatorSyncsWhileItsTanksAreEmpty(GameTestHelper helper) {
        var plotPos = new BlockPos(0, 1, 0);
        helper.setBlock(plotPos, ModRegistration.METEORITE_DISSOCIATOR_BLOCK.get());
        var level = helper.getLevel();
        var pos = helper.absolutePos(plotPos);
        if (!(level.getBlockEntity(pos)
                instanceof cn.dancingsnow.neoecoprototype.blockentity.dissociation
                .MeteoriteDissociatorBlockEntity machine)) {
            helper.fail("the dissociator block placed without a block entity of its own type");
            return;
        }
        if (!machine.getCoolantTank().isEmpty() || !machine.getSolutionTank().isEmpty()) {
            helper.fail("a freshly placed dissociator already holds fluid, so this no longer tests the"
                    + " state that crashed");
            return;
        }
        net.minecraft.nbt.CompoundTag tag;
        var receiver = level.getBlockEntity(pos);
        try {
            tag = machine.getUpdateTag(level.registryAccess());
            receiver.handleUpdateTag(tag, level.registryAccess());
        } catch (RuntimeException thrown) {
            helper.fail("syncing an empty dissociator threw " + thrown.getClass().getSimpleName()
                    + ": " + thrown.getMessage());
            return;
        }
        helper.succeed();
    }

    /**
     * The same machine with an empty coolant bath must sit there doing nothing: it may not consume the
     * core, and it may not spend the charge. This is the other half of the assertion above - without it,
     * a machine that ignored the fluids would still pass.
     */
    @GameTest(template = "empty", batch = "dissociator_refuses_without_coolant", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void dissociatorRefusesToRunWithoutCoolant(GameTestHelper helper) {
        var plotPos = new BlockPos(0, 1, 0);
        helper.setBlock(plotPos, ModRegistration.METEORITE_DISSOCIATOR_BLOCK.get());
        var level = helper.getLevel();
        var pos = helper.absolutePos(plotPos);
        if (!(level.getBlockEntity(pos)
                instanceof cn.dancingsnow.neoecoprototype.blockentity.dissociation
                .MeteoriteDissociatorBlockEntity machine)) {
            helper.fail("the dissociator block placed without a block entity of its own type");
            return;
        }
        machine.getInputInventory().setItemDirect(0,
                new ItemStack(ModRegistration.METEORITE_CORE_ITEM.get()));
        machine.getSolutionTank().setFluid(new net.neoforged.neoforge.fluids.FluidStack(
                ModRegistration.DISSOLUTION_SOLUTION.get(), 5_000));
        for (int fed = 0; fed < 400_000; fed += cn.dancingsnow.neoecoprototype.blockentity.dissociation
                .MeteoriteDissociatorBlockEntity.FE_MAX_RECEIVE) {
            machine.getEnergyStorage().receiveEnergy(cn.dancingsnow.neoecoprototype.blockentity.dissociation
                    .MeteoriteDissociatorBlockEntity.FE_MAX_RECEIVE, false);
        }
        var state = level.getBlockState(pos);
        for (int tick = 0; tick < 40; tick++) {
            cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity
                    .serverTick(level, pos, state, machine);
        }
        if (machine.getInputInventory().getStackInSlot(0).isEmpty()) {
            helper.fail("the machine ate a core with no coolant in the bath: the fluid check is not"
                    + " stopping a run");
            return;
        }
        int produced = 0;
        for (int slot = 0; slot < machine.getOutputInventory().size(); slot++) {
            produced += machine.getOutputInventory().getStackInSlot(slot).getCount();
        }
        if (produced != 0) {
            helper.fail("the machine yielded " + produced + " item(s) without coolant");
            return;
        }
        helper.succeed();
    }

    /**
     * The brackets the crafting panel's queue strip hangs above and below every working core. eco numbers
     * those slots with the core's own tier index, and uses that very same number - 0 - for "no core stands
     * here", so an addon tier indexed 0 loses the brackets outright: the artwork returns for anything
     * outside 1..3. That is what an F1 host looked like until
     * {@code SimplifyCraftingSystemBlockEntity#getCraftingControllerPanelState()} started rebuilding them.
     *
     * <p>Deliberately order-free. Which slot a worker lands in is decided by a sort eco does privately, so
     * this counts how many slots light up and compares that with how many workers the cluster really has a
     * core over - a wrong slot order would still pass here, and that half is the eye check on a standing
     * machine. What this does catch is the strip coming back empty, a bracket sitting where no core is, and
     * a core landing in some other tier's column.
     */
    @GameTest(template = "l1_room", batch = "crafting_panel_brackets", timeoutTicks = 600,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void craftingCoresLightTheirOwnBracketSlots(GameTestHelper helper) {
        buildL1Room(helper, new BlockPos(6, 3, 6),
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), host -> {
                    if (!(host instanceof cn.dancingsnow.neoecoprototype.blockentity.crafting
                            .SimplifyCraftingSystemBlockEntity f1)) {
                        helper.fail("the F1 host is not our block entity: " + host.getClass());
                        return;
                    }
                    var cluster = f1.getCluster();
                    if (cluster == null || cluster.getWorkers().isEmpty()) {
                        helper.fail("the formed F1 host reports no workers, so this scan proves nothing");
                        return;
                    }
                    int slots = cn.dancingsnow.neoecoae.gui.crafting.CraftingControllerPanelState.SLOT_COUNT;
                    if (cluster.getWorkers().size() > slots) {
                        helper.fail("the shape stands " + cluster.getWorkers().size()
                                + " workers but eco's strip only carries " + slots
                                + ", so the counts below are not comparable");
                        return;
                    }
                    var corePositions = cluster.getParallelCores().stream()
                            .map(net.minecraft.world.level.block.entity.BlockEntity::getBlockPos)
                            .collect(java.util.stream.Collectors.toSet());
                    if (corePositions.isEmpty()) {
                        helper.fail("the F1 build plan stands no parallel core, so the strip has nothing"
                                + " to draw and this check would be vacuous");
                        return;
                    }
                    var up = appeng.api.orientation.OrientationStrategies.horizontalFacing()
                            .getSide(f1.getBlockState(), appeng.api.orientation.RelativeSide.TOP);
                    int withCoreAbove = 0;
                    int withCoreBelow = 0;
                    for (var worker : cluster.getWorkers()) {
                        if (corePositions.contains(worker.getBlockPos().relative(up))) {
                            withCoreAbove++;
                        }
                        if (corePositions.contains(worker.getBlockPos().relative(up.getOpposite()))) {
                            withCoreBelow++;
                        }
                    }
                    var state = f1.getCraftingControllerPanelState();
                    int litAbove = 0;
                    int litBelow = 0;
                    for (int slot = 0; slot < slots; slot++) {
                        var upper = state.upperTier(slot);
                        var lower = state.lowerTier(slot);
                        if (upper != 0) {
                            litAbove++;
                        }
                        if (lower != 0) {
                            litBelow++;
                        }
                        if (upper != 0 && upper != 1 || lower != 0 && lower != 1) {
                            helper.fail("slot " + slot + " reads upper=" + upper + " lower=" + lower
                                    + "; an F1 core has to land in column 1, the one the artwork paints green");
                            return;
                        }
                    }
                    if (litAbove != withCoreAbove || litBelow != withCoreBelow) {
                        helper.fail("the strip lights " + litAbove + " upper and " + litBelow
                                + " lower bracket slot(s) for a cluster with " + withCoreAbove
                                + " worker(s) under a core and " + withCoreBelow + " above one");
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The lit-pass wrapper our client installs for the cooling controller is keyed on one string - this
     * blockstate file's name - and the coupling fails in silence: rename the file and the block still draws,
     * just unlit, with nothing to point at. So the guard reads the file under that same name and checks the
     * shape the wrapper assumes, which is eight formed variants (four facings, each with both mirror
     * settings) pointing at the two composite models the wrapper re-lights. Composite is the part that keeps
     * the two passes from doubling up: the wrapper hands the terrain layer nothing, so the geometry has to
     * exist behind a child model rather than on the blockstate model itself.
     */
    @GameTest(template = "empty", batch = "cooling_controller_art", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void coolingControllerFormedVariantsAreWhatTheWrapperMatches(GameTestHelper helper) {
        String states = readResource(
                "/assets/neoecoprototype/blockstates/simplify_computation_cooling_controller.json", helper);
        if (states == null) {
            return;
        }
        var variants = com.google.gson.JsonParser.parseString(states).getAsJsonObject()
                .getAsJsonObject("variants");
        if (variants.size() == 0) {
            helper.fail("the cooling controller blockstate carries no variants, so this guard would pass on"
                    + " an empty scan");
            return;
        }
        int formed = 0;
        for (var entry : variants.entrySet()) {
            if (!java.util.List.of(entry.getKey().split(",")).contains("formed=true")) {
                continue;
            }
            formed++;
            var model = entry.getValue().getAsJsonObject().get("model").getAsString();
            if (!model.endsWith("computation_cooling_controller/controller_l1_formed")
                    && !model.endsWith("computation_cooling_controller/controller_l1_formed_mirrored")) {
                helper.fail("variant " + entry.getKey() + " draws " + model + ", which the wrapper does not"
                        + " recognise as a formed cooling controller");
                return;
            }
        }
        if (formed != 8) {
            helper.fail("the blockstate carries " + formed + " formed variant(s); the wrapper was measured"
                    + " against 8, which is four facings times both mirror settings");
            return;
        }
        for (var file : java.util.List.of("controller_l1_formed", "controller_l1_formed_mirrored")) {
            String json = readResource("/assets/neoecoprototype/models/block/computation_cooling_controller/"
                    + file + ".json", helper);
            if (json == null) {
                return;
            }
            var root = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            if (!root.has("loader") || !"neoforge:composite".equals(root.get("loader").getAsString())) {
                helper.fail(file + " is not a neoforge:composite model, so the terrain layer would draw it"
                        + " as well and the lit pass would double it up");
                return;
            }
        }
        helper.succeed();
    }

    /** Every node our own advancement tree ships, named by file so a deleted node fails loudly. */
    private static final String[] ADVANCEMENT_NODES = {
            "root", "storage_controller", "drive_mounted", "first_l1_cell", "assembler_built",
            "energized_cell", "powered_interface", "computation_host", "crafting_host",
            "pattern_provider", "small_bulk", "fe_matrix", "chemical_matrix",
            "mind", "migration_drive", "tix_doll"};

    /** The one criterion string the two easter-egg awards in Java pass to {@code award}. */
    private static final String EASTER_EGG_CRITERION = "easter_egg";

    /** Storage matrices that are registered whatever is installed, so the tag must always list them. */
    private static final String[] ALWAYS_REGISTERED_MATRICES = {
            "simplify_item_storage_cell_1k", "simplify_item_storage_cell_16k", "simplify_item_storage_cell_64k",
            "simplify_item_storage_cell_1m", "simplify_item_storage_cell_4m",
            "simplify_fluid_storage_cell_1k", "simplify_fluid_storage_cell_16k", "simplify_fluid_storage_cell_64k",
            "simplify_fluid_storage_cell_1m", "simplify_fluid_storage_cell_4m",
            "simplify_concrete_storage_cell", "pigmee_storage_cell", "simplify_singularity_cell"};

    /**
     * Ids the "any L1 matrix" achievement must not count. Beyond Dimensions' cell is registered and
     * even sits in the creative tab when that mod is installed, but nothing crafts it (AGENTS.md §15),
     * and the decided half of that row is "no achievement for support we never delivered". The list
     * above can only say "this one has to be there"; without this one the decision lives in a markdown
     * file and a later edit to the tag would silently undo it.
     */
    private static final String[] MATRICES_OUTSIDE_THE_ACHIEVEMENT = {"neoecoprototype:beyond_storage_cell"};

    private static boolean itemRegistered(String id) {
        return BuiltInRegistries.ITEM.containsKey(ResourceLocation.parse(id));
    }

    /** Whether a texture reference of the {@code namespace:path.png} form ships in this jar. */
    private static boolean textureExists(String reference) {
        ResourceLocation id = ResourceLocation.parse(reference);
        return NeoECOPrototype.class.getResource(
                "/assets/" + id.getNamespace() + "/" + id.getPath()) != null;
    }

    /**
     * Pins the L1 advancement tree on the ways it can silently break.
     *
     * <p>Each node is first asked whether the server loaded it, because an optional family that is not
     * installed gates its own node off and then every item id inside that file is absent by design - read
     * the load state after the contents and the guard calls a correct install a broken one. A node that
     * did load must not name items nobody registered, and one that names a missing parent or carries no
     * condition must not be absent. The language check exists because the JSON carries {@code translate}
     * components: a missing key shows the raw key to the player. And the criterion name for the two
     * {@code minecraft:impossible} nodes is only matched to the Java by agreement, so it is asserted here
     * instead of trusted.
     */
    @GameTest(template = "empty", batch = "advancement_tree", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void advancementTreeLoadsAndNamesRealItems(GameTestHelper helper) {
        // The instrument first: a resolver that cannot answer "no" would make every check below vacuous.
        if (itemRegistered("neoecoprototype:definitely_not_an_item")) {
            helper.fail("the item resolver says a made-up id is registered - it cannot reject, so nothing"
                    + " below it can be trusted");
            return;
        }
        if (!itemRegistered("neoecoprototype:simplify_green_crystal_matrix")) {
            helper.fail("the item resolver rejects an item that is registered - the resolver is broken");
            return;
        }

        var advancements = ((ServerLevel) helper.getLevel()).getServer().getAdvancements();
        var titleKeys = new java.util.LinkedHashSet<String>();
        for (String name : ADVANCEMENT_NODES) {
            String json = readResource("/data/neoecoprototype/advancement/" + name + ".json", helper);
            if (json == null) {
                return;
            }
            com.google.gson.JsonObject node;
            try {
                node = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            } catch (com.google.gson.JsonParseException | IllegalStateException failure) {
                helper.fail(name + ".json did not parse: " + failure);
                return;
            }

            String icon = node.getAsJsonObject("display").getAsJsonObject("icon").get("id").getAsString();
            if ("root".equals(name)) {
                // A tab without a background paints the missing-texture checker across the whole panel.
                // Nothing in the log says so - only eyes catch it - so it is asserted here instead.
                if (!node.getAsJsonObject("display").has("background")) {
                    helper.fail("root has no display.background, so the tab draws a missing-texture grid"
                            + " behind every node");
                    return;
                }
                String background = node.getAsJsonObject("display").get("background").getAsString();
                if (!textureExists(background)) {
                    helper.fail("root's background " + background + " is not a texture in this jar");
                    return;
                }
            }

            // Resolve whether the server actually took this file before reading anything about its
            // contents: an optional family that is not installed gates its node off on purpose, and then
            // every item id in the file is legitimately absent.
            boolean loaded = advancements.get(ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, name)) != null;
            if (node.has("neoforge:conditions")) {
                var condition = node.getAsJsonArray("neoforge:conditions").get(0).getAsJsonObject();
                String gate = condition.get("item").getAsString();
                if (!gate.equals(icon)) {
                    helper.fail(name + " is gated on " + gate + " but shows " + icon
                            + " - one of the two will not follow the other when the family moves");
                    return;
                }
                if (loaded != itemRegistered(gate)) {
                    helper.fail(name + " loaded=" + loaded + " while its gate item " + gate
                            + " is registered=" + itemRegistered(gate));
                    return;
                }
            } else if (!loaded) {
                helper.fail(name + " carries no condition yet the server did not load it - its parent is"
                        + " missing or the file was rejected");
                return;
            }
            // The one thing the branch above cannot say: a node that loaded may still paint an item
            // nobody registered - an icon edited without its gate, or a family retired. Checking the
            // icon before this point is wrong, because a gated-off node is absent with an unregistered
            // icon on purpose.
            if (loaded && !itemRegistered(icon)) {
                helper.fail(name + " is loaded and paints " + icon + " as its icon, but that item is not"
                        + " registered, so the tree shows a missing-texture cube");
                return;
            }

            var criteria = node.getAsJsonObject("criteria");
            if (criteria.size() != 1) {
                helper.fail(name + " has " + criteria.size() + " criteria; the tree expects exactly one");
                return;
            }
            for (var entry : criteria.entrySet()) {
                var criterion = entry.getValue().getAsJsonObject();
                String trigger = criterion.get("trigger").getAsString();
                if (!"minecraft:inventory_changed".equals(trigger)) {
                    if (!EASTER_EGG_CRITERION.equals(entry.getKey())) {
                        helper.fail(name + " is awarded by code but names its criterion \""
                                + entry.getKey() + "\", while the Java passes \""
                                + EASTER_EGG_CRITERION + "\"");
                        return;
                    }
                    continue;
                }
                for (var group : criterion.getAsJsonObject("conditions").getAsJsonArray("items")) {
                    var groupObject = group.getAsJsonObject();
                    if (groupObject.has("tag")) {
                        // A tag whose every entry is optional can quietly end up empty, so the families
                        // that are always registered are asserted by name rather than by count.
                        String tagId = groupObject.get("tag").getAsString();
                        ResourceLocation tagKey = ResourceLocation.parse(tagId);
                        String tagJson = readResource("/data/" + tagKey.getNamespace()
                                + "/tags/item/" + tagKey.getPath() + ".json", helper);
                        if (tagJson == null) {
                            return;
                        }
                        var values = com.google.gson.JsonParser.parseString(tagJson).getAsJsonObject()
                                .getAsJsonArray("values");
                        var listed = new java.util.HashSet<String>();
                        int registered = 0;
                        for (var tagEntry : values) {
                            String id = tagEntry.getAsJsonObject().get("id").getAsString();
                            listed.add(id);
                            if (itemRegistered(id)) {
                                registered++;
                            }
                        }
                        if (registered < 13) {
                            helper.fail(tagId + " resolves to only " + registered
                                    + " registered items; the 13 always-registered matrices should all be in it");
                            return;
                        }
                        for (String always : ALWAYS_REGISTERED_MATRICES) {
                            if (!listed.contains("neoecoprototype:" + always)) {
                                helper.fail(tagId + " no longer lists " + always
                                        + ", so that family stopped counting for the cell achievement");
                                return;
                            }
                        }
                        for (String outside : MATRICES_OUTSIDE_THE_ACHIEVEMENT) {
                            if (listed.contains(outside)) {
                                helper.fail(tagId + " lists " + outside + ", which is the family that is"
                                        + " registered but has no recipe - the tree would hand out an"
                                        + " achievement for support we never delivered");
                                return;
                            }
                        }
                        continue;
                    }
                    for (var item : groupObject.getAsJsonArray("items")) {
                        if (loaded && !itemRegistered(item.getAsString())) {
                            helper.fail(name + " loaded and waits on " + item.getAsString()
                                    + " in criterion \"" + entry.getKey() + "\", which is not registered"
                                    + " - that item was retired without retiring the node");
                            return;
                        }
                    }
                }
            }

            titleKeys.add("advancement.neoecoprototype." + name + ".title");
            titleKeys.add("advancement.neoecoprototype." + name + ".description");
        }

        for (String lang : new String[]{"en_us", "zh_cn"}) {
            String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
            if (json == null) {
                return;
            }
            com.google.gson.JsonObject keys;
            try {
                keys = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            } catch (com.google.gson.JsonParseException | IllegalStateException failure) {
                helper.fail("lang/" + lang + ".json did not parse: " + failure);
                return;
            }
            for (String key : titleKeys) {
                if (!keys.has(key) || keys.get(key).getAsString().isBlank()) {
                    helper.fail("lang/" + lang + ".json has no " + key
                            + ", so that node shows the raw key in the tree");
                    return;
                }
            }
        }
        helper.succeed();
    }

    /**
     * The same question {@code assemblerCanPushItsProductIntoOurProvider} answers for providers, asked of
     * the two interfaces a player reported: AE2 hands a foreign pipe an item handler only through its
     * generic-adapter hook, which wraps blocks answering {@code AECapabilities.GENERIC_INTERNAL_INV}, and
     * that wrap is keyed on the block entity type AE2 registered for itself. Both of our interfaces carry
     * their own type, so the question is whether anything answers there. AE2's own interface is the
     * control: if it exposed nothing either, a pipe complaint could not be about us - it does.
     *
     * <p>The F1 machine interface is put in the table but not asserted: its block entity class is eco's
     * {@code ECOMachineInterfaceBlockEntity}, and eco posts only
     * {@code AECapabilities.IN_WORLD_GRID_NODE_HOST} for its own entries (read off
     * {@code NEBlockEntityEntry}), so that half is eco's shape to answer, not ours to paper over.
     *
     * <p>The F1 smart pattern bus is asserted twice over, because it is a different kind of exposure: the
     * two interfaces hand out an inventory AE2 wraps, while the bus hands out eco's own item handler over
     * the slots that hold the player's patterns. So besides "does a pipe see anything", this asks the
     * question that decides whether that exposure is safe at all - can a pipe push a non-pattern into a
     * pattern slot? eco filters the bus's inventory to encoded patterns; if that filter did not hold on
     * this path, a pipe would be free to bury someone's pattern library under cobblestone.
     */
    @GameTest(template = "empty", batch = "interface_caps_probe", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void interfacesAnswerThePipeCapability(GameTestHelper helper) {
        var level = helper.getLevel();
        var caps = net.neoforged.neoforge.capabilities.Capabilities.ItemHandler.BLOCK;
        var generic = appeng.api.AECapabilities.GENERIC_INTERNAL_INV;
        var ours = new net.minecraft.world.level.block.Block[]{
                ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get(),
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get(),
                ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()};
        var blocks = new net.minecraft.world.level.block.Block[]{
                AEBlocks.INTERFACE.block(), ours[0], ours[1], ours[2],
                ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BLOCK.get()};

        var report = new StringBuilder();
        var exposed = new boolean[blocks.length];
        var pos = new BlockPos(1, 1, 1);
        for (int i = 0; i < blocks.length; i++) {
            helper.setBlock(pos, blocks[i]);
            var abs = helper.absolutePos(pos);
            exposed[i] = level.getCapability(caps, abs, Direction.UP) != null;
            report.append(' ').append(net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(blocks[i]))
                    .append(exposed[i] ? "=itemHandler" : "=none")
                    .append(level.getCapability(generic, abs, Direction.UP) != null ? "+genericInv" : "")
                    .append(";");
        }
        if (!exposed[0]) {
            helper.fail("AE2's own interface exposes no item handler, so a pipe complaint cannot be about"
                    + " ours - this probe is asking the wrong thing. Table:" + report);
            return;
        }
        var missing = new StringBuilder();
        for (int i = 1; i <= ours.length; i++) {
            if (!exposed[i]) {
                missing.append(' ').append(net.minecraft.core.registries.BuiltInRegistries.BLOCK
                        .getKey(blocks[i]));
            }
        }
        if (missing.length() > 0) {
            helper.fail("these of our blocks expose no item handler while AE2's own does:" + missing
                    + " ; table:" + report);
            return;
        }
        // The bus is the one entry here whose slots hold something a player wants kept clean: their pattern
        // grid. Unlike the two interfaces this is not AE2's wrapped inventory but eco's own handler, so the
        // only thing between a pipe and a bus full of cobblestone is the encoded-pattern filter eco sets on
        // the inventory underneath - and that has to hold here, or the exposure we just made is the next
        // bug report.
        var busPos = new BlockPos(1, 1, 1);
        helper.setBlock(busPos, ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get());
        var bus = level.getCapability(caps, helper.absolutePos(busPos), Direction.UP);
        if (bus == null || bus.getSlots() <= 0) {
            helper.fail("the pattern bus answers the pipe face with "
                    + (bus == null ? "nothing" : bus.getSlots() + " slots")
                    + ", expected a handler over its pattern grid");
            return;
        }
        var offered = new ItemStack(Items.DIRT, 3);
        var leftover = bus.insertItem(0, offered.copy(), false);
        var inSlot = bus.getStackInSlot(0);
        if (!inSlot.isEmpty() || leftover.getCount() != offered.getCount()) {
            helper.fail("the pattern bus took " + (offered.getCount() - leftover.getCount())
                    + " dirt through the pipe face into slot 0, which now holds "
                    + net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(inSlot.getItem())
                    + " x" + inSlot.getCount() + ", expected eco's encoded-pattern filter to refuse it");
            return;
        }
        // Patterns, both ways, through the same face a pipe asks about - and nothing else. The insert is
        // asserted before the pull because the pull needs something in the slot, and because eco's own
        // filter (which overrides allowInsert only) is what decides both: a non-pattern cannot get in, and
        // extraction is unrestricted.
        ItemStack[] grid = emptyPatternGrid();
        grid[0] = new ItemStack(Items.OAK_LOG);
        var pattern = PatternDetailsHelper.encodeCraftingPattern(
                craftingRecipe(helper, 1, 1, new ItemStack(Items.OAK_LOG)), grid,
                new ItemStack(Items.OAK_PLANKS, 4), false, false);
        var refused = bus.insertItem(0, pattern.copy(), false);
        if (refused.getCount() != 0 || !bus.getStackInSlot(0).is(pattern.getItem())) {
            helper.fail("the bus would not take an encoded crafting pattern through the pipe face:"
                    + " leftover " + refused.getCount() + ", slot 0 " + itemName(bus.getStackInSlot(0))
                    + ", expected the pattern in slot 0");
            return;
        }
        var pulled = bus.extractItem(0, 16, false);
        if (!pulled.is(pattern.getItem()) || !bus.getStackInSlot(0).isEmpty()) {
            helper.fail("a pipe pulling from the bus got " + itemName(pulled) + " and left "
                    + itemName(bus.getStackInSlot(0)) + ", expected the pattern out and the slot empty");
            return;
        }
        helper.succeed();
    }

    /**
     * The wireless link card, at the layer this suite can actually reach: two devices that share no space,
     * joined by {@code GridHelper.createConnection}, then asked whether they are one network. The edge is
     * read back as {@code edge=virtual} because a connection that passes through zero cable blocks must not
     * look like a cable to AE2's own cleanup - and that cleanup is the reason this needs a test: an
     * in-world connection whose side stops being exposed is destroyed by
     * {@code appeng.me.InWorldGridNode#cleanupConnections}, which walks {@code getConnections()} and
     * skips only what answers {@code isInWorld() == false}.
     *
     * <p>What this does NOT cover, so nobody reads it as covered: the pairing surviving a real chunk
     * unload, and an eco interface being bindable. Both endpoints here are our own block entities, and the
     * rebuild pass is only provable in a client where chunks actually go to sleep.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkCardJoinsTwoDevicesWithoutACable(GameTestHelper helper) {
        // The card has to say out loud that only its own live= reading settles whether a link exists,
        // because the device's own panel is a lie about it (measured: a grid that still holds power keeps
        // the far device looking alive after its edge is gone). A missing key prints the key itself and
        // raises no error anywhere, so the two languages are checked here.
        for (String lang : new String[]{"en_us", "zh_cn"}) {
            String json = readResource("/assets/neoecoprototype/lang/" + lang + ".json", helper);
            if (json == null) {
                return;
            }
            if (!json.contains("wireless_link_card.check")) {
                helper.fail("lang/" + lang + ".json has no wireless_link_card.check key");
                return;
            }
        }
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                () -> "the two devices never became two separate network nodes: a=" + nodeSummary(level, a)
                        + " b=" + nodeSummary(level, b) + ", so a link between them would prove nothing",
                () -> {
                    String bound = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                    NeoECOPrototype.LOGGER.info("wireless link card test: bind said {}", bound);
                    if (!bound.startsWith("linked ") || !bound.contains("edge=virtual")) {
                        helper.fail("binding two separate devices reported \"" + bound
                                + "\", expected \"linked\" over an edge=virtual");
                        return;
                    }
                    if (!oneNetwork(level, a, b)) {
                        helper.fail("the link was reported but the two devices are still on different "
                                + "networks, so nothing merged: " + nodeSummary(level, a) + " / "
                                + nodeSummary(level, b));
                        return;
                    }
                    if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a)
                                    .contains("wirelessLinks=1 live=1")
                            || !WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, b)
                                    .contains("wirelessLinks=1 live=1")) {
                        helper.fail("one edge should count for both ends: a="
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a) + " b="
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, b));
                        return;
                    }
                    String twice = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                    if (!twice.startsWith("already linked ")) {
                        helper.fail("binding the same pair again reported \"" + twice
                                + "\", expected it to be refused - a second edge between the same two "
                                + "nodes is a second cable nobody asked for");
                        return;
                    }
                    String unlinked = WirelessLinks.unlink(level, ModRegistration.WIRELESS_LINKS, a);
                    if (!unlinked.startsWith("dropped 1 link(s)")) {
                        helper.fail("unbinding the near end reported \"" + unlinked
                                + "\", expected exactly one link dropped");
                        return;
                    }
                    waitUntil(helper, 20,
                            () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                            () -> "after dropping the only link the two devices stayed on one network ("
                                    + nodeSummary(level, a) + " / " + nodeSummary(level, b)
                                    + " edges between=" + connectionsBetween(level, a, b)
                                    + " physical=" + physicalEdgesBetween(level, a, b)
                                    + " nodes on this grid=" + gridNodeCount(level, a) + "/"
                                    + gridNodeCount(level, b)
                                    + "), so the edge outlived its record",
                            () -> {
                                if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, b)
                                        .contains("wirelessLinks=0 live=0")) {
                                    helper.fail("the far end still believes it is linked: "
                                            + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, b));
                                    return;
                                }
                                helper.succeed();
                            });
                });
    }

    /**
     * The channel budget is the only thing this feature charges a player for, so it has to be what refuses a
     * bind, and the refusal has to say the three numbers that make the decision reproducible (what this
     * needs, what is free, where to buy more).
     *
     * <p>The last half is a decision, not a measurement: taking the controller away collapses the grant to
     * zero but leaves the sold link standing. Dropping live links because a payment source vanished would
     * tear networks apart according to the order a player built things in, and nothing about removing a
     * block says "unwire that device". Put a controller back on that same spot and the grant comes back too,
     * because grants are stored as positions and resolved against a live node every time they are counted.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkCardChargesChannelsAndAControllerTapPaysThem(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b),
                () -> "the two devices never became two separate network nodes: " + nodeSummary(level, a)
                        + " / " + nodeSummary(level, b) + ", so there is nothing for the budget to charge",
                () -> {
                    String broke = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                    if (!broke.contains("channels=0/0") || !broke.contains("controller")) {
                        helper.fail("a bind with no controller tapped reported \"" + broke
                                + "\", expected a refusal naming channels=0/0 and the controller tap");
                        return;
                    }
                    if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a)
                            .contains("wirelessLinks=0 live=0")) {
                        helper.fail("the refused bind left a record behind: "
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a));
                        return;
                    }
                    grantBudgetBeside(helper, level, near);
                    waitUntil(helper, 20, () -> grantBudgetBeside(helper, level, near),
                            () -> "the controller beside the near device never became a network the card"
                                    + " could charge against: "
                                    + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a),
                            () -> {
                                // Read the ledger before and after rather than asserting absolute numbers:
                                // every gameTest in one run shares a single overworld, so this attachment
                                // also holds other tests' records and controller taps. What is ours to pin is
                                // the difference one link of ours makes - one channel per device.
                                String before = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a);
                                String paid = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                                if (!paid.startsWith("linked ")) {
                                    helper.fail("after buying channels the bind was still refused: \""
                                            + paid + "\" (ledger before: " + before + ")");
                                    return;
                                }
                                String after = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a);
                                int[] was = ledgerOf(before);
                                int[] now = ledgerOf(after);
                                if (was == null || now == null) {
                                    helper.fail("no channels= figure in the status lines, so the charge cannot"
                                            + " be read: " + before + " / " + after);
                                    return;
                                }
                                if (now[0] <= was[0] || now[0] > now[1]
                                        || now[1] < WirelessLinks.CHANNELS_PER_CONTROLLER) {
                                    helper.fail("a link was added without raising the spent total, or spent"
                                            + " more than the grant covers, or with no controller grant behind"
                                            + " it: " + before + " -> " + after);
                                    return;
                                }
                                var soldCell = takeControllerBeside(helper, near);
                                String unpaid = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a);
                                int[] gone = ledgerOf(unpaid);
                                // Asked of our own pair rather than of the endpoint's totals, because a batch
                                // shares one attachment and another test's record can also be riding here.
                                if (soldCell == null || gone == null || gone[1] >= now[1]
                                        || WirelessLinks.edgeOf(level,
                                        ModRegistration.WIRELESS_LINKS, WirelessLinks.linkOf(a, b)) == null) {
                                    helper.fail("removing the controller was expected to shrink the grant while"
                                            + " leaving the sold link alive (granted below " + now[1]
                                            + " and our own edge still held), got " + after + " -> " + unpaid);
                                }
                                // The grant follows the controller, not the square. Measured the hard way:
                                // with only "is there a node at that position" asked, a stale grant paid for a
                                // network that had no controller in it any more - so a player could take the
                                // controller away, stand any device there, and keep the allowance.
                                helper.setBlock(soldCell, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
                                String swapped = WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a);
                                int[] back = ledgerOf(swapped);
                                if (back == null || back[1] > gone[1]) {
                                    helper.fail("a device swapped onto the old controller's square restored the"
                                            + " allowance: granted went " + now[1] + " -> " + gone[1]
                                            + " -> " + (back == null ? "unreadable" : String.valueOf(back[1]))
                                            + " on " + swapped);
                                }
                                helper.succeed();
                            });
                });
    }

    /**
     * The one number in this feature that is ours to set rather than AE2's: how many wireless edges a
     * single device may carry. It has to be enforced where the record is written, because a link costs
     * nothing in blocks and a player can therefore make as many as they can click.
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkCardRefusesAFifthLinkAtOneDevice(GameTestHelper helper) {
        var hub = new BlockPos(0, 1, 0);
        helper.setBlock(hub, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        var spokes = new java.util.ArrayList<BlockPos>();
        for (int i = 0; i < WirelessLinks.MAX_LINKS_PER_ENDPOINT + 1; i++) {
            var pos = new BlockPos(2 + i, 1, 6);
            helper.setBlock(pos, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
            spokes.add(pos);
        }
        var level = (ServerLevel) helper.getLevel();
        var hubPos = GlobalPos.of(level.dimension(), helper.absolutePos(hub));
        waitUntil(helper, 25,
                () -> spokes.stream().allMatch(spoke -> separateNetworks(level, hubPos,
                        GlobalPos.of(level.dimension(), helper.absolutePos(spoke))))
                        && grantBudgetBeside(helper, level, hub),
                () -> "a device in the cap fixture never became a separate network node: hub="
                        + nodeSummary(level, hubPos) + " last spoke="
                        + nodeSummary(level, GlobalPos.of(level.dimension(),
                        helper.absolutePos(spokes.get(spokes.size() - 1))))
                        + " - a block entity answers as a device the moment it is placed, but AE2 only "
                        + "builds its node in onReady, which is what this poll waits for",
                () -> {
                    for (int i = 0; i < WirelessLinks.MAX_LINKS_PER_ENDPOINT; i++) {
                        var spoke = GlobalPos.of(level.dimension(),
                                helper.absolutePos(spokes.get(i)));
                        String bound = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, hubPos, spoke);
                        if (!bound.startsWith("linked ")) {
                            helper.fail("link " + (i + 1) + " of " + WirelessLinks.MAX_LINKS_PER_ENDPOINT
                                    + " was refused: \"" + bound + "\"");
                            return;
                        }
                    }
                    var extra = GlobalPos.of(level.dimension(),
                            helper.absolutePos(spokes.get(WirelessLinks.MAX_LINKS_PER_ENDPOINT)));
                    String refused = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, hubPos, extra);
                    if (!refused.contains("max " + WirelessLinks.MAX_LINKS_PER_ENDPOINT)) {
                        helper.fail("a link past the cap was accepted: \"" + refused + "\"");
                        return;
                    }
                    if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, extra)
                            .contains("wirelessLinks=0 live=0")) {
                        helper.fail("the refused pair still left a record behind: "
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, extra));
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The rebuild pass has to survive losing its own reference while AE2 keeps the edge - which is the
     * state a chunk unload leaves behind, and the one that costs the most to get wrong:
     * {@code appeng.me.GridConnection#create} answers {@code IllegalStateException} for a pair that is
     * already connected, and on the success path it merges grids with a full propagation of both networks
     * plus a repath. So a pass that asks instead of adopts is either spamming a refusal every ten ticks, or
     * recalculating the main network for a link it already has.
     *
     * <p>This is the control group for that: it forces exactly the "record without a reference" state
     * through {@link WirelessLinks#forgetEdge}, runs the pass, and demands one edge and one live link out
     * of it. Before the adopt path existed, the pass went through {@code createConnection} here, the pair
     * already had that connection, and the reference stayed null - which is what the second assertion
     * below would catch.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkRebuildAdoptsTheEdgeAe2AlreadyHas(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        var link = WirelessLinks.linkOf(a, b);
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                () -> "the two devices never became two separate network nodes: " + nodeSummary(level, a)
                        + " / " + nodeSummary(level, b) + ", so this measures nothing",
                () -> {
                    String bound = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                    if (!bound.startsWith("linked ")) {
                        helper.fail("binding the pair reported \"" + bound + "\", expected a link");
                        return;
                    }
                    WirelessLinks.forgetEdge(level, ModRegistration.WIRELESS_LINKS, link);
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS, link) != null) {
                        helper.fail("forgetEdge left a reference behind, so the pass has nothing to prove");
                        return;
                    }
                    if (!oneNetwork(level, a, b)) {
                        helper.fail("dropping only our reference split the network, so AE2 was holding the "
                                + "edge for us and the fixture is not the state being tested");
                        return;
                    }
                    int before = connectionsBetween(level, a, b);
                    WirelessLinks.rebuildNow(level, ModRegistration.WIRELESS_LINKS);
                    WirelessLinks.rebuildNow(level, ModRegistration.WIRELESS_LINKS);
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS, link) == null) {
                        helper.fail("two passes never got hold of the edge AE2 already had: the pair has "
                                + before + " connection(s) between them and our reference is still null");
                        return;
                    }
                    if (connectionsBetween(level, a, b) != before) {
                        helper.fail("the pass added connections instead of adopting the one there was: "
                                + before + " before, " + connectionsBetween(level, a, b) + " after");
                        return;
                    }
                    if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a)
                            .contains("wirelessLinks=1 live=1")) {
                        helper.fail("after rebuilding, the device itself still reports: "
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a));
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The chunk wake has to be a *targeted* wake: a world generates chunks constantly, and if every load
     * swept the whole table - or if the matching were wrong and matched nothing - the cheap fallback pass
     * would stop being cheap, or the link would not come back when it should.
     *
     * <p>The wake marks urgency and reads nothing (see {@code WirelessLinks.mayReadBlocks} for why it may not), so
     * a wake's scope is asserted on the mark itself - both directions, and the first one is the control: waking
     * a chunk that holds neither endpoint must leave the pair unmarked. Without the chunk test in
     * {@code rebuildAround} that assertion would go green by accident, which is why the second half wakes the
     * right chunk and then asks for the main-thread pass by name, to prove the mark is not a dead letter.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkWakeOnlyRebuildsTheLinkInThatChunk(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        var link = WirelessLinks.linkOf(a, b);
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                () -> "the two devices never became two separate network nodes: " + nodeSummary(level, a)
                        + " / " + nodeSummary(level, b) + ", so this measures nothing",
                () -> {
                    if (!WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b).startsWith("linked ")) {
                        helper.fail("the fixture could not bind the pair, so the wake path has nothing to wake");
                        return;
                    }
                    WirelessLinks.forgetEdge(level, ModRegistration.WIRELESS_LINKS, link);
                    var here = helper.absolutePos(near);
                    // A chunk 16 sections away from both endpoints - same dimension, loaded, irrelevant.
                    WirelessLinks.rebuildAround(level, ModRegistration.WIRELESS_LINKS,
                            (here.getX() >> 4) + 512, (here.getZ() >> 4) + 512);
                    if (WirelessLinks.isUrgent(level, ModRegistration.WIRELESS_LINKS, link)) {
                        helper.fail("waking an unrelated chunk marked the pair urgent, so the match is not by chunk");
                        return;
                    }
                    WirelessLinks.rebuildAround(level, ModRegistration.WIRELESS_LINKS,
                            here.getX() >> 4, here.getZ() >> 4);
                    if (!WirelessLinks.isUrgent(level, ModRegistration.WIRELESS_LINKS, link)) {
                        helper.fail("waking the chunk that holds an endpoint did not hand the pair to the burst");
                        return;
                    }
                    // On a live server this pass is the tick that follows; a test has to ask for it by name.
                    WirelessLinks.rebuildNow(level, ModRegistration.WIRELESS_LINKS);
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS, link) == null) {
                        helper.fail("the urgent mark never became an edge once a main-thread pass ran, so the wake "
                                + "defers the work and then loses it");
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * The chunk wake must not read blocks when it is not on the server thread, and that is a live bug, not
     * tidiness. {@code ChunkEvent.Load} is posted from a chunk worker inside {@code ChunkStatusTasks.full};
     * {@code ServerChunkCache.getChunk} opens with "not the main thread? queue the read on the main thread
     * and join"; and during world load the main thread sits in {@code BlockableEventLoop.waitForTasks}
     * waiting on that same worker pool - so the two wait on each other and start-region generation never
     * leaves 0%. A live client reproduced exactly that: two thread dumps 44 s apart with identical stacks,
     * our own {@code presence} frame parked on the join.
     *
     * <p>How this goes red instead of wedging: the main thread holds still inside {@code latch.await}, which
     * does not drain Minecraft's queue, so an unguarded read cannot be serviced and the 5 s await simply
     * expires - after that the worker finishes normally and nothing is left stuck. The rest is the fixed
     * path's own contract: declining is answered as "I cannot tell", so the record survives and no edge is
     * built off-thread, and {@code rebuildNow} on the main thread still takes it over.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkWakeFromAnotherThreadNeverReadsBlocks(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        var link = WirelessLinks.linkOf(a, b);
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                () -> "the two devices never became two separate network nodes: " + nodeSummary(level, a)
                        + " / " + nodeSummary(level, b) + ", so this measures nothing",
                () -> {
                    if (!WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b).startsWith("linked ")) {
                        helper.fail("the fixture could not bind the pair, so the wake path has nothing to wake");
                        return;
                    }
                    WirelessLinks.forgetEdge(level, ModRegistration.WIRELESS_LINKS, link);
                    var here = helper.absolutePos(near);
                    int wakeX = here.getX() >> 4;
                    int wakeZ = here.getZ() >> 4;
                    var answered = new java.util.concurrent.CountDownLatch(1);
                    var worker = new Thread(() -> {
                        WirelessLinks.rebuildAround(level, ModRegistration.WIRELESS_LINKS, wakeX, wakeZ);
                        answered.countDown();
                    }, "neoecoprototype-off-thread-wake");
                    worker.setDaemon(true);
                    worker.start();
                    boolean answeredInTime;
                    try {
                        answeredInTime = answered.await(5, java.util.concurrent.TimeUnit.SECONDS);
                    } catch (InterruptedException interrupted) {
                        Thread.currentThread().interrupt();
                        answeredInTime = false;
                    }
                    if (!answeredInTime) {
                        helper.fail("an off-thread wake could not answer within 5s of the main thread holding its "
                                + "queue still, so it is waiting on a block read only the main thread can run - "
                                + "that is the world-load deadlock (see WirelessLinks.mayReadBlocks)");
                        return;
                    }
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS, link) != null) {
                        helper.fail("the off-thread wake rebuilt the link, so it read block entities off the "
                                + "server thread instead of declining to look");
                        return;
                    }
                    if (WirelessLinks.linksAtEverywhere(level, ModRegistration.WIRELESS_LINKS, a) == 0) {
                        helper.fail("the off-thread wake dropped the record: a declined read answered \"this "
                                + "device is gone\" instead of \"I cannot tell\"");
                        return;
                    }
                    WirelessLinks.rebuildNow(level, ModRegistration.WIRELESS_LINKS);
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS, link) == null) {
                        helper.fail("the record survived the off-thread wake but the main-thread pass did not take "
                                + "it over either, so the guard is not deferring the work - it is losing it");
                        return;
                    }
                    helper.succeed();
                });
    }

    /**
     * Abandoning a filed pair must not leave the survivor wired to a node that has no block behind it.

     * <p>The size yardstick, not a "did they split" question, is what can actually be answered: once the near
     * block is gone there is no node left there to ask about, so the first draft of this test - which called
     * {@code separateNetworks} after the removal - could only ever fail. And the edge itself turns out to be
     * taken down by AE2 along with the node, even though {@code Data#drop} never calls
     * {@code IGridConnection#destroy}: an earlier reading of {@code GridNode#cleanupConnections} ("it skips
     * non-in-world connections, so a wireless edge outlives its block") was wrong for this path, and this test
     * now pins the upstream behaviour instead of my inference about it. If that ever moves, the assertion on
     * {@code virtualEdgesOn} goes red and {@code drop} starts owing a {@code destroy()}.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkDyingEndpointLeavesNoEdgeBehind(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(0, 1, 8);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SIMPLIFY_STORAGE_CASING_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        waitUntil(helper, 25,
                () -> separateNetworks(level, a, b) && grantBudgetBeside(helper, level, near),
                () -> "the two devices never became two separate network nodes: " + nodeSummary(level, a)
                        + " / " + nodeSummary(level, b) + ", so this measures nothing",
                () -> {
                    int alone = gridNodeCount(level, b);
                    if (alone <= 0) {
                        helper.fail("the far device had no grid of its own before the bind (" + alone
                                + " nodes), so there is no size to come back to");
                        return;
                    }
                    if (!WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b).startsWith("linked ")) {
                        helper.fail("the fixture could not bind the pair, so there is no edge to sever");
                        return;
                    }
                    if (virtualEdgesOn(level, b) == 0) {
                        helper.fail("the far node holds no virtual edge after a successful bind, so the reading"
                                + " below has nothing to assert about");
                        return;
                    }
                    if (gridNodeCount(level, b) <= alone) {
                        helper.fail("the bind did not merge anything: " + alone + " nodes before, "
                                + gridNodeCount(level, b) + " after");
                        return;
                    }
                    helper.setBlock(near, Blocks.AIR);
                    WirelessLinks.rebuildNow(level, ModRegistration.WIRELESS_LINKS);
                    if (WirelessLinks.linksAtEverywhere(level, ModRegistration.WIRELESS_LINKS, b) != 0) {
                        helper.fail("the record for a device that is no longer there was never dropped");
                        return;
                    }
                    int leftover = virtualEdgesOn(level, b);
                    if (leftover != 0) {
                        helper.fail("the survivor still holds " + leftover + " virtual edge(s) to a node with no"
                                + " block behind it, so the far device is online inside a broken network");
                        return;
                    }
                    int back = gridNodeCount(level, b);
                    if (back != alone) {
                        helper.fail("the survivor's grid counts " + back + " nodes after the far end was removed"
                                + " and our record dropped, but it counted " + alone + " before the bind");
                        return;
                    }
                    helper.succeed();
                });
    }

    /** How many nodes AE2 counts in the grid the device at this position currently belongs to. */
    private static int gridNodeCount(ServerLevel level, GlobalPos at) {
        var node = networkNode(level, at);
        if (node == null || node.getGrid() == null) {
            return -1;
        }
        int count = 0;
        for (var ignored : node.getGrid().getNodes()) {
            count++;
        }
        return count;
    }

    /** How many direction-less edges AE2 currently reports for the device at this position. */
    private static int virtualEdgesOn(ServerLevel level, GlobalPos at) {
        var node = networkNode(level, at);
        if (node == null) {
            return -1;
        }
        int virtual = 0;
        for (var connection : node.getConnections()) {
            if (!connection.isInWorld()) {
                virtual++;
            }
        }
        return virtual;
    }

    /**
     * The predicate every level read hangs off of, asserted directly: false for a caller that is not on the
     * server thread, true on it. The behavioural tests only catch this guard if the blocking read is re-armed as
     * well, so the guard gets its own face.
     *
     * <p>The control is the one-line neuter - make it answer {@code server != null} and the worker-thread half
     * goes red on its own sentence with nothing else in the batch moving.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 200,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkLevelReadIsRefusedOffTheServerThread(GameTestHelper helper) {
        var level = (ServerLevel) helper.getLevel();
        if (!WirelessLinks.mayReadBlocks(level)) {
            helper.fail("the main thread was refused a level read, so every rebuild would silently decline");
            return;
        }
        var answered = new java.util.concurrent.CountDownLatch(1);
        var workerSays = new boolean[1];
        var workerIsMain = new boolean[1];
        var worker = new Thread(() -> {
            workerIsMain[0] = Thread.currentThread() == level.getServer().getRunningThread();
            workerSays[0] = WirelessLinks.mayReadBlocks(level);
            answered.countDown();
        }, "neoecoprototype-off-thread-predicate");
        worker.setDaemon(true);
        worker.start();
        boolean finished;
        try {
            finished = answered.await(5, java.util.concurrent.TimeUnit.SECONDS);
        } catch (InterruptedException interrupted) {
            Thread.currentThread().interrupt();
            finished = false;
        }
        if (!finished) {
            helper.fail("the predicate itself blocked off the server thread, so it is not a pure in-memory answer");
            return;
        }
        if (workerIsMain[0]) {
            helper.fail("the test's own worker ran on the server thread, so the refusal below measures nothing");
            return;
        }
        if (workerSays[0]) {
            helper.fail("mayReadBlocks answered true for a thread that is not the server thread - that answer is "
                    + "what parks world load at 0%");
            return;
        }
        helper.succeed();
    }

    /** How many edges AE2 currently has between these two nodes - the number a rebuild must not raise. */
    private static int connectionsBetween(ServerLevel level, GlobalPos a, GlobalPos b) {
        var nodeA = networkNode(level, a);
        var nodeB = networkNode(level, b);
        if (nodeA == null || nodeB == null) {
            return -1;
        }
        int count = 0;
        for (var connection : nodeA.getConnections()) {
            if (connection.getOtherSide(nodeA) == nodeB) {
                count++;
            }
        }
        return count;
    }

    /**
     * Two devices that are already wired together with blocks must not get a wireless link, and the reason
     * is not tidiness: an adopted in-world connection would be destroyed the next time the player unbinds,
     * so the card would tear out a cable. Both AE2 addons we read make this same split -
     * ae2lt's {@code WirelessLinkOps#createVirtualConnection} answers
     * "A physical connection already exists between the target and transmitter." and only adopts a
     * connection that answers {@code isInWorld() == false}.
     *
     * <p>The control is the message itself: before the physical check existed, this pair was refused by the
     * later "already on one network" rule, so the words the player got were different - and the rule that
     * caught it there was a coincidence of adjacency, not a guard against adoption.</p>
     */
    @GameTest(template = "empty", batch = "wireless_link_card", timeoutTicks = 400,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void wirelessLinkCardRefusesTwoBlocksAlreadyWiredTogether(GameTestHelper helper) {
        var near = new BlockPos(0, 1, 0);
        var far = new BlockPos(1, 1, 0);
        helper.setBlock(near, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        helper.setBlock(far, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get());
        var level = (ServerLevel) helper.getLevel();
        var a = GlobalPos.of(level.dimension(), helper.absolutePos(near));
        var b = GlobalPos.of(level.dimension(), helper.absolutePos(far));
        waitUntil(helper, 25,
                () -> physicalEdgesBetween(level, a, b) >= 1,
                () -> "the two adjacent devices never formed an in-world connection ("
                        + physicalEdgesBetween(level, a, b) + "), so this fixture cannot test the refusal",
                () -> {
                    String refused = WirelessLinks.bind(level, ModRegistration.WIRELESS_LINKS, a, b);
                    if (!refused.startsWith("those two are already wired")) {
                        helper.fail("binding two blocks that are already wired reported \"" + refused
                                + "\", expected the physical-connection refusal");
                        return;
                    }
                    if (physicalEdgesBetween(level, a, b) < 1) {
                        helper.fail("the refusal destroyed the connection the player built with blocks");
                        return;
                    }
                    if (WirelessLinks.edgeOf(level, ModRegistration.WIRELESS_LINKS,
                            WirelessLinks.linkOf(a, b)) != null) {
                        helper.fail("the refused pair still ended up holding a connection reference");
                        return;
                    }
                    if (!WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a)
                            .contains("wirelessLinks=0 live=0")) {
                        helper.fail("a refused bind left a record behind: "
                                + WirelessLinks.status(level, ModRegistration.WIRELESS_LINKS, a));
                        return;
                    }
                    helper.succeed();
                });
    }

    /** How many block-built connections AE2 currently has between these two nodes. */
    private static int physicalEdgesBetween(ServerLevel level, GlobalPos a, GlobalPos b) {
        var nodeA = networkNode(level, a);
        var nodeB = networkNode(level, b);
        if (nodeA == null || nodeB == null) {
            return -1;
        }
        int count = 0;
        for (var connection : nodeA.getConnections()) {
            if (connection.getOtherSide(nodeA) == nodeB && connection.isInWorld()) {
                count++;
            }
        }
        return count;
    }

    /** Both endpoints awake, both holding a node, and on two different networks. */
    private static boolean separateNetworks(ServerLevel level, GlobalPos a,
                                            GlobalPos b) {
        var nodeA = networkNode(level, a);
        var nodeB = networkNode(level, b);
        return nodeA != null && nodeB != null && nodeA.getGrid() != nodeB.getGrid();
    }

    private static boolean oneNetwork(ServerLevel level, GlobalPos a,
                                      GlobalPos b) {
        var nodeA = networkNode(level, a);
        var nodeB = networkNode(level, b);
        return nodeA != null && nodeB != null && nodeA.getGrid() == nodeB.getGrid();
    }

    private static IGridNode networkNode(ServerLevel level, GlobalPos at) {
        return level.getBlockEntity(at.pos()) instanceof IGridConnectedBlockEntity connected
                ? connected.getGridNode() : null;
    }

    private static String nodeSummary(ServerLevel level, GlobalPos at) {
        var node = networkNode(level, at);
        return at.pos().toShortString() + "(node=" + (node == null ? "none" : "max"
                + node.getMaxChannels() + "/used" + node.getUsedChannels()) + ")";
    }
}

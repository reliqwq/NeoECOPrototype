package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import appeng.api.config.Actionable;
import appeng.api.crafting.IPatternDetails;
import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.networking.crafting.ICraftingProvider;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.storage.cells.ISaveProvider;
import appeng.api.upgrades.Upgrades;
import appeng.core.definitions.AEItems;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.integration.megacells.backend.ECOMegaLongBulkStorageCell;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityCraftingExecutor;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.function.BooleanSupplier;
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

    /** The small-bulk item cell mounts through eco's MEGA long-bulk backend (chain behavior). */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID, required = false)
    public static void smallBulkUsesMegaLongBulkBackend(GameTestHelper helper) {
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL == null) {
            helper.succeed(); // MegaCells absent: the small bulk family is not registered.
            return;
        }
        var stack = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
        var cell = ECOStorageCells.getCellInventory(stack, (ISaveProvider) null);
        helper.assertTrue(
                cell instanceof ECOMegaLongBulkStorageCell,
                "small bulk cell must mount through the MEGA long-bulk backend");
        helper.succeed();
    }

    /** Fixed infinite sources keep a null config so the cell workbench refuses them (AE2 NPE guard). */
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
                            counter.add(stack.what(), stack.amount());
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
        helper.succeed();
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
}

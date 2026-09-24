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
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.core.definitions.AEParts;
import appeng.helpers.InterfaceLogicHost;
import appeng.me.helpers.IGridConnectedBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.integration.megacells.backend.ECOMegaLongBulkStorageCell;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.gui.LocalGuiTitleContext;
import cn.dancingsnow.neoecoprototype.integration.ae2.OversizeInterfaceLogic;
import cn.dancingsnow.neoecoprototype.integration.ae2.SimplifyGridFacade;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyComputationClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyTrinityClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.trinity.TrinityCraftingExecutor;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
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
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.regex.Pattern;
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
    /**
     * Guards the 1.2.5 regression: loading eco's interface UI classes must never abort with a fatal
     * mixin injection error. The title redirects aim at compiler generated lambda names, so an added
     * lambda upstream used to turn "open eco's storage interface" into a crash for every player on a
     * different eco build. Loading with initialize = false still runs the transformer, which is the
     * part we care about, without touching client-only static state.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void ecoInterfaceUiClassesLoadWithoutFatalMixinError(GameTestHelper helper) {
        for (var name : new String[]{
                "cn.dancingsnow.neoecoae.gui.storage.StorageInterfaceUI",
                "cn.dancingsnow.neoecoae.gui.crafting.CraftingInterfaceUI",
                "cn.dancingsnow.neoecoae.gui.computation.ComputationInterfaceUI"}) {
            try {
                Class.forName(name, false, LocalGuiTitleContext.class.getClassLoader());
            } catch (Throwable failure) {
                helper.fail("loading " + name + " threw " + failure);
                return;
            }
        }
        helper.succeed();
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
                // SimplifyTier / SimplifyCraftingTier badge, drawn on every drive panel row
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/tier/l1.png"),
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
                        "models/item/cable_pattern_provider.json"))) {
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
        helper.succeed();
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
            helper.runAfterDelay(20, () -> {
                try {
                    onBuilt.accept(controller);
                } finally {
                    // Batches run one after another but the framework does not reliably restore one
                    // batch before starting the next, and two of these structures seven blocks apart
                    // make every controller "not unique". Clear our own blocks so the next test is
                    // measuring its own build.
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
     * The energized core is sold as "exactly one, in the shell to the host's left". The geometry
     * enforces it by allotting a single cell, so nothing has to be counted afterwards. These tests
     * each build the structure once: swapping members in and out of one standing structure walks
     * into AE2 refusing to re-initialise a grid node.
     */
    @GameTest(template = "l1_room", batch = "l1_core_left", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreInShellLeftOfHostAddsItsParallelism(GameTestHelper helper) {
        buildWithEnergizedCoreInShell(helper, true);
    }

    /** The mirrored cell has to refuse the same block, which is what keeps "exactly one" honest. */
    @GameTest(template = "l1_room", batch = "l1_core_right", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreOnTheShellsOtherSideIsRejected(GameTestHelper helper) {
        buildWithEnergizedCoreInShell(helper, false);
    }

    /**
     * The parallel column no longer takes the energized core. The cluster treats it as a parallel core
     * anyway, so the column is the second place where "only one" would have to be counted.
     */
    @GameTest(template = "l1_room", batch = "l1_core_column", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void energizedCoreInParallelColumnIsRejected(GameTestHelper helper) {
        buildComputationStructure(helper, new BlockPos(13, 3, 4),
                ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get(),
                ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get(), 0, false, 1, controller ->
                        helper.assertTrue(!controller.isFormed(),
                                "the parallel column still takes the energized core, so two of them can "
                                        + "stand in one subsystem"));
    }

    /**
     * Builds the minimum L1 structure, then replaces the shell casing on one side of the controller
     * with an energized core: the allotted cell when {@code left} is true, its mirror otherwise.
     */
    private static void buildWithEnergizedCoreInShell(GameTestHelper helper, boolean left) {
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

            // Both hands come from the production rule, so the negative case is genuinely "the other
            // shell cell of this same build" rather than a direction this file guessed at.
            BlockPos cell = SimplifyComputationClusterCalculator.energizedCoreCell(
                    absolute, helper.getLevel().getBlockState(absolute), !left);
            var stateHere = helper.getLevel().getBlockState(cell);
            // assertTrue does not stop execution: falling through after a recorded failure and then
            // calling succeed() leaves the batch waiting on a test that can no longer finish.
            if (!stateHere.is(ModRegistration.SIMPLIFY_COMPUTATION_CASING_BLOCK.get())) {
                helper.fail((left ? "left" : "right") + " of the controller is " + stateHere.getBlock()
                        + " at " + cell + ", not a casing, so this test cannot speak about it");
                return;
            }

            helper.getLevel().setBlockAndUpdate(cell,
                    ModRegistration.ENERGIZED_COMPUTATION_CORE_BLOCK.get().defaultBlockState());
            controller.rebuildMultiblock();
            helper.runAfterDelay(20, () -> {
                try {
                    if (left) {
                        var cluster = controller.getCluster();
                        if (!controller.isFormed() || cluster == null) {
                            helper.fail("the allotted shell cell did not accept the energized core");
                            return;
                        }
                        int ours = (int) cluster.getParallelCores().stream()
                                .filter(core -> core.getTier() == SimplifyTier.L1_PARALLEL_SWITCH).count();
                        if (ours != 1) {
                            helper.fail("the allotted shell cell rejected the energized core");
                            return;
                        }
                        int plain = cluster.getParallelCores().size() - ours;
                        long expected = (long) plain * SimplifyTier.L1.getCPUAccelerators()
                                + SimplifyTier.L1_PARALLEL_SWITCH.getCPUAccelerators();
                        if (cluster.getCPUAccelerators() != expected) {
                            helper.fail("the energized core should add "
                                    + SimplifyTier.L1_PARALLEL_SWITCH.getCPUAccelerators()
                                    + " co-processors on top of " + plain + " plain cores, but the "
                                    + "cluster reports " + cluster.getCPUAccelerators());
                            return;
                        }
                    } else if (controller.isFormed()) {
                        helper.fail("the shell cell on the other side took the energized core too, so "
                                + "nothing limits it to one");
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
     * The energized threading core gives sixteen real threads -- sixteen {@code ECOCraftingCPU} objects,
     * because eco sizes that array from the tier in the constructor.
     */
    @GameTest(template = "l1_room", batch = "l1_threading", templateNamespace = NeoECOPrototype.MOD_ID)
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
                });
    }

    /**
     * Only the cell nearest the controller takes the energized threading core, which is what caps a
     * structure at one of them: anywhere else the line stops matching and the subsystem will not form.
     */
    @GameTest(template = "l1_room", batch = "l1_threading_second", templateNamespace = NeoECOPrototype.MOD_ID)
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
}

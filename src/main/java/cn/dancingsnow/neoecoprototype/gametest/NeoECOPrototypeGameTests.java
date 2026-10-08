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
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import appeng.me.helpers.IGridConnectedBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import cn.dancingsnow.neoecoae.integration.megacells.backend.ECOMegaLongBulkStorageCell;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.decoration.FumoBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
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
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
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
     * The things that made the dolls look or read broken, each of which is invisible to the compiler:
     * the extra effect a doll has to hand out, the tooltip line that describes it (it was silently
     * missing, and "没回血" was really "没说"), the loot table that has to copy the owner component off
     * the custom-skin doll's block entity, and which doll counts as "named" now that the four honoured
     * players are each their own item. Reads everything through the mod classloader, so it costs no plot
     * and touches no world.
     */
    @GameTest(template = "empty", templateNamespace = NeoECOPrototype.MOD_ID)
    public static void namedDollDescribesAndKeepsItsOwner(GameTestHelper helper) {
        ItemStack stack = new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get());
        net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> extra =
                cn.dancingsnow.neoecoprototype.item.decoration.FumoItem.extraEffect(stack);
        if (extra != net.minecraft.world.effect.MobEffects.REGENERATION) {
            helper.fail("TedXenon's doll hands out " + extra + " instead of Regeneration");
            return;
        }
        // The released half: the four dedicated dolls count as named, and the doll /prototypefumo hands
        // out does not - even when its component says one of the honoured players' names, because the
        // worn effects are keyed off the item now, not off a string a command wrote.
        for (java.util.function.Supplier<? extends net.minecraft.world.item.Item> named : List.of(
                ModRegistration.FUMO_DOLL_RELIQWQ_ITEM, ModRegistration.FUMO_DOLL_YANG120_ITEM,
                ModRegistration.FUMO_DOLL_KOOOKI_ITEM, ModRegistration.FUMO_DOLL_TEDXENON_ITEM)) {
            if (!cn.dancingsnow.neoecoprototype.item.decoration.FumoItem
                    .isNamedDoll(new ItemStack(named.get()))) {
                helper.fail(named.get() + " is in the creative tab as a named doll but grants nothing");
                return;
            }
        }
        var customSkin = new ItemStack(ModRegistration.FUMO_RELIQWQ_ITEM.get());
        customSkin.set(ModRegistration.FUMO_OWNER.get(), new net.minecraft.world.item.component.ResolvableProfile(
                java.util.Optional.of("reliqwq"), java.util.Optional.empty(),
                new com.mojang.authlib.properties.PropertyMap()));
        if (cn.dancingsnow.neoecoprototype.item.decoration.FumoItem.isNamedDoll(customSkin)) {
            helper.fail("the custom-skin doll still reads a player's name off its component, so it hands"
                    + " out armour and night vision that belong to the four named dolls");
            return;
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
        String loot = readResource("/data/neoecoprototype/loot_table/blocks/fumo_reliqwq.json", helper);
        if (loot == null) {
            return;
        }
        if (!loot.contains("\"function\": \"minecraft:copy_components\"")
                || !loot.contains("\"source\": \"block_entity\"")
                || !loot.contains("neoecoprototype:fumo_owner")) {
            helper.fail("the doll's loot table does not copy neoecoprototype:fumo_owner from the"
                    + " block entity, so breaking one drops an anonymous doll");
            return;
        }
        // The real check, though: the loader wants the key to be "function", and a table that does not
        // parse is simply left out of the registry - the block then drops nothing while every test
        // still reports green. Asking the loaded keys is the only way to see that from here.
        var ourLoot = cn.dancingsnow.neoecoprototype.NeoECOPrototype.id("blocks/fumo_reliqwq");
        boolean loaded = helper.getLevel().getServer().reloadableRegistries()
                .getKeys(net.minecraft.core.registries.Registries.LOOT_TABLE).contains(ourLoot);
        if (!loaded) {
            helper.fail("loot table " + ourLoot + " is not among the loaded keys, so it failed to parse"
                    + " and the doll block drops nothing");
            return;
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
            wearingDoll = cn.dancingsnow.neoecoprototype.event.PlushieScare.wearsNamedDoll(creeper);
            creeper.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, ItemStack.EMPTY);
            wearingNothing = cn.dancingsnow.neoecoprototype.event.PlushieScare.wearsNamedDoll(creeper);
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
        ownerSurvivesEveryCopy(helper);
        // A profile can carry a uuid and no name; the lang key is "%s's Doll", so that used to read as
        // a dangling possessive.
        var nameless = new ItemStack(ModRegistration.FUMO_RELIQWQ_ITEM.get());
        nameless.set(ModRegistration.FUMO_OWNER.get(), new net.minecraft.world.item.component.ResolvableProfile(
                java.util.Optional.empty(), java.util.Optional.of(java.util.UUID.randomUUID()),
                new com.mojang.authlib.properties.PropertyMap()));
        var plainTitle = ModRegistration.FUMO_RELIQWQ_ITEM.get().getName(ItemStack.EMPTY);
        if (!nameless.getHoverName().equals(plainTitle)) {
            helper.fail("a doll with a uuid but no name is titled \"" + nameless.getHoverName().getString()
                    + "\" instead of the plain \"" + plainTitle.getString() + "\"");
            return;
        }
        helper.succeed();
    }

    /**
     * A doll's face rides four vanilla paths that never meet: the chunk save and the loader that reads it
     * back, the block entity sync tag, the component map a broken doll's loot copies, and pick-block plus
     * place-from-item. Each one is a separate call into {@link FumoBlockEntity}, so a broken round trip
     * just gives the doll a different owner and nothing anywhere reports it. Touches no world and no plot.
     *
     * <p>Compared field by field on purpose: {@code ResolvableProfile} is a record whose components
     * include a {@code PropertyMap} and a derived {@code GameProfile}, and neither has a value
     * {@code equals}, so two faithful copies of one owner are never {@code equals} to each other.
     */
    private static void ownerSurvivesEveryCopy(GameTestHelper helper) {
        var pos = new BlockPos(0, 0, 0);
        var state = ModRegistration.FUMO_BLOCK.get().defaultBlockState();
        var registries = helper.getLevel().registryAccess();
        var skins = new com.mojang.authlib.properties.PropertyMap();
        skins.put("textures", new com.mojang.authlib.properties.Property("textures",
                "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0In19fQ==",
                "a-signature-under-test"));
        var owner = new net.minecraft.world.item.component.ResolvableProfile(
                java.util.Optional.of("reliqwq"),
                java.util.Optional.of(java.util.UUID.fromString("f0e6d3ba-3b17-4a5c-9c1a-2c5b9f0a7d31")), skins);
        var placed = new FumoBlockEntity(pos, state);
        placed.setOwner(owner);

        var saved = placed.saveWithFullMetadata(registries);
        if (!(net.minecraft.world.level.block.entity.BlockEntity.loadStatic(pos, state, saved, registries)
                instanceof FumoBlockEntity reloaded)) {
            helper.fail("the chunk tag of an owned doll no longer reads back as a FumoBlockEntity");
            return;
        }
        if (!sameOwner(owner, reloaded.owner(), "chunk save", helper)) {
            return;
        }
        var synced = new FumoBlockEntity(pos, state);
        synced.loadCustomOnly(placed.getUpdateTag(registries), registries);
        if (!sameOwner(owner, synced.owner(), "client sync tag", helper)) {
            return;
        }
        var picked = new ItemStack(ModRegistration.FUMO_RELIQWQ_ITEM.get());
        placed.saveToItem(picked, registries);
        var replanted = new FumoBlockEntity(pos, state);
        replanted.applyComponentsFromItemStack(picked);
        if (!sameOwner(owner, replanted.owner(), "pick-block then place", helper)) {
            return;
        }
        if (!sameOwner(owner, placed.collectComponents().get(ModRegistration.FUMO_OWNER.get()),
                "loot component copy", helper)) {
            return;
        }
        // The other half: an unowned doll must write no key at all, and a tag without one has to clear
        // the field rather than leave whatever the block entity happened to hold before.
        var plainTag = new FumoBlockEntity(pos, state).saveWithoutMetadata(registries);
        if (plainTag.contains("fumo_owner")) {
            helper.fail("a doll with no owner still wrote a fumo_owner key: " + plainTag);
            return;
        }
        var hadOwner = new FumoBlockEntity(pos, state);
        hadOwner.setOwner(owner);
        hadOwner.loadCustomOnly(plainTag, registries);
        if (hadOwner.owner() != null) {
            helper.fail("reading a tag with no owner left the old one in place, so an anonymous doll"
                    + " could wear the face of whatever it was copied from");
        }
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
                // SimplifyTier / SimplifyCraftingTier badge, drawn on every drive panel row
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/gui/tier/l1.png"),
                // FumoRenderer.localSkin: the named dolls carry their owner's skin inside the jar. A
                // missing file is not an error at runtime - the doll silently falls back to whatever
                // default face its uuid hashes to, which is precisely the kind of regression nobody
                // would notice until a player points at it.
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/fumo/skins/reliqwq.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/fumo/skins/yang120.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/fumo/skins/kouooki.png"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID,
                        "textures/block/fumo/skins/tedxenon.png"),
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

    /** True when {@code pos} is inside the template and holds nothing. */
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
                {ModRegistration.FUMO_BLOCK.get(), "fumo_reliqwq"},
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
     * The four conditions of frigit mother rock have to move in the right direction: a rock grows crystals
     * into the space in front of it, and every condition wears down into the next one. Getting that
     * backwards is invisible in game and silent in a compile - and the first cut of this chain did have it
     * inverted, so both halves are asserted here.
     *
     * <p>Driven tick by tick with a seeded random because waiting for the world to hand out that many
     * random ticks would take longer than the test is worth.
     */
    @GameTest(template = "empty", batch = "frigit_chain", timeoutTicks = 120,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void frigitBuddingGrowsAndWearsDown(GameTestHelper helper) {
        var level = helper.getLevel();
        var stages = List.of(ModRegistration.SMALL_FRIGIT_BUD.get(),
                ModRegistration.MEDIUM_FRIGIT_BUD.get(), ModRegistration.LARGE_FRIGIT_BUD.get(),
                ModRegistration.FRIGIT_CLUSTER.get());
        var pos = new BlockPos(0, 2, 0);
        helper.setBlock(pos, ModRegistration.FLAWLESS_BUDDING_FRIGIT.get());
        // The structure is addressed relatively, but randomTick talks to the real level in world coordinates.
        var anchor = helper.absolutePos(pos);
        var flawless = ModRegistration.FLAWLESS_BUDDING_FRIGIT.get();
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
        helper.setBlock(wornPos, ModRegistration.FLAWED_BUDDING_FRIGIT.get());
        var worn = helper.absolutePos(wornPos);
        var flawed = ModRegistration.FLAWED_BUDDING_FRIGIT.get();
        var decay = net.minecraft.util.RandomSource.create(7L);
        for (var i = 0; i < 400 && level.getBlockState(worn).is(flawed); i++) {
            flawed.randomTick(level.getBlockState(worn), level, worn, decay);
        }
        helper.assertTrue(level.getBlockState(worn).is(ModRegistration.CHIPPED_BUDDING_FRIGIT.get()),
                "flawed mother rock wore down to " + level.getBlockState(worn).getBlock()
                        + ", expected chipped");
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
                try {
                    if (!host.isFormed()) {
                        helper.fail("the room built from the plan never formed, so the published shape "
                                + "says nothing about the machine");
                        return;
                    }
                    onFormed.accept(host);
                } finally {
                    // Two standing machines seven blocks apart make every controller "not unique", so
                    // clear our own blocks before the next batch starts.
                    for (BlockPos pos : built) {
                        helper.getLevel().setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                    }
                    helper.getLevel().setBlockAndUpdate(absolute, Blocks.AIR.defaultBlockState());
                }
                helper.succeed();
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
                new Object[][]{{"1m", ModRegistration.OPTIONAL_FE_CELL_1M.get(),
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
                != SimplifyStorageHostBlockEntity.ROW_MEGA_ITEM) {
            helper.fail("a mega item matrix must be counted in the mega item row, not the plain one");
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
     * The one-click marking, end to end: a built and formed L1 storage host, one drive holding a source cell
     * with more of a compressible item than the threshold, one drive holding a small-bulk cell.
     *
     * <p>Two passes are the point. The first must mark something; the second must mark nothing again and
     * report it as already marked - which is what proves the comparison is eco's chain rule rather than item
     * equality, and that the per-drive inventory reads hoisted out of the target loop still see every
     * source. The rule itself is asserted first, because a fixture that cannot gather a same-chain pair says
     * nothing either way.
     */
    @GameTest(template = "l1_room", batch = "small_bulk_auto_mark", timeoutTicks = 400, required = false,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void autoMarkFollowsEcosChainRule(GameTestHelper helper) {
        if (!net.neoforged.fml.ModList.get().isLoaded("megacells")) {
            helper.succeed();
            return;
        }
        var sample = SmallBulkTestProbe.findSamples();
        if (!sample.isComplete()) {
            helper.fail("megacells is installed but this fixture could not gather a same-chain pair, a"
                    + " cross-chain pair and two plain items, so it cannot say anything about the rule: "
                    + sample);
            return;
        }
        if (!SimplifyStorageHostBlockEntity.sameMarkerChain(sample.sameLeft(), sample.sameRight())) {
            helper.fail("two members of one compression chain no longer read as the same marker - either eco"
                    + " changed its rule or we stopped calling it");
            return;
        }
        if (SimplifyStorageHostBlockEntity.sameMarkerChain(sample.otherLeft(), sample.otherRight())) {
            helper.fail("two different chains read as the same marker, so a second chain could never be marked");
            return;
        }
        if (SimplifyStorageHostBlockEntity.sameMarkerChain(sample.plainLeft(), sample.plainRight())) {
            helper.fail("two non-compressible items read as the same marker - an empty chain must never match");
            return;
        }
        // The display patch: a small bulk cell must report its own ceiling everywhere, and the hover is the
        // one place we do not own - eco writes that line from a final backend class that hard-codes 25.
        var bulkStack = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
        var bulkInventory = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells
                .getCellInventory(bulkStack, null);
        if (bulkInventory == null
                || cn.dancingsnow.neoecoprototype.items.SmallBulkTypeCap.of(bulkStack, bulkInventory) != 3L) {
            helper.fail("the panel's ceiling helper does not report 3 for a base small bulk cell: "
                    + (bulkInventory == null ? "no cell inventory" : bulkInventory.getTotalItemTypes()));
            return;
        }
        var hover = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        bulkStack.getItem().appendHoverText(bulkStack, net.minecraft.world.item.Item.TooltipContext.EMPTY,
                hover, net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        // Compared as rendered text: AE2 builds this sentence as seven appended pieces, so two identical
        // lines are never the same object and equals() would report a mismatch that isn't one.
        var ours = appeng.core.localization.Tooltips.typesUsed(0L, 3L).getString();
        var ecos = appeng.core.localization.Tooltips.typesUsed(0L, 25L).getString();
        if (hover.stream().noneMatch(line -> line.getString().equals(ours))) {
            helper.fail("the small bulk cell's hover does not carry its own 3-type ceiling, so the swap"
                    + " stopped matching eco's line: " + hover
                    + " [looking for \"" + ours + "\", eco's line reads \"" + ecos + "\"]");
            return;
        }
        if (hover.stream().anyMatch(line -> line.getString().equals(ecos))) {
            helper.fail("eco's unreachable 25-type backend ceiling still shows in the hover");
            return;
        }
        // The fluid variant runs on eco's standard engine, so it may already report the item's own slot
        // count. Measured rather than assumed: both routes have to land on the same sentence.
        var fluidStack = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL.get());
        var fluidHover = new java.util.ArrayList<net.minecraft.network.chat.Component>();
        fluidStack.getItem().appendHoverText(fluidStack, net.minecraft.world.item.Item.TooltipContext.EMPTY,
                fluidHover, net.minecraft.world.item.TooltipFlag.Default.NORMAL);
        if (fluidHover.stream().noneMatch(line -> line.getString().equals(ours))
                || fluidHover.stream().anyMatch(line -> line.getString().equals(ecos))) {
            helper.fail("the fluid small bulk cell's hover does not read the same 3-type ceiling: " + fluidHover);
            return;
        }
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
                    if (drives.size() < 2) {
                        helper.fail("the built room gave us " + drives.size() + " drive(s); this fixture"
                                + " needs one for the source and one for the small-bulk cell");
                        return;
                    }
                    var sourceCell = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_4M.get());
                    var sourceInventory = cn.dancingsnow.neoecoae.api.storage.ECOStorageCells
                            .getCellInventory(sourceCell, null);
                    if (sourceInventory == null) {
                        helper.fail("no eco cell handler claims our own 4M item cell");
                        return;
                    }
                    long want = cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                            .MEGA_BULK_AUTO_MARK_THRESHOLD.get() + 1L;
                    long put = sourceInventory.insert(sample.sameLeft(), want, Actionable.MODULATE, null);
                    if (put < want) {
                        helper.fail("the source cell took only " + put + " of " + want
                                + ", and the marking threshold is " + want);
                        return;
                    }
                    if (!drives.get(0).insertCell(sourceCell)) {
                        helper.fail("the drive refused the source cell");
                        return;
                    }
                    var bulkCell = new ItemStack(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get());
                    if (!drives.get(1).insertCell(bulkCell)) {
                        helper.fail("the drive refused the small-bulk cell");
                        return;
                    }
                    var first = storageHost.runAutoMark();
                    if (first.added() < 1) {
                        helper.fail("the first pass marked nothing at all: " + first);
                        return;
                    }
                    var second = storageHost.runAutoMark();
                    if (second.added() != 0 || second.alreadyMarked() < 1) {
                        helper.fail("the second pass re-marked: first=" + first + " second=" + second);
                        return;
                    }
                    var mounted = drives.get(1).getCellStack();
                    if (!(mounted.getItem()
                            instanceof cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem bulk)) {
                        helper.fail("the small-bulk cell is not what the drive ended up holding: " + mounted);
                        return;
                    }
                    var config = bulk.getConfigInventory(mounted);
                    if (!sample.sameLeft().equals(config.getKey(0))) {
                        helper.fail("the mark in slot 0 is " + config.getKey(0) + ", not the chain item "
                                + sample.sameLeft() + " that was over the threshold");
                    }
                });
    }

    /**
     * A mark is only worth what it folds. Measured against four chains this environment knows: without a
     * compression card in the cell the borrowed MEGA backend accepts exactly the marked key and refuses the
     * rest of the chain - eco's own MEGA long bulk cell measures identically, row for row, so the refusal is
     * the backend's rule rather than something this addon introduced. One card flips it, and our L1 shell
     * has the slot and keeps the card (both measured here). How the folded amount is then counted is the
     * backend's business and not asserted: whole units land under the mark, a remainder can stay as itself.
     */
    @GameTest(template = "empty", batch = "small_bulk_chain_folding", timeoutTicks = 100, required = false,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void smallBulkFoldsTheChainItMarks(GameTestHelper helper) {
        if (!ModList.get().isLoaded("megacells")) {
            helper.succeed();
            return;
        }
        var pairs = SmallBulkTestProbe.findChainPairs(4);
        if (pairs.isEmpty()) {
            helper.fail("no compression chain pair found in this environment");
            return;
        }
        var bulk = ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get();
        for (var pair : pairs) {
            var bare = SmallBulkTestProbe.fold(bulk, pair.marked(), pair.offered(), 64L, false);
            var carded = SmallBulkTestProbe.fold(bulk, pair.marked(), pair.offered(), 64L, true);
            if (bare.transferred() != 0L) {
                helper.fail("the cell took " + pair.offered() + " with no compression card, so the mark alone"
                        + " is no longer the gate: transferred " + bare.transferred() + " while "
                        + pair.marked() + " was the only mark");
                return;
            }
            if (carded.transferred() <= 0L) {
                helper.fail("a compression card did not open the chain: marked " + pair.marked()
                        + ", offered " + pair.offered() + " -> transferred " + carded.transferred());
                return;
            }
        }
        helper.succeed();
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
                        .SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX == 262_144,
                "the cell's biggest single batch is part of its contract");
        helper.succeed();
    }

    /**
     * The same cell read through eco's own cell API, which is the only way a network sees it: nothing goes
     * in, only AE2's singularity comes out, and the bank it reports is the bank it will pay.
     */
    @GameTest(template = "empty", batch = "singularity_cell_yield", timeoutTicks = 100,
            templateNamespace = NeoECOPrototype.MOD_ID)
    public static void singularityCellTakesNothingAndPaysWhatItShows(GameTestHelper helper) {
        var singularity = cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.SINGULARITY;
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
}

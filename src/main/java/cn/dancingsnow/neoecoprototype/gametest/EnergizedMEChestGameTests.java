package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.GridFlags;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.storage.IStorageMounts;
import appeng.api.storage.MEStorage;
import appeng.api.util.AECableType;
import appeng.core.definitions.AEBlocks;
import appeng.core.definitions.AEItems;
import appeng.menu.me.items.BasicCellChestMenu;
import cn.dancingsnow.neoecoae.all.NEItems;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoae.blocks.entity.storage.StorageCellWorkbench;
import cn.dancingsnow.neoecoae.menu.StorageCellWorkbenchLocator;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity.DisplayState;
import cn.dancingsnow.neoecoprototype.menu.EnergizedMEChestMenu;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.authlib.GameProfile;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.GameType;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

@GameTestHolder(NeoECOPrototype.MOD_ID)
@PrefixGameTestTemplate(false)
public final class EnergizedMEChestGameTests {
    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 100)
    public static void cachePowerAndBothCellFactoriesWork(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);
        helper.runAfterDelay(20, () -> verifyCachePowerAndBothCellFactories(helper, chest, pos));
    }

    private static void verifyCachePowerAndBothCellFactories(GameTestHelper helper,
            EnergizedMEChestBlockEntity chest, BlockPos pos) {
        double capacity = 5000.0 * PowerMultiplier.CONFIG.multiply(1.0);
        helper.assertTrue(Math.abs(chest.getAEMaxPower() - capacity) < 0.001,
                "cache capacity is " + chest.getAEMaxPower() + ", expected " + capacity);
        helper.assertTrue(chest.isAEPublicPowerStorage(), "cache is not public to FE cables");
        helper.assertTrue(chest.getPowerFlow() == AccessRestriction.WRITE,
                "power flow is " + chest.getPowerFlow() + ", expected WRITE");
        helper.assertTrue(chest.getCableConnectionType(Direction.NORTH) == AECableType.SMART,
                "chest does not expose an eight-channel smart connection");
        helper.assertTrue(chest.getMainNode().getNode().hasFlag(GridFlags.REQUIRE_CHANNEL),
                "chest does not require one ME channel");
        helper.assertTrue(chest.getCellSlotInventory().size() == 1, "chest must have exactly one cell slot");
        helper.assertTrue(chest.getCellSlotInventory().getSlotLimit(0) == 1, "cell slot accepts a stack");
        helper.assertTrue(!EnergizedMEChestBlockEntity.isCellValid(new ItemStack(Items.STONE)),
                "chest accepted stone as a storage cell");

        double overflow = chest.injectAEPower(capacity + 10, Actionable.MODULATE);
        helper.assertTrue(Math.abs(overflow - 10) < 0.001,
                "cache overflow is " + overflow + ", expected 10");
        var grid = chest.getMainNode().getGrid();
        helper.assertTrue(grid != null, "chest did not form a grid for the power-flow check");
        if (grid == null) {
            throw new IllegalStateException("missing chest grid");
        }
        helper.assertTrue(grid.getEnergyService().extractAEPower(1, Actionable.MODULATE, PowerMultiplier.ONE) == 0,
                "network extracted AE from the WRITE-only chest");
        helper.assertTrue(chest.getEnergyStorage(Direction.EAST).extractEnergy(100, false) == 0,
                "WRITE-only cache leaked FE");
        double beforeIdle = chest.getAECurrentPower();
        chest.serverTick();
        helper.assertTrue(Math.abs(beforeIdle - chest.getAECurrentPower() - 1.0) < 0.001,
                "empty chest did not drain exactly 1 AE/t from its cache");

        ItemStack aeCell = new ItemStack(AEItems.ITEM_CELL_1K.get());
        helper.assertTrue(EnergizedMEChestBlockEntity.isCellValid(aeCell), "AE2 1k cell was rejected");
        ItemStack remainder = chest.getCellSlotInventory().addItems(aeCell);
        helper.assertTrue(remainder.isEmpty(), "AE2 cell insertion left " + remainder);
        ItemStack installedAeCell = chest.getCell();
        helper.assertTrue(ItemStack.isSameItemSameComponents(installedAeCell, aeCell),
                "single-cell insertion changed the AE2 cell");
        var aeInventory = chest.getCurrentCell();
        helper.assertTrue(aeInventory != null, "AE2 cell did not resolve through StorageCells");
        helper.assertTrue(chest.getCellInventory() == null,
                "AE2 cell was incorrectly exposed as an eco cell inventory");
        helper.assertTrue(Math.abs(chest.getMainNode().getNode().getIdlePowerUsage() - 1.0 - aeInventory.getIdleDrain()) < 0.001,
                "AE2 cell idle drain was not added to the chest's 1 AE/t");

        AEItemKey diamond = AEItemKey.of(Items.DIAMOND);
        helper.assertTrue(aeInventory.insert(diamond, 64, Actionable.MODULATE, appeng.api.networking.security.IActionSource.empty()) == 64,
                "could not stock the AE2 cell");
        appeng.api.storage.IStorageProvider.requestUpdate(chest.getMainNode());
        chest.getMainNode().getGrid().getStorageService().invalidateCache();
        helper.assertTrue(chest.getInventory().getAvailableStacks().get(diamond) == 64,
                "terminal storage does not expose the installed AE2 cell");
        helper.assertTrue(chest.getDisplayState() == DisplayState.PARTIAL,
                "part-filled powered chest reports " + chest.getDisplayState());

        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        helper.assertTrue(chest.getUpgrades().size() == 0,
                "energized ME chest unexpectedly exposes upgrade slots");
        var terminalMenu = new BasicCellChestMenu(BasicCellChestMenu.TYPE, 2, player.getInventory(), chest);
        helper.assertTrue(terminalMenu.getHost() == chest,
                "top terminal menu is not bound to the energized ME chest");
        var menu = new EnergizedMEChestMenu(1, player.getInventory(), chest);
        var cellSlots = menu.getSlots(appeng.menu.SlotSemantics.STORAGE_CELL);
        helper.assertTrue(cellSlots.size() == 1, "side menu does not have exactly one storage-cell slot");
        helper.assertTrue(cellSlots.get(0).getItem() == installedAeCell,
                "side menu is not bound to the chest cell slot");
        ItemStack ecoCell = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
        helper.assertTrue(cellSlots.get(0).mayPlace(ecoCell), "side menu rejected an eco matrix");
        helper.assertTrue(!cellSlots.get(0).mayPlace(new ItemStack(Items.STONE)), "side menu accepted stone");

        ItemStack removed = chest.getCellSlotInventory().extractItem(0, 1, false);
        helper.assertTrue(removed == installedAeCell, "extracting the AE2 cell lost its identity");
        remainder = chest.getCellSlotInventory().addItems(ecoCell);
        helper.assertTrue(remainder.isEmpty(), "eco matrix insertion left " + remainder);
        ItemStack installedEcoCell = chest.getCell();
        helper.assertTrue(ItemStack.isSameItemSameComponents(installedEcoCell, ecoCell),
                "single eco matrix insertion changed the matrix");
        var ecoInventory = chest.getCurrentCell();
        helper.assertTrue(ecoInventory instanceof IECOStorageCell,
                "eco matrix did not resolve through ECOStorageCells: " + ecoInventory);
        helper.assertTrue(chest.getCellInventory() == ecoInventory,
                "eco workbench drive did not expose the installed eco matrix");
        helper.assertTrue(ecoInventory != aeInventory, "replacing the cell reused the old inventory");
        helper.assertTrue(Math.abs(chest.getMainNode().getNode().getIdlePowerUsage() - 1.0 - ecoInventory.getIdleDrain()) < 0.001,
                "eco matrix idle drain was not added to the chest's 1 AE/t");

        List<ItemStack> drops = new ArrayList<>();
        chest.addAdditionalDrops(helper.getLevel(), helper.absolutePos(pos), drops);
        helper.assertTrue(drops.size() == 1 && drops.get(0) == installedEcoCell,
                "breaking the chest did not drop the exact installed eco matrix");
        helper.assertTrue(chest.getCell().isEmpty(), "dropped cell stayed installed");
        chest.addAdditionalDrops(helper.getLevel(), helper.absolutePos(pos), drops);
        helper.assertTrue(drops.size() == 1, "cell dropped twice");
        helper.assertTrue(Math.abs(chest.getMainNode().getNode().getIdlePowerUsage() - 1.0) < 0.001,
                "removing the cell did not restore the 1 AE/t idle drain");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 200)
    public static void networkChannelPriorityAndUnreadyMountWork(GameTestHelper helper) {
        BlockPos pos = new BlockPos(2, 1, 2);
        BlockPos energyPos = new BlockPos(1, 1, 2);
        BlockPos neighborPos = new BlockPos(3, 1, 2);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        helper.setBlock(energyPos, AEBlocks.CREATIVE_ENERGY_CELL.block());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);

        helper.runAfterDelay(20, () -> {
            var managedNode = chest.getMainNode();
            var node = managedNode.getNode();
            var grid = managedNode.getGrid();
            helper.assertTrue(grid != null, "chest did not join an ME grid");
            helper.assertTrue(node.isOnline(), "chest grid node is not online");
            helper.assertTrue(node.isPowered(), "creative energy cell did not power the chest");
            helper.assertTrue(node.getUsedChannels() == 1,
                    "chest uses " + node.getUsedChannels() + " channels, expected 1");
            if (grid == null) {
                helper.fail("no grid to inspect");
                return;
            }

            ItemStack cellStack = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
            helper.assertTrue(chest.getCellSlotInventory().addItems(cellStack).isEmpty(), "matrix insertion failed");
            chest.setPriority(17);
            var cell = chest.getCurrentCell();
            helper.assertTrue(cell != null, "installed matrix did not resolve");
            AEItemKey diamond = AEItemKey.of(Items.DIAMOND);
            helper.assertTrue(cell.insert(diamond, 32, Actionable.MODULATE,
                    appeng.api.networking.security.IActionSource.empty()) == 32, "could not stock matrix");
            appeng.api.storage.IStorageProvider.requestUpdate(managedNode);
            grid.getStorageService().invalidateCache();
            helper.setBlock(neighborPos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());

            helper.runAfterDelay(20, () -> {
                EnergizedMEChestBlockEntity neighbor = chest(helper, neighborPos);
                helper.assertTrue(neighbor.getCellSlotInventory().addItems(new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get())).isEmpty(),
                        "neighbor matrix insertion failed");
                var neighborCell = neighbor.getCurrentCell();
                helper.assertTrue(neighborCell != null, "neighbor matrix did not resolve");
                AEItemKey emerald = AEItemKey.of(Items.EMERALD);
                helper.assertTrue(neighborCell.insert(emerald, 16, Actionable.MODULATE,
                        appeng.api.networking.security.IActionSource.empty()) == 16, "could not stock neighbor matrix");
                appeng.api.storage.IStorageProvider.requestUpdate(neighbor.getMainNode());
                grid.getStorageService().invalidateCache();

                RecordingMounts mounts = new RecordingMounts();
                chest.mountInventories(mounts);
                helper.assertTrue(mounts.storage == cell,
                        "online chest did not mount its installed matrix");
                helper.assertTrue(mounts.priority == 17,
                        "mounted priority is " + mounts.priority + ", expected 17");
                var cached = grid.getStorageService().getCachedInventory().get(diamond);
                helper.assertTrue(cached == 32,
                        "network cache sees " + cached + " diamonds, expected 32");
                var terminalInventory = chest.getInventory().getAvailableStacks();
                helper.assertTrue(terminalInventory.get(diamond) == 32,
                        "online terminal lost the chest's own matrix");
                helper.assertTrue(terminalInventory.get(AEItemKey.of(Items.EMERALD)) == 16,
                        "online terminal did not expose the neighbor's network storage");

                chest.injectAEPower(100.0, Actionable.MODULATE);
                managedNode.destroy();
                helper.assertTrue(chest.getAECurrentPower() >= 100.0,
                        "offline setup lost the chest's cached power");
                helper.assertTrue(chest.getDisplayState() == DisplayState.PARTIAL,
                        "cache-powered offline chest did not stay visually online");
                helper.assertTrue(chest.getCellStack() != null,
                        "offline display update lost the installed matrix used by the fixed renderer");
                for (int tick = 0; tick <= 100; tick++) {
                    chest.serverTick();
                }
                helper.assertTrue(chest.getAECurrentPower() < 1.0,
                        "offline chest did not drain its internal cache");
                helper.assertTrue(chest.getDisplayState() == DisplayState.OFF,
                        "offline chest stayed visually powered after its cache ran out");
                helper.assertTrue(chest.getBlockState().getValue(
                                cn.dancingsnow.neoecoprototype.block.storage.EnergizedMEChestBlock.CELL_STATE)
                                == DisplayState.OFF,
                        "offline block state did not follow cache depletion");
                chest.injectAEPower(100.0, Actionable.MODULATE);
                chest.serverTick();
                helper.assertTrue(chest.getDisplayState() == DisplayState.PARTIAL,
                        "offline chest did not recover its matrix display after cache recharge");
                RecordingMounts unready = new RecordingMounts();
                chest.mountInventories(unready);
                helper.assertTrue(unready.storage == null,
                        "unready chest still submitted its matrix to a network");
                var localInventory = chest.getInventory().getAvailableStacks();
                helper.assertTrue(localInventory.get(diamond) == 32,
                        "offline terminal lost the chest's local matrix");
                helper.assertTrue(localInventory.get(AEItemKey.of(Items.EMERALD)) == 0,
                        "offline terminal still exposed the neighbor's network storage");
                ItemStack removed = chest.getCellSlotInventory().extractItem(0, 1, false);
                helper.assertTrue(removed.is(cellStack.getItem()),
                        "offline matrix removal failed");
                helper.assertTrue(chest.getDisplayState() == DisplayState.ABSENT,
                        "offline chest kept a matrix display state after removal");
                helper.assertTrue(chest.getBlockState().getValue(
                                cn.dancingsnow.neoecoprototype.block.storage.EnergizedMEChestBlock.CELL_STATE)
                                == DisplayState.ABSENT,
                        "offline block state did not follow matrix removal");
                EnergizedMEChestBlockEntity receiver = new EnergizedMEChestBlockEntity(
                        ModRegistration.ENERGIZED_ME_CHEST_BE.get(), helper.absolutePos(pos), chest.getBlockState());
                receiver.setLevel(helper.getLevel());
                receiver.loadAdditional(chest.getUpdateTag(helper.getLevel().registryAccess()),
                        helper.getLevel().registryAccess());
                helper.assertTrue(receiver.getCell().isEmpty(),
                        "offline removal left a stale matrix in the client update");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 100)
    public static void cellWorkbenchConfiguresBothFactoriesAndRejectsStaleInstallations(GameTestHelper helper) {
        BlockPos pos = new BlockPos(4, 1, 1);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);
        helper.runAfterDelay(20, () -> verifyCellWorkbench(helper, chest, pos));
    }

    private static void verifyCellWorkbench(GameTestHelper helper,
            EnergizedMEChestBlockEntity chest, BlockPos pos) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        ItemStack aeStack = new ItemStack(AEItems.ITEM_CELL_1K.get());
        helper.assertTrue(chest.getCellSlotInventory().addItems(aeStack).isEmpty(), "AE2 cell insertion failed");
        ItemStack installedAe = chest.getCell();
        var aeInstallation = chest.getCellInteractionId();
        var at = chest.getBlockPos();
        player.setPos(at.getX() + 0.5D, at.getY() + 0.5D, at.getZ() + 0.5D);
        helper.assertTrue(player.level().getBlockEntity(at) == chest,
                "the world does not resolve the chest at its host position");
        helper.assertTrue(chest.getCellWorkbenchBlockEntity() == chest && !chest.isRemoved(),
                "the chest is not a live workbench host");
        helper.assertTrue(chest.isFormed() && chest.canPlayerInteract(player),
                "the chest rejected its workbench viewer");
        helper.assertTrue(chest.getCellStack() != null && chest.getCellStack().getCount() == 1,
                "the chest did not expose exactly one installed cell to the workbench");
        helper.assertTrue(chest.getStorageDrivesForIntegration().size() == 1,
                "the chest did not expose exactly one workbench drive");
        var drive = chest.getStorageDrivesForIntegration().get(0);
        helper.assertTrue(drive == chest && !drive.isRemoved() && drive.canExtractCell(),
                "the workbench drive is not a live extractable chest");
        helper.assertTrue(drive.getBlockPos().equals(at),
                "the workbench drive points at a different block position");
        helper.assertTrue(aeInstallation.equals(drive.getCellInteractionId()),
                "the workbench drive changed its installation id");
        helper.assertTrue(drive.getCellStack() == installedAe,
                "the workbench drive changed the installed cell reference");
        helper.assertTrue(cn.dancingsnow.neoecoae.grid.storage.cell.ECOStorageCellSettings.canConfigure(installedAe),
                "eco rejected the installed AE2 cell as configurable");
        var locator = new StorageCellWorkbenchLocator(at, at, aeInstallation);
        StorageCellWorkbench aeWorkbench = locator.locate(player, StorageCellWorkbench.class);
        helper.assertTrue(aeWorkbench != null && aeWorkbench.isValidFor(player),
                "workbench locator rejected the installed AE2 cell");
        if (aeWorkbench == null) {
            throw new IllegalStateException("missing AE2 cell workbench");
        }
        helper.assertTrue(chest.getStorageDrivesForIntegration().size() == 1
                        && chest.getStorageDrivesForIntegration().get(0) == chest,
                "chest does not expose itself as the single workbench drive");

        AEItemKey diamond = AEItemKey.of(Items.DIAMOND);
        AEItemKey emerald = AEItemKey.of(Items.EMERALD);
        aeWorkbench.getConfig().setStack(0, new GenericStack(diamond, 1));
        helper.assertTrue(diamond.equals(aeWorkbench.getConfig().getKey(0)),
                "could not partition the virtual AE2 workbench");
        var beforeReload = chest.getCurrentCell();
        aeWorkbench.flushConfiguration(player);
        var afterReload = chest.getCurrentCell();
        helper.assertTrue(afterReload != null && afterReload != beforeReload,
                "workbench configuration did not rebuild the mounted AE2 cell");
        helper.assertTrue(afterReload.insert(emerald, 1, Actionable.MODULATE,
                appeng.api.networking.security.IActionSource.empty()) == 0,
                "rebuilt AE2 cell ignored the workbench partition");

        chest.setCellStack(null);
        helper.assertTrue(chest.getCell().isEmpty(), "workbench extraction left the AE2 cell installed");
        helper.assertTrue(chest.getCellStack() == null,
                "empty workbench slot did not report null, so extraction would stop early");
        helper.assertTrue(!chest.getCellInteractionId().equals(aeInstallation),
                "extraction did not invalidate the old installation id");
        helper.assertTrue(!aeWorkbench.isValidFor(player), "stale AE2 workbench remained valid");

        ItemStack ecoStack = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
        chest.setCellStack(ecoStack);
        helper.assertTrue(chest.getCell() == ecoStack, "workbench drive changed the eco matrix reference");
        var ecoInstallation = chest.getCellInteractionId();
        var ecoWorkbench = new StorageCellWorkbench(chest, chest.getBlockPos(), ecoInstallation, player);
        helper.assertTrue(ecoWorkbench.isValidFor(player), "virtual workbench rejected the eco matrix");
        var ecoBeforeReload = chest.getCurrentCell();
        ecoWorkbench.getConfig().setStack(0, new GenericStack(diamond, 1));
        helper.assertTrue(diamond.equals(ecoWorkbench.getConfig().getKey(0)),
                "could not partition the eco matrix workbench");
        ecoWorkbench.flushConfiguration(player);
        helper.assertTrue(chest.getCurrentCell() != ecoBeforeReload,
                "workbench configuration did not rebuild the mounted eco matrix");
        helper.assertTrue(chest.getCellInteractionId() == ecoInstallation,
                "configuration reload changed the installation id");
        chest.setConfiguredCellPriority(23);
        helper.assertTrue(chest.getPriority() == 23 && chest.getConfiguredCellPriority() == 23,
                "single-cell priority did not stay aligned with whole-chest priority");
        chest.setCellStack(new ItemStack(Items.STONE));
        helper.assertTrue(chest.getCell() == ecoStack, "invalid replacement removed the installed matrix");
        chest.setCellStack(null);
        helper.assertTrue(chest.getCell().isEmpty(), "workbench extraction left the eco matrix installed");
        helper.assertTrue(!ecoWorkbench.isValidFor(player), "extracted eco workbench remained valid");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 100)
    public static void clientUpdateCarriesInstalledCellItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(8, 1, 1);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);
        ItemStack cell = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
        helper.assertTrue(chest.getCellSlotInventory().addItems(cell).isEmpty(), "matrix insertion failed");

        EnergizedMEChestBlockEntity receiver = new EnergizedMEChestBlockEntity(
                ModRegistration.ENERGIZED_ME_CHEST_BE.get(), helper.absolutePos(pos), chest.getBlockState());
        receiver.setLevel(helper.getLevel());
        receiver.loadAdditional(chest.getUpdateTag(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(ItemStack.isSameItemSameComponents(receiver.getCell(), cell),
                "client update lost the installed matrix item");

        chest.getCellSlotInventory().setItemDirect(0, ItemStack.EMPTY);
        receiver.loadAdditional(chest.getUpdateTag(helper.getLevel().registryAccess()),
                helper.getLevel().registryAccess());
        helper.assertTrue(receiver.getCell().isEmpty(), "client update kept a removed matrix item");
        helper.succeed();
    }

    @GameTest(template = "empty", batch = "energized_me_chest_quantum", timeoutTicks = 100, required = false)
    public static void onlineQuantumMatrixRemainsStableAcrossDisplayTicks(GameTestHelper helper) {
        if (!ModList.get().isLoaded("ae2omnicells")) {
            helper.succeed();
            return;
        }
        if (ModRegistration.OPTIONAL_QUANTUM_CELL_1M == null) {
            helper.fail("OmniCells is loaded, but the quantum 1M matrix was not registered");
            return;
        }
        BlockPos pos = new BlockPos(10, 1, 1);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        helper.setBlock(new BlockPos(9, 1, 1), AEBlocks.CREATIVE_ENERGY_CELL.block());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);
        helper.runAfterDelay(20, () -> {
            helper.assertTrue(chest.getMainNode().getNode().isOnline(),
                    "quantum-matrix chest did not join an ME grid");
            helper.assertTrue(chest.getMainNode().getNode().isPowered(),
                    "quantum-matrix chest was not powered");
            ItemStack quantum = new ItemStack(ModRegistration.OPTIONAL_QUANTUM_CELL_1M.get());
            helper.assertTrue(chest.acceptsCell(quantum), "energized chest rejected the quantum 1M matrix");
            helper.assertTrue(chest.getCellSlotInventory().addItems(quantum).isEmpty(),
                    "quantum 1M matrix insertion failed");
            ItemStack installed = chest.getCell();
            helper.assertTrue(installed.is(quantum.getItem()) && installed.getCount() == 1,
                    "quantum 1M insertion changed the matrix item or count");
            var cell = chest.getCurrentCell();
            helper.assertTrue(cell != null,
                    "quantum 1M did not resolve through eco's OmniCells handler: " + cell);
            helper.assertTrue(chest.getDisplayState() == DisplayState.EMPTY,
                    "empty powered quantum matrix reports " + chest.getDisplayState());
            AEItemKey diamond = AEItemKey.of(Items.DIAMOND);
            helper.assertTrue(cell.insert(diamond, 1, Actionable.MODULATE,
                            appeng.api.networking.security.IActionSource.empty()) == 1,
                    "could not stock the installed quantum matrix");
            appeng.api.storage.IStorageProvider.requestUpdate(chest.getMainNode());
            helper.assertTrue(chest.getDisplayState() == DisplayState.PARTIAL,
                    "part-filled powered quantum matrix reports " + chest.getDisplayState());
            helper.runAfterDelay(20, () -> {
                helper.assertTrue(chest.getCell() == installed,
                        "display ticks replaced the installed quantum matrix reference");
                helper.assertTrue(chest.getCellStack() == installed,
                        "display ticks lost the quantum matrix used by the fixed renderer");
                helper.assertTrue(chest.getCurrentCell() == cell,
                        "display ticks rebuilt the quantum matrix inventory");
                helper.assertTrue(chest.getDisplayState() == DisplayState.PARTIAL,
                        "quantum matrix display regressed across ticks: " + chest.getDisplayState());
                helper.assertTrue(chest.getBlockState().getValue(
                                cn.dancingsnow.neoecoprototype.block.storage.EnergizedMEChestBlock.CELL_STATE)
                                == DisplayState.PARTIAL,
                        "synchronized block state lost the quantum matrix display");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 100)
    public static void creativeBreakLeavesAirAndDropsNothing(GameTestHelper helper) {
        BlockPos pos = new BlockPos(6, 1, 1);
        helper.setBlock(pos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        EnergizedMEChestBlockEntity chest = chest(helper, pos);
        ItemStack cell = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
        helper.assertTrue(chest.getCellSlotInventory().addItems(cell).isEmpty(), "matrix insertion failed");
        ServerPlayer player = new FakePlayer((ServerLevel) helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "MEChestCreativeTest"));
        player.setGameMode(GameType.CREATIVE);
        helper.assertTrue(player.isCreative(), "fake player did not enter creative mode");
        player.setPos(helper.absolutePos(pos).getX() + 0.5D,
                helper.absolutePos(pos).getY() + 0.5D,
                helper.absolutePos(pos).getZ() + 0.5D);
        helper.assertTrue(player.gameMode.destroyBlock(helper.absolutePos(pos)),
                "creative break did not remove the chest");
        helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),
                "creative break left a block at the chest position");
        helper.runAfterDelay(5, () -> {
            helper.assertTrue(helper.getLevel().getBlockState(helper.absolutePos(pos)).isAir(),
                    "a chest respawned after creative break");
            helper.assertItemEntityNotPresent(ModRegistration.ENERGIZED_ME_CHEST_ITEM.get(), pos, 2.0);
            helper.assertItemEntityNotPresent(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get(), pos, 2.0);
            helper.succeed();
        });
    }

    @GameTest(template = "empty", batch = "energized_me_chest", timeoutTicks = 100)
    public static void chestTiersAcceptOnlyTheirDeclaredMatrices(GameTestHelper helper) {
        BlockPos ec1Pos = new BlockPos(1, 1, 1);
        BlockPos energizedPos = new BlockPos(3, 1, 1);
        helper.setBlock(ec1Pos, ModRegistration.EC1_ME_CHEST_BLOCK.get());
        helper.setBlock(energizedPos, ModRegistration.ENERGIZED_ME_CHEST_BLOCK.get());
        EnergizedMEChestBlockEntity ec1 = chest(helper, ec1Pos);
        EnergizedMEChestBlockEntity energized = chest(helper, energizedPos);
        ItemStack aeCell = new ItemStack(AEItems.ITEM_CELL_1K.get());
        ItemStack tier0 = new ItemStack(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get());
        ItemStack tier1 = new ItemStack(NEItems.ECO_ITEM_CELL_16M.get());
        ItemStack tier2 = new ItemStack(NEItems.ECO_ITEM_CELL_64M.get());
        ItemStack infinite = new ItemStack(NEItems.ECO_INFINITE_ITEM_CELL.get());
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var ec1Menu = new EnergizedMEChestMenu(1, player.getInventory(), ec1);
        var energizedMenu = new EnergizedMEChestMenu(2, player.getInventory(), energized);
        var ec1Slot = ec1Menu.getSlots(appeng.menu.SlotSemantics.STORAGE_CELL).get(0);
        var energizedSlot = energizedMenu.getSlots(appeng.menu.SlotSemantics.STORAGE_CELL).get(0);

        for (ItemStack cell : List.of(aeCell, tier0)) {
            helper.assertTrue(ec1.acceptsCell(cell), "EC1 rejected its declared cell: " + cell);
            helper.assertTrue(ec1Slot.mayPlace(cell), "EC1 menu disagreed with its inventory filter: " + cell);
            helper.assertTrue(energized.acceptsCell(cell), "energized chest rejected its retained cell: " + cell);
            helper.assertTrue(energizedSlot.mayPlace(cell), "energized menu rejected its retained cell: " + cell);
        }
        for (ItemStack cell : List.of(tier1, infinite)) {
            helper.assertTrue(!ec1.acceptsCell(cell), "EC1 accepted a higher-tier cell: " + cell);
            helper.assertTrue(!ec1Slot.mayPlace(cell), "EC1 menu accepted a higher-tier cell: " + cell);
            helper.assertTrue(energized.acceptsCell(cell), "energized chest rejected its added cell: " + cell);
            helper.assertTrue(energizedSlot.mayPlace(cell), "energized menu rejected its added cell: " + cell);
        }
        helper.assertTrue(!ec1.acceptsCell(tier2), "EC1 accepted tier 2");
        helper.assertTrue(!energized.acceptsCell(tier2), "energized chest accepted tier 2");
        helper.assertTrue(!ec1.acceptsCell(new ItemStack(Items.STONE)), "EC1 accepted stone");
        helper.assertTrue(!energized.acceptsCell(new ItemStack(Items.STONE)), "energized chest accepted stone");
        helper.succeed();
    }

    private static EnergizedMEChestBlockEntity chest(GameTestHelper helper, BlockPos pos) {
        var blockEntity = helper.getLevel().getBlockEntity(helper.absolutePos(pos));
        helper.assertTrue(blockEntity instanceof EnergizedMEChestBlockEntity,
                "energized ME chest block entity is missing at " + pos + ": " + blockEntity);
        if (!(blockEntity instanceof EnergizedMEChestBlockEntity chest)) {
            throw new IllegalStateException("missing energized ME chest");
        }
        return chest;
    }

    private static final class RecordingMounts implements IStorageMounts {
        private MEStorage storage;
        private int priority;

        @Override
        public void mount(MEStorage storage, int priority) {
            this.storage = storage;
            this.priority = priority;
        }
    }
}

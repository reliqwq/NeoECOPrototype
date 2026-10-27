package cn.dancingsnow.neoecoprototype;

import appeng.api.AECapabilities;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.parts.RegisterPartCapabilitiesEvent;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoprototype.integration.beyond.BeyondIntegration;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(NeoECOPrototype.MOD_ID)
public class NeoECOPrototype {
    public static final String MOD_ID = "neoecoprototype";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);
    public static net.minecraft.resources.ResourceLocation id(String path) {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }

    public NeoECOPrototype(IEventBus modBus, ModContainer container) {
        LOGGER.info("Loading {}...", MOD_ID);
        // AE2's part-model list is write-once: whoever resolves the cable-bus model or handles
        // ModelEvent.RegisterAdditional first closes it, and registerModels throws after that. Mod
        // construction is the one phase where no mod can have baked anything yet, so it cannot be a race -
        // see NeoECOPrototypeClient#registerPartModels for the player-reported startup crash this fixes.
        if (net.neoforged.fml.loading.FMLEnvironment.dist == net.neoforged.api.distmarker.Dist.CLIENT) {
            cn.dancingsnow.neoecoprototype.client.NeoECOPrototypeClient.registerPartModels();
        }
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, NeoECOPrototypeServerConfig.SPEC);

        // Register blocks / items / block entity types / creative tab.
        // The fluids come first: LiquidBlock is built from the fluid itself, not from a supplier of it.
        ModRegistration.FLUID_TYPES.register(modBus);
        ModRegistration.FLUIDS.register(modBus);
        ModRegistration.BLOCKS.register(modBus);
        if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")) {
            cn.dancingsnow.neoecoprototype.integration.mekanism.MekanismIntegration.register(ModRegistration.ITEMS);
        }
        if (ModList.get().isLoaded("ae2omnicells")) {
            cn.dancingsnow.neoecoprototype.integration.omni.UniversalIntegration.register(ModRegistration.ITEMS);
        }
        if (ModList.get().isLoaded("beyonddimensions")) {
            BeyondIntegration.register(ModRegistration.ITEMS);
        }
        if (ModList.get().isLoaded("appflux")) {
            cn.dancingsnow.neoecoprototype.integration.appflux.FeStorageIntegration
                    .register(ModRegistration.ITEMS);
        }
        ModRegistration.ITEMS.register(modBus);
        ModRegistration.BLOCK_ENTITIES.register(modBus);
        ModRegistration.RECIPE_TYPES.register(modBus);
        ModRegistration.RECIPE_SERIALIZERS.register(modBus);
        ModRegistration.ATTACHMENT_TYPES.register(modBus);
        ModRegistration.FEATURES.register(modBus);
        cn.dancingsnow.neoecoprototype.worldgen.ModWorldgen.STRUCTURE_TYPES.register(modBus);
        cn.dancingsnow.neoecoprototype.worldgen.ModWorldgen.STRUCTURE_PIECES.register(modBus);
        ModRegistration.CREATIVE_TABS.register(modBus);
        ModRegistration.DATA_COMPONENTS.register(modBus);
        ModRegistration.MENU_TYPES.register(modBus);

        // eco registers its ECO cell handler during its own construction; since we are a
        // mandatory dependency of nothing, but eco is our mandatory dependency, its
        // constructor runs before ours, so our cells (subclasses of eco's ECOStorageCellItem)
        // are already handled by eco's registered IECOCellHandler.
        modBus.addListener(NeoECOPrototype::commonSetup);
        modBus.addListener(NeoECOPrototype::registerCapabilities);
        modBus.addListener(NeoECOPrototype::registerPartCapabilities);
        // The C1 host parks measured blockstate bits in a static table keyed by dimension, so the entries
        // outlive an integrated server unless something clears them -- and one client process can open
        // several saves in a row.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.server.ServerStoppingEvent event) ->
                        cn.dancingsnow.neoecoprototype.block.computation
                                .SimplifyComputationSystemBlock.clearPendingShapes());
        // A wireless link joins two devices that belong to other mods, so there is no hook inside them to
        // rebuild it from: the probe measured a pairing that survived a reload while nothing ever put the
        // edge back. This pass is that rebuild, and it only walks the links the dimension signed up for -
        // every hundred ticks, which is why it can be that leisurely.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.tick.LevelTickEvent.Post event) -> {
                    if (!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                            .WIRELESS_LINKS.get()) {
                        return;
                    }
                    cn.dancingsnow.neoecoprototype.event.WirelessLinks.sweep(
                            event.getLevel(), ModRegistration.WIRELESS_LINKS);
                });
        // The event that actually matters is a chunk waking up next to a link. ChunkEvent.Load still fires
        // before AE2 has built the endpoint's node (that happens on the block entity's first tick), so what
        // this really buys is the short burst of follow-up passes it arms - the link comes back in under a
        // second instead of on the next slow sweep.
        net.neoforged.neoforge.common.NeoForge.EVENT_BUS.addListener(
                (net.neoforged.neoforge.event.level.ChunkEvent.Load event) -> {
                    if (!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig
                            .WIRELESS_LINKS.get()) {
                        return;
                    }
                    cn.dancingsnow.neoecoprototype.event.WirelessLinks.rebuildAround(
                            event.getLevel(), ModRegistration.WIRELESS_LINKS,
                            event.getChunk().getPos().x, event.getChunk().getPos().z);
                });
    }

    /**
     * Exposes the cable-mounted provider's return inventory. AE2 posts this event from its own
     * capability hook and builds one shared lookup out of every registration, so the host type is
     * already there and only our part class has to be described. Its javadoc promises a walk up the
     * class hierarchy, but the lookup is by exact class, so inheriting {@code PatternProviderPart}
     * does not inherit the capability.
     */
    private static void registerPartCapabilities(final RegisterPartCapabilitiesEvent event) {
        event.register(AECapabilities.GENERIC_INTERNAL_INV,
                (part, side) -> part.getLogic().getReturnInv(),
                SimplifyPatternProviderPart.class);
    }

    private static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        // Trinity's crafting module must accept products back from the machines it feeds,
        // otherwise a crafting job can push ingredients out but never recover its output.
        event.registerBlock(Capabilities.ItemHandler.BLOCK,
                (level, pos, state, blockEntity, side) -> blockEntity
                        instanceof SimplifyTrinityCraftingModuleBlockEntity module
                        ? module.getReturnHandler() : null,
                ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_BE.get());
        event.registerBlockEntity(AECapabilities.CRAFTING_MACHINE,
                ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(),
                (blockEntity, side) -> (ICraftingMachine) blockEntity);
        // AE2 registers the node-host block capability for its own block entity types only. A machine we
        // leave out of this list is invisible from the outside, so a cable placed against an already
        // standing machine finds nothing and never joins its grid.
        registerNodeHost(event, ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get());
        // The assembler can only eject a finished product into a neighbour that answers
        // Capabilities.ItemHandler.BLOCK, and AE2 never registers that capability directly for its
        // provider: it exposes the return inventory as GENERIC_INTERNAL_INV and lets its own
        // lowest-priority hook wrap every such block into an item (and fluid) handler. Registering
        // the inner capability is therefore the only way to reuse that adapter; a hand-written item
        // handler would have to reimplement the insert-while-injecting guard as well.
        event.registerBlockEntity(AECapabilities.GENERIC_INTERNAL_INV,
                ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BE.get(),
                (blockEntity, side) -> blockEntity.getLogic().getReturnInv());
        registerNodeHost(event, ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_DRIVE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_ENERGY_CELL_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_CASING_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_VENT_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get());
        registerNodeHost(event, ModRegistration.ENERGIZED_COMPUTATION_THREADING_CORE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get());
        registerNodeHost(event, ModRegistration.ENERGIZED_COMPUTATION_CORE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_TRANSMITTER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_CASING_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_WORKER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_VENT_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModRegistration.SIMPLIFY_FLUID_INPUT_HATCH_BE.get(),
                (blockEntity, side) -> blockEntity.tank);
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModRegistration.SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get(),
                (blockEntity, side) -> blockEntity.tank);
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_CRAFTING_CASING_BE.get());
        // The dissociator must be in this list too, or a cable placed against a machine that was already
        // standing finds no node host and the grid never sees it.
        registerNodeHost(event, ModRegistration.METEORITE_DISSOCIATOR_BE.get());
        // Same rule for the two batteries, and it is the whole point of them: AE2's neighbour discovery asks
        // the block entity for this capability (GridHelper.getNodeHost), and AE2 only registers it for the
        // block entity types in its own registry. Without this line each battery sits alone in its own grid
        // of one node and no controller ever adds its buffer.
        registerNodeHost(event, ModRegistration.ENERGIZED_ENERGY_CELL_BE.get());
        // Same rule again, and the plushie CPU is the newest proof of it: AE2 asks the neighbour's block
        // entity for this capability, and AE2 only posts it for the block entity types in its own registry,
        // so an unlisted type sits alone in a grid of one node however correctly its faces are exposed.
        registerNodeHost(event, ModRegistration.FUMO_DOLL_CPU_BE.get());
        event.registerBlockEntity(Capabilities.EnergyStorage.BLOCK,
                ModRegistration.METEORITE_DISSOCIATOR_BE.get(),
                (blockEntity, side) -> blockEntity.getEnergyStorage());
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK,
                ModRegistration.METEORITE_DISSOCIATOR_BE.get(),
                (blockEntity, side) -> blockEntity.getFluidInterface());
        // A Mekanism transporter asks Capabilities.ItemHandler.BLOCK and references nothing in appeng, so
        // it can only reach an interface through AE2's generic-adapter hook - which wraps whichever blocks
        // answer GENERIC_INTERNAL_INV, keyed on the block entity type. Both of our interfaces carry their
        // own type, so until this pair is posted a pipe standing next to one finds nothing at all.
        registerInterface(event, ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BE.get());
        registerInterface(event, ModRegistration.SUPERCONDUCTIVE_INTERFACE_BE.get());
        // The F1 smart pattern bus holds its patterns in its own slots, and a player asked that pipes be
        // able to move them. eco publishes no pipe face for its own bus - read off 21.2.9, no class there
        // touches GENERIC_INTERNAL_INV and NEBlockEntityEntry posts only the node host - but its block
        // entity does carry a public item handler over those slots, and the inventory under it already has
        // eco's own AEEncodedPatternFilter set on it. That filter is what makes handing it out safe: a
        // pipe can move patterns and nothing else.
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK,
                ModRegistration.SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(),
                (blockEntity, unused) -> blockEntity.itemHandler);
    }

    /**
     * The same two values AE2 posts for its own interface ({@code InitCapabilityProviders.initInterface}),
     * for a block entity type of ours: the interface buffer as the generic internal inventory, which is
     * what an outside pipe pushes into and pulls back out of, and the network side as {@code ME_STORAGE}.
     */
    private static <T extends appeng.blockentity.misc.InterfaceBlockEntity> void registerInterface(
            RegisterCapabilitiesEvent event,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        event.registerBlockEntity(AECapabilities.GENERIC_INTERNAL_INV, type,
                (blockEntity, unused) -> blockEntity.getInterfaceLogic().getStorage());
        event.registerBlockEntity(AECapabilities.ME_STORAGE, type,
                (blockEntity, unused) -> blockEntity.getInterfaceLogic().getInventory());
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerNodeHost(
            RegisterCapabilitiesEvent event,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                type,
                (blockEntity, unused) -> (IInWorldGridNodeHost) blockEntity);
    }

    private static boolean isOurBlock(net.minecraft.world.level.block.state.BlockState state) {
        var key = net.minecraft.core.registries.BuiltInRegistries.BLOCK.getKey(state.getBlock());
        return key != null && MOD_ID.equals(key.getNamespace());
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        // Bind each block to its BlockEntityType (AE2's AEBaseEntityBlock#setBlockEntity).
        ModRegistration.linkBlockEntityTypes();
        // The L1 definitions are deliberately NOT touched here. Reading NEMultiBlocks.DEFINITIONS runs
        // eco's class initialiser, which reads NEConfig values that its server config has not filled in
        // yet, and that leaves eco's own L4-L9 definitions with a build range of 1 .. -4 - their host UI
        // then throws IllegalArgumentException: 1 > -4 the moment a player opens it. Both recipe viewers
        // get our structures from our own plugins instead (NeoECOPrototypeJeiPlugin and
        // NeoECOPrototypeEmiPlugin), which never touches that list.
        // eco 21.2.1 routes a structure check to the calculator of the controller it finds in the range
        // (NEComputationClusterCalculator#controllerCalculator), so naming L1 geometry against the host's
        // block entity type covers every member block of the machine. This replaces the mixin that built a
        // throwaway calculator on every check - including for eco's own L2-L4 machines.
        cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity.registerCalculatorFactory(
                ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BE.get(),
                cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyComputationClusterCalculator::new);
        // Same on the crafting side: NECraftingClusterCalculator has the same controllerCalculator
        // routing, so naming the addon calculator against the F1 host covers the whole machine.
        cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity.registerCalculatorFactory(
                ModRegistration.SIMPLIFY_CRAFTING_SYSTEM_BE.get(),
                cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyCraftingClusterCalculator::new);
        // The C1 frame's block entity type is eco's own ECOComputationSystemBlockEntity, and since 21.2.10 that
        // class asks ECOMultiblockDefinitions.resolve, which consults registered providers before its tier
        // hook and before the caller's own supplier. Answering here is what the HEAD injection on that class
        // used to do; the condition is the same one - only the L1 tier constant, not the three member tiers.
        cn.dancingsnow.neoecoae.api.multiblock.ECOMultiblockDefinitions.register(id("l1_computation_definition"),
                100,
                context -> context.tier() == cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1
                        && context.kind() == cn.dancingsnow.neoecoae.api.multiblock.ECOMachineKind.COMPUTATION
                        ? cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition.L1
                        : null);
        // eco sizes every networked block entity's idle draw through ECOPowerProfiles.resolve, which asks its
        // providers before falling back to the caller's base number, and NEBlockEntity#onReady always finishes
        // with a COMPONENT pass-through. Answering that for our namespace is what this mod's onReady injection
        // used to do, and it is the only shape that covers our machines: the sweep `addonMachineIdlePowerInventory`
        // reports 20 of 32 block entities with tier=null, so IECOTier#getComponentIdlePower - the hook eco added
        // for exactly this request - never sees them. The role stays pinned to COMPONENT so the controller and
        // member callers keep the numbers they set for themselves.
        cn.dancingsnow.neoecoae.api.power.ECOPowerProfiles.register(id("addon_component_idle_power"), 100,
                context -> context.role() == cn.dancingsnow.neoecoae.api.power.ECOPowerRole.COMPONENT
                        && isOurBlock(context.state())
                        ? cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile.L1.baseComponentIdlePower()
                        : null);
        // AE2 only lets a card into a machine when the card is associated with that machine's item,
        // and the machine tooltip is generated from the same association. The slot count mirrors the
        // inherited molecular assembler, which allows five cards.
        var assembler = ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get();
        appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.SPEED_CARD.asItem(), assembler, 5);
        appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.ENERGY_CARD.asItem(), assembler, 5);
        var interfaceGroup = appeng.core.localization.GuiText.Interface.getTranslationKey();
        var poweredInterfaceBlock = ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get();
        var poweredInterfacePart = ModRegistration.POWERED_INTERFACE_PART.get();
        var superconductiveInterfaceBlock = ModRegistration.SUPERCONDUCTIVE_INTERFACE_ITEM.get();
        var superconductiveInterfacePart = ModRegistration.SUPERCONDUCTIVE_INTERFACE_PART.get();
        for (var item : java.util.List.of(poweredInterfaceBlock, poweredInterfacePart,
                superconductiveInterfaceBlock, superconductiveInterfacePart)) {
            appeng.api.upgrades.Upgrades.add(
                    appeng.core.definitions.AEItems.CRAFTING_CARD.asItem(), item, 1, interfaceGroup);
            appeng.api.upgrades.Upgrades.add(
                    appeng.core.definitions.AEItems.FUZZY_CARD.asItem(), item, 1, interfaceGroup);
        }
        // AppliedFlux's induction card ("lets an AE device receive power") is mixed into AE2's own
        // InterfaceLogic and PatternProviderLogic, and our machines use those classes rather than
        // subclasses of them, so only the allowance was missing. One card per machine, like appflux
        // asks for on AE2's own four.
        if (ModList.get().isLoaded("appflux")) {
            cn.dancingsnow.neoecoprototype.integration.appflux.FeStorageIntegration.registerUpgradeCards();
        }
        // AE2 的卡↔元件关联表决定：卡片 tooltip 的"可用于"清单、元件工作台升级槽放行
        // （查无登记即拒绝，界面标红"与单元格不兼容"）。按命名空间扫描以同时覆盖
        // KubeJS 脚本创建的矩阵；getConfigInventory 返回 null 的固定无限源没有分区，
        // 跳过（其 isEditable=false，工作台本就拒收）。关联带同一分组翻译键，
        // 卡片清单里我们的几十个矩阵合并为一行"L1 存储元件"，与 AE2/eco 的分组展示一致。
        var storageCellGroup = "gui.neoecoprototype.l1_storage_cells";
        for (var entry : net.minecraft.core.registries.BuiltInRegistries.ITEM.entrySet()) {
            var item = entry.getValue();
            var namespace = entry.getKey().location().getNamespace();
            boolean ourNamespace = namespace.equals(MOD_ID) || namespace.equals("kubejs");
            if (!ourNamespace
                    || !(item instanceof cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem cell)
                    || cell.getConfigInventory(net.minecraft.world.item.ItemStack.EMPTY) == null) {
                continue;
            }
            appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.FUZZY_CARD.asItem(), item, 1, storageCellGroup);
            appeng.api.upgrades.Upgrades.add(appeng.core.definitions.AEItems.INVERTER_CARD.asItem(), item, 1, storageCellGroup);
        }
        // eco's storage hosts enumerate neoecoae:cell_type, so a failed cross-mod registration would
        // silently hide our matrices' rows on eco's own storage systems.
        if (ModList.get().isLoaded("ae2omnicells")) {
            event.enqueueWork(() -> LOGGER.debug("universal cell type registry id = {}",
                    cn.dancingsnow.neoecoae.all.NERegistries.CELL_TYPE.getId(
                            cn.dancingsnow.neoecoprototype.integration.omni.SimplifyUniversalStorageCellItem
                                    .getUniversalCellType())));
        }
        // The infinite concrete matrix is vanilla-only, so its handler needs no mod check.
        event.enqueueWork(
                cn.dancingsnow.neoecoprototype.items.InfiniteConcreteCellHandler::register);
        // Same for the singularity cell: AE2's own singularity is the only thing it ever holds.
        event.enqueueWork(
                cn.dancingsnow.neoecoprototype.items.SingularityCellHandler::register);
        // KubeJS custom infinite matrices: the handler only reacts to
        // CustomInfiniteCellItem instances, which exist only when scripts created them.
        event.enqueueWork(
                cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellHandler::register);
        if (ModList.get().isLoaded("beyonddimensions")) {
            // Hand eco's cell registry a handler for the network-bound cell.
            event.enqueueWork(BeyondIntegration::registerCellHandler);
        }
        // Last work item of startup: after this nothing may join the matrix table. KubeJS' own dynamic
        // lifecycle has already had its say, which is what the registry's javadoc waits for.
        event.enqueueWork(cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry::freeze);
    }
}


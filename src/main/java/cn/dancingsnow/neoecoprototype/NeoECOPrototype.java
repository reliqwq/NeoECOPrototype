package cn.dancingsnow.neoecoprototype;

import appeng.api.AECapabilities;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.networking.IInWorldGridNodeHost;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
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
        container.registerConfig(net.neoforged.fml.config.ModConfig.Type.SERVER, NeoECOPrototypeServerConfig.SPEC);

        // Register blocks / items / block entity types / creative tab.
        ModRegistration.BLOCKS.register(modBus);
        if (ModList.get().isLoaded("mekanism") && ModList.get().isLoaded("appmek")) {
            cn.dancingsnow.neoecoprototype.integration.mekanism.MekanismIntegration.register(ModRegistration.ITEMS);
        }
        if (ModList.get().isLoaded("ae2omnicells")) {
            cn.dancingsnow.neoecoprototype.integration.omni.UniversalCellTypes.register(modBus);
            cn.dancingsnow.neoecoprototype.integration.omni.UniversalIntegration.register(ModRegistration.ITEMS);
        }
        if (ModList.get().isLoaded("beyonddimensions")) {
            BeyondIntegration.register(ModRegistration.ITEMS);
        }
        ModRegistration.ITEMS.register(modBus);
        ModRegistration.BLOCK_ENTITIES.register(modBus);
        ModRegistration.RECIPE_TYPES.register(modBus);
        ModRegistration.RECIPE_SERIALIZERS.register(modBus);
        ModRegistration.CREATIVE_TABS.register(modBus);
        ModRegistration.DATA_COMPONENTS.register(modBus);
        ModRegistration.MENU_TYPES.register(modBus);

        // eco registers its ECO cell handler during its own construction; since we are a
        // mandatory dependency of nothing, but eco is our mandatory dependency, its
        // constructor runs before ours, so our cells (subclasses of eco's ECOStorageCellItem)
        // are already handled by eco's registered IECOCellHandler.
        modBus.addListener(NeoECOPrototype::commonSetup);
        modBus.addListener(NeoECOPrototype::registerCapabilities);
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
    }

    private static <T extends net.minecraft.world.level.block.entity.BlockEntity> void registerNodeHost(
            RegisterCapabilitiesEvent event,
            net.minecraft.world.level.block.entity.BlockEntityType<T> type) {
        event.registerBlockEntity(
                AECapabilities.IN_WORLD_GRID_NODE_HOST,
                type,
                (blockEntity, unused) -> (IInWorldGridNodeHost) blockEntity);
    }

    private static void commonSetup(final FMLCommonSetupEvent event) {
        // Bind each block to its BlockEntityType (AE2's AEBaseEntityBlock#setBlockEntity).
        ModRegistration.linkBlockEntityTypes();
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
            if (item instanceof cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem
                    && ModList.get().isLoaded("megacells")) {
                appeng.api.upgrades.Upgrades.add(
                        gripe._90.megacells.definition.MEGAItems.COMPRESSION_CARD.asItem(), item, 1, storageCellGroup);
            }
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
        // KubeJS custom infinite matrices: the handler only reacts to
        // CustomInfiniteCellItem instances, which exist only when scripts created them.
        event.enqueueWork(
                cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellHandler::register);
        if (ModList.get().isLoaded("beyonddimensions")) {
            // Hand eco's cell registry a handler for the network-bound cell.
            event.enqueueWork(BeyondIntegration::registerCellHandler);
        }
    }
}


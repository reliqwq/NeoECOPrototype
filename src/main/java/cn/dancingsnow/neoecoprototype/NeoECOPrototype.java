package cn.dancingsnow.neoecoprototype;

import appeng.api.AECapabilities;
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
        ModRegistration.CREATIVE_TABS.register(modBus);

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
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_CONTROLLER_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_DRIVE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_ENERGY_CELL_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_CASING_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_INTERFACE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_STORAGE_VENT_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_SYSTEM_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get());
        registerNodeHost(event, ModRegistration.SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get());
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


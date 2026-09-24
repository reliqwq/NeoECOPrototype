package cn.dancingsnow.neoecoprototype.client;

import appeng.api.parts.PartModels;
import appeng.client.gui.implementations.PatternProviderScreen;
import appeng.init.client.InitScreens;
import appeng.menu.implementations.InterfaceMenu;
import appeng.menu.implementations.PatternProviderMenu;
import appeng.client.render.crafting.MolecularAssemblerRenderer;
import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
import cn.dancingsnow.neoecoprototype.integration.kubejs.InfiniteMatrixClientModels;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.client.render.FumoModel;
import cn.dancingsnow.neoecoprototype.client.render.FumoRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyEnergizedComputationCoreRenderer;
import cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoECOPrototypeClient {
    private NeoECOPrototypeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // AE2 freezes this set before baking; a part model missing from it crashes the cable bus
        // tesselation with "Trying to use an unregistered part model".
        PartModels.registerModels(
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/powered_me_interface"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/superconductive_interface"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/pattern_provider"));

        ResourceLocation itemCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_item");
        ResourceLocation fluidCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_fluid");
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get(), itemCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_16K.get(), itemCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_64K.get(), itemCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_1M.get(), itemCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_4M.get(), itemCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_1K.get(), fluidCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_16K.get(), fluidCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_64K.get(), fluidCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_1M.get(), fluidCellModel);
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_4M.get(), fluidCellModel);
        // 小宗流体/化学品盘：复用 eco 的 MEGA 外壳贴图 + 我们的 L1 等级灯（浅绿）。
        ResourceLocation megaFluidCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk_fluid");
        ResourceLocation megaChemicalCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk_chemical");
        if (ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL != null) {
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL.get(), megaFluidCellModel);
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL_EXPANDED.get(), megaFluidCellModel);
        }
        if (ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL.get(), megaChemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED.get(), megaChemicalCellModel);
        }
        ResourceLocation smallBulkCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk");
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL != null) {
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get(), smallBulkCellModel);
            ResourceLocation smallBulkExpandedCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk_expanded");
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_CELL_EXPANDED.get(), smallBulkExpandedCellModel);
        }
        ResourceLocation pigcatCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_pigcat");
        ECOCellModels.register(ModRegistration.PIGCAT_STORAGE_CELL.get(), pigcatCellModel);
        ResourceLocation concreteCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_concrete");
        ECOCellModels.register(ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get(), concreteCellModel);
        if (ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K != null) {
            ResourceLocation omniCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_universal");
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K.get(), omniCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1M.get(), omniCellModel);
        }
        if (ModRegistration.OPTIONAL_QUANTUM_CELL_1K != null) {
            ResourceLocation quantumCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_quantum");
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1K.get(), quantumCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1M.get(), quantumCellModel);
        }
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            ResourceLocation chemicalCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_chemical");
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1K.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_4K.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_16K.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1M.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_4M.get(), chemicalCellModel);
        }
        if (ModList.get().isLoaded("kubejs")) {
            ResourceLocation infiniteItemCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_concrete");
            InfiniteMatrixClientModels.registerCellModels(infiniteItemCellModel);
            // Script-created finite matrices are plain storage-cell items, so they are registered here.
            InfiniteMatrixClientModels.registerScriptedCellModels();
        }
        ECOCellModels.runDeferredRegistration();

        ECOComputationModels.registerCellModel(
                BuiltInRegistries.ITEM.wrapAsHolder(ModRegistration.SIMPLIFY_COMPUTATION_CELL_1M.get()),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed"));
        ECOComputationModels.registerCellModel(
                BuiltInRegistries.ITEM.wrapAsHolder(ModRegistration.ENERGIZED_COMPUTATION_CELL_4M.get()),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed"));
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1,
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4_dis"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4"));
        // The drive renderer adopts the working cell's tier before asking for cable models
        // (SimplifyComputationDriveRenderer#renderFixed), so every tier a cell can carry needs an entry
        // here or chunk rendering throws an NPE inside eco's lookup.
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1_REINFORCED,
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4_dis"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4"));
        ECOComputationModels.runDeferredRegistration();

        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_DRIVE_BE.get(),
                new SimplifyDriveRenderer());
        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(),
                new SimplifyComputationDriveRenderer());
        // The energized core hides its block model when formed, so its lamps come back through
        // eco's section-geometry pass instead.
        FixedBlockEntityRenderers.register(ModRegistration.ENERGIZED_COMPUTATION_CORE_BE.get(),
                new SimplifyEnergizedComputationCoreRenderer());
        // Same hook AE2's StyleManager uses for its style cache: reloads must discard the parsed copy
        // or a resource pack could never override our assembler layout.
        if (Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager resourceManager) {
            resourceManager.registerReloadListener(
                    (ResourceManagerReloadListener) manager -> ProcessorAssemblerScreen.forgetStyle());
        }
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        // Nothing references this from a blockstate: only the section-geometry renderer draws it.
        event.register(ModelResourceLocation.standalone(
                SimplifyEnergizedComputationCoreRenderer.FORMED_FACE_MODEL));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4_dis")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed")));
        // Cell models are only loaded when registered as additional models.
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_item")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_fluid")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_pigcat")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_concrete")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_universal")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_small_bulk_expanded")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_default")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_custom")));
        InfiniteMatrixClientModels.registerAdditionalModels(event);
        InfiniteMatrixClientModels.registerScriptedAdditionalModels(event);
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_chemical")));
        }
    }

    @SubscribeEvent
    public static void onRegisterMenuScreens(net.neoforged.neoforge.client.event.RegisterMenuScreensEvent event) {
        InitScreens.<InterfaceMenu, PoweredInterfaceScreen>register(event,
                ModRegistration.L1_POWERED_INTERFACE_MENU.get(),
                PoweredInterfaceScreen::new,
                "/screens/neoecoprototype/l1_powered_me_interface.json");
        InitScreens.<InterfaceMenu, SuperconductiveInterfaceScreen>register(event,
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_MENU.get(),
                SuperconductiveInterfaceScreen::new,
                "/screens/neoecoprototype/superconductive_interface.json");
        InitScreens.<PatternProviderMenu, PatternProviderScreen<PatternProviderMenu>>register(event,
                ModRegistration.L1_PATTERN_PROVIDER_MENU.get(),
                (menu, inventory, title, style) -> new PatternProviderScreen<>(menu, inventory, title, style),
                "/screens/neoecoprototype/l1_pattern_provider.json");
        event.<ProcessorAssemblerMenu, ProcessorAssemblerScreen>register(
                ProcessorAssemblerMenu.type(), ProcessorAssemblerScreen::new);
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_DRIVE_BE.get(), SimplifyDriveRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(), SimplifyComputationDriveRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(),
                MolecularAssemblerRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.FUMO_BE.get(), FumoRenderer::new);
    }

    @SubscribeEvent
    public static void onRegisterLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(FumoModel.LAYER_LOCATION, FumoModel::createBodyLayer);
        event.registerLayerDefinition(FumoModel.SLIM_LAYER_LOCATION, FumoModel::createSlimBodyLayer);
    }

    /**
     * Break particles come from the block atlas, and only the bundled skin is stitched into it, so a
     * downloaded player skin has no sprite there and every doll would crumble into Mita confetti.
     * Suppressing them is honest; the block still breaks with its sound and drops the right doll.
     */
    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        event.registerBlock(new IClientBlockExtensions() {
            @Override
            public boolean addDestroyEffects(BlockState state, Level level, BlockPos pos,
                                             net.minecraft.client.particle.ParticleEngine engine) {
                return true;
            }

            @Override
            public boolean addHitEffects(BlockState state, Level level,
                                         net.minecraft.world.phys.HitResult target,
                                         net.minecraft.client.particle.ParticleEngine engine) {
                return true;
            }
        }, ModRegistration.FUMO_BLOCK.get());
    }
}


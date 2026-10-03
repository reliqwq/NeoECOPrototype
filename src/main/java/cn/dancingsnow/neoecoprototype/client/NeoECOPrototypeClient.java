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
import cn.dancingsnow.neoecoprototype.client.render.FumoItemRenderer;
import cn.dancingsnow.neoecoprototype.client.render.FumoModel;
import cn.dancingsnow.neoecoprototype.client.render.FumoRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;
import net.neoforged.neoforge.event.AddPackFindersEvent;

@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoECOPrototypeClient {
    private NeoECOPrototypeClient() {
    }

    /**
     * Offers the previous release's artwork as a pack the player can just toggle, instead of making
     * them download one. It ships at {@code legacy_art/} in the jar and is re-cut from a tag by
     * {@code tools/legacy_pack.py}. Required=false and PackSource.DEFAULT keep it switched off after
     * an upgrade, so nobody's machine changes appearance without asking.
     */
    @SubscribeEvent
    public static void onAddPackFinders(AddPackFindersEvent event) {
        if (event.getPackType() != PackType.CLIENT_RESOURCES) {
            return;
        }
        event.addPackFinders(
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "legacy_art"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("pack.neoecoprototype.legacy"),
                PackSource.DEFAULT,
                false,
                Pack.Position.TOP);
    }

    /**
     * AE2 freezes the part-model set the first time its cable-bus model resolves dependencies or its
     * own RegisterAdditional handler runs, and {@code registerModels} throws after that. Freezing can
     * therefore happen before client setup in a large pack, so this has to run in the setup phase AE2
     * names: pre-initialization. A missing entry here is worse than late - it crashes cable bus
     * tessellation with "Trying to use an unregistered part model".
     */
    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event) {
        PartModels.registerModels(
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/powered_me_interface"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/superconductive_interface"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "part/pattern_provider"));
    }

    /**
     * Every in-drive cell model we hand to eco. The same list must be registered as an additional
     * model or the item renders nothing, so it lives here once instead of in two places.
     */
    private static final java.util.List<String> DRIVE_CELL_MODELS = java.util.List.of(
            "l0_item_1k", "l0_item_16k", "l0_item_64k", "l1_item", "l1r_item",
            "l0_fluid_1k", "l0_fluid_16k", "l0_fluid_64k", "l1_fluid", "l1r_fluid",
            "l0_chemical_1k", "l0_chemical_16k", "l0_chemical_64k", "l1_chemical", "l1r_chemical",
            "l1_small_bulk_item", "l1r_small_bulk_item",
            "l1_small_bulk_fluid", "l1r_small_bulk_fluid",
            "l1_small_bulk_chemical", "l1r_small_bulk_chemical",
            // 脚本矩阵按家族拿模型（.material('small_bulk')），没有档位维度，所以这张不分介质的旧图
            // 还得继续加载；注册表里那 27 张才是真在用的。
            "l1_small_bulk",
            "l1_pigmee", "l1_concrete", "l1_universal", "l1_quantum", "l1_default", "l1_custom");

    private static ResourceLocation cellModel(String name) {
        return ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/cell/storage_cell_" + name);
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        // 每档一格模型：1M 以下用 l0_*（drive/cell_*_lower 那组贴图，等级灯按档位往上数第几格），1M 用
        // l1_*，4M 是同一几何只把等级灯换成会闪的 cell_level_4m。以前一个介质共用一张，现在各拿自己的。
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_1K.get(), cellModel("l0_item_1k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_16K.get(), cellModel("l0_item_16k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_64K.get(), cellModel("l0_item_64k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_1M.get(), cellModel("l1_item"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_ITEM_CELL_4M.get(), cellModel("l1r_item"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_1K.get(), cellModel("l0_fluid_1k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_16K.get(), cellModel("l0_fluid_16k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_64K.get(), cellModel("l0_fluid_64k"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_1M.get(), cellModel("l1_fluid"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_FLUID_CELL_4M.get(), cellModel("l1r_fluid"));
        // 小宗流体/化学品盘：复用 eco 的 MEGA 外壳贴图 + 我们的 L1 等级灯（浅绿）。
        if (ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL != null) {
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL.get(),
                    cellModel("l1_small_bulk_fluid"));
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_FLUID_CELL_EXPANDED.get(),
                    cellModel("l1r_small_bulk_fluid"));
        }
        if (ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL.get(),
                    cellModel("l1_small_bulk_chemical"));
            ECOCellModels.register(ModRegistration.OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED.get(),
                    cellModel("l1r_small_bulk_chemical"));
        }
        if (ModRegistration.SIMPLIFY_SMALL_BULK_CELL != null) {
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_CELL.get(),
                    cellModel("l1_small_bulk_item"));
            ECOCellModels.register(ModRegistration.SIMPLIFY_SMALL_BULK_CELL_EXPANDED.get(),
                    cellModel("l1r_small_bulk_item"));
        }
        ECOCellModels.register(ModRegistration.PIGMEE_STORAGE_CELL.get(), cellModel("l1_pigmee"));
        ECOCellModels.register(ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get(), cellModel("l1_concrete"));
        if (ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K.get(), cellModel("l1_universal"));
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1M.get(), cellModel("l1_universal"));
        }
        if (ModRegistration.OPTIONAL_QUANTUM_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1K.get(), cellModel("l1_quantum"));
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1M.get(), cellModel("l1_quantum"));
        }
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1K.get(), cellModel("l0_chemical_1k"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_16K.get(), cellModel("l0_chemical_16k"));
        ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_64K.get(), cellModel("l0_chemical_64k"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1M.get(), cellModel("l1_chemical"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_4M.get(), cellModel("l1r_chemical"));
        }
        if (ModList.get().isLoaded("kubejs")) {
            ResourceLocation infiniteItemCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_concrete");
            InfiniteMatrixClientModels.registerCellModels(infiniteItemCellModel);
            // Script-created finite matrices are plain storage-cell items, so they are registered here.
            InfiniteMatrixClientModels.registerScriptedCellModels();
        }
        ECOCellModels.runDeferredRegistration();

        // TedXenon's split, by cell item rather than by drive: the CE1 array shows the cell_l1 face and
        // the CE1R shows cell_l4. eco's registry holds exactly these two slots per item (normal = not
        // working, formed = working), so the distinction needs no renderer change.
        ECOComputationModels.registerCellModel(
                BuiltInRegistries.ITEM.wrapAsHolder(ModRegistration.SIMPLIFY_COMPUTATION_CELL_1M.get()),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l1"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l1_formed"));
        ECOComputationModels.registerCellModel(
                BuiltInRegistries.ITEM.wrapAsHolder(ModRegistration.ENERGIZED_COMPUTATION_CELL_4M.get()),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed"));
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1,
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1_dis"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1"));
        // The drive renderer adopts the working cell's tier before asking for cable models
        // (SimplifyComputationDriveRenderer#renderFixed), so every tier a cell can carry needs an entry
        // here or chunk rendering throws an NPE inside eco's lookup.
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1_REINFORCED,
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1_dis"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1"));
        ECOComputationModels.runDeferredRegistration();

        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_DRIVE_BE.get(),
                new SimplifyDriveRenderer());
        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(),
                new SimplifyComputationDriveRenderer());
        // Same hook AE2's StyleManager uses for its style cache: reloads must discard the parsed copy
        // or a resource pack could never override our assembler layout. FumoItemRenderer joins it for
        // the same reason - vanilla rebuilds block entity renderers on reload but not the item one.
        if (Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager resourceManager) {
            resourceManager.registerReloadListener((ResourceManagerReloadListener) manager -> {
                ProcessorAssemblerScreen.forgetStyle();
                FumoItemRenderer.forgetCache();
            });
        }
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1_dis")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l1")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l1_formed")));
        // Cell models are only loaded when registered as additional models, so this list is the same
        // set onClientSetup hands to eco - one place to keep in step, not two.
        DRIVE_CELL_MODELS.forEach(name -> event.register(ModelResourceLocation.standalone(cellModel(name))));
        InfiniteMatrixClientModels.registerAdditionalModels(event);
        InfiniteMatrixClientModels.registerScriptedAdditionalModels(event);
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


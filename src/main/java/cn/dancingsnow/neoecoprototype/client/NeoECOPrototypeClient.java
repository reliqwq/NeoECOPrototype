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
import cn.dancingsnow.neoecoprototype.client.model.SimplifyCoolingControllerBakedModel;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.GiantPlushieRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyCoolingControllerRenderer;
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
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.extensions.common.IClientBlockExtensions;
import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
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
     * AE2 freezes the part-model set the first time its cable-bus model resolves dependencies
     * ({@code appeng/client/render/cablebus/CableBusModel}) or its own {@code RegisterAdditional} handler
     * runs ({@code appeng/init/client/InitAdditionalModels}, whose first statement is the freeze), and
     * {@code registerModels} throws after that. So this is not a matter of picking the "right" lifecycle
     * event: it has to run before *any* mod's setup work can bake a model, which is mod construction -
     * NeoForge constructs every mod before it dispatches {@code FMLCommonSetupEvent} to anyone. Measured on
     * a player's pack where the common-setup call site was already too late (NeoForge 21.1.263, AE2
     * 19.2.18; {@code PartModels} bytecode is identical in AE2 19.2.17, so this was never the AE2 version).
     *
     * <p>A missing entry here is worse than late: the three locations are read by
     * {@code CableBusBakedModel}/{@code CableBusModel}/{@code FacadeBuilder}, and cable tessellation then
     * crashes with "Trying to use an unregistered part model". That is also why there is no try/catch -
     * swallowing it would trade a startup crash for a later one plus invisible broken parts.</p>
     */
    public static void registerPartModels() {
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
            "l1_pigmee", "l1_concrete", "l1_universal", "l1_quantum", "l1_default", "l1_custom",
            "l0_fe_1k", "l0_spent_waste", "l0_universal", "l0_quantum", "l0_complex_universal");

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
        // Its own copy of the special-cell housing, so repainting one does not move the other.
        ECOCellModels.register(ModRegistration.SIMPLIFY_SINGULARITY_CELL.get(), cellModel("l1_singularity"));
        if (ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1K.get(),
                    cellModel("l0_universal"));
            ECOCellModels.register(ModRegistration.OPTIONAL_UNIVERSAL_CELL_1M.get(), cellModel("l1_universal"));
        }
        if (ModRegistration.OPTIONAL_QUANTUM_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1K.get(), cellModel("l0_quantum"));
            ECOCellModels.register(ModRegistration.OPTIONAL_QUANTUM_CELL_1M.get(), cellModel("l1_quantum"));
        }
        if (ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1K.get(),
                    cellModel("l0_spent_waste"));
        }
        if (ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1M != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_SPENT_WASTE_CELL_1M.get(),
                    cellModel("l1_spent_waste"));
        }
        if (ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1K.get(),
                    cellModel("l0_complex_universal"));
        }
        if (ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M.get(),
                    cellModel("l1_complex_universal"));
        }
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1K.get(), cellModel("l0_chemical_1k"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_16K.get(), cellModel("l0_chemical_16k"));
        ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_64K.get(), cellModel("l0_chemical_64k"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1M.get(), cellModel("l1_chemical"));
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_4M.get(), cellModel("l1r_chemical"));
        }
        if (ModRegistration.OPTIONAL_FE_CELL_1M != null) {
            // The FE pair reads the energy stripe of cell_type (pixels x 8..9) instead of the item one
            // (x 4..5), exactly the shift eco's own l4/l6/l9_fe models make against its item one. No new
            // artwork: the stripe is already in the cell_type.png we ship, which is that file byte for byte.
            ECOCellModels.register(ModRegistration.OPTIONAL_FE_CELL_1K.get(), cellModel("l0_fe_1k"));
            ECOCellModels.register(ModRegistration.OPTIONAL_FE_CELL_1M.get(), cellModel("l1_fe"));
            ECOCellModels.register(ModRegistration.OPTIONAL_FE_CELL_4M.get(), cellModel("l1r_fe"));
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
        // The cable splits by the drive it sits in, never by which cell pushed it there: our own frame is
        // a level 1 one, so both of our tiers wear the plain pair here. Both entries are still needed --
        // eco's lookup has no miss branch, so a tier a cell can carry without a registration would NPE
        // inside chunk rendering. Our renderer asks the one-argument lookups, which pass a null host and so
        // never read the per-host table below.
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1,
                ComputationCableModels.LOWER_DISCONNECTED, ComputationCableModels.LOWER_CONNECTED);
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1_REINFORCED,
                ComputationCableModels.LOWER_DISCONNECTED, ComputationCableModels.LOWER_CONNECTED);
        // The glowing pair lives in eco's per-host table: eco 21.2.10 hands renderFixed's lookup the drive
        // block the cell is sitting in (blockEntity.getBlockState().getBlock()), which is the key our own
        // frame cannot produce. So each of our four tiers is registered against eco's computation drive --
        // reaching this table at all already means the host is eco's. A tier we do not own is left as eco
        // drew it, exactly as the retired redirect used to return null.
        for (var tier : cn.dancingsnow.neoecoprototype.api.SimplifyTier.values()) {
            ECOComputationModels.registerCableModel(tier,
                    cn.dancingsnow.neoecoae.all.NEBlocks.COMPUTATION_DRIVE.get(),
                    ComputationCableModels.UPPER_DISCONNECTED, ComputationCableModels.UPPER_CONNECTED);
        }
        ECOComputationModels.runDeferredRegistration();

        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_DRIVE_BE.get(),
                new SimplifyDriveRenderer());
        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(),
                new SimplifyComputationDriveRenderer());
        // eco's own lighting pass covers COMPUTATION_DRIVE, ECO_DRIVE and its three cooling controller tiers,
        // and its model selector matches "neoecoae" plus three literal paths - so our controller joins by
        // registering here and wrapping its own models below, not by waiting for that list to open.
        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get(),
                new SimplifyCoolingControllerRenderer());
        // Same hook AE2's StyleManager uses for its style cache: reloads must discard the parsed copy
        // or a resource pack could never override our assembler layout.
        if (Minecraft.getInstance().getResourceManager() instanceof ReloadableResourceManager resourceManager) {
            resourceManager.registerReloadListener((ResourceManagerReloadListener) manager -> {
                ProcessorAssemblerScreen.forgetStyle();
            });
        }
    }

    /**
     * Wraps our formed cooling controller so its geometry is drawn only through the lit pass. eco does the
     * same for its three tiers, but its selector is a namespace test plus three literal paths, so nothing we
     * ship can join it and the wrapper has to come from our side.
     *
     * <p>The match is a string comparison against our blockstate file name, which is why
     * {@code coolingControllerFormedVariantsAreWhatTheWrapperMatches} reads that same file: renaming it would
     * otherwise cost a lit face and leave nothing but a dark block to explain.
     */
    @SubscribeEvent
    public static void onModifyBakingResult(ModelEvent.ModifyBakingResult event) {
        event.getModels().replaceAll((location, model) ->
                NeoECOPrototype.MOD_ID.equals(location.id().getNamespace())
                        && "simplify_computation_cooling_controller".equals(location.id().getPath())
                        && java.util.List.of(location.variant().split(",")).contains("formed=true")
                        ? new SimplifyCoolingControllerBakedModel(model) : model);
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1_dis")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1r")));
        event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l1r_dis")));
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
        // One renderer for the doll block entity type: it draws nothing for the four shelf dolls and the
        // scaled-up plushie for the giant, which is why the type is shared rather than split.
        event.registerBlockEntityRenderer(ModRegistration.FUMO_DOLL_BE.get(), GiantPlushieRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(), SimplifyComputationDriveRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(),
                MolecularAssemblerRenderer::new);
    }

    /** These names only resolve because blocks.json stitches them; see the guard in the suite. */
    private static final ResourceLocation DISSOLUTION_STILL =
            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/dissolution_solution_still");
    private static final ResourceLocation DISSOLUTION_FLOWING =
            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/dissolution_solution_flow");
    private static final ResourceLocation ICE_CREAM_STILL =
            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/cryotheum_ice_cream_still");
    private static final ResourceLocation ICE_CREAM_FLOWING =
            ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/cryotheum_ice_cream_flow");

    @SubscribeEvent
    public static void onRegisterClientExtensions(RegisterClientExtensionsEvent event) {
        // eco gets this for free from Registrate; our fluids are hand-registered, so the sprite names have
        // to be handed over here. IClientFluidTypeExtensions.getStillTexture defaults to null and the
        // bucket model bakes that straight into the block atlas lookup -- a null key is what makes a
        // bucket read as the purple-black missing sprite, with nothing but one WARN in the client log.
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return DISSOLUTION_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return DISSOLUTION_FLOWING;
            }
        }, ModRegistration.DISSOLUTION_SOLUTION_TYPE.get());
        event.registerFluidType(new IClientFluidTypeExtensions() {
            @Override
            public ResourceLocation getStillTexture() {
                return ICE_CREAM_STILL;
            }

            @Override
            public ResourceLocation getFlowingTexture() {
                return ICE_CREAM_FLOWING;
            }
        }, ModRegistration.CRYOTHEUM_ICE_CREAM_FLUID_TYPE.get());
    }
}


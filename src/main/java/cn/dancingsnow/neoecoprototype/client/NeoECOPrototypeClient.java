package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyComputationDriveRenderer;
import cn.dancingsnow.neoecoprototype.client.renderer.blockentity.SimplifyDriveRenderer;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class NeoECOPrototypeClient {
    private NeoECOPrototypeClient() {
    }

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
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
        ResourceLocation pigcatCellModel = ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_pigcat");
        ECOCellModels.register(ModRegistration.PIGCAT_STORAGE_CELL.get(), pigcatCellModel);
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            ResourceLocation chemicalCellModel = ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_chemical");
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1K.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_16K.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_1M.get(), chemicalCellModel);
            ECOCellModels.register(ModRegistration.OPTIONAL_CHEMICAL_CELL_4M.get(), chemicalCellModel);
        }
        ECOCellModels.runDeferredRegistration();

        ECOComputationModels.registerCellModel(
                BuiltInRegistries.ITEM.wrapAsHolder(ModRegistration.SIMPLIFY_COMPUTATION_CELL_1M.get()),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cell/cell_l4_formed"));
        ECOComputationModels.registerCableModel(
                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1,
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4_dis"),
                ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "block/computation_cable/cable_l4"));
        ECOComputationModels.runDeferredRegistration();

        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_DRIVE_BE.get(),
                new SimplifyDriveRenderer());
        FixedBlockEntityRenderers.register(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(),
                new SimplifyComputationDriveRenderer());
    }

    @SubscribeEvent
    public static void onRegisterAdditionalModels(ModelEvent.RegisterAdditional event) {
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
        if (ModRegistration.OPTIONAL_CHEMICAL_CELL_1K != null) {
            event.register(ModelResourceLocation.standalone(ResourceLocation.fromNamespaceAndPath(
                    NeoECOPrototype.MOD_ID, "block/cell/storage_cell_l1_chemical")));
        }
    }

    @SubscribeEvent
    public static void onRegisterRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_DRIVE_BE.get(), SimplifyDriveRenderer::new);
        event.registerBlockEntityRenderer(ModRegistration.SIMPLIFY_COMPUTATION_DRIVE_BE.get(), SimplifyComputationDriveRenderer::new);
    }
}

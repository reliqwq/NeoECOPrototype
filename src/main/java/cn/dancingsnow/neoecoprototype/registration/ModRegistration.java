package cn.dancingsnow.neoecoprototype.registration;

import appeng.api.stacks.AEKeyType;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import cn.dancingsnow.neoecoprototype.block.SimplifyCasingBlock;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationThreadingCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationTransmitterBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingVentBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidInputHatchBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidOutputHatchBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.calculator.NEComputationClusterCalculator;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyDriveBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyEnergyCellBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageCasingBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageControllerBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.storage.SimplifyStorageVentBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationCoolingControllerBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationDriveBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationParallelCoreBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationSystemBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationThreadingCoreBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationTransmitterBlock;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyEnergizedComputationCoreBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingCasingBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingParallelCoreBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingPatternBusBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingVentBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingWorkerBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyStonecuttingAssemblerBlock;
import cn.dancingsnow.neoecoprototype.block.decoration.FumoBlock;
import cn.dancingsnow.neoecoprototype.blockentity.decoration.FumoBlockEntity;
import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyPatternProviderBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyPoweredMEInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifySuperconductiveInterfaceBlock;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPoweredMEInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifySuperconductiveInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyFluidInputHatchBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyFluidOutputHatchBlock;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityControllerBlock;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityStorageModuleBlock;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityComputationModuleBlock;
import cn.dancingsnow.neoecoprototype.block.trinity.SimplifyTrinityCraftingModuleBlock;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityComputationModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageVentBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyEnergizedComputationCoreBlockEntity;
import cn.dancingsnow.neoecoprototype.items.PigcatStorageMatrixHousingItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyComputationCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkFluidStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.Util;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import appeng.items.parts.PartItem;
import cn.dancingsnow.neoecoprototype.part.PoweredInterfacePart;
import cn.dancingsnow.neoecoprototype.part.SuperconductiveInterfacePart;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ModRegistration {

    // ============================ Registries ============================

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NeoECOPrototype.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, NeoECOPrototype.MOD_ID);

    public static final Supplier<PartItem<PoweredInterfacePart>> POWERED_INTERFACE_PART = ITEMS.register(
            "cable_powered_me_interface",
            () -> new PartItem<>(new Item.Properties(), PoweredInterfacePart.class, PoweredInterfacePart::new));
    public static final Supplier<PartItem<SuperconductiveInterfacePart>> SUPERCONDUCTIVE_INTERFACE_PART = ITEMS.register(
            "cable_superconductive_interface",
            () -> new PartItem<>(new Item.Properties(), SuperconductiveInterfacePart.class, SuperconductiveInterfacePart::new));
    public static final Supplier<PartItem<SimplifyPatternProviderPart>> CABLE_PATTERN_PROVIDER_PART = ITEMS.register(
            "cable_pattern_provider",
            () -> new PartItem<>(new Item.Properties(), SimplifyPatternProviderPart.class, SimplifyPatternProviderPart::new));
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_4K;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_16K;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_4M;
    public static Supplier<? extends Item> OPTIONAL_SMALL_BULK_CHEMICAL_CELL;
    public static Supplier<? extends Item> OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED;
    public static Supplier<? extends Item> OPTIONAL_BEYOND_STORAGE_CELL;
    public static Supplier<? extends Item> OPTIONAL_UNIVERSAL_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_UNIVERSAL_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_QUANTUM_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_QUANTUM_CELL_1M;
    public static final Supplier<Item> SIMPLIFY_QUANTUM_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_quantum_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_UNIVERSAL_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_universal_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_CHEMICAL_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_chemical_storage_matrix_housing", () -> new Item(new Item.Properties()));

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoECOPrototype.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoECOPrototype.MOD_ID);
    /** The plushie stores whose skin to wear, exactly like a vanilla player head does. */
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NeoECOPrototype.MOD_ID);
    public static final Supplier<DataComponentType<ResolvableProfile>> FUMO_OWNER =
            DATA_COMPONENTS.register("fumo_owner", () -> DataComponentType.<ResolvableProfile>builder()
                    .persistent(ResolvableProfile.CODEC).networkSynchronized(ResolvableProfile.STREAM_CODEC).build());
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, NeoECOPrototype.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NeoECOPrototype.MOD_ID);
    /**
     * AE2's MenuTypeBuilder.build() queues into its own InitMenuTypes, whose queue is already drained
     * because AE2 loads first, so the type has to go through this mod's register.
     */
    public static final DeferredRegister<net.minecraft.world.inventory.MenuType<?>> MENU_TYPES =
            DeferredRegister.create(Registries.MENU, NeoECOPrototype.MOD_ID);
    public static final Supplier<net.minecraft.world.inventory.MenuType<cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu>>
            PROCESSOR_ASSEMBLER_MENU = MENU_TYPES.register("processor_assembler",
            cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu::buildType);
    public static final Supplier<net.minecraft.world.inventory.MenuType<appeng.menu.implementations.InterfaceMenu>>
            L1_POWERED_INTERFACE_MENU = MENU_TYPES.register("l1_powered_me_interface", () ->
            appeng.menu.implementations.MenuTypeBuilder.<appeng.menu.implementations.InterfaceMenu, appeng.helpers.InterfaceLogicHost>create(
                    (type, id, inventory, host) -> new appeng.menu.implementations.InterfaceMenu(type, id, inventory, host),
                    appeng.helpers.InterfaceLogicHost.class).buildUnregistered(NeoECOPrototype.id("l1_powered_me_interface")));
    public static final Supplier<net.minecraft.world.inventory.MenuType<appeng.menu.implementations.InterfaceMenu>>
            SUPERCONDUCTIVE_INTERFACE_MENU = MENU_TYPES.register("superconductive_interface", () ->
             appeng.menu.implementations.MenuTypeBuilder.<appeng.menu.implementations.InterfaceMenu, appeng.helpers.InterfaceLogicHost>create(
                     (type, id, inventory, host) -> new appeng.menu.implementations.InterfaceMenu(type, id, inventory, host),
                     appeng.helpers.InterfaceLogicHost.class).buildUnregistered(NeoECOPrototype.id("superconductive_interface")));
     public static final Supplier<net.minecraft.world.inventory.MenuType<appeng.menu.implementations.PatternProviderMenu>>
             L1_PATTERN_PROVIDER_MENU = MENU_TYPES.register("l1_pattern_provider", () ->
            appeng.menu.implementations.MenuTypeBuilder.<appeng.menu.implementations.PatternProviderMenu, appeng.helpers.patternprovider.PatternProviderLogicHost>create(
                    (type, id, inventory, host) -> new appeng.menu.implementations.PatternProviderMenu(type, id, inventory, host),
                    appeng.helpers.patternprovider.PatternProviderLogicHost.class).buildUnregistered(NeoECOPrototype.id("l1_pattern_provider")));
    public static final Supplier<RecipeType<ProcessorAssemblerRecipe>> PROCESSOR_ASSEMBLER_RECIPE_TYPE =
            RECIPE_TYPES.register("processor_assembler", () -> ProcessorAssemblerRecipe.TYPE);
    public static final Supplier<RecipeSerializer<ProcessorAssemblerRecipe>> PROCESSOR_ASSEMBLER_RECIPE_SERIALIZER =
            RECIPE_SERIALIZERS.register("processor_assembler", ProcessorAssemblerRecipe.Serializer::new);

    /** 闁绘せ鏅涢幖褔宕ュ鍕槣濡増顭囩挒銏ゆ晬濮樿翰鍋欓柛婊冭嫰鐎规娊宕ｉ弽顒€鍨遍柛銉ュ⒔閸嬶絿绱撻埀顒勬嚌鐠囇呯#80FFAE 闁稿绻楁竟瀣晬婢跺鐟㈢紒顔碱槸閸嶅灚鎯旈弴鈥愁棌闁?A8DCA8 闁稿绻戠拹浼存晬婢跺顓洪梻鍌涚暘閳?*/
    private static final int NAME_THEME_GREEN = 0xFF94EDAB;

    private static final BlockBehaviour.Properties MACHINE_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F);
    private static final BlockBehaviour.Properties COMPUTATION_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    private static final BlockBehaviour.Properties DRIVE_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    private static final BlockBehaviour.Properties CASING_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    private static final BlockBehaviour.Properties GREEN_CASING_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
     private static final BlockBehaviour.Properties AE2_MACHINE_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();

    // ============================ Blocks ============================

    public static final Supplier<SimplifyStorageControllerBlock> SIMPLIFY_STORAGE_CONTROLLER_BLOCK =
            BLOCKS.register("simplify_storage_controller",
                    () -> new SimplifyStorageControllerBlock(MACHINE_PROPS));

    public static final Supplier<SimplifyDriveBlock> SIMPLIFY_DRIVE_BLOCK =
            BLOCKS.register("simplify_drive",
                    () -> new SimplifyDriveBlock(DRIVE_PROPS));

    public static final Supplier<SimplifyEnergyCellBlock> SIMPLIFY_ENERGY_CELL_BLOCK =
            BLOCKS.register("simplify_energy_cell",
                    () -> new SimplifyEnergyCellBlock(MACHINE_PROPS));

    public static final Supplier<SimplifyStorageCasingBlock> SIMPLIFY_STORAGE_CASING_BLOCK =
            BLOCKS.register("simplify_storage_casing",
                    () -> new SimplifyStorageCasingBlock(CASING_PROPS));

    public static final Supplier<SimplifyStorageInterfaceBlock> SIMPLIFY_STORAGE_INTERFACE_BLOCK =
            BLOCKS.register("simplify_storage_interface",
                    () -> new SimplifyStorageInterfaceBlock(MACHINE_PROPS));

    public static final Supplier<SimplifyStorageNetworkInterfaceBlock> SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK =
            BLOCKS.register("simplify_storage_network_interface",
                    () -> new SimplifyStorageNetworkInterfaceBlock(MACHINE_PROPS));

    public static final Supplier<SimplifyStorageVentBlock> SIMPLIFY_STORAGE_VENT_BLOCK =
            BLOCKS.register("simplify_storage_vent",
                    () -> new SimplifyStorageVentBlock(MACHINE_PROPS));

    // ============================ L1 computation blocks ============================

    public static final Supplier<SimplifyComputationSystemBlock> SIMPLIFY_COMPUTATION_SYSTEM_BLOCK =
            BLOCKS.register("simplify_computation_system", () -> new SimplifyComputationSystemBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationDriveBlock> SIMPLIFY_COMPUTATION_DRIVE_BLOCK =
            BLOCKS.register("simplify_computation_drive", () -> new SimplifyComputationDriveBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationThreadingCoreBlock> SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK =
            BLOCKS.register("simplify_computation_threading_core", () -> new SimplifyComputationThreadingCoreBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationThreadingCoreBlock> ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK =
            BLOCKS.register("energized_computation_threading_core",
                    () -> new SimplifyComputationThreadingCoreBlock(COMPUTATION_PROPS,
                            SimplifyTier.L1_ENERGIZED_THREADING));
    public static final Supplier<SimplifyComputationParallelCoreBlock> SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK =
            BLOCKS.register("simplify_computation_parallel_core", () -> new SimplifyComputationParallelCoreBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationCoolingControllerBlock> SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK =
            BLOCKS.register("simplify_computation_cooling_controller", () -> new SimplifyComputationCoolingControllerBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationTransmitterBlock> SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK =
            BLOCKS.register("simplify_computation_transmitter", () -> new SimplifyComputationTransmitterBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyComputationInterfaceBlock> SIMPLIFY_COMPUTATION_INTERFACE_BLOCK =
            BLOCKS.register("simplify_computation_interface", () -> new SimplifyComputationInterfaceBlock(COMPUTATION_PROPS));
     public static final Supplier<SimplifyComputationNetworkInterfaceBlock> SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK =
             BLOCKS.register("simplify_computation_network_interface",
                     () -> new SimplifyComputationNetworkInterfaceBlock(COMPUTATION_PROPS));
    public static final Supplier<cn.dancingsnow.neoecoae.blocks.ECOMachineCasing> SIMPLIFY_COMPUTATION_CASING_BLOCK =
            BLOCKS.register("simplify_computation_casing", () -> new cn.dancingsnow.neoecoae.blocks.ECOMachineCasing(COMPUTATION_PROPS));
     public static final Supplier<SimplifyCasingBlock> SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK =
             BLOCKS.register("simplify_green_aluminum_casing", () -> new SimplifyCasingBlock(GREEN_CASING_PROPS));

    // ============================ L1 crafting blocks ============================

    public static final Supplier<SimplifyCraftingSystemBlock> SIMPLIFY_CRAFTING_SYSTEM_BLOCK =
            BLOCKS.register("simplify_crafting_system", () -> new SimplifyCraftingSystemBlock(COMPUTATION_PROPS));
     public static final Supplier<SimplifyStonecuttingAssemblerBlock> SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK = BLOCKS.register("simplify_stonecutting_assembler", () -> new SimplifyStonecuttingAssemblerBlock(AE2_MACHINE_PROPS));
     public static final Supplier<SimplifyPatternProviderBlock> SIMPLIFY_PATTERN_PROVIDER_BLOCK = BLOCKS.register("simplify_pattern_provider", SimplifyPatternProviderBlock::new);
      public static final Supplier<SimplifyPoweredMEInterfaceBlock> SIMPLIFY_POWERED_ME_INTERFACE_BLOCK = BLOCKS.register("simplify_powered_me_interface", SimplifyPoweredMEInterfaceBlock::new);
       public static final Supplier<SimplifySuperconductiveInterfaceBlock> SUPERCONDUCTIVE_INTERFACE_BLOCK = BLOCKS.register("superconductive_interface", SimplifySuperconductiveInterfaceBlock::new);
    public static final Supplier<SimplifyComputationParallelCoreBlock> ENERGIZED_COMPUTATION_CORE_BLOCK =
            BLOCKS.register("energized_computation_core",
                    () -> new SimplifyEnergizedComputationCoreBlock(COMPUTATION_PROPS,
                            SimplifyTier.L1_PARALLEL_SWITCH));
    public static final Supplier<SimplifyCraftingPatternBusBlock> SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK =
            BLOCKS.register("simplify_crafting_pattern_bus", () -> new SimplifyCraftingPatternBusBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingWorkerBlock> SIMPLIFY_CRAFTING_WORKER_BLOCK =
            BLOCKS.register("simplify_crafting_worker", () -> new SimplifyCraftingWorkerBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingParallelCoreBlock> SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK =
            BLOCKS.register("simplify_crafting_parallel_core", () -> new SimplifyCraftingParallelCoreBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingVentBlock> SIMPLIFY_CRAFTING_VENT_BLOCK =
            BLOCKS.register("simplify_crafting_vent", () -> new SimplifyCraftingVentBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingInterfaceBlock> SIMPLIFY_CRAFTING_INTERFACE_BLOCK =
            BLOCKS.register("simplify_crafting_interface", () -> new SimplifyCraftingInterfaceBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingNetworkInterfaceBlock> SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK =
            BLOCKS.register("simplify_crafting_network_interface",
                    () -> new SimplifyCraftingNetworkInterfaceBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyCraftingCasingBlock> SIMPLIFY_CRAFTING_CASING_BLOCK =
            BLOCKS.register("simplify_crafting_casing", () -> new SimplifyCraftingCasingBlock(CASING_PROPS));
    public static final Supplier<SimplifyFluidInputHatchBlock> SIMPLIFY_FLUID_INPUT_HATCH_BLOCK =
            BLOCKS.register("simplify_fluid_input_hatch", () -> new SimplifyFluidInputHatchBlock(COMPUTATION_PROPS));
    public static final Supplier<SimplifyFluidOutputHatchBlock> SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK =
            BLOCKS.register("simplify_fluid_output_hatch", () -> new SimplifyFluidOutputHatchBlock(COMPUTATION_PROPS));

    // ============================ Trinity controller ============================

    public static final Supplier<SimplifyTrinityControllerBlock> SIMPLIFY_TRINITY_CONTROLLER_BLOCK =
            BLOCKS.register("simplify_trinity_controller", () -> new SimplifyTrinityControllerBlock(MACHINE_PROPS));

    // Trinity's dedicated modules. They exist because eco's stock parts bind themselves to their
    // own subsystem cluster in their constructors and can never be adopted by Trinity.
    public static final Supplier<SimplifyTrinityStorageModuleBlock> SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK =
            BLOCKS.register("simplify_trinity_storage_module",
                    () -> new SimplifyTrinityStorageModuleBlock(MACHINE_PROPS));
    public static final Supplier<SimplifyTrinityComputationModuleBlock> SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK =
            BLOCKS.register("simplify_trinity_computation_module",
                    () -> new SimplifyTrinityComputationModuleBlock(MACHINE_PROPS));
    public static final Supplier<SimplifyTrinityCraftingModuleBlock> SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK =
            BLOCKS.register("simplify_trinity_crafting_module",
                    () -> new SimplifyTrinityCraftingModuleBlock(MACHINE_PROPS));

    // ============================ Block items ============================

    public static final Supplier<BlockItem> SIMPLIFY_TRINITY_CONTROLLER_ITEM =
            ITEMS.register("simplify_trinity_controller",
                    () -> new BlockItem(SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get(),
                            coloredName(SIMPLIFY_TRINITY_CONTROLLER_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_TRINITY_STORAGE_MODULE_ITEM =
            ITEMS.register("simplify_trinity_storage_module",
                    () -> new BlockItem(SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get(),
                            coloredName(SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_TRINITY_COMPUTATION_MODULE_ITEM =
            ITEMS.register("simplify_trinity_computation_module",
                    () -> new BlockItem(SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get(),
                            coloredName(SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM =
            ITEMS.register("simplify_trinity_crafting_module",
                    () -> new BlockItem(SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get(),
                            coloredName(SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_CONTROLLER_ITEM =
            ITEMS.register("simplify_storage_controller",
                    () -> new BlockItem(SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(),
                            coloredName(SIMPLIFY_STORAGE_CONTROLLER_BLOCK, NAME_THEME_GREEN)));

    public static final Supplier<BlockItem> SIMPLIFY_DRIVE_ITEM =
            ITEMS.register("simplify_drive",
                    () -> new BlockItem(SIMPLIFY_DRIVE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_ENERGY_CELL_ITEM =
            ITEMS.register("simplify_energy_cell",
                    () -> new BlockItem(SIMPLIFY_ENERGY_CELL_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_CASING_ITEM =
            ITEMS.register("simplify_storage_casing",
                    () -> new BlockItem(SIMPLIFY_STORAGE_CASING_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_INTERFACE_ITEM =
            ITEMS.register("simplify_storage_interface",
                    () -> new BlockItem(SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_NETWORK_INTERFACE_ITEM =
            ITEMS.register("simplify_storage_network_interface",
                    () -> new BlockItem(SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_VENT_ITEM =
            ITEMS.register("simplify_storage_vent",
                    () -> new BlockItem(SIMPLIFY_STORAGE_VENT_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_SYSTEM_ITEM =
            ITEMS.register("simplify_computation_system",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(),
                            coloredName(SIMPLIFY_COMPUTATION_SYSTEM_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_DRIVE_ITEM =
            ITEMS.register("simplify_computation_drive",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_THREADING_CORE_ITEM =
            ITEMS.register("simplify_computation_threading_core",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> ENERGIZED_COMPUTATION_THREADING_CORE_ITEM =
            ITEMS.register("energized_computation_threading_core",
                    () -> new BlockItem(ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_PARALLEL_CORE_ITEM =
            ITEMS.register("simplify_computation_parallel_core",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_ITEM =
            ITEMS.register("simplify_computation_cooling_controller",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_TRANSMITTER_ITEM =
            ITEMS.register("simplify_computation_transmitter",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_INTERFACE_ITEM =
            ITEMS.register("simplify_computation_interface",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get(), new Item.Properties()));
     public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_ITEM =
             ITEMS.register("simplify_computation_network_interface",
                     () -> new BlockItem(SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_CASING_ITEM =
            ITEMS.register("simplify_computation_casing",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_CASING_BLOCK.get(), new Item.Properties()));
     public static final Supplier<BlockItem> SIMPLIFY_GREEN_ALUMINUM_CASING_ITEM =
             ITEMS.register("simplify_green_aluminum_casing",
                     () -> new BlockItem(SIMPLIFY_GREEN_ALUMINUM_CASING_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM = ITEMS.register("simplify_stonecutting_assembler", () -> new BlockItem(SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get(), coloredName(SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK, NAME_THEME_GREEN)));
     public static final Supplier<BlockItem> SIMPLIFY_PATTERN_PROVIDER_ITEM = ITEMS.register("simplify_pattern_provider", () -> new BlockItem(SIMPLIFY_PATTERN_PROVIDER_BLOCK.get(), coloredName(SIMPLIFY_PATTERN_PROVIDER_BLOCK, NAME_THEME_GREEN)));
      public static final Supplier<BlockItem> SIMPLIFY_POWERED_ME_INTERFACE_ITEM = ITEMS.register("simplify_powered_me_interface", () -> new BlockItem(SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get(), coloredName(SIMPLIFY_POWERED_ME_INTERFACE_BLOCK, NAME_THEME_GREEN)));
       public static final Supplier<BlockItem> SUPERCONDUCTIVE_INTERFACE_ITEM = ITEMS.register("superconductive_interface", () -> new BlockItem(SUPERCONDUCTIVE_INTERFACE_BLOCK.get(), coloredName(SUPERCONDUCTIVE_INTERFACE_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> ENERGIZED_COMPUTATION_CORE_ITEM =
            ITEMS.register("energized_computation_core",
                    () -> new BlockItem(ENERGIZED_COMPUTATION_CORE_BLOCK.get(),
                            coloredName(ENERGIZED_COMPUTATION_CORE_BLOCK, NAME_THEME_GREEN)));
     public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_SYSTEM_ITEM =
            ITEMS.register("simplify_crafting_system",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(),
                            coloredName(SIMPLIFY_CRAFTING_SYSTEM_BLOCK, NAME_THEME_GREEN)));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_PATTERN_BUS_ITEM =
            ITEMS.register("simplify_crafting_pattern_bus",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_WORKER_ITEM =
            ITEMS.register("simplify_crafting_worker",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_WORKER_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_PARALLEL_CORE_ITEM =
            ITEMS.register("simplify_crafting_parallel_core",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_VENT_ITEM =
            ITEMS.register("simplify_crafting_vent",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_VENT_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_INTERFACE_ITEM =
            ITEMS.register("simplify_crafting_interface",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_NETWORK_INTERFACE_ITEM =
            ITEMS.register("simplify_crafting_network_interface",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_CASING_ITEM =
            ITEMS.register("simplify_crafting_casing",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_CASING_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_FLUID_INPUT_HATCH_ITEM =
            ITEMS.register("simplify_fluid_input_hatch",
                    () -> new BlockItem(SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_FLUID_OUTPUT_HATCH_ITEM =
            ITEMS.register("simplify_fluid_output_hatch",
                    () -> new BlockItem(SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get(), new Item.Properties()));

     public static final Supplier<Item> SIMPLIFY_GREEN_CRYSTAL_MATRIX =
             ITEMS.register("simplify_green_crystal_matrix",
                     () -> new Item(coloredItemName("simplify_green_crystal_matrix", NAME_THEME_GREEN)));

     // ============================ Storage cells (L1) ============================

     public static final Supplier<Item> SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_concrete_storage_matrix_housing", () -> new Item(new Item.Properties()));
     public static final Supplier<SimplifyConcreteStorageCellItem> SIMPLIFY_CONCRETE_STORAGE_CELL =
             ITEMS.register("simplify_concrete_storage_cell",
                     () -> new SimplifyConcreteStorageCellItem(new Item.Properties().stacksTo(1)));

    public static final Supplier<Item> SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_item_storage_matrix_housing", () -> new Item(new Item.Properties()));
     public static final Supplier<Item> SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_fluid_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> PIGCAT_STORAGE_MATRIX_HOUSING =
            ITEMS.register("pigcat_storage_matrix_housing",
                    () -> new PigcatStorageMatrixHousingItem(pigcatHousingProperties()));
     public static final Supplier<SimplifyStorageCellItem> PIGCAT_STORAGE_CELL =
             ITEMS.register("pigcat_storage_cell",
                     () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                             SimplifyStorageCellItem.BYTES_2K5, SimplifyStorageCellItem.BYTES_2K5_PER_TYPE));

    /**
     * 3-type L1 long-capacity item cell. Upgrade through AE2's cell-upgrade recipe.
     * Registered only with MegaCells present (needs eco's mega_item cell type); null otherwise.
     */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_CELL;
    /** 10-type expansion target for {@link #SIMPLIFY_SMALL_BULK_CELL}; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_CELL_EXPANDED;
    /** Expansion card consumed by the small bulk upgrade recipe; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_EXPANSION_CARD;
    /** Housing for the L1 small-bulk item matrix; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_STORAGE_MATRIX_HOUSING;
    /** Housing for the L1 small-bulk fluid matrix; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_FLUID_STORAGE_MATRIX_HOUSING;
    /** Housing for the L1 small-bulk chemical matrix; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_CHEMICAL_STORAGE_MATRIX_HOUSING;

    /**
     * 3-type L1 long-capacity fluid cell. Upgrade through AE2's cell-upgrade recipe.
     * Registered only with MegaCells present (needs eco's mega_fluid cell type); null otherwise.
     */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_FLUID_CELL;
    /** 10-type expansion target for {@link #SIMPLIFY_SMALL_BULK_FLUID_CELL}; null without MegaCells. */
    public static Supplier<? extends Item> SIMPLIFY_SMALL_BULK_FLUID_CELL_EXPANDED;

    // The small-bulk family needs the mega cell types and the long-bulk storage backend
    // from neoecoae's MEGA Cells integration; without MegaCells the whole family stays
    // unregistered instead of failing startup.
    static {
        if (ModList.get().isLoaded("megacells")) {
            SIMPLIFY_SMALL_BULK_CELL = ITEMS.register("simplify_small_bulk_storage_cell",
                    () -> new SimplifySmallBulkStorageCellItem(new Item.Properties().stacksTo(1),
                            SimplifyStorageCellItem::getMegaItemCellType, 3));
            SIMPLIFY_SMALL_BULK_CELL_EXPANDED = ITEMS.register("simplify_small_bulk_storage_cell_expanded",
                    () -> new SimplifySmallBulkStorageCellItem(new Item.Properties().stacksTo(1),
                            SimplifyStorageCellItem::getMegaItemCellType, 10));
            SIMPLIFY_SMALL_BULK_FLUID_CELL = ITEMS.register("simplify_small_bulk_fluid_storage_cell",
                    () -> new SimplifySmallBulkFluidStorageCellItem(new Item.Properties().stacksTo(1),
                            SimplifySmallBulkFluidStorageCellItem::getMegaFluidCellType, 3));
            SIMPLIFY_SMALL_BULK_FLUID_CELL_EXPANDED =
                    ITEMS.register("simplify_small_bulk_fluid_storage_cell_expanded",
                            () -> new SimplifySmallBulkFluidStorageCellItem(new Item.Properties().stacksTo(1),
                                    SimplifySmallBulkFluidStorageCellItem::getMegaFluidCellType, 10));
            SIMPLIFY_SMALL_BULK_EXPANSION_CARD =
                    ITEMS.register("simplify_small_bulk_expansion_card", () -> new Item(new Item.Properties()));
            SIMPLIFY_SMALL_BULK_STORAGE_MATRIX_HOUSING =
                    ITEMS.register("simplify_small_bulk_storage_matrix_housing", () -> new Item(new Item.Properties()));
            SIMPLIFY_SMALL_BULK_FLUID_STORAGE_MATRIX_HOUSING =
                    ITEMS.register("simplify_small_bulk_fluid_storage_matrix_housing", () -> new Item(new Item.Properties()));
            SIMPLIFY_SMALL_BULK_CHEMICAL_STORAGE_MATRIX_HOUSING =
                    ITEMS.register("simplify_small_bulk_chemical_storage_matrix_housing", () -> new Item(new Item.Properties()));
        }
    }

    /** reliqwq's fumo plushie, textured after the owner's player skin. */
    public static final Supplier<FumoBlock> FUMO_BLOCK =
            BLOCKS.register("fumo_reliqwq", () -> new FumoBlock(net.minecraft.world.level.block.state.BlockBehaviour.Properties
                    .of().strength(0.8f).sound(net.minecraft.world.level.block.SoundType.WOOL).noOcclusion()
                    .lightLevel(state -> 15)));
    public static final Supplier<FumoItem> FUMO_RELIQWQ_ITEM =
            ITEMS.register("fumo_reliqwq", () -> new FumoItem(FUMO_BLOCK.get(), new Item.Properties().stacksTo(1)));
    public static final Supplier<BlockEntityType<FumoBlockEntity>> FUMO_BE =
            BLOCK_ENTITIES.register("fumo_reliqwq", () -> BlockEntityType.Builder
                    .of(FumoBlockEntity::new, FUMO_BLOCK.get()).build(null));

    public static final Supplier<Item> SIMPLIFY_STORAGE_COMPONENT_1M =
             ITEMS.register("simplify_storage_component_1m", () -> new Item(new Item.Properties()));
     public static final Supplier<Item> SIMPLIFY_STORAGE_COMPONENT_4M =
             ITEMS.register("simplify_storage_component_4m", () -> new Item(new Item.Properties()));

     public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_ITEM_CELL_1K =
             ITEMS.register("simplify_item_storage_cell_1k",
                     () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                             SimplifyStorageCellItem.BYTES_1K, 1 << 2));

     public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_ITEM_CELL_16K =
             ITEMS.register("simplify_item_storage_cell_16k",
                     () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                             SimplifyStorageCellItem.BYTES_16K, 1 << 6));

     public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_ITEM_CELL_64K =
            ITEMS.register("simplify_item_storage_cell_64k",
                    () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                            SimplifyStorageCellItem.BYTES_64K, 1 << 8));

    public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_ITEM_CELL_1M =
            ITEMS.register("simplify_item_storage_cell_1m",
                    () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                            SimplifyStorageCellItem.BYTES_1M, 1 << 12));

    public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_ITEM_CELL_4M =
            ITEMS.register("simplify_item_storage_cell_4m",
                    () -> storageCell(AEKeyType.items(), SimplifyStorageCellItem::getItemCellType,
                            SimplifyStorageCellItem.BYTES_4M, 1 << 14));

    public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_FLUID_CELL_1K =
             ITEMS.register("simplify_fluid_storage_cell_1k",
                     () -> storageCell(AEKeyType.fluids(), SimplifyStorageCellItem::getFluidCellType,
                             SimplifyStorageCellItem.BYTES_1K, 1 << 2));

     public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_FLUID_CELL_16K =
             ITEMS.register("simplify_fluid_storage_cell_16k",
                     () -> storageCell(AEKeyType.fluids(), SimplifyStorageCellItem::getFluidCellType,
                             SimplifyStorageCellItem.BYTES_16K, 1 << 6));

     public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_FLUID_CELL_64K =
            ITEMS.register("simplify_fluid_storage_cell_64k",
                    () -> storageCell(AEKeyType.fluids(), SimplifyStorageCellItem::getFluidCellType,
                            SimplifyStorageCellItem.BYTES_64K, 1 << 8));

    public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_FLUID_CELL_1M =
            ITEMS.register("simplify_fluid_storage_cell_1m",
                    () -> storageCell(AEKeyType.fluids(), SimplifyStorageCellItem::getFluidCellType,
                            SimplifyStorageCellItem.BYTES_1M, 1 << 12));

    public static final Supplier<SimplifyStorageCellItem> SIMPLIFY_FLUID_CELL_4M =
            ITEMS.register("simplify_fluid_storage_cell_4m",
                    () -> storageCell(AEKeyType.fluids(), SimplifyStorageCellItem::getFluidCellType,
                            SimplifyStorageCellItem.BYTES_4M, 1 << 14));

    /**
     * 闁绘せ鏅涢幖褔宕ュ鍛憻闁活偀鍋撻柤鐟扮湴閳ь兛鎷穉rity 闁搞儲绋掗妴鍌炴晬閸垺顏?濮?闁?缂侀硸鍋撶槐姘辨啺閸℃瑦纾板☉鎾崇Т閸╁矂鎯冮崟顖ｆ澒闁肩灏欓弫?ITEM_NAME 缂備礁瀚▎銏㈡暜?
     * 闁哄秴鍢茬槐锛勨偓鍦仧楠炲洭鏁嶅杈╃煁濞寸姴鐖奸崳椋庘偓娑欘焽閻愭洜鎷犻幋锔芥殯闁挎稑鐗婂﹢浼村捶閺夊灝顕ч悗鐟邦槸閸欏繘鏁嶆径娑氱闂佸彞鑳堕悧鏇㈠绩閻熺増鍊抽柡鍐╂构缁稒瀵煎鎰佹蕉 CUSTOM_NAME 閻熸洖妫涘ú濠囧Υ?
     */
    private static Item.Properties coloredName(Supplier<? extends Block> block, int color) {
        return coloredName(block.get().getDescriptionId(), color);
    }

    private static Item.Properties coloredName(String descriptionId, int color) {
        return new Item.Properties()
                .component(DataComponents.ITEM_NAME,
                        Component.translatable(descriptionId).withStyle(Style.EMPTY.withColor(color)));
    }

    /** 闁哄拋鍣ｉ埀顒佹皑婢у潡宕担椋庘敀闁瑰湱鍏橀崳鍛婃姜閺傘倗绐楁繛澶堝妼閸炰粙鎳涢鍥叐闁哄啳鍩栧Λ銈呪枖閺団寬鎺楀几閹邦垰娈伴棅?Supplier闁挎稑鏈€垫粌鈻旈妸銉ユ杸閻犱警鍨扮欢鐐哄几閸曨垪鍋撻悩鐢靛€抽悹鍥ㄥ灴閺侇參濡?*/
    private static Item.Properties coloredItemName(String itemPath, int color) {
        return coloredName(
                Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, itemPath)),
                color);
    }

    /**
     * 闁绘凹浜滈幐顓熷緞閺嵮嶇处鐟滄壋鏅炲▔鏌ユ晬?.6 闁衡偓鐠囨彃姣婂ù绗哄€曢濠囧Υ娴ｇ鈪抽梺顐ゅ枂閳?2 閻庡湱鍋樼紞瀣閵堝嫮闉嶉悹鐑樼箘椤洭濡存笟鈧顔筋洨閺傚灝甯ㄦ繛澶婎潟閳?
     * 濞寸鍊曢?闁衡偓婵犳埃鍋撻悢鐑樼暠 modifier ID 濞戞挸楠哥敮顐︽偋閸繂鈪冲☉鎾亾闁奸绻濇禍鎺撶瑹閹稿寒鍔€缁绢収鍠楃憰鍡涘蓟閹炬潙绲圭紒鈧悮瀵稿耿
     * 闁衡偓鐠囨彃姣婂ù绗哄€曢濠冪┍椤旂瓔鍔€闂?5.6 + 闁绘壕鏅涢宥夊春閾忚鏀?1 = 闂傚牄鍨哄姗€寮伴崜褋浠?6.6闁?
     */
    private static Item.Properties pigcatHousingProperties() {
        return new Item.Properties()
                .attributes(ItemAttributeModifiers.builder()
                        .add(Attributes.ATTACK_DAMAGE,
                                new AttributeModifier(Item.BASE_ATTACK_DAMAGE_ID, 5.6,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ATTACK_SPEED,
                                new AttributeModifier(Item.BASE_ATTACK_SPEED_ID, -2.4,
                                        AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .add(Attributes.ENTITY_INTERACTION_RANGE,
                                new AttributeModifier(
                                        ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "pigcat_reach"),
                                        2.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build())
                .component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
    }

    private static SimplifyStorageCellItem storageCell(
            AEKeyType keyType, Supplier<cn.dancingsnow.neoecoae.api.storage.ECOCellType> cellType,
            long bytes, int bytesPerType) {
        return new SimplifyStorageCellItem(
                new Item.Properties().stacksTo(1), keyType, cellType, bytes, bytesPerType, 256);
    }

    public static final Supplier<SimplifyComputationCellItem> SIMPLIFY_COMPUTATION_CELL_1M =
            ITEMS.register("simplify_computation_cell_1m",
                    () -> new SimplifyComputationCellItem(
                            coloredItemName("simplify_computation_cell_1m", NAME_THEME_GREEN).stacksTo(8)));
    public static final Supplier<SimplifyComputationCellItem> ENERGIZED_COMPUTATION_CELL_4M =
            ITEMS.register("energized_computation_cell_4m",
                    () -> new SimplifyComputationCellItem(
                            coloredItemName("energized_computation_cell_4m", NAME_THEME_GREEN).stacksTo(8),
                            SimplifyTier.L1_REINFORCED));

    // ============================ Block entity types ============================
    public static final Supplier<BlockEntityType<SimplifyTrinityControllerBlockEntity>> SIMPLIFY_TRINITY_CONTROLLER_BE =
            registerTrinityControllerBe();

    private static Supplier<BlockEntityType<SimplifyTrinityControllerBlockEntity>> registerTrinityControllerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_trinity_controller",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyTrinityControllerBlockEntity(
                                SIMPLIFY_TRINITY_CONTROLLER_BE.get(), pos, state),
                        SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get()).build(null));
    }

    public static final Supplier<BlockEntityType<SimplifyTrinityStorageModuleBlockEntity>> SIMPLIFY_TRINITY_STORAGE_MODULE_BE =
            registerTrinityStorageModuleBe();

    private static Supplier<BlockEntityType<SimplifyTrinityStorageModuleBlockEntity>> registerTrinityStorageModuleBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_trinity_storage_module",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyTrinityStorageModuleBlockEntity(
                                SIMPLIFY_TRINITY_STORAGE_MODULE_BE.get(), pos, state),
                        SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get()).build(null));
    }

    public static final Supplier<BlockEntityType<SimplifyTrinityComputationModuleBlockEntity>> SIMPLIFY_TRINITY_COMPUTATION_MODULE_BE =
            registerTrinityComputationModuleBe();

    private static Supplier<BlockEntityType<SimplifyTrinityComputationModuleBlockEntity>> registerTrinityComputationModuleBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_trinity_computation_module",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyTrinityComputationModuleBlockEntity(
                                SIMPLIFY_TRINITY_COMPUTATION_MODULE_BE.get(), pos, state),
                        SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get()).build(null));
    }

    public static final Supplier<BlockEntityType<SimplifyTrinityCraftingModuleBlockEntity>> SIMPLIFY_TRINITY_CRAFTING_MODULE_BE =
            registerTrinityCraftingModuleBe();

    private static Supplier<BlockEntityType<SimplifyTrinityCraftingModuleBlockEntity>> registerTrinityCraftingModuleBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_trinity_crafting_module",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyTrinityCraftingModuleBlockEntity(
                                SIMPLIFY_TRINITY_CRAFTING_MODULE_BE.get(), pos, state),
                        SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get()).build(null));
    }

    // The vanilla BlockEntitySupplier only passes (pos, state), so each factory
    // closes over the not-yet-assigned Supplier and resolves it lazily when a
    // block entity is actually created in the world. The registrations happen
    // inside helper methods to avoid javac's "self-reference in initializer".

    public static final Supplier<BlockEntityType<SimplifyStorageHostBlockEntity>> SIMPLIFY_STORAGE_CONTROLLER_BE =
            registerStorageControllerBe();

    public static final Supplier<BlockEntityType<SimplifyDriveBlockEntity>> SIMPLIFY_DRIVE_BE =
            registerDriveBe();

    public static final Supplier<BlockEntityType<SimplifyEnergyCellBlockEntity>> SIMPLIFY_ENERGY_CELL_BE =
            registerEnergyCellBe();

    public static final Supplier<BlockEntityType<SimplifyStorageCasingBlockEntity>> SIMPLIFY_STORAGE_CASING_BE =
            registerStorageCasingBe();

    public static final Supplier<BlockEntityType<SimplifyStorageInterfaceBlockEntity>> SIMPLIFY_STORAGE_INTERFACE_BE =
            registerStorageInterfaceBe();

    public static final Supplier<BlockEntityType<SimplifyStorageVentBlockEntity>> SIMPLIFY_STORAGE_VENT_BE =
            registerStorageVentBe();

    private static Supplier<BlockEntityType<SimplifyStorageHostBlockEntity>> registerStorageControllerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_storage_controller",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyStorageHostBlockEntity(
                                SIMPLIFY_STORAGE_CONTROLLER_BE.get(), pos, state),
                        SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<SimplifyDriveBlockEntity>> registerDriveBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_drive",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyDriveBlockEntity(
                                SIMPLIFY_DRIVE_BE.get(), pos, state),
                        SIMPLIFY_DRIVE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<SimplifyEnergyCellBlockEntity>> registerEnergyCellBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_energy_cell",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyEnergyCellBlockEntity(
                                SIMPLIFY_ENERGY_CELL_BE.get(), pos, state),
                        SIMPLIFY_ENERGY_CELL_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<SimplifyStorageCasingBlockEntity>> registerStorageCasingBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_storage_casing",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyStorageCasingBlockEntity(
                                SIMPLIFY_STORAGE_CASING_BE.get(), pos, state),
                        SIMPLIFY_STORAGE_CASING_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<SimplifyStorageInterfaceBlockEntity>> registerStorageInterfaceBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_storage_interface",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyStorageInterfaceBlockEntity(
                                SIMPLIFY_STORAGE_INTERFACE_BE.get(), pos, state),
                        SIMPLIFY_STORAGE_INTERFACE_BLOCK.get(),
                        SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get()).build(null));
    }

          private static Supplier<BlockEntityType<SimplifySuperconductiveInterfaceBlockEntity>> registerSuperconductiveInterfaceBe() {
          return (Supplier) BLOCK_ENTITIES.register("superconductive_interface", () -> BlockEntityType.Builder.of((pos, state) -> new SimplifySuperconductiveInterfaceBlockEntity(SUPERCONDUCTIVE_INTERFACE_BE.get(), pos, state), SUPERCONDUCTIVE_INTERFACE_BLOCK.get()).build(null));
      }

    private static Supplier<BlockEntityType<ECOComputationParallelCoreBlockEntity>> registerEnergizedComputationCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("energized_computation_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyEnergizedComputationCoreBlockEntity(
                                ENERGIZED_COMPUTATION_CORE_BE.get(), pos, state,
                                SimplifyTier.L1_PARALLEL_SWITCH),
                        ENERGIZED_COMPUTATION_CORE_BLOCK.get()).build(null));
    }

private static Supplier<BlockEntityType<SimplifyStorageVentBlockEntity>> registerStorageVentBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_storage_vent",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyStorageVentBlockEntity(
                                SIMPLIFY_STORAGE_VENT_BE.get(), pos, state),
                        SIMPLIFY_STORAGE_VENT_BLOCK.get()).build(null));
    }

    public static final Supplier<BlockEntityType<ECOComputationSystemBlockEntity>> SIMPLIFY_COMPUTATION_SYSTEM_BE =
            registerComputationSystemBe();
    public static final Supplier<BlockEntityType<SimplifyComputationDriveBlockEntity>> SIMPLIFY_COMPUTATION_DRIVE_BE =
            registerComputationDriveBe();
    public static final Supplier<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> SIMPLIFY_COMPUTATION_THREADING_CORE_BE =
            registerComputationThreadingCoreBe();
    public static final Supplier<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> ENERGIZED_COMPUTATION_THREADING_CORE_BE =
            registerEnergizedComputationThreadingCoreBe();
    public static final Supplier<BlockEntityType<ECOComputationParallelCoreBlockEntity>> SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE =
            registerComputationParallelCoreBe();
    public static final Supplier<BlockEntityType<ECOComputationCoolingControllerBlockEntity>> SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE =
            registerComputationCoolingControllerBe();
    public static final Supplier<BlockEntityType<ECOComputationTransmitterBlockEntity>> SIMPLIFY_COMPUTATION_TRANSMITTER_BE =
            registerComputationTransmitterBe();
    public static final Supplier<BlockEntityType<ECOMachineInterfaceBlockEntity<NEComputationCluster>>>
            SIMPLIFY_COMPUTATION_INTERFACE_BE = registerComputationInterfaceBe();
    public static final Supplier<BlockEntityType<ECOMachineCasingBlockEntity<NEComputationCluster>>>
            SIMPLIFY_COMPUTATION_CASING_BE = registerComputationCasingBe();

    public static final Supplier<BlockEntityType<SimplifyStonecuttingAssemblerBlockEntity>> SIMPLIFY_STONECUTTING_ASSEMBLER_BE = registerStonecuttingAssemblerBe();

     public static final Supplier<BlockEntityType<SimplifyPatternProviderBlockEntity>> SIMPLIFY_PATTERN_PROVIDER_BE = registerPatternProviderBe();
      public static final Supplier<BlockEntityType<SimplifyPoweredMEInterfaceBlockEntity>> SIMPLIFY_POWERED_ME_INTERFACE_BE = registerPoweredMEInterfaceBe();
       public static final Supplier<BlockEntityType<SimplifySuperconductiveInterfaceBlockEntity>> SUPERCONDUCTIVE_INTERFACE_BE = registerSuperconductiveInterfaceBe();
    public static final Supplier<BlockEntityType<ECOComputationParallelCoreBlockEntity>> ENERGIZED_COMPUTATION_CORE_BE =
            registerEnergizedComputationCoreBe();
     public static final Supplier<BlockEntityType<ECOCraftingSystemBlockEntity>> SIMPLIFY_CRAFTING_SYSTEM_BE =
            registerCraftingSystemBe();
    public static final Supplier<BlockEntityType<ECOCraftingPatternBusBlockEntity>> SIMPLIFY_CRAFTING_PATTERN_BUS_BE =
            registerCraftingPatternBusBe();
    public static final Supplier<BlockEntityType<ECOCraftingWorkerBlockEntity>> SIMPLIFY_CRAFTING_WORKER_BE =
            registerCraftingWorkerBe();
    public static final Supplier<BlockEntityType<ECOCraftingParallelCoreBlockEntity>> SIMPLIFY_CRAFTING_PARALLEL_CORE_BE =
            registerCraftingParallelCoreBe();
    public static final Supplier<BlockEntityType<ECOCraftingVentBlockEntity>> SIMPLIFY_CRAFTING_VENT_BE =
            registerCraftingVentBe();
    public static final Supplier<BlockEntityType<ECOFluidInputHatchBlockEntity>> SIMPLIFY_FLUID_INPUT_HATCH_BE =
            registerFluidInputHatchBe();
    public static final Supplier<BlockEntityType<ECOFluidOutputHatchBlockEntity>> SIMPLIFY_FLUID_OUTPUT_HATCH_BE =
            registerFluidOutputHatchBe();
    public static final Supplier<BlockEntityType<ECOMachineInterfaceBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>>>
            SIMPLIFY_CRAFTING_INTERFACE_BE = registerCraftingInterfaceBe();
    public static final Supplier<BlockEntityType<ECOMachineCasingBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>>>
            SIMPLIFY_CRAFTING_CASING_BE = registerCraftingCasingBe();

    private static Supplier<BlockEntityType<SimplifyStonecuttingAssemblerBlockEntity>> registerStonecuttingAssemblerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_stonecutting_assembler", () -> BlockEntityType.Builder.of((pos, state) -> new SimplifyStonecuttingAssemblerBlockEntity(SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(), pos, state), SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get()).build(null));
    }
     private static Supplier<BlockEntityType<SimplifyPoweredMEInterfaceBlockEntity>> registerPoweredMEInterfaceBe() {
         return (Supplier) BLOCK_ENTITIES.register("simplify_powered_me_interface", () -> BlockEntityType.Builder.of((pos, state) -> new SimplifyPoweredMEInterfaceBlockEntity(SIMPLIFY_POWERED_ME_INTERFACE_BE.get(), pos, state), SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get()).build(null));
     }

    private static Supplier<BlockEntityType<SimplifyPatternProviderBlockEntity>> registerPatternProviderBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_pattern_provider", () -> BlockEntityType.Builder.of((pos, state) -> new SimplifyPatternProviderBlockEntity(SIMPLIFY_PATTERN_PROVIDER_BE.get(), pos, state), SIMPLIFY_PATTERN_PROVIDER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingSystemBlockEntity>> registerCraftingSystemBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_system",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingSystemBlockEntity(
                                SIMPLIFY_CRAFTING_SYSTEM_BE.get(), pos, state, cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier.L1),
                        SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingPatternBusBlockEntity>> registerCraftingPatternBusBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_pattern_bus",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingPatternBusBlockEntity(
                                SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(), pos, state),
                        SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingWorkerBlockEntity>> registerCraftingWorkerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_worker",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingWorkerBlockEntity(
                                SIMPLIFY_CRAFTING_WORKER_BE.get(), pos, state),
                        SIMPLIFY_CRAFTING_WORKER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingParallelCoreBlockEntity>> registerCraftingParallelCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_parallel_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingParallelCoreBlockEntity(
                                SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get(), pos, state, cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier.L1),
                        SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingVentBlockEntity>> registerCraftingVentBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_vent",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingVentBlockEntity(
                                SIMPLIFY_CRAFTING_VENT_BE.get(), pos, state),
                        SIMPLIFY_CRAFTING_VENT_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOFluidInputHatchBlockEntity>> registerFluidInputHatchBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_fluid_input_hatch",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOFluidInputHatchBlockEntity(
                                SIMPLIFY_FLUID_INPUT_HATCH_BE.get(), pos, state),
                        SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOFluidOutputHatchBlockEntity>> registerFluidOutputHatchBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_fluid_output_hatch",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOFluidOutputHatchBlockEntity(
                                SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get(), pos, state),
                        SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOMachineInterfaceBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>>>
            registerCraftingInterfaceBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_interface",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOMachineInterfaceBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>(
                                SIMPLIFY_CRAFTING_INTERFACE_BE.get(), pos, state,
                                cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator::new),
                        SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get(),
                         SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOMachineCasingBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>>>
            registerCraftingCasingBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_casing",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOMachineCasingBlockEntity<cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster>(
                                SIMPLIFY_CRAFTING_CASING_BE.get(), pos, state,
                                cn.dancingsnow.neoecoae.multiblock.calculator.NECraftingClusterCalculator::new),
                        SIMPLIFY_CRAFTING_CASING_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationSystemBlockEntity>> registerComputationSystemBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_system",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationSystemBlockEntity(
                                SIMPLIFY_COMPUTATION_SYSTEM_BE.get(), pos, state, cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1),
                        SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<SimplifyComputationDriveBlockEntity>> registerComputationDriveBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_drive",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyComputationDriveBlockEntity(
                                SIMPLIFY_COMPUTATION_DRIVE_BE.get(), pos, state),
                        SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> registerEnergizedComputationThreadingCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("energized_computation_threading_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationThreadingCoreBlockEntity(
                                ENERGIZED_COMPUTATION_THREADING_CORE_BE.get(), pos, state,
                                SimplifyTier.L1_ENERGIZED_THREADING),
                        ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationThreadingCoreBlockEntity>> registerComputationThreadingCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_threading_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationThreadingCoreBlockEntity(
                                SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get(), pos, state,
                                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1),
                        SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationParallelCoreBlockEntity>> registerComputationParallelCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_parallel_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationParallelCoreBlockEntity(
                                SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get(), pos, state,
                                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1),
                        SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationCoolingControllerBlockEntity>> registerComputationCoolingControllerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_cooling_controller",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationCoolingControllerBlockEntity(
                                SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get(), pos, state,
                                cn.dancingsnow.neoecoprototype.api.SimplifyTier.L1),
                        SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOComputationTransmitterBlockEntity>> registerComputationTransmitterBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_transmitter",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOComputationTransmitterBlockEntity(
                                SIMPLIFY_COMPUTATION_TRANSMITTER_BE.get(), pos, state),
                        SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOMachineInterfaceBlockEntity<NEComputationCluster>>>
            registerComputationInterfaceBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_interface",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOMachineInterfaceBlockEntity<NEComputationCluster>(
                                SIMPLIFY_COMPUTATION_INTERFACE_BE.get(), pos, state,
                                NEComputationClusterCalculator::new),
                        SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get(),
                         SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOMachineCasingBlockEntity<NEComputationCluster>>>
            registerComputationCasingBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_computation_casing",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOMachineCasingBlockEntity<NEComputationCluster>(
                                SIMPLIFY_COMPUTATION_CASING_BE.get(), pos, state,
                                NEComputationClusterCalculator::new),
                        SIMPLIFY_COMPUTATION_CASING_BLOCK.get()).build(null));
    }

    // ============================ Creative tab ============================

    public static final Supplier<CreativeModeTab> SIMPLIFY_TAB =
            CREATIVE_TABS.register("tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.neoecoprototype"))
                    .icon(() -> new ItemStack(SIMPLIFY_STORAGE_CONTROLLER_ITEM.get()))
                    .displayItems((params, output) -> {
                        output.accept(SIMPLIFY_STORAGE_CONTROLLER_ITEM.get());
                        output.accept(SIMPLIFY_DRIVE_ITEM.get());
                        output.accept(SIMPLIFY_ENERGY_CELL_ITEM.get());
                        output.accept(SIMPLIFY_STORAGE_CASING_ITEM.get());
                         output.accept(SIMPLIFY_GREEN_ALUMINUM_CASING_ITEM.get());
                        output.accept(SIMPLIFY_STORAGE_INTERFACE_ITEM.get());
                        output.accept(SIMPLIFY_STORAGE_NETWORK_INTERFACE_ITEM.get());
                        output.accept(SIMPLIFY_STORAGE_VENT_ITEM.get());
                        output.accept(SIMPLIFY_ITEM_CELL_1K.get());
                         output.accept(SIMPLIFY_ITEM_CELL_16K.get());
                         output.accept(SIMPLIFY_ITEM_CELL_64K.get());
                         if (SIMPLIFY_SMALL_BULK_CELL != null) {
                             output.accept(SIMPLIFY_SMALL_BULK_CELL.get());
                             output.accept(SIMPLIFY_SMALL_BULK_CELL_EXPANDED.get());
                             output.accept(SIMPLIFY_SMALL_BULK_EXPANSION_CARD.get());
                             output.accept(SIMPLIFY_SMALL_BULK_STORAGE_MATRIX_HOUSING.get());
                             output.accept(SIMPLIFY_SMALL_BULK_FLUID_STORAGE_MATRIX_HOUSING.get());
                             output.accept(SIMPLIFY_SMALL_BULK_CHEMICAL_STORAGE_MATRIX_HOUSING.get());
                             output.accept(SIMPLIFY_SMALL_BULK_FLUID_CELL.get());
                             output.accept(SIMPLIFY_SMALL_BULK_FLUID_CELL_EXPANDED.get());
                         }
                         output.accept(FumoItem.ownedBy("reliqwq"));
                         output.accept(FumoItem.ownedBy("Yang120"));
                         output.accept(FumoItem.ownedBy("kouooki"));
                         if (OPTIONAL_SMALL_BULK_CHEMICAL_CELL != null) {
                             output.accept(OPTIONAL_SMALL_BULK_CHEMICAL_CELL.get());
                             output.accept(OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED.get());
                         }
                         output.accept(SIMPLIFY_STORAGE_COMPONENT_1M.get());
                         output.accept(SIMPLIFY_STORAGE_COMPONENT_4M.get());
                         output.accept(SIMPLIFY_ITEM_CELL_1M.get());
                         output.accept(SIMPLIFY_ITEM_CELL_4M.get());
                        output.accept(SIMPLIFY_FLUID_CELL_1K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_16K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_64K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_1M.get());
                         output.accept(SIMPLIFY_FLUID_CELL_4M.get());
                         if (OPTIONAL_CHEMICAL_CELL_1K != null) {
                             output.accept(OPTIONAL_CHEMICAL_CELL_1K.get());
                             output.accept(OPTIONAL_CHEMICAL_CELL_16K.get());
                              output.accept(OPTIONAL_CHEMICAL_CELL_1M.get());
                              output.accept(OPTIONAL_CHEMICAL_CELL_4M.get());
                         }
                         if (OPTIONAL_BEYOND_STORAGE_CELL != null) {
                              output.accept(OPTIONAL_BEYOND_STORAGE_CELL.get());
                          }
                          output.accept(SIMPLIFY_CHEMICAL_STORAGE_MATRIX_HOUSING.get());
                         if (OPTIONAL_UNIVERSAL_CELL_1K != null) {
                              output.accept(SIMPLIFY_UNIVERSAL_STORAGE_MATRIX_HOUSING.get());
                              output.accept(OPTIONAL_UNIVERSAL_CELL_1K.get());
                              output.accept(OPTIONAL_UNIVERSAL_CELL_1M.get());
                          }
                          if (OPTIONAL_QUANTUM_CELL_1K != null) {
                              output.accept(SIMPLIFY_QUANTUM_STORAGE_MATRIX_HOUSING.get());
                              output.accept(OPTIONAL_QUANTUM_CELL_1K.get());
                              output.accept(OPTIONAL_QUANTUM_CELL_1M.get());
                          }
                          output.accept(SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING.get());
                          output.accept(SIMPLIFY_CONCRETE_STORAGE_CELL.get());
                         output.accept(SIMPLIFY_GREEN_CRYSTAL_MATRIX.get());
                         output.accept(SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING.get());
                         output.accept(PIGCAT_STORAGE_MATRIX_HOUSING.get());
                         output.accept(PIGCAT_STORAGE_CELL.get());
                         output.accept(SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_COMPUTATION_SYSTEM_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_DRIVE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_THREADING_CORE_ITEM.get());
                        output.accept(ENERGIZED_COMPUTATION_THREADING_CORE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_PARALLEL_CORE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_TRANSMITTER_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_INTERFACE_ITEM.get());
                         output.accept(SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_CASING_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_CELL_1M.get());
                        output.accept(ENERGIZED_COMPUTATION_CELL_4M.get());
                         output.accept(SIMPLIFY_CRAFTING_SYSTEM_ITEM.get());
                         output.accept(SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get());
                         output.accept(SIMPLIFY_PATTERN_PROVIDER_ITEM.get());
                          output.accept(SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get());
                          output.accept(POWERED_INTERFACE_PART.get());
                          output.accept(SUPERCONDUCTIVE_INTERFACE_ITEM.get());
                          output.accept(SUPERCONDUCTIVE_INTERFACE_PART.get());
                          output.accept(CABLE_PATTERN_PROVIDER_PART.get());
                          output.accept(ENERGIZED_COMPUTATION_CORE_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_PATTERN_BUS_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_WORKER_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_PARALLEL_CORE_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_VENT_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_INTERFACE_ITEM.get());
                          output.accept(SIMPLIFY_CRAFTING_NETWORK_INTERFACE_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_CASING_ITEM.get());
                         output.accept(SIMPLIFY_FLUID_INPUT_HATCH_ITEM.get());
                         output.accept(SIMPLIFY_FLUID_OUTPUT_HATCH_ITEM.get());
                         output.accept(SIMPLIFY_TRINITY_CONTROLLER_ITEM.get());
                         output.accept(SIMPLIFY_TRINITY_STORAGE_MODULE_ITEM.get());
                         output.accept(SIMPLIFY_TRINITY_COMPUTATION_MODULE_ITEM.get());
                         output.accept(SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get());
                    })
                    .build());

    private ModRegistration() {
    }

    /**
     * Binds every block to its {@link BlockEntityType} via the AE2 block API.
     * Call once from common setup (all entries are resolvable by then).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void linkBlockEntityTypes() {
        ((AEBaseEntityBlock) SIMPLIFY_TRINITY_CONTROLLER_BLOCK.get()).setBlockEntity(
                SimplifyTrinityControllerBlockEntity.class, SIMPLIFY_TRINITY_CONTROLLER_BE.get(), null,
                (level, pos, state, blockEntity) -> SimplifyTrinityControllerBlockEntity.tick(
                        level, pos, state, (SimplifyTrinityControllerBlockEntity) blockEntity));
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_TRINITY_CONTROLLER_BE.get(),
                SIMPLIFY_TRINITY_CONTROLLER_ITEM.get().asItem());
        ((AEBaseEntityBlock) SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK.get()).setBlockEntity(
                SimplifyTrinityStorageModuleBlockEntity.class, SIMPLIFY_TRINITY_STORAGE_MODULE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK.get()).setBlockEntity(
                SimplifyTrinityComputationModuleBlockEntity.class, SIMPLIFY_TRINITY_COMPUTATION_MODULE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK.get()).setBlockEntity(
                SimplifyTrinityCraftingModuleBlockEntity.class, SIMPLIFY_TRINITY_CRAFTING_MODULE_BE.get(), null, null);

        ((AEBaseEntityBlock) SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get()).setBlockEntity(
                SimplifyStorageHostBlockEntity.class, SIMPLIFY_STORAGE_CONTROLLER_BE.get(), null,
                (level, pos, state, blockEntity) -> SimplifyStorageHostBlockEntity.tick(
                        level, pos, state, (SimplifyStorageHostBlockEntity) blockEntity));
        ((AEBaseEntityBlock) SIMPLIFY_DRIVE_BLOCK.get()).setBlockEntity(
                SimplifyDriveBlockEntity.class, SIMPLIFY_DRIVE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_ENERGY_CELL_BLOCK.get()).setBlockEntity(
                SimplifyEnergyCellBlockEntity.class, SIMPLIFY_ENERGY_CELL_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_STORAGE_CASING_BLOCK.get()).setBlockEntity(
                SimplifyStorageCasingBlockEntity.class, SIMPLIFY_STORAGE_CASING_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_STORAGE_INTERFACE_BLOCK.get()).setBlockEntity(
                SimplifyStorageInterfaceBlockEntity.class, SIMPLIFY_STORAGE_INTERFACE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK.get()).setBlockEntity(
                 SimplifyStorageInterfaceBlockEntity.class, SIMPLIFY_STORAGE_INTERFACE_BE.get(), null, null);
         ((AEBaseEntityBlock) SIMPLIFY_STORAGE_VENT_BLOCK.get()).setBlockEntity(
                SimplifyStorageVentBlockEntity.class, SIMPLIFY_STORAGE_VENT_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get()).setBlockEntity(
                ECOComputationSystemBlockEntity.class, SIMPLIFY_COMPUTATION_SYSTEM_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOComputationSystemBlockEntity) blockEntity).tick(level, pos, state));
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get()).setBlockEntity(
                SimplifyComputationDriveBlockEntity.class, SIMPLIFY_COMPUTATION_DRIVE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get()).setBlockEntity(
                ECOComputationThreadingCoreBlockEntity.class, SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get(), null, null);
        ((AEBaseEntityBlock) ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK.get()).setBlockEntity(
                ECOComputationThreadingCoreBlockEntity.class, ENERGIZED_COMPUTATION_THREADING_CORE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK.get()).setBlockEntity(
                ECOComputationParallelCoreBlockEntity.class, SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK.get()).setBlockEntity(
                ECOComputationCoolingControllerBlockEntity.class, SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK.get()).setBlockEntity(
                ECOComputationTransmitterBlockEntity.class, SIMPLIFY_COMPUTATION_TRANSMITTER_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_INTERFACE_BLOCK.get()).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_COMPUTATION_INTERFACE_BE.get(), null, null);
         ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK.get()).setBlockEntity(
                 ECOMachineInterfaceBlockEntity.class, SIMPLIFY_COMPUTATION_INTERFACE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_COMPUTATION_CASING_BLOCK.get()).setBlockEntity(
                ECOMachineCasingBlockEntity.class, SIMPLIFY_COMPUTATION_CASING_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK.get()).setBlockEntity(SimplifyStonecuttingAssemblerBlockEntity.class, SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_PATTERN_PROVIDER_BLOCK.get()).setBlockEntity(SimplifyPatternProviderBlockEntity.class, SIMPLIFY_PATTERN_PROVIDER_BE.get(), null, null);
         ((AEBaseEntityBlock) SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get()).setBlockEntity(SimplifyPoweredMEInterfaceBlockEntity.class, SIMPLIFY_POWERED_ME_INTERFACE_BE.get(), null, null);
         ((AEBaseEntityBlock) SUPERCONDUCTIVE_INTERFACE_BLOCK.get()).setBlockEntity(SimplifySuperconductiveInterfaceBlockEntity.class, SUPERCONDUCTIVE_INTERFACE_BE.get(), null, null);
        ((AEBaseEntityBlock) ENERGIZED_COMPUTATION_CORE_BLOCK.get()).setBlockEntity(
                ECOComputationParallelCoreBlockEntity.class, ENERGIZED_COMPUTATION_CORE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()).setBlockEntity(
                ECOCraftingSystemBlockEntity.class, SIMPLIFY_CRAFTING_SYSTEM_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOCraftingSystemBlockEntity) blockEntity).tick(level, pos, state));
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()).setBlockEntity(
                ECOCraftingPatternBusBlockEntity.class, SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_WORKER_BLOCK.get()).setBlockEntity(
                ECOCraftingWorkerBlockEntity.class, SIMPLIFY_CRAFTING_WORKER_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get()).setBlockEntity(
                ECOCraftingParallelCoreBlockEntity.class, SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_VENT_BLOCK.get()).setBlockEntity(
                ECOCraftingVentBlockEntity.class, SIMPLIFY_CRAFTING_VENT_BE.get(), null, null);
        ((AEBaseEntityBlock) SIMPLIFY_FLUID_INPUT_HATCH_BLOCK.get()).setBlockEntity(
                ECOFluidInputHatchBlockEntity.class, SIMPLIFY_FLUID_INPUT_HATCH_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOFluidInputHatchBlockEntity) blockEntity).tick(level, pos, state));
        ((AEBaseEntityBlock) SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK.get()).setBlockEntity(
                ECOFluidOutputHatchBlockEntity.class, SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOFluidOutputHatchBlockEntity) blockEntity).tick(level, pos, state));
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_INTERFACE_BLOCK.get()).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_CRAFTING_INTERFACE_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOMachineInterfaceBlockEntity<?>) blockEntity).tick());
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK.get()).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_CRAFTING_INTERFACE_BE.get(), null,
                 (level, pos, state, blockEntity) -> ((ECOMachineInterfaceBlockEntity<?>) blockEntity).tick());
        ((AEBaseEntityBlock) SIMPLIFY_CRAFTING_CASING_BLOCK.get()).setBlockEntity(
                ECOMachineCasingBlockEntity.class, SIMPLIFY_CRAFTING_CASING_BE.get(), null, null);

        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STORAGE_CONTROLLER_BE.get(),
                SIMPLIFY_STORAGE_CONTROLLER_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_DRIVE_BE.get(),
                SIMPLIFY_DRIVE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_ENERGY_CELL_BE.get(),
                SIMPLIFY_ENERGY_CELL_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STORAGE_CASING_BE.get(),
                SIMPLIFY_STORAGE_CASING_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STORAGE_INTERFACE_BE.get(),
                SIMPLIFY_STORAGE_INTERFACE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STORAGE_INTERFACE_BE.get(),
                 SIMPLIFY_STORAGE_NETWORK_INTERFACE_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STORAGE_VENT_BE.get(),
                SIMPLIFY_STORAGE_VENT_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_SYSTEM_BE.get(),
                SIMPLIFY_COMPUTATION_SYSTEM_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_DRIVE_BE.get(),
                SIMPLIFY_COMPUTATION_DRIVE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get(),
                SIMPLIFY_COMPUTATION_THREADING_CORE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(ENERGIZED_COMPUTATION_THREADING_CORE_BE.get(),
                ENERGIZED_COMPUTATION_THREADING_CORE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get(),
                SIMPLIFY_COMPUTATION_PARALLEL_CORE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get(),
                SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_TRANSMITTER_BE.get(),
                SIMPLIFY_COMPUTATION_TRANSMITTER_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_INTERFACE_BE.get(),
                SIMPLIFY_COMPUTATION_INTERFACE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_COMPUTATION_CASING_BE.get(),
                SIMPLIFY_COMPUTATION_CASING_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(), SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_PATTERN_PROVIDER_BE.get(), SIMPLIFY_PATTERN_PROVIDER_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_POWERED_ME_INTERFACE_BE.get(), SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(SUPERCONDUCTIVE_INTERFACE_BE.get(), SUPERCONDUCTIVE_INTERFACE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_SYSTEM_BE.get(),
                SIMPLIFY_CRAFTING_SYSTEM_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(),
                SIMPLIFY_CRAFTING_PATTERN_BUS_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_WORKER_BE.get(),
                SIMPLIFY_CRAFTING_WORKER_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get(),
                SIMPLIFY_CRAFTING_PARALLEL_CORE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_VENT_BE.get(),
                SIMPLIFY_CRAFTING_VENT_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_FLUID_INPUT_HATCH_BE.get(),
                SIMPLIFY_FLUID_INPUT_HATCH_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get(),
                SIMPLIFY_FLUID_OUTPUT_HATCH_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_INTERFACE_BE.get(),
                SIMPLIFY_CRAFTING_INTERFACE_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_CRAFTING_CASING_BE.get(),
                SIMPLIFY_CRAFTING_CASING_ITEM.get().asItem());
    }
}

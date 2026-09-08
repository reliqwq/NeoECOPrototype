package cn.dancingsnow.neoecoprototype.registration;

import appeng.api.stacks.AEKeyType;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import cn.dancingsnow.neoecoprototype.block.SimplifyCasingBlock;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineInterfaceBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationDriveBlockEntity;
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
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingCasingBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingParallelCoreBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingPatternBusBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingVentBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingWorkerBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyFluidInputHatchBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyFluidOutputHatchBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageVentBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.items.SimplifyComputationCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

@SuppressWarnings({"unchecked", "rawtypes"})
public class ModRegistration {

    // ============================ Registries ============================

    public static final DeferredRegister.Blocks BLOCKS =
            DeferredRegister.createBlocks(NeoECOPrototype.MOD_ID);

    public static final DeferredRegister<Item> ITEMS =
            DeferredRegister.create(Registries.ITEM, NeoECOPrototype.MOD_ID);

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoECOPrototype.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoECOPrototype.MOD_ID);

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

    // ============================ Block items ============================

    public static final Supplier<BlockItem> SIMPLIFY_STORAGE_CONTROLLER_ITEM =
            ITEMS.register("simplify_storage_controller",
                    () -> new BlockItem(SIMPLIFY_STORAGE_CONTROLLER_BLOCK.get(), new Item.Properties()));

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
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_SYSTEM_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_DRIVE_ITEM =
            ITEMS.register("simplify_computation_drive",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_DRIVE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> SIMPLIFY_COMPUTATION_THREADING_CORE_ITEM =
            ITEMS.register("simplify_computation_threading_core",
                    () -> new BlockItem(SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK.get(), new Item.Properties()));
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

    public static final Supplier<BlockItem> SIMPLIFY_CRAFTING_SYSTEM_ITEM =
            ITEMS.register("simplify_crafting_system",
                    () -> new BlockItem(SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get(), new Item.Properties()));
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
             ITEMS.register("simplify_green_crystal_matrix", () -> new Item(new Item.Properties()));

     // ============================ Storage cells (L1) ============================

    public static final Supplier<Item> SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_item_storage_matrix_housing", () -> new Item(new Item.Properties()));
     public static final Supplier<Item> SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_fluid_storage_matrix_housing", () -> new Item(new Item.Properties()));

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

    private static SimplifyStorageCellItem storageCell(
            AEKeyType keyType, Supplier<cn.dancingsnow.neoecoae.api.storage.ECOCellType> cellType,
            long bytes, int bytesPerType) {
        return new SimplifyStorageCellItem(
                new Item.Properties().stacksTo(1), keyType, cellType, bytes, bytesPerType, 256);
    }

    public static final Supplier<SimplifyComputationCellItem> SIMPLIFY_COMPUTATION_CELL_1M =
            ITEMS.register("simplify_computation_cell_1m",
                    () -> new SimplifyComputationCellItem(new Item.Properties().stacksTo(8)));

    // ============================ Block entity types ============================
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
                         output.accept(SIMPLIFY_STORAGE_COMPONENT_1M.get());
                         output.accept(SIMPLIFY_STORAGE_COMPONENT_4M.get());
                         output.accept(SIMPLIFY_ITEM_CELL_1M.get());
                         output.accept(SIMPLIFY_ITEM_CELL_4M.get());
                        output.accept(SIMPLIFY_FLUID_CELL_1K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_16K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_64K.get());
                         output.accept(SIMPLIFY_FLUID_CELL_1M.get());
                         output.accept(SIMPLIFY_FLUID_CELL_4M.get());
                         output.accept(SIMPLIFY_GREEN_CRYSTAL_MATRIX.get());
                         output.accept(SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING.get());
                         output.accept(SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING.get());
                        output.accept(SIMPLIFY_COMPUTATION_SYSTEM_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_DRIVE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_THREADING_CORE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_PARALLEL_CORE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_TRANSMITTER_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_INTERFACE_ITEM.get());
                         output.accept(SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_CASING_ITEM.get());
                        output.accept(SIMPLIFY_COMPUTATION_CELL_1M.get());
                         output.accept(SIMPLIFY_CRAFTING_SYSTEM_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_PATTERN_BUS_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_WORKER_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_PARALLEL_CORE_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_VENT_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_INTERFACE_ITEM.get());
                          output.accept(SIMPLIFY_CRAFTING_NETWORK_INTERFACE_ITEM.get());
                         output.accept(SIMPLIFY_CRAFTING_CASING_ITEM.get());
                         output.accept(SIMPLIFY_FLUID_INPUT_HATCH_ITEM.get());
                         output.accept(SIMPLIFY_FLUID_OUTPUT_HATCH_ITEM.get());
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
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_CRAFTING_INTERFACE_BE.get(), null, null);
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

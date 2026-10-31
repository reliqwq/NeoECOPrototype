package cn.dancingsnow.neoecoprototype.registration;

import appeng.api.stacks.AEKeyType;
import appeng.block.AEBaseEntityBlock;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.crafting.CraftingBlockEntity;
import cn.dancingsnow.neoecoprototype.block.SimplifyCasingBlock;
import cn.dancingsnow.neoecoprototype.block.SimplifyCryotheumOreBlock;
import cn.dancingsnow.neoecoprototype.block.grette.GretteBuddingBlock;
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
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
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
import cn.dancingsnow.neoecoprototype.block.crafting.DollCraftingUnitBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.DollCraftingUnitType;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingCasingBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingNetworkInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingParallelCoreBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingPatternBusBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import cn.dancingsnow.neoecoprototype.block.energy.EnergizedEnergyCellBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingVentBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingWorkerBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyStonecuttingAssemblerBlock;
import cn.dancingsnow.neoecoprototype.block.decoration.GiantDollBlock;
import cn.dancingsnow.neoecoprototype.block.decoration.NamedDollBlock;
import cn.dancingsnow.neoecoprototype.blockentity.decoration.NamedDollBlockEntity;
import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyPatternProviderBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyPoweredMEInterfaceBlock;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifySuperconductiveInterfaceBlock;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.energy.EnergizedEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyCraftingParallelCoreBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyCraftingVentBlockEntity;
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
import cn.dancingsnow.neoecoprototype.block.reactor.SimplifyReactorControllerBlock;
import cn.dancingsnow.neoecoprototype.block.reactor.SimplifyReactorFrameBlock;
import cn.dancingsnow.neoecoprototype.block.reactor.SimplifyReactorGlassBlock;
import cn.dancingsnow.neoecoprototype.blockentity.reactor.SimplifyReactorControllerBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageCasingBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageInterfaceBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageVentBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.computation.SimplifyComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.items.PigmeeStorageMatrixHousingItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyComputationCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyConcreteStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyMigrationDriveItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStructureExpansionCardItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkFluidStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.items.WirelessLinkCardItem;
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
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.levelgen.feature.Feature;
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
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_16K;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_64K;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_CHEMICAL_CELL_4M;
    public static Supplier<? extends Item> OPTIONAL_SMALL_BULK_CHEMICAL_CELL;
    public static Supplier<? extends Item> OPTIONAL_SMALL_BULK_CHEMICAL_CELL_EXPANDED;
    public static Supplier<? extends Item> OPTIONAL_BEYOND_STORAGE_CELL;
    public static Supplier<? extends Item> OPTIONAL_UNIVERSAL_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_UNIVERSAL_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_COMPLEX_UNIVERSAL_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_SPENT_WASTE_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_SPENT_WASTE_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_QUANTUM_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_QUANTUM_CELL_1M;
    /** Assigned by FeStorageIntegration only when appflux is installed. */
    public static Supplier<? extends Item> OPTIONAL_FE_CELL_HOUSING;
    public static Supplier<? extends Item> OPTIONAL_FE_CELL_1K;
    public static Supplier<? extends Item> OPTIONAL_FE_CELL_1M;
    public static Supplier<? extends Item> OPTIONAL_FE_CELL_4M;
    public static final Supplier<Item> SIMPLIFY_QUANTUM_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_quantum_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_UNIVERSAL_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_universal_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_COMPLEX_UNIVERSAL_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_complex_universal_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_SPENT_NUCLEAR_WASTE_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_spent_nuclear_waste_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> SIMPLIFY_CHEMICAL_STORAGE_MATRIX_HOUSING =
            ITEMS.register("simplify_chemical_storage_matrix_housing", () -> new Item(new Item.Properties()));

    @SuppressWarnings("unchecked")
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, NeoECOPrototype.MOD_ID);

    public static final DeferredRegister<CreativeModeTab> CREATIVE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, NeoECOPrototype.MOD_ID);
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, NeoECOPrototype.MOD_ID);
    /** The singularity cell's whole state: the tick it started growing from, and what has been drawn. */
    public static final Supplier<DataComponentType<cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank>>
            SINGULARITY_CELL_BANK = DATA_COMPONENTS.register("singularity_cell_bank",
            () -> DataComponentType.<cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank>builder()
                    .persistent(cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank.CODEC)
                    .networkSynchronized(cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.Bank.STREAM_CODEC)
                    .build());
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES =
            DeferredRegister.create(Registries.RECIPE_TYPE, NeoECOPrototype.MOD_ID);
    /** The ore feature the cryotheum worldgen JSONs name; it is the config gate, not the shape. */
    public static final DeferredRegister<Feature<?>> FEATURES =
            DeferredRegister.create(Registries.FEATURE, NeoECOPrototype.MOD_ID);
    public static final Supplier<cn.dancingsnow.neoecoprototype.worldgen.ConfigGatedOreFeature>
            CONFIG_GATED_ORE_FEATURE = FEATURES.register("configurable_ore",
            cn.dancingsnow.neoecoprototype.worldgen.ConfigGatedOreFeature::new);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, NeoECOPrototype.MOD_ID);
    /** NeoForge saves an attachment of this registry on whatever holds it, and a level is one of them. */
    public static final DeferredRegister<net.neoforged.neoforge.attachment.AttachmentType<?>> ATTACHMENT_TYPES =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.ATTACHMENT_TYPES,
                    NeoECOPrototype.MOD_ID);
    /** Wireless link pairs, filed on the dimension that owns the first endpoint. */
    public static final Supplier<net.neoforged.neoforge.attachment.AttachmentType<WirelessLinks.Data>>
            WIRELESS_LINKS = ATTACHMENT_TYPES.register("wireless_links", WirelessLinks::dataType);
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
    /** What a comet core can be taken apart into. KubeJS writes these tables; the machine is stage two. */
    public static final Supplier<RecipeType<cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe>>
            DISSOCIATION_TYPE = RECIPE_TYPES.register("dissociation",
            () -> cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe.TYPE);
    public static final Supplier<RecipeSerializer<cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe>>
            DISSOCIATION_SERIALIZER = RECIPE_SERIALIZERS.register("dissociation",
            cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe.Serializer::new);

    /** The green used by the names of this pack's own cells and matrices; measured as the only name
     *  colour in use before the migration ladder got its own four.
     */
    private static final int NAME_THEME_GREEN = 0xFF94EDAB;

    /** The migration ladder's colours, sampled off the artist's own bands: each drive wears its start tier. */
    private static final int NAME_THEME_TIER_L1 = 0xFF61D389;
    private static final int NAME_THEME_TIER_L4 = 0xFF80AEFF;
    private static final int NAME_THEME_TIER_L6 = 0xFFE57E39;
    private static final int NAME_THEME_TIER_L9 = 0xFFB379E0;

    /**
     * The CPU plushie's own name colour. Deliberately not {@link #NAME_THEME_TIER_L4}: that number means
     * "starts at L4" on the migration ladder, and a plushie CPU is not a tier rung - the letters are the
     * pack's blue, reused because the shelf needs the working doll to read apart from the plain one.
     */
    private static final int NAME_THEME_CPU_DOLL = 0xFF80AEFF;

    /** AE, not FE. eco's own top cell is 1e9, so these are 16x and 512x above it. */
    private static final double ENERGIZED_CELL_CAPACITY_16G = 16_000_000_000.0;
    private static final double ENERGIZED_CELL_CAPACITY_256G = 256_000_000_000.0;

    private static final BlockBehaviour.Properties MACHINE_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F);
    private static final BlockBehaviour.Properties COMPUTATION_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    // Keep noOcclusion(): measured on the L1 room, dropping it stops 10 faces being drawn (the duplicated
    // plane between stacked drives among them) but flips the drive to lightBlock=15, so the machine
    // interior goes black in the Overworld. eco's own drive reads the same false/1 pair we do.
    private static final BlockBehaviour.Properties DRIVE_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    private static final BlockBehaviour.Properties CASING_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
    private static final BlockBehaviour.Properties GREEN_CASING_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();
     private static final BlockBehaviour.Properties AE2_MACHINE_PROPS = BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).noOcclusion();

    // The reactor's two shell materials. The frame is drawn as a post plus per-side beams, so it must
    // not occlude; the glass is what a player looks through into the chamber.
    private static final BlockBehaviour.Properties REACTOR_FRAME_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(4.0F, 6.0F).noOcclusion();
    private static final BlockBehaviour.Properties REACTOR_GLASS_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.NONE).strength(0.6F).sound(SoundType.GLASS)
                    .noOcclusion();
    // A fresh instance on purpose: Properties.lightLevel mutates the object it is called on, and
    // MACHINE_PROPS is shared by every other machine block.
    private static final BlockBehaviour.Properties REACTOR_CORE_PROPS =
            BlockBehaviour.Properties.of().mapColor(MapColor.METAL).strength(2.0F).lightLevel(state -> 15);

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

    /** Standalone batteries: each one adds its buffer to the grid's total, no multiblock involved. */
    public static final Supplier<EnergizedEnergyCellBlock> ENERGIZED_ENERGY_CELL_16G_BLOCK =
            BLOCKS.register("energized_energy_cell_16g",
                    () -> new EnergizedEnergyCellBlock(ENERGIZED_CELL_CAPACITY_16G));
    public static final Supplier<EnergizedEnergyCellBlock> ENERGIZED_ENERGY_CELL_512G_BLOCK =
            BLOCKS.register("energized_energy_cell_512g",
                    () -> new EnergizedEnergyCellBlock(ENERGIZED_CELL_CAPACITY_256G));

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

    /**
     * eco has no ore for its 天外寒冰 - the crystal only comes out of a crafting grid - so these are the
     * world-side entry points for it. Every one of them drops eco's item, not a copy of it, and the four
     * host stones follow vanilla's naming so a player can tell where to dig.
     */
    public static final Supplier<SimplifyCryotheumOreBlock> NETHER_CRYOTHEUM_ORE_BLOCK =
            BLOCKS.register("nether_cryotheum_ore", () -> new SimplifyCryotheumOreBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_RED)
                            .instrument(net.minecraft.world.level.block.state.properties
                                    .NoteBlockInstrument.BASEDRUM)
                            .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
    public static final Supplier<SimplifyCryotheumOreBlock> END_CRYOTHEUM_ORE_BLOCK =
            BLOCKS.register("end_cryotheum_ore", () -> new SimplifyCryotheumOreBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_YELLOW)
                            .instrument(net.minecraft.world.level.block.state.properties
                                    .NoteBlockInstrument.BASEDRUM)
                            .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));
    public static final Supplier<SimplifyCryotheumOreBlock> CRYOTHEUM_ORE_BLOCK =
            BLOCKS.register("cryotheum_ore", () -> new SimplifyCryotheumOreBlock(
                    BlockBehaviour.Properties.of().mapColor(MapColor.STONE)
                            .instrument(net.minecraft.world.level.block.state.properties
                                    .NoteBlockInstrument.BASEDRUM)
                            .strength(3.0F, 3.0F).requiresCorrectToolForDrops()));

    // ============================ Grette, the meteorite's own crystal ============================
    // Four conditions of budding rock, three bud stages, a cluster, a solid block and the item they all
    // produce. The buds and the cluster are vanilla's own cluster block: it already carries the facing and
    // waterlogging properties, the per-facing shapes and the "needs something solid behind it" rule, and it
    // takes its size from the constructor, so there is nothing to gain from rewriting it.

    private static final BlockBehaviour.Properties GRETTE_BUD_PROPS = BlockBehaviour.Properties.of()
            .mapColor(MapColor.DIAMOND).noOcclusion().strength(0.5F)
            .sound(net.minecraft.world.level.block.SoundType.SMALL_AMETHYST_BUD)
            .lightLevel(state -> state.getValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING)
                    .getAxis() == net.minecraft.core.Direction.Axis.Y ? 4 : 2);

    public static final Supplier<net.minecraft.world.level.block.AmethystClusterBlock> SMALL_GRETTE_BUD =
            BLOCKS.register("small_grette_bud",
                    () -> new net.minecraft.world.level.block.AmethystClusterBlock(3.0F, 3.5F, GRETTE_BUD_PROPS));
    public static final Supplier<net.minecraft.world.level.block.AmethystClusterBlock> MEDIUM_GRETTE_BUD =
            BLOCKS.register("medium_grette_bud",
                    () -> new net.minecraft.world.level.block.AmethystClusterBlock(4.0F, 3.0F, GRETTE_BUD_PROPS));
    public static final Supplier<net.minecraft.world.level.block.AmethystClusterBlock> LARGE_GRETTE_BUD =
            BLOCKS.register("large_grette_bud",
                    () -> new net.minecraft.world.level.block.AmethystClusterBlock(5.0F, 2.0F, GRETTE_BUD_PROPS));
    public static final Supplier<net.minecraft.world.level.block.AmethystClusterBlock> GRETTE_CLUSTER =
            BLOCKS.register("grette_cluster",
                    () -> new net.minecraft.world.level.block.AmethystClusterBlock(7.0F, 1.0F, GRETTE_BUD_PROPS));

    public static final Supplier<Block> GRETTE_CRYSTAL_BLOCK =
            BLOCKS.register("grette_crystal_block", () -> new Block(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIAMOND).strength(1.5F).noOcclusion()
                    .sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                    .lightLevel(state -> 4)));

    // Declared worst-first: each condition names the one it decays into, and Java reads a static
    // initializer that mentions a later field as a forward reference even inside a lambda.
    public static final Supplier<GretteBuddingBlock> DAMAGED_BUDDING_GRETTE =
            BLOCKS.register("damaged_budding_grette",
                    () -> new GretteBuddingBlock(() -> GRETTE_CRYSTAL_BLOCK.get(), gretteGrowth()));
    public static final Supplier<GretteBuddingBlock> CHIPPED_BUDDING_GRETTE =
            BLOCKS.register("chipped_budding_grette",
                    () -> new GretteBuddingBlock(() -> DAMAGED_BUDDING_GRETTE.get(), gretteGrowth()));
    public static final Supplier<GretteBuddingBlock> FLAWED_BUDDING_GRETTE =
            BLOCKS.register("flawed_budding_grette",
                    () -> new GretteBuddingBlock(() -> CHIPPED_BUDDING_GRETTE.get(), gretteGrowth()));
    public static final Supplier<GretteBuddingBlock> FLAWLESS_BUDDING_GRETTE =
            BLOCKS.register("flawless_budding_grette",
                    () -> new GretteBuddingBlock(gretteGrowth()));

    private static GretteBuddingBlock.GretteGrowth gretteGrowth() {
        return new GretteBuddingBlock.GretteGrowth(SMALL_GRETTE_BUD.get(), MEDIUM_GRETTE_BUD.get(),
                LARGE_GRETTE_BUD.get(), GRETTE_CLUSTER.get(),
                net.minecraft.world.level.block.AmethystClusterBlock.FACING,
                net.minecraft.world.level.block.AmethystClusterBlock.WATERLOGGED);
    }

    public static final Supplier<Item> GRETTE_CRYSTAL = ITEMS.register("grette_crystal",
            () -> new Item(new Item.Properties()));
    public static final Supplier<BlockItem> SMALL_GRETTE_BUD_ITEM = ITEMS.register("small_grette_bud",
            () -> new BlockItem(SMALL_GRETTE_BUD.get(), new Item.Properties()));
    public static final Supplier<BlockItem> MEDIUM_GRETTE_BUD_ITEM = ITEMS.register("medium_grette_bud",
            () -> new BlockItem(MEDIUM_GRETTE_BUD.get(), new Item.Properties()));
    public static final Supplier<BlockItem> LARGE_GRETTE_BUD_ITEM = ITEMS.register("large_grette_bud",
            () -> new BlockItem(LARGE_GRETTE_BUD.get(), new Item.Properties()));
    public static final Supplier<BlockItem> GRETTE_CLUSTER_ITEM = ITEMS.register("grette_cluster",
            () -> new BlockItem(GRETTE_CLUSTER.get(), new Item.Properties()));
    public static final Supplier<BlockItem> GRETTE_CRYSTAL_BLOCK_ITEM = ITEMS.register("grette_crystal_block",
            () -> new BlockItem(GRETTE_CRYSTAL_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> FLAWLESS_BUDDING_GRETTE_ITEM =
            ITEMS.register("flawless_budding_grette",
                    () -> new BlockItem(FLAWLESS_BUDDING_GRETTE.get(), new Item.Properties()));

    // ============================ 升格迁移驱动 ============================

    /**
     * The ladder, one disk per rung, plus the disk that comes back out and the one that skips to the top.
     * The four paid disks differ only in which rung they cover - {@link SimplifyMigrationDriveItem.Rung} -
     * and the creative one is not "creative mode only": it is the disk that jumps any rung straight to
     * L9 and simply has no recipe yet, so nothing can reach it in survival.
     *
     * <p>All five are in the creative tab; none of them is in any recipe, so there is still no survival route
     * while their recipes are unwritten. They stack because they are spent on use, unlike the expansion
     * card.</p>
     */
    public static final Supplier<SimplifyMigrationDriveItem> MIGRATION_DRIVE_1_4 =
            ITEMS.register("migration_drive_1_4",
                    () -> new SimplifyMigrationDriveItem(
                            coloredItemName("migration_drive_1_4", NAME_THEME_TIER_L1).stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.ONE_TO_FOUR));
    public static final Supplier<SimplifyMigrationDriveItem> MIGRATION_DRIVE_4_6 =
            ITEMS.register("migration_drive_4_6",
                    () -> new SimplifyMigrationDriveItem(
                            coloredItemName("migration_drive_4_6", NAME_THEME_TIER_L4).stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.FOUR_TO_SIX));
    public static final Supplier<SimplifyMigrationDriveItem> MIGRATION_DRIVE_6_9 =
            ITEMS.register("migration_drive_6_9",
                    () -> new SimplifyMigrationDriveItem(
                            coloredItemName("migration_drive_6_9", NAME_THEME_TIER_L6).stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.SIX_TO_NINE));
    /** What the machine returns once it has taken the structure across: the drive with nothing written on it. */
    public static final Supplier<Item> MIGRATION_DRIVE_EMPTY = ITEMS.register("migration_drive_empty",
            () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<SimplifyMigrationDriveItem> MIGRATION_DRIVE_CREATIVE =
            ITEMS.register("migration_drive_creative",
                    () -> new SimplifyMigrationDriveItem(
                            coloredItemName("migration_drive_creative", NAME_THEME_TIER_L9).stacksTo(16),
                            SimplifyMigrationDriveItem.Rung.STRAIGHT_TO_NINE));

    // ============================ 解离器的材料（陨石系列） ============================

    public static final DeferredRegister<net.neoforged.neoforge.fluids.FluidType> FLUID_TYPES =
            DeferredRegister.create(net.neoforged.neoforge.registries.NeoForgeRegistries.Keys.FLUID_TYPES,
                    NeoECOPrototype.MOD_ID);
    public static final DeferredRegister<net.minecraft.world.level.material.Fluid> FLUIDS =
            DeferredRegister.create(Registries.FLUID, NeoECOPrototype.MOD_ID);

    /** What every comet carries at its middle, and the only thing a dissociation table takes as input. */
    public static final Supplier<Block> METEORITE_CORE = BLOCKS.register("meteorite_core",
            () -> new Block(BlockBehaviour.Properties.of().mapColor(MapColor.STONE).strength(4.0F, 12.0F)
                    .requiresCorrectToolForDrops()));
    public static final Supplier<BlockItem> METEORITE_CORE_ITEM = ITEMS.register("meteorite_core",
            () -> new BlockItem(METEORITE_CORE.get(), new Item.Properties()));
    /** The dust one table grinds a core into. Stacks like every other dust. */
    public static final Supplier<Item> SKY_STONE_DUST = ITEMS.register("sky_stone_dust",
            () -> new Item(new Item.Properties()));

    /** The machine that runs those tables. Standalone: it joins the ME grid, but no structure does. */
    public static final Supplier<cn.dancingsnow.neoecoprototype.block.dissociation.MeteoriteDissociatorBlock>
            METEORITE_DISSOCIATOR_BLOCK = BLOCKS.register("meteorite_dissociator",
            () -> new cn.dancingsnow.neoecoprototype.block.dissociation.MeteoriteDissociatorBlock(AE2_MACHINE_PROPS));
    public static final Supplier<BlockItem> METEORITE_DISSOCIATOR_ITEM = ITEMS.register("meteorite_dissociator",
            () -> new BlockItem(METEORITE_DISSOCIATOR_BLOCK.get(), new Item.Properties()));

    public static final Supplier<net.neoforged.neoforge.fluids.FluidType> DISSOLUTION_SOLUTION_TYPE =
            FLUID_TYPES.register("dissolution_solution",
                    () -> new net.neoforged.neoforge.fluids.FluidType(
                            net.neoforged.neoforge.fluids.FluidType.Properties.create()
                                    .supportsBoating(true)));
    /** The bath a run leaves behind: eco's coolant goes in, this comes out, so the two are different fluids. */
    public static final Supplier<net.minecraft.world.level.material.FlowingFluid> DISSOLUTION_SOLUTION =
            FLUIDS.register("dissolution_solution",
                    () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Source(dissolutionProps()));
    public static final Supplier<net.minecraft.world.level.material.FlowingFluid> FLOWING_DISSOLUTION_SOLUTION =
            FLUIDS.register("flowing_dissolution_solution",
                    () -> new net.neoforged.neoforge.fluids.BaseFlowingFluid.Flowing(dissolutionProps()));
    public static final Supplier<net.minecraft.world.level.block.LiquidBlock> DISSOLUTION_SOLUTION_BLOCK =
            BLOCKS.register("dissolution_solution",
                    () -> new net.minecraft.world.level.block.LiquidBlock(DISSOLUTION_SOLUTION.get(),
                            // .replaceable() is what lets a held block swap the fluid out, the way water
                            // does: vanilla's Blocks calls it 24 times, while Registrate never does - so
                            // eco's own cryotheum bath is not placeable-through and ours deliberately is.
                            BlockBehaviour.Properties.of().noCollission().replaceable()
                                    .strength(100.0F).noLootTable()));
    public static final Supplier<net.minecraft.world.item.BucketItem> DISSOLUTION_BUCKET =
            ITEMS.register("dissolution_bucket",
                    () -> new net.minecraft.world.item.BucketItem(DISSOLUTION_SOLUTION.get(),
                            new Item.Properties().stacksTo(1)));

    /**
     * A method rather than a field: the two halves of a fluid name each other, and a static field may not
     * reach for one declared below it. Every link is a supplier, so this runs after all four are bound.
     */
    private static net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties dissolutionProps() {
        return new net.neoforged.neoforge.fluids.BaseFlowingFluid.Properties(
                () -> DISSOLUTION_SOLUTION_TYPE.get(), () -> DISSOLUTION_SOLUTION.get(),
                () -> FLOWING_DISSOLUTION_SOLUTION.get())
                .block(() -> DISSOLUTION_SOLUTION_BLOCK.get()).bucket(() -> DISSOLUTION_BUCKET.get());
    }
    public static final Supplier<BlockItem> FLAWED_BUDDING_GRETTE_ITEM = ITEMS.register("flawed_budding_grette",
            () -> new BlockItem(FLAWED_BUDDING_GRETTE.get(), new Item.Properties()));
    public static final Supplier<BlockItem> CHIPPED_BUDDING_GRETTE_ITEM =
            ITEMS.register("chipped_budding_grette",
                    () -> new BlockItem(CHIPPED_BUDDING_GRETTE.get(), new Item.Properties()));
    public static final Supplier<BlockItem> DAMAGED_BUDDING_GRETTE_ITEM =
            ITEMS.register("damaged_budding_grette",
                    () -> new BlockItem(DAMAGED_BUDDING_GRETTE.get(), new Item.Properties()));

    /**
     * 1.3.9 shipped this family as frigit. Registering the old ids as aliases of the new ones means a
     * lookup of the old name resolves to the renamed entry - blocks already placed, stacks already in
     * inventories, and ids written into old scripts all keep working. Runs in the static initializer,
     * which is before RegisterEvent, as {@code DeferredRegister#addAlias} requires.
     */
    static {
        for (String old : new String[]{"small_frigit_bud", "medium_frigit_bud", "large_frigit_bud",
                "frigit_cluster", "frigit_crystal_block", "damaged_budding_frigit",
                "chipped_budding_frigit", "flawed_budding_frigit", "flawless_budding_frigit"}) {
            ResourceLocation from = NeoECOPrototype.id(old);
            ResourceLocation to = NeoECOPrototype.id(old.replace("frigit", "grette"));
            BLOCKS.addAlias(from, to);
            ITEMS.addAlias(from, to);
        }
        // The crystal is the one item with no block behind it.
        ITEMS.addAlias(NeoECOPrototype.id("frigit_crystal"), NeoECOPrototype.id("grette_crystal"));
    }

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

    // ============================ Superconductive reactor ============================

    /** The single block with a block entity: the 7x7x7 shell is validated against it, not registered to it. */
    public static final Supplier<SimplifyReactorControllerBlock> REACTOR_CONTROLLER_BLOCK =
            BLOCKS.register("reactor_controller", () -> new SimplifyReactorControllerBlock(MACHINE_PROPS));
    public static final Supplier<SimplifyReactorFrameBlock> REACTOR_FRAME_BLOCK =
            BLOCKS.register("reactor_frame", () -> new SimplifyReactorFrameBlock(REACTOR_FRAME_PROPS));
    public static final Supplier<SimplifyReactorGlassBlock> REACTOR_GLASS_BLOCK =
            BLOCKS.register("reactor_glass", () -> new SimplifyReactorGlassBlock(REACTOR_GLASS_PROPS));
    public static final Supplier<Block> REACTOR_COIL_BLOCK =
            BLOCKS.register("reactor_coil", () -> new Block(MACHINE_PROPS));
    public static final Supplier<Block> REACTOR_CORE_BLOCK =
            BLOCKS.register("reactor_core", () -> new Block(REACTOR_CORE_PROPS));

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

    public static final Supplier<BlockItem> REACTOR_CONTROLLER_ITEM =
            ITEMS.register("reactor_controller",
                    () -> new BlockItem(REACTOR_CONTROLLER_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> REACTOR_FRAME_ITEM =
            ITEMS.register("reactor_frame",
                    () -> new BlockItem(REACTOR_FRAME_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> REACTOR_GLASS_ITEM =
            ITEMS.register("reactor_glass",
                    () -> new BlockItem(REACTOR_GLASS_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> REACTOR_COIL_ITEM =
            ITEMS.register("reactor_coil",
                    () -> new BlockItem(REACTOR_COIL_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> REACTOR_CORE_ITEM =
            ITEMS.register("reactor_core",
                    () -> new BlockItem(REACTOR_CORE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_DRIVE_ITEM =
            ITEMS.register("simplify_drive",
                    () -> new BlockItem(SIMPLIFY_DRIVE_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> SIMPLIFY_ENERGY_CELL_ITEM =
            ITEMS.register("simplify_energy_cell",
                    () -> new BlockItem(SIMPLIFY_ENERGY_CELL_BLOCK.get(), new Item.Properties()));

    public static final Supplier<BlockItem> ENERGIZED_ENERGY_CELL_16G_ITEM =
            ITEMS.register("energized_energy_cell_16g",
                    () -> new cn.dancingsnow.neoecoprototype.items.EnergizedEnergyCellBlockItem(
                            ENERGIZED_ENERGY_CELL_16G_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> ENERGIZED_ENERGY_CELL_512G_ITEM =
            ITEMS.register("energized_energy_cell_512g",
                    () -> new cn.dancingsnow.neoecoprototype.items.EnergizedEnergyCellBlockItem(
                            ENERGIZED_ENERGY_CELL_512G_BLOCK.get(), new Item.Properties()));

    /**
     * The wireless link card's gesture, not its memory: the pairs themselves are filed on the level, so an
     * edge outlives the card that made it.
     */
    public static final Supplier<WirelessLinkCardItem> WIRELESS_LINK_CARD =
            ITEMS.register("wireless_link_card",
                    () -> new WirelessLinkCardItem(new Item.Properties().stacksTo(1)));

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
    public static final Supplier<BlockItem> NETHER_CRYOTHEUM_ORE_ITEM =
            ITEMS.register("nether_cryotheum_ore",
                    () -> new BlockItem(NETHER_CRYOTHEUM_ORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> END_CRYOTHEUM_ORE_ITEM =
            ITEMS.register("end_cryotheum_ore",
                    () -> new BlockItem(END_CRYOTHEUM_ORE_BLOCK.get(), new Item.Properties()));
    public static final Supplier<BlockItem> CRYOTHEUM_ORE_ITEM =
            ITEMS.register("cryotheum_ore",
                    () -> new BlockItem(CRYOTHEUM_ORE_BLOCK.get(), new Item.Properties()));

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

    /** Grows its own singularities and takes nothing in, so it needs no housing of another medium. */
    public static final Supplier<SimplifySingularityCellItem> SIMPLIFY_SINGULARITY_CELL =
            ITEMS.register("simplify_singularity_cell",
                    () -> new SimplifySingularityCellItem(new Item.Properties().stacksTo(1)));

    /** eco's own builder refuses to touch a formed machine, so this card is the only way to grow one. */
    public static final Supplier<SimplifyStructureExpansionCardItem> SIMPLIFY_STRUCTURE_EXPANSION_CARD =
            ITEMS.register("simplify_structure_expansion_card",
                    () -> new SimplifyStructureExpansionCardItem(new Item.Properties().stacksTo(1)));

    /** The card hands this out instead of a note when an L1 storage room cannot be paid for. */
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE =
            ITEMS.register("simplify_work_order_grow_l1_storage",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    /** The exchange pattern for an L6 storage room that cannot be paid for prints this. */
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_UPGRADE_STORAGE_L9 =
            ITEMS.register("simplify_work_order_upgrade_storage_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    /**
     * One work order per machine the expansion card can be pointed at: a ticket is the only thing an AE2
     * pattern can hold as an output, since GenericStack stores an id and an amount and nothing else. The names
     * are those machines' own display names with 扩容/升级 in front, copied from upstream wording.
     */
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L4 =
            ITEMS.register("simplify_work_order_grow_storage_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L6 =
            ITEMS.register("simplify_work_order_grow_storage_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_STORAGE_L9 =
            ITEMS.register("simplify_work_order_grow_storage_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L4 =
            ITEMS.register("simplify_work_order_grow_computation_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L6 =
            ITEMS.register("simplify_work_order_grow_computation_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L9 =
            ITEMS.register("simplify_work_order_grow_computation_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L4 =
            ITEMS.register("simplify_work_order_grow_crafting_l4",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L6 =
            ITEMS.register("simplify_work_order_grow_crafting_l6",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L9 =
            ITEMS.register("simplify_work_order_grow_crafting_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_F1_CRAFTING =
            ITEMS.register("simplify_work_order_grow_f1_crafting",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_GROW_TRINITY =
            ITEMS.register("simplify_work_order_grow_trinity",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_UPGRADE_COMPUTATION_L9 =
            ITEMS.register("simplify_work_order_upgrade_computation_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));
    public static final Supplier<Item> SIMPLIFY_WORK_ORDER_UPGRADE_CRAFTING_L9 =
            ITEMS.register("simplify_work_order_upgrade_crafting_l9",
                    () -> new Item(new Item.Properties().stacksTo(16)));

    public static final Supplier<Item> SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_item_storage_matrix_housing", () -> new Item(new Item.Properties()));
     public static final Supplier<Item> SIMPLIFY_FLUID_STORAGE_MATRIX_HOUSING =
             ITEMS.register("simplify_fluid_storage_matrix_housing", () -> new Item(new Item.Properties()));
    public static final Supplier<Item> PIGMEE_STORAGE_MATRIX_HOUSING =
            ITEMS.register("pigmee_storage_matrix_housing",
                    () -> new PigmeeStorageMatrixHousingItem(pigmeeHousingProperties()));
     public static final Supplier<SimplifyStorageCellItem> PIGMEE_STORAGE_CELL =
             ITEMS.register("pigmee_storage_cell",
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

    /**
     * What the four dolls are made of. Their face is in their texture, so nothing resolves a skin and no
     * client renderer is involved - only the shared {@link NamedDollBlockEntity}, which exists to sweep
     * for creepers. The plushie CPU ships the same model on a block of its own.
     *
     * <p>The light level is deliberate rather than a leftover: 11e4d69 put it on the plushie on purpose
     * ("the block lights its cell at level 15") and the four inherited it. No reason beyond that was
     * recorded, so treat the value as a design choice, not as a fact this file can justify.
     */
    private static net.minecraft.world.level.block.state.BlockBehaviour.Properties dollProperties() {
        return net.minecraft.world.level.block.state.BlockBehaviour.Properties.of().strength(0.8f)
                .sound(net.minecraft.world.level.block.SoundType.WOOL).noOcclusion()
                .lightLevel(state -> 15);
    }

    /** A doll's worn stats: its own armour, then whatever effects it hands out on top of the night vision. */
    private static FumoItem.DollStats dollStats(float armor, float toughness,
            FumoItem.DollEffect... extra) {
        return new FumoItem.DollStats(armor, toughness, java.util.List.of(extra), 0.0D);
    }

    /**
     * The same, for the doll that also feeds what its wearer is carrying. The stat is the shipped default
     * rate and marks the doll as the charging one; the live rate is the server config's, read on every
     * sweep, so a pack can turn the charging down or off without a new jar.
     */
    private static FumoItem.DollStats chargingDollStats(float armor, float toughness,
            FumoItem.DollEffect... extra) {
        return new FumoItem.DollStats(armor, toughness, java.util.List.of(extra),
                NeoECOPrototypeServerConfig.FUMO_CHARGE_AE_PER_TICK_DEFAULT);
    }

    /** One effect at one level - amplifier 0 is the first level, as everywhere else in vanilla. */
    private static FumoItem.DollEffect dollEffect(
            net.minecraft.core.Holder<net.minecraft.world.effect.MobEffect> effect, int amplifier) {
        return new FumoItem.DollEffect(effect, amplifier);
    }

    public static final Supplier<NamedDollBlock> FUMO_DOLL_RELIQWQ_BLOCK =
            BLOCKS.register("fumo_doll_reliqwq", () -> new NamedDollBlock(dollProperties()));
    public static final Supplier<FumoItem> FUMO_DOLL_RELIQWQ_ITEM =
            ITEMS.register("fumo_doll_reliqwq", () -> new FumoItem(FUMO_DOLL_RELIQWQ_BLOCK.get(),
                    coloredName(FUMO_DOLL_RELIQWQ_BLOCK, NAME_THEME_GREEN).stacksTo(16), dollStats(4.0F, 2.0F)));
    public static final Supplier<NamedDollBlock> FUMO_DOLL_YANG120_BLOCK =
            BLOCKS.register("fumo_doll_yang120", () -> new NamedDollBlock(dollProperties()));
    public static final Supplier<FumoItem> FUMO_DOLL_YANG120_ITEM =
            ITEMS.register("fumo_doll_yang120", () -> new FumoItem(FUMO_DOLL_YANG120_BLOCK.get(),
                    coloredName(FUMO_DOLL_YANG120_BLOCK, NAME_THEME_GREEN).stacksTo(16), dollStats(4.0F, 2.0F)));

    /**
     * The same plushie with a crafting CPU inside it. It is a second block rather than the shipped doll
     * turned into one, because {@code fumo_doll_yang120} has been in every jar since 1.3.0: a world that
     * already has one set down loads its saved {@code NamedDollBlockEntity} against a crafting-unit block,
     * and vanilla refuses that. Read in the dev client on 2026-10-24, once per chunk load:
     * {@code IllegalStateException: Invalid block entity neoecoprototype:fumo_doll ... got
     * DollCraftingUnitBlock[fumo_doll_yang120]}, thrown out of {@code BlockEntity.create}, so that cell
     * comes back with no block entity of any kind.
     */
    public static final Supplier<DollCraftingUnitBlock> FUMO_DOLL_YANG120_CPU_BLOCK =
            BLOCKS.register("fumo_doll_yang120_cpu", ModRegistration::newYang120CpuDoll);
    public static final Supplier<FumoItem> FUMO_DOLL_YANG120_CPU_ITEM =
            ITEMS.register("fumo_doll_yang120_cpu", () -> new FumoItem(FUMO_DOLL_YANG120_CPU_BLOCK.get(),
                    coloredName(FUMO_DOLL_YANG120_CPU_BLOCK, NAME_THEME_CPU_DOLL).stacksTo(16), dollStats(4.0F, 2.0F)));

    /**
     * Built in a method rather than in the field's own initializer, because the unit names the doll's item
     * and the item names this block: the unit resolves the item when AE2 asks it, which is after both are
     * registered, but a forward reference in an initializer is illegal to the compiler either way.
     */
    private static DollCraftingUnitBlock newYang120CpuDoll() {
        return new DollCraftingUnitBlock(dollProperties(), new DollCraftingUnitType(
                // The same byte arithmetic as SimplifyStorageCellItem.BYTES_1M, twelve of them.
                12L << 20, 12, () -> FUMO_DOLL_YANG120_CPU_ITEM.get(), "yang咪口算单元"));
    }

    /**
     * The plushie drawn three times her size, standing on the one block she was set down on. It is
     * decoration only - no CPU, no grid node - and it carries no model of its own: the block declares
     * itself {@link net.minecraft.world.level.block.RenderShape#INVISIBLE} and
     * {@link cn.dancingsnow.neoecoprototype.client.renderer.blockentity.GiantPlushieRenderer} draws the
     * plain doll's model scaled up, which is why the blockstate still names her model (breaking particles
     * and the fallback path read it).
     */
    public static final Supplier<GiantDollBlock> FUMO_DOLL_YANG120_GIANT_BLOCK =
            BLOCKS.register("fumo_doll_yang120_giant", () -> new GiantDollBlock(dollProperties()));
    public static final Supplier<FumoItem> FUMO_DOLL_YANG120_GIANT_ITEM =
            ITEMS.register("fumo_doll_yang120_giant", () -> new FumoItem(
                    FUMO_DOLL_YANG120_GIANT_BLOCK.get(), new Item.Properties().component(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true).stacksTo(16),
                    dollStats(4.0F, 2.0F)));
    public static final Supplier<NamedDollBlock> FUMO_DOLL_KOOOKI_BLOCK =
            BLOCKS.register("fumo_doll_kouooki", () -> new NamedDollBlock(dollProperties()));
    public static final Supplier<FumoItem> FUMO_DOLL_KOOOKI_ITEM =
            ITEMS.register("fumo_doll_kouooki", () -> new FumoItem(FUMO_DOLL_KOOOKI_BLOCK.get(),
                    coloredName(FUMO_DOLL_KOOOKI_BLOCK, NAME_THEME_GREEN).stacksTo(16), dollStats(1.0F, 5.0F)));
    public static final Supplier<NamedDollBlock> FUMO_DOLL_TEDXENON_BLOCK =
            BLOCKS.register("fumo_doll_tedxenon", () -> new NamedDollBlock(dollProperties()));
    public static final Supplier<FumoItem> FUMO_DOLL_TEDXENON_ITEM =
            ITEMS.register("fumo_doll_tedxenon", () -> new FumoItem(FUMO_DOLL_TEDXENON_BLOCK.get(),
                    coloredName(FUMO_DOLL_TEDXENON_BLOCK, NAME_THEME_GREEN).stacksTo(16),
                    chargingDollStats(6.0F, 1.0F,
                            dollEffect(net.minecraft.world.effect.MobEffects.REGENERATION, 1),
                            dollEffect(net.minecraft.world.effect.MobEffects.LUCK, 0))));

    /**
     * The plushie CPU is AE2's own crafting-unit block entity, and it has to be exactly that class: the grid
     * keys its machine table by the runtime class of the block entity, so a subclass of ours would never be
     * collected as a CPU. The factory below reads this field, which is assigned by the time the supplier is
     * consulted - that is why it is a method rather than an initializer.
     */
    public static final Supplier<BlockEntityType<CraftingBlockEntity>> FUMO_DOLL_CPU_BE =
            registerYang120CpuBe();

    private static Supplier<BlockEntityType<CraftingBlockEntity>> registerYang120CpuBe() {
        return (Supplier) BLOCK_ENTITIES.register("fumo_doll_cpu", () -> BlockEntityType.Builder.of(
                (pos, state) -> new CraftingBlockEntity(FUMO_DOLL_CPU_BE.get(), pos, state),
                FUMO_DOLL_YANG120_CPU_BLOCK.get()).build(null));
    }

    /**
     * All four dolls share one block entity: it saves nothing and exists only to sweep for creepers. The
     * giant plushie is on this list too, which is what lets one client renderer cover her - renderers are
     * registered per block entity type, not per block.
     */
    public static final Supplier<BlockEntityType<NamedDollBlockEntity>> FUMO_DOLL_BE =
            BLOCK_ENTITIES.register("fumo_doll", () -> BlockEntityType.Builder.of(NamedDollBlockEntity::new,
                    FUMO_DOLL_RELIQWQ_BLOCK.get(), FUMO_DOLL_YANG120_BLOCK.get(),
                    FUMO_DOLL_KOOOKI_BLOCK.get(), FUMO_DOLL_TEDXENON_BLOCK.get(),
                    FUMO_DOLL_YANG120_GIANT_BLOCK.get()).build(null));

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
     * A coloured default name goes on ITEM_NAME, never on CUSTOM_NAME: the first is a display
     * override every stack of the item carries, the second is an anvil rename, and only that one
     * makes the tooltip italic and the stack count as renamed. ItemStack#getHoverName reads
     * CUSTOM_NAME first, then ITEM_NAME, then the item's own description id.
     */
    private static Item.Properties coloredName(Supplier<? extends Block> block, int color) {
        return coloredName(block.get().getDescriptionId(), color);
    }

    private static Item.Properties coloredName(String descriptionId, int color) {
        return new Item.Properties()
                .component(DataComponents.ITEM_NAME,
                        Component.translatable(descriptionId).withStyle(Style.EMPTY.withColor(color)));
    }

    /** The same, given an item path in this mod's namespace, so callers do not spell out the key. */
    private static Item.Properties coloredItemName(String itemPath, int color) {
        return coloredName(
                Util.makeDescriptionId("item", ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, itemPath)),
                color);
    }

    /**
     * Held in the main hand, this housing reads as a weapon. Measured off the stack by
     * NeoECOPrototypeGameTests#pigmeeHousingReportsItsModifiersInHand: attack_damage +5.6, attack_speed
     * -2.4 and entity_interaction_range +2.0, all ADD_VALUE. The first two carry vanilla's own modifier
     * ids (base_attack_damage / base_attack_speed) and the third is ours (pigmee_reach). What reusing
     * those ids costs is not measured here - two modifiers with the same id on one holder is MC's own
     * de-duplication rule, so treat the id choice as "vanilla's lines", not as a proven no-stack result.
     */
    private static Item.Properties pigmeeHousingProperties() {
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
                                        ResourceLocation.fromNamespaceAndPath(NeoECOPrototype.MOD_ID, "pigmee_reach"),
                                        2.0, AttributeModifier.Operation.ADD_VALUE),
                                EquipmentSlotGroup.MAINHAND)
                        .build());
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

    public static final Supplier<BlockEntityType<EnergizedEnergyCellBlockEntity>> ENERGIZED_ENERGY_CELL_BE =
            registerEnergizedEnergyCellBe();

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

    @SuppressWarnings("unchecked")
    private static Supplier<BlockEntityType<EnergizedEnergyCellBlockEntity>> registerEnergizedEnergyCellBe() {
        return (Supplier) BLOCK_ENTITIES.register("energized_energy_cell",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new EnergizedEnergyCellBlockEntity(
                                ENERGIZED_ENERGY_CELL_BE.get(), pos, state),
                        ENERGIZED_ENERGY_CELL_16G_BLOCK.get(),
                        ENERGIZED_ENERGY_CELL_512G_BLOCK.get()).build(null));
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
                        (pos, state) -> new ECOComputationParallelCoreBlockEntity(
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
    public static final Supplier<BlockEntityType<SimplifyReactorControllerBlockEntity>> REACTOR_CONTROLLER_BE =
            registerReactorControllerBe();

    private static Supplier<BlockEntityType<SimplifyReactorControllerBlockEntity>> registerReactorControllerBe() {
        return (Supplier) BLOCK_ENTITIES.register("reactor_controller",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyReactorControllerBlockEntity(
                                REACTOR_CONTROLLER_BE.get(), pos, state),
                        REACTOR_CONTROLLER_BLOCK.get()).build(null));
    }
    public static final Supplier<BlockEntityType<cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity>>
            METEORITE_DISSOCIATOR_BE = registerMeteoriteDissociatorBe();
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

    private static Supplier<BlockEntityType<cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity>>
    registerMeteoriteDissociatorBe() {
        return (Supplier) BLOCK_ENTITIES.register("meteorite_dissociator",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new cn.dancingsnow.neoecoprototype.blockentity.dissociation
                                .MeteoriteDissociatorBlockEntity(METEORITE_DISSOCIATOR_BE.get(), pos, state),
                        METEORITE_DISSOCIATOR_BLOCK.get()).build(null));
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
                        (pos, state) -> new SimplifyCraftingSystemBlockEntity(
                                SIMPLIFY_CRAFTING_SYSTEM_BE.get(), pos, state, cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier.L1),
                        SIMPLIFY_CRAFTING_SYSTEM_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingPatternBusBlockEntity>> registerCraftingPatternBusBe() {
        // eco's own class again, and the shape this had before 1.4.0: the only reason an addon subclass
        // stood here was to answer allowsPatternMigrationSource() true, and 1.4.1 withdrew that.
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_pattern_bus",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new ECOCraftingPatternBusBlockEntity(
                                SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(), pos, state),
                        SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingWorkerBlockEntity>> registerCraftingWorkerBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_worker",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyCraftingWorkerBlockEntity(
                                SIMPLIFY_CRAFTING_WORKER_BE.get(), pos, state),
                        SIMPLIFY_CRAFTING_WORKER_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingParallelCoreBlockEntity>> registerCraftingParallelCoreBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_parallel_core",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyCraftingParallelCoreBlockEntity(
                                SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get(), pos, state, cn.dancingsnow.neoecoprototype.api.SimplifyCraftingTier.L1),
                        SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK.get()).build(null));
    }

    private static Supplier<BlockEntityType<ECOCraftingVentBlockEntity>> registerCraftingVentBe() {
        return (Supplier) BLOCK_ENTITIES.register("simplify_crafting_vent",
                () -> BlockEntityType.Builder.of(
                        (pos, state) -> new SimplifyCraftingVentBlockEntity(
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
                        output.accept(ENERGIZED_ENERGY_CELL_16G_ITEM.get());
                        output.accept(ENERGIZED_ENERGY_CELL_512G_ITEM.get());
                        // The wireless link card is withheld from players this release: no tab entry and no
                        // recipe, so the only way to hold one is /give. The feature works and is measured in a
                        // client, but what a link should cost a survival player is still open. Bringing it back
                        // is this line plus data/neoecoprototype/recipe/processor_assembler/
                        // wireless_link_card.json - that recipe block is kept in _removed_sources/.
                        output.accept(SIMPLIFY_STORAGE_CASING_ITEM.get());
                         output.accept(SIMPLIFY_GREEN_ALUMINUM_CASING_ITEM.get());
                        // The whole cryotheum family is out of the tab: it generates nowhere by default and
                        // no recipe names a grette or a neoecoprototype cryotheum id. The items stay
                        // registered so existing worlds keep their blocks.
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
                         output.accept(FUMO_DOLL_RELIQWQ_ITEM.get());
                         output.accept(FUMO_DOLL_YANG120_ITEM.get());
                         output.accept(FUMO_DOLL_KOOOKI_ITEM.get());
                         output.accept(FUMO_DOLL_TEDXENON_ITEM.get());
                         output.accept(FUMO_DOLL_YANG120_CPU_ITEM.get());
                         output.accept(FUMO_DOLL_YANG120_GIANT_ITEM.get());
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
                             output.accept(OPTIONAL_CHEMICAL_CELL_64K.get());
                              output.accept(OPTIONAL_CHEMICAL_CELL_1M.get());
                              output.accept(OPTIONAL_CHEMICAL_CELL_4M.get());
                         }
                         if (OPTIONAL_FE_CELL_1M != null) {
                             output.accept(OPTIONAL_FE_CELL_1K.get());
                             output.accept(OPTIONAL_FE_CELL_1M.get());
                             output.accept(OPTIONAL_FE_CELL_4M.get());
                             output.accept(OPTIONAL_FE_CELL_HOUSING.get());
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
                          if (OPTIONAL_SPENT_WASTE_CELL_1M != null) {
                              output.accept(SIMPLIFY_SPENT_NUCLEAR_WASTE_STORAGE_MATRIX_HOUSING.get());
                              output.accept(OPTIONAL_SPENT_WASTE_CELL_1K.get());
                              output.accept(OPTIONAL_SPENT_WASTE_CELL_1M.get());
                          }
                          if (OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M != null) {
                              output.accept(SIMPLIFY_COMPLEX_UNIVERSAL_STORAGE_MATRIX_HOUSING.get());
                              output.accept(OPTIONAL_COMPLEX_UNIVERSAL_CELL_1K.get());
                              output.accept(OPTIONAL_COMPLEX_UNIVERSAL_CELL_1M.get());
                          }
                          output.accept(SIMPLIFY_CONCRETE_STORAGE_MATRIX_HOUSING.get());
                          output.accept(SIMPLIFY_CONCRETE_STORAGE_CELL.get());
                          output.accept(SIMPLIFY_SINGULARITY_CELL.get());
                        output.accept(SIMPLIFY_STRUCTURE_EXPANSION_CARD.get());
                        // The whole ladder, next to the card it replaces: the four usable disks and the spent
                        // shell, so a tester can hold both ends of a rung at once. None of the five has a
                        // recipe, so this is a creative-mode route, not a survival one.
                        output.accept(MIGRATION_DRIVE_1_4.get());
                        output.accept(MIGRATION_DRIVE_4_6.get());
                        output.accept(MIGRATION_DRIVE_6_9.get());
                        output.accept(MIGRATION_DRIVE_CREATIVE.get());
                        output.accept(MIGRATION_DRIVE_EMPTY.get());
                         output.accept(SIMPLIFY_GREEN_CRYSTAL_MATRIX.get());
                         output.accept(SIMPLIFY_ITEM_STORAGE_MATRIX_HOUSING.get());
                         output.accept(PIGMEE_STORAGE_MATRIX_HOUSING.get());
                         output.accept(PIGMEE_STORAGE_CELL.get());
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
                        // The dissociator family is registered but withheld: it has no survival recipe, its
                        // input comes from the comet, and the comet generates nowhere by default. Keeping the
                        // four items out of the tab is the whole hiding - registrations, models, the
                        // dissociation table and the suite all stay, so a world that already has one still
                        // loads and still runs.
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
                         // The reactor family is registered but withheld for this release: it has no survival
                         // recipe. Keeping the five items out of the tab is the whole hiding - registrations,
                         // models, loot tables, the structure definition and the reactor_formation suite all
                         // stay, so a world that already has one still loads and still runs.
                    })
                    .build());

    private ModRegistration() {
    }

    /**
     * The one cast in the file. A bare {@code (AEBaseEntityBlock) block} would surface as a
     * ClassCastException deep inside common setup with no name attached; this names the block and says
     * what it was expected to extend.
     */
    private static AEBaseEntityBlock asEntityBlock(Supplier<? extends Block> block) {
        var value = block.get();
        if (!(value instanceof AEBaseEntityBlock)) {
            throw new IllegalStateException(value + " must extend AEBaseEntityBlock to carry a block entity");
        }
        return (AEBaseEntityBlock) value;
    }

    /**
     * Binds every block to its {@link BlockEntityType} via the AE2 block API.
     * Call once from common setup (all entries are resolvable by then).
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void linkBlockEntityTypes() {
        asEntityBlock(SIMPLIFY_TRINITY_CONTROLLER_BLOCK).setBlockEntity(
                SimplifyTrinityControllerBlockEntity.class, SIMPLIFY_TRINITY_CONTROLLER_BE.get(), null,
                (level, pos, state, blockEntity) -> SimplifyTrinityControllerBlockEntity.tick(
                        level, pos, state, (SimplifyTrinityControllerBlockEntity) blockEntity));
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_TRINITY_CONTROLLER_BE.get(),
                SIMPLIFY_TRINITY_CONTROLLER_ITEM.get().asItem());
        asEntityBlock(SIMPLIFY_TRINITY_STORAGE_MODULE_BLOCK).setBlockEntity(
                SimplifyTrinityStorageModuleBlockEntity.class, SIMPLIFY_TRINITY_STORAGE_MODULE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_TRINITY_COMPUTATION_MODULE_BLOCK).setBlockEntity(
                SimplifyTrinityComputationModuleBlockEntity.class, SIMPLIFY_TRINITY_COMPUTATION_MODULE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_TRINITY_CRAFTING_MODULE_BLOCK).setBlockEntity(
                SimplifyTrinityCraftingModuleBlockEntity.class, SIMPLIFY_TRINITY_CRAFTING_MODULE_BE.get(), null, null);

        asEntityBlock(SIMPLIFY_STORAGE_CONTROLLER_BLOCK).setBlockEntity(
                SimplifyStorageHostBlockEntity.class, SIMPLIFY_STORAGE_CONTROLLER_BE.get(), null,
                (level, pos, state, blockEntity) -> SimplifyStorageHostBlockEntity.tick(
                        level, pos, state, (SimplifyStorageHostBlockEntity) blockEntity));
        asEntityBlock(SIMPLIFY_DRIVE_BLOCK).setBlockEntity(
                SimplifyDriveBlockEntity.class, SIMPLIFY_DRIVE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_ENERGY_CELL_BLOCK).setBlockEntity(
                SimplifyEnergyCellBlockEntity.class, SIMPLIFY_ENERGY_CELL_BE.get(), null, null);
        asEntityBlock(ENERGIZED_ENERGY_CELL_16G_BLOCK).setBlockEntity(
                EnergizedEnergyCellBlockEntity.class, ENERGIZED_ENERGY_CELL_BE.get(), null, null);
        asEntityBlock(ENERGIZED_ENERGY_CELL_512G_BLOCK).setBlockEntity(
                EnergizedEnergyCellBlockEntity.class, ENERGIZED_ENERGY_CELL_BE.get(), null, null);
        AEBaseBlockEntity.registerBlockEntityItem(ENERGIZED_ENERGY_CELL_BE.get(),
                ENERGIZED_ENERGY_CELL_16G_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(ENERGIZED_ENERGY_CELL_BE.get(),
                ENERGIZED_ENERGY_CELL_512G_ITEM.get().asItem());
        asEntityBlock(SIMPLIFY_STORAGE_CASING_BLOCK).setBlockEntity(
                SimplifyStorageCasingBlockEntity.class, SIMPLIFY_STORAGE_CASING_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_STORAGE_INTERFACE_BLOCK).setBlockEntity(
                SimplifyStorageInterfaceBlockEntity.class, SIMPLIFY_STORAGE_INTERFACE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_STORAGE_NETWORK_INTERFACE_BLOCK).setBlockEntity(
                 SimplifyStorageInterfaceBlockEntity.class, SIMPLIFY_STORAGE_INTERFACE_BE.get(), null, null);
         asEntityBlock(SIMPLIFY_STORAGE_VENT_BLOCK).setBlockEntity(
                SimplifyStorageVentBlockEntity.class, SIMPLIFY_STORAGE_VENT_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_SYSTEM_BLOCK).setBlockEntity(
                ECOComputationSystemBlockEntity.class, SIMPLIFY_COMPUTATION_SYSTEM_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOComputationSystemBlockEntity) blockEntity).tick(level, pos, state));
        asEntityBlock(SIMPLIFY_COMPUTATION_DRIVE_BLOCK).setBlockEntity(
                SimplifyComputationDriveBlockEntity.class, SIMPLIFY_COMPUTATION_DRIVE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_THREADING_CORE_BLOCK).setBlockEntity(
                ECOComputationThreadingCoreBlockEntity.class, SIMPLIFY_COMPUTATION_THREADING_CORE_BE.get(), null, null);
        asEntityBlock(ENERGIZED_COMPUTATION_THREADING_CORE_BLOCK).setBlockEntity(
                ECOComputationThreadingCoreBlockEntity.class, ENERGIZED_COMPUTATION_THREADING_CORE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_PARALLEL_CORE_BLOCK).setBlockEntity(
                ECOComputationParallelCoreBlockEntity.class, SIMPLIFY_COMPUTATION_PARALLEL_CORE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BLOCK).setBlockEntity(
                ECOComputationCoolingControllerBlockEntity.class, SIMPLIFY_COMPUTATION_COOLING_CONTROLLER_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_TRANSMITTER_BLOCK).setBlockEntity(
                ECOComputationTransmitterBlockEntity.class, SIMPLIFY_COMPUTATION_TRANSMITTER_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_INTERFACE_BLOCK).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_COMPUTATION_INTERFACE_BE.get(), null, null);
         asEntityBlock(SIMPLIFY_COMPUTATION_NETWORK_INTERFACE_BLOCK).setBlockEntity(
                 ECOMachineInterfaceBlockEntity.class, SIMPLIFY_COMPUTATION_INTERFACE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_COMPUTATION_CASING_BLOCK).setBlockEntity(
                ECOMachineCasingBlockEntity.class, SIMPLIFY_COMPUTATION_CASING_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_STONECUTTING_ASSEMBLER_BLOCK).setBlockEntity(SimplifyStonecuttingAssemblerBlockEntity.class, SIMPLIFY_STONECUTTING_ASSEMBLER_BE.get(), null, null);
        ((AEBaseEntityBlock<cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity>)
                METEORITE_DISSOCIATOR_BLOCK.get()).setBlockEntity(
                cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity.class,
                METEORITE_DISSOCIATOR_BE.get(), null,
                cn.dancingsnow.neoecoprototype.blockentity.dissociation.MeteoriteDissociatorBlockEntity::serverTick);
        asEntityBlock(SIMPLIFY_PATTERN_PROVIDER_BLOCK).setBlockEntity(SimplifyPatternProviderBlockEntity.class, SIMPLIFY_PATTERN_PROVIDER_BE.get(), null, null);
         asEntityBlock(SIMPLIFY_POWERED_ME_INTERFACE_BLOCK).setBlockEntity(SimplifyPoweredMEInterfaceBlockEntity.class, SIMPLIFY_POWERED_ME_INTERFACE_BE.get(), null, null);
         asEntityBlock(SUPERCONDUCTIVE_INTERFACE_BLOCK).setBlockEntity(SimplifySuperconductiveInterfaceBlockEntity.class, SUPERCONDUCTIVE_INTERFACE_BE.get(), null, null);
        asEntityBlock(REACTOR_CONTROLLER_BLOCK).setBlockEntity(
                SimplifyReactorControllerBlockEntity.class, REACTOR_CONTROLLER_BE.get(), null,
                (level, pos, state, blockEntity) -> SimplifyReactorControllerBlockEntity.serverTick(
                        level, pos, state, (SimplifyReactorControllerBlockEntity) blockEntity));
        // The plushie CPU keeps sweeping for creepers on AE2's own block entity, and takes its CPU-list
        // name from the same tick, because the block entity is not ours to give a constructor hook.
        ((AEBaseEntityBlock<CraftingBlockEntity>) FUMO_DOLL_YANG120_CPU_BLOCK.get()).setBlockEntity(
                CraftingBlockEntity.class, FUMO_DOLL_CPU_BE.get(), null, DollCraftingUnitBlock::tick);
        asEntityBlock(ENERGIZED_COMPUTATION_CORE_BLOCK).setBlockEntity(
                ECOComputationParallelCoreBlockEntity.class, ENERGIZED_COMPUTATION_CORE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_CRAFTING_SYSTEM_BLOCK).setBlockEntity(
                ECOCraftingSystemBlockEntity.class, SIMPLIFY_CRAFTING_SYSTEM_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOCraftingSystemBlockEntity) blockEntity).tick(level, pos, state));
        asEntityBlock(SIMPLIFY_CRAFTING_PATTERN_BUS_BLOCK).setBlockEntity(
                ECOCraftingPatternBusBlockEntity.class, SIMPLIFY_CRAFTING_PATTERN_BUS_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_CRAFTING_WORKER_BLOCK).setBlockEntity(
                ECOCraftingWorkerBlockEntity.class, SIMPLIFY_CRAFTING_WORKER_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_CRAFTING_PARALLEL_CORE_BLOCK).setBlockEntity(
                ECOCraftingParallelCoreBlockEntity.class, SIMPLIFY_CRAFTING_PARALLEL_CORE_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_CRAFTING_VENT_BLOCK).setBlockEntity(
                ECOCraftingVentBlockEntity.class, SIMPLIFY_CRAFTING_VENT_BE.get(), null, null);
        asEntityBlock(SIMPLIFY_FLUID_INPUT_HATCH_BLOCK).setBlockEntity(
                ECOFluidInputHatchBlockEntity.class, SIMPLIFY_FLUID_INPUT_HATCH_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOFluidInputHatchBlockEntity) blockEntity).tick(level, pos, state));
        asEntityBlock(SIMPLIFY_FLUID_OUTPUT_HATCH_BLOCK).setBlockEntity(
                ECOFluidOutputHatchBlockEntity.class, SIMPLIFY_FLUID_OUTPUT_HATCH_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOFluidOutputHatchBlockEntity) blockEntity).tick(level, pos, state));
        asEntityBlock(SIMPLIFY_CRAFTING_INTERFACE_BLOCK).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_CRAFTING_INTERFACE_BE.get(), null,
                (level, pos, state, blockEntity) -> ((ECOMachineInterfaceBlockEntity<?>) blockEntity).tick());
        asEntityBlock(SIMPLIFY_CRAFTING_NETWORK_INTERFACE_BLOCK).setBlockEntity(
                ECOMachineInterfaceBlockEntity.class, SIMPLIFY_CRAFTING_INTERFACE_BE.get(), null,
                 (level, pos, state, blockEntity) -> ((ECOMachineInterfaceBlockEntity<?>) blockEntity).tick());
        asEntityBlock(SIMPLIFY_CRAFTING_CASING_BLOCK).setBlockEntity(
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
        AEBaseBlockEntity.registerBlockEntityItem(METEORITE_DISSOCIATOR_BE.get(),
                METEORITE_DISSOCIATOR_ITEM.get().asItem());
        AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_PATTERN_PROVIDER_BE.get(), SIMPLIFY_PATTERN_PROVIDER_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(SIMPLIFY_POWERED_ME_INTERFACE_BE.get(), SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(SUPERCONDUCTIVE_INTERFACE_BE.get(), SUPERCONDUCTIVE_INTERFACE_ITEM.get().asItem());
         AEBaseBlockEntity.registerBlockEntityItem(REACTOR_CONTROLLER_BE.get(), REACTOR_CONTROLLER_ITEM.get().asItem());
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

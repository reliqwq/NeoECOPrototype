package cn.dancingsnow.neoecoprototype.integration.kubejs;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoprototype.api.MatrixMaterials;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import me.ramidzkh.mekae2.ae2.MekanismKey;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.ChemicalStack;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry;
import cn.dancingsnow.neoecoprototype.integration.mekanism.SimplifyChemicalStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellItem;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;

import java.util.function.Consumer;
import java.util.function.Supplier;

/** Generic KubeJS storage-matrix builder for item, fluid, and AppMek chemical cells. */
public final class StorageMatrixBuilder extends ItemBuilder {
    /** eco's OmniCells marker for "no type limit". */
    private static final int UNLIMITED_OMNI_TYPES = -1;
    private static final double OMNI_IDLE_DRAIN = 8.0D;

    private StorageMatrixDefinition.MatrixType type = StorageMatrixDefinition.MatrixType.ITEM;
    private StorageMatrixDefinition.MatrixSize size = StorageMatrixDefinition.MatrixSize.INFINITE;
    private Supplier<AEKey> key;
    private ResourceLocation boundChemical;
    private ResourceLocation inventoryModel;
    private ResourceLocation driveModel;
    private MatrixMaterials.Kind material;
    private Long customBytes;
    private Integer customTotalTypes;
    private Consumer<ModelGenerator> defaultModelGenerator;

    public StorageMatrixBuilder(ResourceLocation id) {
        super(id);
        // null means "not set by the script": resolved through MatrixMaterials below.
        this.inventoryModel = null;
        this.driveModel = null;
        this.material = null;
        // KubeJS appends layer0=kubejs:item/<id>, a texture that does not exist, unless the model
        // generator short-circuits. Installing a default generator mirrors the infinite builder.
        this.defaultModelGenerator = mg -> mg.parent(inventoryModelOrDefault());
        this.modelGenerator = this.defaultModelGenerator;
    }

    /**
     * Select the matrix material explicitly.
     *
     * <p>Normally the material follows the bound kind, so an item matrix always renders with the
     * item housing and an item drive slot. Use this only to deliberately opt into another family's
     * material (for example {@code other}).
     */
    @Info("设置矩阵材质：item、fluid、chemical、infinite 或 other")
    public StorageMatrixBuilder material(String value) {
        this.material = MatrixMaterials.parse(value);
        return this;
    }

    /** The matrix's material: an explicit override, else the palette of its kind. */
    public MatrixMaterials.Kind resolvedMaterial() {
        return material != null ? material : MatrixMaterials.of(type, size);
    }

    private ResourceLocation inventoryModelOrDefault() {
        return inventoryModel != null ? inventoryModel : resolvedMaterial().inventoryModel();
    }

    private ResourceLocation driveModelOrDefault() {
        return driveModel != null ? driveModel : resolvedMaterial().driveModel();
    }

    /**
     * Set the matrix kind.
     *
     * <p>Storage kinds ({@code item}/{@code fluid}/{@code chemical}) decide both the backend and the
     * material, so the housing and the in-drive model follow automatically. Family names
     * ({@code pigcat}/{@code universal}/{@code quantum}/{@code small_bulk}/{@code other}) only pick a
     * material, which is the same as calling {@link #material(String)}.
     */
    @Info("设置矩阵类型：item/fluid/chemical，或家族名 pigcat/universal/quantum/small_bulk/other")
    public StorageMatrixBuilder type(String value) {
        try {
            this.type = StorageMatrixDefinition.MatrixType.valueOf(value.toUpperCase(java.util.Locale.ROOT));
            return this;
        } catch (IllegalArgumentException ignored) {
            // Not a storage kind; fall through and treat it as a family material.
        }
        this.material = MatrixMaterials.parse(value);
        return this;
    }

    @Info("绑定一个物品")
    public StorageMatrixBuilder itemType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.ITEM;
        this.key = () -> AEItemKey.of(BuiltInRegistries.ITEM.get(id));
        return this;
    }

    @Info("绑定一个流体")
    public StorageMatrixBuilder fluidType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.FLUID;
        this.key = () -> AEFluidKey.of(BuiltInRegistries.FLUID.get(id));
        return this;
    }

    @Info("绑定一个化学品资源 ID；需要 Mekanism 与 Applied Mekanistics")
    public StorageMatrixBuilder chemicalType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.CHEMICAL;
        this.boundChemical = id;
        this.key = () -> {
            var chemical = MekanismAPI.CHEMICAL_REGISTRY.get(id);
            if (chemical == null || chemical.isEmptyType()) {
                throw new IllegalStateException("Unknown Mekanism chemical: " + id);
            }
            return MekanismKey.of(new ChemicalStack(chemical, 1));
        };
        return this;
    }

    @Info("设置容量：infinite、1k、4k、16k、64k、1m 或 4m")
    public StorageMatrixBuilder size(String value) {
        this.size = switch (value.toLowerCase(java.util.Locale.ROOT)) {
            case "infinite" -> StorageMatrixDefinition.MatrixSize.INFINITE;
            case "1k" -> StorageMatrixDefinition.MatrixSize.K1;
            case "4k" -> StorageMatrixDefinition.MatrixSize.K4;
            case "16k" -> StorageMatrixDefinition.MatrixSize.K16;
            case "64k" -> StorageMatrixDefinition.MatrixSize.K64;
            case "1m" -> StorageMatrixDefinition.MatrixSize.M1;
            case "4m" -> StorageMatrixDefinition.MatrixSize.M4;
            default -> throw new IllegalArgumentException("Unknown storage matrix size: " + value);
        };
        return this;
    }

    @Info("设置物品栏模型")
    public StorageMatrixBuilder inventoryModel(ResourceLocation model) {
        this.inventoryModel = model;
        return this;
    }

    /** Set an explicit byte capacity instead of a preset {@code size(...)} tier. */
    @Info("设置自定义存储字节数，例如 bytes(256)")
    public StorageMatrixBuilder bytes(long value) {
        if (value < 1L) {
            throw new IllegalArgumentException("Matrix bytes must be positive: " + value);
        }
        this.customBytes = value;
        // bytes(...) describes a finite matrix, so it replaces the default infinite size.
        if (this.size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            this.size = StorageMatrixDefinition.MatrixSize.CUSTOM;
        }
        return this;
    }

    /** Set an explicit type count instead of the per-kind default (256 item/fluid, 25 chemical). */
    @Info("设置自定义类型数，例如 totalTypes(400)")
    public StorageMatrixBuilder totalTypes(int value) {
        if (value < 1) {
            throw new IllegalArgumentException("Matrix type count must be positive: " + value);
        }
        this.customTotalTypes = value;
        return this;
    }

    @Info("设置驱动器内格子模型")
    public StorageMatrixBuilder cellModel(ResourceLocation model) {
        this.driveModel = model;
        return this;
    }

    /**
     * Give script-created matrices a working inventory model.
     *
     * <p>KubeJS's default flow appends {@code layer0=kubejs:item/<id>}, an atlas texture that does
     * not exist, which renders as the purple/black missing texture. Short-circuiting the model
     * generator with a parent pointing at an existing matrix model avoids that; scripts that set
     * their own {@code textures}/{@code parentModel}/generator keep full control.
     */
    @Override
    protected void generateItemModels(dev.latvian.mods.kubejs.generator.KubeAssetGenerator generator) {
        boolean userCustomized = (this.textures != null && !this.textures.isEmpty())
                || this.baseTexture != null
                || (this.modelGenerator != null && this.modelGenerator != this.defaultModelGenerator);
        if (userCustomized) {
            super.generateItemModels(generator);
            return;
        }
        generator.itemModel(this.id, mg -> mg.parent(inventoryModelOrDefault()));
    }

    @Override
    public Item createObject() {
        if (type == StorageMatrixDefinition.MatrixType.CHEMICAL && boundChemical == null) {
            throw new IllegalStateException("Chemical matrix '%s' requires chemicalType(...).".formatted(id));
        }
        if (type != StorageMatrixDefinition.MatrixType.CHEMICAL && key == null) {
            throw new IllegalStateException("Matrix '%s' requires itemType(...) or fluidType(...).".formatted(id));
        }
        if (customBytes != null && size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            throw new IllegalStateException(
                    "Matrix '%s' cannot combine size('infinite') with bytes(...)".formatted(id));
        }
        if (customTotalTypes != null && customBytes == null && size == StorageMatrixDefinition.MatrixSize.CUSTOM) {
            throw new IllegalStateException(
                    "Matrix '%s' needs bytes(...) or a finite size(...) when totalTypes(...) is used".formatted(id));
        }
        // An explicit bytes(...) or totalTypes(...) wins; otherwise the size tier decides.
        boolean customCapacity = customBytes != null || customTotalTypes != null;
        boolean omniFamily = material == MatrixMaterials.Kind.UNIVERSAL || material == MatrixMaterials.Kind.QUANTUM;
        if (omniFamily && size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            throw new IllegalStateException(
                    "Matrix '%s' is an omni family matrix: give it size('1k') or bytes(...)".formatted(id));
        }
        if (omniFamily && !net.neoforged.fml.ModList.get().isLoaded("ae2omnicells")) {
            throw new IllegalStateException(
                    "Matrix '%s' needs AE2 Omni Cells for the universal/quantum backend".formatted(id));
        }
        long bytes = customBytes != null ? customBytes : size.bytes();
        int totalTypes = customTotalTypes != null
                ? customTotalTypes
                : type == StorageMatrixDefinition.MatrixType.CHEMICAL
                ? SimplifyChemicalStorageCellItem.TOTAL_TYPES : 256;
        int bytesPerType = (int) Math.max(1L, bytes / totalTypes);

        Item item;
        if (omniFamily) {
            // Universal / quantum matrices use eco's OmniCells backend: quantum carries no type limit.
            boolean quantum = material == MatrixMaterials.Kind.QUANTUM;
            Supplier<ECOCellType> omniType = quantum
                    ? cn.dancingsnow.neoecoae.integration.ae2omnicells.NEOmniCellTypes.QUANTUM_OMNI::get
                    : cn.dancingsnow.neoecoprototype.integration.omni.UniversalCellTypes.UNIVERSAL;
            int omniTypes = quantum ? UNLIMITED_OMNI_TYPES : totalTypes;
            item = new cn.dancingsnow.neoecoae.integration.ae2omnicells.item.ECOUniversalStorageCellItem(
                    createItemProperties().stacksTo(1), SimplifyTier.L1, omniType,
                    OMNI_IDLE_DRAIN, omniTypes, bytes);
            totalTypes = quantum ? Integer.MAX_VALUE : totalTypes;
            bytesPerType = (int) Math.max(1L, bytes / Math.max(1, totalTypes));
        } else if (size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            Supplier<AEKey> selectedKey = key;
            Supplier<ECOCellType> selectedCellType = () -> type == StorageMatrixDefinition.MatrixType.FLUID
                    ? SimplifyStorageCellItem.getFluidCellType()
                    : type == StorageMatrixDefinition.MatrixType.CHEMICAL
                    ? NERegistries.CELL_TYPE.get(ResourceLocation.fromNamespaceAndPath("neoecoae", "chemical"))
                    : SimplifyStorageCellItem.getItemCellType();
            item = new CustomInfiniteCellItem(createItemProperties().stacksTo(1), selectedKey,
                    selectedCellType, driveModelOrDefault());
        } else if (type == StorageMatrixDefinition.MatrixType.CHEMICAL) {
            item = new SimplifyChemicalStorageCellItem(createItemProperties().stacksTo(1),
                    bytes, bytesPerType, boundChemical);
        } else {
            AEKeyType keyType = type == StorageMatrixDefinition.MatrixType.FLUID
                    ? AEKeyType.fluids() : AEKeyType.items();
            item = new SimplifyStorageCellItem(createItemProperties().stacksTo(1), keyType,
                    type == StorageMatrixDefinition.MatrixType.FLUID
                            ? SimplifyStorageCellItem::getFluidCellType
                            : SimplifyStorageCellItem::getItemCellType,
                    bytes, bytesPerType, totalTypes);
        }
        StorageMatrixDefinition definition;
        if (size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            definition = StorageMatrixDefinition.infinite(
                    id, type, SimplifyTier.L1, 8.0D, inventoryModelOrDefault(), driveModelOrDefault());
        } else {
            definition = StorageMatrixDefinition.finite(
                    id, type, customCapacity ? StorageMatrixDefinition.MatrixSize.CUSTOM : size,
                    bytes, bytesPerType, totalTypes,
                    SimplifyTier.L1, inventoryModelOrDefault(), driveModelOrDefault());
        }
        StorageMatrixRegistry.register(definition, item);
        return item;
    }
}

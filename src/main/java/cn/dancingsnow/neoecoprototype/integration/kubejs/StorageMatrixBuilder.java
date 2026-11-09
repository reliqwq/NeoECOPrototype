package cn.dancingsnow.neoecoprototype.integration.kubejs;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoprototype.api.MatrixMaterials;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry;
import cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellItem;
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

    private StorageMatrixDefinition.MatrixType type = StorageMatrixDefinition.MatrixType.ITEM;
    private StorageMatrixDefinition.MatrixSize size = StorageMatrixDefinition.MatrixSize.INFINITE;
    private Supplier<AEKey> key;
    /**
     * True when the key came from {@link #keyType(Supplier)} rather than a typed binder. Only the unbounded
     * family can answer for one key; a finite matrix stores a whole medium and has no slot for it.
     */
    private boolean arbitraryKey;
    private ResourceLocation boundChemical;
    private ResourceLocation inventoryModel;
    private ResourceLocation driveModel;
    /** Set by {@link #grows}: the cell makes its bound key instead of storing what the network pushes. */
    private boolean growing;
    private long growPerBatch;
    private long growTicksPerBatch;
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

    /**
     * The matrix's material: an explicit override, else the palette of its kind.
     *
     * <p>A growing cell is the exception, and it is the infinite family's rather than its medium's: a fluid
     * housing would describe a byte budget it has not got, while the endless members of this family already
     * wear the silver concrete shell and share the in-drive chip.
     */
    public MatrixMaterials.Kind resolvedMaterial() {
        if (material != null) {
            return material;
        }
        return growing ? MatrixMaterials.Kind.INFINITE : MatrixMaterials.of(type, size);
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
     * ({@code pigmee}/{@code universal}/{@code quantum}/{@code small_bulk}/{@code infinite}/{@code other})
     * only pick a material, which is the same as calling {@link #material(String)} - capacity is
     * {@code size(...)}, never {@code type(...)}.
     */
    @Info("设置矩阵类型：item/fluid/chemical，或家族名 pigmee/universal/quantum/small_bulk/infinite/other（后者只换材质，容量请用 size(...)）")
    public StorageMatrixBuilder type(String value) {
        try {
            this.type = StorageMatrixDefinition.MatrixType.valueOf(value.toUpperCase(java.util.Locale.ROOT));
            return this;
        } catch (IllegalArgumentException ignored) {
            // Not a storage kind; fall through and treat it as a family material.
        }
        if ("Function".equals(value)) {
            // Rhino answers a JS arrow function handed to this overload with its text form, "Function",
            // rather than converting it to the Supplier overload - so a script that copied the old
            // `.type(() => key)` example lands here. Name the door that does work.
            throw new IllegalStateException("Matrix '" + id + "': a JS function cannot travel through"
                    + " type(...) - bind an arbitrary AEKey with keyType(() => key) instead");
        }
        this.material = MatrixMaterials.parse(value);
        return this;
    }

    /**
     * Bind an arbitrary {@link AEKey} - one whose type is neither items nor fluids, such as Ars Nouveau's
     * Source - by handing in the key itself. A bare id cannot name one of these: only that key's own type
     * knows how to build the object, which is why this is the advanced door and the typed binders stay.
     * Only the unbounded family can hold a single key, so this is rejected on a finite size.
     *
     * <p>The name is deliberately not another {@code type(...)}: Rhino resolves a JS arrow function passed
     * there to the {@link #type(String)} overload (it reads as the text "Function"), so a function never
     * reached this signature. A method name with only one signature is what makes the SAM conversion fire.
     */
    @Info("绑定任意 AEKey（高级用法：魔源这类非物品/流体键），写成 keyType(() => key)；只对 infinite 或 grows(...) 生效，需要自己提供键对象")
    public StorageMatrixBuilder keyType(Supplier<AEKey> key) {
        this.key = () -> {
            AEKey resolved = key.get();
            if (resolved == null) {
                throw new IllegalStateException("Matrix '" + this.id
                        + "' binds an arbitrary AEKey, but its supplier answered null"
                        + " (is the mod that owns it installed?)");
            }
            return resolved;
        };
        this.arbitraryKey = true;
        return this;
    }

    /** Kept for Java callers; scripts must use {@link #keyType(Supplier)} for the reason stated there. */
    public StorageMatrixBuilder type(Supplier<AEKey> key) {
        return keyType(key);
    }

    @Info("绑定一个物品（只有 infinite 档真的绑；有限矩阵按媒介通用，见本文下方说明）")
    public StorageMatrixBuilder itemType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.ITEM;
        this.arbitraryKey = false;
        this.key = () -> {
            var item = BuiltInRegistries.ITEM.get(id);
            var key = item == null ? null : AEItemKey.of(item);
            if (key == null) {
                // AEItemKey.of answers null for an empty stack, which is what an unknown id resolves to,
                // so this is the one check that says which id the script got wrong.
                throw new IllegalStateException("Unknown item '" + id + "' for matrix " + this.id);
            }
            return key;
        };
        return this;
    }

    @Info("绑定一个流体（只有 infinite 档真的绑；有限矩阵按媒介通用，见本文下方说明）")
    public StorageMatrixBuilder fluidType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.FLUID;
        this.arbitraryKey = false;
        this.key = () -> {
            var fluid = BuiltInRegistries.FLUID.get(id);
            var key = fluid == null ? null : AEFluidKey.of(fluid);
            if (key == null) {
                throw new IllegalStateException("Unknown fluid '" + id + "' for matrix " + this.id);
            }
            return key;
        };
        return this;
    }

    @Info("绑定一个化学品资源 ID；需要 Mekanism 与 Applied Mekanistics")
    public StorageMatrixBuilder chemicalType(ResourceLocation id) {
        this.type = StorageMatrixDefinition.MatrixType.CHEMICAL;
        this.arbitraryKey = false;
        this.boundChemical = id;
        // The body that names Mekanism lives in ChemicalMatrixSupport: KubeJS resolves this class when the
        // builder type is registered, and that happens in packs that never installed Mekanism.
        this.key = () -> ChemicalMatrixSupport.resolve(id, this.id);
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

    /**
     * Set an explicit byte capacity instead of a preset {@code size(...)} tier. It wins over a
     * {@code size(...)} already given, in either order: the tier you named is replaced by CUSTOM.
     */
    @Info("设置自定义存储字节数，例如 bytes(256)。它会顶掉先前或之后的 size(...) 档位")
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

    /**
     * Turn this matrix into a self-growing one: it hands out {@code perBatch} of whatever
     * {@code itemType}/{@code fluidType}/{@code chemicalType} bound, jumping once every {@code ticksPerBatch}
     * world ticks, and it refuses everything pushed into it. Both numbers are required - a cell that took its
     * speed from the server config would change under a pack that never asked for that.
     */
    @Info("让它自产：每 ticksPerBatch 世界刻跳一次，每次 perBatch 个；内容就用 itemType/fluidType/chemicalType 指定")
    public StorageMatrixBuilder grows(long perBatch, long ticksPerBatch) {
        if (perBatch < 1L || ticksPerBatch < 1L) {
            throw new IllegalArgumentException("grows() needs positive batch and interval: "
                    + perBatch + "/" + ticksPerBatch);
        }
        this.growing = true;
        this.growPerBatch = perBatch;
        this.growTicksPerBatch = ticksPerBatch;
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
        boolean userCustomized = !this.textures.isEmpty()
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
            throw new IllegalStateException(
                    "Matrix '%s' requires itemType(...), fluidType(...), chemicalType(...) or keyType(() => key)."
                            .formatted(id));
        }
        if (arbitraryKey && size != StorageMatrixDefinition.MatrixSize.INFINITE) {
            // The finite path is a per-medium budget: it would silently store the whole medium while the
            // script believed it had pinned one key, so refuse rather than create a lying item.
            throw new IllegalStateException("Matrix '" + id + "' binds an arbitrary AEKey, which only the"
                    + " infinite family can hold: give it size('infinite') (the default) or bind"
                    + " itemType(...)/fluidType(...)/chemicalType(...)");
        }
        if (customBytes != null && size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            throw new IllegalStateException(
                    "Matrix '%s' cannot combine size('infinite') with bytes(...)".formatted(id));
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
        if (type == StorageMatrixDefinition.MatrixType.CHEMICAL
                && !(net.neoforged.fml.ModList.get().isLoaded("mekanism")
                        && net.neoforged.fml.ModList.get().isLoaded("appmek"))) {
            // Same reason as the omni check above: the chemical cell's own constructor reaches into
            // Applied Mekanistics, so without this the script fails as a NoClassDefFoundError instead
            // of a sentence that says what to install.
            throw new IllegalStateException(
                    "Matrix '%s' needs Mekanism and Applied Mekanistics for the chemical backend"
                            .formatted(id));
        }
        if (growing) {
            // It answers for itself where its stock is concerned; it still joins the matrix registry, because
            // that is the list the in-drive model pass reads, and a script-made item is only reachable there.
            if (key == null) {
                throw new IllegalStateException("Growing matrix '" + id
                        + "' must bind what it grows with itemType(...), fluidType(...), chemicalType(...)"
                        + " or keyType(() => key)");
            }
            if (material == MatrixMaterials.Kind.UNIVERSAL || material == MatrixMaterials.Kind.QUANTUM) {
                throw new IllegalStateException("Growing matrix '" + id
                        + "' cannot borrow an omni family: that backend stores what the network pushes");
            }
            if (customBytes != null || customTotalTypes != null) {
                throw new IllegalStateException("Growing matrix '" + id
                        + "' cannot set bytes(...) or totalTypes(...) - it is unbounded by construction");
            }
            if (size != StorageMatrixDefinition.MatrixSize.INFINITE) {
                // The tier would be read by nothing, and a script that wrote it believes it capped something.
                throw new IllegalStateException("Growing matrix '" + id
                        + "' cannot set size(...) - it has no byte budget to size");
            }
            // Resolve what we can while the script is still running, so a wrong item or fluid id is a startup
            // sentence naming the id rather than a crash the first time somebody mounts the cell. The chemical
            // registry is the one that cannot be read yet at this moment (see ChemicalMatrixSupport), so a
            // chemical growth stays a supplier and resolves on the cell's first runtime read.
            // Typed shorthand resolves here so a bad id is a startup sentence; an arbitrary supplier stays
            // lazy, because the mod that owns the key may not have published it by the time scripts run.
            appeng.api.stacks.AEKey eagerContent =
                    type == StorageMatrixDefinition.MatrixType.CHEMICAL || arbitraryKey ? null : key.get();
            var grown = new cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem(
                    createItemProperties().stacksTo(1),
                    eagerContent != null ? () -> eagerContent : key,
                    growPerBatch, growTicksPerBatch);
            StorageMatrixRegistry.register(StorageMatrixDefinition.infinite(id, type, SimplifyTier.L1,
                    cn.dancingsnow.neoecoprototype.items.SimplifySingularityCellItem.IDLE_DRAIN,
                    inventoryModelOrDefault(), driveModelOrDefault()), grown);
            return grown;
        }

        long bytes = customBytes != null ? customBytes : size.bytes();
        int totalTypes = customTotalTypes != null
                ? customTotalTypes
                : type == StorageMatrixDefinition.MatrixType.CHEMICAL
                ? ChemicalMatrixSupport.totalTypes() : 256;
        int bytesPerType = (int) Math.max(1L, bytes / totalTypes);

        Item item;
        if (omniFamily) {
            // eco's universal cell extends an Omni Cells class, so the construction lives behind its own
            // class and is reached only from here, past the guard above.
            boolean quantum = material == MatrixMaterials.Kind.QUANTUM;
            item = OmniMatrixSupport.create(createItemProperties().stacksTo(1), quantum, totalTypes, bytes);
            totalTypes = OmniMatrixSupport.typesForDefinition(quantum, totalTypes);
            bytesPerType = (int) Math.max(1L, bytes / Math.max(1, totalTypes));
        } else if (size == StorageMatrixDefinition.MatrixSize.INFINITE) {
            Supplier<AEKey> selectedKey = key;
            // An infinite matrix gets its own invisible cell type rather than the media's: borrowing it
            // would add this cell's single bound item and zero bytes to that media's host row.
            item = new CustomInfiniteCellItem(createItemProperties().stacksTo(1), selectedKey,
                    () -> CustomInfiniteCellItem.INFINITE_CELL_TYPE, driveModelOrDefault());
        } else if (type == StorageMatrixDefinition.MatrixType.CHEMICAL) {
            item = ChemicalMatrixSupport.create(createItemProperties().stacksTo(1),
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

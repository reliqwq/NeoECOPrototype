package cn.dancingsnow.neoecoprototype.integration.kubejs;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.api.MatrixMaterials;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixDefinition;
import cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry;
import cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellItem;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * 自定义无限存储矩阵的 KubeJS builder。行为与无限混凝土矩阵一致：绑定键无限
 * 存入吸收、无限取出、首选路由、不可拆卸。注册物品本身走 KubeJS 的
 * {@code StartupEvents.registry('item', ...)}。各方法均返回 builder 自身，
 * 脚本可直接链式调用。
 */
public class InfiniteMatrixBuilder extends ItemBuilder {

    private Supplier<AEKey> record;
    private StorageMatrixDefinition.MatrixType matrixType = StorageMatrixDefinition.MatrixType.ITEM;
    private StorageMatrixDefinition.MatrixSize matrixSize = StorageMatrixDefinition.MatrixSize.INFINITE;
    private SimplifyTier matrixTier = SimplifyTier.L1;
    @Nullable
    private ResourceLocation inventoryModel;
    @Nullable
    private ResourceLocation driveModel;
    private Consumer<ModelGenerator> defaultModelGenerator;

    /** 无限矩阵自己的配色：银灰外壳（无限混凝土矩阵模型）。 */
    private static final ResourceLocation DEFAULT_ITEM_MODEL =
            MatrixMaterials.Kind.INFINITE.inventoryModel();

    /** 与无限矩阵物品栏配色一致的驱动器内格子模型。 */
    public static final ResourceLocation DEFAULT_DRIVE_MODEL =
            MatrixMaterials.Kind.INFINITE.driveModel();

    /** 可选材质覆盖；为空时使用无限矩阵自己的配色。 */
    @Nullable
    private MatrixMaterials.Kind material;

    @Info("设置矩阵材质：infinite、item、fluid、chemical 或 other")
    public InfiniteMatrixBuilder material(String value) {
        this.material = MatrixMaterials.parse(value);
        return this;
    }

    private MatrixMaterials.Kind resolvedMaterial() {
        return material != null ? material : MatrixMaterials.Kind.INFINITE;
    }

    public InfiniteMatrixBuilder(ResourceLocation id) {
        super(id);
        // KJS 生成物品模型时，即使设置了 parentModel 也会追加 layer0=kubejs:item/<id>
        // （不存在的贴图 → 紫黑格）。modelGenerator 优先级最高且直接短路，用它的
        // 默认实现只输出 parent；脚本自定义贴图/模型时让路。
        this.defaultModelGenerator = mg -> mg.parent(DEFAULT_ITEM_MODEL);
        this.modelGenerator = this.defaultModelGenerator;
    }

    @Info("绑定到指定 AEKey（高级用法，通常用 itemType/fluidType）")
    public InfiniteMatrixBuilder type(Supplier<AEKey> key) {
        this.record = key;
        return this;
    }

    @Info("绑定到指定物品：该物品可无限存入与取出（如 minecraft:glass）")
    public InfiniteMatrixBuilder itemType(ResourceLocation id) {
        this.matrixType = StorageMatrixDefinition.MatrixType.ITEM;
        this.record = new TraceableSupplier(id.toString(),
                () -> AEItemKey.of(BuiltInRegistries.ITEM.get(id)));
        return this;
    }

    @Info("绑定到指定流体：该流体可无限存入与取出（如 minecraft:water）")
    public InfiniteMatrixBuilder fluidType(ResourceLocation id) {
        this.matrixType = StorageMatrixDefinition.MatrixType.FLUID;
        this.record = new TraceableSupplier(id.toString(),
                () -> AEFluidKey.of(BuiltInRegistries.FLUID.get(id)));
        return this;
    }

    @Info("设置矩阵在物品栏中使用的模型")
    public InfiniteMatrixBuilder inventoryModel(ResourceLocation model) {
        this.inventoryModel = model;
        return this;
    }

    @Info("设置矩阵在存储矩阵驱动器中的格子模型")
    public InfiniteMatrixBuilder cellModel(ResourceLocation model) {
        this.driveModel = model;
        return this;
    }

    @Info("设置矩阵大小；无限矩阵应使用 infinite")
    public InfiniteMatrixBuilder size(String size) {
        StorageMatrixDefinition.MatrixSize parsed =
                StorageMatrixDefinition.MatrixSize.valueOf(size.toUpperCase(java.util.Locale.ROOT));
        if (parsed != StorageMatrixDefinition.MatrixSize.INFINITE) {
            throw new IllegalArgumentException("Infinite storage matrices only support size('infinite')");
        }
        this.matrixSize = parsed;
        return this;
    }

    @Info("设置矩阵等级；当前无限矩阵仅支持 l1")
    public InfiniteMatrixBuilder tier(String tier) {
        SimplifyTier parsed = SimplifyTier.valueOf(tier.toUpperCase(java.util.Locale.ROOT));
        if (parsed != SimplifyTier.L1) {
            throw new IllegalArgumentException("Infinite storage matrices only support tier('l1')");
        }
        this.matrixTier = parsed;
        return this;
    }

    /**
     * 覆写物品模型生成：未自定义贴图/模型时固定输出 parent=混凝土矩阵模型，
     * 杜绝 KJS 默认流程追加 layer0=kubejs:item/<id> 造成的紫黑格。
     */
    @Override
    protected void generateItemModels(dev.latvian.mods.kubejs.generator.KubeAssetGenerator generator) {
        boolean userCustomized = !this.textures.isEmpty() || this.baseTexture != null
                || (this.modelGenerator != null && this.modelGenerator != this.defaultModelGenerator);
        if (userCustomized) {
            super.generateItemModels(generator);
            return;
        }
        ResourceLocation model = this.inventoryModel == null ? resolvedMaterial().inventoryModel() : this.inventoryModel;
        generator.itemModel(this.id, mg -> mg.parent(model));
    }

    @Override
    public Item createObject() {
        if (this.record == null) {
            throw new IllegalStateException(
                    "Custom infinite storage matrix '%s' has no bound type: call itemType(...) or fluidType(...)."
                            .formatted(this.id));
        }
        if (this.matrixSize != StorageMatrixDefinition.MatrixSize.INFINITE) {
            throw new IllegalStateException("Infinite storage matrices must use size('infinite')");
        }
        CustomInfiniteCellItem item = new CustomInfiniteCellItem(
                this.createItemProperties().stacksTo(1), this.record, this.driveModel);
        // Index the definition for future clients without changing the legacy handlers.
        ResourceLocation itemModel = this.inventoryModel == null ? resolvedMaterial().inventoryModel() : this.inventoryModel;
        StorageMatrixRegistry.register(StorageMatrixDefinition.infinite(
                this.id, this.matrixType, this.matrixTier, CustomInfiniteCellItem.IDLE_DRAIN,
                itemModel, this.driveModel == null ? resolvedMaterial().driveModel() : this.driveModel), item);
        return item;
    }

    /**
     * 延迟解析：脚本注册时其他模组内容可能尚未入册，首次使用时再求值并给出
     * 可读的报错。
     */
    record TraceableSupplier(String err, Supplier<AEKey> supplier) implements Supplier<AEKey> {

        @Override
        public AEKey get() {
            var key = this.supplier.get();
            if (key == null) {
                throw new NullPointerException(
                        "Invalid custom infinite storage matrix, check your KubeJS script. Type: %s."
                                .formatted(this.err));
            }
            return key;
        }
    }
}

package cn.dancingsnow.neoecoprototype.integration.kubejs;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoae.api.ECOTier;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.items.EcoSupplyCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import dev.latvian.mods.kubejs.client.ModelGenerator;
import dev.latvian.mods.kubejs.item.ItemBuilder;
import dev.latvian.mods.kubejs.typings.Info;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * KubeJS builder for a twin of eco's {@code neoecoae:eco_infinite_item_cell}: an unbounded source and
 * sink locked to the resources the script names, running on eco's own {@code ECOInfiniteResourceCell}
 * engine rather than this mod's endless store.
 *
 * <p>It exists as the answer to "can a script make one of eco's own cells?", which eco cannot do today:
 * eco's KubeJS plugin registers three recipe schemas and no builder types, so its cell classes are
 * reachable from Java only.
 */
public final class EcoSupplyCellBuilder extends ItemBuilder {

    /** eco's own infinite-resource matrix model, so the script-made twin reads as the same item. */
    private static final ResourceLocation DEFAULT_ITEM_MODEL =
            ResourceLocation.fromNamespaceAndPath("neoecoae", "item/eco_infinite_item_cell");
    private static final ResourceLocation DEFAULT_DRIVE_MODEL =
            ResourceLocation.fromNamespaceAndPath("neoecoae", "block/cell/storage_cell_l9_infinite_resource");
    /** eco's own tooltip group: ".contents" / ".sink", read off {@code tooltip.neoecoae.infinite_resource}. */
    private static final String TOOLTIP_GROUP = "tooltip.neoecoae.infinite_resource";

    private final List<Supplier<AEKey>> records = new ArrayList<>();
    private IECOTier tier = ECOTier.L9;
    private ResourceLocation inventoryModel;
    private ResourceLocation driveModel;
    private final Consumer<ModelGenerator> defaultModelGenerator;

    public EcoSupplyCellBuilder(ResourceLocation id) {
        super(id);
        // KubeJS appends layer0=kubejs:item/<id> when it generates the model itself, and that texture
        // does not exist. Short-circuiting is the same fix the matrix builders use.
        this.defaultModelGenerator = mg -> mg.parent(DEFAULT_ITEM_MODEL);
        this.modelGenerator = this.defaultModelGenerator;
    }

    @Info("锁定供给的物品：可无限取出，也被无限吸收（如 minecraft:cobblestone）")
    public EcoSupplyCellBuilder supplies(ResourceLocation itemId) {
        this.records.add(() -> {
            AEKey key = AEItemKey.of(BuiltInRegistries.ITEM.get(itemId));
            if (key == null) {
                throw new IllegalStateException("Supply cell '" + this.id + "' names item '" + itemId
                        + "', which is not in the item registry (is the mod that adds it installed?)");
            }
            return key;
        });
        return this;
    }

    @Info("锁定供给的流体：可无限取出，也被无限吸收（如 minecraft:water）")
    public EcoSupplyCellBuilder suppliesFluid(ResourceLocation fluidId) {
        this.records.add(() -> {
            AEKey key = AEFluidKey.of(BuiltInRegistries.FLUID.get(fluidId));
            if (key == null) {
                throw new IllegalStateException("Supply cell '" + this.id + "' names fluid '" + fluidId
                        + "', which is not in the fluid registry (is the mod that adds it installed?)");
            }
            return key;
        });
        return this;
    }

    /**
     * Bind any {@link AEKey} whose type is neither items nor fluids - Ars Nouveau's Source, for example -
     * by handing in the key itself. A bare id cannot name one of these: only that key's own type knows how
     * to build the object, which is why the typed binders stay and this is the advanced door.
     */
    @Info("绑定任意 AEKey（高级用法：魔源这类非物品/流体的键），写成 suppliesKey(() => key)；键对象要脚本自己提供")
    public EcoSupplyCellBuilder suppliesKey(Supplier<AEKey> key) {
        this.records.add(() -> {
            AEKey resolved = key.get();
            if (resolved == null) {
                throw new IllegalStateException("Supply cell '" + this.id
                        + "' binds an arbitrary AEKey, but its supplier answered null"
                        + " (is the mod that owns it installed?)");
            }
            return resolved;
        });
        return this;
    }

    @Info("档位：l4 / l6 / l9，决定每类型容量与待机耗电；默认 l9（与 eco 那台一致）")
    public EcoSupplyCellBuilder tier(String value) {
        this.tier = ECOTier.valueOf(value.toUpperCase(Locale.ROOT));
        return this;
    }

    @Info("设置物品在物品栏中的模型")
    public EcoSupplyCellBuilder inventoryModel(ResourceLocation model) {
        this.inventoryModel = model;
        return this;
    }

    @Info("设置物品在存储驱动器中的格子模型")
    public EcoSupplyCellBuilder cellModel(ResourceLocation model) {
        this.driveModel = model;
        return this;
    }

    @Override
    protected void generateItemModels(dev.latvian.mods.kubejs.generator.KubeAssetGenerator generator) {
        boolean userCustomized = !this.textures.isEmpty() || this.baseTexture != null
                || (this.modelGenerator != null && this.modelGenerator != this.defaultModelGenerator);
        if (userCustomized) {
            super.generateItemModels(generator);
            return;
        }
        ResourceLocation model = this.inventoryModel == null ? DEFAULT_ITEM_MODEL : this.inventoryModel;
        generator.itemModel(this.id, mg -> mg.parent(model));
    }

    @Override
    public Item createObject() {
        if (this.records.isEmpty()) {
            throw new IllegalStateException(("Supply cell '%s' locks nothing: call supplies(...) and/or"
                    + " suppliesFluid(...) with at least one id").formatted(this.id));
        }
        NeoECOPrototype.LOGGER.debug("Registering script-made eco supply cell {}", this.id);
        EcoSupplyCellItem item = new EcoSupplyCellItem(
                this.createItemProperties().stacksTo(1).rarity(Rarity.EPIC),
                this.tier,
                SimplifyStorageCellItem::getItemCellType,
                this.records,
                TOOLTIP_GROUP,
                this.id.toString());
        ECOCellModels.register(item, this.driveModel == null ? DEFAULT_DRIVE_MODEL : this.driveModel);
        return item;
    }
}

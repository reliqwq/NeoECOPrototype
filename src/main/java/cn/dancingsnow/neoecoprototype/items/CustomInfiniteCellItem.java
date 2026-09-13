package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * KubeJS 可创建的自定义无限存储矩阵：与无限混凝土矩阵同一套"无限仓库"行为，
 * 但绑定的类型由脚本决定（单物品或单流体）。
 *
 * <p>记录类型延迟解析（脚本注册时其他模组物品可能尚未入册），首次在游戏逻辑中
 * 使用时才求值。格子行为见 {@link CustomInfiniteCellHandler}；本类只携带
 * tier / 类型等元数据。
 */
public final class CustomInfiniteCellItem extends Item implements IBasicECOCellItem {

    public static final double IDLE_DRAIN = 8.0D;
    /** 单绑定类型：1 个类型槽 × 1 字节，与混凝土矩阵的 footprint 标注口径一致。 */
    public static final int TOTAL_TYPES = 1;
    public static final long REPORTED_BYTES = TOTAL_TYPES;
    /** 终端显示量：对齐 ExtendedAE 的 Integer.MAX_VALUE × amountPerUnit。 */
    public static final long REPORTED_AMOUNT = (long) Integer.MAX_VALUE;

    private final Supplier<AEKey> record;
    @Nullable
    private final ResourceLocation driveModel;

    public CustomInfiniteCellItem(Properties properties, Supplier<AEKey> record,
                                  @Nullable ResourceLocation driveModel) {
        super(properties);
        this.record = record;
        this.driveModel = driveModel;
    }

    /** @return the bound key; resolves lazily and fails with a script-readable message. */
    public AEKey getRecord() {
        AEKey key = this.record.get();
        if (key == null) {
            throw new IllegalStateException(
                    "Invalid custom infinite storage matrix, check your KubeJS script. Item: "
                            + BuiltInRegistries.ITEM.getKey(this));
        }
        return key;
    }

    /** Drive-render model override from the script, or null for the default item cell model. */
    @Nullable
    public ResourceLocation getDriveModel() {
        return this.driveModel;
    }

    @Override
    public cn.dancingsnow.neoecoae.api.IECOTier getTier() {
        return SimplifyTier.L1;
    }

    @Override
    public AEKeyType getKeyType() {
        return getRecord().getType();
    }

    @Override
    public long getBytes() {
        return REPORTED_BYTES;
    }

    @Override
    public int getBytesPerType() {
        return 1;
    }

    @Override
    public int getTotalTypes() {
        return TOTAL_TYPES;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public ECOCellType getCellType() {
        AEKeyType keyType = getKeyType();
        return keyType == AEKeyType.fluids()
                ? SimplifyStorageCellItem.getFluidCellType()
                : SimplifyStorageCellItem.getItemCellType();
    }

    // 固定无限源：没有可编辑的分区，模糊模式保持无效。

    @Override
    public ConfigInventory getConfigInventory(ItemStack stack) {
        return null;
    }

    @Override
    public appeng.api.config.FuzzyMode getFuzzyMode(ItemStack stack) {
        return appeng.api.config.FuzzyMode.IGNORE_ALL;
    }

    @Override
    public void setFuzzyMode(ItemStack stack, appeng.api.config.FuzzyMode mode) {
        // unsupported: fixed infinite source
    }

    /**
     * Hover preview showing the bound key with its reported stock.
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!AEConfig.instance().isTooltipShowCellContent()) {
            return Optional.empty();
        }
        var content = List.of(new GenericStack(getRecord(), REPORTED_AMOUNT * getKeyType().getAmountPerUnit()));
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, false, true));
    }

    /**
     * 同无限混凝土矩阵：拆卸是字面意思的不可拆卸——零产出 + 10 秒失明。
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (tryDisassemble(level, player)) {
            return InteractionResultHolder.sidedSuccess(player.getItemInHand(hand), level.isClientSide());
        }
        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }

    @Override
    public InteractionResult onItemUseFirst(ItemStack stack, UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player != null && tryDisassemble(level, player)) {
            return InteractionResult.sidedSuccess(level.isClientSide());
        }
        return InteractionResult.PASS;
    }

    private static boolean tryDisassemble(Level level, Player player) {
        if (!InteractionUtil.isInAlternateUseMode(player)) {
            return false;
        }
        if (!level.isClientSide) {
            player.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 200, 0));
        }
        return true;
    }
}

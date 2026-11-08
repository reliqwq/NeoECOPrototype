package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import appeng.util.InteractionUtil;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import net.minecraft.network.chat.Component;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 创造模式专用的"全物品"无限存储矩阵：挂在驱动器上时，本模组与 eco 的每一个物品都按无限量可取。
 * 与无限混凝土矩阵同一套虚拟供应机制，只是清单来自注册表而不是固定 16 种颜色；格子本身无状态，
 * 行为见 {@link AllItemsCreativeCellHandler}。
 */
public final class AllItemsCreativeCellItem extends Item implements IBasicECOCellItem {

    /**
     * 名义类型数。这个格子按注册表动态列类型，静态标记里没有真值可用；它只用于 eco 的展示统计，
     * 而 {@link #CELL_TYPE} 是隐藏的（visible=false），不会出现在存储主机的类型行里。
     */
    public static final int NOMINAL_TYPES = 4096;

    /** 与其它无限矩阵同一口径：字节无限，列出量按 ExtendedAE 的 Integer.MAX_VALUE。 */
    public static final long REPORTED_BYTES = Long.MAX_VALUE;
    public static final long REPORTED_AMOUNT_PER_TYPE = (long) Integer.MAX_VALUE;
    public static final double IDLE_DRAIN = 8.0D;

    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype.all_items"),
            NOMINAL_TYPES,
            false);

    public AllItemsCreativeCellItem(Properties properties) {
        super(properties);
    }

    @Override
    public cn.dancingsnow.neoecoae.api.IECOTier getTier() {
        return SimplifyTier.L1;
    }

    @Override
    public AEKeyType getKeyType() {
        return AEKeyType.items();
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
        return NOMINAL_TYPES;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public ECOCellType getCellType() {
        return CELL_TYPE;
    }

    // 固定无限源：没有可编辑的分区。AE2 默认的 isEditable 对 null config 直接调 size() 会 NPE，
    // 所以明确返回 false 让元件工作台拒收。
    @Override
    public boolean isEditable(ItemStack stack) {
        return false;
    }

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

    /** 悬停预览：按 AE2 的条目上限列出本模组与 eco 的物品，量按无限口径。 */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!AEConfig.instance().isTooltipShowCellContent()) {
            return Optional.empty();
        }
        List<GenericStack> content = new ArrayList<>();
        for (AEItemKey key : AllItemsCreativeCellHandler.keys()) {
            content.add(new GenericStack(key, REPORTED_AMOUNT_PER_TYPE));
        }
        int maxShown = AEConfig.instance().getTooltipMaxCellContentShown();
        boolean hasMore = content.size() > maxShown;
        if (hasMore) {
            content.subList(maxShown, content.size()).clear();
        }
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, hasMore, true));
    }

    /** 同无限混凝土矩阵：潜行右键拆不出东西，附赠 10 秒失明。 */
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

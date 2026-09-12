package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.items.storage.StorageCellTooltipComponent;
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
 * L1 无限混凝土存储矩阵 - metadata for an infinite concrete vault cell in the
 * style of ExtendedAE's infinity cells: mounted in a drive, concrete deposits
 * merge into an endless virtual supply (so regular matrices never hold
 * concrete), and any of the 16 vanilla concrete colors can be extracted
 * endlessly. The cell itself is stateless - all behavior lives in
 * {@link InfiniteConcreteCellHandler}, so the item only carries tier / type
 * metadata for eco's cell and drive systems.
 */
public final class SimplifyConcreteStorageCellItem extends Item implements IBasicECOCellItem {

    /** One virtual slot per vanilla concrete color. */
    public static final int CONCRETE_TYPES = 16;

    /**
     * Reported byte budget - just the cell's own footprint: one byte per
     * concrete color slot, 16 total. The supply is endless, so it is never
     * expressed in bytes; used bytes stay 0 and nothing enforces this budget.
     */
    public static final long REPORTED_BYTES = CONCRETE_TYPES;
    /** Per-color amount shown in terminals - Integer.MAX_VALUE, matching ExtendedAE's infinity cells. */
    public static final long REPORTED_AMOUNT_PER_TYPE = (long) Integer.MAX_VALUE;
    public static final double IDLE_DRAIN = 8.0D;

    /**
     * Dedicated cell type (upstream's cell display interface): keeps the
     * matrix out of the shared item row so its 16/16 types and flat byte
     * budget do not pollute the item summary, and gives it a "concrete" row
     * of its own in the storage host UI.
     */
    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype.concrete"),
            CONCRETE_TYPES,
            true);

    public SimplifyConcreteStorageCellItem(Item.Properties properties) {
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
        return CONCRETE_TYPES;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public ECOCellType getCellType() {
        return CELL_TYPE;
    }

    // A fixed infinite source has nothing to partition or fuzzily match; both
    // hooks stay inert so the cell workbench cannot fake a partition.

    @Override
    public appeng.util.ConfigInventory getConfigInventory(ItemStack stack) {
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
     * Hover preview like ExtendedAE's infinity cell: show the 16 concrete
     * colors with their reported stock. Respects AE2's "show cell content"
     * tooltip setting and its per-tooltip entry cap.
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!AEConfig.instance().isTooltipShowCellContent()) {
            return Optional.empty();
        }
        List<GenericStack> content = new ArrayList<>();
        for (AEItemKey concrete : InfiniteConcreteCellHandler.CONCRETE_KEYS) {
            content.add(new GenericStack(concrete, REPORTED_AMOUNT_PER_TYPE));
        }
        int maxShown = AEConfig.instance().getTooltipMaxCellContentShown();
        boolean hasMore = content.size() > maxShown;
        if (hasMore) {
            content.subList(maxShown, content.size()).clear();
        }
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, hasMore, true));
    }

    /**
     * 拆卸惩罚,字面意思的"不可拆卸":潜行 + 右键尝试拆解压缩艺术,什么都拆不出来,
     * 还会喜提 10 秒失明。矩阵本身毫发无损。
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

package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import appeng.core.AEConfig;
import appeng.core.definitions.AEItems;
import appeng.items.storage.StorageCellTooltipComponent;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.api.storage.IBasicECOCellItem;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Optional;

/**
 * A cell that grows singularities out of nothing and takes nothing in.
 *
 * <p>AE2's {@code StorageCell} has no tick method and the L1 drive has no ticker at all, so the bank is
 * never accumulated - it is <em>derived</em> from the world clock whenever somebody asks. Two numbers live
 * on the stack (the world tick it started from, and how much the network has drawn), everything else is
 * arithmetic, and a read never writes. That is what lets the same cell work mounted in our drive, in an
 * eco one, or lying in a chest.
 */
public final class SimplifySingularityCellItem extends Item implements IBasicECOCellItem {

    /** The one thing this cell ever holds. */
    public static final AEItemKey SINGULARITY = AEItemKey.of(AEItems.SINGULARITY);

    /** One type, one row. */
    public static final int TYPES = 1;

    /** Infinity in the byte column, like the other endless members of this family. */
    public static final long REPORTED_BYTES = Long.MAX_VALUE;

    public static final double IDLE_DRAIN = 1.0D;

    /**
     * Its own cell type rather than the shared item row: eco's storage host would otherwise read this as one
     * more ordinary matrix, and "1 / 1 types against an unbounded budget" says nothing a player can act on.
     */
    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype.singularity"), TYPES, false);

    /**
     * All of the cell's state. {@code startGameTime} is the world tick it began counting from; {@code drawn}
     * is how many singularities the network has already taken out of it.
     */
    public record Bank(long startGameTime, long drawn) {
        public static final Codec<Bank> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("start_game_time").forGetter(Bank::startGameTime),
                Codec.LONG.fieldOf("drawn").forGetter(Bank::drawn)).apply(instance, Bank::new));
        public static final StreamCodec<io.netty.buffer.ByteBuf, Bank> STREAM_CODEC =
                ByteBufCodecs.fromCodec(CODEC);
    }

    public SimplifySingularityCellItem(Item.Properties properties) {
        super(properties);
    }

    static DataComponentType<Bank> bankComponent() {
        return cn.dancingsnow.neoecoprototype.registration.ModRegistration.SINGULARITY_CELL_BANK.get();
    }

    /**
     * How many singularities are ready at {@code nowGameTime}. There is no bank ceiling to stop at - the
     * stock is a long and it is allowed to reach the top of one - so the only limit left is arithmetic:
     * the cumulative total saturates instead of wrapping. A batch is what the bank jumps by; it does not drip.
     */
    public static long bankOf(Bank bank, long nowGameTime) {
        long interval = NeoECOPrototypeServerConfig.singularityCellTicksPerBatch();
        long batch = NeoECOPrototypeServerConfig.singularityCellAmountPerBatch();
        long batches = Math.max(0L, (nowGameTime - bank.startGameTime()) / interval);
        long produced = batches > Long.MAX_VALUE / batch ? Long.MAX_VALUE : batches * batch;
        long stored = produced - bank.drawn;
        return stored <= 0L ? 0L : stored;
    }

    /**
     * The cell needs one world tick to count from. Crafting stamps it; a cell that arrives another way (a
     * give command, a pack's loot table) is stamped the first time the server asks it what it holds.
     *
     * @return the cell's bank, or null while there is no server clock to stamp with
     */
    public static Bank stampIfNeeded(ItemStack stack, long nowGameTime) {
        Bank bank = stack.get(bankComponent());
        if (bank != null) {
            return bank;
        }
        if (nowGameTime < 0L) {
            return null;
        }
        Bank fresh = new Bank(nowGameTime, 0L);
        stack.set(bankComponent(), fresh);
        return fresh;
    }

    @Override
    public void onCraftedBy(ItemStack stack, net.minecraft.world.level.Level level,
                            net.minecraft.world.entity.player.Player player) {
        // The cell needs a world tick to grow from, and crafting is the one moment we are handed a level.
        if (level instanceof ServerLevel serverLevel && stack.get(bankComponent()) == null) {
            stack.set(bankComponent(), new Bank(serverLevel.getGameTime(), 0L));
        }
    }

    @Override
    public IECOTier getTier() {
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
        return TYPES;
    }

    @Override
    public double getIdleDrain() {
        return IDLE_DRAIN;
    }

    @Override
    public ECOCellType getCellType() {
        return CELL_TYPE;
    }

    // Nothing to partition and nothing to fuzz-match: it holds the one type it grew itself. AE2's default
    // isEditable dereferences getConfigInventory() without a null check, so the workbench stays off on purpose.
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
        // unsupported: one fixed type
    }

    /**
     * Our own stock line. eco's byte line never runs for this cell: its tooltip starts with
     * {@code stack.getItem() instanceof ECOStorageCellItem}, and this item is deliberately not one - the
     * same reason the infinite concrete matrix shows no byte figure either. A byte line would say nothing
     * here anyway: the total is unbounded and the used figure is always zero.
     *
     * <p>No server clock, or a cell that has never been stamped, means no number to claim - the line stays
     * off rather than guessing.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                 List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        long now = SingularityCellHandler.serverGameTime();
        Bank bank = stack.get(bankComponent());
        if (now < 0L || bank == null) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.neoecoprototype.singularity_stock",
                appeng.core.localization.Tooltips.ofNumber(bankOf(bank, now)))
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    /**
     * The preview reads the same arithmetic the network does, and never stamps: this runs on the client too,
     * and a client-side write to the stack would not reach the server. An unstamped cell previews empty.
     */
    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack stack) {
        if (!AEConfig.instance().isTooltipShowCellContent()) {
            return Optional.empty();
        }
        Bank bank = stack.get(bankComponent());
        long now = SingularityCellHandler.serverGameTime();
        if (bank == null || now < 0L) {
            return Optional.empty();
        }
        long ready = bankOf(bank, now);
        List<GenericStack> content = ready > 0L
                ? List.of(new GenericStack(SINGULARITY, ready))
                : List.of();
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, false, true));
    }
}

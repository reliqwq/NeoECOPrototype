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
     * How many are ready on the stack in hand, or -1 while it has never been stamped. One rule for both
     * places that display it: the item's own hover text and Jade's drive panel.
     */
    public static long stockOf(ItemStack stack, long nowGameTime) {
        Bank bank = stack.get(bankComponent());
        return bank == null ? -1L : bankOf(bank, nowGameTime);
    }

    /**
     * The cell needs one world tick to count from, and there is exactly one moment that happens: the first
     * time a server-side reader asks the mounted cell what it holds ({@link SingularityCellHandler.Cell}).
     *
     * <p>Carrying it is deliberately not such a moment. {@code Item#inventoryTick} runs over all forty-one
     * carried slots on every holder, which is both the one place this cell would need a ticker and the reason
     * a cell merely sitting in an inventory was already producing - the rule a player can be told is the
     * simpler one: it counts once it is in a drive.
     *
     * <p>{@code mayWrite} is what enforces that, and it is decided by the caller from the *level*, not from
     * {@code FMLEnvironment.dist}: in single-player the integrated server runs inside the client's dist, so a
     * dist check there would switch off legitimate stamping along with the unwanted kind. The unwanted kind is
     * real - a tooltip or item-list read that walks the cell's statistics stamps *its own copy* of the stack,
     * and that write never reaches the server, but the copy then shows a stock the server never granted (a
     * fresh cell in a creative tab was counting from world tick 0 for exactly this reason).
     */
    public static Bank stampIfNeeded(ItemStack stack, long nowGameTime, boolean mayWrite) {
        Bank bank = stack.get(bankComponent());
        if (bank != null) {
            return bank;
        }
        if (nowGameTime < 0L || !mayWrite) {
            return null;
        }
        Bank fresh = new Bank(nowGameTime, 0L);
        stack.set(bankComponent(), fresh);
        return fresh;
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
     * <p>No clock at all, or a cell that has never been stamped, means no number to claim - the line stays
     * off rather than guessing. The clock comes from the level this tooltip is being built for, which is the
     * one number a multiplayer client does have; see {@link SingularityCellHandler#clockFor}.
     */
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context,
                                 List<Component> tooltip, net.minecraft.world.item.TooltipFlag flag) {
        // The context can be handed over as null by a caller outside the game's own tooltip path.
        long now = SingularityCellHandler.clockFor(context == null ? null : context.level());
        long stock = now < 0L ? -1L : stockOf(stack, now);
        if (stock < 0L) {
            return;
        }
        tooltip.add(Component.translatable("tooltip.neoecoprototype.singularity_stock",
                appeng.core.localization.Tooltips.ofNumber(stock))
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    /**
     * The preview reads the same arithmetic the network does, and never stamps: this runs on the client too,
     * and a client-side write to the stack would not reach the server. An unstamped cell previews empty.
     *
     * <p>This one line stays off on a multiplayer client where the stock line above does not: the preview is
     * built from the stack alone, with no level in the call to borrow the clock from. The number is the same
     * one either way, and it is readable from the hover text, so the preview is the cheaper half to lose.
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

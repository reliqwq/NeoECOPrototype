package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
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
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

/**
 * A cell that grows something out of nothing and takes nothing in.
 *
 * <p>AE2's {@code StorageCell} has no tick method and the L1 drive has no ticker at all, so the bank is
 * never accumulated - it is <em>derived</em> from the world clock whenever somebody asks. Two numbers live
 * on the stack (the world tick it started from, and how much the network has drawn), everything else is
 * arithmetic, and a read never writes. That is what lets the same cell work mounted in our drive, in an
 * eco one, or lying in a chest.
 *
 * <p>The registered instance grows AE2's singularity at the speed the server config says. A scripted one -
 * see {@code StorageMatrixBuilder#grows} - carries its own content and its own batch figures on the item,
 * which is why {@link #content()}, {@link #perBatch()} and {@link #ticksPerBatch()} are instance methods
 * rather than more statics: what a pack makes has to be readable without a registry lookup, and the
 * matrix registry freezes before any cell is ever asked a question.
 */
public final class SimplifySingularityCellItem extends Item implements IBasicECOCellItem {

    /** The one thing the registered cell ever holds. */
    public static AEItemKey singularity() {
        return SingularityKey.SINGULARITY;
    }

    /** The one thing the meteorite-dust member of the family ever holds: AE2's own sky dust. */
    public static AEItemKey skyStoneDust() {
        return SkyStoneDustKey.SKY_STONE_DUST;
    }

    /**
     * Resolved on first use instead of in this class's initialiser. Our own item registration is what
     * loads this class, and AE2 binds its registry objects during its own RegisterEvent, so reading one
     * statically is a startup crash whenever the loader happens to run us first (issue #6).
     */
    private static final class SingularityKey {
        private static final AEItemKey SINGULARITY = AEItemKey.of(AEItems.SINGULARITY);
    }

    /**
     * The same lazy rule as {@link SingularityKey}, and the same reason: AE2 binds its registry objects
     * during its own RegisterEvent, and this class is loaded by our item registration.
     *
     * <p>What it points at changed on 2026-11-06. This used to be our own {@code
     * neoecoprototype:sky_stone_dust}, an item whose display name in both of our lang files was copied
     * straight from AE2 - "Sky Stone Dust" and 陨石粉 are exactly what {@code ae2:sky_dust} is called - so
     * one dissociation table could pay out two different items under one name, in the same screen as AE2's
     * own 陨石 block. The duplicate is gone and the cell grows AE2's material, which is also what makes it
     * the sibling of the singularity cell rather than a parallel invention.</p>
     */
    private static final class SkyStoneDustKey {
        private static final AEItemKey SKY_STONE_DUST = AEItemKey.of(AEItems.SKY_DUST);
    }

    /** One type, one row. */
    public static final int TYPES = 1;

    /** Infinity in the byte column, like the other endless members of this family. */
    public static final long REPORTED_BYTES = Long.MAX_VALUE;

    public static final double IDLE_DRAIN = 1.0D;

    /**
     * Its own cell type rather than the shared item row: eco's storage host would otherwise read this as one
     * more ordinary matrix, and "1 / 1 types against an unbounded budget" says nothing a player can act on.
     * Scripted members of this family keep this type too, which puts them in the same "one row, no numbers"
     * place as the registered one regardless of whether they grow items, fluids or chemicals.
     */
    public static final ECOCellType CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype.singularity"), TYPES, false);

    /** The meteorite-dust member's own row, for the same reason as above. */
    public static final ECOCellType SKY_STONE_DUST_CELL_TYPE = new ECOCellType(
            Component.translatable("eco_cell_type.neoecoprototype.sky_stone_dust"), TYPES, false);

    /**
     * What a registered member of the family grows and which two config numbers it runs on.
     *
     * <p>Suppliers rather than values, because the config has to be read fresh every time (a cached
     * {@code static final int} would freeze the first value the server ever saw), and data carried by the
     * members rather than a {@code switch} over them, which is how this repository writes a family.</p>
     */
    public record SelfGrow(java.util.function.Supplier<AEKey> content,
                           java.util.function.LongSupplier ticksPerBatch,
                           java.util.function.LongSupplier amountPerBatch,
                           ECOCellType cellType) {
    }

    /** The cell that shipped first: AE2's singularity, on the {@code singularity_cell} config pair. */
    public static final SelfGrow SINGULARITY = new SelfGrow(
            SimplifySingularityCellItem::singularity,
            NeoECOPrototypeServerConfig::singularityCellTicksPerBatch,
            NeoECOPrototypeServerConfig::singularityCellAmountPerBatch,
            CELL_TYPE);

    /** 陨石粉, on its own {@code sky_stone_dust_cell} pair - same defaults, separately tunable. */
    public static final SelfGrow SKY_STONE_DUST = new SelfGrow(
            SimplifySingularityCellItem::skyStoneDust,
            NeoECOPrototypeServerConfig::skyStoneDustCellTicksPerBatch,
            NeoECOPrototypeServerConfig::skyStoneDustCellAmountPerBatch,
            SKY_STONE_DUST_CELL_TYPE);

    /** Whether a panel row lookup is looking at any member of this family. */
    public static boolean isSelfGrowType(ECOCellType cellType) {
        return CELL_TYPE.equals(cellType) || SKY_STONE_DUST_CELL_TYPE.equals(cellType);
    }

    /**
     * All of the cell's state. {@code startGameTime} is the world tick it began counting from; {@code drawn}
     * is how many of its own content the network has already taken out of it.
     */
    public record Bank(long startGameTime, long drawn) {
        public static final Codec<Bank> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                Codec.LONG.fieldOf("start_game_time").forGetter(Bank::startGameTime),
                Codec.LONG.fieldOf("drawn").forGetter(Bank::drawn)).apply(instance, Bank::new));
        public static final StreamCodec<io.netty.buffer.ByteBuf, Bank> STREAM_CODEC =
                ByteBufCodecs.fromCodec(CODEC);
    }

    /**
     * What this instance grows and how fast, or null on the registered one.
     *
     * <p>Its own object rather than fields on the item, because an {@code Item} cannot be built after the
     * registry freezes - which is exactly when a pack's cell would first be interesting to read. The arithmetic
     * and the "does this cell pay that key" rule live here so both are reachable without an item at all.
     *
     * <p>The content is a supplier whose first read is kept, because a script builds this during the item
     * registration event and {@code MekanismAPI.CHEMICAL_REGISTRY.get(id)} is not readable at that moment -
     * it answers {@code NullPointerException: this.defaultValue is null} (measured 10-21, client startup).
     * Everything that asks what a cell grows - a drive, a tooltip, Jade - runs after the freeze.
     */
    public static final class Growth {
        private final java.util.function.Supplier<AEKey> content;
        @Nullable
        private AEKey resolved;
        private final long perBatch;
        private final long ticksPerBatch;

        public Growth(java.util.function.Supplier<AEKey> content, long perBatch, long ticksPerBatch) {
            if (content == null) {
                throw new IllegalArgumentException("A growing cell has to say what it grows");
            }
            if (perBatch < 1L || ticksPerBatch < 1L) {
                throw new IllegalArgumentException("A growing cell's batch and interval must be positive: "
                        + perBatch + "/" + ticksPerBatch);
            }
            this.content = content;
            this.perBatch = perBatch;
            this.ticksPerBatch = ticksPerBatch;
        }

        /** How much of {@link #content()} is ready for a bank that started at {@code startGameTime}. */
        public long readyAt(long startGameTime, long drawn, long nowGameTime) {
            return bankOf(new Bank(startGameTime, drawn), nowGameTime, perBatch, ticksPerBatch);
        }

        /** Whether this cell hands out {@code what} at all. */
        public boolean pays(AEKey what) {
            return content().equals(what);
        }

        /** The one thing this cell ever holds, read on first use and then kept. */
        public AEKey content() {
            AEKey own = resolved;
            if (own == null) {
                own = java.util.Objects.requireNonNull(content.get(),
                        "a growing cell's bound key resolved to nothing");
                resolved = own;
            }
            return own;
        }

        /** How much the bank jumps per batch. */
        public long perBatch() {
            return perBatch;
        }

        /** How long between jumps, in world ticks. */
        public long ticksPerBatch() {
            return ticksPerBatch;
        }
    }

    @Nullable
    private final Growth growth;
    private final SelfGrow profile;

    /** The registered cell: it grows AE2's singularity and takes its speed from the server config. */
    public SimplifySingularityCellItem(Item.Properties properties) {
        this(properties, SINGULARITY);
    }

    /** A registered member that grows something else, on its own two config numbers. */
    public SimplifySingularityCellItem(Item.Properties properties, SelfGrow profile) {
        super(properties);
        this.growth = null;
        this.profile = profile;
    }

    /**
     * A scripted sibling: it grows {@code content} instead of a singularity, jumping {@code perBatch} at a
     * time every {@code ticksPerBatch} world ticks. Both figures are required, because a half-set cell would
     * slow down whenever somebody edits the server config, which is not what a pack asked for.
     */
    public SimplifySingularityCellItem(Item.Properties properties,
                                       java.util.function.Supplier<AEKey> content,
                                       long perBatch, long ticksPerBatch) {
        super(properties);
        this.growth = new Growth(content, perBatch, ticksPerBatch);
        this.profile = SINGULARITY;
    }

    /** Its own figures if a pack wrote them, otherwise its member's - read from the config now. */
    public Growth growth() {
        return growth != null
                ? growth
                : new Growth(profile.content(), profile.amountPerBatch().getAsLong(),
                        profile.ticksPerBatch().getAsLong());
    }

    /** The panel row this cell's family owns; a pack's member shares the registered one's row. */
    public ECOCellType cellType() {
        return profile.cellType();
    }

    /** What this cell hands out, and the only thing it will ever hold. */
    public AEKey content() {
        return growth().content();
    }

    /** How much the bank jumps per batch. */
    public long perBatch() {
        return growth().perBatch();
    }

    /** How long between jumps, in world ticks. */
    public long ticksPerBatch() {
        return growth().ticksPerBatch();
    }

    /** Whether this cell was written by a pack rather than registered by us. */
    public boolean isScripted() {
        return growth != null;
    }

    static DataComponentType<Bank> bankComponent() {
        return cn.dancingsnow.neoecoprototype.registration.ModRegistration.SINGULARITY_CELL_BANK.get();
    }

    /**
     * How many are ready at {@code nowGameTime} on the registered cell.
     *
     * <p>There is no bank ceiling to stop at - the stock is a long and it is allowed to reach the top of one
     * - so the only limit left is arithmetic: the cumulative total saturates instead of wrapping. A batch is
     * what the bank jumps by; it does not drip.
     */
    public static long bankOf(Bank bank, long nowGameTime) {
        long interval = NeoECOPrototypeServerConfig.singularityCellTicksPerBatch();
        long batch = NeoECOPrototypeServerConfig.singularityCellAmountPerBatch();
        return bankOf(bank, nowGameTime, batch, interval);
    }

    /** The same arithmetic with a cell's own figures, which is what a scripted sibling runs on. */
    public static long bankOf(Bank bank, long nowGameTime, long batch, long interval) {
        long batches = Math.max(0L, (nowGameTime - bank.startGameTime()) / interval);
        long produced = batches > Long.MAX_VALUE / batch ? Long.MAX_VALUE : batches * batch;
        long stored = produced - bank.drawn;
        return stored <= 0L ? 0L : stored;
    }

    /** How many are ready on this cell, with its own batch and interval. */
    public long readyAt(Bank bank, long nowGameTime) {
        Growth own = growth();
        return own.readyAt(bank.startGameTime(), bank.drawn(), nowGameTime);
    }

    /**
     * How many are ready on the stack in hand, or -1 while it has never been stamped. One rule for both
     * places that display it: the item's own hover text and Jade's drive panel.
     */
    public static long stockOf(ItemStack stack, long nowGameTime) {
        Bank bank = stack.get(bankComponent());
        if (bank == null) {
            return -1L;
        }
        return stack.getItem() instanceof SimplifySingularityCellItem cell
                ? cell.readyAt(bank, nowGameTime)
                : bankOf(bank, nowGameTime);
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
        return content().getType();
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
        return cellType();
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
                appeng.core.localization.Tooltips.ofNumber(stock), stockSubject())
                .withStyle(net.minecraft.ChatFormatting.AQUA));
    }

    /**
     * What the stock line names as its content. A pack's member of this family grows whatever key its item
     * was built with, so the sentence cannot say "singularity" for the whole family; both places that print
     * the figure (here and Jade's drive panel) take the subject from the cell itself.
     */
    public Component stockSubject() {
        return content().getDisplayName();
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
        long ready = readyAt(bank, now);
        List<GenericStack> content = ready > 0L
                ? List.of(new GenericStack(content(), ready))
                : List.of();
        return Optional.of(new StorageCellTooltipComponent(List.of(), content, false, true));
    }
}

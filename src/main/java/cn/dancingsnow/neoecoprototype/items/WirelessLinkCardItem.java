package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import java.util.List;
import javax.annotation.Nullable;

/**
 * Joins two network devices that are nowhere near each other, without a cable between them.
 *
 * <p>Every gesture is a sneak-right-click, because that is the one that reaches a device without opening
 * it: with a card in hand and the sneak key held, the block is never asked for its own interaction - which
 * is also how AE2's memory card gets to a machine.</p>
 *
 * <p>The card is only the carrier. Once an edge exists the pair lives on the level ({@link WirelessLinks}),
 * so losing the card costs a finished link nothing.</p>
 */
public class WirelessLinkCardItem extends Item implements HeldItemUIMenuType.HeldItemUI {

    private static final String TAG_ENDPOINT = "linkCardEndpoint";
    private static final String TAG_GROUP = "linkCardGroup";
    private static final String TAG_DIM = "dim";
    private static final String TAG_POS = "pos";

    public WirelessLinkCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public boolean stillValid(HeldItemUIMenuType.HeldItemUIHolder holder) {
        ItemStack held = holder.player.getItemInHand(holder.hand);
        return !held.isEmpty() && held.getItem() == this;
    }

    @Override
    public ModularUI createUI(HeldItemUIMenuType.HeldItemUIHolder holder) {
        return WirelessLinkManagementUI.create(holder);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.current(cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS)) {
            // Switched off for this release: answer PASS rather than consume, so the device under the cursor
            // behaves exactly as it would with any other item in hand and no half-gesture is left recorded.
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            // The client has nothing to predict: the card's contents change on the server and sync back.
            return InteractionResult.PASS;
        }
        ItemStack card = context.getItemInHand();
        GlobalPos clicked = GlobalPos.of(level.dimension(), context.getClickedPos());
        GlobalPos held = readEndpoint(card);
        WirelessLinks.Reply report;
        if (held != null && held.equals(clicked)) {
            // Taking links off moved onto its own card on 2026-11-04. This tap used to be the only way out of
            // a link, and it was also the tap a player reached for when they meant to re-mark - so the one
            // gesture that looked like re-marking was the one that dismantled everything on that device. The
            // card now refuses, keeps its mark, and names the other card.
            report = WirelessLinks.Reply.of("teardown_moved",
                    "this card still holds " + WirelessLinks.labelText(serverLevel, clicked)
                            + "; taking links off is the unlink card's job now - sneak-right-click the device"
                            + " with that one",
                    WirelessLinks.label(serverLevel, clicked));
        } else if (WirelessLinks.isController(serverLevel, clicked)) {
            // A controller is the network's knob, never one end of a link: its own node answers
            // getMaxChannels() == 0 (measured in the client as a(max=0,used=0)), so a wireless edge ending
            // on a controller would carry nothing - and while we were trying to bind to one, the raise
            // gesture below could never be reached at all, which is exactly what it looked like in the
            // client on 2026-10-29 (34 taps, zero "raised to" lines, because the card held a mark).
            // Every tap moves AE2's channel mode one step up, and that is all this block does: the raise buys
            // nothing, so there is no purchase to report - what a link costs is the AE it draws every tick,
            // billed by WirelessLinks#billNow and not by this tap.
            var moved = WirelessLinks.raiseModeReply(serverLevel, clicked);
            String verdict = WirelessLinks.controllerState(serverLevel, clicked);
            report = "CONTROLLER_ONLINE".equals(verdict) || "NO_CONTROLLER".equals(verdict)
                    ? moved
                    : WirelessLinks.Reply.of("controller_verdict",
                            moved.english() + " | AE2 reads this controller group as " + verdict
                                    + ", which means the network is getting no channels at all"
                                    + " until the shape is fixed",
                            moved.asComponent(), verdict);
        } else if (held == null) {
            if (!WirelessLinks.isDevice(serverLevel, clicked)) {
                report = WirelessLinks.Reply.of("no_device_at",
                        "no network device at " + WirelessLinks.labelText(serverLevel, clicked),
                        WirelessLinks.label(serverLevel, clicked));
            } else {
                writeEndpoint(card, clicked);
                // The log keeps the full instrument (block id, per-face ledger, ad-hoc arithmetic, AE2's own
                // machine table); the player gets which device the card now holds, named.
                report = WirelessLinks.Reply.of("marked", "marked "
                        + WirelessLinks.status(serverLevel, ModRegistration.WIRELESS_LINKS, clicked),
                        WirelessLinks.label(serverLevel, clicked));
            }
        } else if (WirelessLinks.endpointIsGone(serverLevel, held)) {
            // The square this card marked no longer holds a device, so the mark is void and this tap is
            // taken for the re-mark it is asking to be. Measured in the client on 2026-10-29: a player who
            // breaks the marked device is left holding a card that answers "that is not a network device:
            // <that square>" to every further tap and cannot be re-marked at all - re-marking only happens
            // while the card holds nothing, and useOn never fires on the air where the device used to be.
            // Only a confirmed-empty square does this; an asleep one keeps its mark (see endpointIsGone).
            if (!WirelessLinks.isDevice(serverLevel, clicked)) {
                report = WirelessLinks.Reply.of("marked_gone_no_device",
                        "the device this card marked at " + WirelessLinks.labelText(serverLevel, held)
                                + " is gone, and there is no network device at "
                                + WirelessLinks.labelText(serverLevel, clicked) + " to mark instead",
                        WirelessLinks.label(serverLevel, held), WirelessLinks.label(serverLevel, clicked));
            } else {
                writeEndpoint(card, clicked);
                report = WirelessLinks.Reply.of("marked_gone_remarked",
                        "the device this card marked at " + WirelessLinks.labelText(serverLevel, held)
                                + " is gone, so it marks this one instead - "
                                + WirelessLinks.status(serverLevel, ModRegistration.WIRELESS_LINKS, clicked),
                        WirelessLinks.label(serverLevel, held),
                        WirelessLinks.channelsText(serverLevel, clicked));
            }
        } else {
            report = WirelessLinks.bindReply(serverLevel, ModRegistration.WIRELESS_LINKS, held, clicked,
                    readGroup(card));
            // The mark deliberately survives a successful join: one tap on the hub and then one tap per
            // device is how a player wires a hub to several spokes. What brakes that are all in the world's
            // config - links per device, distance, dimensions, and the AE each live link draws per tick -
            // and the reply line reads off AE2's own numbers.
        }
        NeoECOPrototype.LOGGER.info("wireless link card: {}", report.english());
        // The key goes out unrendered, so the client writes the line in the player's own language; the English
        // sentence stays in the log because that is what the guards and the diagnostics read.
        player.sendSystemMessage(report.asComponent());
        return InteractionResult.CONSUME;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack card = player.getItemInHand(hand);
        if (!(level instanceof ServerLevel serverLevel)
                || !cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.current(
                cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS)) {
            // Only on the server, and not one word while the switch is off: the tooltip already says the
            // feature is off, and a reply line here would take back the PASS that the off path promises to
            // the block under the cursor.
            return InteractionResultHolder.pass(card);
        }
        if (player.isShiftKeyDown()) {
            if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer
                    && HeldItemUIMenuType.openUI(serverPlayer, hand)) {
                return InteractionResultHolder.consume(card);
            }
            return InteractionResultHolder.pass(card);
        }
        // Plain right-click in mid-air drops the mark and nothing else. This release used to be bundled into
        // "click the marked device again", which also deleted its links - now that the unlink card owns that,
        // a card that can be marked has to be able to be un-marked, or a hub in hand is a hub for good.
        var held = readEndpoint(card);
        if (held == null) {
            return InteractionResultHolder.pass(card);
        }
        clearEndpoint(card);
        NeoECOPrototype.LOGGER.info("wireless link card: dropped its mark, it held {}",
                WirelessLinks.describe(held));
        player.displayClientMessage(Component.translatable(
                "item.neoecoprototype.wireless_link_card.unmarked",
                WirelessLinks.label(serverLevel, held)), true);
        return InteractionResultHolder.consume(card);
    }

    /**
     * What a sneak-right-click in mid-air prints: the two channel numbers of the network the card is standing
     * on - what AE2 hands that device and what is already allocated. Nothing else, because on 2026-11-04 the
     * player holding it asked for exactly that pair and called the rest noise.
     *
     * <p>A card holding nothing gets no number, and that is the honest half. AE2 derives channel capacity
     * from one network-wide {@code ChannelMode} and offers no setter anywhere in its jar (measured on
     * 19.2.17: 0 classes name {@code setMaxChannels} or {@code setChannelCapacity}, against 13 that name
     * {@code getUsedChannels}), so there is no world-wide pool to read - only the network a marked device
     * sits on, which is what {@link WirelessLinks#status} reports.
     */
    public static Component balanceReport(ServerLevel level, ItemStack card) {
        var held = readEndpoint(card);
        return held == null
                ? Component.translatable("item.neoecoprototype.wireless_link_card.balance_none")
                : WirelessLinks.channelsText(level, held);
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                               TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var held = readEndpoint(stack);
        tooltip.add(held == null
                ? Component.translatable("item.neoecoprototype.wireless_link_card.tooltip")
                : Component.translatable("item.neoecoprototype.wireless_link_card.holds",
                        heldLabel(context.level(), held)));
        // Named because the honest reading is the opposite of what a player reaches for: a device whose own
        // grid still has power looks alive after its link is gone, so "is the link there" has exactly one
        // answer source - the live= count the card prints.
        tooltip.add(Component.translatable("item.neoecoprototype.wireless_link_card.check"));
        if (!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.current(cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS)) {
            // The switch answers PASS with no reply line, so a player holding this card and tapping a
            // controller gets the same nothing they would get from a broken item - the tooltip is the only
            // place left where the card can say the feature exists but is off in this world.
            tooltip.add(Component.translatable("item.neoecoprototype.wireless_link_card.disabled"));
        }
    }

    // ==================== what the card is carrying ====================

    static String readGroup(ItemStack card) {
        return WirelessLinks.normalizeGroup(customData(card).getString(TAG_GROUP));
    }

    static void writeGroup(ItemStack card, String value) {
        CompoundTag data = customData(card);
        String group = WirelessLinks.normalizeGroup(value);
        if (group.isEmpty()) {
            data.remove(TAG_GROUP);
        } else {
            data.putString(TAG_GROUP, group);
        }
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    /** The endpoint the player marked, or null when the card is empty. */
    @Nullable
    static GlobalPos readEndpoint(ItemStack card) {
        CompoundTag endpoint = customData(card).getCompound(TAG_ENDPOINT);
        String dimension = endpoint.getString(TAG_DIM);
        if (dimension.isEmpty()) {
            return null;
        }
        // The tooltip reads this on hover, so a corrupted mark has to answer "nothing" instead of throwing.
        var location = ResourceLocation.tryParse(dimension);
        if (location == null) {
            return null;
        }
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, location),
                BlockPos.of(endpoint.getLong(TAG_POS)));
    }

    private static void writeEndpoint(ItemStack card, GlobalPos at) {
        CompoundTag data = customData(card);
        CompoundTag endpoint = new CompoundTag();
        endpoint.putString(TAG_DIM, at.dimension().location().toString());
        endpoint.putLong(TAG_POS, at.pos().asLong());
        data.put(TAG_ENDPOINT, endpoint);
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    private static void clearEndpoint(ItemStack card) {
        CompoundTag data = customData(card);
        if (!data.contains(TAG_ENDPOINT)) {
            return;
        }
        data.remove(TAG_ENDPOINT);
        card.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    /** A copy, always: the component itself stays immutable and {@code CustomData#copyTag} says so. */
    private static CompoundTag customData(ItemStack card) {
        return card.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
    }

    /**
     * The tooltip's label for the marked square. A tooltip is built on the client, so the name resolves in the
     * player's language; when there is no client level to ask (a tooltip built off the item's own context, or a
     * mark in a dimension nobody has opened) it prints the plain dimension-and-coordinate form rather than
     * guessing at a name.
     */
    private static Object heldLabel(@Nullable Level looking, GlobalPos held) {
        return looking == null
                ? Component.literal(WirelessLinks.describe(held))
                : WirelessLinks.label(looking, held);
    }
}

package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
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
public class WirelessLinkCardItem extends Item {

    private static final String TAG_ENDPOINT = "linkCardEndpoint";
    private static final String TAG_DIM = "dim";
    private static final String TAG_POS = "pos";

    public WirelessLinkCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            // The client has nothing to predict: the card's contents change on the server and sync back.
            return InteractionResult.PASS;
        }
        ItemStack card = context.getItemInHand();
        GlobalPos clicked = GlobalPos.of(level.dimension(), context.getClickedPos());
        GlobalPos held = readEndpoint(card);
        String report;
        if (held != null && held.equals(clicked)) {
            // Clicking the marked block again is the way out of a link the player can see.
            report = WirelessLinks.unlink(serverLevel, ModRegistration.WIRELESS_LINKS, clicked);
            clearEndpoint(card);
        } else if (held == null) {
            if (!WirelessLinks.isDevice(serverLevel, clicked)) {
                report = "no network device at " + WirelessLinks.describe(clicked);
            } else {
                writeEndpoint(card, clicked);
                report = "marked "
                        + WirelessLinks.status(serverLevel, ModRegistration.WIRELESS_LINKS, clicked);
            }
        } else {
            report = WirelessLinks.bind(serverLevel, ModRegistration.WIRELESS_LINKS, held, clicked);
            if (report.startsWith("linked ") || report.startsWith("recorded ")) {
                clearEndpoint(card);
            }
        }
        NeoECOPrototype.LOGGER.info("wireless link card: {}", report);
        player.sendSystemMessage(Component.literal("wireless link: " + report));
        return InteractionResult.CONSUME;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                               TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        var held = readEndpoint(stack);
        tooltip.add(held == null
                ? Component.translatable("item.neoecoprototype.wireless_link_card.tooltip")
                : Component.translatable("item.neoecoprototype.wireless_link_card.holds",
                        WirelessLinks.describe(held)));
    }

    // ==================== what the card is carrying ====================

    /** The endpoint the player marked, or null when the card is empty. */
    @Nullable
    private static GlobalPos readEndpoint(ItemStack card) {
        CompoundTag endpoint = customData(card).getCompound(TAG_ENDPOINT);
        String dimension = endpoint.getString(TAG_DIM);
        if (dimension.isEmpty()) {
            return null;
        }
        return GlobalPos.of(ResourceKey.create(Registries.DIMENSION, ResourceLocation.parse(dimension)),
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
}

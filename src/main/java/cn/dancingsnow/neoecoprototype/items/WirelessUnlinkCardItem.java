package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Takes every wireless link off the device under the cursor.
 *
 * <p>It carries no mark and holds no state: the card you click with decides whether a device gains links or
 * loses them, so a mis-click with the link card can no longer dismantle a hub. That split is the whole
 * reason this item exists - the gesture it performs used to be the link card's "click the block you already
 * marked again" branch, which meant the one tap that looked like re-marking was the one tap that deleted
 * everything.</p>
 *
 * <p>Like the link card, every gesture is a sneak-right-click: that is the one that reaches a device without
 * opening it.</p>
 */
public class WirelessUnlinkCardItem extends Item {

    public WirelessUnlinkCardItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (!cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.current(
                cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig.WIRELESS_LINKS)) {
            return InteractionResult.PASS;
        }
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (!(level instanceof ServerLevel serverLevel) || player == null) {
            return InteractionResult.PASS;
        }
        GlobalPos clicked = GlobalPos.of(level.dimension(), context.getClickedPos());
        WirelessLinks.Reply report;
        if (WirelessLinks.isController(serverLevel, clicked)) {
            // A controller never carries an edge, so this tap has nothing to take off it. Saying so beats
            // the alternative: a silent PASS that reads as a broken card.
            report = WirelessLinks.Reply.of("controller_no_edge", describe(clicked)
                    + " is a controller, and a controller never carries a wireless edge - use the link card"
                    + " on it to raise that network's channel step", describe(clicked));
        } else if (!WirelessLinks.isDevice(serverLevel, clicked)) {
            report = WirelessLinks.Reply.of("not_a_device",
                    "that is not a network device: " + describe(clicked), describe(clicked));
        } else {
            report = WirelessLinks.unlinkReply(serverLevel, ModRegistration.WIRELESS_LINKS, clicked);
        }
        NeoECOPrototype.LOGGER.info("wireless unlink card: {}", report.english());
        player.sendSystemMessage(report.asComponent());
        return InteractionResult.CONSUME;
    }

    private static String describe(GlobalPos at) {
        return WirelessLinks.describe(at);
    }
}

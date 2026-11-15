package cn.dancingsnow.neoecoprototype.event;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.level.BlockEvent;

/** Starts an auto-connect attempt after a player places a device while carrying a marked wireless card. */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class WirelessAutoConnectEvents {

    private WirelessAutoConnectEvents() {
    }

    @SubscribeEvent
    public static void onPlaced(BlockEvent.EntityPlaceEvent event) {
        if (event.isCanceled() || !(event.getLevel() instanceof net.minecraft.server.level.ServerLevel server)
                || !(event.getEntity() instanceof ServerPlayer player) || player.isFakePlayer()) {
            return;
        }
        WirelessLinks.queueAutoConnect(player, server, event.getPos(), ModRegistration.WIRELESS_LINKS);
    }
}

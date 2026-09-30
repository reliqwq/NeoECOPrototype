package cn.dancingsnow.neoecoprototype.event;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Wearing the plushie grants night vision, and a named doll may grant one effect of its own.
 * NeoForge 1.21.1 has no {@code onEquippedTick} hook, so this is the tick-event equivalent: refresh
 * the effects from the server while the doll occupies the head.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class FumoEquipmentEffects {
    /** Long enough that topping it up halfway leaves a wide margin before expiry. */
    private static final int EFFECT_TICKS = 600;

    private FumoEquipmentEffects() {
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack head = player.getItemBySlot(EquipmentSlot.HEAD);
        if (!head.is(ModRegistration.FUMO_RELIQWQ_ITEM.get())) return;
        MobEffectInstance vision = player.getEffect(MobEffects.NIGHT_VISION);
        // Top it up before it lapses, otherwise the effect blinks at the expiry boundary.
        if (vision == null || vision.getDuration() < EFFECT_TICKS / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_TICKS,
                    0, true, false, false));
        }
        Holder<MobEffect> extra = FumoItem.extraEffect(head);
        if (extra == null) return;
        MobEffectInstance extraInstance = player.getEffect(extra);
        if (extraInstance == null || extraInstance.getDuration() < EFFECT_TICKS / 2) {
            // Drawn as an ordinary, iconated effect, unlike the night vision: a healing doll is
            // something the wearer should be able to see working.
            player.addEffect(new MobEffectInstance(extra, EFFECT_TICKS, 0, false, true, true));
        }
    }
}

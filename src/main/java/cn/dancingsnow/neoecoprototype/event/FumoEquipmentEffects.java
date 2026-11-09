package cn.dancingsnow.neoecoprototype.event;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Wearing one of the four dolls grants night vision, some of them grant effects of their own, one of them
 * feeds AE into the chargeable things the wearer carries, and every one of them makes creepers keep their
 * distance. NeoForge 1.21.1 has no {@code onEquippedTick} hook, so this is the tick-event equivalent:
 * refresh all of that from the server while the doll occupies the head.
 *
 * <p>The creeper answer is the same rule a placed doll runs, and it lives in {@link PlushieScare}: the
 * wearer looks for creepers around itself rather than every creeper looking for a wearer.
 *
 * <p>With Curios present a doll may also sit in that mod's head accessory slot (「首饰」), which is read
 * only when the helmet slot holds nothing wearable. The effects are the same either way; the armour is
 * not, and the mana a doll carries is the one attribute the accessory slot does grant - see
 * {@link cn.dancingsnow.neoecoprototype.integration.curios.CuriosDollSupport}.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class FumoEquipmentEffects {
    /** Long enough that topping it up halfway leaves a wide margin before expiry. */
    private static final int EFFECT_TICKS = 600;

    /**
     * A charging doll pays out in one sweep every this many of the wearer's own ticks, rather than asking
     * forty-one stacks for their energy capability every tick.
     */
    private static final int CHARGE_SWEEP_TICKS = 5;

    private FumoEquipmentEffects() {
    }

    /** True on the ticks a worn charging doll pays out; read off the wearer's clock, not the world's. */
    public static boolean chargeDue(int playerTickCount) {
        return playerTickCount % CHARGE_SWEEP_TICKS == 0;
    }

    /**
     * Pushes at most {@code aeForSweep} into whatever the player is carrying that answers the item energy
     * capability, in slot order until the budget runs out. The whole inventory is swept, the held hotbar
     * slot included, so a tool in the wearer's hand tops up like anything else. Returns the amount that
     * was accepted, which is what the guard reads.
     */
    public static int chargeInventory(ServerPlayer player, double aeForSweep) {
        Inventory inventory = player.getInventory();
        double budget = Math.max(0.0D, aeForSweep);
        int charged = 0;
        for (int slot = 0; slot < inventory.getContainerSize() && budget > 0.0D; slot++) {
            ItemStack carried = inventory.getItem(slot);
            if (carried.isEmpty()) continue;
            IEnergyStorage storage = Capabilities.EnergyStorage.ITEM.getCapability(carried, null);
            if (storage == null || !storage.canReceive()) continue;
            int accepted = storage.receiveEnergy((int) Math.min(budget, Integer.MAX_VALUE), false);
            if (accepted <= 0) continue;
            charged += accepted;
            budget -= accepted;
        }
        return charged;
    }

    @SubscribeEvent
    public static void onPlayerTick(PlayerTickEvent.Post event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        ItemStack worn = player.getItemBySlot(EquipmentSlot.HEAD);
        FumoItem.DollStats stats = FumoItem.statsOf(worn);
        if (stats == null && ModList.get().isLoaded("curios")) {
            // The curio lookup parses nothing but the entity's attachment, but it is still work no player
            // without a doll needs, so it only runs when the helmet slot came up empty.
            worn = cn.dancingsnow.neoecoprototype.integration.curios.CuriosDollSupport.wornDoll(player);
            stats = FumoItem.statsOf(worn);
        }
        if (stats == null) return;
        // Before the effect hand-outs, which stop early: the scare belongs to the doll, not to whoever
        // wears the one that also heals.
        if (PlushieScare.sweepDue(player.level().getGameTime())) {
            PlushieScare.scareAround(player.level(), player.position());
        }
        MobEffectInstance vision = player.getEffect(MobEffects.NIGHT_VISION);
        // Top it up before it lapses, otherwise the effect blinks at the expiry boundary.
        if (vision == null || vision.getDuration() < EFFECT_TICKS / 2) {
            player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, EFFECT_TICKS,
                    0, true, false, false));
        }
        for (FumoItem.DollEffect extra : FumoItem.extraEffects(worn)) {
            MobEffectInstance held = player.getEffect(extra.effect());
            // Top up whatever is about to lapse, and never come down: where a potion or another source
            // already gives the wearer a better level, this leaves it alone.
            if (held == null || held.getAmplifier() < extra.amplifier()
                    || held.getDuration() < EFFECT_TICKS / 2) {
                // Drawn as ordinary, iconated effects, unlike the night vision: a doll that heals or tips
                // the loot tables is something the wearer should be able to see working.
                player.addEffect(new MobEffectInstance(extra.effect(), EFFECT_TICKS, extra.amplifier(),
                        false, true, true));
            }
        }
        // Last, and only for the doll that carries the stat: nothing above depends on it, and a doll with
        // zero here never asks a single stack for its capability. The rate itself is the server's to set,
        // so a pack that turns it off costs no capability lookup either.
        double aePerTick = NeoECOPrototypeServerConfig.fumoChargeAePerTick();
        if (stats.chargeAEPerTick() > 0.0D && aePerTick > 0.0D && chargeDue(player.tickCount)) {
            chargeInventory(player, aePerTick * CHARGE_SWEEP_TICKS);
        }
    }
}

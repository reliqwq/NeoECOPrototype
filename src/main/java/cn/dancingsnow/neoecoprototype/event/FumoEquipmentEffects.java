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
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.tick.PlayerTickEvent;

/**
 * Wearing the plushie grants night vision, and a named doll may grant one effect of its own.
 * NeoForge 1.21.1 has no {@code onEquippedTick} hook, so this is the tick-event equivalent: refresh
 * the effects from the server while the doll occupies the head.
 *
 * <p>It also gives creepers the cat answer: a plushie is a stuffed cat, so a creeper keeps its
 * distance from anyone wearing or holding one.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class FumoEquipmentEffects {
    /** Long enough that topping it up halfway leaves a wide margin before expiry. */
    private static final int EFFECT_TICKS = 600;
    /** Creeper#registerGoals' own numbers for cats and ocelots: 6 blocks, 1.2 walking, 1.2 sprinting. */
    private static final float SCARE_DISTANCE = 6.0F;
    private static final double SCARE_WALK_SPEED = 1.2D;
    private static final double SCARE_SPRINT_SPEED = 1.2D;

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

    /**
     * Gives every creeper the goal vanilla gives it against cats and ocelots - same class, same numbers,
     * same priority 3 - with "what to run from" narrowed to players who have a plushie on their head or
     * in either hand. Placed dolls are deliberately not covered: {@link AvoidEntityGoal} only sees
     * entities, so a block would need its own goal implementation.
     */
    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        // Mob's own constructor calls registerGoals() only server-side, so an empty goal list on the
        // client is vanilla's convention rather than an oversight - adding a goal there would never
        // tick and would leave our list the only one that differs.
        if (event.getLevel().isClientSide()) return;
        if (event.getEntity() instanceof Creeper creeper) {
            creeper.goalSelector.addGoal(3, new DollFleeGoal(creeper));
        }
    }

    /** A creeper runs from this entity only while it is carrying a plushie where a cat would be seen. */
    public static final class DollFleeGoal extends AvoidEntityGoal<Player> {
        DollFleeGoal(PathfinderMob mob) {
            super(mob, Player.class, SCARE_DISTANCE, SCARE_WALK_SPEED, SCARE_SPRINT_SPEED,
                    FumoEquipmentEffects::fearsPlushie);
        }
    }

    private static boolean fearsPlushie(LivingEntity entity) {
        if (!(entity instanceof Player player)) return false;
        return isPlushie(player.getItemBySlot(EquipmentSlot.HEAD))
                || isPlushie(player.getMainHandItem())
                || isPlushie(player.getOffhandItem());
    }

    private static boolean isPlushie(ItemStack stack) {
        return stack.is(ModRegistration.FUMO_RELIQWQ_ITEM.get());
    }
}

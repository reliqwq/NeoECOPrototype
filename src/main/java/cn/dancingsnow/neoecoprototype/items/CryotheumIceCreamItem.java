package cn.dancingsnow.neoecoprototype.items;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/**
 * The dessert, and the only thing it does beyond the FOOD component: it leaves frost on the drinker.
 *
 * <p>The cold is deliberately not a mob effect. {@code MobEffects} carries 39 entries on 1.21.1 and none of
 * them is a freezing (counted, not recalled), because powder snow's cold lives on the entity instead -
 * {@code Entity#setTicksFrozen} - and the numbers there are what makes 80 ticks a safe amount:
 * {@code BASE_TICKS_REQUIRED_TO_FREEZE = 140} has to be reached before {@code DamageSources.freeze} starts
 * landing once per {@code FREEZE_HURT_FREQUENCY = 40} ticks. So this is just over half the threshold: the
 * screen picks up the frost ({@code getPercentFrozen} reports it), and nobody takes damage for finishing a
 * dessert.</p>
 *
 * <p>It is a max, not an addition, on purpose - vanilla already walks this same field down every tick
 * somebody is not standing in snow, and a second bottle adding onto the first would be a stacking rule
 * nobody asked for.</p>
 */
public final class CryotheumIceCreamItem extends DrinkableFoodItem {
    /** Four seconds at 20 ticks per second, against the 140 ticks vanilla needs to start hurting. */
    public static final int FREEZE_TICKS = 80;

    public CryotheumIceCreamItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity livingEntity) {
        var remainder = super.finishUsingItem(stack, level, livingEntity);
        if (!level.isClientSide) {
            livingEntity.setTicksFrozen(Math.max(livingEntity.getTicksFrozen(), FREEZE_TICKS));
        }
        return remainder;
    }
}

package cn.dancingsnow.neoecoprototype.items;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;

/**
 * A food that is drunk rather than eaten.
 *
 * <p>One method, and it is the whole difference: {@code Item#getUseAnimation} on 1.21.1 answers
 * {@code EAT} for anything carrying a FOOD component (measured off the bytecode: {@code stack.has(FOOD)
 * ? EAT : NONE}), so a jar of something cold would otherwise play the eating animation and the eating
 * sound. {@link net.minecraft.world.item.HoneyBottleItem} is vanilla's own precedent for the other half -
 * a food that overrides this to {@code DRINK} - and the sound follows the same value:
 * {@code LivingEntity} line 3166 plays {@code SoundEvents.GENERIC_DRINK} exactly when the animation is
 * {@code DRINK}, and line 3170 plays the eat sound when it is {@code EAT}. So this one override moves
 * both the arm and the noise, and nothing else about the food changes: the eat duration still comes from
 * the FOOD component, and {@code usingConvertsTo} still hands the container back at the end.
 */
public class DrinkableFoodItem extends Item {

    public DrinkableFoodItem(Properties properties) {
        super(properties);
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return UseAnim.DRINK;
    }
}

package cn.dancingsnow.neoecoprototype.integration.jei;

import appeng.api.stacks.GenericStack;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicKey;
import mezz.jei.api.ingredients.IIngredientType;
import tamaized.ae2jeiintegration.api.integrations.jei.IngredientConverter;

/**
 * Teaches ae2jeiintegration's generic-stack bridge about music data. Data Energistics' reassembler page
 * walks {@code IngredientConverters} looking for one that claims a {@code GenericStack}; ours is the only
 * one that claims {@link MusicKey}, so that page draws the real icon instead of an iconless wrapped stack.
 *
 * <p>The class names an optional mod's interface, so it is only ever instantiated behind
 * {@code ModList.isLoaded("ae2jeiintegration")} in {@link NeoECOPrototypeJeiPlugin}.
 */
public final class MusicDataIngredientConverter implements IngredientConverter<MusicKey> {

    @Override
    public IIngredientType<MusicKey> getIngredientType() {
        return MusicDataIngredient.TYPE;
    }

    @Override
    public MusicKey getIngredientFromStack(GenericStack stack) {
        return stack != null && stack.what() instanceof MusicKey key ? key : null;
    }

    @Override
    public GenericStack getStackFromIngredient(MusicKey ingredient) {
        return MusicDataIngredient.oneMusic();
    }
}

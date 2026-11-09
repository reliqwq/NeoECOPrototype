package cn.dancingsnow.neoecoprototype.integration.jei;

import appeng.api.stacks.GenericStack;
import cn.dancingsnow.neoecoprototype.client.key.MusicKeyRenderer;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicKey;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.ingredients.IIngredientHelper;
import mezz.jei.api.ingredients.IIngredientRenderer;
import mezz.jei.api.ingredients.IIngredientType;
import mezz.jei.api.ingredients.subtypes.UidContext;
import mezz.jei.api.registration.IModIngredientRegistration;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Makes music data a first-class JEI ingredient. Data Energistics' own key list is a closed enum, so
 * without this the reassembler page can only fall back to {@code WrappedGenericStack}, which has no icon
 * and no name. The converter that maps it back onto a {@code GenericStack} lives in
 * {@link MusicDataIngredientConverter}, because that half names an optional mod.
 *
 * <p>Everything here is reached only while Data Energistics is installed; see the plugin's
 * {@code registerIngredients}.
 */
public final class MusicDataIngredient {

    public static final IIngredientType<MusicKey> TYPE = new IIngredientType<>() {
        @Override
        public Class<? extends MusicKey> getIngredientClass() {
            return MusicKey.class;
        }

        @Override
        public String getUid() {
            return "neoecoprototype:music_data";
        }
    };

    public static void register(IModIngredientRegistration registration) {
        registration.register(TYPE, List.of(MusicKey.INSTANCE), new Helper(), new Renderer());
    }

    private MusicDataIngredient() {
    }

    private static final class Helper implements IIngredientHelper<MusicKey> {
        @Override
        public IIngredientType<MusicKey> getIngredientType() {
            return TYPE;
        }

        @Override
        public String getDisplayName(MusicKey key) {
            return key.getDisplayName().getString();
        }

        @Override
        public String getUniqueId(MusicKey key, UidContext context) {
            return MusicKey.ID.toString();
        }

        @Override
        public ResourceLocation getResourceLocation(MusicKey key) {
            return MusicKey.ID;
        }

        @Override
        public MusicKey copyIngredient(MusicKey key) {
            return key;
        }

        @Override
        public String getErrorInfo(MusicKey key) {
            return key.toString();
        }
    }

    /** Draws the same 16x16 icon the terminal and the jukebox use, so the key reads as one thing. */
    private static final class Renderer implements IIngredientRenderer<MusicKey> {
        @Override
        public void render(GuiGraphics graphics, MusicKey key) {
            graphics.blit(MusicKeyRenderer.icon(), 0, 0, 0, 0, 16, 16);
        }

        @Override
        public void getTooltip(ITooltipBuilder tooltip, MusicKey key, TooltipFlag flag) {
            tooltip.add(key.getDisplayName());
        }

        @Override
        public List<Component> getTooltip(MusicKey key, TooltipFlag flag) {
            return List.of(key.getDisplayName());
        }
    }

    /** Only used by {@link MusicDataIngredientConverter}; kept here so the two halves agree on amounts. */
    public static GenericStack oneMusic() {
        return new GenericStack(MusicKey.INSTANCE, 1);
    }
}

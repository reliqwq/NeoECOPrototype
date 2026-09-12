package cn.dancingsnow.neoecoprototype.tooltip;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.player.ItemTooltipEvent;

/**
 * Appends one custom description line to selected addon items.
 *
 * <p>Doing this through {@link ItemTooltipEvent} instead of overriding
 * {@code appendHoverText} keeps every description in one place and avoids
 * subclassing items that are already created through shared registration
 * helpers. The text itself lives in the language files, so only the key is
 * referenced here.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class SimplifyTooltipHandler {

    private static final String PIGCAT_CELL_KEY = "item.neoecoprototype.pigcat_storage_cell.desc";
    private static final String PIGCAT_HOUSING_KEY =
            "item.neoecoprototype.pigcat_storage_matrix_housing.desc";
    private static final String CONCRETE_CELL_KEY =
            "item.neoecoprototype.simplify_concrete_storage_cell.desc";
    private static final String L4_COMPONENT_KEY =
            "item.neoecoprototype.simplify_storage_component_4m.desc";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        String key = descriptionKey(event.getItemStack());
        if (key != null) {
            event.getToolTip().add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
        }
    }

    /** @return the language key of the extra line, or null when the item has none. */
    private static String descriptionKey(ItemStack stack) {
        if (stack.is(ModRegistration.PIGCAT_STORAGE_CELL.get())) {
            return PIGCAT_CELL_KEY;
        }
        if (stack.is(ModRegistration.PIGCAT_STORAGE_MATRIX_HOUSING.get())) {
            return PIGCAT_HOUSING_KEY;
        }
        if (stack.is(ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get())) {
            return CONCRETE_CELL_KEY;
        }
        // "ECO - L4 存储组件" / "ECO - L4 Storage Component".
        if (stack.is(ModRegistration.SIMPLIFY_STORAGE_COMPONENT_4M.get())) {
            return L4_COMPONENT_KEY;
        }
        return null;
    }

    private SimplifyTooltipHandler() {
    }
}

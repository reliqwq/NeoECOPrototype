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
 * Appends the addon's own description lines to selected items, and the red "not implemented" note to
 * the Trinity parts.
 *
 * <p>Doing this through {@link ItemTooltipEvent} instead of overriding
 * {@code appendHoverText} keeps every description in one place and avoids
 * subclassing items that are already created through shared registration
 * helpers. The text itself lives in the language files, so only the key is
 * referenced here.
 */
@EventBusSubscriber(modid = NeoECOPrototype.MOD_ID)
public final class SimplifyTooltipHandler {

    private static final String PIGMEE_CELL_KEY = "item.neoecoprototype.pigmee_storage_cell.desc";
    private static final String PIGMEE_HOUSING_KEY =
            "item.neoecoprototype.pigmee_storage_matrix_housing.desc";
    private static final String CONCRETE_CELL_KEY =
            "item.neoecoprototype.simplify_concrete_storage_cell.desc";
    private static final String SINGULARITY_CELL_KEY =
            "item.neoecoprototype.simplify_singularity_cell.desc";
    private static final String L4_COMPONENT_KEY =
            "item.neoecoprototype.simplify_storage_component_4m.desc";
    /** The 3-type small bulk matrix is upgraded to 10 types with a crafting-table recipe. */
    private static final String SMALL_BULK_CELL_KEY =
            "item.neoecoprototype.simplify_small_bulk_storage_cell.desc";
    /** The expansion card has no screen, so its rules have to be readable on the item itself. */
    private static final String EXPANSION_CARD_KEY =
            "item.neoecoprototype.simplify_structure_expansion_card.desc";
    /** Each disk says what the swap costs, what comes back, and the one thing it does not carry over. */
    private static final String MIGRATION_DRIVE_KEY =
            "item.neoecoprototype.migration_drive.desc";
    private static final String MIGRATION_DRIVE_CREATIVE_KEY =
            "item.neoecoprototype.migration_drive_creative.desc";
    /** Trinity is implemented but withheld from players; every one of its parts says so in red. */
    private static final String NOT_IMPLEMENTED_KEY = "item.neoecoprototype.not_implemented";

    @SubscribeEvent
    public static void onItemTooltip(ItemTooltipEvent event) {
        ItemStack stack = event.getItemStack();
        String key = descriptionKey(stack);
        if (key != null) {
            // One component per line: the renderer draws a newline inside a component as a glyph box
            // instead of breaking the row. getString() resolves through the client language, which is
            // the only one there is here - ItemTooltipEvent never fires on a dedicated server.
            for (String line : TooltipLines.split(Component.translatable(key).getString())) {
                event.getToolTip().add(Component.literal(line).withStyle(ChatFormatting.GRAY));
            }
        }
        if (isNotImplemented(stack)) {
            event.getToolTip().add(Component.translatable(NOT_IMPLEMENTED_KEY).withStyle(ChatFormatting.RED));
        }
    }

    /**
     * Trinity controller and its three modules work, but are unreleased; see the JEI gate. The wireless link
     * card joins them for the same reason this release. Public because a guard asks it on a stack rather than
     * trusting that the withheld list was edited in every place that matters.
     */
    public static boolean isNotImplemented(ItemStack stack) {
        return stack.is(ModRegistration.SIMPLIFY_TRINITY_CONTROLLER_ITEM.get())
                || stack.is(ModRegistration.SIMPLIFY_TRINITY_STORAGE_MODULE_ITEM.get())
                || stack.is(ModRegistration.SIMPLIFY_TRINITY_COMPUTATION_MODULE_ITEM.get())
                || stack.is(ModRegistration.SIMPLIFY_TRINITY_CRAFTING_MODULE_ITEM.get())
                // Withheld with it: no tab entry and no recipe this release, so a /give'd card must not read
                // like finished content.
                || stack.is(ModRegistration.WIRELESS_LINK_CARD.get());
    }

    /** @return the language key of the extra line, or null when the item has none. */
    private static String descriptionKey(ItemStack stack) {
        if (stack.is(ModRegistration.PIGMEE_STORAGE_CELL.get())) {
            return PIGMEE_CELL_KEY;
        }
        if (stack.is(ModRegistration.PIGMEE_STORAGE_MATRIX_HOUSING.get())) {
            return PIGMEE_HOUSING_KEY;
        }
        if (stack.is(ModRegistration.SIMPLIFY_CONCRETE_STORAGE_CELL.get())) {
            return CONCRETE_CELL_KEY;
        }
        if (stack.is(ModRegistration.SIMPLIFY_SINGULARITY_CELL.get())) {
            return SINGULARITY_CELL_KEY;
        }
        var smallBulkCell = ModRegistration.SIMPLIFY_SMALL_BULK_CELL;
        if (smallBulkCell != null && stack.is(smallBulkCell.get())) {
            return SMALL_BULK_CELL_KEY;
        }
        // "ECO - L4 存储组件" / "ECO - L4 Storage Component".
        if (stack.is(ModRegistration.SIMPLIFY_STORAGE_COMPONENT_4M.get())) {
            return L4_COMPONENT_KEY;
        }
        if (stack.is(ModRegistration.SIMPLIFY_STRUCTURE_EXPANSION_CARD.get())) {
            return EXPANSION_CARD_KEY;
        }
        if (stack.getItem() instanceof cn.dancingsnow.neoecoprototype.items.SimplifyMigrationDriveItem drive) {
            return drive.rung() == cn.dancingsnow.neoecoprototype.items.SimplifyMigrationDriveItem.Rung
                    .STRAIGHT_TO_NINE ? MIGRATION_DRIVE_CREATIVE_KEY : MIGRATION_DRIVE_KEY;
        }
        return null;
    }

    private SimplifyTooltipHandler() {
    }
}

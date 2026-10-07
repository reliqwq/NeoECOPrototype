package cn.dancingsnow.neoecoprototype.integration.appflux;

import appeng.api.upgrades.Upgrades;
import appeng.core.localization.GuiText;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.glodblock.github.appflux.common.AFSingletons;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * Optional AppliedFlux integration. This class is loaded only when appflux is present.
 *
 * <p>Our own FE housing, like every other medium in this family has one; the cells are that housing
 * plus our own 1M / 4M component.
 */
public final class FeStorageIntegration {
    /** One card per machine, which is what appflux asks for on AE2's own interface and provider. */
    private static final int INDUCTION_CARDS_PER_MACHINE = 1;

    public static void register(DeferredRegister<Item> items) {
        ModRegistration.OPTIONAL_FE_CELL_HOUSING = items.register("simplify_fe_cell_housing",
                () -> new Item(new Item.Properties()));
        ModRegistration.OPTIONAL_FE_CELL_1M = items.register("simplify_fe_storage_cell_1m",
                () -> new SimplifyFeStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyFeStorageCellItem::getFluxCellType, SimplifyFeStorageCellItem.BYTES_1M));
        ModRegistration.OPTIONAL_FE_CELL_4M = items.register("simplify_fe_storage_cell_4m",
                () -> new SimplifyFeStorageCellItem(new Item.Properties().stacksTo(1),
                        SimplifyFeStorageCellItem::getFluxCellType, SimplifyFeStorageCellItem.BYTES_4M));
    }

    /**
     * Lets the induction card - "allows an AE device to receive power" - into our two interfaces and
     * our pattern provider. appflux registers it against AE2's own four, and the card's behaviour is
     * mixed into AE2's {@code InterfaceLogic} and {@code PatternProviderLogic}, which our machines use
     * directly rather than subclass, so the only thing missing was this allowance.
     *
     * <p>Both families join appflux's own tooltip group instead of standing on their own: AE2's card
     * tooltip collapses every machine that shares a group into that one line, and leaves an ungrouped
     * machine to print its own name. Our names differ from AE2's, so an ungrouped add would grow the
     * card's "can be installed in" list with our L1 machines - the allowance is the point, the extra
     * lines are not.
     */
    public static void registerUpgradeCards() {
        var card = AFSingletons.INDUCTION_CARD;
        var interfaceGroup = GuiText.Interface.getTranslationKey();
        // appflux's own key for the provider family; it has no constant for it, it passes the string.
        var patternProviderGroup = "group.pattern_provider.name";
        // Explicit List.<ItemLike>of: letting javac infer the element type across our block items and
        // part items makes it compute an intersection it cannot resolve.
        for (var item : java.util.List.<net.minecraft.world.level.ItemLike>of(
                ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_ITEM.get(),
                ModRegistration.POWERED_INTERFACE_PART.get(),
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_ITEM.get(),
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_PART.get())) {
            Upgrades.add(card, item, INDUCTION_CARDS_PER_MACHINE, interfaceGroup);
        }
        for (var item : java.util.List.<net.minecraft.world.level.ItemLike>of(
                ModRegistration.SIMPLIFY_PATTERN_PROVIDER_ITEM.get(),
                ModRegistration.CABLE_PATTERN_PROVIDER_PART.get())) {
            Upgrades.add(card, item, INDUCTION_CARDS_PER_MACHINE, patternProviderGroup);
        }
    }

    private FeStorageIntegration() {
    }
}

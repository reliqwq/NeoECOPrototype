package cn.dancingsnow.neoecoprototype.part;

import appeng.api.parts.IPartItem;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.parts.PartModel;
import appeng.parts.crafting.PatternProviderPart;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;

/** Cable-mounted L1 pattern provider with the addon's configurable slot count. */
public class SimplifyPatternProviderPart extends PatternProviderPart {
    private static final ResourceLocation BASE_MODEL = NeoECOPrototype.id("part/pattern_provider");
    // AE2's provider has no status models of its own; it borrows the interface's, which is what
    // draws the purple channel indicator over the base model's status ring.
    private static final PartModel MODELS_OFF = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_off"));
    private static final PartModel MODELS_ON = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_on"));
    private static final PartModel MODELS_HAS_CHANNEL = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_has_channel"));

    public SimplifyPatternProviderPart(IPartItem<?> partItem) {
        super(partItem);
    }

    @Override
    public void openMenu(net.minecraft.world.entity.player.Player player, MenuHostLocator locator) {
        MenuOpener.open(ModRegistration.L1_PATTERN_PROVIDER_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(net.minecraft.world.entity.player.Player player, appeng.menu.ISubMenu subMenu) {
        MenuOpener.returnTo(ModRegistration.L1_PATTERN_PROVIDER_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    protected PatternProviderLogic createLogic() {
        return new PatternProviderLogic(getMainNode(), this, 27);
    }

    @Override
    public PartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        return isPowered() ? MODELS_ON : MODELS_OFF;
    }
}


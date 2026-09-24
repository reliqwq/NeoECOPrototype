package cn.dancingsnow.neoecoprototype.part;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.api.parts.IPartItem;
import appeng.helpers.InterfaceLogic;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import appeng.parts.PartModel;
import appeng.parts.misc.InterfacePart;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;

/** Cable-mounted AE2 interface that also provides 200 AE/t. */
public class PoweredInterfacePart extends InterfacePart implements IPassiveEnergyGenerator {
    private static final ResourceLocation BASE_MODEL = NeoECOPrototype.id("part/powered_me_interface");
    private static final PartModel MODELS_OFF = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_off"));
    private static final PartModel MODELS_ON = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_on"));
    private static final PartModel MODELS_HAS_CHANNEL = new PartModel(BASE_MODEL,
            ResourceLocation.fromNamespaceAndPath("ae2", "part/interface_has_channel"));

    private boolean suppressed;

    public PoweredInterfacePart(IPartItem<?> partItem) {
        super(partItem);
        getMainNode().setIdlePowerUsage(0).addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        return new InterfaceLogic(getMainNode(), this, getPartItem().asItem(), 18);
    }

    @Override
    public void openMenu(net.minecraft.world.entity.player.Player player, MenuHostLocator locator) {
        MenuOpener.open(ModRegistration.L1_POWERED_INTERFACE_MENU.get(), player, locator);
    }

    @Override
    public void returnToMainMenu(net.minecraft.world.entity.player.Player player, appeng.menu.ISubMenu subMenu) {
        MenuOpener.returnTo(ModRegistration.L1_POWERED_INTERFACE_MENU.get(), player, subMenu.getLocator());
    }

    @Override
    public double getRate() {
        return suppressed ? 0.0D : 200.0D;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }

    @Override
    public PartModel getStaticModels() {
        if (isActive() && isPowered()) {
            return MODELS_HAS_CHANNEL;
        }
        return isPowered() ? MODELS_ON : MODELS_OFF;
    }
}


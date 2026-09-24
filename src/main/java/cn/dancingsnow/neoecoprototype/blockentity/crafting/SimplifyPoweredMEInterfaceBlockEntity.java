package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.misc.InterfaceBlockEntity;
import appeng.helpers.InterfaceLogic;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** AE2 interface logic plus a passive 200 AE/t network power source. */
public class SimplifyPoweredMEInterfaceBlockEntity extends InterfaceBlockEntity
        implements IPassiveEnergyGenerator {
    public static final double GENERATION_RATE = 200.0D;

    private boolean suppressed;

    public SimplifyPoweredMEInterfaceBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    @Override
    protected InterfaceLogic createLogic() {
        return new InterfaceLogic(getMainNode(), this, getItemFromBlockEntity(), 18);
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
        return suppressed ? 0.0D : GENERATION_RATE;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }
}


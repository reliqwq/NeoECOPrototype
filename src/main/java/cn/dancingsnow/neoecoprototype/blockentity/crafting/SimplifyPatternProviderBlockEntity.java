package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.blockentity.crafting.PatternProviderBlockEntity;
import appeng.helpers.patternprovider.PatternProviderLogic;
import appeng.menu.MenuOpener;
import appeng.menu.locator.MenuHostLocator;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** AE2 pattern provider behavior under the addon's green visual variant. */
public class SimplifyPatternProviderBlockEntity extends PatternProviderBlockEntity {
    /**
     * Three rows of nine: {@code l1_pattern_provider.png} draws exactly this many pattern slots, and the
     * shipped GUI style declares the same grid. {@code GuiGrooveAlignmentTest} reads this number, so
     * raising it without drawing another row now goes red instead of spilling slots onto the panel.
     */
    public static final int PATTERN_SLOTS = 27;

    public SimplifyPatternProviderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
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
        return new PatternProviderLogic(getMainNode(), this, PATTERN_SLOTS);
    }
}

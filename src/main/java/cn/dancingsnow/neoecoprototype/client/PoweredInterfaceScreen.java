package cn.dancingsnow.neoecoprototype.client;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import cn.dancingsnow.neoecoprototype.mixin.SlotYAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;

/** Dedicated L1 interface screen for its alternating row style. */
public class PoweredInterfaceScreen extends InterfaceScreen<InterfaceMenu> {
    public PoweredInterfaceScreen(InterfaceMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }

    @Override
    protected void init() {
        super.init();
        moveSecondRow(SlotSemantics.CONFIG);
        moveSecondRow(SlotSemantics.STORAGE);
    }

    private void moveSecondRow(appeng.menu.SlotSemantic semantic) {
        var slots = getMenu().getSlots(semantic);
        for (int i = 9; i < slots.size(); i++) {
            SlotYAccessor accessor = (SlotYAccessor) (Object) slots.get(i);
            accessor.neoecoprototype$setY(slots.get(i).y + 36);
        }
    }
}

package cn.dancingsnow.neoecoprototype.client;

import appeng.client.gui.implementations.InterfaceScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.InterfaceMenu;
import cn.dancingsnow.neoecoprototype.gui.GuiGrooveAlignment;
import cn.dancingsnow.neoecoprototype.mixin.SlotYAccessor;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import java.util.IdentityHashMap;
import java.util.Map;

/** Dedicated L1 interface screen for its alternating row style. */
public class PoweredInterfaceScreen extends InterfaceScreen<InterfaceMenu> {
    private final Map<Slot, Integer> originalSlotYs = new IdentityHashMap<>();

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
            var slot = slots.get(i);
            SlotYAccessor accessor = (SlotYAccessor) (Object) slot;
            accessor.neoecoprototype$setY(secondRowY(slot, originalSlotYs));
        }
    }

    // Cache the first absolute Y so repeated init calls never compound the extra row-pair offset.
    static int secondRowY(Slot slot, Map<Slot, Integer> originalSlotYs) {
        return originalSlotYs.computeIfAbsent(slot, key -> key.y) + GuiGrooveAlignment.SECOND_ROW_SHIFT;
    }
}

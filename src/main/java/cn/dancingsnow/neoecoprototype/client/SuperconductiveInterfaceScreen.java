package cn.dancingsnow.neoecoprototype.client;

import appeng.menu.implementations.InterfaceMenu;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Inventory;
import appeng.client.gui.style.ScreenStyle;

/** Six-row screen for the 8192-stock superconductive interface. */
public class SuperconductiveInterfaceScreen extends PoweredInterfaceScreen {
    public SuperconductiveInterfaceScreen(InterfaceMenu menu, Inventory inventory, Component title, ScreenStyle style) {
        super(menu, inventory, title, style);
    }
}

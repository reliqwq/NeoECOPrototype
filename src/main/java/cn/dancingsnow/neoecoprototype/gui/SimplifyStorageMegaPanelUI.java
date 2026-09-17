package cn.dancingsnow.neoecoprototype.gui;

import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.world.item.ItemStack;

/** Small local filter panel for L1 MEGA-type matrices. */
public final class SimplifyStorageMegaPanelUI {
    private SimplifyStorageMegaPanelUI() {}

    public static UIElement create(SimplifyStorageHostBlockEntity host) {
        UIElement panel = new UIElement().layout(l -> l.width(103).height(76));
        Button previous = new Button().noText().setOnServerClick(e -> host.changeSelectedSmallBulkDrive(-1));
        Button next = new Button().noText().setOnServerClick(e -> host.changeSelectedSmallBulkDrive(1));
        previous.layout(l -> l.width(10).height(10).left(0).top(0));
        next.layout(l -> l.width(10).height(10).left(92).top(0));
        panel.addChild(previous).addChild(next);
        int typeLimit = Math.max(1, Math.min(10, host.getSelectedSmallBulkTypeLimit()));
        for (int index = 0; index < typeLimit; index++) {
            ItemSlot slot = new FilterSlot(host, index).xeiPhantom();
            int x = (index % 5) * 18;
            int y = 14 + (index / 5) * 18;
            slot.getStyle().backgroundTexture(IGuiTexture.EMPTY);
            slot.layout(l -> l.width(18).height(18).left(x).top(y));
            panel.addChild(slot);
        }
        return panel;
    }

    private static final class FilterSlot extends ItemSlot {
        private final SimplifyStorageHostBlockEntity host;
        private final int slot;

        private FilterSlot(SimplifyStorageHostBlockEntity host, int slot) {
            this.host = host;
            this.slot = slot;
            bind(DataBindingBuilder.itemStackS2C(() -> host.getSmallBulkFilter(slot))
                    .remoteSetter(value -> setValue(value, false)).build());
            addEventListener(UIEvents.MOUSE_DOWN, event -> {
                if (event.button == 1 && !getValue().isEmpty()) {
                    setValue(ItemStack.EMPTY, true);
                    event.hasHandler = true;
                    event.stopImmediatePropagation();
                } else if (event.button == 0) {
                    var player = Minecraft.getInstance().player;
                    ItemStack carried = player == null ? ItemStack.EMPTY : player.containerMenu.getCarried();
                    if (!carried.isEmpty()) {
                        setValue(carried, true);
                        event.hasHandler = true;
                        event.stopImmediatePropagation();
                    }
                }
            }, true);
        }

        @Override
        public ItemSlot setValue(ItemStack value, boolean notify) {
            ItemStack filtered = value == null || value.isEmpty()
                    ? ItemStack.EMPTY : value.copyWithCount(1);
            super.setValue(filtered, notify);
            if (notify) host.setSmallBulkFilterFromClient(slot, filtered);
            return this;
        }
    }
}

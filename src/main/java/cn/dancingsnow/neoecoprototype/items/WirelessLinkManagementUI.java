package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.lowdragmc.lowdraglib2.gui.factory.HeldItemUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.SyncStrategy;
import com.lowdragmc.lowdraglib2.gui.texture.ColorBorderTexture;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.GuiTextureGroup;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextField;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;
import java.util.List;

/**
 * The card's manager screen: name a group, see every recorded link, and unlink a whole group at once.
 *
 * <p>Two labels per link rather than one, because one line cannot hold a group name and both ends: the first
 * version put the group, both endpoints, the state word and the dimension id on one 304 px row, and the player
 * got it back as {@code minecraft:ove...} - cut exactly where the row stopped identifying which link it was.
 * Splitting the pair over two lines, and dropping the dimension id (the labels already add it when the two
 * ends are in different dimensions), keeps the longest realistic row inside the panel.
 *
 * <p>Every string goes through the lang files. The rows are built on the server and synced as components, so
 * a Chinese player reads Chinese rows; the block names inside them were already translatable, and the group
 * and state words were not.
 */
public final class WirelessLinkManagementUI {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 248;
    private static final int COLOR = 0x403e53;
    /** The palette violet, at three weights: a pale plate, the colour itself, and a pressed shade. */
    private static final int BUTTON_IDLE = 0xff_ee_e6_f7;
    private static final int BUTTON_HOVER = 0xff_d9_c8_f0;
    private static final int BUTTON_PRESSED = 0xff_c4_ad_e8;
    private static final int BUTTON_EDGE = 0xff_91_5d_cd;
    /** A field sits one shade lighter than a resting button, so the two do not read as the same control. */
    private static final int FIELD_FILL = 0xff_f7_f3_fc;

    private WirelessLinkManagementUI() {
    }

    public static ModularUI create(HeldItemUIMenuType.HeldItemUIHolder holder) {
        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(new ColorRectTexture(0xff_ef_f0_f4)));

        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.title"),
                Horizontal.CENTER, 8, 6, 324, 14));
        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.group"),
                Horizontal.LEFT, 8, 26, 78, 18));

        TextField groupField = new TextField().setAnyString()
                .setTextValidator(value -> value.codePointCount(0, value.length()) <= 32)
                .setCharValidator(character -> !Character.isISOControl(character));
        groupField.setText(WirelessLinkCardItem.readGroup(holder.itemStack));
        groupField.bind(DataBindingBuilder.string(
                () -> WirelessLinkCardItem.readGroup(holder.player.getItemInHand(holder.hand)),
                value -> WirelessLinkCardItem.writeGroup(holder.player.getItemInHand(holder.hand), value))
                .c2sStrategy(SyncStrategy.CHANGED_PERIODIC)
                .s2cStrategy(SyncStrategy.CHANGED_PERIODIC)
                .build());
        groupField.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(86).top(25).width(246).height(20));
        // The same light plate as the buttons: LDLib2's default is a near-black box, which on this sheet reads
        // as a hole rather than a field. The focus ring stays the library's, so "this is the field you are
        // typing in" still comes from one place.
        groupField.style(style -> style.background(plate(FIELD_FILL)));
        groupField.textFieldStyle(style -> style.textColor(COLOR).textShadow(false));
        root.addChild(groupField);

        StatusList status = new StatusList(holder);
        status.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(8).top(72).width(324).height(146));
        root.addChild(status);

        Button apply = button("gui.neoecoprototype.wireless_manager.apply_group", 8, 52, 100, () -> {
            if (!(holder.player instanceof ServerPlayer player)) {
                return;
            }
            ItemStack held = player.getItemInHand(holder.hand);
            String group = WirelessLinks.normalizeGroup(WirelessLinkCardItem.readGroup(held));
            WirelessLinkCardItem.writeGroup(held, group);
            // No mark means "the links this screen is showing", not "nothing": the list above the button is
            // what the player is naming, and a card that has never marked a device still has to be able to
            // name the links it lists. A mark narrows it to that device's links.
            var marked = WirelessLinkCardItem.readEndpoint(held);
            int moved = WirelessLinks.assignGroup(player.serverLevel(), ModRegistration.WIRELESS_LINKS,
                    marked, group);
            NeoECOPrototype.LOGGER.info("wireless manager: group '{}' applied to {}, {} link(s) moved",
                    WirelessLinks.groupLabel(group),
                    marked == null ? "every recorded link" : WirelessLinks.labelText(player.serverLevel(), marked),
                    moved);
            player.sendSystemMessage(Component.translatable("gui.neoecoprototype.wireless_manager.group_applied",
                    groupName(group)));
            if (moved > 0) {
                player.sendSystemMessage(Component.translatable(
                        "gui.neoecoprototype.wireless_manager.group_assigned", moved, groupName(group)));
            }
            status.refresh();
        });
        root.addChild(apply);

        Button refresh = button("gui.neoecoprototype.wireless_manager.refresh", 114, 52, 96,
                status::refresh);
        root.addChild(refresh);

        Button unlink = button("gui.neoecoprototype.wireless_manager.unlink_group", 216, 52, 116, () -> {
            if (!(holder.player instanceof ServerPlayer player)) {
                return;
            }
            String group = WirelessLinks.normalizeGroup(
                    WirelessLinkCardItem.readGroup(player.getItemInHand(holder.hand)));
            WirelessLinks.Reply reply = group.isEmpty()
                    ? WirelessLinks.Reply.of("group_required", "enter a named group before unlinking it")
                    : WirelessLinks.unlinkGroupReply(player.serverLevel(), ModRegistration.WIRELESS_LINKS, group);
            player.sendSystemMessage(reply.asComponent());
            status.refresh();
        });
        root.addChild(unlink);

        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.help"),
                Horizontal.LEFT, 8, 222, 324, 24));
        return new ModularUI(UI.of(root), holder.player);
    }

    /** A group as the player reads it: the entered name, or the language's word for "no group". */
    private static Component groupName(String group) {
        return group.isEmpty()
                ? Component.translatable("gui.neoecoprototype.wireless_manager.ungrouped")
                : Component.literal(group);
    }

    private static final class StatusList extends UIElement {
        /** Three links fit above the help line; a fourth would push the panel past a scale-4 screen. */
        private static final int SLOTS = 3;
        private static final int SLOT_HEIGHT = 36;
        private static final int FIRST_SLOT_Y = 20;

        private final HeldItemUIMenuType.HeldItemUIHolder holder;
        private final Component[] marked = new Component[1];
        private final Component[] head = new Component[SLOTS];
        private final Component[] tail = new Component[SLOTS];

        private StatusList(HeldItemUIMenuType.HeldItemUIHolder holder) {
            this.holder = holder;
            Arrays.fill(marked, Component.empty());
            Arrays.fill(head, Component.empty());
            Arrays.fill(tail, Component.empty());

            Label markedLine = label(Component.empty(), Horizontal.LEFT, 0, 0, 324, 18);
            markedLine.bind(DataBindingBuilder.componentS2C(() -> marked[0]).build());
            addChild(markedLine);

            for (int index = 0; index < SLOTS; index++) {
                int slot = index;
                Label top = label(Component.empty(), Horizontal.LEFT, 0, FIRST_SLOT_Y + index * SLOT_HEIGHT, 324, 18);
                top.bind(DataBindingBuilder.componentS2C(() -> head[slot]).build());
                addChild(top);
                Label bottom = label(Component.empty(), Horizontal.LEFT, 6,
                        FIRST_SLOT_Y + index * SLOT_HEIGHT + 16, 318, 18);
                bottom.bind(DataBindingBuilder.componentS2C(() -> tail[slot]).build());
                addChild(bottom);
            }
            refresh();
        }

        private void refresh() {
            Arrays.fill(marked, Component.empty());
            Arrays.fill(head, Component.empty());
            Arrays.fill(tail, Component.empty());
            if (!(holder.player instanceof ServerPlayer player)) {
                return;
            }
            var level = player.serverLevel();
            var endpoint = WirelessLinkCardItem.readEndpoint(holder.itemStack);
            if (endpoint != null) {
                marked[0] = Component.translatable("gui.neoecoprototype.wireless_manager.marked",
                        WirelessLinks.label(level, endpoint), WirelessLinks.channelsText(level, endpoint));
            }
            List<WirelessLinks.LinkSummary> rows = WirelessLinks.groupOverview(level, ModRegistration.WIRELESS_LINKS);
            if (rows.isEmpty()) {
                head[0] = Component.translatable("gui.neoecoprototype.wireless_manager.empty");
                return;
            }
            int shown = Math.min(SLOTS, rows.size());
            for (int index = 0; index < shown; index++) {
                WirelessLinks.LinkSummary row = rows.get(index);
                head[index] = Component.translatable("gui.neoecoprototype.wireless_manager.row_head",
                        groupName(row.group()), WirelessLinks.labelAnywhere(level, row.a()));
                tail[index] = Component.translatable("gui.neoecoprototype.wireless_manager.row_tail",
                        WirelessLinks.labelAnywhere(level, row.b()),
                        Component.translatable(row.live()
                                ? "gui.neoecoprototype.wireless_manager.live"
                                : "gui.neoecoprototype.wireless_manager.not_live"));
            }
            if (rows.size() > shown) {
                // Appended to the last shown link rather than taking its slot: a player told "one more exists"
                // still wants to see which three they are looking at.
                tail[shown - 1] = Component.empty()
                        .append(tail[shown - 1])
                        .append(Component.literal("  "))
                        .append(Component.translatable("gui.neoecoprototype.wireless_manager.more",
                                rows.size() - shown));
            }
        }
    }

    private static Label label(Component text, Horizontal alignment, int x, int y, int width, int height) {
        Label label = new Label();
        label.setText(text);
        label.textStyle(style -> style.textWrap(TextWrap.NONE)
                .textShadow(false).textColor(COLOR).textAlignHorizontal(alignment));
        label.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
        return label;
    }

    /**
     * A button that belongs to this panel rather than to LDLib2's default dark plate: the screen is a light
     * sheet, and a near-black button on it reads as a disabled control - measured in the client on 2026-11-13,
     * where he described the row of buttons as the one thing that looked wrong. Three states in our own violet
     * (the palette's 915DCD): a pale fill at rest, the violet itself under the cursor, and a darker violet while
     * pressed, so the click is visible without any text change.
     */
    private static Button button(String translationKey, int x, int y, int width, Runnable action) {
        Button button = new Button().setText(Component.translatable(translationKey));
        button.buttonStyle(style -> style
                .baseTexture(plate(BUTTON_IDLE))
                .hoverTexture(plate(BUTTON_HOVER))
                .pressedTexture(plate(BUTTON_PRESSED)));
        button.textStyle(style -> style.textShadow(false).textColor(COLOR));
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(18));
        button.setOnServerClick(event -> action.run());
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(translationKey)), null, null, null));
        return button;
    }

    /** One button face: a fill plus a one-pixel outline, both in the same violet family. */
    private static IGuiTexture plate(int fill) {
        return GuiTextureGroup.of(
                new ColorRectTexture(fill),
                new ColorBorderTexture(1, BUTTON_EDGE));
    }
}

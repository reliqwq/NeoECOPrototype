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
import dev.vfyjxf.taffy.style.AlignItems;
import dev.vfyjxf.taffy.style.FlexDirection;
import dev.vfyjxf.taffy.style.TaffyDisplay;
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
 *
 * <p>Laid out as flex columns and rows rather than absolute pixels, and with no bevelled frames: the panel is
 * a light sheet, and the one reading that ever corrected it came from a player looking at it ("按钮看着像禁用").
 * Nothing here is verifiable headless - the GameTest suite never loads these classes - so the layout claims
 * below are intent, and the client pass is the reading.
 */
public final class WirelessLinkManagementUI {

    private static final int WIDTH = 340;
    private static final int HEIGHT = 248;
    /** Primary text: a dark slate-violet, not black, so the sheet keeps its warmth. */
    private static final int COLOR = 0x403e53;
    /** Secondary text - the second line of a link and the help line - one step lighter, never the same. */
    private static final int COLOR_SUB = 0x6f6b80;
    /** The palette violet, at three weights: a pale plate, the colour itself, and a pressed shade. */
    private static final int BUTTON_IDLE = 0xff_ee_e6_f7;
    private static final int BUTTON_HOVER = 0xff_d9_c8_f0;
    private static final int BUTTON_PRESSED = 0xff_c4_ad_e8;
    private static final int BUTTON_EDGE = 0xff_91_5d_cd;
    /** A field sits one shade lighter than a resting button, so the two do not read as the same control. */
    private static final int FIELD_FILL = 0xff_f7_f3_fc;
    private static final int FIELD_EDGE = 0xff_d9_c8_f0;
    /** One-pixel rule between the two lines of a link: a divider, not a border around anything. */
    private static final int HAIRLINE = 0x33_91_5d_cd;

    private WirelessLinkManagementUI() {
    }

    public static ModularUI create(HeldItemUIMenuType.HeldItemUIHolder holder) {
        // One flex column with a row gap, instead of the previous ABSOLUTE left/top per element: adding the
        // fourth button used to mean recomputing the three that were already placed, and nothing about the
        // help line's position could be derived from what is above it. The sheet stays 340x248 because that
        // is what fits a scale-4 screen; only the internals are laid out now.
        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT)
                        .display(TaffyDisplay.FLEX).flexDirection(FlexDirection.COLUMN)
                        .gapRow(6).paddingAll(8))
                .style(style -> style.backgroundTexture(new ColorRectTexture(0xff_ef_f0_f4)));

        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.title"),
                Horizontal.CENTER, 14, COLOR));

        StatusList status = new StatusList(holder);

        // The name and its field are one row: the label keeps a fixed width so the field starts at the same
        // x on every screen of this family, and the field takes whatever is left.
        UIElement groupRow = row(20, 8);
        // Equal box heights do NOT give equal baselines, so the label hugs the text line instead. Measured on
        // the 2026-11-14 client screenshot (scale 4, panel 340x248): with both boxes at 16 the field's glyphs
        // start 3.5 px under the label's, because TextField draws its text 4.5 px below its own box top and
        // Label draws 1 px below its own. 9 is MC's font line height, and it is also what every Label line in
        // this panel measures, so centring a 9-tall box in the 20-tall row puts the two texts on one line.
        // The script-metric worry is closed: in the marked line below, which mixes CJK and digits in one draw,
        // both kinds of glyph have the same ink top (60.00) and digits only differ at the bottom.
        groupRow.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.group"),
                Horizontal.LEFT, 16, COLOR).layout(layout -> layout.width(78).height(9)));

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
        // The box hugs the text instead of the row: TextField exposes no padding or vertical-alignment knob
        // (measured on TextField$TextFieldStyle - only font/fontSize/textColor/errorColor/cursorColor/textShadow),
        // so its 16-tall box is the only thing this side can set. Same screenshot: the box lays out at 16 and
        // the border paints 18 - the extra pixel is under the box, not around the text - so 16 is honest here.
        groupField.layout(layout -> layout.flex(1).height(16));
        // No slot bevel. LDLib2's own default is a near-black box, which on this light sheet reads as a hole
        // rather than a field; one shade of fill plus a hairline says "type here" without a second rectangle,
        // and the focus ring still comes from the library so it stays one story.
        groupField.style(style -> style.background(new GuiTextureGroup(
                new ColorRectTexture(FIELD_FILL), new ColorBorderTexture(1, FIELD_EDGE))));
        groupField.textFieldStyle(style -> style.textColor(COLOR).textShadow(false));
        groupRow.addChild(groupField);
        root.addChild(groupRow);

        Button apply = button("gui.neoecoprototype.wireless_manager.apply_group", () -> {
            if (!(holder.player instanceof ServerPlayer player)) {
                return;
            }
            ItemStack held = player.getItemInHand(holder.hand);
            String group = WirelessLinks.normalizeGroup(WirelessLinkCardItem.readGroup(held));
            WirelessLinkCardItem.writeGroup(held, group);
            // No mark means "the links this screen is showing", not "nothing": the list under the buttons is
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

        Button refresh = button("gui.neoecoprototype.wireless_manager.refresh", status::refresh);
        Button unlink = button("gui.neoecoprototype.wireless_manager.unlink_group", () -> {
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

        UIElement buttonRow = row(18, 6);
        buttonRow.addChildren(apply, refresh, unlink);
        root.addChild(buttonRow);

        // The list is the part that grows, so it is the one element given the leftover space.
        status.layout(layout -> layout.widthPercent(100).flex(1));
        root.addChild(status);

        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_manager.help"),
                Horizontal.LEFT, 24, COLOR_SUB));
        return new ModularUI(UI.of(root), holder.player);
    }

    /** A group as the player reads it: the entered name, or the language's word for "no group". */
    private static Component groupName(String group) {
        return group.isEmpty()
                ? Component.translatable("gui.neoecoprototype.wireless_manager.ungrouped")
                : Component.literal(group);
    }

    /** One horizontal band of the column: full width, fixed height, its children spaced left to right. */
    private static UIElement row(int height, int gapBetween) {
        return new UIElement().layout(layout -> layout.widthPercent(100).height(height)
                .display(TaffyDisplay.FLEX).flexDirection(FlexDirection.ROW)
                .gapColumn(gapBetween).alignItems(AlignItems.CENTER));
    }

    /** The link list: three links, each two lines with a hairline between them. */
    private static final class StatusList extends UIElement {
        /**
         * Three, and the panel decides that rather than a taste: a row is 18 + 1 + 18 plus a 6 gap, so three
         * rows need 123 px and the column has 123 left after the title, the field row, the buttons, the help
         * line, the gaps and the padding. A fourth row would push the sheet past a scale-4 screen.
         */
        private static final int SLOTS = 3;
        private static final int LINE_HEIGHT = 18;

        private final HeldItemUIMenuType.HeldItemUIHolder holder;
        private final Component[] marked = new Component[1];
        private final Component[] head = new Component[SLOTS];
        private final Component[] tail = new Component[SLOTS];

        private StatusList(HeldItemUIMenuType.HeldItemUIHolder holder) {
            this.holder = holder;
            Arrays.fill(marked, Component.empty());
            Arrays.fill(head, Component.empty());
            Arrays.fill(tail, Component.empty());

            layout(layout -> layout.heightPercent(100)
                    .display(TaffyDisplay.FLEX).flexDirection(FlexDirection.COLUMN).gapRow(4));

            Label markedLine = label(Component.empty(), Horizontal.LEFT, LINE_HEIGHT, COLOR);
            markedLine.bind(DataBindingBuilder.componentS2C(() -> marked[0]).build());
            addChild(markedLine);

            for (int index = 0; index < SLOTS; index++) {
                int slot = index;
                // One link is one block: the two lines are a column of their own and the rule goes UNDER the
                // block. The previous version put the rule between the head and the tail of the same link,
                // which is the opposite of what it means - the client screenshot of 2026-11-14 showed the
                // rules reading as separators between links, so the structure now says that out loud instead
                // of leaving the order to be guessed from pixels.
                UIElement block = new UIElement().layout(layout -> layout.widthPercent(100).height(37)
                        .display(TaffyDisplay.FLEX).flexDirection(FlexDirection.COLUMN).gapRow(1));
                Label top = label(Component.empty(), Horizontal.LEFT, LINE_HEIGHT, COLOR);
                top.bind(DataBindingBuilder.componentS2C(() -> head[slot]).build());
                block.addChild(top);
                Label bottom = label(Component.empty(), Horizontal.LEFT, LINE_HEIGHT, COLOR_SUB);
                bottom.bind(DataBindingBuilder.componentS2C(() -> tail[slot]).build());
                block.addChild(bottom);
                addChild(block);
                addChild(divider());
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

        /** A 1 px rule across the row: the two lines of one link read as one item without a box. */
        private static UIElement divider() {
            return new UIElement().layout(layout -> layout.widthPercent(100).height(1))
                    .style(style -> style.backgroundTexture(new ColorRectTexture(HAIRLINE)));
        }
    }

    private static Label label(Component text, Horizontal alignment, int height, int color) {
        Label label = new Label();
        label.setText(text);
        // NONE rather than WORD_WRAP: a wrapped row changes height mid-list and the panel is a fixed sheet,
        // which is how the 304 px version managed to cut a dimension id in half. The row content is now sized
        // by the split into two lines instead.
        label.textStyle(style -> style.textWrap(TextWrap.NONE)
                .textShadow(false).textColor(color).textAlignHorizontal(alignment));
        label.layout(layout -> layout.widthPercent(100).height(height));
        return label;
    }

    /**
     * A button that belongs to this panel rather than to LDLib2's default dark plate: the screen is a light
     * sheet, and a near-black button on it reads as a disabled control - measured in the client on 2026-11-13,
     * where he described the row of buttons as the one thing that looked wrong.
     *
     * <p>2026-11-14 restyle: at rest a button is now a flat fill with no outline at all, and the violet edge
     * only appears under the cursor. That is the borderless sheet the panel wanted, and it keeps the affordance
     * the 11-13 reading asked for in the one state where it matters - but it is a judgement about how a pale
     * plate reads on a pale sheet, so the client pass has to confirm the resting row still does not look
     * disabled. Nothing in the suite can see any of this.
     */
    private static Button button(String translationKey, Runnable action) {
        Button button = new Button().setText(Component.translatable(translationKey));
        button.buttonStyle(style -> style
                .baseTexture(new ColorRectTexture(BUTTON_IDLE))
                .hoverTexture(plate(BUTTON_HOVER))
                .pressedTexture(plate(BUTTON_PRESSED)));
        button.textStyle(style -> style.textShadow(false).textColor(COLOR));
        button.layout(layout -> layout.flex(1).heightPercent(100));
        button.setOnServerClick(event -> action.run());
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(translationKey)), null, null, null));
        return button;
    }

    /** One hovered or pressed face: a fill plus a one-pixel outline, both in the same violet family. */
    private static IGuiTexture plate(int fill) {
        return GuiTextureGroup.of(
                new ColorRectTexture(fill),
                new ColorBorderTexture(1, BUTTON_EDGE));
    }
}

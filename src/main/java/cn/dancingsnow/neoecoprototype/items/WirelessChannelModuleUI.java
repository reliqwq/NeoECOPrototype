package cn.dancingsnow.neoecoprototype.items;

import appeng.api.networking.IGridNode;
import appeng.api.networking.pathing.ControllerState;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.wireless.WirelessChannelModuleBlockEntity;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IBindable;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IDataSource;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.Nullable;

/**
 * The subsystem channel module's panel, laid into a hand-drawn 177x247 plate.
 *
 * <p>Every widget is absolutely positioned onto a recess the art already draws, and every widget's own
 * background is {@link IGuiTexture#EMPTY}: the plate is the whole picture, so a second bevel under a label
 * would read as a second panel. Only two things are painted by code - the selection tint on a device row and
 * the hover tint on a button - and both are translucent so the recess still shows through.
 *
 * <p>The three columns are one reading done in order: the left column is the module's six outlets and clicking
 * one chooses it, the right column is the links hanging on the chosen outlet, and the middle column is what the
 * whole module is carrying plus the three actions that work on the chosen link. The selector under the left
 * column names the outlet the right column is showing, and its arrows move that choice without the player
 * having to hit a 48-pixel line.
 *
 * <p>Nothing here is a GameTest surface - LDLib2 screens never run headless - so the layout claims are intent
 * and the client pass is the reading.
 */
public final class WirelessChannelModuleUI {

    private static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/gui/wireless_channel_module.png");

    /** Eight 8x8 chips, four across and two down: one colour per outlet, so the column reads at a glance. */
    private static final ResourceLocation OUTLET_CHIPS =
            NeoECOPrototype.id("textures/gui/wireless_channel_bars.png");
    private static final int CHIP_SIZE = 8;

    /**
     * Three 10x11 lamps in one row: green = channels left, blue = carrying a network, red = its 32 are gone.
     * The whole module's state, so it belongs beside the module-wide reading rather than beside an outlet.
     */
    private static final ResourceLocation OUTLET_STATE =
            NeoECOPrototype.id("textures/gui/wireless_channel_state.png");
    private static final int LAMP_WIDTH = 10;
    private static final int LAMP_HEIGHT = 11;
    private static final int LAMP_FREE = 0;
    private static final int LAMP_USED = 1;
    private static final int LAMP_FULL = 2;

    /**
     * The three text colours, taken from the lamps so a number and the light beside it agree: the amount in
     * use is blue, the ceiling it is measured against is red, and what is left is green.
     */
    private static final int TEXT_USED = 0x337FF0;
    private static final int TEXT_FULL = 0xEC283C;
    private static final int TEXT_FREE = 0x61D389;

    /** The plate's own size; the art is exactly this, with no transparent margin left. */
    private static final int WIDTH = 177;
    private static final int HEIGHT = 247;

    /** RGB, not ARGB: the text renderer keeps the low 24 bits, so an {@code 0xff} prefix would read as pink. */
    private static final int COLOR = 0x404040;

    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 7;
    private static final int TITLE_WIDTH = 161;
    private static final int TITLE_HEIGHT = 9;

    /** Six outlet recesses, 48x14, seventeen pixels apart. Each one is a click target. */
    private static final int PORT_X = 9;
    private static final int PORT_Y = 25;
    private static final int PORT_WIDTH = 48;
    private static final int PORT_HEIGHT = 14;
    private static final int PORT_PITCH = 17;
    /**
     * The chip takes the "node 1" prefix's place: 8 pixels of chip, three of gap, and the read-out right
     * aligned against the recess's inner edge so a two-digit and a one-digit count line up.
     */
    private static final int PORT_CHIP_X = 11;
    private static final int PORT_CHIP_Y = 3;
    private static final int PORT_TEXT_X = 21;
    private static final int PORT_TEXT_WIDTH = 33;
    /**
     * Eight pixels, not the default nine: a recess is 48 wide and the line reads "node 1: 0/32", which is
     * wider than 46 at nine. Measured against the recess rather than guessed - the art leaves one pixel of
     * padding on each side.
     */
    private static final float LINE_FONT = 8;
    /** The selector under the outlets: one size down, because "节点6" is three glyphs in a 19-pixel box. */
    private static final float SELECTOR_FONT = 7;
    /** The module-wide reading, one size down so {@code 161/192} fits the recess after the lamp. */
    private static final float INFO_FONT = 7;

    /** The outlet selector under the column: two arrows and the outlet they last moved onto. */
    private static final int PORT_PAGER_Y = 133;
    private static final int PORT_PAGER_PREV_X = 9;
    private static final int PORT_PAGER_PAGE_X = 24;
    private static final int PORT_PAGER_PAGE_WIDTH = 19;
    private static final int PORT_PAGER_NEXT_X = 45;
    private static final int PAGER_ARROW_WIDTH = 12;
    private static final int PAGER_ARROW_HEIGHT = 12;

    /**
     * The middle column's three read-outs, each 48x14. The first one carries the state lamp at its left edge,
     * so its text starts after the lamp and is right aligned against the recess's inner edge.
     */
    private static final int INFO_TEXT_X = 68;
    private static final int INFO_TEXT_WIDTH = 44;
    private static final int[] INFO_Y = {25, 43, 68};
    private static final int INFO_LAMP_X = 67;
    private static final int INFO_LAMP_Y = 26;
    private static final int INFO_USED_TEXT_X = 78;
    private static final int INFO_USED_TEXT_WIDTH = 35;

    /** The middle column's two button rows: a pager over the device list, then the three actions. */
    /** Measured off the plate: each cell is 16x14 and they sit at x 67, 83, 99. */
    private static final int MID_BUTTON_WIDTH = 16;
    private static final int MID_BUTTON_HEIGHT = 14;
    private static final int MID_PAGER_Y = 94;
    /**
     * The action row's recess is y 117-132 and the painted icons inside it are 123-130 (centre 126.5), so a
     * 14-tall hit box at 118 centres two pixels above the art. Two pixels down puts the box centre exactly on
     * the icon mass; the box then runs 120-133, one pixel past the recess's bottom edge, which is the cheaper
     * error because the hover tint is painted inside the box, not inside the recess.
     */
    private static final int MID_ACTION_Y = 120;
    private static final int MID_PAGER_PREV_X = 67;
    private static final int MID_PAGER_PAGE_X = 83;
    private static final int MID_PAGER_NEXT_X = 99;

    /** Eight device rows, 47x12, fifteen pixels apart. */
    private static final int DEVICE_X = 122;
    private static final int DEVICE_Y = 34;
    private static final int DEVICE_WIDTH = 47;
    private static final int DEVICE_HEIGHT = 12;
    private static final int DEVICE_PITCH = 15;
    /** A 16x16 icon hard against the left edge of the 47x12 cell, two pixels proud of it top and bottom. */
    private static final int DEVICE_ICON_SIZE = 16;
    private static final int DEVICE_ICON_INSET = 0;
    /** The position after the icon: right aligned, one size down so a five-digit coordinate still fits. */
    private static final int DEVICE_TEXT_X = 138;
    private static final int DEVICE_TEXT_WIDTH = 30;
    private static final float DEVICE_FONT = 7;
    private static final int DEVICES_PER_PAGE = 8;

    /** The player inventory block the plate draws at the bottom: nine columns, four rows, eighteen apart. */
    /**
     * The art draws the slot interiors at (8, 167) and every eighteen pixels after, 16x16 each. The widget is
     * an 18-pixel box with one pixel of padding, so the item lands on the recess only when the box starts one
     * pixel left and two above the recess - measured off the plate, not guessed.
     */
    private static final int INVENTORY_X = 7;
    private static final int INVENTORY_Y = 166;
    private static final int INVENTORY_PITCH = 18;
    private static final int SLOT_SIZE = 18;
    private static final int SLOT_PADDING = 1;

    /** A pale wash over the selected device row, per the hand-off spec. */
    private static final ColorRectTexture SELECTED_TINT = new ColorRectTexture(0x55_FF_FF_FF);
    private static final ColorRectTexture HOVER_TINT = new ColorRectTexture(0x22_FF_FF_FF);
    private static final ColorRectTexture PRESSED_TINT = new ColorRectTexture(0x33_FF_FF_FF);

    private WirelessChannelModuleUI() {
    }

    public static ModularUI create(WirelessChannelModuleBlockEntity module, Player player) {
        Panel panel = new Panel(player);
        panel.pin(module);

        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(TEXTURE).setSprite(0, 0, WIDTH, HEIGHT)));

        root.addChild(label(Component.translatable("gui.neoecoprototype.wireless_module.title"),
                Horizontal.CENTER, TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT, COLOR, 9));

        // Left column: six outlet lines, each a click target, then the selector under it. Choosing an outlet is
        // what the right column then lists, so the column is a selector first and a read-out second.
        for (int index = 0; index < WirelessChannelModuleBlockEntity.MAX_PORTS; index++) {
            root.addChild(panel.portRow(index));
        }
        root.addChild(pagerArrow(PORT_PAGER_PREV_X, PORT_PAGER_Y, PAGER_ARROW_WIDTH, PAGER_ARROW_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_prev_port", () -> panel.turnPortSelection(-1)));
        root.addChild(bound(() -> panel.portPageText, Horizontal.CENTER,
                PORT_PAGER_PAGE_X, PORT_PAGER_Y + 3, PORT_PAGER_PAGE_WIDTH, TITLE_HEIGHT,
                COLOR, SELECTOR_FONT, TextWrap.NONE));
        root.addChild(pagerArrow(PORT_PAGER_NEXT_X, PORT_PAGER_Y, PAGER_ARROW_WIDTH, PAGER_ARROW_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_next_port", () -> panel.turnPortSelection(1)));

        // Middle column: used/available/dimension, then the device pager, then the three actions.
        // The green box goes in before the labels so the text it carries paints on top of it.
        root.addChild(panel.availableBox());
        for (int index = 0; index < INFO_Y.length; index++) {
            int line = index;
            // The first two lines carry a square at their left edge, so their text starts after it; the
            // dimension line has nothing in front of it and keeps the full recess width.
            boolean lamped = index != 2;
            root.addChild(bound(() -> panel.infoLines[line],
                    lamped ? Horizontal.RIGHT : Horizontal.CENTER,
                    lamped ? INFO_USED_TEXT_X : INFO_TEXT_X, INFO_Y[index] + 4,
                    lamped ? INFO_USED_TEXT_WIDTH : INFO_TEXT_WIDTH, 8, COLOR, INFO_FONT, TextWrap.NONE));
        }
        root.addChild(panel.moduleLamp());
        root.addChild(panel.infoTooltip());
        root.addChild(pagerArrow(MID_PAGER_PREV_X, MID_PAGER_Y, MID_BUTTON_WIDTH, MID_BUTTON_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_prev", () -> panel.turnDevicePage(-1)));
        // The page counter is a fraction, not a reading: blue like the other channel numbers and one step
        // smaller, so it stops competing with the two numbers the row is actually about.
        root.addChild(bound(() -> panel.devicePageText, Horizontal.CENTER,
                MID_PAGER_PAGE_X, MID_PAGER_Y + 3, MID_BUTTON_WIDTH, TITLE_HEIGHT, TEXT_USED, INFO_FONT,
                TextWrap.NONE));
        root.addChild(pagerArrow(MID_PAGER_NEXT_X, MID_PAGER_Y, MID_BUTTON_WIDTH, MID_BUTTON_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_next", () -> panel.turnDevicePage(1)));

        // The art paints the three icons, so each action is only a hit box with its sentence on hover.
        root.addChild(actionButton(MID_PAGER_PREV_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.tip_highlight", panel::highlightSelected));
        root.addChild(actionButton(MID_PAGER_PAGE_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.tip_refresh", panel::refresh));
        root.addChild(actionButton(MID_PAGER_NEXT_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.tip_unlink", panel::unlinkSelected));

        // Right column: the device rows, each a click target with a bound name and a bound selection tint.
        for (int index = 0; index < DEVICES_PER_PAGE; index++) {
            root.addChild(panel.deviceRow(index));
        }

        // Bottom: the player's own inventory, drawn by the plate as a nine-by-four block.
        Inventory inventory = player.getInventory();
        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                root.addChild(inventorySlot(inventory, 9 + row * 9 + column,
                        INVENTORY_X + column * INVENTORY_PITCH, INVENTORY_Y + row * INVENTORY_PITCH));
            }
        }
        for (int column = 0; column < 9; column++) {
            root.addChild(inventorySlot(inventory, column,
                    INVENTORY_X + column * INVENTORY_PITCH, INVENTORY_Y + 3 * INVENTORY_PITCH));
        }

        // The highlight rides this widget: an integer that moves when the button is pressed, and the two ends
        // it refers to. Nothing is drawn here - the client's render pass reads it and draws the boxes.
        root.addChild(panel.outlinePulse());

        panel.refresh();
        return new ModularUI(UI.of(root), player);
    }

    // ==================== widgets ====================

    private static Label label(Component text, Horizontal align, int x, int y, int width, int height,
                               int color, float fontSize) {
        Label label = new Label();
        label.setText(text);
        label.textStyle(style -> style.textWrap(TextWrap.NONE).textShadow(false)
                .textColor(color).fontSize(fontSize).textAlignHorizontal(align));
        label.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
        label.setAllowHitTest(false);
        return label;
    }

    /** A label whose text is pushed from the server: the whole panel's read-outs travel this way. */
    private static Label bound(java.util.function.Supplier<Component> getter, Horizontal align,
                               int x, int y, int width, int height, int color, float fontSize,
                               TextWrap wrap) {
        Label label = new Label();
        label.setText(getter.get());
        label.textStyle(style -> style.textWrap(wrap).textShadow(false)
                .textColor(color).fontSize(fontSize).textAlignHorizontal(align));
        label.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
        label.setAllowHitTest(false);
        label.bind(DataBindingBuilder.componentS2C(getter).build());
        return label;
    }

    /**
     * One pager arrow. The art paints the arrow itself, so the button is an invisible hit box over it: a hover
     * wash is the only thing code adds, so the plate's own arrow stays the thing the player sees.
     */
    private static Button pagerArrow(int x, int y, int width, int height, String tooltipKey, Runnable action) {
        Button button = new Button().noText();
        button.buttonStyle(style -> style
                .baseTexture(IGuiTexture.EMPTY)
                .hoverTexture(HOVER_TINT)
                .pressedTexture(PRESSED_TINT));
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height).paddingAll(0));
        button.setOnServerClick(event -> action.run());
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(tooltipKey)),
                        null, null, null));
        return button;
    }

    private static Button actionButton(int x, int y, String tooltipKey, Runnable action) {
        Button button = new Button().noText();
        button.buttonStyle(style -> style
                .baseTexture(IGuiTexture.EMPTY)
                .hoverTexture(HOVER_TINT)
                .pressedTexture(PRESSED_TINT));
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(MID_BUTTON_WIDTH).height(MID_BUTTON_HEIGHT).paddingAll(0));
        button.setOnServerClick(event -> action.run());
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(tooltipKey)),
                        null, null, null));
        return button;
    }

    /** One 8x8 colour chip from the outlet sheet, in reading order, at the recess's left edge. */
    private static UIElement chip(int slot) {
        return new UIElement()
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(OUTLET_CHIPS).setSprite(
                                (slot % 4) * CHIP_SIZE, (slot / 4) * CHIP_SIZE, CHIP_SIZE, CHIP_SIZE)))
                .setAllowHitTest(false)
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(PORT_CHIP_X - PORT_X).top(PORT_CHIP_Y)
                        .width(CHIP_SIZE).height(CHIP_SIZE));
    }

    /**
     * The hover box over the used/free recess: it draws nothing, it just holds the state sentence.
     */
    private static final class InfoTooltip extends UIElement implements IBindable<Component> {
        private Component text = Component.empty();

        private InfoTooltip() {
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(INFO_TEXT_X - 1).top(INFO_Y[0] - 1)
                    .width(INFO_TEXT_WIDTH + 2).height(TITLE_HEIGHT + 4));
            style(style -> style.tooltips(text));
        }

        @Override
        public Component getValue() {
            return text;
        }

        @Override
        public IDataSource<Component> setValue(@Nullable Component value) {
            if (value == null || value.getString().isEmpty()) {
                return this;
            }
            text = value;
            style(style -> style.tooltips(value));
            return this;
        }
    }

    /**
     * The module's state lamp: green while it has channels left, blue while it carries a network, red once its
     * thirty-two are spent. A widget of its own because the state is one server-side integer and the button's
     * textures are static.
     */
    private static final class OutletLamp extends UIElement implements IBindable<Integer> {
        private int state = LAMP_FREE;

        private OutletLamp(int x, int y) {
            setAllowHitTest(false);
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(x).top(y).width(LAMP_WIDTH).height(LAMP_HEIGHT));
            paint();
        }

        @Override
        public Integer getValue() {
            return state;
        }

        @Override
        public IDataSource<Integer> setValue(@Nullable Integer value) {
            state = value == null ? LAMP_FREE : value;
            paint();
            return this;
        }

        private void paint() {
            style(style -> style.backgroundTexture(SpriteTexture.of(OUTLET_STATE)
                    .setSprite(state * LAMP_WIDTH, 0, LAMP_WIDTH, LAMP_HEIGHT)));
        }
    }

    /** The art draws every cell, so the widget contributes no recess of its own. */
    private static ItemSlot inventorySlot(Inventory inventory, int index, int x, int y) {
        ItemSlot widget = new ItemSlot(new Slot(inventory, index, 0, 0));
        widget.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        widget.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(SLOT_SIZE).height(SLOT_SIZE).paddingAll(SLOT_PADDING));
        return widget;
    }

    /**
     * The 16x16 icon at the head of a device row.
     *
     * <p>The server ships the item id - {@code IGridNode.getVisualRepresentation()}, the same answer the
     * Network Tool gives for "what is this node", so a cable bus shows its cable rather than a generic block
     * icon - and this turns it into the stack the client can draw.
     */
    private static final class DeviceIcon extends UIElement implements IBindable<String> {
        private final com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture texture =
                new com.lowdragmc.lowdraglib2.gui.texture.ItemStackTexture();
        private String last = "";

        private DeviceIcon() {
            setAllowHitTest(false);
            // Hard against the left edge of the recess; the cell is 12 tall, so a 16-pixel icon stands two
            // pixels proud of it top and bottom rather than being squeezed.
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(DEVICE_ICON_INSET).top(-2)
                    .width(DEVICE_ICON_SIZE).height(DEVICE_ICON_SIZE));
            style(style -> style.backgroundTexture(texture));
            setDisplay(false);
        }

        @Override
        public String getValue() {
            return last;
        }

        @Override
        public IDataSource<String> setValue(@Nullable String value) {
            String id = value == null ? "" : value;
            if (id.equals(last)) {
                return this;
            }
            last = id;
            if (id.isEmpty()) {
                texture.setItems();
                setDisplay(false);
                return this;
            }
            var stack = new net.minecraft.world.item.ItemStack(
                    net.minecraft.core.registries.BuiltInRegistries.ITEM.get(
                            ResourceLocation.parse(id)));
            texture.setItems(stack);
            setDisplay(!stack.isEmpty());
            return this;
        }
    }

    /**
     * The highlight, as one string: the counter, then the two ends.
     *
     * <p>The counter is in the string because a repeat press on the same link leaves the ends unchanged and the
     * binding would not fire; and it is one binding rather than two because an element binds one type.
     */
    private static final class OutlinePulse extends UIElement implements IBindable<String> {
        private String last = "";

        private OutlinePulse() {
            setAllowHitTest(false);
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(0).width(1).height(1));
        }

        @Override
        public String getValue() {
            return last;
        }

        @Override
        public IDataSource<String> setValue(@Nullable String value) {
            if (value == null || value.isEmpty() || value.equals(last)) {
                return this;
            }
            last = value;
            int bar = value.indexOf('|');
            if (bar > 0) {
                cn.dancingsnow.neoecoprototype.client.render.LinkOutline.highlight(value.substring(bar + 1));
            }
            return this;
        }
    }

    /**
     * The row's hover text, which is the far block's whole name - the row itself has 43 pixels, enough for the
     * position and nothing else. A widget of its own because the name is a {@link Component} and the icon is a
     * string, and one element can only be bound to one of them.
     */
    private static final class RowTooltip extends UIElement implements IBindable<Component> {
        private final UIElement row;
        private Component last;

        private RowTooltip(UIElement row, Component initial) {
            this.row = row;
            this.last = initial;
            setAllowHitTest(false);
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(0).width(0).height(0));
            if (!initial.getString().isEmpty()) {
                row.style(style -> style.tooltips(initial));
            }
        }

        @Override
        public Component getValue() {
            return last;
        }

        @Override
        public IDataSource<Component> setValue(@Nullable Component value) {
            if (value == null || value.getString().isEmpty()) {
                return this;
            }
            last = value;
            row.style(style -> style.tooltips(value));
            return this;
        }
    }

    /**
     * The green button behind the available-channels line: the state sheet's own green cell, the same square
     * the outlet lamps and the module lamp are cut from.
     *
     * <p>It paints only while that line has something to say. On an ad-hoc network, or a controller group AE2
     * reads as broken, there is no pool to report and {@code infoLines[1]} is empty - a green light over
     * nothing would read as a number that failed to arrive rather than as a number that does not exist.
     */
    private static final class AvailableBox extends UIElement implements IBindable<Component> {
        private Component last = Component.empty();

        private AvailableBox() {
            setAllowHitTest(false);
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(INFO_TEXT_X - 1).top(INFO_Y[1])
                    .width(LAMP_WIDTH).height(LAMP_HEIGHT));
            style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        }

        @Override
        public Component getValue() {
            return last;
        }

        @Override
        public IDataSource<Component> setValue(@Nullable Component value) {
            boolean shown = value != null && !value.getString().isEmpty();
            last = shown ? value : Component.empty();
            style(style -> style.backgroundTexture(shown ? GREEN_BUTTON : IGuiTexture.EMPTY));
            return this;
        }

        /** The green cell of {@code wireless_channel_state.png} - {@code LAMP_FREE}'s sprite. */
        private static final IGuiTexture GREEN_BUTTON = SpriteTexture.of(OUTLET_STATE)
                .setSprite(LAMP_FREE * LAMP_WIDTH, 0, LAMP_WIDTH, LAMP_HEIGHT);
    }

    /**
     * The wash under the selected row - an outlet in the left column, a device in the right one.
     *
     * <p>A widget of its own rather than a button style, because the selection is one server-side integer and
     * the button's three textures are static: this one binds that integer and repaints when it arrives, and the
     * same widget answers for both columns.
     */
    private static final class SelectionTint extends UIElement implements IBindable<Integer> {
        private final int slot;
        private int selected = -1;

        private SelectionTint(int slot) {
            this.slot = slot;
            setAllowHitTest(false);
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(0).widthPercent(100).heightPercent(100));
            paint();
        }

        @Override
        public Integer getValue() {
            return selected;
        }

        @Override
        public IDataSource<Integer> setValue(@Nullable Integer value) {
            selected = value == null ? -1 : value;
            paint();
            return this;
        }

        private void paint() {
            style(style -> style.backgroundTexture(selected == slot ? SELECTED_TINT : IGuiTexture.EMPTY));
        }
    }

    // ==================== the screen's state ====================

    /**
     * Everything the panel shows, and the only object that reads the world.
     *
     * <p>Holds the mark rather than the block entity: the panel outlives the chunk it was opened over, so each
     * refresh re-resolves it and a square whose chunk is gone answers "no module" instead of loading anything.
     *
     * <p>The client half never reads the world ({@code acting == null}); it is filled by the S2C bindings.
     */
    private static final class Panel {
        private final Player player;
        @Nullable
        private final ServerLevel acting;
        @Nullable
        private GlobalPos pinned;

        private final Component[] portLines = new Component[WirelessChannelModuleBlockEntity.MAX_PORTS];
        private final Component[] infoLines = new Component[INFO_Y.length];
        /** The far device's item id, which the client turns into the 16x16 icon the row shows. */
        private final String[] deviceIcons = new String[DEVICES_PER_PAGE];
        /** The far device's position, which is what the 30 pixels beside the icon hold. */
        private final Component[] deviceLines = new Component[DEVICES_PER_PAGE];
        private final Component[] deviceTips = new Component[DEVICES_PER_PAGE];
        private final List<WirelessLinks.ModuleLink> devices = new ArrayList<>();

        private Component portPageText = Component.empty();
        private Component devicePageText = Component.empty();
        /**
         * The highlight, as one string the client parses: a counter, then the two ends. The counter is what
         * makes a second press on the same link move the value at all.
         */
        private String highlightPulse = "";
        private int highlightTick;
        /** The whole module's lamp state, and the lang key that names it under the reading. */
        private int moduleLampState = LAMP_FREE;
        private String moduleStateKey = "idle";
        private Component infoTip = Component.empty();
        /** The outlet the right column is listing. Outlets are few enough to point at, not page through. */
        private int selectedPort;
        private int devicePage;
        private int selected = -1;

        private Panel(Player player) {
            this.player = player;
            this.acting = player instanceof ServerPlayer server ? server.serverLevel() : null;
            for (int index = 0; index < portLines.length; index++) {
                portLines[index] = Component.empty();
            }
            for (int index = 0; index < infoLines.length; index++) {
                infoLines[index] = Component.empty();
            }
            // Never null: a null Component reaches Tooltips.asList, which is List.of and throws on null - the
            // first client run of this panel died on exactly that, rendering the screen.
            for (int index = 0; index < deviceTips.length; index++) {
                deviceTips[index] = Component.empty();
                deviceLines[index] = Component.empty();
            }
        }

        private void pin(WirelessChannelModuleBlockEntity module) {
            this.pinned = GlobalPos.of(module.getLevel().dimension(), module.getBlockPos());
        }

        @Nullable
        private WirelessChannelModuleBlockEntity module() {
            if (acting == null || pinned == null || !WirelessLinks.mayReadBlocks(acting)) {
                return null;
            }
            ServerLevel owner = levelOf(pinned.dimension());
            if (owner == null || owner.getChunkSource().getChunkNow(
                    pinned.pos().getX() >> 4, pinned.pos().getZ() >> 4) == null) {
                return null;
            }
            return owner.getBlockEntity(pinned.pos()) instanceof WirelessChannelModuleBlockEntity module
                    ? module
                    : null;
        }

        /** Re-reads the module and every link touching it. A no-op on the client, which has no world to read. */
        private void refresh() {
            if (acting == null || pinned == null) {
                return;
            }
            WirelessChannelModuleBlockEntity module = module();
            for (int index = 0; index < portLines.length; index++) {
                portLines[index] = portLine(index, module);
            }
            moduleLampState = moduleLampStateOf(module);
            infoTip = infoTipOf(module);
            portPageText = Component.translatable("gui.neoecoprototype.wireless_module.port_current",
                    selectedPort + 1);
            fillInfo(module);
            fillDevices(module);
        }

        private Component portLine(int index, @Nullable WirelessChannelModuleBlockEntity module) {
            if (module == null) {
                return Component.literal("--");
            }
            List<IGridNode> live = module.livePortNodes();
            IGridNode node = index < live.size() ? live.get(index) : null;
            if (node == null) {
                return Component.translatable("gui.neoecoprototype.wireless_module.port_line_free",
                        coloured(displayNumber(module.portChannelCapacity()), TEXT_FREE));
            }
            return reading(node.getUsedChannels(), node.getMaxChannels());
        }

        /**
         * One outlet line: a click target, a bound read-out and a bound selection wash.
         *
         * <p>Selecting an outlet is what makes the right column mean something - it lists the links on this
         * outlet and nothing else - so the row is a button even though the art draws no button on it.
         */
        private UIElement portRow(int slot) {
            Button row = new Button().noText();
            row.buttonStyle(style -> style
                    .baseTexture(IGuiTexture.EMPTY)
                    .hoverTexture(HOVER_TINT)
                    .pressedTexture(PRESSED_TINT));
            row.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(PORT_X).top(PORT_Y + slot * PORT_PITCH)
                    .width(PORT_WIDTH).height(PORT_HEIGHT).paddingAll(0));
            row.setOnServerClick(event -> selectPort(slot));
            row.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                    event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(
                            "gui.neoecoprototype.wireless_module.tip_port", slot + 1)), null, null, null));

            SelectionTint tint = new SelectionTint(slot);
            tint.bind(DataBindingBuilder.intValS2C(() -> selectedPort).build());
            row.addChild(tint);
            row.addChild(chip(slot));
            row.addChild(bound(() -> portLines[slot], Horizontal.RIGHT,
                    PORT_TEXT_X - PORT_X, 3, PORT_TEXT_WIDTH, TITLE_HEIGHT, COLOR, LINE_FONT,
                    TextWrap.NONE));
            return row;
        }

        /**
         * The state sentence under the reading. Three shapes because the three states answer different
         * questions: idle says how many outlets are in use, healthy says all of them are, congested says the
         * channels are gone and stops quoting a remainder of zero.
         */
        private Component infoTipOf(@Nullable WirelessChannelModuleBlockEntity module) {
            if (module == null) {
                return Component.translatable(
                        "gui.neoecoprototype.wireless_module.info_tip_idle", 0, 0, 0);
            }
            long total = module.totalPortCapacity();
            long used = module.totalPortUsed();
            long left = Math.max(0, total - used);
            // Written out rather than concatenated from moduleStateKey: the language guard reads this file for
            // literal keys, and a built key is one it cannot check.
            if (moduleLampState == LAMP_FULL) {
                return Component.translatable(
                        "gui.neoecoprototype.wireless_module.info_tip_congested",
                        displayNumber(total));
            }
            if (moduleLampState == LAMP_USED) {
                return Component.translatable(
                        "gui.neoecoprototype.wireless_module.info_tip_healthy",
                        WirelessChannelModuleBlockEntity.MAX_PORTS, displayNumber(left));
            }
            return Component.translatable(
                    "gui.neoecoprototype.wireless_module.info_tip_idle",
                    module.busyPorts(), WirelessChannelModuleBlockEntity.MAX_PORTS, displayNumber(left));
        }

        /**
         * The module's state, in the three words the tooltip uses: idle while fewer than six outlets carry a
         * network, healthy while all six do and the module still has channels, congested once the 192 are gone.
         *
         * <p>Six is {@code MAX_PORTS}: the count of outlets a network is actually hanging off, not the count of
         * outlets that exist. Measured off the port's own connection list, because AE2 puts the trunk on every
         * outlet and one connection means nothing landed there yet.
         */
        private int moduleLampStateOf(@Nullable WirelessChannelModuleBlockEntity module) {
            if (module == null) {
                return LAMP_FREE;
            }
            long total = module.totalPortCapacity();
            long used = module.totalPortUsed();
            int carrying = module.busyPorts();
            if (used >= total) {
                moduleStateKey = "congested";
                return LAMP_FULL;
            }
            if (carrying >= WirelessChannelModuleBlockEntity.MAX_PORTS) {
                moduleStateKey = "healthy";
                return LAMP_USED;
            }
            moduleStateKey = "idle";
            return LAMP_FREE;
        }

        /**
         * An invisible box over the used/free recess, carrying the module's state as words.
         *
         * <p>The tooltip is a bound Component rather than a fixed key, so the text changes with the state
         * without rebuilding the screen - the same mechanism the device rows use for their names.
         */
        private UIElement infoTooltip() {
            InfoTooltip box = new InfoTooltip();
            box.bind(DataBindingBuilder.componentS2C(() -> infoTip).build());
            return box;
        }

        /** The module's lamp, bound to the state the whole block is in rather than to one outlet's. */
        private OutletLamp moduleLamp() {
            OutletLamp lamp = new OutletLamp(INFO_LAMP_X, INFO_LAMP_Y);
            lamp.bind(DataBindingBuilder.intValS2C(() -> moduleLampState).build());
            return lamp;
        }

        /** The green button in front of the available line, riding the same component the line is bound to. */
        private UIElement availableBox() {
            AvailableBox box = new AvailableBox();
            box.bind(DataBindingBuilder.componentS2C(() -> infoLines[1]).build());
            return box;
        }

        /**
         * The three read-outs. The pool only means something while AE2 meters the outlets separately, so an
         * ad-hoc grid says so rather than adding six zeroes into a capacity the network cannot spend.
         */
        private void fillInfo(@Nullable WirelessChannelModuleBlockEntity module) {
            if (module == null) {
                infoLines[0] = Component.translatable("gui.neoecoprototype.wireless_module.info_state",
                        "no module");
                infoLines[1] = Component.empty();
                infoLines[2] = Component.empty();
                return;
            }
            String controller = WirelessLinks.controllerState(acting, pinned);
            if ("NO_CONTROLLER".equals(controller)) {
                infoLines[0] = Component.translatable("gui.neoecoprototype.wireless_module.info_adhoc");
                infoLines[1] = Component.empty();
            } else if (!"CONTROLLER_ONLINE".equals(controller)) {
                infoLines[0] = Component.translatable("gui.neoecoprototype.wireless_module.info_state",
                        controller);
                infoLines[1] = Component.empty();
            } else {
                long total = module.totalPortCapacity();
                long used = module.totalPortUsed();
                infoLines[0] = reading(used, total);
                infoLines[1] = Component.translatable("gui.neoecoprototype.wireless_module.info_available",
                        displayNumber(Math.max(0, total - used)));
            }
            infoLines[2] = Component.translatable("gui.neoecoprototype.wireless_module.info_dimension",
                    dimensionName(pinned.dimension()));
        }

        /**
         * The links on the selected outlet. A link whose edge has not been built is listed on every outlet,
         * because it has no outlet to be on and listing it nowhere would make a recorded link unreachable from
         * this panel.
         */
        private void fillDevices(@Nullable WirelessChannelModuleBlockEntity module) {
            devices.clear();
            for (WirelessLinks.ModuleLink row : WirelessLinks.linksOnModule(acting,
                    ModRegistration.WIRELESS_LINKS, pinned,
                    module == null ? List.of() : module.livePortNodes())) {
                if (row.port() == selectedPort || row.port() < 0) {
                    devices.add(row);
                }
            }
            int pages = pageCount(devices.size(), DEVICES_PER_PAGE);
            devicePage = Math.min(devicePage, pages - 1);
            devicePageText = Component.translatable("gui.neoecoprototype.wireless_module.page",
                    devicePage + 1, pages);
            if (selected >= devices.size()) {
                selected = -1;
            }
            for (int slot = 0; slot < DEVICES_PER_PAGE; slot++) {
                int index = devicePage * DEVICES_PER_PAGE + slot;
                if (index >= devices.size()) {
                    deviceIcons[slot] = "";
                    deviceTips[slot] = Component.empty();
                    deviceLines[slot] = Component.empty();
                    continue;
                }
                WirelessLinks.ModuleLink row = devices.get(index);
                deviceIcons[slot] = WirelessLinks.iconId(acting, row.far());
                deviceTips[slot] = WirelessLinks.nameAndCoordinates(acting, row.far());
                // The row's own text is what the link costs, not where the far end is: the position is on the
                // hover line, and six rows of coordinates said nothing a click on the row cannot answer.
                WirelessLinks.Channels channels = WirelessLinks.channelsOf(acting, row.far());
                deviceLines[slot] = channels == null
                        ? Component.literal("--")
                        : reading(channels.used(), channels.max());
            }
        }

        /** One device row: a click target, the far block's icon, its position, and a bound selection wash. */
        private UIElement deviceRow(int slot) {
            int y = DEVICE_Y + slot * DEVICE_PITCH;
            Button row = new Button().noText();
            row.buttonStyle(style -> style
                    .baseTexture(IGuiTexture.EMPTY)
                    .hoverTexture(HOVER_TINT)
                    .pressedTexture(PRESSED_TINT));
            row.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(DEVICE_X).top(y).width(DEVICE_WIDTH).height(DEVICE_HEIGHT).paddingAll(0));
            row.setOnServerClick(event -> select(slot));

            SelectionTint tint = new SelectionTint(slot);
            tint.bind(DataBindingBuilder.intValS2C(this::selectedSlot).build());
            row.addChild(tint);

            // Icon, position, and the whole name on hover - each on its own binding, so the row itself stays a
            // plain button.
            DeviceIcon icon = new DeviceIcon();
            icon.bind(DataBindingBuilder.stringS2C(() -> deviceIcons[slot]).build());
            row.addChild(icon);

            RowTooltip tooltip = new RowTooltip(row, deviceTips[slot]);
            tooltip.bind(DataBindingBuilder.componentS2C(() -> deviceTips[slot]).build());
            row.addChild(tooltip);

            row.addChild(bound(() -> deviceLines[slot], Horizontal.RIGHT,
                    DEVICE_TEXT_X - DEVICE_X, 3, DEVICE_TEXT_WIDTH, TITLE_HEIGHT, COLOR,
                    DEVICE_FONT, TextWrap.NONE));
            return row;
        }

        /**
         * The widget that turns the highlight counter into an outline on this client.
         *
         * <p>It draws nothing: it binds the counter and the two ends, and each time the counter moves it asks
         * {@link cn.dancingsnow.neoecoprototype.client.render.LinkOutline} to put a box around both squares for
         * a few seconds. Reusing a data binding instead of adding a packet keeps the feature inside machinery
         * this panel already relies on for its selection wash.
         */
        private UIElement outlinePulse() {
            OutlinePulse pulse = new OutlinePulse();
            pulse.bind(DataBindingBuilder.stringS2C(() -> highlightPulse).build());
            return pulse;
        }

        /** The selected device as a page-local slot number, which is what each row compares itself to. */
        private Integer selectedSlot() {
            if (selected < 0 || selected / DEVICES_PER_PAGE != devicePage) {
                return -1;
            }
            return selected % DEVICES_PER_PAGE;
        }

        private void select(int slot) {
            int index = devicePage * DEVICES_PER_PAGE + slot;
            selected = index < devices.size() ? index : -1;
            refresh();
        }

        private void turnDevicePage(int delta) {
            int pages = pageCount(devices.size(), DEVICES_PER_PAGE);
            devicePage = Math.floorMod(devicePage + delta, pages);
            selected = -1;
            refresh();
        }

        /**
         * Which outlet the right column lists. Any index is accepted, including an outlet that has not been
         * opened yet - its page is empty and its line says so, which is the honest reading of "free".
         */
        private void selectPort(int index) {
            selectedPort = Math.max(0, Math.min(index, WirelessChannelModuleBlockEntity.MAX_PORTS - 1));
            selected = -1;
            refresh();
        }

        private void turnPortSelection(int delta) {
            selectPort(Math.floorMod(selectedPort + delta, WirelessChannelModuleBlockEntity.MAX_PORTS));
        }

        @Nullable
        private WirelessLinks.ModuleLink selectedLink() {
            return selected >= 0 && selected < devices.size() ? devices.get(selected) : null;
        }

        /**
         * Puts a box around both ends of the selected link, for the player who pressed the button.
         *
         * <p>Not a world effect: the outline is drawn by the client off a counter this panel ships, so the box
         * appears only on the screen that asked for it. Particles were the first version and the client's
         * verdict was that a burst of end rods is not a highlight - it does not stay on the square, and it
         * cannot be read as "this is the thing you selected".
         */
        private void highlightSelected() {
            if (acting == null) {
                return;
            }
            WirelessLinks.ModuleLink link = selectedLink();
            if (link == null) {
                return;
            }
            highlightPulse = (highlightTick++) + "|" + pulse(pinned) + "|" + pulse(link.far());
            NeoECOPrototype.LOGGER.info("highlight asked: pulse={}", highlightPulse);
        }

        /** One end of the pulse: the dimension, the position, and nothing the client has to look up. */
        private static String pulse(GlobalPos at) {
            return at.dimension().location() + ":" + at.pos().getX() + "," + at.pos().getY() + ","
                    + at.pos().getZ();
        }

        private void unlinkSelected() {
            if (acting == null) {
                return;
            }
            WirelessLinks.ModuleLink link = selectedLink();
            if (link == null) {
                return;
            }
            WirelessLinks.Reply reply = WirelessLinks.unlinkLinkReply(acting,
                    ModRegistration.WIRELESS_LINKS, pinned, link.far());
            NeoECOPrototype.LOGGER.info("wireless module unlink: {}", reply.english());
            selected = -1;
            refresh();
        }

        @Nullable
        private ServerLevel levelOf(net.minecraft.resources.ResourceKey<Level> dimension) {
            if (acting == null) {
                return null;
            }
            if (acting.dimension().equals(dimension)) {
                return acting;
            }
            MinecraftServer server = acting.getServer();
            return server == null ? null : server.getLevel(dimension);
        }
    }

    private static int pageCount(int entries, int perPage) {
        return Math.max(1, (entries + perPage - 1) / perPage);
    }

    /**
     * {@code used/max} with the amount in blue and the ceiling in red - the same pair the outlet's lamp shows.
     * Numbers only: they are not translated, and a label in front of them is what pushed this line out of its
     * recess in the client.
     */
    private static Component reading(long used, long max) {
        return Component.empty()
                .append(coloured(displayNumber(used), TEXT_USED))
                .append(Component.literal("/"))
                .append(coloured(displayNumber(max), TEXT_FULL));
    }

    private static Component coloured(String text, int rgb) {
        return Component.literal(text).withStyle(style -> style.withColor(rgb));
    }

    /** AE2 reports an unbounded budget as {@link Integer#MAX_VALUE}; printing that number helps nobody. */
    private static String displayNumber(long value) {
        return value >= Integer.MAX_VALUE ? "∞" : Long.toString(value);
    }

    private static Component dimensionName(net.minecraft.resources.ResourceKey<Level> dimension) {
        if (dimension.equals(Level.OVERWORLD)) {
            return Component.translatable("gui.neoecoprototype.wireless_module.dim_overworld");
        }
        if (dimension.equals(Level.NETHER)) {
            return Component.translatable("gui.neoecoprototype.wireless_module.dim_nether");
        }
        if (dimension.equals(Level.END)) {
            return Component.translatable("gui.neoecoprototype.wireless_module.dim_end");
        }
        return Component.literal(dimension.location().toString());
    }
}

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
    private static final int PORT_TEXT_X = 10;
    private static final int PORT_TEXT_WIDTH = 46;
    /**
     * Eight pixels, not the default nine: a recess is 48 wide and the line reads "node 1: 0/32", which is
     * wider than 46 at nine. Measured against the recess rather than guessed - the art leaves one pixel of
     * padding on each side.
     */
    private static final float LINE_FONT = 8;

    /** The outlet selector under the column: two arrows and the outlet they last moved onto. */
    private static final int PORT_PAGER_Y = 133;
    private static final int PORT_PAGER_PREV_X = 9;
    private static final int PORT_PAGER_PAGE_X = 24;
    private static final int PORT_PAGER_PAGE_WIDTH = 19;
    private static final int PORT_PAGER_NEXT_X = 45;
    private static final int PAGER_ARROW_WIDTH = 12;
    private static final int PAGER_ARROW_HEIGHT = 12;

    /** The middle column's three read-outs, each 48x14. */
    private static final int INFO_TEXT_X = 68;
    private static final int INFO_TEXT_WIDTH = 44;
    private static final int[] INFO_Y = {25, 43, 68};

    /** The middle column's two button rows: a pager over the device list, then the three actions. */
    private static final int MID_BUTTON_WIDTH = 14;
    private static final int MID_BUTTON_HEIGHT = 13;
    private static final int MID_PAGER_Y = 94;
    private static final int MID_ACTION_Y = 118;
    private static final int MID_PAGER_PREV_X = 67;
    private static final int MID_PAGER_PAGE_X = 82;
    private static final int MID_PAGER_NEXT_X = 97;

    /** Eight device rows, 47x12, fifteen pixels apart. */
    private static final int DEVICE_X = 122;
    private static final int DEVICE_Y = 34;
    private static final int DEVICE_WIDTH = 47;
    private static final int DEVICE_HEIGHT = 12;
    private static final int DEVICE_PITCH = 15;
    private static final int DEVICE_TEXT_X = 124;
    private static final int DEVICE_TEXT_WIDTH = 43;
    private static final int DEVICES_PER_PAGE = 8;

    /** The player inventory block the plate draws at the bottom: nine columns, four rows, eighteen apart. */
    private static final int INVENTORY_X = 8;
    private static final int INVENTORY_Y = 168;
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
                PORT_PAGER_PAGE_X, PORT_PAGER_Y + 2, PORT_PAGER_PAGE_WIDTH, TITLE_HEIGHT,
                COLOR, LINE_FONT, TextWrap.NONE));
        root.addChild(pagerArrow(PORT_PAGER_NEXT_X, PORT_PAGER_Y, PAGER_ARROW_WIDTH, PAGER_ARROW_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_next_port", () -> panel.turnPortSelection(1)));

        // Middle column: used/free/dimension, then the device pager, then the three actions.
        for (int index = 0; index < INFO_Y.length; index++) {
            int line = index;
            root.addChild(bound(() -> panel.infoLines[line], Horizontal.LEFT,
                    INFO_TEXT_X, INFO_Y[index] + 4, INFO_TEXT_WIDTH, 8, COLOR, LINE_FONT, TextWrap.NONE));
        }
        root.addChild(pagerArrow(MID_PAGER_PREV_X, MID_PAGER_Y, MID_BUTTON_WIDTH, MID_BUTTON_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_prev", () -> panel.turnDevicePage(-1)));
        root.addChild(bound(() -> panel.devicePageText, Horizontal.CENTER,
                MID_PAGER_PAGE_X, MID_PAGER_Y + 2, MID_BUTTON_WIDTH, TITLE_HEIGHT, COLOR, LINE_FONT,
                TextWrap.NONE));
        root.addChild(pagerArrow(MID_PAGER_NEXT_X, MID_PAGER_Y, MID_BUTTON_WIDTH, MID_BUTTON_HEIGHT,
                "gui.neoecoprototype.wireless_module.tip_next", () -> panel.turnDevicePage(1)));

        // Fourteen-pixel cells cannot hold two hanzi, so each action carries one and names itself on hover.
        root.addChild(actionButton(MID_PAGER_PREV_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.btn_highlight",
                "gui.neoecoprototype.wireless_module.tip_highlight", panel::highlightSelected));
        root.addChild(actionButton(MID_PAGER_PAGE_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.btn_refresh",
                "gui.neoecoprototype.wireless_module.tip_refresh", panel::refresh));
        root.addChild(actionButton(MID_PAGER_NEXT_X, MID_ACTION_Y,
                "gui.neoecoprototype.wireless_module.btn_unlink",
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

    private static Button actionButton(int x, int y, String textKey, String tooltipKey, Runnable action) {
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
        button.addChild(label(Component.translatable(textKey), Horizontal.CENTER,
                0, 2, MID_BUTTON_WIDTH, TITLE_HEIGHT, COLOR, 9));
        return button;
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
        private final Component[] deviceLines = new Component[DEVICES_PER_PAGE];
        private final List<WirelessLinks.ModuleLink> devices = new ArrayList<>();

        private Component portPageText = Component.empty();
        private Component devicePageText = Component.empty();
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
            for (int index = 0; index < deviceLines.length; index++) {
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
            portPageText = Component.translatable("gui.neoecoprototype.wireless_module.port_current",
                    selectedPort + 1);
            fillInfo(module);
            fillDevices(module);
        }

        private Component portLine(int index, @Nullable WirelessChannelModuleBlockEntity module) {
            if (module == null) {
                return Component.translatable("gui.neoecoprototype.wireless_module.port_line_unknown",
                        index + 1);
            }
            List<IGridNode> live = module.livePortNodes();
            IGridNode node = index < live.size() ? live.get(index) : null;
            if (node == null) {
                return Component.translatable("gui.neoecoprototype.wireless_module.port_line_free",
                        index + 1, displayNumber(module.portChannelCapacity()));
            }
            return Component.translatable("gui.neoecoprototype.wireless_module.port_line",
                    index + 1, node.getUsedChannels(), displayNumber(node.getMaxChannels()));
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
            row.addChild(bound(() -> portLines[slot], Horizontal.LEFT,
                    PORT_TEXT_X - PORT_X, 3, PORT_TEXT_WIDTH, TITLE_HEIGHT, COLOR, LINE_FONT,
                    TextWrap.NONE));
            return row;
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
                infoLines[0] = Component.translatable("gui.neoecoprototype.wireless_module.info_used",
                        displayNumber(used), displayNumber(total));
                infoLines[1] = Component.translatable("gui.neoecoprototype.wireless_module.info_free",
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
                    deviceLines[slot] = Component.empty();
                    continue;
                }
                WirelessLinks.ModuleLink row = devices.get(index);
                // The recess is 43 pixels and a device name with its coordinates is over a hundred, so the row
                // carries the position. The name is what needs the wider plate, not more code.
                deviceLines[slot] = Component.literal(WirelessLinks.coordinates(row.far()));
            }
        }

        /** One device row: a click target, a bound position, and a bound selection wash. */
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
            row.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                    event.hoverTooltips = new HoverTooltips(List.of(Component.translatable(
                            "gui.neoecoprototype.wireless_module.tip_device")), null, null, null));

            SelectionTint tint = new SelectionTint(slot);
            tint.bind(DataBindingBuilder.intValS2C(this::selectedSlot).build());
            row.addChild(tint);
            row.addChild(bound(() -> deviceLines[slot], Horizontal.CENTER,
                    DEVICE_TEXT_X - DEVICE_X, 2, DEVICE_TEXT_WIDTH, TITLE_HEIGHT, COLOR, LINE_FONT,
                    TextWrap.NONE));
            return row;
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

        private void highlightSelected() {
            if (acting == null) {
                return;
            }
            WirelessLinks.ModuleLink link = selectedLink();
            if (link == null) {
                player.displayClientMessage(Component.translatable(
                        "gui.neoecoprototype.wireless_module.no_selection"), true);
                return;
            }
            sparkle(pinned);
            sparkle(link.far());
            player.displayClientMessage(Component.translatable(
                    "gui.neoecoprototype.wireless_module.highlighted"), true);
        }

        private void unlinkSelected() {
            if (acting == null) {
                return;
            }
            WirelessLinks.ModuleLink link = selectedLink();
            if (link == null) {
                player.displayClientMessage(Component.translatable(
                        "gui.neoecoprototype.wireless_module.no_selection"), true);
                return;
            }
            player.displayClientMessage(WirelessLinks.unlinkLinkReply(acting,
                    ModRegistration.WIRELESS_LINKS, pinned, link.far()).asComponent(), false);
            selected = -1;
            refresh();
        }

        private void sparkle(GlobalPos at) {
            ServerLevel level = levelOf(at.dimension());
            if (level == null || !level.hasChunkAt(at.pos())) {
                return;
            }
            var center = at.pos().getCenter();
            level.sendParticles((ServerPlayer) player, ParticleTypes.END_ROD, false,
                    center.x, center.y + 0.5, center.z, 8, 0.3, 0.3, 0.3, 0.01);
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

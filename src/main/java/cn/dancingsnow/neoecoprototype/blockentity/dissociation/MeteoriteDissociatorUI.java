package cn.dancingsnow.neoecoprototype.blockentity.dissociation;

import cn.dancingsnow.neoecoae.gui.common.ECOModularUI;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IBindable;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IDataSource;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.FillDirection;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import com.lowdragmc.lowdraglib2.gui.util.WindowDragHelper;
import dev.vfyjxf.taffy.style.TaffyPosition;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The dissociator's panel, built the way eco builds its own machine screens: an LDLib2 block UI whose
 * widgets are placed at the pixel coordinates of the shipped art.
 *
 * <p>Every element is absolutely positioned and draws no frame of its own, because the recesses, the tank
 * windows and the progress groove are already painted into
 * {@code textures/gui/meteorite_dissociator.png}. Left to themselves, {@link ItemSlot} and {@link FluidSlot}
 * each paint a second recess one pixel off the baked one, which reads as a doubled border.
 *
 * <p>The block entity is asked for its state through plain getters and server-to-client bindings. The guide
 * and popup buttons are client-only actions; the six face buttons and the reset button run on the server.
 */
public final class MeteoriteDissociatorUI {

    private static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/gui/meteorite_dissociator.png");
    private static final ResourceLocation FACES_TEXTURE =
            NeoECOPrototype.id("textures/gui/meteorite_dissociator_faces.png");
    private static final ResourceLocation FACE_STATES_TEXTURE =
            NeoECOPrototype.id("textures/gui/meteorite_dissociator_face_states.png");
    private static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath("ae2", "guide");
    private static final ResourceLocation GUIDE_PAGE =
            NeoECOPrototype.id("neoecoprototype_intro/meteorite_dissociator.md");

    private static final int PANEL_WIDTH = 214;
    private static final int HEIGHT = 172;
    private static final int WIDTH = 242;

    /** The two baths: 8x58 windows inside the 10x60 frames the art draws. */
    private static final int COOLANT_X = 28;
    private static final int SOLUTION_X = 42;
    private static final int TANK_Y = 11;
    private static final int TANK_WIDTH = 8;
    private static final int TANK_HEIGHT = 58;

    /** Cells are 18x18 in the art; the item area is the inner 16x16, which is LDLib2's own 1px padding. */
    private static final int CELL_PADDING = 1;
    private static final int CATALYST_X = 56;
    private static final int CATALYST_Y = 31;
    private static final int PRODUCT_X = 114;
    private static final int PRODUCT_Y = 13;
    private static final int PRODUCT_COLUMNS = 4;
    /** The core cell is a 26x26 box; five pixels of padding keep the 16x16 stack centred inside it. */
    private static final int CORE_X = 77;
    private static final int CORE_Y = 27;
    private static final int CORE_PADDING = 5;

    private static final int INVENTORY_X = 26;
    private static final int INVENTORY_Y = 87;
    private static final int INVENTORY_LABEL_X = 27;
    private static final int INVENTORY_LABEL_Y = 77;

    /** The progress sprite is 5x17; the atlas keeps its filled version at x224 y74. */
    private static final int PROGRESS_X = 105;
    private static final int PROGRESS_Y = 31;
    private static final int PROGRESS_WIDTH = 5;
    private static final int PROGRESS_HEIGHT = 17;
    private static final int PROGRESS_ATLAS_X = 224;
    private static final int PROGRESS_ATLAS_Y = 74;
    /** The hovered-slot tint: AE2's own hover is 40% of this blue, and LDLib2 ships none. */
    private static final int HOVER_BLUE = 0x66_9C_D3_FF;
    private static final ColorRectTexture HOVER_RECT = new ColorRectTexture(HOVER_BLUE);

    private static final int GUIDE_BUTTON_X = 3;
    private static final int GUIDE_BUTTON_Y = 4;
    private static final int FACES_BUTTON_X = 3;
    private static final int FACES_BUTTON_Y = 26;
    private static final int TOOLBAR_BUTTON_WIDTH = 18;
    private static final int TOOLBAR_BUTTON_HEIGHT = 19;

    private static final int POPUP_X = 27;
    private static final int POPUP_Y = 35;
    private static final int POPUP_WIDTH = 101;
    private static final int POPUP_HEIGHT = 85;
    private static final int FACE_BUTTON_WIDTH = 18;
    private static final int FACE_BUTTON_HEIGHT = 20;

    private static FacePreviewRenderer facePreviewRenderer;

    public static void setFacePreviewRenderer(FacePreviewRenderer renderer) {
        facePreviewRenderer = renderer;
    }

    @FunctionalInterface
    public interface FacePreviewRenderer {
        void draw(GUIContext context, Direction direction, FacePreviewState state,
                  float x, float y);
    }

    public static final class FacePreviewState {
        private BlockPos neighborPos;
        private BlockState neighborState;
        private ItemStack icon = ItemStack.EMPTY;

        public boolean matches(BlockPos position, BlockState state) {
            return position.equals(neighborPos) && state == neighborState;
        }

        public void update(BlockPos position, BlockState state, ItemStack icon) {
            this.neighborPos = position;
            this.neighborState = state;
            this.icon = icon;
        }

        public ItemStack icon() {
            return icon;
        }
    }

    private MeteoriteDissociatorUI() {
    }

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, MeteoriteDissociatorBlockEntity host) {
        Inventory playerInventory = holder.player.getInventory();

        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT));
        root.addChild(new UIElement()
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(TEXTURE).setSprite(0, 0, PANEL_WIDTH, HEIGHT)))
                .setAllowHitTest(false)
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(0).top(0).width(PANEL_WIDTH).height(HEIGHT)));

        root.addChild(tank(host, COOLANT_X, false));
        root.addChild(tank(host, SOLUTION_X, true));

        // The core goes in the oversized 26x26 box, the catalyst in the small one beside it.
        root.addChild(place(new WideCell(new CoreSlot(host.getInputInventory().toContainer(), 0), CORE_PADDING),
                CORE_X, CORE_Y, CORE_PADDING));
        root.addChild(slot(new PlainSlot(host.getCatalystInventory().toContainer(), 0),
                CATALYST_X, CATALYST_Y, CELL_PADDING)
                .addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                        event.hoverTooltips = new HoverTooltips(
                                List.of(Component.translatable(
                                        "gui." + NeoECOPrototype.MOD_ID + ".dissociator_catalyst")),
                                null, null, null)));

        for (int index = 0; index < MeteoriteDissociatorBlockEntity.OUTPUT_SLOTS; index++) {
            root.addChild(slot(new ProductSlot(host.getOutputInventory().toContainer(), index),
                    PRODUCT_X + (index % PRODUCT_COLUMNS) * 18,
                    PRODUCT_Y + (index / PRODUCT_COLUMNS) * 18, CELL_PADDING));
        }

        root.addChild(new TextElement()
                .setText("container.inventory", true)
                .textStyle(textStyle -> textStyle.textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveHeight(true).textShadow(false).textColor(0x403e53))
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(INVENTORY_LABEL_X).top(INVENTORY_LABEL_Y)));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                root.addChild(slot(new Slot(playerInventory, 9 + row * 9 + column, 0, 0),
                        INVENTORY_X + column * 18, INVENTORY_Y + row * 18, CELL_PADDING));
            }
        }
        for (int column = 0; column < 9; column++) {
            root.addChild(slot(new Slot(playerInventory, column, 0, 0),
                    INVENTORY_X + column * 18, INVENTORY_Y + 58, CELL_PADDING));
        }

        var progress = new ProgressBar();
        progress.setId("dissociator_progress");
        progress.bind(DataBindingBuilder.floatValS2C(() -> host.progressFraction()).build());
        progress.setAllowHitTest(false);
        root.addChild(progress.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(PROGRESS_X).top(PROGRESS_Y).width(PROGRESS_WIDTH).height(PROGRESS_HEIGHT)));

        UIElement progressHover = new UIElement();
        progressHover.setId("dissociator_progress_hover");
        progressHover.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = HoverTooltips.empty().append(
                        Component.literal("%.0f%%".formatted(progress.getValue() * 100.0F))));
        root.addChild(progressHover.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(PROGRESS_X - 1).top(PROGRESS_Y - 1)
                .width(PROGRESS_WIDTH + 3).height(PROGRESS_HEIGHT + 2)));

        UIElement popup = facePopup(host, holder.player);
        root.addChild(popupButton(GUIDE_BUTTON_X, GUIDE_BUTTON_Y,
                TOOLBAR_BUTTON_WIDTH, TOOLBAR_BUTTON_HEIGHT,
                toolbarTexture(GUIDE_BUTTON_X, GUIDE_BUTTON_Y),
                sprite(TEXTURE, 224, 16, TOOLBAR_BUTTON_WIDTH, TOOLBAR_BUTTON_HEIGHT),
                () -> openGuide(holder.player),
                "gui.neoecoprototype.dissociator_guide", false));
        root.addChild(popupButton(FACES_BUTTON_X, FACES_BUTTON_Y,
                TOOLBAR_BUTTON_WIDTH, TOOLBAR_BUTTON_HEIGHT,
                toolbarTexture(FACES_BUTTON_X, FACES_BUTTON_Y),
                sprite(TEXTURE, 224, 48, TOOLBAR_BUTTON_WIDTH, TOOLBAR_BUTTON_HEIGHT),
                () -> popup.setDisplay(!popup.isDisplayed()),
                "gui.neoecoprototype.dissociator_faces", false));
        root.addChild(popup);

        return ECOModularUI.create(root, holder.player);
    }

    private static void openGuide(Player player) {
        GuidesCommon.openGuide(player, GUIDE_ID, PageAnchor.page(GUIDE_PAGE));
    }

    private static UIElement facePopup(MeteoriteDissociatorBlockEntity host, Player player) {
        UIElement popup = new UIElement()
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(FACES_TEXTURE).setSprite(0, 0, POPUP_WIDTH, POPUP_HEIGHT)))
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(POPUP_X).top(POPUP_Y).width(POPUP_WIDTH).height(POPUP_HEIGHT));
        popup.setDisplay(false);
        WindowDragHelper.setDragMove(popup, popup,
                event -> event.button == 0 && event.target == popup, null);

        popup.addChild(popupButton(3, 9, 17, 20,
                sprite(FACES_TEXTURE, 122, 96, 17, 20),
                sprite(FACES_TEXTURE, 143, 96, 17, 20),
                () -> openGuide(player), "gui.neoecoprototype.dissociator_guide", false));
        popup.addChild(popupButton(3, 31, 17, 20,
                sprite(FACES_TEXTURE, 122, 121, 17, 20),
                sprite(FACES_TEXTURE, 143, 121, 17, 20),
                host::resetFaceModes, "gui.neoecoprototype.dissociator_faces_reset", true));
        popup.addChild(popupButton(75, 0, 20, 20,
                sprite(FACES_TEXTURE, 75, 0, 20, 20),
                sprite(FACES_TEXTURE, 166, 120, 20, 20),
                () -> popup.setDisplay(false), "gui.neoecoprototype.dissociator_faces_close", false));

        popup.addChild(faceButton(host, Direction.UP, 48, 14));
        popup.addChild(faceButton(host, Direction.NORTH, 48, 36));
        popup.addChild(faceButton(host, Direction.WEST, 27, 36));
        popup.addChild(faceButton(host, Direction.EAST, 69, 36));
        popup.addChild(faceButton(host, Direction.DOWN, 48, 58));
        popup.addChild(faceButton(host, Direction.SOUTH, 69, 58));
        return popup;
    }

    private static IGuiTexture toolbarTexture(int x, int y) {
        return sprite(TEXTURE, x, y, TOOLBAR_BUTTON_WIDTH, TOOLBAR_BUTTON_HEIGHT);
    }

    private static IGuiTexture sprite(ResourceLocation texture, int x, int y, int width, int height) {
        return SpriteTexture.of(texture).setSprite(x, y, width, height);
    }

    private static Button popupButton(int x, int y, int width, int height,
                                      IGuiTexture normal, IGuiTexture hover, Runnable action,
                                      String tooltip, boolean server) {
        SpriteButton button = new SpriteButton(normal, hover);
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height).paddingAll(0));
        if (server) {
            button.setOnServerClick(event -> action.run());
        } else {
            button.setOnClick(event -> action.run());
        }
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(
                        List.of(Component.translatable(tooltip)), null, null, null));
        return button;
    }

    private static Component faceModeName(MeteoriteDissociatorBlockEntity.FaceMode mode) {
        if (mode == MeteoriteDissociatorBlockEntity.FaceMode.NONE) {
            return Component.translatable("gui.neoecoprototype.dissociator_face_mode.none");
        }
        if (mode == MeteoriteDissociatorBlockEntity.FaceMode.INPUT) {
            return Component.translatable("gui.neoecoprototype.dissociator_face_mode.input");
        }
        if (mode == MeteoriteDissociatorBlockEntity.FaceMode.OUTPUT) {
            return Component.translatable("gui.neoecoprototype.dissociator_face_mode.output");
        }
        return Component.translatable("gui.neoecoprototype.dissociator_face_mode.both");
    }

    private static Component faceName(Direction direction) {
        if (direction == Direction.DOWN) {
            return Component.translatable("gui.neoecoprototype.dissociator_face.down");
        }
        if (direction == Direction.NORTH) {
            return Component.translatable("gui.neoecoprototype.dissociator_face.north");
        }
        if (direction == Direction.WEST) {
            return Component.translatable("gui.neoecoprototype.dissociator_face.west");
        }
        if (direction == Direction.EAST) {
            return Component.translatable("gui.neoecoprototype.dissociator_face.east");
        }
        if (direction == Direction.SOUTH) {
            return Component.translatable("gui.neoecoprototype.dissociator_face.south");
        }
        return Component.translatable("gui.neoecoprototype.dissociator_face.up");
    }

    private static FaceButton faceButton(MeteoriteDissociatorBlockEntity host, Direction direction,
                                         int x, int y) {
        FaceButton button = new FaceButton(host, direction);
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(FACE_BUTTON_WIDTH).height(FACE_BUTTON_HEIGHT));
        button.setId("dissociator_face_" + direction.getName());
        button.bind(DataBindingBuilder.intValS2C(() -> host.getFaceMode(direction).ordinal()).build());
        return button;
    }

    /** Both baths are inputs; neither can be pulled back into a bucket. */
    private static FluidSlot tank(MeteoriteDissociatorBlockEntity host, int x, boolean solution) {
        FluidSlot slot = new FluidSlot()
                .bind(solution ? host.getSolutionTank() : host.getCoolantTank(), 0)
                .slotStyle(style -> style.fillDirection(FillDirection.DOWN_TO_UP)
                        .hoverOverlay(new ColorRectTexture(HOVER_BLUE)))
                .setAllowClickFilled(false)
                .setAllowClickDrained(true);
        // LDLib2 names these from the tank's side: "drained" empties the held container into it.
        slot.setId(solution ? "dissociator_solution" : "dissociator_coolant");
        slot.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        slot.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(TANK_Y).width(TANK_WIDTH).height(TANK_HEIGHT).paddingAll(0));
        return slot;
    }

    /**
     * A widget over one of the art's cells. The box is the 16x16 item area grown by {@code padding} on each
     * side, which is what centres it in a cell the art drew bigger: one pixel for the 18x18 cells, four for
     * the oversized core cell.
     */
    private static ItemSlot slot(Slot slot, int x, int y, int padding) {
        return place(new ItemSlot(slot), x, y, padding);
    }

    private static ItemSlot place(ItemSlot widget, int x, int y, int padding) {
        widget.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        widget.slotStyle(style -> style.hoverOverlay(HOVER_RECT));
        widget.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(16 + padding * 2).height(16 + padding * 2).paddingAll(padding));
        return widget;
    }

    /**
     * The core cell, and the only widget that needs its own hover: {@code ItemSlot.drawHover} paints a
     * hardcoded 16x16 at the content origin -- the size is in the caller, not in the texture -- so a tint
     * fed through {@code hoverOverlay} can never cover the 26x26 recess no matter which image is given.
     * Drawing from minus the padding puts the rectangle back at the cell's own corner.
     */
    private static final class WideCell extends ItemSlot {
        private final int padding;

        WideCell(Slot slot, int padding) {
            super(slot);
            this.padding = padding;
        }

        @Override
        protected void drawHover(GUIContext context) {
            context.drawTexture(HOVER_RECT, -padding, -padding, padding * 2 + 16, padding * 2 + 16);
        }
    }

    /** The core: one stack, and only one stack, and whatever the shipped table names as an input. */
    private static final class CoreSlot extends Slot {
        CoreSlot(net.minecraft.world.Container container, int index) {
            super(container, index, 0, 0);
        }
    }

    private static final class PlainSlot extends Slot {
        PlainSlot(net.minecraft.world.Container container, int index) {
            super(container, index, 0, 0);
        }
    }

    /** Products come out; nothing goes back in, so a shift-click cannot lose an item in a full slot. */
    private static final class ProductSlot extends Slot {
        ProductSlot(net.minecraft.world.Container container, int index) {
            super(container, index, 0, 0);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return false;
        }

        @Override
        public int getMaxStackSize(ItemStack stack) {
            return MeteoriteDissociatorBlockEntity.OUTPUT_SLOTS * 64;
        }
    }

    private static final class SpriteButton extends Button {
        private final IGuiTexture normal;
        private final IGuiTexture hover;

        private SpriteButton(IGuiTexture normal, IGuiTexture hover) {
            this.normal = normal;
            this.hover = hover;
            noText();
        }

        @Override
        public void drawBackgroundAdditional(GUIContext context) {
            IGuiTexture texture = getState() == State.DEFAULT ? normal : hover;
            context.drawTexture(texture, getPositionX(), getPositionY(), getSizeWidth(), getSizeHeight());
        }
    }

    private static final class FaceButton extends Button implements IBindable<Integer> {
        private final MeteoriteDissociatorBlockEntity host;
        private Direction direction;
        private FacePreviewState previewState;
        private MeteoriteDissociatorBlockEntity.FaceMode mode =
                MeteoriteDissociatorBlockEntity.FaceMode.INPUT;

        private FaceButton(MeteoriteDissociatorBlockEntity host, Direction direction) {
            this.host = host;
            this.direction = direction;
            noText();
            setOnServerClick(event -> {
                if (event.button == 0) {
                    host.cycleFaceMode(direction);
                } else if (event.button == 1) {
                    host.setFaceMode(direction, host.getFaceMode(direction).previous());
                }
            });
            addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                    event.hoverTooltips = new HoverTooltips(
                            List.of(faceName(direction()), faceModeName(mode)), null, null, null));
            paint();
        }

        private Direction direction() {
            if (direction == null && getId() != null && getId().startsWith("dissociator_face_")) {
                direction = Direction.byName(getId().substring("dissociator_face_".length()));
            }
            return direction;
        }

        @Override
        public Integer getValue() {
            return mode.ordinal();
        }

        @Override
        public IDataSource<Integer> setValue(@Nullable Integer value) {
            int ordinal = value == null ? MeteoriteDissociatorBlockEntity.FaceMode.INPUT.ordinal() : value;
            var values = MeteoriteDissociatorBlockEntity.FaceMode.values();
            mode = values[Mth.clamp(ordinal, 0, values.length - 1)];
            paint();
            return this;
        }

        @Override
        public void drawBackgroundAdditional(GUIContext context) {
            super.drawBackgroundAdditional(context);
            Direction faceDirection = direction();
            if (facePreviewRenderer != null && faceDirection != null) {
                if (previewState == null) {
                    previewState = new FacePreviewState();
                }
                facePreviewRenderer.draw(context, faceDirection, previewState,
                        getPositionX() + 2, getPositionY() + 3);
            }
        }

        private void paint() {
            IGuiTexture normal = SpriteTexture.of(FACE_STATES_TEXTURE)
                    .setSprite(mode.ordinal() * FACE_BUTTON_WIDTH, 0,
                            FACE_BUTTON_WIDTH, FACE_BUTTON_HEIGHT);
            IGuiTexture hover = SpriteTexture.of(FACE_STATES_TEXTURE)
                    .setSprite((mode.ordinal() + MeteoriteDissociatorBlockEntity.FaceMode.values().length)
                                    * FACE_BUTTON_WIDTH, 0,
                            FACE_BUTTON_WIDTH, FACE_BUTTON_HEIGHT);
            buttonStyle(style -> style.baseTexture(normal).hoverTexture(hover).pressedTexture(hover));
        }
    }

    /** The groove filled from the bottom. The art draws the empty groove; this only paints the level. */
    private static final class ProgressBar extends UIElement implements IBindable<Float> {
        private float value;

        @Override
        public IDataSource<Float> setValue(@Nullable Float newValue) {
            this.value = newValue == null ? 0.0F : Mth.clamp(newValue, 0.0F, 1.0F);
            return this;
        }

        @Override
        public Float getValue() {
            return value;
        }

        @Override
        public void drawContents(GUIContext guiContext) {
            int filled = Mth.ceil(PROGRESS_HEIGHT * value);
            if (filled <= 0) {
                return;
            }
            int x = Mth.floor(getPositionX());
            int y = Mth.floor(getPositionY());
            IGuiTexture fill = SpriteTexture.of(TEXTURE).setSprite(PROGRESS_ATLAS_X,
                    PROGRESS_ATLAS_Y + PROGRESS_HEIGHT - filled, PROGRESS_WIDTH, filled);
            guiContext.drawTexture(fill, x, y + PROGRESS_HEIGHT - filled,
                    PROGRESS_WIDTH, filled);
        }
    }
}

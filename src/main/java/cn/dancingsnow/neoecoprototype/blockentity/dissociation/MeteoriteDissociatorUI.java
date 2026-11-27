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
import com.lowdragmc.lowdraglib2.gui.ui.elements.FluidSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

/**
 * The dissociator's panel, built the way eco builds its own machine screens: an LDLib2 block UI whose
 * widgets are placed at the pixel coordinates of the shipped art.
 *
 * <p>Every element is absolutely positioned and draws no frame of its own, because the recesses, the tank
 * windows and the progress groove are already painted into
 * {@code textures/gui/meteorite_dissociator.png}. Left to themselves, {@code ItemSlot} and {@code FluidSlot}
 * each paint a second recess one pixel off the baked one, which reads as a doubled border.
 *
 * <p>The block entity is asked for its state through plain getters and one server-to-client binding; no
 * value is pushed the other way, so nothing here can be written by a client.
 */
public final class MeteoriteDissociatorUI {

    private static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/gui/meteorite_dissociator.png");
    private static final int WIDTH = 168;
    private static final int HEIGHT = 170;

    /** The two baths: 8x58 windows inside the 10x60 frames the art draws. */
    private static final int COOLANT_X = 5;
    private static final int SOLUTION_X = 19;
    private static final int TANK_Y = 9;
    private static final int TANK_WIDTH = 8;
    private static final int TANK_HEIGHT = 58;

    /** Cells are 18x18 in the art; the item area is the inner 16x16, which is LDLib2's own 1px padding. */
    private static final int CELL_PADDING = 1;
    private static final int CATALYST_X = 33;
    private static final int CATALYST_Y = 29;
    private static final int PRODUCT_X = 91;
    private static final int PRODUCT_Y = 11;
    private static final int PRODUCT_COLUMNS = 4;
    /** The core cell is a 24x24 box; four pixels of padding keep the stack centred inside it. */
    private static final int CORE_X = 55;
    private static final int CORE_Y = 26;
    private static final int CORE_SIZE = 24;
    private static final int CORE_PADDING = 4;

    private static final int INVENTORY_X = 3;
    private static final int INVENTORY_Y = 85;
    private static final int INVENTORY_LABEL_X = 4;
    private static final int INVENTORY_LABEL_Y = 75;

    /** The progress groove: a 4x16 core inside a 6x18 frame at x82 y29. */
    private static final int PROGRESS_X = 83;
    private static final int PROGRESS_Y = 30;
    private static final int PROGRESS_WIDTH = 4;
    private static final int PROGRESS_HEIGHT = 16;
    private static final int PROGRESS_COLOR = 0xFF_5F_C8_64;
    /** The hovered-slot tint: AE2's own hover is 40% of this blue, and LDLib2 ships none. */
    private static final int HOVER_BLUE = 0x66_9C_D3_FF;
    private static final ColorRectTexture HOVER_RECT = new ColorRectTexture(HOVER_BLUE);

    private MeteoriteDissociatorUI() {
    }

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, MeteoriteDissociatorBlockEntity host) {
        Inventory playerInventory = holder.player.getInventory();

        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(TEXTURE).setSprite(0, 0, WIDTH, HEIGHT)));

        root.addChild(tank(host, COOLANT_X, true));
        root.addChild(tank(host, SOLUTION_X, false));

        // The core goes in the oversized 24x24 box, the catalyst in the small one beside it.
        root.addChild(place(new WideCell(new CoreSlot(host.getInputInventory().toContainer(), 0), CORE_PADDING),
                CORE_X, CORE_Y, CORE_PADDING));
        root.addChild(slot(new PlainSlot(host.getCatalystInventory().toContainer(), 0),
                CATALYST_X, CATALYST_Y, CELL_PADDING)
                .addEventListener(com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents.HOVER_TOOLTIPS, event ->
                        event.hoverTooltips = new com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips(
                                java.util.List.of(net.minecraft.network.chat.Component.translatable(
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

        root.addChild(new ProgressBar()
                .bind(DataBindingBuilder.floatValS2C(() -> host.progressFraction()).build())
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(PROGRESS_X).top(PROGRESS_Y).width(PROGRESS_WIDTH).height(PROGRESS_HEIGHT)));

        return ECOModularUI.create(root, holder.player);
    }

    /** Coolant takes fluid and gives none back; the spent bath is the other way round. */
    private static FluidSlot tank(MeteoriteDissociatorBlockEntity host, int x, boolean inlet) {
        FluidSlot slot = new FluidSlot()
                .bind(inlet ? host.getCoolantTank() : host.getSolutionTank(), 0)
                .slotStyle(style -> style.fillDirection(FillDirection.DOWN_TO_UP)
                        .hoverOverlay(new ColorRectTexture(HOVER_BLUE)))
                .setAllowClickFilled(inlet)
                .setAllowClickDrained(!inlet);
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
     * fed through {@code hoverOverlay} can never cover the 24x24 recess no matter which image is given.
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
            int filled = Mth.floor(PROGRESS_HEIGHT * value);
            if (filled <= 0) {
                return;
            }
            int x = Mth.floor(getPositionX());
            int y = Mth.floor(getPositionY());
            guiContext.graphics.fill(x, y + PROGRESS_HEIGHT - filled, x + PROGRESS_WIDTH,
                    y + PROGRESS_HEIGHT, PROGRESS_COLOR);
        }
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.music;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IBindable;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.IDataSource;
import com.lowdragmc.lowdraglib2.gui.sync.bindings.impl.DataBindingBuilder;
import com.lowdragmc.lowdraglib2.gui.texture.ColorRectTexture;
import com.lowdragmc.lowdraglib2.gui.texture.IGuiTexture;
import com.lowdragmc.lowdraglib2.gui.texture.SpriteTexture;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;
import com.lowdragmc.lowdraglib2.gui.ui.UI;
import com.lowdragmc.lowdraglib2.gui.ui.UIElement;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.ItemSlot;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Label;
import com.lowdragmc.lowdraglib2.gui.ui.elements.TextElement;
import com.lowdragmc.lowdraglib2.gui.ui.event.HoverTooltips;
import com.lowdragmc.lowdraglib2.gui.ui.event.UIEvents;
import com.lowdragmc.lowdraglib2.gui.ui.rendering.GUIContext;
import dev.vfyjxf.taffy.style.TaffyPosition;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

import javax.annotation.Nullable;
import java.util.List;

/**
 * The recorder's panel: a line naming the song the room is hearing, twelve input discs on the left, one
 * data disc on the right, and a groove that fills while that song plays. Art is drawn at the same
 * 168x170, with the same cells and player inventory as the dissociator panel, so the two machines read
 * as one family.
 *
 * <p>Only the progress and the song line travel server to client; the slots use the inventories
 * themselves, so a client cannot write anything the filters would refuse.
 */
public final class MusicRecorderUI {

    private static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/gui/music_recorder.png");
    private static final int WIDTH = 168;
    private static final int HEIGHT = 170;

    /** The song line sits in the empty strip above the cells; long names roll on hover. */
    private static final int SONG_X = 7;
    private static final int SONG_Y = 2;
    private static final int SONG_WIDTH = 154;
    private static final int SONG_HEIGHT = 9;

    /** The twelve input cells: six columns, two rows, 18x18 with a one-pixel inner padding. */
    private static final int INPUT_X = 7;
    private static final int INPUT_Y = 12;
    private static final int INPUT_COLUMNS = 6;
    private static final int CELL_PADDING = 1;
    private static final int CELL_SIZE = 18;

    /** The one data disc cell, vertically centred beside the two rows. */
    private static final int DATA_X = 133;
    private static final int DATA_Y = 21;

    /** The progress groove, filled left to right. The art draws a one-pixel frame around this area. */
    private static final int PROGRESS_X = 8;
    private static final int PROGRESS_Y = 57;
    private static final int PROGRESS_WIDTH = 106;
    private static final int PROGRESS_HEIGHT = 4;
    private static final int PROGRESS_COLOR = 0xFF_5F_C8_64;

    private static final int INVENTORY_X = 3;
    private static final int INVENTORY_Y = 85;
    private static final int INVENTORY_LABEL_X = 4;
    private static final int INVENTORY_LABEL_Y = 75;

    /** AE2's own hover is 40% of this blue; LDLib2 ships none. */
    private static final int HOVER_BLUE = 0x66_9C_D3_FF;
    private static final ColorRectTexture HOVER_RECT = new ColorRectTexture(HOVER_BLUE);

    private MusicRecorderUI() {
    }

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, MusicRecorderBlockEntity host) {
        Inventory playerInventory = holder.player.getInventory();

        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(TEXTURE).setSprite(0, 0, WIDTH, HEIGHT)));

        for (int index = 0; index < MusicRecorderBlockEntity.DISC_SLOTS; index++) {
            root.addChild(slot(new Slot(host.getDiscInventory().toContainer(), index, 0, 0),
                    INPUT_X + (index % INPUT_COLUMNS) * CELL_SIZE,
                    INPUT_Y + (index / INPUT_COLUMNS) * CELL_SIZE));
        }

        root.addChild(slot(new Slot(host.getDataDiscInventory().toContainer(),
                        MusicRecorderBlockEntity.DATA_SLOT, 0, 0), DATA_X, DATA_Y)
                .addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                        event.hoverTooltips = new HoverTooltips(
                                List.of(Component.translatable(
                                        "gui.neoecoprototype.music_recorder_data_slot")),
                                null, null, null)));

        Label songLine = new Label();
        songLine.setText(host.currentSongLine());
        songLine.textStyle(textStyle -> textStyle.textWrap(TextWrap.HOVER_ROLL)
                .textShadow(false).textColor(0x403e53));
        songLine.bind(DataBindingBuilder.componentS2C(host::currentSongLine).build());
        songLine.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(SONG_X).top(SONG_Y).width(SONG_WIDTH).height(SONG_HEIGHT));
        root.addChild(songLine);

        root.addChild(new TextElement()
                .setText("container.inventory", true)
                .textStyle(textStyle -> textStyle.textWrap(TextWrap.HOVER_ROLL)
                        .adaptiveHeight(true).textShadow(false).textColor(0x403e53))
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(INVENTORY_LABEL_X).top(INVENTORY_LABEL_Y)));

        for (int row = 0; row < 3; row++) {
            for (int column = 0; column < 9; column++) {
                root.addChild(slot(new Slot(playerInventory, 9 + row * 9 + column, 0, 0),
                        INVENTORY_X + column * 18, INVENTORY_Y + row * 18));
            }
        }
        for (int column = 0; column < 9; column++) {
            root.addChild(slot(new Slot(playerInventory, column, 0, 0),
                    INVENTORY_X + column * 18, INVENTORY_Y + 58));
        }

        root.addChild(new ProgressBar()
                .bind(DataBindingBuilder.floatValS2C(() -> host.progressFraction()).build())
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(PROGRESS_X).top(PROGRESS_Y)
                        .width(PROGRESS_WIDTH).height(PROGRESS_HEIGHT)));

        return new ModularUI(UI.of(root), holder.player);
    }

    /** The art already paints each cell, so the widget draws no recess of its own. */
    private static ItemSlot slot(Slot slot, int x, int y) {
        ItemSlot widget = new ItemSlot(slot);
        widget.style(style -> style.backgroundTexture(IGuiTexture.EMPTY));
        widget.slotStyle(style -> style.hoverOverlay(HOVER_RECT));
        widget.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(CELL_SIZE).height(CELL_SIZE).paddingAll(CELL_PADDING));
        return widget;
    }

    /** The groove filled from the left. The art draws the empty groove; this only paints the level. */
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
            int filled = Mth.floor(PROGRESS_WIDTH * value);
            if (filled <= 0) {
                return;
            }
            int x = Mth.floor(getPositionX());
            int y = Mth.floor(getPositionY());
            guiContext.graphics.fill(x, y, x + filled, y + PROGRESS_HEIGHT, PROGRESS_COLOR);
        }
    }
}

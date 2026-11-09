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
import com.lowdragmc.lowdraglib2.gui.ui.data.Horizontal;
import com.lowdragmc.lowdraglib2.gui.ui.data.TextWrap;
import com.lowdragmc.lowdraglib2.gui.ui.elements.Button;
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
 * The recorder's panel: a titled AE machine face with the song line under it, twelve input discs in a
 * four-by-three block, one data disc beside them, a pause and a skip button, a groove that fills while a
 * song plays, and the player inventory.
 *
 * <p>The panel is drawn at 176x204; the bottom half (label, three rows, hotbar) is the dissociator's
 * player-inventory block, so the two machines read as one family.
 *
 * <p>Only the song line, the pause state and the progress travel server to client; the slots use the
 * inventories themselves, so a client cannot write anything the filters would refuse. The two buttons
 * are server-click handlers: the pause handler runs on the server's own block entity.
 */
public final class MusicRecorderUI {

    private static final ResourceLocation TEXTURE =
            NeoECOPrototype.id("textures/gui/music_recorder.png");
    private static final ResourceLocation BUTTONS =
            NeoECOPrototype.id("textures/gui/music_recorder_buttons.png");
    private static final int WIDTH = 176;
    private static final int HEIGHT = 204;

    private static final int TEXT_COLOR = 0x403e53;

    /** The machine name, and under it the song the room is hearing. */
    private static final int TITLE_X = 8;
    private static final int TITLE_Y = 5;
    private static final int TITLE_WIDTH = 160;
    private static final int TITLE_HEIGHT = 9;
    private static final int SONG_X = 8;
    private static final int SONG_Y = 16;
    private static final int SONG_WIDTH = 160;
    private static final int SONG_HEIGHT = 9;

    /** The twelve input cells: four columns, three rows, 18x18 with a one-pixel inner padding. */
    private static final int INPUT_X = 8;
    private static final int INPUT_Y = 32;
    private static final int INPUT_COLUMNS = 4;
    private static final int CELL_PADDING = 1;
    private static final int CELL_SIZE = 18;

    /** The one data disc cell, centred beside the block of input cells. */
    private static final int DATA_X = 118;
    private static final int DATA_Y = 50;

    /** The two buttons, stacked at the right edge. */
    private static final int BUTTON_X = 152;
    private static final int PAUSE_Y = 32;
    private static final int SKIP_Y = 52;
    private static final int BUTTON_SIZE = 16;
    private static final int GLYPH_INSET = 3;
    private static final int GLYPH_SIZE = 10;

    /** The progress groove, filled left to right. The art draws a one-pixel frame around this area. */
    private static final int PROGRESS_X = 8;
    private static final int PROGRESS_Y = 92;
    private static final int PROGRESS_WIDTH = 160;
    private static final int PROGRESS_HEIGHT = 4;
    private static final int PROGRESS_COLOR = 0xFF_5F_C8_64;

    private static final int INVENTORY_X = 7;
    private static final int INVENTORY_Y = 119;
    private static final int INVENTORY_LABEL_X = 8;
    private static final int INVENTORY_LABEL_Y = 109;

    /** AE2's own hover is 40% of this blue; LDLib2 ships none. */
    private static final int HOVER_BLUE = 0x66_9C_D3_FF;
    private static final ColorRectTexture HOVER_RECT = new ColorRectTexture(HOVER_BLUE);

    /** Glyph columns in the button atlas: pause bars, play triangle, skip. */
    private static final int GLYPH_PAUSE = 0;
    private static final int GLYPH_PLAY = 1;
    private static final int GLYPH_SKIP = 2;

    private MusicRecorderUI() {
    }

    public static ModularUI create(BlockUIMenuType.BlockUIHolder holder, MusicRecorderBlockEntity host) {
        Inventory playerInventory = holder.player.getInventory();

        UIElement root = new UIElement()
                .layout(layout -> layout.width(WIDTH).height(HEIGHT))
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(TEXTURE).setSprite(0, 0, WIDTH, HEIGHT)));

        root.addChild(text(Component.translatable("block.neoecoprototype.music_recorder"),
                Horizontal.CENTER, TITLE_X, TITLE_Y, TITLE_WIDTH, TITLE_HEIGHT));

        Label songLine = new Label();
        songLine.setText(host.currentSongLine());
        songLine.textStyle(textStyle -> textStyle.textWrap(TextWrap.HOVER_ROLL)
                .textShadow(false).textColor(TEXT_COLOR).textAlignHorizontal(Horizontal.LEFT));
        songLine.bind(DataBindingBuilder.componentS2C(host::currentSongLine).build());
        songLine.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(SONG_X).top(SONG_Y).width(SONG_WIDTH).height(SONG_HEIGHT));
        root.addChild(songLine);

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

        PauseGlyph pauseGlyph = new PauseGlyph(icon(GLYPH_PLAY), icon(GLYPH_PAUSE),
                host.isPaused());
        pauseGlyph.bind(DataBindingBuilder.boolS2C(host::isPaused).build());
        root.addChild(button(BUTTON_X, PAUSE_Y, pauseGlyph, host::togglePaused,
                "gui.neoecoprototype.music_recorder_pause"));
        root.addChild(button(BUTTON_X, SKIP_Y, icon(GLYPH_SKIP), host::skipCurrentSong,
                "gui.neoecoprototype.music_recorder_skip"));

        root.addChild(text(Component.translatable("container.inventory"),
                Horizontal.LEFT, INVENTORY_LABEL_X, INVENTORY_LABEL_Y, SONG_WIDTH, TITLE_HEIGHT));

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

    private static TextElement text(Component text, Horizontal align, int x, int y, int width,
                                     int height) {
        Label label = new Label();
        label.setText(text);
        label.textStyle(textStyle -> textStyle.textWrap(TextWrap.HOVER_ROLL)
                .textShadow(false).textColor(TEXT_COLOR).textAlignHorizontal(align));
        label.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(width).height(height));
        return label;
    }

    /** One 10x10 glyph from the second atlas row. The plate under it handles the mouse. */
    private static UIElement icon(int column) {
        return new UIElement()
                .style(style -> style.backgroundTexture(
                        SpriteTexture.of(BUTTONS).setSprite(
                                column * 16 + GLYPH_INSET, 16 + GLYPH_INSET,
                                GLYPH_SIZE, GLYPH_SIZE)))
                .setAllowHitTest(false)
                .layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                        .left(GLYPH_INSET).top(GLYPH_INSET)
                        .width(GLYPH_SIZE).height(GLYPH_SIZE));
    }

    private static Button button(int x, int y, UIElement glyph, Runnable action, String tooltip) {
        Button button = new Button().noText();
        button.buttonStyle(style -> style
                .baseTexture(plate(0))
                .hoverTexture(plate(1))
                .pressedTexture(plate(2)));
        button.layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                .left(x).top(y).width(BUTTON_SIZE).height(BUTTON_SIZE));
        button.addChild(glyph);
        button.setOnServerClick(event -> action.run());
        button.addEventListener(UIEvents.HOVER_TOOLTIPS, event ->
                event.hoverTooltips = new HoverTooltips(
                        List.of(Component.translatable(tooltip)), null, null, null));
        return button;
    }

    /** One 16x16 plate from the first atlas row: resting, hovered, pressed. */
    private static IGuiTexture plate(int state) {
        return SpriteTexture.of(BUTTONS).setSprite(state * BUTTON_SIZE, 0,
                BUTTON_SIZE, BUTTON_SIZE);
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

    /**
     * The pause button's face: a pause sign while the run is going, a play sign while it is paused - the
     * icon shows what the click will do. The state arrives through the same S2C binding as the song line.
     */
    private static final class PauseGlyph extends UIElement implements IBindable<Boolean> {
        private final UIElement play;
        private final UIElement pause;
        private boolean paused;

        private PauseGlyph(UIElement play, UIElement pause, boolean paused) {
            this.play = play;
            this.pause = pause;
            setAllowHitTest(false);
            // Without a box of its own Taffy sizes this wrapper to zero and the absolute glyphs inside
            // it land offset from the plate; fill the button so they sit where the skip glyph sits.
            layout(layout -> layout.positionType(TaffyPosition.ABSOLUTE)
                    .left(0).top(0).width(BUTTON_SIZE).height(BUTTON_SIZE));
            addChild(play);
            addChild(pause);
            setValue(paused);
        }

        @Override
        public Boolean getValue() {
            return paused;
        }

        @Override
        public IDataSource<Boolean> setValue(@Nullable Boolean value) {
            paused = Boolean.TRUE.equals(value);
            play.setDisplay(paused);
            pause.setDisplay(!paused);
            return this;
        }
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

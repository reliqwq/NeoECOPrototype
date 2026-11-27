package cn.dancingsnow.neoecoprototype.client;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import appeng.client.gui.AEBaseScreen;
import appeng.client.gui.Icon;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.style.Text;
import appeng.client.gui.style.TextAlignment;
import appeng.client.gui.style.Position;
import appeng.client.gui.widgets.IconButton;
import appeng.menu.SlotSemantics;
import cn.dancingsnow.neoecoae.grid.storage.cell.ECOStorageCellSettings;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.menu.EnergizedMEChestMenu;
import guideme.GuidesCommon;
import guideme.PageAnchor;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;

public class EnergizedMEChestScreen extends AEBaseScreen<EnergizedMEChestMenu> {
    private static final ScreenStyle STYLE = loadStyle();
    private static final ResourceLocation GUIDE_ID = ResourceLocation.fromNamespaceAndPath("ae2", "guide");
    private static final ResourceLocation GUIDE_PAGE =
            NeoECOPrototype.id("neoecoprototype_intro/energized_me_chest.md");
    private static final ResourceLocation GUI_TEXTURE =
            NeoECOPrototype.id("textures/gui/energized_me_chest.png");

    private final Slot cellSlot;
    private final ToolbarButton guideButton;
    private final ToolbarButton cellWorkbenchButton;

    public EnergizedMEChestScreen(EnergizedMEChestMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, STYLE);
        widgets.addOpenPriorityButton();
        cellSlot = menu.getSlots(SlotSemantics.STORAGE_CELL).get(0);
        guideButton = new ToolbarButton(Icon.HELP, 0, 25, 0, 4, 18,
                pressed -> GuidesCommon.openGuide(getPlayer(), GUIDE_ID, PageAnchor.page(GUIDE_PAGE)),
                Component.translatable("gui.neoecoprototype.energized_me_chest.guide"));
        cellWorkbenchButton = new ToolbarButton(Icon.COG, 25, 27, 32, 1, 22,
                pressed -> menu.sendCellWorkbenchRequest(),
                Component.translatable("gui.neoecoprototype.energized_me_chest.configure"));
    }

    @Override
    protected boolean shouldAddToolbar() {
        return false;
    }

    @Override
    protected void init() {
        super.init();
        guideButton.setPosition(leftPos - 19, topPos);
        cellWorkbenchButton.setPosition(leftPos - 19, topPos + 25);
        addRenderableWidget(guideButton);
        addRenderableWidget(cellWorkbenchButton);
    }

    @Override
    protected PageAnchor getHelpTopic() {
        return null;
    }

    private static ScreenStyle loadStyle() {
        try (InputStream input = EnergizedMEChestScreen.class.getResourceAsStream(
                "/assets/neoecoprototype/screens/energized_me_chest.json")) {
            ScreenStyle style = ScreenStyle.GSON.fromJson(
                    new InputStreamReader(input, StandardCharsets.UTF_8), ScreenStyle.class);
            style.validate();
            Text dialogTitle = style.getText().computeIfAbsent("dialog_title", ignored -> new Text());
            Position titlePosition = new Position();
            titlePosition.setLeft(14);
            titlePosition.setTop(6);
            dialogTitle.setPosition(titlePosition);
            dialogTitle.setAlign(TextAlignment.LEFT);
            return style;
        } catch (IOException e) {
            throw new IllegalStateException("Failed to load the Energized ME Chest screen style", e);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        if (!title.getString().isEmpty()) {
            setTextContent("dialog_title", title);
        }
        cellWorkbenchButton.setVisibility(true);
        cellWorkbenchButton.active = ECOStorageCellSettings.canConfigure(cellSlot.getItem());
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 1 && !hasShiftDown() && cellSlot.hasItem()
                && ECOStorageCellSettings.canConfigure(cellSlot.getItem())
                && isHovering(cellSlot, mouseX, mouseY)) {
            menu.sendCellWorkbenchRequest();
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private final class ToolbarButton extends IconButton {
        private static final int WIDTH = 22;
        private static final int TEXTURE_WIDTH = 25;
        private static final int ICON_HEIGHT = 20;

        private final Icon icon;
        private final int slotY;
        private final int slotHeight;
        private final int hoverY;
        private final int hoverOffsetY;
        private final int hoverWidth;

        private ToolbarButton(Icon icon, int slotY, int slotHeight, int hoverY, int hoverOffsetY, int hoverWidth,
                Button.OnPress onPress, Component message) {
            super(onPress);
            this.icon = icon;
            this.slotY = slotY;
            this.slotHeight = slotHeight;
            this.hoverY = hoverY;
            this.hoverOffsetY = hoverOffsetY;
            this.hoverWidth = hoverWidth;
            setWidth(WIDTH);
            setHeight(slotHeight);
            setMessage(message);
        }

        @Override
        protected Icon getIcon() {
            return icon;
        }

        @Override
        public void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            if (!visible) {
                return;
            }
            graphics.blit(GUI_TEXTURE, getX(), getY(), 0, slotY, TEXTURE_WIDTH, slotHeight, 256, 256);
            if (isHovered()) {
                graphics.blit(GUI_TEXTURE, getX() + 3, getY() + hoverOffsetY, 208, hoverY,
                        hoverWidth, ICON_HEIGHT, 256, 256);
            }
        }
    }
}

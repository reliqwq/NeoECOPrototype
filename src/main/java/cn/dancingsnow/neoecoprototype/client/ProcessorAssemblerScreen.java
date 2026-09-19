package cn.dancingsnow.neoecoprototype.client;

import appeng.client.gui.implementations.UpgradeableScreen;
import appeng.client.gui.style.ScreenStyle;
import appeng.client.gui.widgets.ProgressBar;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.menu.ProcessorAssemblerMenu;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;

import java.io.Reader;

/**
 * AE2's molecular assembler layout without the pattern slot. The style is our own copy because AE2
 * resolves every style path against its own namespace and keeps the dialog-title override private.
 */
public class ProcessorAssemblerScreen extends UpgradeableScreen<ProcessorAssemblerMenu> {
    private static final ResourceLocation STYLE = NeoECOPrototype.id("screens/processor_assembler.json");

    private final ProcessorAssemblerMenu assemblerMenu;
    private final ProgressBar progressBar;

    public ProcessorAssemblerScreen(ProcessorAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, loadStyle());
        assemblerMenu = menu;
        progressBar = new ProgressBar(menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", progressBar);
    }

    /**
     * AE2's StyleManager resolves every path against its own namespace and keeps the screen's text
     * override hook private, so the dialog title can only be changed with a style we ship ourselves.
     */
    private static ScreenStyle loadStyle() {
        try (Reader reader = Minecraft.getInstance().getResourceManager().openAsReader(STYLE)) {
            ScreenStyle loaded = ScreenStyle.GSON.fromJson(reader, ScreenStyle.class);
            loaded.validate();
            return loaded;
        } catch (Exception e) {
            throw new IllegalStateException("Could not load assembler screen style " + STYLE, e);
        }
    }

    @Override
    protected void updateBeforeRender() {
        super.updateBeforeRender();
        int max = assemblerMenu.getMaxProgress();
        progressBar.setFullMsg(Component.literal(
                max > 0 ? assemblerMenu.getCurrentProgress() * 100 / max + "%" : "0%"));
    }
}

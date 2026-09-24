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
    /**
     * Loaded by {@link #loadStyle()} below with <b>our</b> namespace, so the file must stay at
     * {@code assets/neoecoprototype/screens/processor_assembler.json}. Do not move it into
     * {@code assets/ae2/screens/}: that folder only works for screens registered through AE2's
     * {@code InitScreens}, which resolves paths against AE2's namespace. Nothing references the file
     * statically, so it looks unused until opening the GUI throws and NeoForge drops the client.
     */
    private static final ResourceLocation STYLE = NeoECOPrototype.id("screens/processor_assembler.json");

    private final ProcessorAssemblerMenu assemblerMenu;
    private final ProgressBar progressBar;

    /**
     * Parsed once and shared, the way AE2's StyleManager caches its style documents: a style is
     * immutable data, and re-reading the JSON on every open was what turned a missing file into a
     * disconnect. {@link NeoECOPrototypeClient} drops this when the resource manager reloads, so a
     * resource pack can still override the style.
     */
    private static ScreenStyle cachedStyle;

    public ProcessorAssemblerScreen(ProcessorAssemblerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title, sharedStyle());
        assemblerMenu = menu;
        progressBar = new ProgressBar(menu, style.getImage("progressBar"), ProgressBar.Direction.VERTICAL);
        widgets.add("progressBar", progressBar);
    }

    private static ScreenStyle sharedStyle() {
        if (cachedStyle == null) {
            cachedStyle = loadStyle();
        }
        return cachedStyle;
    }

    static void forgetStyle() {
        cachedStyle = null;
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

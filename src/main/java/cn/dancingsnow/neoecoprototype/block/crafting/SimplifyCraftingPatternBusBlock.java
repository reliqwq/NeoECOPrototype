package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOCraftingPatternBus;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/** Owns its GUI header, which eco asks the block for since 21.2.1. */
public class SimplifyCraftingPatternBusBlock extends ECOCraftingPatternBus implements GuiTitleProvider {
    public SimplifyCraftingPatternBusBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getGuiTitle(BlockState state) {
        return getName();
    }
}

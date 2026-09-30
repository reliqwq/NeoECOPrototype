package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * F1 subsystem communication interface: the block the player opens, with eco's interface UI. Same
 * cluster behaviour as {@code simplify_crafting_interface} plus the panel, which is what the
 * {@code ae2:terminal} in its recipe buys.
 */
public class SimplifyCraftingNetworkInterfaceBlock extends ECOMachineInterface<NECraftingCluster>
        implements GuiTitleProvider {
    public SimplifyCraftingNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getGuiTitle(BlockState state) {
        return getName();
    }
}

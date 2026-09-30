package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * C1 subsystem communication interface: the block the player opens. Same cluster behaviour as
 * {@code simplify_computation_interface} plus eco's interface UI, which is why it costs an
 * {@code ae2:terminal} on top of that block.
 */
public class SimplifyComputationNetworkInterfaceBlock extends ECOMachineInterface<NEComputationCluster>
        implements GuiTitleProvider {
    public SimplifyComputationNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getGuiTitle(BlockState state) {
        return getName();
    }
}

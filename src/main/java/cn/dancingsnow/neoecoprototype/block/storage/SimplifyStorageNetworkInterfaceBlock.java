package cn.dancingsnow.neoecoprototype.block.storage;

import cn.dancingsnow.neoecoae.gui.GuiTitleProvider;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.state.BlockState;

/**
 * The L1 storage subsystem communication interface: the block the player opens. Same cluster behaviour
 * as {@code simplify_storage_interface} plus eco's interface UI, which is what the {@code ae2:terminal}
 * in its recipe buys -- and the one {@code supportsStorageInterfaceUi()} answers true for.
 */
public class SimplifyStorageNetworkInterfaceBlock extends SimplifyStorageInterfaceBlock implements GuiTitleProvider {
    public SimplifyStorageNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    public Component getGuiTitle(BlockState state) {
        return getName();
    }
}

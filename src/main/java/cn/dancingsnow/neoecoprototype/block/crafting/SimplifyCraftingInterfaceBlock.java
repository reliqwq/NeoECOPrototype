package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * F1 subsystem interface: the plain endpoint that joins the cluster but opens nothing. The GUI lives on
 * {@code simplify_crafting_network_interface}, which is what the lang files call 通讯接口 and what
 * adding an {@code ae2:terminal} to this block produces. Same rule as C1 and L1.
 */
public class SimplifyCraftingInterfaceBlock extends ECOMachineInterface<NECraftingCluster> {
    public SimplifyCraftingInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}

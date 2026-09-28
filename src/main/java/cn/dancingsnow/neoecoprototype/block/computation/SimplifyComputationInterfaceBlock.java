package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

/**
 * C1 subsystem interface: the plain endpoint that joins the cluster but opens nothing. The GUI lives on
 * {@code simplify_computation_network_interface}, which is what the lang files call 通讯接口 and what
 * adding an {@code ae2:terminal} to this block produces.
 */
public class SimplifyComputationInterfaceBlock extends ECOMachineInterface<NEComputationCluster> {
    public SimplifyComputationInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player,
                                               BlockHitResult hit) {
        return InteractionResult.PASS;
    }
}

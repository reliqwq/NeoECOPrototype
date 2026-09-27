package cn.dancingsnow.neoecoprototype.block.computation;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NEComputationCluster;

/**
 * Keeps drawing once the structure forms so its blockstate can carry a formed model of its own; eco
 * hides interface blocks, which leaves the artist no JSON hook for the formed look. The formed model
 * pushes every face out by 1/16 because the shell next to it is invisible and does not occlude.
 */
public class SimplifyComputationInterfaceBlock extends ECOMachineInterface<NEComputationCluster> {
    public SimplifyComputationInterfaceBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected boolean hideWhenFormed() {
        return false;
    }
}

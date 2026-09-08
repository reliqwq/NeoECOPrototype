package cn.dancingsnow.neoecoprototype.block.storage;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;

/**
 * Interface block of the L1 storage subsystem. Reuses eco's
 * {@link ECOMachineInterface} block logic (becomes invisible once formed and
 * exposes the cluster grid node to outside ME cables).
 */
public class SimplifyStorageInterfaceBlock extends ECOMachineInterface<SimplifyStorageCluster> {

    public SimplifyStorageInterfaceBlock(Properties properties) {
        super(properties);
    }

}

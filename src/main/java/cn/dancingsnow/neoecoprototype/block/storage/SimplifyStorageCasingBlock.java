package cn.dancingsnow.neoecoprototype.block.storage;

import cn.dancingsnow.neoecoae.blocks.ECOMachineCasing;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;

/** Casing block of the L1 storage subsystem (reuses eco's casing rendering logic). */
public class SimplifyStorageCasingBlock extends ECOMachineCasing<SimplifyStorageCluster> {

    public SimplifyStorageCasingBlock(Properties properties) {
        super(properties);
    }
}

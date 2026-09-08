package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;

/** Network-connected variant that keeps the same crafting interface UI. */
public class SimplifyCraftingNetworkInterfaceBlock extends ECOMachineInterface<NECraftingCluster> {
    public SimplifyCraftingNetworkInterfaceBlock(Properties properties) {
        super(properties);
    }
}

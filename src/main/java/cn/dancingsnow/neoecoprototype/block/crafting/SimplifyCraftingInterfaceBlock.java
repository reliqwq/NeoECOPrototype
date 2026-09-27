package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.ECOMachineInterface;
import cn.dancingsnow.neoecoae.multiblock.cluster.NECraftingCluster;

/**
 * F1 subsystem communication interface: the full interface block, opened by hand and by the
 * one-click builder, with eco's interface GUI. Matches how C1 spells the pair -- the block without
 * "network" in its name is the one the player uses.
 */
public class SimplifyCraftingInterfaceBlock extends ECOMachineInterface<NECraftingCluster> {
    public SimplifyCraftingInterfaceBlock(Properties properties) {
        super(properties);
    }
}

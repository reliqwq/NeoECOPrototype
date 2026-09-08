package cn.dancingsnow.neoecoprototype.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * Casing of the L1 storage subsystem. Reuses eco's {@link ECOMachineCasingBlockEntity}
 * (which is designed for any cluster type) with the cluster calculator of this addon.
 */
public class SimplifyStorageCasingBlockEntity extends ECOMachineCasingBlockEntity<SimplifyStorageCluster> {

    public SimplifyStorageCasingBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }
}

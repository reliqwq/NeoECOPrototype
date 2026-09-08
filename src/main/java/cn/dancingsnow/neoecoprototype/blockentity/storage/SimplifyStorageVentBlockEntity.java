package cn.dancingsnow.neoecoprototype.blockentity.storage;

import cn.dancingsnow.neoecoae.blocks.entity.NEBlockEntity;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import cn.dancingsnow.neoecoprototype.multiblock.calculator.SimplifyStorageClusterCalculator;
import cn.dancingsnow.neoecoprototype.multiblock.cluster.SimplifyStorageCluster;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** L1 storage subsystem vent; keeps the cluster cool for flavour & idle power cost. */
public class SimplifyStorageVentBlockEntity extends NEBlockEntity<SimplifyStorageCluster, SimplifyStorageVentBlockEntity> {

    public SimplifyStorageVentBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState, SimplifyStorageClusterCalculator::new);
    }

    @Override
    public void onReady() {
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.highIdleComponentPower());
    }
}

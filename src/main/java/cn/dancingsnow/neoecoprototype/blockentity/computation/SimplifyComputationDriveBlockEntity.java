package cn.dancingsnow.neoecoprototype.blockentity.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationTransmitterBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

public class SimplifyComputationDriveBlockEntity extends ECOComputationDriveBlockEntity {
    public SimplifyComputationDriveBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    public IECOTier getTier() {
        return SimplifyTier.L1;
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        if (level != null) {
            boolean lower = !(level.getBlockState(worldPosition.below()).getBlock()
                    instanceof SimplifyComputationTransmitterBlock);
            setLowerDrive(lower);
        }
        // ECO's inherited @DescSynced field targets eco_tier, which cannot encode addon enums.
        setTier(null);
    }
}

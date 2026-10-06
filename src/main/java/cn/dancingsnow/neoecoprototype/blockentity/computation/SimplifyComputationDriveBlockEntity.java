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
            // Deliberately not upstream's shape: eco only ever writes true, from the one branch of
            // addBlockEntity() where the block below is not a transmitter, so a drive whose neighbour
            // changes after formation keeps a stale flag. Writing the computed value every update keeps
            // the two states honest.
            boolean lower = !(level.getBlockState(worldPosition.below()).getBlock()
                    instanceof SimplifyComputationTransmitterBlock);
            setLowerDrive(lower);
        }
        // The inherited LDLib2 @DescSynced field is typed to eco's own tier and marked @Nullable - eco
        // itself writes null here whenever the drive has no cluster - so leaving it null is upstream's
        // own shape, not a value we invented. getTier() above is the only reader of the field.
        setTier(null);
    }
}

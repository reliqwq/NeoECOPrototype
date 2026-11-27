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
            boolean aboveTransmitter = level.getBlockState(worldPosition.below()).getBlock()
                    instanceof SimplifyComputationTransmitterBlock;
            setLowerDrive(!aboveTransmitter);
            placeInPanelRow(aboveTransmitter);
        }
        // The inherited LDLib2 @DescSynced field is typed to eco's own tier and marked @Nullable - eco
        // itself writes null here whenever the drive has no cluster - so leaving it null is upstream's
        // own shape, not a value we invented. getTier() above is the only reader of the field.
        setTier(null);
    }

    /**
     * Puts this drive in the panel row its position says it belongs to.
     *
     * <p>eco decides that row in {@code NEComputationCluster.addBlockEntity} by asking whether the block
     * below the drive {@code is(NEBlocks.COMPUTATION_TRANSMITTER)} - its own transmitter. Ours is a
     * different block and that test compares block identity, so it never passes: every L1 drive lands in
     * the lower list and the upper list stays empty forever. The row is ours to decide, because our shape
     * does use it: the y=2 drive column sits on the y=1 transmitter column.</p>
     *
     * <p>{@code getUpperDrives()} and {@code getLowerDrives()} hand out the live lists (their bytecode is
     * a getfield and an areturn) and {@code NECluster} has no removal path at all, so this is one move
     * rather than a copy, and re-running it on every later update cannot stack duplicates.</p>
     */
    private void placeInPanelRow(boolean aboveTransmitter) {
        var cluster = getCluster();
        if (cluster == null) {
            return;
        }
        var topRow = cluster.getUpperDrives();
        var bottomRow = cluster.getLowerDrives();
        if (aboveTransmitter) {
            bottomRow.remove(this);
            if (!topRow.contains(this)) {
                topRow.add(this);
            }
        } else {
            topRow.remove(this);
            if (!bottomRow.contains(this)) {
                bottomRow.add(this);
            }
        }
    }
}

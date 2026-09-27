package cn.dancingsnow.neoecoprototype.blockentity.computation;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationSystemBlockEntity;
import cn.dancingsnow.neoecoprototype.block.computation.SimplifyComputationSystemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * C1 computation host. Publishes what the finished machine holds onto its own block state so the
 * blockstate file can pick a formed model per combination.
 *
 * <p>The write is deferred to the next server tick. Both the calculator and eco's own
 * {@code setFormed -> updateState} run inside AE2's cluster recalculation, which holds a global
 * "modification in progress" latch; a block change made anywhere on that stack re-enters the latch and
 * wedges every multiblock for the rest of the session -- measured, not theorised.
 */
public class SimplifyComputationSystemBlockEntity extends ECOComputationSystemBlockEntity {
    private boolean communicationInterface;
    private boolean energizedThreadingCore;
    private boolean energizedParallelCore;
    private boolean appearancePending;

    public SimplifyComputationSystemBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                BlockState state, IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** Records the machine's shape for the next {@link #updateState(boolean)}. */
    public void setPublishedShape(boolean communicationInterface, boolean energizedThreadingCore,
                                  boolean energizedParallelCore) {
        if (this.communicationInterface == communicationInterface
                && this.energizedThreadingCore == energizedThreadingCore
                && this.energizedParallelCore == energizedParallelCore) {
            return;
        }
        this.communicationInterface = communicationInterface;
        this.energizedThreadingCore = energizedThreadingCore;
        this.energizedParallelCore = energizedParallelCore;
        this.appearancePending = true;
        setChanged();
    }

    @Override
    public void tick(net.minecraft.world.level.Level level, BlockPos pos, BlockState state) {
        super.tick(level, pos, state);
        if (appearancePending && !level.isClientSide) {
            appearancePending = false;
            publishAppearance(level, pos);
        }
    }

    @Override
    public void updateState(boolean updateExposed) {
        super.updateState(updateExposed);
        appearancePending = true;
    }

    private void publishAppearance(net.minecraft.world.level.Level reader, BlockPos pos) {
        if (isRemoved() || level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(SimplifyComputationSystemBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        boolean formed = isFormed();
        BlockState next = state
                .setValue(SimplifyComputationSystemBlock.COMMUNICATION_INTERFACE,
                        formed && communicationInterface)
                .setValue(SimplifyComputationSystemBlock.ENERGIZED_THREADING_CORE,
                        formed && energizedThreadingCore)
                .setValue(SimplifyComputationSystemBlock.ENERGIZED_PARALLEL_CORE,
                        formed && energizedParallelCore);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }
}

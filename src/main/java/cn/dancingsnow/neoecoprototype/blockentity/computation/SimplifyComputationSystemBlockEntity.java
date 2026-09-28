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
    private boolean appearancePending;

    public SimplifyComputationSystemBlockEntity(BlockEntityType<?> type, BlockPos pos,
                                                BlockState state, IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** Records which interface the machine turned out to hold, for the next {@link #updateState(boolean)}. */
    public void setPublishedCommunicationInterface(boolean communicationInterface) {
        if (this.communicationInterface == communicationInterface) {
            return;
        }
        this.communicationInterface = communicationInterface;
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
        var next = state.setValue(SimplifyComputationSystemBlock.COMMUNICATION_INTERFACE,
                isFormed() && communicationInterface);
        // eco lights these two from a cell beside the host and reads them back to decide whether the
        // cluster registers its CPUs through a switch frequency. An L1 machine has no switch block and so
        // no frequency, and leaving them set sent our CPUs into a logical network that does not exist -
        // the machine looked connected but never appeared in the network's CPU list. Measured: both came
        // up true with plain casings in both candidate cells.
        next = pinSwitchBit(next, cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.NETWORK_SWITCH);
        next = pinSwitchBit(next,
                cn.dancingsnow.neoecoae.blocks.computation.ECOComputationSystem.HIGH_ENERGY_NETWORK_SWITCH);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }

    private static BlockState pinSwitchBit(BlockState state,
                                           net.minecraft.world.level.block.state.properties.BooleanProperty bit) {
        return state.hasProperty(bit) && state.getValue(bit) ? state.setValue(bit, false) : state;
    }
}

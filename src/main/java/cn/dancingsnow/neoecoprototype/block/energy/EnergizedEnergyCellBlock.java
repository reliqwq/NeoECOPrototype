package cn.dancingsnow.neoecoprototype.block.energy;

import appeng.block.AEBaseBlock;
import appeng.block.AEBaseEntityBlock;
import cn.dancingsnow.neoecoprototype.blockentity.energy.EnergizedEnergyCellBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jetbrains.annotations.Nullable;

/**
 * A standalone battery. It is deliberately not a multiblock member: AE2's grid collects every
 * {@code IAEPowerStorage} node into the network's total, so placing one of these adds its buffer without
 * any {@code MultiBlockDefinition} having to know the block exists.
 */
public class EnergizedEnergyCellBlock extends AEBaseEntityBlock<EnergizedEnergyCellBlockEntity> {

    /**
     * TedXenon draws five faces per tier - the charge bar fills in steps of a quarter, and the fifth rung is
     * the only one carrying the bright blue (102,190,255). Same five-bucket property AE2's own
     * {@code EnergyCellBlock.ENERGY_STORAGE} uses, and the same reason to leave the default state to the
     * definition: an {@code IntegerProperty} starting at 0 means the derived default already shows the empty
     * face, so nothing here has to register one.
     */
    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);

    private final double capacity;

    public EnergizedEnergyCellBlock(double capacity) {
        // a fresh Properties per call (AEBaseBlock.defaultProps starts from Properties.of()), so chaining here
        // touches our block only - metalProps() itself is AE2's and stays as it is.
        super(AEBaseBlock.metalProps().requiresCorrectToolForDrops());
        this.capacity = capacity;
    }

    /** AE, not FE: this is the number the grid's energy service adds up. */
    public double getCapacity() {
        return capacity;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    /**
     * The charge bar is server state pushed into the block state, so it needs a server tick and nothing on
     * the client. AE2's grid gives this battery no {@code IGridTickable} pass, and its
     * {@code StoredEnergyAmount} takes its event emitter at construction, so a tick here is the only place
     * an amount change can be noticed without reaching into AE2's energy service.
     */
    @Nullable
    @SuppressWarnings("unchecked")
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                 BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModRegistration.ENERGIZED_ENERGY_CELL_BE.get()) {
            return null;
        }
        BlockEntityTicker<EnergizedEnergyCellBlockEntity> sweep = EnergizedEnergyCellBlockEntity::tick;
        return (BlockEntityTicker<T>) (BlockEntityTicker<?>) sweep;
    }

}

package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype.block.crafting.SimplifyCraftingSystemBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/**
 * F1 crafting host. Publishes which of the two interface blocks the finished machine holds onto its
 * own block state so the blockstate file can pick a formed model per combination.
 *
 * <p>eco's own {@code updateState} already writes {@code mirrored} with a block change, and both that
 * and this run inside AE2's cluster recalculation, which holds a global "modification in progress"
 * latch. A second write on the same stack is what wedged the C1 host, so the value is recorded here
 * and applied on the next server tick instead.
 */
public class SimplifyCraftingSystemBlockEntity extends ECOCraftingSystemBlockEntity {
    /**
     * The host rolls for the face once, the first time it forms, and keeps the answer: geometry checks
     * re-run on neighbour changes, chunk loads and rebuilds, so a per-check roll would flip the look
     * back and forth instead of being rare. One in sixteen.
     */
    private static final int MIND_CHANCE_ONE_IN = 16;
    private static final String HAS_MIND_TAG = "neoecoprototype:has_mind";
    private static final String MIND_ROLLED_TAG = "neoecoprototype:mind_rolled";

    private boolean communicationInterface;
    private boolean appearancePending;
    private boolean hasMind;
    private boolean mindRolled;

    public SimplifyCraftingSystemBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state,
                                             IECOTier tier) {
        super(type, pos, state, tier);
    }

    /** Records what the interface cell turned out to hold, for the next {@link #tick}. */
    public void setCommunicationInterface(boolean communicationInterface) {
        if (this.communicationInterface == communicationInterface) {
            return;
        }
        this.communicationInterface = communicationInterface;
        this.appearancePending = true;
    }

    @Override
    public void tick(Level level, BlockPos pos, BlockState state) {
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

    private void publishAppearance(Level reader, BlockPos pos) {
        if (isRemoved() || level == null || level.isClientSide) {
            return;
        }
        BlockState state = level.getBlockState(worldPosition);
        if (!state.hasProperty(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE)) {
            return;
        }
        if (isFormed() && !mindRolled) {
            mindRolled = true;
            hasMind = level.random.nextInt(MIND_CHANCE_ONE_IN) == 0;
            setChanged();
        }
        BlockState next = state.setValue(SimplifyCraftingSystemBlock.COMMUNICATION_INTERFACE,
                        isFormed() && communicationInterface)
                .setValue(SimplifyCraftingSystemBlock.HAS_MIND, isFormed() && hasMind);
        if (next != state) {
            level.setBlock(worldPosition, next, Block.UPDATE_CLIENTS);
        }
    }

    /** Whether this host grew a face. Read by the blockstate write above and by the game tests. */
    public boolean hasMind() {
        return hasMind;
    }

    /** Whether the once-per-host roll has already happened. */
    public boolean mindRolled() {
        return mindRolled;
    }

    @Override
    public void saveAdditional(net.minecraft.nbt.CompoundTag tag,
                               net.minecraft.core.HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean(MIND_ROLLED_TAG, mindRolled);
        tag.putBoolean(HAS_MIND_TAG, hasMind);
    }

    @Override
    public void loadTag(net.minecraft.nbt.CompoundTag tag,
                        net.minecraft.core.HolderLookup.Provider registries) {
        // AE2 declares loadAdditional final and routes it here, so loadTag is the hook for extra fields.
        super.loadTag(tag, registries);
        mindRolled = tag.getBoolean(MIND_ROLLED_TAG);
        hasMind = tag.getBoolean(HAS_MIND_TAG);
    }
}

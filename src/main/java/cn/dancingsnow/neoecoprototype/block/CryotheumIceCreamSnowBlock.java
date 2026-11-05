package cn.dancingsnow.neoecoprototype.block;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.PowderSnowBlock;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * The dessert as a block: powder snow's behaviour, and the bucket that goes with it.
 *
 * <p>Everything about standing in it - sinking, the slowed fall, putting out a fire, freezing whoever
 * stays - is inherited from {@code PowderSnowBlock} untouched, because that behaviour is long and full of
 * edge cases no addon should re-derive. What has to be overridden is the one pair of methods that decide
 * <i>which bucket</i> comes out of it: vanilla's {@code pickupBlock} hard-codes
 * {@code Items.POWDER_SNOW_BUCKET}, so a plain bucket drawn against this block would come back holding
 * snow that was never there, and the dessert could be emptied into the world but never taken back out.</p>
 *
 * <p>The other difference from vanilla is deliberate and it is the one a player would call a bug
 * otherwise: this block drops itself when broken. Powder snow has no loot table at all because a bucket is
 * its only collection method; a pillar of this has a bucket too, but a pickaxe should not silently delete
 * what it just broke.</p>
 */
public class CryotheumIceCreamSnowBlock extends PowderSnowBlock {

    public CryotheumIceCreamSnowBlock(Properties properties) {
        super(properties);
    }

    /**
     * The generic bucket path: {@code BucketItem#use} asks whatever block it hit, if it is a
     * {@code BucketPickup}, for a stack, and keeps the result when the answer is not empty. So the whole
     * of picking this up is these two methods - the removal of the block is vanilla's, one level above.
     */
    @Override
    public ItemStack pickupBlock(Player player, LevelAccessor level, BlockPos pos, BlockState state) {
        if (!level.isClientSide()) {
            level.setBlock(pos, net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(),
                    Block.UPDATE_ALL);
        }
        return new ItemStack(ModRegistration.CRYOTHEUM_ICE_CREAM_BUCKET.get());
    }

    @Override
    public Optional<SoundEvent> getPickupSound() {
        return Optional.of(SoundEvents.BUCKET_FILL_POWDER_SNOW);
    }
}

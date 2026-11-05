package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.block.CryotheumIceCreamSnowBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.function.Supplier;

/**
 * The dessert's bucket: a real fluid container that puts down a solid cell.
 *
 * <p>It is deliberately not a {@code SolidBucketItem}, which is what this item shipped as until the guard
 * {@code everyBucketItemWeRegisterCarriesItsFluid} went red on it. That class is a {@code BlockItem}, and
 * NeoForge hangs the item fluid handler on {@code getClass() == BucketItem.class} exactly
 * (CapabilityHooks.registerFallbackVanillaProviders, 21.1.251) - so the bucket our fluid's own properties
 * named as its container came back from a 1000 mB draw carrying nothing at all. The tank gave the fluid up
 * and no item received it.</p>
 *
 * <p>Two halves fix it, and the second one is not optional: this class carries a real {@code content} fluid,
 * and {@code NeoECOPrototype.registerCapabilities} registers {@code FluidBucketWrapper} for it, because being
 * a subclass is precisely what NeoForge's own loop skips. The class change alone measured red on the suite -
 * that reading is what the guard exists to keep from happening again.</p>
 *
 * <p>Being a real fluid container is also the only way this coolant reaches a bath: the input hatch and every
 * pump ask that handler. What powder snow's bucket did for the hand - place against the clicked face instead
 * of pouring - is re-done on the two paths that can empty a bucket, so one cell is one bucket is 1000 mB, in
 * both directions, with no bookkeeping of our own.</p>
 */
public class CryotheumIceCreamBucketItem extends BucketItem {

    private final Supplier<CryotheumIceCreamSnowBlock> placeable;

    public CryotheumIceCreamBucketItem(Supplier<CryotheumIceCreamSnowBlock> placeable, Fluid content,
                                       Properties properties) {
        super(content, properties);
        this.placeable = placeable;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos().relative(context.getClickedFace());
        Player player = context.getPlayer();
        ItemStack stack = context.getItemInHand();
        if (!level.isEmptyBlock(pos) || !level.mayInteract(player, pos)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.sidedSuccess(true);
        }
        if (player != null && !player.mayUseItemAt(pos, context.getClickedFace(), stack)) {
            return InteractionResult.PASS;
        }
        if (!placeCell(level, pos, player)) {
            return InteractionResult.PASS;
        }
        if (player != null && !player.hasInfiniteMaterials()) {
            stack.shrink(1);
            var container = new ItemStack(Items.BUCKET);
            if (stack.isEmpty()) {
                player.setItemInHand(context.getHand(), container);
            } else if (!player.getInventory().add(container)) {
                player.drop(container, false);
            }
        }
        return InteractionResult.sidedSuccess(false);
    }

    /**
     * A dispenser - or any code that empties a bucket through the vanilla path - gets the cell too rather
     * than a puddle, so the fluid can never be released as a liquid by accident.
     */
    @Override
    public boolean emptyContents(Player player, Level level, BlockPos pos, @Nullable BlockHitResult result,
                                 @Nullable ItemStack container) {
        return level.isEmptyBlock(pos) && placeCell(level, pos, player);
    }

    private boolean placeCell(Level level, BlockPos pos, @Nullable Player player) {
        if (!level.setBlock(pos, placeable.get().defaultBlockState(), Block.UPDATE_ALL)) {
            return false;
        }
        level.playSound(player, pos, SoundEvents.POWDER_SNOW_PLACE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }
}

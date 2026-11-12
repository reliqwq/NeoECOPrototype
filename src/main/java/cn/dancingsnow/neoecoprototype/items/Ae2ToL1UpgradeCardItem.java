package cn.dancingsnow.neoecoprototype.items;

import appeng.api.parts.IPart;
import appeng.api.parts.IPartItem;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.blockentity.networking.CableBusBlockEntity;
import appeng.parts.AEBasePart;
import appeng.api.parts.SelectedPart;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.function.Supplier;

public final class Ae2ToL1UpgradeCardItem extends Item {
    private final Class<? extends BlockEntity> sourceBlockEntity;
    private final Class<? extends IPart> sourcePart;
    private final Supplier<? extends Block> targetBlock;
    private final Supplier<? extends BlockEntityType<?>> targetBlockEntity;
    private final Supplier<? extends IPartItem<?>> targetPart;

    public Ae2ToL1UpgradeCardItem(Properties properties, Class<? extends BlockEntity> sourceBlockEntity,
                                  Class<? extends IPart> sourcePart, Supplier<? extends Block> targetBlock,
                                  Supplier<? extends BlockEntityType<?>> targetBlockEntity,
                                  Supplier<? extends IPartItem<?>> targetPart) {
        super(properties.stacksTo(1));
        this.sourceBlockEntity = sourceBlockEntity;
        this.sourcePart = sourcePart;
        this.targetBlock = targetBlock;
        this.targetBlockEntity = targetBlockEntity;
        this.targetPart = targetPart;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        BlockPos pos = context.getClickedPos();
        BlockEntity clicked = level.getBlockEntity(pos);
        if (clicked != null && clicked.getClass() == this.sourceBlockEntity && this.targetBlock != null) {
            return upgradeBlock(context, clicked);
        }
        if (clicked instanceof CableBusBlockEntity cableBus && this.sourcePart != null
                && this.targetPart != null) {
            return upgradePart(context, cableBus, pos);
        }
        return InteractionResult.PASS;
    }

    private InteractionResult upgradeBlock(UseOnContext context, BlockEntity source) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState oldState = level.getBlockState(pos);
        BlockState newState = this.targetBlock.get().getStateForPlacement(new BlockPlaceContext(context));
        if (newState == null) {
            return InteractionResult.PASS;
        }
        for (Map.Entry<Property<?>, Comparable<?>> property : oldState.getValues().entrySet()) {
            newState = copyProperty(newState, property.getKey(), property.getValue());
        }
        CompoundTag data = source.saveWithFullMetadata(level.registryAccess());
        if (!level.setBlock(pos, newState, Block.UPDATE_ALL)) {
            return InteractionResult.PASS;
        }
        BlockEntity target = level.getBlockEntity(pos);
        if (target == null || target.getType() != this.targetBlockEntity.get()) {
            throw new IllegalStateException("L1 upgrade card did not create its target block entity at " + pos);
        }
        target.loadWithComponents(data, level.registryAccess());
        target.setChanged();
        if (target instanceof AEBaseBlockEntity aeTarget) {
            aeTarget.markForUpdate();
        }
        consume(context.getPlayer(), context);
        return InteractionResult.CONSUME;
    }

    private InteractionResult upgradePart(UseOnContext context, CableBusBlockEntity cableBus, BlockPos pos) {
        Vec3 hit = context.getClickLocation();
        Vec3 localHit = new Vec3(hit.x - pos.getX(), hit.y - pos.getY(), hit.z - pos.getZ());
        SelectedPart selected = cableBus.getCableBus().selectPartLocal(localHit);
        IPart part = selected.part;
        if (part == null || part.getClass() != this.sourcePart) {
            return InteractionResult.PASS;
        }
        Direction side = ((AEBasePart) part).getSide();
        CompoundTag data = new CompoundTag();
        part.writeToNBT(data, context.getLevel().registryAccess());
        IPart replaced = cableBus.replacePart(this.targetPart.get(), side, context.getPlayer(), context.getHand());
        if (replaced == null) {
            return InteractionResult.PASS;
        }
        // No addToWorld() here. CableBusContainer.replacePart is removePartWithoutUpdates + addPart, and
        // addPart already calls part.addToWorld() once the host is in the world - that is also the order
        // AE2's own tag-driven replacement uses (replacePart, then readFromNBT, then nothing). Asking twice
        // runs ManagedGridNode.create on a node that is already created, which throws
        // "The node has already been initialized" - on a placed bus at click time, and in a fresh
        // structure one tick later when CableBusBlockEntity.onReady adds every part itself.
        replaced.readFromNBT(data, context.getLevel().registryAccess());
        consume(context.getPlayer(), context);
        return InteractionResult.CONSUME;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static BlockState copyProperty(BlockState target, Property property, Comparable value) {
        return target.hasProperty(property) ? target.setValue(property, value) : target;
    }

    private static void consume(Player player, UseOnContext context) {
        if (player == null || !player.getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.decoration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.authlib.GameProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.component.ResolvableProfile;
import org.jetbrains.annotations.Nullable;

/**
 * Decorative plushie. The doll itself is drawn by the client renderer; this only carries whose skin
 * to wear, using the same {@link ResolvableProfile} plumbing vanilla player heads rely on, so skin
 * download and caching stay in {@code SkinManager}.
 */
public class FumoBlockEntity extends BlockEntity {
    private static final String OWNER_TAG = "fumo_owner";
    @Nullable
    private ResolvableProfile owner;

    public FumoBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistration.FUMO_BE.get(), pos, blockState);
    }

    @Nullable
    public GameProfile ownerProfile() {
        return owner == null ? null : owner.gameProfile();
    }

    public void setOwner(@Nullable ResolvableProfile profile) {
        if (owner == null ? profile == null : owner.equals(profile)) return;
        owner = profile;
        setChanged();
        if (level != null && !level.isClientSide) {
            level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
        }
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) {
        if (owner != null) builder.set(ModRegistration.FUMO_OWNER.get(), owner);
    }

    @Override
    protected void applyImplicitComponents(BlockEntity.DataComponentInput input) {
        owner = input.get(ModRegistration.FUMO_OWNER.get());
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (owner != null) {
            tag.put(OWNER_TAG, ResolvableProfile.CODEC.encodeStart(
                    registries.createSerializationContext(NbtOps.INSTANCE), owner).getOrThrow());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        owner = tag.contains(OWNER_TAG)
                ? ResolvableProfile.CODEC.parse(registries.createSerializationContext(NbtOps.INSTANCE),
                        tag.get(OWNER_TAG)).resultOrPartial(error -> NeoECOPrototype.LOGGER.error(
                                "Failed to read fumo owner at {}: {}", worldPosition, error)).orElse(null)
                : null;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}

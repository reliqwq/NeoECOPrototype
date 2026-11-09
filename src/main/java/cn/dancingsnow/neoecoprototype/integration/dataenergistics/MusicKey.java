package cn.dancingsnow.neoecoprototype.integration.dataenergistics;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.GenericStack;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * The one "music data" key: every disc, vanilla or modded, is worth the ticks it would play, and all
 * of them add up in this single key.
 */
public final class MusicKey extends AEKey {
    public static final ResourceLocation ID = NeoECOPrototype.id("music_data");
    public static final MusicKey INSTANCE = new MusicKey();
    public static final MapCodec<MusicKey> MAP_CODEC = MapCodec.unit(INSTANCE);
    public static final Codec<MusicKey> CODEC = MAP_CODEC.codec();

    private MusicKey() {
    }

    @Override
    public AEKeyType getType() {
        return MusicKeyType.TYPE;
    }

    @Override
    public AEKey dropSecondary() {
        return this;
    }

    @Override
    public CompoundTag toTag(HolderLookup.Provider provider) {
        return new CompoundTag();
    }

    @Override
    public Object getPrimaryKey() {
        return ID;
    }

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public void writeToPacket(RegistryFriendlyByteBuf buffer) {
    }

    @Override
    protected Component computeDisplayName() {
        return Component.translatable("key.neoecoprototype.music_data");
    }

    @Override
    public void addDrops(long amount, List<ItemStack> drops, Level level, BlockPos pos) {
        if (amount > 0) {
            drops.add(GenericStack.wrapInItemStack(this, amount));
        }
    }

    @Override
    public boolean hasComponents() {
        return false;
    }

    @Override
    public ItemStack wrapForDisplayOrFilter() {
        return GenericStack.wrapInItemStack(this, 1);
    }

    @Override
    public boolean equals(Object object) {
        return object instanceof MusicKey;
    }

    @Override
    public int hashCode() {
        return ID.hashCode();
    }

    @Override
    public String toString() {
        return "MusicKey{}";
    }
}

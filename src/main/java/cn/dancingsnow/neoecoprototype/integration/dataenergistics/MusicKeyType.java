package cn.dancingsnow.neoecoprototype.integration.dataenergistics;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.mojang.serialization.MapCodec;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

/**
 * Key type for music data. It is published through Data Energistics' plugin API, so it only exists
 * while that mod is installed and lands right after its three built-in types.
 */
public final class MusicKeyType extends AEKeyType {
    public static final ResourceLocation ID = NeoECOPrototype.id("music");
    public static final MusicKeyType TYPE = new MusicKeyType();

    private MusicKeyType() {
        super(ID, MusicKey.class, Component.translatable("key_type.neoecoprototype.music"));
    }

    @Override
    public MapCodec<? extends AEKey> codec() {
        return MusicKey.MAP_CODEC;
    }

    @Override
    public AEKey readFromPacket(RegistryFriendlyByteBuf buffer) {
        return MusicKey.INSTANCE;
    }

    @Override
    public int getAmountPerByte() {
        return 8;
    }
}

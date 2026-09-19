package cn.dancingsnow.neoecoprototype.blockentity.decoration;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/** Decorative fumo plushie; the doll itself is drawn by the client renderer. */
public class FumoBlockEntity extends BlockEntity {

    public FumoBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModRegistration.FUMO_BE.get(), pos, blockState);
    }
}

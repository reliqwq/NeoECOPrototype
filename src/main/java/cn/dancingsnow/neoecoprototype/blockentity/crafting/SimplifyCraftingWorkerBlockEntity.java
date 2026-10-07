package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingWorkerBlockEntity;
import cn.dancingsnow.neoecoprototype.api.SimplifyPowerProfile;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

/** eco writes 64.0 AE/t for this member; L1 runs at the addon's own rate. */
public final class SimplifyCraftingWorkerBlockEntity extends ECOCraftingWorkerBlockEntity {
    public SimplifyCraftingWorkerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    @Override
    public void onReady() {
        super.onReady();
        getMainNode().setIdlePowerUsage(SimplifyPowerProfile.L1.highIdleComponentPower());
    }
}

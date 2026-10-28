package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidOutputHatchBlock;
import cn.dancingsnow.neoecoae.gui.common.FluidHatchUI;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidOutputHatchBlockEntity;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class SimplifyFluidOutputHatchBlock extends ECOFluidOutputHatchBlock {
    public SimplifyFluidOutputHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof ECOFluidOutputHatchBlockEntity be) {
            return FluidHatchUI.createFluidHatchUI(
                    holder, be.getFluidTankHandler(), "block.neoecoprototype.simplify_fluid_output_hatch", false, true);
        }
        return null;
    }
}

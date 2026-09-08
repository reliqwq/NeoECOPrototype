package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidInputHatchBlock;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.CraftingUIHelper;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidInputHatchBlockEntity;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

public class SimplifyFluidInputHatchBlock extends ECOFluidInputHatchBlock {
    public SimplifyFluidInputHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos) instanceof ECOFluidInputHatchBlockEntity be) {
            return CraftingUIHelper.createFluidHatchUI(
                    holder, be.tank, "block.neoecoprototype.simplify_fluid_input_hatch", true, true);
        }
        return null;
    }
}

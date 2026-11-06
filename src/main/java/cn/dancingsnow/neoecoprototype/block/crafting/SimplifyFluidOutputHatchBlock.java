package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidOutputHatchBlock;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOFluidOutputHatchBlockEntity;
import cn.dancingsnow.neoecoae.gui.common.FluidHatchUI;
import com.lowdragmc.lowdraglib2.gui.factory.BlockUIMenuType;
import com.lowdragmc.lowdraglib2.gui.ui.ModularUI;

/**
 * Our output hatch is eco's block under our id, with one exception: the panel title. eco's
 * {@code ECOFluidOutputHatchBlockEntity.createUI} loads its own key as a literal (21.2.15, offset 5) and
 * hands it to a factory whose title argument is public, so the only way our name can appear there is to
 * call that factory ourselves. Issue #155 gave the input panel its block-name title in the stable release
 * and this half was never part of that request, which is why the two hatches of one machine read
 * differently.
 *
 * <p>This is the hand copy we deleted on 2026-11-04, taken back deliberately: one line, whose every number
 * is pinned against upstream's class file by {@code FluidHatchPanelTitleContractTest} - the factory it
 * calls, the two direction flags {@code (false, true)}, and the tank coming through the accessor #143
 * promised rather than the field. If upstream changes any of those the test reddens before a player does.
 * When they take the same one-line change #155 was, arm three of that file reddens instead, and the answer
 * is to delete this override and the caveat sentence in the release notes - not to leave both behind.</p>
 */
public class SimplifyFluidOutputHatchBlock extends ECOFluidOutputHatchBlock {
    public SimplifyFluidOutputHatchBlock(Properties properties) {
        super(properties);
    }

    @Override
    public ModularUI createUI(BlockUIMenuType.BlockUIHolder holder) {
        if (holder.player.level().getBlockEntity(holder.pos)
                instanceof ECOFluidOutputHatchBlockEntity hatch) {
            return FluidHatchUI.createFluidHatchUI(holder, hatch.getFluidTankHandler(),
                    "block.neoecoprototype.simplify_fluid_output_hatch", false, true);
        }
        return null;
    }
}

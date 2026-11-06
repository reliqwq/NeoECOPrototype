package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidInputHatchBlock;

/**
 * The L1 input hatch deliberately keeps eco's own panel: as of eco 21.2.11 the UI decision moved into
 * {@code ECOFluidInputHatchBlockEntity#createUI}, and that block entity now carries upgrade slots, a marked
 * fluid, a target amount and a priority - all of which only the new panel exposes. Our previous override drew
 * eco's previous panel with our own title, which hid every one of those, so the override is gone rather than
 * retargeted: eco's own {@code createUI} already does the same instanceof-and-delegate we were doing by hand.
 *
 * <p>What deleting it cost us was the panel title, and eco issue #155 - which we filed - gave it back: from
 * 21.2.15, {@code FluidInputHatchUI.createRoot} titles the panel with
 * {@code getBlockState().getBlock().getDescriptionId()}, so {@code block.neoecoprototype.simplify_fluid_input_hatch}
 * is what a player reads here and this class needs no hook at all. The floor is honest about the rest: it is
 * {@code [21.2.15-beta,)}, and the beta still loads eco's own literal, so a player on the beta reads eco's
 * name and a player on the stable release reads ours from the same addon jar.
 * {@code FluidHatchPanelTitleContractTest} pins which side of that line the jar we compile against is on.</p>
 */
public class SimplifyFluidInputHatchBlock extends ECOFluidInputHatchBlock {
    public SimplifyFluidInputHatchBlock(Properties properties) {
        super(properties);
    }
}

package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidInputHatchBlock;

/**
 * The L1 input hatch deliberately keeps eco's own panel: as of eco 21.2.11 the UI decision moved into
 * {@code ECOFluidInputHatchBlockEntity#createUI}, and that block entity now carries upgrade slots, a marked
 * fluid, a target amount and a priority - all of which only the new panel exposes. Our previous override drew
 * eco's previous panel with our own title, which hid every one of those, so the override is gone rather than
 * retargeted: eco's own {@code createUI} already does the same instanceof-and-delegate we were doing by hand.
 * The cost is measured, not assumed - {@code FluidHatchUI.createInputHatchUI(BlockUIHolder, BE)} takes no
 * title argument, so the panel is titled by eco's key and
 * {@code block.neoecoprototype.simplify_fluid_input_hatch} is no longer what a player reads there.
 */
public class SimplifyFluidInputHatchBlock extends ECOFluidInputHatchBlock {
    public SimplifyFluidInputHatchBlock(Properties properties) {
        super(properties);
    }
}

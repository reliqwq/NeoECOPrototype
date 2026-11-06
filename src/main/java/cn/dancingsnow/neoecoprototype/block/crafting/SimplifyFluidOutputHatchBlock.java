package cn.dancingsnow.neoecoprototype.block.crafting;

import cn.dancingsnow.neoecoae.blocks.crafting.ECOFluidOutputHatchBlock;

/**
 * Our output hatch is eco's block under our id, and that includes the panel: eco's own
 * {@code ECOFluidOutputHatchBlock.createUI} already does the instanceof-and-delegate to its block
 * entity, which is the same thing the five lines we used to write here did. Keeping a copy of
 * upstream's method body only buys us a place to drift - the input hatch stopped carrying one on
 * 2026-10-27 and that is what made the two hatches agree with eco instead of with each other's
 * history.
 *
 * <p>What the copy did buy was our own title string in the panel: {@code FluidHatchUI
 * .createFluidHatchUI} takes a title key, so the override could pass
 * {@code block.neoecoprototype.simplify_fluid_output_hatch}. This panel still loads eco's key as a literal
 * ({@code createUI}, offset 5, on 21.2.15 - and offset 2 reads the {@code tank} field rather than the
 * accessor eco promised in #143), so its header says "ECO Fluid Output Hatch" while its neighbour on the
 * same machine now says ours: #155 gave the input panel its block-name title in 21.2.15 and this half was
 * never part of that request. The draft asking for the identical one-line change sits unsent at
 * {@code 上游请求稿/NEO_ECO_AE_PR_FLUID_OUTPUT_HATCH_TITLE.md}; {@code FluidHatchPanelTitleContractTest}
 * reddens when it lands so the caveat cannot outlive the fix. Buying the title back by re-copying the method
 * body is the option we already rejected once, on 2026-11-04, for exactly the drift it caused.</p>
 */
public class SimplifyFluidOutputHatchBlock extends ECOFluidOutputHatchBlock {
    public SimplifyFluidOutputHatchBlock(Properties properties) {
        super(properties);
    }
}

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
 * {@code block.neoecoprototype.simplify_fluid_output_hatch}. The new input panel hardcodes its
 * key and keeps the title hook package-private, so it cannot do the same - see eco issue #155.
 * Until that lands the panel title of both hatches is eco's; the block's own name still uses our
 * key either way.</p>
 */
public class SimplifyFluidOutputHatchBlock extends ECOFluidOutputHatchBlock {
    public SimplifyFluidOutputHatchBlock(Properties properties) {
        super(properties);
    }
}

package cn.dancingsnow.neoecoprototype.block;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * Guide-only block for the processor-assembly card page. {@code body=true} draws the large integrated
 * working station's working model; {@code body=false} draws nothing but still counts as a filled cell.
 * GuideMe sizes a scene from the cells its blocks occupy, not from the model's extent, so a scene whose
 * only block carries a 3x2x2 model is always clipped - the empty cells give it the bounds to match.
 */
public class WorkstationPreviewBlock extends Block {
    public static final BooleanProperty BODY = BooleanProperty.create("body");

    public WorkstationPreviewBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BODY);
    }
}
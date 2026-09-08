package cn.dancingsnow.neoecoprototype.block;

import cn.dancingsnow.neoecoae.blocks.CasingBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** A plain material casing that keeps Eco's casing collision shape without its formed-state logic. */
public class SimplifyCasingBlock extends Block {
    public SimplifyCasingBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return CasingBlock.SHAPE;
    }
}

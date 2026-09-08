package cn.dancingsnow.neoecoprototype.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyEnergyCellBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

/** L1 high-density energy cell block. */
public class SimplifyEnergyCellBlock extends NEBlock<SimplifyEnergyCellBlockEntity> {

    public static final IntegerProperty LEVEL = IntegerProperty.create("level", 0, 4);
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SimplifyEnergyCellBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(LEVEL, 0)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(LEVEL);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }
}

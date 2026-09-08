package cn.dancingsnow.neoecoprototype.block.storage;

import appeng.api.orientation.IOrientationStrategy;
import appeng.api.orientation.OrientationStrategies;
import cn.dancingsnow.neoecoae.blocks.NEBlock;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageVentBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;

/** L1 storage subsystem vent block. */
public class SimplifyStorageVentBlock extends NEBlock<SimplifyStorageVentBlockEntity> {

    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public SimplifyStorageVentBlock(Properties properties) {
        super(properties);
        registerDefaultState(getStateDefinition().any()
                .setValue(FORMED, false)
                .setValue(FACING, Direction.NORTH));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
    }

    @Override
    public IOrientationStrategy getOrientationStrategy() {
        return OrientationStrategies.horizontalFacing();
    }
}

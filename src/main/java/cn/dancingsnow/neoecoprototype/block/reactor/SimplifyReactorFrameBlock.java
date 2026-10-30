package cn.dancingsnow.neoecoprototype.block.reactor;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import java.util.Map;

/**
 * Structural strut along the reactor's twelve edges.
 *
 * <p>The six booleans say which of its neighbours are also struts, and the multipart model draws one
 * beam per true flag; that is what makes an edge read as a continuous strut without baking 64 model
 * variants or borrowing a CTM mod.
 */
public class SimplifyReactorFrameBlock extends Block {

    public static final Map<Direction, BooleanProperty> CONNECTED = Map.of(
            Direction.NORTH, BlockStateProperties.NORTH,
            Direction.EAST, BlockStateProperties.EAST,
            Direction.SOUTH, BlockStateProperties.SOUTH,
            Direction.WEST, BlockStateProperties.WEST,
            Direction.UP, BlockStateProperties.UP,
            Direction.DOWN, BlockStateProperties.DOWN);

    public SimplifyReactorFrameBlock(Properties properties) {
        super(properties);
        registerDefaultState(stateDefinition.any()
                .setValue(BlockStateProperties.NORTH, false)
                .setValue(BlockStateProperties.EAST, false)
                .setValue(BlockStateProperties.SOUTH, false)
                .setValue(BlockStateProperties.WEST, false)
                .setValue(BlockStateProperties.UP, false)
                .setValue(BlockStateProperties.DOWN, false));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BlockStateProperties.NORTH, BlockStateProperties.EAST, BlockStateProperties.SOUTH,
                BlockStateProperties.WEST, BlockStateProperties.UP, BlockStateProperties.DOWN);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return connectedState(defaultBlockState(), context.getLevel(), context.getClickedPos());
    }

    @Override
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighbourState,
                                     LevelAccessor level, BlockPos pos, BlockPos neighbourPos) {
        return state.setValue(CONNECTED.get(direction), isStrut(neighbourState));
    }

    private static BlockState connectedState(BlockState state, BlockGetter level, BlockPos pos) {
        BlockState connected = state;
        for (Direction direction : Direction.values()) {
            connected = connected.setValue(CONNECTED.get(direction),
                    isStrut(level.getBlockState(pos.relative(direction))));
        }
        return connected;
    }

    private static boolean isStrut(BlockState state) {
        return state.getBlock() instanceof SimplifyReactorFrameBlock;
    }
}

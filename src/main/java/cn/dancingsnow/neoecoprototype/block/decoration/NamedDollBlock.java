package cn.dancingsnow.neoecoprototype.block.decoration;

import cn.dancingsnow.neoecoprototype.blockentity.decoration.NamedDollBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import org.jetbrains.annotations.Nullable;

/**
 * One of the four honoured players' dolls, sitting on a shelf. It is a plain block model - its face is
 * in its texture - with two pieces of behaviour: it turns to look at whoever sets it down, and creepers
 * keep their distance from it, the way they keep their distance from a cat.
 *
 * <p>The scare is paid for here rather than by the creeper. A goal would mean every creeper in the world
 * asking, on its own ticks, whether a plushie is near - a cost that grows with how many creepers the
 * game happens to have loaded. This way the cost grows with how many dolls the player chose to place,
 * which is the number they can see.
 */
public class NamedDollBlock extends Block implements EntityBlock {
    public static final DirectionProperty FACING = BlockStateProperties.HORIZONTAL_FACING;

    public NamedDollBlock(BlockBehaviour.Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(FACING, net.minecraft.core.Direction.NORTH));
    }

    /** The doll is set down facing whoever set it down. */
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return defaultBlockState().setValue(FACING, context.getHorizontalDirection().getOpposite());
    }

    @Override
    protected BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new NamedDollBlockEntity(pos, state);
    }

    /**
     * The block entity holds nothing, so it exists only to be swept - and only on the server, since a
     * client that repels nothing would still pay for the query.
     */
    @Override
    @Nullable
    @SuppressWarnings("unchecked")
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
                                                                  BlockEntityType<T> type) {
        if (level.isClientSide() || type != ModRegistration.FUMO_DOLL_BE.get()) {
            return null;
        }
        BlockEntityTicker<NamedDollBlockEntity> sweep = NamedDollBlockEntity::tick;
        return (BlockEntityTicker<T>) (BlockEntityTicker<?>) sweep;
    }
}

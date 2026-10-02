package cn.dancingsnow.neoecoprototype.block.frigit;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import org.jetbrains.annotations.Nullable;

/**
 * A budding crystal that grows in four conditions instead of one.
 *
 * <p>The shape is AE2's and eco's - a flawless rock grows crystals and never wears out, the three lower
 * conditions degrade on their own over time - but the chain is passed in rather than looked up. eco's
 * version hardcodes its own eight block entries into the class and throws {@code IllegalStateException} on
 * anything else, which is exactly why we could not reuse it for a second family.
 */
public class FrigitBuddingBlock extends Block {
    private static final Direction[] DIRECTIONS = Direction.values();
    /** One growth attempt per five random ticks, matching AE2 and eco. */
    private static final int GROWTH_CHANCE = 5;
    /** A worn rock loses another piece of itself once every twelve of its own random ticks. */
    private static final int DECAY_CHANCE = 12;

    /** Resolved lazily: the four conditions are registered in a ring, so a plain field would be a forward
     * reference to a block that is not registered yet. Null only for flawless, which never wears down -
     * eco and AE2 both keep the top condition permanent so a pristine rock stays a renewable source. */
    @Nullable
    private final java.util.function.Supplier<Block> nextCondition;
    private final FrigitGrowth growth;

    /** The top of the chain: grows forever, does not degrade. */
    public FrigitBuddingBlock(FrigitGrowth growth) {
        this(null, growth);
    }

    public FrigitBuddingBlock(@Nullable java.util.function.Supplier<Block> nextCondition, FrigitGrowth growth) {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.DIAMOND).strength(1.5F).sound(net.minecraft.world.level.block.SoundType.AMETHYST)
                .randomTicks().noOcclusion());
        this.nextCondition = nextCondition;
        this.growth = growth;
    }

    @Override
    public PushReaction getPistonPushReaction(BlockState state) {
        return PushReaction.DESTROY;
    }

    /**
     * Public rather than protected so the growth and decay chain can be driven tick by tick from a game
     * test; waiting for the world to hand out enough random ticks would take longer than the test is worth.
     */
    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (random.nextInt(GROWTH_CHANCE) == 0) {
            growOneFace(level, pos, random);
        }
        if (nextCondition != null && random.nextInt(DECAY_CHANCE) == 0) {
            level.setBlockAndUpdate(pos, nextCondition.get().defaultBlockState());
        }
    }

    /** Pick a face at random: either start a bud on it, or take the bud already there one stage further. */
    private void growOneFace(ServerLevel level, BlockPos pos, RandomSource random) {
        Direction direction = DIRECTIONS[random.nextInt(DIRECTIONS.length)];
        BlockPos target = pos.relative(direction);
        BlockState existing = level.getBlockState(target);
        Block next = null;
        if (canClusterGrowAt(existing)) {
            next = growth.smallBud();
        } else if (existing.is(growth.smallBud()) && existing.getValue(growth.facing()) == direction) {
            next = growth.mediumBud();
        } else if (existing.is(growth.mediumBud()) && existing.getValue(growth.facing()) == direction) {
            next = growth.largeBud();
        } else if (existing.is(growth.largeBud()) && existing.getValue(growth.facing()) == direction) {
            next = growth.cluster();
        }
        if (next == null) {
            return;
        }
        BlockState state = next.defaultBlockState().setValue(growth.facing(), direction);
        if (growth.waterlogged() != null) {
            state = state.setValue(growth.waterlogged(), existing.is(Blocks.WATER));
        }
        level.setBlockAndUpdate(target, state);
    }

    private static boolean canClusterGrowAt(BlockState state) {
        return state.isAir() || (state.is(Blocks.WATER) && state.getFluidState().getAmount() == 8);
    }

    /** The four things a budding rock can grow, so one class covers the whole family. */
    public record FrigitGrowth(Block smallBud, Block mediumBud, Block largeBud, Block cluster,
                               net.minecraft.world.level.block.state.properties.DirectionProperty facing,
                               net.minecraft.world.level.block.state.properties.BooleanProperty waterlogged) {
    }
}

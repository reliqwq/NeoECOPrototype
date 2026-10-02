package cn.dancingsnow.neoecoprototype.worldgen;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;

import java.util.Optional;

/**
 * A rock that floats in the void between the End's islands, with the ores nobody can find on the ground
 * packed inside it.
 *
 * <p>It is our own structure rather than a hook into AE2's meteorite on purpose: AE2 builds its rock from
 * code, so adding blocks to it would mean copying its block-entity setup and breaking whenever it changes.
 * Owning the structure also means the placement is not a feature at all - the piece decides every block, so
 * no custom placement modifier is needed.
 */
public class CryotheumMeteoriteStructure extends Structure {
    public static final MapCodec<CryotheumMeteoriteStructure> CODEC = RecordCodecBuilder.mapCodec(instance ->
            instance.group(
                    com.mojang.serialization.Codec.intRange(3, 10).fieldOf("radius")
                            .forGetter(structure -> structure.radius),
                    com.mojang.serialization.Codec.intRange(0, 200).fieldOf("center_y")
                            .forGetter(structure -> structure.centerY),
                    settingsCodec(instance))
                    .apply(instance, (radius, centerY, settings) ->
                            new CryotheumMeteoriteStructure(settings, radius, centerY)));

    private final int radius;
    private final int centerY;

    public CryotheumMeteoriteStructure(StructureSettings settings, int radius, int centerY) {
        super(settings);
        this.radius = radius;
        this.centerY = centerY;
    }

    @Override
    public StructureType<?> type() {
        return ModWorldgen.CRYOTHEUM_METEORITE.get();
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext context) {
        ChunkPos chunk = context.chunkPos();
        int x = chunk.getMiddleBlockX();
        int z = chunk.getMiddleBlockZ();
        if (centerY + radius >= context.heightAccessor().getMaxBuildHeight()
                || centerY - radius <= context.heightAccessor().getMinBuildHeight()) {
            return Optional.empty();
        }
        var center = new BlockPos(x, centerY, z);
        return Optional.of(new GenerationStub(center, builder -> builder.addPiece(
                new MeteoritePiece(center, radius))));
    }

    /**
     * The comet itself: an ellipsoid head of sky stone with banded ore inside, a hollow cell-sized room at its
     * centre whose floor is the mother rock, and a tapering tail of ice behind it. The rock's face on the tail
     * side is frosted too, so the tail does not start out of thin air.
     */
    public static class MeteoritePiece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
        /** How far the tail reaches past the head's own surface. Measured from the surface, not from the
         * centre: from the centre a tail is inside the rock for its first several blocks and tapers to nothing
         * before it gets out, which lays down about four blocks of ice. */
        private static final double TAIL_PAST_HEAD = 24.0;
        /** AE2's own base radius factor, applied to the head's radius in the direction the tail points. */
        private static final double TAIL_BASE_FACTOR = 0.8;
        /** Ice density at the head, and how fast it thins out toward the tip. */
        private static final double FROST_DENSITY = 0.35;
        private static final double FROST_FADE_POWER = 1.6;
        /** How much of the tail-facing rock surface is frosted instead of left as sky stone. */
        private static final double SURFACE_FROST = 0.5;
        private final BlockPos center;
        private final int radius;
        private final Direction tail;

        MeteoritePiece(BlockPos center, int radius) {
            this(ModWorldgen.METEORITE_PIECE.get(), center, radius, cometBox(center, radius));
        }

        MeteoritePiece(net.minecraft.nbt.CompoundTag tag) {
            this(new BlockPos(tag.getInt("Cx"), tag.getInt("Cy"), tag.getInt("Cz")), tag.getInt("Radius"));
        }

        private MeteoritePiece(StructurePieceType type, BlockPos center, int radius, BoundingBox box) {
            super(type, 0, box);
            this.center = center;
            this.radius = radius;
            this.tail = tailAxis(center);
        }

        @Override
        protected void addAdditionalSaveData(
                net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext context,
                net.minecraft.nbt.CompoundTag tag) {
            tag.putInt("Cx", center.getX());
            tag.putInt("Cy", center.getY());
            tag.putInt("Cz", center.getZ());
            tag.putInt("Radius", radius);
        }

        /**
         * Any of the six axes, including straight up and straight down - AE2's own choice - drawn from a seed
         * of the centre so every chunk that writes this rock points its tail the same way.
         */
        private static Direction tailAxis(BlockPos center) {
            return Direction.values()[RandomSource.create(Mth.getSeed(center) + 4242L).nextInt(6)];
        }

        /** How far the ellipsoid reaches along an axis whose weight is this: 0.7 sideways, 1.4 above, 0.8 below. */
        private static double headExtent(int radius, double weight) {
            return radius / Math.sqrt(weight);
        }

        private static double headExtentAlong(int radius, Direction tail) {
            return headExtent(radius, tail.getStepY() > 0 ? 1.4 : tail.getStepY() < 0 ? 0.8 : 0.7);
        }

        /**
         * The box is not a cube: the comet is wide only around its head, and long only along its tail. Keeping
         * it tight matters because every chunk that touches the box walks the whole thing.
         */
        private static BoundingBox cometBox(BlockPos center, int radius) {
            Direction tail = tailAxis(center);
            int lateral = (int) Math.ceil(headExtent(radius, 0.7));
            int forward = (int) Math.ceil(headExtentAlong(radius, tail) + TAIL_PAST_HEAD);
            int xLo = -lateral, xHi = lateral, yLo = -lateral, yHi = lateral, zLo = -lateral, zHi = lateral;
            switch (tail.getAxis()) {
                case X -> {
                    if (tail.getStepX() > 0) {
                        xHi = forward;
                    } else {
                        xLo = -forward;
                    }
                }
                case Y -> {
                    if (tail.getStepY() > 0) {
                        yHi = forward;
                    } else {
                        yLo = -forward;
                    }
                }
                case Z -> {
                    if (tail.getStepZ() > 0) {
                        zHi = forward;
                    } else {
                        zLo = -forward;
                    }
                }
            }
            return BoundingBox.fromCorners(center.offset(xLo, yLo, zLo), center.offset(xHi, yHi, zHi));
        }

        /** AE2's own shape test: a dome, flattened on top of the centre and slightly deeper below it. */
        private static boolean insideRock(int dx, int dy, int dz, int radius) {
            return dx * dx * 0.7 + dy * dy * (dy > 0 ? 1.4 : 0.8) + dz * dz * 0.7 < (double) radius * radius;
        }

        /** The offset from the centre for a cell `along` the tail axis and `a`/`b` across it. */
        private static BlockPos offsetAlong(Direction tail, int along, int a, int b) {
            return switch (tail.getAxis()) {
                case X -> new BlockPos(tail.getStepX() * along, a, b);
                case Y -> new BlockPos(a, tail.getStepY() * along, b);
                case Z -> new BlockPos(a, b, tail.getStepZ() * along);
            };
        }

        @Override
        public void postProcess(WorldGenLevel level, net.minecraft.world.level.StructureManager manager,
                                net.minecraft.world.level.chunk.ChunkGenerator generator, RandomSource random,
                                BoundingBox restriction, ChunkPos chunkPos, BlockPos pivot) {
            placeHead(level, restriction);
            placeTail(level, restriction);
        }

        private void placeHead(WorldGenLevel level, BoundingBox restriction) {
            int lateral = (int) Math.ceil(headExtent(radius, 0.7));
            var shell = state("ae2", "sky_stone_block", Blocks.END_STONE);
            for (int dx = -lateral; dx <= lateral; dx++) {
                for (int dy = -lateral; dy <= lateral; dy++) {
                    for (int dz = -lateral; dz <= lateral; dz++) {
                        if (!insideRock(dx, dy, dz, radius)) {
                            continue;
                        }
                        if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                            // The room is only its floor: AE2 never writes the other cells, and neither do we -
                            // the End's void is already air, and writing air here would clobber the bud that
                            // the row below just hung one cell up.
                            if (dy == -1) {
                                placeFloorCell(level, restriction, dx, dy, dz);
                            }
                            continue;
                        }
                        var rock = stateFor(dx, dy, dz);
                        var frost = rock.getBlock() == shell.getBlock() ? surfaceFrostAt(dx, dy, dz) : null;
                        put(level, restriction, center.offset(dx, dy, dz), frost == null ? rock : frost);
                    }
                }
            }
        }

        /** Walk the tail along its own axis, one coordinate outward and two across it. */
        private void placeTail(WorldGenLevel level, BoundingBox restriction) {
            double start = headExtentAlong(radius, tail);
            double base = TAIL_BASE_FACTOR * start;
            int cross = (int) Math.ceil(base);
            int forward = (int) Math.ceil(start + TAIL_PAST_HEAD);
            for (int along = 1; along <= forward; along++) {
                // Clamped at 1: the cells before the head's surface would otherwise get a cone wider than
                // its own base. They are inside the rock anyway, so the clamp only stops them being placed.
                double fade = Math.min(1.0, 1.0 - (along - start) / TAIL_PAST_HEAD);
                if (fade <= 0.0) {
                    break;
                }
                double width = base * fade;
                for (int a = -cross; a <= cross; a++) {
                    for (int b = -cross; b <= cross; b++) {
                        if (a * a + b * b > width * width) {
                            continue;
                        }
                        var offset = offsetAlong(tail, along, a, b);
                        if (insideRock(offset.getX(), offset.getY(), offset.getZ(), radius)) {
                            continue;
                        }
                        var pos = center.offset(offset);
                        var rolls = RandomSource.create(Mth.getSeed(pos) + 777L);
                        if (rolls.nextDouble() <= FROST_DENSITY * Math.pow(fade, FROST_FADE_POWER)) {
                            put(level, restriction, pos, frostOf(rolls));
                        }
                    }
                }
            }
        }

        /**
         * Frost on the face of the rock the tail leaves from, so the join reads as one object: a rock cell
         * whose outward side is empty and whose distance from the tail axis is within the tail's own width
         * turns to ice. Only the shell is replaced, so the ore bands still show through the frost.
         */
        private BlockState surfaceFrostAt(int dx, int dy, int dz) {
            double along = dx * tail.getStepX() + dy * tail.getStepY() + dz * tail.getStepZ();
            if (along <= 0.0) {
                return null;
            }
            double across = dx * dx + dy * dy + dz * dz - along * along;
            double base = TAIL_BASE_FACTOR * headExtentAlong(radius, tail);
            if (across > base * base) {
                return null;
            }
            if (insideRock(dx + tail.getStepX(), dy + tail.getStepY(), dz + tail.getStepZ(), radius)) {
                return null;
            }
            var rolls = RandomSource.create(Mth.getSeed(center.offset(dx, dy, dz)) + 1234L);
            return rolls.nextDouble() <= SURFACE_FROST ? frostOf(rolls) : null;
        }

        /** The three ices he asked for: 20% ice, 25% 浮冰, the rest blue ice. */
        private static BlockState frostOf(RandomSource rolls) {
            int pick = rolls.nextInt(100);
            if (pick < 20) {
                return Blocks.ICE.defaultBlockState();
            }
            return pick < 45 ? Blocks.PACKED_ICE.defaultBlockState() : Blocks.BLUE_ICE.defaultBlockState();
        }

        /**
         * One cell of the room's floor, plus the bud it grows upward when it is a mother rock.
         *
         * <p>Both rolls come from a seed of this position rather than from the shared worldgen random, because
         * a structure is written one chunk at a time: a roll taken from a stream would give every chunk a
         * different answer for the same cell. The dead centre gets its floor block but no bud, which is the
         * cell AE2 reserves for its mysterious cube and where a bud would block the room's only doorway.
         */
        private void placeFloorCell(WorldGenLevel level, BoundingBox restriction, int dx, int dy, int dz) {
            // Resolved here, not in a static field: this class is initialized as soon as the piece type is
            // registered, and the block registry is not guaranteed to be filled at that moment.
            var floor = new Block[]{ModRegistration.FRIGIT_CRYSTAL_BLOCK.get(),
                    ModRegistration.DAMAGED_BUDDING_FRIGIT.get(), ModRegistration.CHIPPED_BUDDING_FRIGIT.get(),
                    ModRegistration.FLAWED_BUDDING_FRIGIT.get(), ModRegistration.FLAWLESS_BUDDING_FRIGIT.get()};
            var buds = new Block[]{ModRegistration.SMALL_FRIGIT_BUD.get(), ModRegistration.MEDIUM_FRIGIT_BUD.get(),
                    ModRegistration.LARGE_FRIGIT_BUD.get()};
            var rolls = net.minecraft.util.RandomSource.create(Mth.getSeed(center.offset(dx, dy, dz)) + 981234567L);
            int index = rolls.nextInt(floor.length);
            put(level, restriction, center.offset(dx, dy, dz), floor[index].defaultBlockState());
            if (index == 0 || (dx == 0 && dz == 0) || rolls.nextFloat() > 0.7f) {
                return;
            }
            var bud = buds[rolls.nextInt(buds.length)].defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING, Direction.UP);
            put(level, restriction, center.offset(dx, dy + 1, dz), bud);
        }

        private static void put(WorldGenLevel level, BoundingBox restriction, BlockPos pos, BlockState state) {
            if (restriction.isInside(pos)) {
                level.setBlock(pos, state, Block.UPDATE_KNOWN_SHAPE);
            }
        }

        /**
         * The bands are a function of the world position, not of the shared worldgen random: a structure is
         * written one chunk at a time, and anything random per call would come out different in every chunk.
         *
         * <p>The shell is AE2's sky stone rather than end stone because a floating End rock made of the
         * End's own ground reads as a piece of scenery that was always there; sky stone is what tells the
         * player this fell in from somewhere else.
         */
        private BlockState stateFor(int dx, int dy, int dz) {
            double band = Math.sin(dx * 0.78 + center.getX() * 0.11)
                    + Math.cos(dz * 0.71 + center.getZ() * 0.13)
                    + Math.sin(dy * 0.9);
            if (band > 1.55) {
                return state("neoecoae", "tungsten_ore", Blocks.END_STONE);
            }
            if (band > 0.85) {
                return state("neoecoae", "aluminum_ore", Blocks.END_STONE);
            }
            if (band < -1.35) {
                return ModRegistration
                        .CRYOTHEUM_ORE_BLOCK.get().defaultBlockState();
            }
            if (band < -0.6) {
                return ModRegistration
                        .END_CRYOTHEUM_ORE_BLOCK.get().defaultBlockState();
            }
            return state("ae2", "sky_stone_block", Blocks.END_STONE);
        }

        /** A block from a mod we depend on; end stone stands in if the id is ever gone. */
        private static BlockState state(String namespace, String path, Block fallback) {
            Block found = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
            return (found == null || found == Blocks.AIR ? fallback : found).defaultBlockState();
        }
    }
}

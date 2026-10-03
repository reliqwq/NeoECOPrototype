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
     * centre whose floor is the mother rock, and a tail of ice that leaves the head at about thirty degrees.
     * The rock's face on the tail side is frosted too, so the tail does not start out of thin air.
     */
    public static class MeteoritePiece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
        /** How far the tail reaches past the head's own surface. Measured from the surface, not from the
         * centre: from the centre a tail is inside the rock for its first several blocks and tapers to nothing
         * before it gets out, which lays down about four blocks of ice. */
        private static final double TAIL_PAST_HEAD = 72.0;
        /** AE2's own base radius factor, applied to the head's radius in the direction the tail points. */
        private static final double TAIL_BASE_FACTOR = 0.8;
        /** The frosted patch on the rock, as a multiple of the tail's own base radius. At 1.3 the whole
         * tail-facing side turns to ice, which is not "mostly rock with a little ice"; 0.8 frosts 41% of that
         * side and 16% of the surface overall. */
        private static final double CAP_WIDTH_FACTOR = 0.8;
        /** Ice density at the head, and how fast it thins out toward the tip. */
        private static final double FROST_DENSITY = 0.35;
        private static final double FROST_FADE_POWER = 1.6;
        /** The tail's first few blocks are laid solid and filled with eco's energized crystal, so the tail
         * grows out of the rock instead of hovering a few blocks off its surface. */
        private static final double ROOT_DEPTH = 6.0;
        /** The last quarter of the tail is certus quartz and plain ice rather than frost. */
        private static final double TIP_FADE = 0.25;
        /** Fifteen degrees, and always pointing up: a tail behind and above the rock is what makes the rock
         * read as diving. It used to be thirty and randomly up or down, which made half the comets in the End
         * look like they were climbing away. */
        private static final double TAIL_SINE = 0.2588190451;
        private static final double TAIL_COSINE = Math.sqrt(1.0 - TAIL_SINE * TAIL_SINE);
        private final BlockPos center;
        private final int radius;
        private final Tail tail;

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
            this.tail = tailDirection(center);
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
         * One of the four horizontal azimuths, drawn from a seed of the centre so every chunk that writes this
         * rock points its tail the same way. The pitch is not random: see {@link #TAIL_SINE}.
         */
        private static Tail tailDirection(BlockPos center) {
            var rolls = RandomSource.create(Mth.getSeed(center) + 4242L);
            int spin = rolls.nextInt(4);
            double ax = switch (spin) {
                case 0 -> 1.0;
                case 2 -> -1.0;
                default -> 0.0;
            };
            double az = switch (spin) {
                case 1 -> 1.0;
                case 3 -> -1.0;
                default -> 0.0;
            };
            return new Tail(ax * TAIL_COSINE, TAIL_SINE, az * TAIL_COSINE);
        }

        /** How far the ellipsoid reaches along an axis whose weight is this: 0.7 sideways, 1.4 above, 0.8 below. */
        private static double headExtent(int radius, double weight) {
            return radius / Math.sqrt(weight);
        }

        /** AE2's own shape test: a dome, flattened on top of the centre and slightly deeper below it. */
        private static boolean insideRock(int dx, int dy, int dz, int radius) {
            return dx * dx * 0.7 + dy * dy * (dy > 0 ? 1.4 : 0.8) + dz * dz * 0.7 < (double) radius * radius;
        }

        /**
         * A unit vector, plus the two projections the tail and the frost cap are measured on. The tail is no
         * longer a lattice direction, so everything about it is dot products rather than axis steps.
         */
        private record Tail(double x, double y, double z) {
            /** Solve the head's ellipsoid for this ray, using the same weights as the shape test. */
            double headExtent(int radius) {
                return radius / Math.sqrt(x * x * 0.7 + z * z * 0.7 + y * y * (y > 0 ? 1.4 : 0.8));
            }

            double along(int dx, int dy, int dz) {
                return dx * x + dy * y + dz * z;
            }

            double acrossSquared(int dx, int dy, int dz) {
                double t = along(dx, dy, dz);
                return dx * dx + dy * dy + dz * dz - t * t;
            }

            /** The nearest whole-block steps outward: the horizontal one and the pitched one both count. */
            int outwardX() {
                return (int) Math.signum(x);
            }

            int outwardY() {
                return (int) Math.signum(y);
            }

            int outwardZ() {
                return (int) Math.signum(z);
            }
        }

        /** Low/high per axis: the comet is wide only around its head and long only along its own tail. */
        private static int[] extents(int radius, Tail tail) {
            double base = TAIL_BASE_FACTOR * tail.headExtent(radius);
            double end = tail.headExtent(radius) + TAIL_PAST_HEAD;
            int lateral = (int) Math.ceil(headExtent(radius, 0.7));
            int[] extents = new int[6];
            extents[0] = -(tail.x() >= 0 ? lateral : (int) Math.ceil(end * Math.abs(tail.x()) + base));
            extents[1] = tail.x() >= 0 ? (int) Math.ceil(end * Math.abs(tail.x()) + base) : lateral;
            extents[2] = -(tail.y() >= 0 ? lateral : (int) Math.ceil(end * Math.abs(tail.y()) + base));
            extents[3] = tail.y() >= 0 ? (int) Math.ceil(end * Math.abs(tail.y()) + base) : lateral;
            extents[4] = -(tail.z() >= 0 ? lateral : (int) Math.ceil(end * Math.abs(tail.z()) + base));
            extents[5] = tail.z() >= 0 ? (int) Math.ceil(end * Math.abs(tail.z()) + base) : lateral;
            return extents;
        }

        private static BoundingBox cometBox(BlockPos center, int radius) {
            int[] e = extents(radius, tailDirection(center));
            return BoundingBox.fromCorners(
                    center.offset(e[0], e[2], e[4]), center.offset(e[1], e[3], e[5]));
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
                        // Ore never shows on the outside: the outermost layer is shell whatever the band says,
                        // and that includes the walls of the centre room, which are one cell from the void too.
                        var rock = isSkin(dx, dy, dz, radius) ? shell : stateFor(dx, dy, dz);
                        var frost = rock.getBlock() == shell.getBlock() ? surfaceFrostAt(dx, dy, dz) : null;
                        put(level, restriction, center.offset(dx, dy, dz), frost == null ? rock : frost);
                    }
                }
            }
        }

        /** The outermost layer of the rock: any cell with a neighbour that is not rock. */
        private static boolean isSkin(int dx, int dy, int dz, int radius) {
            return !insideRock(dx + 1, dy, dz, radius) || !insideRock(dx - 1, dy, dz, radius)
                    || !insideRock(dx, dy + 1, dz, radius) || !insideRock(dx, dy - 1, dz, radius)
                    || !insideRock(dx, dy, dz + 1, radius) || !insideRock(dx, dy, dz - 1, radius);
        }

        /** The tail is a projection test now, not a walk: its pitch is not a lattice direction. */
        private void placeTail(WorldGenLevel level, BoundingBox restriction) {
            int[] e = extents(radius, tail);
            double start = tail.headExtent(radius);
            double base = TAIL_BASE_FACTOR * start;
            var crystal = state("neoecoae", "energized_crystal_block", Blocks.END_STONE);
            for (int dx = e[0]; dx <= e[1]; dx++) {
                for (int dy = e[2]; dy <= e[3]; dy++) {
                    for (int dz = e[4]; dz <= e[5]; dz++) {
                        double along = tail.along(dx, dy, dz);
                        if (along <= start || along > start + TAIL_PAST_HEAD) {
                            continue;
                        }
                        double fade = 1.0 - (along - start) / TAIL_PAST_HEAD;
                        double width = base * fade;
                        if (tail.acrossSquared(dx, dy, dz) > width * width) {
                            continue;
                        }
                        var pos = center.offset(dx, dy, dz);
                        if (along - start <= ROOT_DEPTH) {
                            put(level, restriction, pos, crystal);
                            continue;
                        }
                        var rolls = RandomSource.create(Mth.getSeed(pos) + 777L);
                        // The tip is laid solid, not by the fading chance: the cone narrows to almost nothing
                        // over its last quarter, and at radius 9 the whole band is 80 cells - a fading density
                        // there left three blocks of ice and nothing else to read as the end of the comet.
                        if (fade <= TIP_FADE
                                || rolls.nextDouble() <= FROST_DENSITY * Math.pow(fade, FROST_FADE_POWER)) {
                            put(level, restriction, pos, tailBlock(rolls, fade));
                        }
                    }
                }
            }
        }

        /** What the tail is made of at this point along it: certus quartz and ice at the far tip, the three
         * ices in between. */
        private static BlockState tailBlock(RandomSource rolls, double fade) {
            if (fade <= TIP_FADE) {
                return rolls.nextBoolean() ? state("ae2", "quartz_block", Blocks.BLUE_ICE)
                        : Blocks.ICE.defaultBlockState();
            }
            return frostOf(rolls);
        }

        /**
         * Frost on the face of the rock the tail leaves from, so the join reads as one object: every rock cell
         * within most of the tail's own width whose outward step leaves the rock turns to ice. Only the shell
         * is replaced, so the rest of the face stays meteorite and the ore bands still show through.
         */
        private BlockState surfaceFrostAt(int dx, int dy, int dz) {
            if (tail.along(dx, dy, dz) <= 0.0) {
                return null;
            }
            double cap = TAIL_BASE_FACTOR * tail.headExtent(radius) * CAP_WIDTH_FACTOR;
            if (tail.acrossSquared(dx, dy, dz) > cap * cap) {
                return null;
            }
            // Both whole-block steps that lean toward the tail count as outward, because the tail's own
            // direction is not one of them: the patch then wraps the join instead of sitting on the equator.
            if (insideRock(dx + tail.outwardX(), dy, dz, radius)
                    && insideRock(dx, dy + tail.outwardY(), dz, radius)
                    && insideRock(dx, dy, dz + tail.outwardZ(), radius)) {
                return null;
            }
            return frostOf(RandomSource.create(Mth.getSeed(center.offset(dx, dy, dz)) + 1234L));
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

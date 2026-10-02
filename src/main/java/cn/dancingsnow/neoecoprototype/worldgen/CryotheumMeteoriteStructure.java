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

    /** The floating rock itself: a shell of end stone, banded ore inside, one crystal block at the core. */
    public static class MeteoritePiece extends net.minecraft.world.level.levelgen.structure.StructurePiece {
        /** Half-width of the frost field around the rock: 16x16 columns, matching how he asked for it. */
        private static final int FROST_HALF = 8;
        private final BlockPos center;
        private final int radius;

        MeteoritePiece(BlockPos center, int radius) {
            this(ModWorldgen.METEORITE_PIECE.get(), center, radius,
                    BoundingBox.fromCorners(center.offset(-FROST_HALF, -radius - 1, -FROST_HALF),
                            center.offset(FROST_HALF, radius, FROST_HALF)));
        }

        private MeteoritePiece(StructurePieceType type, BlockPos center, int radius, BoundingBox box) {
            super(type, 0, box);
            this.center = center;
            this.radius = radius;
        }

        MeteoritePiece(net.minecraft.nbt.CompoundTag tag) {
            this(new BlockPos(tag.getInt("Cx"), tag.getInt("Cy"), tag.getInt("Cz")), tag.getInt("Radius"));
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

        @Override
        public void postProcess(WorldGenLevel level, net.minecraft.world.level.StructureManager manager,
                                net.minecraft.world.level.chunk.ChunkGenerator generator, RandomSource random,
                                BoundingBox restriction, ChunkPos chunkPos, BlockPos pivot) {
            for (int dx = -radius; dx <= radius; dx++) {
                for (int dy = -radius; dy <= radius; dy++) {
                    for (int dz = -radius; dz <= radius; dz++) {
                        double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
                        if (distance > radius + 0.25) {
                            continue;
                        }
                        BlockPos pos = center.offset(dx, dy, dz);
                        if (!restriction.isInside(pos)) {
                            continue;
                        }
                        level.setBlock(pos, stateFor(dx, dy, dz), Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
            scatterFrost(level, restriction);
        }

        /**
         * Blue ice floating around the rock. The field is drawn from a random seeded by the centre and the
         * draw loop never looks at which chunk is writing, so a rock that spans several chunks lays down the
         * same 72-105 blocks in total rather than that many per chunk.
         */
        private void scatterFrost(WorldGenLevel level, BoundingBox restriction) {
            var random = net.minecraft.util.RandomSource.create(Mth.getSeed(center));
            var wanted = 72 + random.nextInt(34);
            var taken = new java.util.HashSet<BlockPos>();
            int attempts = 0;
            for (int drawn = 0; drawn < wanted && attempts++ < wanted * 6; ) {
                int dx = random.nextInt(FROST_HALF * 2 + 1) - FROST_HALF;
                int dy = random.nextInt(radius * 2 + 1) - radius;
                int dz = random.nextInt(FROST_HALF * 2 + 1) - FROST_HALF;
                if (Math.sqrt(dx * dx + dy * dy + dz * dz) <= radius + 0.9) {
                    continue;
                }
                var pos = center.offset(dx, dy, dz);
                if (!taken.add(pos)) {
                    continue;
                }
                drawn++;
                if (restriction.isInside(pos)) {
                    level.setBlock(pos, Blocks.BLUE_ICE.defaultBlockState(), Block.UPDATE_KNOWN_SHAPE);
                }
            }
        }

        /**
         * The bands are a function of the world position, not of the shared worldgen random: a structure is
         * written one chunk at a time, and anything random per call would come out different in every chunk.
         *
         * <p>The middle of the rock is hollowed out into a pocket around the mother block, and the crystals
         * grow from the mother rock into that empty space. They need somewhere to be: a bud whose facing
         * points into solid rock is placed, then dropped by its own survival check the next time the chunk
         * loads.
         *
         * <p>The shell is AE2's sky stone rather than end stone because a floating End rock made of the
         * End's own ground reads as a piece of scenery that was always there; sky stone is what tells the
         * player this fell in from somewhere else.
         */
        private BlockState stateFor(int dx, int dy, int dz) {
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);
            if (Math.abs(dx) <= 1 && Math.abs(dy) <= 1 && Math.abs(dz) <= 1) {
                return ModRegistration.FLAWLESS_BUDDING_FRIGIT.get().defaultBlockState();
            }
            if (distance < 3.6) {
                return crystalInPocket(dx, dy, dz);
            }
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

        /** A bud or cluster on the wall of the pocket, or the empty space itself. */
        private BlockState crystalInPocket(int dx, int dy, int dz) {
            int ax = Math.abs(dx);
            int ay = Math.abs(dy);
            int az = Math.abs(dz);
            if (ax < 2 && ay < 2 && az < 2) {
                return Blocks.AIR.defaultBlockState();
            }
            Direction out = Direction.fromAxisAndDirection(
                    ax >= ay && ax >= az ? Direction.Axis.X
                            : ay >= az ? Direction.Axis.Y : Direction.Axis.Z,
                    (ax >= ay && ax >= az ? dx : ay >= az ? dy : dz) < 0
                            ? Direction.AxisDirection.NEGATIVE : Direction.AxisDirection.POSITIVE);
            var growth = switch (Math.abs((dx * 7 + dy * 13 + dz * 21 + center.getX()) % 4)) {
                case 0 -> ModRegistration.FRIGIT_CLUSTER;
                case 1 -> ModRegistration.LARGE_FRIGIT_BUD;
                case 2 -> ModRegistration.MEDIUM_FRIGIT_BUD;
                default -> ModRegistration.SMALL_FRIGIT_BUD;
            };
            return growth.get().defaultBlockState()
                    .setValue(net.minecraft.world.level.block.AmethystClusterBlock.FACING, out);
        }

        /** A block from a mod we depend on; end stone stands in if the id is ever gone. */
        private static BlockState state(String namespace, String path, Block fallback) {
            Block found = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
            return (found == null || found == Blocks.AIR ? fallback : found).defaultBlockState();
        }
    }
}

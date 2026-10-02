package cn.dancingsnow.neoecoprototype.worldgen;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
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
        private final BlockPos center;
        private final int radius;

        MeteoritePiece(BlockPos center, int radius) {
            this(ModWorldgen.METEORITE_PIECE.get(), center, radius,
                    BoundingBox.fromCorners(center.offset(-radius - 1, -radius - 1, -radius - 1),
                            center.offset(radius, radius, radius)));
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
            Block core = BuiltInRegistries.BLOCK.get(
                    ResourceLocation.fromNamespaceAndPath("neoecoae", "energized_crystal_block"));
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
                        level.setBlock(pos, stateFor(distance, core, dx, dy, dz, pos), Block.UPDATE_KNOWN_SHAPE);
                    }
                }
            }
        }

        /**
         * The bands are a function of the world position, not of the shared worldgen random: a structure is
         * written one chunk at a time, and anything random per call would come out different in every chunk.
         */
        private BlockState stateFor(double distance, Block core, int dx, int dy, int dz, BlockPos pos) {
            if (distance > radius - 1.5) {
                return Blocks.END_STONE.defaultBlockState();
            }
            if (distance < 1.9) {
                return core.defaultBlockState();
            }
            double band = Math.sin(dx * 0.78 + center.getX() * 0.11)
                    + Math.cos(dz * 0.71 + center.getZ() * 0.13)
                    + Math.sin(dy * 0.9);
            if (band > 1.55) {
                return ore("neoecoae", "tungsten_ore");
            }
            if (band > 0.85) {
                return ore("neoecoae", "aluminum_ore");
            }
            if (band < -1.35) {
                return cn.dancingsnow.neoecoprototype.registration.ModRegistration
                        .CRYOTHEUM_ORE_BLOCK.get().defaultBlockState();
            }
            if (band < -0.6) {
                return cn.dancingsnow.neoecoprototype.registration.ModRegistration
                        .END_CRYOTHEUM_ORE_BLOCK.get().defaultBlockState();
            }
            return Blocks.END_STONE.defaultBlockState();
        }

        private static BlockState ore(String namespace, String path) {
            Block block = BuiltInRegistries.BLOCK.get(ResourceLocation.fromNamespaceAndPath(namespace, path));
            return block == null || block == Blocks.AIR ? Blocks.END_STONE.defaultBlockState()
                    : block.defaultBlockState();
        }
    }
}

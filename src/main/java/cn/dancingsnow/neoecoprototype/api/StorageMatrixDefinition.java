package cn.dancingsnow.neoecoprototype.api;

import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.Nullable;

/** Immutable metadata for new storage-matrix registrations. Existing cells do not use this yet. */
public record StorageMatrixDefinition(
        ResourceLocation id,
        MatrixType type,
        MatrixSize size,
        long bytes,
        int bytesPerType,
        int totalTypes,
        SimplifyTier tier,
        double idleDrain,
        @Nullable ResourceLocation inventoryModel,
        @Nullable ResourceLocation driveModel,
        boolean infinite
) {
    public enum MatrixType {
        ITEM,
        FLUID,
        CHEMICAL
    }

    public enum MatrixSize {
        K1(1L << 10),
        K4(1L << 12),
        K16(1L << 14),
        K64(1L << 16),
        M1(1L << 20),
        M4(1L << 22),
        INFINITE(-1),
        /** Script-defined capacity that matches no preset tier; the byte count is authoritative. */
        CUSTOM(0L);

        private final long bytes;

        MatrixSize(long bytes) {
            this.bytes = bytes;
        }

        public long bytes() {
            return bytes;
        }
    }

    public StorageMatrixDefinition {
        if (id == null || type == null || size == null || tier == null) {
            throw new IllegalArgumentException("Matrix identity, type, size, and tier are required");
        }
        if (bytes < 1 && !infinite) {
            throw new IllegalArgumentException("Finite matrix bytes must be positive");
        }
        if (bytesPerType < 1 || totalTypes < 1) {
            throw new IllegalArgumentException("Matrix type limits must be positive");
        }
        if (idleDrain < 0) {
            throw new IllegalArgumentException("Matrix idle drain cannot be negative");
        }
        if (infinite != (size == MatrixSize.INFINITE)) {
            throw new IllegalArgumentException("Infinite matrices must use MatrixSize.INFINITE");
        }
        if (!infinite && size != MatrixSize.CUSTOM && size.bytes() != bytes) {
            throw new IllegalArgumentException("Matrix bytes do not match declared size: " + size);
        }
    }

    /** Define a finite matrix while preserving an explicit bytes-per-type value. */
    public static StorageMatrixDefinition finite(ResourceLocation id, MatrixType type, MatrixSize size,
                                                  long bytes, int bytesPerType, int totalTypes,
                                                  SimplifyTier tier,
                                                  @Nullable ResourceLocation inventoryModel,
                                                  @Nullable ResourceLocation driveModel) {
        return new StorageMatrixDefinition(id, type, size, bytes, bytesPerType, totalTypes, tier,
                (double) bytes / (1L << 20), inventoryModel, driveModel, false);
    }

    public static StorageMatrixDefinition infinite(ResourceLocation id, MatrixType type,
                                                    SimplifyTier tier, double idleDrain,
                                                    @Nullable ResourceLocation inventoryModel,
                                                    @Nullable ResourceLocation driveModel) {
        return new StorageMatrixDefinition(id, type, MatrixSize.INFINITE, 1, 1, 1, tier,
                idleDrain, inventoryModel, driveModel, true);
    }
}

package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import net.minecraft.resources.ResourceLocation;

/** Immutable task intent used by the read-only Trinity preflight layer. */
public record TrinityTaskRequest(Kind kind, ResourceLocation targetItem, int quantity,
                                 boolean needsStorage,
                                 boolean needsComputation,
                                 boolean needsCrafting) {
    public TrinityTaskRequest {
        if (kind == null) {
            throw new IllegalArgumentException("Task kind is required.");
        }
        if (targetItem == null) {
            throw new IllegalArgumentException("Target item is required.");
        }
    }

    public static TrinityTaskRequest fullTask(ResourceLocation targetItem, int quantity) {
        return new TrinityTaskRequest(Kind.GENERIC, targetItem, quantity, true, true, true);
    }

    /** Compatibility request for the status-only overview. */
    public static TrinityTaskRequest fullTask(int quantity) {
        return fullTask(ResourceLocation.withDefaultNamespace("air"), quantity);
    }

    public enum Kind {
        GENERIC,
        STORAGE,
        COMPUTATION,
        CRAFTING
    }
}

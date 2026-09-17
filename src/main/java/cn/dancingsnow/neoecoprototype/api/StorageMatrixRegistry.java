package cn.dancingsnow.neoecoprototype.api;

import net.minecraft.world.item.Item;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import net.minecraft.resources.ResourceLocation;

import java.util.IdentityHashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/** Registry for new matrix definitions; legacy registration paths remain independent. */
public final class StorageMatrixRegistry {
    private static final Map<StorageMatrixDefinition, Item> BY_DEFINITION = new LinkedHashMap<>();
    private static final Map<Item, StorageMatrixDefinition> BY_ITEM = new IdentityHashMap<>();
    private static boolean frozen;

    private StorageMatrixRegistry() {
    }

    public static synchronized void register(StorageMatrixDefinition definition, Item item) {
        Objects.requireNonNull(definition, "definition");
        Objects.requireNonNull(item, "item");
        if (frozen) {
            throw new IllegalStateException("Storage matrix registry is already frozen");
        }
        if (BY_DEFINITION.keySet().stream().anyMatch(existing -> existing.id().equals(definition.id()))) {
            throw new IllegalArgumentException("Duplicate storage matrix id: " + definition.id());
        }
        if (BY_ITEM.containsKey(item)) {
            throw new IllegalArgumentException("Item is already registered as a storage matrix: " + item);
        }
        BY_DEFINITION.put(definition, item);
        BY_ITEM.put(item, definition);
    }

    /** Freeze after all normal registrations; KubeJS may use its own dynamic lifecycle before this point. */
    public static synchronized void freeze() {
        frozen = true;
    }

    public static synchronized Map<StorageMatrixDefinition, Item> definitions() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(BY_DEFINITION));
    }

    @Nullable
    public static synchronized StorageMatrixDefinition get(Item item) {
        return BY_ITEM.get(item);
    }

    @Nullable
    public static synchronized StorageMatrixDefinition get(ResourceLocation id) {
        for (StorageMatrixDefinition definition : BY_DEFINITION.keySet()) {
            if (definition.id().equals(id)) {
                return definition;
            }
        }
        return null;
    }
}

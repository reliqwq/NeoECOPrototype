package cn.dancingsnow.neoecoprototype.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/** Server-side compatibility choices for the L1 storage host. */
public final class NeoECOPrototypeServerConfig {
    public static final ModConfigSpec SPEC;
    /**
     * Additional eco-handled storage-cell item IDs that an L1 drive may mount despite their tier.
     * L1-native cells and this addon's small bulk cells never need to be listed here.
     */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> L1_ADDITIONAL_STORAGE_CELLS;
    public static final ModConfigSpec.LongValue MEGA_BULK_AUTO_MARK_THRESHOLD;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("l1_storage");
        L1_ADDITIONAL_STORAGE_CELLS = builder
                .comment("Additional eco storage-cell item IDs accepted by L1 drives despite tier.",
                        "Example: [\"neoecoae:eco_mega_long_bulk_cell\"]. Default: []")
                .defineListAllowEmpty("additional_storage_cells", List::of, value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        builder.pop();
        MEGA_BULK_AUTO_MARK_THRESHOLD = builder
                .comment("Minimum stored item count for automatic MEGA marker selection; compression still requires the MEGA compression upgrade.")
                .defineInRange("mega_bulk_auto_mark_threshold", 20_000L, 0L, Long.MAX_VALUE);
        SPEC = builder.build();
    }

    private NeoECOPrototypeServerConfig() {
    }
}

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
    /** Number of pattern slots in the green pattern provider. */
    public static final ModConfigSpec.IntValue GREEN_PATTERN_PROVIDER_SLOTS;
    /** Whether the processor assembler also accepts recipes derived from AE2's inscriber. */
    public static final ModConfigSpec.BooleanValue DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER;
    /** Processor outputs the assembler must refuse, from either the JSON recipes or the derivation. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_PROCESSOR_RECIPES;
    /** Whether /prototypefumo may be used. */
    public static final ModConfigSpec.BooleanValue FUMO_COMMAND_ENABLED;
    /**
     * Computation threads per L1 threading core. Each core allocates one {@code ECOCraftingCPU} per
     * thread when it is created, so a core already standing keeps its old count until it is replaced.
     */
    public static final ModConfigSpec.IntValue L1_CPU_THREADS;
    /** Co-processors per L1 parallel core, summed by the cluster with no design cap. */
    public static final ModConfigSpec.IntValue L1_CPU_ACCELERATORS;
    /** Crafting storage bytes per L1 computation cell. Raising it leaves stored cells valid. */
    public static final ModConfigSpec.LongValue L1_CPU_TOTAL_BYTES;

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
        FUMO_COMMAND_ENABLED = builder
                .comment("Enable the /prototypefumo command, which hands out a plushie wearing any player's skin.",
                        "The command still needs OP level 2 or creative mode; turn this off and it refuses every use.")
                .define("fumo_command_enabled", true);
        GREEN_PATTERN_PROVIDER_SLOTS = builder
                .comment("Number of pattern slots in the green pattern provider. Default: 9.")
                .defineInRange("green_pattern_provider_slots", 9, 1, 9_999);
        DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER = builder
                .comment("Also accept processor recipes derived from AE2's inscriber (press-mode recipes, with",
                        "printed parts unfolded into the material inscribed into them).",
                        "This is what lets other content that adds inscriber recipes work with no data file.",
                        "Default: true.")
                .define("derive_processor_recipes_from_inscriber", true);
        builder.push("l1_computation");
        L1_CPU_THREADS = builder
                .comment("Computation threads per L1 threading core. Was 1 before this was configurable.")
                .defineInRange("cpu_threads", 2, 1, 1_024);
        L1_CPU_ACCELERATORS = builder
                .comment("Co-processors per L1 parallel core, i.e. what the panel shows as 并行. Was 16.")
                .defineInRange("cpu_accelerators", 24, 1, 65_536);
        L1_CPU_TOTAL_BYTES = builder
                .comment("Crafting storage bytes per L1 computation cell. Was 1 MiB (1048576).")
                .defineInRange("cpu_total_bytes", 1_572_864L, 1L, 1L << 40);
        builder.pop();
        DISABLED_PROCESSOR_RECIPES = builder
                .comment("Processor outputs the assembler must refuse, e.g. [\"ae2:logic_processor\"].",
                        "Applies to the JSON recipes and the inscriber derivation alike, so removing a data",
                        "file alone is not enough to disable a recipe the inscriber still provides.")
                .defineListAllowEmpty("disabled_processor_recipes",
                        List.of(), value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        SPEC = builder.build();
    }

    /** Shared by the assembler and its JEI page so both agree on what is refused. */
    public static boolean isProcessorRecipeDisabled(net.minecraft.world.item.Item output) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output);
        return DISABLED_PROCESSOR_RECIPES.get().stream().anyMatch(entry -> entry.equals(id.toString()));
    }

    private NeoECOPrototypeServerConfig() {
    }
}

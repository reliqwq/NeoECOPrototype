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
    /** Whether the processor assembler also accepts recipes derived from AE2's inscriber. */
    public static final ModConfigSpec.BooleanValue DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER;
    /** Processor outputs the assembler must refuse, from either the JSON recipes or the derivation. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_PROCESSOR_RECIPES;
    /** Only the /prototypefumo hand-out; the dolls themselves ship switched on. */
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
    /** Same number for the energized cell (CE1R), which is otherwise fixed at the tier constant. */
    public static final ModConfigSpec.LongValue ENERGIZED_CELL_TOTAL_BYTES;
    /** Dimensions the cryotheum ore may generate in; an empty list generates nowhere. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> CRYOTHEUM_ORE_DIMENSIONS;
    /**
     * Out of the box the cryotheum ores generate in nothing. The crystal family they lead to has no recipe
     * yet, so an ore that cannot be earned or spent anywhere stays switched off until that lands. The GUI
     * guard asserts this is empty, so re-enabling a dimension is a deliberate edit.
     */
    public static final List<String> CRYOTHEUM_ORE_DIMENSIONS_DEFAULT = List.of();
    /** How much of the vanilla powder-snow chill one broken cryotheum ore is worth. */
    public static final ModConfigSpec.IntValue CRYOTHEUM_ORE_FREEZE_TICKS;
    /** Whether the floating End comet generates at all; it is the family's only remaining world entry. */
    public static final ModConfigSpec.BooleanValue CRYOTHEUM_METEORITE_ENABLED;
    /**
     * Out of the box the comet generates nowhere too. {@link #CRYOTHEUM_ORE_DIMENSIONS_DEFAULT} already
     * turns the veins off, and the comet is the one other thing that puts this family in front of a player:
     * its mother rock, buds and clusters are the ore's only source and the crystal has no recipe yet.
     */
    public static final boolean CRYOTHEUM_METEORITE_ENABLED_DEFAULT = false;

    /** Tick interval of the singularity cell: one batch per this many ticks. */
    public static final ModConfigSpec.LongValue SINGULARITY_CELL_TICKS_PER_BATCH;
    /** Singularities the cell makes per batch, up to {@link #SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX}. */
    public static final ModConfigSpec.IntValue SINGULARITY_CELL_AMOUNT_PER_BATCH;
    /**
     * The ceiling on a single batch: 256 Ki singularities, enough that a pack pushing the interval to its
     * floor fills a bank of {@link Long#MAX_VALUE} in hours rather than centuries.
     */
    public static final int SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX = 262_144;
    /** The shortest interval the cell accepts, in ticks. */
    public static final long SINGULARITY_CELL_TICKS_PER_BATCH_MIN = 60L;

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
                        "Off by default: the command needs OP level 2 or creative mode as well, and the skin it",
                        "copies belongs to a named player. Turn this on and it answers again.",
                        "The dolls themselves are not switched off here: their worn effects, their creative tab",
                        "entries and the creepers that keep their distance from a placed one all ship on.")
                .define("fumo_command_enabled", false);
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
        ENERGIZED_CELL_TOTAL_BYTES = builder
                .comment("Crafting storage bytes per energized computation cell (CE1R).",
                        "Default 5242880 is the old 4 MiB plus 30%.")
                .defineInRange("energized_cell_total_bytes", 5_242_880L, 1L, 1L << 40);
        builder.pop();
        DISABLED_PROCESSOR_RECIPES = builder
                .comment("Processor outputs the assembler must refuse, e.g. [\"ae2:logic_processor\"].",
                        "Applies to the JSON recipes and the inscriber derivation alike, so removing a data",
                        "file alone is not enough to disable a recipe the inscriber still provides.")
                .defineListAllowEmpty("disabled_processor_recipes",
                        List.of(), value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        builder.push("cryotheum_ore");
        CRYOTHEUM_ORE_DIMENSIONS = builder
                .comment("Dimensions the cryotheum ores generate in, by full ID. Each dimension has its own",
                        "ore and its own biome modifier, so adding one entry only switches that ore on.",
                        "An empty list turns all three off. Moving an ore to a dimension it was not authored",
                        "for needs a datapack, because the host stone is chosen by the feature.",
                        "Default: none, until the crystal family they lead to has a recipe.")
                .defineListAllowEmpty("dimensions",
                        () -> CRYOTHEUM_ORE_DIMENSIONS_DEFAULT,
                        value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        CRYOTHEUM_ORE_FREEZE_TICKS = builder
                .comment("Freezing ticks a player gains per broken cryotheum ore. Vanilla starts taking freeze",
                        "damage at 140 ticks in powder snow, so anything below that is the frostbite meter",
                        "filling up, not damage. Zero disables the effect. Default: 60.")
                .defineInRange("freeze_ticks", 60, 0, 600);
        builder.pop();
        builder.push("cryotheum_meteorite");
        CRYOTHEUM_METEORITE_ENABLED = builder
                .comment("Whether the floating End comet is placed at all. Its ore veins are switched on by",
                        "dimensions; the comet is the one world entry left, because it carries the mother rock",
                        "and the buds that the veins would otherwise have no source from.",
                        "It is read while chunks generate, so already placed comets stay in the world and a",
                        "newly generated area simply has none. Default: false - the crystal family this leads to",
                        "has no recipe yet, and a half-built discovery is worse than none.")
                .define("enabled", CRYOTHEUM_METEORITE_ENABLED_DEFAULT);
        builder.pop();
        builder.push("singularity_cell");
        SINGULARITY_CELL_TICKS_PER_BATCH = builder
                .comment("How often the singularity cell makes a batch, in ticks. 600 = every 30 seconds.",
                        "Clamped to at least " + SINGULARITY_CELL_TICKS_PER_BATCH_MIN + " ticks.",
                        "Default: 600.")
                .defineInRange("ticks_per_batch", 600L, SINGULARITY_CELL_TICKS_PER_BATCH_MIN, 1L << 24);
        SINGULARITY_CELL_AMOUNT_PER_BATCH = builder
                .comment("Singularities one batch is worth, and therefore how coarse the bank is: it jumps",
                        "by this number once per interval, it does not drip.",
                        "Clamped to at most " + SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX + ", so the fastest",
                        "this cell can run is that many per " + SINGULARITY_CELL_TICKS_PER_BATCH_MIN
                                + " ticks (about 3 seconds).",
                        "Default: 120.")
                .defineInRange("amount_per_batch", 120, 1, SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        SPEC = builder.build();
    }

    /** The cell's interval, floored: the config range is the guard, this is the read-side one. */
    public static long singularityCellTicksPerBatch() {
        return Math.max(SINGULARITY_CELL_TICKS_PER_BATCH_MIN, SINGULARITY_CELL_TICKS_PER_BATCH.get());
    }

    /** The cell's batch size, capped the same way. */
    public static int singularityCellAmountPerBatch() {
        return Math.min(SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX,
                Math.max(1, SINGULARITY_CELL_AMOUNT_PER_BATCH.get()));
    }

    /** Shared by the assembler and its JEI page so both agree on what is refused. */
    public static boolean isProcessorRecipeDisabled(net.minecraft.world.item.Item output) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output);
        return DISABLED_PROCESSOR_RECIPES.get().stream().anyMatch(entry -> entry.equals(id.toString()));
    }

    private NeoECOPrototypeServerConfig() {
    }
}

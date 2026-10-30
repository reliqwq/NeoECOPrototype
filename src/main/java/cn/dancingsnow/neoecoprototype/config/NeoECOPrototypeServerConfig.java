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
    /** Whether the processor assembler also accepts recipes derived from AE2's inscriber. */
    public static final ModConfigSpec.BooleanValue DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER;
    /** Processor outputs the assembler must refuse, from either the JSON recipes or the derivation. */
    public static final ModConfigSpec.ConfigValue<List<? extends String>> DISABLED_PROCESSOR_RECIPES;
    /** Kept as a plain constant so a test can pin the shipped default without loading a config file. */
    public static final boolean STRUCTURE_CARD_CONSUMES_ITEMS_DEFAULT = true;
    public static final ModConfigSpec.BooleanValue STRUCTURE_CARD_CONSUMES_ITEMS;
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
    /**
     * The wireless link card ships switched off. It works, and every part of it has now been read in a client:
     * two devices join with no cable, channels and power cross the edge, the rebuild pass re-establishes a link
     * after its chunk wakes (four rebuilt lines - two from the wake burst, two from the 100-tick sweep - plus the
     * warning that says so when the burst window was too short), and AE2's own Network Tool lists the joined
     * machines afterwards (read 2026-10-30: "3 channels", two groups of three).
     *
     * <p>What keeps it off is balance, not behaviour: a link crosses zero cable yet carries {@code 8 x the
     * network's channel multiplier} per face, costs no distance, and may cross dimensions, so four of them off
     * one hub are a free dense trunk - the one shape AE2 itself charges cable for. That is a product decision
     * still owed (distance gate, idle draw like ExtendedAE's, or accepting it), and the default stays off until
     * it is made. The channel-allowance question that used to be cited here is gone: AE2 keeps no network-wide
     * budget to price against, so the card charges nothing and the only limit in the code is four links per
     * device.
     */
    public static final boolean WIRELESS_LINKS_DEFAULT = false;
    public static final ModConfigSpec.BooleanValue WIRELESS_LINKS;
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
    public static final ModConfigSpec.LongValue SINGULARITY_CELL_AMOUNT_PER_BATCH;
    /**
     * The ceiling on a single batch: the top of a long, so a pack may set any batch size it can write down
     * and the cell still cannot produce a number the arithmetic cannot hold. {@code bankOf} saturates at
     * this same value, which is where overflow is decided - not here.
     */
    public static final long SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX = Long.MAX_VALUE;
    /** The shortest interval the cell accepts, in ticks. */
    public static final long SINGULARITY_CELL_TICKS_PER_BATCH_MIN = 60L;

    /**
     * How much of a formed host's idle draw a contented mind may take off, as a fraction: 0.10 means the
     * happiest machine costs 90% of what the same machine without a face would. Capped at one, so a mind
     * can only ever save power, never cost any.
     */
    public static final ModConfigSpec.DoubleValue MIND_IDLE_POWER_SAVING;

    /**
     * AE per tick the charging plushie pays out, out of the box. A plain constant because the doll's own
     * stat and this key's default have to be the same number, and a test has to pin the shipped value
     * without loading a config file.
     */
    public static final double FUMO_CHARGE_AE_PER_TICK_DEFAULT = 100.0D;
    /**
     * AE per tick the charging plushie feeds into what its wearer carries. Zero switches the charging off.
     * The tooltip cannot quote this number: it is a server key, and a dedicated client never loads one.
     */
    public static final ModConfigSpec.DoubleValue FUMO_CHARGE_AE_PER_TICK;

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("l1_storage");
        L1_ADDITIONAL_STORAGE_CELLS = builder
                .comment("Additional eco storage-cell item IDs accepted by L1 drives despite tier.",
                        "Example: [\"neoecoae:eco_mega_long_bulk_cell\"]. Default: []")
                .defineListAllowEmpty("additional_storage_cells", List::of, value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        builder.pop();
        STRUCTURE_CARD_CONSUMES_ITEMS = builder
                .comment("Whether the structure expansion card eats the ticket it works from: one paper to",
                        "write a shortfall note, and one blank AE2 pattern to write an exchange pattern.",
                        "On by default - off, every shift-click prints either of them for nothing, and a",
                        "pattern is a real recipe that stays in the world once it is made.")
                .define("structure_card_consumes_items", STRUCTURE_CARD_CONSUMES_ITEMS_DEFAULT);
        WIRELESS_LINKS = builder
                .comment("Wireless ME links: the link card, the level's rebuild pass and its chunk wake.",
                        "Off by default in this release - the join works and AE2's own Network Tool lists what",
                        "it built, but a link is not yet priced: it crosses no cable, no distance and may cross",
                        "dimensions while carrying 8 x the network's channel multiplier per face, so four of them",
                        "off one hub are a free dense trunk. Turning it on lets a player join two devices with no",
                        "cable between them; links recorded while it was on stay in the world's data and come",
                        "back when it is switched on again.")
                .define("wireless_links", WIRELESS_LINKS_DEFAULT);
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
                        "Freely configurable up to " + SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX + ", the top of",
                        "a long. The interval keeps its floor of " + SINGULARITY_CELL_TICKS_PER_BATCH_MIN
                                + " ticks, so the fastest the cell can run is one batch every 3 seconds.",
                        "Overflow is not a crash and not a wrap: once the bank reaches that same top it stops",
                        "growing, and because production is pinned there too, anything you draw afterwards",
                        "stays drawn for good.",
                        "Default: 120.")
                .defineInRange("amount_per_batch", 120L, 1L, SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("machine_mind");
        MIND_IDLE_POWER_SAVING = builder
                .comment("How much of a formed crafting host's idle power draw its mind may save, as a",
                        "fraction of the normal draw. 0.10 means a contented machine costs 90% of the same",
                        "machine without a face. It only ever saves: a machine short of coolant or with",
                        "nothing to do draws exactly the normal amount, so the 1-in-16 roll is a reward for",
                        "building well and never a tax. Zero turns the hidden bonus off entirely.",
                        "Default: 0.10.")
                .defineInRange("idle_power_saving", 0.10D, 0.0D, 0.25D);
        builder.pop();
        builder.push("fumo_dolls");
        FUMO_CHARGE_AE_PER_TICK = builder
                .comment("AE per tick the wearing TedXenon plushie makes and feeds into the chargeable items",
                        "in the wearer's inventory and armour slots, the held hotbar slot included.",
                        "It is void-made, never drawn from the grid, and nothing is banked while there is",
                        "nothing to charge. Zero switches the charging off; the tooltip line is static text",
                        "either way, because a server config is not readable on a client.",
                        "Default: 100.")
                .defineInRange("charge_ae_per_tick", FUMO_CHARGE_AE_PER_TICK_DEFAULT, 0.0D, 1_000_000.0D);
        builder.pop();
        SPEC = builder.build();
    }

    /** The plushie's rate, floored at zero and read on every sweep so a pack edit takes effect at once. */
    public static double fumoChargeAePerTick() {
        return Math.max(0.0D, FUMO_CHARGE_AE_PER_TICK.get());
    }

    /** The cell's interval, floored: the config range is the guard, this is the read-side one. */
    public static long singularityCellTicksPerBatch() {
        return Math.max(SINGULARITY_CELL_TICKS_PER_BATCH_MIN, SINGULARITY_CELL_TICKS_PER_BATCH.get());
    }

    /** The cell's batch size, floored at one; the top of a long is both its ceiling and its saturation point. */
    public static long singularityCellAmountPerBatch() {
        return Math.min(SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX,
                Math.max(1L, SINGULARITY_CELL_AMOUNT_PER_BATCH.get()));
    }

    /** Shared by the assembler and its JEI page so both agree on what is refused. */
    public static boolean isProcessorRecipeDisabled(net.minecraft.world.item.Item output) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output);
        return DISABLED_PROCESSOR_RECIPES.get().stream().anyMatch(entry -> entry.equals(id.toString()));
    }

    private NeoECOPrototypeServerConfig() {
    }
}

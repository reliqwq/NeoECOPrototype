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
     * network's channel multiplier} per face, so four of them off one hub are a dense trunk AE2 itself charges
     * cable for. The three gates below are the answer we have so far - edges per device, distance, dimensions -
     * and they are tunable rather than fixed, which is why a pack can turn this on and set its own price.
     * <p>Measured against the 19.2.17 jar, AE2 already bills a controllerless network for what a join creates:
     * {@code PathingService} sets the grid's channel drain to {@code channelsByBlocks / 128.0} AE per tick, and
     * on that branch {@code channelsByBlocks = grid.size() x calculateAdHocChannels()}, both of which a merge
     * raises. What AE2 does <em>not</em> charge for is a link that carries capacity but uses no channels, which
     * is the ordinary case on a controller network: with nothing routed over the edge, {@code channelsInUse} is
     * 0 and the edge is free. {@code wireless_ae_per_link_per_tick} is the price for the link itself.
     */
    public static final boolean WIRELESS_LINKS_DEFAULT = false;
    public static final ModConfigSpec.BooleanValue WIRELESS_LINKS;
    /**
     * How many wireless edges one device may carry - the brake on how dense one hub may get, which is the
     * shape a pack is most likely to want to bound. Read live on every bind, never cached.
     */
    public static final ModConfigSpec.IntValue WIRELESS_MAX_LINKS_PER_DEVICE;
    /**
     * How far apart two devices may be to join, in blocks, measured between the two squares. 0 means no
     * distance limit, which is what the feature shipped with. Applied when a link is made; a link already in
     * the world stays, because moving a machine to break a silent rule is worse than the rule.
     */
    public static final ModConfigSpec.IntValue WIRELESS_MAX_RANGE_BLOCKS;
    /**
     * Whether an edge may join devices in different dimensions. Off by default: crossing dimensions is the
     * one thing a cable can never do, so it is the clearest place where a link is not "free cable" but a
     * different machine. Links already recorded across dimensions stay until a player unbinds them.
     */
    public static final ModConfigSpec.BooleanValue WIRELESS_ALLOW_CROSS_DIMENSION;
    /**
     * The energy price of one live link, in AE per tick, billed to the network the link's own end sits on.
     * 0 switches the charge off without switching the feature off.
     * <p>The default is set from two numbers read off the jars, not invented: our smallest L1 component idles
     * at 2.0 AE/t ({@code SimplifyPowerProfile}, {@code 16.0 x 1/8}), and AE2's own rate for channels is
     * 1/128 AE/t per channel per block. 0.5 AE/t per link makes a full hub of four links cost exactly one L1
     * component - visible to a power budget, not a wall.
     * <p>Read live, once per sweep, and the sweep asks {@code IEnergyService.extractAEPower} rather than
     * registering an idle draw: {@code IGridNode} has no setter for idle power (only the impl class
     * {@code appeng.me.GridNode} does), so billing the network is the one route that also works for endpoints
     * owned by other mods. A network that cannot pay keeps its links and says so once in the log - AE2 takes
     * the whole grid offline when its power runs out, so the bill binds without our help.
     */
    public static final ModConfigSpec.DoubleValue WIRELESS_AE_PER_LINK_PER_TICK;
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

    /**
     * AE per tick each coil of a formed superconductive reactor pays. The cube holds 26 of them, so
     * the shipped rate is 26 x 400 + 1000 (the core) = 11,400 AE/t, which covers one L9 storage
     * system's 8,448 idle draw with room to spare.
     */
    public static final double REACTOR_COIL_AE_PER_TICK_DEFAULT = 400.0D;
    public static final ModConfigSpec.DoubleValue REACTOR_COIL_AE_PER_TICK;
    /** AE per tick the single core pays on top of the coils. */
    public static final double REACTOR_CORE_AE_PER_TICK_DEFAULT = 1000.0D;
    public static final ModConfigSpec.DoubleValue REACTOR_CORE_AE_PER_TICK;
    /**
     * Multiplier while the block above the cube can see the sky through a thunderstorm. 1 switches
     * the bonus off, which is the only way to make the weather irrelevant.
     */
    public static final ModConfigSpec.DoubleValue REACTOR_STORM_MULTIPLIER;

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
                        "Off by default in this release. Everything a link costs is configured below - an energy",
                        "price per link, edges per device, distance, and whether it may cross dimensions - and a",
                        "join that would leave the merged network with no channels at all is refused outright.",
                        "Turn this on and set the four numbers to your pack, or leave them: the defaults price a",
                        "full hub of four links at what one L1 machine idles at. Links recorded while it was on",
                        "stay in the world's data and come back when it is switched on again.")
                .define("wireless_links", WIRELESS_LINKS_DEFAULT);
        WIRELESS_MAX_LINKS_PER_DEVICE = builder
                .comment("How many wireless edges one device may carry. Default 4. This is the structural",
                        "limit: a wireless edge costs no blocks, so without it one machine could be clicked",
                        "into the switch for every network in the world.")
                .defineInRange("wireless_max_links_per_device", 4, 1, 32);
        WIRELESS_MAX_RANGE_BLOCKS = builder
                .comment("How far apart two devices may be to join, in blocks, measured between the squares.",
                        "0 = no distance limit (what shipped so far). Applied when a link is made; existing",
                        "links are never broken by lowering this, because a silent teardown after the fact is",
                        "worse than a refusal up front.")
                .defineInRange("wireless_max_range_blocks", 0, 0, 100000);
        WIRELESS_ALLOW_CROSS_DIMENSION = builder
                .comment("Whether a link may join devices in different dimensions. Off by default - crossing",
                        "dimensions is the one thing no cable can do, so it is where a link stops being a free",
                        "cable and becomes its own machine.")
                .define("wireless_allow_cross_dimension", false);
        WIRELESS_AE_PER_LINK_PER_TICK = builder
                .comment("The energy price of one live link, in AE per tick, taken from the network the link's",
                        "own end stands on. 0 = links draw nothing. Default 0.5: a full hub of four links then",
                        "costs 2 AE/t, which is what one of our L1 components idles at. AE2 bills a",
                        "controllerless network for a join by itself (its channel drain is grid size x channels",
                        "used, divided by 128), so this is the price for the link rather than a second charge",
                        "for traffic. A network too short of power keeps its links and the log says so once:",
                        "AE2 drops the whole grid offline when the store runs dry, so the price binds without",
                        "us tearing anything down.")
                .defineInRange("wireless_ae_per_link_per_tick", 0.5D, 0.0D, 1024.0D);
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
        builder.push("superconductive_reactor");
        REACTOR_COIL_AE_PER_TICK = builder
                .comment("AE per tick each coil of a formed superconductive reactor pays. A complete cube",
                        "holds 26 coils. Default 400, so the shipped cube pays 26 x 400 + 1000 = 11,400",
                        "AE/t, against the 8,448 AE/t an L9 storage system idles at.")
                .defineInRange("coil_ae_per_tick", REACTOR_COIL_AE_PER_TICK_DEFAULT, 0.0D, 100_000.0D);
        REACTOR_CORE_AE_PER_TICK = builder
                .comment("AE per tick the single core pays on top of the coils. Default 1000.")
                .defineInRange("core_ae_per_tick", REACTOR_CORE_AE_PER_TICK_DEFAULT, 0.0D, 100_000.0D);
        REACTOR_STORM_MULTIPLIER = builder
                .comment("Multiplier while the block above the cube can see the sky and the level is",
                        "thundering. 1 turns the weather bonus off. Default 2.")
                .defineInRange("storm_multiplier", 2.0D, 1.0D, 16.0D);
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

    /** The reactor's per-coil rate, floored; read on every {@code getRate()} so a pack edit is live. */
    public static double reactorCoilAePerTick() {
        return Math.max(0.0D, REACTOR_COIL_AE_PER_TICK.get());
    }

    /** The reactor's core rate, floored; read on every {@code getRate()} so a pack edit is live. */
    public static double reactorCoreAePerTick() {
        return Math.max(0.0D, REACTOR_CORE_AE_PER_TICK.get());
    }

    /** The storm multiplier, never below one: weather may only ever add power to a formed reactor. */
    public static double reactorStormMultiplier() {
        return Math.max(1.0D, REACTOR_STORM_MULTIPLIER.get());
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

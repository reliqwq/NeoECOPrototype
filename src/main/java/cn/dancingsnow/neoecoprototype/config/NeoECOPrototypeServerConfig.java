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
    /** The same two figures for the meteorite-dust member, separately tunable. */
    public static final ModConfigSpec.LongValue SKY_STONE_DUST_CELL_TICKS_PER_BATCH;
    public static final ModConfigSpec.LongValue SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH;
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

    /**
     * Whether a migration drive hands the blocks it replaced back as one backup drive. Off by default: the
     * drive is the whole price, and the L1 parts it converts are what that price buys, so returning them as
     * well would refund the machine and sell the next tier at the same time. What the machine holds is not
     * part of this - a mounted cell and stored patterns always move with it.
     */
    public static final boolean MIGRATION_DRIVE_REFUND_BLOCKS_DEFAULT = false;
    public static final ModConfigSpec.BooleanValue MIGRATION_DRIVE_REFUND_BLOCKS;

    /**
     * Reads a key the way a client that has no server-side copy of this addon must.
     *
     * <p>{@code get()} throws {@code IllegalStateException: Cannot get config value before config is loaded}
     * while the spec has no loaded handle, and NeoForge only fills that handle when the file is read on the
     * server side or, for a client, when the server's {@code ConfigSync} configuration task sends its copy
     * over. Joining a server without this addon sends nothing, so every client-side path that asks a key
     * here - item use, tooltip, block callbacks, the recipe viewers - would throw for a player who is only
     * holding an item or walking into a chunk. Answering the shipped default keeps the live-read rule
     * (AGENTS §7: never cache a config value in a field) intact and states the truth about a config nobody
     * handed us: we do not have it.</p>
     */
    public static <T> T current(ModConfigSpec.ConfigValue<T> value) {
        return SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    static {
        ModConfigSpec.Builder builder = new ModConfigSpec.Builder();
        builder.push("l1_storage");
        L1_ADDITIONAL_STORAGE_CELLS = builder
                .comment("Additional eco storage-cell IDs an L1 drive may mount regardless of tier.",
                        "Default: []. L1-native cells need no entry, e.g. [\"neoecoae:eco_mega_long_bulk_cell\"].",
                        "额外允许 L1 驱动器挂载的 eco 存储单元 ID（无视档位限制）。默认 []。",
                        "L1 原生单元无需填写，示例：[\"neoecoae:eco_mega_long_bulk_cell\"]")
                .defineListAllowEmpty("additional_storage_cells", List::of, value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        builder.pop();
        STRUCTURE_CARD_CONSUMES_ITEMS = builder
                .comment("Whether the structure card consumes 1 paper and 1 blank pattern per click.",
                        "Default: true. Off, neither is consumed; a written exchange pattern stays a real recipe.",
                        "结构扩容卡点击时是否消耗 1 张纸与 1 张空白样板。默认 true。",
                        "关掉后两者都不消耗，但已写出的交换样板仍是留在世界里的真实配方。")
                .define("structure_card_consumes_items", STRUCTURE_CARD_CONSUMES_ITEMS_DEFAULT);
        WIRELESS_LINKS = builder
                .comment("Master switch for wireless ME links (the two cards, rebuild and wake).",
                        "Default: false. Off only disables the links; recorded links stay in the save.",
                        "无线 ME 链路系统总开关（含两张卡、重建与唤醒）。默认 false。",
                        "关闭仅停用功能，已记录的链路数据仍保留在存档中。")
                .define("wireless_links", WIRELESS_LINKS_DEFAULT);
        WIRELESS_MAX_LINKS_PER_DEVICE = builder
                .comment("Max wireless edges per device. Default: 8 | Range: 1~32.",
                        "Note: on a network without an AE2 controller the actual ceiling is 7.",
                        "单台设备最大承载的无线链路数。默认 8 | 范围 1~32。",
                        "注：无 AE2 控制器时，实际可用上限为 7 条。")
                .defineInRange("wireless_max_links_per_device", 8, 1, 32);
        WIRELESS_MAX_RANGE_BLOCKS = builder
                .comment("Max distance between two devices, in blocks between the squares. Default: 0 (no limit) | Range: 0~100000.",
                        "Note: applied on bind; lowering it will not break existing links.",
                        "建立连接的最大距离（格对格）。默认 0（不限）| 范围 0~100000。",
                        "注：仅对新建链路生效，调小不会断开已有链路。")
                .defineInRange("wireless_max_range_blocks", 0, 0, 100000);
        WIRELESS_ALLOW_CROSS_DIMENSION = builder
                .comment("Whether links may join devices in different dimensions. Default: false.",
                        "Note: links already joined across dimensions stay until a player unbinds them.",
                        "是否允许跨维度建立无线链路。默认 false。",
                        "注：已跨维度连上的链路不受开关影响，会保留到玩家主动解除。")
                .define("wireless_allow_cross_dimension", false);
        WIRELESS_AE_PER_LINK_PER_TICK = builder
                .comment("AE/t one live link draws from the network at its own end. Default: 0.5 (8 links = 4 AE/t) | Range: 0~1024. 0 = free.",
                        "Note: charged live; a network that cannot pay keeps its links but stalls.",
                        "每条在线链路每刻消耗的网络 AE。默认 0.5（8 条 = 4 AE/t）| 范围 0~1024。0 = 免费。",
                        "注：每次扫描实时扣费；付不起的网络链路保留，但会停机。")
                .defineInRange("wireless_ae_per_link_per_tick", 0.5D, 0.0D, 1024.0D);
        DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER = builder
                .comment("Derive processor recipes from AE2 inscriber recipes. Default: true.",
                        "Note: with this on, deleting a data file alone does not disable a recipe; use the blacklist below.",
                        "是否接受从 AE2 压印器推导的处理器配方。默认 true。",
                        "注：开启时仅删数据文件无法禁用配方，请用下方的黑名单。")
                .define("derive_processor_recipes_from_inscriber", true);
        builder.push("l1_computation");
        L1_CPU_THREADS = builder
                .comment("Crafting threads per L1 threading core. Default: 2 | Range: 1~1024.",
                        "Note: a placed core must be replaced to pick up the new value.",
                        "每只 L1 线程核心提供的合成线程数。默认 2 | 范围 1~1024。",
                        "注：已放置的核心需拆除重放才会应用新值。")
                .defineInRange("cpu_threads", 2, 1, 1_024);
        L1_CPU_ACCELERATORS = builder
                .comment("Co-processors per L1 parallel core (the panel shows it as 并行). Default: 24 | Range: 1~65536.",
                        "Note: summed across the cluster, with no design cap.",
                        "每只 L1 并行核心提供的协处理器数（面板显示的并行）。默认 24 | 范围 1~65536。",
                        "注：集群按累加算，没有设计上限。")
                .defineInRange("cpu_accelerators", 24, 1, 65_536);
        L1_CPU_TOTAL_BYTES = builder
                .comment("Crafting storage bytes per L1 computation cell. Default: 1572864 | Range: 1~1099511627776.",
                        "Note: raising it leaves stored cells valid.",
                        "每只 L1 计算单元提供的合成存储字节数。默认 1572864 | 范围 1~1099511627776。",
                        "注：调大不影响已经存了东西的单元。")
                .defineInRange("cpu_total_bytes", 1_572_864L, 1L, 1L << 40);
        ENERGIZED_CELL_TOTAL_BYTES = builder
                .comment("Crafting storage bytes per energized computation cell (CE1R). Default: 5242880 | Range: same as above.",
                        "Note: raising it leaves stored cells valid.",
                        "每只盈能计算单元（CE1R）提供的合成存储字节数。默认 5242880 | 范围同上。",
                        "注：调大不影响已经存了东西的单元。")
                .defineInRange("energized_cell_total_bytes", 5_242_880L, 1L, 1L << 40);
        builder.pop();
        DISABLED_PROCESSOR_RECIPES = builder
                .comment("Processor outputs the assembler must refuse, e.g. [\"ae2:logic_processor\"]. Default: [].",
                        "Note: applies to the JSON recipes and the inscriber derivation alike.",
                        "必须禁用的处理器配方输出 ID，例如 [\"ae2:logic_processor\"]。默认 []。",
                        "注：对 JSON 配方与压印器推导都生效。")
                .defineListAllowEmpty("disabled_processor_recipes",
                        List.of(), value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        builder.push("cryotheum_ore");
        CRYOTHEUM_ORE_DIMENSIONS = builder
                .comment("Dimensions the cryotheum ores generate in, by full ID. Default: [] (all off).",
                        "Note: each entry switches one ore on; changing the host stone needs a datapack.",
                        "极寒晶矿生成的维度完整 ID，加一条只开那一种。默认 []（全部关闭）。",
                        "注：更换主岩需要数据包，此配置无法做到。")
                .defineListAllowEmpty("dimensions",
                        () -> CRYOTHEUM_ORE_DIMENSIONS_DEFAULT,
                        value -> value instanceof String id
                        && net.minecraft.resources.ResourceLocation.tryParse(id) != null);
        CRYOTHEUM_ORE_FREEZE_TICKS = builder
                .comment("Freeze ticks per broken cryotheum ore. Default: 60 | Range: 0~600. 0 = off.",
                        "Note: vanilla starts taking freeze damage at 140 ticks, so below that it only fills the meter.",
                        "每挖掉一块极寒晶矿获得的冰冻刻数。默认 60 | 范围 0~600。0 = 关闭。",
                        "注：原版需 140 刻才开始扣血，低于此值仅涨霜冻条。")
                .defineInRange("freeze_ticks", 60, 0, 600);
        builder.pop();
        builder.push("cryotheum_meteorite");
        CRYOTHEUM_METEORITE_ENABLED = builder
                .comment("Whether the floating End comet generates. Default: false.",
                        "Note: read while chunks generate, so placed comets stay.",
                        "末地漂浮陨石是否生成。默认 false。",
                        "注：在区块生成时决定，关闭不影响已生成的陨石。")
                .define("enabled", CRYOTHEUM_METEORITE_ENABLED_DEFAULT);
        builder.pop();
        builder.push("superconductive_reactor");
        REACTOR_COIL_AE_PER_TICK = builder
                .comment("AE/t per coil of a formed superconductive reactor (a full cube has 26). Default: 400 | Range: 0~100000.",
                        "成型超导反应堆中，每根线圈每刻产出的 AE（满堆 26 根）。默认 400 | 范围 0~100000。")
                .defineInRange("coil_ae_per_tick", REACTOR_COIL_AE_PER_TICK_DEFAULT, 0.0D, 100_000.0D);
        REACTOR_CORE_AE_PER_TICK = builder
                .comment("Extra AE/t generated by the core. Default: 1000 | Range: 0~100000.",
                        "反应堆核心额外产出的 AE/刻。默认 1000 | 范围 0~100000。")
                .defineInRange("core_ae_per_tick", REACTOR_CORE_AE_PER_TICK_DEFAULT, 0.0D, 100_000.0D);
        REACTOR_STORM_MULTIPLIER = builder
                .comment("Output multiplier during a thunderstorm while the block above sees the sky. Default: 2 | Range: 1~16. 1 = off.",
                        "雷雨且上方见天时的产出倍率。默认 2 | 范围 1~16。1 = 关闭。")
                .defineInRange("storm_multiplier", 2.0D, 1.0D, 16.0D);
        builder.pop();
        builder.push("singularity_cell");
        SINGULARITY_CELL_TICKS_PER_BATCH = builder
                .comment("Ticks per batch. Default: 600 | Range: 60~16777216.",
                        "奇点单元产出间隔（刻）。默认 600 | 范围 60~16777216。")
                .defineInRange("ticks_per_batch", 600L, SINGULARITY_CELL_TICKS_PER_BATCH_MIN, 1L << 24);
        SINGULARITY_CELL_AMOUNT_PER_BATCH = builder
                .comment("Singularities per batch. Default: 120 | Range: 1~Long.MAX.",
                        "每批产出的奇点数量。默认 120 | 范围 1~Long.MAX。")
                .defineInRange("amount_per_batch", 120L, 1L, SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("sky_stone_dust_cell");
        SKY_STONE_DUST_CELL_TICKS_PER_BATCH = builder
                .comment("Ticks per batch for the meteorite-dust cell. Default: 600 | Range: 60~16777216.",
                        "Note: independent from the singularity cell.",
                        "陨石粉单元产出间隔（刻）。默认 600 | 范围 60~16777216。",
                        "注：与奇点单元配置相互独立。")
                .defineInRange("ticks_per_batch", 600L, SINGULARITY_CELL_TICKS_PER_BATCH_MIN, 1L << 24);
        SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH = builder
                .comment("Dust per batch. Default: 120 | Range: 1~Long.MAX.",
                        "每批产出的陨石粉数量。默认 120 | 范围 1~Long.MAX。")
                .defineInRange("amount_per_batch", 120L, 1L, SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX);
        builder.pop();
        builder.push("machine_mind");
        MIND_IDLE_POWER_SAVING = builder
                .comment("Fraction of idle draw the machine mind saves. Default: 0.1 | Range: 0~0.25. 0 = off.",
                        "Note: it only ever saves power, never costs more.",
                        "“机魂”能够节省的待机功耗比例。默认 0.1 | 范围 0~0.25。0 = 关闭。",
                        "注：只会省电，不会费电。")
                .defineInRange("idle_power_saving", 0.10D, 0.0D, 0.25D);
        builder.pop();
        builder.push("fumo_dolls");
        FUMO_CHARGE_AE_PER_TICK = builder
                .comment("AE/t the worn plushie charges items in the inventory and armour slots. Default: 100 | Range: 0~1000000. 0 = off.",
                        "Note: the power is void-made, never drawn from the grid.",
                        "穿戴中的玩偶每刻为背包物品充能的 AE 速度。默认 100 | 范围 0~1000000。0 = 关闭。",
                        "注：电量凭空产生，不从电网抽取。")
                .defineInRange("charge_ae_per_tick", FUMO_CHARGE_AE_PER_TICK_DEFAULT, 0.0D, 1_000_000.0D);
        builder.pop();
        builder.push("migration_drive");
        MIGRATION_DRIVE_REFUND_BLOCKS = builder
                .comment("Whether a drive packs replaced blocks into one backup drive. Default: false.",
                        "Note: a mounted cell and stored patterns are never affected by this switch.",
                        "升格迁移驱动是否将换下的旧方块装进“备份驱动”返还。默认 false。",
                        "注：机器内部的存储矩阵与样板无论开关均会保留。")
                .define("refund_replaced_blocks", MIGRATION_DRIVE_REFUND_BLOCKS_DEFAULT);
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

    /** The meteorite-dust member's interval, on the same floor as the singularity's. */
    public static long skyStoneDustCellTicksPerBatch() {
        return Math.max(SINGULARITY_CELL_TICKS_PER_BATCH_MIN, SKY_STONE_DUST_CELL_TICKS_PER_BATCH.get());
    }

    /** The meteorite-dust member's batch size, on the same ceiling as the singularity's. */
    public static long skyStoneDustCellAmountPerBatch() {
        return Math.min(SINGULARITY_CELL_AMOUNT_PER_BATCH_MAX,
                Math.max(1L, SKY_STONE_DUST_CELL_AMOUNT_PER_BATCH.get()));
    }

    /** Shared by the assembler and its JEI page so both agree on what is refused. */
    public static boolean isProcessorRecipeDisabled(net.minecraft.world.item.Item output) {
        var id = net.minecraft.core.registries.BuiltInRegistries.ITEM.getKey(output);
        return DISABLED_PROCESSOR_RECIPES.get().stream().anyMatch(entry -> entry.equals(id.toString()));
    }

    private NeoECOPrototypeServerConfig() {
    }
}

package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoae.api.IECOTier;
import net.minecraft.resources.ResourceLocation;

/** L1 crafting-only tier; its parallelism is independent from computation L1. */
public enum SimplifyCraftingTier implements IECOTier {
    /**
     * 16 parallel per parallel core, with the 32 overclock bonus left exactly where eco put it.
     *
     * <p>How eco reads these two: {@code ECOCraftingSystemBlockEntity.getCoreThreadCountLong} sums
     * {@code max(crafterParallel,0)} over the machine's parallel cores and, while the network's overclock
     * switch is on, {@code saturatingAdd}s {@code max(overclockedCrafterParallel,0)} on top - it adds, it
     * does not replace. At the default maximum length the frame holds 11 workers and 22 parallel cores, so
     * its pool is 352 normally and 1056 switched. The switch itself is eco's, not ours: it lives on the
     * network cluster, the leader host's setting is copied to every host on it, and eco refuses to move it
     * while a job is running.</p>
     *
     * <p>Per-core this is still well under eco's own ladder - measured from 21.2.15-beta's {@code ECOTier}
     * constants: L4 is 24 + 32, L6 is 72 + 96, L9 is 256 + 384 - so an L1 host is not cheap because the
     * number is small; it stays modest because of how few cores the shape accepts. The last two columns of
     * that ladder also carry the cpuThreads eco needs to feed the parallelism (4 / 16 / 64), which is the
     * axis an L1 host does not compete on at all (this tier leaves threads at 0).</p>
     *
     * <p>Two more facts from the same jar, so nobody re-derives them: {@code MAX_OVERCLOCK_TIMES = 9} comes
     * with a {@code calculateOverclockTimes(long,long)} that awards one step per 5% of idle capacity, and
     * neither has a call site anywhere in 21.2.15-beta; {@code getParallelCoreMultiplier} returns a constant
     * 1. If that automatic path is ever connected, base + bonus is what decides how loud an L1 host gets.</p>
     *
     * <p>What those two numbers actually buy is one rung of eco's batch clock, and the rule is a single
     * ratio: pool per worker divided by batch per worker - 2 x 48 / 64 = 1.5 for us - which lands on overflow
     * rung 7, i.e. 80 progress per tick, so 2 ticks per batch uncooled and 4 ticks on water, because active
     * cooling takes the minimum of that rung and the coolant's own {@code max_overclock} (water 2, sodium 6,
     * cryotheum 9). Rungs 4 through 8 all read as 2 ticks, so this ratio is a coarse dial. Both halves are
     * re-runnable as the {@code crafting_batch_ticks} probe, and the reason eco's own L4 sits at 10 ticks per
     * batch instead of overclocking at all is upstream issue #152.</p>
     */
    L1(0, 16, 32, 0, 0, 0, 0, 0);

    private final int tier;
    private final int crafterParallel;
    private final int overclockedCrafterParallel;
    private final int cpuAccelerators;
    private final int cpuThreads;
    private final long cpuTotalBytes;
    private final long storageTotalBytes;
    private final long powerStorageSize;

    SimplifyCraftingTier(int tier, int crafterParallel, int overclockedCrafterParallel,
                         int cpuAccelerators, int cpuThreads, long cpuTotalBytes,
                         long storageTotalBytes, long powerStorageSize) {
        this.tier = tier;
        this.crafterParallel = crafterParallel;
        this.overclockedCrafterParallel = overclockedCrafterParallel;
        this.cpuAccelerators = cpuAccelerators;
        this.cpuThreads = cpuThreads;
        this.cpuTotalBytes = cpuTotalBytes;
        this.storageTotalBytes = storageTotalBytes;
        this.powerStorageSize = powerStorageSize;
    }

    @Override
    public int getTier() {
        return tier;
    }

    /**
     * The L1 idle baseline, asked in two places by eco: the COMPONENT pass-through that
     * {@code NEBlockEntity#onReady} finishes with, and the base its own host classes hand to
     * {@code applyIdlePower} for their controller role. Leaving this at the interface's 16.0 would make
     * an F1 frame idle at eco's L4 number the moment its host class re-applies after onReady.
     */
    @Override
    public double getComponentIdlePower() {
        return SimplifyPowerProfile.L1.baseComponentIdlePower();
    }

    @Override
    public int getCrafterParallel() {
        return crafterParallel;
    }

    @Override
    public int getOverclockedCrafterParallel() {
        return overclockedCrafterParallel;
    }

    @Override
    public int getCPUAccelerators() {
        return cpuAccelerators;
    }

    @Override
    public int getCPUThreads() {
        return cpuThreads;
    }

    @Override
    public long getCPUTotalBytes() {
        return cpuTotalBytes;
    }

    @Override
    public long getStorageTotalBytes() {
        return storageTotalBytes;
    }

    @Override
    public long getPowerStorageSize() {
        return powerStorageSize;
    }

    /**
     * Shares the badge with {@link SimplifyTier}: eco resolves this path at runtime, so no static
     * search sees a reference and deleting the file breaks the GUI overlay only in game.
     */
    @Override
    public ResourceLocation getCPUOverlayTexture() {
        return ResourceLocation.fromNamespaceAndPath("neoecoprototype", "textures/gui/tier/l1.png");
    }

    /** Same reason as {@link SimplifyTier#getPanelTierName()}: the tier index is 0, and "L0" is not a tier. */
    @Override
    public net.minecraft.network.chat.Component getPanelTierName() {
        return net.minecraft.network.chat.Component.literal("L1");
    }

    /** Same brackets as {@link SimplifyTier#getHostMarkerTexture()}; eco asks each tier separately. */
    @Override
    public net.minecraft.resources.ResourceLocation getHostMarkerTexture() {
        return ResourceLocation.fromNamespaceAndPath("neoecoprototype",
                "textures/gui/panel/host_marker.png");
    }

    /** Same six sheets as {@link SimplifyTier#getPanelAtlas(String)}; the crafting panel asks its own tier. */
    @Override
    @org.jetbrains.annotations.Nullable
    public ResourceLocation getPanelAtlas(String atlas) {
        return PanelAtlas.resolve(atlas);
    }
}

package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import net.minecraft.resources.ResourceLocation;

/**
 * Tier definition of the "1 级" storage subsystem.
 *
 * <p>eco's own tiers are L4/L6/L9 whose internal tier indexes are 1/2/3
 * (see {@code cn.dancingsnow.neoecoae.api.ECOTier}). This addon adds the
 * lower tier L1 (internal index 0):
 * <ul>
 *   <li>per storage cell bytes : 1 MiB (1 &lt;&lt; 20)</li>
 *   <li>per energy cell       : 100 000 AE</li>
 * </ul>
 * Because {@code IECOTier#compareTo} compares {@link #getTier()}, an L1 host
 * (index 0) accepts only cells whose tier index is &le; 0 (i.e. our own L1
 * cells), while eco's L4+ hosts (index &ge; 1) keep accepting our L1 cells -
 * the intended upgrade path.
 */
public enum SimplifyTier implements IECOTier {
    L1(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 16, 1),
    /**
     * The tier behind the energized computation cell: same tier index, so an L1 frame accepts it
     * wherever it accepts a plain L1 cell. Its storage bytes are the 4 MiB it shipped with plus 30%,
     * and a server may retune them; its thread and co-processor numbers are never read, because no
     * member of this tier is a threading or parallel core -- those numbers only matter to cells
     * through {@link #getCPUTotalBytes()}.
     */
    L1_REINFORCED(0, 1L << 20, 100_000L, 5_242_880L, 1, 1, 128, 4),
    /**
     * The energized threading core: sixteen real threads, because eco allocates one
     * {@code ECOCraftingCPU} per thread in the core's constructor, so this is the only way to raise
     * concurrency without inflating the panel number. It is deliberately not given extra
     * co-processors or cell bytes -- those belong to the other members.
     */
    L1_ENERGIZED_THREADING(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 16, 16),
    /**
     * The shell-mounted parallel member: it replaces a casing block, so only
     * {@link #getCPUAccelerators()} matters -- eco sums it over the parallel cores it finds in the
     * bounding box, and {@code getPooledParallelism()} is what {@code ECOCraftingCPU} and AE2's
     * crafting service actually read, so the 1024 is work rather than a displayed number. Tier index
     * stays 0 so an L1 host accepts it; threads, cell bytes and storage stay at L1's because this
     * member is never a threading core or a drive.
     */
    L1_PARALLEL_SWITCH(0, 1L << 20, 100_000L, 1L << 20, 1, 1, 1024, 1);

    private final int tier;
    private final long storageTotalBytes;
    private final long powerStorageSize;
    private final long cpuTotalBytes;
    private final int crafterParallel;
    private final int overclockedCrafterParallel;
    private final int cpuAccelerators;
    private final int cpuThreads;

    SimplifyTier(int tier, long storageTotalBytes, long powerStorageSize, long cpuTotalBytes,
                 int crafterParallel, int overclockedCrafterParallel, int cpuAccelerators, int cpuThreads) {
        this.tier = tier;
        this.storageTotalBytes = storageTotalBytes;
        this.powerStorageSize = powerStorageSize;
        this.cpuTotalBytes = cpuTotalBytes;
        this.crafterParallel = crafterParallel;
        this.overclockedCrafterParallel = overclockedCrafterParallel;
        this.cpuAccelerators = cpuAccelerators;
        this.cpuThreads = cpuThreads;
    }

    @Override
    public int getTier() {
        return tier;
    }

    /**
     * The number eco asks its own host classes for: {@code ECOComputationSystemBlockEntity#onReady} finishes
     * with {@code applyIdlePower(COMPUTATION_CONTROLLER, tier.getComponentIdlePower(), 0)}, so leaving this at
     * the interface's 16.0 would have the C1 frame idle at eco's L4 baseline. This is the same figure the
     * onReady injection used to write to the node by hand - see {@code NeoECOPrototype#commonSetup} for the
     * provider that covers the machines which report no tier at all.
     */
    @Override
    public double getComponentIdlePower() {
        return SimplifyPowerProfile.L1.baseComponentIdlePower();
    }

    // L1 computation capacity is one eighth of the previous 8 MiB setting.

    /**
     * L1's computation numbers are the ones a server may retune. {@link #L1_REINFORCED} is a different
     * member, so only its storage bytes follow the config: its threads and co-processors stay fixed.
     */
    private boolean isConfigurable() {
        return this == L1;
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
        return isConfigurable() ? NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.L1_CPU_ACCELERATORS) : cpuAccelerators;
    }

    @Override
    public int getCPUThreads() {
        return isConfigurable() ? NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.L1_CPU_THREADS) : cpuThreads;
    }

    @Override
    public long getCPUTotalBytes() {
        if (this == L1) {
            return NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.L1_CPU_TOTAL_BYTES);
        }
        if (this == L1_REINFORCED) {
            return NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.ENERGIZED_CELL_TOTAL_BYTES);
        }
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
     * eco paints this badge over its computation GUIs. Nothing in code references the file itself -
     * the path is only resolved at runtime - so a static "unused" search will call it dead and
     * deleting it breaks the overlay silently, visible only in game.
     */
    @Override
    public ResourceLocation getCPUOverlayTexture() {
        return ResourceLocation.fromNamespaceAndPath("neoecoprototype", "textures/gui/tier/l1.png");
    }

    /**
     * eco's own default spells the tier number, and our tier index is 0, so without this the host panel
     * labels every L1 cell "L0" - a tier that does not exist.
     *
     * <p>Who reads it, from a throwaway stack-trace log rather than from inference: the computation badge
     * ({@code ComputationControllerArtwork.drawBackgroundAdditional}) and the crafting badge
     * ({@code CraftingControllerArtwork.drawTier}) both reach it through {@code HostPanelTier.drawName}, and
     * the storage cell list reaches it through {@code StorageCellGrid.tierName} on hover. The storage badge
     * never does, and handing a tier to {@code StorageHostUI.create} would not change that - the
     * {@code storage_tier} widget returns as soon as {@code IHostPanelTextureProvider} has given it a
     * texture, so our {@code STORAGE_TIER} art outranks the text. Colour is always the caller's, never
     * ours: the two badges above carry our "L1" painted in eco's grey-blue.</p>
     */
    @Override
    public net.minecraft.network.chat.Component getPanelTierName() {
        return net.minecraft.network.chat.Component.literal("L1");
    }

    /**
     * The brackets eco paints above and below the host icon. It used to ask only the tier, which forced one
     * file to answer every machine; since 21.2.9 each host block answers its own {@code *_MARKER} part and
     * {@code HostPanelTier.drawMarker} is consulted only when the block stayed silent - so this is the
     * fallback now, kept because a tier is also read by panels that have no block to ask. eco reads it at
     * 13x8 twice: the lower half is the idle bracket, the upper half the lit one, which is why the file is
     * 13x16. {@code ECOTier} does not override this method, so eco's own panels keep their painted markers.
     */
    @Override
    public net.minecraft.resources.ResourceLocation getHostMarkerTexture() {
        return net.minecraft.resources.ResourceLocation.fromNamespaceAndPath(
                "neoecoprototype", "textures/gui/panel/host_marker.png");
    }

    /**
     * The whole panel sheet, asked by name. Each answer is a full-size copy of eco's own sheet with our L1
     * columns drawn over it, and it is the only channel that reaches the cells cut by tier number - the
     * queue strip's bracket pairs and the gauge ticks are read out of the sheet at a column eco only painted
     * for its own L4/L6/L9, so on our machine they land on blank pixels no per-cell ask can cover.
     * Names we do not have answer null, which is eco's own sheet unchanged.
     */
    @Override
    @org.jetbrains.annotations.Nullable
    public net.minecraft.resources.ResourceLocation getPanelAtlas(String atlas) {
        return PanelAtlas.resolve(atlas);
    }
}

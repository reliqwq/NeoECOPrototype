package cn.dancingsnow.neoecoprototype.api;

import cn.dancingsnow.neoecoae.api.IECOTier;
import net.minecraft.resources.ResourceLocation;

/** L1 crafting-only tier; its parallelism is independent from computation L1. */
public enum SimplifyCraftingTier implements IECOTier {
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

    @Override
    public ResourceLocation getCPUOverlayTexture() {
        return ResourceLocation.fromNamespaceAndPath("neoecoprototype", "textures/gui/tier/l1.png");
    }
}

package cn.dancingsnow.neoecoprototype.client;

import net.minecraft.client.renderer.chunk.RenderChunkRegion;

/** Uses the same flattened cache index as RenderChunkRegion, including its aliasing behavior. */
public final class RenderRegionCacheBounds {
    private RenderRegionCacheBounds() {
    }

    public static boolean isOutside(int minChunkX, int minChunkZ, int blockX, int blockZ) {
        int index = RenderChunkRegion.index(minChunkX, minChunkZ, blockX >> 4, blockZ >> 4);
        return index < 0 || index >= RenderChunkRegion.SIZE * RenderChunkRegion.SIZE;
    }
}

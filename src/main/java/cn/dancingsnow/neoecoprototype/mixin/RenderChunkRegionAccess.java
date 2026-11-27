package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251. RenderChunkRegion still stores its 3x3 cache origin in these fields. */

import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(RenderChunkRegion.class)
public interface RenderChunkRegionAccess {
    @Accessor("minChunkX")
    int neoecoprototype$getMinChunkX();

    @Accessor("minChunkZ")
    int neoecoprototype$getMinChunkZ();
}

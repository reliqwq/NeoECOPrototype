package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251, eco 21.3.0. Re-checked against that jar on 2026-11-27:
 * FixedBlockEntityRenderers.render(AddSectionGeometryEvent$SectionRenderingContext, BlockPos) still
 * asks BlockAndTintGetter.getBlockEntity exactly once, at offset 59, while scanning origin..origin+15.
 * RenderChunkRegion.getBlockEntity indexes its 3x3 horizontal chunk cache; Y/build height is not part
 * of that index. The normal path below checks the real cache origin through RenderChunkRegionAccess and
 * returns before the lookup can throw. The exception catch remains only for non-RenderChunkRegion
 * implementations supplied by other mods.
 * This class sits in the config's client array, so runGameTestServer never applies it; a client launch
 * with Mixin readouts is still required. Retire it when eco adds its own bounds check at offset 59.
 * Earlier checks: 21.2.15-beta on 2026-11-04, 21.2.11 on 2026-10-27, 21.2.10 on 2026-10-25, 21.2.9 on
 * 2026-10-22, 21.2.1-beta2 on 2026-10-06, the 21.2.1 release on 2026-10-08. */

import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
import cn.dancingsnow.neoecoprototype.client.RenderRegionCacheBounds;
import net.minecraft.client.renderer.chunk.RenderChunkRegion;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/** Guards eco's section scan against positions outside RenderChunkRegion's cache. */
@Mixin(FixedBlockEntityRenderers.class)
public final class FixedBlockEntityRenderersMixin {
    @Redirect(
            method = "render(Lnet/neoforged/neoforge/client/event/AddSectionGeometryEvent$SectionRenderingContext;Lnet/minecraft/core/BlockPos;)V",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/world/level/BlockAndTintGetter;getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;"
            )
    )
    private static BlockEntity neoecoprototype$guardRegionLookup(BlockAndTintGetter region, BlockPos pos) {
        if (region instanceof RenderChunkRegion renderRegion) {
            var access = (RenderChunkRegionAccess) renderRegion;
            if (RenderRegionCacheBounds.isOutside(access.neoecoprototype$getMinChunkX(),
                    access.neoecoprototype$getMinChunkZ(), pos.getX(), pos.getZ())) {
                return null;
            }
            return renderRegion.getBlockEntity(pos);
        }
        try {
            return region.getBlockEntity(pos);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return null;
        }
    }
}

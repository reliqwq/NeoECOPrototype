package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.251, eco 21.2.11. Re-checked against that jar on 2026-10-27:
 * FixedBlockEntityRenderers.render(AddSectionGeometryEvent$SectionRenderingContext, BlockPos) is still
 * the descriptor below and still asks BlockAndTintGetter.getBlockEntity inside it (offset 59) with no
 * bounds check - no withinRange, no isAnyOfSectionsLoaded - which is why the guard below is needed.
 * The class is eco's, not NeoForge's - only the parameter type comes from NeoForge.
 * Earlier checks: 21.2.10 on 2026-10-25, 21.2.9 on 2026-10-22, 21.2.1-beta2 on 2026-10-06, the 21.2.1 release on 2026-10-08. */

import cn.dancingsnow.neoecoae.client.rendering.FixedBlockEntityRenderers;
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
        try {
            return region.getBlockEntity(pos);
        } catch (ArrayIndexOutOfBoundsException ignored) {
            return null;
        }
    }
}

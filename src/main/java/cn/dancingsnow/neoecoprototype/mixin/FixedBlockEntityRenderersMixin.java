package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, Eco 21.2.0-preview12. Check render method descriptor and lookup call. */

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

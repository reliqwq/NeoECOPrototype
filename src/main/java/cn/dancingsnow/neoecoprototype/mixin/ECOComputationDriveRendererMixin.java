package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: eco 21.2.1. Check that renderFixed still asks ECOComputationModels for one
 * connected and two disconnected cable models, and that SimplifyComputationDriveRenderer overrides
 * renderFixed(ECOComputationDriveBlockEntity, ...) -- that override is what makes "our tier was asked
 * for" mean "the host is eco's". */

import cn.dancingsnow.neoecoae.api.ECOComputationModels;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoprototype.api.SimplifyTier;
import cn.dancingsnow.neoecoprototype.client.ComputationCableModels;
import net.minecraft.resources.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Wears the glowing computation cable in someone else's frame.
 *
 * <p>The split Ted asked for is by host, not by cell: our own frame shows the plain pair, an eco frame
 * shows the {@code _normal} pair. eco cannot express that through its registry, because its drive
 * renderer replaces the lookup key with the inserted cell's tier as soon as the cell is accepted - so
 * our cells resolve to our plain entries no matter how high the host is. The redirects below answer
 * for our tiers only, and reaching this method at all already means the host is eco's: our drive has
 * its own {@code renderFixed}, which this mixin cannot see.
 */
@Mixin(cn.dancingsnow.neoecoae.client.renderer.blockentity.ECOComputationDriveRenderer.class)
public final class ECOComputationDriveRendererMixin {
    @Redirect(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE",
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableConnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation neoecoprototype$connected(IECOTier tier) {
        var upper = neoecoprototype$upperHostCable(tier, false);
        return upper != null ? upper : ECOComputationModels.getCableConnectedModel(tier);
    }

    @Redirect(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", ordinal = 0,
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableDisconnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation neoecoprototype$disconnectedFirst(IECOTier tier) {
        var upper = neoecoprototype$upperHostCable(tier, true);
        return upper != null ? upper : ECOComputationModels.getCableDisconnectedModel(tier);
    }

    @Redirect(method = "renderFixed(Lcn/dancingsnow/neoecoae/blocks/entity/computation/ECOComputationDriveBlockEntity;"
            + "FLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;II)V",
            at = @At(value = "INVOKE", ordinal = 1,
                    target = "Lcn/dancingsnow/neoecoae/api/ECOComputationModels;getCableDisconnectedModel("
                            + "Lcn/dancingsnow/neoecoae/api/IECOTier;)Lnet/minecraft/resources/ResourceLocation;"))
    private ResourceLocation neoecoprototype$disconnectedSecond(IECOTier tier) {
        var upper = neoecoprototype$upperHostCable(tier, true);
        return upper != null ? upper : ECOComputationModels.getCableDisconnectedModel(tier);
    }

    /**
     * The model this addon wants, or null to let eco answer. A stub whose key is an eco tier is eco's
     * own drawing and stays that way; that happens whenever no accepted cell pushed our tier into the
     * lookup.
     */
    @Unique
    private ResourceLocation neoecoprototype$upperHostCable(IECOTier tier, boolean disconnected) {
        if (!(tier instanceof SimplifyTier)) return null;
        return disconnected ? ComputationCableModels.UPPER_DISCONNECTED : ComputationCableModels.UPPER_CONNECTED;
    }
}

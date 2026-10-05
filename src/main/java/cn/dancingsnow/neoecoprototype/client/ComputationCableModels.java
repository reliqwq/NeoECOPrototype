package cn.dancingsnow.neoecoprototype.client;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.resources.ResourceLocation;

/**
 * The four computation cable models, in one place, because two of them are reached only from the
 * renderer mixin - a search that starts in {@code models/block/computation_cable} finds no reference
 * to them and calls them dead files.
 *
 * <p>Ted's README item 5 splits them by the drive they sit in, not by the cell: a lower (L1-class)
 * frame wears the plain pair, an upper frame wears the {@code _normal} pair. eco's own lookup cannot
 * express that - it keys on the inserted cell's tier - hence {@code ECOComputationDriveRendererMixin}.
 */
public final class ComputationCableModels {
    private static ResourceLocation id(String name) {
        return ResourceLocation.fromNamespaceAndPath(
                NeoECOPrototype.MOD_ID, "block/computation_cable/" + name);
    }

    /** Used by the L1 frame, and registered as the model of both of our tiers. */
    public static final ResourceLocation LOWER_CONNECTED = id("cable_l1");
    public static final ResourceLocation LOWER_DISCONNECTED = id("cable_l1_dis");
    /** Only reachable through the mixin, and only when the drive wearing them is not a lower one. */
    public static final ResourceLocation UPPER_CONNECTED = id("cable_l1r");
    public static final ResourceLocation UPPER_DISCONNECTED = id("cable_l1r_dis");

    private ComputationCableModels() {
    }
}

package cn.dancingsnow.neoecoprototype.multiblock.trinity.fastpath;

import cn.dancingsnow.neoecoae.api.me.ECOFastPathFacade;
import cn.dancingsnow.neoecoae.api.me.provider.ECOFastPathDispatchProvider;
import appeng.api.networking.crafting.ICraftingProvider;

/**
 * Read-only compatibility checks for Neo ECO AE Extension beta2 FastPath.
 *
 * <p>FastPath is resolved from an AE2 {@link ICraftingProvider}; it is not a
 * global registry. Trinity does not implement that AE2 service yet, so this
 * class deliberately performs no registration and no task submission.
 */
public final class TrinityFastPathCompatibility {
    private TrinityFastPathCompatibility() {
    }

    /** Returns whether the beta2 FastPath facade is available on the runtime classpath. */
    public static boolean isFacadeAvailable() {
        try {
            Class.forName(ECOFastPathFacade.class.getName(), false,
                    TrinityFastPathCompatibility.class.getClassLoader());
            return true;
        } catch (LinkageError | ClassNotFoundException ignored) {
            return false;
        }
    }

    /**
     * Checks an already registered AE2 provider without mutating it or the network.
     *
     * @return the ECO FastPath adapter when the provider supports beta2 FastPath;
     *         otherwise {@code null}
     */
    public static ECOFastPathDispatchProvider resolveReadOnly(ICraftingProvider provider) {
        if (provider == null || !isFacadeAvailable() || !ECOFastPathFacade.supports(provider)) {
            return null;
        }
        return ECOFastPathFacade.resolveProvider(provider);
    }
}

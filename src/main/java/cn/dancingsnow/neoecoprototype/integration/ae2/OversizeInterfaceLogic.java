package cn.dancingsnow.neoecoprototype.integration.ae2;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.api.stacks.AEKeyTypes;
import appeng.api.storage.AEKeySlotFilter;
import appeng.helpers.InterfaceLogic;
import appeng.helpers.externalstorage.GenericStackInv;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoprototype.mixin.InterfaceLogicAccess;

import java.util.Set;

/** InterfaceLogic replacement inventories that widen how much one marker may stock. */
public final class OversizeInterfaceLogic {
    public static final long MAX_AMOUNT = 8192L;
    /** AE2 caps an item marker at one stack of 64, so this is how much wider every marker gets. */
    public static final long WIDENING = MAX_AMOUNT / 64L;

    private OversizeInterfaceLogic() {
    }

    public static void install(InterfaceLogic logic) {
        var access = (InterfaceLogicAccess) (Object) logic;
        var config = new OversizeConfigInventory(
                AEKeyTypes.getAll(),
                null,
                GenericStackInv.Mode.CONFIG_STACKS,
                logic.getConfig().size(),
                access::neoecoprototype$onConfigRowChanged,
                false,
                MAX_AMOUNT);
        var storage = new OversizeConfigInventory(
                AEKeyTypes.getAll(),
                access::neoecoprototype$isAllowedInStorageSlot,
                GenericStackInv.Mode.STORAGE,
                logic.getStorage().size(),
                access::neoecoprototype$onStorageChanged,
                false,
                MAX_AMOUNT);
        access.neoecoprototype$setConfig(config);
        access.neoecoprototype$setStorage(storage);
        config.useRegisteredCapacities();
        storage.useRegisteredCapacities();
    }

    private static final class OversizeConfigInventory extends ConfigInventory {
        private final long maxAmount;

        private OversizeConfigInventory(Set<AEKeyType> types, AEKeySlotFilter filter,
                                        GenericStackInv.Mode mode, int size, Runnable listener,
                                        boolean allowOverstacking, long maxAmount) {
            super(types, filter, mode, size, listener, allowOverstacking);
            this.maxAmount = maxAmount;
        }

        @Override
        public long getMaxAmount(AEKey key) {
            // AE2 caps a configured item slot at the item's stack size (64) and a fluid slot at 4000 mB,
            // and chemicals inherit the fluid value. Flattening every type to maxAmount would have shrunk
            // a fluid marker to 8 buckets, so widen each type from whatever AE2 gives it.
            if (key instanceof AEItemKey itemKey) {
                return Math.max(maxAmount, Math.min(itemKey.getMaxStackSize(), getCapacity(key.getType())));
            }
            var capacity = getCapacity(key.getType());
            return capacity > Long.MAX_VALUE / WIDENING ? Long.MAX_VALUE : capacity * WIDENING;
        }
    }
}

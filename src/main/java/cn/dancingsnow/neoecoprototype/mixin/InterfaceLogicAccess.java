package cn.dancingsnow.neoecoprototype.mixin;

import appeng.helpers.InterfaceLogic;
import appeng.util.ConfigInventory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

/* Version lock: AE2 19.2.17. Re-checked against that jar on 2026-10-06: InterfaceLogic still holds
 * `private final ConfigInventory config` and `... storage` (both need @Mutable), and still declares
 * onConfigChanged(), onConfigRowChanged(), onStorageChanged() and isAllowedInStorageSlot(int, AEKey).
 * remap = false because the target is a mod, not Minecraft: these members are looked up by these names. */
@Mixin(value = InterfaceLogic.class, remap = false)
public interface InterfaceLogicAccess {
    @Mutable
    @Accessor("config")
    void neoecoprototype$setConfig(ConfigInventory config);

    @Mutable
    @Accessor("storage")
    void neoecoprototype$setStorage(ConfigInventory storage);

    @Invoker("onConfigChanged")
    void neoecoprototype$onConfigChanged();

    @Invoker("onConfigRowChanged")
    void neoecoprototype$onConfigRowChanged();

    @Invoker("onStorageChanged")
    void neoecoprototype$onStorageChanged();

    @Invoker("isAllowedInStorageSlot")
    boolean neoecoprototype$isAllowedInStorageSlot(int slot, appeng.api.stacks.AEKey key);
}

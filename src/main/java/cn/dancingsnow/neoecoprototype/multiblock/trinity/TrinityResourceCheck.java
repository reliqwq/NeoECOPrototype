package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEItemKey;
import appeng.api.networking.security.IActionSource;
import appeng.api.storage.MEStorage;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/** Read-only resource-side result; never extracts or mutates storage. */
public record TrinityResourceCheck(boolean available, ResourceLocation targetItem,
                                   int requested, List<String> reasons) {
    public TrinityResourceCheck {
        reasons = List.copyOf(reasons);
    }

    public static TrinityResourceCheck noTarget() {
        return new TrinityResourceCheck(false, null, 0,
                List.of("No target item has been selected."));
    }

    public static TrinityResourceCheck unavailable(ResourceLocation targetItem, int requested,
                                                   String reason) {
        return new TrinityResourceCheck(false, targetItem, requested, List.of(reason));
    }

    public static boolean isStorageConnected(List<BlockEntity> storageParts) {
        for (BlockEntity part : storageParts) {
            if (part instanceof SimplifyDriveBlockEntity drive
                    && drive.getCluster() != null
                    && drive.getCluster().getController() != null
                    && drive.getCluster().getController().getMainNode().getGrid() != null) {
                return true;
            }
        }
        return false;
    }

    public static TrinityResourceCheck simulateStorage(List<BlockEntity> storageParts,
                                                       ResourceLocation targetItem, int requested) {
        if (targetItem == null) {
            return noTarget();
        }
        if (requested <= 0) {
            return unavailable(targetItem, requested, "Requested amount must be greater than zero.");
        }
        var item = BuiltInRegistries.ITEM.get(targetItem);
        if (item == net.minecraft.world.item.Items.AIR
                && !targetItem.equals(ResourceLocation.withDefaultNamespace("air"))) {
            return unavailable(targetItem, requested, "Target item is not registered: " + targetItem);
        }
        SimplifyStorageHostBlockEntity host = null;
        for (BlockEntity part : storageParts) {
            if (part instanceof SimplifyDriveBlockEntity drive && drive.getCluster() != null) {
                host = drive.getCluster().getController();
                if (host != null) {
                    break;
                }
            }
        }
        if (host == null || host.getMainNode().getGrid() == null) {
            return unavailable(targetItem, requested, "Storage Host grid is not connected.");
        }
        MEStorage storage = host.getMainNode().getGrid().getStorageService().getInventory();
        long available = storage.extract(AEItemKey.of(item), requested, Actionable.SIMULATE,
                IActionSource.ofMachine(host));
        if (available < requested) {
            return unavailable(targetItem, requested,
                    "Insufficient stored amount: available=" + available + ", requested=" + requested + ".");
        }
        return new TrinityResourceCheck(true, targetItem, requested,
                List.of("Storage check passed with SIMULATE; no items were extracted."));
    }
}

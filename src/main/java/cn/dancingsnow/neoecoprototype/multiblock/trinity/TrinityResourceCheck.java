package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import appeng.api.storage.MEStorage;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyStorageHostBlockEntity;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityStorageModuleBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/** Read-only storage observation; AE2 owns recursive material planning and input selection. */
public record TrinityResourceCheck(boolean available, ResourceLocation targetItem,
                                   int requested, List<Requirement> requirements,
                                   List<String> reasons) {
    public TrinityResourceCheck {
        requirements = List.copyOf(requirements);
        reasons = List.copyOf(reasons);
    }

    public record Requirement(AEKey what, long required, long available) {
        public long missing() {
            return Math.max(0L, required - Math.max(0L, available));
        }

        public String describe() {
            return what.getId() + " " + Math.max(0L, available) + "/" + required
                    + (missing() > 0 ? " (缺 " + missing() + ")" : "");
        }
    }

    public String summary() {
        return available ? "当前库存可见（仅供参考）" : reasons.isEmpty() ? "当前库存不足（仅供参考）" : reasons.get(0);
    }

    public static TrinityResourceCheck noTarget() {
        return new TrinityResourceCheck(false, null, 0, List.of(),
                List.of("No target item has been selected."));
    }

    public static TrinityResourceCheck unavailable(ResourceLocation targetItem, int requested,
                                                   String reason) {
        return new TrinityResourceCheck(false, targetItem, requested, List.of(), List.of(reason));
    }

    public static boolean isStorageConnected(List<BlockEntity> storageParts) {
        for (BlockEntity part : storageParts) {
            if (part instanceof SimplifyTrinityStorageModuleBlockEntity module
                    && module.getMainNode().getGrid() != null) {
                return true;
            }
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
        return simulateForCraft(storageParts, targetItem, requested, null);
    }

    /**
     * Reports only direct storage visibility. The pattern argument is retained for source
     * compatibility, but is intentionally not inspected: AE2 owns all crafting-input planning.
     */
    public static TrinityResourceCheck simulateForCraft(List<BlockEntity> storageParts,
                                                        ResourceLocation targetItem, int requested,
                                                        TrinityPatternCheck ignoredPattern) {
        if (targetItem == null || targetItem.equals(ResourceLocation.withDefaultNamespace("air"))) {
            return noTarget();
        }
        if (requested <= 0) {
            return unavailable(targetItem, requested, "Requested amount must be greater than zero.");
        }
        var item = BuiltInRegistries.ITEM.get(targetItem);
        if (item == net.minecraft.world.item.Items.AIR) {
            return unavailable(targetItem, requested, "Target item is not registered: " + targetItem);
        }
        AEItemKey key = AEItemKey.of(item);
        long available = availableAmount(storageParts, key, requested);
        if (available < 0L) {
            return unavailable(targetItem, requested, "Storage network is not reachable (advisory).");
        }
        boolean enough = available >= requested;
        return new TrinityResourceCheck(enough, targetItem, requested,
                List.of(new Requirement(key, requested, available)),
                List.of(enough
                        ? "Target item is currently visible in storage; AE2 still plans the real job."
                        : "Target item is not fully stored; AE2 may craft it from network inputs."));
    }

    private static long availableAmount(List<BlockEntity> storageParts, AEKey key, long amount) {
        for (BlockEntity part : storageParts) {
            if (part instanceof SimplifyTrinityStorageModuleBlockEntity module
                    && module.getMainNode().getGrid() != null) {
                MEStorage storage = module.getMainNode().getGrid().getStorageService().getInventory();
                return storage.extract(key, amount, Actionable.SIMULATE, IActionSource.ofMachine(module));
            }
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
            return -1L;
        }
        MEStorage storage = host.getMainNode().getGrid().getStorageService().getInventory();
        return storage.extract(key, amount, Actionable.SIMULATE, IActionSource.ofMachine(host));
    }
}

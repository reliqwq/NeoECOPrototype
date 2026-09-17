package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;


/** Read-only request-validation hint; AE2 owns final task feasibility and planning. */
public record TrinityTaskReadiness(boolean executable, List<String> reasons) {
    public TrinityTaskReadiness {
        reasons = List.copyOf(reasons);
    }

    public static TrinityTaskReadiness ready() {
        return new TrinityTaskReadiness(true, List.of("All Trinity services are ready."));
    }

    public static TrinityTaskReadiness blocked(List<String> reasons) {
        return new TrinityTaskReadiness(false, reasons);
    }

    public static TrinityTaskReadiness check(TrinityTaskRequest request,
                                             TrinityResourceCheck resource,
                                             TrinityPatternCheck pattern,
                                             TrinityService storage,
                                             TrinityService computation,
                                             TrinityService crafting,
                                             TrinityEnergySnapshot energy) {
        // This record is intentionally limited to request validation. Pattern selection, recursive
        // material planning, CPU selection, and execution feasibility belong to AE2's planner.
        // The other arguments remain part of the call shape for source compatibility with existing
        // UI/readiness snapshots; they must not turn this read-only hint into a second planner.
        List<String> reasons = new ArrayList<>();
        if (request.quantity() <= 0) {
            reasons.add("Task quantity must be greater than zero.");
        }
        if (BuiltInRegistries.ITEM.get(request.targetItem()) == net.minecraft.world.item.Items.AIR
                && !request.targetItem().equals(net.minecraft.resources.ResourceLocation.withDefaultNamespace("air"))) {
            reasons.add("Target item is not registered: " + request.targetItem());
        }
        return reasons.isEmpty() ? ready() : blocked(uniqueReasons(reasons));
    }

    private static List<String> uniqueReasons(List<String> reasons) {
        return new ArrayList<>(new java.util.LinkedHashSet<>(reasons));
    }
}

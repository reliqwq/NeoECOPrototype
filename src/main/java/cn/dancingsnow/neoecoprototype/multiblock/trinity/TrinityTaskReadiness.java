package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import net.minecraft.core.registries.BuiltInRegistries;

import java.util.ArrayList;
import java.util.List;


/** Read-only preflight result for a future Trinity task submission. */
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
        List<String> reasons = new ArrayList<>();
        if (request.quantity() <= 0) {
            reasons.add("Task quantity must be greater than zero.");
        }
        if (BuiltInRegistries.ITEM.get(request.targetItem()) == net.minecraft.world.item.Items.AIR
                && !request.targetItem().equals(net.minecraft.resources.ResourceLocation.withDefaultNamespace("air"))) {
            reasons.add("Target item is not registered: " + request.targetItem());
        }
        if (!resource.available()) {
            reasons.addAll(resource.reasons());
        }
        if (!pattern.available()) {
            reasons.addAll(pattern.reasons());
        }
        if (request.needsStorage() && !storage.online()) {
            reasons.add("Storage service is offline.");
        }
        if (request.needsComputation() && !computation.online()) {
            reasons.add("Computation service is offline.");
        }
        if (request.needsCrafting() && !crafting.online()) {
            reasons.add("Crafting service is offline.");
        }
        if (!energy.available()) {
            reasons.add("Trinity energy is not available.");
        }
        return reasons.isEmpty() ? ready() : blocked(uniqueReasons(reasons));
    }

    private static List<String> uniqueReasons(List<String> reasons) {
        return new ArrayList<>(new java.util.LinkedHashSet<>(reasons));
    }
}

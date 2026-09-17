package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import java.util.List;

/** Advisory summary of local observations; it is never an AE2 plan or execution gate. */
public record TrinityPlanCheck(boolean available, String summary, List<String> reasons) {
    public TrinityPlanCheck {
        reasons = List.copyOf(reasons);
    }

    public static TrinityPlanCheck evaluate(TrinityPatternCheck pattern,
                                            TrinityResourceCheck resource,
                                            TrinityService crafting) {
        List<String> reasons = new java.util.ArrayList<>();
        // Keep this as a diagnostic snapshot only. AE2's ICraftingService is the sole authority for
        // pattern choice, recursive inputs, missing materials, CPU capacity, and submission.
        if (!crafting.online()) {
            reasons.add("Crafting service is offline (advisory).");
        }
        if (!pattern.available()) {
            reasons.add("No matching pattern was observed in the local candidate scan (advisory).");
        }
        if (!resource.available()) {
            reasons.add("Local storage simulation did not confirm all inputs (advisory).");
        }
        return new TrinityPlanCheck(true,
                "advisory-only; AE2 ICraftingService decides the real plan",
                reasons.isEmpty()
                        ? List.of("Pattern and resource observations are advisory; submission uses AE2 planning.")
                        : List.copyOf(reasons));
    }
}

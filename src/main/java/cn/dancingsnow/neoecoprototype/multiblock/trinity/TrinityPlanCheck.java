package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import java.util.List;

/** Read-only gate before an AE2 ICraftingPlan is requested. */
public record TrinityPlanCheck(boolean available, String summary, List<String> reasons) {
    public TrinityPlanCheck {
        reasons = List.copyOf(reasons);
    }

    public static TrinityPlanCheck evaluate(TrinityPatternCheck pattern,
                                            TrinityResourceCheck resource,
                                            TrinityService crafting) {
        List<String> reasons = new java.util.ArrayList<>();
        if (!crafting.online()) {
            reasons.add("Crafting service is offline.");
        }
        if (!pattern.available()) {
            reasons.addAll(pattern.reasons());
        }
        if (!resource.available()) {
            reasons.addAll(resource.reasons());
        }
        if (!reasons.isEmpty()) {
            return new TrinityPlanCheck(false, "plan-not-ready", reasons);
        }
        return new TrinityPlanCheck(true,
                "plan-inputs-ready; ICraftingPlan not requested yet",
                List.of("CPU selection and plan creation remain read-only and are not submitted."));
    }
}

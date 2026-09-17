package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import cn.dancingsnow.neoecoprototype.blockentity.trinity.SimplifyTrinityCraftingModuleBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.LinkedHashSet;
import java.util.List;

/** Read-only pattern-output observation; AE2 owns pattern choice and input planning. */
public record TrinityPatternCheck(boolean available, ResourceLocation targetItem,
                                  int requested, int matchingPatterns, int loadedPatterns,
                                  List<String> loadedOutputs, List<String> reasons) {
    private static final int MAX_REPORTED_OUTPUTS = 6;

    public TrinityPatternCheck {
        loadedOutputs = List.copyOf(loadedOutputs);
        reasons = List.copyOf(reasons);
    }

    public String summary() {
        return "matching=" + matchingPatterns + " loaded=" + loadedPatterns;
    }

    public String outputSummary() {
        return loadedOutputs.isEmpty() ? "none" : String.join(", ", loadedOutputs);
    }

    public static TrinityPatternCheck simulate(List<BlockEntity> craftingParts,
                                               ResourceLocation targetItem, int requested) {
        if (targetItem == null || targetItem.equals(ResourceLocation.withDefaultNamespace("air"))) {
            return unavailable(targetItem, requested, "No target item has been selected.");
        }
        if (requested <= 0) {
            return unavailable(targetItem, requested, "Requested amount must be greater than zero.");
        }
        var item = BuiltInRegistries.ITEM.get(targetItem);
        if (item == net.minecraft.world.item.Items.AIR) {
            return unavailable(targetItem, requested, "Target item is not registered: " + targetItem);
        }
        AEItemKey targetKey = AEItemKey.of(item);
        int matches = 0;
        int loaded = 0;
        LinkedHashSet<String> outputs = new LinkedHashSet<>();
        for (BlockEntity part : craftingParts) {
            if (!(part instanceof SimplifyTrinityCraftingModuleBlockEntity bus)) {
                continue;
            }
            for (IPatternDetails pattern : bus.getAvailablePatterns()) {
                if (pattern == null) {
                    continue;
                }
                loaded++;
                boolean matched = false;
                for (var output : pattern.getOutputs()) {
                    if (output.amount() <= 0) {
                        continue;
                    }
                    if (outputs.size() < MAX_REPORTED_OUTPUTS) {
                        outputs.add(output.what().getId() + " x" + output.amount());
                    }
                    if (output.what() instanceof AEItemKey outputKey && outputKey.equals(targetKey)) {
                        matched = true;
                    }
                }
                if (matched) {
                    matches++;
                }
            }
        }
        if (loaded == 0) {
            return new TrinityPatternCheck(false, targetItem, requested, 0, 0,
                    List.copyOf(outputs), List.of("The crafting module has no decoded patterns."));
        }
        if (matches == 0) {
            return new TrinityPatternCheck(false, targetItem, requested, 0, loaded,
                    List.copyOf(outputs), List.of("No loaded pattern outputs " + targetItem + "."));
        }
        return new TrinityPatternCheck(true, targetItem, requested, matches, loaded,
                List.copyOf(outputs),
                List.of("Matching output observed; AE2 chooses the submitted pattern and inputs."));
    }

    private static TrinityPatternCheck unavailable(ResourceLocation targetItem, int requested, String reason) {
        return new TrinityPatternCheck(false, targetItem, requested, 0, 0,
                List.of(), List.of(reason));
    }
}

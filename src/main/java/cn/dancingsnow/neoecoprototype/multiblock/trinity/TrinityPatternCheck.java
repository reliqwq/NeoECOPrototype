package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.crafting.IPatternDetails;
import appeng.api.stacks.AEItemKey;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;

/** Read-only pattern-output lookup for Trinity preflight. */
public record TrinityPatternCheck(boolean available, ResourceLocation targetItem,
                                  int requested, int matchingPatterns, long outputPerPattern,
                                  List<String> reasons) {
    public TrinityPatternCheck {
        reasons = List.copyOf(reasons);
    }

    public String summary() {
        return available
                ? "patterns=" + matchingPatterns + ", output-per-pattern=" + outputPerPattern
                : "patterns=0";
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
        long batch = 0;
        for (BlockEntity part : craftingParts) {
            if (!(part instanceof ECOCraftingPatternBusBlockEntity bus)) {
                continue;
            }
            for (IPatternDetails pattern : bus.getAvailablePatterns()) {
                if (pattern == null) {
                    continue;
                }
                for (var output : pattern.getOutputs()) {
                    if (output.what() instanceof AEItemKey outputKey
                            && outputKey.equals(targetKey)
                            && output.amount() > 0) {
                        matches++;
                        batch = Math.max(batch, output.amount());
                        break;
                    }
                }
            }
        }
        if (matches == 0) {
            return unavailable(targetItem, requested, "No loaded pattern outputs " + targetItem + ".");
        }
        return new TrinityPatternCheck(true, targetItem, requested, matches, batch,
                List.of("Matching pattern output found; pattern was not submitted."));
    }

    private static TrinityPatternCheck unavailable(ResourceLocation targetItem, int requested, String reason) {
        return new TrinityPatternCheck(false, targetItem, requested, 0, 0, List.of(reason));
    }
}

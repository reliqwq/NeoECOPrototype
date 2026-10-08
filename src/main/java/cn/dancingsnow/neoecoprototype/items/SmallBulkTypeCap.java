package cn.dancingsnow.neoecoprototype.items;

import appeng.core.localization.Tooltips;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;

/**
 * The type ceiling a small bulk cell can actually reach, and the one place that knows it.
 *
 * <p>eco's MEGA bulk backend is a {@code final} class whose {@code getTotalItemTypes()} returns 25 - or 50
 * with its own upgrade card - without ever reading the item. Neither of those is reachable from this cell:
 * it stores what its filter slots mark, three or ten of them, so the fourth mark is the wall no matter what
 * the backend says it could hold. The reachable figure is shown instead, in all three places a player can
 * read a type count: the cell's own tooltip, the drive's tooltip, and the storage host panel.
 */
public final class SmallBulkTypeCap {

    private SmallBulkTypeCap() {
    }

    /** The type ceiling to report for a mounted stack: ours for a small bulk cell, eco's for everything else. */
    public static long of(ItemStack stack, IECOStorageCell cell) {
        if (stack != null && stack.getItem() instanceof SimplifySmallBulkStorageCellItem item) {
            return item.getTotalTypes();
        }
        if (stack != null && stack.getItem() instanceof SimplifySmallBulkFluidStorageCellItem fluid) {
            return fluid.getTotalTypes();
        }
        return cell.getTotalItemTypes();
    }

    /**
     * Replace the "types used" line eco just added with the same sentence at our ceiling.
     *
     * <p>The line cannot be found by its translation key: AE2's {@code Tooltips#typesUsed} builds it as an
     * empty literal with seven pieces appended, so no top-level contents carries a key at all. It is found by
     * position instead - eco's parent writes {@code bytesUsedLine} and then {@code typesUsed} and nothing
     * after them - and confirmed by rebuilding the sentence eco would have written and comparing it to the
     * candidate. Either check failing leaves eco's line untouched, which is a wrong-but-honest number rather
     * than a missing line or a corrupted one.
     *
     * @param from how many lines were already in the list before eco wrote into it
     * @param cell the inventory eco reported its two numbers from, so the replacement keeps its "used" figure
     */
    static void swapTooltipLine(List<Component> tooltip, int from, @Nullable IECOStorageCell cell, long cap) {
        if (cell == null || tooltip.size() < from + 2) {
            return;
        }
        long used = Math.max(0L, cell.getStoredItemTypes());
        long ecoReports = Math.max(0L, cell.getTotalItemTypes());
        if (cap >= ecoReports) {
            return;
        }
        int index = from + 1;
        if (!tooltip.get(index).getString().equals(Tooltips.typesUsed(used, ecoReports).getString())) {
            return;
        }
        tooltip.set(index, Tooltips.typesUsed(used, cap));
    }
}

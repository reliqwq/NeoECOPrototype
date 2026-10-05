package cn.dancingsnow.neoecoprototype.integration.jade.provider;

import appeng.core.localization.GuiText;
import appeng.core.localization.Tooltips;
import cn.dancingsnow.neoecoae.api.storage.IECOStorageCell;
import cn.dancingsnow.neoecoprototype.blockentity.storage.SimplifyDriveBlockEntity;
import cn.dancingsnow.neoecoprototype.integration.jade.SimplifyJadePlugin;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkFluidStorageCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifySmallBulkStorageCellItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.ItemStack;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.IElementHelper;

/**
 * Drive tooltip, following eco's own {@code ECODriveProvider}: mounted state, used/total bytes and
 * used/total types, with the byte total rendered as an infinity sign when a cell is unbounded. The
 * online/offline line is deliberately absent - AE2's device module already prints it, and a second
 * copy of the same sentence reads as a second, different state.
 */
public enum SimplifyDriveProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("mounted")) {
            boolean mounted = serverData.getBoolean("mounted");
            if (mounted) {
                tooltip.add(Component.translatable("jade.neoecoprototype.mounted").withStyle(ChatFormatting.GREEN));
            } else {
                tooltip.add(Component.translatable("jade.neoecoprototype.unmounted").withStyle(ChatFormatting.RED));
            }
        }
        if (serverData.contains("usedBytes") && serverData.contains("totalBytes")) {
            tooltip.add(bytesUsedLine(serverData.getLong("usedBytes"), serverData.getLong("totalBytes")));
        }
        if (serverData.contains("storedItemTypes")) {
            long used = serverData.getLong("storedItemTypes");
            tooltip.add(serverData.getBoolean("infiniteTypes")
                    ? Component.translatable("jade.neoecoprototype.types_infinite", used)
                    : Tooltips.typesUsed(used, serverData.getLong("totalItemTypes")));
        }
        ItemStack cell = mountedCellOf(serverData);
        if (!cell.isEmpty()) {
            // Icon first, then the name - the order eco's drive tooltip reads in on screen. Worth knowing
            // where that icon does NOT come from: no class in eco's jar references IElementHelper,
            // ItemStackElement or AE2's igtooltip SPI at all, so eco's own provider writes nothing but
            // Components and the small icon is drawn by something else in that stack. This line is
            // therefore ours; only its layout is copied from what the screen shows.
            // The name has to be its own text either way, because Jade's item element is an icon plus a
            // count badge and never draws the item's name. smallItem is Jade's own half-scale icon with
            // the smaller reserved box, so it sits on a text line without pushing it apart.
            tooltip.add(IElementHelper.get().smallItem(cell));
            tooltip.append(Component.translatable("jade.neoecoprototype.mounted_cell",
                    cell.getCount(), cell.getHoverName()));
        }
    }

    /**
     * The cell as an item line, the way Jade draws one for eco's drives. eco's own drive provider never
     * builds it - it comes from Jade's item element helper - so we name the item over the wire and
     * rebuild the stack here rather than assume the client block entity has the cell synced.
     */
    private static ItemStack mountedCellOf(CompoundTag data) {
        if (!data.contains("cellItem")) {
            return ItemStack.EMPTY;
        }
        var item = BuiltInRegistries.ITEM.get(ResourceLocation.parse(data.getString("cellItem")));
        return item == Items.AIR ? ItemStack.EMPTY : new ItemStack(item, data.getInt("cellCount"));
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof SimplifyDriveBlockEntity be) {
            // No online/offline line here on purpose: AE2's own device module already prints exactly
            // be.isOnline() for every in-world device, and saying it twice reads as two different states.
            tag.putBoolean("mounted", be.isMounted());
            // getCellStack() answers null for an empty drive, not an empty stack - and an unguarded read
            // here threw on every empty drive, which aborted the whole tag write and left the tooltip with
            // nothing but the mounted line.
            ItemStack cellStack = be.getCellStack();
            if (cellStack != null) {
                tag.putString("cellItem", BuiltInRegistries.ITEM.getKey(cellStack.getItem()).toString());
                tag.putInt("cellCount", cellStack.getCount());
            }
            IECOStorageCell cell = be.getCellInventory();
            if (cell != null && cellStack != null && cell.getTotalBytes() != Long.MAX_VALUE) {
                // A cell whose byte total is unbounded shows no numbers at all: its type count is fixed
                // by what it is, so both ratios say nothing a player can act on.
                tag.putLong("usedBytes", cell.getUsedBytes());
                tag.putLong("totalBytes", cell.getTotalBytes());
                tag.putBoolean("infiniteTypes", cell.hasInfiniteTypeCapacity());
                tag.putLong("storedItemTypes", cell.getStoredItemTypes());
                if (!cell.hasInfiniteTypeCapacity()) {
                    tag.putLong("totalItemTypes", markedTypeCap(cellStack, cell));
                }
            }
        }
    }

    /**
     * The type count a small bulk cell can actually reach. eco's MEGA bulk backend is a final class and
     * hard-codes 25 (50 with its own upgrade card) without ever reading the item, but a small bulk cell
     * holds only what its filter slots mark - 3 or 10 - so the tooltip reports the item's number, which
     * is the ceiling that is reachable. Every other cell reports what its inventory says.
     */
    private static long markedTypeCap(ItemStack stack, IECOStorageCell cell) {
        if (stack.getItem() instanceof SimplifySmallBulkStorageCellItem item) {
            return item.getTotalTypes();
        }
        if (stack.getItem() instanceof SimplifySmallBulkFluidStorageCellItem fluid) {
            return fluid.getTotalTypes();
        }
        return cell.getTotalItemTypes();
    }

    /**
     * AE2's own bytesUsed never special-cases an unbounded total, so it prints 9223372036854775807. eco
     * works around that in a private helper; this is the same construction through AE2's public pieces -
     * the number, AE2's own "of" separator, and an infinity glyph where the total would go - so the
     * wording stays whatever AE2 renders rather than a string we typed.
     */
    private static Component bytesUsedLine(long used, long total) {
        if (total != Long.MAX_VALUE) {
            return Tooltips.bytesUsed(used, total);
        }
        return Tooltips.of(GuiText.BytesUsed, Tooltips.of(
                Tooltips.ofUnformattedNumberWithRatioColor(used, 0.0D, false),
                Tooltips.of(""),
                Tooltips.of(GuiText.Of),
                Tooltips.of(""),
                Component.literal("∞")));
    }

    @Override
    public net.minecraft.resources.ResourceLocation getUid() {
        return SimplifyJadePlugin.id("simplify_drive");
    }
}

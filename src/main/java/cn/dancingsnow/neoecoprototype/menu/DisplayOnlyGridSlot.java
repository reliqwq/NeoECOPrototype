package cn.dancingsnow.neoecoprototype.menu;

import appeng.api.inventories.InternalInventory;
import appeng.menu.slot.FakeSlot;
import net.minecraft.world.item.ItemStack;

/**
 * A ghost slot that refuses to write.
 *
 * <p>AE2's {@code FakeSlot} blocks placement and pickup but not writes: its {@code set} only asks
 * {@code canSetFilterTo} - which is the inventory's own {@code isItemValid} - and then calls
 * {@code AppEngSlot.set}, and that one ends in {@code InternalInventory.setItemDirect}. So "fill this ghost
 * slot" from JEI's transfer or AE2's own click handling put <em>real</em> stacks into a machine whose grid is
 * meant to be display-only; the stacks were saved with the block entity, and breaking the assembler dropped
 * them back out. The grid here belongs to the pattern provider alone, so both write entry points are refused.
 *
 * <p>{@code getItem} is untouched, which is what keeps these slots showing what is actually inside. AE2's
 * {@code increase}/{@code decrease} (scrolling on a ghost slot) already return early unless the inventory is
 * a {@code ConfigMenuInventory}, so this grid was never reachable through them.
 */
public final class DisplayOnlyGridSlot extends FakeSlot {

    public DisplayOnlyGridSlot(InternalInventory inventory, int slot) {
        super(inventory, slot);
    }

    @Override
    public void set(ItemStack stack) {
        // Refused on purpose: super.set would land in the crafting grid.
    }

    @Override
    public void setFilterTo(ItemStack stack) {
        // Refused on purpose: the client half of this sends an InventoryActionPacket, and the server's slot
        // is an instance of this class too, so neither side can reach the grid.
    }
}

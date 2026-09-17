package cn.dancingsnow.neoecoprototype.blockentity.trinity;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;

/**
 * Return path for machines fed by Trinity's crafting module.
 *
 * <p>AE2's molecular assembler -- and any other {@code ICraftingMachine} -- pushes its result back
 * into the neighbour it received the ingredients from, using the plain NeoForge item handler
 * capability. Trinity's crafting module is a {@code PatternProviderLogicHost} but not an
 * {@code AENetworkedInvBlockEntity}, so without this handler a job would push its ingredients out
 * and then wait forever for a product that has no way back into the network.</p>
 *
 * <p>The handler has no storage of its own: everything handed to it goes straight into the ME
 * network the module belongs to, and whatever the network cannot take is returned to the caller so
 * nothing is silently voided.</p>
 */
public final class TrinityReturnItemHandler implements IItemHandler {
    private final SimplifyTrinityCraftingModuleBlockEntity owner;

    public TrinityReturnItemHandler(SimplifyTrinityCraftingModuleBlockEntity owner) {
        this.owner = owner;
    }

    @Override
    public int getSlots() {
        return 1;
    }

    @Override
    public ItemStack getStackInSlot(int slot) {
        return ItemStack.EMPTY;
    }

    @Override
    public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
        if (stack.isEmpty()) {
            return ItemStack.EMPTY;
        }
        var grid = owner.getMainNode().getGrid();
        if (grid == null) {
            return stack;
        }
        long accepted = grid.getStorageService().getInventory().insert(
                AEItemKey.of(stack), stack.getCount(),
                simulate ? Actionable.SIMULATE : Actionable.MODULATE,
                IActionSource.ofMachine(owner));
        if (accepted <= 0) {
            return stack;
        }
        if (accepted >= stack.getCount()) {
            return ItemStack.EMPTY;
        }
        ItemStack leftover = stack.copy();
        leftover.shrink((int) accepted);
        return leftover;
    }

    @Override
    public ItemStack extractItem(int slot, int amount, boolean simulate) {
        return ItemStack.EMPTY;
    }

    @Override
    public int getSlotLimit(int slot) {
        return 64;
    }

    @Override
    public boolean isItemValid(int slot, ItemStack stack) {
        return true;
    }
}

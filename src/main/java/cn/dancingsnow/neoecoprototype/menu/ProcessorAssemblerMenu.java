package cn.dancingsnow.neoecoprototype.menu;

import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * The assembler's menu: upgrade column and progress bar from AE2's standard toolkit, the nine grid slots
 * as a read-only preview of what the active job is consuming (the machine has no crafting grid to fill),
 * and the output slot showing the stack waiting to eject.
 */
public class ProcessorAssemblerMenu extends UpgradeableMenu<SimplifyStonecuttingAssemblerBlockEntity>
        implements IProgressProvider {
    private static final int PREVIEW_SLOTS = 9;

    public static MenuType<ProcessorAssemblerMenu> buildType() {
        return MenuTypeBuilder
                .create((id, inv, host) -> new ProcessorAssemblerMenu(id, inv, host),
                        SimplifyStonecuttingAssemblerBlockEntity.class)
                .withMenuTitle(host -> host.getBlockState().getBlock().getName())
                .buildUnregistered(NeoECOPrototype.id("processor_assembler"));
    }

    public static MenuType<ProcessorAssemblerMenu> type() {
        return ModRegistration.PROCESSOR_ASSEMBLER_MENU.get();
    }

    private final SimplifyStonecuttingAssemblerBlockEntity assembler;
    private int craftProgress;

    public ProcessorAssemblerMenu(int id, Inventory inv, SimplifyStonecuttingAssemblerBlockEntity host) {
        super(type(), id, inv, host);
        assembler = host;
    }

    @Override
    protected void setupConfig() {
        super.setupConfig();
        // setupConfig runs from the super constructor, before this.assembler is assigned.
        var host = (SimplifyStonecuttingAssemblerBlockEntity) getHost();
        var inventory = host.getInternalInventory();
        for (int slot = 0; slot < PREVIEW_SLOTS; slot++) {
            addSlot(new DisplayOnlyGridSlot(inventory, slot), SlotSemantics.MACHINE_CRAFTING_GRID);
        }
        addSlot(new OutputSlot(inventory, PREVIEW_SLOTS, null), SlotSemantics.MACHINE_OUTPUT);
    }

    @Override
    public void broadcastChanges() {
        craftProgress = assembler.getCraftingProgress();
        super.broadcastChanges();
    }

    @Override
    public int getCurrentProgress() {
        return craftProgress;
    }

    @Override
    public int getMaxProgress() {
        return 100;
    }
}

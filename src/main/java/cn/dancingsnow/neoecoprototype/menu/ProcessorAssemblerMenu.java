package cn.dancingsnow.neoecoprototype.menu;

import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.implementations.UpgradeableMenu;
import appeng.menu.interfaces.IProgressProvider;
import appeng.menu.slot.OutputSlot;
import appeng.menu.slot.FakeSlot;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;

/**
 * The assembler's menu: AE2's molecular assembler layout without the encoded-pattern slot, because this
 * machine is only meant to be driven by a pattern provider. The grid slots are display-only.
 */
public class ProcessorAssemblerMenu extends UpgradeableMenu<SimplifyStonecuttingAssemblerBlockEntity>
        implements IProgressProvider {
    /** AE2's INV_MAIN is grid 0..8, output 9, encoded pattern 10. */
    private static final int GRID_SLOTS = 9;
    private static final int OUTPUT_SLOT = 9;

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
        var inventory = ((SimplifyStonecuttingAssemblerBlockEntity) getHost())
                .getSubInventory(MolecularAssemblerBlockEntity.INV_MAIN);
        for (int slot = 0; slot < GRID_SLOTS; slot++) {
            addSlot(new FakeSlot(inventory, slot), SlotSemantics.MACHINE_CRAFTING_GRID);
        }
        addSlot(new OutputSlot(inventory, OUTPUT_SLOT, null), SlotSemantics.MACHINE_OUTPUT);
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

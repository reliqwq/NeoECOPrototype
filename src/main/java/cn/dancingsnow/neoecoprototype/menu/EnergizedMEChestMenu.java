package cn.dancingsnow.neoecoprototype.menu;

import appeng.menu.AEBaseMenu;
import appeng.menu.SlotSemantics;
import appeng.menu.implementations.MenuTypeBuilder;
import appeng.menu.slot.AppEngSlot;
import cn.dancingsnow.neoecoae.grid.storage.cell.ECOStorageCellSettings;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.blockentity.storage.EnergizedMEChestBlockEntity;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;

public class EnergizedMEChestMenu extends AEBaseMenu {
    private static final String OPEN_CELL_WORKBENCH = "open_cell_workbench";
    public static MenuType<EnergizedMEChestMenu> buildType() {
        return MenuTypeBuilder
                .create((id, inventory, host) -> new EnergizedMEChestMenu(id, inventory, host),
                        EnergizedMEChestBlockEntity.class)
                .withMenuTitle(host -> host.getBlockState().getBlock().getName())
                .buildUnregistered(NeoECOPrototype.id("energized_me_chest"));
    }

    public static MenuType<EnergizedMEChestMenu> type() {
        return ModRegistration.ENERGIZED_ME_CHEST_MENU.get();
    }

    public EnergizedMEChestMenu(int id, Inventory playerInventory, EnergizedMEChestBlockEntity host) {
        super(type(), id, playerInventory, host);
        registerClientAction(OPEN_CELL_WORKBENCH, this::requestCellWorkbench);
        addSlot(new EnergizedMECellSlot(host, 0), SlotSemantics.STORAGE_CELL);
        createPlayerInventorySlots(playerInventory);
    }

    public void sendCellWorkbenchRequest() {
        sendClientAction(OPEN_CELL_WORKBENCH);
    }

    public void requestCellWorkbench() {
        Player player = getPlayer();
        EnergizedMEChestBlockEntity host = (EnergizedMEChestBlockEntity) getTarget();
        if (player instanceof ServerPlayer serverPlayer && stillValid(player)
                && host.canPlayerInteract(player) && ECOStorageCellSettings.canConfigure(host.getCell())) {
            host.openCellConfiguration(player, host.getBlockPos(), host.getCellInteractionId());
        } else {
            player.displayClientMessage(Component.translatable(
                    "message.neoecoprototype.energized_me_chest.no_cell"), true);
        }
    }

    private static final class EnergizedMECellSlot extends AppEngSlot {
        private final EnergizedMEChestBlockEntity host;

        private EnergizedMECellSlot(EnergizedMEChestBlockEntity host, int slot) {
            super(host.getCellSlotInventory(), slot);
            this.host = host;
            setIcon(null);
        }

        @Override
        public boolean mayPlace(ItemStack stack) {
            return host.acceptsCell(stack);
        }
    }
}

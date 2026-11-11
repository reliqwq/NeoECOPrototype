package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.251, AE2 19.2.17. Re-checked against 19.2.17, 19.2.18 and 19.2.19 on
 * 2026-11-11: injectProviderPower(double, Actionable) still reads the private final SortedSet requesters,
 * and at offsets 74-89 still does `if (remainder > 0 && mode == MODULATE) iterator.remove()` - i.e. it
 * drops a storage that merely could not absorb the *whole* offer, even when it just took part of it and
 * still has room. Restoring it is left to GridPowerStorageStateChanged.RECEIVE_POWER, which
 * StoredEnergyAmount only emits on the isReceiving false->true transition, so a storage can stay out of
 * the set indefinitely while it still has room (measured in the field: a 12.8M cell with 4.5M free and
 * 200/200 SIMULATE offers of 214,748,364,700 AE absorbed 0.00). Retire this mixin when upstream either
 * removes only storages that are full (a getAECurrentPower() >= getAEMaxPower() check next to the remove)
 * or re-adds capable storages itself; both are readable straight off javap -c.
 */

import appeng.api.config.Actionable;
import appeng.api.networking.IGridNode;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.me.Grid;
import appeng.me.service.EnergyService;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.SortedSet;

/**
 * Puts public storages that still have room back into the energy service's requester set after an offer
 * they could not absorb whole. The list is rebuilt from the grid's own nodes, so it does not depend on
 * an event having been delivered: a battery that was removed while full and has since been drained is
 * exactly the case that reaches the field, and it is repaired by the next injection attempt.
 *
 * <p>This runs for SIMULATE as well as MODULATE on purpose - the stuck grid only ever receives simulated
 * offers (the FE side asks what can be accepted, is told nothing, and never sends the modulating half),
 * so waiting for a MODULATE would never repair it. At {@code RETURN} the iterator is finished and
 * {@code ongoingInjectOperation} is back to false, so adding to the set here is the same state
 * {@code EnergyService#addRequester} writes (it only re-checks insertion permission).</p>
 */
@Mixin(EnergyService.class)
public abstract class EnergyServiceRequesterReAddMixin {
    @Shadow
    @Final
    private SortedSet<IAEPowerStorage> requesters;

    @Shadow
    @Final
    Grid grid;

    @Inject(method = "injectProviderPower", at = @At("RETURN"), require = 1)
    private void neoecoprototype$readdStoragesTurnedAwayWithRoom(double amount, Actionable mode,
                                                                 CallbackInfoReturnable<Double> cir) {
        // Nothing was refused: every visited storage took what it was offered.
        if (cir.getReturnValue() <= 0.0) {
            return;
        }
        for (IGridNode node : grid.getNodes()) {
            IAEPowerStorage storage = node.getService(IAEPowerStorage.class);
            if (storage == null
                    || !storage.isAEPublicPowerStorage()
                    || !storage.getPowerFlow().isAllowInsertion()
                    || storage.getAECurrentPower() >= storage.getAEMaxPower()) {
                continue;
            }
            requesters.add(storage);
        }
    }
}

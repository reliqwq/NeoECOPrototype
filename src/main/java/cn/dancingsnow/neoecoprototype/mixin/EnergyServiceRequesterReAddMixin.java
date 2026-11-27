package cn.dancingsnow.neoecoprototype.mixin;

/*
 * Version lock: NeoForge 21.1.251, AE2 19.2.17. Re-checked against 19.2.17, 19.2.18 and 19.2.19 on
 * 2026-11-11: injectProviderPower(double, Actionable) still reads the private final SortedSet requesters,
 * and at offsets 74-89 still does `if (remainder > 0 && mode == MODULATE) iterator.remove()` - i.e. it
 * drops a storage that merely could not absorb the *whole* offer, even when it just took part of it and
 * still has room. Restoring it is left to GridPowerStorageStateChanged.RECEIVE_POWER, which
 * StoredEnergyAmount only emits on the isReceiving false->true transition, so a storage can stay out of
 * the set indefinitely while it still has room (measured in the field: a 12.8M cell with 4.5M free and
 * 200/200 SIMULATE offers of 214,748,364,700 AE absorbed 0.00). The repair now records only the
 * requesters removed by the current modulating offer; it must not walk grid.getNodes() on every refused
 * injection. Retire this mixin when upstream either removes only storages that are full (a
 * getAECurrentPower() >= getAEMaxPower() check next to the remove) or re-adds capable storages itself;
 * both are readable straight off javap -c.
 */

import appeng.api.config.Actionable;
import appeng.api.networking.energy.IAEPowerStorage;
import appeng.me.service.EnergyService;
import cn.dancingsnow.neoecoprototype.integration.ae2.RemovedRequesterIterator;
import com.llamalad7.mixinextras.sugar.Local;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Iterator;
import java.util.SortedSet;

/** Restores only storages evicted by the current oversized modulating offer. */
@Mixin(EnergyService.class)
public abstract class EnergyServiceRequesterReAddMixin {
    @Shadow
    @Final
    private SortedSet<IAEPowerStorage> requesters;

    @Redirect(
            method = "injectProviderPower",
            at = @At(value = "INVOKE", target = "Ljava/util/SortedSet;iterator()Ljava/util/Iterator;"),
            require = 1
    )
    private Iterator<IAEPowerStorage> neoecoprototype$trackRemovedRequesters(
            SortedSet<IAEPowerStorage> requesters) {
        return new RemovedRequesterIterator(requesters.iterator());
    }

    @Inject(method = "injectProviderPower", at = @At("RETURN"), require = 1)
    private void neoecoprototype$restoreRemovedRequesters(
            double amount,
            Actionable mode,
            CallbackInfoReturnable<Double> cir,
            @Local(index = 6) Iterator<IAEPowerStorage> iterator) {
        ((RemovedRequesterIterator) iterator).restoreEligible(requesters);
    }
}

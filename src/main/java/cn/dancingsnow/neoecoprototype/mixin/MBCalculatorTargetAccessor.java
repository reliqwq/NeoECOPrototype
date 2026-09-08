package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: NeoForge 21.1.233, AE2 19.2.17. Check MBCalculator#target field name. */

import appeng.me.cluster.IAEMultiBlock;
import appeng.me.cluster.MBCalculator;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

/** Exposes AE2's calculator target to the L1 structure-check mixin. */
@Mixin(MBCalculator.class)
public interface MBCalculatorTargetAccessor {
    @Accessor("target")
    IAEMultiBlock<?> neoecoprototype$getTarget();
}

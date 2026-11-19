package cn.dancingsnow.neoecoprototype.mixin;

/* Version lock: Minecraft 1.21.1, NeoForge 21.1.251, AE2 19.2.17.
 * AE2's MolecularAssemblerBlockEntity.hasMats() also treats assemble(EMPTY) as no materials. In
 * tickingRequest, assemble is at offset 404, isEmpty at 413, and the early return at 416 skips
 * getRemainingItems at 451. For our fluid-only plan, both checks allow the empty result; the completion
 * callback queues fluid after a real cycle. EMPTY.onCraftedBySystem and pushOut(EMPTY) are harmless.
 * Remove when AE2 exposes a non-item completion hook or either checked invocation changes.
 */

import appeng.blockentity.crafting.IMolecularAssemblerSupportedPattern;
import appeng.blockentity.crafting.MolecularAssemblerBlockEntity;
import appeng.crafting.CraftingEvent;
import net.minecraft.world.Container;
import net.minecraft.world.level.Level;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.ProcessorAssemblyPattern;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyStonecuttingAssemblerBlockEntity;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(MolecularAssemblerBlockEntity.class)
public abstract class MolecularAssemblerFluidOutputMixin {
    @Shadow
    private IMolecularAssemblerSupportedPattern myPlan;

    @Redirect(method = "hasMats", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 0), require = 1)
    private boolean neoecoae$fluidOnlyHasMats(ItemStack output) {
        return output.isEmpty() && !(myPlan instanceof ProcessorAssemblyPattern plan && plan.isFluidOnly());
    }

    @Redirect(method = "tickingRequest", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/world/item/ItemStack;isEmpty()Z", ordinal = 2), require = 1)
    private boolean neoecoae$continueFluidOnlyCycle(ItemStack output) {
        return output.isEmpty() && !(myPlan instanceof ProcessorAssemblyPattern plan && plan.isFluidOnly());
    }

    @Redirect(method = "tickingRequest", at = @At(value = "INVOKE",
            target = "Lappeng/crafting/CraftingEvent;fireAutoCraftingEvent(Lnet/minecraft/world/level/Level;"
                    + "Lappeng/api/crafting/IPatternDetails;Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/Container;)V"),
            require = 1)
    private void neoecoae$deferCraftEventForFluidOnly(Level level, appeng.api.crafting.IPatternDetails pattern,
            ItemStack result, Container grid) {
        if (!(pattern instanceof ProcessorAssemblyPattern plan && plan.isFluidOnly())) {
            CraftingEvent.fireAutoCraftingEvent(level, pattern, result, grid);
        }
    }

    @Inject(method = "tickingRequest", at = @At("RETURN"), require = 1)
    private void neoecoae$commitFluidOutputCycle(
            appeng.api.networking.IGridNode node, int ticksSinceLastCall,
            CallbackInfoReturnable<appeng.api.networking.ticking.TickRateModulation> cir) {
        if ((Object) this instanceof SimplifyStonecuttingAssemblerBlockEntity assembler
                && assembler.getInternalInventory().getStackInSlot(9).isEmpty()) {
            assembler.commitPendingFluidOutputs();
        }
    }
}

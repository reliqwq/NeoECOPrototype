package cn.dancingsnow.neoecoprototype.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

/* Version lock: Minecraft 1.21.1 (NeoForge 21.1.251). Re-checked on 2026-10-06: Slot.y is
 * `public final int`, so the accessor is here for the final, not for the privacy - @Mutable is what
 * lets the setter write it. A rename of the field fails the launch, not a silent no-op. */
@Mixin(Slot.class)
public interface SlotYAccessor {
    @Mutable
    @Accessor("y")
    void neoecoprototype$setY(int y);
}

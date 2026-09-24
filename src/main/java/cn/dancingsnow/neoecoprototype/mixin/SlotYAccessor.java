package cn.dancingsnow.neoecoprototype.mixin;

import net.minecraft.world.inventory.Slot;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Slot.class)
public interface SlotYAccessor {
    @Mutable
    @Accessor("y")
    void neoecoprototype$setY(int y);
}

package cn.dancingsnow.neoecoprototype.integration.mekanism;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import mekanism.api.chemical.Chemical;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.registration.impl.ChemicalDeferredRegister;
import mekanism.common.registration.impl.DeferredChemical;
import mekanism.common.registries.MekanismTileEntityTypes;
import mekanism.common.tile.component.TileComponentConfig;
import net.minecraft.core.Direction;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.common.NeoForgeMod;

/** Optional machine compatibility and acquisition belong to Prototype, not ECO itself. */
public final class MilkInfusionCompatibility {
    private static final ChemicalDeferredRegister CHEMICALS = new ChemicalDeferredRegister(NeoECOPrototype.MOD_ID);
    private static final DeferredChemical<Chemical> MILK = CHEMICALS.registerInfuse("milk", 0xFFF8E7);

    private MilkInfusionCompatibility() {}

    public static void register(IEventBus bus) {
        NeoForgeMod.enableMilkFluid();
        CHEMICALS.register(bus);
        bus.addListener(MilkInfusionCompatibility::capabilities);
    }

    private static void capabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.FluidHandler.BLOCK, MekanismTileEntityTypes.METALLURGIC_INFUSER.get(),
            (tile, side) -> {
                if (!allowsInput(tile.getConfig(), side)) return null;
                return new MilkInfusionFluidHandler(tile.infusionTank, MILK.get(), NeoForgeMod.MILK.get(),
                    () -> !tile.isRemoved() && tile.getLevel() != null && !tile.getLevel().isClientSide
                        && allowsInput(tile.getConfig(), side), tile::markForSave);
            });
    }

    static boolean allowsInput(TileComponentConfig config, Direction side) {
        if (side == null) return true;
        var slot = config.getSlotInfo(TransmissionType.CHEMICAL, side);
        return slot != null && slot.canInput();
    }
}

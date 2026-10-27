package cn.dancingsnow.neoecoprototype.integration.mekanism;

import appeng.api.config.Actionable;
import appeng.api.stacks.AEFluidKey;
import appeng.parts.automation.HandlerStrategy;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import mekanism.api.chemical.BasicChemicalTank;
import mekanism.api.chemical.ChemicalStack;
import mekanism.api.chemical.IChemicalTank;
import mekanism.common.lib.transmitter.TransmissionType;
import mekanism.common.tile.component.TileComponentConfig;
import mekanism.common.tile.component.config.slot.ISlotInfo;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static cn.dancingsnow.neoecoprototype.integration.mekanism.MilkInfusionTestBootstrap.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MilkInfusionFluidHandlerTest {
    private IChemicalTank tank;
    private MilkInfusionFluidHandler handler;
    private final AtomicBoolean enabled = new AtomicBoolean(true);
    private final AtomicInteger saves = new AtomicInteger();

    @BeforeAll static void bootstrap() { initialize(); }
    @BeforeEach void setup() {
        tank = newTank();
        handler = new MilkInfusionFluidHandler(tank, milk, milkFluid, enabled::get, saves::incrementAndGet);
    }
    private static IChemicalTank newTank() {
        return BasicChemicalTank.createModern(1000, (stack, type) -> true, (stack, type) -> true, stack -> true, () -> {});
    }

    @Test void ae2FluidExportAcceptsMinecraftMilkWithoutMutatingSimulation() {
        assertEquals(ResourceLocation.withDefaultNamespace("milk"), BuiltInRegistries.FLUID.getKey(milkFluid));
        var key = AEFluidKey.of(milkFluid);
        assertEquals(1000, HandlerStrategy.FLUIDS.insert(handler, key, 1000, Actionable.SIMULATE));
        assertTrue(tank.isEmpty());
        assertEquals(0, saves.get());
        assertEquals(1000, HandlerStrategy.FLUIDS.insert(handler, key, 1000, Actionable.MODULATE));
        assertEquals(1000, tank.getStored());
        assertEquals(1, saves.get());
    }

    @Test void capacityAndCallerStackArePreserved() {
        var source = new FluidStack(milkFluid, 750);
        assertEquals(750, handler.fill(source, FluidAction.EXECUTE));
        assertEquals(250, handler.fill(source, FluidAction.EXECUTE));
        assertEquals(0, handler.fill(source, FluidAction.EXECUTE));
        assertEquals(750, source.getAmount());
        assertEquals(1000, handler.getFluidInTank(0).getAmount());
    }

    @Test void waterAndOtherChemicalsAreRejectedAndDrainingCannotDuplicateMilk() {
        assertEquals(0, handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE));
        tank.setStack(new ChemicalStack(carbon, 200));
        assertEquals(0, handler.fill(new FluidStack(milkFluid, 1000), FluidAction.EXECUTE));
        assertTrue(tank.isTypeEqual(carbon));
        assertEquals(200, tank.getStored());
        assertTrue(handler.drain(1000, FluidAction.EXECUTE).isEmpty());
        assertEquals(0, saves.get());
    }

    @Test void nativeTankSaveLoadRetainsMilkAndCachedHandlerChecksCurrentInputState() {
        handler.fill(new FluidStack(milkFluid, 500), FluidAction.EXECUTE);
        var restored = newTank();
        restored.deserializeNBT(lookups, tank.serializeNBT(lookups));
        assertTrue(restored.isTypeEqual(milk));
        assertEquals(500, restored.getStored());
        enabled.set(false);
        assertEquals(0, handler.fill(new FluidStack(milkFluid, 500), FluidAction.EXECUTE));
        assertEquals(500, tank.getStored());
    }

    @Test void inputSideConfigurationRejectsOutputOnlyFaces() {
        var config = mock(TileComponentConfig.class);
        var slot = mock(ISlotInfo.class);
        when(config.getSlotInfo(TransmissionType.CHEMICAL, Direction.EAST)).thenReturn(slot);
        assertFalse(MilkInfusionCompatibility.allowsInput(config, Direction.EAST));
        when(slot.canInput()).thenReturn(true);
        assertTrue(MilkInfusionCompatibility.allowsInput(config, Direction.EAST));
        when(slot.canInput()).thenReturn(false);
        when(slot.canOutput()).thenReturn(true);
        assertFalse(MilkInfusionCompatibility.allowsInput(config, Direction.EAST));
    }
}

package cn.dancingsnow.neoecoprototype.integration.mekanism;

import java.util.List;
import java.util.stream.Stream;
import mekanism.api.MekanismAPI;
import mekanism.api.chemical.Chemical;
import mekanism.api.chemical.ChemicalBuilder;
import net.minecraft.SharedConstants;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.MappedRegistry;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.WaterFluid;
import net.neoforged.fml.loading.LoadingModList;
import net.neoforged.neoforge.common.NeoForgeMod;
import static org.mockito.Mockito.*;

public final class MilkInfusionTestBootstrap {
    public static Chemical milk;
    public static Chemical carbon;
    public static Fluid milkFluid;
    public static HolderLookup.Provider lookups;
    private static boolean initialized;

    private MilkInfusionTestBootstrap() {}

    public static synchronized void initialize() {
        if (initialized) return;
        SharedConstants.tryDetectVersion();
        try (var loading = mockStatic(LoadingModList.class)) {
            var mods = mock(LoadingModList.class);
            when(mods.getModFiles()).thenReturn(List.of());
            loading.when(LoadingModList::get).thenReturn(mods);
            Bootstrap.bootStrap();
        }
        ((MappedRegistry<?>) BuiltInRegistries.ITEM).unfreeze();
        ((MappedRegistry<?>) BuiltInRegistries.FLUID).unfreeze();
        Registry.register(MekanismAPI.CHEMICAL_REGISTRY, MekanismAPI.EMPTY_CHEMICAL_KEY, MekanismAPI.EMPTY_CHEMICAL);
        milk = Registry.register(MekanismAPI.CHEMICAL_REGISTRY, "neoecoprototype:milk", new Chemical(ChemicalBuilder.infuseType()));
        carbon = Registry.register(MekanismAPI.CHEMICAL_REGISTRY, "milk_test:carbon", new Chemical(ChemicalBuilder.infuseType()));
        // The transport test needs the registered minecraft:milk key, without a NeoForge mod constructor.
        milkFluid = Registry.register(BuiltInRegistries.FLUID, NeoForgeMod.MILK.getId(), new WaterFluid.Source());
        Registry.register(BuiltInRegistries.ITEM, "useless_stretcher:two_i_doll", new Item(new Item.Properties()));
        Registry.register(BuiltInRegistries.ITEM, "neoecoae:eco_infinite_sand_cell", new Item(new Item.Properties()));
        lookups = HolderLookup.Provider.create(Stream.of(MekanismAPI.CHEMICAL_REGISTRY.asLookup(),
            BuiltInRegistries.ITEM.asLookup(), BuiltInRegistries.FLUID.asLookup()));
        initialized = true;
    }
}

package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.AEKey;
import cn.dancingsnow.neoecoae.api.ECOTier;
import cn.dancingsnow.neoecoprototype.items.EcoSupplyCellItem;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluids;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;
import java.util.function.Supplier;

/**
 * The two supply cells {@code scriptSupplyCellBindsLazilyAndEcosEngineServesOnlyThoseKeys} measures, built
 * at registry time because an {@link Item} cannot be constructed afterwards: {@code Item}'s own field
 * initialiser calls {@code BuiltInRegistries.ITEM.createIntrusiveHolder(this)} (Item.java:67) and
 * {@code MappedRegistry.validateWrite} throws {@code IllegalStateException("Registry is already frozen")}
 * once registration is over - which is exactly when a game test runs.
 *
 * <p>Both cells are registered under {@link #NAMESPACE}, which is not our mod id and not {@code kubejs}.
 * That keeps them out of every startup pass and every sweep that walks registered items by namespace: the
 * common-setup card association loop would ask {@link #broken()} for its partition table and throw, and
 * the cell-card sweep would then demand fuzzy/inverter/void associations of a fixture. A registered item
 * that nothing else looks at is the only shape that satisfies both.
 *
 * <p>{@link #init} is reached only from {@code NeoECOPrototype}'s constructor behind the gametest gate, so
 * production never takes that branch and never resolves this class - which matters because {@code gametest/**}
 * is excluded from the shipped jar.
 */
public final class SupplyCellGuardProbe {

    /** Deliberately neither ours nor {@code kubejs}: no sweep filters on this namespace. */
    public static final String NAMESPACE = "neoecoprototype_gametest";
    public static final ResourceLocation BINDINGS_ID =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "bound_supply_cell");
    public static final ResourceLocation BROKEN_ID =
            ResourceLocation.fromNamespaceAndPath(NAMESPACE, "broken_supply_cell");

    /** One slot per binder of the healthy cell, in script order (item, then fluid). */
    private static final int[] BINDER_CALLS = new int[2];

    private static boolean initialised;
    private static volatile int callsAtConstruction = -1;
    private static volatile EcoSupplyCellItem bindings;
    private static volatile EcoSupplyCellItem broken;

    private SupplyCellGuardProbe() {
    }

    public static void init(IEventBus modBus) {
        if (initialised) {
            return;
        }
        initialised = true;
        var items = DeferredRegister.create(Registries.ITEM, NAMESPACE);
        items.register(BINDINGS_ID.getPath(), SupplyCellGuardProbe::buildBindings);
        items.register(BROKEN_ID.getPath(), SupplyCellGuardProbe::buildBroken);
        items.register(modBus);
    }

    private static EcoSupplyCellItem buildBindings() {
        var cell = new EcoSupplyCellItem(
                new Item.Properties().stacksTo(1),
                ECOTier.L9,
                SimplifyStorageCellItem::getItemCellType,
                List.<Supplier<AEKey>>of(
                        () -> {
                            BINDER_CALLS[0]++;
                            return AEItemKey.of(Items.COBBLESTONE);
                        },
                        () -> {
                            BINDER_CALLS[1]++;
                            return AEFluidKey.of(Fluids.WATER);
                        }),
                "tooltip.neoecoae.infinite_resource",
                "kubejs:guard_supply_cell");
        // Read while the binder is still untouched: anything above zero means construction resolved.
        callsAtConstruction = BINDER_CALLS[0] + BINDER_CALLS[1];
        bindings = cell;
        return cell;
    }

    private static EcoSupplyCellItem buildBroken() {
        var cell = new EcoSupplyCellItem(
                new Item.Properties().stacksTo(1),
                ECOTier.L9,
                SimplifyStorageCellItem::getItemCellType,
                List.<Supplier<AEKey>>of(() -> null),
                "tooltip.neoecoae.infinite_resource",
                "kubejs:guard_broken_cell");
        broken = cell;
        return cell;
    }

    public static boolean isBuilt() {
        return initialised && bindings != null && broken != null;
    }

    public static int callsAtConstruction() {
        return callsAtConstruction;
    }

    public static EcoSupplyCellItem bindings() {
        return bindings;
    }

    public static EcoSupplyCellItem broken() {
        return broken;
    }

    public static int binderCalls(int index) {
        return BINDER_CALLS[index];
    }
}

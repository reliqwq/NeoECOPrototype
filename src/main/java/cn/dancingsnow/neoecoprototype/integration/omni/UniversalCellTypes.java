package cn.dancingsnow.neoecoprototype.integration.omni;

import cn.dancingsnow.neoecoae.all.NERegistries;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.network.chat.Component;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * ECO cell types owned by this addon.
 *
 * <p>eco's storage hosts build their type rows by enumerating {@code neoecoae:cell_type}
 * (see {@code ECOStorageSystemBlockEntity#createStorageTypeLines}), and they resolve a mounted
 * cell's row through {@code NERegistries.CELL_TYPE.getId(cellType)}. A bare {@code ECOCellType}
 * that is never registered therefore shows as an untranslated key in our own host and is invisible
 * on eco's own storage systems, so the universal matrix registers a real entry here.
 */
public final class UniversalCellTypes {
    private static final DeferredRegister<ECOCellType> CELL_TYPES =
            DeferredRegister.create(NERegistries.Keys.CELL_TYPE, NeoECOPrototype.MOD_ID);

    /** 256-type Omni matrix; distinct from eco's own omni and quantum omni entries. */
    public static final DeferredHolder<ECOCellType, ECOCellType> UNIVERSAL = CELL_TYPES.register(
            "universal",
            () -> ECOCellType.builder()
                    .desc(Component.translatable("eco_cell_type.neoecoprototype.universal").withColor(0x6FE3C4))
                    .typeCount(SimplifyUniversalStorageCellItem.TOTAL_TYPES)
                    .visible(true)
                    .build());

    public static void register(IEventBus modBus) {
        CELL_TYPES.register(modBus);
    }

    private UniversalCellTypes() {
    }
}

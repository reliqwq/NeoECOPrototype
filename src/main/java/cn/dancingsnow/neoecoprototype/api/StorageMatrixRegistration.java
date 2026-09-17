package cn.dancingsnow.neoecoprototype.api;

import appeng.api.stacks.AEKeyType;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoprototype.items.SimplifyStorageCellItem;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

/** Factory entry point for new finite matrices; legacy registrations intentionally do not use it. */
public final class StorageMatrixRegistration {
    private StorageMatrixRegistration() {
    }

    public static Supplier<SimplifyStorageCellItem> registerFinite(
            DeferredRegister<Item> items,
            String registryName,
            StorageMatrixDefinition definition,
            AEKeyType keyType,
            Supplier<ECOCellType> cellType) {
        if (definition.infinite()) {
            throw new IllegalArgumentException("Use registerInfinite for an infinite matrix");
        }
        if (!definition.id().getNamespace().equals(items.getRegistryKey().location().getNamespace())
                || !definition.id().getPath().equals(registryName)) {
            throw new IllegalArgumentException("Matrix definition id does not match item registration: "
                    + definition.id() + " vs " + registryName);
        }
        return items.register(registryName, () -> {
            var item = new SimplifyStorageCellItem(new Item.Properties().stacksTo(1), keyType, cellType,
                    definition.bytes(), definition.bytesPerType(), definition.totalTypes());
            StorageMatrixRegistry.register(definition, item);
            return item;
        });
    }
}

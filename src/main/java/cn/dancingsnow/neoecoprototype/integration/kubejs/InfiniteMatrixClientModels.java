package cn.dancingsnow.neoecoprototype.integration.kubejs;

import cn.dancingsnow.neoecoae.api.ECOCellModels;
import cn.dancingsnow.neoecoprototype.items.CustomInfiniteCellItem;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.ModelEvent;

/** Client-side model registration for KubeJS infinite storage matrices. */
public final class InfiniteMatrixClientModels {
    private InfiniteMatrixClientModels() {
    }

    /** Register the eco default cell model or each script's explicit override. */
    public static void registerCellModels(ResourceLocation defaultModel) {
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof CustomInfiniteCellItem custom) {
                ECOCellModels.register(custom, custom.getDriveModel() != null
                        ? custom.getDriveModel()
                        : defaultModel);
            }
        }
    }

    /** Register explicit KubeJS cell model overrides as standalone baked models. */
    public static void registerAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (var item : BuiltInRegistries.ITEM) {
            if (item instanceof CustomInfiniteCellItem custom && custom.getDriveModel() != null) {
                event.register(ModelResourceLocation.standalone(custom.getDriveModel()));
            }
        }
    }

    /**
     * Register the in-drive model of every KubeJS-created storage matrix.
     *
     * <p>Script-created finite matrices are plain {@code SimplifyStorageCellItem} instances, not
     * {@link CustomInfiniteCellItem}, so the loop above never sees them and their drive slot would
     * render without a model.
     */
    public static void registerScriptedCellModels() {
        int registered = 0;
        for (var entry : cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry.definitions().entrySet()) {
            ResourceLocation driveModel = entry.getKey().driveModel();
            if (driveModel != null) {
                ECOCellModels.register(entry.getValue(), driveModel);
                registered++;
                cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.debug(
                        "scripted matrix {} ({} {}) -> inventory {} / drive {}",
                        entry.getKey().id(), entry.getKey().type(), entry.getKey().size(),
                        entry.getKey().inventoryModel(), driveModel);
            }
        }
        cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.debug(
                "registered in-drive models for {} scripted storage matrices", registered);
    }

    /** Standalone models for the scripted drive overrides registered above. */
    public static void registerScriptedAdditionalModels(ModelEvent.RegisterAdditional event) {
        for (var entry : cn.dancingsnow.neoecoprototype.api.StorageMatrixRegistry.definitions().entrySet()) {
            ResourceLocation driveModel = entry.getKey().driveModel();
            if (driveModel != null) {
                event.register(ModelResourceLocation.standalone(driveModel));
            }
        }
    }
}

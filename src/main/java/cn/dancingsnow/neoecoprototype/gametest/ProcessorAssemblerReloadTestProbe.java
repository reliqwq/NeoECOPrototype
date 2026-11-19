package cn.dancingsnow.neoecoprototype.gametest;

import dev.latvian.mods.kubejs.core.RecipeManagerKJS;
import dev.latvian.mods.kubejs.core.ReloadableServerResourcesKJS;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.crafting.RecipeManager;

/** Optional KubeJS linkage stays out of the GameTest holder. No scripts or global resources are reloaded. */
public final class ProcessorAssemblerReloadTestProbe {
    private ProcessorAssemblerReloadTestProbe() {
    }

    public static void attachKubeJS(RecipeManager manager, GameTestHelper helper) {
        ((RecipeManagerKJS) manager).kjs$setResources((ReloadableServerResourcesKJS)
                helper.getLevel().getServer().getServerResources().managers());
    }
}

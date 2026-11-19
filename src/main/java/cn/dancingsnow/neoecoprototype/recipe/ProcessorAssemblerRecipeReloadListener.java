package cn.dancingsnow.neoecoprototype.recipe;

import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.neoforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.List;

public final class ProcessorAssemblerRecipeReloadListener {
    private ProcessorAssemblerRecipeReloadListener() {
    }

    public static void register(AddReloadListenerEvent event) {
        RecipeManager manager = event.getServerResources().getRecipeManager();
        event.addListener(new SimplePreparableReloadListener<Void>() {
            @Override
            protected Void prepare(ResourceManager resourceManager, ProfilerFiller profiler) {
                return null;
            }

            @Override
            protected void apply(Void unused, ResourceManager resourceManager, ProfilerFiller profiler) {
                if (!NeoECOPrototypeServerConfig.current(
                        NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER)) {
                    return;
                }
                List<RecipeHolder<?>> recipes = new ArrayList<>(manager.getRecipes());
                recipes.addAll(ProcessorAssemblerRecipes.derivedHolders(manager));
                manager.replaceRecipes(recipes);
            }
        });
    }
}

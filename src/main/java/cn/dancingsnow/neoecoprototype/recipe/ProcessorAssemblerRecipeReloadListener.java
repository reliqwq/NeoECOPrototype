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

/**
 * NeoForge 21.1.251 appends these listeners after RecipeManager. SimpleReloadInstance orders their
 * apply stages, so this sees KubeJS 2101.7.2-build.377's apply HEAD/TAIL recipe edits as well.
 * Generated holders are synchronized by the normal recipe packet; clients must not derive them again.
 * Each resource reload creates a fresh manager: removed sources and a disabled switch leave no stale
 * additions. Later mod listeners can still edit recipes; this is not a universal last-listener hook.
 */
public final class ProcessorAssemblerRecipeReloadListener extends SimplePreparableReloadListener<Void> {
    private final RecipeManager manager;

    public ProcessorAssemblerRecipeReloadListener(RecipeManager manager) {
        this.manager = manager;
    }

    public static void register(AddReloadListenerEvent event) {
        event.addListener(new ProcessorAssemblerRecipeReloadListener(
                event.getServerResources().getRecipeManager()));
    }

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
        List<RecipeHolder<?>> derived = ProcessorAssemblerRecipes.derivedHolders(manager);
        for (RecipeHolder<?> holder : derived) {
            if (!holder.value().getType().equals(ProcessorAssemblerRecipe.TYPE)) continue;
            if (holder.value() instanceof ProcessorAssemblerRecipe recipe && !recipe.hasItemOutput()
                    && !recipe.hasFluidOutput()) continue;
            recipes.removeIf(existing -> existing.id().equals(holder.id()));
            recipes.add(holder);
        }
        manager.replaceRecipes(recipes);
    }
}

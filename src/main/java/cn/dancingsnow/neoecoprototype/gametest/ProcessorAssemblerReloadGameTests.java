package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipeReloadListener;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.PathPackResources;
import net.minecraft.server.packs.repository.PackSource;
import net.minecraft.server.packs.resources.MultiPackResourceManager;
import net.minecraft.server.packs.resources.SimpleReloadInstance;
import net.minecraft.util.Unit;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeManager;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

/** Resource parsing and ordered listener reloads, without replacing the live server's recipe manager. */
@GameTestHolder(NeoECOPrototype.MOD_ID)
@PrefixGameTestTemplate(false)
public final class ProcessorAssemblerReloadGameTests {
    private static final ResourceLocation DERIVED = ResourceLocation.parse(
            "neoecoprototype:derived/inscriber/reload_probe/processor");

    @GameTest(template = "empty", timeoutTicks = 100)
    public static void derivedRecipesFollowResourceReload(GameTestHelper helper) throws Exception {
        var derive = NeoECOPrototypeServerConfig.DERIVE_PROCESSOR_RECIPES_FROM_INSCRIBER;
        var blacklist = NeoECOPrototypeServerConfig.DISABLED_PROCESSOR_RECIPES;
        boolean previous = derive.get();
        var previousBlacklist = blacklist.get();
        Path root = Files.createTempDirectory("processor-reload-");
        try {
            derive.set(true);
            blacklist.set(List.of());
            Path recipe = root.resolve("data/reload_probe/recipe/processor.json");
            Files.createDirectories(recipe.getParent());
            Files.writeString(recipe, source("minecraft:gold_ingot"));
            RecipeManager manager = new RecipeManager(helper.getLevel().registryAccess());
            manager.injectContext(ICondition.IContext.EMPTY, helper.getLevel().registryAccess());
            if (ModList.get().isLoaded("kubejs")) {
                ProcessorAssemblerReloadTestProbe.attachKubeJS(manager, helper);
            }
            reload(manager, root);
            assertRecipe(helper, manager, Items.GOLD_INGOT);
            reload(manager, root);
            assertRecipe(helper, manager, Items.GOLD_INGOT);

            Files.writeString(recipe, source("minecraft:diamond"));
            reload(manager, root);
            assertRecipe(helper, manager, Items.DIAMOND);
            Files.delete(recipe);
            reload(manager, root);
            helper.assertTrue(manager.byKey(DERIVED).isEmpty(), "deleted source retained derived holder");

            Files.writeString(recipe, source("minecraft:gold_ingot"));
            reload(manager, root);
            assertRecipe(helper, manager, Items.GOLD_INGOT);
            derive.set(false);
            reload(manager, root);
            helper.assertTrue(manager.byKey(DERIVED).isEmpty(), "disabled derivation retained derived holder");
            helper.assertTrue(manager.byKey(ResourceLocation.parse("reload_probe:processor")).isPresent(),
                    "disabled derivation removed source recipe");

            derive.set(true);
            blacklist.set(List.of("minecraft:emerald"));
            reload(manager, root);
            helper.assertTrue(manager.byKey(DERIVED).isEmpty(), "blacklisted output gained derived holder");
            blacklist.set(List.of());
            reload(manager, root);
            assertRecipe(helper, manager, Items.GOLD_INGOT);
            helper.succeed();
        } finally {
            derive.set(previous);
            blacklist.set(previousBlacklist);
            try (var paths = Files.walk(root)) {
                for (Path path : paths.sorted(java.util.Comparator.reverseOrder()).toList()) {
                    Files.delete(path);
                }
            }
        }
    }

    private static void reload(RecipeManager manager, Path root) {
        var pack = new PathPackResources(new PackLocationInfo("reload_probe", Component.literal("reload probe"),
                PackSource.BUILT_IN, Optional.empty()), root);
        try (var resources = new MultiPackResourceManager(PackType.SERVER_DATA, List.of(pack))) {
            SimpleReloadInstance.create(resources,
                    List.of(manager, new ProcessorAssemblerRecipeReloadListener(manager)),
                    Runnable::run, Runnable::run, CompletableFuture.completedFuture(Unit.INSTANCE), false)
                    .done().join();
        }
    }

    private static void assertRecipe(GameTestHelper helper, RecipeManager manager,
            net.minecraft.world.item.Item material) {
        var recipes = manager.getAllRecipesFor(ModRegistration.PROCESSOR_ASSEMBLER_RECIPE_TYPE.get());
        helper.assertTrue(recipes.size() == 1, "reload produced " + recipes.size() + " assembler recipes, expected 1");
        helper.assertTrue(manager.byKey(DERIVED).isPresent(), "reload omitted stable derived id " + DERIVED);
        ProcessorAssemblerRecipe recipe = recipes.getFirst().value();
        helper.assertTrue(recipe.matches(new ItemStack[] {new ItemStack(material),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT)}),
                "reloaded recipe rejected material " + material);
        var oldMaterial = material == Items.DIAMOND ? Items.GOLD_INGOT : Items.DIAMOND;
        helper.assertTrue(!recipe.matches(new ItemStack[] {new ItemStack(oldMaterial),
                new ItemStack(Items.REDSTONE), new ItemStack(Items.IRON_INGOT)}),
                "reloaded recipe retained stale material " + oldMaterial);
    }

    private static String source(String material) {
        return """
                {"type":"ae2:inscriber","ingredients":{
                  "middle":{"item":"%s"},"top":{"item":"minecraft:redstone"},
                  "bottom":{"item":"minecraft:iron_ingot"}},"mode":"press",
                  "result":{"id":"minecraft:emerald","count":1}}
                """.formatted(material);
    }
}

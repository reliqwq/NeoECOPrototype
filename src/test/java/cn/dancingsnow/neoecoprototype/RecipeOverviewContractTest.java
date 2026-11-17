package cn.dancingsnow.neoecoprototype;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.zip.ZipFile;
import java.util.regex.Pattern;
import org.junit.jupiter.api.Test;

final class RecipeOverviewContractTest {

    private static final Path RECIPES = Path.of("src/main/resources/data/neoecoprototype/recipe");
    private static final Path OVERVIEW = Path.of("docs/配方总览.md");
    private static final Pattern RECIPE_ROW = Pattern.compile("\\| `[^`]+\\.json` \\|");
    private static final Pattern HEADER_COUNT = Pattern.compile("共 \\*\\*(\\d+) 条\\*\\*。");
    private static final Pattern TABLE_TOTAL = Pattern.compile("\\| \\*\\*合计\\*\\* \\|  \\| \\*\\*(\\d+)\\*\\* \\|");

    @Test
    void overviewMatchesRecipeFilesAndShippedJar() throws Exception {
        long recipeFiles;
        try (var files = Files.walk(RECIPES)) {
            recipeFiles = files.filter(Files::isRegularFile)
                    .filter(path -> path.toString().endsWith(".json"))
                    .count();
        }

        String overview = Files.readString(OVERVIEW);
        long listedRecipes = RECIPE_ROW.matcher(overview).results().count();
        long declaredRecipes = declaredCount(HEADER_COUNT, overview, "recipe overview header count is missing");
        long tableRecipes = declaredCount(TABLE_TOTAL, overview, "recipe overview table total is missing");
        long jarRecipes;
        boolean gameTestClassShipped;
        try (var jar = new ZipFile(Path.of(System.getProperty("neoecoprototype.test.jar")).toFile())) {
            var entries = jar.stream().filter(entry -> !entry.isDirectory())
                    .map(entry -> entry.getName()).toList();
            jarRecipes = entries.stream()
                    .filter(name -> name.startsWith("data/neoecoprototype/recipe/") && name.endsWith(".json"))
                    .count();
            gameTestClassShipped = entries.contains(
                    "cn/dancingsnow/neoecoprototype/gametest/NeoECOPrototypeGameTests.class");
        }

        assertTrue(!gameTestClassShipped,
                "the release jar ships NeoECOPrototypeGameTests: players never run the suite, and a pack"
                        + " that wants it gets it from runGameTestServer through the source set, not from the jar");
        assertEquals(recipeFiles, listedRecipes,
                "recipe overview rows must match the bundled recipe JSON files");
        assertEquals(recipeFiles, declaredRecipes,
                "recipe overview header must match the bundled recipe JSON files");
        assertEquals(recipeFiles, tableRecipes,
                "recipe overview table total must match the bundled recipe JSON files");
        assertEquals(recipeFiles, jarRecipes,
                "shipped jar recipe JSON files must match the bundled recipe JSON files");
    }

    private static long declaredCount(Pattern pattern, String overview, String message) {
        var matcher = pattern.matcher(overview);
        if (!matcher.find()) {
            throw new AssertionError(message);
        }
        return Long.parseLong(matcher.group(1));
    }
}

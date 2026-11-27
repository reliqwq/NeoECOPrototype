package cn.dancingsnow.neoecoprototype;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The processor table in docs/processor-assembler-recipes.md is what the two guide pages and JEI repeat to
 * the player, and the shipped JSON is what the machine actually accepts. Nothing compared them: the crossover
 * row for AE2 Lightning Tech was documented as overload crystal and shipped as overload circuit board, and
 * both the count guard (which reads a different file) and the recipe-loading guard (which skips every recipe
 * carrying a condition) stayed green the whole time.
 *
 * <p>So the doc is parsed and each line is matched against the recipe file that produces that output. The
 * notation is the file's own: a tag is written {@code #c:silicon}, a plain id is an item.
 */
class ProcessorAssemblerDocContractTest {

    private static final Path DOC = Path.of("docs/processor-assembler-recipes.md");
    private static final Path RECIPES = Path.of("src/main/resources/data/neoecoprototype/recipe/processor_assembler");
    private static final Pattern PAIR = Pattern.compile("^\\s*(.+?)\\s*\\n\\s*->\\s*(\\S+)(.*)$",
            Pattern.MULTILINE);

    @Test
    void everyDocumentedProcessorRowIsTheShippedRecipe() throws Exception {
        String block = processorBlock();
        List<String[]> rows = new ArrayList<>();
        Matcher matcher = PAIR.matcher(block);
        while (matcher.find()) {
            rows.add(new String[] {matcher.group(1).trim(), matcher.group(2).trim()});
        }
        assertEquals(5, rows.size(), "the processor block of " + DOC + " lists " + rows.size()
                + " output lines, expected the five processors");

        for (String[] row : rows) {
            JsonObject recipe = recipeFor(row[1]);
            List<String> shipped = new ArrayList<>();
            JsonArray ingredients = recipe.getAsJsonArray("ingredients");
            for (int entry = 0; entry < ingredients.size(); entry++) {
                JsonObject one = ingredients.get(entry).getAsJsonObject();
                shipped.add(one.has("tag") ? "#" + one.get("tag").getAsString()
                        : one.get("item").getAsString());
            }
            List<String> documented = Arrays.stream(row[0].split("\\s*\\+\\s*"))
                    .filter(text -> !text.isEmpty()).sorted().toList();
            assertEquals(documented, shipped.stream().sorted().toList(),
                    DOC + " promises [" + row[0] + "] for " + row[1] + " but the shipped file says " + shipped);
        }
    }

    /**
     * The crossover row only exists when lightning is installed, and the doc says so in parentheses. That
     * promise is the whole shape of a gated recipe, so the condition is read off the file rather than trusted.
     */
    @Test
    void theLightningRowIsGatedOnLightningBeingLoaded() throws Exception {
        JsonObject recipe = recipeFor("ae2lt:overload_processor");
        JsonArray conditions = recipe.getAsJsonArray("neoforge:conditions");
        assertNotNull(conditions, "the overload crossover recipe carries no conditions at all, so it would"
                + " load without AE2 Lightning Tech and reference a missing item");
        boolean gated = false;
        for (int entry = 0; entry < conditions.size(); entry++) {
            JsonObject one = conditions.get(entry).getAsJsonObject();
            if ("neoforge:mod_loaded".equals(one.get("type").getAsString())
                    && "ae2lt".equals(one.get("modid").getAsString())) {
                gated = true;
            }
        }
        assertTrue(gated, "the overload crossover recipe is not gated on ae2lt: " + conditions);
    }

    /** The fenced text block that lists the five processors, chosen by content rather than by line number. */
    private static String processorBlock() throws IOException {
        String doc = Files.readString(DOC);
        String[] fences = doc.split("```");
        for (int block = 1; block < fences.length; block += 2) {
            if (fences[block].contains("logic_processor") && fences[block].contains("overload_processor")) {
                return fences[block];
            }
        }
        throw new AssertionError(DOC + " has no fenced block listing both the logic and the overload processor");
    }

    private static JsonObject recipeFor(String outputId) throws IOException {
        try (var files = Files.list(RECIPES)) {
            for (Path file : files.sorted().toList()) {
                JsonObject recipe = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
                if (!"neoecoprototype:processor_assembler".equals(recipe.get("type").getAsString())) {
                    continue;
                }
                var result = recipe.getAsJsonObject("result");
                if (result != null && result.has("id") && result.get("id").getAsString().equals(outputId)) {
                    return recipe;
                }
            }
        }
        throw new AssertionError("no recipe in " + RECIPES + " produces " + outputId);
    }
}

package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * The self-growing family's bills, and the fact that each cell's crafting bill and its disassembly bill are
 * the same items.
 *
 * <p>Read off the data files rather than in-game because the two halves live in two different systems: the
 * crafting bill is a recipe file (vanilla shapeless, or Ars Nouveau's enchanting apparatus for the source
 * member), while what comes back when a cell is taken apart is an AE2 {@code storage_cell_disassembly} file
 * that the game never cross-checks against it. Repricing one side and forgetting the other is silent - a
 * cell that costs three things and returns four - and no game test reads both, so this is the place where
 * the pair is compared.</p>
 *
 * <p>The 1.4.6 change is the component of the first two members: {@code
 * neoecoae:eco_infinite_cell_component} (whose own bill is 128 of the 256M components, 64 ingots, 64
 * processors and 64,000 mB of cryotheum solution) came out and {@code neoecoae:eco_cell_component_64m} (3 of
 * the 16M, 48 ingots, 16 processors, 1 crystal ingot) went in, on both cells.</p>
 *
 * <p>The source member is priced off that ladder on purpose: the infinite housing is the reagent and the
 * seven pedestal items are an energized superconductive ingot, the treasure-crab token, a source gem block,
 * a magebloom seed and water, fire and earth essence. What it still shares with the family is the rule
 * asserted here - the disassembly bill is the crafting bill. The ingot took kouooki's doll out of this bill
 * on 2026-11-11: a doll you stumble onto is an easter egg, and an easter egg inside a required recipe turns
 * a crossover feature into a chore whose first step is an item the pack never explains.</p>
 */
final class SelfGrowingCellBillContractTest {

    private static final Path ROOT = Path.of("src/main/resources/data/neoecoprototype/recipe");
    private static final String HOUSING = "neoecoprototype:simplify_concrete_storage_matrix_housing";
    private static final String LATTICE = "neoecoprototype:simplify_green_crystal_matrix";
    private static final String COMPONENT_64M = "neoecoae:eco_cell_component_64m";
    private static final String COMPONENT_INFINITE = "neoecoae:eco_infinite_cell_component";
    private static final String SINGULARITY = "ae2:singularity";
    private static final String SKY_STONE_DUST = "ae2:sky_dust";
    private static final String SUPERCONDUCTIVE_INGOT = "neoecoae:energized_superconductive_ingot";
    private static final String KOOOKI_DOLL = "neoecoprototype:fumo_doll_kouooki";
    private static final String ALAKARKINOS_TOKEN = "ars_nouveau:alakarkinos_token";
    private static final String SOURCE_GEM_BLOCK = "ars_nouveau:source_gem_block";
    /** The seed is the crop block item; {@code ars_nouveau:magebloom} is the flower the crop grows into. */
    private static final String MAGEBLOOM_SEED = "ars_nouveau:magebloom_crop";
    private static final String WATER_ESSENCE = "ars_nouveau:water_essence";
    private static final String FIRE_ESSENCE = "ars_nouveau:fire_essence";
    private static final String EARTH_ESSENCE = "ars_nouveau:earth_essence";

    private static JsonObject read(Path file) throws Exception {
        try (Reader reader = Files.newBufferedReader(file)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }

    /**
     * What a cell costs, in the order its file writes it - the reagent first for the apparatus, since it is
     * the one piece the player must supply before the pedestals can fire.
     */
    private static List<String> craftingBill(JsonObject recipe) {
        String type = recipe.get("type").getAsString();
        if ("ars_nouveau:enchanting_apparatus".equals(type)) {
            List<String> ids = new ArrayList<>();
            ids.add(ingredientId(recipe.getAsJsonObject("reagent")));
            for (var element : recipe.getAsJsonArray("pedestalItems")) {
                ids.add(ingredientId(element.getAsJsonObject()));
            }
            return ids;
        }
        assertEquals("minecraft:crafting_shapeless", type,
                "a self-growing matrix is crafted shapelessly or in Ars Nouveau's enchanting apparatus");
        List<String> ids = new ArrayList<>();
        for (var element : recipe.getAsJsonArray("ingredients")) {
            ids.add(ingredientId(element.getAsJsonObject()));
        }
        return ids;
    }

    private static String ingredientId(JsonObject entry) {
        return entry.has("item") ? entry.get("item").getAsString() : "tag:" + entry.get("tag").getAsString();
    }

    /** What AE2 hands back when the cell is taken apart. */
    private static List<String> disassemblyBill(JsonObject recipe) {
        assertEquals("ae2:storage_cell_disassembly", recipe.get("type").getAsString());
        List<String> ids = new ArrayList<>();
        for (var element : recipe.getAsJsonArray("cell_disassembly_items")) {
            ids.add(element.getAsJsonObject().get("id").getAsString());
        }
        return ids;
    }

    @Test
    void bothCellsArePricedOnTheSixtyFourMComponent() throws Exception {
        for (String cell : List.of("simplify_singularity_cell", "simplify_sky_stone_dust_cell")) {
            var bill = craftingBill(read(ROOT.resolve(cell + ".json")));
            assertTrue(bill.contains(COMPONENT_64M), cell + " does not name the 64M component: " + bill);
            assertFalse(bill.contains(COMPONENT_INFINITE), cell + " still names the infinite component:" + bill);
        }
        var disassembly = disassemblyBill(
                read(ROOT.resolve("disassembly/simplify_singularity_cell.json")));
        assertFalse(disassembly.contains(COMPONENT_INFINITE),
                "the singularity cell's disassembly still returns the infinite component: " + disassembly);
    }

    @Test
    void takingAPartReturnsExactlyWhatItCost() throws Exception {
        for (String cell : List.of("simplify_singularity_cell", "simplify_sky_stone_dust_cell",
                "simplify_source_growing_cell")) {
            var cost = craftingBill(read(ROOT.resolve(cell + ".json")));
            var back = disassemblyBill(read(ROOT.resolve("disassembly/" + cell + ".json")));
            assertEquals(sorted(cost), sorted(back),
                    cell + " costs " + cost + " but returns " + back);
        }
    }

    @Test
    void eachCellSeedsItselfAndSharesTheOtherTwoParts() throws Exception {
        var singularity = craftingBill(read(ROOT.resolve("simplify_singularity_cell.json")));
        var dust = craftingBill(read(ROOT.resolve("simplify_sky_stone_dust_cell.json")));
        assertEquals(List.of(HOUSING, COMPONENT_64M, LATTICE, SINGULARITY), singularity);
        assertEquals(List.of(HOUSING, COMPONENT_64M, LATTICE, SKY_STONE_DUST), dust);
    }

    /**
     * The source member is not on the component ladder, and every one of its parts but the housing lives in
     * another mod, so the file is gated: without those conditions it would fail to load on every install
     * that lacks Ars Nouveau, which takes the whole recipe reload down with it rather than just hiding one
     * item.
     */
    @Test
    void theSourceCellIsBuiltInTheApparatusBehindItsModGate() throws Exception {
        var recipe = read(ROOT.resolve("simplify_source_growing_cell.json"));
        assertEquals("ars_nouveau:enchanting_apparatus", recipe.get("type").getAsString(),
                "the source cell is built in Ars Nouveau's enchanting apparatus");
        assertFalse(craftingBill(recipe).contains(KOOOKI_DOLL),
                "an easter egg cannot be allowed back into a required bill: " + craftingBill(recipe));
        assertEquals(
                List.of(HOUSING, SUPERCONDUCTIVE_INGOT, ALAKARKINOS_TOKEN, SOURCE_GEM_BLOCK, MAGEBLOOM_SEED,
                        WATER_ESSENCE, FIRE_ESSENCE, EARTH_ESSENCE),
                craftingBill(recipe));
        assertTrue(recipe.getAsJsonArray("pedestalItems").size() <= 8,
                "the apparatus has eight pedestals; this recipe asks for more: "
                        + recipe.getAsJsonArray("pedestalItems"));
        var conditions = recipe.getAsJsonArray("neoforge:conditions");
        List<String> mods = new ArrayList<>();
        for (var element : conditions) {
            mods.add(element.getAsJsonObject().get("modid").getAsString());
        }
        assertEquals(List.of("arseng", "ars_nouveau"), mods,
                "the source cell names ars_nouveau items, so both mods are conditions");
    }

    private static List<String> sorted(List<String> ids) {
        var copy = new ArrayList<>(ids);
        java.util.Collections.sort(copy);
        return copy;
    }
}

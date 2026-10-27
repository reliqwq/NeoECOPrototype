package cn.dancingsnow.neoecoprototype.integration.mekanism;

import com.google.gson.JsonParser;
import com.mojang.serialization.JsonOps;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import net.neoforged.fml.ModList;
import net.neoforged.neoforge.common.conditions.ICondition;
import net.neoforged.neoforge.common.conditions.ItemExistsCondition;
import net.neoforged.neoforge.common.conditions.ModLoadedCondition;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MilkInfusionRecipeConditionsTest {
    @BeforeAll static void bootstrap() { MilkInfusionTestBootstrap.initialize(); }

    @ParameterizedTest @ValueSource(ints = {0, 1, 2, 3, 4, 5, 6, 7})
    void recipeRequiresAllThreeModsAndTheCoreRegisteredOutput(int loadedMask) throws Exception {
        var json = JsonParser.parseString(Files.readString(Path.of(
            "src/main/resources/data/neoecoprototype/recipe/compat/useless_stretcher/infinite_sand_cell.json"))).getAsJsonObject();
        var conditions = json.getAsJsonArray("neoforge:conditions");
        assertEquals(4, conditions.size());
        List<String> mods = List.of("neoecoae", "mekanism", "useless_stretcher");
        ModList installed = mock(ModList.class);
        for (int i = 0; i < mods.size(); i++) when(installed.isLoaded(mods.get(i))).thenReturn((loadedMask & (1 << i)) != 0);
        try (var modList = mockStatic(ModList.class)) {
            modList.when(ModList::get).thenReturn(installed);
            boolean enabled = true;
            for (int i = 0; i < 3; i++) {
                var condition = conditions.get(i).getAsJsonObject();
                assertEquals("neoforge:mod_loaded", condition.get("type").getAsString());
                enabled &= ModLoadedCondition.CODEC.codec().parse(JsonOps.INSTANCE, condition).getOrThrow()
                    .test(ICondition.IContext.EMPTY);
            }
            assertEquals(loadedMask == 7, enabled);
        }
        var itemGate = conditions.get(3).getAsJsonObject();
        assertEquals("neoforge:item_exists", itemGate.get("type").getAsString());
        assertTrue(ItemExistsCondition.CODEC.codec().parse(JsonOps.INSTANCE, itemGate).getOrThrow().test(ICondition.IContext.EMPTY));
        assertFalse(new ItemExistsCondition("milk_test:missing_core_output").test(ICondition.IContext.EMPTY));
        assertEquals("neoecoae:eco_infinite_sand_cell", json.getAsJsonObject("output").get("id").getAsString());
        assertEquals(1000, json.getAsJsonObject("chemical_input").get("amount").getAsInt());
        assertFalse(json.get("per_tick_usage").getAsBoolean());
    }
}

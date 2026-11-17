package cn.dancingsnow.neoecoprototype.client;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The channel module is one directional cube whose top face carries art that points somewhere: the blockstate
 * turns the whole model by y, so the only place the top's own orientation can be fixed is the model's up face.
 *
 * <p>That fix is invisible in the art file and easy to lose - re-exporting the model from Blockbench, or
 * switching the parent back to {@code block/cube}, puts the top back the other way with every other face
 * untouched and no compiler noticing. So the shape is read off the shipped JSON.
 */
class ChannelModuleTopFaceTest {

    private static final Path ASSETS
            = Path.of("src/main/resources/assets/neoecoprototype");
    private static final Path MODEL = ASSETS.resolve("models/block/subsystem_channel_module.json");
    private static final Path BLOCKSTATE = ASSETS.resolve("blockstates/subsystem_channel_module.json");

    @Test
    void theBlockstateTurnsTheModelRatherThanTheArt() throws Exception {
        JsonObject variants = json(BLOCKSTATE).getAsJsonObject("variants");
        assertEquals(4, variants.size(),
                "the module's blockstate has " + variants.size() + " keys, expected one per facing");
        for (String facing : new String[]{"north", "east", "south", "west"}) {
            JsonObject one = variants.getAsJsonObject("facing=" + facing);
            assertEquals("neoecoprototype:block/subsystem_channel_module",
                    one.get("model").getAsString(), facing + " does not point at our model");
            int y = one.has("y") ? one.get("y").getAsInt() : 0;
            assertEquals(switch (facing) {
                case "north" -> 0;
                case "east" -> 90;
                case "south" -> 180;
                default -> 270;
            }, y, facing + " turns the model by " + y + ", so the top face cannot be fixed here");
        }
    }

    @Test
    void theTopFaceIsTurnedHalfAroundAndNothingElseIs() throws Exception {
        JsonObject model = json(MODEL);
        assertFalse(model.has("parent") && "minecraft:block/cube".equals(model.get("parent").getAsString()),
                "the model is back to parent block/cube, which has no way to turn one face - the top would"
                        + " read half a turn off");
        assertEquals(1, model.getAsJsonArray("elements").size(),
                "this block is one cube; more elements mean the guard below is checking the wrong element");
        JsonObject faces = model.getAsJsonArray("elements").get(0).getAsJsonObject().getAsJsonObject("faces");
        assertEquals(6, faces.size(), "faces present: " + faces.keySet());
        int rotation = faces.getAsJsonObject("up").get("rotation").getAsInt();
        // Minecraft's parser takes degrees and rejects anything else outright - "rotation": 2 parses as a
        // model error, the block loses every face, and the guard below would have been green reading the 2
        // back out of the same file it wrote. So the legal set is asserted before the value is.
        assertTrue(rotation == 0 || rotation == 90 || rotation == 180 || rotation == 270,
                "the up face carries rotation " + rotation + ", which Minecraft's model parser rejects: only"
                        + " 0/90/180/270 are allowed, and a rejected model renders the block missing-texture");
        assertEquals(180, rotation, "the top face is not turned 180 degrees, so the art's own direction lands"
                + " on the wrong side of the block");
        for (String face : new String[]{"down", "north", "south", "east", "west"}) {
            assertFalse(faces.getAsJsonObject(face).has("rotation"),
                    face + " now carries a rotation too, which this block's art does not ask for");
        }
    }

    @Test
    void theSixFacesStillNameTheTexturesWeShip() throws Exception {
        JsonObject textures = json(MODEL).getAsJsonObject("textures");
        String base = "neoecoprototype:block/subsystem_channel_module/";
        for (String face : new String[]{"north", "south", "east", "west", "up"}) {
            String id = textures.get(face).getAsString();
            String name = id.substring(id.lastIndexOf('/') + 1);
            assertTrue(Files.isRegularFile(ASSETS.resolve("textures/block/subsystem_channel_module/"
                            + name + ".png")),
                    face + " points at " + id + ", which is not in our textures folder");
        }
        assertEquals(base + "front", textures.get("north").getAsString(), "the front face moved");
        assertEquals(base + "back", textures.get("south").getAsString(), "the back face moved");
        assertEquals(base + "top", textures.get("up").getAsString(), "the top face moved");
        assertEquals(base + "side", textures.get("east").getAsString(), "the east face moved");
        assertEquals(base + "side", textures.get("west").getAsString(), "the west face moved");
    }

    private static JsonObject json(Path path) throws Exception {
        return JsonParser.parseString(Files.readString(path)).getAsJsonObject();
    }
}

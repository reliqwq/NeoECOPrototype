package cn.dancingsnow.neoecoprototype.multiblock.reactor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;
import org.junit.jupiter.api.Test;

/**
 * The frame block draws one post plus a beam per strut neighbour. All of those parts live inside the same
 * block, so they have to <i>tile</i> the strut's surface: a beam that starts behind the post's face puts its
 * side quads on the same plane as the post's, and the GPU then picks between two different slices of the
 * texture for every pixel - the border showed a fine diagonal hatch and shimmered while the camera moved
 * (10-30 screenshot). Touching is fine, sharing volume is not.
 *
 * <p>Reads the shipped JSON and applies the same rotations the multipart blockstate declares, because a Java
 * copy of this geometry would drift from the files the client actually loads.
 */
final class ReactorFrameModelGeometryTest {
    private static final Path BLOCKSTATE = Path.of(
            "src/main/resources/assets/neoecoprototype/blockstates/reactor_frame.json");
    private static final Path MODELS = Path.of("src/main/resources/assets/neoecoprototype/models/block/reactor");
    /** Model space runs 0..16 on every axis, so a 90 degree turn mirrors one axis around this. */
    private static final float SIZE = 16f;

    @Test
    void everyBeamTilesThePostInsteadOfSharingItsSpace() throws Exception {
        float[] post = elementBox(MODELS.resolve("frame_post.json"));
        float[] beam = elementBox(MODELS.resolve("frame_beam.json"));

        Map<String, float[]> beams = new LinkedHashMap<>();
        readMultipart(beams, beam);
        assertTrue(beams.containsKey("north") && beams.containsKey("south") && beams.containsKey("east")
                        && beams.containsKey("west") && beams.containsKey("up") && beams.containsKey("down"),
                "the blockstate no longer draws a beam for all six strut flags; it currently draws "
                        + beams.keySet() + ". The frame would show gaps along those edges.");

        for (Map.Entry<String, float[]> entry : beams.entrySet()) {
            String direction = entry.getKey();
            float[] rail = entry.getValue();
            assertEquals(0f, overlapVolume(post, rail), 1e-4f, "the " + direction + " beam shares "
                    + overlapVolume(post, rail) + " cubic texels with the post. Both draw their side faces on"
                    + " the same plane there, so the strut's surface flickers - start the beam at the post's"
                    + " face instead of at the block centre.");
            assertTrue(sharedFaceArea(post, rail) > 0f, "the " + direction + " beam does not touch the post:"
                    + " its box " + describe(rail) + " leaves a gap against the post " + describe(post)
                    + ". The strut would show a seam at every block.");
            assertTrue(reachesBlockFace(rail), "the " + direction + " beam " + describe(rail) + " stops short"
                    + " of the block boundary. Neighbouring struts meet at that boundary, so the edge would"
                    + " show a gap between blocks.");
            assertTrue(pointsToward(direction, rail), "the " + direction + " beam " + describe(rail)
                    + " does not reach the " + direction + " side of the block. The flag says a strut"
                    + " neighbour is there, so the rail would be drawn into a side that has none (and the"
                    + " real neighbour would meet nothing).");
            assertEquals(2, axesMatchingPost(post, rail), "the " + direction + " beam " + describe(rail)
                    + " does not share the post's 6x6 cross-section " + describe(post) + ", so the strut"
                    + " steps in or out where a rail meets a joint.");
        }
    }

    /** The post is drawn by every frame block, so the joint exists wherever a flag says so. */
    private static void readMultipart(Map<String, float[]> beams, float[] beam) throws Exception {
        JsonObject state = JsonParser.parseString(Files.readString(BLOCKSTATE)).getAsJsonObject();
        boolean postDrawn = false;
        for (var entry : state.getAsJsonArray("multipart")) {
            JsonObject part = entry.getAsJsonObject();
            JsonObject apply = part.getAsJsonObject("apply");
            String model = apply.get("model").getAsString();
            if (!part.has("when")) {
                assertTrue(model.endsWith("frame_post"), "the unconditional part of the frame now draws "
                        + model + "; every frame block needs the joint drawn or the beams meet nothing.");
                postDrawn = true;
                continue;
            }
            JsonObject when = part.getAsJsonObject("when");
            assertEquals(1, when.size(), "a beam part keys on " + when + "; this test reads one strut flag"
                    + " per part, so a combined condition needs the reader widened before believing it.");
            String direction = when.keySet().iterator().next();
            assertEquals("true", when.get(direction).getAsString(), "the " + direction + " beam is drawn "
                    + when + " instead of on the flag being true.");
            assertTrue(model.endsWith("frame_beam"), "the " + direction + " flag now draws " + model
                    + " instead of the beam model.");
            int x = apply.has("x") ? apply.get("x").getAsInt() : 0;
            int y = apply.has("y") ? apply.get("y").getAsInt() : 0;
            assertTrue(x % 90 == 0 && y % 90 == 0 && (x == 0 || y == 0), "the " + direction + " beam is"
                    + " rotated by x=" + x + " y=" + y + "; this test follows one quarter turn at a time,"
                    + " around one axis. A compound turn needs the reader widened first.");
            beams.put(direction, rotate(beam, x, y));
        }
        assertTrue(postDrawn, "reactor_frame.json no longer draws frame_post unconditionally, so a frame"
                + " block standing alone would be invisible.");
    }

    private static float[] elementBox(Path file) throws Exception {
        JsonObject model = JsonParser.parseString(Files.readString(file)).getAsJsonObject();
        var elements = model.getAsJsonArray("elements");
        assertEquals(1, elements.size(), file.getFileName() + " has " + elements.size() + " elements; this"
                + " test reasons about one box per model, so a second one has to be read in here first.");
        JsonObject element = elements.get(0).getAsJsonObject();
        float[] from = triple(element.getAsJsonArray("from"));
        float[] to = triple(element.getAsJsonArray("to"));
        return new float[]{from[0], from[1], from[2], to[0], to[1], to[2]};
    }

    private static float[] triple(com.google.gson.JsonArray array) {
        return new float[]{array.get(0).getAsFloat(), array.get(1).getAsFloat(), array.get(2).getAsFloat()};
    }

    /**
     * The same quarter turns the blockstate JSON means. Vanilla builds them as a quaternion
     * {@code rotateYXZ(-y, -x, 0)} about the block centre ({@code BlockModelRotation}'s constructor), so
     * {@code x=90} lifts the model's south end (its {@code +z}) to the top and {@code y=90} turns that end
     * to the west.
     */
    private static float[] rotate(float[] box, int xDeg, int yDeg) {
        float[] turned = switch (xDeg) {
            case 0 -> box.clone();
            case 90 -> new float[]{box[0], box[2], SIZE - box[4], box[3], box[5], SIZE - box[1]};
            case 180 -> new float[]{box[0], SIZE - box[4], SIZE - box[5], box[3], SIZE - box[1], SIZE - box[2]};
            case 270 -> new float[]{box[0], SIZE - box[5], box[1], box[3], SIZE - box[2], box[4]};
            default -> fail("unsupported x rotation " + xDeg);
        };
        return switch (yDeg) {
            case 0 -> turned;
            case 90 -> new float[]{SIZE - turned[5], turned[1], turned[0],
                    SIZE - turned[2], turned[4], turned[3]};
            case 180 -> new float[]{SIZE - turned[3], turned[1], SIZE - turned[5],
                    SIZE - turned[0], turned[4], SIZE - turned[2]};
            case 270 -> new float[]{turned[2], turned[1], SIZE - turned[3],
                    turned[5], turned[4], SIZE - turned[0]};
            default -> {
                fail("unsupported y rotation " + yDeg);
                yield null;
            }
        };
    }

    private static float overlapVolume(float[] a, float[] b) {
        float volume = 1f;
        for (int axis = 0; axis < 3; axis++) {
            volume *= Math.max(0f, Math.min(a[axis + 3], b[axis + 3]) - Math.max(a[axis], b[axis]));
        }
        return volume;
    }

    /** Area of the plane the two boxes share; zero when they overlap in volume or stand apart. */
    private static float sharedFaceArea(float[] a, float[] b) {
        int touchingAxes = 0;
        float area = 1f;
        for (int axis = 0; axis < 3; axis++) {
            float low = Math.max(a[axis], b[axis]);
            float high = Math.min(a[axis + 3], b[axis + 3]);
            float overlap = high - low;
            if (overlap > 0f) {
                area *= overlap;
                continue;
            }
            if (overlap == 0f) {
                touchingAxes++;
                continue;
            }
            return 0f;
        }
        return touchingAxes == 1 ? area : 0f;
    }

    /** The rail has to end on the block face the strut flag names, or it is drawn to the wrong neighbour. */
    private static boolean pointsToward(String direction, float[] box) {
        return switch (direction) {
            case "north" -> box[2] == 0f;
            case "south" -> box[5] == SIZE;
            case "west" -> box[0] == 0f;
            case "east" -> box[3] == SIZE;
            case "down" -> box[1] == 0f;
            case "up" -> box[4] == SIZE;
            default -> fail("unknown strut direction " + direction);
        };
    }

    private static boolean reachesBlockFace(float[] box) {
        for (int axis = 0; axis < 3; axis++) {
            if (box[axis] == 0f || box[axis + 3] == SIZE) {
                return true;
            }
        }
        return false;
    }

    private static int axesMatchingPost(float[] post, float[] rail) {
        int matching = 0;
        for (int axis = 0; axis < 3; axis++) {
            if (post[axis] == rail[axis] && post[axis + 3] == rail[axis + 3]) {
                matching++;
            }
        }
        return matching;
    }

    private static String describe(float[] box) {
        return "[" + box[0] + "," + box[1] + "," + box[2] + " -> " + box[3] + "," + box[4] + "," + box[5] + "]";
    }
}

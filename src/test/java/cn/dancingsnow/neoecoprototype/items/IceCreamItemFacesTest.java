package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * TedXenon's 2026-11-18 delivery replaced both ice cream item faces. Neither mistake shows up in a recipe or
 * a tooltip - a wrong path renders the missing-texture square, and a bucket that picked up a fluid loader
 * would get part of her drawing repainted by NeoForge - so the wiring is pinned here.
 */
class IceCreamItemFacesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");

    @Test
    void theFoodFaceIsAPlainLayerZeroAndIsSixteenBySixteen() throws Exception {
        Path texture = ASSETS.resolve("textures/item/cryotheum_ice_cream.png");
        assertSquare(texture);
        var textures = readJson(ASSETS.resolve("models/item/cryotheum_ice_cream.json"))
                .getAsJsonObject("textures");
        assertEquals("neoecoprototype:item/cryotheum_ice_cream", textures.get("layer0").getAsString(),
                "the ice cream item does not show TedXenon's flat face");
    }

    /**
     * The bucket shows TedXenon's drawing whole, with no fluid painted over it - reliqwq's ruling of
     * 2026-11-18, "按照她的来，我们不要修改". That is NeoForge's own shape for a bucket with fixed contents
     * ({@code neoforge:item/bucket_milk} is a plain generated model, no loader), and the contents never
     * change here: the item is only ever handed out full, and both emptying paths swap it for a plain
     * bucket, which {@code everyBucketItemWeRegisterCarriesItsFluid} already pins.
     */
    @Test
    void theBucketIsHerWholeDrawingAndCarriesNoLoader() throws Exception {
        Path texture = ASSETS.resolve("textures/item/cryotheum_ice_cream_bucket.png");
        assertSquare(texture);

        var model = readJson(ASSETS.resolve("models/item/cryotheum_ice_cream_bucket.json"));
        var obj = model.getAsJsonObject();
        assertTrue(!obj.has("loader") && !obj.has("fluid"),
                "the bucket must not carry the fluid_container loader: NeoForge paints its own fluid layer"
                        + " over the container art, and that would repaint part of TedXenon's drawing");
        assertEquals("minecraft:item/generated", obj.get("parent").getAsString(),
                "the bucket should be a plain generated item, like NeoForge's milk bucket");
        assertEquals("neoecoprototype:item/cryotheum_ice_cream_bucket",
                obj.getAsJsonObject("textures").get("layer0").getAsString(),
                "the bucket does not show TedXenon's container art");
    }

    /**
     * The coolant fluid draws TedXenon's block face now (reliqwq, 2026-11-18: "用那个冰淇淋方块"), so the two
     * fluid pngs must be gone and the block face must be the name the atlas and the fluid model both use.
     * Nothing on the server side notices a fluid whose sprite is unlisted - it renders as the missing sprite.
     */
    @Test
    void theCoolantFluidIsHerBlockFaceAndNothingNamesTheRetiredPair() throws IOException {
        for (String retired : new String[]{"cryotheum_ice_cream_still", "cryotheum_ice_cream_flow"}) {
            assertTrue(!Files.exists(ASSETS.resolve("textures/block/" + retired + ".png")),
                    "textures/block/" + retired + ".png is unreferenced now and must not ship");
        }
        String atlas = Files.readString(
                Path.of("src/main/resources/assets/minecraft/atlases/blocks.json"), StandardCharsets.UTF_8);
        assertTrue(atlas.contains("neoecoprototype:block/cryotheum_ice_cream_block"),
                "the block atlas does not stitch the face the coolant now uses, so the fluid and its bucket"
                        + " get no sprite");
        String fluidModel = Files.readString(ASSETS.resolve("models/block/cryotheum_ice_cream.json"),
                StandardCharsets.UTF_8);
        assertTrue(fluidModel.contains("neoecoprototype:block/cryotheum_ice_cream_block"),
                "the coolant's own block model still breaks into a particle that no longer exists");
        try (var paths = Files.walk(Path.of("src/main"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString();
                if (!(name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".mcmeta"))) {
                    continue;
                }
                String text = Files.readString(path, StandardCharsets.UTF_8);
                assertTrue(!text.contains("cryotheum_ice_cream_still")
                                && !text.contains("cryotheum_ice_cream_flow"),
                        path + " still names one of the two fluid faces the block face replaced");
            }
        }
    }

    /** The other half: nothing may still point at the layered glass this delivery retired. */
    @Test
    void theLayeredSundaeGlassIsGoneFromEveryReferenceRoot() throws IOException {
        try (var paths = Files.walk(Path.of("src/main"))) {
            for (Path path : paths.filter(Files::isRegularFile).toList()) {
                String name = path.getFileName().toString();
                if (!(name.endsWith(".java") || name.endsWith(".json") || name.endsWith(".mcmeta")
                        || name.endsWith(".md"))) {
                    continue;
                }
                assertTrue(!Files.readString(path, StandardCharsets.UTF_8).contains("sundae_glass"),
                        path + " still names the layered face the flat item texture replaced");
            }
        }
        assertTrue(!Files.exists(ASSETS.resolve("textures/item/cryotheum_ice_cream")),
                "textures/item/cryotheum_ice_cream/ is unreferenced now and must not ship");
    }

    private static void assertSquare(Path texture) throws IOException {
        assertTrue(Files.isRegularFile(texture), texture + " is missing");
        BufferedImage image = ImageIO.read(texture.toFile());
        assertNotNull(image, texture + " is not a readable image");
        assertEquals(16, image.getWidth(), texture + " width");
        assertEquals(16, image.getHeight(), texture + " height");
    }

    private static JsonObject readJson(Path path) throws IOException {
        try (var reader = new InputStreamReader(Files.newInputStream(path), StandardCharsets.UTF_8)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        }
    }
}

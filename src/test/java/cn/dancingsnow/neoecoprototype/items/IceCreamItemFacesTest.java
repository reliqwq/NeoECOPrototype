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
 * TedXenon's 2026-11-18 delivery replaced both ice cream item faces, and each one is wired differently: the
 * food is a plain {@code layer0}, the bucket is the {@code base} slot of the {@code neoforge:fluid_container}
 * loader. Neither mistake shows up in a recipe or a tooltip - a wrong path renders the missing-texture
 * square, and a bucket that lost its loader stops carrying the animated fill - so the chain is pinned here.
 */
class IceCreamItemFacesTest {
    private static final Path ASSETS = Path.of("src/main/resources/assets/neoecoprototype");
    /**
     * The fluid layer is baked from NeoForge's own bucket mask: the loader draws the animated fluid only
     * where that mask is opaque, nowhere else. So the container art has to be drawn material under every
     * masked pixel, or the fill floats outside the pail.
     */
    private static final String FLUID_MASK = "/assets/neoforge/textures/item/mask/bucket_fluid_drip.png";

    @Test
    void theFoodFaceIsAPlainLayerZeroAndIsSixteenBySixteen() throws Exception {
        Path texture = ASSETS.resolve("textures/item/cryotheum_ice_cream.png");
        assertSquare(texture);
        var textures = readJson(ASSETS.resolve("models/item/cryotheum_ice_cream.json"))
                .getAsJsonObject("textures");
        assertEquals("neoecoprototype:item/cryotheum_ice_cream", textures.get("layer0").getAsString(),
                "the ice cream item does not show TedXenon's flat face");
    }

    @Test
    void theBucketKeepsItsLoaderAndSuppliesOnlyTheContainerArt() throws Exception {
        Path texture = ASSETS.resolve("textures/item/cryotheum_ice_cream_bucket.png");
        assertSquare(texture);

        var model = readJson(ASSETS.resolve("models/item/cryotheum_ice_cream_bucket.json"));
        assertEquals("neoforge:item/bucket_drip", model.get("parent").getAsString(),
                "the bucket must keep NeoForge's bucket parent or it stops rendering as a fluid container");
        assertEquals("neoforge:fluid_container", model.get("loader").getAsString(),
                "the bucket must keep the fluid_container loader - it is what animates the fill");
        assertEquals("neoecoprototype:cryotheum_ice_cream", model.get("fluid").getAsString(),
                "the bucket's loader points at a fluid that is not ours");
        assertEquals("neoecoprototype:item/cryotheum_ice_cream_bucket",
                model.getAsJsonObject("textures").get("base").getAsString(),
                "the container art is not in the loader's base slot");

        var maskUrl = IceCreamItemFacesTest.class.getResource(FLUID_MASK);
        assertTrue(maskUrl != null, FLUID_MASK + " is not readable from the test classpath, so this guard"
                + " cannot see what the loader paints");
        BufferedImage mask = ImageIO.read(maskUrl);
        BufferedImage container = ImageIO.read(texture.toFile());
        int painted = 0;
        int floating = 0;
        StringBuilder where = new StringBuilder();
        for (int y = 0; y < mask.getHeight(); y++) {
            for (int x = 0; x < mask.getWidth(); x++) {
                if ((mask.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                painted++;
                if ((container.getRGB(x, y) >>> 24) == 0) {
                    floating++;
                    where.append(' ').append(x).append(',').append(y);
                }
            }
        }
        assertTrue(painted > 0, FLUID_MASK + " gave " + painted + " opaque pixels - the mask is empty,"
                + " so it proves nothing about the art");
        assertTrue(floating == 0, texture.getFileName() + " is empty under " + floating + " of the " + painted
                + " pixels the fluid mask paints (" + where + "): the animated fill would render outside the"
                + " drawn container");
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

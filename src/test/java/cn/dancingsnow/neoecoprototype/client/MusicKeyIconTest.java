package cn.dancingsnow.neoecoprototype.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;

/**
 * Music data is an AE key, so it has no item model to point at a texture: the only thing that draws it
 * is {@code MusicKeyRenderer}, which blits one png by hand. GameTest never loads that class and never
 * renders, so a wrong path or a texture that is not 16x16 ships as the pink-black missing texture in
 * every terminal without failing anything. This reads the file and the renderer's own source.
 */
final class MusicKeyIconTest {
    private static final Path ICON =
            Path.of("src/main/resources/assets/neoecoprototype/textures/item/music_data.png");
    private static final Path RENDERER =
            Path.of("src/main/java/cn/dancingsnow/neoecoprototype/client/key/MusicKeyRenderer.java");

    @Test
    void theMusicKeyHasAnIconTheRendererDraws() throws Exception {
        assertTrue(Files.isRegularFile(ICON), "the music key icon is missing at " + ICON
                + "; every terminal would draw the missing-texture checkerboard for music data");
        BufferedImage image = ImageIO.read(ICON.toFile());
        assertNotNull(image, "could not decode " + ICON + " as a png");
        assertEquals(16, image.getWidth(), "the key icon is " + image.getWidth() + " px wide; the renderer"
                + " blits it as a 16x16 sprite");
        assertEquals(16, image.getHeight(), "the key icon is 16x" + image.getHeight() + "; the renderer blits"
                + " it as a 16x16 sprite");

        int opaque = 0;
        int clear = 0;
        for (int y = 0; y < 16; y++) {
            for (int x = 0; x < 16; x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    clear++;
                } else {
                    opaque++;
                }
            }
        }
        assertTrue(clear > 0, "the icon is a full square with no transparent pixel. Keys are drawn on top of"
                + " terminal rows, so the icon has to carry its own shape");
        assertTrue(opaque >= 60, "only " + opaque + " pixels of the 256 are drawn; the icon would read as a"
                + " speck in the terminal");
    }

    @Test
    void theRendererPointsAtThatFile() throws Exception {
        String source = Files.readString(RENDERER);
        assertTrue(source.contains("textures/item/music_data.png"), "MusicKeyRenderer no longer names"
                + " textures/item/music_data.png, so the icon above is dead art and the key has no face");
    }
}

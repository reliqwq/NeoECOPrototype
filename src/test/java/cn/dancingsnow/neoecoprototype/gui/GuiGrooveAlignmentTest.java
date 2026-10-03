package cn.dancingsnow.neoecoprototype.gui;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins both directions of the GUI groove ruler on synthetic data, so the guard's ability to go red is
 * proven without a game JVM: a texture with drawn grooves and the rows a style declares must agree, and
 * moving one of them by the 2px we actually shipped in {@code 9f2e5e8} must be reported by name.
 *
 * <p>Only the pure halves are tested here - Gson is not on this source set's classpath, so
 * {@link GuiGrooveAlignment#sectionsFrom} is exercised by the GameTest that reads the real files.
 */
class GuiGrooveAlignmentTest {
    /** The eight rows l1_pattern_provider.png draws, measured from the shipped texture. */
    private static final List<Integer> ROWS = List.of(43, 61, 79, 131, 167, 185, 203, 225);
    private static final int HEIGHT = 251;

    private static byte[] textureWithGroovesAt(List<Integer> tops) throws Exception {
        var image = new BufferedImage(256, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < 256; x++) {
                image.setRGB(x, y, 0xFFFFFF);
            }
        }
        for (int top : tops) {
            for (int y = top; y < top + 16 && y < HEIGHT; y++) {
                for (int x = 8; x < 170; x++) {
                    image.setRGB(x, y, 0x202020);
                }
            }
        }
        var out = new ByteArrayOutputStream();
        ImageIO.write(image, "png", out);
        return out.toByteArray();
    }

    /** The provider's four sections as its style JSON describes them, with the bottoms already converted. */
    private static List<GuiGrooveAlignment.Section> provider(int storageTop, int inventoryTop, int hotbarTop) {
        return List.of(new GuiGrooveAlignment.Section("ENCODED_PATTERN", 27, 9, 43),
                new GuiGrooveAlignment.Section("STORAGE", 9, 9, storageTop),
                new GuiGrooveAlignment.Section("PLAYER_INVENTORY", 27, 9, inventoryTop),
                new GuiGrooveAlignment.Section("PLAYER_HOTBAR", 9, 9, hotbarTop));
    }

    @Test
    void readsTheGroovesATextureDraws() throws Exception {
        assertEquals(ROWS, GuiGrooveAlignment.drawnGrooveTops(textureWithGroovesAt(ROWS), 8, 170),
                "each drawn 16-row band must come back as its own row start");
        assertEquals(HEIGHT, GuiGrooveAlignment.textureHeight(textureWithGroovesAt(ROWS)));
    }

    @Test
    void breaksSlotsIntoRowsEighteenApart() {
        assertEquals(List.of(43, 61, 79),
                GuiGrooveAlignment.declaredTops(List.of(
                        new GuiGrooveAlignment.Section("ENCODED_PATTERN", 27, 9, 43))),
                "27 slots at nine per row are three rows, stepping by 18");
        assertEquals(List.of(120, 138),
                GuiGrooveAlignment.declaredTops(List.of(
                        new GuiGrooveAlignment.Section("VERTICAL_ONE", 1, 1, 120),
                        new GuiGrooveAlignment.Section("VERTICAL_TWO", 1, 1, 138))),
                "sections come back ordered from the top of the texture down");
    }

    @Test
    void agreesWithTheRowsTheShippedPairDeclares() throws Exception {
        var declared = GuiGrooveAlignment.declaredTops(provider(131, 167, 225));
        assertEquals(ROWS, declared);
        assertNull(GuiGrooveAlignment.firstMismatch(declared,
                        GuiGrooveAlignment.drawnGrooveTops(textureWithGroovesAt(ROWS), 8, 170)),
                "the shape this pair has today must be green");
    }

    @Test
    void aRowMovedByTwoPixelsIsNamed() throws Exception {
        var mismatch = GuiGrooveAlignment.firstMismatch(
                GuiGrooveAlignment.declaredTops(provider(129, 167, 225)),
                GuiGrooveAlignment.drawnGrooveTops(textureWithGroovesAt(ROWS), 8, 170));
        assertNotNull(mismatch, "the 131 -> 129 edit that 9f2e5e8 shipped must go red");
        assertTrue(mismatch.contains("row 4") && mismatch.contains("y=129") && mismatch.contains("y=131"),
                mismatch);
    }

    @Test
    void aMissingOrExtraRowIsReportedAsACount() throws Exception {
        var mismatch = GuiGrooveAlignment.firstMismatch(
                GuiGrooveAlignment.declaredTops(provider(131, 167, 225)),
                GuiGrooveAlignment.drawnGrooveTops(textureWithGroovesAt(ROWS.subList(0, ROWS.size() - 1)), 8, 170));
        assertNotNull(mismatch, "a texture that lost its hotbar groove must not read as aligned");
        assertTrue(mismatch.contains("8 slot row(s)") && mismatch.contains("7 groove row(s)"), mismatch);
    }

    @Test
    void aTextureWithNoGroovesChecksNothing() throws Exception {
        assertTrue(GuiGrooveAlignment.drawnGrooveTops(textureWithGroovesAt(List.of()), 8, 170).isEmpty(),
                "a blank panel has to surface as an empty reading, which the guard turns red");
    }

    @Test
    void ignoresBandsThatAreNotASlotRowTall() throws Exception {
        // The provider texture ends in a 7-row panel edge, and the powered one has a 4-row stub: neither is
        // a slot row, and reading either as one would report a phantom extra row.
        var withStrays = new BufferedImage(256, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < 256; x++) {
                withStrays.setRGB(x, y, 0xFFFFFF);
            }
        }
        paint(withStrays, 40, 16);
        paint(withStrays, 100, 7);
        paint(withStrays, 140, 4);
        var out = new ByteArrayOutputStream();
        ImageIO.write(withStrays, "png", out);
        assertEquals(List.of(40), GuiGrooveAlignment.drawnGrooveTops(out.toByteArray(), 8, 170),
                "only the 16-row band counts as a groove");
    }

    @Test
    void aHighlightRowInsideAGrooveDoesNotSplitItInTwo() throws Exception {
        // The powered interface's first row is drawn with a bright line through it, so the ruler has to see
        // one groove at 54 rather than a 54 and a 66 - and must not glue that over the two-row gap that
        // separates real neighbouring rows.
        var split = new BufferedImage(256, HEIGHT, BufferedImage.TYPE_INT_RGB);
        for (int y = 0; y < HEIGHT; y++) {
            for (int x = 0; x < 256; x++) {
                split.setRGB(x, y, 0xFFFFFF);
            }
        }
        paint(split, 54, 11);
        paint(split, 66, 4);
        paint(split, 72, 16);
        var out = new ByteArrayOutputStream();
        ImageIO.write(split, "png", out);
        assertEquals(List.of(54, 72), GuiGrooveAlignment.drawnGrooveTops(out.toByteArray(), 8, 170),
                "the 1-row highlight merges, the 2-row gap between grooves does not");
    }

    private static void paint(BufferedImage image, int top, int rows) {
        for (int y = top; y < top + rows && y < image.getHeight(); y++) {
            for (int x = 8; x < 170; x++) {
                image.setRGB(x, y, 0x202020);
            }
        }
    }
}

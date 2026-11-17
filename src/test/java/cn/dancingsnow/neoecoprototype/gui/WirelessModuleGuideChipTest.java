package cn.dancingsnow.neoecoprototype.gui;

import appeng.items.tools.GuideItem;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The module panel's guide chip borrows four things it does not own: AE2's guide id, AE2's help sprite,
 * AE2's toolbar strip, and a page that lives in our own resources. None of the four is a compile-time
 * symbol - a renamed guide, a redrawn sprite sheet, a resized strip, or a moved page would leave the chip
 * drawing the wrong picture and opening nothing.
 *
 * <p>Every value is read out of the shipped source and the shipped artifacts rather than copied into a table
 * here, so the guard moves with the code instead of freezing one version of it.
 */
class WirelessModuleGuideChipTest {

    private static final Path UI = Path.of(
            "src/main/java/cn/dancingsnow/neoecoprototype/items/WirelessChannelModuleUI.java");
    private static final Path PLATE = Path.of(
            "src/main/resources/assets/neoecoprototype/textures/gui/wireless_channel_module.png");
    private static final Path GUIDE_ROOT = Path.of(
            "src/main/resources/assets/neoecoprototype/ae2guide");

    @Test
    void theGuideIdTheChipHardcodesIsStillAe2sOwn() throws Exception {
        String source = Files.readString(UI);
        String[] guide = group(source,
                "AE2_GUIDE_ID\\s*=\\s*ResourceLocation\\.fromNamespaceAndPath\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        assertEquals(GuideItem.GUIDE_ID.toString(), guide[0] + ":" + guide[1],
                "AE2's GuideItem.GUIDE_ID is " + GuideItem.GUIDE_ID + ", so the chip's press would open "
                        + "nothing but an empty book");
    }

    @Test
    void thePageTheChipPointsAtExistsInBothLanguages() throws Exception {
        String page = group(Files.readString(UI),
                "GUIDE_PAGE\\s*=\\s*NeoECOPrototype\\.id\\(\"([^\"]+)\"\\)")[0];
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve(page + ".md")),
                GUIDE_ROOT.resolve(page + ".md") + " is not in the jar, so the chip has no page to open");
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve("_zh_cn").resolve(page + ".md")),
                "the Chinese twin of " + page + " is missing, so a zh client gets the English page");
    }

    @Test
    void theBorrowedHelpCellStillHasTheGlyphAndNotNeighbouringArt() throws Exception {
        String source = Files.readString(UI);
        String[] icon = group(source,
                "AE2_STATE_ICONS\\s*=\\s*ResourceLocation\\.fromNamespaceAndPath\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        int u = Integer.parseInt(group(source, "GUIDE_U\\s*=\\s*(\\d+)")[0]);
        int size = Integer.parseInt(group(source, "BUTTON_SIZE\\s*=\\s*(\\d+)")[0]);
        BufferedImage sheet = readClasspath("assets/" + icon[0] + "/" + icon[1]);
        assertEquals(256, sheet.getWidth(), icon[1] + " is no longer a 256 wide sheet");
        assertEquals(256, sheet.getHeight(), icon[1] + " is no longer a 256 tall sheet");
        int ink = 0;
        for (int dy = 0; dy < size; dy++) {
            for (int dx = 0; dx < size; dx++) {
                if ((sheet.getRGB(u + dx, dy) >>> 24) != 0) {
                    ink++;
                }
            }
        }
        // Icon.HELP's own cell measures 34 inked pixels out of 256; the band rejects both ways it can rot -
        // a cell that moved (0 ink, a blank square) and a cell that landed on solid art (256 ink).
        assertTrue(ink >= 16 && ink <= 64,
                "the 16x16 cell at u=" + u + " of " + icon[1] + " has " + ink + " inked pixels, so it is not"
                        + " AE2's help glyph any more");
    }

    @Test
    void theBorrowedToolbarStripIsStillTheSizeAndColourWeDrawItAt() throws Exception {
        String source = Files.readString(UI);
        String[] strip = group(source,
                "AE2_TOOLBAR_BG\\s*=\\s*ResourceLocation\\.fromNamespaceAndPath\\(\"([^\"]+)\",\\s*\"([^\"]+)\"\\)");
        BufferedImage sheet = readClasspath("assets/" + strip[0] + "/" + strip[1]);
        int width = Integer.parseInt(group(source, "TOOLBAR_WIDTH\\s*=\\s*(\\d+)")[0]);
        int height = Integer.parseInt(group(source, "TOOLBAR_HEIGHT\\s*=\\s*(\\d+)")[0]);
        assertEquals(width, sheet.getWidth(),
                strip[1] + " is " + sheet.getWidth() + " wide, so drawing it at " + width
                        + " would scale AE2's nine-slice borders");
        assertEquals(height, sheet.getHeight(),
                strip[1] + " is " + sheet.getHeight() + " tall, so drawing it at " + height
                        + " would scale AE2's nine-slice borders");

        // The strip's inner cell is AE2's plain fill; the plate's own fill is what the chip's neighbours are
        // painted with. If those two ever differ, the strip reads as a card stuck on the window rather than
        // as part of the machine, which is the whole reason for borrowing it.
        BufferedImage plate = ImageIO.read(PLATE.toFile());
        int plateFill = plate.getRGB(151, 2) & 0xFFFFFF;
        int stripFill = sheet.getRGB(10, 10) & 0xFFFFFF;
        assertEquals(plateFill, stripFill,
                "AE2's strip fill is " + Integer.toHexString(stripFill) + " but this plate fills with "
                        + Integer.toHexString(plateFill));
    }

    @Test
    void theButtonStillLandsInsideTheStripsOwnCell() throws Exception {
        String source = Files.readString(UI);
        int stripX = Integer.parseInt(group(source, "TOOLBAR_X\\s*=\\s*(-?\\d+)")[0]);
        int stripW = Integer.parseInt(group(source, "TOOLBAR_WIDTH\\s*=\\s*(\\d+)")[0]);
        int stripH = Integer.parseInt(group(source, "TOOLBAR_HEIGHT\\s*=\\s*(\\d+)")[0]);
        int buttonX = Integer.parseInt(group(source, "BUTTON_X\\s*=\\s*(-?\\d+)")[0]);
        int localTop = Integer.parseInt(group(source, "BUTTON_Y\\s*=\\s*TOOLBAR_Y\\s*\\+\\s*(\\d+)")[0]);
        int size = Integer.parseInt(group(source, "BUTTON_SIZE\\s*=\\s*(\\d+)")[0]);
        // AE2's strip has a one-pixel outline and a shadow band along the bottom, so its cell is local
        // x=2..width-1, y=2..height-5. The button's left edge is the coordinate the hand-off names.
        assertTrue(stripX < 0, "the strip is at x=" + stripX + ", so it is painted on the plate rather than"
                + " hanging off its left edge");
        int localLeft = buttonX - stripX;
        assertTrue(localLeft >= 2 && localLeft + size <= stripW - 1,
                "the button is at strip-local x=" + localLeft + ".." + (localLeft + size) + ", outside the"
                        + " cell 2.." + (stripW - 1));
        assertTrue(localTop >= 2 && localTop + size <= stripH - 4,
                "the button is at strip-local y=" + localTop + ".." + (localTop + size) + ", outside the"
                        + " cell 2.." + (stripH - 4));
        assertEquals(16, size, "the button box is " + size + " wide; the strip's cell only has 16 to give"
                + " once the hand-off's -18 left edge is honoured");
    }

    @Test
    void thePointerBlueIsStillAe2sOwnButtonBlue() throws Exception {
        String source = Files.readString(UI);
        assertEquals(dominantOf("assets/ae2/textures/gui/sprites/button.png"),
                Integer.parseInt(group(source, "AE2_BUTTON_BASE = new ColorRectTexture\\(0xFF_([0-9A-F]{6})\\)")[0], 16),
                "the chip's idle fill is no longer AE2's button.png dominant colour");
        assertEquals(dominantOf("assets/ae2/textures/gui/sprites/button_highlighted.png"),
                Integer.parseInt(group(source, "AE2_BUTTON_HOVER = new ColorRectTexture\\(0xFF_([0-9A-F]{6})\\)")[0], 16),
                "the chip's hover fill is no longer AE2's highlighted blue, so the pointer would not answer");
    }

    /** The most common opaque RGB value of a sheet's first 20 rows, as a packed int. */
    private static int dominantOf(String path) throws Exception {
        BufferedImage image = readClasspath(path);
        java.util.Map<Integer, Integer> counts = new java.util.HashMap<>();
        for (int y = 0; y < Math.min(20, image.getHeight()); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) != 0) {
                    counts.merge(argb & 0xFFFFFF, 1, Integer::sum);
                }
            }
        }
        return counts.entrySet().stream().max(java.util.Map.Entry.comparingByValue())
                .orElseThrow(() -> new AssertionError(path + " carries no opaque pixels"))
                .getKey();
    }

    /**
     * The booster column has to start one column past the plate's own right frame, because the frame is the
     * thing that makes the two read as one panel. The frame's position is measured out of the shipped plate
     * rather than written down here, so repainting the plate moves the assertion with it.
     */
    @Test
    void theBoosterColumnStartsOnePastThePlatesOwnFrame() throws Exception {
        BufferedImage plate = ImageIO.read(PLATE.toFile());
        int lastPainted = -1;
        for (int x = plate.getWidth() - 1; x >= 0; x--) {
            boolean painted = false;
            for (int y = 0; y < 200; y += 7) {
                if ((plate.getRGB(x, y) >>> 24) != 0) {
                    painted = true;
                    break;
                }
            }
            if (painted) {
                lastPainted = x;
                break;
            }
        }
        int boosterX = Integer.parseInt(group(Files.readString(UI), "BOOSTER_X\\s*=\\s*(\\d+)")[0]);
        assertEquals(lastPainted + 1, boosterX,
                "the plate's last painted column is " + lastPainted + " but the booster column starts at "
                        + boosterX + ", so it either covers the frame or leaves a gap");
        assertTrue(boosterX + 21 <= 256, "the column runs past the plate file's canvas");
    }

    private static String[] group(String source, String regex) {
        Matcher matcher = Pattern.compile(regex, Pattern.DOTALL).matcher(source);
        assertTrue(matcher.find(), "the module panel no longer contains " + regex
                + ", so this guard is checking a shape the code does not have");
        String[] out = new String[matcher.groupCount()];
        for (int i = 0; i < out.length; i++) {
            out[i] = matcher.group(i + 1);
        }
        return out;
    }

    private static BufferedImage readClasspath(String path) throws Exception {
        try (InputStream in = WirelessModuleGuideChipTest.class.getClassLoader().getResourceAsStream(path)) {
            assertNotNull(in, path + " is not on the test classpath, so the mod that ships it is not a "
                    + "dependency any more");
            return ImageIO.read(in);
        }
    }
}

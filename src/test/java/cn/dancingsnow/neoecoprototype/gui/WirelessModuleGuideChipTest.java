package cn.dancingsnow.neoecoprototype.gui;

import appeng.items.tools.GuideItem;
import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The module panel's guide chip is one hand-drawn two-state sheet plus two things that are not pixels at all:
 * AE2's guide id and a page in our own resources. A renamed guide, a moved page, a re-exported sheet with its
 * second state somewhere else, or a chip that drifted off the plate's edge would leave the chip drawing the
 * wrong picture, answering nothing, or sitting on the plate's fill.
 *
 * <p>Every value is measured out of the shipped source and the shipped artifacts rather than copied into a
 * table here, so a repaint moves the assertions with it instead of freezing one version of the art.
 */
class WirelessModuleGuideChipTest {

    private static final Path UI = Path.of(
            "src/main/java/cn/dancingsnow/neoecoprototype/items/WirelessChannelModuleUI.java");
    private static final Path PLATE = Path.of(
            "src/main/resources/assets/neoecoprototype/textures/gui/wireless_channel_module.png");
    private static final Path DECK = Path.of(
            "src/main/resources/assets/neoecoprototype/textures/gui/wireless_channel_boosters.png");
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
    void theWirelessPageFollowsTheInterfacePagesInBothGuideIndexes() throws Exception {
        for (Path page : List.of(
                GUIDE_ROOT.resolve("neoecoprototype_intro/subsystem_channel_module.md"),
                GUIDE_ROOT.resolve("_zh_cn/neoecoprototype_intro/subsystem_channel_module.md"))) {
            String frontmatter = Files.readString(page).split("---", 3)[1];
            assertTrue(Pattern.compile("(?m)^\\s*position:\\s*100\\s*$").matcher(frontmatter).find(),
                    page + " does not place the wireless core after the powered/superconductive interface pages");
        }
    }

    @Test
    void theNodeDetailsLabelUsesTheRequestedTextInBothLanguages() throws Exception {
        for (String language : List.of("zh_cn", "en_us")) {
            String json = Files.readString(Path.of(
                    "src/main/resources/assets/neoecoprototype/lang/" + language + ".json"));
            var keys = com.google.gson.JsonParser.parseString(json).getAsJsonObject();
            assertTrue(keys.has("gui.neoecoprototype.wireless_module.node_details"),
                    language + ".json has no node-details label");
            if (language.equals("zh_cn")) {
                assertEquals("节点详情", keys.get("gui.neoecoprototype.wireless_module.node_details").getAsString());
            }
        }
    }

    @Test
    void thePageTheChipPointsAtExistsInBothLanguages() throws Exception {
        String page = group(Files.readString(UI),
                "GUIDE_PAGE\\s*=\\s*NeoECOPrototype\\.id\\(\"([^\"]+)\"\\)")[0];
        assertTrue(page.endsWith(".md"), "GuideME page IDs include the .md suffix: " + page);
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve(page)),
                GUIDE_ROOT.resolve(page) + " is not in the jar, so the chip has no page to open");
        assertTrue(Files.isRegularFile(GUIDE_ROOT.resolve("_zh_cn").resolve(page)),
                "the Chinese twin of " + page + " is missing, so a zh client gets the English page");
    }

    /**
     * The hand-off names the idle frame's pixel (20, 1) and the hovered frame's (52, 1) as the two that land
     * on the plate's (1, 0): the frame's last-but-one column and its second row, because the re-export put a
     * one-pixel outline above and left of the art. Those numbers agree only if the frame is 22 wide and the
     * hovered state sits 32 pixels along (20 = width - 2, and 52 = 32 + 20), which is what this pins: the two
     * states are one frame apart by exactly the pitch the hand-off draws them at, and the chip's origin
     * follows from them rather than being copied here.
     */
    @Test
    void theChipSheetCarriesBothStatesWhereTheHandOffPutsThem() throws Exception {
        int[] chip = chipSource();
        int width = chip[2];
        int height = chip[3];
        int hoverU = chip[4];
        BufferedImage sheet = readClasspath(SHIPPED_CHIP);
        assertTrue(sheet.getWidth() >= hoverU + width && sheet.getHeight() >= height,
                "the sheet is " + sheet.getWidth() + "x" + sheet.getHeight() + ", so the hovered frame at u="
                        + hoverU + " (" + width + "x" + height + ") runs off it");

        int inkedInFrames = 0;
        int inkedAnywhere = 0;
        for (int y = 0; y < sheet.getHeight(); y++) {
            for (int x = 0; x < sheet.getWidth(); x++) {
                if ((sheet.getRGB(x, y) >>> 24) != 0) {
                    inkedAnywhere++;
                    boolean insideIdle = x < width && y < height;
                    boolean insideHover = x >= hoverU && x < hoverU + width && y < height;
                    if (insideIdle || insideHover) {
                        inkedInFrames++;
                    }
                }
            }
        }
        assertEquals(inkedAnywhere, inkedInFrames,
                "the sheet paints " + (inkedAnywhere - inkedInFrames) + " pixels outside the two states at u=0"
                        + " and u=" + hoverU + ", so it is not the two-frame chip the hand-off delivered");

        // The anchor as arithmetic: column width - 2 lands on panel column 1; the chip sits one pixel lower
        // than the hand-off, so its row 1 lands on panel row 1.
        assertEquals(1, chip[0] + width - 2,
                "the chip starts at x=" + chip[0] + " and is " + width + " wide, so its column " + (width - 2)
                        + " lands on panel column " + (chip[0] + width - 2) + " instead of the hand-off's 1");
        assertEquals(1, chip[1] + 1, "the frame starts at y=" + chip[1] + ", so its row 1 lands on panel row "
                + (chip[1] + 1) + " - one row below the plate's corner, which is where he asked to move it");
        assertTrue(hoverU > width, "the hovered state starts at u=" + hoverU + ", which reaches into the idle"
                + " frame (" + width + " wide), so the pointer would show a mix of the two");
    }

    /**
     * The pointer has to redraw the button and nothing else: the strip's bevel is what tells the player the
     * chip is attached to the machine, so if a re-export moved the box, the whole strip would jump under the
     * pointer. The two border columns and the two border rows are the part that must not move.
     */
    @Test
    void thePointerRepaintsTheButtonAndLeavesTheStripAlone() throws Exception {
        int[] chip = chipSource();
        int width = chip[2];
        int height = chip[3];
        BufferedImage sheet = readClasspath(SHIPPED_CHIP);
        assertEquals(idleFrameColour(sheet, width, 0, 1), hoverFrameColour(sheet, chip[4], width, 0, 1),
                "the chip's first column changes with the pointer, so the strip's own highlight moves");
        assertEquals(idleFrameColour(sheet, width, width - 1, 1), hoverFrameColour(sheet, chip[4], width, width - 1, 1),
                "the chip's last column changes with the pointer, so the strip's fill moves");
        for (int x = 0; x < width; x++) {
            assertEquals(idleFrameColour(sheet, width, x, 0), hoverFrameColour(sheet, chip[4], width, x, 0),
                    "row 0 of the chip changes at x=" + x + ", so the strip's top edge moves");
            assertEquals(idleFrameColour(sheet, width, x, height - 1),
                    hoverFrameColour(sheet, chip[4], width, x, height - 1),
                    "the chip's last row changes at x=" + x + ", so the strip's shadow band moves");
        }

        int left = width;
        int right = -1;
        int top = height;
        int bottom = -1;
        int whites = 0;
        int whiteDifference = 0;
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idle = idleFrameColour(sheet, width, x, y) & 0xFFFFFF;
                int hover = hoverFrameColour(sheet, chip[4], width, x, y) & 0xFFFFFF;
                if (idle != hover) {
                    left = Math.min(left, x);
                    right = Math.max(right, x);
                    top = Math.min(top, y);
                    bottom = Math.max(bottom, y);
                }
            }
        }
        assertTrue(right >= 0, "the two states are identical, so the pointer would never answer");
        assertTrue(left >= 2 && right <= width - 2 && top >= 2 && bottom <= height - 2,
                "the pointer repaints " + left + ".." + right + " by " + top + ".." + bottom
                        + ", which reaches the strip's border");

        for (int y = top; y <= bottom; y++) {
            for (int x = left; x <= right; x++) {
                if ((idleFrameColour(sheet, width, x, y) & 0xFFFFFF) == 0xF2F2F2) {
                    whites++;
                }
                if ((hoverFrameColour(sheet, chip[4], width, x, y) & 0xFFFFFF) == 0xF2F2F2) {
                    whiteDifference++;
                }
            }
        }
        // The HELP glyph is the only white inside the button box - 24 pixels of it. A blank button (0) and a
        // box painted over with the strip's fill (also 0) both read as "the glyph is gone".
        assertTrue(whites >= 16 && whites <= 64,
                "the button box carries " + whites + " white pixels, so the HELP glyph is there no more");
        assertEquals(whites, whiteDifference,
                "the glyph is " + whites + " white pixels when idle and " + whiteDifference
                        + " when hovered, so the two states draw different glyphs");
    }

    /**
     * The chip is painted in the plate's own three colours, which is what lets it read as part of the machine
     * rather than as a card stuck on the window: the outline it opens with is the plate's outline, the line
     * inside it is the plate's highlight line, and its fill is the plate's fill.
     */
    @Test
    void theChipIsPaintedInThePlatesOwnPalette() throws Exception {
        BufferedImage plate = ImageIO.read(PLATE.toFile());
        BufferedImage sheet = readClasspath(SHIPPED_CHIP);
        int highlight = plate.getRGB(1, 1) & 0xFFFFFF;
        int fill = plate.getRGB(4, 20) & 0xFFFFFF;
        int outline = plate.getRGB(0, 50) & 0xFFFFFF;
        assertEquals(outline, sheet.getRGB(0, 0) & 0xFFFFFF,
                "the chip's outer corner is not the plate's outline colour " + Integer.toHexString(outline));
        assertEquals(highlight, sheet.getRGB(1, 1) & 0xFFFFFF,
                "the chip's highlight line is not the plate's highlight colour "
                        + Integer.toHexString(highlight));
        assertEquals(fill, sheet.getRGB(2, 2) & 0xFFFFFF,
                "the chip's fill is not the plate's fill " + Integer.toHexString(fill));
    }

    /**
     * The pointer's answer is AE2's own button blue: every pixel the two states disagree on is painted in the
     * idle state with {@code button.png}'s fill and in the hovered state with {@code button_highlighted.png}'s,
     * so the chip answers with the same two colours every other AE2 machine gives.
     */
    @Test
    void thePointerBlueIsStillAe2sOwnButtonBlue() throws Exception {
        int[] chip = chipSource();
        int width = chip[2];
        int height = chip[3];
        BufferedImage sheet = readClasspath(SHIPPED_CHIP);
        Map<Integer, Integer> idleCounts = new HashMap<>();
        Map<Integer, Integer> hoverCounts = new HashMap<>();
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int idle = idleFrameColour(sheet, width, x, y);
                int hover = hoverFrameColour(sheet, chip[4], width, x, y);
                if (idle != hover) {
                    idleCounts.merge(idle & 0xFFFFFF, 1, Integer::sum);
                    hoverCounts.merge(hover & 0xFFFFFF, 1, Integer::sum);
                }
            }
        }
        assertEquals(dominantOf("assets/ae2/textures/gui/sprites/button.png"), dominant(idleCounts, "the idle button"),
                "the idle button is no longer painted in AE2's button.png fill");
        assertEquals(dominantOf("assets/ae2/textures/gui/sprites/button_highlighted.png"),
                dominant(hoverCounts, "the hovered button"),
                "the hovered button is no longer painted in AE2's highlighted fill, so the pointer would not"
                        + " answer with the colour the rest of the machine uses");
    }

    @Test
    void interactiveHoverAndPressUseAe2sBlue() throws Exception {
        String source = Files.readString(UI);
        String ae2Blue = "assets/ae2/textures/gui/sprites/button_highlighted.png";
        int expectedRgb = dominantOf(ae2Blue);
        int hover = (int) Long.parseLong(group(source,
                "HOVER_TINT\\s*=\\s*new ColorRectTexture\\(0x([0-9A-Fa-f_]+)\\)")[0].replace("_", ""), 16);
        int pressed = (int) Long.parseLong(group(source,
                "PRESSED_TINT\\s*=\\s*new ColorRectTexture\\(0x([0-9A-Fa-f_]+)\\)")[0].replace("_", ""), 16);
        assertEquals(expectedRgb, hover & 0xFFFFFF,
                "interactive hover tint is not AE2's highlighted button blue from " + ae2Blue);
        assertEquals(0x66, hover >>> 24, "interactive hover tint no longer matches the panel's established blue wash");
        assertEquals(hover, pressed, "pressing an interactive region drops the AE2 blue feedback");
    }

    /**
     * The chip hangs off the plate's left edge and tucks into its frame: it must reach the frame - otherwise
     * it floats away from the panel - and it must stop where the plate's body begins, or it would read as a
     * hole punched into the panel rather than as a strip beside it. Its last column is allowed onto the
     * plate's first fill column, because that column is where the art hands the panel back.
     */
    @Test
    void theChipHangsOffThePlatesLeftEdgeWithoutCoveringItsBody() throws Exception {
        int[] chip = chipSource();
        BufferedImage plate = ImageIO.read(PLATE.toFile());
        int fill = plate.getRGB(4, 20) & 0xFFFFFF;
        int firstFill = -1;
        for (int x = 0; x < plate.getWidth() && firstFill < 0; x++) {
            for (int y = 0; y < plate.getHeight(); y += 3) {
                if ((plate.getRGB(x, y) & 0xFFFFFF) == fill) {
                    firstFill = x;
                    break;
                }
            }
        }
        assertTrue(firstFill > 0, "the plate paints no fill column at all, so this guard is measuring the"
                + " wrong picture");
        assertTrue(chip[0] < 0, "the chip starts at x=" + chip[0] + ", so it is painted on the plate instead of"
                + " hanging off its left edge");
        assertTrue(chip[0] + chip[2] >= 1,
                "the chip ends at x=" + (chip[0] + chip[2]) + ", so it does not reach the plate's frame and"
                        + " reads as floating");
        assertTrue(chip[0] + chip[2] <= firstFill + 1,
                "the chip ends at x=" + (chip[0] + chip[2]) + " but the plate's body starts at " + firstFill
                        + ", so the chip paints over the panel instead of only its frame");

        // Below the chip the plate's frame carries on, and the chip's last row has to hand it back: measured
        // from the plate one row past the chip's bottom, the columns the chip paints last must already be the
        // colours the plate goes on with.
        BufferedImage sheet = readClasspath(SHIPPED_CHIP);
        int below = chip[1] + chip[3];
        assertTrue(below < plate.getHeight(), "the plate is " + plate.getHeight() + " tall, so there is no row"
                + " below the chip to compare its seam against");
        int lastCovered = Math.min(plate.getWidth() - 1, chip[0] + chip[2] - 1);
        for (int plateX = Math.max(0, lastCovered - 2); plateX <= lastCovered; plateX++) {
            int local = plateX - chip[0];
            assertEquals(plate.getRGB(plateX, below) & 0xFFFFFF,
                    sheet.getRGB(local, chip[3] - 1) & 0xFFFFFF,
                    "the chip's last row paints "
                            + Integer.toHexString(sheet.getRGB(local, chip[3] - 1))
                            + " over the plate's " + Integer.toHexString(plate.getRGB(plateX, below))
                            + " at column " + plateX + ", so the frame below it stops dead at the chip's"
                            + " bottom edge");
        }
    }

    /**
     * The booster column starts on the plate's own highlight column and repaints the frame itself: its first
     * column is the highlight white, its last is the outline dark, so the two read as one panel with no seam
     * and no floating edge. All four values are measured out of the shipped images rather than written down
     * here, so repainting either picture moves the assertions with it.
     */
    @Test
    void theBoosterColumnContinuesThePlatesOwnFrame() throws Exception {
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
        int boosterWidth = Integer.parseInt(group(Files.readString(UI), "BOOSTER_WIDTH\\s*=\\s*(\\d+)")[0]);
        assertEquals(lastPainted - 1, boosterX,
                "the plate's last painted column is " + lastPainted + " (its outline) but the booster column"
                        + " starts at " + boosterX + ", so it either leaves a gap in the frame or paints over"
                        + " more of the plate than the frame");
        assertTrue(boosterX + boosterWidth <= 256, "the column runs past the plate file's canvas");

        BufferedImage deck = ImageIO.read(DECK.toFile());
        assertNotEquals(plate.getRGB(lastPainted - 1, 50) & 0xFFFFFF, plate.getRGB(lastPainted, 50) & 0xFFFFFF,
                "the plate no longer draws a highlight line and an outline at " + (lastPainted - 1) + ".."
                        + lastPainted + ", so this guard is measuring something else");
        for (int y = 0; y < deck.getHeight(); y++) {
            int left = deck.getRGB(0, y) & 0xFFFFFF;
            int highlight = plate.getRGB(lastPainted - 1, y) & 0xFFFFFF;
            int fill = plate.getRGB(lastPainted - 2, y) & 0xFFFFFF;
            assertTrue(left == highlight || left == fill,
                    "the deck's first column paints " + Integer.toHexString(left) + " at row " + y
                            + " over the plate's " + Integer.toHexString(highlight) + "/"
                            + Integer.toHexString(fill) + ", so it draws a line of its own along the seam");
            assertEquals(plate.getRGB(lastPainted, y) & 0xFFFFFF, deck.getRGB(deck.getWidth() - 1, y) & 0xFFFFFF,
                    "the deck's last column is not the plate's outline colour at row " + y
                            + ", so the deck floats against the screen instead of closing on the panel");
        }
    }

    private static final String SHIPPED_CHIP = "assets/neoecoprototype/textures/gui/wireless_channel_guide_button.png";

    /** x, y, width, height and the hovered state's u, in that order, out of the shipped source. */
    private static int[] chipSource() throws Exception {
        String source = Files.readString(UI);
        assertEquals(SHIPPED_CHIP, "assets/neoecoprototype/" + group(source,
                        "GUIDE_CHIP\\s*=\\s*NeoECOPrototype\\.id\\(\"([^\"]+)\"\\)")[0],
                "the chip reads its pixels from a sheet this guard does not open");
        return new int[]{
                Integer.parseInt(group(source, "GUIDE_CHIP_X\\s*=\\s*(-?\\d+)")[0]),
                Integer.parseInt(group(source, "GUIDE_CHIP_Y\\s*=\\s*(-?\\d+)")[0]),
                Integer.parseInt(group(source, "GUIDE_CHIP_WIDTH\\s*=\\s*(\\d+)")[0]),
                Integer.parseInt(group(source, "GUIDE_CHIP_HEIGHT\\s*=\\s*(\\d+)")[0]),
                Integer.parseInt(group(source, "GUIDE_CHIP_HOVER_U\\s*=\\s*(\\d+)")[0]),
        };
    }

    private static int idleFrameColour(BufferedImage sheet, int width, int x, int y) {
        assertTrue(x < width, "x=" + x + " is outside the " + width + " wide frame");
        return sheet.getRGB(x, y);
    }

    private static int hoverFrameColour(BufferedImage sheet, int u, int width, int x, int y) {
        assertTrue(u + x < sheet.getWidth(), "the hovered frame's x=" + x + " runs past the sheet");
        return sheet.getRGB(u + x, y);
    }

    /** The most common opaque RGB value of a sheet's first 20 rows, as a packed int. */
    private static int dominantOf(String path) throws Exception {
        BufferedImage image = readClasspath(path);
        Map<Integer, Integer> counts = new HashMap<>();
        for (int y = 0; y < Math.min(20, image.getHeight()); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int argb = image.getRGB(x, y);
                if ((argb >>> 24) != 0) {
                    counts.merge(argb & 0xFFFFFF, 1, Integer::sum);
                }
            }
        }
        return dominant(counts, path);
    }

    private static int dominant(Map<Integer, Integer> counts, String what) {
        return counts.entrySet().stream().max(Map.Entry.comparingByValue())
                .orElseThrow(() -> new AssertionError(what + " carries no opaque pixels"))
                .getKey();
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
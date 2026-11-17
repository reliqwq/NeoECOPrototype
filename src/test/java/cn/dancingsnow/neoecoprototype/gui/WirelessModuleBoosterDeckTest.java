package cn.dancingsnow.neoecoprototype.gui;

import org.junit.jupiter.api.Test;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The module panel's booster column is one hand-drawn deck: four card faces in a 23x90 strip, with four
 * invisible slots laid on top of them. None of that is a compile-time symbol - redrawing the deck at another
 * size, or re-spacing its cards, would leave every slot a few pixels off the drawn recess with no error
 * anywhere. So the card positions are read back out of the shipped image instead of being copied here.
 */
class WirelessModuleBoosterDeckTest {

    private static final Path UI = Path.of(
            "src/main/java/cn/dancingsnow/neoecoprototype/items/WirelessChannelModuleUI.java");
    private static final Path BLOCK_ENTITY = Path.of(
            "src/main/java/cn/dancingsnow/neoecoprototype/blockentity/wireless/WirelessChannelModuleBlockEntity.java");
    private static final Path DECK = Path.of(
            "src/main/resources/assets/neoecoprototype/textures/gui/wireless_channel_boosters.png");

    @Test
    void interactiveHitboxesAndNodeDetailsMatchThePlateCoordinates() throws Exception {
        assertEquals(67, number("MID_PAGER_PREV_X"));
        assertEquals(14, number("MID_PAGER_WIDTH"));
        assertEquals(94, number("MID_PAGER_Y"));
        assertEquals(14, number("MID_PAGER_HEIGHT"));
        assertEquals(82, number("MID_PAGER_PAGE_X"));
        assertEquals(97, number("MID_PAGER_NEXT_X"));
        assertEquals(67, number("MID_ACTION_HIGHLIGHT_X"));
        assertEquals(15, number("MID_ACTION_HIGHLIGHT_WIDTH"));
        assertEquals(82, number("MID_ACTION_REFRESH_X"));
        assertEquals(14, number("MID_ACTION_REFRESH_WIDTH"));
        assertEquals(97, number("MID_ACTION_DELETE_X"));
        assertEquals(14, number("MID_ACTION_DELETE_WIDTH"));
        assertEquals(118, number("MID_ACTION_Y"));
        assertEquals(14, number("MID_ACTION_HEIGHT"));
        assertEquals(121, number("NODE_DETAILS_X"));
        assertEquals(24, number("NODE_DETAILS_Y"));
        assertEquals(17, number("NODE_DETAILS_WIDTH"));
        assertEquals(8, number("NODE_DETAILS_HEIGHT"));
        String source = Files.readString(UI);
        assertTrue(source.contains("DEVICE_PAGE_FONT = 6"),
                "the n/m device page counter should use the smaller font");
        assertTrue(source.contains("NODE_DETAILS_HEIGHT,\n                COLOR, 7)"),
                "the node-details label should use the panel's normal text color");
        assertTrue(source.contains("slotStyle(style -> style.hoverOverlay(HOVER_TINT))"),
                "the booster-slot hover overlay should use the panel's AE2 blue tint");
    }

    @Test
    void theDeckIsDrawnAtItsOwnSize() throws Exception {
        BufferedImage deck = ImageIO.read(DECK.toFile());
        int width = number("BOOSTER_WIDTH");
        int height = number("BOOSTER_HEIGHT");
        assertEquals(width, deck.getWidth(),
                "the deck is " + deck.getWidth() + " wide, so drawing it at " + width + " would resample it");
        assertEquals(height, deck.getHeight(),
                "the deck is " + deck.getHeight() + " tall, so drawing it at " + height + " would resample it");
    }

    /**
     * Every slot has to land on a card, and the item it holds has to stay inside that card's frame. A card
     * interior is what tells the faces apart from the deck's bands, so it is measured, not assumed: a row
     * belongs to a card when both of its frame columns are the frame and the column between them is not.
     */
    @Test
    void everySlotLandsOnACardAndTheItemStaysInsideItsFrame() throws Exception {
        BufferedImage deck = ImageIO.read(DECK.toFile());
        int frame = brightestOpaque(deck);
        int faceWidth = 18;

        List<int[]> cards = new ArrayList<>();
        int runStart = -1;
        for (int y = 0; y <= deck.getHeight(); y++) {
            boolean face = y < deck.getHeight()
                    && rgb(deck, 0, y) == frame
                    && rgb(deck, faceWidth - 1, y) == frame
                    && rgb(deck, 1, y) != frame;
            if (face && runStart < 0) {
                runStart = y;
            } else if (!face && runStart >= 0) {
                cards.add(new int[]{runStart, y - 1});
                runStart = -1;
            }
        }

        int slots = maxBoosters();
        assertEquals(slots, cards.size(),
                "the panel places " + slots + " booster slots but the deck draws " + cards.size()
                        + " card faces");
        int top = number("BOOSTER_CELL_TOP");
        int pitch = number("BOOSTER_CELL_PITCH");
        for (int index = 0; index < cards.size(); index++) {
            int first = cards.get(index)[0];
            int last = cards.get(index)[1];
            assertEquals(top + index * pitch, first - 1,
                    "card " + index + " starts at row " + first + ", but the slot above it is placed at "
                            + (top + index * pitch) + ", so a frame row would show beside the item");
            for (int y = first; y <= last; y++) {
                assertEquals(frame, rgb(deck, 0, y),
                        "card " + index + " has no frame column left of its recess at row " + y);
                assertEquals(frame, rgb(deck, faceWidth - 1, y),
                        "card " + index + " has no frame column right of its recess at row " + y);
                for (int x = 1; x <= 16; x++) {
                    assertNotEquals(frame, rgb(deck, x, y),
                            "the 16-wide item on card " + index + " covers the frame at (" + x + ", " + y
                                    + "), so its recess is narrower than the item");
                }
            }
        }
    }

    private static int maxBoosters() throws Exception {
        Matcher matcher = Pattern.compile("MAX_BOOSTERS\\s*=\\s*(\\d+)")
                .matcher(Files.readString(BLOCK_ENTITY));
        assertTrue(matcher.find(),
                BLOCK_ENTITY + " no longer declares MAX_BOOSTERS, so this guard is checking a shape the code"
                        + " does not have");
        return Integer.parseInt(matcher.group(1));
    }

    private static int number(String constant) throws Exception {
        Matcher matcher = Pattern.compile("\\b" + constant + "\\s*=\\s*(\\d+)").matcher(Files.readString(UI));
        assertTrue(matcher.find(),
                UI + " no longer declares " + constant + ", so this guard is checking a shape the code does"
                        + " not have");
        return Integer.parseInt(matcher.group(1));
    }

    private static int rgb(BufferedImage image, int x, int y) {
        return image.getRGB(x, y) & 0xFFFFFF;
    }

    /** The deck's card frames are the brightest colour it paints; nothing else comes near white. */
    private static int brightestOpaque(BufferedImage image) {
        int best = -1;
        int brightest = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                if ((image.getRGB(x, y) >>> 24) == 0) {
                    continue;
                }
                int colour = rgb(image, x, y);
                int luminance = ((colour >> 16) & 0xFF) * 299 + ((colour >> 8) & 0xFF) * 587
                        + (colour & 0xFF) * 114;
                if (luminance > best) {
                    best = luminance;
                    brightest = colour;
                }
            }
        }
        return brightest;
    }
}
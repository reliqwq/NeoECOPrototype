package cn.dancingsnow.neoecoprototype.gui;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Checks a GUI style JSON's declared slot rows against the groove rows its texture actually draws.
 *
 * <p>The two live in different files and nothing else in the game compares them: AE2 draws the panel from
 * the texture and places the clickable slots from the JSON, so a mismatch shows up as items sitting on the
 * border of their box and is invisible to the compiler, to a compile-time resource check and to every unit
 * test that does not look at pixels.
 *
 * <p>Both sides are measured, not carried by hand: the declared rows come out of the JSON's own
 * {@code top}/{@code bottom} plus the grid it names, and the drawn rows out of the PNG. The only inputs a
 * caller supplies are the slot counts, which are read from the code that creates them.
 *
 * <p>Pure over bytes, and {@code java.desktop}'s ImageIO rather than {@code NativeImage}, because
 * {@code NativeImage} is marked client-only and this runs inside a dedicated server's GameTest.
 */
public final class GuiGrooveAlignment {
    /** An AE2 slot cell is 18x18 with a 1px border, so a drawn groove is 16 rows tall and rows step by 18. */
    public static final int ROW_PITCH = 18;
    private static final int GROOVE_HEIGHT = 16;
    /** What {@code PoweredInterfaceScreen.moveSecondRow} adds to a section's second row and beyond. */
    public static final int SECOND_ROW_SHIFT = 36;
    /** The band across the panel that slots are drawn in: x=8 to x=170 for AE2's 176-wide srcRect. */
    private static final int BAND_LEFT = 8;
    private static final int BAND_RIGHT = 170;
    /** A row counts as drawn-over when most of that band is darker than this mean brightness. */
    private static final int DARK_MEAN = 195;
    private static final int DARK_COLUMNS = 100;
    /**
     * Bright rows this short inside a dark band are a highlight the artist drew inside the groove, not the
     * gap to the next one - neighbouring grooves sit two bright rows apart, which is what keeps this from
     * gluing two rows of slots into one.
     */
    private static final int HIGHLIGHT_GAP = 1;

    /**
     * One slot section of a style JSON: where AE2 anchors its first row and how the menu's slots break into
     * rows. {@link #sectionsFrom} reads these out of a real style file; a test can name them directly.
     *
     * @param secondRowShift set for a section whose later rows the screen moves down by an extra row pair,
     *                       which is what {@code PoweredInterfaceScreen.moveSecondRow} does to keep a second
     *                       group of amount buttons a row of its own
     */
    public record Section(String name, int slots, int perRow, int firstTop, boolean secondRowShift) {
        public Section(String name, int slots, int perRow, int firstTop) {
            this(name, slots, perRow, firstTop, false);
        }
    }

    /** The row tops a set of sections occupies, ordered from the top of the texture down. */
    public static List<Integer> declaredTops(List<Section> sections) {
        var tops = new ArrayList<Integer>();
        for (var section : sections) {
            int rows = (section.slots() + section.perRow() - 1) / section.perRow();
            for (int row = 0; row < rows; row++) {
                int shift = row >= 1 && section.secondRowShift() ? SECOND_ROW_SHIFT : 0;
                tops.add(section.firstTop() + row * ROW_PITCH + shift);
            }
        }
        Collections.sort(tops);
        return tops;
    }

    /**
     * The sections a style JSON declares, with the bottom-anchored ones already converted into a row top.
     *
     * @param slotsPerSection how many slots the menu really hands each section; a section missing from it is
     *                        skipped, so the caller decides what is checked
     * @param shifted         the sections whose second row the screen moves down, see {@link Section}
     * @param fallbackHeight  the texture height, used when the style declares no {@code background.srcRect}
     */
    public static List<Section> sectionsFrom(String styleJson, Map<String, Integer> slotsPerSection,
                                             java.util.Set<String> shifted, int fallbackHeight) {
        var document = JsonParser.parseString(styleJson).getAsJsonObject();
        var slots = document.getAsJsonObject("slots");
        int height = fallbackHeight;
        if (document.has("background")) {
            var background = document.getAsJsonObject("background");
            if (background.has("srcRect")) {
                height = background.getAsJsonArray("srcRect").get(3).getAsInt();
            }
        }
        var sections = new ArrayList<Section>();
        for (var entry : slotsPerSection.entrySet()) {
            var spec = slots.getAsJsonObject(entry.getKey());
            if (spec == null) {
                throw new IllegalArgumentException("the style JSON declares no slot section " + entry.getKey());
            }
            int count = entry.getValue();
            int perRow = switch (spec.has("grid") ? spec.get("grid").getAsString() : "HORIZONTAL") {
                case "BREAK_AFTER_2COLS" -> 2;
                case "BREAK_AFTER_3COLS" -> 3;
                case "BREAK_AFTER_9COLS" -> 9;
                case "VERTICAL" -> 1;
                // HORIZONTAL and IO_BUS_CONFIG put every slot on one row.
                default -> Math.max(1, count);
            };
            int rows = (count + perRow - 1) / perRow;
            int first = firstTop(spec, height);
            sections.add(new Section(entry.getKey(), count, perRow, first,
                    shifted.contains(entry.getKey())));
        }
        return sections;
    }

    /** The row tops a texture really draws a groove at. */
    public static List<Integer> drawnGrooveTops(byte[] png) throws IOException {
        var image = decode(png);
        var bands = new ArrayList<int[]>();
        int runStart = -1;
        for (int y = 0; y <= image.getHeight(); y++) {
            boolean dark = y < image.getHeight() && isGrooveRow(image, y);
            if (dark && runStart < 0) {
                runStart = y;
            } else if (!dark && runStart >= 0) {
                bands.add(new int[]{runStart, y});
                runStart = -1;
            }
        }
        var starts = new ArrayList<Integer>();
        int[] groove = null;
        for (var band : bands) {
            if (groove != null && band[0] - groove[1] <= HIGHLIGHT_GAP) {
                groove[1] = band[1];
                continue;
            }
            if (groove != null) {
                addIfGroove(starts, groove);
            }
            groove = band;
        }
        if (groove != null) {
            addIfGroove(starts, groove);
        }
        return starts;
    }

    private static void addIfGroove(List<Integer> starts, int[] band) {
        if (band[1] - band[0] >= GROOVE_HEIGHT && band[1] - band[0] <= ROW_PITCH) {
            starts.add(band[0]);
        }
    }

    public static int textureHeight(byte[] png) throws IOException {
        return decode(png).getHeight();
    }

    /** A message naming the first disagreement, or null when the two lists are the same rows. */
    public static String firstMismatch(List<Integer> declared, List<Integer> drawn) {
        if (declared.equals(drawn)) {
            return null;
        }
        if (declared.size() != drawn.size()) {
            return "declared " + declared.size() + " slot row(s) " + declared + " but the texture draws "
                    + drawn.size() + " groove row(s) " + drawn;
        }
        for (int i = 0; i < declared.size(); i++) {
            if (declared.get(i) != drawn.get(i)) {
                return "slot row " + (i + 1) + " is declared at y=" + declared.get(i) + " but the texture"
                        + " draws its groove at y=" + drawn.get(i);
            }
        }
        return null;
    }

    private static BufferedImage decode(byte[] png) throws IOException {
        var image = ImageIO.read(new ByteArrayInputStream(png));
        if (image == null) {
            throw new IOException("ImageIO found no reader for this PNG");
        }
        return image;
    }

    private static boolean isGrooveRow(BufferedImage image, int y) {
        int width = Math.min(BAND_RIGHT, image.getWidth());
        var pixels = new int[width];
        image.getRGB(0, y, width, 1, pixels, 0, 1);
        int dark = 0;
        for (int rgb : pixels) {
            if (((rgb >> 16 & 0xFF) + (rgb >> 8 & 0xFF) + (rgb & 0xFF)) / 3.0 < DARK_MEAN) {
                dark++;
            }
        }
        return dark > DARK_COLUMNS;
    }

    private static int firstTop(JsonObject spec, int height) {
        if (spec.has("top")) {
            return spec.get("top").getAsInt();
        }
        if (spec.has("bottom")) {
            return height - spec.get("bottom").getAsInt();
        }
        throw new IllegalArgumentException("a slot section declares neither top nor bottom: " + spec);
    }

    private GuiGrooveAlignment() {
    }
}

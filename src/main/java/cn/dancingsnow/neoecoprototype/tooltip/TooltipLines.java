package cn.dancingsnow.neoecoprototype.tooltip;

import java.util.Arrays;
import java.util.List;

/**
 * Turns one language entry into the lines a tooltip should show.
 *
 * <p>The renderer does not split newlines inside a component - it draws them as the missing-glyph box -
 * so a multi-line description has to arrive as one component per line. Written without touching a
 * Minecraft type because the test source set has no Minecraft on its classpath, and this is the part
 * worth being able to turn red.
 */
public final class TooltipLines {
    private TooltipLines() {
    }

    /**
     * @param text the resolved language entry
     * @return one entry per line; blank interior lines are kept, a single trailing newline is not
     */
    public static List<String> split(String text) {
        if (text.isEmpty()) {
            return List.of();
        }
        String[] lines = text.split("\n", -1);
        if (lines.length > 1 && lines[lines.length - 1].isEmpty()) {
            return List.of(Arrays.copyOf(lines, lines.length - 1));
        }
        return List.of(lines);
    }
}

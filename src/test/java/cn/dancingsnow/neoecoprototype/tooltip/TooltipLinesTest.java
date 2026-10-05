package cn.dancingsnow.neoecoprototype.tooltip;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * The line split behind a multi-line item tooltip. The renderer will not break a component for us, so
 * whatever the language file writes as several lines has to arrive as several components - including the
 * blank line the pigmee entry uses before its signature.
 */
class TooltipLinesTest {

    @Test
    void aSingleLineStaysSingle() {
        assertEquals(List.of("Yes, I am L4"), TooltipLines.split("Yes, I am L4"));
    }

    @Test
    void everyLineOfTheVerseBecomesItsOwnRow() {
        assertEquals(List.of("猪咪的肚子空空的", "好像能装很多东西", "“你好，我吃一点”"),
                TooltipLines.split("猪咪的肚子空空的\n好像能装很多东西\n“你好，我吃一点”"),
                "a three line entry must produce three rows, or the renderer draws the newlines as boxes");
    }

    @Test
    void aBlankInteriorLineIsKept() {
        assertEquals(List.of("one", "", "three"), TooltipLines.split("one\n\nthree"),
                "an empty line inside the entry is spacing the author asked for");
    }

    @Test
    void aTrailingNewlineAddsNoEmptyRow() {
        assertEquals(List.of("one", "two"), TooltipLines.split("one\ntwo\n"));
    }

    @Test
    void emptyTextAddsNothing() {
        assertEquals(List.of(), TooltipLines.split(""));
    }
}

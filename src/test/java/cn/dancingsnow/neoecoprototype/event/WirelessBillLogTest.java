package cn.dancingsnow.neoecoprototype.event;

import cn.dancingsnow.neoecoprototype.event.WirelessLinks.Bill;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * The predicate behind the one line this feature writes on a timer. The pass runs every hundred ticks for
 * every dimension that holds a link, so "nothing moved" has to be silence - and each of the three numbers
 * that mean something has to be able to break that silence on its own.
 */
class WirelessBillLogTest {
    @Test
    void theFirstBillOfADimensionIsAlwaysWorthAWord() {
        assertTrue(WirelessLinks.billIsWorthAWord(Bill.NONE, new Bill(50.0D, 1, 0)));
    }

    @Test
    void aBillThatRepeatsItselfSaysNothing() {
        assertFalse(WirelessLinks.billIsWorthAWord(new Bill(50.0D, 1, 0), new Bill(50.0D, 1, 0)));
    }

    @Test
    void aLinkAddedOrRemovedBreaksTheSilence() {
        assertTrue(WirelessLinks.billIsWorthAWord(new Bill(50.0D, 1, 0), new Bill(100.0D, 2, 0)));
        assertTrue(WirelessLinks.billIsWorthAWord(new Bill(100.0D, 2, 0), new Bill(50.0D, 1, 0)));
    }

    /** The count is the same and the money is not: that is a pack moving the rate, and it should show. */
    @Test
    void aChangedRateBreaksTheSilenceEvenWithTheSameNumberOfLinks() {
        assertTrue(WirelessLinks.billIsWorthAWord(new Bill(50.0D, 1, 0), new Bill(5.0D, 1, 0)));
    }

    @Test
    void aLinkGoingUnpayableBreaksTheSilenceAtTheSameCount() {
        assertTrue(WirelessLinks.billIsWorthAWord(new Bill(50.0D, 1, 0), new Bill(50.0D, 1, 1)));
    }

    @Test
    void aDimensionWithNoLinksAtAllStaysQuiet() {
        assertFalse(WirelessLinks.billIsWorthAWord(Bill.NONE, Bill.NONE));
    }
}

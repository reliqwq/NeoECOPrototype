package cn.dancingsnow.neoecoprototype.items;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

/**
 * Every endless cell must hand eco the credential that says so.
 *
 * <p>A player reported an infinite concrete cell being stocked into the network forever and billing about
 * 2.1 billion AE per second - which is literally {@code SimplifyConcreteStorageCellItem.REPORTED_AMOUNT_PER_TYPE},
 * {@code Integer.MAX_VALUE}, read out of AE2's plain listing and treated as a movable amount. eco's own
 * infinite cells avoid that because they implement {@code ExactAmountSource}: the transfer code asks it for
 * an {@code ExactAmount} and gets an unbounded one. So the seam is not "does our cell report a big number",
 * it is "does eco recognise the number as not-movable", and that recognition is only reachable through the
 * interface - which is what this file pins.
 *
 * <p>Read at the class level on purpose: the cells are private and one of them needs a registered item, and
 * what breaks here is the declaration, not the arithmetic. The behaviour side - an eco interface actually
 * leaving this cell alone - is the game test's job.
 */
final class EndlessCellExactAmountContractTest {

    private static final String EXACT_AMOUNT_SOURCE =
            "cn.dancingsnow.neoecoae.crafting.display.terminal.ExactAmountSource";
    private static final List<String> REQUIRED_METHODS = List.of(
            "neoecoae$visitExactAmounts", "neoecoae$hasSaturatingListing");

    @Test
    void theEndlessCellsCarryTheCredentialEcoAsksFor() {
        carriesSource(EndlessCellExactAmountContractTest.class, InfiniteConcreteCellHandler.class, "Cell");
        carriesSource(EndlessCellExactAmountContractTest.class, CustomInfiniteCellHandler.class, "Cell");
    }

    @Test
    void theSingularityCellStillDeclinesToClaimAnUnboundedStock() {
        var nested = Arrays.stream(SingularityCellHandler.class.getDeclaredClasses())
                .filter(type -> type.getSimpleName().equals("Cell"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("SingularityCellHandler no longer has a Cell class"));
        assertFalse(interfacesOf(nested).contains(EXACT_AMOUNT_SOURCE),
                "the singularity cell now reports itself as endless: its stock is generated over world time "
                        + "and is finite at every moment, so an unbounded ExactAmount would be a lie");
    }

    private static void carriesSource(Class<?> from, Class<?> owner, String nestedName) {
        var nested = Arrays.stream(owner.getDeclaredClasses())
                .filter(type -> type.getSimpleName().equals(nestedName))
                .findFirst()
                .orElseThrow(() -> new AssertionError(owner.getSimpleName() + " no longer has a " + nestedName
                        + " class - if the cell moved, this guard and the seam it checks both need re-reading"));
        var declared = interfacesOf(nested);
        assertTrue(declared.contains(EXACT_AMOUNT_SOURCE),
                owner.getSimpleName() + "." + nestedName + " implements " + declared
                        + " but not " + EXACT_AMOUNT_SOURCE + ". Without it eco reads our saturated listing as"
                        + " a finite movable amount and an interface can pull from this cell forever.");

        var methods = Arrays.stream(nested.getDeclaredMethods()).map(java.lang.reflect.Method::getName)
                .collect(Collectors.toSet());
        assertTrue(methods.containsAll(REQUIRED_METHODS),
                owner.getSimpleName() + "." + nestedName + " declares " + methods
                        + " but eco's credential needs " + REQUIRED_METHODS);
    }

    private static Set<String> interfacesOf(Class<?> type) {
        return Arrays.stream(type.getInterfaces()).map(Class::getName).collect(Collectors.toSet());
    }

    /** Guards the guard: the interface name we match against has to be the one on the classpath. */
    @Test
    void theCredentialInterfaceIsStillWhereWeThinkItIs() throws Exception {
        assertNotNull(Class.forName(EXACT_AMOUNT_SOURCE),
                "eco no longer ships " + EXACT_AMOUNT_SOURCE + " at that path, so every assertion above is"
                        + " matching a name that cannot exist - re-read upstream before trusting any of them");
    }
}

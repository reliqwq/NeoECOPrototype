package cn.dancingsnow.neoecoprototype.integration.ae2;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import appeng.api.config.AccessRestriction;
import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IAEPowerStorage;
import java.io.InputStream;
import java.util.Comparator;
import java.util.TreeSet;
import org.junit.jupiter.api.Test;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;

final class EnergyRequesterRestoreTest {
    private static final String MIXIN =
            "cn/dancingsnow/neoecoprototype/mixin/EnergyServiceRequesterReAddMixin";

    @Test
    void restoresTheExactRemovedStorageOnlyAfterIterationFinishes() {
        var removed = new TestStorage(2, true, AccessRestriction.READ_WRITE, 5.0, 10.0);
        var kept = new TestStorage(1, true, AccessRestriction.READ_WRITE, 0.0, 10.0);
        var requesters = sorted(removed, kept);
        var iterator = new RemovedRequesterIterator(requesters.iterator());

        assertSame(kept, iterator.next());
        assertSame(removed, iterator.next());
        iterator.remove();
        assertFalse(requesters.contains(removed), "the wrapper must not mutate the active upstream set");
        assertTrue(requesters.contains(kept));

        iterator.restoreEligible(requesters);
        assertTrue(requesters.contains(removed));
        assertTrue(requesters.contains(kept));
    }

    @Test
    void skipsFullPrivateAndInsertionBlockedStorages() {
        var eligible = new TestStorage(4, true, AccessRestriction.READ_WRITE, 5.0, 10.0);
        var full = new TestStorage(3, true, AccessRestriction.READ_WRITE, 10.0, 10.0);
        var privateStorage = new TestStorage(2, false, AccessRestriction.READ_WRITE, 0.0, 10.0);
        var readOnly = new TestStorage(1, true, AccessRestriction.READ, 0.0, 10.0);
        var requesters = sorted(eligible, full, privateStorage, readOnly);
        var iterator = new RemovedRequesterIterator(requesters.iterator());
        while (iterator.hasNext()) {
            iterator.next();
            iterator.remove();
        }

        iterator.restoreEligible(requesters);
        assertEquals(1, requesters.size(), "only the removed storage that can still accept power returns");
        assertTrue(requesters.contains(eligible));
    }

    @Test
    void leavesUntouchedRequestersAloneWhenUpstreamRemovesNothing() {
        var storage = new TestStorage(1, true, AccessRestriction.READ_WRITE, 0.0, 10.0);
        var requesters = sorted(storage);
        var iterator = new RemovedRequesterIterator(requesters.iterator());

        assertSame(storage, iterator.next());
        iterator.restoreEligible(requesters);
        assertEquals(1, requesters.size());
        assertTrue(requesters.contains(storage));
    }

    @Test
    void compiledMixinNeverWalksTheWholeGrid() throws Exception {
        var node = new ClassNode();
        try (InputStream input = getClass().getClassLoader().getResourceAsStream(MIXIN + ".class")) {
            assertNotNull(input, MIXIN + " is missing from the test classpath");
            new ClassReader(input).accept(node, Opcodes.ASM9);
        }
        assertFalse(node.methods.isEmpty(), "the compiled mixin has no methods to inspect");
        boolean wrapsIterator = false;
        boolean restoresRecorder = false;
        for (var method : node.methods) {
            for (AbstractInsnNode instruction : method.instructions) {
                if (!(instruction instanceof MethodInsnNode call)) {
                    continue;
                }
                assertFalse(call.owner.equals("appeng/me/Grid") && call.name.equals("getNodes"),
                        "the requester repair regressed to an O(N) grid scan");
                assertFalse(call.owner.equals("appeng/api/networking/IGridNode")
                                && call.name.equals("getService"),
                        "the requester repair must not resolve every grid node's power service");
                if (call.owner.equals("cn/dancingsnow/neoecoprototype/integration/ae2/"
                        + "RemovedRequesterIterator") && call.name.equals("<init>")) {
                    wrapsIterator = true;
                }
                if (call.owner.equals("cn/dancingsnow/neoecoprototype/integration/ae2/"
                        + "RemovedRequesterIterator") && call.name.equals("restoreEligible")) {
                    restoresRecorder = true;
                }
            }
        }
        assertTrue(wrapsIterator, "injectProviderPower no longer records iterator removals");
        assertTrue(restoresRecorder, "the recorded removals are never restored");
    }

    private static TreeSet<IAEPowerStorage> sorted(IAEPowerStorage... storages) {
        var requesters = new TreeSet<IAEPowerStorage>(Comparator.comparingInt(IAEPowerStorage::getPriority));
        for (IAEPowerStorage storage : storages) {
            requesters.add(storage);
        }
        return requesters;
    }

    private static final class TestStorage implements IAEPowerStorage {
        private final int priority;
        private final boolean publicStorage;
        private final AccessRestriction flow;
        private final double current;
        private final double maximum;

        private TestStorage(int priority, boolean publicStorage, AccessRestriction flow,
                            double current, double maximum) {
            this.priority = priority;
            this.publicStorage = publicStorage;
            this.flow = flow;
            this.current = current;
            this.maximum = maximum;
        }

        @Override
        public double injectAEPower(double amount, Actionable mode) {
            return amount;
        }

        @Override
        public double extractAEPower(double amount, Actionable mode, PowerMultiplier multiplier) {
            return 0.0;
        }

        @Override
        public double getAEMaxPower() {
            return maximum;
        }

        @Override
        public double getAECurrentPower() {
            return current;
        }

        @Override
        public boolean isAEPublicPowerStorage() {
            return publicStorage;
        }

        @Override
        public AccessRestriction getPowerFlow() {
            return flow;
        }

        @Override
        public int getPriority() {
            return priority;
        }
    }
}

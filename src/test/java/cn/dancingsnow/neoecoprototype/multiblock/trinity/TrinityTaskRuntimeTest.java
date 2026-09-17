package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TrinityTaskRuntimeTest {
    @Test
    void refreshMapsReadinessToReadyAndBlocked() {
        TrinityTaskRuntime runtime = new TrinityTaskRuntime();
        runtime.refresh(new TrinityTaskReadiness(true, List.of()));
        assertEquals(TrinityTaskRuntime.State.READY, runtime.state());

        runtime.refresh(TrinityTaskReadiness.blocked(List.of("energy unavailable")));
        assertEquals(TrinityTaskRuntime.State.BLOCKED, runtime.state());
        assertEquals(List.of("energy unavailable"), runtime.reasons());
    }

    @Test
    void preflightNeverOverwritesALiveJob() {
        TrinityTaskRuntime runtime = new TrinityTaskRuntime();
        runtime.syncExecution(TrinityCraftingExecutor.Phase.RUNNING, "crafting oak planks");
        assertEquals(TrinityTaskRuntime.State.RUNNING, runtime.state());
        assertTrue(runtime.isExecuting());

        // The panel refreshes the preflight every tick; it must not reset a running job to READY.
        runtime.refresh(TrinityTaskReadiness.ready());
        assertEquals(TrinityTaskRuntime.State.RUNNING, runtime.state());

        runtime.syncExecution(TrinityCraftingExecutor.Phase.COMPLETED, "4 oak planks arrived");
        assertEquals(TrinityTaskRuntime.State.COMPLETED, runtime.state());
        assertEquals(List.of("4 oak planks arrived"), runtime.reasons());

        // Only an explicit clear returns the runtime to the preflight family.
        runtime.clear();
        assertEquals(TrinityTaskRuntime.State.IDLE, runtime.state());
        runtime.refresh(TrinityTaskReadiness.ready());
        assertEquals(TrinityTaskRuntime.State.READY, runtime.state());
    }

    @Test
    void clearReturnsToIdle() {
        TrinityTaskRuntime runtime = new TrinityTaskRuntime();
        runtime.refresh(TrinityTaskReadiness.blocked(List.of("missing cell")));
        runtime.clear();
        assertEquals(TrinityTaskRuntime.State.IDLE, runtime.state());
        assertEquals(List.of(), runtime.reasons());
    }
}

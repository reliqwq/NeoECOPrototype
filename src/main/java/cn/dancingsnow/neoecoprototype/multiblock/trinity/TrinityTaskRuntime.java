package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import java.util.List;

/**
 * UI projection for Trinity's task state.
 *
 * <p>The executor owns the real task lifecycle. This class only projects executor phases and
 * request-validation hints into the panel; it never decides whether AE2 can craft a task.</p>
 */
public final class TrinityTaskRuntime {
    private State state = State.IDLE;
    private List<String> reasons = List.of();

    public State state() {
        return state;
    }

    public List<String> reasons() {
        return reasons;
    }

    /**
     * Returns whether the panel must preserve an executor report over advisory refreshes.
     * Terminal states are included deliberately: their result remains visible until the user
     * explicitly clears it.
     */
    public boolean isExecuting() {
        return state == State.RUNNING || state == State.WAITING_OUTPUT
                || state == State.COMPLETED || state == State.FAILED;
    }

    /**
     * Projects readiness hints only. The result is not an execution verdict; AE2 decides whether
     * the submitted task can actually be planned and run.
     */
    public void refresh(TrinityTaskReadiness readiness) {
        if (isExecuting()) {
            return;
        }
        if (readiness == null) {
            state = State.IDLE;
            reasons = List.of("No task preflight is available.");
        } else if (readiness.executable()) {
            state = State.READY;
            reasons = List.of();
        } else {
            state = State.BLOCKED;
            reasons = readiness.reasons();
        }
    }

    /** Mirrors the executor's phase into the panel state; IDLE leaves the preflight state alone. */
    public void syncExecution(TrinityCraftingExecutor.Phase phase, String detail) {
        List<String> detailLines = detail == null || detail.isEmpty() ? List.of() : List.of(detail);
        switch (phase) {
            case IDLE -> {
                // The executor is not driving; keep whatever the preflight reported.
            }
            case PLANNING, RUNNING -> {
                state = State.RUNNING;
                reasons = detailLines;
            }
            case WAITING_OUTPUT -> {
                state = State.WAITING_OUTPUT;
                reasons = detailLines;
            }
            case COMPLETED -> {
                state = State.COMPLETED;
                reasons = detailLines;
            }
            case FAILED -> {
                state = State.FAILED;
                reasons = detailLines;
            }
        }
    }

    public void clear() {
        state = State.IDLE;
        reasons = List.of();
    }

    public enum State {
        IDLE,
        READY,
        BLOCKED,
        RUNNING,
        WAITING_OUTPUT,
        COMPLETED,
        FAILED
    }
}

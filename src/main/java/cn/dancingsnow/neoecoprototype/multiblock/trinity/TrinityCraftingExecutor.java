package cn.dancingsnow.neoecoprototype.multiblock.trinity;

import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.crafting.CalculationStrategy;
import appeng.api.networking.crafting.ICraftingLink;
import appeng.api.networking.crafting.ICraftingPlan;
import appeng.api.networking.crafting.ICraftingRequester;
import appeng.api.networking.crafting.ICraftingSimulationRequester;
import appeng.api.networking.crafting.ICraftingSubmitResult;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.KeyCounter;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import com.google.common.collect.ImmutableSet;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.Future;

/**
 * Trinity's execution layer: it turns a preflighted task request into a real AE2 crafting job and
 * follows it all the way to a verified output.
 *
 * <p>Trinity deliberately does <em>not</em> implement its own crafting worker. The plan is computed
 * by AE2's {@code ICraftingService} and executed by whatever crafting CPU the ME network provides
 * (an AE2 crafting storage block or eco's computation machine). Trinity's contribution is exactly
 * the part AE2 normally leaves to a terminal: request the plan, submit it under Trinity's own
 * name, keep the link, and confirm the output actually landed back in the network.</p>
 *
 * <p>Every storage read uses {@code SIMULATE} except the output check, which also uses
 * {@code SIMULATE} -- extraction and ingredient pushing are the CPU's job, not Trinity's.</p>
 */
public final class TrinityCraftingExecutor {
    public enum Phase {
        IDLE,
        PLANNING,
        RUNNING,
        WAITING_OUTPUT,
        COMPLETED,
        FAILED
    }

    private static final int PLAN_TIMEOUT_TICKS = 200;
    private static final int RUN_TIMEOUT_TICKS = 1200;
    private static final int OUTPUT_TIMEOUT_TICKS = 200;

    private static final String NBT_PHASE = "trinityJobPhase";
    private static final String NBT_DETAIL = "trinityJobDetail";
    private static final String NBT_TARGET = "trinityJobTarget";
    private static final String NBT_REQUESTED = "trinityJobRequested";
    private static final String NBT_PRODUCED = "trinityJobProduced";

    private final ICraftingRequester owner;
    private final ICraftingSimulationRequester simulationRequester = new ICraftingSimulationRequester() {
        @Override
        public IActionSource getActionSource() {
            return actionSource;
        }

        @Override
        @Nullable
        public IGridNode getGridNode() {
            return owner.getActionableNode();
        }
    };

    private Phase phase = Phase.IDLE;
    private String detail = "No task has been submitted yet.";
    @Nullable
    private IActionSource actionSource;
    @Nullable
    private Future<ICraftingPlan> planFuture;
    @Nullable
    private ICraftingLink link;
    @Nullable
    private AEItemKey target;
    private long requested;
    private long produced;
    private long baseline;
    private int ticksInPhase;

    public TrinityCraftingExecutor(ICraftingRequester owner) {
        this.owner = owner;
    }

    public Phase phase() {
        return phase;
    }

    public String detail() {
        return detail;
    }

    @Nullable
    public AEItemKey target() {
        return target;
    }

    public long requested() {
        return requested;
    }

    /** Output the network gained since the job was submitted; only meaningful once running. */
    public long produced() {
        return produced;
    }

    /** Ticks spent in the current phase; a stalled planning phase shows up here. */
    public int ticksInPhase() {
        return ticksInPhase;
    }

    public boolean isBusy() {
        return phase == Phase.PLANNING || phase == Phase.RUNNING || phase == Phase.WAITING_OUTPUT;
    }

    /** Accept output only from the currently owned live link and only while the job is active. */
    public boolean acceptsCraftedItems(@Nullable ICraftingLink incoming) {
        return incoming != null && incoming == link
                && (phase == Phase.RUNNING || phase == Phase.WAITING_OUTPUT)
                && !incoming.isCanceled() && !incoming.isDone();
    }

    /** True once the job reached a terminal state and is only being reported. */
    public boolean isTerminal() {
        return phase == Phase.COMPLETED || phase == Phase.FAILED;
    }

    /**
     * Asks AE2 to plan the requested stack. Nothing is extracted or crafted here; the plan is
     * submitted on a later tick once the asynchronous calculation finishes.
     */
    public void failBeforeStart(String reason) {
        if (!isBusy()) {
            reset();
            fail(reason);
        }
    }

    public boolean start(Level level, IGrid grid, IActionSource source, AEItemKey target, long amount) {
        if (isBusy()) {
            return false;
        }
        if (amount <= 0) {
            fail("Task quantity must be greater than zero.");
            return false;
        }
        reset();
        this.actionSource = source;
        this.target = target;
        this.requested = amount;
        if (grid.getCraftingService().getCpus().isEmpty()) {
            fail("No AE2 crafting CPU is available on the network; Trinity computation bytes are not an AE2 CPU.");
            return false;
        }
        this.baseline = countAvailable(grid, target);
        try {
            this.planFuture = grid.getCraftingService().beginCraftingCalculation(
                    level, simulationRequester, target, amount, CalculationStrategy.REPORT_MISSING_ITEMS);
        } catch (RuntimeException exception) {
            fail("The crafting calculation could not be started: " + exception);
            return false;
        }
        this.phase = Phase.PLANNING;
        this.ticksInPhase = 0;
        this.detail = "Planning " + target.getId() + " x" + amount + ".";
        log("STARTED target=" + target.getId() + " amount=" + amount);
        return true;
    }

    /** Advances the job by one server tick. */
    public void tick(@Nullable IGrid grid, IActionSource source) {
        if (phase == Phase.IDLE || isTerminal()) {
            return;
        }
        if (grid == null) {
            fail("The ME network disappeared while the task was running.");
            return;
        }
        switch (phase) {
            case PLANNING -> tickPlanning(grid, source);
            case RUNNING -> tickRunning();
            case WAITING_OUTPUT -> tickWaitingForOutput(grid);
            default -> {
            }
        }
        ticksInPhase++;
    }

    private void tickPlanning(IGrid grid, IActionSource source) {
        Future<ICraftingPlan> future = planFuture;
        if (future == null) {
            fail("The crafting calculation was lost.");
            return;
        }
        if (!future.isDone()) {
            if (ticksInPhase > PLAN_TIMEOUT_TICKS) {
                future.cancel(true);
                fail("Planning timed out after " + PLAN_TIMEOUT_TICKS + " ticks.");
                return;
            }
            detail = "Planning " + describeTarget() + " (" + ticksInPhase + " ticks elapsed).";
            return;
        }
        ICraftingPlan plan;
        try {
            plan = future.get();
        } catch (Exception exception) {
            fail("The crafting calculation failed: " + exception);
            return;
        }
        planFuture = null;
        if (plan == null) {
            fail("The crafting service produced no plan.");
            return;
        }
        if (plan.simulation()) {
            fail("The plan is only a simulation and was not submitted: " + describeMissing(plan));
            return;
        }
        log("PLAN_READY target=" + describeTarget() + " final=" + plan.finalOutput().what().getId());
        ICraftingSubmitResult result;
        try {
            result = grid.getCraftingService().submitJob(plan, owner, null, false, source);
        } catch (RuntimeException exception) {
            fail("Submitting the job failed: " + exception);
            return;
        }
        if (result == null || !result.successful()) {
            fail("The crafting service refused the job: "
                    + (result == null ? "no result was returned" : result.errorCode()
                    + (result.errorDetail() == null ? "" : " (" + result.errorDetail() + ")")));
            return;
        }
        link = result.link();
        if (link == null) {
            fail("The crafting service accepted the job without returning a tracking link.");
            return;
        }
        log("SUBMITTED target=" + describeTarget() + " link=attached");
        phase = Phase.RUNNING;
        ticksInPhase = 0;
        detail = "Crafting " + plan.finalOutput().what().getId() + " x" + plan.finalOutput().amount()
                + "; cpu=busy";
    }

    private void tickRunning() {
        ICraftingLink activeLink = link;
        if (activeLink == null) {
            fail("The crafting link was lost.");
            return;
        }
        // Treat cancellation as terminal even if an implementation briefly reports both flags.
        if (activeLink.isCanceled()) {
            fail("The crafting job was cancelled.");
            return;
        }
        if (activeLink.isDone()) {
            enterWaitingForOutput();
            return;
        }
        if (ticksInPhase > RUN_TIMEOUT_TICKS) {
            fail("The crafting job timed out after " + RUN_TIMEOUT_TICKS
                    + " ticks; the network may be unable to run the matched pattern.");
        }
    }

    /** Single transition gate used by both the ticker and AE2's requester callback. */
    private void enterWaitingForOutput() {
        if (phase != Phase.RUNNING) {
            return;
        }
        phase = Phase.WAITING_OUTPUT;
        ticksInPhase = 0;
        detail = "The crafting job finished; waiting for the output to reach the network.";
        log("WAITING_OUTPUT target=" + describeTarget());
    }

    private void tickWaitingForOutput(IGrid grid) {
        if (target == null) {
            fail("The output check has no target item.");
            return;
        }
        produced = countAvailable(grid, target) - baseline;
        if (produced < 0L) {
            produced = 0L;
        }
        if (produced >= requested) {
            // AE2 marks the link dead before notifying the requester; release it as soon as the
            // output is verified so completed jobs are no longer advertised through getRequestedJobs().
            cancelPendingWork();
            phase = Phase.COMPLETED;
            ticksInPhase = 0;
            detail = "Completed: " + produced + " x " + target.getId() + " reached the network.";
            log("COMPLETED target=" + describeTarget() + " produced=" + produced);
            return;
        }
        if (ticksInPhase > OUTPUT_TIMEOUT_TICKS) {
            fail("The job finished but only " + produced + "/" + requested + " of "
                    + target.getId() + " reached the network.");
        }
    }

    /** Cancels a running job; a finished job is simply cleared. */
    public void cancel() {
        if (isBusy()) {
            fail("The task was cancelled by the operator.");
            return;
        }
        reset();
    }

    /** Clears every trace of the previous job so a new one can be submitted. */
    public void reset() {
        cancelPendingWork();
        phase = Phase.IDLE;
        detail = "No task has been submitted yet.";
        target = null;
        requested = 0L;
        produced = 0L;
        baseline = 0L;
        ticksInPhase = 0;
    }

    /** Called by AE2 when a link we own changes state. */
    public void onJobStateChange(@Nullable ICraftingLink changed) {
        if (changed == null || changed != link) {
            return;
        }
        if (changed.isDone() && phase == Phase.RUNNING) {
            log("LINK_DONE target=" + describeTarget());
            enterWaitingForOutput();
        }
    }

    private String describeTarget() {
        return (target == null ? "unknown" : target.getId()) + " x" + requested;
    }

    private static long countAvailable(IGrid grid, AEItemKey key) {
        KeyCounter available = grid.getStorageService().getInventory().getAvailableStacks();
        return available.get(key);
    }

    private void log(String message) {
        NeoECOPrototype.LOGGER.info("TrinityCraftingExecutor {}", message);
    }

    private static String describeMissing(ICraftingPlan plan) {
        StringBuilder builder = new StringBuilder();
        for (var entry : plan.missingItems()) {
            if (builder.length() > 0) {
                builder.append(", ");
            }
            builder.append(entry.getKey().getId()).append(" x").append(entry.getLongValue());
        }
        return builder.isEmpty() ? "no missing items were reported" : "missing " + builder;
    }

    private void fail(String reason) {
        cancelPendingWork();
        phase = Phase.FAILED;
        detail = reason;
        log("FAILED target=" + describeTarget() + " reason=" + reason);
        ticksInPhase = 0;
    }

    /** Releases all live AE2 work before the executor changes task ownership. */
    private void cancelPendingWork() {
        Future<ICraftingPlan> pendingPlan = planFuture;
        planFuture = null;
        if (pendingPlan != null && !pendingPlan.isDone()) {
            pendingPlan.cancel(true);
        }
        ICraftingLink activeLink = link;
        link = null;
        if (activeLink != null && !activeLink.isDone() && !activeLink.isCanceled()) {
            activeLink.cancel();
        }
        actionSource = null;
    }

    /**
     * Persists only the reportable part of the job. The live {@link ICraftingLink} belongs to the
     * running server, so a job that was still in flight when the world was saved is reported as
     * failed rather than silently resumed.
     */
    public void save(CompoundTag data) {
        data.putString(NBT_PHASE, phase.name());
        data.putString(NBT_DETAIL, detail);
        data.putLong(NBT_REQUESTED, requested);
        data.putLong(NBT_PRODUCED, produced);
        if (target != null) {
            data.putString(NBT_TARGET, target.getId().toString());
        }
    }

    public void load(CompoundTag data) {
        reset();
        requested = data.getLong(NBT_REQUESTED);
        produced = data.getLong(NBT_PRODUCED);
        String savedTarget = data.getString(NBT_TARGET);
        if (!savedTarget.isEmpty()) {
            try {
                ResourceLocation id = ResourceLocation.parse(savedTarget);
                var item = BuiltInRegistries.ITEM.get(id);
                if (item != Items.AIR) {
                    target = AEItemKey.of(item);
                }
            } catch (RuntimeException ignored) {
                target = null;
            }
        }
        String savedPhase = data.getString(NBT_PHASE);
        String savedDetail = data.getString(NBT_DETAIL);
        Phase saved = parsePhase(savedPhase);
        if (saved == Phase.PLANNING || saved == Phase.RUNNING || saved == Phase.WAITING_OUTPUT) {
            phase = Phase.FAILED;
            detail = "The previous job was interrupted by a world reload; it was not resumed.";
            log("INTERRUPTED_ON_RELOAD target=" + describeTarget() + " savedPhase=" + saved);
            return;
        }
        if (saved != null) {
            phase = saved;
            detail = savedDetail.isEmpty() ? detail : savedDetail;
        }
    }

    @Nullable
    private static Phase parsePhase(String name) {
        if (name == null || name.isEmpty()) {
            return null;
        }
        try {
            return Phase.valueOf(name);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    public ImmutableSet<ICraftingLink> getRequestedJobs() {
        return link == null ? ImmutableSet.of() : ImmutableSet.of(link);
    }
}

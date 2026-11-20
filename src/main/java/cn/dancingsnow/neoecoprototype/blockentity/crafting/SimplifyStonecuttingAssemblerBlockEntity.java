package cn.dancingsnow.neoecoprototype.blockentity.crafting;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.crafting.IPatternDetails;
import appeng.api.implementations.IPowerChannelState;
import appeng.api.implementations.blockentities.ICraftingMachine;
import appeng.api.implementations.blockentities.PatternContainerGroup;
import appeng.api.networking.IGrid;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IManagedGridNode;
import appeng.api.networking.energy.IEnergyService;
import appeng.api.networking.ticking.IGridTickable;
import appeng.api.networking.ticking.TickRateModulation;
import appeng.api.networking.ticking.TickingRequest;
import appeng.api.stacks.AEFluidKey;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import appeng.api.upgrades.IUpgradeInventory;
import appeng.api.upgrades.IUpgradeableObject;
import appeng.api.upgrades.UpgradeInventories;
import appeng.blockentity.grid.AENetworkedInvBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipe;
import cn.dancingsnow.neoecoprototype.recipe.ProcessorAssemblerRecipes;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.fluids.FluidStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The L1 processor assembler as a standalone machine, in the shape ExtendedAE's own extended molecular
 * assembler proved on the same AE2: {@code AENetworkedInvBlockEntity + IUpgradeableObject + IGridTickable +
 * ICraftingMachine} - no subclass of AE2's assembler anywhere, so AE2's item-only completion path is not in
 * the picture and the fluid-output mixin that bridged it is gone.
 *
 * <p>A pattern provider pushes materials through {@link ICraftingMachine#pushPattern}; the machine matches
 * the push against its recipe family, consumes the whole batch, and runs its own cycle: progress at the
 * speed-card curve (10/13/17/20/25/50 per tick, the numbers the inherited assembler used) paid out of the
 * grid's energy service at the same per-card tax (1.0-5.0). Completion delivers the item half through one
 * ejected stack plus the network (the old surplus path) and the fluid half through the pending queue; a
 * batch may state both halves. There is no crafting grid to fill, so the old nine-slot refusal is gone with
 * the mixin - a 64x fluid pattern runs here like any other.
 *
 * <p>In-flight jobs are in-memory only and survive neither a save nor a pattern-inventory rebuild; the
 * pending fluid queue is persisted, exactly the split the previous implementation had.
 */
public class SimplifyStonecuttingAssemblerBlockEntity extends AENetworkedInvBlockEntity
        implements IUpgradeableObject, IGridTickable, ICraftingMachine, IPowerChannelState {
    public static final net.minecraft.resources.ResourceLocation OUTPUT_INVENTORY =
            NeoECOPrototype.id("assembler_output");
    public static final net.minecraft.resources.ResourceLocation PREVIEW_INVENTORY =
            NeoECOPrototype.id("assembler_preview");

    /** Progress per tick and its energy tax for zero through five speed cards - AE2's own assembler curve. */
    private static final int[] SPEED_RATE = {10, 13, 17, 20, 25, 50};
    private static final double[] SPEED_TAX = {1.0, 1.3, 1.7, 2.0, 2.5, 5.0};

    private final AppEngInternalInventory outputInventory = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory previewInventory = new AppEngInternalInventory(this, 9);
    private final IUpgradeInventory upgrades = UpgradeInventories.forMachine(
            ModRegistration.SIMPLIFY_STONECUTTING_ASSEMBLER_ITEM.get(), 5, this::saveChanges);

    @Nullable
    private SimplifyAssemblerJob activeJob;
    private double progress;
    private boolean isAwake;
    private boolean powered;
    private boolean dirtyOutput;

    /** Synced to the client through the stream; the TESR rebuilds the animation from these. */
    private byte animationSpeed;
    private ItemStack animationDisplay = ItemStack.EMPTY;

    private final List<GenericStack> pendingFluidOutputs = new ArrayList<>();
    private boolean pendingFluidOutputsDirty;

    /** The last pattern output we logged a refusal for, so a provider retrying every tick logs once. */
    @Nullable
    private AEItemKey refusedOutput;

    public SimplifyStonecuttingAssemblerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode().setIdlePowerUsage(0.0)
                .addService(IGridTickable.class, this);
    }

    // ---------------------------------------------------------------- cycles

    @Override
    public TickRateModulation tickingRequest(IGridNode node, int ticksSinceLastCall) {
        ejectOutput();
        if (activeJob == null) {
            updateSleepiness();
            return TickRateModulation.SLEEP;
        }
        int cards = upgrades.getInstalledUpgrades(appeng.core.definitions.AEItems.SPEED_CARD);
        int rate = SPEED_RATE[Math.min(cards, SPEED_RATE.length - 1)];
        double tax = SPEED_TAX[Math.min(cards, SPEED_TAX.length - 1)];
        progress += userPower(ticksSinceLastCall, rate, tax);
        if (progress >= 100) {
            completeJob();
        }
        updateSleepiness();
        return activeJob != null ? TickRateModulation.FASTER : TickRateModulation.SLEEP;
    }

    @Override
    public TickingRequest getTickingRequest(IGridNode node) {
        return new TickingRequest(1, 1, !isAwake);
    }

    /** The inherited curve: extract the tick's AE from the grid, and advance by what was actually paid. */
    private int userPower(int ticks, int rate, double tax) {
        IGrid grid = getMainNode().getGrid();
        if (grid == null) return 0;
        IEnergyService energy = grid.getEnergyService();
        double drawn = energy.extractAEPower((double) ticks * rate * tax,
                Actionable.MODULATE, PowerMultiplier.CONFIG);
        return (int) (drawn / tax);
    }

    private void updateSleepiness() {
        boolean wantsWork = activeJob != null || !outputInventory.getStackInSlot(0).isEmpty();
        if (isAwake != wantsWork) {
            isAwake = wantsWork;
            getMainNode().ifPresent((grid, node) -> {
                if (isAwake) grid.getTickManager().wakeDevice(node);
                else grid.getTickManager().sleepDevice(node);
            });
        }
    }

    /**
     * One finished batch. The item half rides the same path the inherited machine used - one stack out
     * through the neighbors, everything beyond it into the network and, failing that, on the floor - and the
     * fluid half rides the pending queue. A recipe may state both halves; neither is delivered twice.
     */
    private void completeJob() {
        SimplifyAssemblerJob job = activeJob;
        if (job == null || level == null || level.isClientSide) {
            activeJob = null;
            progress = 0;
            return;
        }
        ProcessorAssemblerRecipe recipe = job.recipe();
        if (recipe.hasItemOutput()) {
            long total = Math.multiplyExact(job.runs(), recipe.result().getCount());
            ItemStack first = job.itemOutput();
            ItemStack rest = first.copy();
            for (Direction direction : Direction.values()) {
                rest = pushTo(rest, direction);
                if (rest.isEmpty()) break;
            }
            long ejected = first.getCount() - rest.getCount();
            if (!rest.isEmpty()) outputInventory.setItemDirect(0, rest);
            long surplus = total - ejected - rest.getCount();
            if (surplus > 0) {
                deliverSurplus(AEItemKey.of(recipe.result()), surplus);
            }
            appeng.crafting.CraftingEvent.fireAutoCraftingEvent(level, job.pattern(), first,
                    SimplifyAssemblerJob.singleSlotContainer(first));
        }
        if (recipe.hasFluidOutput()) {
            FluidStack fluid = job.fluidOutput();
            if (!fluid.isEmpty()) {
                stageFluidOutput(AEFluidKey.of(fluid), fluid.getAmount());
                NeoECOPrototype.LOGGER.info("[stonecutting assembler] completed {} runs of {} at {}: fluid"
                        + " staged {} mB, item output {}", job.runs(), recipe.result().isEmpty()
                        ? "<fluid>" : recipe.result().getHoverName().getString(),
                        worldPosition, fluid.getAmount(), job.itemOutput().isEmpty()
                                ? "<none>" : job.itemOutput().getCount() + "x");
            }
        }
        activeJob = null;
        progress = 0;
        clearPreview();
        animationSpeed = 0;
        animationDisplay = ItemStack.EMPTY;
        markForUpdate();
        saveChanges();
    }

    // ---------------------------------------------------------------- pattern intake

    @Override
    public boolean acceptsPlans() {
        return activeJob == null;
    }

    @Override
    public PatternContainerGroup getCraftingMachineInfo() {
        int cards = upgrades.getInstalledUpgrades(appeng.core.definitions.AEItems.SPEED_CARD);
        List<Component> tooltip = cards > 0 ? List.of(Component.translatable(
                "gui.tooltips.ae2.CpuStatusCoProcessors", cards)) : List.of();
        return new PatternContainerGroup(AEItemKey.of(getBlockState().getBlock()),
                hasCustomName() ? getCustomName() : getBlockState().getBlock().getName(), tooltip);
    }

    @Override
    public boolean pushPattern(IPatternDetails pattern, KeyCounter[] inputs, Direction direction) {
        if (level == null || level.isClientSide) return false;
        ProcessorAssemblerRecipe recipe = matchRecipe(pattern);
        if (recipe == null) {
            logRefusal(pattern, "这台机器只跑处理器装配配方，普通分子装配样板请交给分子装配室");
            return false;
        }
        long runs = SimplifyAssemblerJob.requestedRuns(pattern, recipe);
        String refusal = refusalReason(recipe, runs);
        if (refusal != null) {
            logRefusal(pattern, refusal);
            return false;
        }
        if (!SimplifyAssemblerJob.consumeJobInputs(pattern, inputs)) {
            logRefusal(pattern, "推送的原料不是这个样板的恰好一批（多了或少了都会让材料凭空消失）");
            return false;
        }
        beginJob(pattern, recipe, runs);
        return true;
    }

    /**
     * Why a push cannot run right now, or null when it can. The pending-fluid gate first: with undelivered
     * fluid waiting, every new push waits too, so a slow network cannot pile up an unbounded queue.
     */
    @Nullable
    private String refusalReason(ProcessorAssemblerRecipe recipe, long runs) {
        if (runs <= 0) {
            return "样板没有陈述一个整数份的产出";
        }
        if (!pendingFluidOutputs.isEmpty()) {
            return "本机还有待送入 AE 网络的流体产物，待网络恢复后会自动重试";
        }
        if (activeJob != null) {
            return "上一批还在加工中";
        }
        int cap = NeoECOPrototypeServerConfig.processorAssemblerMaxRunsPerCycle();
        if (runs > cap) {
            return "样板一次要 " + runs + " 份，超过本机每周期上限 " + cap
                    + " 份（服务端配置 processor_assembler_max_runs_per_cycle）";
        }
        if (recipe.hasItemOutput()
                && NeoECOPrototypeServerConfig.isProcessorRecipeDisabled(recipe.result().getItem())) {
            return "该产物的处理器配方已被服务端配置关闭";
        }
        return null;
    }

    @Nullable
    private ProcessorAssemblerRecipe matchRecipe(IPatternDetails pattern) {
        return ProcessorAssemblerRecipes.resolve(level, pattern);
    }

    private void beginJob(IPatternDetails pattern, ProcessorAssemblerRecipe recipe, long runs) {
        activeJob = new SimplifyAssemblerJob(pattern, recipe, runs);
        progress = 0;
        refusedOutput = null;
        fillPreview(recipe);
        int cards = upgrades.getInstalledUpgrades(appeng.core.definitions.AEItems.SPEED_CARD);
        animationSpeed = (byte) Math.min(cards, 5);
        animationDisplay = recipe.hasItemOutput() ? recipe.result().copy() : ItemStack.EMPTY;
        markForUpdate();
        saveChanges();
        updateSleepiness();
    }

    private void fillPreview(ProcessorAssemblerRecipe recipe) {
        var stacks = new SimplifyAssemblerJob(null, recipe, 1).previewStacks();
        for (int slot = 0; slot < 9; slot++) previewInventory.setItemDirect(slot, stacks.get(slot));
    }

    private void clearPreview() {
        for (int slot = 0; slot < 9; slot++) previewInventory.setItemDirect(slot, ItemStack.EMPTY);
    }

    /**
     * Refusing silently is what made this impossible to diagnose from a report: the crafting CPU had
     * already accepted the job, so all the player sees is materials that never move. Logged once per
     * machine per output, because a provider retries a refused pattern every tick.
     */
    private void logRefusal(IPatternDetails pattern, String reason) {
        var outputs = pattern.getOutputs();
        AEItemKey output = outputs.size() == 1 && outputs.get(0).what() instanceof AEItemKey key ? key : null;
        if (output == null || output.equals(refusedOutput)) return;
        refusedOutput = output;
        var slots = new StringBuilder();
        for (IPatternDetails.IInput slot : pattern.getInputs()) {
            GenericStack[] possible = slot.getPossibleInputs();
            slots.append('[').append(possible.length).append('x')
                    .append(possible.length > 0 ? possible[0].amount() : 0).append('x')
                    .append(slot.getMultiplier()).append(']');
        }
        NeoECOPrototype.LOGGER.warn("[stonecutting assembler] refused a pattern at {}: {}；output={} "
                        + "patternClass={} slots={} askedBy={} (每槽 = 候选数 x 每候选数量 x 批量倍数)",
                worldPosition, reason, outputs, pattern.getClass().getSimpleName(), slots, describeCallers());
    }

    /** The first few callers outside our own code, so a refusal says who pushed. */
    private String describeCallers() {
        var picked = new ArrayList<String>();
        for (var frame : Thread.currentThread().getStackTrace()) {
            String cls = frame.getClassName();
            if (cls.startsWith("java.") || cls.startsWith("jdk.")
                    || cls.startsWith("cn.dancingsnow.neoecoprototype.")) {
                continue;
            }
            picked.add(cls.substring(cls.lastIndexOf('.') + 1) + "#" + frame.getMethodName());
            if (picked.size() == 4) break;
        }
        return picked.isEmpty() ? "unknown" : String.join(" < ", picked);
    }

    // ---------------------------------------------------------------- outputs

    /** Ejects the finished stack into whatever neighbors accept; leftovers wait in the output slot. */
    private void ejectOutput() {
        ItemStack stack = outputInventory.getStackInSlot(0);
        if (stack.isEmpty()) return;
        ItemStack rest = stack.copy();
        for (Direction direction : Direction.values()) {
            rest = pushTo(rest, direction);
            if (rest.isEmpty()) break;
        }
        if (ItemStack.matches(rest, stack)) return;
        outputInventory.setItemDirect(0, rest);
        saveChanges();
    }

    private ItemStack pushTo(ItemStack stack, Direction direction) {
        if (stack.isEmpty() || level == null) return stack;
        var neighbor = appeng.api.inventories.InternalInventory.wrapExternal(level,
                worldPosition.relative(direction), direction.getOpposite());
        return neighbor == null ? stack : neighbor.addItems(stack);
    }

    /**
     * Where the part of a finished batch that did not fit into the one ejected stack goes: this grid's
     * storage, since a cycle can only hand over a single stack of an item. Whatever the network will not
     * take falls next to the machine -- the alternative is that the materials simply stop existing.
     */
    private void deliverSurplus(AEItemKey what, long amount) {
        long left = amount;
        var grid = getMainNode().getGrid();
        if (grid != null) {
            left -= grid.getStorageService().getInventory().insert(what, left,
                    Actionable.MODULATE, appeng.api.networking.security.IActionSource.ofMachine(this));
        }
        if (left <= 0) return;
        if (!(getLevel() instanceof ServerLevel serverLevel)) {
            NeoECOPrototype.LOGGER.warn("[stonecutting assembler] had no output for {} x {} at {}",
                    what, left, worldPosition);
            return;
        }
        var center = worldPosition.getCenter();
        int stackSize = what.toStack(1).getMaxStackSize();
        while (left > 0) {
            int count = (int) Math.min(stackSize, left);
            serverLevel.addFreshEntity(new ItemEntity(serverLevel, center.x, center.y, center.z,
                    new ItemStack(what.getItem(), count)));
            left -= count;
        }
    }

    private void stageFluidOutput(AEFluidKey key, long amount) {
        if (amount <= 0) return;
        addFluidOutput(pendingFluidOutputs, new GenericStack(key, amount));
        pendingFluidOutputsDirty = true;
    }

    private static void addFluidOutput(List<GenericStack> target, GenericStack addition) {
        if (addition == null || addition.amount() <= 0 || !(addition.what() instanceof AEFluidKey)) return;
        for (int i = 0; i < target.size(); i++) {
            GenericStack existing = target.get(i);
            if (existing.what().equals(addition.what())) {
                target.set(i, new GenericStack(existing.what(), Math.addExact(existing.amount(), addition.amount())));
                return;
            }
        }
        target.add(addition);
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state,
            SimplifyStonecuttingAssemblerBlockEntity blockEntity) {
        if (level.isClientSide) return;
        blockEntity.flushPendingFluidOutputs();
    }

    public void flushPendingFluidOutputs() {
        if (pendingFluidOutputs.isEmpty() || level == null || level.isClientSide) return;
        var grid = getMainNode().getGrid();
        if (grid == null) return;
        var storage = grid.getStorageService().getInventory();
        var source = appeng.api.networking.security.IActionSource.ofMachine(this);
        for (int i = 0; i < pendingFluidOutputs.size();) {
            GenericStack pending = pendingFluidOutputs.get(i);
            if (!(pending.what() instanceof AEFluidKey fluidKey) || pending.amount() <= 0) {
                pendingFluidOutputs.remove(i);
                saveChanges();
                continue;
            }
            long accepted = storage.insert(fluidKey, pending.amount(), Actionable.MODULATE, source);
            if (accepted <= 0) {
                i++;
                continue;
            }
            long left = pending.amount() - accepted;
            if (left == 0) {
                pendingFluidOutputs.remove(i);
                NeoECOPrototype.LOGGER.info("[stonecutting assembler] flushed {} mB of {} into the network"
                        + " at {}", pending.amount(), fluidKey.getDisplayName().getString(), worldPosition);
            } else {
                pendingFluidOutputs.set(i, new GenericStack(fluidKey, left));
            }
            saveChanges();
        }
    }

    public List<GenericStack> pendingFluidOutputs() {
        return List.copyOf(pendingFluidOutputs);
    }

    // ---------------------------------------------------------------- power state

    @Override
    public void onMainNodeStateChanged(appeng.api.networking.IGridNodeListener.State state) {
        if (state == appeng.api.networking.IGridNodeListener.State.GRID_BOOT) {
            return;
        }
        boolean nowPowered = false;
        IGrid grid = getMainNode().getGrid();
        if (grid != null && getMainNode().isPowered()) {
            nowPowered = grid.getEnergyService()
                    .extractAEPower(1.0, Actionable.SIMULATE, PowerMultiplier.CONFIG) > 1.0E-4;
        }
        if (nowPowered != powered) {
            powered = nowPowered;
            markForUpdate();
        }
    }

    @Override
    public boolean isPowered() {
        return powered;
    }

    @Override
    public boolean isActive() {
        return powered;
    }

    // ---------------------------------------------------------------- upgrades

    @Override
    public IUpgradeInventory getUpgrades() {
        return upgrades;
    }

    /** The pattern the machine is currently running, or null - the batch-cap guard reads this. */
    @Nullable
    public IPatternDetails getCurrentPattern() {
        return activeJob == null ? null : activeJob.pattern();
    }

    /** What the panel's progress bar and the TESR read back. */
    public int getCraftingProgress() {
        return (int) progress;
    }

    public byte getAnimationSpeed() {
        return animationSpeed;
    }

    public ItemStack getAnimationDisplay() {
        return animationDisplay;
    }

    public AppEngInternalInventory getOutputInventory() {
        return outputInventory;
    }

    public AppEngInternalInventory getPreviewInventory() {
        return previewInventory;
    }

    // ---------------------------------------------------------------- inventory plumbing

    @Override
    public appeng.api.inventories.InternalInventory getInternalInventory() {
        return outputInventory;
    }

    @Override
    public appeng.api.inventories.InternalInventory getSubInventory(net.minecraft.resources.ResourceLocation id) {
        if (OUTPUT_INVENTORY.equals(id)) return outputInventory;
        if (PREVIEW_INVENTORY.equals(id)) return previewInventory;
        return super.getSubInventory(id);
    }

    @Override
    protected appeng.api.inventories.InternalInventory getExposedInventoryForSide(Direction side) {
        return outputInventory;
    }

    @Override
    public void onChangeInventory(AppEngInternalInventory inv, int slot) {
        if (inv == outputInventory) saveChanges();
    }

    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        ItemStack output = outputInventory.getStackInSlot(0);
        if (!output.isEmpty()) drops.add(output);
        for (ItemStack card : upgrades) drops.add(card);
    }

    @Override
    public void clearContent() {
        outputInventory.clear();
        clearPreview();
        upgrades.clear();
    }

    // ---------------------------------------------------------------- persistence

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        outputInventory.writeToNBT(tag, "output", registries);
        upgrades.writeToNBT(tag, "upgrades", registries);
        writeFluidOutputs(tag, "pendingFluidOutputs", pendingFluidOutputs, registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        outputInventory.readFromNBT(tag, "output", registries);
        upgrades.readFromNBT(tag, "upgrades", registries);
        pendingFluidOutputs.clear();
        readFluidOutputs(tag.getList("pendingFluidOutputs", Tag.TAG_COMPOUND), registries, pendingFluidOutputs);
    }

    private static void writeFluidOutputs(CompoundTag tag, String key, List<GenericStack> stacks,
            HolderLookup.Provider registries) {
        if (stacks.isEmpty()) return;
        ListTag outputs = new ListTag();
        for (GenericStack stack : stacks) outputs.add(GenericStack.writeTag(registries, stack));
        tag.put(key, outputs);
    }

    private static void readFluidOutputs(ListTag outputs, HolderLookup.Provider registries,
            List<GenericStack> target) {
        for (int i = 0; i < outputs.size(); i++) {
            GenericStack stack = GenericStack.readTag(registries, outputs.getCompound(i));
            if (stack != null && stack.what() instanceof AEFluidKey && stack.amount() > 0) target.add(stack);
        }
    }

    // ---------------------------------------------------------------- client sync

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf buf) {
        boolean changed = super.readFromStream(buf);
        byte speed = buf.readByte();
        ItemStack display = ItemStack.OPTIONAL_STREAM_CODEC.decode(buf);
        changed |= speed != animationSpeed || !ItemStack.matches(display, animationDisplay);
        animationSpeed = speed;
        animationDisplay = display;
        powered = buf.readBoolean();
        return changed;
    }

    @Override
    protected void writeToStream(RegistryFriendlyByteBuf buf) {
        super.writeToStream(buf);
        buf.writeByte(animationSpeed);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buf, animationDisplay);
        buf.writeBoolean(powered);
    }
}

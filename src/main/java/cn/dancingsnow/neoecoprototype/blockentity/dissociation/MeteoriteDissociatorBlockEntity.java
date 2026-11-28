package cn.dancingsnow.neoecoprototype.blockentity.dissociation;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.config.PowerUnit;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.recipe.DissociationRecipe;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.neoforge.energy.EnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.templates.FluidTank;
import net.neoforged.neoforge.items.IItemHandler;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * The meteorite core dissociator: one core and two baths of fluid in, twelve weighted products out.
 *
 * <p>Everything about a run - what goes in, what can come out, how much fluid and power it costs and how
 * long it takes - is read from the {@link DissociationRecipe} that matches the input. This class owns no
 * numbers of its own except the two buffers, so a datapack retunes the machine without touching it.
 *
 * <p>Power is denominated in AE because that is what the tables are written in. The FE buffer is filled
 * from outside and spent first; the grid only pays the remainder, which is what lets the machine run in a
 * pack that feeds it FE and has no ME network at all.
 */
public class MeteoriteDissociatorBlockEntity extends AENetworkedBlockEntity
        implements InternalInventoryHost {

    /** What automation may do through one block face. */
    public enum FaceMode {
        NONE,
        INPUT,
        OUTPUT,
        BOTH;

        public boolean acceptsInput() {
            return this == INPUT || this == BOTH;
        }

        public boolean acceptsOutput() {
            return this == OUTPUT || this == BOTH;
        }

        public FaceMode next() {
            return values()[(ordinal() + 1) % values().length];
        }

        public FaceMode previous() {
            return values()[(ordinal() + values().length - 1) % values().length];
        }
    }

    public static final int OUTPUT_SLOTS = DissociationRecipe.MAX_PRODUCTS;
    /** Both baths are this big, so one full run's worth fits {@value} / 5 000 = ten times over. */
    public static final int TANK_CAPACITY = 50_000;
    public static final int FE_CAPACITY = 400_000;
    /** A cable can push this much per transfer, and nothing can pull AE back out through it. */
    public static final int FE_MAX_RECEIVE = 8_000;

    private static final double ENERGY_EPSILON = 0.001D;

    private static final String INPUT_TAG = "dissociator_input";
    private static final String CATALYST_TAG = "dissociator_catalyst";
    private static final String OUTPUT_TAG = "dissociator_outputs";
    private static final String COOLANT_TAG = "dissociator_coolant";
    private static final String SOLUTION_TAG = "dissociator_solution";
    private static final String ENERGY_TAG = "dissociator_fe";
    private static final String PROGRESS_TAG = "dissociator_progress";
    private static final String TOTAL_TAG = "dissociator_total_progress";
    private static final String RECIPE_TAG = "dissociator_table";
    private static final String FACE_MODES_TAG = "dissociator_face_modes";

    private final AppEngInternalInventory input = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory catalyst = new AppEngInternalInventory(this, 1);
    private final AppEngInternalInventory outputs = new AppEngInternalInventory(this, OUTPUT_SLOTS);
    private final IItemHandler[] itemInterfaces = new IItemHandler[Direction.values().length];
    private final IFluidHandler[] fluidInterfaces = new IFluidHandler[Direction.values().length];
    private final IItemHandler unsidedItemInterface;
    private final IFluidHandler unsidedFluidInterface;
    private final FaceMode[] faceModes = new FaceMode[Direction.values().length];

    private final FluidTank coolantTank = new FluidTank(TANK_CAPACITY, this::isCoolant);
    private final FluidTank solutionTank = new FluidTank(TANK_CAPACITY, this::isSolution);
    private final ReceiveOnlyBuffer energy = new ReceiveOnlyBuffer(FE_CAPACITY, FE_MAX_RECEIVE);

    private int progress;
    private int totalProgress = 1;
    /** What the clients were last told the baths hold, so a change can be pushed once instead of per tick. */
    private FluidStack lastCoolantSent = FluidStack.EMPTY;
    private FluidStack lastSolutionSent = FluidStack.EMPTY;
    /** Which table the run in flight was started from, so finishing does not need the core any more. */
    @Nullable
    private ResourceLocation runningRecipe;

    public MeteoriteDissociatorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            outputs.setMaxStackSize(slot, DissociationRecipe.MAX_PRODUCTS * 64);
        }
        for (Direction direction : Direction.values()) {
            faceModes[direction.ordinal()] = FaceMode.INPUT;
            itemInterfaces[direction.ordinal()] = new SidedItemHandler(direction);
            fluidInterfaces[direction.ordinal()] = new SidedFluidHandler(direction);
        }
        unsidedItemInterface = new SidedItemHandler(null);
        unsidedFluidInterface = new SidedFluidHandler(null);
    }

    /**
     * FE goes in from a cable and is spent only by the machine itself. NeoForge's {@code extractEnergy}
     * refuses outright when {@code maxExtract} is zero, so giving the buffer a private way to pay is what
     * keeps a cable from taking the charge back out without leaving the machine unable to run.
     */
    private static final class ReceiveOnlyBuffer extends EnergyStorage {
        ReceiveOnlyBuffer(int capacity, int maxReceive) {
            super(capacity, maxReceive, 0);
        }

        int pay(int wanted, boolean simulate) {
            int extracted = Math.max(0, Math.min(wanted, getEnergyStored()));
            if (!simulate) {
                this.energy -= extracted;
            }
            return extracted;
        }
    }

    // ==================== the run ====================

    /** Server ticker, wired through {@code AEBaseEntityBlock.setBlockEntity}. */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MeteoriteDissociatorBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    private void tickServer(Level level) {
        syncTanksIfNeeded();
        if (progress > 0) {
            progress++;
            if (progress >= totalProgress) {
                progress = 0;
                finishRun(level);
                runningRecipe = null;
            }
            setChanged();
            return;
        }
        var holder = currentRecipe(level);
        if (holder == null) {
            runningRecipe = null;
            return;
        }
        DissociationRecipe recipe = holder.value();
        CatalystBonus bonus = catalystBonus();
        double cost = recipe.powerPerRun() * bonus.powerPercent() / 100.0D;
        if (!hasFluids(recipe) || !hasEnergy(cost, false)) {
            return;
        }
        if (!hasEnergy(cost, true)) {
            return;
        }
        drain(recipe);
        // A run can put at most draws x perHit of one product into one slot, so the slot has to be
        // widened to that before the roll happens rather than after it overflows.
        int limit = recipe.maxPerProduct();
        for (int slot = 0; slot < OUTPUT_SLOTS; slot++) {
            outputs.setMaxStackSize(slot, limit);
        }
        input.extractItem(0, 1, false);
        // The core is spent when the run starts, so the table has to be remembered by name: looking it up
        // from the input again at the end would find nothing and quietly yield no products.
        runningRecipe = holder.id();
        totalProgress = Math.max(1, recipe.ticksPerRun() * bonus.ticksPercent() / 100);
        progress = 1;
        setChanged();
    }

    @Nullable
    private RecipeHolder<DissociationRecipe> currentRecipe(Level level) {
        ItemStack stack = input.getStackInSlot(0);
        if (stack.isEmpty()) {
            return null;
        }
        return level.getRecipeManager()
                .getRecipeFor(ModRegistration.DISSOCIATION_TYPE.get(),
                        new DissociationRecipe.Input(stack), level)
                .orElse(null);
    }

    private void finishRun(Level level) {
        DissociationRecipe recipe = runningTable(level);
        if (recipe == null) {
            // Only a reload that removed the table can get here, and the run already paid for its fluids
            // and power. The core is gone either way, so nothing is owed to the player but the attempt.
            return;
        }
        for (Map.Entry<Item, Integer> entry : recipe.roll(level.getRandom()).entrySet()) {
            insertOrSpill(level, new ItemStack(entry.getKey(), entry.getValue()));
        }
    }

    @Nullable
    private DissociationRecipe runningTable(Level level) {
        if (runningRecipe == null) {
            return null;
        }
        return level.getRecipeManager().byKey(runningRecipe)
                .map(RecipeHolder::value)
                .filter(DissociationRecipe.class::isInstance)
                .map(DissociationRecipe.class::cast)
                .orElse(null);
    }

    /**
     * Products go wherever they fit - a slot already holding the same kind first, then any empty one - and
     * whatever does not fit falls out beside the machine. Nothing a run produced is destroyed by a full
     * output, which is the only failure this machine can otherwise reach silently.
     */
    private void insertOrSpill(Level level, ItemStack product) {
        ItemStack left = outputs.addItems(product, false);
        if (!left.isEmpty()) {
            Block.popResource(level, worldPosition.above(), left);
        }
    }

    // ==================== what a run needs ====================

    private boolean hasFluids(DissociationRecipe recipe) {
        return holds(recipe.coolant(), coolantTank.getFluid()) && holds(recipe.solution(), solutionTank.getFluid());
    }

    private static boolean holds(FluidStack wanted, FluidStack held) {
        return !wanted.isEmpty() && held.getFluid() == wanted.getFluid() && held.getAmount() >= wanted.getAmount();
    }

    private void drain(DissociationRecipe recipe) {
        coolantTank.drain(recipe.coolant(), IFluidHandler.FluidAction.EXECUTE);
        solutionTank.drain(recipe.solution(), IFluidHandler.FluidAction.EXECUTE);
    }

    /**
     * Ask for {@code aeCost} AE, paying from the FE buffer first and the grid second. Called as a
     * simulation before a run starts and again for real after it passed, because taking FE and then
     * finding the grid short would charge the player for a run that never happened.
     */
    private boolean hasEnergy(double aeCost, boolean apply) {
        Actionable mode = apply ? Actionable.MODULATE : Actionable.SIMULATE;
        long wantedFe = (long) Math.ceil(PowerUnit.AE.convertTo(PowerUnit.FE, aeCost));
        long fromFe = energy.pay((int) Math.min(wantedFe, Integer.MAX_VALUE), !apply);
        double paid = PowerUnit.FE.convertTo(PowerUnit.AE, fromFe);
        double rest = aeCost - paid;
        if (rest <= ENERGY_EPSILON) {
            return true;
        }
        double[] fromGrid = {0.0D};
        getMainNode().ifPresent(grid -> fromGrid[0] = grid.getEnergyService()
                .extractAEPower(rest, mode, PowerMultiplier.ONE));
        return paid + fromGrid[0] >= aeCost - ENERGY_EPSILON;
    }

    // ==================== catalyst ====================

    /** How one permanent bonus part changes a run, in percent of the table's own numbers. */
    private record CatalystBonus(int ticksPercent, int powerPercent) {
    }

    private static final CatalystBonus GRETTE_BONUS = new CatalystBonus(75, 125);

    /**
     * The catalogue is deliberately one line long: it is ours to tune, and every entry has to say what a
     * run is worth in ticks and in power, which is the part a pack author cannot derive from the item.
     */
    private CatalystBonus catalystBonus() {
        ItemStack stack = catalyst.getStackInSlot(0);
        if (!stack.isEmpty() && stack.is(ModRegistration.GRETTE_CRYSTAL.get())) {
            return GRETTE_BONUS;
        }
        return new CatalystBonus(100, 100);
    }

    // ==================== face configuration ====================

    public FaceMode getFaceMode(Direction direction) {
        return faceModes[direction.ordinal()];
    }

    public void setFaceMode(Direction direction, FaceMode mode) {
        int index = direction.ordinal();
        if (faceModes[index] == mode) {
            return;
        }
        faceModes[index] = mode;
        setChanged();
        markForClientUpdate();
    }

    public void cycleFaceMode(Direction direction) {
        setFaceMode(direction, getFaceMode(direction).next());
    }

    public void resetFaceModes() {
        boolean changed = false;
        for (int index = 0; index < faceModes.length; index++) {
            if (faceModes[index] != FaceMode.NONE) {
                faceModes[index] = FaceMode.NONE;
                changed = true;
            }
        }
        if (changed) {
            setChanged();
            markForClientUpdate();
        }
    }

    private static FaceMode faceMode(int ordinal) {
        FaceMode[] values = FaceMode.values();
        return ordinal >= 0 && ordinal < values.length ? values[ordinal] : FaceMode.INPUT;
    }

    private boolean anyFaceAcceptsInput() {
        for (FaceMode mode : faceModes) {
            if (mode.acceptsInput()) {
                return true;
            }
        }
        return false;
    }

    private boolean anyFaceAcceptsOutput() {
        for (FaceMode mode : faceModes) {
            if (mode.acceptsOutput()) {
                return true;
            }
        }
        return false;
    }

    private boolean anyFaceIsOpen() {
        for (FaceMode mode : faceModes) {
            if (mode != FaceMode.NONE) {
                return true;
            }
        }
        return false;
    }

    private boolean acceptsInput(@Nullable Direction side) {
        return side == null ? anyFaceAcceptsInput() : getFaceMode(side).acceptsInput();
    }

    private boolean acceptsOutput(@Nullable Direction side) {
        return side == null ? anyFaceAcceptsOutput() : getFaceMode(side).acceptsOutput();
    }

    // ==================== tanks ====================

    public FluidTank getCoolantTank() {
        return coolantTank;
    }

    public FluidTank getSolutionTank() {
        return solutionTank;
    }

    public EnergyStorage getEnergyStorage() {
        return energy;
    }

    @Nullable
    public EnergyStorage getEnergyStorage(@Nullable Direction side) {
        return acceptsInput(side) ? energy : null;
    }

    /**
     * One handler for both baths, pointed one way: the coolant side takes, the solution side gives. A pipe
     * or a bucket therefore cannot pull fresh coolant back out of the machine, which is the mistake the
     * two tanks would make if they were simply exposed as a list.
     */
    private final IFluidHandler fluidInterface = new IFluidHandler() {
        @Override
        public int getTanks() {
            return 2;
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return tank == 0 ? coolantTank.getFluid() : solutionTank.getFluid();
        }

        @Override
        public int getTankCapacity(int tank) {
            return TANK_CAPACITY;
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return tank == 0 ? isCoolant(stack) : isSolution(stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return coolantTank.fill(resource, action);
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return solutionTank.drain(resource, action);
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return solutionTank.drain(maxDrain, action);
        }
    };

    public IFluidHandler getFluidInterface() {
        return fluidInterface;
    }

    @Nullable
    public IFluidHandler getFluidInterface(@Nullable Direction side) {
        if (side == null) {
            return anyFaceIsOpen() ? unsidedFluidInterface : null;
        }
        return getFaceMode(side) == FaceMode.NONE ? null : fluidInterfaces[side.ordinal()];
    }

    private final class SidedFluidHandler implements IFluidHandler {
        @Nullable
        private final Direction side;

        private SidedFluidHandler(Direction side) {
            this.side = side;
        }

        @Override
        public int getTanks() {
            return fluidInterface.getTanks();
        }

        @Override
        public FluidStack getFluidInTank(int tank) {
            return fluidInterface.getFluidInTank(tank);
        }

        @Override
        public int getTankCapacity(int tank) {
            return fluidInterface.getTankCapacity(tank);
        }

        @Override
        public boolean isFluidValid(int tank, FluidStack stack) {
            return acceptsInput(side) && fluidInterface.isFluidValid(tank, stack);
        }

        @Override
        public int fill(FluidStack resource, FluidAction action) {
            return acceptsInput(side) ? fluidInterface.fill(resource, action) : 0;
        }

        @Override
        public FluidStack drain(FluidStack resource, FluidAction action) {
            return acceptsOutput(side) ? fluidInterface.drain(resource, action) : FluidStack.EMPTY;
        }

        @Override
        public FluidStack drain(int maxDrain, FluidAction action) {
            return acceptsOutput(side) ? fluidInterface.drain(maxDrain, action) : FluidStack.EMPTY;
        }
    }

    /**
     * A bath accepts only a fluid some table actually names, so a pipe cannot fill the coolant side with
     * lava and leave the machine holding something no run will ever consume.
     */
    private boolean isCoolant(FluidStack stack) {
        return anyTableNames(stack, true);
    }

    private boolean isSolution(FluidStack stack) {
        return anyTableNames(stack, false);
    }

    private boolean anyTableNames(FluidStack stack, boolean coolant) {
        if (level == null || stack.isEmpty()) {
            return false;
        }
        Fluid fluid = stack.getFluid();
        for (var holder : level.getRecipeManager()
                .getAllRecipesFor(ModRegistration.DISSOCIATION_TYPE.get())) {
            DissociationRecipe recipe = holder.value();
            if (recipe.coolant().getFluid() == fluid && coolant) {
                return true;
            }
            if (recipe.solution().getFluid() == fluid && !coolant) {
                return true;
            }
        }
        return false;
    }

    // ==================== inventories ====================

    public InternalInventory getInputInventory() {
        return input;
    }

    public InternalInventory getCatalystInventory() {
        return catalyst;
    }

    public InternalInventory getOutputInventory() {
        return outputs;
    }

    @Nullable
    public IItemHandler getItemInterface(@Nullable Direction side) {
        if (side == null) {
            return anyFaceIsOpen() ? unsidedItemInterface : null;
        }
        return getFaceMode(side) == FaceMode.NONE ? null : itemInterfaces[side.ordinal()];
    }

    private final class SidedItemHandler implements IItemHandler {
        private static final int INPUT_SLOTS = 2;
        @Nullable
        private final Direction side;

        private SidedItemHandler(Direction side) {
            this.side = side;
        }

        private InternalInventory inventory(int slot) {
            return slot == 0 ? input : slot == 1 ? catalyst : outputs;
        }

        private int localSlot(int slot) {
            return slot < INPUT_SLOTS ? 0 : slot - INPUT_SLOTS;
        }

        @Override
        public int getSlots() {
            return INPUT_SLOTS + OUTPUT_SLOTS;
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return slot >= 0 && slot < getSlots()
                    ? inventory(slot).getStackInSlot(localSlot(slot)) : ItemStack.EMPTY;
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (!acceptsInput(side) || slot < 0 || slot >= INPUT_SLOTS || stack.isEmpty()) {
                return stack;
            }
            return inventory(slot).insertItem(localSlot(slot), stack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            if (!acceptsOutput(side) || slot < INPUT_SLOTS || slot >= getSlots()) {
                return ItemStack.EMPTY;
            }
            return outputs.toItemHandler().extractItem(localSlot(slot), amount, simulate);
        }

        @Override
        public int getSlotLimit(int slot) {
            return slot >= 0 && slot < getSlots() ? inventory(slot).getSlotLimit(localSlot(slot)) : 0;
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return acceptsInput(side) && slot >= 0 && slot < INPUT_SLOTS
                    && inventory(slot).isItemValid(localSlot(slot), stack);
        }
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inv) {
        setChanged();
    }

    /** The loot table drops the machine itself; the cores and products inside it come from here. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, java.util.List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        for (var inv : new AppEngInternalInventory[]{input, catalyst, outputs}) {
            for (int slot = 0; slot < inv.size(); slot++) {
                ItemStack stack = inv.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    drops.add(stack.copy());
                }
            }
        }
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    // ==================== persistence ====================

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        input.writeToNBT(tag, INPUT_TAG, registries);
        catalyst.writeToNBT(tag, CATALYST_TAG, registries);
        outputs.writeToNBT(tag, OUTPUT_TAG, registries);
        tag.put(COOLANT_TAG, coolantTank.writeToNBT(registries, new CompoundTag()));
        tag.put(SOLUTION_TAG, solutionTank.writeToNBT(registries, new CompoundTag()));
        tag.put(ENERGY_TAG, energy.serializeNBT(registries));
        tag.putInt(PROGRESS_TAG, progress);
        tag.putInt(TOTAL_TAG, totalProgress);
        byte[] modes = new byte[faceModes.length];
        for (int index = 0; index < faceModes.length; index++) {
            modes[index] = (byte) faceModes[index].ordinal();
        }
        tag.putByteArray(FACE_MODES_TAG, modes);
        if (runningRecipe != null) {
            tag.putString(RECIPE_TAG, runningRecipe.toString());
        }
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        input.readFromNBT(tag, INPUT_TAG, registries);
        catalyst.readFromNBT(tag, CATALYST_TAG, registries);
        outputs.readFromNBT(tag, OUTPUT_TAG, registries);
        coolantTank.readFromNBT(registries, tag.getCompound(COOLANT_TAG));
        solutionTank.readFromNBT(registries, tag.getCompound(SOLUTION_TAG));
        if (tag.contains(ENERGY_TAG)) {
            energy.deserializeNBT(registries, tag.get(ENERGY_TAG));
        }
        progress = tag.getInt(PROGRESS_TAG);
        totalProgress = Math.max(1, tag.getInt(TOTAL_TAG));
        byte[] modes = tag.getByteArray(FACE_MODES_TAG);
        for (int index = 0; index < faceModes.length; index++) {
            faceModes[index] = index < modes.length ? faceMode(modes[index]) : FaceMode.INPUT;
        }
        runningRecipe = tag.contains(RECIPE_TAG)
                ? ResourceLocation.tryParse(tag.getString(RECIPE_TAG)) : null;
    }

    /**
     * Fluid enters through a pipe or a bucket, and {@code FluidTank} has no change callback - it mutates
     * its own field. So the tick compares against what the clients were last told and pushes a new packet
     * when it moved; anything that writes a bath is covered, not only the writers I remembered.
     */
    private void syncTanksIfNeeded() {
        FluidStack coolant = coolantTank.getFluid();
        FluidStack solution = solutionTank.getFluid();
        if (coolant.getAmount() != lastCoolantSent.getAmount()
                || coolant.getFluid() != lastCoolantSent.getFluid()
                || solution.getAmount() != lastSolutionSent.getAmount()
                || solution.getFluid() != lastSolutionSent.getFluid()) {
            lastCoolantSent = coolant.copy();
            lastSolutionSent = solution.copy();
            markForClientUpdate();
        }
    }

    /** Which fluid is in which bath, and how much, is visual state: it rides the short update packet. */
    @Override
    protected void writeToStream(RegistryFriendlyByteBuf data) {
        super.writeToStream(data);
        // OPTIONAL, not plain: FluidStack.STREAM_CODEC throws on an empty stack, and an empty bath is this
        // machine's default state - so the strict codec would take the whole server down on the first
        // chunk sync after the block is placed.
        FluidStack.OPTIONAL_STREAM_CODEC.encode(data, coolantTank.getFluid());
        FluidStack.OPTIONAL_STREAM_CODEC.encode(data, solutionTank.getFluid());
        for (FaceMode mode : faceModes) {
            data.writeByte((byte) mode.ordinal());
        }
    }

    @Override
    protected boolean readFromStream(RegistryFriendlyByteBuf data) {
        boolean changed = super.readFromStream(data);
        FluidStack coolant = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
        FluidStack solution = FluidStack.OPTIONAL_STREAM_CODEC.decode(data);
        boolean fluidsChanged = !FluidStack.isSameFluidSameComponents(coolant, coolantTank.getFluid())
                || !FluidStack.isSameFluidSameComponents(solution, solutionTank.getFluid());
        coolantTank.setFluid(coolant);
        solutionTank.setFluid(solution);
        for (int index = 0; index < faceModes.length; index++) {
            FaceMode mode = faceMode(data.readByte());
            if (faceModes[index] != mode) {
                faceModes[index] = mode;
                changed = true;
            }
        }
        return changed || fluidsChanged;
    }

    // ==================== progress, read by the panel ====================

    public int getProgress() {
        return progress;
    }

    /** How far the run in flight has got, 0..1, for the groove on the panel. */
    public float progressFraction() {
        if (progress <= 0) {
            return 0.0F;
        }
        return Mth.clamp((float) progress / totalProgress, 0.0F, 1.0F);
    }

    public void setProgress(int value) {
        this.progress = value;
    }

    public int getTotalProgress() {
        return totalProgress;
    }

    public void setTotalProgress(int value) {
        this.totalProgress = Math.max(1, value);
    }
}

package cn.dancingsnow.neoecoprototype.blockentity.reactor;

import appeng.api.networking.energy.IPassiveEnergyGenerator;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyReactorDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.reactor.SimplifyReactorStructure;
import cn.dancingsnow.neoecoprototype.multiblock.reactor.SimplifyReactorStructure.Part;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.entity.BlockEntityType;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Controller of the superconductive reactor.
 *
 * <p>The node service is the same one the energized superconductive interface publishes: AE2 pays a
 * grid for its single strongest passive generator, so a giant machine has to be one controller with
 * one rate, not a field of small ones.
 *
 * <p>The cube is not an eco cluster. Nothing else in the shell has a block entity, so the controller
 * validates the fixed box itself and caches the answer; {@link #getRate()} is then a field read.
 */
public class SimplifyReactorControllerBlockEntity extends AENetworkedBlockEntity
        implements IPassiveEnergyGenerator, MultiBlockBuildController.Host {

    /** How often a formed or broken cube is re-checked, in ticks. */
    private static final int SCAN_INTERVAL = 20;
    /** How many mismatched positions a status message names before it says "and more". */
    private static final int MAX_REPORTED_ISSUES = 4;

    private final MultiBlockBuildController buildController = new MultiBlockBuildController(this);
    private boolean suppressed;
    private boolean formed;
    private boolean storming;
    private int coils;
    private List<String> issues = List.of();
    private int selectedBuildLength = 1;
    private boolean buildInProgress;
    private int scanCountdown;

    public SimplifyReactorControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
        getMainNode()
                .setIdlePowerUsage(0)
                .addService(IPassiveEnergyGenerator.class, this);
    }

    // ============================ passive generator ============================

    @Override
    public double getRate() {
        if (suppressed || !formed) {
            return 0.0D;
        }
        double rate = NeoECOPrototypeServerConfig.reactorCoreAePerTick()
                + NeoECOPrototypeServerConfig.reactorCoilAePerTick() * coils;
        return storming ? rate * NeoECOPrototypeServerConfig.reactorStormMultiplier() : rate;
    }

    @Override
    public void setSuppressed(boolean suppressed) {
        this.suppressed = suppressed;
    }

    @Override
    public boolean isSuppressed() {
        return suppressed;
    }

    // ============================ structure scan ============================

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  SimplifyReactorControllerBlockEntity controller) {
        if (level instanceof ServerLevel serverLevel) {
            controller.tick(serverLevel);
        }
    }

    private void tick(ServerLevel level) {
        buildController.tick(level);
        if (scanCountdown > 0 && --scanCountdown > 0) {
            return;
        }
        scanCountdown = SCAN_INTERVAL;
        refresh(level);
    }

    /** Forces the next tick to re-check, so placing or breaking a shell block is seen at once. */
    public void requestScan() {
        scanCountdown = 0;
    }

    public void refresh(ServerLevel level) {
        ScanResult result = scan(level);
        formed = result.formed();
        coils = result.coils();
        storming = result.storming();
        issues = result.issues();
    }

    /** Walks the fixed box and reports what is wrong, if anything. Reads only; never places. */
    public ScanResult scan(ServerLevel level) {
        Direction facing = facing();
        List<String> found = new ArrayList<>();
        int foundCoils = 0;
        for (int depth = 0; depth < SimplifyReactorStructure.SIZE; depth++) {
            for (int up = -SimplifyReactorStructure.HALF; up <= SimplifyReactorStructure.HALF; up++) {
                for (int across = -SimplifyReactorStructure.HALF;
                        across <= SimplifyReactorStructure.HALF; across++) {
                    Part part = SimplifyReactorStructure.partAt(depth, across, up);
                    BlockPos pos = SimplifyReactorStructure.worldPos(worldPosition, facing,
                            SimplifyReactorStructure.localOffset(depth, across, up));
                    BlockState state = level.getBlockState(pos);
                    boolean ok = switch (part) {
                        case CONTROLLER -> state.is(ModRegistration.REACTOR_CONTROLLER_BLOCK.get());
                        case FRAME -> state.is(ModRegistration.REACTOR_FRAME_BLOCK.get());
                        case GLASS -> state.is(ModRegistration.REACTOR_GLASS_BLOCK.get());
                        case COIL -> state.is(ModRegistration.REACTOR_COIL_BLOCK.get());
                        case CORE -> state.is(ModRegistration.REACTOR_CORE_BLOCK.get());
                        case AIR -> state.isAir();
                    };
                    if (part == Part.COIL && ok) {
                        foundCoils++;
                    }
                    if (!ok && found.size() < MAX_REPORTED_ISSUES) {
                        found.add(pos.toShortString() + " expected " + expectedName(part));
                    }
                }
            }
        }
        boolean foundStorming = level.isThundering()
                && level.canSeeSky(SimplifyReactorStructure.topProbe(worldPosition, facing));
        return new ScanResult(found.isEmpty(), foundCoils, foundStorming, List.copyOf(found));
    }

    private static String expectedName(Part part) {
        return switch (part) {
            case CONTROLLER -> "reactor controller";
            case FRAME -> "reactor frame";
            case GLASS -> "reactor glass";
            case COIL -> "reactor coil";
            case CORE -> "reactor core";
            case AIR -> "air";
        };
    }

    private Direction facing() {
        BlockState state = getBlockState();
        return state.hasProperty(BlockStateProperties.HORIZONTAL_FACING)
                ? state.getValue(BlockStateProperties.HORIZONTAL_FACING)
                : Direction.NORTH;
    }

    // ============================ player feedback ============================

    public void reportStatus(ServerPlayer player) {
        if (level instanceof ServerLevel serverLevel) {
            refresh(serverLevel);
        }
        if (!formed) {
            player.displayClientMessage(Component.translatable(
                    "gui.neoecoprototype.reactor.status_broken", String.join("; ", issues)), false);
            return;
        }
        Component storm = storming
                ? Component.translatable("gui.neoecoprototype.reactor.storm_suffix",
                        format(NeoECOPrototypeServerConfig.reactorStormMultiplier()))
                : Component.empty();
        player.displayClientMessage(Component.translatable("gui.neoecoprototype.reactor.status_formed",
                coils, format(getRate()), storm), false);
    }

    private static String format(double value) {
        return String.format(Locale.ROOT, "%.0f", value);
    }

    /** Builds the cube for the player, or explains why it cannot; safe to call on a formed machine. */
    public void requestBuild(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return;
        }
        if (!canPlayerInteract(serverPlayer)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.multiblock.too_far"), true);
            return;
        }
        if (!(level instanceof ServerLevel)) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.multiblock.server_only"), true);
            return;
        }
        if (isFormed()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.reactor.already_formed"), true);
            return;
        }
        if (isBuildInProgress()) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.multiblock.build_in_progress"), true);
            return;
        }
        MultiBlockPlacementPlan plan = buildController.createLocalPreviewPlan();
        if (plan == null) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.multiblock.no_plan"), true);
            return;
        }
        if (!plan.getConflictPositions().isEmpty()) {
            serverPlayer.displayClientMessage(Component.translatable(
                    "gui.neoecoprototype.multiblock.conflicts", plan.getConflictPositions().size()), true);
            return;
        }
        if (!serverPlayer.isCreative()
                && !MultiBlockPlacementService.hasRequiredItems(serverPlayer, plan.getRequiredItems())) {
            serverPlayer.displayClientMessage(
                    Component.translatable("gui.neoecoprototype.multiblock.missing_items"), true);
            return;
        }
        buildController.autoBuild(serverPlayer);
    }

    // ============================ build host ============================

    @Override
    public MultiBlockDefinition getBuildDefinition() {
        return SimplifyReactorDefinition.CUBE;
    }

    @Override
    public int getMinBuildLength() {
        return getBuildDefinition().getExpandMin();
    }

    @Override
    public int getMaxBuildLength() {
        return getBuildDefinition().getExpandMax();
    }

    @Override
    public int getSelectedBuildLength() {
        return selectedBuildLength;
    }

    @Override
    public void setSelectedBuildLength(int length) {
        selectedBuildLength = Math.clamp(length, getMinBuildLength(), getMaxBuildLength());
    }

    /** The cube is fixed size and mirror-symmetric, so the toggle has nothing to act on. */
    @Override
    public boolean isMirrorBuild() {
        return false;
    }

    @Override
    public void setMirrorBuild(boolean mirrorBuild) {
        // Intentionally ignored; see isMirrorBuild().
    }

    @Override
    public boolean isBuildInProgress() {
        return buildInProgress;
    }

    @Override
    public void setBuildInProgress(boolean buildInProgress) {
        this.buildInProgress = buildInProgress;
    }

    @Override
    public boolean isFormed() {
        return formed;
    }

    @Override
    public boolean canPlayerInteract(Player player) {
        return level != null
                && player.level() == level
                && player.distanceToSqr(worldPosition.getX() + 0.5D, worldPosition.getY() + 0.5D,
                        worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public Level getBuildLevel() {
        return level;
    }

    @Override
    public BlockPos getBuildPosition() {
        return worldPosition;
    }

    @Override
    public BlockState getBuildState() {
        return getBlockState();
    }

    @Override
    public void rebuildAfterBuild() {
        if (level instanceof ServerLevel serverLevel) {
            refresh(serverLevel);
        }
    }

    @Override
    public void buildStateChanged() {
        setChanged();
        markForUpdate();
    }

    public record ScanResult(boolean formed, int coils, boolean storming, List<String> issues) {
    }
}

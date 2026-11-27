package cn.dancingsnow.neoecoprototype.blockentity.wireless;

import appeng.api.config.Actionable;
import appeng.api.config.PowerMultiplier;
import appeng.api.networking.energy.IEnergyService;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.event.WirelessLinks;
import cn.dancingsnow.neoecoprototype.multiblock.definition.CommunicationCoreDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.CommunicationCoreStructure;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/**
 * The communication core: a 1xN tower whose base is the controller and whose sections carry the
 * relay. It extends the subsystem channel module on purpose - that class already solved the one
 * problem a hub has, "one device, many links", with virtual dense ports hung off the widest
 * controller node, and {@code WirelessLinks} matches it by {@code instanceof}, so a formed tower's
 * links land on its ports and tower-to-tower links are refused by the module blacklist exactly the
 * way two modules would be.
 *
 * <p>The tower is not an eco cluster: nothing in the column carries a block entity, so the base
 * validates the column itself every {@link #SCAN_INTERVAL} ticks and caches the answer - the same
 * self-scan shape the superconductive reactor used. Formation is the whole gate for the relay: an
 * unformed tower keeps its node (cables still see it) but {@code bindReply} refuses it with one
 * sentence, and the column unforming drops every link it carried plus every port it had minted, so
 * a relay that lost its height never answers as a hub with stale outlets.
 *
 * <p>Its own idle draw is billed the way {@code WirelessLinks#bill} bills links - a MODULATE
 * extraction against this grid's energy service, every tick - rather than as node idle usage,
 * because the amount moves with the column and the efficient module, and the config-off contract
 * runs the whole family inert: with {@code wireless_links} off an unformed-or-formed tower draws
 * nothing and accepts nothing.
 */
public class CommunicationCoreBlockEntity extends WirelessChannelModuleBlockEntity
        implements MultiBlockBuildController.Host {

    /** How often a formed or broken column is re-checked, in ticks. */
    private static final int SCAN_INTERVAL = 20;

    /** Range modules one tower may carry: radius is bought by height, not by a wall of one module. */
    public static final int MAX_RANGE_MODULES = 2;

    /** Efficient modules one tower may carry: the discount is one decision, not a stack. */
    public static final int MAX_EFFICIENT_MODULES = 1;

    /** How many mismatched positions a status message names before it says "and more". */
    private static final int MAX_REPORTED_ISSUES = 4;

    private final MultiBlockBuildController buildController = new MultiBlockBuildController(this);
    private boolean formed;
    private int sections;
    private int rangeModules;
    private int efficientModules;
    private List<String> issues = List.of();
    private int selectedBuildLength = 1;
    private boolean buildInProgress;
    private int scanCountdown;

    public CommunicationCoreBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    // ============================ tower answers ============================

    /**
     * Radius a formed tower grants its own links. The bind path takes the larger of this and the
     * global range (which booster cards lift), so a pack tuning the global down can never shrink a
     * formed tower below what its height bought; an unformed tower answers 0, granting nothing.
     */
    public int formedRadiusBlocks() {
        if (!formed) {
            return 0;
        }
        return NeoECOPrototypeServerConfig.commCoreRadiusBase()
                + NeoECOPrototypeServerConfig.commCoreRadiusPerModule() * rangeModules;
    }

    /**
     * Devices a formed tower carries before AE2's own channel arithmetic becomes the real ceiling.
     * Height buys capacity; the count gate reads this per end and answers the shared at-limit key
     * with a tower-shaped sentence.
     */
    public int formedLinkCapacity() {
        if (!formed) {
            return 0;
        }
        return NeoECOPrototypeServerConfig.commCoreCapacityBase()
                + NeoECOPrototypeServerConfig.commCoreCapacityPerSection() * sections;
    }

    /** AE/t the formed column draws from its own grid; an unformed tower draws nothing. */
    public double idleDrawPerTick() {
        if (!formed) {
            return 0.0D;
        }
        double perBlock = NeoECOPrototypeServerConfig.commCoreIdlePerBlock();
        double blocks = 1 + sections;
        double multiplier = efficientModules > 0
                ? NeoECOPrototypeServerConfig.commCoreEfficientMultiplier()
                : 1.0D;
        return perBlock * blocks * multiplier;
    }

    public boolean isTowerFormed() {
        return formed;
    }

    /** Tower sections standing above the base, as the last scan found them. */
    public int towerSections() {
        return sections;
    }

    public int towerRangeModules() {
        return rangeModules;
    }

    public int towerEfficientModules() {
        return efficientModules;
    }

    public List<String> towerIssues() {
        return issues;
    }

    // ============================ structure scan ============================

    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  CommunicationCoreBlockEntity tower) {
        if (level instanceof ServerLevel serverLevel) {
            tower.tick(serverLevel);
        }
    }

    private void tick(ServerLevel level) {
        drawIdle(level);
        buildController.tick(level);
        if (scanCountdown > 0 && --scanCountdown > 0) {
            return;
        }
        scanCountdown = SCAN_INTERVAL;
        refresh(level);
    }

    public void refresh(ServerLevel level) {
        ScanResult result = scan(level);
        boolean wasFormed = formed;
        formed = result.formed();
        sections = result.sections();
        rangeModules = result.rangeModules();
        efficientModules = result.efficientModules();
        issues = result.issues();
        if (wasFormed && !formed) {
            // The relay died: kill the ports first (their links' edges die with them), then drop the
            // records, which all name this base's square - one unlink at the endpoint clears them.
            destroyPorts();
            WirelessLinks.unlinkReply(level, ModRegistration.WIRELESS_LINKS,
                    GlobalPos.of(level.dimension(), worldPosition));
        }
        setChanged();
    }

    /** Forces the next tick to re-check, so placing or breaking a section is seen at once. */
    public void requestScan() {
        scanCountdown = 0;
    }

    /**
     * Walks the column above the base and reports what is wrong, if anything. Reads only; never
     * places. The column stops at the first block that is not ours, so foreign neighbours above the
     * tower are the tower's business exactly as far as the tower reaches.
     */
    public ScanResult scan(ServerLevel level) {
        List<String> found = new ArrayList<>();
        int maxSections = NeoECOPrototypeServerConfig.commCoreMaxHeight() - 1;
        int body = 0;
        int range = 0;
        int efficient = 0;
        for (int index = 1; index <= maxSections; index++) {
            BlockState state = level.getBlockState(CommunicationCoreStructure.worldPos(worldPosition, index));
            if (state.is(ModRegistration.COMMUNICATION_CORE_BLOCK.get())) {
                body++;
            } else if (state.is(ModRegistration.COMMUNICATION_RANGE_MODULE_BLOCK.get())) {
                body++;
                range++;
            } else if (state.is(ModRegistration.COMMUNICATION_EFFICIENT_MODULE_BLOCK.get())) {
                body++;
                efficient++;
            } else {
                break;
            }
        }
        if (body == 0) {
            found.add("no section stands on the base");
        }
        BlockState above = level.getBlockState(
                CommunicationCoreStructure.worldPos(worldPosition, maxSections + 1));
        if (body == maxSections && isColumnBlock(above)) {
            found.add("the column reaches past the configured height cap of "
                    + (maxSections + 1) + " blocks at " + above.getBlock());
        }
        if (range > MAX_RANGE_MODULES) {
            found.add("the column carries " + range + " range modules, at most " + MAX_RANGE_MODULES);
        }
        if (efficient > MAX_EFFICIENT_MODULES) {
            found.add("the column carries " + efficient + " efficient modules, at most "
                    + MAX_EFFICIENT_MODULES);
        }
        return new ScanResult(found.isEmpty(), body, range, efficient, List.copyOf(found));
    }

    private static boolean isColumnBlock(BlockState state) {
        return state.is(ModRegistration.COMMUNICATION_CORE_BLOCK.get())
                || state.is(ModRegistration.COMMUNICATION_RANGE_MODULE_BLOCK.get())
                || state.is(ModRegistration.COMMUNICATION_EFFICIENT_MODULE_BLOCK.get());
    }

    // ============================ idle draw ============================

    /**
     * The tower's own draw, charged the way {@code WirelessLinks#bill} charges links. Per-link fees
     * bill on top of this; the config-off contract covers both, so with the family switched off the
     * tower is a dumb column of metal.
     */
    private void drawIdle(ServerLevel level) {
        double draw = idleDrawPerTick();
        if (draw <= 0.0D || !NeoECOPrototypeServerConfig.current(NeoECOPrototypeServerConfig.WIRELESS_LINKS)) {
            return;
        }
        var node = getMainNode().getNode();
        var grid = node == null ? null : node.getGrid();
        var energy = grid == null ? null : grid.getService(IEnergyService.class);
        if (energy == null) {
            return;
        }
        energy.extractAEPower(draw, Actionable.MODULATE, PowerMultiplier.ONE);
    }

    // ============================ player feedback ============================

    /** Builds the column for the player, or explains why it cannot; safe to call on a formed tower. */
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
                    Component.translatable("gui.neoecoprototype.comm_core.already_formed"), true);
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
        return CommunicationCoreDefinition.TOWER;
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

    /** A column has no mirror; the toggle has nothing to act on. */
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

    public record ScanResult(boolean formed, int sections, int rangeModules, int efficientModules,
                             List<String> issues) {
    }
}

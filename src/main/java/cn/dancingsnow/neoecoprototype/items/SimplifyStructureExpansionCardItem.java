package cn.dancingsnow.neoecoprototype.items;

import appeng.api.crafting.PatternDetailsHelper;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.core.definitions.AEItems;
import cn.dancingsnow.neoecoae.all.NEMultiBlocks;
import cn.dancingsnow.neoecoae.blocks.entity.ECOMachineCasingBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.computation.ECOComputationCoolingControllerBlockEntity;
import cn.dancingsnow.neoecoae.blocks.entity.crafting.ECOCraftingPatternBusBlockEntity;
import cn.dancingsnow.neoecoae.util.ICellHost;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockBuildController;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementPlan;
import cn.dancingsnow.neoecoae.multiblock.placement.MultiBlockPlacementService;
import cn.dancingsnow.neoecoae.multiblock.placement.RequiredItem;
import cn.dancingsnow.neoecoae.multiblock.placement.WorldPlannedBlock;
import cn.dancingsnow.neoecoprototype.config.NeoECOPrototypeServerConfig;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.SimplifyOffTableMembers;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyTrinityDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Works on a machine that has already been built, in two directions.
 *
 * <p><b>Along its edge</b> - eco's own one-click builder refuses to run on a formed machine
 * ({@code MultiBlockBuildController.autoBuild} returns on {@code isFormed}), so enlarging today means
 * tearing the whole thing down and building it again.</p>
 *
 * <p><b>Along its tier</b> - at the ceiling there is nothing left to grow, and the only way to a
 * higher tier is the same teardown, because the host block itself is registered per tier. Shift-click
 * a machine that is already at its ceiling and it swaps in place: the blocks that change with the tier
 * come back to the player first, then the higher tier's blocks are placed from what they carry.
 * Nothing is computed as a difference - the cost is simply the higher tier's own parts.</p>
 *
 * <p>Two things about eco's planner had to be worked around, and both only bite once the machine is
 * already formed - which is why upstream never trips over them:</p>
 *
 * <ul>
 * <li>{@code preview} compares whole block states ({@code existingState.equals(targetState)}), while
 * {@code ECOMachineCasingBlockEntity} writes its {@code invisible} property into the state when a
 * cluster forms. Every casing the cluster hides therefore stops matching the plan, and
 * {@code buildInstant} refuses outright on a non-empty conflict list. So the plan handed to it is
 * reclassified here: a position holding the block the plan wants, differing only in properties the
 * formation sets by itself, counts as reused rather than conflicting.</li>
 * <li>The plan never mentions the host cell at all - {@code preview} skips
 * {@code MultiBlockRotation.CONTROLLER_ANCHOR}, which is where the system block stands. A tier swap
 * therefore has to move the anchor by itself, and the anchor is also the block entity that owns the
 * build state, so the machine is re-formed through whatever block entity ends up in that cell.</li>
 * </ul>
 *
 * <p>Nothing is consumed and no block is broken unless every position the target shape needs is
 * already free, already holds the right block, or is one of the blocks this machine's own definition
 * claims and which the target shape wants differently.</p>
 */
public class SimplifyStructureExpansionCardItem extends Item {

    /** How many lines of the shortfall list fit in an action bar before it stops being readable. */
    private static final int MAX_REPORTED_ITEMS = 4;

    /** The name eco's own {@code NEBlock.FORMED} is written under. Never copied across a swap. */
    private static final String FORMED_PROPERTY = "formed";

    /**
     * The upgrade ladder this card knows: only upward, and every branch of it lands on L9 - which is what
     * the item's own text promises ("结构已满：直接升级至 L9"), and what the person who specified the card
     * just re-read it as saying. Our own L1 machines are on it together with eco's L4 and L6, so a maxed
     * room climbs as far as the parts in the bag let it; the {@link SimplifyMigrationDriveItem}s are the
     * other way to the same place and cost a disk instead of the kit.
     *
     * <p>Written as comparisons rather than a static map on purpose. eco's definitions are singletons
     * whose ceiling comes off {@code NEConfig} when {@code NEMultiBlocks} is initialised, and a static
     * field here would trigger that during item registration - before the config has been read.</p>
     */
    private static @Nullable MultiBlockDefinition nextTier(MultiBlockDefinition current) {
        if (current == SimplifyStorageDefinition.L1 || current == NEMultiBlocks.STORAGE_SYSTEM_L4
                || current == NEMultiBlocks.STORAGE_SYSTEM_L6) {
            return NEMultiBlocks.STORAGE_SYSTEM_L9;
        }
        if (current == SimplifyComputationDefinition.L1
                || current == NEMultiBlocks.COMPUTATION_SYSTEM_L4
                || current == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
            return NEMultiBlocks.COMPUTATION_SYSTEM_L9;
        }
        if (current == SimplifyCraftingDefinition.L1 || current == NEMultiBlocks.CRAFTING_SYSTEM_L4
                || current == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return NEMultiBlocks.CRAFTING_SYSTEM_L9;
        }
        return null;
    }

    /**
     * The tier this click should land on. The expansion card climbs straight to the ceiling; a
     * {@link SimplifyMigrationDriveItem} overrides this to cover one rung and nothing else.
     */
    protected @Nullable MultiBlockDefinition targetOf(MultiBlockDefinition current) {
        return nextTier(current);
    }

    /** The card's own ladder, readable without a machine standing there - the table guard walks it. */
    public static @Nullable MultiBlockDefinition ladderTarget(MultiBlockDefinition current) {
        return nextTier(current);
    }

    /**
     * Why a machine that is standing at its ceiling gets refused, said as the fact that actually applies.
     *
     * <p>One sentence about side length used to cover three different things, and two of them were lies.
     * "上限由配置项决定" is true of eco's three families - each definition takes its ceiling from
     * {@code NEConfig.*SystemMaxLength - 4} - and false of ours, which stop at
     * {@code SimplifyMultiblockConfig.L1_PLACEMENT_EXPAND_MAX}, a constant. And the Trinity and the reactor
     * declare {@code expandMin == expandMax}, so they were never growable and never will be - no config
     * value would have moved them.</p>
     *
     * <p>The fall-through answer is for a machine outside the ladder, and after the L1 rows went in there
     * is no such machine among the block entities that answer {@code MultiBlockBuildController.Host} -
     * eco ships three of those and this mod has its five, and the table guard walks all fourteen. It stays
     * because a machine neither card covers still has to be refused by a sentence that is true.</p>
     */
    public static String ceilingRefusalKey(MultiBlockDefinition atTheCeiling) {
        if (atTheCeiling == NEMultiBlocks.STORAGE_SYSTEM_L9
                || atTheCeiling == NEMultiBlocks.COMPUTATION_SYSTEM_L9
                || atTheCeiling == NEMultiBlocks.CRAFTING_SYSTEM_L9) {
            return "card.neoecoprototype.expansion.at_top";
        }
        if (atTheCeiling.getExpandMin() == atTheCeiling.getExpandMax()) {
            return "card.neoecoprototype.expansion.fixed_shape";
        }
        return "card.neoecoprototype.expansion.no_rung";
    }

    /**
     * The colour the click's bag should wear: the rung the machine <b>started</b> on, which is the question
     * the migration disks already answer ("each drive wears its start tier"), so a green bag and the green
     * disk agree about where those parts came from. 0 means plain, and it is what a bag can only be when no
     * host moved - growing a room along its edge leaves the host standing, so there is no tier in the bag
     * to name. L9 never appears here either: nothing climbs past it, so no click can start there.
     */
    public static int tierColour(@Nullable MultiBlockDefinition from) {
        if (from == SimplifyStorageDefinition.L1 || from == SimplifyComputationDefinition.L1
                || from == SimplifyCraftingDefinition.L1) {
            return ModRegistration.NAME_THEME_TIER_L1;
        }
        if (from == NEMultiBlocks.STORAGE_SYSTEM_L4 || from == NEMultiBlocks.COMPUTATION_SYSTEM_L4
                || from == NEMultiBlocks.CRAFTING_SYSTEM_L4) {
            return ModRegistration.NAME_THEME_TIER_L4;
        }
        if (from == NEMultiBlocks.STORAGE_SYSTEM_L6 || from == NEMultiBlocks.COMPUTATION_SYSTEM_L6
                || from == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return ModRegistration.NAME_THEME_TIER_L6;
        }
        return 0;
    }

    /** What to tell the player when {@link #targetOf} came back with nothing. */
    protected Component refusal(MultiBlockDefinition current, int length) {
        // Named by the machine rather than by a bare number: "已经是最大边长 11" on its own does not say
        // which of the three things above just happened, and the player has to decide what to go get.
        return Component.translatable(ceilingRefusalKey(current), current.getOwner().value().getName(),
                length);
    }

    /** Whether this card may still stretch a machine that is not at its ceiling yet. */
    protected boolean canGrow() {
        return true;
    }

    /** Whether the card itself is the price, so a successful click has to eat it. */
    protected boolean consumedOnSuccess() {
        return false;
    }

    /**
     * Whether a tier swap charges the player's inventory for the parts it changes. The expansion card
     * does - it is a builder, and its one-for-one ledger is what makes it safe to run on a loaded
     * machine. A rung card does not: the card itself is the price, so the click touches no inventory at
     * all and the swap has to be placed by hand instead of through eco's builder, which refuses to build
     * for a survival player it cannot bill.
     */
    protected boolean chargesParts() {
        return true;
    }

    /**
     * Whether the blocks a free tier swap takes out come back to the player as items. The rung cards let
     * them go: their price is the card, and the parts they convert are what that card stands for. The
     * migration drive prices its voucher at the whole kit instead, so it asks nothing of the inventory and
     * hands every replaced block back - the player's L1 parts have to end up somewhere they can use.
     */
    protected boolean refundsReplacedBlocks() {
        return false;
    }

    /**
     * Runs once a free swap has succeeded, still holding the block entity that stood in the host cell
     * beforehand, and with the click's own bag still open. That is the only moment a subclass can tell what
     * the machine used to be - the block standing there afterwards belongs to another tier and answers none
     * of those questions - and the only moment it can still put something into the same bag instead of
     * starting a second one.
     */
    protected void onSwapped(ServerLevel level, BlockPos pos, ServerPlayer player,
                             @Nullable BlockEntity previous, Owed owed) {
    }

    /**
     * The line a finished swap prints. The billing card returns every block it takes out, so its line
     * says so; the free one returns nothing, and a message that promises a refund the player will never
     * see is worse than no message.
     */
    protected String upgradedKey() {
        return "card.neoecoprototype.expansion.upgraded";
    }

    public SimplifyStructureExpansionCardItem(Properties properties) {
        super(properties);
    }

    /**
     * A target plan with eco's formation-flag mismatches moved off the conflict list.
     *
     * @param plan    the plan to hand over, with only genuinely foreign positions left conflicting
     * @param moved   positions holding this machine's own block that the target shape wants differently
     * @param bill    what the plan and {@code moved} together cost, merged per item
     * @param foreign a position that belongs to neither, which stops the whole card
     */
    private record Classified(MultiBlockPlacementPlan plan, List<WorldPlannedBlock> moved,
                              List<RequiredItem> bill, @Nullable BlockPos foreign) {
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        // Decided on both sides, from the world and the inventory alone, so client and server never
        // disagree about whether this click was the card's or the host's own panel.
        if (!(level.getBlockEntity(pos) instanceof MultiBlockBuildController.Host host)) {
            return InteractionResult.PASS;
        }
        Player player = context.getPlayer();
        if (player == null || !player.isShiftKeyDown()) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (!(player instanceof ServerPlayer serverPlayer)) {
            return InteractionResult.FAIL;
        }
        grow(host, (ServerLevel) level, pos, serverPlayer, context.getHand());
        return InteractionResult.CONSUME;
    }

    private void grow(MultiBlockBuildController.Host host, ServerLevel level, BlockPos pos,
                      ServerPlayer player, net.minecraft.world.InteractionHand hand) {
        // Plain: growing moves no host, so the parts this click hands back carry no tier to wear.
        Owed owed = new Owed(player, 0);
        if (!host.canPlayerInteract(player) || host.isBuildInProgress()) {
            say(player, "card.neoecoprototype.expansion.busy");
            return;
        }
        MultiBlockDefinition definition = host.getBuildDefinition();
        if (definition == null) {
            say(player, "card.neoecoprototype.expansion.no_definition");
            return;
        }
        // Whether this is a machine at all is the cluster's answer, not the plan's. A definition can name
        // only one block per cell, while the calculators take either interface kind and the computation
        // one lets an energized core wear a casing cell - so measuring formation off the table used to
        // call a running machine unformed. See SimplifyOffTableMembers.
        if (!host.isFormed()) {
            say(player, "card.neoecoprototype.expansion.not_formed");
            return;
        }
        int target = definition.getExpandMax();
        // selectedBuildLength is the panel's pending choice, not the size of what stands here - eco
        // initialises it to the ceiling, so reading it made every machine that never had its slider
        // moved look already maxed out. The size has to come off the world.
        Measured measured = largestSatisfiedLength(host, level, pos, definition, target);
        int current = measured.length();
        if (current < definition.getExpandMin()) {
            if (measured.blocked() == null) {
                // Formed, but the shape needs cells that hold nothing: "build it, or fill the gaps" is
                // still the truth to tell here.
                say(player, "card.neoecoprototype.expansion.not_formed");
                return;
            }
            // Formed, yet something that is not a member of this machine stands in a cell the shape
            // wants, and the coordinates are the only part of that a player can act on.
            reportBlocked(player, measured.blocked());
            return;
        }
        if (current >= target) {
            upgradeTier(host, level, pos, player, definition, current, hand);
            return;
        }
        // A card that only changes tiers must leave a short machine exactly as it found it - and must
        // not eat itself doing nothing, which is why this is checked before any block is touched.
        if (!canGrow()) {
            say(player, Component.translatable("card.neoecoprototype.upgrade.not_full", target));
            return;
        }

        Classified grown = classify(preview(host, level, pos, definition, target),
                plannedByPosition(preview(host, level, pos, definition, current)), level, false);
        if (grown.foreign() != null) {
            reportBlocked(player, grown.foreign());
            return;
        }
        MultiBlockPlacementPlan plan = grown.plan();
        List<RequiredItem> net = netted(grown.bill(), reclaimed(grown.moved(), level));
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, net)) {
            Component missing = shortfall(net);
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of", missing));
            handOverTicket(player, owed, growWorkOrder(definition), net, missing);
            owed.seal(level, pos);
            return;
        }
        // Paid for first, so a player who cannot afford the larger shape keeps the machine they had.
        // The price is the difference, not the whole shape: growing re-uses the blocks this machine already
        // holds, and eco's builder bills the whole bigger shape, so the refund has to be in the inventory
        // before it runs. Whatever is left over once it has spent them goes in the click's bag.
        var pending = giveNow(player, takeOut(level, player, positionsOf(grown.moved())));

        host.setSelectedBuildLength(target);
        if (!MultiBlockPlacementService.buildInstant(level, plan, player)) {
            // buildInstant only refuses here because a placement failed part-way through, so the
            // structure really did change. Re-form anyway: leaving the length bumped without telling
            // the cluster is the worse failure.
            host.rebuildAfterBuild();
            owed.bagAll(giveNow(player, pending));
            owed.seal(level, pos);
            say(player, "card.neoecoprototype.expansion.partly_built");
            return;
        }
        host.rebuildAfterBuild();
        owed.bagAll(giveNow(player, pending));
        owed.seal(level, pos);
        say(player, Component.translatable("card.neoecoprototype.expansion.grown", target,
                plan.getMissingBlocks().size()));
    }

    /**
     * Swaps an already-maxed machine up a tier without touching its size or its shape.
     *
     * <p>The three system families keep one geometry per family and vary only which block fills the
     * tier-bearing cells, so "what changes with the tier" is read off the two plans for the same
     * length rather than from a list of blocks: energy cells for storage, threading and parallel
     * cores plus the cooling controller for computation, parallel cores for crafting, and the host
     * block itself - which is the anchor, and so appears in neither plan.</p>
     */
    private void upgradeTier(MultiBlockBuildController.Host host, ServerLevel level, BlockPos pos,
                             ServerPlayer player, MultiBlockDefinition current, int length,
                             net.minecraft.world.InteractionHand hand) {
        Owed owed = new Owed(player, tierColour(current));
        MultiBlockDefinition target = targetOf(current);
        if (target == null) {
            say(player, refusal(current, length));
            return;
        }
        BlockState anchor = anchorTarget(host, level, pos, target);
        if (anchor == null) {
            say(player, "card.neoecoprototype.expansion.upgrade.shape");
            return;
        }
        Classified swap = classify(preview(host, level, pos, target, length),
                plannedByPosition(preview(host, level, pos, current, length)), level, true);
        if (swap.foreign() != null) {
            reportBlocked(player, swap.foreign());
            return;
        }
        if (!chargesParts()) {
            swapInPlace(level, pos, anchor, swap, player, hand, current, target, owed);
            return;
        }
        List<RequiredItem> bill = new ArrayList<>(swap.bill());
        mergeItem(bill, new ItemStack(anchor.getBlock().asItem()));
        List<RequiredItem> net = netted(bill, upgradeReclaimed(swap.moved(), level, pos));
        if (!player.isCreative() && !MultiBlockPlacementService.hasRequiredItems(player, net)) {
            Component missing = shortfall(net);
            say(player, Component.translatable("card.neoecoprototype.expansion.short_of", missing));
            handOverTicket(player, owed, upgradeWorkOrder(target), net, missing);
            owed.seal(level, pos);
            return;
        }

        // What every position this swap is about to destroy holds comes out first, and goes back after the
        // new blocks stand. A swap between two of eco's own tiers never needed this - its tier-bearing cells
        // hold nothing - but reaching an L1 room puts the drives and the pattern buses inside the moved set,
        // and giveBack asks for no drops, so without the rescue a player's mounted matrices and stored
        // patterns land on the floor (or worse) instead of staying where they were put.
        Map<BlockPos, List<ItemStack>> contents = rescueContents(swap.moved(), level, pos);
        // Unlike growing, a tier swap takes back parts the higher tier's bill never asks for - this tier's
        // own energy cells, cores and host - so the refund does not have to be in the bag before eco's
        // builder runs. It goes over afterwards, once the builder has consumed the kit and freed the room.
        var swapPositions = positionsOf(swap.moved());
        swapPositions.add(pos);
        var pending = takeOut(level, player, swapPositions);
        if (!MultiBlockPlacementService.buildInstant(level, swap.plan(), player)) {
            finishAnchor(level, pos, anchor, player);
            returnContents(level, contents, owed);
            owed.bagAll(pending);
            owed.seal(level, pos);
            say(player, "card.neoecoprototype.expansion.partly_built");
            return;
        }
        // The host block is not in the plan, so eco's builder never bills or consumes it. Taking it
        // after the rest is only safe because the bill above was checked with it included.
        if (!player.isCreative() && !consumeOne(player, new ItemStack(anchor.getBlock().asItem()))) {
            finishAnchor(level, pos, anchor, player);
            returnContents(level, contents, owed);
            owed.bagAll(pending);
            owed.seal(level, pos);
            say(player, "card.neoecoprototype.expansion.upgrade.host_missing");
            return;
        }
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
        returnContents(level, contents, owed);
        owed.bagAll(pending);
        owed.seal(level, pos);
        if (consumedOnSuccess() && !player.hasInfiniteMaterials()) {
            // Every earlier return path leaves both the machine and the card untouched, so this is the
            // only place the card can be eaten - and it is eaten after the new tier is standing.
            var held = player.getItemInHand(hand);
            if (held.is(this)) {
                held.shrink(1);
            }
        }
        say(player, Component.translatable(upgradedKey(),
                tierLabel(target), swap.moved().size() + 1));
    }

    /**
     * Everything the positions of this swap hold, keyed by the position that held it, and taken out before
     * any of those blocks is destroyed. Both swap paths need it: {@code giveBack}'s {@code destroyBlock} asks
     * for no drops, so anything still inside a block when it goes is gone.
     */
    private static Map<BlockPos, List<ItemStack>> rescueContents(List<WorldPlannedBlock> moved, Level level,
                                                                 BlockPos host) {
        Map<BlockPos, List<ItemStack>> contents = new LinkedHashMap<>();
        for (WorldPlannedBlock position : moved) {
            List<ItemStack> taken = carriedContent(level, position.worldPos());
            if (!taken.isEmpty()) {
                contents.put(position.worldPos(), taken);
            }
        }
        List<ItemStack> atHost = carriedContent(level, host);
        if (!atHost.isEmpty()) {
            contents.put(host, atHost);
        }
        return contents;
    }

    /** Back into the position it came from where the new block will take it, and into the bag if it will not. */
    private static void returnContents(Level level, Map<BlockPos, List<ItemStack>> contents, Owed owed) {
        contents.forEach((where, stacks) -> stacks.forEach(
                stack -> returnContent(level, where, stack, owed)));
    }

    /**
     * Puts a tier swap in without touching anyone's inventory, because the rung card is the whole price:
     * there is nothing to bill, so writing the positions from the plan is both simpler and the only option -
     * eco's builder refuses to build for a survival player it cannot charge, which is exactly the gate the
     * expansion card needs and this one does not.
     *
     * <p>What this does spend is the block that stood in each moved position, and whatever it held. Since
     * this card reached our own L1 rooms that set is every cell in the machine - casings, drives with a
     * matrix mounted in them, buses with patterns in them - not just the tier-bearing parts, so
     * {@link #rescueContents} takes the contents out before the block goes and {@link #returnContents} puts
     * them back after it. The blocks themselves go onto the click's list and leave in one backup drive,
     * because a disk costs a disk and not the inventory - and the empty shell the disk comes back as is that
     * same bag, whose tile is the darkened drive and whose right-click turns it back.</p>
     */
    private void swapInPlace(ServerLevel level, BlockPos pos, BlockState anchor, Classified swap,
                             ServerPlayer player, net.minecraft.world.InteractionHand hand,
                             MultiBlockDefinition current, MultiBlockDefinition target, Owed owed) {
        BlockEntity previous = level.getBlockEntity(pos);
        Map<BlockPos, List<ItemStack>> contents = new LinkedHashMap<>();
        List<ItemStack> pending = new ArrayList<>();
        if (refundsReplacedBlocks()) {
            // Whatever a position holds goes with it: the cell mounted in a drive, the patterns in a bus.
            contents = rescueContents(swap.moved(), level, pos);
            var refundPositions = positionsOf(swap.moved());
            refundPositions.add(pos);
            pending = takeOut(level, player, refundPositions);
        }
        for (WorldPlannedBlock moved : swap.moved()) {
            level.setBlock(moved.worldPos(), moved.targetState(), Block.UPDATE_ALL);
        }
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
        // Back into the position it came from where the new block will take it, and onto the click's list
        // where it will not - never into the void.
        returnContents(level, contents, owed);
        owed.bagAll(pending);
        if (consumedOnSuccess() && !player.hasInfiniteMaterials()) {
            var held = player.getItemInHand(hand);
            if (held.is(this)) {
                held.shrink(1);
            }
        }
        // Before the seal, so whatever the subclass still owes the player rides in the same bag.
        onSwapped(level, pos, player, previous, owed);
        owed.seal(level, pos);
        say(player, Component.translatable(upgradedKey(),
                tierLabel(target), swap.moved().size() + 1));
    }

    /**
     * The tier a definition climbs to, read off its own host block id
     * ({@code neoecoae:storage_system_l6} to {@code L6}). A block id with no such suffix is reported as
     * its own path rather than guessed at, so a message never invents a tier number.
     */
    private static String tierLabel(MultiBlockDefinition definition) {
        String path = net.minecraft.core.registries.BuiltInRegistries.BLOCK
                .getKey(definition.getOwner().value()).getPath();
        java.util.regex.Matcher found = java.util.regex.Pattern.compile("l(\\d+)\\w*$")
                .matcher(path);
        return found.find() ? "L" + found.group(1) : path;
    }

    /**
     * The higher tier's host block, wearing this machine's own orientation.
     *
     * <p>Returns null when the target tier does not want the machine at this size - the ceiling is a
     * config value read per family, so the two tiers of one family only agree while the config holds
     * still between them - or when the block has no item to pay for it with.</p>
     */
    private @Nullable BlockState anchorTarget(MultiBlockBuildController.Host host, Level level,
                                              BlockPos pos, MultiBlockDefinition target) {
        int length = largestSatisfiedLength(host, level, pos, host.getBuildDefinition(),
                target.getExpandMax()).length();
        if (length < 0 || length != target.getExpandMax()) {
            return null;
        }
        Block anchorBlock = target.getOwner().value();
        if (anchorBlock.asItem() == Items.AIR) {
            return null;
        }
        return carryOver(level.getBlockState(pos), anchorBlock.defaultBlockState());
    }

    /**
     * The blocks the card hands back before it builds the bigger shape: every position it moves, plus -
     * when the click swaps tiers - the host block itself. They are not a cost, so they come back out of
     * the work order's input list; the loop above only ever runs while those blocks still stand.
     */
    private List<ItemStack> reclaimed(List<WorldPlannedBlock> moved, Level level) {
        List<ItemStack> back = new ArrayList<>(moved.size());
        for (WorldPlannedBlock position : moved) {
            back.add(new ItemStack(level.getBlockState(position.worldPos()).getBlock().asItem()));
        }
        return back;
    }

    private List<ItemStack> upgradeReclaimed(List<WorldPlannedBlock> moved, Level level, BlockPos host) {
        List<ItemStack> back = reclaimed(moved, level);
        back.add(new ItemStack(level.getBlockState(host).getBlock().asItem()));
        return back;
    }

    /** {@code bill} with each reclaimed stack subtracted from the entry for the same item. */
    private static List<RequiredItem> netted(List<RequiredItem> bill, List<ItemStack> back) {
        List<RequiredItem> net = new ArrayList<>(bill);
        for (ItemStack given : back) {
            for (int i = 0; i < net.size(); i++) {
                RequiredItem entry = net.get(i);
                if (!ItemStack.isSameItemSameComponents(entry.stack(), given)) {
                    continue;
                }
                long left = entry.count() - given.getCount();
                if (left <= 0) {
                    net.remove(i);
                } else {
                    net.set(i, new RequiredItem(entry.stack(), (int) left));
                }
                break;
            }
        }
        return net;
    }

    /**
     * The work order for this machine: one registered item per definition the card can be pointed at.
     * They have to be registered items and not renamed paper because all an AE2 pattern can hold is
     * {@code GenericStack(AEKey, long)} - an id and an amount - so a name written on a stack would not
     * survive being produced by the very recipe it names.
     */
    private static @Nullable Item growWorkOrder(MultiBlockDefinition definition) {
        if (definition == SimplifyStorageDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_L1_STORAGE.get();
        }
        if (definition == SimplifyCraftingDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_F1_CRAFTING.get();
        }
        if (definition == SimplifyTrinityDefinition.L1) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_TRINITY.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L4.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L6.get();
        }
        if (definition == NEMultiBlocks.STORAGE_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_STORAGE_L9.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L4.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L6.get();
        }
        if (definition == NEMultiBlocks.COMPUTATION_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_COMPUTATION_L9.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L4) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L4.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L6.get();
        }
        if (definition == NEMultiBlocks.CRAFTING_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_GROW_CRAFTING_L9.get();
        }
        return null;
    }

    /** The work order for the tier this click was heading to; every tier the card climbs to has one. */
    private static @Nullable Item upgradeWorkOrder(MultiBlockDefinition target) {
        if (target == NEMultiBlocks.STORAGE_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_STORAGE_L9.get();
        }
        if (target == NEMultiBlocks.COMPUTATION_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_COMPUTATION_L9.get();
        }
        if (target == NEMultiBlocks.CRAFTING_SYSTEM_L9) {
            return ModRegistration.SIMPLIFY_WORK_ORDER_UPGRADE_CRAFTING_L9.get();
        }
        return null;
    }

    /**
     * What a refused click leaves behind is a pair: the machine's work order, and a processing pattern
     * that turns the parts this step still needs into one more of those work orders. The direction is not a style choice - a pattern that ate
     * the ticket instead would turn one blank pattern into 48 drives and cells, which is what he caught
     * in game. The work order is therefore always the output, and the pattern is what tells the network
     * what "paid for" costs.
     *
     * <p>Both costs - the sheet the ticket is written on and the blank pattern the recipe is stored on -
     * are checked before either is taken, and each refusal says so out loud: a silent return here reads
     * to the player as a broken card.</p>
     *
     * <p>Both go into the click's backup drive rather than being handed over loose. The pattern is the reason:
     * an encoded AE2 pattern is nothing but components, so a one-count entity on the ground is a ticket that
     * stops being one five minutes later, and the player has no way to see that clock.</p>
     */
    private void handOverTicket(ServerPlayer player, Owed owed, @Nullable Item workOrder,
                                List<RequiredItem> bill, Component missing) {
        if (workOrder == null) {
            return;
        }
        if (NeoECOPrototypeServerConfig.STRUCTURE_CARD_CONSUMES_ITEMS.get()) {
            ItemStack paper = new ItemStack(Items.PAPER);
            ItemStack blank = new ItemStack(AEItems.BLANK_PATTERN.asItem());
            if (MultiBlockPlacementService.countMatchingItems(player, paper) <= 0) {
                say(player, Component.translatable("card.neoecoprototype.expansion.no_paper", missing));
                return;
            }
            if (MultiBlockPlacementService.countMatchingItems(player, blank) <= 0) {
                say(player, Component.translatable("card.neoecoprototype.expansion.no_blank", missing));
                return;
            }
            if (!consumeOne(player, paper) || !consumeOne(player, blank)) {
                return;
            }
        }
        owed.bag(new ItemStack(workOrder));
        List<GenericStack> inputs = new ArrayList<>(bill.size());
        long parts = 0;
        for (RequiredItem required : bill) {
            inputs.add(new GenericStack(AEItemKey.of(required.stack().getItem()), required.count()));
            parts += required.count();
        }
        owed.bag(PatternDetailsHelper.encodeProcessingPattern(inputs,
                List.of(new GenericStack(AEItemKey.of(workOrder), 1))));
        say(player, Component.translatable("card.neoecoprototype.expansion.pattern", parts));
    }

    /**
     * Removes one of {@code wanted} from the player's own inventory - the nine hotbar slots, the
     * twenty-seven above them and the off hand, which is the scope eco's own {@code hasRequiredItems}
     * checks against.
     */
    private static boolean consumeOne(Player player, ItemStack wanted) {
        Inventory inventory = player.getInventory();
        List<ItemStack> haystack = new ArrayList<>(inventory.items);
        haystack.add(inventory.offhand.get(0));
        for (ItemStack stack : haystack) {
            if (ItemStack.isSameItemSameComponents(stack, wanted)) {
                stack.shrink(1);
                inventory.setChanged();
                return true;
            }
        }
        return false;
    }

    /**
     * The largest shape length the world already satisfies at this host, and the first position that
     * stopped it fitting.
     *
     * <p>Read off the world rather than off {@code selectedBuildLength}: a satisfied shape is one
     * whose plan needs nothing placed and whose only conflicts are positions already holding a block
     * this machine accepts. That is the size of the machine standing here, which is what both "grow it"
     * and "change its tier" have to be measured against.</p>
     */
    private Measured largestSatisfiedLength(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                            @Nullable MultiBlockDefinition definition, int max) {
        if (definition == null) {
            return new Measured(-1, null);
        }
        BlockPos blocked = null;
        for (int length = max; length >= definition.getExpandMin(); length--) {
            Classified satisfied = classify(preview(host, level, pos, definition, length), Map.of(),
                    level, false);
            if (satisfied.foreign() == null && satisfied.moved().isEmpty()
                    && satisfied.plan().getMissingBlocks().isEmpty()) {
                return new Measured(length, null);
            }
            if (blocked == null) {
                blocked = satisfied.foreign();
            }
        }
        return new Measured(definition.getExpandMin() - 1, blocked);
    }

    /** What the world says about this host's size, plus the cell that made the answer "no". */
    private record Measured(int length, @Nullable BlockPos blocked) { }

    /**
     * Moves the positions eco's own formation wrote off the conflict list, splits what is left into
     * "ours to move" and "not ours to touch", and prices what is moved.
     *
     * <p>A conflict is ignored when the standing block is the block the target shape wants there - the
     * two then differ at most by properties the cluster sets on itself, which is not a reason to
     * refuse - or when it is the other kind this family's calculator accepts in that cell, which the
     * shape cannot ask for at all (see {@code SimplifyOffTableMembers}). Otherwise it is queued to move
     * when the standing block is one this machine claims at its current size: the outer shell, when
     * growing ({@code tiered} is false and the block has to hold nothing), or any block the next tier
     * wants in a different shape, when swapping tiers. Anything
     * else stops the card - a drive that somehow ended up in the way must never be broken with cells
     * inside it.</p>
     */
    private Classified classify(MultiBlockPlacementPlan plan, Map<BlockPos, WorldPlannedBlock> owned,
                                Level level, boolean tiered) {
        Map<BlockPos, WorldPlannedBlock> wanted = plannedByPosition(plan);
        List<WorldPlannedBlock> moved = new ArrayList<>();
        List<WorldPlannedBlock> missing = new ArrayList<>(plan.getMissingBlocks());
        List<RequiredItem> bill = new ArrayList<>(plan.getRequiredItems());
        Set<BlockPos> settled = new HashSet<>();
        int reusedAsIs = 0;
        for (BlockPos conflict : plan.getConflictPositions()) {
            BlockState standing = level.getBlockState(conflict);
            WorldPlannedBlock wantBlock = wanted.get(conflict);
            BlockState want = wantBlock == null ? null : wantBlock.targetState();
            if (want == null) {
                return refused(plan, conflict);
            }
            // Checked before anything about ownership: a position holding the very block the target
            // shape wants differs at most by what formation writes, and that is no reason to refuse.
            // The caller that measures the machine's current size passes no ownership map at all, so
            // putting this check after the ownership test would make every formed machine look unbuilt.
            // The second half of the test is the same argument for a cell whose two legal kinds a plan
            // can only name one of; the caller has already heard isFormed() say yes, so nothing here
            // lets a machine that came apart pass as built.
            if (standing.getBlock() == want.getBlock()
                    || SimplifyOffTableMembers.alternatesFor(want.getBlock())
                            .contains(standing.getBlock())) {
                reusedAsIs++;
                settled.add(conflict);
                continue;
            }
            // A cell this machine owns can also be worn by a block no plan is able to name there. It is
            // still ours to move, and the bigger or higher shape wants its own block in that cell, so
            // this only lets the card pick the member up instead of stopping on it.
            WorldPlannedBlock mine = owned.get(conflict);
            if (mine == null || !(mine.targetState().getBlock() == standing.getBlock()
                    || SimplifyOffTableMembers.isOffTableMember(standing.getBlock()))) {
                return refused(plan, conflict);
            }
            // A shell the bigger shape wants elsewhere, or a block whose tier is written into its id.
            // A block with no item to give back for is not ours to move at all.
            if (standing.getBlock().asItem() == Items.AIR
                    || (tiered ? want.getBlock() == mine.targetState().getBlock()
                    : !isShell(level, conflict))) {
                return refused(plan, conflict);
            }
            WorldPlannedBlock replacement = new WorldPlannedBlock(conflict, want,
                    wantBlock.requiredItem().copy());
            moved.add(replacement);
            missing.add(replacement);
            mergeItem(bill, replacement.requiredItem());
            settled.add(conflict);
        }
        if (settled.isEmpty()) {
            return new Classified(plan, moved, bill, null);
        }
        List<BlockPos> stillConflicting = new ArrayList<>(plan.getConflictPositions());
        stillConflicting.removeAll(settled);
        return new Classified(new MultiBlockPlacementPlan(plan.getAllBlocks(), missing, stillConflicting,
                bill, plan.getReusedBlockCount() + reusedAsIs), moved, bill, null);
    }

    private static Classified refused(MultiBlockPlacementPlan plan, BlockPos foreign) {
        return new Classified(plan, List.of(), List.of(), foreign);
    }

    /**
     * Whether a position holds one of the two blocks that can stand in the outer column and carry
     * nothing: eco's casing, and the computation system's cooling controller - {@code
     * createComputationSystem} puts the cooler in that column while the other two systems put only
     * casings there.
     *
     * <p>Both are safe to move. A casing's block entity is what writes the {@code invisible} property
     * and holds no items, and the cooling controller's is just {@code tier} plus {@code mirrored}
     * (checked against the 21.2.1 jar, not inferred).</p>
     */
    private static boolean isShell(Level level, BlockPos pos) {
        BlockEntity blockEntity = level.getBlockEntity(pos);
        return blockEntity == null || blockEntity instanceof ECOMachineCasingBlockEntity
                || blockEntity instanceof ECOComputationCoolingControllerBlockEntity;
    }

    /** The cells one plan moves, as plain positions the refund can be collected by. */
    private static List<BlockPos> positionsOf(List<WorldPlannedBlock> moved) {
        List<BlockPos> positions = new ArrayList<>(moved.size());
        for (WorldPlannedBlock position : moved) {
            positions.add(position.worldPos());
        }
        return positions;
    }

    /**
     * Takes every one of these positions out of the world and returns what stood there, merged per block and
     * split into stacks the item can hold - a machine at its ceiling is eighty-nine cells, and handing those
     * back one at a time is what filled the room with loose blocks.
     *
     * <p>Deliberately not {@code Block#getDrops}: the card is what is in hand while this runs, so a drop
     * table that asks for a tool would silently return nothing and the player would pay for the bigger shape
     * with blocks they never got. Nothing is handed over here; see {@link #giveNow}.</p>
     */
    private static List<ItemStack> takeOut(Level level, @Nullable Player player, List<BlockPos> positions) {
        Map<Item, Integer> merged = new LinkedHashMap<>();
        for (BlockPos position : positions) {
            Block taken = level.getBlockState(position).getBlock();
            Item asItem = taken.asItem();
            level.destroyBlock(position, false, player);
            if (asItem != Items.AIR) {
                merged.merge(asItem, 1, Integer::sum);
            }
        }
        List<ItemStack> stacks = new ArrayList<>(merged.size());
        merged.forEach((item, count) -> {
            ItemStack prototype = new ItemStack(item);
            int left = count;
            while (left > 0) {
                ItemStack stack = new ItemStack(item);
                stack.setCount(Math.min(left, prototype.getMaxStackSize()));
                stacks.add(stack);
                left -= stack.getCount();
            }
        });
        return stacks;
    }

    /** Puts the refund into the player's own bag, and returns whatever still had nowhere to go. */
    private static List<ItemStack> giveNow(ServerPlayer player, List<ItemStack> stacks) {
        if (player.isCreative() || stacks.isEmpty()) {
            return new ArrayList<>();
        }
        List<ItemStack> stuck = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (!player.getInventory().add(stack.copy())) {
                stuck.add(stack.copy());
            }
        }
        return stuck;
    }

    /**
     * What one click owes the player, gathered in one place so it leaves as a single backup drive instead of
     * a floor covered in one-count drops.
     *
     * <p>Bagged rather than handed, on {@code #refundsReplacedBlocks()} paths and for everything a refusal
     * prints: the refund is dozens of stacks, so pushing them into the inventory one at a time both fills it
     * with parts the player then has to sort out and leaves whatever did not fit lying where the block stood,
     * with five minutes on the clock. Right-clicking the bag is the way back out. The encoded processing
     * pattern a refused click prints is the case that really needed this - its whole content is its
     * components, so a loose drop on the ground is a ticket that quietly rots.</p>
     *
     * <p>One path still hands into the inventory instead: {@link #grow}'s refund, because eco's own builder
     * bills the whole bigger shape out of those slots a moment later and would refuse without them. That
     * path is also the one a creative player is billed nothing on, so its parts are let go rather than
     * bagged - the first version skipped the bag for every creative click, which read to the person testing
     * it in creative as the feature not working at all.</p>
     */
    static final class Owed {
        private final List<ItemStack> stacks = new ArrayList<>();
        private final ServerPlayer player;
        /** The rung to put on the bag's name, or 0 for plain - see {@link #tierColour}. */
        private final int nameColour;

        Owed(ServerPlayer player, int nameColour) {
            this.player = player;
            this.nameColour = nameColour;
        }

        /** Whether anything is on the list - which is another way of asking whether a bag will come out. */
        boolean isEmpty() {
            return stacks.isEmpty();
        }

        /** Put one thing on the list - in creative too, because the bag is what the player reads. */
        void bag(ItemStack stack) {
            if (!stack.isEmpty()) {
                stacks.add(stack.copy());
            }
        }

        void bagAll(List<ItemStack> refund) {
            refund.forEach(this::bag);
        }

        /**
         * Seal what is on the list into one bag, hand it over, and drop it on the host's cell - unable to
         * despawn - if even the bag will not fit. A subclass that still owes the player something asks
         * {@link #isEmpty} before this runs, because the bag is itself the empty drive its tile was drawn
         * from and paying a shell as well would hand the same disk back twice.
         */
        void seal(ServerLevel level, BlockPos anchor) {
            if (stacks.isEmpty()) {
                return;
            }
            List<ItemStack> merged = mergedForBag(stacks);
            stacks.clear();
            var bag = BackupDriveItem.withContents(level.registryAccess(), merged);
            if (nameColour != 0) {
                BackupDriveItem.wearTier(bag, nameColour);
            }
            var handed = player.getInventory().add(bag.copy());
            // One line per sealed bag: what went in, which rung it claims to wear, and whether the player
            // actually received it - so a client report can say which half failed when the hand looks wrong.
            NeoECOPrototype.LOGGER.info("backup drive: {} entries sealed, colour = {}, host cell {}, "
                            + "handed to the player = {}",
                    merged.size(), nameColour == 0 ? "plain" : Integer.toHexString(nameColour), anchor,
                    handed);
            if (handed) {
                say(player, Component.translatable("card.neoecoprototype.expansion.bagged", merged.size()));
                return;
            }
            var drop = new ItemEntity(level, anchor.getX() + 0.5D, anchor.getY() + 0.5D, anchor.getZ() + 0.5D,
                    bag);
            drop.setNoPickUpDelay();
            // Public field on ItemEntity (checked against 21.1.251), not a mixin and not a tick handler.
            drop.lifespan = Integer.MAX_VALUE;
            level.addFreshEntity(drop);
            say(player, Component.translatable("card.neoecoprototype.expansion.bagged_dropped",
                    merged.size()));
        }
    }

    /** What the list holds, merged per item and split again at each item's own stack limit. */
    private static List<ItemStack> mergedForBag(List<ItemStack> stacks) {
        List<ItemStack> merged = new ArrayList<>();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty()) {
                continue;
            }
            int left = stack.getCount();
            for (ItemStack entry : merged) {
                if (left == 0 || !ItemStack.isSameItemSameComponents(entry, stack)) {
                    continue;
                }
                int moved = Math.min(entry.getMaxStackSize() - entry.getCount(), left);
                if (moved > 0) {
                    entry.setCount(entry.getCount() + moved);
                    left -= moved;
                }
            }
            while (left > 0) {
                int moved = Math.min(left, stack.getMaxStackSize());
                ItemStack rest = stack.copy();
                rest.setCount(moved);
                merged.add(rest);
                left -= moved;
            }
        }
        return merged;
    }

    /** Into the inventory first, on the floor second - a reclaimed block must never just vanish. */
    protected static void handTo(Level level, BlockPos where, ItemStack stack, ServerPlayer player) {
        if (!player.getInventory().add(stack.copy())) {
            level.addFreshEntity(new ItemEntity(level, where.getX() + 0.5D, where.getY() + 0.5D,
                    where.getZ() + 0.5D, stack.copy()));
        }
    }

    /**
     * The items that belong to a position rather than to its block: one cell mounted in a drive of either
     * kind (our drives and eco's both implement the same host interface, so one check covers the pair), and
     * every pattern in a bus - whose handler is public and is the same class on both sides of the ladder, so
     * the patterns keep their pages instead of dying with the block.
     *
     * <p>The machine interface is deliberately not carried, and this is a decision rather than an oversight:
     * eco opens its slots for reading only ({@code getPatternInterfaceStack(int)}; the setters are private and
     * the writable one wants an RPC sender), so a swap resets one interface - its stored settings and
     * anything held in its 63 planning filters and 54 pattern slots. That is the price accepted for keeping
     * the other 88 cells' contents, and the disk's success line says so to the player in one clause
     * ({@code card.neoecoprototype.migration.upgraded}). Do not "fix" this by quietly dropping the slots or
     * by refusing every swap whose interface is loaded - a fixture cannot even test it, because there is no
     * door to put something in.</p>
     */
    private static List<ItemStack> carriedContent(Level level, BlockPos where) {
        List<ItemStack> taken = new ArrayList<>();
        if (!(level.getBlockEntity(where) instanceof BlockEntity blockEntity)) {
            return taken;
        }
        if (blockEntity instanceof ICellHost host) {
            // An empty drive answers null rather than an empty stack, and that used to be enough to
            // bring a whole tooltip down, so both shapes are treated as "nothing mounted".
            ItemStack cell = host.getCellStack();
            if (cell == null || cell.isEmpty()) {
                return taken;
            }
            // Clearing is null-only here: our drive's own setCellStack javadoc says an empty stack is
            // refused as "invalid input", so passing EMPTY leaves the cell mounted and turns a copy into
            // a duplicate - which is exactly what this line used to do on a real machine.
            host.setCellStack(null);
            ItemStack stillThere = host.getCellStack();
            if (stillThere == null || stillThere.isEmpty()) {
                taken.add(cell.copy());
            }
            // If it is still mounted (eco gates extraction while a matrix is locked by infinite mode),
            // leave it alone: the block's own removal path deals with a cell it can hold onto, and taking a
            // copy anyway is how one matrix becomes two.
            return taken;
        }
        if (blockEntity instanceof ECOCraftingPatternBusBlockEntity bus) {
            // itemHandler is a paged view (its own class maps through toActualSlot), so reading it sees
            // one screenful of a 22-bus machine and quietly leaves the other pages to die with the block.
            var slots = bus.getPatternSlotInventory();
            for (int slot = 0; slot < slots.size(); slot++) {
                ItemStack stored = slots.getStackInSlot(slot);
                if (stored.isEmpty()) {
                    continue;
                }
                taken.add(stored.copy());
                slots.setItemDirect(slot, ItemStack.EMPTY);
            }
            return taken;
        }
        return taken;
    }

    /** Puts one rescued stack back where it lived, or onto the click's list if that block will not take it. */
    private static void returnContent(Level level, BlockPos where, ItemStack stack, Owed owed) {
        if (level.getBlockEntity(where) instanceof ICellHost host) {
            var free = host.getCellStack();
            if ((free == null || free.isEmpty()) && host.isItemValid(stack)) {
                host.setCellStack(stack);
                return;
            }
        }
        if (level.getBlockEntity(where) instanceof ECOCraftingPatternBusBlockEntity bus) {
            // The slot numbers here are the backing inventory's, because that is what was read out - and
            // they are put back through the bus's own setter so it re-reads the pattern list instead of
            // holding a stack the grid never heard of.
            var slots = bus.getPatternSlotInventory();
            for (int slot = 0; slot < slots.size(); slot++) {
                if (slots.getStackInSlot(slot).isEmpty()) {
                    bus.setPatternDirect(slot, stack);
                    return;
                }
            }
        }
        owed.bag(stack);
    }

    private MultiBlockPlacementPlan preview(MultiBlockBuildController.Host host, Level level, BlockPos pos,
                                            MultiBlockDefinition definition, int length) {
        return MultiBlockPlacementService.preview(level, pos, level.getBlockState(pos), definition,
                length, builtMirrored(level.getBlockState(pos), host.isMirrorBuild()));
    }

    /**
     * The hand this machine was actually built in, which is not the question
     * {@code Host.isMirrorBuild()} answers.
     *
     * <p>eco writes that flag from the panel's mirror toggle and nowhere else - no save, no load, no
     * formation - so a machine built mirrored and later reloaded reports "not mirrored" while standing
     * mirrored. A plan generated on that answer puts every cell on the wrong side, and the card then
     * reads the machine's own casing as an intruder. Formation is the one that knows: it stores the
     * answer in the host block's state, which is what survives a reload. Read by property name because
     * each family declares its own {@code mirrored} (eco's computation and crafting hosts, our storage
     * controller), and a block without one falls back to the panel's choice.</p>
     */
    private static boolean builtMirrored(BlockState hostState, boolean panelChoice) {
        var property = hostState.getBlock().getStateDefinition().getProperty("mirrored");
        return property instanceof net.minecraft.world.level.block.state.properties.BooleanProperty mirrored
                ? hostState.getValue(mirrored) : panelChoice;
    }

    private static Map<BlockPos, WorldPlannedBlock> plannedByPosition(MultiBlockPlacementPlan plan) {
        Map<BlockPos, WorldPlannedBlock> planned = new LinkedHashMap<>();
        for (WorldPlannedBlock block : plan.getAllBlocks()) {
            planned.put(block.worldPos(), block);
        }
        return planned;
    }

    /** eco's own merge is private, and an unmerged bill would charge the same item twice. */
    private static void mergeItem(List<RequiredItem> bill, ItemStack stack) {
        for (int i = 0; i < bill.size(); i++) {
            RequiredItem entry = bill.get(i);
            if (ItemStack.isSameItemSameComponents(entry.stack(), stack)) {
                bill.set(i, entry.grow(stack.getCount()));
                return;
            }
        }
        bill.add(new RequiredItem(stack.copy(), stack.getCount()));
    }

    private void reportBlocked(ServerPlayer player, BlockPos where) {
        say(player, Component.translatable("card.neoecoprototype.expansion.blocked", where.getX(),
                where.getY(), where.getZ()));
    }

    /**
     * The bill minus what the planner counted as reused. Only the entries still unaffordable would be
     * worth showing, and {@code hasRequiredItems} does not say which those are, so this reports the
     * full bill when the player is short - the honest reading of "you cannot pay for this" without a
     * screen to itemise it against their inventory.
     */
    private Component shortfall(List<RequiredItem> required) {
        StringBuilder text = new StringBuilder();
        int shown = Math.min(required.size(), MAX_REPORTED_ITEMS);
        for (int i = 0; i < shown; i++) {
            RequiredItem item = required.get(i);
            if (i > 0) {
                text.append("，");
            }
            text.append(item.stack().getHoverName().getString()).append(" ×").append(item.count());
        }
        if (required.size() > shown) {
            text.append(" …");
        }
        return Component.literal(text.toString());
    }

    private void rebuildHost(ServerLevel level, BlockPos pos) {
        // The anchor cell was replaced, so eco's host block entity went with it and the machine has to
        // be re-formed through whichever block entity stands there now.
        if (level.getBlockEntity(pos) instanceof MultiBlockBuildController.Host fresh) {
            fresh.rebuildAfterBuild();
        }
    }

    /**
     * Places the new host block when a build stopped part-way through. The old one is already back in
     * the player's inventory, so the machine is left with a higher tier's host on a lower tier's
     * frame - which will not form - rather than with a hole in it.
     */
    private void finishAnchor(ServerLevel level, BlockPos pos, BlockState anchor, ServerPlayer player) {
        level.setBlock(pos, anchor, Block.UPDATE_ALL);
        rebuildHost(level, pos);
    }

    /** The new block wearing the old block's orientation, except for what formation writes itself. */
    private static BlockState carryOver(BlockState from, BlockState to) {
        for (Property<?> property : from.getProperties()) {
            if (!FORMED_PROPERTY.equals(property.getName())) {
                to = copyProperty(to, from, property);
            }
        }
        return to;
    }

    private static <T extends Comparable<T>> BlockState copyProperty(BlockState to, BlockState from,
                                                                     Property<T> property) {
        if (!to.hasProperty(property)) {
            return to;
        }
        T value = from.getValue(property);
        return to.setValue(property, value);
    }

    private static void say(ServerPlayer player, String key) {
        player.displayClientMessage(Component.translatable(key), true);
    }

    private static void say(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}

package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoae.all.NEMultiBlocks;
import cn.dancingsnow.neoecoae.api.ECOTier;
import cn.dancingsnow.neoecoae.multiblock.definition.MultiBlockDefinition;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyCraftingSystemBlockEntity;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyComputationDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyCraftingDefinition;
import cn.dancingsnow.neoecoprototype.multiblock.definition.SimplifyStorageDefinition;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * A disk that moves one finished machine up the tier ladder in place.
 *
 * <p>Two choices make these different from the card that grows a machine along its edge, and both were
 * called by the person who asked for the feature: the disk asks nothing of the inventory, and it hands back
 * every block it replaces. So the disk's own recipe is the whole price, and the player's old parts end up
 * in their pocket rather than inside the new machine's walls where they would silently vanish.</p>
 *
 * <p>What does move with the machine is what is mounted in it - the storage matrix in a drive, the patterns
 * in a bus - see {@link #carriedContent}. The one thing that does not is the interface, whose slots eco only
 * opens for reading; that cost is accepted and it is said to the player in the success line.</p>
 */
public final class SimplifyMigrationDriveItem extends SimplifyStructureExpansionCardItem {

    /**
     * The rung one disk covers. Each rung writes its own comparison rather than reading a shared table,
     * because eco's definitions are singletons whose ceilings come off {@code NEConfig} when
     * {@code NEMultiBlocks} is initialised - a static map built here would trigger that during item
     * registration, before the config has been read.
     */
    public enum Rung {
        /** Our L1 machines onto eco's L4, the rung no card of eco's reaches. */
        ONE_TO_FOUR {
            @Override
            protected @Nullable MultiBlockDefinition target(MultiBlockDefinition current) {
                if (current == SimplifyStorageDefinition.L1) {
                    return NEMultiBlocks.getStorageSystemDefinition(ECOTier.L4);
                }
                if (current == SimplifyComputationDefinition.L1) {
                    return NEMultiBlocks.getComputationSystemDefinition(ECOTier.L4);
                }
                if (current == SimplifyCraftingDefinition.L1) {
                    return NEMultiBlocks.getCraftingSystemDefinition(ECOTier.L4);
                }
                return null;
            }
        },
        /** eco's L4 to its L6, all three families. */
        FOUR_TO_SIX {
            @Override
            protected @Nullable MultiBlockDefinition target(MultiBlockDefinition current) {
                if (current == NEMultiBlocks.STORAGE_SYSTEM_L4) {
                    return NEMultiBlocks.getStorageSystemDefinition(ECOTier.L6);
                }
                if (current == NEMultiBlocks.COMPUTATION_SYSTEM_L4) {
                    return NEMultiBlocks.getComputationSystemDefinition(ECOTier.L6);
                }
                if (current == NEMultiBlocks.CRAFTING_SYSTEM_L4) {
                    return NEMultiBlocks.getCraftingSystemDefinition(ECOTier.L6);
                }
                return null;
            }
        },
        /** eco's L6 to its L9, all three families. */
        SIX_TO_NINE {
            @Override
            protected @Nullable MultiBlockDefinition target(MultiBlockDefinition current) {
                if (current == NEMultiBlocks.STORAGE_SYSTEM_L6) {
                    return NEMultiBlocks.getStorageSystemDefinition(ECOTier.L9);
                }
                if (current == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
                    return NEMultiBlocks.getComputationSystemDefinition(ECOTier.L9);
                }
                if (current == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
                    return NEMultiBlocks.getCraftingSystemDefinition(ECOTier.L9);
                }
                return null;
            }
        },
        /** The creative disk: whatever rung stands there, it lands on L9 - and L9 is already L9. */
        STRAIGHT_TO_NINE {
            @Override
            protected @Nullable MultiBlockDefinition target(MultiBlockDefinition current) {
                if (current == null) {
                    return null;
                }
                if (current == SimplifyStorageDefinition.L1 || current == NEMultiBlocks.STORAGE_SYSTEM_L4
                        || current == NEMultiBlocks.STORAGE_SYSTEM_L6) {
                    return NEMultiBlocks.getStorageSystemDefinition(ECOTier.L9);
                }
                if (current == SimplifyComputationDefinition.L1
                        || current == NEMultiBlocks.COMPUTATION_SYSTEM_L4
                        || current == NEMultiBlocks.COMPUTATION_SYSTEM_L6) {
                    return NEMultiBlocks.getComputationSystemDefinition(ECOTier.L9);
                }
                if (current == SimplifyCraftingDefinition.L1
                        || current == NEMultiBlocks.CRAFTING_SYSTEM_L4
                        || current == NEMultiBlocks.CRAFTING_SYSTEM_L6) {
                    return NEMultiBlocks.getCraftingSystemDefinition(ECOTier.L9);
                }
                return null;
            }
        };

        /** The definition this rung climbs to from the one standing there, or null for "not my rung". */
        protected abstract @Nullable MultiBlockDefinition target(MultiBlockDefinition current);
    }

    private final Rung rung;

    public SimplifyMigrationDriveItem(Properties properties, Rung rung) {
        super(properties);
        this.rung = rung;
    }

    public Rung rung() {
        return rung;
    }

    /** Read one rung's answer without a machine standing there - the table guard walks eco's list. */
    public static @Nullable MultiBlockDefinition rungTarget(Rung rung, MultiBlockDefinition current) {
        return rung.target(current);
    }

    /**
     * Which refusal a disk would show for the machine standing there. The table guard needs this because
     * "already on the top" is a claim about the tier, and printing it on a machine that could still climb
     * would be a lie the player acts on.
     */
    public static String refusalKey(MultiBlockDefinition current) {
        return isTopTier(current) ? "card.neoecoprototype.migration.at_top"
                : "card.neoecoprototype.migration.wrong_machine";
    }

    /** A disk moves a machine up the ladder and never makes it longer. */
    @Override
    protected boolean canGrow() {
        return false;
    }

    @Override
    protected boolean consumedOnSuccess() {
        return true;
    }

    /** The disk is the price: nothing is taken from the inventory for the new tier's shape. */
    @Override
    protected boolean chargesParts() {
        return false;
    }

    @Override
    protected boolean refundsReplacedBlocks() {
        return true;
    }

    @Override
    protected String upgradedKey() {
        return "card.neoecoprototype.migration.upgraded";
    }

    @Override
    protected @Nullable MultiBlockDefinition targetOf(MultiBlockDefinition current) {
        return rung.target(current);
    }

    @Override
    protected Component refusal(MultiBlockDefinition current, int length) {
        return Component.translatable(refusalKey(current));
    }

    private static boolean isTopTier(MultiBlockDefinition current) {
        return current == NEMultiBlocks.getStorageSystemDefinition(ECOTier.L9)
                || current == NEMultiBlocks.getComputationSystemDefinition(ECOTier.L9)
                || current == NEMultiBlocks.getCraftingSystemDefinition(ECOTier.L9);
    }

    /**
     * The disk comes back empty, and a machine that had grown a mind gives its player the doll - because
     * eco's host block has no mind to carry over, so that is the one thing the swap really does take away.
     * Only the one-in-sixteen hosts that grew one can pay it out, or every L1 machine in a base would be a
     * doll waiting to happen.
     *
     * <p>Both go the same way the refund does - into the click's reward bag - and one of them must not be
     * paid twice: the bag's tile is this disk's own shell darkened, and pouring a bag turns it back into an
     * empty drive, so when a bag came out of the click the bag *is* the shell. The click that owes nothing
     * else is handed the drive by {@link Owed#seal} itself, which is also what every paid grow relies on.</p>
     */
    @Override
    protected void onSwapped(ServerLevel level, BlockPos pos, ServerPlayer player,
                              @Nullable BlockEntity previous, Owed owed) {
        if (player.hasInfiniteMaterials()) {
            return;
        }
        if (previous instanceof SimplifyCraftingSystemBlockEntity host && host.hasMind()) {
            owed.bag(new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get()));
        }
    }
}

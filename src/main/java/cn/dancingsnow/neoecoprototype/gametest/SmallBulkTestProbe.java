package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.config.Actionable;
import appeng.api.networking.security.IActionSource;
import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.api.stacks.KeyCounter;
import cn.dancingsnow.neoecoae.api.storage.ECOStorageCells;
import cn.dancingsnow.neoecoae.items.ECOStorageCellItem;
import gripe._90.megacells.misc.CompressionChain;
import gripe._90.megacells.misc.CompressionService;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/**
 * Loads megacells' compression chains and hands the test a few item keys to compare.
 *
 * <p>Its own class on purpose: a game test that names {@code CompressionService} in its body loads it while
 * the test class is being built, which takes the whole game test server down when megacells is not installed.
 * The caller only reaches this after checking the mod is present, so the reference resolves late.
 */
final class SmallBulkTestProbe {

    /** How many chain-carrying items to keep before pairing them up - a pair needs no more than a handful. */
    private static final int SAMPLE_LIMIT = 64;

    /** Two keys on one chain, two on different chains, and two that are not compressible at all. */
    record ChainSample(AEItemKey sameLeft, AEItemKey sameRight, AEItemKey otherLeft, AEItemKey otherRight,
                       AEItemKey plainLeft, AEItemKey plainRight) {
        boolean isComplete() {
            return sameLeft != null && sameRight != null && otherLeft != null && otherRight != null
                    && plainLeft != null && plainRight != null;
        }
    }

    private SmallBulkTestProbe() {
    }

    static ChainSample findSamples() {
        List<AEItemKey> keys = new ArrayList<>();
        List<CompressionChain> chains = new ArrayList<>();
        AEItemKey plainLeft = null;
        AEItemKey plainRight = null;
        for (Item item : BuiltInRegistries.ITEM) {
            AEItemKey key = AEItemKey.of(item);
            if (key == null) {
                continue;
            }
            CompressionChain chain = CompressionService.getChain(key);
            if (chain.isEmpty()) {
                if (plainLeft == null) {
                    plainLeft = key;
                } else if (plainRight == null && !plainLeft.equals(key)) {
                    plainRight = key;
                }
            } else if (keys.size() < SAMPLE_LIMIT) {
                keys.add(key);
                chains.add(chain);
            }
        }
        AEItemKey sameLeft = null;
        AEItemKey sameRight = null;
        CompressionChain shared = null;
        for (int left = 0; left < keys.size() && sameRight == null; left++) {
            for (int right = left + 1; right < keys.size(); right++) {
                if (!keys.get(left).equals(keys.get(right)) && chains.get(left).equals(chains.get(right))) {
                    sameLeft = keys.get(left);
                    sameRight = keys.get(right);
                    shared = chains.get(left);
                    break;
                }
            }
        }
        AEItemKey otherLeft = null;
        AEItemKey otherRight = null;
        if (shared != null) {
            for (int index = 0; index < keys.size(); index++) {
                if (!shared.equals(chains.get(index))) {
                    otherLeft = keys.get(index);
                    otherRight = sameLeft;
                    break;
                }
            }
        }
        return new ChainSample(sameLeft, sameRight, otherLeft, otherRight, plainLeft, plainRight);
    }

    /** Two keys that sit on one compression chain, in the order they should be marked and offered. */
    record ChainPair(AEItemKey marked, AEItemKey offered) {
    }

    /**
     * One mark/offer pair per compression chain the environment knows - a single pair cannot tell
     * "this chain is special" from "the backend never folds".
     */
    static List<ChainPair> findChainPairs(int limit) {
        java.util.Map<CompressionChain, AEItemKey> firstMember = new java.util.LinkedHashMap<>();
        List<ChainPair> pairs = new ArrayList<>();
        for (Item item : BuiltInRegistries.ITEM) {
            AEItemKey key = AEItemKey.of(item);
            if (key == null) {
                continue;
            }
            CompressionChain chain = CompressionService.getChain(key);
            if (chain.isEmpty()) {
                continue;
            }
            AEItemKey other = firstMember.putIfAbsent(chain, key);
            if (other != null && !other.equals(key)) {
                pairs.add(new ChainPair(other, key));
                firstMember.remove(chain);
                if (pairs.size() >= limit) {
                    break;
                }
            }
        }
        return pairs;
    }

    /** Where one insert landed: under the marked key, under the key actually offered, and how much moved. */
    record FoldOutcome(long storedUnderMark, long storedUnderVariant, long transferred) {
    }

    /**
     * Mark one key in a fresh cell, offer another, then read the cell back.
     *
     * @param marked  the key written into the cell's first filter slot
     * @param offered the key handed to the cell
     * @param card    whether to install MEGA Cells' compression card first - the backend reads that one
     *                item as the switch for chain behaviour, not eco's own MEGA upgrade card
     */
    static FoldOutcome fold(Item cellItem, AEItemKey marked, AEItemKey offered, long amount, boolean card) {
        ItemStack stack = new ItemStack(cellItem);
        var item = (ECOStorageCellItem) cellItem;
        item.getConfigInventory(stack).setStack(0, new GenericStack(marked, 0L));
        if (card) {
            item.getUpgrades(stack).addItems(compressionCard().stack(1));
        }
        var cell = ECOStorageCells.getCellInventory(stack, null);
        if (cell == null) {
            return new FoldOutcome(0L, 0L, 0L);
        }
        long transferred = cell.insert(offered, amount, Actionable.MODULATE, IActionSource.empty());
        KeyCounter stored = new KeyCounter();
        cell.getAvailableStacks(stored);
        return new FoldOutcome(stored.get(marked), stored.get(offered), transferred);
    }

    private static appeng.core.definitions.ItemDefinition<?> compressionCard() {
        return gripe._90.megacells.definition.MEGAItems.COMPRESSION_CARD;
    }
}

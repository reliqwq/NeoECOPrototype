package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.stacks.GenericStack;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicKey;
import com.fish_dan_.data_energistics.api.recipe.reassembler.DataRipperReassemblerRecipe;
import com.fish_dan_.data_energistics.common.blockentity.storage.DigitalStorageDepotBlockEntity;
import com.fish_dan_.data_energistics.common.data.DEAE2Keys;
import com.fish_dan_.data_energistics.common.data.DEBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeManager;

import java.util.ArrayList;
import java.util.List;

/**
 * The only place in the game test source that touches Data Energistics classes.
 *
 * <p>Same reason {@link AppFluxTestProbe} exists: the game test bootstrap resolves every
 * {@code @GameTest} method on its own class, so a reference to a class from a mod that is not
 * installed stops the server from starting at all. Every caller has already checked that
 * {@code data_energistics} is loaded, and everything handed back here is plain data.</p>
 */
final class DataEnergisticsTestProbe {
    private static final ResourceLocation DATA_FLOW =
            ResourceLocation.fromNamespaceAndPath("data_energistics", "data_flow");

    /** The key types Data Energistics ended up registering, in registration order. */
    static List<String> keyTypeIds() {
        List<String> ids = new ArrayList<>();
        for (var type : DEAE2Keys.types()) {
            ids.add(type.getId().toString());
        }
        return ids;
    }

    /** How many reassembler recipes mint music data. This has to stay zero. */
    static int recipesEmittingMusic(RecipeManager recipes) {
        int found = 0;
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof DataRipperReassemblerRecipe recipe)) {
                continue;
            }
            GenericStack output = recipe.getKeyOutput();
            if (output != null && output.what() instanceof MusicKey) {
                found++;
            }
        }
        return found;
    }

    /**
     * Every recipe that consumes music data, as {@code {amountIn, amountOut, processTicks}};
     * {@code amountOut} is -1 when the output is not Data Energistics' data flow.
     */
    static List<long[]> musicConsumingRecipes(RecipeManager recipes) {
        List<long[]> found = new ArrayList<>();
        for (RecipeHolder<?> holder : recipes.getRecipes()) {
            if (!(holder.value() instanceof DataRipperReassemblerRecipe recipe)) {
                continue;
            }
            GenericStack in = recipe.getKeyInput();
            if (in == null || !(in.what() instanceof MusicKey)) {
                continue;
            }
            GenericStack out = recipe.getKeyOutput();
            long outAmount = out != null && out.what().getId().equals(DATA_FLOW) ? out.amount() : -1L;
            found.add(new long[]{in.amount(), outAmount, recipe.getProcessTicks()});
        }
        return found;
    }

    /**
     * Puts Data Energistics' digital storage depot at {@code pos} and answers whether it became a block
     * entity. This is the only store in the mod that takes a music key: the digital storage cell and the
     * infinite cell both compare against their own fixed key sets, and the supply interface only accepts
     * keys an adapter explicitly registered.
     */
    static boolean placeDigitalStorageDepot(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, DEBlocks.DIGITAL_STORAGE_DEPOT.get());
        return helper.getLevel().getBlockEntity(helper.absolutePos(pos))
                instanceof DigitalStorageDepotBlockEntity;
    }

    /** Ticks of music the depot at {@code pos} holds, summed over its key slots; -1 if there is no depot. */
    static long storedMusic(GameTestHelper helper, BlockPos pos) {
        if (!(helper.getLevel().getBlockEntity(helper.absolutePos(pos))
                instanceof DigitalStorageDepotBlockEntity depot)) {
            return -1L;
        }
        long total = 0L;
        for (int slot = 0; slot < DigitalStorageDepotBlockEntity.KEY_SLOTS; slot++) {
            GenericStack stored = depot.getKeyStack(slot);
            if (stored != null && stored.what() instanceof MusicKey) {
                total += stored.amount();
            }
        }
        return total;
    }

    private DataEnergisticsTestProbe() {
    }
}

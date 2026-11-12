package cn.dancingsnow.neoecoprototype.items;

import appeng.api.stacks.AEKey;
import appeng.api.stacks.AEKeyType;
import appeng.util.ConfigInventory;
import cn.dancingsnow.neoecoae.api.IECOTier;
import cn.dancingsnow.neoecoae.api.storage.ECOCellType;
import cn.dancingsnow.neoecoae.items.ECOInfiniteResourceCellItem;
import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 * A script-registered twin of eco's {@code eco_infinite_item_cell}: same class family, same engine
 * ({@code ECOInfiniteResourceCell}), only the locked key list comes from the script instead of
 * {@link ECOInfiniteResourceCellItem#lockedKeys()}.
 *
 * <p>The keys resolve lazily. A KubeJS script runs before the item registry is complete, so another
 * mod's item may not exist yet at registration time; eco's constructor copies the list immediately,
 * which is why the lazy part lives in the four methods that read it rather than in a supplier.
 */
public final class EcoSupplyCellItem extends ECOInfiniteResourceCellItem {

    private final List<Supplier<AEKey>> records;
    private final String ownerId;
    @Nullable
    private List<AEKey> resolved;
    @Nullable
    private Set<AEKeyType> resolvedTypes;
    private boolean failureLogged;

    public EcoSupplyCellItem(Item.Properties properties, IECOTier tier, Supplier<ECOCellType> cellType,
                             List<Supplier<AEKey>> records, String tooltipGroup, String ownerId) {
        super(properties, tier, cellType, List.of(), tooltipGroup);
        this.records = List.copyOf(records);
        this.ownerId = ownerId;
    }

    private List<AEKey> resolve() {
        if (resolved == null) {
            List<AEKey> keys = new ArrayList<>(this.records.size());
            for (Supplier<AEKey> record : this.records) {
                AEKey key;
                try {
                    key = record.get();
                } catch (RuntimeException failure) {
                    // The binder knows which id it names; keep its sentence and log it once for the pack author.
                    logResolutionFailure(failure);
                    throw failure;
                }
                if (key == null) {
                    IllegalStateException failure = new IllegalStateException(
                            ("Script-made supply cell '%s' binds a key that is not registered;"
                                    + " check the ids passed to supplies(...)/suppliesFluid(...)")
                                    .formatted(this.ownerId));
                    logResolutionFailure(failure);
                    throw failure;
                }
                keys.add(key);
            }
            this.resolved = List.copyOf(keys);
            Set<AEKeyType> types = new LinkedHashSet<>();
            for (AEKey key : keys) {
                types.add(key.getType());
            }
            this.resolvedTypes = Set.copyOf(types);
        }
        return this.resolved;
    }

    /**
     * One line per broken cell, not one per hover: the tooltip asks on every frame, and the stack is
     * only useful the first time. The message names the script item; the binder that threw names the id.
     */
    private void logResolutionFailure(Throwable failure) {
        if (!this.failureLogged) {
            this.failureLogged = true;
            NeoECOPrototype.LOGGER.error("Script-made supply cell '" + this.ownerId
                    + "' cannot resolve its bound ids", failure);
        }
    }

    private Set<AEKeyType> types() {
        resolve();
        return resolvedTypes;
    }

    @Override
    public List<AEKey> suppliedKeys() {
        return resolve();
    }

    @Override
    public boolean suppliesKey(@Nullable AEKey what) {
        return what != null && resolve().contains(what);
    }

    @Override
    public Set<AEKeyType> getKeyTypes() {
        return types();
    }

    @Override
    public boolean isBlackListed(ItemStack cellStack, AEKey what) {
        return !suppliesKey(what);
    }

    @Override
    public ConfigInventory getConfigInventory(ItemStack is) {
        return ConfigInventory.configTypes(resolve().size()).slotFilter(this::suppliesKey).build();
    }

    /**
     * eco's own tooltip group names water, cobblestone and lava in its first and third lines, so
     * inheriting it would let a script bind anything and still read those three. The lines are built
     * from the keys this cell actually holds instead.
     */
    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> lines, TooltipFlag tooltipFlag) {
        List<AEKey> keys;
        try {
            keys = resolve();
        } catch (IllegalStateException unregistered) {
            // Hovering a misconfigured cell must not throw: the readable id is already in the log.
            lines.add(Component.translatable("tooltip.neoecoprototype.eco_supply_cell.broken")
                    .withStyle(ChatFormatting.RED));
            return;
        }
        lines.add(Component.translatable("tooltip.neoecoprototype.eco_supply_cell.contents", contents(keys))
                .withStyle(ChatFormatting.AQUA));
        lines.add(Component.translatable("tooltip.neoecoae.infinite_resource.unbounded")
                .withStyle(ChatFormatting.GRAY));
        lines.add(Component.translatable("tooltip.neoecoprototype.eco_supply_cell.sink")
                .withStyle(ChatFormatting.DARK_GRAY, ChatFormatting.ITALIC));
    }

    private static Component contents(List<AEKey> keys) {
        MutableComponent out = Component.empty();
        for (int i = 0; i < keys.size(); i++) {
            if (i > 0) {
                out.append(Component.literal(" / "));
            }
            out.append(keys.get(i).getDisplayName());
        }
        return out;
    }
}

package cn.dancingsnow.neoecoprototype.integration.curios;

import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import net.minecraft.core.Holder;
import net.minecraft.core.NonNullList;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.item.ItemStack;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import java.util.List;
import java.util.function.Supplier;

/**
 * Optional Curios integration: the dolls can be worn in the head accessory slot as well as on the head.
 *
 * <p>Curios' own Chinese name for its {@code head} slot is 「首饰」, so that is what a player means by the
 * accessory slot here. Being a curio is two separate registrations: the item has to answer the curio
 * capability ({@link CuriosApi#registerCurio}), and it has to be in the {@code curios:head} item tag, which
 * is what the slot's {@code curios:tag} validator reads - the capability alone only says "this item is
 * wearable somewhere".
 *
 * <p>Worn as a curio a doll grants no armour. That is deliberate: Curios reads attribute modifiers from
 * {@link ICurioItem#getAttributeModifiers}, whose default is an empty multimap, and it never looks at the
 * item's vanilla {@code attribute_modifiers} component. The one thing a doll does hand over here is the
 * mana it is authored to grant ({@link FumoItem#manaGrants}), which is empty unless both the doll and Ars
 * Nouveau are present. Everything else - night vision, the per-doll effects, the charging, the creeper
 * scare - comes from the same item and is granted by
 * {@link cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects}, which treats the curio as a
 * fallback when the helmet slot is empty.
 *
 * <p>This class is only touched inside {@code ModList.isLoaded("curios")}: its own class load pulls in the
 * Curios API classes, which do not exist otherwise.
 */
public final class CuriosDollSupport {
    /** Curios' identifier for the head slot, the one it calls 「首饰」. */
    public static final String HEAD_SLOT = "head";

    private static final List<Supplier<FumoItem>> DOLLS = List.of(
            ModRegistration.FUMO_DOLL_RELIQWQ_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_CPU_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM,
            ModRegistration.FUMO_DOLL_KOOOKI_ITEM,
            ModRegistration.FUMO_DOLL_TEDXENON_ITEM);

    private CuriosDollSupport() {
    }

    /** Registers all six dolls; called once during common setup, only with Curios present. */
    public static void register() {
        for (Supplier<FumoItem> doll : DOLLS) {
            CuriosApi.registerCurio(doll.get(), DollCurio.INSTANCE);
        }
    }

    /** The doll in the player's head curio slot, or an empty stack. Only the real slot is read, not cosmetic. */
    public static ItemStack wornDoll(ServerPlayer player) {
        return CuriosApi.getCuriosInventory(player)
                .flatMap(handler -> handler.getStacksHandler(HEAD_SLOT))
                .map(CuriosDollSupport::firstDoll)
                .orElse(ItemStack.EMPTY);
    }

    private static ItemStack firstDoll(ICurioStacksHandler handler) {
        IDynamicStackHandler stacks = handler.getStacks();
        NonNullList<Boolean> active = handler.getActiveStates();
        for (int slot = 0; slot < stacks.getSlots(); slot++) {
            // A slot the player switched off still holds its stack but does not apply it.
            if (slot >= active.size() || !active.get(slot)) continue;
            ItemStack worn = stacks.getStackInSlot(slot);
            if (worn.getItem() instanceof FumoItem) {
                return worn;
            }
        }
        return ItemStack.EMPTY;
    }

    /**
     * The curio identity itself. Only the attribute hook is overridden: the defaults already say the rest
     * - the doll can be equipped from use, it can be unequipped, and off the head it contributes no armour.
     * The mana a doll carries is the one thing that has to be handed to Curios explicitly; without Ars
     * Nouveau {@link FumoItem#manaGrants} answers empty and this returns Curios' own default.
     */
    private enum DollCurio implements ICurioItem {
        INSTANCE;

        @Override
        public Multimap<Holder<Attribute>, AttributeModifier> getAttributeModifiers(
                SlotContext slotContext, ResourceLocation id, ItemStack stack) {
            List<FumoItem.AttributeGrant> grants = FumoItem.manaGrants(stack);
            if (grants.isEmpty()) {
                return ImmutableMultimap.of();
            }
            Multimap<Holder<Attribute>, AttributeModifier> modifiers = HashMultimap.create();
            for (FumoItem.AttributeGrant grant : grants) {
                modifiers.put(grant.attribute(), grant.modifier());
            }
            return modifiers;
        }
    }
}

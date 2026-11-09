package cn.dancingsnow.neoecoprototype.gametest;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.item.decoration.FumoItem;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * The only place the game test source touches Curios' classes.
 *
 * <p>Same reason {@link ArsEnergistiqueCellTestProbe} exists: the game test bootstrap resolves every
 * {@code @GameTest} method on its own class, so naming a class from a mod that is not installed stops the
 * test server from starting at all. The caller has already checked that Curios is loaded, and this probe
 * answers either {@code null} or one sentence naming what failed.</p>
 */
final class CurioDollTestProbe {

    /** The six dolls, listed once here so a seventh cannot be added to the registry and forgotten. */
    private static final List<Supplier<FumoItem>> DOLLS = List.of(
            ModRegistration.FUMO_DOLL_RELIQWQ_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_CPU_ITEM,
            ModRegistration.FUMO_DOLL_YANG120_GIANT_ITEM,
            ModRegistration.FUMO_DOLL_KOOOKI_ITEM,
            ModRegistration.FUMO_DOLL_TEDXENON_ITEM);

    private static final String HEAD = "head";

    /**
     * Everything the accessory slot promises, on the four layers Curios actually consults: the item
     * capability, the {@code curios:head} tag its slot validator reads, the attribute multimap it adds to
     * the wearer, and the doll the effect tick finds when only the accessory slot is occupied.
     */
    static String firstProblem(ServerLevel level) {
        TagKey<Item> headTag = TagKey.create(Registries.ITEM, ResourceLocation.fromNamespaceAndPath("curios", HEAD));
        for (Supplier<FumoItem> dollSupplier : DOLLS) {
            Item doll = dollSupplier.get();
            ItemStack stack = new ItemStack(doll);
            ResourceLocation id = BuiltInRegistries.ITEM.getKey(doll);
            if (CuriosApi.getCurio(stack).isEmpty()) {
                return id + " answers no curio capability, so Curios will not let it into any accessory slot";
            }
            // The capability only says "wearable somewhere"; this tag is what the head slot's own
            // curios:tag validator reads, and without it the slot refuses the doll even though it is a curio.
            if (!stack.is(headTag)) {
                return id + " is not in the curios:head tag, so the head slot's validator rejects it";
            }
        }

        ServerPlayer player = FakePlayerFactory.get(level,
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "doll-curio"));
        player.getInventory().clearContent();
        player.setItemSlot(EquipmentSlot.HEAD, ItemStack.EMPTY);
        ItemStack doll = new ItemStack(ModRegistration.FUMO_DOLL_TEDXENON_ITEM.get());
        SlotContext headSlot = new SlotContext(HEAD, player, 0, false, true);
        if (!CuriosApi.isStackValid(headSlot, doll)) {
            return "a doll is not valid in the head curio slot, so the two registrations above do not meet";
        }
        // The ruler has to be able to say no: if a plain stone were valid here, the line above would prove
        // nothing about the tag or the capability.
        if (CuriosApi.isStackValid(headSlot, new ItemStack(net.minecraft.world.item.Items.STONE))) {
            return "stone is accepted by the head curio slot, so the validity check above proves nothing";
        }

        // "No armour in the accessory slot" is exactly this multimap being empty - Curios reads modifiers
        // from here and never from the item's vanilla attribute_modifiers component. TedXenon carries no
        // mana, so for her the map has to stay empty even when Ars Nouveau is installed.
        var curioModifiers = CuriosApi.getAttributeModifiers(headSlot, NeoECOPrototype.id("curio_test"), doll);
        if (!curioModifiers.isEmpty()) {
            return "worn as a curio the doll contributes " + curioModifiers
                    + ", expected none - the accessory slot must not add armour";
        }
        // Control for the line above: the same item on the head must still carry its armour, otherwise an
        // empty multimap could just mean the doll has no attribute modifiers at all.
        if (doll.getItem().getDefaultAttributeModifiers(doll).modifiers().isEmpty()) {
            return "the helmet path carries no attribute modifiers either, so the emptiness above proves"
                    + " nothing";
        }

        // kouooki is the one doll with mana, and the accessory slot is a second grant path that has to
        // agree with the helmet one. Both sides come from FumoItem.manaGrants, so with Ars Nouveau absent
        // the expected list and this map are both empty - the no-mod shape is asserted here, not skipped.
        ItemStack mage = new ItemStack(ModRegistration.FUMO_DOLL_KOOOKI_ITEM.get());
        List<FumoItem.AttributeGrant> expectedMana = FumoItem.manaGrants(mage);
        var curioMana = CuriosApi.getAttributeModifiers(headSlot, NeoECOPrototype.id("curio_mana"), mage);
        if (curioMana.size() != expectedMana.size()) {
            return "kouooki worn as a curio contributes " + curioMana + ", expected exactly " + expectedMana;
        }
        for (FumoItem.AttributeGrant grant : expectedMana) {
            if (!curioMana.get(grant.attribute()).contains(grant.modifier())) {
                return "kouooki worn as a curio contributes " + curioMana + ", missing " + grant.modifier();
            }
        }

        ICuriosItemHandler inventory = CuriosApi.getCuriosInventory(player).orElse(null);
        if (inventory == null) {
            return "the test player has no curio inventory, so the accessory slot cannot be exercised";
        }
        ICurioStacksHandler head = inventory.getStacksHandler(HEAD).orElse(null);
        if (head == null) {
            return "the test player has no " + HEAD + " curio slot";
        }
        head.getStacks().setStackInSlot(0, doll.copy());
        ItemStack worn = cn.dancingsnow.neoecoprototype.integration.curios.CuriosDollSupport.wornDoll(player);
        if (!ItemStack.isSameItemSameComponents(worn, doll)) {
            return "a doll sitting in the head curio slot was read back as " + worn + ", not " + doll;
        }

        // And the tick itself: with the helmet slot empty, the curio alone has to be enough for the doll's
        // hand-outs. Night vision is the one every doll gives, so this fails for all six at once if the
        // fallback never runs.
        player.tickCount = 0;
        cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects.onPlayerTick(
                new net.neoforged.neoforge.event.tick.PlayerTickEvent.Post(player));
        if (player.getEffect(MobEffects.NIGHT_VISION) == null) {
            return "a doll in the head curio slot handed out no night vision, so the tick still only"
                    + " reads the helmet slot";
        }
        return null;
    }

    private CurioDollTestProbe() {
    }
}

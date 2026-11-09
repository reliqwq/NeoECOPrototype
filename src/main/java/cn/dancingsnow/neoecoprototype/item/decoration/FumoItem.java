package cn.dancingsnow.neoecoprototype.item.decoration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.level.block.Block;

import java.util.List;
import org.jetbrains.annotations.Nullable;

/**
 * Block item for the four dolls of honoured players. Every one of them can be worn on the head: night
 * vision and the creepers keeping their distance come from
 * {@link cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects}, and each doll carries its own armour
 * and its own list of worn effects as a {@link DollStats} handed over where it is registered. One of them
 * also carries a {@link ManaBonus}, granted only when Ars Nouveau is installed.
 *
 * <p>The face is in the block's own model, so nothing here resolves a player skin and no custom renderer
 * is involved. There used to be a fifth plushie that did exactly that, textured after whoever
 * {@code /prototypefumo} looked up; it is gone, and the four are what ships.
 */
public class FumoItem extends BlockItem implements Equipable {
    /**
     * One effect a worn doll hands out, at the level it is handed out at.
     *
     * <p>The level lives here rather than at the site that applies it because the tooltip and the
     * hand-out have to agree: {@code FumoEquipmentEffects} builds its {@code MobEffectInstance} straight
     * from this record, and the line below quotes the same number.
     */
    public record DollEffect(Holder<MobEffect> effect, int amplifier) {
        private static final String[] NUMERALS = {"", "II", "III", "IV", "V"};

        /** The effect's own name with vanilla's roman numeral on it - none for the first level, as vanilla. */
        public Component describe() {
            var name = effect.value().getDisplayName().copy();
            if (amplifier <= 0) {
                return name;
            }
            return name.append(" ")
                    .append(amplifier < NUMERALS.length ? NUMERALS[amplifier] : Integer.toString(amplifier + 1));
        }
    }

    /**
     * The Ars Nouveau mana a doll hands out while worn, both numbers added straight to the wearer's own
     * bonus attributes. It lives here as two doubles rather than as an Ars Nouveau type on purpose: this
     * class is loaded on every pack, including the ones that never installed Ars Nouveau, so a field or
     * parameter typed after that mod would take the item down at class load. The attributes are found by
     * id when they are asked for - see {@link #manaGrants}.
     */
    public record ManaBonus(double regen, double maxMana) {
        public static final ManaBonus NONE = new ManaBonus(0.0D, 0.0D);

        public boolean isNone() {
            return this.regen == 0.0D && this.maxMana == 0.0D;
        }
    }

    /** One attribute a doll adds, already resolved against the registry; absent when its mod is absent. */
    public record AttributeGrant(Holder<Attribute> attribute, AttributeModifier modifier) {
    }

    /**
     * What the doll grants while worn: its own armour, the effects on top of the shared night vision, the
     * AE/t it is authored to push into chargeable things in the wearer's pockets - zero for the dolls that
     * do not charge anything, which is most of them - and the {@link ManaBonus} it adds, which is none for
     * every doll except kouooki.
     *
     * <p>A non-zero charge rate marks the doll as the charging one; how much it actually pays is the server
     * config's call, so the number is only the shipped default.
     */
    public record DollStats(float armor, float toughness, List<DollEffect> extraEffects,
                            double chargeAEPerTick, ManaBonus mana) {
    }

    /** The doll's own armour values, given at registration and never null. */
    private final DollStats wornStats;
    private static final ResourceLocation ARMOR_ID = NeoECOPrototype.id("fumo_armor");
    private static final ResourceLocation TOUGHNESS_ID = NeoECOPrototype.id("fumo_armor_toughness");
    // Ars Nouveau registers these two in its own namespace, with the perk's description id as the path
    // (PerkAttributes reads "ars_nouveau" + "ars_nouveau.perk.mana_regen" / "…max_mana"). These two strings
    // are the whole reference - no Ars Nouveau class is named here, which is what keeps the doll loadable
    // on a pack without that mod.
    private static final ResourceLocation MANA_REGEN_ATTRIBUTE =
            ResourceLocation.fromNamespaceAndPath("ars_nouveau", "ars_nouveau.perk.mana_regen");
    private static final ResourceLocation MAX_MANA_ATTRIBUTE =
            ResourceLocation.fromNamespaceAndPath("ars_nouveau", "ars_nouveau.perk.max_mana");
    private static final ResourceLocation MANA_REGEN_MODIFIER = NeoECOPrototype.id("fumo_mana_regen");
    private static final ResourceLocation MAX_MANA_MODIFIER = NeoECOPrototype.id("fumo_max_mana");

    public FumoItem(Block block, Properties properties, DollStats wornStats) {
        super(block, properties);
        this.wornStats = java.util.Objects.requireNonNull(wornStats, "a doll needs its worn stats");
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** What a particular doll grants on top of the night vision every doll gives, empty for most of them. */
    public static List<DollEffect> extraEffects(ItemStack stack) {
        DollStats stats = statsOf(stack);
        return stats == null ? List.of() : stats.extraEffects();
    }

    /** Null for anything that is not one of our dolls. */
    @Nullable
    public static DollStats statsOf(ItemStack stack) {
        return stack.getItem() instanceof FumoItem item ? item.wornStats : null;
    }

    /**
     * The mana attributes this doll adds, resolved by id at the moment of the question.
     *
     * <p>Empty in two cases that look the same to the caller and are both correct: the doll has no mana to
     * give, or Ars Nouveau is not installed, in which case the registry has no entry under those two ids.
     * Nothing is cached - the registry is the same one the server and the client both hold, so a pack that
     * adds Ars Nouveau later, or a client that receives it, resolves the attributes without a second
     * code path.
     */
    public static List<AttributeGrant> manaGrants(ItemStack stack) {
        DollStats stats = statsOf(stack);
        if (stats == null || stats.mana().isNone()) {
            return List.of();
        }
        List<AttributeGrant> grants = new java.util.ArrayList<>(2);
        addManaGrant(grants, MANA_REGEN_ATTRIBUTE, MANA_REGEN_MODIFIER, stats.mana().regen());
        addManaGrant(grants, MAX_MANA_ATTRIBUTE, MAX_MANA_MODIFIER, stats.mana().maxMana());
        return List.copyOf(grants);
    }

    private static void addManaGrant(List<AttributeGrant> grants, ResourceLocation attributeId,
                                     ResourceLocation modifierId, double amount) {
        if (amount == 0.0D) {
            return;
        }
        BuiltInRegistries.ATTRIBUTE.getHolder(attributeId)
                .map(attribute -> new AttributeGrant(attribute,
                        new AttributeModifier(modifierId, amount, AttributeModifier.Operation.ADD_VALUE)))
                .ifPresent(grants::add);
    }

    /** The dolls double as light armour, each with its own values. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        DollStats stats = statsOf(stack);
        if (stats == null) return super.getDefaultAttributeModifiers(stack);
        ItemAttributeModifiers.Builder modifiers = ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(ARMOR_ID, stats.armor(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(TOUGHNESS_ID, stats.toughness(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD);
        // The mana lines are not written as tooltip text anywhere: vanilla prints whatever this component
        // holds, under "When on head", with Ars Nouveau's own names for the two attributes.
        for (AttributeGrant grant : manaGrants(stack)) {
            modifiers.add(grant.attribute(), grant.modifier(), EquipmentSlotGroup.HEAD);
        }
        return modifiers.build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        DollStats stats = statsOf(stack);
        if (stats == null) return;
        // What these lines promise is exactly what FumoEquipmentEffects#onPlayerTick grants.
        tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_hint")
                .withStyle(ChatFormatting.GRAY));
        for (DollEffect extra : stats.extraEffects()) {
            // Named after the effect itself, so a future DollStats entry needs no new lang key and the
            // line cannot drift from what FumoEquipmentEffects actually applies - the level comes from the
            // same number that is handed to the MobEffectInstance.
            tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_extra",
                            extra.describe())
                    .withStyle(ChatFormatting.GRAY));
        }
        if (stats.chargeAEPerTick() > 0.0D) {
            // No number on this line: the rate is a server config key, and a dedicated client never loads
            // one, so quoting it here would mean hard-coding a value the server may have changed.
            tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_charge")
                    .withStyle(ChatFormatting.GRAY));
        }
    }
}

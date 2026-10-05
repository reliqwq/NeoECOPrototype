package cn.dancingsnow.neoecoprototype.item.decoration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.client.render.FumoItemRenderer;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Equipable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;
import net.minecraft.world.item.component.ResolvableProfile;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

/**
 * Block item for the plushies, all of which can be worn on the head - see
 * {@link cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects} for the night vision and
 * {@link #isNamedDoll(ItemStack)} for which dolls grant more than that.
 *
 * <p>Two kinds live here. The four dolls of honoured players are each their own block, drawn from the
 * art the player posed with, and carry their armour and extra effect as a {@link DollStats} given at
 * registration. The fifth one - {@code fumo_reliqwq} - wears whoever's skin {@code /prototypefumo}
 * looked up, is drawn by a block entity renderer instead of a block model, and grants nothing.
 */
public class FumoItem extends BlockItem implements Equipable {
    /** What the doll grants while worn on the head, plus the effect only some of them carry. */
    public record DollStats(float armor, float toughness, @Nullable Holder<MobEffect> extraEffect) {
    }

    /** The four honoured players' armour, each with its own values. Null on the custom-skin doll. */
    @Nullable
    private final DollStats wornStats;
    private static final ResourceLocation ARMOR_ID = NeoECOPrototype.id("fumo_armor");
    private static final ResourceLocation TOUGHNESS_ID = NeoECOPrototype.id("fumo_armor_toughness");

    public FumoItem(Block block, Properties properties) {
        this(block, properties, null);
    }

    public FumoItem(Block block, Properties properties, @Nullable DollStats wornStats) {
        super(block, properties);
        this.wornStats = wornStats;
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** True for the dolls of the honoured players, false for a doll wearing an ad-hoc skin. */
    public static boolean isNamedDoll(ItemStack stack) {
        return statsOf(stack) != null;
    }

    /** What a particular doll grants on top of the night vision every named doll gives. */
    @Nullable
    public static Holder<MobEffect> extraEffect(ItemStack stack) {
        DollStats stats = statsOf(stack);
        return stats == null ? null : stats.extraEffect();
    }

    @Nullable
    public static DollStats statsOf(ItemStack stack) {
        return stack.getItem() instanceof FumoItem item ? item.wornStats : null;
    }

    /** The named dolls double as light armour, each with its own values. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        DollStats stats = statsOf(stack);
        if (stats == null) return super.getDefaultAttributeModifiers(stack);
        return ItemAttributeModifiers.builder()
                .add(Attributes.ARMOR,
                        new AttributeModifier(ARMOR_ID, stats.armor(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .add(Attributes.ARMOR_TOUGHNESS,
                        new AttributeModifier(TOUGHNESS_ID, stats.toughness(), AttributeModifier.Operation.ADD_VALUE),
                        EquipmentSlotGroup.HEAD)
                .build();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        // What these two lines promise is exactly what FumoEquipmentEffects#onPlayerTick now grants,
        // and that is gated on the doll being one of the named four. An unnamed doll must not advertise
        // a night vision it never applies.
        if (!isNamedDoll(stack)) return;
        tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_hint")
                .withStyle(ChatFormatting.GRAY));
        Holder<MobEffect> extra = extraEffect(stack);
        if (extra != null) {
            // Named after the effect itself, so a future DollStats entry needs no new lang key and the
            // line cannot drift from what FumoEquipmentEffects actually applies.
            tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_extra",
                            extra.value().getDisplayName())
                    .withStyle(ChatFormatting.GRAY));
        }
    }

    @Override
    public Component getName(ItemStack stack) {
        // A dedicated doll is named by its own block; only the custom-skin doll borrows a player's name.
        if (wornStats != null) return super.getName(stack);
        ResolvableProfile owner = stack.get(ModRegistration.FUMO_OWNER.get());
        // The lang key is "%s's Doll", so a profile carrying a uuid and no name reads as a dangling
        // possessive - "'s Doll", and " 玩偶" in Chinese.
        if (owner == null || owner.name().isEmpty()) return super.getName(stack);
        return Component.translatable("block.neoecoprototype.fumo_reliqwq.named", owner.name().get());
    }

    /** Only the custom-skin doll is drawn from a resolved player skin; the others are block models. */
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        if (wornStats != null) {
            super.initializeClient(consumer);
            return;
        }
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FumoItemRenderer.get();
            }
        });
    }
}

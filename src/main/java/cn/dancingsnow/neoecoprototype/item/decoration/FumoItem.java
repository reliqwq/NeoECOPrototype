package cn.dancingsnow.neoecoprototype.item.decoration;

import cn.dancingsnow.neoecoprototype.NeoECOPrototype;
import cn.dancingsnow.neoecoprototype.client.render.FumoItemRenderer;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import com.mojang.authlib.properties.PropertyMap;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
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
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import org.jetbrains.annotations.Nullable;

/**
 * Block item that draws the plushie with the same doll renderer as the block in every item context,
 * and can be worn on the head - see {@link cn.dancingsnow.neoecoprototype.event.FumoEquipmentEffects}
 * for the night vision and {@link #isNamedDoll(ItemStack)} for the named dolls' armour.
 */
public class FumoItem extends BlockItem implements Equipable {
    /** The dolls the guide book documents; only these are named, green and armoured. */
    private static final Map<String, DollStats> NAMED_DOLLS = Map.of(
            "reliqwq", new DollStats(4.0F, 2.0F),
            "yang120", new DollStats(4.0F, 2.0F),
            "kouooki", new DollStats(1.0F, 5.0F));
    private static final ResourceLocation ARMOR_ID = NeoECOPrototype.id("fumo_armor");
    private static final ResourceLocation TOUGHNESS_ID = NeoECOPrototype.id("fumo_armor_toughness");

    /** Armour a named doll grants while worn on the head. */
    private record DollStats(float armor, float toughness) {
    }

    public FumoItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public EquipmentSlot getEquipmentSlot() {
        return EquipmentSlot.HEAD;
    }

    /** True for the dolls of the honoured players, false for a plain or ad-hoc named doll. */
    public static boolean isNamedDoll(ItemStack stack) {
        return namedDollStats(stack) != null;
    }

    @Nullable
    private static DollStats namedDollStats(ItemStack stack) {
        ResolvableProfile owner = stack.get(ModRegistration.FUMO_OWNER.get());
        return owner == null ? null
                : owner.name().map(name -> NAMED_DOLLS.get(name.toLowerCase(Locale.ROOT))).orElse(null);
    }

    /** The named dolls double as light armour, each with its own values. */
    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers(ItemStack stack) {
        DollStats stats = namedDollStats(stack);
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
        tooltip.add(Component.translatable("block.neoecoprototype.fumo_reliqwq.worn_hint")
                .withStyle(ChatFormatting.GRAY));
    }

    /** A doll that already wears the given player's skin, for the creative tab and testing. */
    public static ItemStack ownedBy(String name) {
        ItemStack stack = new ItemStack(ModRegistration.FUMO_RELIQWQ_ITEM.get());
        stack.set(ModRegistration.FUMO_OWNER.get(),
                new ResolvableProfile(Optional.of(name), Optional.empty(), new PropertyMap()));
        return stack;
    }

    @Override
    public Component getName(ItemStack stack) {
        ResolvableProfile owner = stack.get(ModRegistration.FUMO_OWNER.get());
        if (owner == null) return super.getName(stack);
        MutableComponent name = Component.translatable("block.neoecoprototype.fumo_reliqwq.named",
                owner.name().orElse(""));
        return isNamedDoll(stack) ? name.withStyle(ChatFormatting.GREEN) : name;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(new IClientItemExtensions() {
            @Override
            public BlockEntityWithoutLevelRenderer getCustomRenderer() {
                return FumoItemRenderer.get();
            }
        });
    }
}

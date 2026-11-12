package cn.dancingsnow.neoecoprototype.gametest;

import appeng.api.stacks.AEItemKey;
import appeng.api.stacks.GenericStack;
import appeng.block.crafting.PatternProviderBlock;
import appeng.core.definitions.AEItems;
import appeng.helpers.patternprovider.PatternProviderLogicHost;
import cn.dancingsnow.neoecoprototype.blockentity.crafting.SimplifyPatternProviderBlockEntity;
import cn.dancingsnow.neoecoprototype.part.SimplifyPatternProviderPart;
import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public final class AddonProviderConversionTestProbe {
    private static final ResourceLocation EXTENDED_PATTERN_UPGRADE = id("extendedae", "pattern_provider_upgrade");
    private static final ResourceLocation ADVANCED_PATTERN_UPGRADE = id("advanced_ae", "adv_pattern_provider_upgrade");

    private AddonProviderConversionTestProbe() {
    }

    public static String checkAe2PatternProviderBecomesL1(GameTestHelper helper) {
        return checkAe2BlockUpgrade(helper, appeng.core.definitions.AEBlocks.PATTERN_PROVIDER.block(),
                ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BLOCK.get(),
                () -> ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BE.get(),
                ModRegistration.AE2_PATTERN_PROVIDER_TO_L1_CARD.get(), "AE2 pattern provider");
    }

    public static String checkAe2InterfaceBecomesL1(GameTestHelper helper, boolean superconductive) {
        return checkAe2BlockUpgrade(helper, appeng.core.definitions.AEBlocks.INTERFACE.block(),
                (superconductive ? ModRegistration.SUPERCONDUCTIVE_INTERFACE_BLOCK.get()
                        : ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BLOCK.get()),
                () -> (superconductive ? ModRegistration.SUPERCONDUCTIVE_INTERFACE_BE.get()
                        : ModRegistration.SIMPLIFY_POWERED_ME_INTERFACE_BE.get()),
                (superconductive ? ModRegistration.AE2_INTERFACE_TO_L1_SUPERCONDUCTIVE_CARD.get()
                        : ModRegistration.AE2_INTERFACE_TO_L1_POWERED_CARD.get()), "AE2 interface");
    }

    public static String checkAe2PartsBecomeL1(GameTestHelper helper) {
        String providerProblem = checkPartUpgrade(helper, appeng.parts.crafting.PatternProviderPart.class,
                ModRegistration.CABLE_PATTERN_PROVIDER_PART,
                ModRegistration.AE2_PATTERN_PROVIDER_TO_L1_CARD.get(), "pattern provider");
        if (providerProblem != null) return providerProblem;
        String poweredProblem = checkPartUpgrade(helper, appeng.parts.misc.InterfacePart.class,
                ModRegistration.POWERED_INTERFACE_PART,
                ModRegistration.AE2_INTERFACE_TO_L1_POWERED_CARD.get(), "powered interface");
        if (poweredProblem != null) return poweredProblem;
        return checkPartUpgrade(helper, appeng.parts.misc.InterfacePart.class,
                ModRegistration.SUPERCONDUCTIVE_INTERFACE_PART,
                ModRegistration.AE2_INTERFACE_TO_L1_SUPERCONDUCTIVE_CARD.get(), "superconductive interface");
    }

    private static String checkPartUpgrade(GameTestHelper helper, Class<? extends appeng.api.parts.IPart> sourceClass,
                                           java.util.function.Supplier<? extends appeng.api.parts.IPartItem<?>> target,
                                           Item card, String label) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos, appeng.core.definitions.AEBlocks.CABLE_BUS.block().defaultBlockState(), 3);
        var bus = (appeng.blockentity.networking.CableBusBlockEntity) level.getBlockEntity(pos);
        Direction side = Direction.NORTH;
        appeng.api.parts.IPartItem<?> sourceItem;
        if (sourceClass == appeng.parts.crafting.PatternProviderPart.class) {
            sourceItem = appeng.core.definitions.AEParts.PATTERN_PROVIDER.get();
        } else {
            sourceItem = appeng.core.definitions.AEParts.INTERFACE.get();
        }
        var source = bus.getCableBus().addPart((appeng.api.parts.IPartItem) sourceItem, side, null);
        if (source == null) {
            return "could not install AE2 " + label + " source part";
        }
        if (source == null || source.getClass() != sourceClass) {
            return "AE2 " + label + " source part class mismatch: "
                    + (source == null ? "null" : source.getClass().getName());
        }
        var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "l1-part-upgrade"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        // Click inside the face's own region, not the middle of the block. The card asks AE2
        // selectPartLocal(localHit), and the exact centre belongs to the cable anchor, so a centre hit
        // selects no part, the card returns PASS, and this test would read "the card does nothing" when
        // what failed was the fixture. A player never aims there: the click lands on the part's surface.
        Vec3 hitPos = Vec3.atLowerCornerOf(pos).add(0.5D + 0.25D * side.getStepX(),
                0.5D + 0.25D * side.getStepY(), 0.5D + 0.25D * side.getStepZ());
        var hit = new BlockHitResult(hitPos, side, pos, false);
        ItemStack cardStack = new ItemStack(card);
        cardStack.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                level, player, InteractionHand.MAIN_HAND, cardStack, hit));
        var converted = bus.getCableBus().getPart(side);
        if (converted == null || converted.getClass() != target.get().getPartClass()) {
            return label + " card did not install the requested L1 part: "
                    + (converted == null ? "null" : converted.getClass().getName());
        }
        return cardStack.isEmpty() ? null : label + " card was not consumed";
    }

    private static String checkAe2BlockUpgrade(GameTestHelper helper, net.minecraft.world.level.block.Block sourceBlock,
                                                net.minecraft.world.level.block.Block targetBlock,
                                                java.util.function.Supplier<? extends net.minecraft.world.level.block.entity.BlockEntityType<?>> targetType,
                                                Item card, String label) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos, sourceBlock.defaultBlockState(), 3);
        BlockEntity before = level.getBlockEntity(pos);
        if (before == null || before.getClass() != (sourceBlock == appeng.core.definitions.AEBlocks.PATTERN_PROVIDER.block()
                ? appeng.blockentity.crafting.PatternProviderBlockEntity.class
                : appeng.blockentity.misc.InterfaceBlockEntity.class)) {
            return label + " fixture did not create the exact AE2 source block entity";
        }
        var sourceTag = before.saveWithFullMetadata(level.registryAccess());
        var player = FakePlayerFactory.get(level, new com.mojang.authlib.GameProfile(UUID.randomUUID(), "l1-upgrade"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        ItemStack stack = new ItemStack(card);
        var hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        var result = stack.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                level, player, InteractionHand.MAIN_HAND, stack, hit));
        BlockEntity after = level.getBlockEntity(pos);
        if (after == before || after == null || after.getType() != targetType.get()
                || !targetType.get().isValid(level.getBlockState(pos))) {
            return label + " upgrade failed: result=" + result + " target block="
                    + BuiltInRegistries.BLOCK.getKey(level.getBlockState(pos).getBlock()) + " target entity="
                    + (after == null ? "null" : after.getClass().getName());
        }
        var targetTag = after.saveWithFullMetadata(level.registryAccess());
        if (sourceBlock == appeng.core.definitions.AEBlocks.PATTERN_PROVIDER.block()
                && !sourceTag.getList("patterns", net.minecraft.nbt.Tag.TAG_COMPOUND)
                .equals(targetTag.getList("patterns", net.minecraft.nbt.Tag.TAG_COMPOUND))) {
            return "AE2 pattern provider patterns did not survive conversion";
        }
        if (sourceBlock == appeng.core.definitions.AEBlocks.INTERFACE.block()
                && (!sourceTag.getCompound("config").equals(targetTag.getCompound("config"))
                || !sourceTag.getCompound("storage").equals(targetTag.getCompound("storage")))) {
            return "AE2 interface configuration did not survive conversion";
        }
        if (!stack.isEmpty()) {
            return label + " conversion succeeded but did not consume the upgrade card";
        }
        return null;
    }

    public static String checkExtendedPatternProvider(GameTestHelper helper) {
        return checkPatternProviderUpgrade(helper, EXTENDED_PATTERN_UPGRADE, "ExtendedAE");
    }

    public static String checkAdvancedPatternProvider(GameTestHelper helper) {
        return checkPatternProviderUpgrade(helper, ADVANCED_PATTERN_UPGRADE, "Advanced AE");
    }

    private static String checkPatternProviderUpgrade(GameTestHelper helper, ResourceLocation upgradeId,
                                                      String label) {
        var level = helper.getLevel();
        BlockPos pos = helper.absolutePos(new BlockPos(2, 2, 2));
        level.setBlock(pos, ModRegistration.SIMPLIFY_PATTERN_PROVIDER_BLOCK.get().defaultBlockState()
                .setValue(PatternProviderBlock.PUSH_DIRECTION, appeng.block.crafting.PushDirection.EAST), 3);
        BlockEntity before = level.getBlockEntity(pos);
        if (!(before instanceof SimplifyPatternProviderBlockEntity source)) {
            return label + " fixture did not create the addon's L1 pattern provider";
        }
        var logic = source.getLogic();
        ItemStack encoded = appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(
                List.of(new GenericStack(AEItemKey.of(net.minecraft.world.item.Items.IRON_INGOT), 3)),
                List.of(new GenericStack(AEItemKey.of(net.minecraft.world.item.Items.GOLD_INGOT), 1)));
        ItemStack encodedLast = appeng.api.crafting.PatternDetailsHelper.encodeProcessingPattern(
                List.of(new GenericStack(AEItemKey.of(net.minecraft.world.item.Items.COPPER_INGOT), 5)),
                List.of(new GenericStack(AEItemKey.of(net.minecraft.world.item.Items.COPPER_BLOCK), 1)));
        logic.getPatternInv().setItemDirect(0, encoded.copy());
        logic.getPatternInv().setItemDirect(26, encodedLast.copy());
        logic.setPriority(43);
        var sourceTag = source.saveWithFullMetadata(level.registryAccess());

        ItemStack upgrade = upgradeStack(helper, upgradeId);
        if (upgrade.isEmpty()) {
            return label + " pattern-provider upgrade item is missing from the registry: " + upgradeId;
        }
        var result = useOn(helper, upgrade, pos);
        BlockEntity after = level.getBlockEntity(pos);
        if (after == before) {
            return label + " upgrade did not replace the L1 provider: result=" + result
                    + " before=" + before.getClass().getName();
        }
        var convertedTag = after.saveWithFullMetadata(level.registryAccess());
        var sourcePatterns = sourceTag.getList("patterns", net.minecraft.nbt.Tag.TAG_COMPOUND);
        var convertedPatterns = convertedTag.getList("patterns", net.minecraft.nbt.Tag.TAG_COMPOUND);
        if (!sourcePatterns.equals(convertedPatterns)) {
            return label + " pattern inventory NBT changed during conversion: before=" + sourcePatterns
                    + " after=" + convertedPatterns + " destination=" + after.getClass().getName();
        }
        if (convertedTag.getInt("priority") != 43) {
            return label + " provider priority did not survive conversion: " + convertedTag.getInt("priority");
        }
        BlockState convertedState = level.getBlockState(pos);
        if (convertedState.hasProperty(PatternProviderBlock.PUSH_DIRECTION)
                && convertedState.getValue(PatternProviderBlock.PUSH_DIRECTION)
                != appeng.block.crafting.PushDirection.EAST) {
            return label + " provider push direction did not survive conversion: "
                    + convertedState.getValue(PatternProviderBlock.PUSH_DIRECTION);
        }
        return null;
    }

    private static ItemStack upgradeStack(GameTestHelper helper, ResourceLocation itemId) {
        Item item = BuiltInRegistries.ITEM.get(itemId);
        return item == net.minecraft.world.item.Items.AIR ? ItemStack.EMPTY : new ItemStack(item);
    }

    private static net.minecraft.world.InteractionResult useOn(GameTestHelper helper, ItemStack stack,
                                                                 BlockPos pos) {
        ServerPlayer player = FakePlayerFactory.get(helper.getLevel(),
                new com.mojang.authlib.GameProfile(UUID.randomUUID(), "provider-upgrade-test"));
        player.setGameMode(GameType.SURVIVAL);
        player.setPos(pos.getX() + 0.5, pos.getY() + 1.0, pos.getZ() + 0.5);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(pos), Direction.UP, pos, false);
        return stack.getItem().useOn(new net.minecraft.world.item.context.UseOnContext(
                helper.getLevel(), player, InteractionHand.MAIN_HAND, stack, hit));
    }

    private static ResourceLocation id(String namespace, String path) {
        return ResourceLocation.fromNamespaceAndPath(namespace, path);
    }
}

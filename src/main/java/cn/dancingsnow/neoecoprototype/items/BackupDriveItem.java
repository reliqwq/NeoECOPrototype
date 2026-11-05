package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * The last resting place for everything a structure card owes the player.
 *
 * <p>Why it exists: a swap takes out the blocks it moved - roughly ninety for a machine at its ceiling - and
 * a refused click prints a work order plus an encoded processing pattern. None of that may land on the floor:
 * an item entity there despawns in five minutes, and a machine that just changed tier would otherwise litter
 * its own footprint with ninety loose blocks. The card therefore gathers what a click owes into one bag and
 * hands that over instead.</p>
 *
 * <p>What is in a bag is a plain saved {@link ItemStack} per entry, in {@link DataComponents#CUSTOM_DATA} -
 * id, count and components. The first version of this item stored id and count only, which was fine for
 * refund blocks and quietly fatal for the one thing a bag now also carries: a work-order pattern, whose whole
 * content is its components. An entry this game cannot resolve (a pack change) is kept in the bag rather than
 * quietly eaten, which is the same rule the card follows everywhere else: something the player owns may move
 * places, but it never just vanishes.</p>
 */
public class BackupDriveItem extends Item {

    /** The list inside {@code CUSTOM_DATA}; each element is one saved item stack. */
    private static final String TAG_BLOCKS = "neoecoprototype:blocks";
    /**
     * Set only when the click that filled this drive <b>paid an item</b> - a card or a disk eaten - which is
     * what entitles the player to that item's shell back. The person who found the mint asked for exactly
     * this accounting: a card that costs nothing could be clicked again and again, each refusal printing a
     * ticket into a drive that pours into one more empty migration drive.
     */
    private static final String TAG_SHELL = "neoecoprototype:shell";

    public BackupDriveItem(Properties properties) {
        super(properties);
    }

    /** Mark that this drive owes its player the shell of the card or disk the click ate. */
    public static void oweShell(ItemStack drive) {
        CompoundTag tag = tagOf(drive);
        tag.putBoolean(TAG_SHELL, true);
        CustomData.set(DataComponents.CUSTOM_DATA, drive, tag);
    }

    /** Whether pouring this drive out should leave the player holding one empty migration drive. */
    public static boolean paysShell(ItemStack drive) {
        return tagOf(drive).getBoolean(TAG_SHELL);
    }

    private static CompoundTag tagOf(ItemStack drive) {
        CustomData data = drive.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    /**
     * A bag holding these stacks. The caller is expected to have merged them already - one entry per item,
     * each within the item's own stack limit - because that is the whole point of the container.
     */
    public static ItemStack withContents(@Nullable HolderLookup.Provider registries, List<ItemStack> stacks) {
        ItemStack bag = new ItemStack(ModRegistration.BACKUP_DRIVE.get());
        ListTag list = new ListTag();
        for (ItemStack stack : stacks) {
            if (stack.isEmpty() || registries == null) {
                continue;
            }
            if (stack.save(registries) instanceof CompoundTag saved) {
                list.add(saved);
            }
        }
        CompoundTag tag = new CompoundTag();
        tag.put(TAG_BLOCKS, list);
        CustomData.set(DataComponents.CUSTOM_DATA, bag, tag);
        return bag;
    }

    /** What the bag holds right now; entries this game cannot name are skipped here. */
    public static List<ItemStack> contentsOf(ItemStack bag, @Nullable HolderLookup.Provider registries) {
        List<ItemStack> contents = new ArrayList<>();
        if (registries == null) {
            return contents;
        }
        for (CompoundTag entry : entries(bag)) {
            ItemStack stack = read(entry, registries);
            if (!stack.isEmpty()) {
                contents.add(stack);
            }
        }
        return contents;
    }

    /** Entries the bag holds that this game cannot name - they stay in the bag and are counted here. */
    public static int unresolvableOf(ItemStack bag, @Nullable HolderLookup.Provider registries) {
        int unresolvable = 0;
        for (CompoundTag entry : entries(bag)) {
            if (registries == null || read(entry, registries).isEmpty()) {
                unresolvable++;
            }
        }
        return unresolvable;
    }

    /** How many entries the bag holds, counted whether or not this game can name them. */
    public static int kindsOf(ItemStack bag) {
        return entries(bag).size();
    }

    private static ItemStack read(CompoundTag entry, HolderLookup.Provider registries) {
        return ItemStack.parseOptional(registries, entry);
    }

    private static List<CompoundTag> entries(ItemStack bag) {
        List<CompoundTag> entries = new ArrayList<>();
        CustomData data = bag.get(DataComponents.CUSTOM_DATA);
        if (data == null) {
            return entries;
        }
        CompoundTag tag = data.copyTag();
        if (!tag.contains(TAG_BLOCKS, Tag.TAG_LIST)) {
            return entries;
        }
        for (Tag element : tag.getList(TAG_BLOCKS, Tag.TAG_COMPOUND)) {
            if (element instanceof CompoundTag entry) {
                entries.add(entry);
            }
        }
        return entries;
    }

    /**
     * Put the rung this bag came from on its name, in the ladder's own colours - the same answer a migration
     * disk gives ("each drive wears its start tier"), so a green bag and the green disk say the same thing
     * about where the parts came from.
     *
     * <p>On {@link DataComponents#ITEM_NAME} and never {@code CUSTOM_NAME}: the first is a display override
     * every stack of this bag carries, the second is an anvil rename, which would make the name render
     * italic and read as renamed. A bag whose click moved no host - one that grew a room along its edge -
     * is left plain, because there is no tier in it to name.</p>
     */
    public static void wearTier(ItemStack bag, int colour) {
        bag.set(DataComponents.ITEM_NAME, Component
                .translatable("item.neoecoprototype.backup_drive")
                .withStyle(net.minecraft.network.chat.Style.EMPTY.withColor(colour)));
    }

    /**
     * Right-click: everything the bag holds goes into the player's inventory, whatever cannot fit falls out
     * as an ordinary item drop at their feet, and the bag itself turns into one empty migration drive - the
     * tile it is drawn from. Nothing is left holding a residue, and nothing has to be hunted for twice.
     *
     * <p>The one case that refuses is a bag holding an entry this game cannot name, which only happens after
     * a pack change. Emptying it would drop what cannot be rebuilt and destroying it would delete things
     * nobody was told about, so the bag stays and the tooltip says how many entries are the problem.</p>
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bag = player.getItemInHand(hand);
        List<ItemStack> contents = contentsOf(bag, level.registryAccess());
        int unresolvable = unresolvableOf(bag, level.registryAccess());
        if (unresolvable > 0) {
            if (!level.isClientSide) {
                player.displayClientMessage(Component.translatable(
                        "card.neoecoprototype.backup_drive.unresolvable_refusal", unresolvable), true);
            }
            return InteractionResultHolder.fail(bag);
        }
        if (contents.isEmpty()) {
            if (!level.isClientSide) {
                player.displayClientMessage(
                        Component.translatable("card.neoecoprototype.backup_drive.empty"), true);
            }
            return InteractionResultHolder.fail(bag);
        }
        if (!level.isClientSide) {
            int poured = 0;
            int dropped = 0;
            for (ItemStack stack : contents) {
                if (player.getInventory().add(stack.copy())) {
                    poured += stack.getCount();
                    continue;
                }
                dropped += stack.getCount();
                var drop = new ItemEntity(level, player.getX(), player.getEyeY(), player.getZ(),
                        stack.copy());
                // Ten ticks, so a stack that just fell out cannot go straight back into the hand that
                // threw it while the bag is still being held.
                drop.setPickUpDelay(10);
                level.addFreshEntity(drop);
            }
            // The empty drive is what a spent disk leaves in your hand, and this tile is that tile
            // darkened - so a drive whose click actually ate an item closes that loop: it turns back into
            // the shell. A drive from a click that paid nothing (a refusal, or creative) pours out and is
            // simply gone, or every spare card in a base would be a mint.
            var shell = paysShell(bag)
                    ? new ItemStack(ModRegistration.MIGRATION_DRIVE_EMPTY.get())
                    : ItemStack.EMPTY;
            player.setItemInHand(hand, shell);
            cn.dancingsnow.neoecoprototype.NeoECOPrototype.LOGGER.info(
                    "backup drive poured: {} entries, {} into the inventory, {} on the floor at {}, shell"
                            + " owed = {}, the hand now holds {}",
                    contents.size(), poured, dropped, player.blockPosition(), paysShell(bag),
                    shell.isEmpty() ? "nothing" : BuiltInRegistries.ITEM.getKey(shell.getItem()));
            return InteractionResultHolder.sidedSuccess(shell, level.isClientSide);
        }
        return InteractionResultHolder.pass(bag);
    }

    @Override
    public void appendHoverText(ItemStack bag, Item.TooltipContext context, List<Component> lines,
                                TooltipFlag flag) {
        HolderLookup.Provider registries = context == null ? null : context.registries();
        List<ItemStack> contents = contentsOf(bag, registries);
        if (contents.isEmpty() && unresolvableOf(bag, registries) == 0) {
            lines.add(Component.translatable("card.neoecoprototype.backup_drive.empty_contents"));
            return;
        }
        int shown = 0;
        for (ItemStack stack : contents) {
            if (shown == 4) {
                lines.add(Component.translatable("card.neoecoprototype.backup_drive.more",
                        contents.size() - shown));
                break;
            }
            lines.add(Component.translatable("card.neoecoprototype.backup_drive.line",
                    stack.getHoverName(), stack.getCount()));
            shown++;
        }
        int unresolvable = unresolvableOf(bag, registries);
        if (unresolvable > 0) {
            lines.add(Component.translatable("card.neoecoprototype.backup_drive.unresolvable", unresolvable));
        }
    }
}

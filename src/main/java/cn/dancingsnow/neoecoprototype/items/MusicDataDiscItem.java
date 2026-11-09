package cn.dancingsnow.neoecoprototype.items;

import cn.dancingsnow.neoecoprototype.registration.ModRegistration;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * The disc that carries recorded songs into the ME jukebox. It is an ordinary item, not a storage cell:
 * nothing in AE2 can mount it, and the only thing that reads it is our own jukebox.
 *
 * <p>A disc with no component is a blank: the same stack the recorder will fill. Its playable time is the
 * sum of the song lengths frozen into {@link MusicPlaylist}, and playing it in the jukebox spends that time
 * one tick per tick - the disc itself is never destroyed, it just runs out.</p>
 */
public final class MusicDataDiscItem extends Item {

    public MusicDataDiscItem(Properties properties) {
        super(properties);
    }

    public static DataComponentType<MusicPlaylist> playlistComponent() {
        return ModRegistration.MUSIC_DATA_PLAYLIST.get();
    }

    public static MusicPlaylist playlistOf(ItemStack stack) {
        return stack.getOrDefault(playlistComponent(), MusicPlaylist.EMPTY);
    }

    /** Writes {@code playlist} back, or removes the component once the disc is blank again. */
    public static void setPlaylist(ItemStack stack, MusicPlaylist playlist) {
        if (playlist.equals(MusicPlaylist.EMPTY)) {
            stack.remove(playlistComponent());
        } else {
            stack.set(playlistComponent(), playlist);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip,
                                TooltipFlag flag) {
        MusicPlaylist playlist = playlistOf(stack);
        if (playlist.isEmpty()) {
            tooltip.add(Component.translatable("tooltip.neoecoprototype.music_data_disc_empty")
                    .withStyle(ChatFormatting.GRAY));
            return;
        }
        tooltip.add(Component.translatable("tooltip.neoecoprototype.music_data_disc_songs",
                playlist.songCount()).withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("tooltip.neoecoprototype.music_data_disc_remaining",
                formatTime(playlist.remainingTicks())).withStyle(ChatFormatting.AQUA));
    }

    /** {@code m:ss}, the shape a player reads off a record sleeve. */
    static String formatTime(long ticks) {
        long seconds = Math.max(0L, ticks) / 20L;
        return String.format("%d:%02d", seconds / 60L, seconds % 60L);
    }
}

package cn.dancingsnow.neoecoprototype.items;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/**
 * What one data disc carries: the songs recorded onto it and how much of them the network has already
 * been paid for.
 *
 * <p>Two numbers per song, not one, because they answer different questions. The id says what to play and
 * is resolved against the datapack's jukebox songs at play time; the length is frozen here, so a pack that
 * removes a song cannot silently change what a disc in a chest is worth. {@code playedTicks} is a cursor
 * into the whole playlist, which is also what makes resuming exact: a disc ejected in the middle of a song
 * restarts that song's audio but only pays for the part that was never delivered.</p>
 *
 * <p>The cap of {@value #MAX_SONGS} songs is enforced here rather than by the item, so every door into the
 * component - the recorder later, a command, a hand-edited NBT - lands on the same limit.</p>
 */
public record MusicPlaylist(List<Song> songs, long playedTicks) {

    /** One recording session's worth of distinct songs. */
    public static final int MAX_SONGS = 12;

    public static final MusicPlaylist EMPTY = new MusicPlaylist(List.of(), 0L);

    public record Song(ResourceLocation song, int ticks) {
        public static final Codec<Song> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                ResourceLocation.CODEC.fieldOf("song").forGetter(Song::song),
                Codec.INT.fieldOf("ticks").forGetter(Song::ticks)).apply(instance, Song::new));
        public static final StreamCodec<ByteBuf, Song> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

        public Song {
            if (ticks < 0) {
                ticks = 0;
            }
        }
    }

    public static final Codec<MusicPlaylist> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Song.CODEC.listOf().fieldOf("songs").forGetter(MusicPlaylist::songs),
            Codec.LONG.optionalFieldOf("played_ticks", 0L).forGetter(MusicPlaylist::playedTicks))
            .apply(instance, MusicPlaylist::new));

    public static final StreamCodec<ByteBuf, MusicPlaylist> STREAM_CODEC = ByteBufCodecs.fromCodec(CODEC);

    public MusicPlaylist {
        if (songs.size() > MAX_SONGS) {
            songs = songs.subList(0, MAX_SONGS);
        }
        songs = List.copyOf(songs);
        long total = totalTicksOf(songs);
        if (playedTicks < 0L) {
            playedTicks = 0L;
        } else if (playedTicks > total) {
            playedTicks = total;
        }
    }

    private static long totalTicksOf(List<Song> songs) {
        long total = 0L;
        for (Song song : songs) {
            total += song.ticks();
        }
        return total;
    }

    public boolean isEmpty() {
        return songs.isEmpty();
    }

    public int songCount() {
        return songs.size();
    }

    public long totalTicks() {
        return totalTicksOf(songs);
    }

    public long remainingTicks() {
        return totalTicks() - playedTicks;
    }

    /** The playlist with its cursor moved, clamped to what the songs are actually worth. */
    public MusicPlaylist withPlayed(long ticks) {
        return new MusicPlaylist(songs, ticks);
    }

    /** Where song {@code index} begins in the playlist. */
    public long startOf(int index) {
        long start = 0L;
        for (int i = 0; i < index && i < songs.size(); i++) {
            start += songs.get(i).ticks();
        }
        return start;
    }

    /** The song the cursor at {@code offset} is inside, or -1 once the playlist is spent. */
    public int indexAt(long offset) {
        if (offset < 0L || offset >= totalTicks()) {
            return -1;
        }
        long cursor = 0L;
        for (int i = 0; i < songs.size(); i++) {
            cursor += songs.get(i).ticks();
            if (offset < cursor) {
                return i;
            }
        }
        return -1;
    }
}

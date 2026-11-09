package cn.dancingsnow.neoecoprototype.blockentity.music;

import appeng.api.inventories.InternalInventory;
import appeng.blockentity.AEBaseBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import cn.dancingsnow.neoecoprototype.items.MusicDataDiscItem;
import cn.dancingsnow.neoecoprototype.items.MusicPlaylist;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSong;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * The machine that fills a data disc. It is the acoustic half of the music key: the jukebox spends a
 * recorded disc, this one writes it.
 *
 * <p>Recording is playback. A disc in one of the twelve input slots is started on the vanilla
 * {@link JukeboxSongPlayer}, so the room hears the song while it is being copied, and only when the song
 * ends normally does its id and frozen length join the data disc's {@link MusicPlaylist}. Pulling either
 * the source disc or the data disc out mid-song stops the audio and writes nothing - a half-heard song is
 * not a song, and a playlist only ever grows on complete plays.
 *
 * <p>The input discs are never consumed; the same disc can be recorded onto any number of data discs. The
 * data disc is capped by {@link MusicPlaylist#MAX_SONGS}, and songs are deduplicated by their jukebox song
 * id, so feeding the same song in twice (or from two discs) records it once.
 */
public final class MusicRecorderBlockEntity extends AEBaseBlockEntity implements InternalInventoryHost {

    /** Twelve distinct input discs at once, the same number one data disc can carry. */
    public static final int DISC_SLOTS = MusicPlaylist.MAX_SONGS;
    public static final int DATA_SLOT = 0;

    private static final String DISCS_TAG = "music_recorder_discs";
    private static final String DATA_TAG = "music_recorder_data_disc";

    private final AppEngInternalInventory discs = new AppEngInternalInventory(
            this, DISC_SLOTS, 1, new PlayableDiscFilter());
    private final AppEngInternalInventory dataDisc = new AppEngInternalInventory(
            this, 1, 1, new DataDiscFilter());
    private final JukeboxSongPlayer songPlayer = new JukeboxSongPlayer(this::setChanged, getBlockPos());

    private int playingSlot = -1;
    private ResourceLocation playingSong;
    private Component playingName;
    private int playingTicks;
    /** What the two involved stacks looked like when the song started, so an outside edit can abort it. */
    private ItemStack playingDisc = ItemStack.EMPTY;
    private ItemStack playingDataDisc = ItemStack.EMPTY;

    public MusicRecorderBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Server ticker, wired through {@code AEBaseEntityBlock.setBlockEntity}. */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MusicRecorderBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    // ==================== the recording run ====================

    private void tickServer(Level level) {
        if (playingSlot >= 0) {
            continueSong(level);
            return;
        }
        startNext(level);
    }

    private void continueSong(Level level) {
        // Anything that changes either stack means the player took something out (or automation swapped
        // it); the song being heard is no longer the one this run started with, so nothing is written.
        if (!ItemStack.matches(discs.getStackInSlot(playingSlot), playingDisc)
                || !ItemStack.matches(dataDisc.getStackInSlot(DATA_SLOT), playingDataDisc)) {
            abort(level);
            return;
        }
        songPlayer.tick(level, getBlockState());
        if (!songPlayer.isPlaying()) {
            finishSong();
        }
    }

    private void startNext(Level level) {
        ItemStack target = dataDisc.getStackInSlot(DATA_SLOT);
        if (target.isEmpty()) {
            return;
        }
        MusicPlaylist playlist = MusicDataDiscItem.playlistOf(target);
        if (playlist.songCount() >= MusicPlaylist.MAX_SONGS) {
            return;
        }
        HolderLookup.Provider registries = level.registryAccess();
        for (int slot = 0; slot < discs.size(); slot++) {
            ItemStack stack = discs.getStackInSlot(slot);
            if (stack.isEmpty()) {
                continue;
            }
            Optional<Holder<JukeboxSong>> found = JukeboxSong.fromStack(registries, stack);
            if (found.isEmpty()) {
                continue;
            }
            Holder<JukeboxSong> song = found.get();
            ResourceLocation id = song.unwrapKey().map(ResourceKey::location).orElse(null);
            if (id == null || contains(playlist, id)) {
                continue;
            }
            playingSlot = slot;
            playingSong = id;
            playingName = song.value().description();
            playingTicks = song.value().lengthInTicks();
            playingDisc = stack.copy();
            playingDataDisc = target.copy();
            songPlayer.play(level, song);
            setChanged();
            return;
        }
    }

    /** The song ran to its end: freeze its id and length onto the disc, then let the next one start. */
    private void finishSong() {
        ItemStack target = dataDisc.getStackInSlot(DATA_SLOT);
        MusicPlaylist playlist = MusicDataDiscItem.playlistOf(target);
        List<MusicPlaylist.Song> songs = new ArrayList<>(playlist.songs());
        songs.add(new MusicPlaylist.Song(playingSong, playingTicks));
        MusicDataDiscItem.setPlaylist(target, new MusicPlaylist(songs, playlist.playedTicks()));
        clearPlaying();
        setChanged();
    }

    private void abort(Level level) {
        if (songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        clearPlaying();
        setChanged();
    }

    private void clearPlaying() {
        playingSlot = -1;
        playingSong = null;
        playingName = null;
        playingTicks = 0;
        playingDisc = ItemStack.EMPTY;
        playingDataDisc = ItemStack.EMPTY;
    }

    /** The line above the cells: which song the room is hearing, or that nothing is playing. */
    public Component currentSongLine() {
        if (playingSlot < 0 || playingName == null) {
            return Component.translatable("gui.neoecoprototype.music_recorder_idle");
        }
        return Component.translatable("gui.neoecoprototype.music_recorder_now_playing", playingName);
    }

    private static boolean contains(MusicPlaylist playlist, ResourceLocation song) {
        for (MusicPlaylist.Song recorded : playlist.songs()) {
            if (recorded.song().equals(song)) {
                return true;
            }
        }
        return false;
    }

    // ==================== inventories and panel state ====================

    public InternalInventory getDiscInventory() {
        return discs;
    }

    public InternalInventory getDataDiscInventory() {
        return dataDisc;
    }

    /** How far the song being recorded has got, 0..1, for the groove on the panel. */
    public float progressFraction() {
        if (playingSlot < 0 || playingTicks <= 0) {
            return 0.0F;
        }
        return Mth.clamp((float) songPlayer.getTicksSinceSongStarted() / playingTicks, 0.0F, 1.0F);
    }

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        setChanged();
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    // ==================== persistence ====================

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        discs.writeToNBT(tag, DISCS_TAG, registries);
        dataDisc.writeToNBT(tag, DATA_TAG, registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        discs.readFromNBT(tag, DISCS_TAG, registries);
        dataDisc.readFromNBT(tag, DATA_TAG, registries);
        // A song in flight is not persisted: the disc on the spindle was never finished, so nothing
        // about it is owed to the data disc after a reload.
        clearPlaying();
    }

    /** The loot table drops the block; the thirteen stacks inside it come from here. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        if (songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        clearPlaying();
        for (AppEngInternalInventory inventory : new AppEngInternalInventory[]{discs, dataDisc}) {
            for (int slot = 0; slot < inventory.size(); slot++) {
                ItemStack stack = inventory.getStackInSlot(slot);
                if (!stack.isEmpty()) {
                    drops.add(stack.copy());
                }
            }
        }
    }

    @Override
    public void setRemoved() {
        Level level = getLevel();
        if (level != null && !level.isClientSide() && songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        clearPlaying();
        super.setRemoved();
    }

    /** Any item the vanilla jukebox could play, which is exactly "has a jukebox song component". */
    private static final class PlayableDiscFilter implements appeng.util.inv.filter.IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            return stack.has(DataComponents.JUKEBOX_PLAYABLE);
        }
    }

    private static final class DataDiscFilter implements appeng.util.inv.filter.IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            return stack.getItem() instanceof MusicDataDiscItem;
        }
    }
}

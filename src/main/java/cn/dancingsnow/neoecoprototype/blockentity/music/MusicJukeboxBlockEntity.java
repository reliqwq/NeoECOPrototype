package cn.dancingsnow.neoecoprototype.blockentity.music;

import appeng.api.networking.IGrid;
import appeng.api.networking.security.IActionSource;
import appeng.api.inventories.InternalInventory;
import appeng.blockentity.grid.AENetworkedBlockEntity;
import appeng.util.inv.AppEngInternalInventory;
import appeng.util.inv.InternalInventoryHost;
import cn.dancingsnow.neoecoprototype.block.music.MusicJukeboxBlock;
import cn.dancingsnow.neoecoprototype.integration.dataenergistics.MusicNetworkFeed;
import cn.dancingsnow.neoecoprototype.items.MusicDataDiscItem;
import cn.dancingsnow.neoecoprototype.items.MusicPlaylist;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.JukeboxSongPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;

/**
 * A jukebox that pays the ME network instead of the air.
 *
 * <p>Playback is vanilla's {@link JukeboxSongPlayer}, so a song is exactly as long as the game says and the
 * client hears it through the same level events a normal jukebox fires - this block has no client half.
 * What is ours is the accounting: every tick of audio that actually came out of the speaker is a tick of
 * music data, delivered to the grid in batches of {@value #DELIVERY_BATCH} and written back onto the disc
 * as {@link MusicPlaylist#playedTicks()}.
 *
 * <p>The cursor only ever moves on <em>delivered</em> ticks. That is what keeps the machine honest in both
 * directions: a network that refuses the insert pauses the song (so the disc cannot be listened to for
 * free), and a disc pulled out mid-song keeps its already-paid ticks when it next starts, because playback
 * resumes at the song the cursor is inside with that song's audio restarted but only its unpaid tail
 * credited.
 */
public final class MusicJukeboxBlockEntity extends AENetworkedBlockEntity implements InternalInventoryHost {

    public static final int DISC_SLOT = 0;

    private static final String DISC_TAG = "music_disc";
    /** Ticks are handed over this many at a time, so a long song is not one insert per tick. */
    private static final int DELIVERY_BATCH = 20;
    /** How long to wait before asking a full network whether it has room again. */
    private static final int RETRY_INTERVAL = 20;
    /** No song has been chosen yet, so the cursor in the playlist decides where to start. */
    private static final int NOT_STARTED = -2;
    /** Nothing left to play, either the disc is spent or a song it names is gone from the pack. */
    private static final int SPENT = -1;

    private final AppEngInternalInventory disc = new AppEngInternalInventory(this, 1, 1, new DiscFilter());
    private final JukeboxSongPlayer songPlayer = new JukeboxSongPlayer(this::setChanged, getBlockPos());
    private final IActionSource actionSource = IActionSource.ofMachine(this);

    /** Heard but not yet handed to the network; never persisted, because it is not yet paid. */
    private long pendingTicks;
    /** How much of the current song has been counted into {@link #pendingTicks}. */
    private long countedForSong;
    /** Ticks of the current song that were already paid before this playback started. */
    private long skipTicks;
    private int currentSong = NOT_STARTED;
    /** The network had no room, so the song is stopped and the disc is not being spent. */
    private boolean waitingForStorage;
    private int retryTimer;

    public MusicJukeboxBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    /** Server ticker, wired through {@code AEBaseEntityBlock.setBlockEntity}. */
    public static void serverTick(Level level, BlockPos pos, BlockState state,
                                  MusicJukeboxBlockEntity blockEntity) {
        blockEntity.tickServer(level);
    }

    // ==================== the run ====================

    private void tickServer(Level level) {
        ItemStack stack = disc.getStackInSlot(DISC_SLOT);
        boolean present = !stack.isEmpty();
        syncHasRecord(level, present);
        if (!present) {
            stopAudio(level);
            pendingTicks = 0L;
            return;
        }
        MusicPlaylist playlist = MusicDataDiscItem.playlistOf(stack);
        if (playlist.isEmpty() || playlist.remainingTicks() <= 0L) {
            stopAudio(level);
            pendingTicks = 0L;
            return;
        }

        // Without Data Energistics there is no music key to pay, so the block behaves like a plain
        // jukebox: it plays the disc and spends none of it.
        if (!MusicNetworkFeed.loaded()) {
            waitingForStorage = false;
            playFreely(level);
            return;
        }
        if (waitingForStorage && !retryStorage()) {
            return;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            pause(level);
            return;
        }
        playPaid(level, grid);
    }

    private void playFreely(Level level) {
        if (!songPlayer.isPlaying()) {
            advance(level);
        }
        if (songPlayer.isPlaying()) {
            songPlayer.tick(level, getBlockState());
        }
    }

    private void playPaid(Level level, IGrid grid) {
        if (!songPlayer.isPlaying()) {
            if (currentSong == SPENT) {
                return;
            }
            if (!flush()) {
                pause(level);
                return;
            }
            advance(level);
            return;
        }

        MusicPlaylist playlist = MusicDataDiscItem.playlistOf(disc.getStackInSlot(DISC_SLOT));
        MusicPlaylist.Song song = currentSong >= 0 && currentSong < playlist.songCount()
                ? playlist.songs().get(currentSong)
                : null;
        long before = songPlayer.getTicksSinceSongStarted();
        songPlayer.tick(level, getBlockState());
        boolean ended = !songPlayer.isPlaying();

        if (song != null) {
            long elapsed = before + 1L;
            long heard = Math.min(Math.max(0L, elapsed - skipTicks),
                    Math.max(0L, song.ticks() - skipTicks));
            long delta = heard - countedForSong;
            if (delta > 0L) {
                pendingTicks += delta;
                countedForSong = heard;
            }
        }

        if (ended) {
            if (!flush()) {
                pause(level);
                return;
            }
            advance(level);
        } else if (pendingTicks >= DELIVERY_BATCH) {
            if (!flush()) {
                pause(level);
            }
        }
    }

    /** Picks the next song and starts its audio, skipping past the part the cursor already paid for. */
    private void advance(Level level) {
        if (currentSong == SPENT) {
            return;
        }
        MusicPlaylist playlist = MusicDataDiscItem.playlistOf(disc.getStackInSlot(DISC_SLOT));
        if (playlist.isEmpty()) {
            currentSong = SPENT;
            return;
        }
        if (currentSong == NOT_STARTED) {
            int index = playlist.indexAt(playlist.playedTicks());
            if (index < 0) {
                currentSong = SPENT;
                return;
            }
            currentSong = index;
            skipTicks = Math.max(0L, playlist.playedTicks() - playlist.startOf(index));
        } else {
            currentSong++;
            skipTicks = 0L;
            if (currentSong >= playlist.songCount()) {
                currentSong = SPENT;
                return;
            }
        }
        countedForSong = 0L;
        if (!startSong(level, playlist.songs().get(currentSong))) {
            // A song the pack no longer carries cannot be heard, so the disc stops here instead of
            // burning the ticks it would have paid for something nobody played.
            currentSong = SPENT;
        }
    }

    private boolean startSong(Level level, MusicPlaylist.Song song) {
        var songs = level.registryAccess().registryOrThrow(Registries.JUKEBOX_SONG);
        var holder = songs.getHolder(ResourceKey.create(Registries.JUKEBOX_SONG, song.song()));
        if (holder.isEmpty()) {
            return false;
        }
        songPlayer.play(level, holder.get());
        return true;
    }

    /**
     * Hands whatever has been heard to the grid and moves the disc's cursor by exactly what was accepted.
     * Answers whether everything went in; a partial insert leaves the rest unpaid and the caller pauses.
     */
    private boolean flush() {
        if (pendingTicks <= 0L) {
            return true;
        }
        IGrid grid = getMainNode().getGrid();
        if (grid == null) {
            return false;
        }
        long accepted = MusicNetworkFeed.insert(grid, pendingTicks, actionSource);
        if (accepted > 0L) {
            ItemStack stack = disc.getStackInSlot(DISC_SLOT);
            MusicPlaylist playlist = MusicDataDiscItem.playlistOf(stack);
            MusicDataDiscItem.setPlaylist(stack, playlist.withPlayed(playlist.playedTicks() + accepted));
            pendingTicks -= accepted;
            setChanged();
        }
        return pendingTicks <= 0L;
    }

    /** Stops the audio and forgets the unpaid tail; the cursor in the disc decides where to resume. */
    private void pause(Level level) {
        stopAudio(level);
        pendingTicks = 0L;
        waitingForStorage = true;
        retryTimer = RETRY_INTERVAL;
    }

    private boolean retryStorage() {
        if (--retryTimer > 0) {
            return false;
        }
        retryTimer = RETRY_INTERVAL;
        IGrid grid = getMainNode().getGrid();
        if (grid == null || !MusicNetworkFeed.accepts(grid, actionSource)) {
            return false;
        }
        waitingForStorage = false;
        return true;
    }

    private void stopAudio(Level level) {
        if (songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        resetCursor();
    }

    private void resetCursor() {
        currentSong = NOT_STARTED;
        skipTicks = 0L;
        countedForSong = 0L;
    }

    // ==================== the slot ====================

    /** The spindle itself, so the disc can be read without reaching into the block entity's fields. */
    public InternalInventory getInternalInventory() {
        return disc;
    }

    /** Puts one disc on the spindle, handing back whatever was there first. */
    public boolean insertDisc(ItemStack held, Player player) {
        Level level = getLevel();
        if (level == null || level.isClientSide() || held.isEmpty()
                || !(held.getItem() instanceof MusicDataDiscItem)) {
            return false;
        }
        ejectDisc();
        disc.setItemDirect(DISC_SLOT, held.copyWithCount(1));
        held.consume(1, player);
        resetCursor();
        pendingTicks = 0L;
        waitingForStorage = false;
        setChanged();
        syncHasRecord(level, true);
        return true;
    }

    /** Flushes what was already heard, stops the song and puts the disc on the ground. */
    public void ejectDisc() {
        Level level = getLevel();
        if (level == null || level.isClientSide()) {
            return;
        }
        ItemStack stack = disc.getStackInSlot(DISC_SLOT);
        if (stack.isEmpty()) {
            return;
        }
        flush();
        pendingTicks = 0L;
        stopAudio(level);
        ItemStack dropped = stack.copy();
        disc.setItemDirect(DISC_SLOT, ItemStack.EMPTY);
        waitingForStorage = false;
        Block.popResource(level, getBlockPos(), dropped);
        setChanged();
        syncHasRecord(level, false);
    }

    private void syncHasRecord(Level level, boolean present) {
        BlockState state = getBlockState();
        if (!state.hasProperty(MusicJukeboxBlock.HAS_RECORD)
                || state.getValue(MusicJukeboxBlock.HAS_RECORD) == present) {
            return;
        }
        level.setBlock(getBlockPos(), state.setValue(MusicJukeboxBlock.HAS_RECORD, present), Block.UPDATE_ALL);
    }

    // ==================== persistence ====================

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        disc.writeToNBT(tag, DISC_TAG, registries);
    }

    @Override
    public void loadTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadTag(tag, registries);
        disc.readFromNBT(tag, DISC_TAG, registries);
        pendingTicks = 0L;
        waitingForStorage = false;
        retryTimer = 0;
        resetCursor();
    }

    /** The loot table drops the block itself; the disc on the spindle comes from here. */
    @Override
    public void addAdditionalDrops(Level level, BlockPos pos, List<ItemStack> drops) {
        super.addAdditionalDrops(level, pos, drops);
        ItemStack stack = disc.getStackInSlot(DISC_SLOT);
        if (stack.isEmpty()) {
            return;
        }
        flush();
        pendingTicks = 0L;
        if (songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        drops.add(stack.copy());
        disc.setItemDirect(DISC_SLOT, ItemStack.EMPTY);
    }

    @Override
    public void setRemoved() {
        Level level = getLevel();
        if (level != null && !level.isClientSide() && songPlayer.isPlaying()) {
            songPlayer.stop(level, getBlockState());
        }
        super.setRemoved();
    }

    // ==================== inventory host ====================

    @Override
    public void saveChangedInventory(AppEngInternalInventory inventory) {
        setChanged();
    }

    @Override
    public boolean isClientSide() {
        return level != null && level.isClientSide();
    }

    /** The one slot takes data discs and nothing else, so a hopper cannot feed the machine. */
    private static final class DiscFilter implements appeng.util.inv.filter.IAEItemFilter {
        @Override
        public boolean allowInsert(InternalInventory inventory, int slot, ItemStack stack) {
            return stack.getItem() instanceof MusicDataDiscItem;
        }
    }
}

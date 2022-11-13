package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.player.LavaPlayerManager;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;
import me.yamayaki.musicbot.audio.entities.TrackInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class PlaylistManager {
    private final TrackManager trackManager;
    private final List<AudioTrack> trackList;

    private AudioTrack currentTrack = null;

    public boolean loop = false;

    public PlaylistManager(TrackManager trackManager) {
        this.trackManager = trackManager;
        this.trackList = new ArrayList<>();

        try {
            this.restore();
        } catch (ExecutionException | InterruptedException e) {
            MusicBot.LOGGER.error("failed to restore playlist:", e);
        }
    }

    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    public void addTrack(AudioTrack track) {
        if (track == null) {
            return;
        }

        this.trackList.add(track);
    }

    public List<AudioTrack> getTracks(boolean inclCurrent) {
        final ArrayList<AudioTrack> list = new ArrayList<>();

        if (inclCurrent && this.currentTrack != null) {
            list.add(this.currentTrack);
        }

        list.addAll(this.trackList);

        return list;
    }

    public void clearList() {
        this.trackList.clear();
    }

    public boolean hasNext() {
        return this.trackList.size() > 0 || (this.currentTrack != null && this.loop);
    }

    public AudioTrack getNext() {
        if (!this.hasNext()) {
            return null;
        }

        if (this.loop) {
            this.trackList.add(this.currentTrack.makeClone());
        }

        this.currentTrack = this.trackList.get(0);
        this.trackList.remove(0);

        return this.currentTrack;
    }

    public boolean toggleRepeat() {
        this.loop = !this.loop;
        return this.loop;
    }

    public void restore() throws ExecutionException, InterruptedException {
        var response = MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .getValue(this.trackManager.getServerId());

        if (response.isEmpty()) {
            return;
        }

        for (TrackInfo trackInfo : response.get()) {
            LavaPlayerManager.getPlayerManager().loadItem(trackInfo.uri, new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    track.setPosition(trackInfo.position);
                    trackList.add(track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    trackList.addAll(playlist.getTracks());
                }

                @Override
                public void noMatches() {
                    //do nothing
                }

                @Override
                public void loadFailed(FriendlyException exception) {
                    MusicBot.LOGGER.error(exception);
                }
            }).get();
        }

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .deleteValue(this.trackManager.getServerId());
    }

    public void store() {
        final List<TrackInfo> ids = new ArrayList<>();

        //add currently playing track
        ids.add(TrackInfo.of(this.getCurrentTrack()));

        //add all remaining songs
        trackList.forEach(track -> ids.add(TrackInfo.of(track)));

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .putValue(this.trackManager.getServerId(), ids.toArray(TrackInfo[]::new));
    }
}

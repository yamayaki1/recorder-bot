package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.entities.TrackInfo;
import me.yamayaki.musicbot.audio.handler.LoadResultHandler;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ExecutionException;

public class PlaylistManager {
    private final Long serverID;
    private final Queue<AudioTrack> trackList;

    private AudioTrack currentTrack = null;

    public boolean loop = false;

    public PlaylistManager(Long serverID) {
        this.serverID = serverID;
        this.trackList = new ArrayDeque<>();

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

        this.currentTrack = this.trackList.poll();
        return this.currentTrack;
    }

    public boolean toggleRepeat() {
        this.loop = !this.loop;
        return this.loop;
    }

    public void restore() throws ExecutionException, InterruptedException {
        var response = MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .getValue(this.serverID);

        if (response.isEmpty()) {
            return;
        }

        for (TrackInfo trackInfo : response.get()) {
            LavaManager.loadTrack(trackInfo.uri, new LoadResultHandler(this, trackInfo.position, null)).get();
        }

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .deleteValue(this.serverID);
    }

    public void store() {
        final List<TrackInfo> ids = new ArrayList<>();

        //add currently playing track
        ids.add(TrackInfo.of(this.getCurrentTrack()));

        //add all remaining songs
        trackList.forEach(track -> ids.add(TrackInfo.of(track)));

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .putValue(this.serverID, ids.toArray(TrackInfo[]::new));
    }
}

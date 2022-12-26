package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.TrackInfo;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.audio.player.LoadResultHandler;
import me.yamayaki.musicbot.storage.database.specs.impl.CacheSpecs;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

public class PlaylistManager {
    private final Long serverID;
    private final Queue<AudioTrack> trackList;

    private AudioTrack currentTrack = null;

    private boolean loop = false;

    public PlaylistManager(Long serverID) {
        this.serverID = serverID;
        this.trackList = new LinkedBlockingQueue<>();

        this.restore();
    }

    public AudioTrack current() {
        return this.currentTrack;
    }

    public void add(AudioTrack track) {
        if (track == null) {
            return;
        }

        this.trackList.add(track);
    }

    public List<AudioTrack> getTracks(boolean inclCurrent) {
        final ArrayList<AudioTrack> list = new ArrayList<>(this.trackList);

        if (inclCurrent && this.currentTrack != null) {
            list.add(this.currentTrack);
        }

        return list;
    }

    public void clear() {
        this.trackList.clear();
    }

    public boolean hasNext() {
        return this.trackList.size() > 0 || (this.currentTrack != null && this.loop);
    }

    public AudioTrack next() {
        if (!this.hasNext()) {
            return null;
        }

        if (this.loop && this.currentTrack != null) {
            this.trackList.add(this.currentTrack.makeClone());
        }

        this.currentTrack = this.trackList.poll();
        return this.currentTrack;
    }

    public AudioTrack peekNext() {
        if (!this.hasNext()) {
            return null;
        }

        return this.trackList.peek();
    }

    public boolean toggleRepeat() {
        this.loop = !this.loop;
        return this.loop;
    }

    public void restore() {
        var response = MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .getValue(this.serverID);

        if (response.isEmpty()) {
            return;
        }

        for (TrackInfo trackInfo : response.get()) {
            try {
                LavaManager.loadTrack(trackInfo.uri(), new LoadResultHandler(this, trackInfo.position(), null)).get();
            } catch (Exception e) {
                MusicBot.LOGGER.error("couldn't load track {}: {}", trackInfo, e);
            }
        }

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .deleteValue(this.serverID);
    }

    public void store() {
        TrackInfo[] trackList = this.getTracks(true)
                .stream()
                .map(TrackInfo::of)
                .toArray(TrackInfo[]::new);

        MusicBot.DATABASE
                .getDatabase(CacheSpecs.PLAYLIST_CACHE)
                .putValue(this.serverID, trackList);
    }
}

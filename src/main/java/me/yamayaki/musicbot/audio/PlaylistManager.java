package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.audio.player.LoadResultHandler;
import me.yamayaki.musicbot.entities.TrackInfo;
import me.yamayaki.musicbot.storage.database.specs.impl.CacheSpecs;

import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private final Long serverID;

    private final List<AudioTrack> scheduledTracks;
    private int selectedTrack;

    public PlaylistManager(Long serverID) {
        this.serverID = serverID;

        this.scheduledTracks = new ArrayList<>();
        this.selectedTrack = -1;

        this.restore();
    }

    public AudioTrack previous(boolean select) {
        final int position = Math.max(this.selectedTrack - 1, 0);

        if (position > this.scheduledTracks.size() - 1) {
            return null;
        }

        if (select) {
            this.selectedTrack--;
        }

        return this.scheduledTracks.get(position).makeClone();
    }

    public AudioTrack current() {
        final int position = Math.max(this.selectedTrack, 0);

        if (position > this.scheduledTracks.size() - 1) {
            return null;
        }

        return this.scheduledTracks.get(position).makeClone();
    }

    public AudioTrack next(boolean select) {
        final int position = Math.max(this.selectedTrack + 1, 0);

        if (position > this.scheduledTracks.size() - 1) {
            return null;
        }

        if (select) {
            this.selectedTrack++;
        }

        return this.scheduledTracks.get(position).makeClone();
    }

    public void add(AudioTrack track) {
        if (track == null) {
            return;
        }

        this.scheduledTracks.add(track);
    }

    public List<AudioTrack> getTracks(boolean inclCurrent) {
        final int from = Math.max(0, inclCurrent ? this.selectedTrack : this.selectedTrack + 1);
        final int to = this.scheduledTracks.size();

        MusicBot.LOGGER.debug("({}): {}, {}", this.scheduledTracks.size(), from, to);

        return List.copyOf(this.scheduledTracks)
                .subList(from, to);
    }

    public void clear() {
        this.selectedTrack = -1;
        this.scheduledTracks.clear();
    }

    public List<AudioTrack> __dEntireList() {
        return scheduledTracks;
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
                LavaManager.loadTrack(trackInfo.uri(), new LoadResultHandler(this, trackInfo, null)).get();
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

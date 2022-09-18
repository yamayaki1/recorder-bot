package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.DatabaseInstance;
import me.yamayaki.musicbot.database.RocksManager;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;

public class TrackCache {
    private final RocksManager cacheInstance;

    public TrackCache(RocksManager rocksManager) {
        this.cacheInstance = rocksManager;
    }

    public DatabaseInstance<String, SpotifyTrack> getTrackCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.SPOTIFY_CACHE);
    }

    public DatabaseInstance<String, String> getYoutubeCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.YOUTUBE_CACHE);
    }

    public DatabaseInstance<Long, String[]> getPlaylistCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.PLAYLIST_CACHE);
    }
}

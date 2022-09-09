package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.DatabaseInstance;
import me.yamayaki.musicbot.database.specs.CacheSpecs;
import org.rocksdb.RocksDBException;

import java.io.File;

public class TrackCache {
    private final DatabaseInstance<String, SpotifyTrack> spotifyCache;
    private final DatabaseInstance<String, String> youtubeCache;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public TrackCache(File cachePath) {
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }

        this.spotifyCache = new DatabaseInstance<>(new File(cachePath, "spotify"), CacheSpecs.SPOTIFY_CACHE);
        this.youtubeCache = new DatabaseInstance<>(new File(cachePath, "youtube"), CacheSpecs.YOUTUBE_CACHE);
    }

    public DatabaseInstance<String, SpotifyTrack> getSpotifyCache() {
        return spotifyCache;
    }

    public DatabaseInstance<String, String> getYoutubeCache() {
        return youtubeCache;
    }

    public void shutdown() throws RocksDBException {
        this.spotifyCache.close();
        this.youtubeCache.close();
    }
}

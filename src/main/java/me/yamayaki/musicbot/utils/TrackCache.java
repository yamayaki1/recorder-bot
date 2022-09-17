package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.DatabaseInstance;
import me.yamayaki.musicbot.database.RocksManager;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import me.yamayaki.musicbot.database.specs.impl.CacheSpecs;
import org.rocksdb.RocksDBException;

import java.io.File;

public class TrackCache {
    private final RocksManager cacheInstance;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public TrackCache(File cachePath) {
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }

        this.cacheInstance = new RocksManager(cachePath, new DatabaseSpec[]{
                CacheSpecs.SPOTIFY_CACHE,
                CacheSpecs.YOUTUBE_CACHE,
                CacheSpecs.PLAYLIST_CACHE
        });
    }

    public DatabaseInstance<String, SpotifyTrack> getSpotifyCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.SPOTIFY_CACHE);
    }

    public DatabaseInstance<String, String> getYoutubeCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.YOUTUBE_CACHE);
    }

    public DatabaseInstance<Long, String[]> getPlaylistCache() {
        return this.cacheInstance.getDatabase(CacheSpecs.PLAYLIST_CACHE);
    }

    public void shutdown() throws RocksDBException {
        this.cacheInstance.close();
    }
}

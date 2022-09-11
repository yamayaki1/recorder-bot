package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.DatabaseInstance;
import me.yamayaki.musicbot.database.specs.CacheSpecs;
import org.rocksdb.RocksDBException;

import java.io.File;

public class TrackCache {
    private final DatabaseInstance<String, SpotifyTrack> spotifyCache;
    private final DatabaseInstance<String, String> youtubeCache;
    private final DatabaseInstance<Long, String[]> playlistCache;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public TrackCache(File cachePath) {
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }

        this.spotifyCache = new DatabaseInstance<>(new File(cachePath, "spotify"), CacheSpecs.SPOTIFY_CACHE);
        this.youtubeCache = new DatabaseInstance<>(new File(cachePath, "youtube"), CacheSpecs.YOUTUBE_CACHE);
        this.playlistCache = new DatabaseInstance<>(new File(cachePath, "playlist"), CacheSpecs.PLAYLIST_CACHE);
    }

    public DatabaseInstance<String, SpotifyTrack> getSpotifyCache() {
        return spotifyCache;
    }

    public DatabaseInstance<String, String> getYoutubeCache() {
        return youtubeCache;
    }

    public DatabaseInstance<Long, String[]> getPlaylistCache() {
        return playlistCache;
    }

    public void shutdown() throws RocksDBException {
        this.spotifyCache.close();
        this.youtubeCache.close();
        this.playlistCache.close();
    }
}

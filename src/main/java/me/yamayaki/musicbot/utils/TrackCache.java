package me.yamayaki.musicbot.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.internal.LinkedTreeMap;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

//TODO maybe use something like LMDB or RocksDB?
public class TrackCache {
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Path cacheFile;

    private CacheInstance cacheInstance;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public TrackCache(File cachePath) {
        if (!cachePath.exists()) {
            cachePath.mkdirs();
        }

        this.cacheFile = new File(cachePath, "tracks.json").toPath();
        this.loadFile();
    }

    private void loadFile() {
        try {
            this.cacheInstance = this.gson.fromJson(new String(Files.readAllBytes(this.cacheFile)), CacheInstance.class);
        } catch (IOException e) {
            this.cacheInstance = new CacheInstance();
            this.saveFile();
            MusicBot.LOGGER.error("error reading track-cache file, generating new one", e);
        }
    }

    public void saveFile() {
        try {
            Files.writeString(this.cacheFile, this.gson.toJson(this.cacheInstance, CacheInstance.class));
        } catch (IOException e) {
            MusicBot.LOGGER.error("error writing track-cache file", e);
        }
    }

    public CacheInstance getCacheInstance() {
        return cacheInstance;
    }

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
    public static class CacheInstance {
        private Cache<String> youTubeCache = new Cache<>();
        private Cache<SpotifyTrack> spotifyCache = new Cache<>();

        public Cache<SpotifyTrack> getSpotifyCache() {
            return spotifyCache;
        }

        public Cache<String> getYouTubeCache() {
            return youTubeCache;
        }
    }

    @SuppressWarnings({"CanBeFinal", "FieldMayBeFinal", "FieldCanBeLocal"})
    public static class Cache<T> {
        private LinkedTreeMap<String, T> linkedTreeMap = new LinkedTreeMap<>();

        public Optional<T> fromCache(String trackId) {
            return this.linkedTreeMap.containsKey(trackId) ? Optional.of(this.linkedTreeMap.get(trackId)) : Optional.empty();
        }

        public void toCache(String trackId, T spotifyTrack) {
            this.linkedTreeMap.put(trackId, spotifyTrack);
        }
    }
}

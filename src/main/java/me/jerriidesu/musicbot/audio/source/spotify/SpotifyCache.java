package me.jerriidesu.musicbot.audio.source.spotify;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import me.jerriidesu.musicbot.MusicBot;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class SpotifyCache {
    private final Type mapType = new TypeToken<Map<String, SpotifyTrack>>() {
    }.getType();
    private final Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
    private final Path cacheFile;

    private HashMap<String, SpotifyTrack> idTrackMap;
    private boolean dirty = false;

    @SuppressWarnings("ResultOfMethodCallIgnored")
    public SpotifyCache(File cacheFile) {
        if (!cacheFile.exists()) {
            cacheFile.mkdirs();
        }

        this.cacheFile = cacheFile.toPath();
        this.loadFile();
    }

    private void loadFile() {
        try {
            this.idTrackMap = this.gson.fromJson(new String(Files.readAllBytes(this.cacheFile)), this.mapType);
        } catch (IOException e) {
            MusicBot.getLogger().error("error reading spotify-cache file, generating new one", e);
        }
    }

    public void saveFile() {
        if (!this.dirty) {
            return;
        }

        try {
            Files.writeString(this.cacheFile, this.gson.toJson(this.idTrackMap, this.mapType));
        } catch (IOException e) {
            MusicBot.getLogger().error("error writing spotify-cache file", e);
        }
    }

    public Optional<SpotifyTrack> fromCache(String trackId) {
        return this.idTrackMap.containsKey(trackId) ? Optional.of(this.idTrackMap.get(trackId)) : Optional.empty();
    }

    public void toCache(String trackId, SpotifyTrack spotifyTrack) {
        this.idTrackMap.put(trackId, spotifyTrack);
        this.dirty = true;
    }
}

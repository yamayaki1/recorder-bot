package me.yamayaki.musicbot.database.specs;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.DatabaseSpec;

public class CacheSpecs {
    public static final DatabaseSpec<String, SpotifyTrack> SPOTIFY_CACHE =
            new DatabaseSpec<>(String.class, SpotifyTrack.class);

    public static final DatabaseSpec<String, String> YOUTUBE_CACHE =
            new DatabaseSpec<>(String.class, String.class);

    public static final DatabaseSpec<Long, String[]> PLAYLIST_CACHE =
            new DatabaseSpec<>(Long.class, String[].class);
}

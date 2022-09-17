package me.yamayaki.musicbot.database.specs.impl;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;

public class CacheSpecs {
    public static final DatabaseSpec<String, SpotifyTrack> SPOTIFY_CACHE =
            new DatabaseSpec<>(0, String.class, SpotifyTrack.class);

    public static final DatabaseSpec<String, String> YOUTUBE_CACHE =
            new DatabaseSpec<>(1, String.class, String.class);

    public static final DatabaseSpec<Long, String[]> PLAYLIST_CACHE =
            new DatabaseSpec<>(2, Long.class, String[].class);
}

package me.yamayaki.musicbot.database.specs.impl;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.specs.DatabaseSpec;

public class CacheSpecs {
    public static final DatabaseSpec<String, SpotifyTrack> SPOTIFY_CACHE =
            new DatabaseSpec<>("spotify_cache", String.class, SpotifyTrack.class);

    public static final DatabaseSpec<String, String> YOUTUBE_CACHE =
            new DatabaseSpec<>("youtube_cache", String.class, String.class);

    public static final DatabaseSpec<Long, String[]> PLAYLIST_CACHE =
            new DatabaseSpec<>("playlist_cache", Long.class, String[].class);
}

package me.jerriidesu.musicbot.audio.source.spotify;

import java.util.regex.Pattern;

public class SpotifyPatterns {
    public static final Pattern SPOTIFY_PATTERN = Pattern.compile("(https?://)?(.*)?spotify\\.com.*");

    public static final Pattern TRACK_PATTERN = Pattern.compile("/tracks?/([^?/\\s]*)");
    public static final Pattern PLAYLIST_PATTERN = Pattern.compile("/playlists?/([^?/\\s]*)");
    public static final Pattern ALBUM_PATTERN = Pattern.compile("/albums?/([^?/\\s]*)");
}

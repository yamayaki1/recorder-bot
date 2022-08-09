package me.jerriidesu.musicbot.audio.source.spotify;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;
import java.util.Locale;

public class SpotifyTrack {

    private final String name;
    private final String artist;

    private final String name_lower;
    private final String artist_lower;

    public SpotifyTrack(Track item) {
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();

        this.name_lower = this.name.toLowerCase(Locale.ROOT);
        this.artist_lower = this.artist.toLowerCase(Locale.ROOT);
    }

    public SpotifyTrack(TrackSimplified item) {
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();
        this.name_lower = this.name.toLowerCase(Locale.ROOT);
        this.artist_lower = this.artist.toLowerCase(Locale.ROOT);
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getNameLower() {
        return name_lower;
    }

    public String getArtistLower() {
        return artist_lower;
    }
}

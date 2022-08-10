package me.jerriidesu.musicbot.audio.source.spotify;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;
import java.util.Locale;

public class SpotifyTrack {

    private final String identifier;
    private final String name;
    private final String artist;


    public SpotifyTrack(Track item) {
        this.identifier = item.getId();
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();
    }

    public SpotifyTrack(TrackSimplified item) {
        this.identifier = item.getId();
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }

    public String getNameLower() {
        return name.toLowerCase(Locale.ROOT);
    }

    public String getArtistLower() {
        return artist.toLowerCase(Locale.ROOT);
    }
}

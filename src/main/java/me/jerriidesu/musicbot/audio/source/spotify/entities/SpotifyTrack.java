package me.jerriidesu.musicbot.audio.source.spotify.entities;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.util.Arrays;

public class SpotifyTrack {

    private final String name;
    private final String artist;

    public SpotifyTrack(Track item) {
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();
    }

    public SpotifyTrack(TrackSimplified item) {
        this.name = item.getName();
        this.artist = Arrays.stream(item.getArtists()).toList().get(0).getName();
    }

    public String getName() {
        return name;
    }

    public String getArtist() {
        return artist;
    }
}

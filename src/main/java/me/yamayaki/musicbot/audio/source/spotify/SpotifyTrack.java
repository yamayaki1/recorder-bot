package me.yamayaki.musicbot.audio.source.spotify;

import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

public class SpotifyTrack {

    private final String identifier;
    private final String name;
    private final String artist;


    public SpotifyTrack(Track item) {
        this.identifier = item.getId();
        this.name = item.getName();
        this.artist = item.getArtists()[0].getName();
    }

    public SpotifyTrack(TrackSimplified item) {
        this.identifier = item.getId();
        this.name = item.getName();
        this.artist = item.getArtists()[0].getName();
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

    @Override
    public String toString() {
        return "SpotifyTrack{" + "identifier='" + identifier + ", name='" + name + ", artist='" + artist + '}';
    }
}

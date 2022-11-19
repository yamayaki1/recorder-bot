package me.yamayaki.musicbot.audio.source.spotify;

import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;

public class SpotifyTrack {
    private final String identifier;
    private final String name;
    private final String image;
    private final String artist;


    public SpotifyTrack(Track item) {
        this.identifier = item.getId();
        this.name = item.getName();
        this.image = getOptimalImage(item.getAlbum().getImages());
        this.artist = item.getArtists()[0].getName();
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public String getArtist() {
        return artist;
    }

    private String getOptimalImage(Image[] images) {
        int index = 0;
        int width = 0;

        for (int i = 0; i < images.length; i++) {
            Image image = images[i];

            if (image.getWidth() > width) {
                index = i;
            }
        }

        return images[index].getUrl();
    }

    @Override
    public String toString() {
        return "SpotifyTrack{" +
                "identifier='" + identifier + '\'' +
                ", name='" + name + '\'' +
                ", image='" + image + '\'' +
                ", artist='" + artist + '\'' +
                '}';
    }
}

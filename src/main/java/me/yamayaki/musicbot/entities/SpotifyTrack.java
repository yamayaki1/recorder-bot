package me.yamayaki.musicbot.entities;

import se.michaelthelin.spotify.model_objects.specification.Image;
import se.michaelthelin.spotify.model_objects.specification.Track;

public record SpotifyTrack(String identifier, String name, String image, String artist) {
    public static SpotifyTrack of(Track track) {
        return new SpotifyTrack(track.getId(), track.getName(), getBiggestImage(track.getAlbum().getImages()), track.getArtists()[0].getName());
    }

    private static String getBiggestImage(Image[] images) {
        int index = 0;
        int width = 0;

        for (int i = 0; i < images.length; i++) {
            Image image = images[i];

            if (image.getWidth() > width) {
                width = image.getWidth();
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

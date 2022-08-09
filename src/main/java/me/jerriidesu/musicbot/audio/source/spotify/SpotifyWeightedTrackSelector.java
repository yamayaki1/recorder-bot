package me.jerriidesu.musicbot.audio.source.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.utils.Either;

import java.util.List;
import java.util.Locale;

public class SpotifyWeightedTrackSelector {
    private final SpotifyTrack spotifyTrack;
    private final List<AudioTrack> youtubeTracks;

    private int highestScore = -5000;
    private boolean perfectMatch = false;

    private AudioTrack selectedTrack = null;

    public SpotifyWeightedTrackSelector(SpotifyTrack spotifyTrack, List<AudioTrack> youtubeTracks) {
        this.spotifyTrack = spotifyTrack;
        this.youtubeTracks = youtubeTracks;
    }

    public static Either<AudioTrack, Boolean> getWeightedTrack(SpotifyTrack spotifyTrack, List<AudioTrack> youtubeTracks) {
        SpotifyWeightedTrackSelector spotifyWeightedTrackSelector = new SpotifyWeightedTrackSelector(spotifyTrack, youtubeTracks);
        spotifyWeightedTrackSelector.runSelector();

        return new Either<>(spotifyWeightedTrackSelector.getSelectedTrack(), spotifyWeightedTrackSelector.isPerfectMatch());
    }

    private Boolean isPerfectMatch() {
        return this.perfectMatch;
    }

    private AudioTrack getSelectedTrack() {
        return this.selectedTrack;
    }

    private void runSelector() {
        for (AudioTrack youtubeTrack : this.youtubeTracks) {
            if(this.perfectMatch) {
                continue;
            }

            String spotTitle = spotifyTrack.getName().toLowerCase(Locale.ROOT);
            String spotAuthor = spotifyTrack.getArtist().toLowerCase(Locale.ROOT);

            String ytTitle = youtubeTrack.getInfo().title.toLowerCase(Locale.ROOT);
            String ytAuthor = youtubeTrack.getInfo().author.toLowerCase(Locale.ROOT);

            int score = 0;

            if (ytTitle.equals(spotTitle) && ytAuthor.equals(spotAuthor)) {
                this.perfectMatch = true;
                score = score + 5000;
            }

            if (ytAuthor.equals(spotAuthor)) {
                score = score + 5;
            } else if (ytAuthor.contains(spotAuthor)) {
                score = score + 3;
            } else {
                score = score - 20;
            }

            if (ytTitle.equals(spotTitle)) {
                score = score + 5;
            } else {
                score = score - 3;
            }

            if (ytTitle.contains(spotTitle)) {
                score = score + 1;
            } else {
                score = score - 1;
            }

            if (this.highestScore < score) {
                this.selectedTrack = youtubeTrack;
                this.highestScore = score;
            }

            if(MusicBot.DEBUG) {
                MusicBot.getLogger().info("{}: {} ({}) - {} ({})", score, ytTitle, spotTitle, ytAuthor, spotAuthor);
            }
        }
    }
}

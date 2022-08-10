package me.jerriidesu.musicbot.audio.source.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.utils.Either;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;

public class SpotifyWeightedTrackSelector {
    private final SpotifyTrack spotifyTrack;
    private final List<AudioTrack> youtubeTracks;

    private int highestScore = -5000;
    private boolean perfectMatch = false;

    private boolean authorExists = false;
    private boolean titleExists = false;

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
            if (this.perfectMatch) {
                continue;
            }

            String ytTitle = youtubeTrack.getInfo().title.toLowerCase(Locale.ROOT);
            String ytAuthor = youtubeTrack.getInfo().author.toLowerCase(Locale.ROOT);

            //early elimination
            if (ytTitle.equals(spotifyTrack.getNameLower()) && ytAuthor.equals(spotifyTrack.getArtistLower())) {
                this.titleExists = true;  // normally unneeded, but we set it anyway
                this.authorExists = true; // normally unneeded, but we set it anyway
                this.perfectMatch = true;
                this.highestScore = 5000;
                this.selectedTrack = youtubeTrack;

                continue;
            }

            AtomicReference<Integer> score = new AtomicReference<>(0);

            this.checkAuthor(spotifyTrack.getArtistLower(), ytAuthor, score);
            this.checkTitle(spotifyTrack.getNameLower(), ytTitle, score);

            if (this.highestScore < score.get()) {
                this.selectedTrack = youtubeTrack;
                this.highestScore = score.get();
            }

            if (MusicBot.DEBUG) {
                MusicBot.getLogger().info("{}: {} ({}) - {} ({})", score, ytTitle, spotifyTrack.getNameLower(), ytAuthor, spotifyTrack.getArtistLower());
            }
        }
    }

    private void checkAuthor(String spotAuthor, String ytAuthor, AtomicReference<Integer> score) {
        if (ytAuthor.equals(spotAuthor)) {
            this.authorExists = true;
            score.set(score.get() + 5);
        } else if (ytAuthor.contains(spotAuthor)) {
            score.set(score.get() + 3);
        } else {
            score.set(score.get() - 20);
        }
    }

    private void checkTitle(String nameLower, String ytTitle, AtomicReference<Integer> score) {
        if (ytTitle.equals(nameLower)) {
            this.titleExists = true;
            score.set(score.get() + 5);
        } else {
            score.set(score.get() - 3);
        }

        if (ytTitle.contains(nameLower)) {
            score.set(score.get() + 1);
        } else {
            score.set(score.get() - 1);
        }
    }
}

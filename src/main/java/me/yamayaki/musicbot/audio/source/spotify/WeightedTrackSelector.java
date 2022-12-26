package me.yamayaki.musicbot.audio.source.spotify;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.utilities.Pair;

import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class WeightedTrackSelector {
    private final SpotifyTrack spotifyTrack;
    private final List<AudioTrack> youtubeTracks;

    private int highestScore = -5000;
    private boolean perfectMatch = false;

    private boolean authorExists = false;
    private boolean authorExactExists = false;
    private boolean titleExists = false;
    private boolean titleExactExists = false;

    private AudioTrack selectedTrack = null;

    public static Pair<AudioTrack, Boolean> getWeightedTrack(SpotifyTrack spotifyTrack, List<AudioTrack> youtubeTracks) {
        WeightedTrackSelector weightedTrackSelector = new WeightedTrackSelector(spotifyTrack, youtubeTracks);
        weightedTrackSelector.runPreFilter();
        weightedTrackSelector.runSelector();

        return new Pair<>(weightedTrackSelector.getSelectedTrack(), weightedTrackSelector.isPerfectMatch());
    }

    private WeightedTrackSelector(SpotifyTrack spotifyTrack, List<AudioTrack> youtubeTracks) {
        this.spotifyTrack = spotifyTrack;
        this.youtubeTracks = youtubeTracks;
    }

    private Boolean isPerfectMatch() {
        return this.perfectMatch;
    }

    private AudioTrack getSelectedTrack() {
        return this.selectedTrack;
    }

    private void runPreFilter() {
        for (AudioTrack youtubeTrack : this.youtubeTracks) {
            //we don't have to run this when we already found a track
            if (this.perfectMatch) {
                continue;
            }

            //when both, the authors and the titles match, we can early exit and speed up this process quite a lot
            if (
                    (youtubeTrack.getInfo().author.equalsIgnoreCase(spotifyTrack.artist()) && youtubeTrack.getInfo().title.equalsIgnoreCase(spotifyTrack.name()))
                            || (youtubeTrack.getInfo().author.equalsIgnoreCase(spotifyTrack.artist() + " - Topic") && youtubeTrack.getInfo().title.equalsIgnoreCase(spotifyTrack.name()))
            ) {
                this.titleExactExists = true;
                this.authorExactExists = true;
                this.perfectMatch = true;
                this.highestScore = 5000;
                this.selectedTrack = youtubeTrack;

                MusicBot.LOGGER.debug("perfect match: {} ({}) - {} ({})", youtubeTrack.getInfo().title, spotifyTrack.name(), youtubeTrack.getInfo().author, spotifyTrack.artist());
                continue;
            }

            /*
             * when there is a video with the same author as the spotify track,
             * we can eliminate all the videos, that don't have the same uploader.
             */
            if (youtubeTrack.getInfo().author.equalsIgnoreCase(spotifyTrack.artist())) {
                this.authorExactExists = true;
            }

            /*
             * when there is a video with a similar author as the spotify track,
             * we can eliminate all the videos, that don't have a similar author.
             */
            if (!this.authorExactExists && youtubeTrack.getInfo().author.contains(spotifyTrack.artist())) {
                this.authorExists = true;
            }

            /*
             * when there is a video with the exact title as the spotify track,
             * we can eliminate all the videos, than don't contain the exact title.
             */
            if (youtubeTrack.getInfo().title.equalsIgnoreCase(spotifyTrack.name())) {
                this.titleExactExists = true;
            }

            /*
             * when there is a video with a similar title as the spotify track,
             * we can eliminate all the videos, than don't contain a similar title.
             */
            if (!this.titleExactExists && youtubeTrack.getInfo().title.contains(spotifyTrack.name())) {
                this.titleExists = true;
            }
        }
    }

    private void runSelector() {
        if (this.perfectMatch) {
            return;
        }

        for (AudioTrack youtubeTrack : this.youtubeTracks) {
            AtomicReference<Integer> score = new AtomicReference<>(0);

            this.checkAuthor(youtubeTrack.getInfo().author, score);
            this.checkTitle(youtubeTrack.getInfo().title, score);

            if (this.highestScore < score.get()) {
                this.selectedTrack = youtubeTrack;
                this.highestScore = score.get();
            }

            MusicBot.LOGGER.debug("{}: {} ({}) - {} ({})", score, youtubeTrack.getInfo().title, spotifyTrack.name(), youtubeTrack.getInfo().author, spotifyTrack.artist());
        }
    }

    private void checkAuthor(String ytAuthor, AtomicReference<Integer> score) {
        if (this.authorExactExists) {
            if (ytAuthor.equalsIgnoreCase(this.spotifyTrack.artist())) {
                score.set(score.get() + 20);
            } else {
                score.set(score.get() - 20);
            }

            return;
        }

        if (this.authorExists) {
            if (ytAuthor.contains(this.spotifyTrack.artist())) {
                score.set(score.get() + 20);
            } else {
                score.set(score.get() - 20);
            }

            return;
        }

        score.set(score.get() - 20);
    }

    private void checkTitle(String ytTitle, AtomicReference<Integer> score) {
        if (this.titleExactExists) {
            if (ytTitle.equalsIgnoreCase(this.spotifyTrack.name())) {
                score.set(score.get() + 20);
            } else {
                score.set(score.get() - 20);
            }

            return;
        }

        if (this.titleExists) {
            if (ytTitle.contains(this.spotifyTrack.name())) {
                score.set(score.get() + 20);
            } else {
                score.set(score.get() - 20);
            }

            return;
        }

        score.set(score.get() - 20);
    }
}

package me.yamayaki.musicbot.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;

import java.util.function.Consumer;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final TrackManager trackManager;
    private final Consumer<LoaderResponse> consumer;

    public LoadResultHandler(TrackManager trackManager, Consumer<LoaderResponse> consumer) {
        this.trackManager = trackManager;
        this.consumer = consumer;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.trackManager.addTrack(track);
        this.consumer.accept(new LoaderResponse(true));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        this.consumer.accept(new LoaderResponse(true));

        if (playlist.isSearchResult()) {
            this.trackManager.addTrack(playlist.getTracks().get(0));
            return;
        }

        for (AudioTrack track : playlist.getTracks()) {
            this.trackManager.addTrack(track);
        }
    }

    @Override
    public void noMatches() {
        this.trackManager.lastError = "no matches";
        this.consumer.accept(new LoaderResponse(false));
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        MusicBot.LOGGER.error("Failed to load track: {}", exception.getMessage(), exception);

        this.trackManager.lastError = exception.getMessage();
        this.consumer.accept(new LoaderResponse(false));
    }
}

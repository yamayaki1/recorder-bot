package me.yamayaki.musicbot.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.ServerAudioManager;
import me.yamayaki.musicbot.audio.entities.LoaderResponse;

import java.util.function.Consumer;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final ServerAudioManager serverAudioManager;
    private final Consumer<LoaderResponse> consumer;

    public LoadResultHandler(ServerAudioManager serverAudioManager, Consumer<LoaderResponse> consumer) {
        this.serverAudioManager = serverAudioManager;
        this.consumer = consumer;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        this.serverAudioManager.addTrack(track);
        this.consumer.accept(new LoaderResponse(true, 1, "", track.getInfo().title));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            this.serverAudioManager.addTrack(playlist.getTracks().get(0));
            this.consumer.accept(new LoaderResponse(true, 1));
            return;
        }

        for (AudioTrack track : playlist.getTracks()) {
            this.serverAudioManager.addTrack(track);
            this.consumer.accept(new LoaderResponse(true, playlist.getTracks().size()));
        }
    }

    @Override
    public void noMatches() {
        this.serverAudioManager.lastError = "no matches";
        this.consumer.accept(new LoaderResponse(false, 0, "Keine Ergebnisse."));
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        MusicBot.LOGGER.error("Failed to load track: {}", exception.getMessage(), exception);

        this.serverAudioManager.lastError = exception.getMessage();
        this.consumer.accept(new LoaderResponse(false, 0, exception.getMessage()));
    }
}

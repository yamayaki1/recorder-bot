package me.yamayaki.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.PlaylistManager;
import me.yamayaki.musicbot.audio.entities.LoaderResponse;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final PlaylistManager playlistManager;
    private final long position;
    private final @Nullable Consumer<LoaderResponse> consumer;

    public LoadResultHandler(PlaylistManager playlistManager, long position, @Nullable Consumer<LoaderResponse> consumer) {
        this.playlistManager = playlistManager;
        this.position = position;
        this.consumer = consumer;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        track.setPosition(this.position);

        this.playlistManager.add(track);

        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(true, 1, "", track.getInfo().title));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult()) {
            this.playlistManager.add(playlist.getTracks().get(0));

            assert this.consumer != null;
            this.consumer.accept(new LoaderResponse(true, 1));
            return;
        }

        for (AudioTrack track : playlist.getTracks()) {
            this.playlistManager.add(track);

            assert this.consumer != null;
            this.consumer.accept(new LoaderResponse(true, playlist.getTracks().size()));
        }
    }

    @Override
    public void noMatches() {
        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(false, 0, "Keine Ergebnisse."));
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        MusicBot.LOGGER.error("Failed to load track: {}", exception.getMessage(), exception);

        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(false, 0, exception.getMessage()));
    }
}

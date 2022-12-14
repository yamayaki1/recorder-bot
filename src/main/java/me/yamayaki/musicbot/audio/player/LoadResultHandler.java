package me.yamayaki.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.PlaylistManager;
import me.yamayaki.musicbot.entities.LoaderResponse;
import me.yamayaki.musicbot.entities.TrackInfo;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class LoadResultHandler implements AudioLoadResultHandler {
    private final PlaylistManager playlistManager;
    private final @Nullable TrackInfo trackInfo;
    private final @Nullable Consumer<LoaderResponse> consumer;

    public LoadResultHandler(PlaylistManager playlistManager, @Nullable TrackInfo trackInfo, @Nullable Consumer<LoaderResponse> consumer) {
        this.playlistManager = playlistManager;
        this.trackInfo = trackInfo;
        this.consumer = consumer;
    }

    @Override
    public void trackLoaded(AudioTrack track) {
        if (this.trackInfo != null) {
            track.setPosition(this.trackInfo.position());
            track.setUserData(this.trackInfo.userData());
        }

        this.playlistManager.add(track);

        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(track));
    }

    @Override
    public void playlistLoaded(AudioPlaylist playlist) {
        if (playlist.isSearchResult() || playlist.getTracks().size() == 1) {
            this.playlistManager.add(playlist.getTracks().get(0));

            assert this.consumer != null;
            this.consumer.accept(new LoaderResponse(playlist.getTracks().get(0)));
            return;
        }

        for (AudioTrack track : playlist.getTracks()) {
            this.playlistManager.add(track);

            assert this.consumer != null;
            this.consumer.accept(new LoaderResponse(playlist));
        }
    }

    @Override
    public void noMatches() {
        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(null));
    }

    @Override
    public void loadFailed(FriendlyException exception) {
        MusicBot.LOGGER.error("Failed to load track: {}", exception.getMessage(), exception);

        assert this.consumer != null;
        this.consumer.accept(new LoaderResponse(null));
    }
}

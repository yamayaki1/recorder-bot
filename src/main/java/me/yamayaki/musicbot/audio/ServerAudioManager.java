package me.yamayaki.musicbot.audio;

import me.yamayaki.musicbot.audio.entities.LoaderResponse;
import me.yamayaki.musicbot.audio.handler.AudioEventHandler;
import me.yamayaki.musicbot.audio.handler.LoadResultHandler;
import me.yamayaki.musicbot.audio.player.LavaAudioSource;
import me.yamayaki.musicbot.audio.player.LavaManager;
import org.javacord.api.entity.server.Server;

import java.util.function.Consumer;

public class ServerAudioManager {
    private final Server server;

    private final LavaAudioSource audioSource;
    private final PlaylistManager playlistManager;

    public ServerAudioManager(Server server) {
        this.server = server;
        this.audioSource = new LavaAudioSource(server.getApi(), new AudioEventHandler(this));
        this.playlistManager = new PlaylistManager(this.server.getId());
        this.resumeOrNext();
    }

    public void tryLoadItems(String song, Consumer<LoaderResponse> consumer) {
        LavaManager.loadTrack(song, new LoadResultHandler(this.getPlaylist(), 0L, consumer));
    }

    public void skipTrack(int count) {
        if (count < 1) {
            return;
        }

        for (int i = 0; i < count; i++) {
            this.audioSource.getAudioPlayer().stopTrack();
            this.resumeOrNext();
        }
    }

    public void resumeOrNext() {
        if (this.isPaused()) {
            this.setPaused(false);
        }

        if (this.audioSource.hasFinished() && this.playlistManager.hasNext()) {
            this.audioSource.getAudioPlayer()
                    .playTrack(this.playlistManager.getNext());
        }

        this.fixAudioSource();
    }

    public boolean hasFinished() {
        return this.audioSource
                .hasFinished();
    }

    public void setPaused(boolean bool) {
        this.audioSource.getAudioPlayer()
                .setPaused(bool);
    }

    public boolean isPaused() {
        return this.audioSource.getAudioPlayer()
                .isPaused();
    }

    public void setVolume(int volume) {
        this.audioSource.getAudioPlayer()
                .setVolume(volume);
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> audioConnection.setAudioSource(this.audioSource));
    }

    public PlaylistManager getPlaylist() {
        return this.playlistManager;
    }

    public String getServerName() {
        return this.server.getName();
    }

    public void shutdown(boolean save) {
        if (save) {
            this.playlistManager.store();
        }

        this.playlistManager.clearList();
        this.audioSource.getAudioPlayer().stopTrack();
    }
}

package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.audio.handler.AudioEventHandler;
import me.yamayaki.musicbot.audio.handler.LoadResultHandler;
import me.yamayaki.musicbot.audio.handler.LoaderResponse;
import me.yamayaki.musicbot.audio.player.LavaAudioSource;
import org.javacord.api.entity.server.Server;

import java.util.function.Consumer;

public class TrackManager {
    private final Server server;

    private final LavaAudioSource audioSource;
    private final PlaylistManager playlistManager;

    public String lastError = "";

    public TrackManager(Server server) {
        this.server = server;
        this.audioSource = new LavaAudioSource(server.getApi(), new AudioEventHandler(this));
        this.playlistManager = new PlaylistManager(this);
    }

    public void tryLoadItems(String song, Consumer<LoaderResponse> consumer) {
        this.audioSource.getPlayerManager()
                .loadItem(song, new LoadResultHandler(this, consumer));
    }

    public void addTrack(AudioTrack track) {
        this.playlistManager.addTrack(track);
        this.resumeOrNext();
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

    public Long getServerId() {
        return this.server.getId();
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

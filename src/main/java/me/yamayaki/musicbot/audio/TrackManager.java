package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.audio.handler.AudioEventHandler;
import me.yamayaki.musicbot.audio.handler.LoadResultHandler;
import me.yamayaki.musicbot.audio.player.LavaAudioSource;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrackManager {
    private final Server server;

    private final LavaAudioSource audioSource;
    private final List<AudioTrack> trackList = new ArrayList<>();

    public boolean repeat = false;
    public boolean paused = false;
    public String lastError = "";

    public TrackManager(Server server) {
        this.server = server;
        this.audioSource = new LavaAudioSource(server.getApi(), new AudioEventHandler(this));
    }

    public void tryLoadItems(String song, Consumer<Boolean> consumer) {
        this.audioSource.getPlayerManager()
                .loadItem(song, new LoadResultHandler(this, consumer));
    }

    public boolean hasFinished() {
        return this.audioSource
                .hasFinished();
    }

    public void addTrack(AudioTrack track) {
        if (track == null) {
            return;
        }

        this.trackList.add(track);
        this.startTrackIfIdle();
    }

    public void skipTrack(int count) {
        if (count < 1) {
            return;
        }

        for (int i = 0; i < count; i++) {
            this.audioSource.getAudioPlayer().stopTrack();
            this.startTrackIfIdle();
        }
    }

    public List<AudioTrack> getTracks() {
        return new ArrayList<>(this.trackList);
    }

    public void clearTracks() {
        this.trackList.clear();
    }

    //TODO make this more resilient
    public void startTrackIfIdle() {
        if (this.paused) {
            this.resumeTrack();
        }

        if (this.audioSource.hasFinished() && this.trackList.size() != 0) {
            if (this.repeat) {
                AudioTrack oldTrack = this.trackList.get(0);
                this.trackList.add(oldTrack.makeClone());
            }

            this.audioSource.getAudioPlayer().playTrack(this.trackList.get(0));
            this.trackList.remove(0);
        }

        this.fixAudioSource();
    }

    public AudioTrack getCurrentTrack() {
        return this.audioSource.getAudioPlayer()
                .getPlayingTrack();
    }

    public void pauseTrack() {
        this.audioSource.getAudioPlayer()
                .setPaused(true);
    }

    public void resumeTrack() {
        this.audioSource.getAudioPlayer()
                .setPaused(false);
    }

    //TODO make this more resilient
    public boolean toggleRepeat() {
        this.repeat = !this.repeat;
        return repeat;
    }

    public void setVolume(int volume) {
        this.audioSource.getAudioPlayer()
                .setVolume(volume);
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> audioConnection.setAudioSource(this.audioSource));
    }

    public String getServerName() {
        return this.server.getName();
    }

    public void shutdown() {
        this.clearTracks();
        this.audioSource.getAudioPlayer().stopTrack();
    }
}

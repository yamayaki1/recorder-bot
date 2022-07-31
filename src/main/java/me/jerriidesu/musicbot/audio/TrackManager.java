package me.jerriidesu.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.event.AudioEventListener;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.audio.handler.AudioEventHandler;
import me.jerriidesu.musicbot.audio.handler.LoadResultHandler;
import me.jerriidesu.musicbot.audio.player.LavaAudioSource;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrackManager {
    private final Server server;
    private final AudioEventHandler eventHandler;

    private final LavaAudioSource audioSource;
    private final List<AudioTrack> trackList = new ArrayList<>();

    public boolean paused = false;
    public String lastError = "";

    private boolean repeat = false;

    public TrackManager(Server server) {
        this.server = server;
        this.eventHandler = new AudioEventHandler(this);
        this.audioSource = new LavaAudioSource(server.getApi(), this);
    }

    public void tryLoadItems(String song, Consumer<Boolean> consumer) {
        this.audioSource.getPlayerManager()
                .loadItem(song, new LoadResultHandler(this, consumer));
    }

    public void addTrack(AudioTrack track) {
        this.trackList.add(track);
        this.startTrackIfIdle();
    }

    public void skipTrack(int count) {
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

    public boolean toggleRepeat() {
        this.repeat = !this.repeat;
        return repeat;
    }

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

    public void pauseTrack() {
        this.audioSource.getAudioPlayer()
                .setPaused(true);
    }

    public void resumeTrack() {
        this.audioSource.getAudioPlayer()
                .setPaused(false);
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> audioConnection.setAudioSource(this.audioSource));
    }

    public AudioEventListener getEventHandler() {
        return this.eventHandler;
    }

    public LavaAudioSource getAudioSource() {
        return this.audioSource;
    }

    public void close() {
        //TODO implement
    }
}

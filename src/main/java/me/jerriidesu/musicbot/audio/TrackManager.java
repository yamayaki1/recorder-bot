package me.jerriidesu.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.audio.source.LavaPlayerAudioSource;
import org.javacord.api.entity.server.Server;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class TrackManager extends AudioEventAdapter {

    private final Server server;

    private final LavaPlayerAudioSource audioSource;
    private final List<AudioTrack> trackList = new ArrayList<>();

    private boolean repeat = false;

    public TrackManager(Server server) {
        this.server = server;
        this.audioSource = new LavaPlayerAudioSource(server.getApi());
        this.audioSource.getAudioPlayer().addListener(this);
    }

    public LavaPlayerAudioSource getAudioSource() {
        return this.audioSource;
    }

    public List<AudioTrack> getTrackList() {
        return new ArrayList<>(this.trackList);
    }

    public void addItems(String song, Consumer<Boolean> consumer) {
        this.audioSource.getPlayerManager().loadItem(song, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                trackList.add(track);
                consumer.accept(true);
                startPlaying();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackList.addAll(playlist.getTracks());
                consumer.accept(true);
                startPlaying();
            }

            @Override
            public void noMatches() {
                MusicBot.getLogger().error("[playlist-manager] no matches");
                consumer.accept(false);
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                MusicBot.getLogger().error("[playlist-manager] {}", exception.getMessage());
                consumer.accept(false);
            }
        });
    }

    public void skipTracks(int count) {
        this.audioSource.getAudioPlayer().stopTrack();
        this.startPlaying();
    }

    public boolean toggleRepeat() {
        this.repeat = !this.repeat;
        return repeat;
    }

    public void clearTrackList() {
        this.trackList.clear();
    }

    public void startPlaying() {
        if(this.audioSource.getAudioPlayer().isPaused()) {
            this.audioSource.getAudioPlayer().setPaused(false);
        }

        if(this.audioSource.hasFinished() && this.trackList.size() != 0) {
            if(this.repeat) {
                AudioTrack oldTrack = this.trackList.get(0);
                this.trackList.add(oldTrack.makeClone());
            }

            this.audioSource.getAudioPlayer().playTrack(this.trackList.get(0));
            this.trackList.remove(0);
        }

        this.fixAudioSource();
    }

    @Override
    public void onPlayerPause(AudioPlayer player) {
        // Player was paused
    }

    @Override
    public void onPlayerResume(AudioPlayer player) {
        // Player was resumed
    }

    @Override
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // A track started playing
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if(trackList.size() == 0) {
            return;
        }

        if (endReason.mayStartNext) {
            startPlaying();
        }
    }

    @Override
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        // An already playing track threw an exception (track end event will still be received separately)
    }

    @Override
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Audio track has been unable to provide us any audio, might want to just start a new track
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.setAudioSource(this.audioSource);
        });
    }

    public void close() {
    }
}

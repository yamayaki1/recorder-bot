package me.jerriidesu.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.audio.messages.PlayerResponse;
import me.jerriidesu.musicbot.audio.source.LavaPlayerAudioSource;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PlaylistManager extends AudioEventAdapter {

    private final LavaPlayerAudioSource audioSource;
    private final MusicBot botInstance;

    private final List<AudioTrack> trackList = new ArrayList<>();

    public PlaylistManager(MusicBot bot) {
        this.botInstance = bot;
        this.audioSource = new LavaPlayerAudioSource(bot.getAPI());
        this.audioSource.getAudioPlayer().addListener(this);
    }

    public LavaPlayerAudioSource getAudioSource() {
        return this.audioSource;
    }

    public List<AudioTrack> getTrackList() {
        return this.trackList;
    }

    public void addItems(String song, Consumer<PlayerResponse> consumer) {
        this.audioSource.getPlayerManager().loadItem(song, new AudioLoadResultHandler() {

            @Override
            public void trackLoaded(AudioTrack track) {
                trackList.add(track);
                consumer.accept(new PlayerResponse(true, "Titel "+track.getInfo().title+" hinzugefügt."));
                startPlaying();
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                trackList.addAll(playlist.getTracks());
                consumer.accept(new PlayerResponse(true, "Playlist "+playlist.getName()+" mit "+playlist.getTracks().size()+" Titel(n) hinzugefügt"));
                startPlaying();
            }

            @Override
            public void noMatches() {
                consumer.accept(new PlayerResponse(false, "Kein Song gefunden."));
            }

            @Override
            public void loadFailed(FriendlyException exception) {
                consumer.accept(new PlayerResponse(false, exception.getMessage()));
            }
        });
    }

    public void startPlaying() {
        if(audioSource.getAudioPlayer().isPaused()) {
            audioSource.getAudioPlayer().setPaused(false);
        }

        if(this.audioSource.hasFinished()) {
            this.audioSource.getAudioPlayer().playTrack(trackList.get(0));
        }
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
        trackList.remove(0);

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
}

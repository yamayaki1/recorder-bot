package me.jerriidesu.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import me.jerriidesu.musicbot.audio.TrackManager;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

public class LavaAudioSource extends AudioSourceBase {
    private final TrackManager trackManager;

    private final AudioPlayerManager playerManager;
    private final AudioPlayer audioPlayer;

    private AudioFrame lastFrame;

    public LavaAudioSource(DiscordApi api, TrackManager trackManager) {
        super(api);

        this.trackManager = trackManager;
        this.playerManager = LavaPlayerManager.getPlayerManager();
        this.audioPlayer = LavaPlayerManager.getPlayer(this.playerManager, trackManager);
    }

    @Override
    public byte[] getNextFrame() {
        if (lastFrame == null) {
            return null;
        }

        return applyTransformers(lastFrame.getData());
    }

    @Override
    public boolean hasFinished() {
        return this.audioPlayer.getPlayingTrack() == null
                || this.audioPlayer.getPlayingTrack().getPosition() >= this.audioPlayer.getPlayingTrack().getDuration();
    }

    @Override
    public boolean hasNextFrame() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public AudioSource copy() {
        return new LavaAudioSource(getApi(), this.trackManager);
    }

    public AudioPlayerManager getPlayerManager() {
        return this.playerManager;
    }

    public AudioPlayer getAudioPlayer() {
        return this.audioPlayer;
    }
}


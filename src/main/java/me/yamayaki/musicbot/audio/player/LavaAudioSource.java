package me.yamayaki.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

public class LavaAudioSource extends AudioSourceBase {
    private final AudioEventAdapter audioEventHandler;
    private final AudioPlayer audioPlayer;

    private AudioFrame lastFrame;

    public LavaAudioSource(DiscordApi api, AudioEventAdapter audioEventHandler) {
        super(api);

        this.audioEventHandler = audioEventHandler;
        this.audioPlayer = LavaManager.getPlayer(audioEventHandler);
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
        return this.audioPlayer.getPlayingTrack() == null || this.audioPlayer.getPlayingTrack().getPosition() >= this.audioPlayer.getPlayingTrack().getDuration();
    }

    @Override
    public boolean hasNextFrame() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public AudioSource copy() {
        return new LavaAudioSource(getApi(), this.audioEventHandler);
    }

    public AudioPlayer getAudioPlayer() {
        return this.audioPlayer;
    }
}


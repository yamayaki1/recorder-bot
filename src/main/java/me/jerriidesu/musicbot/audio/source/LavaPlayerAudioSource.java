package me.jerriidesu.musicbot.audio.source;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.http.HttpAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.local.LocalAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import org.javacord.api.DiscordApi;
import org.javacord.api.audio.AudioSource;
import org.javacord.api.audio.AudioSourceBase;

public class LavaPlayerAudioSource extends AudioSourceBase {

    private final AudioPlayerManager playerManager;
    private final AudioPlayer audioPlayer;
    private AudioFrame lastFrame;

    /**
     * Creates a new lavaplayer audio source.
     *
     * @param api A discord api instance.
     */
    public LavaPlayerAudioSource(DiscordApi api) {
        super(api);

        AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
        playerManager.registerSourceManager(new YoutubeAudioSourceManager());
        playerManager.registerSourceManager(new HttpAudioSourceManager());
        playerManager.registerSourceManager(new LocalAudioSourceManager());
        playerManager.registerSourceManager(new TwitchStreamAudioSourceManager());

        this.playerManager = playerManager;
        this.audioPlayer = playerManager.createPlayer();
    }

    public AudioPlayerManager getPlayerManager() {
        return playerManager;
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
        return this.audioPlayer.getPlayingTrack().getPosition() >= this.audioPlayer.getPlayingTrack().getDuration();
    }

    @Override
    public boolean hasNextFrame() {
        lastFrame = audioPlayer.provide();
        return lastFrame != null;
    }

    @Override
    public AudioSource copy() {
        return new LavaPlayerAudioSource(getApi());
    }
}


package me.jerriidesu.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import me.jerriidesu.musicbot.audio.handler.AudioEventHandler;

public class LavaPlayerManager {
    public static AudioPlayerManager getPlayerManager() {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        AudioSourceManagers.registerRemoteSources(audioPlayerManager);

        int OPUS_QUALITY = AudioConfiguration.OPUS_QUALITY_MAX;
        audioPlayerManager.getConfiguration().setOpusEncodingQuality(OPUS_QUALITY);
        audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);

        return audioPlayerManager;
    }

    public static AudioPlayer getPlayer(AudioPlayerManager audioPlayerManager, AudioEventHandler audioEventHandler) {
        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        audioPlayer.setVolume(50);
        audioPlayer.addListener(audioEventHandler);

        return audioPlayer;
    }
}

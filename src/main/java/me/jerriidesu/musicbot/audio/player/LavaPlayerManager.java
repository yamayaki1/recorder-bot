package me.jerriidesu.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import me.jerriidesu.musicbot.audio.handler.AudioEventHandler;
import me.jerriidesu.musicbot.audio.source.spotify.SpotifyAudioSourceManager;

public class LavaPlayerManager {
    public static AudioPlayerManager getPlayerManager() {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();

        audioPlayerManager.registerSourceManager(new SpotifyAudioSourceManager());
        audioPlayerManager.registerSourceManager(new YoutubeAudioSourceManager());
        audioPlayerManager.registerSourceManager(new BandcampAudioSourceManager());
        audioPlayerManager.registerSourceManager(SoundCloudAudioSourceManager.createDefault());
        audioPlayerManager.registerSourceManager(new TwitchStreamAudioSourceManager());

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

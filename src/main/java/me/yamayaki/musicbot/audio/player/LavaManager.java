package me.yamayaki.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.filter.equalizer.EqualizerFactory;
import com.sedmelluq.discord.lavaplayer.player.AudioConfiguration;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import me.yamayaki.musicbot.audio.source.spotify.SpotifySourceManager;

import java.util.concurrent.Future;

public class LavaManager {
    public static final SpotifySourceManager spotifySource;
    public static final YoutubeAudioSourceManager youtubeSource;
    public static final BandcampAudioSourceManager bandcampSource;
    public static final SoundCloudAudioSourceManager soundcloudSource;
    public static final TwitchStreamAudioSourceManager twitchSource;

    public static final EqualizerFactory equalizerFactory;

    private static final AudioPlayerManager audioPlayerManager;

    static {
        spotifySource = new SpotifySourceManager();
        youtubeSource = new YoutubeAudioSourceManager();
        bandcampSource = new BandcampAudioSourceManager();
        soundcloudSource = SoundCloudAudioSourceManager.createDefault();
        twitchSource = new TwitchStreamAudioSourceManager();

        equalizerFactory = new EqualizerFactory();

        audioPlayerManager = createPlayerManager();
    }

    private static void registerSources(final AudioPlayerManager audioPlayerManager) {
        audioPlayerManager.registerSourceManager(spotifySource);
        audioPlayerManager.registerSourceManager(youtubeSource);
        audioPlayerManager.registerSourceManager(bandcampSource);
        audioPlayerManager.registerSourceManager(soundcloudSource);
        audioPlayerManager.registerSourceManager(twitchSource);
    }

    private static AudioPlayerManager createPlayerManager() {
        AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
        registerSources(audioPlayerManager);

        audioPlayerManager.getConfiguration().setOpusEncodingQuality(AudioConfiguration.OPUS_QUALITY_MAX);
        audioPlayerManager.getConfiguration().setResamplingQuality(AudioConfiguration.ResamplingQuality.HIGH);

        return audioPlayerManager;
    }

    public static AudioPlayer getPlayer(final AudioEventAdapter audioEventHandler) {
        AudioPlayer audioPlayer = audioPlayerManager.createPlayer();
        audioPlayer.addListener(audioEventHandler);

        return audioPlayer;
    }

    public static Future<Void> loadTrack(final String identifier, final LoadResultHandler resultHandler) {
        return audioPlayerManager.loadItem(identifier, resultHandler);
    }
}

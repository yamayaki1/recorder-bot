package me.yamayaki.musicbot.audio.player;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.bandcamp.BandcampAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.soundcloud.SoundCloudAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.twitch.TwitchStreamAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import me.yamayaki.musicbot.audio.source.spotify.SpotifySourceManager;

public class LavaSourceManager {
    public static final SpotifySourceManager spotifySource;
    public static final YoutubeAudioSourceManager youtubeSource;
    public static final BandcampAudioSourceManager bandcampSource;
    public static final SoundCloudAudioSourceManager soundcloudSource;
    public static final TwitchStreamAudioSourceManager twitchSource;

    static {
        spotifySource = new SpotifySourceManager();
        youtubeSource = new YoutubeAudioSourceManager();
        bandcampSource = new BandcampAudioSourceManager();
        soundcloudSource = SoundCloudAudioSourceManager.createDefault();
        twitchSource = new TwitchStreamAudioSourceManager();
    }

    public static void registerAudioSources(AudioPlayerManager audioPlayerManager) {
        audioPlayerManager.registerSourceManager(spotifySource);
        audioPlayerManager.registerSourceManager(youtubeSource);
        audioPlayerManager.registerSourceManager(bandcampSource);
        audioPlayerManager.registerSourceManager(soundcloudSource);
        audioPlayerManager.registerSourceManager(twitchSource);
    }
}

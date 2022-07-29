package me.jerriidesu.musicbot.audio;

import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.audio.source.LavaPlayerAudioSource;
import org.javacord.api.audio.AudioSource;

public class PlaylistManager {

    private final AudioSource audioSource;
    private final MusicBot botInstance;

    public PlaylistManager(MusicBot bot) {
        this.botInstance = bot;
        this.audioSource = new LavaPlayerAudioSource(bot.getAPI());
    }

    public AudioSource getAudioSource() {
        return this.audioSource;
    }
}

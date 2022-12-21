package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEvent;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.entities.LoaderResponse;
import me.yamayaki.musicbot.audio.player.LavaAudioSource;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.audio.player.LoadResultHandler;
import org.javacord.api.entity.server.Server;

import java.util.function.Consumer;

public class ServerAudioManager extends AudioEventAdapter {
    private final Server server;

    private final LavaAudioSource audioSource;
    private final PlaylistManager playlist;
    private final PlayerControl playerControl;

    private boolean skipping = false;

    private long lastActiveTime = System.currentTimeMillis();

    public ServerAudioManager(Server server) {
        this.server = server;
        this.audioSource = new LavaAudioSource(server.getApi(), this);
        this.playlist = new PlaylistManager(this.server.getId());
        this.playerControl = new PlayerControl(this);

        this.startPlaying();
    }

    public PlayerControl getPlayerControl() {
        return this.playerControl;
    }

    public void tryLoadItems(String song, Consumer<LoaderResponse> consumer) {
        try {
            LavaManager.loadTrack(song, new LoadResultHandler(this.getPlaylist(), 0L, consumer)).get();
            this.startPlaying();
            this.playerControl.setDirty();
        } catch (Exception ignored) {
        }
    }

    public void skipTrack(int count) {
        this.skipping = true;

        for (int i = 0; i < Math.max(count, 1); i++) {
            this.audioSource.getAudioPlayer().stopTrack();
            this.startPlaying();
        }

        this.skipping = false;
    }

    public void startPlaying() {
        this.setPaused(false);

        if (this.audioSource.hasFinished() && this.playlist.hasNext()) {
            this.audioSource.getAudioPlayer()
                    .playTrack(this.playlist.next());
        }

        this.fixAudioSource();
    }

    public boolean hasFinished() {
        return this.audioSource
                .hasFinished();
    }

    public boolean isPaused() {
        return this.audioSource.getAudioPlayer()
                .isPaused();
    }

    public void setPaused(boolean bool) {
        this.audioSource.getAudioPlayer()
                .setPaused(bool);
    }

    public int getVolume() {
        return this.audioSource.getAudioPlayer()
                .getVolume();
    }

    public void setVolume(int volume) {
        int vol = Math.max(0, Math.min(volume, 100));
        this.audioSource.getAudioPlayer()
                .setVolume(vol);
    }

    public void fixAudioSource() {
        this.server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.setAudioSource(this.audioSource);
            MusicBot.LOGGER.debug("Setting audio source for connection in {}", this.server.getId());
        });
    }

    public PlaylistManager getPlaylist() {
        return this.playlist;
    }

    @Override
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (!(this.getPlaylist().hasNext() || endReason.mayStartNext)) {
            return;
        }

        if (!this.skipping) {
            this.startPlaying();
        }
    }

    @Override
    public void onEvent(AudioEvent event) {
        super.onEvent(event);

        this.playerControl.setDirty();
        this.lastActiveTime = System.currentTimeMillis();
    }

    public boolean isInactive() {
        return this.audioSource.hasFinished() && this.lastActiveTime < (System.currentTimeMillis() - 3E5);
    }

    public void shutdown(boolean save) {
        this.audioSource.getAudioPlayer().destroy();
        this.playerControl.shutdown();

        this.server.getAudioConnection().ifPresent(audioConnection -> {
            audioConnection.removeAudioSource();
            audioConnection.getChannel().disconnect();
        });

        if (save) {
            this.playlist.store();
        }
    }

    public Server getServer() {
        return this.server;
    }

    public void stopTrack() {
        this.audioSource.getAudioPlayer().stopTrack();
    }

    public AudioTrack getPlayingTrack() {
        return this.audioSource.getAudioPlayer().getPlayingTrack();
    }
}

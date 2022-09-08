package me.yamayaki.musicbot.audio.handler;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import me.yamayaki.musicbot.audio.TrackManager;

public class AudioEventHandler extends AudioEventAdapter {
    private final TrackManager trackManager;

    public AudioEventHandler(TrackManager trackManager) {
        this.trackManager = trackManager;
    }

    /**
     * @param player Audio player
     */
    public void onPlayerPause(AudioPlayer player) {
        this.trackManager.paused = false;
    }

    /**
     * @param player Audio player
     */
    public void onPlayerResume(AudioPlayer player) {
        this.trackManager.paused = true;
    }

    /**
     * @param player Audio player
     * @param track  Audio track that started
     */
    public void onTrackStart(AudioPlayer player, AudioTrack track) {
        // Adapter dummy method
    }

    /**
     * @param player    Audio player
     * @param track     Audio track that ended
     * @param endReason The reason why the track stopped playing
     */
    public void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (this.trackManager.getTracks().size() == 0) {
            return;
        }

        if (endReason.mayStartNext) {
            this.trackManager.startTrackIfIdle();
        }
    }

    /**
     * @param player    Audio player
     * @param track     Audio track where the exception occurred
     * @param exception The exception that occurred
     */
    public void onTrackException(AudioPlayer player, AudioTrack track, FriendlyException exception) {
        this.trackManager.lastError = exception.getMessage();
    }

    /**
     * @param player      Audio player
     * @param track       Audio track where the exception occurred
     * @param thresholdMs The wait threshold that was exceeded for this event to trigger
     */
    public void onTrackStuck(AudioPlayer player, AudioTrack track, long thresholdMs) {
        // Adapter dummy method
    }
}

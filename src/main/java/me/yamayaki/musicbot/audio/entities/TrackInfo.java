package me.yamayaki.musicbot.audio.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public record TrackInfo(String uri, long position) {
    public static TrackInfo of(AudioTrack audioTrack) {
        return new TrackInfo(audioTrack.getInfo().uri, audioTrack.getPosition());
    }

    @Override
    public String toString() {
        return "TrackInfo{" + "uri='" + uri + '\'' + ", position=" + position + '}';
    }
}

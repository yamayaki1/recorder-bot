package me.yamayaki.musicbot.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public record TrackInfo(String uri, long position, Object userData) {
    public static TrackInfo of(AudioTrack audioTrack) {
        return new TrackInfo(audioTrack.getInfo().uri, audioTrack.getPosition(), audioTrack.getUserData());
    }

    @Override
    public String toString() {
        return "TrackInfo{" +
                "uri='" + uri + '\'' +
                ", position=" + position +
                ", userData=" + userData +
                '}';
    }
}

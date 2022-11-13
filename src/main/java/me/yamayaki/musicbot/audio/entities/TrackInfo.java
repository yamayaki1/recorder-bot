package me.yamayaki.musicbot.audio.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class TrackInfo {
    public String uri;
    public long position;

    public static TrackInfo of(AudioTrack audioTrack) {
        TrackInfo trackInfo = new TrackInfo();

        trackInfo.uri = audioTrack.getInfo().uri;
        trackInfo.position = audioTrack.getPosition();

        return trackInfo;
    }
}

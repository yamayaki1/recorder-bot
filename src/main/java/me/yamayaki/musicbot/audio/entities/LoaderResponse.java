package me.yamayaki.musicbot.audio.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

public class LoaderResponse {
    private final AudioItem loadedItem;

    public LoaderResponse(AudioItem audioItem) {
        this.loadedItem = audioItem;
    }

    public boolean isSuccess() {
        return this.loadedItem != null;
    }

    public int getCount() {
        if (this.loadedItem instanceof AudioPlaylist playlist) {
            return playlist.getTracks().size();
        }

        if (this.loadedItem instanceof AudioTrack) {
            return 1;
        }

        return 0;
    }

    public String getTitle() {
        if (this.loadedItem instanceof AudioTrack track) {
            return track.getInfo().title;
        }

        return "";
    }
}

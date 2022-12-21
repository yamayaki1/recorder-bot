package me.yamayaki.musicbot.audio.entities;

import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import org.jetbrains.annotations.Nullable;

public record LoaderResponse(@Nullable AudioItem audioItem) {
    public boolean isSuccess() {
        return this.audioItem != null;
    }

    public String getTitle() {
        return this.audioItem instanceof AudioTrack track ? track.getInfo().title : "unknown";
    }

    public int getCount() {
        return this.audioItem instanceof AudioPlaylist playlist ? playlist.getTracks().size() :
                this.audioItem instanceof AudioTrack ? 1 : 0;
    }
}

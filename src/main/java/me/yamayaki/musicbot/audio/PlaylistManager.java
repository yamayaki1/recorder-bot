package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private final List<AudioTrack> trackList;

    private AudioTrack currentTrack = null;

    public boolean loop = false;

    public PlaylistManager(TrackManager trackManager) {
        this.trackList = new ArrayList<>();
    }

    public AudioTrack getCurrentTrack() {
        return currentTrack;
    }

    public void addTrack(AudioTrack track) {
        if (track == null) {
            return;
        }

        this.trackList.add(track);
    }

    public List<AudioTrack> getTracks(boolean inclCurrent) {
        final ArrayList<AudioTrack> list = new ArrayList<>();

        if (inclCurrent) {
            list.add(this.currentTrack);
        }

        list.addAll(this.trackList);

        return list;
    }

    public void clearList() {
        this.trackList.clear();
    }

    public boolean hasNext() {
        return this.trackList.size() > 0 || (this.currentTrack != null && this.loop);
    }

    public AudioTrack getNext() {
        if (!this.hasNext()) {
            return null;
        }

        if (this.loop) {
            this.trackList.add(this.currentTrack.makeClone());
        }

        this.currentTrack = this.trackList.get(0);
        this.trackList.remove(0);

        return this.currentTrack;
    }

    public boolean toggleRepeat() {
        this.loop = !this.loop;
        return this.loop;
    }
}

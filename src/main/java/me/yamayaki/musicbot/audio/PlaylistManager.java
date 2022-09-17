package me.yamayaki.musicbot.audio;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;

import java.util.ArrayList;
import java.util.List;

public class PlaylistManager {
    private final TrackManager trackManager;
    private final List<AudioTrack> trackList;

    private AudioTrack currentTrack = null;

    public boolean loop = false;

    public PlaylistManager(TrackManager trackManager) {
        this.trackManager = trackManager;
        this.trackList = new ArrayList<>();
        this.restore();
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

        if (inclCurrent && this.currentTrack != null) {
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

    public void restore() {
        var response = MusicBot.getCache()
                .getPlaylistCache()
                .getValue(this.trackManager.getServerId());

        if (response.isEmpty()) {
            return;
        }

        for (String url : response.get()) {
            this.trackManager.tryLoadItems(url, loaderResponse -> {
                if(MusicBot.DEBUG) {
                    MusicBot.LOGGER.info("restored track {}", loaderResponse.getTitle());
                }
            });
        }

        MusicBot.getCache()
                .getPlaylistCache()
                .deleteValue(this.trackManager.getServerId());
    }

    public void store() {
        final List<String> ids = new ArrayList<>();

        //add currently playing track
        ids.add(this.getCurrentTrack().getInfo().uri);

        //add all remaining songs
        trackList.forEach(track -> ids.add(track.getInfo().uri));

        MusicBot.getCache()
                .getPlaylistCache()
                .putValue(this.trackManager.getServerId(), ids.toArray(String[]::new));
    }
}

package me.yamayaki.musicbot.audio.source.spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.storage.database.specs.impl.CacheSpecs;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class SpotifySourceManager implements AudioSourceManager {
    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!SpotifyAccess.SPOTIFY_PATTERN.matcher(reference.identifier).matches()) {
            return null;
        }

        AudioItem audioItem = null;

        try {
            final String url = new URL(reference.identifier).getPath();
            SpotifyTrack[] tracks = new SpotifyTrack[0];

            final Matcher trackMatcher = SpotifyAccess.TRACK_PATTERN.matcher(url);
            if (trackMatcher.find()) {
                tracks = SpotifyAccess.getByTrack(trackMatcher.group(1));
            }

            final Matcher albumMatcher = SpotifyAccess.ALBUM_PATTERN.matcher(url);
            if (albumMatcher.find()) {
                tracks = SpotifyAccess.getByAlbum(albumMatcher.group(1));
            }

            final Matcher playlistMatcher = SpotifyAccess.PLAYLIST_PATTERN.matcher(url);
            if (playlistMatcher.find()) {
                tracks = SpotifyAccess.getByPlaylist(playlistMatcher.group(1));
            }

            audioItem = this.getAudioItem(tracks);
        } catch (Exception exception) {
            MusicBot.LOGGER.error(exception);
        }

        return audioItem;
    }

    private AudioItem getAudioItem(final SpotifyTrack[] spotifyTracks) throws Exception {
        List<AudioTrack> tracks = new ArrayList<>(spotifyTracks.length);
        for (SpotifyTrack spotifyTrack : spotifyTracks) {
            var audioTrack = this.fromYouTube(spotifyTrack);
            if (audioTrack != null) {
                tracks.add(audioTrack);
            }
        }

        return new BasicAudioPlaylist("YouTube-List", tracks, null, false);
    }

    private AudioTrack fromYouTube(SpotifyTrack spotifyTrack) throws Exception {
        String ytId = MusicBot.DATABASE.getDatabase(CacheSpecs.YOUTUBE_CACHE).getOrPut(spotifyTrack.identifier(), spotTrack -> {
            final AudioReference reference = new AudioReference(
                    "ytmsearch:" +
                            spotifyTrack.name().replaceAll("-", "") +
                            " - " +
                            spotifyTrack.artist(),
                    ""
            );

            final AudioItem youtubeMusicItem = LavaManager.youtubeSource.loadItem(null, reference);
            if (!(youtubeMusicItem instanceof AudioPlaylist playlistItem)) {
                return null;
            }

            return WeightedTrackSelector.getWeightedTrack(spotifyTrack, playlistItem.getTracks())
                    .getLeft().getIdentifier();
        });

        try {
            final AudioItem youtubeItem = LavaManager.youtubeSource.loadTrackWithVideoId(ytId, true);
            if (youtubeItem instanceof AudioTrack ytTrack) {
                ytTrack.setUserData(spotifyTrack);
                return ytTrack;
            }
        } catch (Exception e) {
            return null;
        }

        return null;
    }

    @Override
    public boolean isTrackEncodable(AudioTrack track) {
        return false;
    }

    @Override
    public void encodeTrack(AudioTrack track, DataOutput output) throws IOException {
        // Nothing special to encode
    }

    @Override
    public AudioTrack decodeTrack(AudioTrackInfo trackInfo, DataInput input) throws IOException {
        return null;
    }

    @Override
    public void shutdown() {
        //not needed
    }
}

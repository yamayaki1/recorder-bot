package me.yamayaki.musicbot.audio.source.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.player.LavaManager;
import me.yamayaki.musicbot.storage.database.specs.impl.CacheSpecs;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.Track;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
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
                tracks = this.buildFromTrack(trackMatcher.group(1));
            }

            final Matcher albumMatcher = SpotifyAccess.ALBUM_PATTERN.matcher(url);
            if (albumMatcher.find()) {
                tracks = this.buildFromAlbum(albumMatcher.group(1));
            }

            final Matcher playlistMatcher = SpotifyAccess.PLAYLIST_PATTERN.matcher(url);
            if (playlistMatcher.find()) {
                tracks = this.buildFromPlaylist(playlistMatcher.group(1));
            }

            audioItem = this.getAudioItem(tracks);
        } catch (MalformedURLException | ExecutionException | InterruptedException exception) {
            MusicBot.LOGGER.error(exception);
        }

        return audioItem;
    }

    private SpotifyTrack[] buildFromTrack(String trackId) {
        try {
            var spotifyTrack = MusicBot.DATABASE
                    .getDatabase(CacheSpecs.SPOTIFY_CACHE)
                    .getValue(trackId);

            if (spotifyTrack.isEmpty()) {
                final Track track = SpotifyAccess.getAPI()
                        .getTrack(trackId)
                        .market(CountryCode.US)
                        .build().execute();
                spotifyTrack = Optional.of(new SpotifyTrack(track));

                MusicBot.DATABASE
                        .getDatabase(CacheSpecs.SPOTIFY_CACHE)
                        .putValue(trackId, spotifyTrack.get());
            }

            return new SpotifyTrack[]{spotifyTrack.get()};
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private SpotifyTrack[] buildFromAlbum(String albumId) {
        try {
            final Album album = SpotifyAccess.getAPI()
                    .getAlbum(albumId)
                    .build().execute();

            SpotifyTrack[] tracks = new SpotifyTrack[album.getTracks().getItems().length];
            for (int i = 0; i < album.getTracks().getItems().length; i++) {
                tracks[i] = this.buildFromTrack(album.getTracks().getItems()[i].getId())[0];
            }

            return tracks;
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private SpotifyTrack[] buildFromPlaylist(String playlistId) {
        try {
            final Playlist playlist = SpotifyAccess.getAPI()
                    .getPlaylist(playlistId)
                    .build().execute();

            SpotifyTrack[] tracks = new SpotifyTrack[playlist.getTracks().getItems().length];
            for (int i = 0; i < playlist.getTracks().getItems().length; i++) {
                tracks[i] = this.buildFromTrack(playlist.getTracks().getItems()[i].getTrack().getId())[0];
            }

            return tracks;
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioItem getAudioItem(final SpotifyTrack[] spotifyTracks) throws ExecutionException, InterruptedException {
        List<AudioTrack> tracks = new ArrayList<>(spotifyTracks.length);
        for (SpotifyTrack spotifyTrack : spotifyTracks) {
            tracks.add(this.fromYouTube(spotifyTrack));
        }

        return new BasicAudioPlaylist("YouTube-List", tracks, null, false);
    }

    private AudioTrack fromYouTube(SpotifyTrack spotifyTrack) {
        Optional<String> ytIdent = MusicBot.DATABASE
                .getDatabase(CacheSpecs.YOUTUBE_CACHE)
                .getValue(spotifyTrack.getIdentifier());

        if (ytIdent.isEmpty()) {
            final String artist = spotifyTrack.getArtist();
            final String title = spotifyTrack.getName();

            final var reference = new AudioReference("ytmsearch:" + title.replaceAll("-", "") + " - " + artist, title);
            final AudioItem youtubeMusicItem = LavaManager.youtubeSource.loadItem(null, reference);
            if (!(youtubeMusicItem instanceof AudioPlaylist playlistItem)) {
                return null;
            }

            final var weightedResult = WeightedTrackSelector
                    .getWeightedTrack(spotifyTrack, playlistItem.getTracks());

            if (weightedResult.getRight()) {
                MusicBot.DATABASE
                        .getDatabase(CacheSpecs.YOUTUBE_CACHE)
                        .putValue(spotifyTrack.getIdentifier(), weightedResult.getLeft().getIdentifier());
            }

            weightedResult.getLeft().setUserData(spotifyTrack);
            return weightedResult.getLeft();
        }

        final var reference = new AudioReference("https://youtube.com/watch?v=" + ytIdent.get(), spotifyTrack.getName());
        final AudioItem youtubeItem = LavaManager.youtubeSource.loadItem(null, reference);

        if (youtubeItem instanceof AudioTrack ytTrack) {
            ytTrack.setUserData(spotifyTrack);
            return ytTrack;
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

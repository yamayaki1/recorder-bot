package me.jerriidesu.musicbot.audio.source.spotify;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManager;
import com.sedmelluq.discord.lavaplayer.source.youtube.YoutubeAudioSourceManager;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioItem;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioReference;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import com.sedmelluq.discord.lavaplayer.track.BasicAudioPlaylist;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.audio.source.spotify.entities.SpotifyTrack;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SpotifyAudioSourceManager implements AudioSourceManager {

    private static final Pattern TRACK_PATTERN = Pattern.compile("/tracks?/([^?/\\s]*)");
    private static final Pattern PLAYLIST_PATTERN = Pattern.compile("/playlists?/([^?/\\s]*)");
    private static final Pattern ALBUM_PATTERN = Pattern.compile("/albums?/([^?/\\s]*)");

    private static final YoutubeAudioSourceManager youtube = new YoutubeAudioSourceManager();

    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private long tokenExpires = 0;

    public SpotifyAudioSourceManager() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(MusicBot.getConfig().get().getSpotify().getClientId())
                .setClientSecret(MusicBot.getConfig().get().getSpotify().getClientSecret())
                .build();
        this.clientCredentialsRequest = this.spotifyApi.clientCredentials()
                .build();

        this.renewAccessToken();
    }

    private void renewAccessToken() {
        try {
            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            this.spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            this.tokenExpires = Instant.now().getEpochSecond() + clientCredentials.getExpiresIn();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!reference.identifier.matches("(https?://)?(.*)?spotify\\.com.*")) {
            return null;
        }

        AudioItem audioItem = null;

        try {
            String url = new URL(reference.identifier).getPath();

            if (TRACK_PATTERN.matcher(url).matches()) {
                audioItem = this.buildTrack(url);
            }

            if (PLAYLIST_PATTERN.matcher(url).matches()) {
                audioItem = this.buildPlaylist(url);
            }

            if (ALBUM_PATTERN.matcher(url).matches()) {
                audioItem = this.buildPlaylistFromAlbum(url);
            }
        } catch (MalformedURLException ignored) {
        }

        return audioItem;
    }

    private AudioItem buildTrack(String url) {
        Matcher matcher = TRACK_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            if (this.tokenExpires >= Instant.now().getEpochSecond()) {
                this.renewAccessToken();
            }

            Track track = this.spotifyApi.getTrack(matcher.group(1)).build().execute();
            return this.getAudioItemFromTrack(new SpotifyTrack(track));
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioItem buildPlaylistFromAlbum(String url) {
        Matcher matcher = ALBUM_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            if (this.tokenExpires >= Instant.now().getEpochSecond()) {
                this.renewAccessToken();
            }

            Album album = this.spotifyApi.getAlbum(matcher.group(1)).build().execute();
            List<AudioTrack> tracks = new ArrayList<>();

            for (TrackSimplified item : album.getTracks().getItems()) {
                tracks.add(this.getAudioItemFromTrack(new SpotifyTrack(item)));
            }

            return new BasicAudioPlaylist(album.getName(), tracks, null, false);
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioItem buildPlaylist(String url) {
        Matcher matcher = PLAYLIST_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            if (this.tokenExpires >= Instant.now().getEpochSecond()) {
                this.renewAccessToken();
            }

            Playlist playlist = this.spotifyApi.getPlaylist(matcher.group(1)).build().execute();
            List<AudioTrack> tracks = new ArrayList<>();

            for (PlaylistTrack item : playlist.getTracks().getItems()) {
                Track track = this.spotifyApi.getTrack(item.getTrack().getId()).build().execute();
                tracks.add(this.getAudioItemFromTrack(new SpotifyTrack(track)));
            }

            return new BasicAudioPlaylist(playlist.getName(), tracks, null, false);
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioTrack getAudioItemFromTrack(SpotifyTrack track) {
        if (track == null) {
            return null;
        }

        List<AudioTrack> tracks = new ArrayList<>();

        //first search in YouTube music
        AudioItem youtubeMusicItem = youtube.loadItem(null, new AudioReference("ytmsearch:" + track.getName() + " - " + track.getArtist(), track.getName()));
        if (youtubeMusicItem instanceof AudioPlaylist audioPlaylist) {
            tracks.addAll(audioPlaylist.getTracks());
        }

        //then through normal YouTube
        AudioItem youtubeItem = youtube.loadItem(null, new AudioReference("ytsearch:" + track.getName() + " - " + track.getArtist(), track.getName()));
        if (youtubeItem instanceof AudioPlaylist audioPlaylist) {
            tracks.addAll(audioPlaylist.getTracks());
        }

        return this.weightedTrackSelector(track, tracks);
    }

    public AudioTrack weightedTrackSelector(SpotifyTrack spotifyTrack, List<AudioTrack> youtubeTracks) {
        int highest_score = -100;
        AudioTrack track = null;

        for (AudioTrack youtubeTrack : youtubeTracks) {
            String spotTitle = spotifyTrack.getName().toLowerCase(Locale.ROOT);
            String spotAuthor = spotifyTrack.getArtist().toLowerCase(Locale.ROOT);
            String ytTitle = youtubeTrack.getInfo().title.toLowerCase(Locale.ROOT);
            String ytAuthor = youtubeTrack.getInfo().author.toLowerCase(Locale.ROOT);

            int score = 0;

            if (ytTitle.equals(spotTitle) && ytAuthor.equals(spotAuthor)) {
                score = score + 1000;
            }

            if (ytAuthor.equals(spotAuthor)) {
                score = score + 5;
            } else if (ytAuthor.contains(spotAuthor)) {
                score = score + 3;
            } else {
                score = score - 20;
            }

            if (ytTitle.equals(spotTitle)) {
                score = score + 5;
            } else {
                score = score - 3;
            }

            if (ytTitle.contains(spotTitle)) {
                score = score + 1;
            } else {
                score = score - 1;
            }

            if (highest_score < score) {
                track = youtubeTrack;
                highest_score = score;
            }
        }

        return track;
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
        //TODO implement
    }
}

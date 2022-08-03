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
        if(!matcher.find()) {
            return null;
        }

        try {
            if(this.tokenExpires >= Instant.now().getEpochSecond()) {
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
        if(!matcher.find()) {
            return null;
        }

        try {
            if(this.tokenExpires >= Instant.now().getEpochSecond()) {
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
        if(!matcher.find()) {
            return null;
        }

        try {
            if(this.tokenExpires >= Instant.now().getEpochSecond()) {
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
        if(track == null) {
            return null;
        }

        AudioItem audioItem = youtube.loadItem(null, new AudioReference("ytmsearch:" + track.getName() + " - " + track.getArtist(), track.getName()));
        if(audioItem instanceof AudioTrack audioTrack) {
            return audioTrack;
        }

        if(audioItem instanceof AudioPlaylist audioPlaylist) {
            boolean selected = audioPlaylist.getSelectedTrack() != null;
            System.out.println(selected);

            return audioPlaylist.getTracks().get(0);
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
        //TODO implement
    }
}

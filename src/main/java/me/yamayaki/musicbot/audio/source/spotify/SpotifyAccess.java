package me.yamayaki.musicbot.audio.source.spotify;

import com.neovisionaries.i18n.CountryCode;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.storage.database.specs.impl.CacheSpecs;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.model_objects.specification.Paging;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.regex.Pattern;

public class SpotifyAccess {
    public static final Pattern SPOTIFY_PATTERN = Pattern.compile("(https?://)?(.*)?spotify\\.com.*");

    public static final Pattern TRACK_PATTERN = Pattern.compile("/tracks?/([^?/\\s]*)");
    public static final Pattern PLAYLIST_PATTERN = Pattern.compile("/playlists?/([^?/\\s]*)");
    public static final Pattern ALBUM_PATTERN = Pattern.compile("/albums?/([^?/\\s]*)");

    private static final SpotifyApi spotifyApi;
    private static final ClientCredentialsRequest clientCredentialsRequest;
    private static long tokenExpires = 0;

    static {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(MusicBot.CONFIG.getSetting("spotify.id"))
                .setClientSecret(MusicBot.CONFIG.getSetting("spotify.secret"))
                .build();

        clientCredentialsRequest = spotifyApi.clientCredentials()
                .build();
    }

    private static void renewAccessToken() {
        try {
            if (tokenExpires >= Instant.now().getEpochSecond()) {
                return;
            }

            final ClientCredentials clientCredentials = clientCredentialsRequest.execute();
            spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            tokenExpires = Instant.now().getEpochSecond() + clientCredentials.getExpiresIn();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public static SpotifyApi getAPI() {
        renewAccessToken();
        return spotifyApi;
    }

    public static SpotifyTrack[] getByTrack(String trackId) {
        try {
            SpotifyTrack spotifyTrack = MusicBot.DATABASE.getDatabase(CacheSpecs.SPOTIFY_CACHE).getOrPut(trackId, (id) -> {
                final Track track = getAPI()
                        .getTrack(id)
                        .market(CountryCode.US)
                        .build().execute();

                return SpotifyTrack.of(track);
            });

            return new SpotifyTrack[]{spotifyTrack};
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    //TODO: fix paging (https://github.com/spotify-web-api-java/spotify-web-api-java/issues/234#issuecomment-922890827)
    public static SpotifyTrack[] getByAlbum(String albumId) {
        try {
            final Paging<TrackSimplified> album = getAPI()
                    .getAlbumsTracks(albumId)
                    .build().execute();

            SpotifyTrack[] tracks = new SpotifyTrack[album.getItems().length];
            for (int i = 0; i < album.getItems().length; i++) {
                tracks[i] = getByTrack(album.getItems()[i].getId())[0];
            }

            return tracks;
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    //TODO: fix paging (https://github.com/spotify-web-api-java/spotify-web-api-java/issues/234#issuecomment-922890827)
    public static SpotifyTrack[] getByPlaylist(String playlistId) {
        try {
            final Paging<PlaylistTrack> playlist = getAPI()
                    .getPlaylistsItems(playlistId)
                    .build().execute();

            SpotifyTrack[] tracks = new SpotifyTrack[playlist.getItems().length];
            for (int i = 0; i < playlist.getItems().length; i++) {
                tracks[i] = getByTrack(playlist.getItems()[i].getTrack().getId())[0];
            }

            return tracks;
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }
}

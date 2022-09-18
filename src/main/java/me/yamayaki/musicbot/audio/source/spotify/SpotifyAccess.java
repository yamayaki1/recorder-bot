package me.yamayaki.musicbot.audio.source.spotify;

import me.yamayaki.musicbot.MusicBot;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.time.Instant;
import java.util.regex.Pattern;

public class SpotifyAccess {

    public static final Pattern SPOTIFY_PATTERN = Pattern.compile("(https?://)?(.*)?spotify\\.com.*");

    public static final Pattern TRACK_PATTERN = Pattern.compile("/tracks?/([^?/\\s]*)");
    public static final Pattern PLAYLIST_PATTERN = Pattern.compile("/playlists?/([^?/\\s]*)");
    public static final Pattern ALBUM_PATTERN = Pattern.compile("/albums?/([^?/\\s]*)");

    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private long tokenExpires = 0;

    public SpotifyAccess() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(MusicBot.CONFIG.get().getSpotify().getClientId())
                .setClientSecret(MusicBot.CONFIG.get().getSpotify().getClientSecret())
                .build();

        this.clientCredentialsRequest = this.spotifyApi.clientCredentials()
                .build();
    }

    private void renewAccessToken() {
        try {
            if (this.tokenExpires >= Instant.now().getEpochSecond()) {
                return;
            }

            final ClientCredentials clientCredentials = this.clientCredentialsRequest.execute();
            this.spotifyApi.setAccessToken(clientCredentials.getAccessToken());

            this.tokenExpires = Instant.now().getEpochSecond() + clientCredentials.getExpiresIn();
        } catch (IOException | SpotifyWebApiException | ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public SpotifyApi getSpotifyApi() {
        this.renewAccessToken();
        return spotifyApi;
    }
}

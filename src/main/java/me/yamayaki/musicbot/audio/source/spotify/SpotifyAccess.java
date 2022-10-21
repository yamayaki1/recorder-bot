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

    private static final SpotifyApi spotifyApi;
    private static final ClientCredentialsRequest clientCredentialsRequest;
    private static long tokenExpires = 0;

    static {
        spotifyApi = new SpotifyApi.Builder()
                .setClientId(MusicBot.CONFIG.get().getSpotify().getClientId())
                .setClientSecret(MusicBot.CONFIG.get().getSpotify().getClientSecret())
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
}

package me.yamayaki.musicbot.audio.source.spotify;

import me.yamayaki.musicbot.MusicBot;
import org.apache.hc.core5.http.ParseException;
import se.michaelthelin.spotify.SpotifyApi;
import se.michaelthelin.spotify.exceptions.SpotifyWebApiException;
import se.michaelthelin.spotify.model_objects.credentials.ClientCredentials;
import se.michaelthelin.spotify.requests.authorization.client_credentials.ClientCredentialsRequest;

import java.io.IOException;
import java.time.Instant;

public class SpotifyAccess {

    private final SpotifyApi spotifyApi;
    private final ClientCredentialsRequest clientCredentialsRequest;
    private long tokenExpires = 0;

    public SpotifyAccess() {
        this.spotifyApi = new SpotifyApi.Builder()
                .setClientId(MusicBot.getConfig().get().getSpotify().getClientId())
                .setClientSecret(MusicBot.getConfig().get().getSpotify().getClientSecret())
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

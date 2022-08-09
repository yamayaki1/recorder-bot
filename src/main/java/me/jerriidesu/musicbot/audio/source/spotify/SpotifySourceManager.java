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
import me.jerriidesu.musicbot.utils.Either;
import se.michaelthelin.spotify.model_objects.specification.Album;
import se.michaelthelin.spotify.model_objects.specification.Playlist;
import se.michaelthelin.spotify.model_objects.specification.PlaylistTrack;
import se.michaelthelin.spotify.model_objects.specification.Track;
import se.michaelthelin.spotify.model_objects.specification.TrackSimplified;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

public class SpotifySourceManager implements AudioSourceManager {

    private static YoutubeAudioSourceManager youtube;

    public SpotifySourceManager() {
        youtube = new YoutubeAudioSourceManager();
    }

    @Override
    public String getSourceName() {
        return "spotify";
    }

    @Override
    public AudioItem loadItem(AudioPlayerManager manager, AudioReference reference) {
        if (!SpotifyPatterns.SPOTIFY_PATTERN.matcher(reference.identifier).matches()) {
            return null;
        }

        AudioItem audioItem = null;

        try {
            String url = new URL(reference.identifier).getPath();

            if (SpotifyPatterns.TRACK_PATTERN.matcher(url).matches()) {
                audioItem = this.buildTrack(url);
            }

            if (SpotifyPatterns.PLAYLIST_PATTERN.matcher(url).matches()) {
                audioItem = this.buildPlaylist(url);
            }

            if (SpotifyPatterns.ALBUM_PATTERN.matcher(url).matches()) {
                audioItem = this.buildPlaylistFromAlbum(url);
            }
        } catch (MalformedURLException ignored) {
        }

        return audioItem;
    }

    private AudioItem buildTrack(String url) {
        Matcher matcher = SpotifyPatterns.TRACK_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            Track track = MusicBot.getSpotifyAccess().getSpotifyApi().getTrack(matcher.group(1)).build().execute();
            return this.getAudioItemFromTrack(new SpotifyTrack(track)).getLeft();
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioItem buildPlaylistFromAlbum(String url) {
        Matcher matcher = SpotifyPatterns.ALBUM_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            Album album = MusicBot.getSpotifyAccess().getSpotifyApi().getAlbum(matcher.group(1)).build().execute();
            List<AudioTrack> tracks = new ArrayList<>();

            for (TrackSimplified item : album.getTracks().getItems()) {
                tracks.add(this.getAudioItemFromTrack(new SpotifyTrack(item)).getLeft());
            }

            return new BasicAudioPlaylist(album.getName(), tracks, null, false);
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private AudioItem buildPlaylist(String url) {
        Matcher matcher = SpotifyPatterns.PLAYLIST_PATTERN.matcher(url);
        if (!matcher.find()) {
            return null;
        }

        try {
            Playlist playlist = MusicBot.getSpotifyAccess().getSpotifyApi().getPlaylist(matcher.group(1)).build().execute();
            List<AudioTrack> tracks = new ArrayList<>();

            for (PlaylistTrack item : playlist.getTracks().getItems()) {
                Track track = MusicBot.getSpotifyAccess().getSpotifyApi().getTrack(item.getTrack().getId()).build().execute();
                tracks.add(this.getAudioItemFromTrack(new SpotifyTrack(track)).getLeft());
            }

            return new BasicAudioPlaylist(playlist.getName(), tracks, null, false);
        } catch (Exception exception) {
            throw new FriendlyException(exception.getMessage(), FriendlyException.Severity.FAULT, exception);
        }
    }

    private Either<AudioTrack, Boolean> getAudioItemFromTrack(SpotifyTrack track) {
        if (track == null) {
            return null;
        }

        //first search in YouTube music
        AudioItem youtubeMusicItem = youtube.loadItem(null, new AudioReference("ytmsearch:" + track.getName() + " - " + track.getArtist(), track.getName()));
        if (youtubeMusicItem instanceof AudioPlaylist audioPlaylist) {
            return SpotifyWeightedTrackSelector.getWeightedTrack(track, audioPlaylist.getTracks());
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

package me.yamayaki.musicbot.database.serializers.impl;

import com.google.gson.Gson;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;

public class SpotifyTrackSerializer implements Serializer<SpotifyTrack> {
    private final Gson gson = new Gson();

    @Override
    public byte[] serialize(SpotifyTrack value) throws IOException {
        return DefaultSerializers.getSerializer(String.class).serialize(gson.toJson(value));
    }

    @Override
    public SpotifyTrack deserialize(byte[] input) throws IOException {
        return gson.fromJson(DefaultSerializers.getSerializer(String.class).deserialize(input), SpotifyTrack.class);
    }
}

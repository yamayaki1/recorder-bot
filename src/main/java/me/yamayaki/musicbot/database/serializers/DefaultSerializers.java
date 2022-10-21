package me.yamayaki.musicbot.database.serializers;

import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.serializers.impl.LongSerializer;
import me.yamayaki.musicbot.database.serializers.impl.ObjectSerializer;
import me.yamayaki.musicbot.database.serializers.impl.StringSerializer;
import me.yamayaki.musicbot.utils.ChannelInfo;

import java.util.HashMap;

public class DefaultSerializers {
    private static final HashMap<Class<?>, Serializer<?>> serializers = new HashMap<>();

    static {
        serializers.put(ChannelInfo.class, new ObjectSerializer<>(ChannelInfo.class));
        serializers.put(SpotifyTrack.class, new ObjectSerializer<>(SpotifyTrack.class));
        serializers.put(Long.class, new LongSerializer());
        serializers.put(String[].class, new ObjectSerializer<>(String[].class));
        serializers.put(String.class, new StringSerializer());
    }

    @SuppressWarnings("unchecked")
    public static <K> Serializer<K> getSerializer(Class<K> clazz) {
        Serializer<?> serializer = serializers.get(clazz);

        if (serializer == null) {
            throw new NullPointerException("No serializer exists for type: " + clazz.getName());
        }

        return (Serializer<K>) serializer;
    }
}

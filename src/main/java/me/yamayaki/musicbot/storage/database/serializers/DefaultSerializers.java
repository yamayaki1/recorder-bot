package me.yamayaki.musicbot.storage.database.serializers;

import me.yamayaki.musicbot.entities.ChannelCopy;
import me.yamayaki.musicbot.entities.ChannelMessagePair;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.entities.TrackInfo;
import me.yamayaki.musicbot.storage.database.serializers.impl.JSONSerializer;
import me.yamayaki.musicbot.storage.database.serializers.impl.LongSerializer;
import me.yamayaki.musicbot.storage.database.serializers.impl.StringSerializer;
import me.yamayaki.musicbot.storage.database.serializers.impl.entities.ChannelMessagePairSerializer;

import java.util.HashMap;

public class DefaultSerializers {
    private static final HashMap<Class<?>, Serializer<?>> serializers = new HashMap<>();

    static {

        serializers.put(ChannelMessagePair.class, new ChannelMessagePairSerializer());

        serializers.put(ChannelCopy.class, new JSONSerializer<>(ChannelCopy.class));
        serializers.put(SpotifyTrack.class, new JSONSerializer<>(SpotifyTrack.class));
        serializers.put(TrackInfo[].class, new JSONSerializer<>(TrackInfo[].class));

        serializers.put(Long.class, new LongSerializer());
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

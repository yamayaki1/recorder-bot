package me.yamayaki.musicbot.database.serializers;

import it.unimi.dsi.fastutil.objects.Reference2ReferenceMap;
import it.unimi.dsi.fastutil.objects.Reference2ReferenceOpenHashMap;
import me.yamayaki.musicbot.audio.source.spotify.SpotifyTrack;
import me.yamayaki.musicbot.database.serializers.impl.LongSerializer;
import me.yamayaki.musicbot.database.serializers.impl.JsonSerializer;
import me.yamayaki.musicbot.database.serializers.impl.StringArrSerializer;
import me.yamayaki.musicbot.database.serializers.impl.StringSerializer;
import me.yamayaki.musicbot.utils.ChannelInfo;

public class DefaultSerializers {
    private static final Reference2ReferenceMap<Class<?>, Serializer<?>> serializers = new Reference2ReferenceOpenHashMap<>();

    static {
        serializers.put(ChannelInfo.class, new JsonSerializer<>(ChannelInfo.class));
        serializers.put(SpotifyTrack.class, new JsonSerializer<>(SpotifyTrack.class));
        serializers.put(Long.class, new LongSerializer());
        serializers.put(String[].class, new StringArrSerializer());
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

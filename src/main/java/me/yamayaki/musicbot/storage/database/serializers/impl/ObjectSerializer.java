package me.yamayaki.musicbot.storage.database.serializers.impl;

import me.yamayaki.musicbot.storage.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.storage.database.serializers.Serializer;
import me.yamayaki.musicbot.utils.GsonHolder;

import java.io.IOException;

public class ObjectSerializer<T> implements Serializer<T> {
    private final Class<T> clazz;

    public ObjectSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T value) throws IOException {
        try {
            return DefaultSerializers.getSerializer(String.class).serialize(GsonHolder.getGson().toJson(value));
        } catch (Exception e) {
            throw new IOException("cant write json:", e);
        }
    }

    @Override
    public T deserialize(byte[] input) throws IOException {
        try {
            return GsonHolder.getGson().fromJson(DefaultSerializers.getSerializer(String.class).deserialize(input), clazz);
        } catch (Exception e) {
            throw new IOException("cant read json:", e);
        }
    }
}

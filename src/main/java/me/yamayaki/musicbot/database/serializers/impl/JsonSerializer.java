package me.yamayaki.musicbot.database.serializers.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;

public class JsonSerializer<T> implements Serializer<T> {
    private final Class<T> clazz;
    private final Gson gson = new GsonBuilder().setLenient().create();

    public JsonSerializer(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public byte[] serialize(T value) throws IOException {
        try {
            return DefaultSerializers.getSerializer(String.class).serialize(gson.toJson(value));
        } catch (Exception e) {
            throw new IOException("cant write json:", e);
        }
    }

    @Override
    public T deserialize(byte[] input) throws IOException {
        try {
            return gson.fromJson(DefaultSerializers.getSerializer(String.class).deserialize(input), clazz);
        } catch (Exception e) {
            throw new IOException("cant read json:", e);
        }
    }
}

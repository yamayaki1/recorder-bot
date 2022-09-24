package me.yamayaki.musicbot.database.serializers.impl;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import me.yamayaki.musicbot.database.serializers.DefaultSerializers;
import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;

public class StringArrSerializer implements Serializer<String[]> {
    private final Gson gson = new GsonBuilder().setLenient().create();

    @Override
    public byte[] serialize(String[] value) throws IOException {
        try {
            return DefaultSerializers.getSerializer(String.class).serialize(gson.toJson(value));
        } catch (Exception e) {
            throw new IOException("cant write json:", e);
        }
    }

    @Override
    public String[] deserialize(byte[] input) throws IOException {
        try {
            return gson.fromJson(DefaultSerializers.getSerializer(String.class).deserialize(input), String[].class);
        } catch (Exception e) {
            throw new IOException("cant read json:", e);
        }
    }
}

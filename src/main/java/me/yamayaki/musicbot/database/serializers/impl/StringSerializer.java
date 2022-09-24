package me.yamayaki.musicbot.database.serializers.impl;

import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer<String> {
    @Override
    public byte[] serialize(String value) throws IOException {
        return value.getBytes(StandardCharsets.UTF_8);
    }

    @Override
    public String deserialize(byte[] input) throws IOException {
        return new String(input, StandardCharsets.UTF_8);
    }
}

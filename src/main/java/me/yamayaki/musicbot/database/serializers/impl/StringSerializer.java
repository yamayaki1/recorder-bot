package me.yamayaki.musicbot.database.serializers.impl;

import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class StringSerializer implements Serializer<String> {
    @Override
    public byte[] serialize(String value) throws IOException {
        return StandardCharsets.UTF_8.encode(value).array();
    }

    @Override
    public String deserialize(byte[] input) throws IOException {
        return StandardCharsets.UTF_8.decode(ByteBuffer.wrap(input))
                .toString();
    }
}

package me.yamayaki.musicbot.database.serializers.impl;

import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;
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

    @Override
    public byte[] serialize(Integer prefix, String key) throws IOException {
        try {
            byte[] val = key.getBytes(StandardCharsets.UTF_8);

            ByteBuffer buf = ByteBuffer.allocateDirect(val.length + 4);
            buf.putInt(0, prefix);
            buf.put(4, val);

            byte[] arr = new byte[buf.remaining()];
            buf.get(arr);

            return arr;
        } catch (Exception e) {
            throw new IOException("cant write json:", e);
        }
    }
}

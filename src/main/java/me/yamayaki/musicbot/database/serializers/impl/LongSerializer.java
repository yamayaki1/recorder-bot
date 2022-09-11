package me.yamayaki.musicbot.database.serializers.impl;

import me.yamayaki.musicbot.database.serializers.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class LongSerializer implements Serializer<Long> {
    @Override
    public byte[] serialize(Long value) throws IOException {
        ByteBuffer buf = ByteBuffer.allocateDirect(8);
        buf.putLong(0, value);

        byte[] arr = new byte[buf.remaining()];
        buf.get(arr);

        return arr;
    }

    @Override
    public Long deserialize(byte[] input) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(input);
        return buf.getLong(0);
    }
}

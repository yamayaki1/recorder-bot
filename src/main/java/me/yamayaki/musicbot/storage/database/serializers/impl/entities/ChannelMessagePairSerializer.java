package me.yamayaki.musicbot.storage.database.serializers.impl.entities;

import me.yamayaki.musicbot.entities.ChannelMessagePair;
import me.yamayaki.musicbot.storage.database.serializers.Serializer;

import java.io.IOException;
import java.nio.ByteBuffer;

public class ChannelMessagePairSerializer implements Serializer<ChannelMessagePair> {
    @Override
    public byte[] serialize(ChannelMessagePair value) throws IOException {
        ByteBuffer buffer = ByteBuffer.allocateDirect(16);
        buffer.putLong(0, value.channel());
        buffer.putLong(8, value.message());

        byte[] arr = new byte[buffer.remaining()];
        buffer.get(arr);

        return arr;
    }

    @Override
    public ChannelMessagePair deserialize(byte[] input) throws IOException {
        ByteBuffer buffer = ByteBuffer.wrap(input);
        return new ChannelMessagePair(buffer.getLong(0), buffer.getLong(8));
    }
}

package me.yamayaki.musicbot.database.serializers;

import java.io.IOException;

public interface Serializer<T> {
    byte[] serialize(T value) throws IOException;

    T deserialize(byte[] input) throws IOException;
}

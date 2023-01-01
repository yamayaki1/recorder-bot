package me.yamayaki.musicbot.entities;

import org.javacord.api.entity.message.Message;

public record ChannelMessagePair(long channel, long message) {
    public static ChannelMessagePair of(Message message) {
        return new ChannelMessagePair(message.getChannel().getId(), message.getId());
    }
}

package me.yamayaki.musicbot.storage.database.specs.impl;

import me.yamayaki.musicbot.entities.ChannelCopy;
import me.yamayaki.musicbot.entities.ChannelMessagePair;
import me.yamayaki.musicbot.storage.database.specs.DatabaseSpec;

public class ChannelSpecs {
    public static final DatabaseSpec<Long, ChannelCopy> CHANNEL_SETTINGS =
            new DatabaseSpec<>("channel_settings", Long.class, ChannelCopy.class);

    public static final DatabaseSpec<Long, ChannelMessagePair> SERVER_PLAYERCHANNEL =
            new DatabaseSpec<>("server_playerchannel", Long.class, ChannelMessagePair.class);
}

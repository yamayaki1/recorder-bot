package me.yamayaki.musicbot.database.specs.impl;

import me.yamayaki.musicbot.database.specs.DatabaseSpec;
import me.yamayaki.musicbot.utils.ChannelInfo;
import me.yamayaki.musicbot.utils.ChannelMessagePair;

public class ChannelSpecs {
    public static final DatabaseSpec<Long, ChannelInfo> CHANNEL_SETTINGS =
            new DatabaseSpec<>("channel_settings", Long.class, ChannelInfo.class);

    public static final DatabaseSpec<Long, ChannelMessagePair> SERVER_PLAYERCHANNEL =
            new DatabaseSpec<>("server_playerchannel", Long.class, ChannelMessagePair.class);
}

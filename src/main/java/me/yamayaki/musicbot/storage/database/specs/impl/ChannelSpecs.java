package me.yamayaki.musicbot.storage.database.specs.impl;

import me.yamayaki.musicbot.storage.database.specs.DatabaseSpec;
import me.yamayaki.musicbot.entities.ChannelInfo;
import me.yamayaki.musicbot.utilities.Pair;

public class ChannelSpecs {
    public static final DatabaseSpec<Long, ChannelInfo> CHANNEL_SETTINGS =
            new DatabaseSpec<>("channel_settings", Long.class, ChannelInfo.class);

    public static final DatabaseSpec<Long, Pair<String, String>> SERVER_PLAYERCHANNEL =
            (DatabaseSpec<Long, Pair<String, String>>) new DatabaseSpec<>("server_playerchannel", Long.class, new Pair<>("", "").getClass());
}

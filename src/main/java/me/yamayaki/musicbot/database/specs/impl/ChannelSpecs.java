package me.yamayaki.musicbot.database.specs.impl;

import me.yamayaki.musicbot.database.specs.DatabaseSpec;

public class ChannelSpecs {
    public static final DatabaseSpec<Long, Long> CHANNEL_SETTINGS =
            new DatabaseSpec<>("channel_settings", Long.class, Long.class);

}

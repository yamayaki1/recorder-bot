package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.database.DatabaseInstance;
import me.yamayaki.musicbot.database.RocksManager;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;

public class ChannelSettings {
    private final RocksManager channelSettings;

    public ChannelSettings(RocksManager rocksManager) {
        this.channelSettings = rocksManager;
    }

    public DatabaseInstance<Long, ChannelInfo> getChannelSettings() {
        return this.channelSettings.getDatabase(ChannelSpecs.CHANNEL_SETTINGS);
    }
}

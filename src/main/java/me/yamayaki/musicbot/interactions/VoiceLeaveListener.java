package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.utils.ChannelInfo;
import me.yamayaki.musicbot.utils.ChannelUtilities;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

import java.util.Optional;

public class VoiceLeaveListener implements ServerVoiceChannelMemberLeaveListener {

    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if(event.getChannel().getConnectedUsers().size() != 0) {
            return;
        }

        Optional<ChannelInfo> channelInfo = MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .getValue(event.getChannel().getId());

        channelInfo.ifPresent(info -> ChannelUtilities.disableGhostChannel(info, event.getChannel()));
    }
}

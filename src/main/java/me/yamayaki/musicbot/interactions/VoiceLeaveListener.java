package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.storage.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.entities.ChannelCopy;
import me.yamayaki.musicbot.utilities.ChannelUtilities;
import org.javacord.api.event.channel.server.voice.ServerVoiceChannelMemberLeaveEvent;
import org.javacord.api.listener.channel.server.voice.ServerVoiceChannelMemberLeaveListener;

import java.util.Optional;

public class VoiceLeaveListener implements ServerVoiceChannelMemberLeaveListener {
    @Override
    public void onServerVoiceChannelMemberLeave(ServerVoiceChannelMemberLeaveEvent event) {
        this.handleBotLeave(event);
        this.handleGhostChannel(event);
    }

    private void handleBotLeave(ServerVoiceChannelMemberLeaveEvent event) {
        if (!(event.getUser().equals(event.getApi().getYourself()))) {
            return;
        }

        MusicBot.instance().getAudioManager(event.getServer()).shutdown(false);
    }

    private void handleGhostChannel(ServerVoiceChannelMemberLeaveEvent event) {
        if (event.getChannel().getConnectedUsers().size() != 0) {
            return;
        }

        long channelId = event.getChannel().getId();

        Optional<ChannelCopy> channelInfo = MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .getValue(channelId);

        channelInfo.ifPresent(info -> {
            MusicBot.LOGGER.info("deactivating ghost-channel {}, as all users have leaved.", channelId);
            ChannelUtilities.disableGhostChannel(info, event.getChannel());
        });
    }
}

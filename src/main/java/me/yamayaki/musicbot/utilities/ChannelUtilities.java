package me.yamayaki.musicbot.utilities;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.ChannelCopy;
import me.yamayaki.musicbot.storage.database.specs.impl.ChannelSpecs;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.ServerVoiceChannelUpdater;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.permission.Role;
import org.javacord.api.entity.server.Server;
import org.javacord.api.entity.user.User;

import java.util.concurrent.CompletableFuture;

public class ChannelUtilities {
    public static void activateGhostChannel(ServerVoiceChannel originalChannel) {
        Server server = originalChannel.getServer();
        ChannelCopy channelCopy = ChannelCopy.of(originalChannel);

        //create channel copy
        ServerVoiceChannel copiedChannel = channelCopy.newFrom(server);
        channelCopy.setAssociatedChannel(copiedChannel.getId());

        //save original channel-data
        MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .putValue(originalChannel.getId(), channelCopy);

        //update original channel
        setCurrentPrivate(originalChannel).join();
    }

    public static void disableGhostChannel(ChannelCopy channelCopy, ServerVoiceChannel voiceChannel) {
        voiceChannel.getServer()
                .getVoiceChannelById(channelCopy.getAssociatedChannel())
                .ifPresent(ServerChannel::delete);

        channelCopy.into(voiceChannel, true).join();

        MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .deleteValue(voiceChannel.getId());
    }

    private static CompletableFuture<Void> setCurrentPrivate(ServerVoiceChannel serverVoiceChannel) {
        ServerVoiceChannelUpdater voiceChannelUpdater = serverVoiceChannel.createUpdater();

        //remove existing permission overwrites
        serverVoiceChannel.getOverwrittenRolePermissions().forEach((roleId, permissions) -> {
            Role role = serverVoiceChannel.getServer().getRoleById(roleId).orElse(null);
            voiceChannelUpdater.removePermissionOverwrite(role);
        });

        serverVoiceChannel.getOverwrittenUserPermissions().forEach((userId, permissions) -> {
            User user = serverVoiceChannel.getServer().getMemberById(userId).orElse(null);
            voiceChannelUpdater.removePermissionOverwrite(user);
        });

        //deny everyone from seeing channel
        Role everyoneRole = serverVoiceChannel.getServer().getEveryoneRole();
        voiceChannelUpdater.addPermissionOverwrite(everyoneRole, new PermissionsBuilder().setDenied(PermissionType.VIEW_CHANNEL).build());

        //allow currently connected users to interact with this channel
        serverVoiceChannel.getConnectedUsers().forEach(user -> {
            Permissions allowed = new PermissionsBuilder()
                    .setAllowed(PermissionType.MOVE_MEMBERS, PermissionType.SPEAK)
                    .build();

            voiceChannelUpdater.addPermissionOverwrite(user, allowed);
        });

        return voiceChannelUpdater.update();
    }
}

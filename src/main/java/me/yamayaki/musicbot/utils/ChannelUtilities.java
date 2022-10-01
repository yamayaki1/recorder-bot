package me.yamayaki.musicbot.utils;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.channel.VoiceChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ChannelUtilities {
    public static void joinVoiceChannel(Either<SlashCommandInteraction, Server> either, Runnable onSuccess, Runnable onError) {
        var optChannel = either.getLeft().getUser()
                .getConnectedVoiceChannel(either.getRight());
        var curChannel = either.getRight()
                .getAudioConnection();

        if (curChannel.isPresent() && curChannel.get().getChannel().equals(optChannel.orElse(null))) {
            onSuccess.run();
            return;
        }

        optChannel.ifPresentOrElse(voiceChannel -> {
            voiceChannel.connect().thenAccept(audioConnection -> {
                audioConnection.setSelfDeafened(true);
                onSuccess.run();
            });
        }, onError);
    }

    public static void activateGhostChannel(ServerVoiceChannel voiceChannel) {
        var server = voiceChannel.getServer();
        var rolePermissions = voiceChannel.getOverwrittenRolePermissions();
        var userPermissions = voiceChannel.getOverwrittenUserPermissions();

        //create fake channel
        var channelBuilder = server.createVoiceChannelBuilder()
                .setName(voiceChannel.getName())
                .setUserlimit(voiceChannel.getUserLimit().orElse(0))
                .setCategory(voiceChannel.getCategory().orElse(null));
        rolePermissions.forEach((roleId, permission) -> channelBuilder.addPermissionOverwrite(server.getRoleById(roleId).orElse(null), permission));

        var newChannel = channelBuilder.create().join();
        newChannel.createUpdater().setRawPosition(voiceChannel.getRawPosition()).update().join();

        //save original channel-data
        MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .putValue(voiceChannel.getId(), new ChannelInfo(newChannel.getId(), rolePermissions, userPermissions, voiceChannel.getName(), voiceChannel.getUserLimit()));

        //update original channel
        var originalUpdater = voiceChannel.createUpdater();
        rolePermissions.forEach((roleId, permission) -> originalUpdater.removePermissionOverwrite(server.getRoleById(roleId).orElse(null)));
        originalUpdater.addPermissionOverwrite(server.getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.VIEW_CHANNEL).build());
        voiceChannel.getConnectedUsers().forEach(user -> originalUpdater.addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS, PermissionType.SPEAK).build()));

        originalUpdater.update().join();
    }

    public static void disableGhostChannel(ChannelInfo channelInfo, ServerVoiceChannel voiceChannel) {
        var server = voiceChannel.getServer();

        var fakeChannelOpt = server
                .getVoiceChannelById(channelInfo.newChannel);
        fakeChannelOpt.ifPresent(ServerChannel::delete);

        //update original channel
        var originalUpdater = voiceChannel.createUpdater();
        voiceChannel.getOverwrittenUserPermissions().forEach((userId, permissions) -> originalUpdater.removePermissionOverwrite(server.getMemberById(userId).orElse(null)));
        channelInfo.userPermissions.forEach((userId, permissions) -> originalUpdater.addPermissionOverwrite(server.getMemberById(userId).orElse(null), Permissions.fromBitmask(permissions[0], permissions[1])));
        channelInfo.rolePermissions.forEach((roleId, permissions) -> originalUpdater.addPermissionOverwrite(server.getRoleById(roleId).orElse(null), Permissions.fromBitmask(permissions[0], permissions[1])));

        originalUpdater.update().join();

        MusicBot.DATABASE
                .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                .deleteValue(voiceChannel.getId());
    }
}

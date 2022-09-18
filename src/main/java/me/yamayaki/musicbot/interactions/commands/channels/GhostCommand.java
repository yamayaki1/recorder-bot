package me.yamayaki.musicbot.interactions.commands.channels;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.ChannelInfo;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ServerChannel;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.permission.Permissions;
import org.javacord.api.entity.permission.PermissionsBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class GhostCommand implements Command {
    @Override
    public String getName() {
        return "ghost";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.withRequiredPermissions(getName(), "Lösche alle Lieder aus der Playlist.", PermissionType.ADMINISTRATOR)
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var userChannel = either.getLeft().getUser().getConnectedVoiceChannel(either.getRight());

        userChannel.ifPresentOrElse(voiceChannel -> {
            var channelInfoOpt = MusicBot.getChannelSettings().getChannelSettings().getValue(voiceChannel.getId());

            channelInfoOpt.ifPresentOrElse(channelInfo -> {
                var fakeChannelOpt = either.getRight()
                        .getVoiceChannelById(channelInfo.newChannel);
                fakeChannelOpt.ifPresent(ServerChannel::delete);

                var originalUpdater = voiceChannel.createUpdater()
                        .setName(channelInfo.name)
                        .setUserLimit(channelInfo.userLimit);
                voiceChannel.getOverwrittenUserPermissions().forEach((userId, permissions)-> originalUpdater.removePermissionOverwrite(either.getRight().getMemberById(userId).orElse(null)));
                channelInfo.userPermissions.forEach((userId, permissions)-> originalUpdater.addPermissionOverwrite(either.getRight().getMemberById(userId).orElse(null), Permissions.fromBitmask(permissions[0], permissions[1])));
                channelInfo.rolePermissions.forEach((roleId, permissions)-> originalUpdater.addPermissionOverwrite(either.getRight().getRoleById(roleId).orElse(null), Permissions.fromBitmask(permissions[0], permissions[1])));

                originalUpdater.update().join();

                MusicBot.getChannelSettings().getChannelSettings().deleteValue(voiceChannel.getId());

                interUpdater.setContent("Der Ghost-Kanal wurde gelöscht.").update();
            }, () -> {
                var rolePermissions = voiceChannel.getOverwrittenRolePermissions();
                var userPermissions = voiceChannel.getOverwrittenUserPermissions();

                //create fake channel
                var channelBuilder = either.getRight().createVoiceChannelBuilder()
                        .setName(voiceChannel.getName())
                        .setUserlimit(voiceChannel.getUserLimit().orElse(0))
                        .setRawPosition(voiceChannel.getRawPosition())
                        .setCategory(voiceChannel.getCategory().orElse(null));
                rolePermissions.forEach((roleId, permission)-> channelBuilder.addPermissionOverwrite(either.getRight().getRoleById(roleId).orElse(null), permission));

                var newChannel = channelBuilder.create().join();

                //save original channel-data
                MusicBot.getChannelSettings().getChannelSettings().putValue(voiceChannel.getId(), new ChannelInfo(newChannel.getId(), rolePermissions, userPermissions, voiceChannel.getName(), voiceChannel.getUserLimit()));

                //update original channel
                var originalUpdater = voiceChannel.createUpdater()
                        .setName(voiceChannel.getName()+" \uD83D\uDC7B")
                        .setUserLimit(voiceChannel.getConnectedUserIds().size());

                rolePermissions.forEach((roleId, permission)-> originalUpdater.removePermissionOverwrite(either.getRight().getRoleById(roleId).orElse(null)));
                originalUpdater.addPermissionOverwrite(either.getRight().getEveryoneRole(), new PermissionsBuilder().setDenied(PermissionType.VIEW_CHANNEL).build());
                voiceChannel.getConnectedUsers().forEach(user-> originalUpdater.addPermissionOverwrite(user, new PermissionsBuilder().setAllowed(PermissionType.MOVE_MEMBERS, PermissionType.SPEAK).build()));

                originalUpdater.update().join();
                interUpdater.setContent("Der Ghost-Kanal wurde erstellt.").update();
            });
        }, () -> interUpdater.setContent("Du befindest dich ein keinen Voice-Kanal!").update());
    }
}

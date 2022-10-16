package me.yamayaki.musicbot.interactions.commands.channels;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.database.specs.impl.ChannelSpecs;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.ChannelUtilities;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
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
        return SlashCommand.withRequiredPermissions(getName(), "Move alle User in einen temporären Channel.", PermissionType.ADMINISTRATOR)
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var userChannel = either.getLeft().getUser().getConnectedVoiceChannel(either.getRight());

        userChannel.ifPresentOrElse(voiceChannel -> {
            var channelInfoOpt = MusicBot.DATABASE
                    .getDatabase(ChannelSpecs.CHANNEL_SETTINGS)
                    .getValue(voiceChannel.getId());

            channelInfoOpt.ifPresentOrElse(channelInfo -> {
                ChannelUtilities.disableGhostChannel(channelInfo, voiceChannel);
                interUpdater.setContent("Der Ghost-Kanal wurde gelöscht.").update();
            }, () -> {
                ChannelUtilities.activateGhostChannel(voiceChannel);
                interUpdater.setContent("Der Ghost-Kanal wurde erstellt.").update();
            });
        }, () -> interUpdater.setContent("Du befindest dich ein keinen Voice-Kanal!").update());
    }
}
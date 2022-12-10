package me.yamayaki.musicbot.interactions.commands.channels;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;

import java.util.Arrays;

public class PlayerChannelCommand implements Command {
    @Override
    public String getName() {
        return "playerchannel";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.withRequiredPermissions(getName(), "Setzte den Kanal, der für den Musikbot verwendet werden soll.", PermissionType.ADMINISTRATOR)
                .addOption(
                        SlashCommandOption.createChannelOption("channel", "Der zu verwendende Kanal", true, Arrays.asList(ChannelType.getTextChannelTypes()))
                )
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        boolean success = MusicBot.instance().getAudioManager(either.getRight())
                .getPlayerControl()
                .setPlayerChannel(either.getLeft().getArgumentChannelValueByName("channel").get());

        String content = success ? "Kanal erfolgreich gesetzt." : "Beim setzen des Kanals ist ein Fehler aufgetreten!";
        interUpdater.setContent(content).update();
    }
}

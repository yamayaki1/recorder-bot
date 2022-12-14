package me.yamayaki.musicbot.interactions.commands.channels;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.channel.ChannelType;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandOption;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.Arrays;

public class PlayerChannelCommand implements ApplicationInteraction {
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
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        boolean success = MusicBot.instance().getAudioManager(interaction.getServer().orElseThrow())
                .getPlayerControl()
                .setPlayerChannel(interaction.getArgumentChannelValueByName("channel").orElseThrow());

        String content = success ? "Kanal erfolgreich gesetzt." : "Beim setzen des Kanals ist ein Fehler aufgetreten!";
        updater.setContent(content).update();
    }
}

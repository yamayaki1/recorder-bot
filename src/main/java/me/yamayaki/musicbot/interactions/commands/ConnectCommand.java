package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.ChannelUtilities;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ConnectCommand implements Command {
    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Verbinde den Bot mit einen Sprachkanal.")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        ChannelUtilities.joinVoiceChannel(either, () -> {
            interUpdater.setContent("Verbindung hergstellt.")
                    .update();
        }, () -> {
            interUpdater.setContent("Beim Beitreten des Sprachkanals ist ein Fehler aufgetreten. Befindest du dich in einen Kanal?")
                    .update();
        });
    }
}

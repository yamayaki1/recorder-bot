package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class DisconnectCommand implements Command {
    @Override
    public String getName() {
        return "disconnect";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Trenne die Verbindung des Bots.")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var optConnection = either.getRight()
                .getAudioConnection();

        optConnection.ifPresentOrElse(audioConnection -> {
            MusicBot.getAudioManager().removeTrackManager(either.getRight());

            try {
                audioConnection.close();
            } catch(NullPointerException e) {
                interUpdater.setContent("Es ist ein Fehler aufgetreten, starte Bot neu!").update().join();
                System.exit(-1);
            }

            interUpdater.setContent("Verbindung getrennt.").update();
        }, () -> interUpdater.setContent("Es ist ein Fehler aufgetreten!").update());
    }
}

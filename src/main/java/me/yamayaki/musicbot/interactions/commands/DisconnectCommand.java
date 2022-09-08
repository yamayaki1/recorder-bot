package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class DisconnectCommand implements Command {
    @Override
    public String getName() {
        return "disconnect";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Trenne die Verbindung des Bots.")
                .setEnabledInDms(false)
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var optConnection = either.getRight()
                .getAudioConnection();

        optConnection.ifPresentOrElse(audioConnection -> {
            MusicBot.getAudioManager().removeTrackManager(either.getRight());
            audioConnection.close();

            interUpdater.setContent("Verbindung getrennt.").update();
        }, () -> interUpdater.setContent("Es ist ein Fehler aufgetreten!").update());
    }
}
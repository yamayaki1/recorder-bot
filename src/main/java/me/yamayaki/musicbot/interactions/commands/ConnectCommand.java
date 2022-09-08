package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ConnectCommand implements Command {
    @Override
    public String getName() {
        return "connect";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Verbinde den Bot mit einem Sprachkanal.")
                .setEnabledInDms(false)
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        var optChannel = either.getLeft().getUser()
                .getConnectedVoiceChannel(either.getRight());

        optChannel.ifPresentOrElse(voiceChannel -> {
            voiceChannel.connect().thenAccept(audioConnection -> {
                audioConnection.setSelfDeafened(true);
                interUpdater.setContent("Verbindung hergstellt.").update();
            });
        }, () -> {
            interUpdater.setContent("Beim Beitreten des Sprachkanals ist ein Fehler aufgetreten. Befindest du dich in einem Kanal?").update();
        });
    }
}

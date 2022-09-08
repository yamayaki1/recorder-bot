package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class PauseCommand implements Command {
    @Override
    public String getName() {
        return "pause";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Pausiere das aktuelle Lied.")
                .setEnabledInDms(false)
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .pauseTrack();

        interUpdater.setContent("Aktuelles Lied pausiert.").update();
    }
}

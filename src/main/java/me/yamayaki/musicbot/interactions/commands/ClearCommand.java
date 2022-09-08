package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ClearCommand implements Command {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Lösche alle Lieder aus der Playlist.")
                .setEnabledInDms(false)
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();

        MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .clearTracks();

        interUpdater.setContent("Playlist gecleart.").update();
    }
}

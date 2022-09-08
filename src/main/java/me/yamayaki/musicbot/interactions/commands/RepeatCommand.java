package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;

public class RepeatCommand implements Command {
    @Override
    public String getName() {
        return "repeat";
    }

    @Override
    public void register(DiscordApi api) {
        SlashCommand.with(getName(), "Wiederhole die aktuelle Playlist.")
                .setEnabledInDms(false)
                .createGlobal(api).join();
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        boolean repeat = MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .toggleRepeat();

        interUpdater.setContent(repeat ? "Wiederholen eingeschaltet." : "Wiederholen ausgeschaltet.").update();
    }
}

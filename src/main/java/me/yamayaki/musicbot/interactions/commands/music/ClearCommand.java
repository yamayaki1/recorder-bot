package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ClearCommand implements Command {
    @Override
    public String getName() {
        return "clear";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Lösche alle Lieder aus der Playlist.")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(SlashCommandInteraction interaction) {
        var interUpdater = interaction.respondLater(true).join();

        MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow())
                .getPlaylist()
                .clear();

        interUpdater.setContent("Alle Lieder aus der Playlist entfernt.").update();
    }
}

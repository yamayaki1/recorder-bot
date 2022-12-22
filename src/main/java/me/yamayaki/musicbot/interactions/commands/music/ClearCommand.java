package me.yamayaki.musicbot.interactions.commands.music;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

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
    public void execute(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow())
                .getPlaylist()
                .clear();

        updater.setContent("Alle Lieder aus der Playlist entfernt.").update();
    }
}

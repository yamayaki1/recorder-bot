package me.yamayaki.musicbot.interactions;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

public interface Command {
    String getName();

    SlashCommandBuilder register(DiscordApi api);

    void execute(SlashCommandInteraction interaction);
}

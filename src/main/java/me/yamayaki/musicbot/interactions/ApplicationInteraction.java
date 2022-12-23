package me.yamayaki.musicbot.interactions;

import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.UserContextMenuInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public interface ApplicationInteraction {
    String getName();
    ApplicationCommandBuilder<?, ?, ?> register(DiscordApi api);

    default boolean isExperimental() {
        return false;
    }

    default void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater response) {
        throw new IllegalArgumentException("Not implemented!");
    }

    default void executeContext(UserContextMenuInteraction interaction, InteractionOriginalResponseUpdater response) {
        throw new IllegalArgumentException("Not implemented!");
    }
}

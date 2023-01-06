package me.yamayaki.musicbot.interactions.commands.utilities;

import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.permission.PermissionType;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public class ShutdownCommand implements ApplicationInteraction {
    @Override
    public String getName() {
        return "shutdown";
    }

    @Override
    public boolean isExperimental() {
        return true;
    }

    @Override
    public ApplicationCommandBuilder<?, ?, ?> register(DiscordApi api) {
        return SlashCommand.withRequiredPermissions(getName(), "Fährt den Bot herunter.", PermissionType.ADMINISTRATOR)
                .setEnabledInDms(false);
    }

    @Override
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater response) {
        response.setContent("Bot wird heruntergefahren ...").update().join();
        System.exit(15);
    }
}

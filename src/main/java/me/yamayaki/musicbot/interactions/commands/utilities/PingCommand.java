package me.yamayaki.musicbot.interactions.commands.utilities;

import me.yamayaki.musicbot.interactions.Command;
import org.javacord.api.DiscordApi;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public class PingCommand implements Command {
    @Override
    public String getName() {
        return "ping";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Liefert den Ping des Bots zurück.")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        updater.setContent("Pong! Zugriffszeit beträgt " + interaction.getApi().getLatestGatewayLatency().toMillis() + "ms.")
                .update();
    }
}

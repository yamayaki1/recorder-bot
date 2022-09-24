package me.yamayaki.musicbot.interactions.commands;

import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.MessageFlag;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

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
    public void execute(Either<SlashCommandInteraction, Server> either) {
        either.getLeft().createImmediateResponder()
                .setFlags(MessageFlag.EPHEMERAL)
                .setContent("Pong! Zugriffszeit beträgt " + either.getLeft().getApi().getLatestGatewayLatency().toMillis() + "ms.")
                .respond();

    }
}
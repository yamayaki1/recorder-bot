package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

public interface Command {
    String getName();

    void register(DiscordApi api);

    void execute(Either<SlashCommandInteraction, Server> either);
}

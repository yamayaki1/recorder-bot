package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.commands.channels.GhostCommand;
import me.yamayaki.musicbot.interactions.commands.channels.PlayerChannelCommand;
import me.yamayaki.musicbot.interactions.commands.music.ClearCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlayCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlayerCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlaylistCommand;
import me.yamayaki.musicbot.interactions.commands.music.SkipCommand;
import me.yamayaki.musicbot.interactions.commands.utilities.AboutCommand;
import me.yamayaki.musicbot.interactions.commands.utilities.PingCommand;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Threads;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

public class InteractionListener implements SlashCommandCreateListener {
    private final HashMap<String, Command> commands = new HashMap<>();

    public InteractionListener(DiscordApi discordApi) {
        registerCommands(discordApi,
                new GhostCommand(),
                new PlayerChannelCommand(),

                new ClearCommand(),
                new PlayCommand(),
                new PlayerCommand(),
                new PlaylistCommand(),
                new SkipCommand(),

                new AboutCommand(),
                new PingCommand()
        );
    }

    private void registerCommands(DiscordApi discordApi, Command... commands) {
        MusicBot.LOGGER.info("registering {} commands, this may take a while ...", Arrays.stream(commands).count());
        final Set<SlashCommandBuilder> builderSet = new HashSet<>();

        for (Command clazz : commands) {
            this.commands.put(clazz.getName(), clazz);
            builderSet.add(clazz.register(discordApi));
        }

        discordApi.bulkOverwriteGlobalApplicationCommands(builderSet).join();
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        CompletableFuture.supplyAsync(() -> {
            SlashCommandInteraction interaction = event.getSlashCommandInteraction();
            this.printCommandUsed(interaction);

            Command command = this.commands.getOrDefault(interaction.getCommandName(), null);
            if (command != null) {
                command.execute(new Either<>(interaction, interaction.getServer().orElse(null)));
            }

            return null;
        }, Threads.mainWorker()).orTimeout(60L, TimeUnit.SECONDS).exceptionally(throwable -> {
            event.getSlashCommandInteraction().createFollowupMessageBuilder()
                    .setContent("Beim Ausführen des Befehls ist ein Fehler aufgetreten:\n" + throwable.getMessage())
                    .send();

            MusicBot.LOGGER.error(throwable);
            return 0;
        });
    }

    private void printCommandUsed(SlashCommandInteraction interaction) {
        StringBuilder builder = new StringBuilder(interaction.getUser().getDiscriminatedName() + " using '/");
        builder.append(interaction.getCommandName());
        for (SlashCommandInteractionOption argument : interaction.getArguments()) {
            builder.append(" ").append(argument.getName()).append(":").append(argument.getStringRepresentationValue().orElse(""));
        }
        builder.append("'");

        MusicBot.LOGGER.info(builder);
    }
}

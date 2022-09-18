package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.commands.channels.GhostCommand;
import me.yamayaki.musicbot.interactions.commands.music.ClearCommand;
import me.yamayaki.musicbot.interactions.commands.music.ConnectCommand;
import me.yamayaki.musicbot.interactions.commands.music.DebugCommands;
import me.yamayaki.musicbot.interactions.commands.music.DisconnectCommand;
import me.yamayaki.musicbot.interactions.commands.music.HelpCommand;
import me.yamayaki.musicbot.interactions.commands.music.LoopCommand;
import me.yamayaki.musicbot.interactions.commands.music.PauseCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlayCommand;
import me.yamayaki.musicbot.interactions.commands.music.PlaylistCommand;
import me.yamayaki.musicbot.interactions.commands.music.ResumeCommand;
import me.yamayaki.musicbot.interactions.commands.music.SkipCommand;
import me.yamayaki.musicbot.interactions.commands.music.VolumeCommand;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.SlashCommandInteractionOption;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class InteractionListener implements SlashCommandCreateListener {

    private final HashMap<String, Command> commands = new HashMap<>();

    public InteractionListener(DiscordApi discordApi) {
        registerCommands(discordApi,
                new DebugCommands(),
                new ConnectCommand(),
                new DisconnectCommand(),
                new PlayCommand(),
                new VolumeCommand(),
                new PlaylistCommand(),
                new SkipCommand(),
                new ClearCommand(),
                new LoopCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new HelpCommand()
        );
    }

    private void registerCommands(DiscordApi discordApi, Command... commands) {
        MusicBot.LOGGER.info("registering {} commands, this may take a while ...", Arrays.stream(commands).count());
        final List<SlashCommandBuilder> builders = new ArrayList<>();

        for (Command clazz : commands) {
            this.commands.put(clazz.getName(), clazz);
            builders.add(clazz.register(discordApi));
        }

        discordApi.bulkOverwriteGlobalApplicationCommands(builders).join();

        //cleanup old commands
        discordApi.getGlobalSlashCommands().thenAccept(list -> {
            for (SlashCommand slashCommand : list) {
                if (this.commands.containsKey(slashCommand.getName())) {
                    return;
                }

                MusicBot.LOGGER.info("removing {} command ...", slashCommand.getName());
                slashCommand.deleteGlobal().join();
            }
        });
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        Command command = this.commands.getOrDefault(interaction.getCommandName(), null);
        if (command != null) {
            this.printCommandUsed(interaction);
            command.execute(new Either<>(interaction, interaction.getServer().orElse(null)));
        }
    }

    private void printCommandUsed(SlashCommandInteraction interaction) {
        StringBuilder builder = new StringBuilder(interaction.getUser().getDiscriminatedName() +" using '/");
        builder.append(interaction.getCommandName()).append(" ");
        for (SlashCommandInteractionOption argument : interaction.getArguments()) {
            builder.append(argument.getName()).append(":").append(argument.getStringRepresentationValue().orElse(""));
        }
        builder.append("'");

        MusicBot.LOGGER.info(builder);
    }
}

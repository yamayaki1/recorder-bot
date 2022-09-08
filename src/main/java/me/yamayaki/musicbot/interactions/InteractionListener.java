package me.yamayaki.musicbot.interactions;

import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.commands.ClearCommand;
import me.yamayaki.musicbot.interactions.commands.ConnectCommand;
import me.yamayaki.musicbot.interactions.commands.DebugCommands;
import me.yamayaki.musicbot.interactions.commands.DisconnectCommand;
import me.yamayaki.musicbot.interactions.commands.HelpCommand;
import me.yamayaki.musicbot.interactions.commands.PauseCommand;
import me.yamayaki.musicbot.interactions.commands.PlayCommand;
import me.yamayaki.musicbot.interactions.commands.PlaylistCommand;
import me.yamayaki.musicbot.interactions.commands.RepeatCommand;
import me.yamayaki.musicbot.interactions.commands.ResumeCommand;
import me.yamayaki.musicbot.interactions.commands.SkipCommand;
import me.yamayaki.musicbot.interactions.commands.VolumeCommand;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.event.interaction.SlashCommandCreateEvent;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.listener.interaction.SlashCommandCreateListener;

import java.util.Arrays;
import java.util.HashMap;

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
                new RepeatCommand(),
                new PauseCommand(),
                new ResumeCommand(),
                new HelpCommand()
        );
    }

    private void registerCommands(DiscordApi discordApi, Command... commands) {
        MusicBot.LOGGER.info("registering {} commands ...", Arrays.stream(commands).count());

        for (Command clazz : commands) {
            clazz.register(discordApi);
            this.commands.put(clazz.getName(), clazz);
        }
    }

    @Override
    public void onSlashCommandCreate(SlashCommandCreateEvent event) {
        SlashCommandInteraction interaction = event.getSlashCommandInteraction();

        Command command = this.commands.getOrDefault(interaction.getCommandName(), null);
        if (command != null) {
            command.execute(new Either<>(interaction, interaction.getServer().orElse(null)));
        }
    }
}

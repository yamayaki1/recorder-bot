package me.jerriidesu.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vdurmont.emoji.EmojiParser;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.implementations.ClearCommand;
import me.jerriidesu.musicbot.commands.implementations.ConnectCommand;
import me.jerriidesu.musicbot.commands.implementations.DebugCommands;
import me.jerriidesu.musicbot.commands.implementations.DisconnectCommand;
import me.jerriidesu.musicbot.commands.implementations.PauseCommand;
import me.jerriidesu.musicbot.commands.implementations.PlayCommand;
import me.jerriidesu.musicbot.commands.implementations.PlaylistCommand;
import me.jerriidesu.musicbot.commands.implementations.RepeatCommand;
import me.jerriidesu.musicbot.commands.implementations.ResumeCommand;
import me.jerriidesu.musicbot.commands.implementations.SkipCommand;
import me.jerriidesu.musicbot.commands.implementations.VolumeCommand;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CommandListener implements MessageCreateListener {
    private final CommandDispatcher<Either<MessageCreateEvent, Server>> commandDispatcher = new CommandDispatcher<>();

    public CommandListener() {
        registerCommands(
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
                new ResumeCommand()
        );
    }

    private void registerCommands(Command... command) {
        MusicBot.getLogger().info("registering {} commands", Arrays.stream(command).count());

        for (Command clazz : command) {
            clazz.registerBrigadier(this.commandDispatcher);
        }
    }

    @Override
    public void onMessageCreate(MessageCreateEvent source) {
        if (!source.isServerMessage() || source.getMessageAuthor().isWebhook() || source.getMessageAuthor().isBotUser() || source.getMessageContent().chars().sum() < 1 || source.getMessageContent().charAt(0) != MusicBot.getConfig().get().getBot().getPrefix()) {
            return;
        }

        try {
            this.commandDispatcher.execute(
                    source.getMessageContent().substring(1),
                    new Either<>(source, source.getServer().orElse(null))
            );
        } catch (CommandSyntaxException e) {
            source.getMessage().reply(
                    new EmbedBuilder().setColor(Color.RED).addField("Beim Ausführen des Befehls ist ein Fehler aufgetreten", e.getMessage(), false)
            ).thenAccept(message -> {
                Reactions.addTrashReaction(message);
                message.addReactionAddListener(event -> event.getReaction().ifPresent(reaction -> {
                    if (reaction.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(Reactions.DISCARD))) {
                        message.delete();
                    }
                })).removeAfter(1, TimeUnit.MINUTES);
            });
        }
    }
}

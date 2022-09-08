package me.yamayaki.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vdurmont.emoji.EmojiParser;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.commands.implementations.ClearCommand;
import me.yamayaki.musicbot.commands.implementations.ConnectCommand;
import me.yamayaki.musicbot.commands.implementations.DebugCommands;
import me.yamayaki.musicbot.commands.implementations.DisconnectCommand;
import me.yamayaki.musicbot.commands.implementations.HelpCommand;
import me.yamayaki.musicbot.commands.implementations.PauseCommand;
import me.yamayaki.musicbot.commands.implementations.PlayCommand;
import me.yamayaki.musicbot.commands.implementations.PlaylistCommand;
import me.yamayaki.musicbot.commands.implementations.RepeatCommand;
import me.yamayaki.musicbot.commands.implementations.ResumeCommand;
import me.yamayaki.musicbot.commands.implementations.SkipCommand;
import me.yamayaki.musicbot.commands.implementations.VolumeCommand;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Reactions;
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
                new ResumeCommand(),
                new HelpCommand()
        );
    }

    private void registerCommands(Command... command) {
        MusicBot.getLogger().info("registering {} commands", Arrays.stream(command).count());

        for (Command clazz : command) {
            clazz.register(this.commandDispatcher);
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

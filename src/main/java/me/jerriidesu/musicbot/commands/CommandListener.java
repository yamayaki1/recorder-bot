package me.jerriidesu.musicbot.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.vdurmont.emoji.EmojiParser;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.implementations.ConnectCommand;
import me.jerriidesu.musicbot.commands.implementations.DisconnectCommand;
import me.jerriidesu.musicbot.commands.implementations.PlayCommand;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;
import org.javacord.api.listener.message.MessageCreateListener;

import java.awt.Color;
import java.util.Arrays;
import java.util.concurrent.TimeUnit;

public class CommandListener implements MessageCreateListener {

    private final CommandDispatcher<MessageCreateEvent> commandDispatcher = new CommandDispatcher<>();

    public CommandListener() {
        registerCommands(
                new ConnectCommand(),
                new DisconnectCommand(),
                new PlayCommand()
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
        if(!source.isServerMessage() || source.getMessageAuthor().isWebhook() || source.getMessageAuthor().isBotUser() || source.getMessageContent().chars().sum() < 1 || source.getMessageContent().charAt(0) != MusicBot.getConfig().get().getBot().getPrefix()) {
            return;
        }

        try {
            this.commandDispatcher.execute(source.getMessageContent().substring(1), source);
        } catch (CommandSyntaxException e) {
            Reactions.addFailureReaction(source.getMessage());

            source.getMessage().reply(
                    new EmbedBuilder().setColor(Color.RED).addField("Beim Ausführen des Befehls ist ein Fehler aufgetreten", e.getMessage(), false)
            ).thenAccept(message -> {
                Reactions.addTrashReaction(message);
                message.addReactionAddListener(event -> event.getReaction().ifPresent(reaction -> {
                    if(reaction.getEmoji().equalsEmoji(EmojiParser.parseToUnicode(Reactions.DISCARD))) {
                        message.delete();
                    }
                })).removeAfter(1, TimeUnit.MINUTES);
            });
        }
    }
}

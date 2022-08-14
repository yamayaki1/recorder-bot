package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class DisconnectCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("disconnect").executes(context -> {
            //execute
            context.getSource().getRight().getAudioConnection().ifPresentOrElse(audioConnection -> {
                MusicBot.getAudioManager().removeTrackManager(context.getSource().getRight());
                audioConnection.close();
                Reactions.addSuccessfulReaction(context.getSource().getLeft().getMessage());
            }, () -> Reactions.addFailureReaction(context.getSource().getLeft().getMessage()));

            return 1;
        }));
    }
}

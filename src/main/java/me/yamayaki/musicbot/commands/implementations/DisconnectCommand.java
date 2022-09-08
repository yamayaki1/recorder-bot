package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Reactions;
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

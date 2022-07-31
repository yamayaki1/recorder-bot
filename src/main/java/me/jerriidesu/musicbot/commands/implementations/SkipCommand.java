package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class SkipCommand implements Command {

    @Override
    public void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("skip")
                .then(RequiredArgumentBuilder.<Either<MessageCreateEvent, Server>, Integer>argument("count", IntegerArgumentType.integer(1)).executes(context -> {
                    //execute
                    int amount = IntegerArgumentType.getInteger(context, "count");
                    skipTracks(context.getSource(), amount);
                    return 1;
                })).executes(context -> {
                    //execute
                    skipTracks(context.getSource(), 1);
                    return 1;
                })
        );
    }

    private void skipTracks(Either<MessageCreateEvent, Server> context, int amount) {
        int max_amount = MusicBot.getAudioManager()
                .getTrackManager(context.getRight())
                .getTrackList().size();

        if(amount > max_amount) {
            Reactions.addFailureReaction(context.getLeft().getMessage());
            return;
        }

        MusicBot.getAudioManager()
                .getTrackManager(context.getRight())
                .skipTracks(amount);

        Reactions.addSuccessfullReaction(context.getLeft().getMessage());
    }
}

package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Reactions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class SkipCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
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
        TrackManager manager = MusicBot.getAudioManager()
                .getTrackManager(context.getRight());

        int max_amount = manager.getTracks().size();

        if (amount > max_amount && manager.hasFinished()) {
            Reactions.addFailureReaction(context.getLeft().getMessage());
            return;
        }

        MusicBot.getAudioManager()
                .getTrackManager(context.getRight())
                .skipTrack(amount);

        Reactions.addSuccessfulReaction(context.getLeft().getMessage());
    }
}

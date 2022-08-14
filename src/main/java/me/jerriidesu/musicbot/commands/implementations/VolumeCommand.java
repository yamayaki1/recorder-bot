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

public class VolumeCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("volume")
                .then(RequiredArgumentBuilder.<Either<MessageCreateEvent, Server>, Integer>argument("vol", IntegerArgumentType.integer(0, 200)).executes(context -> {
                    //execute
                    int volume = IntegerArgumentType.getInteger(context, "vol");
                    setVolume(context.getSource(), volume);
                    return 1;
                })).executes(context -> {
                    //execute
                    Reactions.addRefuseReaction(context.getSource().getLeft().getMessage());
                    return 1;
                })
        );
    }

    private void setVolume(Either<MessageCreateEvent, Server> context, int volume) {
        MusicBot.getAudioManager()
                .getTrackManager(context.getRight())
                .setVolume(volume);

        Reactions.addSuccessfulReaction(context.getLeft().getMessage());
    }
}

package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Reactions;
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

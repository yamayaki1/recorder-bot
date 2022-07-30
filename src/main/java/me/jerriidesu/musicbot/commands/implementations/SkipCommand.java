package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.event.message.MessageCreateEvent;

public class SkipCommand implements Command {

    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("skip")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, Integer>argument("count", IntegerArgumentType.integer(1)).executes(context -> {
                    int max_amount = MusicBot.getPlaylistManager().getTrackList().size();
                    int amount = IntegerArgumentType.getInteger(context, "count");

                    if(amount > max_amount) {
                        Reactions.addFailureReaction(context.getSource().getMessage());
                        return 1;
                    }

                    MusicBot.getPlaylistManager().skipTrack(amount);
                    Reactions.addSuccessfullReaction(context.getSource().getMessage());
                    return 1;
                })).executes(context -> {
                    MusicBot.getPlaylistManager().skipTrack(1);
                    Reactions.addSuccessfullReaction(context.getSource().getMessage());
                    return 1;
                })
        );
    }
}

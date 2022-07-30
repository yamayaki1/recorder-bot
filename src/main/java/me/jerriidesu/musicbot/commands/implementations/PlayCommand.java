package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.event.message.MessageCreateEvent;

public class PlayCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("play")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, String>argument("song", StringArgumentType.greedyString()).executes(context -> {
                    context.getSource().getServer().ifPresent(server -> {
                        server.getAudioConnection().ifPresentOrElse(audioConnection -> {
                            addSong(context.getSource(), StringArgumentType.getString(context, "song"));
                        }, () -> Reactions.addRefuseReaction(context.getSource().getMessage()));
                    });

                    return 0;
                })).executes(context -> {
                    Reactions.addRefuseReaction(context.getSource().getMessage());
                    return 0;
                })
        );
    }

    private void addSong(MessageCreateEvent source, String song) {
        MusicBot.getPlaylistManager().addItems(song, playerResponse -> {
            source.getMessage().removeEmbed();

            if(playerResponse) {
                Reactions.addSuccessfullReaction(source.getMessage());
            } else {
                Reactions.addFailureReaction(source.getMessage());
            }
        });
    }
}

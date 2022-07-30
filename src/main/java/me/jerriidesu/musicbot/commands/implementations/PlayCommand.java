package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

import java.awt.Color;

public class PlayCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("play")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, String>argument("song", StringArgumentType.string()).executes(context -> {
                    context.getSource().getServer().ifPresent(server -> {
                        server.getAudioConnection().ifPresentOrElse(audioConnection -> {
                            audioConnection.setAudioSource(MusicBot.getPlaylistManager().getAudioSource());
                            addSong(context.getSource(), StringArgumentType.getString(context, "song"));
                        }, () -> this.sendRefused(context.getSource()));
                    });

                    return 0;
                })).executes(context -> {
                    this.sendRefused(context.getSource());
                    return 0;
                })
        );
    }

    private void sendRefused(MessageCreateEvent source) {
        Reactions.addRefuseReaction(source.getMessage());
    }

    private void addSong(MessageCreateEvent source, String song) {
        MusicBot.getPlaylistManager().addItems(song, playerResponse -> {
            if(playerResponse.isSuccess()) {
                Reactions.addSuccessfullReaction(source.getMessage());
                source.getMessage().reply(
                        new EmbedBuilder()
                                .setColor(Color.GREEN)
                                .setDescription(playerResponse.getMessage())
                );
            } else {
                Reactions.addFailureReaction(source.getMessage());
                source.getMessage().reply(
                        new EmbedBuilder()
                                .setColor(Color.RED)
                                .setDescription(playerResponse.getMessage())
                );
            }
        });
    }
}

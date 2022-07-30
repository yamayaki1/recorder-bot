package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.event.message.MessageCreateEvent;

public class DebugCommands implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("debug")
                .then(LiteralArgumentBuilder.<MessageCreateEvent>literal("isplaying")
                        .executes(context -> {
                            context.getSource().getMessage().reply(String.valueOf(!MusicBot.getPlaylistManager().getAudioSource().hasFinished()));
                            return 0;
                        })
                )
                .then(LiteralArgumentBuilder.<MessageCreateEvent>literal("current")
                        .executes(context -> {
                            context.getSource().getMessage().reply(MusicBot.getPlaylistManager().getAudioSource().getAudioPlayer().getPlayingTrack().getInfo().title);
                            return 0;
                        })
                )
                .then(LiteralArgumentBuilder.<MessageCreateEvent>literal("position")
                        .executes(context -> {
                            context.getSource().getMessage().reply(String.valueOf(MusicBot.getPlaylistManager().getAudioSource().getAudioPlayer().getPlayingTrack().getPosition()));
                            return 0;
                        })
                )
                .then(LiteralArgumentBuilder.<MessageCreateEvent>literal("fix")
                        .executes(context -> {
                            fixAudioSource(context.getSource());
                            return 0;
                        })
                ).then(LiteralArgumentBuilder.<MessageCreateEvent>literal("version")
                        .executes(context -> {
                            context.getSource().getMessage().reply(MusicBot.getConfig().getBotVersion());
                            return 0;
                        })
                ).executes(context -> {
                    Reactions.addRefuseReaction(context.getSource().getMessage());
                    return 0;
                })
        );
    }

    public static void fixAudioSource(MessageCreateEvent context) {
        context.getServer().ifPresent(server -> {
            server.getAudioConnection().ifPresent(audioConnection -> {
                audioConnection.setAudioSource(MusicBot.getPlaylistManager().getAudioSource());
            });
        });
    }
}

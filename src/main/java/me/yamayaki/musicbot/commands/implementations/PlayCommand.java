package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.audio.TrackManager;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import me.yamayaki.musicbot.utils.Reactions;
import me.yamayaki.musicbot.utils.StringTools;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class PlayCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("play")
                .then(RequiredArgumentBuilder.<Either<MessageCreateEvent, Server>, String>argument("song", StringArgumentType.greedyString()).executes(context -> {
                    //execute
                    //TODO unfuck this garbage
                    context.getSource().getRight().getAudioConnection().ifPresentOrElse(audioConnection -> {
                        addSong(context.getSource(), StringArgumentType.getString(context, "song"));
                    }, () -> Reactions.addRefuseReaction(context.getSource().getLeft().getMessage()));

                    return 1;
                })).executes(context -> {
                    //execute
                    TrackManager trackManager = MusicBot
                            .getAudioManager()
                            .getTrackManager(context.getSource().getRight());

                    if (trackManager.paused) {
                        trackManager.resumeTrack();
                        Reactions.addSuccessfulReaction(context.getSource().getLeft().getMessage());
                    } else {
                        Reactions.addRefuseReaction(context.getSource().getLeft().getMessage());
                    }

                    return 1;
                })
        );
    }

    //TODO unfuck this garbage
    private void addSong(Either<MessageCreateEvent, Server> context, String song) {
        if (!StringTools.isURL(song) && !song.contains("ytmsearch:") && !song.contains("ytsearch:")) {
            song = "ytmsearch:" + song;
        }

        MusicBot.getAudioManager().getTrackManager(context.getRight()).tryLoadItems(song, playerResponse -> {
            if (playerResponse) {
                Reactions.addSuccessfulReaction(context.getLeft().getMessage());
            } else {
                Reactions.addFailureReaction(context.getLeft().getMessage());
            }
        });
    }
}

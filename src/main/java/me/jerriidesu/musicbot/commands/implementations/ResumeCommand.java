package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class ResumeCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("resume").executes(context -> {
            //execute
            MusicBot.getAudioManager()
                    .getTrackManager(context.getSource().getRight())
                    .resumeTrack();

            Reactions.addSuccessfulReaction(context.getSource().getLeft().getMessage());
            return 1;
        }));
    }
}

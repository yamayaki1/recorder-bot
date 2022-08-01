package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class RepeatCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("repeat").executes(context -> {
            //execute
            boolean repeat = MusicBot.getAudioManager()
                    .getTrackManager(context.getSource().getRight())
                    .toggleRepeat();

            if (repeat) {
                context.getSource().getLeft()
                        .getMessage().reply(new EmbedBuilder().setDescription("Wiederholen eingeschaltet."));
            } else {
                context.getSource().getLeft()
                        .getMessage().reply(new EmbedBuilder().setDescription("Wiederholen ausgeschaltet."));
            }

            Reactions.addSuccessfulReaction(context.getSource().getLeft().getMessage());
            return 1;
        }));
    }
}
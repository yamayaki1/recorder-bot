package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class ConnectCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("connect").executes(context -> {
            //execute
            MessageCreateEvent event = context.getSource().getLeft();

            event.getMessageAuthor().getConnectedVoiceChannel().ifPresentOrElse(serverVoiceChannel -> {
                this.joinChannel(event, serverVoiceChannel);
            }, ()-> this.sendError(event));

            return 0;
        }));
    }

    private void joinChannel(MessageCreateEvent event, ServerVoiceChannel serverVoiceChannel) {
        serverVoiceChannel.connect(false, false).thenAccept(audioConnection -> {
            Reactions.addSuccessfulReaction(event.getMessage());
        });
    }

    private void sendError(MessageCreateEvent event) {
        Reactions.addFailureReaction(event.getMessage());
    }
}

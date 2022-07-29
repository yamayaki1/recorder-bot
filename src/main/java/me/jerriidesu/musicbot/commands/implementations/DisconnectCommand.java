package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.audio.AudioConnection;
import org.javacord.api.event.message.MessageCreateEvent;

public class DisconnectCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("disconnect").executes(context -> {
            context.getSource().getServer().ifPresent(server -> {
                server.getAudioConnection().ifPresentOrElse(audioConnection -> {
                    this.disconnectChannel(context.getSource(), audioConnection);
                }, () -> this.sendError(context.getSource()));
            });
            return 0;
        }));
    }

    private void disconnectChannel(MessageCreateEvent event, AudioConnection audioConnection) {
        audioConnection.close().thenRun(() -> {
            Reactions.addSuccessfullReaction(event.getMessage());
        });
    }

    private void sendError(MessageCreateEvent event) {
        Reactions.addFailureReaction(event.getMessage());
    }
}

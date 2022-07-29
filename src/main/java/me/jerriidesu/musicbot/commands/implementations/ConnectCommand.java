package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.channel.ServerVoiceChannel;
import org.javacord.api.event.message.MessageCreateEvent;

public class ConnectCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("connect").executes(context -> {
            MessageCreateEvent event = context.getSource();

            event.getMessageAuthor().getConnectedVoiceChannel().ifPresentOrElse(serverVoiceChannel -> {
                this.joinChannel(event, serverVoiceChannel);
            }, ()-> this.sendError(event));

            return 0;
        }));
    }

    private void joinChannel(MessageCreateEvent event, ServerVoiceChannel serverVoiceChannel) {
        serverVoiceChannel.connect(false, true).thenAccept(audioConnection -> {
            audioConnection.setAudioSource(MusicBot.getPlaylistManager().getAudioSource());
            Reactions.addSuccessfullReaction(event.getMessage());
        });
    }

    private void sendError(MessageCreateEvent event) {
        Reactions.addFailureReaction(event.getMessage());
    }
}

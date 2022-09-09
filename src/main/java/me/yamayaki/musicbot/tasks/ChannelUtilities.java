package me.yamayaki.musicbot.tasks;

import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommandInteraction;

public class ChannelUtilities {
    public static void joinVoiceChannel(Either<SlashCommandInteraction, Server> either, Runnable onSuccess, Runnable onError) {
        var optChannel = either.getLeft().getUser()
                .getConnectedVoiceChannel(either.getRight());
        var curChannel = either.getRight()
                .getAudioConnection();

        if(curChannel.isPresent() && curChannel.get().getChannel().equals(optChannel.orElse(null))) {
            onSuccess.run();
            return;
        }

        optChannel.ifPresentOrElse(voiceChannel -> {
            voiceChannel.connect().thenAccept(audioConnection -> {
                audioConnection.setSelfDeafened(true);
                onSuccess.run();
            });
        }, onError);
    }
}

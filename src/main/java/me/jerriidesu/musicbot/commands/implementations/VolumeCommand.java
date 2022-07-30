package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.event.message.MessageCreateEvent;

public class VolumeCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("volume")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, Integer>argument("vol", IntegerArgumentType.integer(0, 100)).executes(context -> {
                    MusicBot.getPlaylistManager().getAudioSource().getAudioPlayer().setVolume(IntegerArgumentType.getInteger(context, "vol"));
                    Reactions.addSuccessfullReaction(context.getSource().getMessage());
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
}

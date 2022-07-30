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
                .then(RequiredArgumentBuilder.<MessageCreateEvent, Integer>argument("vol", IntegerArgumentType.integer(0, 50)).executes(context -> {
                    int volume = IntegerArgumentType.getInteger(context, "vol");
                    setVolume(context.getSource(), volume);
                    return 0;
                })).executes(context -> {
                    Reactions.addRefuseReaction(context.getSource().getMessage());
                    return 0;
                })
        );

        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("volume_allow_ear_rape")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, Integer>argument("vol", IntegerArgumentType.integer(0)).executes(context -> {
                    int volume = IntegerArgumentType.getInteger(context, "vol");
                    setVolume(context.getSource(), Math.min(volume, 150));
                    return 0;
                })).executes(context -> {
                    Reactions.addRefuseReaction(context.getSource().getMessage());
                    return 0;
                })
        );
    }

    private void setVolume(MessageCreateEvent context, int volume) {
        MusicBot.getPlaylistManager()
                .getAudioSource()
                .getAudioPlayer()
                .setVolume(volume);

        Reactions.addSuccessfullReaction(context.getMessage());
    }
}

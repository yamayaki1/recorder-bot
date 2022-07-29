package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.event.message.MessageCreateEvent;

public class PlayCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("disconnect")
                .then(RequiredArgumentBuilder.<MessageCreateEvent, String>argument("song", StringArgumentType.string()).executes(context -> {

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

package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Reactions;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class RepeatCommand implements Command {

    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("repeat").executes(context -> {
            boolean repeat = MusicBot.getPlaylistManager().toggleRepeat();
            if(repeat) {
                context.getSource().getMessage().reply(
                        new EmbedBuilder().setDescription("Wiederholen eingeschaltet.")
                );
            } else {
                context.getSource().getMessage().reply(
                        new EmbedBuilder().setDescription("Wiederholen ausgeschaltet.")
                );
            }

            Reactions.addSuccessfullReaction(context.getSource().getMessage());
            return 1;
        }));
    }
}

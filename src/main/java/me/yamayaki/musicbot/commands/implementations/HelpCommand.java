package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

public class HelpCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("help").executes(context -> {
            //execute
            String prefix = String.valueOf(MusicBot.getConfig().get().getBot().getPrefix());

            context.getSource().getLeft().getMessage().reply(
                    new EmbedBuilder().setTitle("Befehlsübersicht")
                            .addField("Bot verbinden", prefix + "connect")
                            .addField("Bot trennen", prefix + "disconnect")
                            .addField("Musik abspielen", prefix + "play <spotify*|youtube|twitch|soundcloud|search>")
                            .addField("Music pausieren*", prefix + "pause")
                            .addField("Music fortsetzen*", prefix + "resume")
                            .addField("Music überspringen*", prefix + "skip [track-id]")
                            .addField("Lautstärke ändern", prefix + "volume <0-200>")
                            .addField("Playlist anzeigen*", prefix + "playlist")
                            .addField("Playlist wiederholen*", prefix + "repeat")
                            .addField("Playlist leeren", prefix + "clear")
                            .addField("*", "Indev-Funktion")
            );
            return 1;
        }));
    }
}

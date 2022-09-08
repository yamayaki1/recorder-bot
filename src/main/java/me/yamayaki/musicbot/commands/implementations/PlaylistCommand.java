package me.yamayaki.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.commands.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

public class PlaylistCommand implements Command {
    @Override
    public void register(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("playlist").executes(context -> {
            //execute
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Playlist");

            List<AudioTrack> tracks = MusicBot.getAudioManager()
                    .getTrackManager(context.getSource().getRight())
                    .getTracks();

            if (tracks.size() > 0) {
                for (int i = 0; i < tracks.size(); i++) {
                    AudioTrack audioTrack = tracks.get(i);
                    embedBuilder.addField((i + 1) + ". " + audioTrack.getInfo().title, audioTrack.getInfo().author);
                }
            } else {
                embedBuilder.setDescription("Die Playlist ist leer.");
            }

            context.getSource().getLeft().getMessage().reply(embedBuilder);
            return 1;
        }));
    }
}

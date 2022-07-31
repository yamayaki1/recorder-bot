package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import me.jerriidesu.musicbot.utils.Either;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.event.message.MessageCreateEvent;

import java.util.List;

public class PlaylistCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<Either<MessageCreateEvent, Server>> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<Either<MessageCreateEvent, Server>>literal("playlist").executes(context -> {
            //execute
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Playlist");

            List<AudioTrack> tracks = MusicBot.getAudioManager()
                    .getTrackManager(context.getSource().getRight())
                    .getTrackList();

            int index = 0;
            for (AudioTrack track : tracks) {
                index++;
                embedBuilder.addField(index+". "+ track.getInfo().title, track.getInfo().author);
            }

            context.getSource().getLeft().getMessage().reply(embedBuilder);
            return 0;
        }));
    }
}

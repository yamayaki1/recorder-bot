package me.jerriidesu.musicbot.commands.implementations;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.jerriidesu.musicbot.MusicBot;
import me.jerriidesu.musicbot.commands.Command;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.event.message.MessageCreateEvent;

public class PlaylistCommand implements Command {
    @Override
    public void registerBrigadier(CommandDispatcher<MessageCreateEvent> dispatcher) {
        dispatcher.register(LiteralArgumentBuilder.<MessageCreateEvent>literal("playlist").executes(context -> {
            EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Playlist");

            int index = 0;
            for (AudioTrack track : MusicBot.getPlaylistManager().getTrackList()) {
                index++;
                embedBuilder.addField(index+". "+ track.getInfo().title, track.getInfo().author);
            }

            context.getSource().getMessage().reply(embedBuilder);
            return 0;
        }));
    }
}

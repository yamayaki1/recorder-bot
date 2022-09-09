package me.yamayaki.musicbot.interactions.commands;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.Command;
import me.yamayaki.musicbot.utils.Either;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.entity.server.Server;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;

import java.util.List;

public class PlaylistCommand implements Command {
    @Override
    public String getName() {
        return "playlist";
    }

    @Override
    public SlashCommandBuilder register(DiscordApi api) {
        return SlashCommand.with(getName(), "Zeigt die aktuelle Playlist.")
                .setEnabledInDms(false);
    }

    @Override
    public void execute(Either<SlashCommandInteraction, Server> either) {
        var interUpdater = either.getLeft().respondLater(true).join();
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Playlist");

        List<AudioTrack> tracks = MusicBot.getAudioManager()
                .getTrackManager(either.getRight())
                .getPlaylist()
                .getTracks(true);

        if (tracks.size() > 0) {
            for (int i = 0; i < tracks.size(); i++) {
                AudioTrackInfo trackInfo = tracks.get(i).getInfo();
                embedBuilder.addField((i + 1) + ". " + trackInfo.title, trackInfo.author.replaceAll("- Topic", ""));
            }
        } else {
            embedBuilder.setDescription("Die Playlist ist leer.");
        }

        interUpdater.addEmbed(embedBuilder).update();
    }
}

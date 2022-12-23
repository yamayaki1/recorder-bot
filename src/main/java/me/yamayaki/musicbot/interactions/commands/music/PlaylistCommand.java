package me.yamayaki.musicbot.interactions.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandBuilder;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

import java.util.List;

public class PlaylistCommand implements ApplicationInteraction {
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
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater updater) {
        EmbedBuilder embedBuilder = new EmbedBuilder().setTitle("Playlist");

        List<AudioTrack> tracks = MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow())
                .getPlaylist()
                .getTracks(true);

        if (tracks.size() > 0) {
            for (int i = 0; i < Math.min(24, tracks.size()); i++) {
                AudioTrackInfo trackInfo = tracks.get(i).getInfo();
                embedBuilder.addField(i + ". " + trackInfo.title, trackInfo.author.replaceAll("- Topic", ""));
            }
        } else {
            embedBuilder.setDescription("Die Playlist ist leer.");
        }

        updater.addEmbed(embedBuilder).update();
    }
}

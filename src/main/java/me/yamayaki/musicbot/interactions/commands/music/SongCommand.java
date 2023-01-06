package me.yamayaki.musicbot.interactions.commands.music;

import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import me.yamayaki.musicbot.MusicBot;
import me.yamayaki.musicbot.entities.SpotifyTrack;
import me.yamayaki.musicbot.interactions.ApplicationInteraction;
import me.yamayaki.musicbot.utilities.CommonUtils;
import org.javacord.api.DiscordApi;
import org.javacord.api.entity.message.embed.EmbedBuilder;
import org.javacord.api.interaction.ApplicationCommandBuilder;
import org.javacord.api.interaction.SlashCommand;
import org.javacord.api.interaction.SlashCommandInteraction;
import org.javacord.api.interaction.callback.InteractionOriginalResponseUpdater;

public class SongCommand implements ApplicationInteraction {
    @Override
    public String getName() {
        return "song";
    }

    @Override
    public ApplicationCommandBuilder<?, ?, ?> register(DiscordApi api) {
        return SlashCommand.with(getName(), "Zeige den aktuellen Song.")
                .setEnabledInDms(false);
    }

    @Override
    public void executeCommand(SlashCommandInteraction interaction, InteractionOriginalResponseUpdater response) {
        var trackManager = MusicBot.instance()
                .getAudioManager(interaction.getServer().orElseThrow());

        if (trackManager.hasFinished() || trackManager.getPlaylist().current() == null) {
            response.setContent("Es spielt aktuell kein Lied.").update();
            return;
        }

        AudioTrack audioTrack = trackManager.getPlaylist()
                .current();

        SpotifyTrack spotifyData = audioTrack.getUserData(SpotifyTrack.class);

        EmbedBuilder replyEmbed = new EmbedBuilder()
                .setImage(spotifyData != null ? spotifyData.image() : CommonUtils.getThumbnail(audioTrack.getIdentifier()))
                .addField("Titel", audioTrack.getInfo().title)
                .addField("Künstler", audioTrack.getInfo().author)
                .setTimestampToNow();

        response.addEmbed(replyEmbed).update();
    }
}
